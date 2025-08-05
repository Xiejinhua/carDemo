package com.desaysv.psmap.ui

import android.Manifest
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.common.path.model.SubCameraExtType
import com.autonavi.gbl.guide.model.NaviCameraExt
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.common.AutoStatus
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.HomeCardTipsData
import com.desaysv.psmap.base.bean.HomeCardTipsType
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.ForecastBusiness
import com.desaysv.psmap.base.business.InitSDKBusiness.InitSDKResult
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.PermissionReqController.Companion.PERMISSION_OK
import com.desaysv.psmap.base.def.InitSdkResultType
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.AppUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.ActivityMainBinding
import com.desaysv.psmap.databinding.FloatingCallWindowBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.bean.MapCommandType.AlongWaySearch
import com.desaysv.psmap.model.bean.MapCommandType.AroundSearch
import com.desaysv.psmap.model.bean.MapCommandType.Confirm
import com.desaysv.psmap.model.bean.MapCommandType.KeyWordSearch
import com.desaysv.psmap.model.bean.MapCommandType.KeyWordSearchForCollect
import com.desaysv.psmap.model.bean.MapCommandType.MoveAppToBack
import com.desaysv.psmap.model.bean.MapCommandType.OpenFavoritePage
import com.desaysv.psmap.model.bean.MapCommandType.OpenSearchPage
import com.desaysv.psmap.model.bean.MapCommandType.OpenSettingPage
import com.desaysv.psmap.model.bean.MapCommandType.ShowPoiCard
import com.desaysv.psmap.model.bean.MapCommandType.ShowPoiDetail
import com.desaysv.psmap.model.bean.MapCommandType.StartNavi
import com.desaysv.psmap.model.bean.MapCommandType.StartPlanRoute
import com.desaysv.psmap.model.bean.ScenicSectorBean
import com.desaysv.psmap.model.bean.TeamPushData
import com.desaysv.psmap.model.business.AhaTripBusiness
import com.desaysv.psmap.model.business.MobileInternetWXBusiness
import com.desaysv.psmap.model.business.OfflineDataBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.car.dashboard.CarDashboardBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.ahatrip.ScenicSectorView
import com.desaysv.psmap.ui.compose.LogoScaleLine
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.engineer.MainEngineerViewModel
import com.desaysv.psmap.ui.group.PushCardView
import com.desaysv.psmap.ui.settings.AccountAndSettingTab
import com.desaysv.psmap.utils.LoadingUtil
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NetWorkManager.NetWorkChangeListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel>()
    private val mainEngineerViewModel by viewModels<MainEngineerViewModel>()
    private lateinit var navController: NavController
    private var navControllerUninitialized = false
    private var floatingView: View? = null

    private var pendingCollapseKeyword = false
    private var isStartAnimator = false

    //组队推送卡片
    private val mPushCardView: PushCardView? by lazy {
        PushCardView(this)
    }

    //巡航景点推荐卡片
    private val mScenicSectorView: ScenicSectorView? by lazy {
        ScenicSectorView(this)
    }

    //组队推送卡片
    private val layoutParams: ConstraintLayout.LayoutParams? by lazy {
        ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
    }

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var mToast: ToastUtil

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var foreBusiness: ForecastBusiness

    private lateinit var animator: ValueAnimator

    @Inject
    lateinit var loadingUtil: LoadingUtil

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var offlineDataBusiness: OfflineDataBusiness

    @Inject
    lateinit var carDashboardBusiness: CarDashboardBusiness

    @Inject
    lateinit var mapBusiness: MapBusiness

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    @Inject
    lateinit var locationBusiness: LocationBusiness

    @Inject
    lateinit var ahaTripBusiness: AhaTripBusiness

    @Inject
    lateinit var ahaTripImpl: AhaTripImpl

    @Inject
    lateinit var mobileInternetWXBusiness: MobileInternetWXBusiness

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    private var dialogPermissionTips: CustomDialogFragment? = null

    private var teamDialog: CustomDialogFragment? = null

    private var scenicSectorDialog: CustomDialogFragment? = null

    private var destinationId = -1

    /**
     * Navigation界面目标Fragment变化回调
     */
    private val onDestinationChangedListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            destinationId = destination.id
            //通过判断当前页面，做对应的逻辑处理
            viewModel.showActivateAgreementOrMapButton.value?.run {
                Timber.d("showActivateAgreementOrMapButton onDestinationChangedListener")
                showActiveAgreementOrMapBase(
                    destination.id,
                    first,
                    second,
                    true
                )//用于判断是否显示主图按钮或者显示激活界面或者鲸图界面
            }
            viewModel.setInRouteFragment(destination.id == R.id.routeFragment)
            viewModel.setInAddHomeFragment(destination.id == R.id.searchAddHomeFragment || destination.id == R.id.mapPointDataFragment)
            viewModel.setInNaviFragment(destination.id == R.id.naviFragment || destination.id == R.id.simNaviFragment)
            viewModel.setInHomeFragment(destination.id == R.id.homeFragment)
            viewModel.setIsInPOICardPage(destination.id == R.id.POICardDialogFragment)
            viewModel.setInGroupModule(destination.id == R.id.myTeamFragment || destination.id == R.id.inviteJoinTeamFragment)
            viewModel.showActivateAgreement.postValue(destination.id == R.id.activateMapFragment || destination.id == R.id.agreementFragment)
            binding.showControlBtn =
                destination.id == R.id.homeFragment || destination.id == R.id.POICardDialogFragment
            if (destination.id != R.id.homeFragment && destination.id != R.id.POICardDialogFragment)
                viewModel.clearSomeCardTips()

            if (destination.id != R.id.homeFragment) {
                pushViewGone() //关闭组队消息弹框
                scenicSectorViewGone() //关闭景点推荐弹框
            }

        }

    //关闭组队消息弹框
    private fun pushViewGone() {
        try {
            binding.pushMessageContainer.removeAllViews()
            binding.pushMessageContainer.visibility = View.GONE
        } catch (e: Exception) {
            Timber.i("pushViewGone Exception:${e.message}")
        }
    }

    // 用于记录首次触摸位置
    private var startX = 0f
    var lastSlidingTime: Long = 0
    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(this, object : SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (e2.pointerCount == 3) { // 检测三指滑动
                    // 结束位置的 X 坐标
                    val endX = e2.x
                    // 计算滑动方向
                    if (startX - endX > 100) {
                        // 左滑 进入仪表全屏地图
                        Timber.d("gestureDetector 左滑")
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastSlidingTime >= 700) {
                            lastSlidingTime = currentTime
                            carDashboardBusiness.setCarDashboardShowNavi(true)
                        }
                    } else if (endX - startX > 100) {
                        // 右滑 退出仪表全屏地图
                        Timber.d("gestureDetector 右滑")
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastSlidingTime >= 700) {
                            lastSlidingTime = currentTime
                            carDashboardBusiness.setCarDashboardShowNavi(false)
                        }

                    }
                    //Timber.i("gestureDetector pointerCount == 3")
                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
        })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Timber.i("onSaveInstanceState() is called")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val decorView = window.decorView
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        decorView.systemUiVisibility = option
        window.statusBarColor = Color.TRANSPARENT

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.i("onCreate()")
        window.decorView.accessibilityPaneTitle = "v2swindow@psmap:home"
        initBinding()
        initMapPage()
        viewModel.setRestartMainActivity(false)
        AutoStatusAdapter.sendStatus(AutoStatus.MAIN_ACTIVITY_CREATE)
    }

    private fun initBinding() {
        binding.lifecycleOwner = this@MainActivity
        binding.viewmodel = viewModel
        binding.engineerViewModel = mainEngineerViewModel
        navController = findNavController(this, R.id.container)

        val isNightMode = viewModel.getFirstTimeNightMode()
        Timber.i("initBinding themeChange isNightMode $isNightMode")
        viewModel.updateAllSkinView(binding.root, isNightMode)

        viewModel.themeChange.unPeek().observe(this) {
            Timber.i("themeChange $it")
            viewModel.updateAllSkinView(binding.root, it)
            if (floatingView != null) {
                viewModel.updateAllSkinView(floatingView ?: binding.root, it)
            }
            mapBusiness.updateMapSurfaceViewInitColor(it == true) //当日夜模式修改时调用
            window.navigationBarColor = if (it) Color.BLACK else resources.getColor(com.desaysv.psmap.model.R.color.customColorNavigationBarColorDay)
        }
        netWorkManager.addNetWorkChangeListener(this)

        viewModel.showFloatingView.unPeek().observe(this) {
            val isJoinCall = viewModel.isJoinCall.value
            Timber.i("showFloatingView $it isJoinCall $isJoinCall")
            if (it) {
                if (isJoinCall == true) {
                    showInAppFloatingWindow()
                }
            } else {
                if (floatingView != null) {
                    (window.decorView as FrameLayout).removeView(floatingView)
                }
            }
        }

        viewModel.isJoinCall.unPeek().observe(this) {
            Timber.i("isJoinCall $it")
            if (!it) {
                if (floatingView != null) {
                    (window.decorView as FrameLayout).removeView(floatingView)
                }
            }
        }
    }

    private fun addNavigation() {
        navController.graph = navController.navInflater.inflate(R.navigation.nav_home)
        navController.addOnDestinationChangedListener(onDestinationChangedListener)
        navControllerUninitialized = false
    }


    private fun removeNavigation() {
        if (navControllerUninitialized) return
        navController.removeOnDestinationChangedListener(onDestinationChangedListener)
    }

    private fun initMapPage() {
        if (viewModel.isInitSuccess()) {
            initMapSuccessfullyTask()
        } else {
            Timber.i("initResult observe")
            val initResultTask: (initResult: InitSDKResult) -> Unit = { initResult ->
                Timber.i("initResultTask initResult =  ${initResult.code}")
                when (initResult.code) {
                    InitSdkResultType.FAIL_AAR_OVERDUE, InitSdkResultType.FAIL -> {
                        binding.showErrorTips = true
                    }

                    InitSdkResultType.FAIL_ACTIVATE -> {
                        binding.showErrorTips = false
                        navController.graph =
                            navController.navInflater.inflate(R.navigation.nav_home)
                        lifecycleScope.launch {
                            val currentDestination = navController.currentDestination
                            Timber.d(
                                " initMapPage FAIL_ACTIVATE showActivateAgreementOrMapButton==null:%s  currentDestination == null:%s",
                                viewModel.showActivateAgreementOrMapButton.value == null,
                                currentDestination == null
                            )
                            if (currentDestination != null) {
                                showActiveAgreementOrMapBase(
                                    currentDestination.id,
                                    viewModel.isShowActivateLayout.value ?: false,
                                    viewModel.isShowAgreementLayout.value ?: false,
                                    false
                                )//用于判断是否显示主图按钮或者显示激活界面或者鲸图界面
                            }
                        }
                    }

                    InitSdkResultType.OK -> {
                        binding.showErrorTips = false
                        initMapSuccessfullyTask()
                    }
                }
            }

            initResultTask(viewModel.initResult.value!!)
            viewModel.initResult.unPeek().observe(this) {
                Timber.i("observe initResult =  ${it.code}")
                initResultTask(it)
            }
        }
        viewModel.showRequestPermissionRationale.unPeek().observe(this) {
            onPermissionTask(it)
        }

        binding.ivScreenExtend.setDebouncedOnClickListener {
            viewModel.setScreenStatus(!binding.ivScreenExtend.isSelected)
        }

        //判断是否弹出U盘罐装提示框
        offlineDataBusiness.usbMount.unPeek().observe(this) { result ->
            Timber.i("usbMount result:$result  MAP_APP_FOREGROUND:${BaseConstant.MAP_APP_FOREGROUND}")
        }

        //根据是否燃油版，来显示一键补能的图标
        if (iCarInfoProxy.isT1JFL2ICE()) {
            binding.btCharge.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_fuel_night else R.drawable.selector_ic_fuel_day)
            binding.btCharge.setBackground(R.drawable.selector_ic_fuel_day, R.drawable.selector_ic_fuel_night)
        } else {
            binding.btCharge.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_charge_station_night else R.drawable.selector_ic_charge_station_day)
            binding.btCharge.setBackground(R.drawable.selector_ic_charge_station_day, R.drawable.selector_ic_charge_station_night)
        }

        ViewClickEffectUtils.addClickScale(binding.ivScreenExtend, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.teamView, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivGpsState, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.btnCarMode, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.btnSound, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.btCharge, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.btSetting, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.btnBackCar, CLICKED_SCALE_90)
    }

    private fun initMapSuccessfullyTask() {
        //----start 防止开机viewModel.themeChange.unPeek().observe(this)没有回调------
        Timber.i("initMapSuccessfullyTask isNightMode ${NightModeGlobal.isNightMode()}")
        viewModel.surfaceViewRenderComplete.observe(this) {
            Timber.i("surfaceViewRenderComplete $it")
            if (it) {
                lifecycleScope.launch {
                    delay(400)
                    if (BaseConstant.MAP_APP_FOREGROUND) {
                        binding.ivMapBg.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.updateAllSkinView(binding.root, NightModeGlobal.isNightMode())
        //----end 防止开机viewModel.themeChange.unPeek().observe(this)没有回调------
        MainScope().launch {
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            viewModel.glMapSurface.parent?.let {
                Timber.d("remove mapSurfaceView from parent.")
                try {
                    val viewGroup = it as ViewGroup
                    viewGroup.removeView(viewModel.glMapSurface)
                } catch (e: Exception) {
                    Timber.e("catch Exception: %s", e.message)
                }
            }
            val mapSurfaceView = viewModel.glMapSurface
            if (mapSurfaceView != null) {
                mapSurfaceView.holder.setFormat(PixelFormat.TRANSLUCENT)
                mapSurfaceView.holder.lockCanvas()?.apply {
                    drawColor(Color.TRANSPARENT)
                }
                mapSurfaceView.alpha = 0f
                delay(100)
                binding.mapContainer.removeAllViews()
                binding.mapContainer.addView(mapSurfaceView, lp)
                delay(400)
                mapSurfaceView.alpha = 1f
            } else {
                Timber.e("mapSurfaceView is null")
            }
        }
        Timber.i("showMapLayer() addView mapSurfaceView")
        addNavigation()
        initEventOperation()
        observeAllChange()

        lifecycleScope.launch {
            binding.cmpLogoScaleLine.setContent {
                LogoScaleLine(viewModel)
            }
        }
        //接收处理外部指令
        viewModel.mapCommand.observe(this) { mapCommand ->
            Timber.i("mapCommand $mapCommand")
            lifecycleScope.launch {
                delay(500)
                mapCommand?.let {
                    Timber.i("mapCommand ${it.mapCommandType}")
                    when (it.mapCommandType) {
                        KeyWordSearch -> {
                            val commandBean =
                                CommandRequestSearchBean.Builder().setKeyword(it.data).setPoi(it.poi)
                                    .setType(CommandRequestSearchBean.Type.SEARCH_KEYWORD)
                                    .apply {
                                        it.isVoice?.let {
                                            setIsVoiceSearch(it)
                                        }
                                    }
                                    .build()
                            navController.navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                        }

                        KeyWordSearchForCollect -> {
                            val commandBean =
                                CommandRequestSearchBean.Builder().setKeyword(it.data).setPoi(it.poi)
                                    .setType(CommandRequestSearchBean.Type.SEARCH_KEYWORD_COLLECT).apply {
                                        it.isVoice?.let {
                                            setIsVoiceSearch(it)
                                        }
                                    }.build()
                            navController.navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                        }

                        AroundSearch -> {
                            val commandBean =
                                CommandRequestSearchBean.Builder().setKeyword(it.data).setPoi(it.poi)
                                    .setType(CommandRequestSearchBean.Type.SEARCH_AROUND).setRange(it.range)
                                    .apply {
                                        it.isVoice?.let {
                                            setIsVoiceSearch(it)
                                        }
                                    }
                                    .build()
                            navController.navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                        }

                        StartPlanRoute -> {
                            it.poi?.let { poi ->
                                val commandBean = when {
                                    it.fromPoi != null && it.viaPoi != null -> {
                                        CommandRequestRouteNaviBean.Builder().build(it.fromPoi, poi, ArrayList(it.viaPoi!!))
                                    }

                                    it.viaPoi != null -> {
                                        CommandRequestRouteNaviBean.Builder().build(poi, ArrayList(it.viaPoi!!))
                                    }

                                    it.fromPoi != null -> {
                                        CommandRequestRouteNaviBean.Builder().build(it.fromPoi, poi, null)
                                    }

                                    else -> CommandRequestRouteNaviBean.Builder().build(poi)

                                }
                                viewModel.planRoute(commandBean)
                            }


                        }

                        MapCommandType.NaviToHome -> {
                            it.poi?.let { poi ->
                                val commandBean = CommandRequestRouteNaviBean.Builder().build(poi)
                                viewModel.planRoute(commandBean)
                            } ?: run {
                                val commandBean = CommandRequestSearchBean.Builder()
                                    .setType(CommandRequestSearchBean.Type.SEARCH_HOME)
                                    .setShouldShowKeyboard(true)
                                    .build()
                                navController.navigate(R.id.to_searchAddHomeFragment, commandBean.toBundle())
                            }
                        }

                        MapCommandType.NaviToWork -> {
                            it.poi?.let { poi ->
                                val commandBean = CommandRequestRouteNaviBean.Builder().build(poi)
                                viewModel.planRoute(commandBean)
                            } ?: run {
                                val commandBean = CommandRequestSearchBean.Builder()
                                    .setType(CommandRequestSearchBean.Type.SEARCH_COMPANY)
                                    .setShouldShowKeyboard(true)
                                    .build()
                                navController.navigate(R.id.to_searchAddHomeFragment, commandBean.toBundle())
                            }
                        }

                        StartNavi -> {
                            it.poi?.let { poi ->
                                val commandBean = when {
                                    it.fromPoi != null && it.viaPoi != null -> {
                                        CommandRequestRouteNaviBean.Builder().build(it.fromPoi, poi, ArrayList(it.viaPoi!!))
                                    }

                                    it.viaPoi != null -> {
                                        CommandRequestRouteNaviBean.Builder().build(poi, ArrayList(it.viaPoi!!))
                                    }

                                    it.fromPoi != null -> {
                                        CommandRequestRouteNaviBean.Builder().build(it.fromPoi, poi, null)
                                    }

                                    else -> CommandRequestRouteNaviBean.Builder().build(poi)

                                }
                                viewModel.startNavi(commandBean)
                            }
                        }

                        OpenSearchPage -> {
                            if (viewModel.isNavigating()) {
                                navController.navigate(
                                    R.id.to_searchAlongWayFragment,
                                    CommandRequestSearchBean().toBundle()
                                )
                            } else {
                                navController.navigate(
                                    R.id.to_searchFragment, CommandRequestSearchBean.Builder()
                                        .setShouldShowKeyboard(true).build().toBundle()
                                )
                            }
                        }

                        MapCommandType.OpenNaviPage -> {
                            if (viewModel.isRealNavi()) {
                                navController.popBackStack(R.id.naviFragment, false)
                            } else {
                                Timber.i("MapCommandType.OpenNaviPage. But isRealNavi = false")
                            }
                        }

                        MapCommandType.OpenGroupPage -> {
                            if (viewModel.isSimulationNavi()) {
                                mToast.showToast("模拟导航态下无法使用组队出行")
                            } else if (netWorkManager.isNetworkConnected()) {
                                if (settingAccountBusiness.isLoggedIn()) {
                                    if ((viewModel.teamInfo.value?.team_id ?: 0) == 0) {
                                        navController.navigate(R.id.to_createTeamFragment)
                                    } else {
                                        navController.navigate(
                                            R.id.to_myTeamFragment,
                                            Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_MAIN_TYPE) })
                                    }
                                } else {
                                    navController.navigate(R.id.to_createTeamFragment)
                                }
                            } else {
                                mToast.showToast(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again)
                            }
                        }

                        OpenSettingPage -> {
                            navController.navigate(
                                R.id.to_accountAndSettingFragment,
                                Bundle().also { data ->
                                    data.putInt(
                                        BaseConstant.ACCOUNT_SETTING_TAB,
                                        AccountAndSettingTab.SETTING
                                    )
                                })
                        }

                        OpenFavoritePage -> {
                            navController.navigate(
                                R.id.to_favoriteFragment,
                                Bundle().apply { putInt(Biz.TO_FAVORITE_TYPE, BaseConstant.TO_FAVORITE_MAIN_TYPE) })
                        }

                        MoveAppToBack -> {
                            moveTaskToBack(true)
                        }

                        AlongWaySearch -> {
                            val commandBean =
                                CommandRequestSearchCategoryBean.Builder().setKeyword(it.data)
                                    .setType(CommandRequestSearchCategoryBean.Type.SEARCH_ALONG_WAY)
                                    .apply {
                                        it.isVoice?.let {
                                            setIsVoiceSearch(it)
                                        }
                                    }
                                    .build()
                            navController.navigate(
                                R.id.to_searchAlongWayResultFragment,
                                commandBean.toBundle()
                            )
                        }

                        ShowPoiCard -> {
                            navController.popBackStack(R.id.homeFragment, false)
                            it.poi?.let { poi ->
                                viewModel.showPoiCard(poi)
                            }
                        }

                        ShowPoiDetail -> {
                            it.poi?.let { poi ->
                                viewModel.showPoiDetail(poi)
                            }
                        }

                        Confirm -> {
                            viewModel.confirmInNavi(destinationId == R.id.searchAlongWayResultFragment)
                        }

                        MapCommandType.SearchHomeCompanyAddressResultPage -> {
                            Timber.i("SearchHomeCompanyAddressResultPage pair=${it.pair}")
                            it.pair?.let { pair ->
                                val commandBean = CommandRequestSearchBean.Builder()
                                    .setType(if (pair.second == 0) CommandRequestSearchBean.Type.SEARCH_HOME else CommandRequestSearchBean.Type.SEARCH_COMPANY)
                                    .setIsVoiceSearch(true)
                                    .setKeyword(pair.first)
                                    .build()
                                navController.navigate(R.id.to_searchResultFragment, commandBean.toBundle())
                            }
                        }

                        MapCommandType.OpenModifyHomeCompanyAddressPage -> {
                            Timber.i("OpenModifyHomeCompanyAddressPage data=${it.data}")
                            it.data?.let { data ->
                                val commandBean = CommandRequestSearchBean.Builder()
                                    .setType(if (data == "家") CommandRequestSearchBean.Type.SEARCH_HOME else CommandRequestSearchBean.Type.SEARCH_COMPANY)
                                    .build()
                                navController.navigate(R.id.to_searchAddHomeFragment, commandBean.toBundle())
                                viewModel.notifyModifyHomeCompanyAddress(data)
                            }
                        }

                        MapCommandType.SearchAhaTrip -> {
                            val commandBean =
                                CommandRequestSearchBean.Builder().setKeyword(it.data)
                                    .setDay(it.day)
                                    .setCity(it.cityItemInfo)
                                    .setType(CommandRequestSearchBean.Type.SEARCH_CUSTOM_AHA_TRIP).build()
                            navController.navigate(R.id.to_ahaTripMainFragment, commandBean.toBundle())
                        }

                        MapCommandType.OpenAhaTripDetailPage -> {
                            val commandBean =
                                CommandRequestSearchBean.Builder().setKeyword(it.data)
                                    .setType(CommandRequestSearchBean.Type.SEARCH_CUSTOM_AHA_TRIP).build()
                            navController.navigate(R.id.to_ahaTripMainFragment, commandBean.toBundle())
                        }

                        else -> {}
                    }
                }
            }

        }

        viewModel.screenStatus.observe(this) { screenStatus ->
            Timber.i("screenStatus is called = $screenStatus")//分屏状态
            viewModel.setMapCenterNavi(screenStatus)
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                Pair(EventTrackingUtils.EventValueName.MappageForm, if (screenStatus) 0 else 1)
            )
        }
        // 创建一个ValueAnimator对象，指定透明度从0到1再从1到0的变化
        animator = ValueAnimator.ofFloat(1f, 0f, 1f)
        // 设置动画时长
        animator.setDuration(1000)
        // 设置动画重复模式为循环
        animator.repeatMode = ValueAnimator.RESTART
        animator.repeatCount = ValueAnimator.INFINITE
        // 监听动画数值变化
        animator.addUpdateListener(AnimatorUpdateListener { animation: ValueAnimator ->
            val alpha = animation.animatedValue as Float
            binding.speedingView.alpha = alpha
        })
        viewModel.naviCameraList.unPeek().observeForever(naviCameraOb)//0x21 电子眼的数据

        //互联导航 通知结束本地导航
        viewModel.phoneLinkNaviStopLocalNavi.unPeek().observeForever {
            Timber.i("phoneLinkNaviStopLocalNavi:$it")
            if (viewModel.isNavigating()) {
                viewModel.stopNavi()
            }
        }
        if (viewModel.restartMainActivity) {
            //todo 恢复导航
        } else {
            viewModel.initMapSuccessfullyTask()
        }
        viewModel.gpsState.observe(this) {
            Timber.d("gpsState is called = $it")
            viewModel.locationPermissionUse(it)
        }

        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.LoginStatus, if (settingAccountBusiness.isLogin()) 0 else 1)
            )
        )
        if (settingAccountBusiness.isLogin()) {
            mobileInternetWXBusiness.initWXStatusData(true)
        } else {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Map_Set,
                mapOf(
                    Pair(EventTrackingUtils.EventValueName.WeChatStatus, 2)
                )
            )
        }
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.PhoneToCarStatus, if (settingAccountBusiness.isLogin()) 1 else 2)
            )
        )
    }

    private fun observeAllChange() {
        viewModel.zoomOutEnable.unPeek().observe(this) {
            if (!it) mToast.showToast(R.string.sv_map_zoom_out_max_tip)
        }
        viewModel.zoomInEnable.unPeek().observe(this) {
            if (!it) mToast.showToast(R.string.sv_map_zoom_in_max_tip)
        }
        viewModel.loginStatus.unPeek().observe(this) {
            when (it) {
                BaseConstant.LOGIN_STATE_SUCCESS, BaseConstant.LOGIN_STATE_GUEST -> {
                    Timber.i("loginStatus $it")
                    viewModel.accountLoginStatusChange()
                    if (BaseConstant.LOGIN_STATE_SUCCESS == it) {
                        viewModel.getTeamUserStatus("loginStatus") //获取组队状态
                        viewModel.doTeamMessageListener()  //TeamMessageListener监听
                        viewModel.isLoginSuccessfully(true)
                    } else {
                        viewModel.isLoginSuccessfully(false)
                    }
                }

            }
        }

        viewModel.isRequestRoute.unPeek().observe(this) {
            Timber.i("isRequestRoute currentDestination =  ${navController.currentDestination}")
            Timber.i("isRequestRoute is called $it")
            loadingUtil.cancelLoading()
            if (it) {
                loadingUtil.showLoading(R.string.sv_route_navi_route_planning, onItemClick = {
                    outsideUnInit()
                    viewModel.recoverRouteViaPois()
                })
            } else {
                if (!viewModel.isPlanRouteing()) {
                    //路线详情和添加途经点页面不反初始化
                    viewModel.outsideUnInit()
                }
                if (viewModel.routeErrorMessage.value?.isNotEmpty() == true) {
                    Timber.i("isRequestRoute routeErrorMessage is called")
                    mToast.showToast(viewModel.routeErrorMessage.value)
                } else if (!viewModel.pathListLiveData.value.isNullOrEmpty()) {
                    if (navController.currentDestination?.id != R.id.routeFragment) {
                        if (!viewModel.isPlanRouteing()) {
                            val commandBean = CommandRequestRouteNaviBean.Builder()
                                .buildNoNeedPlanRoute(mRouteRequestController.carRouteResult)
                            navController.navigate(R.id.to_routeFragment, commandBean.toBundle())
                        } else {
                            Timber.i("isRequestRoute viewModel.isNavigating() = ${viewModel.isNavigating()}")
                            if (viewModel.isNavigating()) {
                                navController.popBackStack(R.id.naviFragment, false)
                            } else {
                                navController.popBackStack(R.id.routeFragment, false)
                            }
                        }
                    }
                }
            }
        }
        viewModel.showGetSAPAInfoDialogVisible.unPeek().observe(this) {
            loadingUtil.cancelLoading()
            if (it) {
                Timber.i("showGetSAPAInfoDialogVisible is called")
                loadingUtil.showLoading(R.string.sv_navi_route_high_speed_loading, onItemClick = {
                    viewModel.setShowGetSAPAInfoDialogVisible(false)
                })
            }
        }
        viewModel.loadingView.unPeek().observe(this) {
            Timber.i("loadingView currentDestination =  ${navController.currentDestination}")
            Timber.i("loadingView is called $it")
            loadingUtil.cancelLoading()
            if (it) {
                Timber.i("loadingView is called isNavigating = ${viewModel.isNavigating()}")
                if (!viewModel.isNavigating()) {
                    viewModel.initNaviScope()
                    viewModel.setNaviType()
                }
                loadingUtil.showLoading(R.string.sv_route_navi_route_planning, onItemClick = {
                    viewModel.abortRequestTaskId()
                    if (!viewModel.isNavigating()) {
                        viewModel.cancelNaviScope()
                    }
                    viewModel.recoverViaPois()
                })
            } else {
                if (viewModel.naviErrorMessage.value?.isNotEmpty() == true) {
                    Timber.i("loadingView naviErrorMessage is called")
                    mToast.showToast(viewModel.naviErrorMessage.value)
                } else {
                    if (viewModel.isRealNavi()) {

                        if (!isFragmentInBackStack(navController, R.id.naviFragment)) {
                            Timber.i("loadingView to_naviFragment is called, but not in back stack")
                            // 如果不在返回栈，执行备用逻辑
                            val commandBean = CommandRequestRouteNaviBean.Builder().buildNoNeedPlanRoute(mRouteRequestController.carRouteResult)
                            viewModel.setNotDeleteRoute(true)
                            navController.navigate(R.id.to_naviFragment, commandBean.toBundle())
                        } else {
                            // 如果在返回栈，尝试弹出
                            Timber.i("loadingView popBackStack is called")
                            navController.popBackStack(R.id.naviFragment, false)
                        }
                    } else {
                        Timber.i("loadingView to_naviFragment is called")
                        val commandBean = CommandRequestRouteNaviBean.Builder().buildNoNeedPlanRoute(mRouteRequestController.carRouteResult)
                        viewModel.setNotDeleteRoute(true)
                        navController.navigate(R.id.to_naviFragment, commandBean.toBundle())
                    }
                }
            }
        }

        viewModel.naviGreenWaveCarSpeedVisible.unPeek().observe(this) {
            //绿波车速显示监听
            Timber.i("naviGreenWaveCarSpeedVisible is called isStartAnimator = $isStartAnimator；it = $it")
            binding.greenSpeedingView.visibility = if (!isStartAnimator && it) View.VISIBLE else View.GONE
        }
    }

    /**
     * 判断当前Fragment是否在返回栈中
     */
    private fun isFragmentInBackStack(navController: NavController, @IdRes fragmentId: Int): Boolean {
        return try {
            navController.getBackStackEntry(fragmentId) // 检查是否存在
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    private fun onPermissionTask(it: List<String>) {
        if (it.isNotEmpty()) {
            if (it[0] == PERMISSION_OK) {
                Timber.i("PERMISSION_OK initMap")
                viewModel.initMap()
            } else {
                var tips = when {
                    it.contains(Manifest.permission.ACCESS_FINE_LOCATION) && it.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                        if (it.contains(Manifest.permission.RECORD_AUDIO)) "地图需要位置权限和麦克风权限" else "地图需要位置权限"
                    }

                    it.contains(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                        if (it.contains(Manifest.permission.RECORD_AUDIO)) "地图需要位置权限和麦克风权限" else "地图需要位置权限"
                    }

                    it.contains(Manifest.permission.ACCESS_BACKGROUND_LOCATION) -> {
                        if (it.contains(Manifest.permission.RECORD_AUDIO)) "地图需要后台访问位置权限和麦克风权限" else "地图需要后台访问位置权限"
                    }

                    else -> {
                        "地图需要麦克风权限"
                    }

                }
                tips = tips.plus(getString(R.string.sv_common_permission_how))
                //提示为什么需要这些权限
                dismissPermissionTipsDialog()
                dialogPermissionTips = CustomDialogFragment.builder()
                    .singleButton(getString(com.desaysv.psmap.base.R.string.sv_common_got_it)).setContent(tips).setOnClickListener {
                        //用户可能看到提示后，不点击弹窗，去设置授予了权限回来后再点击
                        if (viewModel.requiredPermissionsIsOk()) {
                            Timber.i("btnConfirmPermissionTips initMap")
                            viewModel.initMap()
                        } else {
                            Timber.i("btnConfirmPermissionTips finish activity")
                            finish()
                        }
                    }.apply {
                        show(this@MainActivity.supportFragmentManager, "dialogPermissionTips")
                    }
                viewModel.clearPermissions()
            }
        } else if (viewModel.requiredPermissionsIsOk()) {
            if (!viewModel.isInitSuccess()) {
                Timber.i("doPermissionTask initMap")
                viewModel.initMap()
            }

        }

    }

    private fun initEventOperation() {
        //进入设置模块
        binding.btSetting.setDebouncedOnClickListener {
            Timber.i("btSetting 进入设置模块")
            navController.navigate(R.id.to_accountAndSettingFragment,
                Bundle().also {
                    it.putInt(
                        BaseConstant.ACCOUNT_SETTING_TAB,
                        AccountAndSettingTab.ACCOUNT
                    )
                })
        }

        //进入组队出行模块
        binding.teamView.setDebouncedOnClickListener {
            if (viewModel.isSimulationNavi()) {
                mToast.showToast("模拟导航态下无法使用组队出行")
            } else if (netWorkManager.isNetworkConnected()) {
                navController.navigate(
                    R.id.to_myTeamFragment,
                    Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_MAIN_TYPE) })
            } else {
                mToast.showToast(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again)
            }
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.TeamClick, System.currentTimeMillis()))
            )
        }

        binding.ivGpsState.setDebouncedOnClickListener {
            navController.navigate(R.id.to_gnssInfoDialogFragment)
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.GPSLocClick, System.currentTimeMillis()))
            )
        }

        /**判断是否显示主图按钮或者显示激活界面或者鲸图界面
         *通过添加 distinctUntilChanged() 方法，只有当 isShowActivate 或 isShowAgreement 的值发生变化时，
         * 才会触发观察者并执行处理逻辑。这样可以避免重复执行相同的逻辑。
         */
        viewModel.showActivateAgreementOrMapButton.unPeek().distinctUntilChanged()
            .observe(this) { (isShowActivate, isShowAgreement) ->
                Timber.d("showActivateAgreementOrMapButton isShowActivate:$isShowActivate isShowAgreement：$isShowAgreement")
                val currentDestination = navController.currentDestination
                if (currentDestination != null) {
                    showActiveAgreementOrMapBase(
                        currentDestination.id,
                        isShowActivate,
                        isShowAgreement,
                        false
                    )//用于判断是否显示主图按钮或者显示激活界面或者鲸图界面
                }
            }


        //显示邀请组队弹条
        viewModel.showPushCardView.unPeek().observe(this) {
            try {
                if (it != null && navController.currentDestination?.id == R.id.homeFragment) {
                    binding.pushMessageContainer.removeAllViews()
                    binding.pushMessageContainer.visibility = View.VISIBLE
//                    mPushCardView?.layoutParams = layoutParams
                    binding.pushMessageContainer.addView(mPushCardView)
                    mPushCardView?.show(it)
                    mPushCardView?.setPushCardViewListener(object : PushCardView.PushCardViewListener {
                        override fun onclick(teamPushData: TeamPushData?) {
                            val poi = teamPushData?.poi
                            val code = teamPushData?.code
                            pushViewGone() //关闭组队消息弹框
                            if (poi != null) {
                                Timber.i("mPushCardView onclick poi:${gson.toJson(poi)}")
                                if (netWorkManager.isNetworkConnected()) {
                                    viewModel.startRoute(poi)
                                } else {
                                    mToast.showToast(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again)
                                }
                            } else if (code != null) {
                                if (netWorkManager.isNetworkConnected()) {
                                    loadingUtil.cancelLoading()
                                    loadingUtil.showLoading("正在加入队伍，请稍等")
                                    viewModel.joinTeam(code)
                                } else {
                                    mToast.showToast(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again)
                                }
                            }
                        }
                    })
                    /*mPushCardView?.setOnShowJoinDialogListener(object :
                        PushCardView.ShowJoinDialogListener {
                        override fun showJoinDialog(gotoTeamOrDes: Boolean) {//进入组队
                            lifecycleScope.launch(Dispatchers.Main) {
                                pushViewGone() //关闭组队消息弹框
                                Timber.d("showJoinDialog gotoTeam:$gotoTeamOrDes")
                                if (gotoTeamOrDes) {
                                    if (it.type == 0) {//组队消息
                                        if (viewModel.isSimulationNavi()) {
                                            mToast.showToast("模拟导航态下无法使用组队出行")
                                        } else if (netWorkManager.isNetworkConnected()) {
                                            navController.navigate(
                                                R.id.to_myTeamFragment,
                                                Bundle().apply {
                                                    putInt(
                                                        Biz.TO_TEAM_TYPE,
                                                        BaseConstant.TO_TEAM_MAIN_TYPE
                                                    )
                                                })
                                        } else {
                                            mToast.showToast(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again)
                                        }
                                    } else { //队长设置目的地
                                        val coord2DDouble =
                                            OperatorPosture.mapToLonLat(
                                                it.destination.display.lon.toDouble(),
                                                it.destination.display.lat.toDouble()
                                            )
                                        val poi = POIFactory.createPOI()
                                        poi.point = GeoPoint(coord2DDouble.lon, coord2DDouble.lat)
                                        poi.name = it.destination.name
                                        poi.addr = it.destination.address
                                        val commandBean = CommandRequestRouteNaviBean.Builder().build(poi)
                                        viewModel.startNavi(commandBean)
                                    }
                                }
                            }
                        }
                    })*/
                }
            } catch (e: Exception) {
                Timber.i("showPushCardView Exception:${e.message}")
            }
        }

        //POI消息接收
        viewModel.notifyPoiPushMessage.unPeek().observe(this) { poiPusMessage ->
            poiPusMessage?.run {
                Timber.i("notifyPoiPushMessage ${content?.name}")
                if (AppUtils.mapIsTopAPP(application.applicationContext) && viewModel.isSend2CarPoiMsg(this)) {
                    val poiP = POIFactory.createPOI(
                        content.name,
                        GeoPoint(
                            content.lon.toDouble() / 1000000,
                            content.lat.toDouble() / 1000000
                        ),
                        content.poiId
                    ).apply {
                        addr = content.address
                    }
                    viewModel.addHomeCardTipsData(
                        HomeCardTipsData(
                            HomeCardTipsType.SEND_TO_CAR_POI, "收到地址", poiP
                                .name, "查看地址"
                        ).apply { poi = poiP }
                    )
                }
            }
        }

        //路线消息推送接收
        viewModel.notifyRoutePushMessage.unPeek().observe(this) { routePusMessage ->
            routePusMessage?.run {
                Timber.i("notifyRoutePushMessage ${content?.routeParam?.destination?.name} linkMode：$linkMode")
                if (linkMode == 2 || TextUtils.isEmpty(traceId)) {
                    val poiR = POIFactory.createPOI(
                        content.routeParam.destination.name,
                        GeoPoint(
                            content.path.endPoints.points[0].lon.toDouble(),
                            content.path.endPoints.points[0].lat.toDouble()
                        )
                    )
                    viewModel.addHomeCardTipsData(
                        HomeCardTipsData(
                            HomeCardTipsType.SEND_TO_CAR_ROUTE, "收到路线", poiR
                                .name, "规划路线"
                        ).apply { poi = poiR }
                    )
                } else {
                    val commandBean = CommandRequestRouteNaviBean.Builder().buildByPushRoute(routePusMessage)
                    viewModel.pushMsgStartNavi(commandBean)
                }
            }

        }

        //开始导航
        viewModel.linkCarStartNavi.unPeek().observe(this) { poi ->
            poi?.let {
                Timber.i("linkCarStartNavi")
                val commandBean = CommandRequestRouteNaviBean.Builder().build(it)
                viewModel.startNavi(commandBean)
            }
        }

        //结束导航
        viewModel.linkCarExitNavi.unPeek().observe(this) {
            Timber.i("linkCarExitNavi $it")
            if (it)
                viewModel.exitNavi()
        }

        //路线通知，开始手车互联
        viewModel.linkRoutePushMessage.unPeek().observe(this) {
            if (it != null) {
                Timber.i("linkRoutePushMessage ${it.content?.routeParam?.destination?.name} linkMode：$it.linkMode")
                if (it.linkMode == 2 || TextUtils.isEmpty(it.traceId)) {
                    val poiR = POIFactory.createPOI(
                        it.content.routeParam.destination.name,
                        GeoPoint(
                            it.content.path.endPoints.points[0].lon.toDouble(),
                            it.content.path.endPoints.points[0].lat.toDouble()
                        )
                    )
                    viewModel.addHomeCardTipsData(
                        HomeCardTipsData(
                            HomeCardTipsType.SEND_TO_CAR_ROUTE, "收到路线", poiR
                                .name, "规划路线"
                        ).apply { poi = poiR }
                    )
                } else {
                    val commandBean = CommandRequestRouteNaviBean.Builder().buildByPushRoute(it)
                    viewModel.pushMsgStartNavi(commandBean)
                }
            }
        }

        //组队消息推送卡片中的toast
        viewModel.groupToast.unPeek().observe(this) {
            mToast.showToast(it)
        }

        //判断是否在组队界面
        viewModel.isPushMessageLiveData.unPeek().observe(this) {
            if (navController.currentDestination?.id == R.id.myTeamFragment || navController.currentDestination?.id == R.id.inviteJoinTeamFragment) {
                viewModel.doStartGroupPosition(false)  //上报自己位置
            } else {
                viewModel.doStartGroupPosition(true)  //上报自己位置
            }
        }

        //关闭组队消息推送卡片
        viewModel.pushMessageCloseView.unPeek().observe(this) {
            mPushCardView?.closeView()
        }

        binding.btCharge.setDebouncedOnClickListener {
            val commandBean = CommandRequestSearchBean.Builder().setKeyword("充电站")
                .setType(CommandRequestSearchBean.Type.SEARCH_CHARGE).build()
            navController.navigate(
                R.id.to_searchResultFragment,
                commandBean.toBundle()
            )
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                Pair(EventTrackingUtils.EventValueName.OnerefuelClick, System.currentTimeMillis())
            )
        }

        binding.btnCarMode.setDebouncedOnClickListener {
            val switchMapViewMode = viewModel.switchMapViewMode()
            when (switchMapViewMode) {
                MapModeType.VISUALMODE_3D_CAR -> {
                    mToast.showToast(R.string.sv_map_switch_3dcar_tips)
                }

                MapModeType.VISUALMODE_2D_CAR -> {
                    mToast.showToast(R.string.sv_map_switch_2dcar_tips)
                }

                MapModeType.VISUALMODE_2D_NORTH -> {
                    mToast.showToast(R.string.sv_map_switch_north_tips)
                }

                else -> {}
            }
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Map_Set,
                mapOf(
                    Pair(EventTrackingUtils.EventValueName.TowardClick, System.currentTimeMillis()),
                    Pair(EventTrackingUtils.EventValueName.TowardSet, switchMapViewMode)
                )
            )
        }

        binding.btnSound.setDebouncedOnClickListener {
            val mute = !binding.btnSound.isSelected
            viewModel.setPlayTTsMute(mute)
            binding.btnSound.contentDescription =
                if (mute) binding.btnSound.resources.getString(R.string.sv_main_text_no_mute_accessibility) else binding.btnSound.resources.getString(
                    R.string.sv_main_text_mute_accessibility
                )
        }

        viewModel.mapPointCard.unPeek().observe(this) { cardData ->
            try {
                cardData?.run {
                    Timber.i("cardData.showStatus = ${cardData.showStatus} cardData.cardType=${cardData.cardType}")
                    if (cardData.showStatus) {
                        if (navController.currentDestination?.id != R.id.POICardDialogFragment) {
                            navController.navigate(R.id.to_POICardDialogFragment)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.i("mapPointCard Exception message = ${e.message}")
            }
        }

        viewModel.pointDetail.unPeek().observe(this) { cardData ->
            try {
                cardData?.run {
                    Timber.i("pointDetail cardData.showStatus = ${cardData.showStatus} cardData.cardType=${cardData.cardType}")
                    if (cardData.showStatus) {
                        if (navController.currentDestination?.id != R.id.POICardDialogFragment) {
                            navController.navigate(R.id.to_POIDetailFragment)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.i("mapPointCard Exception message = ${e.message}")
            }
        }

        binding.btnAdasStart.setDebouncedOnClickListener {
            mToast.showToast("智驾功能开发中...")
        }

        //队伍状态，0正常，1无效，2解散，3移除或退出，4队伍信息变化
        viewModel.teamHasQuitOrDismiss.unPeek().observe(this) {
            if (it == 2 || it == 3) {
                if (binding.pushMessageContainer.visibility == View.VISIBLE) {
                    Timber.i("teamHasQuitOrDismiss 组队推送消息存在拉，不弹这个框")
                } else {
                    Timber.i("teamHasQuitOrDismiss 没有组队推送 ，在其他设备，弹出")
                    dismissTeamDialog()
                    teamDialog = CustomDialogFragment.builder().setTitle(if (it == 2) "您所在的队伍已被队长解散" else "您已退出队伍")
                        .setContent("(3)秒后离开此界面")
                        .setCountDown(4000)
                        .setIsTeam(true).singleButton(getString(com.desaysv.psmap.base.R.string.sv_common_got_it))
                        .setOnClickListener {}
                        .apply {
                            show(supportFragmentManager, "teamHasQuitOrDismiss")
                        }
                }
            }
        }

        //打开收藏夹
        viewModel.openUserFavorite.unPeek().observe(this) {
            navController.navigate(R.id.to_favoriteFragment,
                Bundle().apply { putInt(Biz.TO_FAVORITE_TYPE, BaseConstant.TO_FAVORITE_MAIN_TYPE) })
        }

        //回到地图主图
        viewModel.backToMap.unPeek().observe(this) {
            Timber.i("finishObserver backToMap")
            navController.popBackStack(R.id.homeFragment, false)
        }

        //回到地图主图--在导航/模拟导航需要退出
        viewModel.backToMapHome.unPeek().observeForever(backToMapHomeOb)

        //回到导航主图
        viewModel.backToNavi.unPeek().observe(this) {
            navController.popBackStack(R.id.naviFragment, false)
        }

        //巡航景点播报请求
        ahaTripBusiness.requestScenicSector.unPeek().observe(this) {
            lifecycleScope.launch {
                //巡航景点推荐开关
                ahaTripImpl.requestScenicSector(5000)
            }
        }

        ////显示巡航景点推荐
        ahaTripBusiness.scenicSectorData.unPeek().observe(this) {
            showScenicSectorView(it) //显示巡航景点推荐
        }

        viewModel.naviStatisticsInfo.unPeek().observe(this) {
            //驾车导航过程 统计信息
            Timber.i(" naviStatisticsInfo = ${gson.toJson(it)}")
            it?.let {
                Timber.i(" naviStatisticsInfo go to driverReportFragment")
                navController.navigate(R.id.to_driverReportFragment)
            }
        }

        //加入队伍结果
        viewModel.joinTeamResult.unPeek().observe(this) {
            loadingUtil.cancelLoading()
            if (it) {
                mToast.showToast(getString(com.autosdk.R.string.agroup_join_success))
                navController.navigate(
                    R.id.to_myTeamFragment,
                    Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_MAIN_TYPE) })
            } else {
                mToast.showToast(getString(com.autosdk.R.string.agroup_join_team_error))
            }
        }
    }

    private fun dismissPermissionTipsDialog() {
        dialogPermissionTips?.run {
            if (isAdded || isVisible)
                dismissAllowingStateLoss()
        }
        dialogPermissionTips = null
    }

    private fun dismissTeamDialog() {
        teamDialog?.run {
            if (isAdded || isVisible)
                dismissAllowingStateLoss()
        }
        teamDialog = null
    }

    //用于判断是否显示主图按钮或者显示激活界面或者鲸图界面
    private fun showActiveAgreementOrMapBase(
        currentDestinationId: Int,
        isShowActivate: Boolean,
        isShowAgreement: Boolean,
        isDestinationChanged: Boolean
    ) {
        when (currentDestinationId) {
            R.id.activateMapFragment -> {
                Timber.d("currentDestination activateMapFragment")
            }

            else -> {
                binding.showMapBase = when {
                    isShowActivate -> {
                        if (!isDestinationChanged) {
                            navController.navigate(R.id.to_activateMapFragment)
                        }
                        false
                    }

                    isShowAgreement && currentDestinationId != R.id.agreementFragment -> {
                        if (!isDestinationChanged) {
                            navController.navigate(R.id.to_agreementFragment)
                        }
                        false
                    }

                    viewModel.isNavigating() && currentDestinationId == R.id.POICardDialogFragment -> false

                    else -> currentDestinationId == R.id.homeFragment || currentDestinationId == R.id.routeFragment
                            || currentDestinationId == R.id.searchFragment || currentDestinationId == R.id.naviFragment
                            || currentDestinationId == R.id.searchResultFragment || currentDestinationId == R.id.searchCategoryFragment
                            || currentDestinationId == R.id.searchAddHomeFragment || currentDestinationId == R.id.searchAlongWayResultFragment
                            || currentDestinationId == R.id.simNaviFragment
                            || currentDestinationId == R.id.POICardDialogFragment
                }

                binding.showGroupBtn = when {
                    isShowActivate -> {
                        if (!isDestinationChanged) {
                            navController.navigate(R.id.to_activateMapFragment)
                        }
                        false
                    }

                    isShowAgreement && currentDestinationId != R.id.agreementFragment -> {
                        if (!isDestinationChanged) {
                            navController.navigate(R.id.to_agreementFragment)
                        }
                        false
                    }

                    else -> currentDestinationId != R.id.createTeamFragment
                            && currentDestinationId != R.id.myTeamFragment
                            && currentDestinationId != R.id.inviteJoinTeamFragment
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume()")
//        viewModel.checkIOVLicenseIsOK()
    }

    private var screenBitmap: Bitmap? = null

    private val screenHandle: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onPause() {
        getScreenBitmap()
        super.onPause()
        Timber.i("onPause()")
    }

    private fun getScreenBitmap() {
        if (screenBitmap == null) {
            window.decorView.let { decorView ->
                if (decorView.width > 0 && decorView.height > 0)
                    screenBitmap = Bitmap.createBitmap(decorView.width, decorView.height, Bitmap.Config.ARGB_8888)
            }
        }
        screenBitmap?.let { bitmap ->
            if (viewModel.isInitSuccess()) {
                if (viewModel.glMapSurface.holder.surface.isValid) {
                    PixelCopy.request(
                        viewModel.glMapSurface, bitmap,
                        { copyResult ->
                            if (PixelCopy.SUCCESS == copyResult && isCenterNotBlack(bitmap)) {
                                Timber.i("getScreenBitmap PixelCopy SUCCESS")
                                binding.ivMapBg.setImageBitmap(bitmap)
                            } else {
                                Timber.i("getScreenBitmap PixelCopy FAILED $copyResult")
                            }
                        }, screenHandle
                    )
                }
            }
        }

    }

    private fun isCenterNotBlack(bitmap: Bitmap): Boolean {
        val centerX = bitmap.width / 2
        val centerY = bitmap.height / 2
        val centerPixel = bitmap.getPixel(centerX, centerY)

        // 检查 RGB 是否全为 0（纯黑）
        return (Color.red(centerPixel) != 0 ||
                Color.green(centerPixel) != 0 ||
                Color.blue(centerPixel) != 0)
    }

    override fun onStart() {
        binding.ivMapBg.visibility = View.VISIBLE
        super.onStart()
        Timber.i("onStart()")
        if (!viewModel.isPermissionRequesting())
            viewModel.requestPermissions(this)
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        lifecycleScope.launch {
            loadingUtil.cancelLoading()
            dismissTeamDialog()
            dismissScenicSectorDialog()
            dismissPermissionTipsDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setRestartMainActivity(true)
        Timber.i("onDestroy()")
        removeNavigation()
        viewModel.naviCameraList.unPeek().removeObserver(naviCameraOb)
        viewModel.backToMapHome.unPeek().removeObserver(backToMapHomeOb)
        netWorkManager.removeNetWorkChangeListener(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.i("onNewIntent()")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("onRequestPermissionsResult,,,,,,,,")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // 点击非输入框位置优先隐藏软键盘
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (carDashboardBusiness.dashboardReady()) {
            if (ev.action == MotionEvent.ACTION_DOWN)
                startX = ev.x; // 记录开始的 X 坐标
            gestureDetector.onTouchEvent(ev)
        }
        if (ev.action == MotionEvent.ACTION_DOWN) {
            Timber.i("dispatchTouchEvent() ACTION_DOWN called with: ev = $ev")
            pendingCollapseKeyword = isShouldHideInput(ev)
            viewModel.checkResetBackCppTimer()
        } else if (ev.action == MotionEvent.ACTION_UP) {
            Timber.i("dispatchTouchEvent() ACTION_UP called with: ev = $ev")
            if (pendingCollapseKeyword) {
                KeyboardUtil.hideKeyboard(binding.root)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isShouldHideInput(event: MotionEvent): Boolean {
        val v = currentFocus
        if (v is EditText) {
            val location = intArrayOf(0, 0)
            v.getLocationInWindow(location)
            return event.x < location[0] || event.x > location[0] + v.getWidth() || event.y < location[1] || event.y > location[1] + v.getHeight()
        }
        return false
    }

    /**
     * 0x21 电子眼的数据
     */
    private val naviCameraOb = Observer<ArrayList<NaviCameraExt>?> { data ->
        lifecycleScope.launch {
            if (!data.isNullOrEmpty()) {
                val naviCamera = data.firstOrNull() ?: return@launch
                var smallSpeed: Short = 0
                for (subCamera in naviCamera.subCameras) {
                    if (subCamera.subType == SubCameraExtType.SubCameraExtTypeUltrahighSpeed ||
                        subCamera.subType == SubCameraExtType.SubCameraExtTypeVariableSpeed
                    ) {
                        if (subCamera.speed != null && subCamera.speed.isNotEmpty()) {
                            Timber.i(" subCamera.speed: ${subCamera.speed}; naviCamera.distance = ${naviCamera.distance}")
                            val sp = subCamera.speed[0]
                            if (smallSpeed == 0.toShort() || sp > smallSpeed) {
                                smallSpeed = sp
                            }
                        }
                    }
                }

                if (smallSpeed > 0) {
                    Timber.i(" naviCameraOb smallSpeed: $smallSpeed carSpeed: ${viewModel.getCurSpeed()} distance = ${naviCamera.distance}")
                    if (viewModel.getCurSpeed() > smallSpeed) { //超速提醒
                        binding.speedingView.visibility = View.VISIBLE
                        if (isStartAnimator) return@launch
                        binding.greenSpeedingView.visibility = View.GONE
                        if (animator.isPaused) animator.resume() else animator.start()
                        AutoStatusAdapter.sendStatus(AutoStatus.SPEEDING)
                        isStartAnimator = true
                    } else {
                        stopAnimator()
                    }
                } else {
                    stopAnimator()
                }
            } else {
                stopAnimator()
            }
        }
    }

    private val backToMapHomeOb = Observer<Boolean> {
        Timber.i("backToMapHome")
        if (viewModel.isNavigating()) {
            viewModel.stopNavi()
            if (!viewModel.isRealNavi()) {
                navController.popBackStack(R.id.homeFragment, false)
            }
        } else if (navController.currentDestination?.id != R.id.homeFragment) {
            navController.popBackStack(R.id.homeFragment, false)
        }
    }

    private fun stopAnimator() {
        if (isStartAnimator) {
            Timber.i(" naviCameraOb stopAnimator is called")
            binding.speedingView.visibility = View.GONE
            AutoStatusAdapter.sendStatus(AutoStatus.NOT_SPEEDING)
            animator.pause()
            binding.greenSpeedingView.visibility = if (viewModel.naviGreenWaveCarSpeedVisible.value == true) View.VISIBLE else View.GONE
        }
        isStartAnimator = false
    }

    private fun outsideUnInit() {
        viewModel.abortRouteRequestTaskId()
        if (!viewModel.isPlanRouteing()) {
            viewModel.outsideUnInit()
        }
    }

    //监听网络变化
    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        Timber.i("onNetWorkChangeListener isNetworkConnected:$isNetworkConnected")
        if (isNetworkConnected && viewModel.loginStatus != null && viewModel.loginStatus.value == BaseConstant.LOGIN_STATE_SUCCESS) {
            viewModel.getTeamUserStatus("loginStatus") //获取组队状态
        }
    }

    //显示巡航景点推荐
    private fun showScenicSectorView(data: ScenicSectorBean?) {
        try {
            if (data != null && navController.currentDestination?.id == R.id.homeFragment) {
                binding.scenicSectoreContainer.removeAllViews()
                binding.scenicSectoreContainer.visibility = View.VISIBLE
                binding.scenicSectoreContainer.addView(mScenicSectorView)
                mScenicSectorView?.show(data)
                mScenicSectorView?.setOnScenicSectorListener(object :
                    ScenicSectorView.ScenicSectorListener {
                    override fun close(type: Int) { //关闭 0.关闭按钮 1.图片点击 2.到时间关闭
                        if (type == 0) {
                            showScenicSectorDialog() //巡航景点推荐开关提示框
                        } else if (type == 1) {
                            scenicSectorViewGone() //关闭景点推荐弹框
                            if (data.data != null) {
                                navController.navigate(R.id.to_ahaScenicDetailFragment, Bundle().apply {
                                    putInt("id", data.data!!.id)
                                })
                            } else {
                                Timber.i("showScenicSectorView data.data == null")
                            }
                        } else {
                            scenicSectorViewGone() //关闭景点推荐弹框
                        }
                    }

                    override fun goto() {
                        scenicSectorViewGone() //关闭景点推荐弹框
                    }
                })
            }
        } catch (e: Exception) {
            Timber.i("showScenicSectorView Exception:${e.message}")
        }
    }

    //关闭景点推荐弹框
    private fun scenicSectorViewGone() {
        try {
            binding.scenicSectoreContainer.removeAllViews()
            binding.scenicSectoreContainer.visibility = View.GONE
        } catch (e: Exception) {
            Timber.i("scenicSectorViewGone Exception:${e.message}")
        }
    }

    //巡航景点推荐开关提示框
    private fun showScenicSectorDialog() {
        lifecycleScope.launch {
            try {
                dismissScenicSectorDialog()
                scenicSectorDialog = CustomDialogFragment.builder().setTitle("").setContent("确认后将不再为您提供景点推荐服务")
                    .doubleButton(
                        getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
                        getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
                    )
                    .setOnClickListener {
                        if (it) {
                            viewModel.setAhaScenicBroadcastSwitch(false)
                        }
                        scenicSectorViewGone() //关闭景点推荐弹框
                    }.apply {
                        show(supportFragmentManager, "showScenicSectorDialog")
                    }
            } catch (e: Exception) {
                Timber.i("showScenicSectorDialog Exception:${e.message}")
            }
        }
    }

    private fun dismissScenicSectorDialog() {
        scenicSectorDialog?.run {
            if (isAdded || isVisible)
                dismissAllowingStateLoss()
        }
        scenicSectorDialog = null
    }

    private fun showInAppFloatingWindow() {
        Timber.i("showInAppFloatingWindow floatingView:$floatingView")
        val floatingViewDataBinding = FloatingCallWindowBinding.inflate(LayoutInflater.from(this))

        if (floatingView == null) {
            floatingViewDataBinding.lifecycleOwner = this
            floatingView = floatingViewDataBinding.root
            floatingViewDataBinding.viewModel = viewModel
        }
        if (floatingView != null) {
            floatingViewDataBinding.ivCall.setDebouncedOnClickListener {
                (window.decorView as FrameLayout).removeView(floatingView)
                if (navController.currentDestination?.id != R.id.myTeamFragment && navController.currentDestination?.id != R.id.inviteJoinTeamFragment) {
                    navController.navigate(
                        R.id.to_myTeamFragment,
                        Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_MAIN_TYPE) })
                }
                viewModel.tvJoinCall()
            }

            // 获取父容器和视图尺寸
            val parentView = window.decorView
            val parentWidth = parentView.width
            val parentHeight = parentView.height
            val viewWidth = resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_300)
            val viewHeight = resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_236)

            floatingViewDataBinding.root.setOnTouchListener(object : View.OnTouchListener {
                private var initialX = 0
                private var initialY = 0
                private var initialTouchX = 0f
                private var initialTouchY = 0f

                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event?.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val layoutParams = floatingViewDataBinding.root.layoutParams as FrameLayout.LayoutParams
                            initialX = layoutParams.leftMargin
                            initialY = layoutParams.topMargin
                            initialTouchX = event.rawX
                            initialTouchY = event.rawY
                            return true
                        }

                        MotionEvent.ACTION_UP -> return true
                        MotionEvent.ACTION_MOVE -> {
                            val layoutParams = floatingViewDataBinding.root.layoutParams as FrameLayout.LayoutParams
                            val newX = initialX + (event.rawX - initialTouchX).toInt()
                            val newY = initialY + (event.rawY - initialTouchY).toInt()

                            // 计算边界限制
                            val maxLeft = parentWidth - viewWidth
                            val maxTop = parentHeight - viewHeight - resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_130)

                            // 应用边界限制
                            val boundedX = newX.coerceIn(0, maxLeft)
                            val boundedY = newY.coerceIn(resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_90), maxTop)

//                            Timber.d("floatingView onTouch ACTION_MOVE parentWidth:$parentWidth parentHeight:$parentHeight viewWidth:$viewWidth viewHeight:$viewHeight newX:$newX newY:$newY boundedX:$boundedX boundedY:$boundedY maxLeft:$maxLeft maxTop:$maxTop")

                            layoutParams.leftMargin = boundedX
                            layoutParams.topMargin = boundedY
                            floatingViewDataBinding.root.layoutParams = layoutParams
                            floatingViewDataBinding.root.requestLayout()
                            return true
                        }
                    }
                    return false
                }
            })

            val params = FrameLayout.LayoutParams(
                resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_300),
                resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_236),
                Gravity.TOP or Gravity.START
            )
            params.marginStart = 390
            params.topMargin = 1010


            // 检查 floatingView 是否有父布局，如果有则移除
            val parent = floatingView?.parent
            if (parent is ViewGroup) {
                parent.removeView(floatingView)
            }
            (window.decorView as FrameLayout).removeView(floatingView)

            (window.decorView as FrameLayout).addView(floatingView, params)
        }
    }
}