package com.desaysv.psmap.ui.route

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.widget.route.constant.ConfigRoutePreference
import com.autosdk.common.AutoState
import com.autosdk.common.AutoStatus
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.AppExecutors
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentRouteBinding
import com.desaysv.psmap.model.bean.CommandRequestPOICardBean
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.bean.ModifyPoiBean
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.route.compose.ChildPOIComposeListView
import com.desaysv.psmap.ui.route.compose.RouteComposeListView
import com.desaysv.psmap.ui.route.view.TouchConstraintLayout
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.desaysv.psmap.utils.LoadingUtil
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description 路线规划页面
 */
@AndroidEntryPoint
class RouteFragment : Fragment() {
    private lateinit var binding: FragmentRouteBinding
    private val viewModel by viewModels<RouteViewModel>()

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var appExecutors: AppExecutors

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    @Inject
    lateinit var loadingUtil: LoadingUtil

    private lateinit var mItemTouchHelper: ItemTouchHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("RouteFragment onCreate()")
//        val commandRequestRouteNaviBean = requireArguments().getParcelable<CommandRequestRouteNaviBean>(Biz.KEY_BIZ_ROUTE_START_END_VIA_POI_LIST)
//        viewModel.setRouteNaviBean(commandRequestRouteNaviBean)
//        startBtStartNaviTimer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteBinding.inflate(inflater, container, false).apply {
            composeView.setContent {
                composeView()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initData()
        initEventOperation()
        startBtStartNaviTimer()
        AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_FRAGMENT_START)
        SdkAdapterManager.getInstance().sendNormalMessage(AutoState.ROUTE_START)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        removeBtStartNaviTimer()
    }

    override fun onDestroy() {
        super.onDestroy()
        AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_FRAGMENT_EXIT)
        AutoStatusAdapter.sendStatus(AutoStatus.PLAN_ROUTE_FRAGMENT_EXIT0)
        SdkAdapterManager.getInstance().sendNormalMessage(AutoState.ROUTE_STOP)
        Timber.i("onDestroy()")
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.childPoiInfoView.childPOIComposeView.setContent {
            childPOIComposeView()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(500)
            viewModel.showPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        stopCountDownToNavi()
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        //燃油版不显示充电站
        binding.viewNaviStrategy.chargingPileIv.visibility = if (iCarInfoProxy.isT1JFL2ICE()) View.GONE else View.VISIBLE

        if (iCarInfoProxy.isT1JFL2ICE()) {
            binding.btCharge.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_fuel_night else R.drawable.selector_ic_fuel_day)
            binding.btCharge.setBackground(R.drawable.selector_ic_fuel_day, R.drawable.selector_ic_fuel_night)
        } else {

            binding.btCharge.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_charge_station_night else R.drawable.selector_ic_charge_station_day)
            binding.btCharge.setBackground(R.drawable.selector_ic_charge_station_day, R.drawable.selector_ic_charge_station_night)
        }

        //设置导航路线偏好配置
        viewModel.setNavigationData()
        //监听是否更改了路线偏好，进行重算路线
        viewModel.incrementalRouteNotice.unPeek().observe(viewLifecycleOwner) {
            Timber.i("incrementalRouteNotice is called $it")
            if (it) {
                viewModel.retryPlanRoute()
                viewModel.setIncrementalRouteNotice()
            }
        }
        //保存路线偏好设置
        viewModel.saveStrategy.unPeek().observe(viewLifecycleOwner) {
            viewModel.checkAndSavePrefer()
        }

        viewModel.showViaNaviViaDataDialog.unPeek().observe(viewLifecycleOwner) {
            NavHostFragment.findNavController(this@RouteFragment).navigate(R.id.action_routeFragment_to_naviDeleteViaDialogFragment)
        }

        viewModel.mapCommand.unPeek().observe(viewLifecycleOwner) { mapCommand ->
            Timber.i("mapCommand $mapCommand")
            mapCommand?.let {
                Timber.i("mapCommand ${it.mapCommandType}")
                when (it.mapCommandType) {
                    MapCommandType.StartNaviWhenHasRoute -> {
                        if (viewModel.isRequestRoute.value == false) {
                            startNavi()
                        }
                    }

                    MapCommandType.ChooseRoute -> {
                        it.data?.let { data ->
                            if (viewModel.isRequestRoute.value == false) {
                                if (viewModel.focusPathIndex.value != data.toInt()) {
                                    viewModel.selectPathByIndexOnMap(data.toInt())
                                }
                            }
                        }
                    }

                    MapCommandType.PosRank -> {
                        Timber.i("PosRank rank = ${mapCommand.pair} ")
                        val offset = mapCommand.pair?.second!!
                        val routeSize = (mRouteRequestController.carRouteResult?.pathResult?.size ?: 0)
                        if (routeSize < offset) {
                            viewModel.notifyVoiceCommandResult(
                                MapCommandType.PosRank,
                                false,
                                "当前只有${routeSize}条路线，请换个试试"
                            )
                        } else {
                            viewModel.selectPathByIndexOnMap(offset - 1)
                            viewModel.notifyVoiceCommandResult(
                                MapCommandType.PosRank,
                                true,
                                "已为您切换第${offset}条路线"
                            )
                        }
                    }

                    MapCommandType.Confirm -> {
                        Timber.i("Confirm called")
                        if (viewModel.isRequestRoute.value == false) {
                            startNavi()
                            viewModel.notifyVoiceCommandResult(
                                MapCommandType.Confirm,
                                true,
                                "已为您开启导航"
                            )
                        }
                    }

                    else -> {}

                }

            }

        }

    }


    private fun initEventOperation() {
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }
        //退出路线规划界面
        binding.ivBack.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.ivBack, CLICKED_SCALE_90)

        //添加途经点
        binding.searchViaCL.setOnClickListener {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.OnthewaySearch_Click,
                mapOf(Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis()))
            )
            findNavController().navigate(R.id.to_searchAlongWayFragment, ModifyPoiBean().toBundle())
        }
        //打开路线偏好设置View
        binding.btnMore.setOnClickListener {
            viewModel.setPreferenceSetting()
        }
        ViewClickEffectUtils.addClickScale(binding.btnMore, CLICKED_SCALE_90)

        //关闭路线偏好设置View
        binding.viewNaviStrategy.ivClose.setOnClickListener {
            viewModel.setPreferenceSetting()
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.ivClose, CLICKED_SCALE_90)

        //重新规划路线隐藏 常用界面
        viewModel.closePreferenceSetting.observe(viewLifecycleOwner) {
            if (it) {
                if (viewModel.isShowPreferenceSetting.value == true) {
                    viewModel.setPreferenceSetting()
                }
            }
        }
        //智能推荐操作
        binding.viewNaviStrategy.clDefaultStrategy0.setDebouncedOnClickListener {
            viewModel.preferSelect(ConfigRoutePreference.PREFERENCE_DEFAULT, viewModel.rsDefaultSelected.value ?: false)
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.clDefaultStrategy0)
        //躲避拥堵操作
        binding.viewNaviStrategy.clAvoidJanStrategy1.setDebouncedOnClickListener {
            viewModel.preferSelect(ConfigRoutePreference.PREFERENCE_AVOID_JAN, viewModel.rsTmcSelected.value ?: false)
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.clAvoidJanStrategy1, CLICKED_SCALE_95)
        //少收费操作
        binding.viewNaviStrategy.clAvoidChargeStrategy2.setDebouncedOnClickListener {
            viewModel.preferSelect(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE, viewModel.rsMoneySelected.value ?: false)
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.clAvoidChargeStrategy2, CLICKED_SCALE_95)
        //不走高速操作
        binding.viewNaviStrategy.clAvoidHighwayStrategy3.setDebouncedOnClickListener {
            viewModel.preferSelect(ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY, viewModel.rsFreewayNoSelected.value ?: false)
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.clAvoidHighwayStrategy3, CLICKED_SCALE_95)
        //高速优先操作
        binding.viewNaviStrategy.clUsingHighwayStrategy4.setDebouncedOnClickListener {
            viewModel.preferSelect(ConfigRoutePreference.PREFERENCE_USING_HIGHWAY, viewModel.rsFreewayYesSelected.value ?: false)
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.clUsingHighwayStrategy4, CLICKED_SCALE_95)
        //速度最快操作
        binding.viewNaviStrategy.clUsingSpeedFirstStrategy5.setDebouncedOnClickListener {
            viewModel.preferSelect(ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST, viewModel.rsFreewayQuickSelected.value ?: false)
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.clUsingSpeedFirstStrategy5, CLICKED_SCALE_95)
        //大路优先操作
        binding.viewNaviStrategy.clUsingRouteWidthStrategy6.setDebouncedOnClickListener {
            viewModel.preferSelect(ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST, viewModel.rsFreewayBigSelected.value ?: false)
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.clUsingRouteWidthStrategy6, CLICKED_SCALE_95)

        binding.btStartNavi.setDebouncedOnClickListener {
            startNavi()
        }
        ViewClickEffectUtils.addClickScale(binding.btStartNavi, CLICKED_SCALE_95)

        binding.cloudShowInfoView.ivClose.setDebouncedOnClickListener {
            viewModel.closeHasPriorTip()
        }
        ViewClickEffectUtils.addClickScale(binding.cloudShowInfoView.ivClose, CLICKED_SCALE_90)

        binding.cl.setCallBack(object : TouchConstraintLayout.OnDispatchTouchEventCallBack {
            override fun dispatchTouchEvent(ev: MotionEvent?) {
                if (ev?.action == MotionEvent.ACTION_DOWN) {
//                    val x = ev.x
//                    val y = ev.y
//                    if (x > 728 && x < 802 && y > 103 && y < 192) {
//                        Timber.i("close naviStrategy do not stopCountDown !")
//                        return
//                    }
                    //屏幕点击就停止倒计时
                    stopCountDownToNavi()
                }
            }
        })
        viewModel.pathListLiveData.unPeek().observe(viewLifecycleOwner) {
            if (it.isNotEmpty() && viewModel.isShowPreferenceSetting.value == false) {
                removeBtStartNaviTimer()
                startBtStartNaviTimer()
            }
        }
        viewModel.isRequestRoute.unPeek().observe(viewLifecycleOwner) {
            if (it) {
                Timber.i("isRequestRoute = true stopCountDownToNavi is called ")
                //请求中就停止倒计时
                stopCountDownToNavi()
            }
        }
        //模拟导航
