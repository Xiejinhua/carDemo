package com.desaysv.psmap.base.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.os.Environment
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.user.usertrack.model.HistoryRouteItem
import com.autosdk.BuildConfig
import com.autosdk.bussiness.account.UserTrackController
import com.autosdk.bussiness.widget.route.constant.ConfigRoutePreference
import com.desaysv.psmap.base.R
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


object CommonUtils {

    /**
     * 是否是真实车辆
     */
    fun isVehicle(): Boolean {
        return BuildConfig.autoDevicesType == 1
    }

    /**
     * 获取车机环境类型(模拟器默认测试环境)
     * @return 0正式环境；非0 测试环境
     */
    fun isProdEnvironment(): Boolean {
        val env = getSystemProperties("persist.sys.tsp.env", "0")
        Timber.i("isBaseEnvironment $env")
        return env == "0"
    }

    /**
     * 是否启动账号绑定
     */
    fun isUseVehicleAccount(): Boolean {
        return com.desaysv.psmap.base.BuildConfig.useVehicleAccount
    }

    /**
     * 获取dp转化成int值
     */
    fun getAutoDimenValue(mContext: Context?, resId: Int): Int {
        return mContext?.resources?.getDimension(resId)?.toInt() ?: 0
    }

    /**
     * 获取dp转出sp再转化成int值
     */
    fun getAutoDimenValueSP(mContext: Context?, resId: Int): Float {
        val pxValue = mContext?.resources?.getDimension(resId)?.toInt() ?: 0
        val scaledDensity = Resources.getSystem().displayMetrics.scaledDensity
        return pxValue / scaledDensity
    }

    /***
     * 获取私有成员变量的值
     *
     */
    fun <F> getPrivate(instance: Any, name: String): F? {
        var clazz: Class<*> = instance.javaClass
        while (clazz != Any::class.java) {
            //向上循环 遍历父类
            for (f in clazz.declaredFields) {
                f.isAccessible = true
                if (f.name == name) (f[instance] as? F)?.also { return it }
            }
            clazz = clazz.superclass
        }
        return null
    }

    /**
     * 根据距离长度判断距离单位及返回数据
     * @param distance
     * @return
     */
    fun showDistance(distance: Double): String {
        val distanceStr = if (distance < 1000.00) {
            String.format("%.2f米", distance)
        } else {
            String.format("%.2f公里", distance / 1000)
        }
        return distanceStr
    }

    /**
     * 根据距离长度判断距离及返回数据
     * @param distance
     * @return
     */
    fun getDistanceKm(distance: Double): String {
        val distanceStr = String.format("%.1f", distance / 1000)
        return distanceStr
    }

    fun secondsToHours(seconds: Double): String {
        val hours = seconds / 3600.0
        return String.format("%.1f", hours) // 保留1位小数
    }

    fun getDistanceStr(context: Context, distance: Double): String {
        val distanceStr = if (distance <= 0.00) {
            ""
        } else if (distance < 1000.00) {
            distance.toInt().toString() + context.getString(R.string.sv_common_meter)
        } else {
            String.format("%.2f" + context.getString(R.string.sv_common_km), distance / 1000)
        }
        return distanceStr
    }

    fun calcDistanceBetweenPoints(context: Context, startP: Coord2DDouble, endP: Coord2DDouble): String {
        val calcDistanceBetweenPoints = BizLayerUtil.calcDistanceBetweenPoints(startP, endP)
        return getDistanceStr(context, calcDistanceBetweenPoints)
    }

    fun calcDistance(startP: Coord2DDouble, endP: Coord2DDouble): Double {
        return BizLayerUtil.calcDistanceBetweenPoints(startP, endP)
    }

    /**
     * 获取时间字符串
     *
     * @param second
     * @return
     */
    fun getTimeStr(mContext: Context, second: Long): String {
        val minute = ((second + 30) / 60).toInt()
        val resources = mContext.resources
        val restTime: String = if (minute < 60) {
            //小于1小时
            val displayMinute = if (minute == 0) 1 else minute
            "$displayMinute${resources.getString(R.string.sv_route_minutes)}"
        } else {
            //小于1天
            val hour = minute / 60
            var displayTime = "$hour${resources.getString(R.string.sv_route_hour)}"
            val remainingMinutes = minute % 60
            if (remainingMinutes > 0) {
                displayTime += "$remainingMinutes${resources.getString(R.string.sv_route_minutes)}"
            }
            displayTime
        }
        return restTime
    }

