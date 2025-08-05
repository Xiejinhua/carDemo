package com.desaysv.psmap.ui.settings.mobileinternet

import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentWechatBinding
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.settings.view.UnBindPopWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 微信互联
 */
@AndroidEntryPoint
class WechatFragment : Fragment(), NetWorkManager.NetWorkChangeListener {
    private lateinit var binding: FragmentWechatBinding
    private val viewModel by viewModels<InternetWXViewModel>()

    private var isVisibility = true

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var unBindPopWindow: UnBindPopWindow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWechatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onResume() {
        super.onResume()
        isVisibility = true
    }

    override fun onPause() {
        super.onPause()
        isVisibility = false
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        unBindPopWindow.cancelUnBindPop()
    }

    override fun onDestroy() {
        super.onDestroy()
        netWorkManager.removeNetWorkChangeListener(this)
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        viewModel.initWXStatusData() //获取微信绑定状态
        //监听网络变化
        netWorkManager.addNetWorkChangeListener(this)
        viewModel.setShowMoreInfo(MoreInfoBean())
    }

    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        if (isVisibility && viewModel.showWeChatLayout.value == 1) {
            qrCodeRxTimer()//定时刷新二维码
        }

        val accountProfile = viewModel.getAccountProfile()
        if (accountProfile == null || accountProfile.uid.isNullOrEmpty()) {
            Timber.i("onNetWorkChangeListener 未登录")
        } else if (isNetworkConnected) {
            viewModel.updateAvatar()//方便网络变化重新加载头像
        }
    }

    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        //点击重试--重新获取二维码
        binding.qrRefresh.setDebouncedOnClickListener {
            viewModel.getRetryQrCode() //点击重试--重新获取二维码
        }
        ViewClickEffectUtils.addClickScale(binding.qrRefresh, CLICKED_SCALE_93)

        //解绑提示
        binding.howUnbindWechat.setDebouncedOnClickListener {
            viewModel.setShowMoreInfo(MoreInfoBean(requireContext().getString(R.string.sv_setting_wechat_unbind_tip), requireContext().getString(R.string.sv_setting_wechat_unbind_tip1)))
        }

        //判断是否显示提示文言
        viewModel.showMoreInfo.observe(viewLifecycleOwner) {
            if (!TextUtils.isEmpty(it.content)) {
                unBindPopWindow.showUnBindPop(it)
            } else {
                unBindPopWindow.cancelUnBindPop()
            }
        }

        //高德--定时刷新二维码
        viewModel.qrCodeRefresh.unPeek().observe(viewLifecycleOwner) {
            qrCodeRxTimer()//定时刷新二维码
        }

        //二维码刷新失败
        viewModel.showWeChatLayout.unPeek().observe(viewLifecycleOwner) { result: Int ->
            if (result == 4) {
                toastUtil.showToast(R.string.sv_setting_connect_wx_qr_code_load_fail)
            }
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            viewModel.defaultAvatar.value =
                if (it) ContextCompat.getDrawable(requireContext(), com.desaysv.psmap.base.R.drawable.ic_default_avatar_night)
                else ContextCompat.getDrawable(requireContext(), com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)
        }
    }

    //定时刷新二维码
    private fun qrCodeRxTimer() {
        lifecycleScope.launch {
            delay(5000)
            if (isVisibility) {
                viewModel.sendReqQRCodeConfirm()
            }
        }
    }
}