//        binding.btSimulationNavi.setDebouncedOnClickListener {
//            if (viewModel.pathListLiveData.value?.isEmpty() == false) {
//                stopCountDownToNavi()
////            viewModel.setNotDeleteRoute(true)
//                val commandBean = CommandRequestRouteNaviBean.Builder().build(mRouteRequestController.carRouteResult)
//                findNavController().navigate(R.id.action_routeFragment_to_simNaviFragment, commandBean.toBundle())
//            } else {
//                toast.showToast(com.desaysv.psmap.base.R.string.sv_route_pls_calc_first)
//            }
//        }
        //沿途搜
//        binding.btAlongSearch.setDebouncedOnClickListener {
//            val commandBean = CommandRequestSearchCategoryBean.Builder()
//                .setType(CommandRequestSearchCategoryBean.Type.SEARCH_ALONG_WAY)
//                .build()
//            findNavController().navigate(R.id.to_searchCategoryFragment, commandBean.toBundle())
//        }
        binding.btCharge.setDebouncedOnClickListener {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.OnerefuelClick, System.currentTimeMillis()))
            )
            viewModel.clearAllSelect()
            val commandBean = CommandRequestSearchBean.Builder().setKeyword("充电站").setType(CommandRequestSearchBean.Type.SEARCH_CHARGE).build()
            findNavController().navigate(R.id.to_searchResultFragment, commandBean.toBundle())
        }
        ViewClickEffectUtils.addClickScale(binding.btCharge, CLICKED_SCALE_90)

        //分享二维码
        binding.btShare.setDebouncedOnClickListener {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.TravelShareClick, System.currentTimeMillis()))
            )
            NavHostFragment.findNavController(this@RouteFragment).navigate(R.id.action_routeFragment_to_tripShareDialogFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.btShare, CLICKED_SCALE_90)

        viewModel.routeShowViaPoi.unPeek().observe(viewLifecycleOwner) { showViaPoi ->
            showViaPoi?.let {
                Timber.i(" routeShowViaPoi is called showViaPoi = ${gson.toJson(it)}")
//                viewModel.showAddViaMap(it)
                val commandRequestPOICardBean =
                    CommandRequestPOICardBean.Builder().setType(CommandRequestPOICardBean.Type.POI_CARD_ROUTE_ADD_VIA).build()
                findNavController().navigate(R.id.to_MapPointDataFragment, commandRequestPOICardBean.toBundle())
            }
        }
