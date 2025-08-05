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
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.widget.navi.utils.NaviUiUtil
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.base.bean.MapLightBarItem
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.EnlargeInfo
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentSimNaviBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.NaviLaneUtil
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.navi.compose.NaviLaneComposeListView
import com.desaysv.psmap.ui.navi.compose.SapaComposeListView
import com.desaysv.psmap.ui.navi.view.TmcBarView
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */
@AndroidEntryPoint
class SimNaviFragment : Fragment() {
    private lateinit var binding: FragmentSimNaviBinding
    private val viewModel by viewModels<SimNaviViewModel>()

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var mNaviController: NaviController

    private var isFirstOpen = true

    //Fragment数据共享方式一：通过同一个MainViewModel做数据共享
//    private val mainViewModel by lazy { ViewModelProvider(requireActivity())[MainViewModel::class.java] }
    private var miRouteTotalLength: Long = 0
    private var mCursorPos = 0f
    private var mTmcViewLength = 0
    private var mTmcHorViewLength = 0
    private var restDistance: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("SimNaviFragment onCreate()")
        val commandRequestRouteNaviBean = requireArguments().getParcelable<CommandRequestRouteNaviBean>(Biz.KEY_BIZ_ROUTE_START_END_VIA_POI_LIST)
        commandRequestRouteNaviBean?.let {
            toStartPlanRoute(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSimNaviBinding.inflate(inflater, container, false).apply {
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
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
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

    }

    private fun initEventOperation() {
        binding.naviInfoView.seekBar.isEnabled = false; // 禁用 SeekBar
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }
        binding.slResumeNavi.setDebouncedOnClickListener {

            if (mNaviController.isNaving) { //是否正在导航
                viewModel.pauseNavi()
                viewModel.setSimNavigationStatus(false)
            } else {
                viewModel.resumeNavi()
                viewModel.setSimNavigationStatus(true)
                viewModel.naviBackCurrentCarPosition()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.slResumeNavi, CLICKED_SCALE_90)

        binding.slExitNavi.setDebouncedOnClickListener {
            Timber.i("finishObserver slExitNavi")
            viewModel.stopNavi()
        }
        ViewClickEffectUtils.addClickScale(binding.slExitNavi, CLICKED_SCALE_90)

        binding.eagleView.setDebouncedOnClickListener {
            if (viewModel.inFullView.value == true) {
                viewModel.exitPreview()
            } else {
                viewModel.showPreview()
            }
        }

        binding.btnOverview.setDebouncedOnClickListener {
            if (viewModel.inFullView.value == true) {
                viewModel.exitPreview()
            } else {
                viewModel.showPreview()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.btnOverview, CLICKED_SCALE_90)

        binding.sendEndToPhoneView.close.setDebouncedOnClickListener {
            viewModel.hideSendToPhone()
        }
        ViewClickEffectUtils.addClickScale(binding.sendEndToPhoneView.close, CLICKED_SCALE_90)

        binding.sendEndToPhoneView.btSend.setDebouncedOnClickListener {
            viewModel.sendToPhone()
        }
        ViewClickEffectUtils.addClickScale(binding.sendEndToPhoneView.btSend, CLICKED_SCALE_93)

        binding.btnSpeed.setDebouncedOnClickListener {
            when (viewModel.simNaviSpeedType.value) {
                0 -> viewModel.setSimSpeed(BaseConstant.SIM_NAVI_SPEED_MEDIUM)

                1 -> viewModel.setSimSpeed(BaseConstant.SIM_NAVI_SPEED_HIGH)

                2 -> viewModel.setSimSpeed(BaseConstant.SIM_NAVI_SPEED_LOW)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.btnSpeed, CLICKED_SCALE_90)

        viewModel.finishFragment.observe(viewLifecycleOwner) { isFinish ->
            Timber.i("finishObserver is called isFinish = $isFinish; isFirstOpen = $isFirstOpen")
            if (isFirstOpen) {
                isFirstOpen = false
            } else if (isFinish == true) {
                findNavController().navigateUp()
            }
        }

        viewModel.tmcModelInfo.observe(viewLifecycleOwner)
        { tmcInfo ->
            updateTmc(tmcInfo.lightBarItems, tmcInfo.tmcTotalDistance, tmcInfo.tmcRestDistance)
        }
        binding.tmcBarProgress.tmcBarView.setFirstDrawListener(mFirstDrawListener)

        viewModel.inFullView.observe(viewLifecycleOwner)
        {
            binding.btnOverview.isSelected = it
        }

        viewModel.backCCPVisible.observe(viewLifecycleOwner)
        {
            if (!it) {
                viewModel.setShowMore(false)
            }
        }
        //道路限速监听
        viewModel.mCurrentRoadSpeed.observe(viewLifecycleOwner)
        { roadSpeed ->
            if (roadSpeed > 0) {
                Timber.i("currentRoadSpeed observe roadSpeed:$roadSpeed  carSpeed: ${viewModel.getCurSpeed()} ")
            }
        }
//        viewModel.screenStatus.observe(viewLifecycleOwner)
//        {
//            if (viewModel.inFullView.value == true) {
//                viewModel.showPreview()
//            }
//            //路口大图位置
//            val x = if (it) AutoConstant.enlargeViewX + 552f else AutoConstant.enlargeViewX
//            EnlargeInfo.getInstance().setEnlargeCrossImageSize(x, AutoConstant.enlargeViewY)
//            viewModel.hideCrossView()
//        }
        viewModel.themeChange.observe(viewLifecycleOwner)
        {
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
                CommonUtils.getAutoDimenValue(activity, com.desaysv.psmap.base.R.dimen.sv_dimen_400)
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
                SapaComposeListView(sapaList = list, 1)
            }
        }
    }
}