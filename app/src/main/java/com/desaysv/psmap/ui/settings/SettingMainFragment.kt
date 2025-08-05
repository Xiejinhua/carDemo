package com.desaysv.psmap.ui.settings

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
import com.autosdk.common.AutoStatus
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.databinding.FragmentSettingMainBinding
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.ui.settings.settingconfig.MapSettingFragment
import com.desaysv.psmap.ui.settings.settingconfig.NaviBroadcastFragment
import com.desaysv.psmap.ui.settings.settingconfig.NaviSettingFragment
import com.desaysv.psmap.ui.settings.settingconfig.OtherSettingFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 设置主界面
 */
@AndroidEntryPoint
class SettingMainFragment : Fragment(), NetWorkManager.NetWorkChangeListener {
    private lateinit var binding: FragmentSettingMainBinding
    private val viewModel: SettingMainViewModel by viewModels()

    private var naviSettingFragment: NaviSettingFragment? = null
    private var naviBroadcastFragment: NaviBroadcastFragment? = null
    private var mapSettingFragment: MapSettingFragment? = null
    private var otherSettingFragment: OtherSettingFragment? = null

    private var settingTab = 0
    private var lastCheckId = 0
    private var lastCheckedId: Int? = null // 记录上一个选中的 RadioButton ID
    private var lastTargetX = 0
    private var isAnimationRunning = false

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var navigationSettingBusiness: NavigationSettingBusiness

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingMainBinding.inflate(inflater, container, false)
        if (naviSettingFragment == null) {
            naviSettingFragment = NaviSettingFragment()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
        AutoStatusAdapter.sendStatus(AutoStatus.SETTING_FRAGMENT_START)
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        KeyboardUtil.hideKeyboard(view)
        netWorkManager.addNetWorkChangeListener(this)

        lifecycleScope.launch {
            settingTab = arguments?.getInt(BaseConstant.SETTING_TAB, SettingTab.NAVI) ?: settingTab
            Timber.e("onCreate() 1 settingTab：$settingTab")
            //进入导航设置
            if (settingTab == SettingTab.NAVI) {
                openChildFragmentByTab(settingTab)
                gotoNaviSetFragment() //切换到设置Fragment
            }
        }
    }

