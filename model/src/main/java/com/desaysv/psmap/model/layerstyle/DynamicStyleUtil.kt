package com.desaysv.psmap.model.layerstyle

import android.annotation.SuppressLint
import android.app.Application
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.autonavi.auto.skin.view.SkinImageView
import com.autonavi.auto.skin.view.SkinTextView
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.model.LayerIconAnchor
import com.autonavi.gbl.map.layer.model.LayerIconType.LayerIconTypeBMP
import com.autonavi.gbl.map.layer.model.LayerTexture
import com.autonavi.gbl.util.model.BinaryStream
import com.autosdk.common.SdkApplicationUtils
import com.autosdk.common.utils.BitmapUtils
import com.autosdk.common.utils.CommonUtil
import com.desaysv.psmap.base.auto.layerstyle.utils.StyleJsonAnalysisUtil
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.google.gson.Gson
import com.txzing.sdk.bean.TeamInfoResponse
import com.txzing.sdk.bean.UserInfo
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import java.nio.ByteBuffer

// 新增：定义 EntryPoint 接口，用于获取 UserGroupBusiness 实例
@EntryPoint
@InstallIn(SingletonComponent::class)
interface DynamicStyleUtilEntryPoint {
    fun getUserGroupBusiness(): UserGroupBusiness
}

object DynamicStyleUtil {

    var mMembersList: List<UserInfo> = emptyList()
    var teamInfo: TeamInfoResponse? = null
    var userId: Int? = null

    val gson = Gson()

