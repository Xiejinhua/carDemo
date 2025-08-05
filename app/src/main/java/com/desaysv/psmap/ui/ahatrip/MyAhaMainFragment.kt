package com.desaysv.psmap.ui.ahatrip

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
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
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.FragmentMyAhaMainBinding
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 我的路书&收藏主页
 */
@AndroidEntryPoint
class MyAhaMainFragment : Fragment() {
    private lateinit var binding: FragmentMyAhaMainBinding
    private val viewModel: MyAhaMainViewModel by viewModels()

    private var commandRequestSearchBean: CommandRequestSearchBean? = null
    private var myAhaTripFragment: MyAhaTripFragment? = null
    private var myAhaFavoriteFragment: MyAhaFavoriteFragment? = null
    private var lastCheckId = 0
    private var lastTargetX = 0
    private var isFirst = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyAhaMainBinding.inflate(inflater, container, false)
        commandRequestSearchBean = requireArguments().getParcelable(Biz.KEY_BIZ_SEARCH_REQUEST)
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
        if (commandRequestSearchBean?.type == CommandRequestSearchBean.Type.SEARCH_CUSTOM_AHA_TRIP) {
            if (isFirst) {
                isFirst = false
            }else{
                return
            }
            when (commandRequestSearchBean?.keyword) {
                "行程列表" -> {
                    openChildFragmentByTab(0)
                    gotoMyAhaTripFragment()
                }

                "收藏" -> {
                    binding.indicator.animate()
                        .x(264f)
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                    openChildFragmentByTab(1)
                    gotoMyAhaFavoriteFragment()
                }
                else -> {
                    Timber.e("initBinding() 1 commandRequestSearchBean?.keyword：${commandRequestSearchBean?.keyword}")
                }
            }
        } else {
            val pageTab = viewModel.tabSelect.value ?: 0
            Timber.e("onCreate() 1 pageTab：$pageTab")

            if (pageTab == 0) {
                openChildFragmentByTab(pageTab)
                gotoMyAhaTripFragment() //切换到MyAhaTripFragment
            }
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
                if (targetX == 0 && checkedId != R.id.rb_my_trip) {
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
                                    R.id.rb_my_trip -> {
                                        viewModel.tabSelect.postValue(0)
                                        lastCheckId = checkedId
                                        if (myAhaTripFragment == null) {
                                            myAhaTripFragment = MyAhaTripFragment()
                                            transaction.add(R.id.fragment_container, myAhaTripFragment!!)
                                        } else {
                                            transaction.show(myAhaTripFragment!!)
                                        }
                                    }

                                    R.id.rb_trip_favorite -> {
                                        viewModel.tabSelect.postValue(1)
                                        lastCheckId = checkedId
                                        if (myAhaFavoriteFragment == null) {
                                            myAhaFavoriteFragment = MyAhaFavoriteFragment()
                                            transaction.add(R.id.fragment_container, myAhaFavoriteFragment!!)
                                        } else {
                                            transaction.show(myAhaFavoriteFragment!!)
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

        childFragmentManager.addOnBackStackChangedListener { // 回退栈发生变化时的处理逻辑
            val currentFragment = childFragmentManager.findFragmentById(R.id.myAhaMainFragment)
            if (currentFragment is MyAhaMainFragment) {
                Timber.d("addOnBackStackChangedListener")
                judeOpenChildFragmentByTab() //判断是否跳转到对应的tab
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
        if (!viewModel.isLogin()) {
            findNavController().navigateUp()
            return
        }
        judeOpenChildFragmentByTab() //判断是否跳转到对应的tab
    }

    override fun onDestroy() {
        super.onDestroy()
        myAhaTripFragment = null
        myAhaFavoriteFragment = null
    }

    private fun openChildFragmentByTab(tab: Int) {
        Timber.i("openChildFragmentByTab tab:$tab")
        when (tab) {
            0 -> binding.layoutTab.check(R.id.rb_my_trip)
            1 -> binding.layoutTab.check(R.id.rb_trip_favorite)
            else -> Timber.d(" openChildFragment By other:$tab")
        }
    }

    //获取当前选择的tab
    private fun getNowTab(): Int {
        var tab = 0
        Timber.i("getNowTab 1 lastCheckId:$lastCheckId")
        when (lastCheckId) {
            R.id.rb_my_trip -> tab = 0
            R.id.rb_trip_favorite -> tab = 1
            else -> {
                Timber.d(" getNowTab else")
            }
        }
        Timber.i("getNowTab lastCheckId:$lastCheckId tab:$tab")
        return tab
    }

    //判断是否跳转到对应的tab
    private fun judeOpenChildFragmentByTab() {
        val pageTab = viewModel.tabSelect.value ?: 0
        if (pageTab != getNowTab()) {
            openChildFragmentByTab(pageTab)
        }
    }

    //切换到MyAhaTripFragment
    private fun gotoMyAhaTripFragment() {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        hideFragment(transaction)
        if (myAhaTripFragment == null) {
            myAhaTripFragment = MyAhaTripFragment()
            transaction.add(R.id.fragment_container, myAhaTripFragment!!)
        } else {
            transaction.show(myAhaTripFragment!!)
        }
        transaction.commitAllowingStateLoss()
    }

    //切换到MyAhaTripFragment
    private fun gotoMyAhaFavoriteFragment() {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        hideFragment(transaction)
        if (myAhaFavoriteFragment == null) {
            myAhaFavoriteFragment = MyAhaFavoriteFragment()
            transaction.add(R.id.fragment_container, myAhaFavoriteFragment!!)
        } else {
            transaction.show(myAhaFavoriteFragment!!)
        }
        transaction.commitAllowingStateLoss()
    }
}