    /**
     * 按照固定的策略取整距离数值，与TBT保持一致
     * 1）10公里级别向下取整；
     * 2）1公里级别的四舍五入；
     * 3）1公里以下的暂不修改。
     *
     * @param dis
     * @return
     */
    fun routeResultDistanceEnglish(mContext: Context, dis: Long): String {
        val sb = StringBuilder()
        var distance = dis.toInt()
        when {
            distance >= 10000 -> distance = distance / 1000 * 1000
            distance >= 1000 -> distance = (distance + 50) / 100 * 100
        }
        if (distance >= 1000) {
            val kiloMeter = distance / 1000
            var leftMeter = distance % 1000 / 100
            sb.append(if (leftMeter > 0) "$kiloMeter.$leftMeter" else kiloMeter)
            sb.append(mContext.resources.getString(R.string.sv_common_km))
        } else {
            sb.append(distance)
            sb.append(mContext.resources.getString(R.string.sv_common_meter))
        }
        return sb.toString()
    }

    /**
     * 获取时间和公里数
     *
     * @param second
     * @return
     */
    fun getTimeAndKilometersStr(mContext: Context, second: Long, dis: Long): String {
        val minute = ((second + 30) / 60).toInt()
        val resources = mContext.resources
        val restTime: String = if (minute < 60) {
            //小于1小时
            val displayMinute = if (minute == 0) 1 else minute
            "$displayMinute${resources.getString(R.string.sv_route_minutes)}"
        } else {
            //小于1天
            val hour = minute / 60
            var displayTime = "$hour${resources.getString(R.string.sv_route_hour)}"
            val remainingMinutes = minute % 60
            if (remainingMinutes > 0) {
                displayTime += "$remainingMinutes${resources.getString(R.string.sv_route_minutes)}"
            }
            displayTime
        }
        val sb = StringBuilder()
        var distance = dis.toInt()
        when {
            distance >= 10000 -> distance = distance / 1000 * 1000
            distance >= 1000 -> distance = (distance + 50) / 100 * 100
        }
        if (distance >= 1000) {
            val kiloMeter = distance / 1000
            var leftMeter = distance % 1000 / 100
            sb.append(if (leftMeter > 0) "$kiloMeter.$leftMeter" else kiloMeter)
            sb.append(mContext.resources.getString(R.string.sv_common_km))
        } else {
            sb.append(distance)
            sb.append(mContext.resources.getString(R.string.sv_common_meter))
        }
        return "$sb · $restTime"
    }

    /**
     * 导航记录去掉重复
     */
    fun removeDuplicateHistoryRoute(list: ArrayList<HistoryRouteItem>?): ArrayList<HistoryRouteItem>? {
        if (list != null && list.size > 0) {
            for (i in 0 until list.size - 1) {
                for (j in list.size - 1 downTo i + 1) {
                    if (list[j].toPoi.poiId == list[i].toPoi.poiId && list[j].toPoi.name == list[i].toPoi.name) {
                        UserTrackController.getInstance().delHistoryRoute(list[i], SyncMode.SyncModeNow)
                        list.removeAt(j)
                    }
                }
            }
        }
        return list
    }


    fun getScaleLineLengthDesc(mContext: Context, scale: Int): String {
        val desc: String = if (scale % 1000 == 0) {
            val kmScale = scale / 1000
            kmScale.toString() + mContext.getString(R.string.sv_common_km)
        } else {
            scale.toString() + mContext.getString(R.string.sv_common_meter)
        }
        return desc
    }

