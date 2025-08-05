package com.desaysv.psmap.model.business

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.aosclient.model.GWsTserviceInternalLinkCarReportRequestParam
import com.autonavi.gbl.aosclient.model.WsTserviceInternalLinkCarReportNaviLocInfo
import com.autonavi.gbl.aosclient.model.WsTserviceInternalLinkCarReportPoiInfo
import com.autonavi.gbl.aosclient.observer.ICallBackWsTserviceInternalLinkCarGet
import com.autonavi.gbl.aosclient.observer.ICallBackWsTserviceInternalLinkCarReport
import com.autonavi.gbl.common.path.option.RouteStrategy
import com.autonavi.gbl.user.msgpush.model.AimPoiInfo
import com.autonavi.gbl.user.msgpush.model.AimPoiPushMsg
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autonavi.gbl.user.msgpush.model.DestinationPushMsg
import com.autonavi.gbl.user.msgpush.model.LinkStatusPushMsg
import com.autonavi.gbl.user.msgpush.model.MsgPoiInfo
import com.autonavi.gbl.user.msgpush.model.PlanPrefPushMsg
import com.autonavi.gbl.user.msgpush.model.QuitNaviPushMsg
import com.autonavi.gbl.user.msgpush.model.SceneSendType
import com.autosdk.R
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.account.LinkCarController
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.push.listener.AimPushMessageListener
import com.autosdk.bussiness.push.listener.LinkPushMessageListener
import com.autosdk.bussiness.widget.setting.SettingComponent
import com.autosdk.common.AutoState
import com.autosdk.common.NaviStateListener
import com.autosdk.common.tts.IAutoPlayer
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.base.bean.DestinationData
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.data.NaviRepository
import com.desaysv.psmap.base.handle.ReportCarLocHandler
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ILinkStatusChangeListener
import com.desaysv.psmap.base.utils.AppUtils
import com.google.gson.Gson
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 手车互联管理
 * @author AutoSDK
 */