    private fun initEventOperation() {
        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            if (!isAdded) return@setOnCheckedChangeListener

            // 如果动画正在运行且目标 Tab 不同，则取消当前动画
            if (isAnimationRunning && lastCheckedId != checkedId) {
                binding.indicator.animate().cancel()
                isAnimationRunning = false
            }
            isAnimationRunning = true

            childFragmentManager.executePendingTransactions()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            hideFragment(transaction)
            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("setting layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 判断动画持续时间
                val duration = if (lastCheckedId != null && areAdjacent(lastCheckedId!!, checkedId)) {
                    200 // 相邻的 RadioButton
                } else {
                    300 // 非相邻的 RadioButton
                }
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_navi){
                    binding.indicator.animate()
                        .x(lastTargetX.toFloat())
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                } else {
                    binding.indicator.animate()
                        .x(targetX.toFloat())
                        .setDuration(duration.toLong())
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                lastTargetX = targetX
                                hideFragment(transaction)
                                setTabAndFragment(checkedId, transaction)
                                isAnimationRunning = false // 动画结束标志
                            }
                        })
                        .start()
                }
            }
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }
    }

    private fun setTabAndFragment(checkedId: Int, transaction: FragmentTransaction){
        try {
            lastCheckedId = checkedId // 更新上一个选中的 ID
            when (checkedId) {
                R.id.rb_navi -> {
                    viewModel.tabSelect.postValue(0)
                    lastCheckId = checkedId
                    settingTab = SettingTab.NAVI
                    arguments?.putInt(BaseConstant.SETTING_TAB, SettingTab.NAVI)
                    if (!naviSettingFragment!!.isAdded) {
                        transaction.add(R.id.fragment_container, naviSettingFragment!!)
                    } else {
                        transaction.show(naviSettingFragment!!)
                    }
                    navigationSettingBusiness.isMapSetting.postValue(false)
                }

                R.id.rb_broadcast -> {
                    viewModel.tabSelect.postValue(1)
                    lastCheckId = checkedId
                    settingTab = SettingTab.BROADCAST
                    arguments?.putInt(BaseConstant.SETTING_TAB, SettingTab.BROADCAST)
                    if (naviBroadcastFragment == null) {
                        naviBroadcastFragment = NaviBroadcastFragment()
                        transaction.add(R.id.fragment_container, naviBroadcastFragment!!)
                    } else {
                        transaction.show(naviBroadcastFragment!!)
                    }
                    navigationSettingBusiness.isMapSetting.postValue(false)
                }

                R.id.rb_map -> {
                    viewModel.tabSelect.postValue(2)
                    lastCheckId = checkedId
                    settingTab = SettingTab.MAP_SETTING
                    arguments?.putInt(BaseConstant.SETTING_TAB, SettingTab.MAP_SETTING)
                    if (mapSettingFragment == null) {
                        mapSettingFragment = MapSettingFragment()
                        transaction.add(R.id.fragment_container, mapSettingFragment!!)
                    } else {
                        transaction.show(mapSettingFragment!!)
                    }
                    navigationSettingBusiness.isMapSetting.postValue(true)
                }

                R.id.rb_other -> {
                    viewModel.tabSelect.postValue(3)
                    lastCheckId = checkedId
                    settingTab = SettingTab.OTHER_SETTING
                    arguments?.putInt(BaseConstant.SETTING_TAB, SettingTab.OTHER_SETTING)
                    if (otherSettingFragment == null) {
                        otherSettingFragment = OtherSettingFragment()
                        transaction.add(R.id.fragment_container, otherSettingFragment!!)
                    } else {
                        transaction.show(otherSettingFragment!!)
                    }
                    navigationSettingBusiness.isMapSetting.postValue(false)
                }
            }
            transaction.commitAllowingStateLoss()
        } catch (e: Exception){
            Timber.i("setTabAndFragment Exception:${e.message}")
        }
    }

    // 判断两个 RadioButton 是否相邻
    private fun areAdjacent(lastId: Int, currentId: Int): Boolean {
        return when {
            (lastId == R.id.rb_navi && currentId == R.id.rb_broadcast) ||
                    (lastId == R.id.rb_broadcast && currentId == R.id.rb_navi) -> true
            (lastId == R.id.rb_broadcast && currentId == R.id.rb_map) ||
                    (lastId == R.id.rb_map && currentId == R.id.rb_broadcast) -> true
            (lastId == R.id.rb_map && currentId == R.id.rb_other) ||
                    (lastId == R.id.rb_other && currentId == R.id.rb_map) -> true
            else -> false
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
        netWorkManager.removeNetWorkChangeListener(this)
        naviSettingFragment = null
        naviBroadcastFragment = null
        mapSettingFragment = null
        otherSettingFragment = null
        AutoStatusAdapter.sendStatus(AutoStatus.SETTING_FRAGMENT_EXIT)
    }

    //监听网络变化
    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        //todo
    }

    private fun openChildFragmentByTab(@SettingTab tab: Int) {
        Timber.i("openChildFragmentByTab tab:$tab")
        when (tab) {
            SettingTab.NAVI -> binding.layoutTab.check(R.id.rb_navi)
            SettingTab.BROADCAST -> binding.layoutTab.check(R.id.rb_broadcast)
            SettingTab.MAP_SETTING -> binding.layoutTab.check(R.id.rb_map)
            SettingTab.OTHER_SETTING -> binding.layoutTab.check(R.id.rb_other)
            else -> Timber.d(" openChildFragment By other:$tab")
        }
    }

    //获取当前选择的tab
    private fun getNowSettingTab(): Int {
        var settingTab = SettingTab.NAVI
        Timber.i("getNowSettingTab 1 lastCheckId:$lastCheckId")
        when (lastCheckId) {
            R.id.rb_navi -> settingTab = SettingTab.NAVI
            R.id.rb_broadcast -> settingTab = SettingTab.BROADCAST
            R.id.rb_map -> settingTab = SettingTab.MAP_SETTING
            R.id.rb_other -> settingTab = SettingTab.OTHER_SETTING
            else -> {
                Timber.d(" getNowSettingTab else")
            }
        }
        Timber.i("getNowSettingTab lastCheckId:$lastCheckId settingTab:$settingTab")
        return settingTab
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        //进入导航设置
        if (settingTab == SettingTab.NAVI) {
            binding.layoutTab.check(R.id.rb_navi)
            gotoNaviSetFragment() //切换到设置Fragment
        } else {
            judeOpenChildFragmentByTab() //判断是否跳转到对应的tab
        }
    }

    //判断是否跳转到对应的tab
    private fun judeOpenChildFragmentByTab() {
        if (settingTab != getNowSettingTab()) {
            openChildFragmentByTab(settingTab)
        }
    }

    //切换到设置Fragment
    private fun gotoNaviSetFragment() {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        hideFragment(transaction)
        if (!naviSettingFragment!!.isAdded) {
            transaction.add(R.id.fragment_container, naviSettingFragment!!)
        } else {
            transaction.show(naviSettingFragment!!)
        }
        transaction.commitAllowingStateLoss()
        navigationSettingBusiness.isMapSetting.postValue(false)
    }
}