package com.desaysv.psmap.base.data

import android.app.Application
import android.graphics.Rect
import com.autonavi.gbl.aosclient.model.GRestrictRule
import com.autonavi.gbl.common.model.RectDouble
import com.autonavi.gbl.common.path.model.PointType
import com.autonavi.gbl.common.path.option.PathInfo
import com.autonavi.gbl.map.model.PreviewParam
import com.autonavi.gbl.route.model.RouteControlKey
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.layer.DrivingLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.navi.route.callback.IRouteResultCallBack
import com.autosdk.bussiness.navi.route.model.RouteCarResultData
import com.autosdk.bussiness.navi.route.model.RouteRequestParam
import com.autosdk.bussiness.widget.route.model.RoutePathItemContent
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.bussiness.widget.setting.SettingConst
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.business.EngineerBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.CommonUtils
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author 谢锦华
 * @time 2024/1/10
 * @description 路线页面的方法
 */

@Singleton
class RouteRepository @Inject constructor(
    private val mSettingComponent: ISettingComponent,
    private val mNaviController: NaviController,
    private val mRouteRequestController: RouteRequestController,
    private val mMapController: MapController,
    private val mLayerController: LayerController,
    private val application: Application,
    private val mNetWorkManager: NetWorkManager,
    private val gson: Gson,
    private val engineerBusiness: EngineerBusiness,
    private val iCarInfoProxy: ICarInfoProxy,
) :
    IRouteRepository {

    //导航引导图层
    private val mDrivingLayer: DrivingLayer? by lazy {
        mLayerController.getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    /**
     * 开始路径规划
     * startPoi：起点POI
     * endPoi：终点POI
     * midPois：途经点POI列表
     */
    override fun planRoute(
        startPoi: POI,
        endPoi: POI,
        midPois: ArrayList<POI>?,
        routeResultCallBack: IRouteResultCallBack
    ): Long {
        startPoi.type = PointType.PointTypeStart
        endPoi.type = PointType.PointTypeEnd
        Timber.i("RouteRepository startPoi=${gson.toJson(startPoi)}")
        Timber.i("RouteRepository endPoi=${gson.toJson(endPoi)}")
        Timber.i("RouteRepository midPois=${gson.toJson(midPois)}")

        //获取当前车位为起点
        val routeRequestParam = RouteRequestParam(startPoi, endPoi)
        midPois?.takeIf { it.isNotEmpty() }?.let {
            it.forEach { misPoi ->
                misPoi.type = PointType.PointTypeVia
            }
            routeRequestParam.midPois = it
        }

        //需要设置算路策略、车牌等
        val routePrefer = mSettingComponent.getConfigKeyPlanPref()
        routeRequestParam.routeStrategy = AutoRouteUtil.getRouteStrategy(routePrefer, mNetWorkManager.isNetworkConnected())
        routeRequestParam.routeConstrainCode =
            AutoRouteUtil.getRouteConstrainCode(routePrefer, engineerBusiness.elecContinue.value!!, mNetWorkManager.isNetworkConnected())

        Timber.i("RouteRepository:routeRequestParam.routeStrategy=${routeRequestParam.routeStrategy}, routeConstrainCode=${routeRequestParam.routeConstrainCode}")

        //通过设置模块获取车牌号
        val plateNumber: String = mSettingComponent.getLicensePlateNumber()
        routeRequestParam.carPlate = plateNumber

        //通过设置模块获取是否避开限行
        val isEmptyNumber = plateNumber.isNotEmpty()
        val avoidLimitConfig = mSettingComponent.getConfigKeyAvoidLimit()
        routeRequestParam.openAvoidLimit = isEmptyNumber == true && avoidLimitConfig

        //算路前要设置导航播报
        val mCurrentBroadcastMode = mSettingComponent.getConfigKeyBroadcastMode()
        /**
         * 播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
         */
        if (mCurrentBroadcastMode == SettingConst.BROADCAST_EASY) {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "4");
        } else if (mCurrentBroadcastMode == SettingConst.BROADCAST_MINIMALISM) {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "6");
        } else {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "2");
        }
        if (mNaviController.isNaving) {
            routeRequestParam.invokerType = "navi"
        } else {
            routeRequestParam.invokerType = "plan"
        }
        return mRouteRequestController.requestRoute(routeRequestParam, routeResultCallBack)
    }

    override fun planAimRoutePushMsgRoute(pushMsg: AimRoutePushMsg?, routeResultCallBack: IRouteResultCallBack): Long {
        if (pushMsg == null) {
            return 0
        }
        //算路前要设置导航播报
        val mCurrentBroadcastMode = mSettingComponent.getConfigKeyBroadcastMode()
        /**
         * 播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
         */
        if (mCurrentBroadcastMode == SettingConst.BROADCAST_EASY) {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "4");
        } else if (mCurrentBroadcastMode == SettingConst.BROADCAST_MINIMALISM) {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "6");
        } else {
            mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "2");
        }
        return mRouteRequestController.requestRouteRestoration(pushMsg, routeResultCallBack)
    }

    /**
     * 获取路线
     */
    override fun getVariantPathWrapList(mRouteCarResult: RouteCarResultData?): ArrayList<PathInfo> {
        if (mRouteCarResult == null) return ArrayList()
        val pathResult = mRouteCarResult.pathResult ?: return ArrayList()
        val count = pathResult.size
        if (count == 0) {
            return ArrayList()
        }
        val pathInfos = ArrayList<PathInfo>()
        for (i in 0 until count) {
            val navigationPath = pathResult[i]
            pathInfos.add(navigationPath)
        }
        return pathInfos
    }

    /**
     * 获取路线数据
     */
    override fun getItemContentList(pathWrapList: List<PathInfo>?): ArrayList<RoutePathItemContent> {
        var pathWrapList = pathWrapList
        val list: ArrayList<RoutePathItemContent> = ArrayList()
        if (null == pathWrapList) {
            pathWrapList = ArrayList()
        }
        for (i in pathWrapList.indices) {
            val variantPathWrap = pathWrapList[i]
            val routePathItemContent = RoutePathItemContent()
            val labelCount = variantPathWrap.labelInfoCount.toInt()
            val index: Short = 0
            val labelInfo = variantPathWrap.getLabelInfo(index)
            val elecPathInfo = variantPathWrap.elecPathInfo
            elecPathInfo?.let { info ->
                routePathItemContent.isElecRoute = info.mIsElecRoute
                //LogUtils.d("新能源：" + info.mIsElecRoute);
                info.mEnergyConsume?.vehiclechargeleft?.forEach { integer ->
                    //LogUtils.d("新能源电量：" + integer);
                }
                //判断是否电动汽车导航true是存在
                if (info.mIsElecRoute && !info.mEnergyConsume?.vehiclechargeleft.isNullOrEmpty()) {
                    //剩余电量【单位：0.01瓦时】-1代表不可到达。 按行程点排序。途经1剩余电量，途经地2剩余电量....目的地剩余电量
                    routePathItemContent.vehiclechargeleft = info.mEnergyConsume.vehiclechargeleft
                }
            }
            val content = labelInfo?.content ?: ""
            Timber.v("drivePathAccessor.getChargeStationInfo()=${variantPathWrap.chargeStationInfo}")
            routePathItemContent.title = when (i) {
                0 -> if (content.isEmpty()) "推荐" else content.replace("备选一", "推荐")
                    .replace("备选方案一", "推荐")

                1 -> if (content.isEmpty()) "备选一" else content.replace("备选二", "备选一")
                    .replace("备选方案二", "备选一")

                2 -> if (content.isEmpty()) "备选二" else content.replace("备选三", "备选二")
                    .replace("备选方案三", "备选二")

                else -> ""
            }
            routePathItemContent.content = content
            routePathItemContent.travelTime = variantPathWrap.travelTime
            routePathItemContent.length = variantPathWrap.length
            routePathItemContent.trafficLightCount = variantPathWrap.trafficLightCount
            routePathItemContent.tollCost = variantPathWrap.tollCost.toLong()

            //街道数量
            val gSegCount = variantPathWrap.groupSegmentCount.toInt()
            routePathItemContent.streetNamesSize = gSegCount
            val streetNames: MutableList<String> = ArrayList()
            for (j in 0 until gSegCount) {
                val gSeg = variantPathWrap.getGroupSegment(j.toLong())
                streetNames.add(gSeg.roadName)
            }
            //街道名称
            routePathItemContent.streetNames = streetNames
            list.add(routePathItemContent)
        }
        return list
    }

    /**
     * 设置全览
     */
    override fun showPreview() {
        mDrivingLayer?.getPathResultBound(mRouteRequestController.carRouteResult?.pathResult)?.let { rectDouble ->
            val previewParam = PreviewParam().apply {
                // 进行预览
                val mapPreviewRect = getMapPreviewRect()
                leftOfMap = CommonUtils.getAutoDimenValue(
                    application,
                    if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1100 else R.dimen.sv_dimen_0
                )
                topOfMap = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_370)
                screenLeft = mapPreviewRect.left
                screenTop = mapPreviewRect.top
                screenRight = mapPreviewRect.right
                screenBottom = mapPreviewRect.bottom
                bUseRect = true
                mapBound = rectDouble
            }
            mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                ?.showPreview(previewParam, true, 500, -1)
        }
    }

    /**
     * 设置全览
     */
    override fun showPreview(curSegIdx: Long, curLinkIdx: Long, curPointIdx: Long) {
        Timber.i("showPreview curSegIdx = $curSegIdx，curLinkIdx = $curLinkIdx，curPointIdx = $curPointIdx")
        mRouteRequestController.carRouteResult?.pathResult?.takeIf { it.isNotEmpty() }?.get(mRouteRequestController.carRouteResult.focusIndex)
            ?.let { pathInfo ->
                pathInfo.getBound(curSegIdx, curLinkIdx, curPointIdx)?.let { rectDouble ->
                    // 进行预览
                    val mapPreviewRect = getMapPreviewRect()
                    val previewParam = PreviewParam().apply {
                        leftOfMap = CommonUtils.getAutoDimenValue(
                            application,
                            if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1100 else R.dimen.sv_dimen_0
                        )
                        topOfMap = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_370)
                        screenLeft = mapPreviewRect.left
                        screenTop = mapPreviewRect.top
                        screenRight = mapPreviewRect.right
                        screenBottom = mapPreviewRect.bottom
                        bUseRect = true
                        mapBound = rectDouble
                    }
                    mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                        ?.showPreview(previewParam, true, 500, -1)
                }
            }
    }

    /**
     * 设置限行全览
     */
    override fun showRestrictPreview(rule: GRestrictRule) {
        val rectDouble = RectDouble(rule.bound.left, rule.bound.right, rule.bound.top, rule.bound.bottom)
        val previewParam = PreviewParam().apply {
            val mapPreviewRect = getMapPreviewRect()
            leftOfMap = CommonUtils.getAutoDimenValue(
                application,
                if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1100 else R.dimen.sv_dimen_0
            )
            topOfMap = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_370)
            screenLeft = mapPreviewRect.left
            screenTop = mapPreviewRect.top
            screenRight = mapPreviewRect.right
            screenBottom = mapPreviewRect.bottom
            bUseRect = true
            mapBound = rectDouble
        }
        mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
            ?.showPreview(previewParam, true, 500, -1)
    }

    override fun getMapPreviewRect(): Rect {
        var rect = Rect()
        rect.left =
            CommonUtils.getAutoDimenValue(
                application,
                if (iCarInfoProxy.getScreenStatus().value == true) R.dimen.sv_dimen_1500 else R.dimen.sv_dimen_780
            )
        rect.top = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_200)
        rect.right = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_200)
        rect.bottom = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_200)
        return rect
    }
}