    @SuppressLint("PrivateApi")
    fun getSystemProperties(key: String?, def: String?): String? {
        return try {
            val forName = Class.forName("android.os.SystemProperties")
            forName.getMethod("get", String::class.java, String::class.java).invoke(forName, key, def) as String
        } catch (e: Exception) {
            Timber.e("Exception:${e.message}")
            def
        }
    }

    @SuppressLint("PrivateApi")
    fun setSystemProperties(key: String?, value: String?) {
        try {
            val c = Class.forName("android.os.SystemProperties")
            val set = c.getMethod("set", String::class.java, String::class.java)
            set.invoke(c, key, value)
        } catch (e: Exception) {
            Timber.e("Exception:${e.message}")
        }
    }

    /**
     * 距离现在时间 参数单位 秒
     */
    fun strTimeFromNow(context: Context, time: Long): String {
        val timeDist: Long = System.currentTimeMillis() / 1000 - time
        val totalMin = (timeDist / 60).toInt()
        val hour = totalMin / 60
        val updateTime: String = if (hour > 24) {
            context.getString(R.string.sv_common_time_day, hour / 24)
        } else {
            secondToStr(context, timeDist)
        }
        return updateTime
    }

    fun secondToStr(context: Context, travelTime: Long): String {
        val totalMin = (travelTime / 60).toInt()
        val hour = totalMin / 60
        val min = totalMin % 60
        if (hour == 0) {
            return context.getString(R.string.sv_common_time_minute, min)
        }
        return if (min == 0) context.getString(R.string.sv_common_time_hour, hour) else context.getString(
            R.string.sv_common_time_hour_minute,
            hour,
            min
        )
    }


    /**
     * 将时间戳转换成描述性时间（昨天、今天、明天）
     *
     * @return 描述性日期
     */
    open fun descriptiveData(dateStr: String?): String? {
        var descriptiveText = dateStr
        //当前时间
        val currentTime = Calendar.getInstance()
        //要转换的时间
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        var date: Date? = null
        try {
            date = sdf.parse(dateStr)
        } catch (e: ParseException) {
            Timber.d("ParseException:%s", e.message)
        }
        val calendar = Calendar.getInstance()
        calendar.time = date
        //年相同
        if (currentTime[Calendar.YEAR] == calendar[Calendar.YEAR]) {
            //获取一年中的第几天并相减，取差值
            val difference = currentTime[Calendar.DAY_OF_YEAR] - calendar[Calendar.DAY_OF_YEAR]
            if (difference == 1) { //当前比目标多一天，那么目标就是昨天
                descriptiveText = "昨天"
            } else if (difference == 0) { //当前和目标是同一天，就是今天
                descriptiveText = "今天"
            }
        }
        return descriptiveText
    }

    //Bitmap压缩
    fun imageZoom(bitMap: Bitmap): Bitmap {
        //图片允许最大空间   单位：KB
        val maxSize = 5.00
        //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
        val baos = ByteArrayOutputStream()
        bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        //将字节换成KB
        val mid = (b.size / 1024).toDouble()
        //判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
        if (mid > maxSize) {
            //获取bitmap大小 是允许最大大小的多少倍
            val i = mid / maxSize
            //开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
            return zoomImage(
                bitMap, bitMap.width / Math.sqrt(i),
                bitMap.height / Math.sqrt(i)
            )
        }
        return bitMap
    }

