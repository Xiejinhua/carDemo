package com.desaysv.psmap.ui.navi

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.guide.model.NaviFacility
import com.autonavi.gbl.guide.model.NaviGreenWaveCarSpeed
import com.autonavi.gbl.guide.model.NaviType
import com.autonavi.gbl.guide.model.ServiceAreaInfo
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.common.AutoStatus
import com.autosdk.common.tts.IAutoPlayer
import com.desaysv.psmap.base.bean.DestinationData
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.business.LinkCarBusiness
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SmartDriveBusiness
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */
@HiltViewModel
open class NaviViewModel @Inject constructor(
    val mNaviBusiness: NaviBusiness,
    mSkyBoxBusiness: SkyBoxBusiness,
    private val gson: Gson,
    private val application: Application,
    private val mSettingComponent: ISettingComponent,
    private val iCarInfoProxy: ICarInfoProxy,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val mRouteRequestController: RouteRequestController,
    private val linkCarBusiness: LinkCarBusiness,
    private val iAutoPlayer: IAutoPlayer,
    private val smartDriveBusiness: SmartDriveBusiness,

    ) : ViewModel() {
    val setToast: LiveData<String> = mNaviBusiness.setToast
    val finishFragment: LiveData<Boolean> = mNaviBusiness.finishFragment

    private val _isShowMore = MutableLiveData<Boolean>(false)
    val isShowMore: LiveData<Boolean> = _isShowMore

    //分屏状态监听
    val screenStatus = iCarInfoProxy.getScreenStatus()

    val isJetOurGaoJie = iCarInfoProxy.isJetOurGaoJie()

    //触碰态显示控件
    val backCCPVisible = mNaviBusiness.backCCPVisible

    //鹰眼图点击View显示隐藏状态
    val eagleViewVisible = mNaviBusiness.eagleViewVisible

    //日夜模式回调
    val themeChange = mSkyBoxBusiness.themeChange()

    //光柱图View显示隐藏状态
    val tmcBarVisible = mNaviBusiness.tmcBarVisible

    //光柱图信息
    val tmcModelInfo = mNaviBusiness.tmcModelInfo

    //是否处于全览状态
    val inFullView = mNaviBusiness.inFullView
    val naviInfo = mNaviBusiness.naviInfo

    //路线Loading提示图标
    val loadingView = mNaviBusiness.loadingView

    //距离下一个路口距离
    val topDistance = mNaviBusiness.topDistance

    //距离下一个路口距离 单位
    val topDistanceUnit = mNaviBusiness.topDistanceUnit

    //距离下一个路口距离 提示
    val topDistanceTip = mNaviBusiness.topDistanceTip

    //距离下一个路口名称
    val topRoadName = mNaviBusiness.topRoadName

    //显示到下一条道路的路口大图的距离
    val crossImageProgress = mNaviBusiness.crossImageProgress

    //到达时间和距离
    val remainTimeAndDistance = mNaviBusiness.remainTimeAndDistance

    //是否显示TBT卡片
    val tbtVisible = mNaviBusiness.tbtVisible

    //到达时间和距离
    val arriveTime = mNaviBusiness.arriveTime

    //显示路口放大图
    val showCrossView = mNaviBusiness.showCrossView

    //更新(接近)进阶动作信息显示隐藏通知
    val nearThumInfoVisibility = mNaviBusiness.nearThumInfoVisibility

    //更新(接近)进阶动作 路口名称
    val nearRoadName = mNaviBusiness.nearRoadName

    //步行最后一公里View显示隐藏通知
    val naviSendToPhoneLd = mNaviBusiness.naviSendToPhoneLd

    //进阶动作图标
    val turnIcon = mNaviBusiness.turnIcon

    //更新(接近)进阶动作图标
    val nearThumTurnIcon = mNaviBusiness.nearThumTurnIcon

    //城市信息
    val cityInfo = mNaviBusiness.cityInfo

    //城市信息显示隐藏状态
    val cityVisibility = mNaviBusiness.cityVisibility

    //出口信息显示隐藏状态
    val exitDirectionInfoVisible = mNaviBusiness.exitDirectionInfoVisible

    //出口信息编号
    val exitTip = mNaviBusiness.exitTip

    //出口信息
    val exitDirectionInfo = mNaviBusiness.exitDirectionInfo

    //车道线显示隐藏状态
    val naviLaneVisible = mNaviBusiness.naviLaneVisible

    //车道线数据
    val naviLaneList = mNaviBusiness.naviLaneList

    //删除途经点信息
    val showViaNaviViaDataDialog = mNaviBusiness.showViaNaviViaDataDialog

    //服务区信息显示隐藏状态
    val sapaViewVisibility = mNaviBusiness.sapaViewVisibility

    //服务区信息
    val sapaInfoList = mNaviBusiness.sapaInfoList

    //更改路线偏好后重新算路
    val onReRouteFromPlanPref = linkCarBusiness.onReRouteFromPlanPref

    //手车互联变更目的地或者途经点
    val onChangeDestination = linkCarBusiness.onChangeDestination

    //电量不足提示
    val showBatteryLowCard = mNaviBusiness.showBatteryLowCard

    //主辅路显示隐藏 平行路
    val parallelRoadVisible = mNaviBusiness.parallelRoadVisible

    //主辅路状态 平行路
    val parallelRoadState = mNaviBusiness.parallelRoadState

    //桥上桥下显示隐藏 桥上桥下
    val parallelBridgeVisible = mNaviBusiness.parallelBridgeVisible

    //桥上桥下状态 桥上桥下
    val parallelBridgeState = mNaviBusiness.parallelBridgeState

    //当前道路名称
    val mRoadName = mNaviBusiness.mRoadName

    //道路限速
    val mCurrentRoadSpeed = mNaviBusiness.mCurrentRoadSpeed

    //区间限速显示隐藏 view
    val limitSpeedVisible = mNaviBusiness.limitSpeedVisible

    //区间限速最大可行驶速度
    val limitSpeed = mNaviBusiness.limitSpeed

    //区间限速当前平均速度
    val averageSpeed = mNaviBusiness.averageSpeed

    //区间限速剩余距离
    val remainDist = mNaviBusiness.remainDist

    //道路限行，道路措施 交通信息
    val forbiddenInfo = mNaviBusiness.forbiddenInfo

    //显示隐藏 道路限行，道路措施 交通信息
    val forbiddenInfoVisibility = mNaviBusiness.forbiddenInfoVisibility

    //显示隐藏道路限速View
    val roadSpeedVisible = mNaviBusiness.roadSpeedVisible

    //红绿灯绿波车速数据
    val naviGreenWaveCarSpeed = mNaviBusiness.naviGreenWaveCarSpeed

    //红绿灯绿波车速数据显示隐藏 View
    val naviGreenWaveCarSpeedVisible = mNaviBusiness.naviGreenWaveCarSpeedVisible

    //通过绿波车速最大速和最小速
    private val _minSpeedAndMaxSpeed = MutableLiveData<String>()
    val minSpeedAndMaxSpeed: LiveData<String> = _minSpeedAndMaxSpeed

    //当前车速
    private val _curSpeedLiveData = MutableLiveData<String>()
    val curSpeedLiveData: LiveData<String> = _curSpeedLiveData

    //绿波红绿灯个数
    private val _lightCount = MutableLiveData<String>()
    val lightCount: LiveData<String> = _lightCount


    val isMute: LiveData<Boolean> = navigationSettingBusiness.volumeMute.map { value ->
        value == 1
    }

    //0 小地图  1 光柱图 2 极简
    val mapType = navigationSettingBusiness.mapType

    //高速服务区信息显示隐藏 view
    val serviceAreaInfoVisible = mNaviBusiness.serviceAreaInfoVisible

    //获取高速服务区信息
    val serviceAreaInfo = mNaviBusiness.serviceAreaInfo

    //简易光柱图View显隐状态
    val simpleTmcBarVisible = mNaviBusiness.simpleTmcBarVisible


    //高速服务区个数
    private val _remainServiceAreaNum = MutableLiveData<String>()
    val remainServiceAreaNum: LiveData<String> = _remainServiceAreaNum

    //高速剩余距离
    private val _remainingDis = MutableLiveData<String>()
    val remainingDis: LiveData<String> = _remainingDis

    //高速剩余时间
    private val _remainingTime = MutableLiveData<String>()
    val remainingTime: LiveData<String> = _remainingTime

    //高速服务区详情列表数据
    private val _sapaInfoDetailsList = MutableLiveData<ArrayList<NaviFacility>>()
    val sapaInfoDetailsList: LiveData<ArrayList<NaviFacility>> = _sapaInfoDetailsList

    //进行终点周边搜  停车场推荐
    val parkingRecommend = mNaviBusiness.parkingRecommend
    val parkingRecommendVisible = mNaviBusiness.parkingRecommendVisible

    //显示记忆行车按钮
    private val _showModBtn = MutableLiveData<Boolean>(true)
    val showModBtn: LiveData<Boolean> = _showModBtn

    private val _parkingSize = MutableLiveData(0)
    val parkingSize: LiveData<Int> = _parkingSize
    val parkingSizeTip = parkingSize.map {
        "${it}个停车场"
    }
    val showParkingRecommendDetail = mNaviBusiness.showParkingRecommendDetail

    private val _parkingName = MutableLiveData<String>()
    val parkingName: LiveData<String> = _parkingName
    private val _parkingDistance = MutableLiveData<String>()
    val parkingDistance: LiveData<String> = _parkingDistance

    val naviParkingPosition = mNaviBusiness.parkingPosition

    private var parkingPosition = 0

    private val naviGreenWaveCarSpeedOb = Observer<NaviGreenWaveCarSpeed> {
        _minSpeedAndMaxSpeed.postValue("${it.minSpeed}-${it.maxSpeed}")
        _curSpeedLiveData.postValue(getCurSpeed().toString())
        _lightCount.postValue(it.lightCount.toString())
    }
    private val serviceAreaInfoOb = Observer<ServiceAreaInfo?> { mServiceAreaInfo ->
        mServiceAreaInfo?.let {
            _sapaInfoDetailsList.postValue(it.serviceAreaList)
            // 剩余服务区个数
            _remainServiceAreaNum.postValue(it.remainServiceAreaNum.toInt().toString())
            // 剩余高速里程  （当前自车位到查询的最后一段高速的里程总和，单位米）
            _remainingDis.postValue(CommonUtils.getDistanceKm(it.remainFreewayDistance.toDouble()))
            // 剩余高速时间  （当前自车位到查询的最后一段高速的时间总和，单位秒）
            _remainingTime.postValue(CommonUtils.secondsToHours(it.remainFreewayTime.toDouble()))
        }
    }

    fun init() {
        Timber.i("NaviViewModel init")
        mNaviBusiness.init(NaviType.NaviTypeGPS)
        naviGreenWaveCarSpeed.observeForever(naviGreenWaveCarSpeedOb)
        serviceAreaInfo.observeForever(serviceAreaInfoOb)
        iCarInfoProxy.publishNaviStatus(true)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("NaviViewModel onCleared")
        mNaviBusiness.resetNaviCardData()
        naviGreenWaveCarSpeed.removeObserver(naviGreenWaveCarSpeedOb)
        serviceAreaInfo.removeObserver(serviceAreaInfoOb)
        iAutoPlayer.stop(false)
    }

    /**
     * 是否有导航信息透出
     */
    fun isUpdateNaviInfo(): Boolean = mNaviBusiness.getUpdateNaviInfoState()

    /**
     * 日夜模式切换 新(接近)进阶动作 日夜模式UI
     */
    fun updateNextThumTurnTheme() = mNaviBusiness.updateNextThumTurnTheme()

    /**
     * 日夜模式切换 进阶动作 图标icon日夜模式UI
     */
    fun updateTurnIconTheme() = mNaviBusiness.updateTurnIconTheme()

    /**
     * 当前车辆速度
     */
    fun getCurSpeed(): Int = mNaviBusiness.getCurSpeed().toInt()
    val naviSpeed = mNaviBusiness.naviSpeed

    /**
     * 设置路线并开始导航
     */
    open fun setRouteAndNavi(type: Int, mIsHome: Boolean) {
        Timber.i("NaviViewModel setRouteAndNavi is called")
        mNaviBusiness.setIsHome(mIsHome)
        mNaviBusiness.setRouteAndNavi(type)
    }

    /**
     * 结束导航
     */
    fun stopNavi() {
        mNaviBusiness.stopNavi()
        AutoStatusAdapter.sendStatus(AutoStatus.NAVI_MANUAL_STOP)
        AutoStatusAdapter.sendStatus(AutoStatus.NAVI_MANUAL_STOP1)
    }

    /**
     * 继续导航 (回车位)
     */
    fun naviBackCurrentCarPosition() = mNaviBusiness.naviBackCurrentCarPosition()

    /**
     * 退出全览
     */
    fun exitPreview() = mNaviBusiness.exitPreview()

    /**
     * 打开全览
     */
    fun showPreview() = mNaviBusiness.showPreview(1)

    /**
     * 隐藏步行最后一公里View
     */
    fun hideSendToPhone() = mNaviBusiness.hideSendToPhone()

    /**
     * 发送步行最后一公里到手机
     */
    fun sendToPhone() = mNaviBusiness.sendToPhone()

    /**
     * 隐藏路口大图
     */
    fun hideCrossView() = mNaviBusiness.hideCrossView()


    /**
     * 是否打开更多
     */
    fun setShowMore(showMore: Boolean) = _isShowMore.postValue(showMore)

    /**
     * 点击切换主辅路
     * isMainSide： true=主辅路  false=桥上桥下
     */
    fun onParallelWayClick(isMainSide: Boolean) = mNaviBusiness.onParallelWayClick(isMainSide)


    /**
     * 重新刷新路线
     */
    fun networkRefreshRoute() = mNaviBusiness.networkRefreshRoute()

    /**
     * 设置离线路线样式
     */
    fun setSwitchOffline() = mNaviBusiness.setSwitchOffline()

    /**
     * 发起路线规划并开始导航
     */
    fun startPlanRoute(start: POI?, end: POI?, midPois: ArrayList<POI>?, type: Int) {
        viewModelScope.launch {
            var requestId = mNaviBusiness.planRoute(start!!, end!!, midPois, type)
            Timber.i("NaviViewModel startPlanRoute is called requestId = $requestId")
        }
    }

    /**
     * 发起路线规划并开始导航
     */
    fun startPlanRouteAimRoutePushMsg(pushMsg: AimRoutePushMsg?, type: Int) {
        viewModelScope.launch {
            var requestId = mNaviBusiness.planAimRoutePushMsgRoute(pushMsg, type)
            Timber.i("NaviViewModel startPlanRouteAimRoutePushMsg is called requestId = $requestId")
        }
    }


    /**
     * 删除途经点
     */
    fun delWayPoint(index: Int) {
        Timber.i("RouteViewModel delWayPoint is called 222 index = $index")
        viewModelScope.launch {
            mNaviBusiness.deleteViaPoi(index)
        }
    }

    //更改路线偏好后重新算路
    fun onReRouteFromPlanPref(mPlanPref: String?) {
        mNaviBusiness.onReRouteFromPlanPref(mPlanPref)
    }

    //手车互联变更目的地或者途经点
    fun onChangeDestination(destinationData: DestinationData) {
        destinationData.poi?.let {
            mNaviBusiness.onChangeDestination(
                it,
                destinationData.sendType,
                destinationData.mPlanPref
            )
        }
    }

    fun closeBatteryLowTipsCard() {
        mNaviBusiness.closeBatteryLowTipsCard()
    }

    fun setPlayTTsMute(isMute: Boolean) {
        Timber.i("setPlayTTsMute $isMute")
        navigationSettingBusiness.setConfigKeyMute(if (isMute) 1 else 0, true)
    }

    /**
     * 设置鹰眼图或光柱图显隐
     */
    fun setEagleVisible(isTrue: Boolean) = mNaviBusiness.setEagleVisible(isTrue = isTrue)

    /**
     * 获取高速SAPA信息
     */
    fun obtainSAPAInfo() = mNaviBusiness.obtainSAPAInfo()

    fun setServiceAreaInfoVisible(isShow: Boolean) = mNaviBusiness.setServiceAreaInfoVisible(isShow)


    fun setParkingPosition(position: Int) = mNaviBusiness.setParkingPosition(position)

    fun showParkingCardDetail() {
        mNaviBusiness.showParkingCardDetail(true)
    }

    /**
     * 停车场选择
     */
    fun chooseParkingRecommend(position: Int) {
        parkingPosition = position
        _parkingSize.postValue(parkingRecommend.value?.size ?: 0)
        parkingRecommend.value?.takeIf { it.isNotEmpty() }?.let {
            // 处理非空且非空列表的情况
            _parkingName.postValue(it[parkingPosition].name)
            _parkingDistance.postValue("距离${it[parkingPosition].dis}")
            mNaviBusiness.setCustomLayerItemFocus(BizCustomTypePoint.BizCustomTypePoint4, it[parkingPosition].id, true)
        }
    }

    /**
     * 导航到停车场，重新规划路线
     */
    fun retryPlanRouteParking() {
        Timber.i("retryPlanRouteParking is called")
        parkingRecommend.value?.takeIf { it.isNotEmpty() }?.let {
            val carRouteResult = mRouteRequestController.carRouteResult
            val start = carRouteResult?.fromPOI
            val end = it[parkingPosition]
            val midPois = carRouteResult?.midPois
            if (start != null && end != null) {
                viewModelScope.launch {
                    mNaviBusiness.setParkingRecommendVisible(false)
                    mNaviBusiness.planRoute(start, end, midPois, BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD)
                }
            }
        }
    }

    /**
     * 检查是否处于 ModMapper 地图训练状态
     *
     * @return 如果处于 ModMapper 地图训练状态返回 true，否则返回 false
     */
    fun isModMapperMapTraining(): Boolean {
        return smartDriveBusiness.isModMapperMapTraining()
    }

    /**
     * 设置显示记忆行车按钮的可见状态
     *
     * @param show 是否显示记忆行车按钮，true 表示显示，false 表示隐藏
     */
    fun clickModBtn(show: Boolean) {
        _showModBtn.postValue(show)
    }

}

