package com.desaysv.psmap.ui.settings.settingconfig

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentNavigationFunctionMenuBinding
import com.desaysv.psmap.databinding.LayoutNaviPreviewBinding
import com.desaysv.psmap.databinding.LayoutSettingSwitchBinding
import com.desaysv.psmap.databinding.LayoutTtsModeBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.settings.view.PromptLanguagePopWindow
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 导航功能菜单
 */
@AndroidEntryPoint
class NavigationFunctionMenuFragment : Fragment(), NetWorkManager.NetWorkChangeListener {
    private lateinit var binding: FragmentNavigationFunctionMenuBinding
    private val viewModel: NavigationFunctionMenuViewModel by viewModels()
    private var isFirstOpen = true
    private var isFirstOpenGetCar = true
    private var callback: OnBackPressedCallback? = null
    private lateinit var bindingSwitch: LayoutSettingSwitchBinding
    private lateinit var scaleSwitch: LayoutSettingSwitchBinding
    private lateinit var bindingTtsMode: LayoutTtsModeBinding
    private lateinit var bindingNaviPreview: LayoutNaviPreviewBinding
    private var countDownTimes = 10000L
    private var engineerNum = 0 //工程模式按钮点击次数统计

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var promptLanguagePopWindow: PromptLanguagePopWindow

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNavigationFunctionMenuBinding.inflate(inflater, container, false).apply {
            bindingSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context))
            scaleSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context))
            bindingTtsMode = LayoutTtsModeBinding.inflate(LayoutInflater.from(context))
            bindingNaviPreview = LayoutNaviPreviewBinding.inflate(LayoutInflater.from(context))
            composeView.setContent {
                val themeChange by skyBoxBusiness.themeChange().unPeek().observeAsState(
                    NightModeGlobal.isNightMode()
                )
                DsDefaultTheme(themeChange) {
                    NavigationFunctionMenuScreen(
                        Modifier,
                        bindingSwitch,
                        scaleSwitch,
                        bindingTtsMode,
                        bindingNaviPreview,
                        viewModel,
                        noNetUseLimit = {
                            bindingSwitch.switchChange.contentDescription = if (it) "避开限行关#关闭避开限行#避开限行关闭#避开限行" else "避开限行开#开启避开限行#避开限行开启#避开限行"
                        },
                        startSettingCar = {
                            startSettingCar(BaseConstant.ACTION_VEHICLE_INFO) //跳转车牌设置页（跳转到车辆设置我的车页面）
                        }, onDropBy = { //顺路搜操作
                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.OnthewaySearch_Click,
                                mapOf(Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis()))
                            )
                            NavHostFragment.findNavController(this@NavigationFunctionMenuFragment).navigate(R.id.action_navigationFunctionMenuFragment_to_searchAlongWayFragment)
                        }, onRouteRefresh = { //路线刷新
                            viewModel.restartPlanRoute() //重新开始路径规划
                        }, onRouteShare = { //行程分享
                            EventTrackingUtils.trackEvent(
                                EventTrackingUtils.EventName.Nav_Start,
                                mapOf(Pair(EventTrackingUtils.EventValueName.TravelShareClick, System.currentTimeMillis()))
                            )
                            findNavController().navigate(R.id.to_tripShareDialogFragment)
                        })
                }
            }
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (isFirstOpenGetCar) {
            isFirstOpenGetCar = false
        } else {
            viewModel.getVehicleNumberLimit() //获取车牌号和限行数据
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        promptLanguagePopWindow.cancelPromptLanguagePop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback = null
    }

    override fun onDestroy() {
        super.onDestroy()
        netWorkManager.removeNetWorkChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    private fun initBinding() {
        lifecycleScope.launch {
            delay(50)
            viewModel.setNavigationSettingData()//设置导航设置数据
        }
        netWorkManager.addNetWorkChangeListener(this)
        countDownTimes = 10000L
        countDownTimer.cancel()
        countDownTimer.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }

        //工程模式按钮点击
        binding.engineerTv.setDebouncedOnClickListener(50L) {
            engineerBtnClick()
        }

        //工程模式按钮长按
        binding.engineerTv.setOnLongClickListener {
            gotoEngineer()
            false
        }

        //保存路线偏好设置
        viewModel.saveStrategy.unPeek().observe(viewLifecycleOwner) {
            viewModel.checkAndSavePrefer()
            viewModel.onReRouteFromPlanPref()
        }

        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { integer ->
            if (integer == BaseConstant.LOGIN_STATE_GUEST || integer == BaseConstant.LOGIN_STATE_SUCCESS) {
                if (isFirstOpen) {
                    isFirstOpen = false
                } else {
                    viewModel.initStrategy()
                    viewModel.setNavigationSettingData()
                }
            }
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            viewModel.isNight.postValue(it)
            view?.run { skyBoxBusiness.updateView(this, true) }
            skyBoxBusiness.updateView(bindingSwitch.root, true)
            skyBoxBusiness.updateView(scaleSwitch.root, true)
            skyBoxBusiness.updateView(bindingTtsMode.root, true)
            skyBoxBusiness.updateView(bindingNaviPreview.root, true)
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        viewModel.setNaviToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        binding.touch.setOnTouchListener { _, _ ->
            try {
                countDownTimes = 10000L
                countDownTimer.cancel()
                countDownTimer.start()
            } catch (e: Exception) {
                Timber.i("setOnTouchListener e:${e.message}")
            }
            false // 返回false表示不拦截事件，允许事件继续传递
        }

        //判断是否显示提示文言
        viewModel.showMoreInfo.observe(viewLifecycleOwner) {
            if (!TextUtils.isEmpty(it.content)) {
                promptLanguagePopWindow.showPromptLanguagePop(it)
            } else {
                promptLanguagePopWindow.cancelPromptLanguagePop()
            }
        }

        viewModel.limitChecked.unPeek().observe(viewLifecycleOwner) {
            bindingSwitch.switchChange.contentDescription = if (it) "避开限行关#关闭避开限行#避开限行关闭#避开限行" else "避开限行开#开启避开限行#避开限行开启#避开限行"
        }

        viewModel.scaleChecked.unPeek().observe(viewLifecycleOwner) {
            scaleSwitch.switchChange.contentDescription = if (it) "自动比例尺#自动比例尺关#关闭自动比例尺#自动比例尺关闭" else "自动比例尺#自动比例尺开#开启自动比例尺#自动比例尺开启"
        }

        //通知系统车牌号变化
        viewModel.licensePlateChange.unPeek().observe(viewLifecycleOwner){
            viewModel.getVehicleNumberLimit() //获取车牌号数据
        }
    }

    //监听网络变化
    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        viewModel.isNetworkConnected.postValue(isNetworkConnected)
    }

    /**
     * 跳转车牌设置页（跳转到车辆设置我的车页面）
     */
    private fun startSettingCar(settingAction: String) {
        try {
            if (CommonUtils.isVehicle()) {
                bindingSwitch.switchChange.contentDescription = "避开限行开#开启避开限行#避开限行开启#避开限行"
                CommonUtils.startSettingCar(settingAction, requireContext())
            } else {
                findNavController().navigate(R.id.to_vehicleFragment)
            }
        } catch (e: Exception) {
            Timber.d("startSettingCar: Exception e: ${e.message}")
        }
    }

    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(countDownTimes, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                //不作处理
            }

            override fun onFinish() { // 倒计时完成后执行的操作
                if (isAdded) {
                    try {
                        findNavController().navigateUp()
                    } catch (e: Exception) {
                        Timber.i("countDownTimer onFinish e:${e.message}")
                    }
                }
            }
        }
    }

    //进入工程模式按钮点击计数
    private fun engineerBtnClick() {
        if (engineerNum != 5) {
            engineerNum++
        }
    }

    //进入工程模式
    private fun gotoEngineer() {
        if (engineerNum == 5) {
            engineerNum = 0
            findNavController().navigate(R.id.to_engineerFragment)
        }
    }
}