@Singleton
class LinkCarBusiness @Inject constructor(
    private val application: Application,
    private val pushMessageBusiness: PushMessageBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val linkCarController: LinkCarController,
    private val mReportCarLocHandler: ReportCarLocHandler,
    private val mRouteRequestController: RouteRequestController,
    private val settingComponent: SettingComponent,
    private val iCarInfoProxy: ICarInfoProxy,
    private val naviRepository: NaviRepository,
    private val gson: Gson,
    private val ttsPlayer: IAutoPlayer
) {

    companion object {
        private const val DELAYED_TIME = 60 * 1000
        private const val TIME = 1
    }

    /**
     * 客户端请求轨迹
     */
    private var traceId = ""
    private var linkStatus = 0
    private var naviCalcuResult = 0
    private var isInNavi = false
    private var dataChange = 0
    private var mRequestParam: GWsTserviceInternalLinkCarReportRequestParam? = null

    /**
     * 行程点POI名称
     */
    private var destinationName: String? = null

    /**
     * 途经点名称
     */
    private var wayPoiName: String? = null

    /**
     * 路线偏好
     */
    private var planPrefName: String? = null

    /**
     * 取消的算路偏好名称
     */
    private var cancelPlanPrefName: String? = null

    /**
     * 手机下发的算路策略
     */
    private var mPlanPref: String? = null
    private var sendType = 0
    private var mPlanPrefs: ArrayList<Int>? = null
    private var mEndPoi: POI? = null
    private val midPois: ArrayList<POI> = ArrayList()
    private var mILinkStatusChangeListenerList: MutableList<ILinkStatusChangeListener>? = CopyOnWriteArrayList()
    val onReRouteFromPlanPref = MutableLiveData<String?>() //更改路线偏好后重新算路
    val onChangeDestination = MutableLiveData<DestinationData>() //手车互联变更目的地或者途经点
    val linkRoutePushMessage = MutableLiveData<AimRoutePushMsg?>() //手车互联路线通知
    val notifyRoutePushMessage = MutableLiveData<AimRoutePushMsg?>() //路线消息推送接收
    val notifyPoiPushMessage = MutableLiveData<AimPoiPushMsg?>() //POI消息接收
    val startNavi = MutableLiveData<POI?>() //开始导航
    val exitNavi = MutableLiveData<Boolean>() //结束导航

    private val screenStatus = iCarInfoProxy.getScreenStatus()
    private val launcherStatus = iCarInfoProxy.getLauncherStatus()

    private val handle: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                TIME -> {
                    dataChange = 0
                    startLinkCarNoHandleReport()
                    sendEmptyMessageDelayed(TIME, DELAYED_TIME.toLong())
                }

                else -> {}
            }
        }
    }

    fun addLinkPhoneNaviStatus() {
        SdkAdapterManager.getInstance().addNaviStatusListener(naviStateListener)
    }

    fun removeLinkPhoneNaviStatus() {
        SdkAdapterManager.getInstance().removeNaviStatusListener(naviStateListener)
    }

    fun startLinkPhone() {
        pushMessageBusiness.addLinkPushMessageListener(mLinkPushMessageListener)
        pushMessageBusiness.addSend2carPushMsgListener(mAimPushMessageListener)
        linkCarController.getLinkPhoneStatus(iCallBackWsTServiceInternalLinkCarGet)
        startLinkCarReport()
        mReportCarLocHandler.doStop()
        mReportCarLocHandler.doStart()
    }

    private fun startLinkCarReport() {
//        linkCarController.startLinkCarReport(linkCarReportRequestParam, iCallBackWsTServiceInternalLinkCarReport)
        traceId = ""
        handle.sendEmptyMessageDelayed(TIME, DELAYED_TIME.toLong())
    }

    fun startLinkCarNoHandleReport() {
//        linkCarController.startLinkCarReport(linkCarReportRequestParam, iCallBackWsTServiceInternalLinkCarReport)
        traceId = ""
    }

    private val linkCarReportRequestParam: GWsTserviceInternalLinkCarReportRequestParam
        private get() {
            if (mRequestParam == null) {
                mRequestParam = GWsTserviceInternalLinkCarReportRequestParam()
            }
            mRequestParam!!.appType = "1"
            val location = mLocationBusiness.getLastLocation()
            if (location != null) {
                val naviLocInfo = WsTserviceInternalLinkCarReportNaviLocInfo()
                naviLocInfo.lon = location.longitude
                naviLocInfo.lat = location.latitude
                mRequestParam!!.naviLocInfo = naviLocInfo
            }
            mRequestParam!!.endPoi = WsTserviceInternalLinkCarReportPoiInfo()
            val endPoi = endPoi
            if (endPoi != null) {
                mRequestParam!!.endPoi = endPoi
            }
            val midPoiList = midPoiList
            if (midPoiList != null && midPoiList.size > 0) {
                mRequestParam!!.midPois = midPoiList
            } else {
                mRequestParam!!.midPois.clear()
            }
            mRequestParam!!.naviStatus = if (isInNavi) 1 else 0
            mRequestParam!!.clientTraceId = traceId
            mRequestParam!!.dataChange = dataChange
            var poiName: String? = null
            if (mRequestParam!!.endPoi != null) {
                poiName = mRequestParam!!.endPoi.name
            }
            Timber.d(
                "====link Link getLinkCarReportRequestParam clientTraceId = %s， dataChange = %s, endPoi = %s",
                mRequestParam!!.clientTraceId,
                mRequestParam!!.dataChange,
                poiName
            )
            mRequestParam!!.naviCalcuResult = naviCalcuResult
            if (isInNavi) {
                //如果在行中，则取相关的终点信息
            }
            return mRequestParam as GWsTserviceInternalLinkCarReportRequestParam
        }

    /**
     * 停止手车互联
     */
    fun stopLinkCar() {
        linkStatus = 0
        handle.removeCallbacksAndMessages(null)
        mReportCarLocHandler.doStop()
        pushMessageBusiness.removeSend2carPushMsgListener(mAimPushMessageListener)
        mILinkStatusChangeListenerList!!.clear()
        mEndPoi = null
        midPois.clear()
    }

    private val endPoi: WsTserviceInternalLinkCarReportPoiInfo?
        private get() {
            val resultData = mRouteRequestController.carRouteResult
            var poiInfo: WsTserviceInternalLinkCarReportPoiInfo? = null
            if (resultData != null) {
                val endPoi = resultData.toPOI
                poiInfo = poiToReportPoi(endPoi)
            } else {
                if (mEndPoi != null) {
                    poiInfo = poiToReportPoi(mEndPoi!!)
                }
            }
            return poiInfo
        }

    private fun poiToReportPoi(poi: POI): WsTserviceInternalLinkCarReportPoiInfo {
        val poiInfo = WsTserviceInternalLinkCarReportPoiInfo()
        val endGeoPoint = poi.point
        poiInfo.lon = endGeoPoint.longitude
        poiInfo.lat = endGeoPoint.latitude
        poiInfo.adcode = poi.adCode
        poiInfo.address = poi.addr
        poiInfo.name = poi.name
        poiInfo.poiid = poi.id
        return poiInfo
    }

    private val midPoiList: ArrayList<WsTserviceInternalLinkCarReportPoiInfo>?
        private get() {
            val resultData = mRouteRequestController.carRouteResult
            var midReoportPois: ArrayList<WsTserviceInternalLinkCarReportPoiInfo>? = null
            if (resultData != null) {
                val midPoiList = resultData.midPois
                if (midPoiList != null && midPoiList.size > 0) {
                    midReoportPois = ArrayList()
                    for (midPoi in midPoiList) {
                        val poiInfo = poiToReportPoi(midPoi)
                        midReoportPois.add(poiInfo)
                    }
                }
            } else {
                if (midPois != null && midPois.size > 0) {
                    midReoportPois = ArrayList()
                    for (poi in midPois) {
                        val poiInfo = poiToReportPoi(poi)
                        midReoportPois.add(poiInfo)
                    }
                }
            }
            return midReoportPois
        }

    private fun aimPoiInfoToPoi(msgPoiInfo: AimPoiInfo?): POI? {
        if (msgPoiInfo == null) {
            return null
        }
        var poi: POI? = null
        poi = if (msgPoiInfo.navLon != 0 && msgPoiInfo.navLat != 0) {
            val lon = java.lang.Double.valueOf(msgPoiInfo.navLon.toDouble()) / 1000000
            val lat = java.lang.Double.valueOf(msgPoiInfo.navLat.toDouble()) / 1000000
            POIFactory.createPOI(msgPoiInfo.name, GeoPoint(lon, lat), msgPoiInfo.poiId)
        } else {
            val lon = java.lang.Double.valueOf(msgPoiInfo.lon.toDouble()) / 1000000
            val lat = java.lang.Double.valueOf(msgPoiInfo.lat.toDouble()) / 1000000
            POIFactory.createPOI(msgPoiInfo.name, GeoPoint(lon, lat), msgPoiInfo.poiId)
        }
        Timber.d(
            "====link aimPoiInfoToPoi lon = %s, lat = %s",
            poi.point.longitude,
            poi.point.latitude
        )
        poi.addr = msgPoiInfo.address
        return poi
    }

    private fun msgPoiInfoToPoi(msgPoiInfo: MsgPoiInfo?): POI? {
        if (msgPoiInfo == null) {
            return null
        }
        var poi: POI? = null
        if (msgPoiInfo.poiLoc.lon != 0.0 && msgPoiInfo.poiLoc.lat != 0.0) {
            poi = POIFactory.createPOI(msgPoiInfo.name, GeoPoint(msgPoiInfo.poiLoc.lon, msgPoiInfo.poiLoc.lat), msgPoiInfo.poiId)
            Timber.d("====link msgPoiInfoToPoi lon = %s, lat = %s", poi.point.longitude, poi.point.latitude)
            poi.addr = msgPoiInfo.address
        }
        return poi
    }

    private fun playResult(content: String) {
        if (!TextUtils.isEmpty(content)) {
            if (ttsPlayer.isPlaying) {
                ttsPlayer.stop(false)
            }
            ttsPlayer.playText(content)
        }
    }

    fun addLinkStatusChangeListener(listener: ILinkStatusChangeListener) {
        if (mILinkStatusChangeListenerList == null) {
            mILinkStatusChangeListenerList = CopyOnWriteArrayList()
        }
        synchronized(mILinkStatusChangeListenerList!!) {
            if (!mILinkStatusChangeListenerList!!.contains(listener)) {
                mILinkStatusChangeListenerList!!.add(listener)
            }
        }
    }

    fun removeLinkStatusChangeListener(listener: ILinkStatusChangeListener) {
        if (mILinkStatusChangeListenerList == null) {
            return
        }
        synchronized(mILinkStatusChangeListenerList!!) {
            if (mILinkStatusChangeListenerList != null) {
                mILinkStatusChangeListenerList!!.remove(listener)
            }
        }
    }

    private fun setPlanPref(planPref: Int): String? {
        //路线偏好 0:无，2:躲避拥堵, 4:避免收费, 8:不走高速, 16:高速优先, 32:大路优先, 64:速度最快;
        var planPrefString: String? = null
        when (planPref) {
            0 ->                 //高德推荐（智能推荐）
                planPrefString = ResUtil.getString(R.string.preference_default_desc)

            2 ->                 //躲避拥堵
                planPrefString = ResUtil.getString(R.string.preference_avoid_jan_desc)

            4 ->                 //避免收费
                planPrefString = ResUtil.getString(R.string.preference_avoid_charge_desc)

            8 ->                 //不走高速
                planPrefString = ResUtil.getString(R.string.preference_avoid_highway_desc)

            16 ->                 //高速优先
                planPrefString = ResUtil.getString(R.string.preference_using_highway_desc)

            32 ->                 //大路优先
                planPrefString = ResUtil.getString(R.string.preference_personal_width_first)

            64 ->                 //速度最快
                planPrefString =
                    ResUtil.getString(R.string.preference_personal_speed_first_desc)

            else -> {}
        }
        return planPrefString
    }

    private fun sortPlanPrefList(planPrefs: ArrayList<Int>): ArrayList<Int> {
        planPrefs.sortWith(Comparator { o1, o2 ->
            val diff = (o2 as Int) - (o1 as Int)
            if (diff > 0) {
                -1
            } else if (diff < 0) {
                1
            } else {
                0
            }
        })
        return planPrefs
    }

    /**
     * 针对取消算路偏好进行处理，将取消算路偏好转换成最终需要传入进行算路的算路偏好
     * @param planPrefs
     * @return
     */
    private fun handleCandlePlanPrefList(planPrefs: ArrayList<Int>): ArrayList<Int> {
        //将收到的算路偏好list进行取消的重组
        val tempPlanPrefs = ArrayList<Int>()
        tempPlanPrefs.addAll(mPlanPrefs!!)
        val prefNames = StringBuffer()
        for (i in planPrefs.indices) {
            val name = setPlanPref(planPrefs[i])
            prefNames.append(name)
            if (tempPlanPrefs.contains(planPrefs[i])) {
                tempPlanPrefs.remove(planPrefs[i])
            }
        }
        if (tempPlanPrefs.size == 0) {
            tempPlanPrefs.add(0)
        }
        cancelPlanPrefName = prefNames.toString()
        return tempPlanPrefs
    }

    /**
     * 获取第一次路线推送下发的算路偏好
     * @param strategy
     * @return
     */
    private fun getNaviStrategy(strategy: String): ArrayList<Int>? {
        val prefList = ArrayList<Int>()
        if (TextUtils.isEmpty(strategy)) {
            return null
        }
        when (strategy.toInt()) {
            RouteStrategy.RouteStrategyPersonalGaodeBest ->                 //高德推荐
                prefList.add(0)

            RouteStrategy.RouteStrategyPersonalTMC ->                 //躲避拥堵
                prefList.add(2)

            RouteStrategy.RouteStrategyPersonalTMC2Highway -> {
                //躲避拥堵+高速优先
                prefList.add(2)
                prefList.add(16)
            }

            RouteStrategy.RouteStrategyPersonalTMC2LessHighway -> {
                //躲避拥堵+不走高速
                prefList.add(2)
                prefList.add(8)
            }

            RouteStrategy.RouteStrategyPersonalTMC2LessMondy2LessHighway -> {
                //躲避拥堵+不走高速+少收费
                prefList.add(2)
                prefList.add(4)
                prefList.add(8)
            }

            RouteStrategy.RequestRouteTypeTMCFree -> {
                //躲避拥堵+少收费
                prefList.add(2)
                prefList.add(4)
            }

            RouteStrategy.RouteStrategyPersonalTMC2WidthFirst -> {
                //躲避拥堵+大路优先
                prefList.add(2)
                prefList.add(64)
            }

            RouteStrategy.RouteStrategyPersonalTMC2SpeedFirst -> {
                //躲避拥堵+速度最快
                prefList.add(2)
                prefList.add(32)
            }

            RouteStrategy.RouteStrategyPersonalSpeedFirst ->                 //速度最快
                prefList.add(32)

            RouteStrategy.RouteStrategyPersonalHighwayFirst ->                 //高速优先
                prefList.add(16)

            RouteStrategy.RouteStrategyPersonalLessHighway ->                 //不走高速
                prefList.add(8)

            RouteStrategy.RouteStrategyPersonalLessMoney2LessHighway -> {
                //不走高速+少收费
                prefList.add(4)
                prefList.add(8)
            }

            RouteStrategy.RouteStrategyPersonalWidthFirst ->                 //大路优先
                prefList.add(64)

            RouteStrategy.RouteStrategyPersonalLessMoney ->                 //少收费
                prefList.add(4)

            else -> {}
        }
        return prefList
    }

    private val mLinkPushMessageListener: LinkPushMessageListener = object : LinkPushMessageListener {
        override fun notifyDestinationPushMessage(destinationPushMsg: DestinationPushMsg?) {
            if (destinationPushMsg?.content == null) {
                return
            }
            traceId = destinationPushMsg.traceId
            //收到终点信息
            Timber.d("====link Link notifyMessage DestinationPushMsg traceId = %s", traceId)
            //手机侧语音规划算路推送走的这个接口
            if (destinationPushMsg.content != null) {
                val endPoi = msgPoiInfoToPoi(destinationPushMsg.content.endPoi)
                mEndPoi = endPoi
                AppUtils.startOrBringActivityToFront(application)
                startNavi.postValue(endPoi)
            }
        }

        override fun notifyLinkStatusPushMessage(linkStatusPushMsg: LinkStatusPushMsg?) {
            if (linkStatusPushMsg != null) {
                traceId = linkStatusPushMsg.traceId
                linkStatus = linkStatusPushMsg.content.status
                Timber.d(
                    "====link Link notifyMessage LinkStatusPushMsg traceId = %s, status = %s",
                    traceId,
                    linkStatusPushMsg.content.status
                )
                if (mILinkStatusChangeListenerList != null && mILinkStatusChangeListenerList!!.size > 0) {
                    for (i in mILinkStatusChangeListenerList!!.indices) {
                        mILinkStatusChangeListenerList!![i].notifyLinkPhoneStatus(linkStatus == 1)
                    }
                }
            }
        }

        override fun notifyQuitNaviPushMessage(quitNaviPushMsg: QuitNaviPushMsg?) {
            traceId = quitNaviPushMsg?.traceId.toString()
            Timber.d("====link Link notifyMessage quitNaviPushMsg traceId = %s", traceId)
            //退出导航
            exitNavi.postValue(true)
            mEndPoi = null
            midPois.clear()
        }

        override fun notifyPlanPrefPushMessage(planPrefPushMsg: PlanPrefPushMsg?) {
            //修改设置中的路线偏好
            if (planPrefPushMsg?.content == null) {
                return
            }
            traceId = planPrefPushMsg.traceId
            Timber.d("====link Link notifyMessage PlanPrefPushMsg traceId = %s", traceId)
            sendType = planPrefPushMsg.sendType
            val planPrefs = planPrefPushMsg.content.planPrefs
            val tempPlanPrefs: ArrayList<Int>? =
                if (sendType == SceneSendType.SceneTypeCancelPlanPref) {
                    //取消算路偏好
                    handleCandlePlanPrefList(planPrefs)
                } else {
                    planPrefs
                }
            mPlanPrefs = tempPlanPrefs
            if (tempPlanPrefs != null && tempPlanPrefs.size > 0) {
                if (tempPlanPrefs.size == 1) {
                    val planPref = tempPlanPrefs[0]
                    planPrefName = setPlanPref(planPref)
                    mPlanPref = if (planPref == 32) {
                        64.toString()
                    } else if (planPref == 64) {
                        32.toString()
                    } else {
                        planPref.toString()
                    }
                } else {
                    val planPrefSb = StringBuffer()
                    val planPrefNameSb = StringBuffer()
                    sortPlanPrefList(planPrefs)
                    for (i in planPrefs.indices) {
                        val planPref = planPrefs[i]
                        val planPrefNameStr = setPlanPref(planPref)
                        planPrefNameSb.append(planPrefNameStr)
                        var sdkPlanPref = planPref
                        if (planPref == 32) {
                            sdkPlanPref = 64
                        } else if (planPref == 64) {
                            sdkPlanPref = 32
                        }
                        planPrefSb.append(sdkPlanPref)
                        if (i < planPrefs.size - 1) {
                            planPrefSb.append("|")
                        }
                    }
                    planPrefName = planPrefNameSb.toString()
                    mPlanPref = planPrefSb.toString()
                    Timber.i(
                        "====link notifyPlanPrefPushMessage planPrefName = %s, PlanPref = %s",
                        planPrefName,
                        mPlanPref
                    )
                }
                settingComponent.configKeyPlanPref = mPlanPref
                onReRouteFromPlanPref.postValue(mPlanPref)
            }

        }
    }

    private val mAimPushMessageListener: AimPushMessageListener = object : AimPushMessageListener {
        override fun notifyPoiPushMessage(aimPoiPushMsg: AimPoiPushMsg?) {
            try {
                //针对更改目的地类型进行处理
                if (aimPoiPushMsg == null) {
                    return
                }
                traceId = aimPoiPushMsg.traceId
                //收到终点信息
                val sendType = aimPoiPushMsg.sendType
                Timber.d("====link Link notifyMessage AimPoiPushMsg traceId = %s， sendType = %s", traceId, sendType)
                //要先对目的地进行转换
                if (aimPoiPushMsg.content != null && !TextUtils.isEmpty(traceId)) {
                    if (naviRepository.isRealNavi()) {
                        val poi = aimPoiInfoToPoi(aimPoiPushMsg.content)
                        Timber.e("aimPoiPushMsg aimPoiPushMsg.content != null poi:${gson.toJson(poi)}")
                        if (poi != null) {
                            if (sendType == SceneSendType.SceneTypeChangeDestination) {
                                //更改目的地
                                destinationName = poi.name
                                mEndPoi = poi
                                onChangeDestination.postValue(DestinationData(poi, sendType, mPlanPref ?: settingComponent.configKeyPlanPref))
                            } else if (sendType == SceneSendType.SceneTypeAddPathPoint) {
                                //添加途经点
                                wayPoiName = poi.name
                                onChangeDestination.postValue(DestinationData(poi, sendType, mPlanPref ?: settingComponent.configKeyPlanPref))
                            } else if (sendType == SceneSendType.SceneTypeDelPathPoint) {
                                //删除途经点
                                wayPoiName = poi.name
                                onChangeDestination.postValue(DestinationData(poi, sendType, mPlanPref ?: settingComponent.configKeyPlanPref))
                            }
                        } else {
                            Timber.i("POI is null after conversion from aimPoiPushMsg.content")
                        }
                    } else {
                        Timber.i("aimPoiPushMsg 非导航")
                    }
                } else {
                    Timber.i("aimPoiPushMsg.content is null or traceId is empty")
                    notifyPoiPushMessage.postValue(aimPoiPushMsg)
                }
            } catch (e: Exception) {
                Timber.e("Exception:${e.message}")
            }
        }

        override fun notifyRoutePushMessage(aimRoutePushMsg: AimRoutePushMsg?) {
            if (aimRoutePushMsg?.content != null && !TextUtils.isEmpty(traceId)) {
                var isHasEndPoi = false
                traceId = aimRoutePushMsg.traceId
                mPlanPref = aimRoutePushMsg.content.routeParam.type
                mPlanPrefs = getNaviStrategy(aimRoutePushMsg.content.routeParam.type)
                Timber.d("====link Link notifyMessage AimRoutePushMsg traceId = %s, planPref = %s", traceId, mPlanPref)
                val path = aimRoutePushMsg.content.path
                if (path != null) {
                    if (path.endPoints.points.isNotEmpty()) {
                        val endPoint = GeoPoint(
                            path.endPoints.points[0].lon.toDouble(),
                            path.endPoints.points[0].lat.toDouble()
                        )
                        mEndPoi = POIFactory.createPOI(
                            aimRoutePushMsg.content.routeParam.destination.name,
                            endPoint,
                            aimRoutePushMsg.content.routeParam.destination.poiId
                        )
                        isHasEndPoi = true
                    }
                    var j = 0
                    while (j < path.viaPoints.points.size) {
                        if (j < aimRoutePushMsg.content.routeParam.routeViaPoints.size) {
                            val points = POI()
                            points.name =
                                aimRoutePushMsg.content.routeParam.routeViaPoints[j].name
                            points.id =
                                aimRoutePushMsg.content.routeParam.routeViaPoints[j].poiId
                            points.point = GeoPoint(
                                path.viaPoints.points[j].lon.toDouble(),
                                path.viaPoints.points[j].lat.toDouble()
                            )
                            midPois!!.add(points)
                        }
                        j += 2
                    }
                }
                if (isHasEndPoi) {
                    linkRoutePushMessage.postValue(aimRoutePushMsg)
                }
            } else {
                Timber.i("aimRoutePushMsg 普通推送 traceId:${aimRoutePushMsg?.traceId}")
                notifyRoutePushMessage.postValue(aimRoutePushMsg)
            }
        }

        override fun isSend2Car(): Boolean {
            return false
        }
    }

    private val iCallBackWsTServiceInternalLinkCarReport = ICallBackWsTserviceInternalLinkCarReport { response ->
        Timber.d("====link onRecvAck GWsTserviceInternalLinkCarReportResponseParam response = %s", gson.toJson(response))
    }

    private val iCallBackWsTServiceInternalLinkCarGet = ICallBackWsTserviceInternalLinkCarGet { response ->
        Timber.d("====link onRecvAck GWsTserviceInternalLinkCarGetResponseParam response = %s", gson.toJson(response))
        linkStatus = response?.data?.linkStatus!!
        if (mILinkStatusChangeListenerList != null && mILinkStatusChangeListenerList!!.size > 0) {
            for (i in mILinkStatusChangeListenerList!!.indices) {
                mILinkStatusChangeListenerList!![i].notifyLinkPhoneStatus(linkStatus == 1)
            }
        }
    }

    private val naviStateListener = NaviStateListener { state ->
        var isNeedRestartSend = false
        var playText: String? = null
        when (state) {
            AutoState.LOGIN_SUCCESS -> startLinkPhone()
            AutoState.CALCUATE_ROUTE_FINISH_SUCC -> {
                isNeedRestartSend = true
                //算路成功，上报
                naviCalcuResult = 1
                dataChange = 6
                startLinkCarNoHandleReport()
            }

            AutoState.CALCUATE_ROUTE_FINISH_FAIL -> {
                isNeedRestartSend = true
                //算路失败，上报
                naviCalcuResult = 0
                startLinkCarNoHandleReport()
            }

            AutoState.CURRENT_NAVI, AutoState.LINK_CAR_IN_NAVI -> {
                Timber.d("====link CURRENT_NAVI")
                isNeedRestartSend = true
                //                //导航开始
                naviCalcuResult = 1
                isInNavi = true
                dataChange = 5
                startLinkCarNoHandleReport()
            }

            AutoState.GUIDE_STOP -> {
                isNeedRestartSend = true
                //导航结束
                isInNavi = false
                traceId = ""
                startLinkCarNoHandleReport()
            }

            AutoState.LINK_CAR_REROUTE_SUCCESS -> {
                isNeedRestartSend = true
                //更改目的地成功
                naviCalcuResult = 1
                dataChange = 6
                naviCalcuResult = 1
                dataChange = 5
                isInNavi = true
                startLinkCarNoHandleReport()
                if (!TextUtils.isEmpty(destinationName)) {
                    playText = "已修改目的地为$destinationName"
                    playResult(playText)
                }
            }

            AutoState.LINK_CAR_REROUTE_ERROR -> {
                isNeedRestartSend = false
                playText = "修改目的地失败，请重试"
                playResult(playText)
            }

            AutoState.LINK_CAR_REROUTE_PREF_SUCCESS -> {
                isNeedRestartSend = false
                playText = "已为您规划" + planPrefName + "的路线"
                if (sendType == SceneSendType.SceneTypeCancelPlanPref) {
                    playText = "已为您取消" + cancelPlanPrefName + "的偏好"
                }
                if (!TextUtils.isEmpty(planPrefName)) {
                    playResult(playText)
                }
            }

            AutoState.LINK_CAR_REROUTE_PREF_ERROR -> {
                //修改偏好算路失败
                isNeedRestartSend = false
                playText = "修改偏好失败，请重试"
                playResult(playText)
            }

            AutoState.LINK_CAR_ADD_MID_SUCCESS -> {
                //添加途经点成功
                isNeedRestartSend = true
                //更改目的地成功
                naviCalcuResult = 1
                dataChange = 6
                naviCalcuResult = 1
                dataChange = 2
                isInNavi = true
                startLinkCarNoHandleReport()
                if (!TextUtils.isEmpty(wayPoiName)) {
                    playText = "已添加途经点$wayPoiName"
                    playResult(playText)
                }
            }

            AutoState.LINK_CAR_DELETE_MID_SUCCESS ->                 //删除途经点成功
                if (!TextUtils.isEmpty(wayPoiName)) {
                    playText = "已取消途经点$wayPoiName"
                    playResult(playText)
                }

            AutoState.LINK_CAR_ADD_MID_ERROR -> {
                playText = "添加途经点失败，请重试"
                playResult(playText)
            }

            AutoState.LINK_CAR_DELETE_MID_ERROR -> {
                playText = "删除途经点失败，请重试"
                playResult(playText)
            }

            else -> {}
        }
        if (isNeedRestartSend) {
            handle.removeCallbacksAndMessages(null)
            handle.sendEmptyMessageDelayed(TIME, DELAYED_TIME.toLong())
        }
    }
}
