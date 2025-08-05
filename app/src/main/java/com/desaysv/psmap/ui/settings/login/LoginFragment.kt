package com.desaysv.psmap.ui.settings.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.FragmentTransaction
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentLoginBinding
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.settings.AccountAndSettingTab
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 登录
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    private var loginQrFragment: LoginQrFragment? = null
    private var loginMobileFragment: LoginMobileFragment? = null
    private var pageTab = 0
    private var lastCheckId = 0
    private var lastTargetX = 0

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
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

        pageTab = arguments?.getInt(BaseConstant.ACCOUNT_SETTING_TAB, AccountAndSettingTab.QR_LOGIN) ?: pageTab
        Timber.e("onCreate() 1 pageTab：$pageTab")
        if (pageTab == AccountAndSettingTab.QR_LOGIN) {
            openChildFragmentByTab(pageTab)
            gotoLoginQrFragment() //切换到LoginQrFragment
        }
    }

    private fun initEventOperation() {
        //点击返回按钮，退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            if (!isAdded) return@setOnCheckedChangeListener
            childFragmentManager.executePendingTransactions()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            hideFragment(transaction)
            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_qr){
                    binding.indicator.animate()
                        .x(lastTargetX.toFloat())
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                } else {
                    binding.indicator.animate()
                        .x(targetX.toFloat())
                        .setDuration(200)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                lastTargetX = targetX
                                when (checkedId) {
                                    R.id.rb_qr -> {
                                        viewModel.tabSelect.postValue(0)
                                        lastCheckId = checkedId
                                        pageTab = AccountAndSettingTab.QR_LOGIN
                                        arguments?.putInt(BaseConstant.ACCOUNT_SETTING_TAB, AccountAndSettingTab.QR_LOGIN)
                                        if (loginQrFragment == null) {
                                            loginQrFragment = LoginQrFragment()
                                            transaction.add(R.id.fragment_container, loginQrFragment!!)
                                        } else {
                                            transaction.show(loginQrFragment!!)
                                        }
                                    }

                                    R.id.rb_mobile -> {
                                        viewModel.tabSelect.postValue(1)
                                        lastCheckId = checkedId
                                        pageTab = AccountAndSettingTab.MOBILE_LOGIN
                                        arguments?.putInt(BaseConstant.ACCOUNT_SETTING_TAB, AccountAndSettingTab.MOBILE_LOGIN)
                                        if (loginMobileFragment == null) {
                                            loginMobileFragment = LoginMobileFragment()
                                            transaction.add(R.id.fragment_container, loginMobileFragment!!)
                                        } else {
                                            transaction.show(loginMobileFragment!!)
                                        }
                                    }
                                }
                                transaction.commitAllowingStateLoss()
                            }
                        })
                        .start()
                }
            }
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) { toast ->
            toastUtil.showToast(toast)
        }

        viewModel.codeSuccess.unPeek().observe(viewLifecycleOwner) { codeSuccess ->
            if (codeSuccess) {
                findNavController().navigateUp()
            }
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            skyBoxBusiness.updateView(binding.root, true)
        }

        childFragmentManager.addOnBackStackChangedListener { // 回退栈发生变化时的处理逻辑
            val currentFragment = childFragmentManager.findFragmentById(R.id.loginFragment)
            if (currentFragment is LoginFragment) {
                pageTab = arguments?.getInt(BaseConstant.ACCOUNT_SETTING_TAB, AccountAndSettingTab.QR_LOGIN) ?: pageTab
                Timber.d("addOnBackStackChangedListener pageTab:$pageTab")
                judeOpenChildFragmentByTab() //判断是否跳转到对应的tab
            }
        }

        settingAccountBusiness.jetourAccountState.unPeek().observe(viewLifecycleOwner) {
            if (!it) {
                findNavController().navigateUp()
            }
        }
    }

    /**
     * 动态添加子Fragment
     */
    private fun hideFragment(transaction: FragmentTransaction) {
        val fragments = childFragmentManager.fragments
        Timber.i(" --------->: hideFragment:%s", fragments.size)
        for (fragment1 in fragments) {
            transaction.hide(fragment1)
        }
    }

    override fun onResume() {
        super.onResume()
        judeOpenChildFragmentByTab() //判断是否跳转到对应的tab
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy")
        loginQrFragment = null
        loginMobileFragment = null
    }

    private fun openChildFragmentByTab(@AccountAndSettingTab tab: Int) {
        Timber.i("openChildFragmentByTab tab:$tab")
        when (tab) {
            AccountAndSettingTab.QR_LOGIN -> binding.layoutTab.check(R.id.rb_qr)
            AccountAndSettingTab.MOBILE_LOGIN -> binding.layoutTab.check(R.id.rb_mobile)
            else -> Timber.d(" openChildFragment By other:$tab")
        }
    }

    //获取当前选择的tab
    private fun getNowTab(): Int {
        var tab = AccountAndSettingTab.QR_LOGIN
        Timber.i("getNowTab 1 lastCheckId:$lastCheckId")
        when (lastCheckId) {
            R.id.rb_qr -> tab = AccountAndSettingTab.QR_LOGIN
            R.id.rb_mobile -> tab = AccountAndSettingTab.MOBILE_LOGIN
            else -> {
                Timber.d(" getNowTab else")
            }
        }
        Timber.i("getNowTab lastCheckId:$lastCheckId tab:$tab")
        return tab
    }

    //判断是否跳转到对应的tab
    private fun judeOpenChildFragmentByTab() {
        if (pageTab != getNowTab()) {
            openChildFragmentByTab(pageTab)
        }
    }

    //切换到LoginQrFragment
    private fun gotoLoginQrFragment() {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        hideFragment(transaction)
        if (loginQrFragment == null) {
            loginQrFragment = LoginQrFragment()
            transaction.add(R.id.fragment_container, loginQrFragment!!)
        } else {
            transaction.show(loginQrFragment!!)
        }
        transaction.commitAllowingStateLoss()
    }
}