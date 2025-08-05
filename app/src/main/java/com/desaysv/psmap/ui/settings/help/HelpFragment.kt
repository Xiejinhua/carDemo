package com.desaysv.psmap.ui.settings.help

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RadioButton
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.databinding.FragmentHelpBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.HelpAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 帮助界面
 */
@AndroidEntryPoint
class HelpFragment : Fragment() {
    private lateinit var binding: FragmentHelpBinding
    private val viewModel: HelpViewModel by viewModels()
    private var adapter: HelpAdapter? = null

    private var lastCheckedId: Int? = null // 记录上一个选中的 RadioButton ID
    private var lastTargetX = 0
    private var isAnimationRunning = false

    private var lastClickTime = 0L
    private val clickInterval = 100L // 最小点击间隔（毫秒）

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHelpBinding.inflate(inflater, container, false)
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
        adapter = HelpAdapter().also { binding.list.adapter = it }

        Timber.d(" initBinding selectTab:%s", viewModel.selectTab.value )
        if (viewModel.selectTab.value == BaseConstant.TYPE_HELP_POPULAR_QUESTIONS){
            binding.layoutTab.check(R.id.rb_popular_questions)
            viewModel.setSelectCheckedId(R.id.rb_popular_questions)
            setHelpData(BaseConstant.TYPE_HELP_POPULAR_QUESTIONS)
        }
    }

    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < clickInterval) return@setOnCheckedChangeListener
            lastClickTime = currentTime

            // 如果动画正在运行且目标 Tab 不同，则取消当前动画
            if (isAnimationRunning && lastCheckedId != checkedId) {
                binding.indicator.animate().setListener(null).cancel()
                isAnimationRunning = false
            }
            isAnimationRunning = true

            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 判断动画持续时间
                val duration = if (lastCheckedId != null && areAdjacent(lastCheckedId!!, checkedId)) {
                    200 // 相邻的 RadioButton
                } else {
                    300 // 非相邻的 RadioButton
                }
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_popular_questions){
                    binding.indicator.animate()
                        .x(lastTargetX.toFloat())
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                    setHelpData(viewModel.selectTab.value ?: BaseConstant.TYPE_HELP_POPULAR_QUESTIONS)
                } else {
                    binding.indicator.animate()
                        .x(targetX.toFloat())
                        .setDuration(duration.toLong())
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                lastTargetX = targetX
                                lastCheckedId = checkedId // 更新上一个选中的 ID
                                when (checkedId) {
                                    R.id.rb_popular_questions -> setHelpData(BaseConstant.TYPE_HELP_POPULAR_QUESTIONS)
                                    R.id.rb_drawing_display -> setHelpData(BaseConstant.TYPE_HELP_DRAWING_DISPLAY)
                                    R.id.rb_route_planning -> setHelpData(BaseConstant.TYPE_HELP_ROUTE_PLANNING)
                                    R.id.rb_search_function -> setHelpData(BaseConstant.TYPE_HELP_SEARCH_FUNCTION)
                                    R.id.rb_voice_broadcast -> setHelpData(BaseConstant.TYPE_HELP_VOICE_BROADCAST)
                                    R.id.rb_map_data -> setHelpData(BaseConstant.TYPE_HELP_MAP_DATA)
                                    else -> setHelpData(BaseConstant.TYPE_HELP_POPULAR_QUESTIONS)
                                }
                                isAnimationRunning = false // 动画结束标志
                                scrollToSelectedTab(checkedId)
                                viewModel.setSelectCheckedId(checkedId)
                            }
                        })
                        .start()
                }
            }
        }

        // 确保在布局完成后获取每个 RadioButton 的位置
        binding.tab.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.tab.viewTreeObserver.removeOnGlobalLayoutListener(this)
                scrollToSelectedTab(viewModel.selectCheckedId.value!!)
            }
        })

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            adapter?.notifyDataSetChanged()
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

    //界面数据显示
    private fun setHelpData(select: Int) {
        viewModel.setSelectTab(select)
        viewModel.setSelectList(select)
        adapter?.onRefreshData(viewModel.getSelectList())
        binding.list.scrollToPosition(0)
    }

    private fun scrollToSelectedTab(checkedId: Int) {
        try {
            if (!isAnimationRunning){
                val selectedView = view?.findViewById<View>(checkedId)
                if (selectedView != null) {
                    val left = selectedView.left
                    val width = selectedView.width
                    val targetScrollX = left - (binding.tab.width - width) / 2
                    binding.tab.smoothScrollTo(targetScrollX, 0)
                }
            }
        }catch (e: Exception){
            Timber.e("scrollToSelectedTab Exception:${e.message}")
        }
    }
}