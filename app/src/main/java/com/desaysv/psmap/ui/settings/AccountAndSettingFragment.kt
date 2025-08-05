package com.desaysv.psmap.ui.settings

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentAccountAndSettingBinding
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.settings.view.PromptLanguagePopWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 个人中心&设置主界面
 */
@AndroidEntryPoint
class AccountAndSettingFragment : Fragment() {
    private val viewModel: AccountAndSettingViewModel by viewModels()
    private lateinit var binding: FragmentAccountAndSettingBinding

    private var engineerNum = 0 //工程模式按钮点击次数统计
    private var pageTab = 0

    private var accountCenterFragment: AccountCenterFragment? = null
    private var settingsFragment: SettingMainFragment? = null

    private var lastCheckId = 0
    private var lastTargetX = 0

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var navigationSettingBusiness: NavigationSettingBusiness

    @Inject
    lateinit var promptLanguagePopWindow: PromptLanguagePopWindow

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountAndSettingBinding.inflate(inflater, container, false)
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
        pageTab = arguments?.getInt(BaseConstant.ACCOUNT_SETTING_TAB, AccountAndSettingTab.ACCOUNT) ?: pageTab
        Timber.e("onCreate() 1 pageTab：$pageTab")
        openChildFragmentByTab(pageTab)
        if (pageTab == AccountAndSettingTab.ACCOUNT) {
            gotoAccountCenterFragment() //切换到AccountCenterFragment
        } else {
            gotoSettingMainFragment() //切换到SettingMainFragment
        }
        viewModel.setShowMoreInfo(MoreInfoBean())
    }

    private fun initEventOperation() {
        //点击返回按钮，退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        //工程模式按钮点击
        binding.engineerTv.setDebouncedOnClickListener(50L) {
            engineerBtnClick()
        }

        //工程模式按钮长按
        binding.engineerTv.setOnLongClickListener {
            gotoEngineer()
            false
        }

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
                if (targetX == 0 && checkedId != R.id.rb_account) {
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
                                setTabAndFragment(checkedId, transaction)
                            }
                        })
                        .start()
                }
            }
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }

        childFragmentManager.addOnBackStackChangedListener { // 回退栈发生变化时的处理逻辑
            val currentFragment = childFragmentManager.findFragmentById(R.id.accountAndSettingFragment)
            if (currentFragment is AccountAndSettingFragment) {
                pageTab = arguments?.getInt(BaseConstant.ACCOUNT_SETTING_TAB, AccountAndSettingTab.ACCOUNT) ?: pageTab
                Timber.d("addOnBackStackChangedListener pageTab:$pageTab")
                judeOpenChildFragmentByTab() //判断是否跳转到对应的tab
            }
        }

        //判断是否显示提示文言
        viewModel.showMoreInfo.observe(viewLifecycleOwner) {
            if (!TextUtils.isEmpty(it.content)) {
                promptLanguagePopWindow.showPromptLanguagePop(it)
            } else {
                promptLanguagePopWindow.cancelPromptLanguagePop()
            }
        }

        viewModel.mapCommand.unPeek().observe(viewLifecycleOwner) { mapCommand ->
            Timber.i("mapCommand ${mapCommand.mapCommandType}")
            if (mapCommand.mapCommandType == MapCommandType.CloseSettingPage) {
                findNavController().navigateUp()
                viewModel.notifyMapCommandResult(mapCommand.mapCommandType, "好的，已为您关闭导航设置页")
            }
        }
    }

    private fun setTabAndFragment(checkedId: Int, transaction: FragmentTransaction) {
        try {
            when (checkedId) {
                R.id.rb_account -> {
                    viewModel.tabSelect.postValue(0)
                    lastCheckId = checkedId
                    pageTab = AccountAndSettingTab.ACCOUNT
                    arguments?.putInt(BaseConstant.ACCOUNT_SETTING_TAB, AccountAndSettingTab.ACCOUNT)
                    if (accountCenterFragment == null) {
                        accountCenterFragment = AccountCenterFragment()
                        transaction.add(R.id.fragment_container, accountCenterFragment!!)
                    } else {
                        transaction.show(accountCenterFragment!!)
                    }
                    navigationSettingBusiness.isMapSetting.postValue(false)
                }

                R.id.rb_set -> {
                    viewModel.tabSelect.postValue(1)
                    lastCheckId = checkedId
                    pageTab = AccountAndSettingTab.SETTING
                    arguments?.putInt(BaseConstant.ACCOUNT_SETTING_TAB, AccountAndSettingTab.SETTING)
                    if (settingsFragment == null) {
                        settingsFragment = SettingMainFragment()
                        transaction.add(R.id.fragment_container, settingsFragment!!)
                    } else {
                        transaction.show(settingsFragment!!)
                    }
                    navigationSettingBusiness.isMapSetting.postValue(false)
                }
            }
            transaction.commitAllowingStateLoss()
        } catch (e: Exception) {
            Timber.i("setTabAndFragment Exception:${e.message}")
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

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        promptLanguagePopWindow.cancelPromptLanguagePop()
    }

    override fun onResume() {
        super.onResume()
        judeOpenChildFragmentByTab() //判断是否跳转到对应的tab
    }

    override fun onDestroy() {
        super.onDestroy()
        engineerNum = 0
        accountCenterFragment = null
        settingsFragment = null
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

    private fun openChildFragmentByTab(@AccountAndSettingTab tab: Int) {
        Timber.i("openChildFragmentByTab tab:$tab")
        when (tab) {
            AccountAndSettingTab.ACCOUNT -> binding.layoutTab.check(R.id.rb_account)
            AccountAndSettingTab.SETTING -> binding.layoutTab.check(R.id.rb_set)
            else -> Timber.d(" openChildFragment By other:$tab")
        }
    }

    //获取当前选择的tab
    private fun getNowAccountAndSettingTab(): Int {
        var tab = AccountAndSettingTab.ACCOUNT
        Timber.i("getNowAccountAndSettingTab 1 lastCheckId:$lastCheckId")
        when (lastCheckId) {
            R.id.rb_set -> tab = AccountAndSettingTab.SETTING
            R.id.rb_account -> tab = AccountAndSettingTab.ACCOUNT
            else -> {
                Timber.d(" getNowAccountAndSettingTab else")
            }
        }
        Timber.i("getNowAccountAndSettingTab lastCheckId:$lastCheckId tab:$tab")
        return tab
    }

    //判断是否跳转到对应的tab
    private fun judeOpenChildFragmentByTab() {
        if (pageTab != getNowAccountAndSettingTab()) {
            openChildFragmentByTab(pageTab)
        }
    }

    //切换到AccountCenterFragment
    private fun gotoAccountCenterFragment() {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        hideFragment(transaction)
        if (accountCenterFragment == null) {
            accountCenterFragment = AccountCenterFragment()
            transaction.add(R.id.fragment_container, accountCenterFragment!!)
        } else {
            transaction.show(accountCenterFragment!!)
        }
        transaction.commitAllowingStateLoss()
        navigationSettingBusiness.isMapSetting.postValue(false)
        viewModel.tabSelect.postValue(0)
        lastCheckId = R.id.rb_account
    }

    //切换到SettingMainFragment
    private fun gotoSettingMainFragment() {
        binding.indicator.animate()
            .x(254f)
            .setDuration(0)
            .setInterpolator(FastOutSlowInInterpolator())
            .start()
        lastTargetX = 254
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        hideFragment(transaction)
        if (settingsFragment == null) {
            settingsFragment = SettingMainFragment()
            transaction.add(R.id.fragment_container, settingsFragment!!)
        } else {
            transaction.show(settingsFragment!!)
        }
        transaction.commitAllowingStateLoss()
        viewModel.tabSelect.postValue(1)
        lastCheckId = R.id.rb_set
        navigationSettingBusiness.isMapSetting.postValue(false)
    }
}