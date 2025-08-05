package com.desaysv.psmap.ui.navi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.widget.navi.utils.NaviUiUtil
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapLightBarItem
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.EnlargeInfo
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentNaviBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.NaviLaneUtil
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.navi.compose.NaviLaneComposeListView
import com.desaysv.psmap.ui.navi.compose.SapaComposeListView
import com.desaysv.psmap.ui.navi.view.TmcBarView
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
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
class NaviFragment : Fragment(), NetWorkManager.NetWorkChangeListener {
    private lateinit var binding: FragmentNaviBinding
    private val viewModel by viewModels<NaviViewModel>()

    @Inject
    lateinit var mNetWorkManager: NetWorkManager

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var gson: Gson

    private var customDialogFragment: CustomDialogFragment? = null

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    //Fragment数据共享方式一：通过同一个MainViewModel做数据共享
//    private val mainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }
    private var miRouteTotalLength: Long = 0
    private var mCursorPos = 0f
    private var mTmcViewLength = 0
    private var mTmcHorViewLength = 0
    private var restDistance: Long = 0
    private var commandRequestRouteNaviBean: CommandRequestRouteNaviBean? = null
    private final val MOD_MAPPER_READY_VALUE: Int = 2
    private final val MOD_MAPPER_MAP_TRAINING_VALUE: Int = 3
    private final val MOD_MAPPER_FINISH_VALUE: Int = 5
    private var isFirstOpen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("NaviFragment onCreate()")
        viewModel.init()
        commandRequestRouteNaviBean = requireArguments().getParcelable(Biz.KEY_BIZ_ROUTE_START_END_VIA_POI_LIST)
        commandRequestRouteNaviBean?.let {
            toStartPlanRoute(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNaviBinding.inflate(inflater, container, false).apply {
            sapaComposeView.setContent {
                sapaComposeView()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
        viewModel.naviBackCurrentCarPosition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView()")
        mNetWorkManager.removeNetWorkChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause()")
        viewModel.setEagleVisible(false)
        viewModel.hideCrossView()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            delay(500)
            viewModel.setEagleVisible(true)
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissDialog()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.fragmentRouteSapaDetails.composeView.setContent {
            sapaDetailsComposeView()
        }
        binding.naviInfoView.laneComposeView.setContent {
            composeView()
        }
        binding.naviInfoView.showCrossLaneComposeView.setContent {
            composeView()
        }
        //路口大图位置
//        val x = viewModel.screenStatus.value?.let {
//            if (it) AutoConstant.enlargeViewX + 552f else AutoConstant.enlargeViewX
//        } ?: AutoConstant.enlargeViewX
        EnlargeInfo.getInstance().setEnlargeCrossImageSize(AutoConstant.enlargeViewX, AutoConstant.enlargeViewY)
        if (iCarInfoProxy.isT1JFL2ICE()) {
            binding.btAlongSearch.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_fuel_night else R.drawable.selector_ic_fuel_day)
            binding.btAlongSearch.setBackground(R.drawable.selector_ic_fuel_day, R.drawable.selector_ic_fuel_night)
        } else {
            binding.btAlongSearch.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_charge_station_night else R.drawable.selector_ic_charge_station_day)
            binding.btAlongSearch.setBackground(R.drawable.selector_ic_charge_station_day, R.drawable.selector_ic_charge_station_night)
        }
    }

    private fun initEventOperation() {
        binding.naviInfoView.seekBar.isEnabled = false; // 禁用 SeekBar
        mNetWorkManager.addNetWorkChangeListener(this)
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            it?.let { nonNullString ->
                if (nonNullString.isNotEmpty()) {
                    toast.showToast(nonNullString)
                }
            }
        }
        binding.slResumeNavi.setDebouncedOnClickListener {
            viewModel.naviBackCurrentCarPosition()
        }
        ViewClickEffectUtils.addClickScale(binding.slResumeNavi, CLICKED_SCALE_95)

        binding.slExitNavi.setDebouncedOnClickListener {
            Timber.i("slExitNavi is OnClickListener")
            if (viewModel.isModMapperMapTraining()) {
                launchSmartDriveDialog()
            } else {
                viewModel.stopNavi()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.slExitNavi, CLICKED_SCALE_90)

        binding.btnOverview.setDebouncedOnClickListener {
            if (viewModel.inFullView.value == true) {
                viewModel.exitPreview()
            } else {
                viewModel.showPreview()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.btnOverview, CLICKED_SCALE_90)

        binding.btnDropBy.setDebouncedOnClickListener {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.OnthewaySearch_Click,
                mapOf(Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis()))
            )
            findNavController().navigate(R.id.to_searchAlongWayFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.btnDropBy, CLICKED_SCALE_90)

        binding.eagleView.setDebouncedOnClickListener {
            if (viewModel.inFullView.value == true) {
                viewModel.exitPreview()
            } else {
                viewModel.showPreview()
            }
        }

        binding.tmcBarProgress.tmcBarBady.setDebouncedOnClickListener {
            if (viewModel.inFullView.value == true) {
                viewModel.exitPreview()
            } else {
                viewModel.showPreview()
            }
        }
        binding.sendEndToPhoneView.close.setDebouncedOnClickListener {
            viewModel.hideSendToPhone()
        }
        ViewClickEffectUtils.addClickScale(binding.sendEndToPhoneView.close, CLICKED_SCALE_90)
        binding.sendEndToPhoneView.btSend.setDebouncedOnClickListener {
            viewModel.sendToPhone()
        }
        ViewClickEffectUtils.addClickScale(binding.sendEndToPhoneView.btSend, CLICKED_SCALE_93)

        binding.btnSound.setDebouncedOnClickListener {
            viewModel.setPlayTTsMute(!binding.btnSound.isSelected)
        }
        ViewClickEffectUtils.addClickScale(binding.btnSound, CLICKED_SCALE_90)

        viewModel.finishFragment.observe(viewLifecycleOwner) { isFinish ->
            Timber.i("finishObserver is called isFinish = $isFinish; isFirstOpen = $isFirstOpen")
            if (isFirstOpen) {
                isFirstOpen = false
            } else if (isFinish == true) {
                val navigateUp = findNavController().navigateUp()
                Timber.i("finishFragment navigateUp $navigateUp")
            }
        }

        viewModel.tmcModelInfo.observe(viewLifecycleOwner) { tmcInfo ->
            updateTmc(tmcInfo.lightBarItems, tmcInfo.tmcTotalDistance, tmcInfo.tmcRestDistance)
        }
        binding.tmcBarProgress.tmcBarView.setFirstDrawListener(mFirstDrawListener)

        //沿途搜
        binding.btAlongSearch.setDebouncedOnClickListener {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.OnerefuelClick, System.currentTimeMillis()))
            )
            viewModel.hideCrossView()
            val commandBean = CommandRequestSearchBean.Builder().setKeyword("充电站").setType(CommandRequestSearchBean.Type.SEARCH_CHARGE).build()
            findNavController().navigate(R.id.to_searchResultFragment, commandBean.toBundle())
        }
        ViewClickEffectUtils.addClickScale(binding.btAlongSearch, CLICKED_SCALE_90)

        //设置
        binding.btSetting.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_navigationFunctionMenuFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.btSetting, CLICKED_SCALE_90)

        viewModel.backCCPVisible.observe(viewLifecycleOwner) {
            if (!it) {
                viewModel.setShowMore(false)
            }
        }
        binding.fragmentRouteSapaDetails.startViaEndView.setDebouncedOnClickListener {
            Timber.i("fragmentRouteSapaDetails startViaEndView is OnClickListener")
            viewModel.setServiceAreaInfoVisible(true)
        }

        viewModel.showViaNaviViaDataDialog.unPeek().observe(viewLifecycleOwner) {
            NavHostFragment.findNavController(this@NaviFragment).navigate(R.id.action_naviFragment_to_naviDeleteViaDialogFragment)
        }

        //更改路线偏好后重新算路
        viewModel.onReRouteFromPlanPref.unPeek().observe(viewLifecycleOwner) {
            viewModel.onReRouteFromPlanPref(it)
        }

        //手车互联变更目的地或者途经点
        viewModel.onChangeDestination.unPeek().observe(viewLifecycleOwner) {
            viewModel.onChangeDestination(it)
        }