    // 新增：通过 Hilt EntryPoint 延迟获取 UserGroupBusiness 实例
    private val userGroupBusiness: UserGroupBusiness by lazy {
        // 获取 Application 上下文（需确保 SdkApplicationUtils 能提供 Application 实例）
        val appContext = SdkApplicationUtils.getApplication()
        // 通过 EntryPointAccessors 获取 EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            appContext,
            DynamicStyleUtilEntryPoint::class.java
        )
        // 获取注入的 UserGroupBusiness 实例
        entryPoint.getUserGroupBusiness()
    }

    /**
     * 处理终点区域父节点扎标样式,统一样式风格
     * @param app
     * @param pLayer
     * @param poiName
     * @param mDirection
     * @param travelTime
     * @param dynamicMarkerId
     * @return
     */
    @JvmStatic
    @Synchronized
    fun handleEndAreaParentPointsMarkedId(
        app: Application?,
        pLayer: BaseLayer?,
        poiName: String?,
        mDirection: Int,
        travelTime: Long,
        dynamicMarkerId: Int
    ): Int {
        var view: View
        // 终点区域父节点扎标view
        /*todo 缺少资源从demo找、
        view = View.inflate(app, R.layout.end_area_parent_poi, null);
        StrokeTextView tvPoiName = view.findViewById(R.id.poi_name);
        //预留eta 到达时间 显示
        TextView travelTimeTv = view.findViewById(R.id.travel_time);
        tvPoiName.setText(poiName,8);
        String travelTimeStr = AutoRouteUtil.getScheduledTime(SdkApplicationUtils.getApplication().getApplicationContext(), travelTime,false);
        if(SdkNetworkUtil.isNetworkConnected()){
            travelTimeTv.setText(travelTimeStr);
            travelTimeTv.setVisibility(View.VISIBLE);
        }else {
            travelTimeTv.setVisibility(View.GONE);
        }
        if (tvPoiName.getVisibility() == View.GONE && travelTimeTv.getVisibility() == View.GONE) {
            return -1;
        }
        Bitmap dynamicBitmap = BitmapUtils.createBitmapFromView(view);
        ByteBuffer dataBuffer = ByteBuffer.allocate(dynamicBitmap.getByteCount());
        dynamicBitmap.copyPixelsToBuffer(dataBuffer);
        LayerTexture layerTexture = new LayerTexture();
        layerTexture.dataBuff = new BinaryStream(dataBuffer.array());
        layerTexture.width = dynamicBitmap.getWidth();
        layerTexture.height = dynamicBitmap.getHeight();
        layerTexture.iconType = LayerIconTypeBMP;
        switch (mDirection) {
            case RouteEndAreaDirection.RouteEndAreaDirectionLeft://左侧
                layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenterRight;
                break;
            case RouteEndAreaDirection.RouteEndAreaDirectionRight://右侧
                layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenterLeft;
                break;
            case RouteEndAreaDirection.RouteEndAreaDirectionBottom://下方
                layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenterTop;
                break;
            default:
                break;
        }
        layerTexture.resID = dynamicMarkerId;
        pLayer.getMapView().addLayerTexture(layerTexture);*/return dynamicMarkerId
    }

    /**
     * 组队正常图层view
     * @param pLayer
     * @param strMarkerInfo
     * @return
     */
    @JvmStatic
    @Synchronized
    fun addGroupMarker(
        application: Application?,
        pLayer: BaseLayer?,
        id: String,
        strMarkerInfo: String?,
        groupDynamicIds: MutableMap<String?, Int?>,
        dynamicMarkerId: Int,
        styleJsonAnalysisUtil: StyleJsonAnalysisUtil
    ): Int {
        var dynamicId = -1
        if (pLayer == null) {
            return dynamicId
        }
        val membersList = mMembersList
        if (membersList.isEmpty()) {
            return -1
        }
        val groupMember = mMembersList.find { id.toIntOrNull() == it.user_id }
        Timber.i(
            "addGroupMarker id:$id groupMember: ${gson.toJson(groupMember)} teamInfo: ${gson.toJson(teamInfo)} mMembersList:${gson.toJson(mMembersList)}"
        )
//        val groupMember = UserGroupController.getInstance().getMemberInfo(id)
        if (TextUtils.isEmpty(groupMember?.user_id.toString())) {
            return -1
        }
        val teamInfo = teamInfo
        val view = View.inflate(application, R.layout.agroup_layout, null)
        val crHead = view.findViewById<View>(R.id.cr_head) as SkinImageView
        val headIv = view.findViewById<View>(R.id.head_iv) as SkinImageView
        val ivOfflineBg = view.findViewById<View>(R.id.iv_offline_bg) as SkinImageView
        ivOfflineBg.visibility =
            if (groupMember?.isOnline_status == true) View.GONE else View.VISIBLE
        crHead.setBackgroundResource(
            if (TextUtils.equals(
                    teamInfo?.master_user_id.toString(),
                    groupMember?.user_id.toString()
                )
            ) if (groupMember?.isOnline_status == true) R.drawable.ic_group_leader_head_portrait else R.drawable.ic_group_leader_offline_head_portrait
            else if (groupMember?.isOnline_status == true) R.drawable.ic_group_head_portrait else R.drawable.ic_group_offline_head_portrait
        )
        val url = groupMember?.head_img ?: ""
        Timber.i("addGroupMarker url:${!TextUtils.isEmpty(url) && userGroupBusiness.containsKey(url)}")
        if (!TextUtils.isEmpty(url) && userGroupBusiness.containsKey(url)) {
            val bitmap: Bitmap? = userGroupBusiness.getBitmapByUrl(url)
            if (bitmap == null) {
                headIv.setImageResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)
            } else {
                headIv.setImageBitmap(BitmapUtils.toRoundBitmap(bitmap))
            }
            headIv.alpha = if (groupMember?.isOnline_status == true) 1.0f else 0.4f
            dynamicId = addGroupLayerTexture(
                pLayer,
                id,
                strMarkerInfo,
                view,
                false,
                groupDynamicIds,
                dynamicMarkerId,
                styleJsonAnalysisUtil
            )
        } else {
            headIv.setImageResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)
            headIv.alpha = if (groupMember?.isOnline_status == true) 1.0f else 0.4f
            dynamicId = addGroupLayerTexture(
                pLayer,
                id,
                strMarkerInfo,
                view,
                false,
                groupDynamicIds,
                dynamicMarkerId,
                styleJsonAnalysisUtil
            )
        }
        return dynamicId
    }

    /**
     * 组队焦点态图层view
     * @param pLayer
     * @param strMarkerInfo
     * @return
     */
    @SuppressLint("SetTextI18n")
    @JvmStatic
    @Synchronized
    fun addGroupFocusMarker(
        application: Application?,
        pLayer: BaseLayer?,
        id: String,
        strMarkerInfo: String?,
        groupDynamicIds: MutableMap<String?, Int?>,
        dynamicMarkerId: Int,
        styleJsonAnalysisUtil: StyleJsonAnalysisUtil
    ): Int {
        var dynamicId = -1
        if (pLayer == null) {
            return dynamicId
        }
        val membersList = mMembersList
        if (membersList.isEmpty()) {
            return -1
        }
        val groupMember = mMembersList.find { id.toIntOrNull() == it.user_id }
        if (TextUtils.isEmpty(groupMember?.user_id.toString())) {
            return -1
        }
        val teamInfo = teamInfo
        Timber.i(
            "addGroupFocusMarker userId:$userId id:$id groupMember:${gson.toJson(groupMember)} memberList: ${
                gson.toJson(
                    membersList
                )
            } teamInfo: ${gson.toJson(teamInfo)}"
        )
        val view = View.inflate(application, R.layout.agroup_layout_focus, null)
        val tvNickName = view.findViewById<View>(R.id.name) as SkinTextView
        val crHead = view.findViewById<View>(R.id.cr_head) as SkinImageView
        val headIv = view.findViewById<View>(R.id.head_iv) as SkinImageView
        val vOnline = view.findViewById<View>(R.id.online) as SkinTextView
        val ivOfflineBg = view.findViewById<View>(R.id.iv_offline_bg) as SkinImageView
        ivOfflineBg.visibility =
            if (groupMember?.isOnline_status == true) View.GONE else View.VISIBLE

        /*val meTitle = view.findViewById<View>(R.id.me_title) as SkinTextView
        val meDis = view.findViewById<View>(R.id.me_dis) as SkinTextView
        val disTitle = view.findViewById<View>(R.id.dis_title) as SkinTextView
        val tvDis = view.findViewById<View>(R.id.tv_dis) as SkinTextView*/
        val updateTitle = view.findViewById<View>(R.id.update_title) as SkinTextView
        val updateTime = view.findViewById<View>(R.id.update_time) as SkinTextView
        val topLayout = view.findViewById<View>(R.id.top_layout) as SkinConstraintLayout

        vOnline.visibility = if (groupMember?.isOnline_status == true) View.GONE else View.VISIBLE
        tvNickName.text = when {
            !groupMember?.remark.isNullOrEmpty() -> groupMember?.remark
            !groupMember?.nick_name.isNullOrEmpty() -> groupMember?.nick_name
            else -> ""
        }
        /*if (userId != null && userId == groupMember?.user_id) {//自己
            meTitle.visibility = View.GONE
            meDis.visibility = View.GONE
            if (teamInfo?.lon?.isNotEmpty() == true && teamInfo.lat?.isNotEmpty() == true) {//有目的地
                disTitle.visibility = View.VISIBLE
                tvDis.visibility = View.VISIBLE
                val coord2DDouble =
                    OperatorPosture.mapToLonLat(teamInfo.lon?.toDouble()!!, teamInfo.lat?.toDouble()!!)
                val distantMeter = CommonUtil.distanceUnitTransformKm(
                    GeoPoint.calcDistanceBetweenPoints(
                        GeoPoint(coord2DDouble.lon, coord2DDouble.lat),
                        GeoPoint(groupMember?.latest_lon?.toDouble() ?: 0.0, groupMember?.latest_lat?.toDouble() ?: 0.0)
                    ).toLong()
                )
                tvDis.text = distantMeter
            } else {
                disTitle.visibility = View.GONE
                tvDis.visibility = View.GONE
            }
        } else {
            meTitle.visibility = View.VISIBLE
            meDis.visibility = View.VISIBLE
            val location = LocationController.getInstance().lastLocation
            val meDisMeter = CommonUtil.distanceUnitTransformKm(
                GeoPoint.calcDistanceBetweenPoints(
                    GeoPoint(location.longitude, location.latitude),
                    GeoPoint(groupMember?.latest_lon?.toDouble() ?: 0.0, groupMember?.latest_lat?.toDouble() ?: 0.0)
                ).toLong()
            )
            meDis.text = meDisMeter
            if (teamInfo?.lon?.isNotEmpty() == true && teamInfo.lat?.isNotEmpty() == true) {//有目的地
                disTitle.visibility = View.VISIBLE
                tvDis.visibility = View.VISIBLE
                val coord2DDouble =
                    OperatorPosture.mapToLonLat(teamInfo.lon?.toDouble()!!, teamInfo.lat?.toDouble()!!)
                val distantMeter = CommonUtil.distanceUnitTransformKm(
                    GeoPoint.calcDistanceBetweenPoints(
                        GeoPoint(coord2DDouble.lon, coord2DDouble.lat),
                        GeoPoint(groupMember?.latest_lon?.toDouble() ?: 0.0, groupMember?.latest_lat?.toDouble() ?: 0.0)
                    ).toLong()
                )
                tvDis.text = distantMeter
            } else {
                disTitle.visibility = View.GONE
                tvDis.visibility = View.GONE
            }
        }*/

        val lastTime = CommonUtil.switchTime(groupMember?.time_stamp ?: 0)
        updateTime.text = lastTime
        Timber.i("addGroupFocusMarker is master :${teamInfo?.master_user_id == groupMember?.user_id}")
        crHead.setBackgroundResource(
            if (teamInfo?.master_user_id == groupMember?.user_id) if (groupMember?.isOnline_status == true) R.drawable.ic_group_leader_head_portrait_select else R.drawable.ic_group_leader_offline_head_portrait_select else
                if (groupMember?.isOnline_status == true) R.drawable.ic_group_head_portrait_select else R.drawable.ic_group_offline_head_portrait_select
        )

        val url = groupMember?.head_img ?: ""
        Timber.i("addGroupFocusMarker url:${!TextUtils.isEmpty(url) && userGroupBusiness.containsKey(url)}")
        if (!TextUtils.isEmpty(url) && userGroupBusiness.containsKey(url)) {
            val bitmap: Bitmap? = userGroupBusiness.getBitmapByUrl(url)
            if (bitmap == null) {
                headIv.setImageResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)
            } else {
                headIv.setImageBitmap(BitmapUtils.toRoundBitmap(bitmap))
            }
            headIv.alpha = if (groupMember?.isOnline_status == true) 1.0f else 0.4f
            dynamicId = addGroupLayerTexture(
                pLayer,
                id,
                strMarkerInfo,
                view,
                true,
                groupDynamicIds,
                dynamicMarkerId,
                styleJsonAnalysisUtil
            )
        } else {
            headIv.setImageResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)
            headIv.alpha = if (groupMember?.isOnline_status == true) 1.0f else 0.4f
            dynamicId = addGroupLayerTexture(
                pLayer,
                id,
                strMarkerInfo,
                view,
                true,
                groupDynamicIds,
                dynamicMarkerId,
                styleJsonAnalysisUtil
            )
        }
        return dynamicId
    }

    private fun addGroupLayerTexture(
        pLayer: BaseLayer,
        id: String,
        strMarkerInfo: String?,
        view: View?,
        isFocus: Boolean,
        groupDynamicIds: MutableMap<String?, Int?>,
        dynamicMarkerId: Int,
        styleJsonAnalysisUtil: StyleJsonAnalysisUtil
    ): Int {
        val key = id + isFocus
        val dynamicBitmap = BitmapUtils.createBitmapFromView(view)
        val dataBuffer = ByteBuffer.allocate(dynamicBitmap.byteCount)
        dynamicBitmap.copyPixelsToBuffer(dataBuffer)
        val layerTexture = LayerTexture()
        layerTexture.dataBuff = BinaryStream(dataBuffer.array())
        layerTexture.width = dynamicBitmap.width.toLong()
        layerTexture.height = dynamicBitmap.height.toLong()
        layerTexture.iconType = LayerIconTypeBMP
        setAgroupLayerTexture(strMarkerInfo, layerTexture, styleJsonAnalysisUtil)
        var dynamicId = -1
        try {
            dynamicId = if (isFocus) id.toInt() + 10000 else id.toInt()
        } catch (e: Exception) {
            Timber.e("addGroupLayerTexture e:${e.message}")
        }
        if (groupDynamicIds.containsKey(key)) {
            //dynamicId = groupDynamicIds[key]!!
        } else {
            groupDynamicIds[key] = dynamicId
        }
        layerTexture.resID = dynamicId
        val isAddSuccess = pLayer.mapView.addLayerTexture(layerTexture)
        dynamicBitmap.recycle()
        if (!isAddSuccess) {
            Timber.d("AddDynamicMarder: 创建纹理:$key isAddSuccess = $isAddSuccess")
            return -1
        }

        return dynamicId
    }

    /**
     * 设置锚点
     * @param strMarkerInfo
     * @param layerTexture
     */
    @SuppressLint("WrongConstant")
    fun setAgroupLayerTexture(
        strMarkerInfo: String?,
        layerTexture: LayerTexture,
        styleJsonAnalysisUtil: StyleJsonAnalysisUtil
    ) {
        val markerInfoBean = styleJsonAnalysisUtil.getMarkerInfoFromJson(strMarkerInfo)
        Timber.d(
            "====setLayerTexture strMarkerInfo = %s, markerInfoBean = %s",
            strMarkerInfo,
            markerInfoBean
        )
        if (markerInfoBean != null) {
            layerTexture.anchorType = markerInfoBean.anchor
            layerTexture.xRatio = markerInfoBean.x_ratio
            layerTexture.yRatio = markerInfoBean.y_ratio
            layerTexture.isRepeat = markerInfoBean.repeat == 1
            layerTexture.isGenMipmaps = markerInfoBean.gen_mipmaps == 1
            layerTexture.isPreMulAlpha =
                true //纹理是否预乘透明通道,1：预乘；0：未预乘  bitmap Image are loaded with the {@link Bitmap.Config#ARGB_8888} config by default
        } else {
            layerTexture.anchorType = LayerIconAnchor.LayerIconAnchorCenter
            layerTexture.isRepeat = false
            layerTexture.xRatio = 0f
            layerTexture.yRatio = 0f
            layerTexture.isGenMipmaps = false
            layerTexture.isPreMulAlpha = true
        }
    }

    /**
     * 由主样式对象传递
     * @param dynamicMarkerId
     * @return
     */
    private fun getDynamicMarkerId(dynamicMarkerId: Int): Int {
        var dynamicMarkerId = dynamicMarkerId
        if (dynamicMarkerId >= 0x60000) {
            dynamicMarkerId = 0
        }
        return ++dynamicMarkerId
    }
}