//        viewModel.addViaPoi.observe(viewLifecycleOwner) { addViaPoi ->
//            addViaPoi?.let {
//                Timber.i("addViaPoi 添加了途经点")
//                var viaList = viewModel.getRouteNaviBean()?.midPois
//                if (viaList == null) {
//                    viaList = arrayListOf<POI>()
//                }
//                viaList.add(it)
//                //更新保存的起点终点途经点数据
//                viewModel.getRouteNaviBean()?.apply {
//                    midPois = viaList
//                }
//            }
//        }

        viewModel.screenStatus.observe(viewLifecycleOwner) {
            viewModel.showPreview()
        }

        binding.cloudShowInfoView.btSetUp.setDebouncedOnClickListener {
            viewModel.disposeRestrictBt()
        }
        ViewClickEffectUtils.addClickScale(binding.cloudShowInfoView.btSetUp, CLICKED_SCALE_93)

        binding.btnRefresh.setDebouncedOnClickListener {
            viewModel.clearAllSelect()
            viewModel.retryPlanRoute(isRouteRestart = true)
        }
        ViewClickEffectUtils.addClickScale(binding.btnRefresh, CLICKED_SCALE_90)

        viewModel.restrictInfoDetails.unPeek().observe(viewLifecycleOwner) { data ->
            data?.let {
                NavHostFragment.findNavController(this@RouteFragment).navigate(R.id.action_routeFragment_to_restrictFragment)
            }
        }
        viewModel.startSettingCar.unPeek().observe(viewLifecycleOwner) {
            if (it) {
                startSettingCar(BaseConstant.ACTION_VEHICLE_INFO) //跳转车牌设置页（跳转到车辆设置我的车页面）
            }
        }
        viewModel.avoidTrafficJamsBean.unPeek().observe(viewLifecycleOwner) {
            it?.let {
                NavHostFragment.findNavController(this@RouteFragment).navigate(R.id.action_routeFragment_to_priorTipFragment)
            }
        }

        binding.viewNaviStrategy.gasStationIv.setDebouncedOnClickListener {
            toSearchAlongWayResultFragment("加油站")
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.gasStationIv, CLICKED_SCALE_93)
        binding.viewNaviStrategy.toiletIv.setDebouncedOnClickListener {
            toSearchAlongWayResultFragment("卫生间")
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.toiletIv, CLICKED_SCALE_93)
        binding.viewNaviStrategy.restaurantIv.setDebouncedOnClickListener {
            toSearchAlongWayResultFragment("美食")
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.restaurantIv, CLICKED_SCALE_93)
        binding.viewNaviStrategy.repairIv.setDebouncedOnClickListener {
            toSearchAlongWayResultFragment("维修站")
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.repairIv, CLICKED_SCALE_93)
        binding.viewNaviStrategy.chargingPileIv.setDebouncedOnClickListener {
            toSearchAlongWayResultFragment("充电站")
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.chargingPileIv, CLICKED_SCALE_93)
        binding.viewNaviStrategy.serviceAreaIv.setDebouncedOnClickListener {
            if (viewModel.getShowRouteService()) {
                Timber.i("xjh serviceAreaIv isSelected = false ")
                viewModel.clearRouteRestArea()//清理服务区扎点
            } else {
                Timber.i("xjh serviceAreaIv isRequest  ")
                viewModel.onClickViaRoad(false)//清理途经路
                viewModel.clearWeatherOverlay()//清理天气扎点
                viewModel.changeAlongWayRestArea()//获取服务区
            }
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.serviceAreaIv, CLICKED_SCALE_93)
        binding.viewNaviStrategy.weatherIv.setDebouncedOnClickListener {
            if (viewModel.getShowRouteWeather()) {
                Timber.i("xjh weatherIv isSelected = false ")
                viewModel.clearWeatherOverlay()//清理天气扎点
            } else {
                Timber.i("xjh weatherIv isRequest  ")
                viewModel.clearRouteRestArea()//清理服务区扎点
                viewModel.onClickViaRoad(false)//清理途经路
                viewModel.changedRouteWeather()//获取天气
            }
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.weatherIv, CLICKED_SCALE_93)
        binding.viewNaviStrategy.pathwayIv.setDebouncedOnClickListener {
            if (viewModel.getShowRoutePathWay()) {
                Timber.i("xjh pathwayIv isSelected = false ")
                viewModel.onClickViaRoad(false)//清理途经路
            } else {
                Timber.i("xjh pathwayIv isRequest  ")
                viewModel.clearWeatherOverlay()//清理天气扎点
                viewModel.clearRouteRestArea()//清理服务区扎点
                viewModel.onClickViaRoad(true)//获取途经路
            }
        }
        ViewClickEffectUtils.addClickScale(binding.viewNaviStrategy.pathwayIv, CLICKED_SCALE_93)

        viewModel.isRequestRouteInfoLoading.unPeek().observe(viewLifecycleOwner) {
            loadingUtil.cancelLoading()
            //请求路线上的信息- 1:天气- 2:途径路- 3:服务区 4:请求完成 提示框
            when (it) {
                1 -> {
                    loadingUtil.showLoading(R.string.sv_route_navi_request_the_weather)
                }

                2 -> {
                    loadingUtil.showLoading(R.string.sv_route_navi_request_the_via)
                }

                3 -> {
                    loadingUtil.showLoading(R.string.sv_route_navi_request_the_service)
                }
            }
        }
        viewModel.weatherLabelItem.unPeek().observe(viewLifecycleOwner) {
            Timber.i("xjh weatherLabelItem  = ${gson.toJson(it)} ")
            it?.let {
                NavHostFragment.findNavController(this@RouteFragment).navigate(R.id.action_routeFragment_to_weatherDetailsFragment)
            }
        }
        viewModel.alongWayPoiDeepInfo.unPeek().observe(viewLifecycleOwner) {
            Timber.i("xjh alongWayPoiDeepInfo  = ${gson.toJson(it)} ")
            it?.let {
                NavHostFragment.findNavController(this@RouteFragment).navigate(R.id.action_routeFragment_to_restAreaDetailsFragment)
            }
        }
        //通知系统车牌号变化
        viewModel.licensePlateChange.unPeek().observe(viewLifecycleOwner) {
            Timber.i("route licensePlateChange is called ")
            viewModel.retryPlanRoute() //重新算路
        }
    }

    /**
     * 跳转沿途搜界面
     */
    private fun toSearchAlongWayResultFragment(keyWord: String = "") {
        viewModel.clearAllSelect()
        val commandBean =
            CommandRequestSearchCategoryBean.Builder().setKeyword(keyWord).setType(CommandRequestSearchCategoryBean.Type.SEARCH_ALONG_WAY)
                .build()
        findNavController().navigate(R.id.to_searchAlongWayResultFragment, commandBean.toBundle())
        viewModel.setPreferenceSetting()
    }

    private val btStartNaviTimer = object : CountDownTimer(11000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val time: Int = (millisUntilFinished / 1000).toDouble().roundToInt()
            viewModel.setBtStartNaviTip(getString(R.string.sv_route_start_navigating) + "(" + time + "s)")
        }

        override fun onFinish() {
            Timber.i(" btStartNaviTimer onFinish is called ")
            startNavi()
        }
    }

    private fun removeBtStartNaviTimer() = btStartNaviTimer.cancel()
    private fun startBtStartNaviTimer() = btStartNaviTimer.start()

    private fun startNavi() {
        try {
            Timber.i("startNavi is called ")
            stopCountDownToNavi()
            mRouteRequestController.carRouteResult?.let {
                EventTrackingUtils.trackEvent(
                    EventTrackingUtils.EventName.Nav_Start,
                    mapOf(
                        Pair(EventTrackingUtils.EventValueName.StartTime, System.currentTimeMillis()),
                        Pair(EventTrackingUtils.EventValueName.Departure, JSONObject().apply {
                            put("lon", it.fromPOI?.point?.longitude ?: 0.0)  // 默认值 0.0
                            put("lat", it.fromPOI?.point?.latitude ?: 0.0)   // 默认值 0.0
                        }.toString()),
                        Pair(EventTrackingUtils.EventValueName.MappageForm, if (viewModel.screenStatus.value == true) "0" else "1"),
                    )
                )
                viewModel.setNotDeleteRoute(true)
                val commandBean = CommandRequestRouteNaviBean.Builder().build(it)
                findNavController().navigate(R.id.to_naviFragment, commandBean.toBundle())
            }
        } catch (e: Exception) {
            Timber.e("startNavi Exception:${e.message}")
        }
    }

    private fun stopCountDownToNavi() {
        Timber.i("stopCountDownToNavi is called ")
        removeBtStartNaviTimer()
        viewModel.setBtStartNaviTip(getString(R.string.sv_route_start_navigating))
    }

    /**
     * 跳转车牌设置页（跳转到车辆设置我的车页面）
     */
    private fun startSettingCar(settingAction: String) {
        try {
            if (CommonUtils.isVehicle()) {
                CommonUtils.startSettingCar(settingAction, requireContext())
            } else {
                findNavController().navigate(R.id.to_vehicleFragment)
            }
        } catch (e: Exception) {
            Timber.d("startSettingCar: Exception e: ${e.message}")
        }
    }

    /**
     * 路线列表
     */
    @Composable
    private fun composeView() {
        val routeList by viewModel.pathListLiveData.observeAsState(emptyList())
        val selectedItemIndex = rememberSaveable { mutableStateOf(-1) }
        val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())