//        binding.layoutLowBatteryCard.btnFindChargeStation.setDebouncedOnClickListener {
//            val commandBean =
//                CommandRequestSearchCategoryBean.Builder().setKeyword("充电站").setType(CommandRequestSearchCategoryBean.Type.SEARCH_ALONG_WAY)
//                    .build()
//            findNavController().navigate(R.id.to_searchAlongWayResultFragment, commandBean.toBundle())
//        }
//
//        binding.layoutLowBatteryCard.btnCancel.setDebouncedOnClickListener {
//            viewModel.closeBatteryLowTipsCard()
//        }
        binding.btnParallelRoadSwitch.setDebouncedOnClickListener {
            viewModel.onParallelWayClick(false)
        }
        ViewClickEffectUtils.addClickScale(binding.btnParallelRoadSwitch, CLICKED_SCALE_93)
        binding.btnParallelRoadSwitchMainSide.setDebouncedOnClickListener {
            viewModel.onParallelWayClick(true)
        }
        ViewClickEffectUtils.addClickScale(binding.btnParallelRoadSwitchMainSide, CLICKED_SCALE_93)
        binding.fragmentRouteSapaDetails.ivBack.setDebouncedOnClickListener {
            viewModel.setServiceAreaInfoVisible(false)
        }
        ViewClickEffectUtils.addClickScale(binding.fragmentRouteSapaDetails.ivBack, CLICKED_SCALE_90)

        //道路限速监听
        viewModel.mCurrentRoadSpeed.observe(viewLifecycleOwner) { roadSpeed ->
            if (roadSpeed > 0) {
                Timber.i("currentRoadSpeed observe roadSpeed:$roadSpeed  carSpeed: ${viewModel.getCurSpeed()} ")
            }
        }