    //Bitmap压缩，--newWidth newHeight
    private fun zoomImage(bgimage: Bitmap, newWidth: Double, newHeight: Double): Bitmap {
        // 获取这个图片的宽和高
        val width = bgimage.width.toFloat()
        val height = bgimage.height.toFloat()
        // 创建操作图片用的matrix对象
        val matrix = Matrix()
        // 计算宽高缩放率
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bgimage, 0, 0, width.toInt(), height.toInt(), matrix, true)
    }

    /**
     * 只校验正数 0-90.000000 0-180.000000 范围内
     * 经纬度校验
     * 经度longitude: (?:[0-9]|[1-9][0-9]|1[0-7][0-9]|180)\\.([0-9]{6})
     * 纬度latitude：  (?:[0-9]|[1-8][0-9]|90)\\.([0-9]{6})
     *
     * @return boolean
     */
    fun checkLoLa(longitude: String, latitude: String): Boolean {
        var longitude = longitude
        var latitude = latitude
        val regLo =
            "(((?:[0-9]|[1-9][0-9]|1[0-7][0-9])\\.([0-9]{0,6}))|((?:180)\\.([0]{0,6}))|((?:[0-9]|[1-9][0-9]|1[0-7][0-9]))|(?:180))"
        val regLa = "(((?:[0-9]|[1-8][0-9]|90)\\.([0-9]{0,6}))|(?:[0-9]|[1-8][0-9]|90))"
        longitude = longitude.trim { it <= ' ' }
        latitude = latitude.trim { it <= ' ' }
        return if (longitude.matches(regLo.toRegex()) == true) latitude.matches(regLa.toRegex()) else false
    }

    /**
     * 只校验正数 0-90.000000 范围内
     * 纬度校验
     * 纬度latitude：  (?:[0-9]|[1-8][0-9]|90)\\.([0-9]{6})
     *
     * @return boolean
     */
    fun checkLatitude(latitude: String): Boolean {
        var latitude = latitude
        val regLa = "(((?:[0-9]|[1-8][0-9]|90)\\.([0-9]{0,6}))|(?:[0-9]|[1-8][0-9]|90))"
        latitude = latitude.trim { it <= ' ' }
        return latitude.matches(regLa.toRegex())
    }

    /**
     * 只校验正数 0-180.000000 范围内
     * 经度校验
     * 经度longitude: (?:[0-9]|[1-9][0-9]|1[0-7][0-9]|180)\\.([0-9]{6})
     *
     * @return boolean
     */
    fun checkLongitude(longitude: String): Boolean {
        var longitude = longitude
        val regLo =
            "(((?:[0-9]|[1-9][0-9]|1[0-7][0-9])\\.([0-9]{0,6}))|((?:180)\\.([0]{0,6}))|((?:[0-9]|[1-9][0-9]|1[0-7][0-9]))|(?:180))"
        longitude = longitude.trim { it <= ' ' }
        return longitude.matches(regLo.toRegex())
    }

    fun checkLoLa(longitude: Double, latitude: Double): Boolean {
        return if (longitude >= 0 && longitude <= 180 && latitude >= 0 && latitude <= 90) {
            true
        } else false
    }

    //讲文本转成语音播报格式
    fun updateNavigationJson(text: String?): String {
        val jsonObject = JSONObject()
        try {
            jsonObject.put("EXTRA_LOCATION_TRAFFIC_INFO", text)
        } catch (e: JSONException) {
            Timber.d(" updateNavigationJson e:%s", e.message)
        }
        return jsonObject.toString()
    }

    /**
     * 跳转车牌设置页（跳转到车辆设置我的车页面）
     */
    fun startSettingCar(settingAction: String, context: Context) {
        try {
            Timber.d("startSettingCar: Action - $settingAction Package - ${BaseConstant.SETTING}")

            val intent = Intent(settingAction).apply {
                component = ComponentName(BaseConstant.SETTING, BaseConstant.SETTING_SERVICE)
            }
            // 根据 Service 是否是前台服务选择方法
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            Timber.d("startSettingCar: Exception e: ${e.message}")
        }
    }

    /**
     * U盘是否已经挂载
     */
    fun hasUsbPath(usbRootPath: String): Boolean {
        return Environment.getExternalStorageState(File(usbRootPath)).let {
            Environment.MEDIA_MOUNTED == it
        }
    }

    /**
     * 获取路线偏好类型
     */
    fun getConfigRoutePreferenceValue(routePreferSet: Int): String {
        return when (routePreferSet) {
            1 -> { //避免收费
                ConfigRoutePreference.PREFERENCE_AVOID_CHARGE
            }

            2 -> { //多策略算路-不勾选任何偏好-走高德推荐
                ConfigRoutePreference.PREFERENCE_DEFAULT
            }

            3 -> { //不⾛⾼速
                ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY
            }

            4 -> { //躲避拥堵
                ConfigRoutePreference.PREFERENCE_AVOID_JAN
            }

            5 -> { //不⾛⾼速且避免收费
                ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY
            }

            6 -> { //不⾛⾼速且躲避拥堵
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY
            }

            7 -> { //躲避收费和拥堵
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE
            }

            8 -> { //不⾛⾼速躲避收费和拥堵
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY
            }

            9 -> { //大路优先
                ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST
            }

            10 -> { //速度最快
                ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST
            }

            11 -> { //少收费
                ConfigRoutePreference.PREFERENCE_AVOID_CHARGE
            }

            12 -> { //⾼德推荐
                ConfigRoutePreference.PREFERENCE_DEFAULT
            }

            13 -> { //不⾛⾼速
                ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY
            }

            14 -> { //躲避拥堵
                ConfigRoutePreference.PREFERENCE_AVOID_JAN
            }

            15 -> { //少收费+不⾛⾼速
                ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY
            }

            16 -> { //躲避拥堵+不⾛⾼速
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY
            }

            17 -> { //躲避拥堵+少收费
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE
            }

            18 -> { //躲避拥堵+少收费+不⾛⾼速
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY
            }

            20 -> { //⾼速优先
                ConfigRoutePreference.PREFERENCE_USING_HIGHWAY
            }

            24 -> { //躲避拥堵且⾼速优先
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY
            }

            34 -> { //⾼速优先
                ConfigRoutePreference.PREFERENCE_USING_HIGHWAY
            }

            39 -> { //躲避拥堵+⾼速优先
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY
            }

            44 -> { //躲避拥堵+⼤路优先
                ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST
            }

            45 -> { //躲避拥堵+速度最快
                ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST
            }

            else -> {
                ConfigRoutePreference.PREFERENCE_DEFAULT
            }
        }
    }

    /**
     * 获取路线偏好类型
     */
    fun getConfigRoutePreferenceKey(routePreferSet: String): Int {
        return when (routePreferSet) {
            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE -> { //避免收费 少收费
                1
            }

            ConfigRoutePreference.PREFERENCE_DEFAULT -> { //多策略算路-不勾选任何偏好-走高德推荐
                2
            }

            ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY -> { //不⾛⾼速
                3
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN -> { //躲避拥堵
                4
            }

            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY -> { //不⾛⾼速且避免收费 少收费+不⾛⾼速
                5
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY -> { //不⾛⾼速且躲避拥堵 躲避拥堵+不⾛⾼速
                6
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE -> { //躲避收费和拥堵 躲避拥堵+少收费
                7
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY -> { //不⾛⾼速躲避收费和拥堵 躲避拥堵+少收费+不⾛⾼速
                8
            }

            ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST -> { //大路优先
                9
            }

            ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST -> { //速度最快
                10
            }

            ConfigRoutePreference.PREFERENCE_USING_HIGHWAY -> { //⾼速优先
                20
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY -> { //躲避拥堵且⾼速优先
                24
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST -> { //躲避拥堵+⼤路优先
                44
            }

            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST -> { //躲避拥堵+速度最快
                45
            }

            else -> {
                0
            }
        }
    }

    fun getConfigRoutePreferenceInfo(routePreferSet: Int): String {
        val networkNotConnected = "网络未连接，无法"
        return when (routePreferSet) {
            4, 6, 7, 8, 14, 16, 17, 18, 24, 39 -> "${networkNotConnected}躲避拥堵路线"
            9 -> "${networkNotConnected}大路优先路线"
            10 -> "${networkNotConnected}速度最快路线"
            44 -> "${networkNotConnected}躲避拥堵大路优先路线"
            45 -> "${networkNotConnected}躲避拥堵速度最快路线"
            else -> "请求成功"
        }
    }

    //手机号码去掉中间的空格
    fun phoneNoSpace(phone: String):String {
        return phone.replace("\\s+".toRegex(), "")
    }

    //手机号码格式变为[XXX XXXX XXXX]
    fun getFormattedPhone(phone: String): String{
        return "${phone.substring(0, 3)} ${phone.substring(3, 7)} ${phone.substring(7)}"
    }
}