//        val refreshView by viewModel.mDataRefresh.unPeek().observeAsState(-1)

        viewModel.focusPathIndex.observe(viewLifecycleOwner) {
            Timber.i("RouteComposeView 选择了第 focusPathIndex = $it 路线 , selectedItemIndex.value = ${selectedItemIndex.value}")
            if (selectedItemIndex.value != it) {
                selectedItemIndex.value = it
            }
        }
        DsDefaultTheme(themeChange) {
            Timber.i("RouteComposeView themeChange = $themeChange")
//            if (refreshView == 1) {
            Timber.i("RouteComposeView routeList = ${gson.toJson(routeList)}")
            RouteComposeListView(
                routeList, selectedItemIndex,
                onContinueClicked = {
                    //列表点击选择路线
                    Timber.i("RouteComposeView onContinueClicked  focusPathIndex = ${viewModel.focusPathIndex.value}, position = $it")
                    if (viewModel.focusPathIndex.value != it)
                        viewModel.selectPathByIndexOnMap(it)
                },
                onRouteDetailsClicked = {
                    Timber.i("RouteComposeView onRouteDetailsClicked is called")
                    val naviStationItemData = viewModel.getPathNaviStationList()
                    //如果是路线详情界面点击路线详情按钮，直接跳转到路线详情界面
                    if (!naviStationItemData.isNullOrEmpty()) {
                        stopCountDownToNavi()
                        findNavController().navigate(R.id.routeDetailsFragment)
                    } else {
                        Timber.i("RouteComposeView onRouteDetailsClicked naviStationItemData is null or empty")
                    }
                })
//            } else if (refreshView == 0) {
//                NotDataComposeView(1, onContinueClicked = {
//                    viewModel.retryPlanRoute()
//                }, viewModel.routeErrorMessage.value)
//            }
        }
    }

    @Composable
    fun childPOIComposeView() {
        val childPois by viewModel.childPois.observeAsState(emptyList())
        val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())

        DsDefaultTheme(themeChange) {
            ChildPOIComposeListView(childPois, onContinueClicked = {
                viewModel.planRoute(it)
            })
        }
    }
}