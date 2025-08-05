package com.desaysv.psmap.ui.settings.settingconfig

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.LayoutNaviPreviewBinding
import com.desaysv.psmap.databinding.LayoutSettingSwitchBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 导航设置
 */
@AndroidEntryPoint
class NaviSettingFragment : Fragment(), NetWorkManager.NetWorkChangeListener {
    private val viewModel: NaviSettingViewModel by viewModels()
    private var isFirstOpen = true
    private var isFirstOpenGetCar = true
    private var callback: OnBackPressedCallback? = null
    private lateinit var bindingSwitch: LayoutSettingSwitchBinding
    private lateinit var bindingNaviPreview: LayoutNaviPreviewBinding

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var netWorkManager: NetWorkManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context))
        bindingNaviPreview = LayoutNaviPreviewBinding.inflate(LayoutInflater.from(context))
        return ComposeView(requireContext()).apply {
            setContent {
                val themeChange by skyBoxBusiness.themeChange().unPeek().observeAsState(
                    NightModeGlobal.isNightMode()
                )
                DsDefaultTheme(themeChange) {
                    NaviSettingScreen(
                        Modifier,
                        bindingSwitch,
                        bindingNaviPreview,
                        viewModel,
                        noNetUseLimit = {
                            bindingSwitch.switchChange.contentDescription = if (it) "避开限行关#关闭避开限行#避开限行关闭#避开限行" else "避开限行开#开启避开限行#避开限行开启#避开限行"
                        },
                        startSettingCar = {
                            startSettingCar(BaseConstant.ACTION_VEHICLE_INFO) //跳转车牌设置页（跳转到车辆设置我的车页面）
                        })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFirstOpenGetCar) {
            isFirstOpenGetCar = false
        } else {
            viewModel.getVehicleNumberLimit() //获取车牌号和限行数据
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback = null
    }

    override fun onDestroy() {
        super.onDestroy()
        netWorkManager.removeNetWorkChangeListener(this)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.getVehicleNumberLimit() //获取车牌号和限行数据
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    private fun initBinding() {
        lifecycleScope.launch {
            viewModel.setNavigationSettingData()//设置导航设置数据
        }
        netWorkManager.addNetWorkChangeListener(this)
    }

    private fun initEventOperation() {
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
            skyBoxBusiness.updateView(bindingNaviPreview.root, true)
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        viewModel.limitChecked.unPeek().observe(viewLifecycleOwner) {
            bindingSwitch.switchChange.contentDescription = if (it) "避开限行关#关闭避开限行#避开限行关闭#避开限行" else "避开限行开#开启避开限行#避开限行开启#避开限行"
        }

        //通知系统车牌号变化
        viewModel.licensePlateChange.unPeek().observe(viewLifecycleOwner){
            viewModel.getVehicleNumberLimit() //获取车牌号数据
        }
    }

    //监听网络变化
    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        Timber.i("onNetWorkChangeListener isNetworkConnected:$isNetworkConnected")
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
}