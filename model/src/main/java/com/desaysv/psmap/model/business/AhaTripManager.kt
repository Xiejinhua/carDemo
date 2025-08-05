package com.desaysv.psmap.model.business

import android.app.Application
import android.text.TextUtils
import androidx.activity.result.ActivityResult
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.net.NetworkConstants
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.model.bean.AhaTankDetailBean
import com.desaysv.psmap.model.bean.GuideData
import com.desaysv.psmap.model.bean.MineGuideDetail
import com.desaysv.psmap.model.bean.MineGuideListBean
import com.desaysv.psmap.model.bean.MineTankCollectListBean
import com.desaysv.psmap.model.bean.MineTankListBean
import com.desaysv.psmap.model.bean.ScenicDetailBean
import com.desaysv.psmap.model.bean.ScenicDetailResponseBean
import com.desaysv.psmap.model.bean.ScenicSectorBean
import com.desaysv.psmap.model.bean.TankData
import com.example.aha_api_sdkd01.manger.AhaAPIManger
import com.example.aha_api_sdkd01.manger.AhaAPIManger.RequestCallBackListener
import com.example.aha_api_sdkd01.manger.models.LineDetailModel
import com.example.aha_api_sdkd01.manger.models.LineListModel
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * 路书接口管理类
 */
@Singleton
class AhaTripManager @Inject constructor(
    private val application: Application,
    private val iCarInfoProxy: ICarInfoProxy,
    private val locationBusiness: LocationBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val ahaTripBusiness: AhaTripBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val netWorkManager: NetWorkManager,
    private val gson: Gson
):  AhaTripImpl {

    private var currentJob: Job? = null // 用于保存当前的请求任务
    private var lastTime = 0L //上电后上次保存的时间

    //失败toast
    fun setToast(errorCode: Int){
        ahaTripBusiness.setToast.postValue(when (errorCode){
            -1  -> "网络获取数据失败，请稍后再试"
            -2 -> "数据异常"
            -3 -> "请求该接口需要登录"
            1112 -> "服务已经关闭"
            1113 -> "请校正车机时间"
            9999 -> "账户被顶掉"
            else -> "未获取到数据"
        })
    }

    //路书init
    override fun ahaInit(){
        try {
            Timber.i("ahaInit init start")
            AhaAPIManger.getInstance().init(application, iCarInfoProxy.vinCode)
            Timber.i("ahaInit init end")
            ahaTripBusiness.scenicSectorObserve()
        } catch (e: Exception) {
            Timber.i("ahaInit e:${e.message}")
            settingAccountBusiness.accountSdkInit() //个人中心账号绑定初始化
        }
    }

    //请求路书列表
    override suspend fun requestLineList(page: Int, size: Int,sort: String, cityItemInfo: CityItemInfo, keyword: String, day: String) {
        Timber.i("requestLineList page:$page, size:$size, sort:$sort, cityItemInfo:${cityItemInfo.cityName}, keyword:$keyword, day:$day")
        if (netWorkManager.isNetworkConnected()){
            if (page == 1)
                ahaTripBusiness.lineLoading.postValue(true)
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["page"] = page.toString() // 页码,比如1
                            this["size"] = size.toString() // //每页个
                            this["stCity"] = cityItemInfo.cityAdcode.toString() // 高德城市 code （出发城市由近及远 排序必传）
                            this["sort"] = sort //排序
                            if (!TextUtils.isEmpty(day)){
                                this["day"] = day //天数
                            }
                            this["lnglat"] = "${cityItemInfo.cityX / 1000000}, ${cityItemInfo.cityY / 1000000}" // 高德经纬度格式
                            this["keyword"] = keyword//路书搜索 关键词 景区相关路书（取景区详情 caption 字段作为 keyword 进行搜索）
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.LINE_LIST_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestLineList onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                ahaTripBusiness.lineList.postValue(arrayListOf())
                                ahaTripBusiness.lineLoading.postValue(false)
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val model = response as LineListModel
                                    Timber.i("requestLineList onSuccess response:${gson.toJson(model)}")
                                    ahaTripBusiness.lineMaxPage.postValue(model.data.meta.maxPage)
                                    val list = model.data.list
                                    ahaTripBusiness.lineList.postValue(list)
                                    if (list.size <= 0){
                                        if (ahaTripBusiness.lineAllList.value != null && ahaTripBusiness.lineAllList.value!!.isEmpty()){
                                            setToast(10000) //失败toast
                                        } else {
                                            Timber.i("requestLineList list.size <= 0 有数据")
                                        }
                                    }
                                }
                                ahaTripBusiness.lineLoading.postValue(false)
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestLineList request Timeout：${e.message}")
                    ahaTripBusiness.lineList.postValue(arrayListOf())
                    setToast(10001) //失败toast
                    ahaTripBusiness.lineLoading.postValue(false)
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestLineList request cancelled：${e.message}")
                    ahaTripBusiness.lineList.postValue(arrayListOf())
                    setToast(10002) //失败toast
                    ahaTripBusiness.lineLoading.postValue(false)
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestLineList Error during requestLineList：${e.message}")
                    ahaTripBusiness.lineList.postValue(arrayListOf())
                    setToast(10003) //失败toast
                    ahaTripBusiness.lineLoading.postValue(false)
                }
            }
        } else {
            Timber.i("requestLineList onError 没有网络")
            setToast(-1)
            ahaTripBusiness.lineList.postValue(arrayListOf())
            ahaTripBusiness.lineLoading.postValue(false)
        }
    }

    //单条路书详情
    override suspend fun requestLineDetail(lineId: String) {
        if (netWorkManager.isNetworkConnected()){
            ahaTripBusiness.lineDetailLoading.postValue(true)
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["id"] = lineId //路书id
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.LINE_DETAIL_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestLineDetail onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                ahaTripBusiness.lineDetail.postValue(LineDetailModel.DataDTO().apply { id = -1 })
                                ahaTripBusiness.lineDetailLoading.postValue(false)
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val model = response as LineDetailModel
                                    Timber.i("requestLineDetail onSuccess response:${gson.toJson(model)}")
                                    if (model.data == null){
                                        setToast(10000) //失败toast
                                        ahaTripBusiness.lineDetail.postValue(LineDetailModel.DataDTO().apply { id = -1 })
                                    } else {
                                        ahaTripBusiness.lineDetail.postValue(model.data)
                                        ahaTripBusiness.isLineFav.postValue(model.data.otherData.isIsFav)
                                    }
                                }
                                ahaTripBusiness.lineDetailLoading.postValue(false)
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestLineDetail request Timeout：${e.message}")
                    setToast(10001) //失败toast
                    ahaTripBusiness.lineDetail.postValue(LineDetailModel.DataDTO().apply { id = -1 })
                    ahaTripBusiness.lineDetailLoading.postValue(false)
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestLineDetail request cancelled：${e.message}")
                    setToast(10002) //失败toast
                    ahaTripBusiness.lineDetail.postValue(LineDetailModel.DataDTO().apply { id = -1 })
                    ahaTripBusiness.lineDetailLoading.postValue(false)
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestLineDetail Error during requestLineList：${e.message}")
                    setToast(10003) //失败toast
                    ahaTripBusiness.lineDetail.postValue(LineDetailModel.DataDTO().apply { id = -1 })
                    ahaTripBusiness.lineDetailLoading.postValue(false)
                }
            }
        } else {
            Timber.i("requestLineDetail onError 没有网络")
            setToast(-1)
            ahaTripBusiness.lineDetail.postValue(LineDetailModel.DataDTO().apply { id = -1 })
            ahaTripBusiness.lineDetailLoading.postValue(false)
        }
    }

    //请求路书主题分类
    override suspend fun requestLineThemeList() {
        if (netWorkManager.isNetworkConnected()){
            // 取消之前的请求任务
            currentJob?.cancel()

            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.LINE_THEME_LIST_PATH, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestLineThemeList onError errorCode:$errorCode, msg:$msg")
                            }

                            override fun onSuccess(response: Any?) {
                                Timber.i("requestLineThemeList onSuccess response:${gson.toJson(response)}")
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestLineThemeList request Timeout：${e.message}")
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestLineThemeList request cancelled：${e.message}")
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestLineThemeList Error during requestLineList：${e.message}")
                }
            }
        } else {
            Timber.i("requestLineThemeList onError 没有网络")
        }
    }

    //请求收藏/取消收藏
    override suspend fun requestFavorite(type: String, id: String, isMineFav: Boolean) {
        if (netWorkManager.isNetworkConnected()){
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["type"] = type //路书 1、轨迹 12、景点 3 、共创 4
                            this["id"] = id //对应精品路书/共创路书/轨迹的 id
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.COLLECT_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestFavorite onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                            }

                            override fun onSuccess(response: Any?) {
                                Timber.i("requestFavorite onSuccess response:${gson.toJson(response)}")
                                val responseJSONObject = JSONObject(response as String)
                                if (TextUtils.equals(type, "1")){
                                    val fav = responseJSONObject.getJSONObject("data").getInt("fav")
                                    if (isMineFav){
                                        ahaTripBusiness.deleteLineCollectResult.postValue(id)
                                    }
                                    ahaTripBusiness.isLineFav.postValue(fav == 1)
                                    ahaTripBusiness.isLineFavChange.postValue(fav == 1)
                                    ahaTripBusiness.setToast.postValue(if (fav == 1) "收藏路书成功" else "已取消收藏路书")
                                } else if (TextUtils.equals(type, "12")){
                                    val fav = responseJSONObject.getJSONObject("data").getInt("fav")
                                    if (isMineFav){
                                        ahaTripBusiness.deleteTankCollectResult.postValue(id)
                                    }
                                    ahaTripBusiness.isTankFav.postValue(fav == 1)
                                    ahaTripBusiness.isTankFavChange.postValue(fav == 1)
                                    ahaTripBusiness.setToast.postValue(if (fav == 1) "收藏轨迹路书成功" else "已取消轨迹收藏路书")
                                } else if (TextUtils.equals(type, "4")){
                                    val fav = responseJSONObject.getJSONObject("data").getBoolean("collect")
                                    if (isMineFav){
                                        ahaTripBusiness.deleteGuideCollectResult.postValue(id)
                                    }
                                    ahaTripBusiness.isGuideFav.postValue(fav)
                                    ahaTripBusiness.isGuideFavChange.postValue(fav)
                                    ahaTripBusiness.setToast.postValue(if (fav) "收藏共创路书成功" else "已取消收藏共创路书")
                                }
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestFavorite request Timeout：${e.message}")
                    ahaTripBusiness.setToast.postValue("请求超时了，请稍后重试")
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestFavorite request cancelled：${e.message}")
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestFavorite Error during requestLineList：${e.message}")
                    setToast(-2)
                }
            }
        } else {
            Timber.i("requestFavorite onError 没有网络")
            setToast(-1)
        }
    }

    //景区列表、搜索接口
    override suspend fun requestScenicList(
        page: Int,
        sort: String,
        isCurrentCity: Boolean,
        cityCode: String
    ) {
        if (netWorkManager.isNetworkConnected()){
            // 取消之前的请求任务
            currentJob?.cancel()

            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val lastLocation = locationBusiness.getLastLocation()
                        val map = HashMap<String, String>().apply {
                            this["page"] = page.toString() // 页码,比如1
                            this["city"] = if (isCurrentCity){
                                val currentCityAdCode = mapDataBusiness.getAdCodeByLonLat(lastLocation.longitude, lastLocation.latitude)
                                currentCityAdCode.toString()
                            } else cityCode // 高德城市 code （出发城市由近及远 排序必传）
                            this["sort"] = sort //排序
                            this["lgt"] = if (isCurrentCity){
                                "${lastLocation.longitude}, ${lastLocation.latitude}"
                            } else {
                                val cityItemInfo = mapDataBusiness.getCityInfo(cityCode.toInt())
                                "${cityItemInfo?.cityX}, ${cityItemInfo?.cityY}"
                            } // 高德经纬度格式
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.SCENIC_LIST_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestScenicList onError errorCode:$errorCode, msg:$msg")
                            }

                            override fun onSuccess(response: Any?) {
                                Timber.i("requestScenicList onSuccess response:${gson.toJson(response)}")
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestScenicList request Timeout：${e.message}")
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestScenicList request cancelled：${e.message}")
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestScenicList Error during requestLineList：${e.message}")
                }
            }
        } else {
            Timber.i("requestScenicList onError 没有网络")
        }
    }

    //景区详情接口
    override suspend fun requestScenicDetail(scenicId: String) {
        if (netWorkManager.isNetworkConnected()){
            ahaTripBusiness.scenicDetailLoading.postValue(true)
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["id"] = scenicId //景点 id
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.SCENIC_DETAIL_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestScenicDetail onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                ahaTripBusiness.scenicDetail.postValue(ScenicDetailBean().apply { id = -100000 })
                                ahaTripBusiness.scenicDetailLoading.postValue(false)
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val responseBean = gson.fromJson(response as String, ScenicDetailResponseBean::class.java)
                                    Timber.i("requestScenicDetail onSuccess response:${gson.toJson(responseBean)}")
                                    if (responseBean.code == 0 && responseBean.data != null){//成功
                                        ahaTripBusiness.scenicDetail.postValue(responseBean.data)
                                    } else {
                                        setToast(10000) //失败toast
                                        ahaTripBusiness.scenicDetail.postValue(ScenicDetailBean().apply { id = -100000 })
                                    }
                                }
                                ahaTripBusiness.scenicDetailLoading.postValue(false)
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestScenicDetail request Timeout：${e.message}")
                    setToast(10001) //失败toast
                    ahaTripBusiness.scenicDetail.postValue(ScenicDetailBean().apply { id = -100000 })
                    ahaTripBusiness.scenicDetailLoading.postValue(false)
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestScenicDetail request cancelled：${e.message}")
                    setToast(10002) //失败toast
                    ahaTripBusiness.scenicDetail.postValue(ScenicDetailBean().apply { id = -100000 })
                    ahaTripBusiness.scenicDetailLoading.postValue(false)
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestScenicDetail Error during requestLineList：${e.message}")
                    setToast(10003) //失败toast
                    ahaTripBusiness.scenicDetail.postValue(ScenicDetailBean().apply { id = -100000 })
                    ahaTripBusiness.scenicDetailLoading.postValue(false)
                }
            }
        } else {
            Timber.i("requestScenicDetail onError 没有网络")
            setToast(-1)
            ahaTripBusiness.scenicDetail.postValue(ScenicDetailBean().apply { id = -100000 })
            ahaTripBusiness.scenicDetailLoading.postValue(false)
        }
    }

    //景区播报
    override suspend fun requestScenicSector(distance: Int) {
        if (netWorkManager.isNetworkConnected()){
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val lastLocation = locationBusiness.getLastLocation()
                        val map = HashMap<String, String>().apply {
                            this["lgt"] = "${lastLocation.longitude}, ${lastLocation.latitude}" // 高德经纬度格式
                            this["degree"] = "${lastLocation.bearing}" //车方向角度
                            this["distance"] = distance.toString() // 前方扇形搜索范围 单位米默认 5000 米
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.SCENE_SECTOR_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestScenicSector onError errorCode:$errorCode, msg:$msg")
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val responseBean = gson.fromJson(response as String, ScenicSectorBean::class.java)
                                    Timber.i("requestScenicSector onSuccess response:${gson.toJson(responseBean)}")
                                    if (responseBean.code == 0 && responseBean.data != null){ //成功
                                        val time = CustomFileUtils.getFile(responseBean.data.id.toString()) //上次保存的时间
                                        if (TextUtils.isEmpty(time)){ //没有保存，查不到记录
                                            if (ahaTripBusiness.scenicSectorData.value == null){
                                                val nowTime = System.currentTimeMillis()
                                                lastTime = nowTime //更新上次时间
                                                CustomFileUtils.saveFile(nowTime.toString(), responseBean.data.id.toString())
                                                ahaTripBusiness.scenicSectorData.postValue(responseBean)
                                                Timber.i("requestScenicSector ahaTripBusiness.scenicSectorData.value == null")
                                            } else {
                                                val nowTime = System.currentTimeMillis()
                                                val timeDifference = isTimeDifferenceMoreThan30Minutes(lastTime, nowTime) //判断两个时间是否相差超过 30 分钟
                                                Timber.i("requestScenicSector timeDifference:$timeDifference")
                                                if (timeDifference){
                                                    lastTime = nowTime //更新上次时间
                                                    CustomFileUtils.saveFile(nowTime.toString(), responseBean.data.id.toString())
                                                    ahaTripBusiness.scenicSectorData.postValue(responseBean)
                                                } else {
                                                    Timber.i("requestScenicSector timeDifference < 30分钟")
                                                }
                                            }
                                        } else {
                                            val nowTime = System.currentTimeMillis()
                                            val timeDifferenceDay = isTimeDifferenceMoreThan30Day(time?.toLong() ?: 0L, nowTime) //判断两个时间是否相差超过 30 天
                                            Timber.i("requestScenicSector timeDifferenceDay:$timeDifferenceDay")
                                            if (timeDifferenceDay){
                                                lastTime = nowTime //更新上次时间
                                                CustomFileUtils.saveFile(nowTime.toString(), responseBean.data.id.toString())
                                                ahaTripBusiness.scenicSectorData.postValue(responseBean)
                                            } else {
                                                Timber.i("requestScenicSector timeDifferenceDay < 30天")
                                            }
                                        }
                                    }
                                }
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestScenicSector request Timeout：${e.message}")
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestScenicSector request cancelled：${e.message}")
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestScenicSector Error during requestLineList：${e.message}")
                }
            }
        } else {
            Timber.i("requestScenicSector onError 没有网络")
        }
    }

    //我的-我制作的共创路书列表
    override suspend fun requestMineGuideList(
        page: Int,
        size: Int
    ) {
        if (netWorkManager.isNetworkConnected()){
            if (page == 1)
                ahaTripBusiness.guideLoading.postValue(true)
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["page"] = page.toString() // 页码,比如1
                            this["size"] = size.toString() // //每页个
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.MINE_GUIDE_LIST_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestMineGuideList onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                ahaTripBusiness.guideList.postValue(arrayListOf())
                                ahaTripBusiness.guideLoading.postValue(false)
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val model = gson.fromJson(response as String, MineGuideListBean::class.java)
                                    Timber.i("requestMineGuideList onSuccess response:${gson.toJson(model)}")
                                    ahaTripBusiness.guideMaxPage.postValue(model.data?.meta?.maxPage)
                                    val list = model.data?.list
                                    ahaTripBusiness.guideList.postValue(list ?: arrayListOf())
                                    if (list != null && list.isEmpty()) {
                                        if (ahaTripBusiness.guideAllList.value != null && ahaTripBusiness.guideAllList.value!!.isEmpty()){
                                            setToast(10000) //失败toast
                                        } else {
                                            Timber.i("requestMineGuideList list.size <= 0 有数据")
                                        }
                                    }
                                }
                                ahaTripBusiness.guideLoading.postValue(false)
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestMineGuideList request Timeout：${e.message}")
                    ahaTripBusiness.guideList.postValue(arrayListOf())
                    setToast(10001) //失败toast
                    ahaTripBusiness.guideLoading.postValue(false)
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestMineGuideList request cancelled：${e.message}")
                    ahaTripBusiness.guideList.postValue(arrayListOf())
                    setToast(10002) //失败toast
                    ahaTripBusiness.guideLoading.postValue(false)
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestMineGuideList Error during requestLineList：${e.message}")
                    ahaTripBusiness.guideList.postValue(arrayListOf())
                    setToast(10003) //失败toast
                    ahaTripBusiness.guideLoading.postValue(false)
                }
            }
        } else {
            Timber.i("requestMineGuideList onError 没有网络")
            setToast(-1)
            ahaTripBusiness.guideList.postValue(arrayListOf())
            ahaTripBusiness.guideLoading.postValue(false)
        }
    }

    //共创路书详情
    override suspend fun requestMineGuideDetail(id: String) {
        if (netWorkManager.isNetworkConnected()){
            ahaTripBusiness.guideDetailLoading.postValue(true)
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["id"] = id //共创路书 id
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.MINE_GUIDE_DETAIL_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestMineGuideDetail onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                ahaTripBusiness.guideDetail.postValue(GuideData().apply { this.id = -1 })
                                ahaTripBusiness.guideDetailLoading.postValue(false)
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val model = gson.fromJson(response as String, MineGuideDetail::class.java)
                                    Timber.i("requestMineGuideDetail onSuccess response:${gson.toJson(model)}")
                                    if (model.data == null){
                                        setToast(10000) //失败toast
                                        ahaTripBusiness.guideDetail.postValue(GuideData().apply { this.id = -1 })
                                    } else {
                                        ahaTripBusiness.guideDetail.postValue(model.data)
                                        ahaTripBusiness.isGuideFav.postValue(model.data!!.otherData?.fav ?: false)
                                    }
                                }
                                ahaTripBusiness.guideDetailLoading.postValue(false)
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestMineGuideDetail request Timeout：${e.message}")
                    setToast(10001) //失败toast
                    ahaTripBusiness.guideDetail.postValue(GuideData().apply { this.id = -1 })
                    ahaTripBusiness.guideDetailLoading.postValue(false)
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestMineGuideDetail request cancelled：${e.message}")
                    setToast(10002) //失败toast
                    ahaTripBusiness.guideDetail.postValue(GuideData().apply { this.id = -1 })
                    ahaTripBusiness.guideDetailLoading.postValue(false)
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestMineGuideDetail Error during requestLineList：${e.message}")
                    setToast(10003) //失败toast
                    ahaTripBusiness.guideDetail.postValue(GuideData().apply { this.id = -1 })
                    ahaTripBusiness.guideDetailLoading.postValue(false)
                }
            }
        } else {
            Timber.i("requestMineGuideDetail onError 没有网络")
            setToast(-1)
            ahaTripBusiness.guideDetail.postValue(GuideData().apply { this.id = -1 })
            ahaTripBusiness.guideDetailLoading.postValue(false)
        }
    }

    //我的-我制作的轨迹列表
    override suspend fun requestMineTankList(
        page: Int,
        size: Int
    ) {
        if (netWorkManager.isNetworkConnected()){
            if (page == 1)
                ahaTripBusiness.tankLoading.postValue(true)
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["page"] = page.toString() // 页码,比如1
                            this["size"] = size.toString() // //每页个
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.MINE_TRANK_LIST_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestMineTankList onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                ahaTripBusiness.tankList.postValue(arrayListOf())
                                ahaTripBusiness.tankLoading.postValue(false)
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val model = gson.fromJson(response as String, MineTankListBean::class.java)
                                    Timber.i("requestMineTankList onSuccess response:${gson.toJson(model)}")
                                    ahaTripBusiness.tankMaxPage.postValue(model.data?.meta?.maxPage)
                                    var list = model.data?.list
                                    list = list?.sortedWith(compareByDescending {
                                        try {
                                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.time)
                                        } catch (e: Exception) {
                                            Timber.i("requestMineTankList sortedWith Exception:${e.message}")
                                            null
                                        }
                                    })?.toMutableList()
                                    ahaTripBusiness.tankList.postValue(list ?: arrayListOf())
                                    if (list != null && list.isEmpty()) {
                                        if (ahaTripBusiness.tankAllList.value != null && ahaTripBusiness.tankAllList.value!!.isEmpty()){
                                            setToast(10000) //失败toast
                                        } else {
                                            Timber.i("requestMineTankList list.size <= 0 有数据")
                                        }
                                    }
                                }
                                ahaTripBusiness.tankLoading.postValue(false)
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestMineTankList request Timeout：${e.message}")
                    ahaTripBusiness.tankList.postValue(arrayListOf())
                    setToast(10001) //失败toast
                    ahaTripBusiness.tankLoading.postValue(false)
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestMineTankList request cancelled：${e.message}")
                    ahaTripBusiness.tankList.postValue(arrayListOf())
                    setToast(10002) //失败toast
                    ahaTripBusiness.tankLoading.postValue(false)
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestMineTankList Error during requestLineList：${e.message}")
                    ahaTripBusiness.tankList.postValue(arrayListOf())
                    setToast(10003) //失败toast
                    ahaTripBusiness.tankLoading.postValue(false)
                }
            }
        } else {
            Timber.i("requestMineTankList onError 没有网络")
            setToast(-1)
            ahaTripBusiness.tankList.postValue(arrayListOf())
            ahaTripBusiness.tankLoading.postValue(false)
        }
    }

    //我的-轨迹详情
    override suspend fun requestMineTankDetail(id: String) {
        if (netWorkManager.isNetworkConnected()){
            ahaTripBusiness.tankDetailLoading.postValue(true)
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["id"] = id //共创路书 id
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.MINE_TRANK_DETAIL_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestMineTankDetail onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                ahaTripBusiness.tankDetail.postValue(TankData().apply { this.id = -1 })
                                ahaTripBusiness.tankDetailLoading.postValue(false)
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val model = gson.fromJson(response as String, AhaTankDetailBean::class.java)
                                    Timber.i("requestMineTankDetail onSuccess response:${gson.toJson(model)}")
                                    if (model.data == null){
                                        setToast(10000) //失败toast
                                        ahaTripBusiness.tankDetail.postValue(TankData().apply { this.id = -1 })
                                    } else {
                                        ahaTripBusiness.tankDetail.postValue(model.data)
                                        ahaTripBusiness.isTankFav.postValue(model.data!!.otherData?.fav ?: false)
                                    }
                                }
                                ahaTripBusiness.tankDetailLoading.postValue(false)
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestMineTankDetail request Timeout：${e.message}")
                    setToast(10001) //失败toast
                    ahaTripBusiness.tankDetail.postValue(TankData().apply { this.id = -1 })
                    ahaTripBusiness.tankDetailLoading.postValue(false)
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestMineTankDetail request cancelled：${e.message}")
                    setToast(10002) //失败toast
                    ahaTripBusiness.tankDetail.postValue(TankData().apply { this.id = -1 })
                    ahaTripBusiness.tankDetailLoading.postValue(false)
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestMineTankDetail Error during requestLineList：${e.message}")
                    setToast(10003) //失败toast
                    ahaTripBusiness.tankDetail.postValue(TankData().apply { this.id = -1 })
                    ahaTripBusiness.tankDetailLoading.postValue(false)
                }
            }
        } else {
            Timber.i("requestMineTankDetail onError 没有网络")
            setToast(-1)
            ahaTripBusiness.tankDetail.postValue(TankData().apply { this.id = -1 })
            ahaTripBusiness.tankDetailLoading.postValue(false)
        }
    }

    //删除我的共创
    override suspend fun requestMineGuideDelete(id: String) {
        if (netWorkManager.isNetworkConnected()){
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["id"] = id //共创路书 id
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.MINE_GUIDE_DELETE_PATH
                            , map, object : RequestCallBackListener<Any> {
                                override fun onError(errorCode: Int, msg: String) {
                                    Timber.i("requestMineGuideDelete onError errorCode:$errorCode, msg:$msg")
                                    if (errorCode == -1){
                                        setToast(errorCode)
                                    }else {
                                        ahaTripBusiness.setToast.postValue(msg) //失败toast
                                    }
                                }

                                override fun onSuccess(response: Any?) {
                                    Timber.i("requestMineGuideDelete onSuccess response:${gson.toJson(response)}")
                                    val responseJSONObject = JSONObject(response as String)
                                    val code = responseJSONObject.getInt("code")
                                    ahaTripBusiness.deleteGuideResult.postValue(id)
                                    ahaTripBusiness.setToast.postValue(if (code == 0) "删除共创路书成功" else "删除共创路书失败")
                                }
                            })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestMineGuideDelete request Timeout：${e.message}")
                    ahaTripBusiness.setToast.postValue("请求超时了，请稍后重试")
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestMineGuideDelete request cancelled：${e.message}")
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestMineGuideDelete Error during requestLineList：${e.message}")
                    setToast(-2)
                }
            }
        } else {
            Timber.i("requestMineGuideDelete onError 没有网络")
            setToast(-1)
        }
    }

    override suspend fun requestMineTankDelete(id: String) {
        if (netWorkManager.isNetworkConnected()){
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["id"] = id //轨迹 id
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.MINE_TRANK_DELETE_PATH
                            , map, object : RequestCallBackListener<Any> {
                                override fun onError(errorCode: Int, msg: String) {
                                    Timber.i("requestMineTankDelete onError errorCode:$errorCode, msg:$msg")
                                    if (errorCode == -1){
                                        setToast(errorCode)
                                    }else {
                                        ahaTripBusiness.setToast.postValue(msg) //失败toast
                                    }
                                }

                                override fun onSuccess(response: Any?) {
                                    if (response != null){
                                        Timber.i("requestMineTankDelete onSuccess response:${gson.toJson(response)}")
                                        val responseJSONObject = JSONObject(response as String)
                                        val code = responseJSONObject.getInt("code")
                                        ahaTripBusiness.deleteTankResult.postValue(id)
                                        ahaTripBusiness.setToast.postValue(if (code == 0) "删除轨迹路书成功" else "删除轨迹路书失败")
                                    }
                                }
                            })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestMineTankDelete request Timeout：${e.message}")
                    ahaTripBusiness.setToast.postValue("请求超时了，请稍后重试")
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestMineTankDelete request cancelled：${e.message}")
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestMineTankDelete Error during requestLineList：${e.message}")
                    setToast(-2)
                }
            }
        } else {
            Timber.i("requestMineTankDelete onError 没有网络")
            setToast(-1)
        }
    }

    //我的-我的收藏（路书(非共创路书)、轨迹、景点）
    override suspend fun requestMineCollectList(page: Int, size: Int, type: Int) {
        if (netWorkManager.isNetworkConnected()){
            when(type){
                1 ->{
                    if (page == 1)
                        ahaTripBusiness.lineCollectLoading.postValue(true)
                }
                else ->{
                    if (page == 1)
                        ahaTripBusiness.tankCollectLoading.postValue(true)
                }
            }

            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["page"] = page.toString() // 页码,比如1
                            this["size"] = size.toString() // //每页个
                            this["type"] = type.toString() // //路书1、轨迹12、景点3
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.MINE_COLLECT_LIST_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestMineCollectList onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                when(type){
                                    1 ->{
                                        ahaTripBusiness.lineCollectList.postValue(arrayListOf())
                                        ahaTripBusiness.lineCollectLoading.postValue(false)
                                    }
                                    else ->{
                                        ahaTripBusiness.tankCollectList.postValue(arrayListOf())
                                        ahaTripBusiness.tankCollectLoading.postValue(false)
                                    }
                                }
                            }

                            override fun onSuccess(response: Any?) {
                                Timber.i("requestMineCollectList onSuccess response:${response as String}")
                                when(type){
                                    1 ->{
                                        if (response != null){
                                            val model = gson.fromJson(response, LineListModel::class.java)
                                            Timber.i("requestMineCollectList onSuccess response:${gson.toJson(model)}")
                                            ahaTripBusiness.lineCollectMaxPage.postValue(model.data?.meta?.maxPage)
                                            val list = model.data.list
                                            ahaTripBusiness.lineCollectList.postValue(list)
                                            if (list.size <= 0){
                                                setToast(10000) //失败toast
                                            }
                                        }
                                        ahaTripBusiness.lineCollectLoading.postValue(false)
                                    }
                                    else ->{
                                        if (response != null){
                                            val model = gson.fromJson(response, MineTankCollectListBean::class.java)
                                            Timber.i("requestMineCollectList onSuccess response:${gson.toJson(model)}")
                                            ahaTripBusiness.tankCollectMaxPage.postValue(model.data?.meta?.maxPage)
                                            val list = model.data?.list
                                            ahaTripBusiness.tankCollectList.postValue(list ?: arrayListOf())
                                            if (list != null && list.isEmpty()) {
                                                setToast(10000) //失败toast
                                            }
                                        }
                                        ahaTripBusiness.tankCollectLoading.postValue(false)
                                    }
                                }
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestMineCollectList request Timeout：${e.message}")
                    setToast(10001) //失败toast
                    when(type){
                        1 ->{
                            ahaTripBusiness.lineCollectList.postValue(arrayListOf())
                            ahaTripBusiness.lineCollectLoading.postValue(false)
                        }
                        else ->{
                            ahaTripBusiness.tankCollectList.postValue(arrayListOf())
                            ahaTripBusiness.tankCollectLoading.postValue(false)
                        }
                    }
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestMineCollectList request cancelled：${e.message}")
                    setToast(10002) //失败toast
                    when(type){
                        1 ->{
                            ahaTripBusiness.lineCollectList.postValue(arrayListOf())
                            ahaTripBusiness.lineCollectLoading.postValue(false)
                        }
                        else ->{
                            ahaTripBusiness.tankCollectList.postValue(arrayListOf())
                            ahaTripBusiness.tankCollectLoading.postValue(false)
                        }
                    }
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestMineCollectList Error during requestLineList：${e.message}")
                    setToast(10003) //失败toast
                    when(type){
                        1 ->{
                            ahaTripBusiness.lineCollectList.postValue(arrayListOf())
                            ahaTripBusiness.lineCollectLoading.postValue(false)
                        }
                        else ->{
                            ahaTripBusiness.tankCollectList.postValue(arrayListOf())
                            ahaTripBusiness.tankCollectLoading.postValue(false)
                        }
                    }
                }
            }
        } else {
            Timber.i("requestMineCollectList onError 没有网络")
            setToast(-1)
            when(type){
                1 ->{
                    ahaTripBusiness.lineCollectList.postValue(arrayListOf())
                    ahaTripBusiness.lineCollectLoading.postValue(false)
                }
                else ->{
                    ahaTripBusiness.tankCollectList.postValue(arrayListOf())
                    ahaTripBusiness.tankCollectLoading.postValue(false)
                }
            }
        }
    }

    //我的-我的收藏共创路书列表
    override suspend fun requestMineCollectGuideList(page: Int, size: Int) {
        if (netWorkManager.isNetworkConnected()){
            if (page == 1)
                ahaTripBusiness.guideCollectLoading.postValue(true)
            // 取消之前的请求任务
            currentJob?.cancel()
            // 创建新的请求任务
            currentJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    withTimeout(TimeUnit.SECONDS.toMillis(NetworkConstants.TIME_OUT_REQUEST.toLong())) {
                        val map = HashMap<String, String>().apply {
                            this["page"] = page.toString() // 页码,比如1
                            this["size"] = size.toString() // //每页个
                        }

                        AhaAPIManger.getInstance().request(AhaAPIManger.APIPATH.MINE_COLLECT_GUIDE_LIST_PATH, map, object : RequestCallBackListener<Any> {
                            override fun onError(errorCode: Int, msg: String) {
                                Timber.i("requestMineCollectGuideList onError errorCode:$errorCode, msg:$msg")
                                if (errorCode == -1){
                                    setToast(errorCode)
                                }else {
                                    ahaTripBusiness.setToast.postValue(msg) //失败toast
                                }
                                ahaTripBusiness.guideCollectList.postValue(arrayListOf())
                                ahaTripBusiness.guideCollectLoading.postValue(false)
                            }

                            override fun onSuccess(response: Any?) {
                                if (response != null){
                                    val model = gson.fromJson(response as String, MineGuideListBean::class.java)
                                    Timber.i("requestMineCollectGuideList onSuccess response:${gson.toJson(model)}")
                                    ahaTripBusiness.guideCollectMaxPage.postValue(model.data?.meta?.maxPage)
                                    val list = model.data?.list
                                    ahaTripBusiness.guideCollectList.postValue(list ?: arrayListOf())
                                    if (list != null && list.isEmpty()) {
                                        if (ahaTripBusiness.guideCollectAllList.value != null && ahaTripBusiness.guideCollectAllList.value!!.isEmpty()){
                                            setToast(10000) //失败toast
                                        } else {
                                            Timber.i("requestMineCollectGuideList list.size <= 0 有数据")
                                        }
                                    }
                                }
                                ahaTripBusiness.guideCollectLoading.postValue(false)
                            }
                        })
                    }
                } catch (e: TimeoutCancellationException) { //超时
                    Timber.i("requestMineCollectGuideList request Timeout：${e.message}")
                    ahaTripBusiness.guideCollectList.postValue(arrayListOf())
                    setToast(10001) //失败toast
                    ahaTripBusiness.guideCollectLoading.postValue(false)
                } catch (e: CancellationException) { // 请求被取消时，不抛出异常
                    Timber.i("requestMineCollectGuideList request cancelled：${e.message}")
                    ahaTripBusiness.guideCollectList.postValue(arrayListOf())
                    setToast(10002) //失败toast
                    ahaTripBusiness.guideCollectLoading.postValue(false)
                } catch (e: Exception) { // 其他异常处理
                    Timber.e(e, "requestMineCollectGuideList Error during requestLineList：${e.message}")
                    ahaTripBusiness.guideCollectList.postValue(arrayListOf())
                    setToast(10003) //失败toast
                    ahaTripBusiness.guideCollectLoading.postValue(false)
                }
            }
        } else {
            Timber.i("requestMineCollectGuideList onError 没有网络")
            setToast(-1)
            ahaTripBusiness.guideCollectList.postValue(arrayListOf())
            ahaTripBusiness.guideCollectLoading.postValue(false)
        }
    }

    //判断当前是否是登录状态 true 登录 false 未登录
    override fun isLogin(): Boolean {
        return AhaAPIManger.getInstance().isLogin
    }

    //注册阿哈登录回调
    override fun registerLogin() {
        AhaAPIManger.getInstance().registerLogin()
    }

    //跳转阿哈登录
    override fun goLogin(result: ActivityResult) {
        AhaAPIManger.getInstance().goLogin(result) {
            Timber.i("goLogin result:$it")
        }
    }

    /**
     * 页面跳转根据type跳转
     * type；0：(非必传 默认 0 ) 0、app 首页，可带参数：定位城市+主题 1、共创列表，可带参
     * 数：定位城市 2、景区列表，可带参数：定位城市+主题、 3、轨迹首页 4、组队首页
     * cityId： 定位城市 id（非必传）
     * cityName： 定位城市名称 （非必传）
     * themeId：主题 （非必传）
     */
    override fun goHome(type: Int, cityId: Int, cityName: String, themeId: Int) {
        AhaAPIManger.getInstance().goHome(type , cityId, cityName, themeId)
    }

    /**
     * 跳转详情页
     * type；（必传 ） 0、路书详情 1、共创详情 2、景区详情 3、轨迹详情
     * id: （必传 ）对应详情 ID
     */
    override fun goDetail(type: Int, id: Int) {
        AhaAPIManger.getInstance().goDetail(type, id)
    }

    //判断两个时间是否相差超过 30 分钟
    fun isTimeDifferenceMoreThan30Minutes(time1: Long, time2: Long): Boolean {
        val diff = abs(time1 - time2) // 时间差（毫秒）
        return diff > 30 * 60 * 1000 // 是否大于 30 分钟
    }

    //判断两个时间是否相差超过 30 天
    fun isTimeDifferenceMoreThan30Day(time1: Long, time2: Long): Boolean {
        val diff = abs(time1 - time2) // 时间差（毫秒）
        return diff > 3 * 24 * 60 * 60 * 1000 // 是否大于 30 天
    }
}