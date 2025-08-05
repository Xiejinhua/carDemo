package com.desaysv.psmap.ui.settings.settingconfig

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentOtherSettingBinding
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 其他设置界面
 */
@AndroidEntryPoint
class OtherSettingFragment : Fragment() {
    private lateinit var binding: FragmentOtherSettingBinding
    private val viewModel: OtherSettingViewModel by viewModels()
    private var isFirstOpen = true

    private var customDialogFragment: CustomDialogFragment? = null

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
        binding = FragmentOtherSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        lifecycleScope.launch {
            viewModel.setNavigationSettingData()//设置导航设置数据
            binding.intentionNavigationSwitch.setChecked(viewModel.intentionNavigation.value ?: false, false)
        }
    }

    private fun initEventOperation() {
        //意图导航开关
        binding.intentionNavigationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setIntentionNavigation(isChecked)
        }
        //意图导航提示
        binding.intentionNavigationInfo.setDebouncedOnClickListener {
            viewModel.setShowMoreInfo(MoreInfoBean(requireContext().getString(R.string.sv_setting_intention_nav), requireContext().getString(R.string.sv_setting_intention_nav_tip)))
        }

        //进入错误信息上报界面
        binding.report.setDebouncedOnClickListener {
            if (netWorkManager.isNetworkConnected()) {
                findNavController().navigate(R.id.to_errorReportFragment)
            } else {
                toastUtil.showToast(com.desaysv.psmap.model.R.string.sv_setting_network_not_connected_check_try_again)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.report, CLICKED_SCALE_93)

        //点击清除缓存
        binding.clearCacheBtn.setDebouncedOnClickListener {
            toClearCache()//清除缓存弹窗
        }
        ViewClickEffectUtils.addClickScale(binding.clearCacheBtn, CLICKED_SCALE_93)

        //恢复出厂设置按钮点击
        binding.resetSettingBtn.setDebouncedOnClickListener {
            toDefaultSettings() //恢复出厂设置弹框
        }
        ViewClickEffectUtils.addClickScale(binding.resetSettingBtn, CLICKED_SCALE_93)

        //点击进入关于界面
        binding.about.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_aboutFragment)
        }
        binding.aboutMore.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_aboutFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.aboutMore, CLICKED_SCALE_93)

        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { integer ->
            if (integer == BaseConstant.LOGIN_STATE_GUEST || integer == BaseConstant.LOGIN_STATE_SUCCESS) {
                if (isFirstOpen) {
                    isFirstOpen = false
                } else {
                    viewModel.setNavigationSettingData()
                }
            }
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        viewModel.intentionNavigation.unPeek().observe(viewLifecycleOwner) {
            binding.intentionNavigationSwitch.contentDescription = if (it) "意图导航开#意图导航开启#开启意图导航#意图导航" else "意图导航关#意图导航关闭#关闭意图导航#意图导航"
        }
    }

    //清除缓存弹窗
    private fun toClearCache() {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setContent(requireContext().getString(R.string.sv_setting_sure_clear_cache))
            .doubleButton(
                requireContext().getString(R.string.sv_setting_clear),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            ).setOnClickListener {
                if (it) {
                    viewModel.toClearCache(false)
                }
            }.apply {
                show(this@OtherSettingFragment.childFragmentManager, "clearCacheDialog")
            }
    }

    //恢复出厂设置弹框
    private fun toDefaultSettings() {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setContent(requireContext().getString(R.string.sv_setting_sure_restore_default_settings))
            .doubleButton(
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            ).setOnClickListener {
                if (it) {
                    viewModel.toClearCache(true)
                    viewModel.returnAllSetting()
                }
            }.apply {
                show(this@OtherSettingFragment.childFragmentManager, "toDefaultSettings")
            }
    }

    private fun dismissCustomDialog() {
        customDialogFragment?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        customDialogFragment = null
    }
}