//        viewModel.screenStatus.observe(viewLifecycleOwner) {
//            if (viewModel.inFullView.value == true) {
//                viewModel.showPreview()
//            }
//            //路口大图位置
//            val x = if (it) AutoConstant.enlargeViewX + 552f else AutoConstant.enlargeViewX
//            EnlargeInfo.getInstance().setEnlargeCrossImageSize(x, AutoConstant.enlargeViewY)
//            viewModel.hideCrossView()
//        }

        viewModel.mapType.observe(viewLifecycleOwner) {
            viewModel.setEagleVisible(true)
        }

        viewModel.parkingRecommend.unPeek().observe(viewLifecycleOwner) {
            it?.let { parkingList ->
                Timber.i("startSearchEndRecommend parkingRecommend = ${gson.toJson(parkingList)}")
                viewModel.chooseParkingRecommend(0)
            }
        }
        viewModel.naviParkingPosition.unPeek().observe(viewLifecycleOwner) {
            Timber.i("startSearchEndRecommend naviParkingPosition = $it")
            viewModel.chooseParkingRecommend(it)
        }
        binding.viewParkingRecommend.clNaviParkingSimple.setDebouncedOnClickListener {
            viewModel.showParkingCardDetail()
        }
        ViewClickEffectUtils.addClickScale(binding.viewParkingRecommend.clNaviParkingSimple, CLICKED_SCALE_95)
        binding.viewParkingRecommend.btyNaviParkingFirst.setDebouncedOnClickListener {
            viewModel.setParkingPosition(0)
        }
        ViewClickEffectUtils.addClickScale(binding.viewParkingRecommend.btyNaviParkingFirst, CLICKED_SCALE_93)
        binding.viewParkingRecommend.btyNaviParkingSecond.setDebouncedOnClickListener {
            viewModel.setParkingPosition(1)
        }
        ViewClickEffectUtils.addClickScale(binding.viewParkingRecommend.btyNaviParkingSecond, CLICKED_SCALE_93)
        binding.viewParkingRecommend.btyNaviParkingThree.setDebouncedOnClickListener {
            viewModel.setParkingPosition(2)
        }
        ViewClickEffectUtils.addClickScale(binding.viewParkingRecommend.btyNaviParkingThree, CLICKED_SCALE_93)
        binding.viewParkingRecommend.btStartNavi.setDebouncedOnClickListener {
            viewModel.retryPlanRouteParking()
        }
        ViewClickEffectUtils.addClickScale(binding.viewParkingRecommend.btStartNavi, CLICKED_SCALE_95)

        viewModel.themeChange.observe(viewLifecycleOwner) {
            //日夜模式监听
            viewModel.updateTurnIconTheme()
            viewModel.updateNextThumTurnTheme()
            binding.naviInfoView.tmcBarHorizontalView.setNightMode(it)
            binding.tmcBarProgress.tmcBarView.setNightMode(it)
        }
    }


    /**
     * 开始算路啦
     */
    private fun toStartPlanRoute(commandBean: CommandRequestRouteNaviBean) {
        Timber.i("toStartPlanRoute commandBean = ${gson.toJson(commandBean)}")
        if (commandBean.type == BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD || commandBean.type == BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD_MISPOI) {
            //NEED_REQUEST_RX_PLAN_ROAD发起路线规划，  NEED_REQUEST_RX_PLAN_ROAD_MISPOI 导航中添加途经点
            viewModel.startPlanRoute(commandBean.start, commandBean.end, commandBean.midPois, commandBean.type)
        } else if (commandBean.type == BaseConstant.Type.NEED_RX_PLAN_HAVE_SUCCESS) {
            //路径规划已经成功
            viewModel.setRouteAndNavi(commandBean.type, false)
        } else if (commandBean.type == BaseConstant.Type.NEED_FILE_DATA_HAVE_SUCCESS) {
            //主页跳转-继续上一次导航
            viewModel.setRouteAndNavi(commandBean.type, true)
        } else if (commandBean.type == BaseConstant.Type.NEED_PHONE_SEND_ROUTE_DATA) {
            //手车互联发起导航
            viewModel.startPlanRouteAimRoutePushMsg(commandBean.aimRoutePushMsg, commandBean.type)
        }
    }


    //================================更新光柱图 交通状态图  start============================================================
    private fun updateTmc(items: List<MapLightBarItem>?, totalDistance: Long, restDistance: Long) {
        Timber.i("updateTmc is called totalDistance = $totalDistance __restDistance = $restDistance")
        if (items.isNullOrEmpty()) {
            return
        }
        updateRouteTotalLength(totalDistance)
        createTmcBar(items, restDistance)
        updateTmcInfo(items, restDistance)
    }

    private fun updateTmcInfo(mLightBarItem: List<MapLightBarItem>?, restDistance: Long) {
        if (miRouteTotalLength == 0L || mLightBarItem == null) {
            return
        }
        val max: Long = miRouteTotalLength
        val progress: Long = restDistance
        val curPos: Float = mTmcHorViewLength * (max - progress) / (max * 1f)
        //光柱图状态
        binding.naviInfoView.tmcBarHorizontalView.setData(mLightBarItem, max)
        binding.naviInfoView.tmcBarHorizontalView.setCursorPos(curPos)
        //光柱图图标位置
        binding.naviInfoView.seekBar.max = max.toInt()
        binding.naviInfoView.seekBar.progress = (max - progress).toInt()
    }

    private fun updateRouteTotalLength(totalLength: Long) {
        miRouteTotalLength = totalLength
        if (mTmcViewLength == 0) {
            mTmcViewLength = if (binding.tmcBarProgress.tmcBarView.measuredHeight != 0) {
                binding.tmcBarProgress.tmcBarView.measuredHeight
            } else {
                CommonUtils.getAutoDimenValue(activity, com.desaysv.psmap.base.R.dimen.sv_dimen_580)
            }
        }
        if (mTmcHorViewLength == 0) {
            mTmcHorViewLength = CommonUtils.getAutoDimenValue(activity, com.desaysv.psmap.base.R.dimen.sv_dimen_640)
        }
    }

    private fun createTmcBar(mLightBarItem: List<MapLightBarItem>?, restDistance: Long) {
        if (miRouteTotalLength == 0L || mTmcViewLength == 0 || mLightBarItem == null) {
            return
        }
        this.restDistance = restDistance
        mCursorPos = (restDistance * 1.0 / miRouteTotalLength * mTmcViewLength).toFloat()
        /**
         * 往上一些，避免灰色
         */
        NaviUiUtil.setTranslationY(binding.tmcBarProgress.naviTmcCursor, mCursorPos)
        binding.tmcBarProgress.naviTmcCursor.invalidate()

//        isValid = true;
        binding.tmcBarProgress.tmcBarView.setData(mLightBarItem, miRouteTotalLength)
        binding.tmcBarProgress.tmcBarView.setCursorPos(mCursorPos)
        binding.tmcBarProgress.tmcBarView.invalidate()
    }

    private val mFirstDrawListener: TmcBarView.TmcBarViewFirstDraw = TmcBarView.TmcBarViewFirstDraw { width, height ->
        if (height > 0) {
            mTmcViewLength = height
            mCursorPos = (restDistance * 1.0 / miRouteTotalLength * mTmcViewLength).toFloat()
            /**
             * 往上一些，避免灰色
             */
            NaviUiUtil.setTranslationY(binding.tmcBarProgress.naviTmcCursor, mCursorPos)
            binding.tmcBarProgress.naviTmcCursor.invalidate()
        }
    }
    //================================更新光柱图 交通状态图  end============================================================

    /**
     * 车道线列表
     */
    @Composable
    private fun composeView() {
        val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())
        val naviLaneList by viewModel.naviLaneList.observeAsState(emptyList())
        val showCrossView by viewModel.showCrossView.observeAsState(false)
        NaviLaneUtil.initLaneResId()
        NaviLaneUtil.initCrossLaneResId()
        DsDefaultTheme(themeChange) {
            Timber.i("NaviLaneComposeView themeChange = $themeChange")
            NaviLaneComposeListView(tbtLaneInfoList = naviLaneList, showCrossView)
        }
    }

    /**
     * 服务区列表
     */
    @Composable
    private fun sapaComposeView() {
        val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())
        val sapaInfoList by viewModel.sapaInfoList.observeAsState(emptyList())
        DsDefaultTheme(themeChange) {
            Timber.i("sapaComposeView themeChange = $themeChange")
            sapaInfoList?.let {
                val list = it.take(2)
                SapaComposeListView(sapaList = list, 2, onItemClick = { type ->
                    viewModel.obtainSAPAInfo()
                })
            }
        }
    }

    /**
     * 高速服务区详情列表
     */
    @Composable
    private fun sapaDetailsComposeView() {
        val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())
        val sapaInfoDetailsList by viewModel.sapaInfoDetailsList.observeAsState(emptyList())
        DsDefaultTheme(themeChange) {
            Timber.i("sapaDetailsComposeView themeChange = $themeChange")
            SapaComposeListView(sapaList = sapaInfoDetailsList, 3)
        }
    }

    /**
     * 网络监听
     */
    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        Timber.i(" onNetWorkChangeListener is called isNetworkConnected = $isNetworkConnected}")
        if (isNetworkConnected) {
            viewModel.networkRefreshRoute()
        } else {
            viewModel.setSwitchOffline()
        }
    }

    //显示退出导航智驾弹框
    private fun launchSmartDriveDialog() {
        Timber.i("launchSmartDriveDialog")
        dismissDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle("确认结束导航？").setContent("结束导航将终止路线学习")
            .singleButton("")
            .doubleButton(
                requireContext().getString(com.autosdk.R.string.message_text_join),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            )
            .setOnClickListener {
                if (it) {
                    viewModel.stopNavi()
                }
            }.apply {
                show(this@NaviFragment.childFragmentManager, "launchSmartDriveDialog")
            }
    }

    private fun dismissDialog() {
        customDialogFragment?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        customDialogFragment = null
    }
}