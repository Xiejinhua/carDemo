package com.desaysv.psmap.ui.settings.mobileinternet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMobileInternetBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 手车互联
 */
@AndroidEntryPoint
class MobileInternetFragment : Fragment(), NetWorkManager.NetWorkChangeListener {
    private lateinit var binding: FragmentMobileInternetBinding
    private val viewModel: MobileInternetViewModel by viewModels()
    private var customDialogFragment: CustomDialogFragment? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var layerController: LayerController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMobileInternetBinding.inflate(inflater, container, false)
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
        KeyboardUtil.hideKeyboard(view)
        netWorkManager.addNetWorkChangeListener(this)
    }

    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)
        //刷新
        binding.qrRefresh.setDebouncedOnClickListener {
            viewModel.getQRImage()//获取二维码
        }
        ViewClickEffectUtils.addClickScale(binding.qrRefresh, CLICKED_SCALE_93)

        //点击连接手机
        binding.connectPhone.setDebouncedOnClickListener {
            viewModel.showLoginPage.postValue(true)
            viewModel.getQRImage()//获取二维码
        }
        ViewClickEffectUtils.addClickScale(binding.connectPhone, CLICKED_SCALE_95)

        //退出账号
        binding.signOut.setDebouncedOnClickListener {
            toLogout() //退出登录
        }
        ViewClickEffectUtils.addClickScale(binding.signOut, CLICKED_SCALE_93)

        //未连接-重试
        binding.connectRetry.setDebouncedOnClickListener {
            val isNetworkConnected = netWorkManager.isNetworkConnected()
            viewModel.isNetworkConnected.postValue(isNetworkConnected)
            if (isNetworkConnected) {//有网
                if (viewModel.loginState.value == true) {//已经登录
                    viewModel.showLoginPage.postValue(false)
                } else {
                    viewModel.showLoginPage.postValue(true)
                    viewModel.getQRImage()//获取二维码
                }
            }
        }
        ViewClickEffectUtils.addClickScale(binding.connectRetry, CLICKED_SCALE_95)

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            skyBoxBusiness.updateView(binding.root, true)
            if (viewModel != null) {
                viewModel.isNight.postValue(it)
                viewModel.defaultAvatar.value =
                    if (it) ContextCompat.getDrawable(requireContext(), com.desaysv.psmap.base.R.drawable.ic_default_avatar_night)
                    else ContextCompat.getDrawable(requireContext(), com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)
            }
        }

        viewModel.showLoginLayout.unPeek().observe(viewLifecycleOwner) {
            Timber.i("showLoginLayout: $it")
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { integer: Int ->
            if (integer == BaseConstant.LOGIN_STATE_GUEST) {
                if (viewModel.getAccountProfile()?.uid.isNullOrEmpty()) {
                    Timber.d("已退出账号")
                    viewModel.isNetworkConnected.postValue(netWorkManager.isNetworkConnected())
                    viewModel.showLoginPage.postValue(false)
                }
                try {
                    layerController.getAGroupLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).clearAllItems()
                    layerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).clearAllItems()
                } catch (e: Exception) {
                    Timber.d(" getLoginLoading Exception:%s", e.message)
                }
                dismissCustomDialog()
            } else if (integer == BaseConstant.LOGIN_STATE_SUCCESS) {
                viewModel.isNetworkConnected.postValue(netWorkManager.isNetworkConnected())
                viewModel.showLoginPage.postValue(false)
                viewModel.getTeamUserStatus() //获取组队状态
                viewModel.requestFootSummary() //获取足迹信息
            }
        }
    }

    //退出登录弹窗
    private fun toLogout() {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle("确认退出登录?").setContent("退出后，将无法同步搜索记录")
            .setOnClickListener {
                if (it) {
                    viewModel.signOut()
                }
            }.apply {
                show(this@MobileInternetFragment.childFragmentManager, "customDialog")
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

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        netWorkManager.removeNetWorkChangeListener(this)
    }

    //监听网络变化
    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        viewModel.isNetworkConnected.postValue(isNetworkConnected)
        val accountProfile = viewModel.getAccountProfile()
        if (accountProfile == null || accountProfile.uid.isNullOrEmpty()) {
            Timber.i("onNetWorkChangeListener 未登录")
        } else if (isNetworkConnected) {
            viewModel.updateAvatar()//方便网络变化重新加载头像
            viewModel.requestFootSummary() //获取足迹信息
        }
    }
}