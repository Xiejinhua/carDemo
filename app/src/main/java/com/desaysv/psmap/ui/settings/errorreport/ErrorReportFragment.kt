package com.desaysv.psmap.ui.settings.errorreport

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentErrorReportBinding
import com.desaysv.psmap.utils.LoadingUtil
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_97
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.UNCLICKED_SCALE
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.IssueFeedbackAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 错误信息上报
 */
@AndroidEntryPoint
class ErrorReportFragment : Fragment() {
    private lateinit var binding: FragmentErrorReportBinding
    private val viewModel: ErrorReportViewModel by viewModels()
    private lateinit var issueFeedbackAdapter: IssueFeedbackAdapter
    private var lastCheckedId: Int? = null // 记录上一个选中的 RadioButton ID
    private var lastTargetX = 0

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var loadingUtil: LoadingUtil

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        loadingUtil.cancelLoading(onCancelClick = { viewModel.setShowLoading(false) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentErrorReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onDestroy() {
        super.onDestroy()
        lastTargetX = 0
        lastCheckedId = null
        viewModel.resetDefault(BaseConstant.TYPE_PAGE_ISSUE) //回到反馈列表/或者指定界面
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        Timber.d(" initBinding selectTab:%s", viewModel.posProblemSelect.value )
        if (viewModel.posProblemSelect.value == 0){
            binding.problemLayoutTab.check(R.id.problem_one)
        }
        setEditTextFocus(binding.inputDec, false)
        setEditTextFocus(binding.inputPhone, false)
        issueFeedbackAdapter = IssueFeedbackAdapter().also { binding.issueList.adapter = it }
        issueFeedbackAdapter.onRefreshData(viewModel.issueTypeList)
        binding.inputPhone.setClearDrawable(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_delete_circle_night else R.drawable.selector_ic_delete_circle_day)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            doBack() //返回操作
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        issueFeedbackAdapter.setOnItemClickListener { _, _, position ->
            val type = issueFeedbackAdapter.data[position]?.type ?: BaseConstant.TYPE_ISSUE_POS
            Timber.i("ErrorReportFragment setOnItemClickListener pageType:$type")
            lastTargetX = 0
            lastCheckedId = null
            viewModel.resetDefault(type) //回到反馈列表/或者指定界面
            setEditTextFocus(binding.inputDec, false)
            setEditTextFocus(binding.inputPhone, false)
        }

        binding.problemLayoutTab.setOnCheckedChangeListener { group, checkedId ->
            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                // 判断动画持续时间
                val duration = if (lastCheckedId != null && areAdjacent(lastCheckedId!!, checkedId)) {
                    200 // 相邻的 RadioButton
                } else {
                    300 // 非相邻的 RadioButton
                }
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.problem_one){
                    binding.indicator.animate()
                        .x(if (lastTargetX > 0) (lastTargetX - 1).toFloat() else lastTargetX.toFloat())
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
                                lastCheckedId = checkedId // 更新上一个选中的 ID
                                lastTargetX = targetX
                                when (checkedId) {
                                    R.id.problem_one -> {
                                        viewModel.setPosProblemSelect(0) //存在的问题，常用选项 选择操作
                                    }

                                    R.id.problem_two -> {
                                        viewModel.setPosProblemSelect(1) //存在的问题，常用选项 选择操作
                                    }

                                    R.id.problem_three -> {
                                        viewModel.setPosProblemSelect(2) //存在的问题，常用选项 选择操作
                                    }
                                }
                            }
                        })
                        .start()
                }
            }
        }

        //进入问题描述编辑界面
        binding.inputFeedbackDec.setDebouncedOnClickListener {
            viewModel.setPageType(BaseConstant.TYPE_ISSUE_EDIT)
            setEditTextFocus(binding.inputPhone, false)
            setEditTextFocus(binding.inputDec, true)
        }
        ViewClickEffectUtils.addClickScale(binding.inputFeedbackDec)
        binding.inputFeedbackDecTv.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    ViewClickEffectUtils.animateScale(binding.inputFeedbackDec, CLICKED_SCALE_97)
                    true // 返回 true 以消费事件
                }
                MotionEvent.ACTION_MOVE -> {
                    // 检查手指是否在控件外部
                    if (event.x < 0 || event.x > v.width || event.y < 0 || event.y > v.height) {
//                        ViewClickEffectUtils.animateScale(binding.inputFeedbackDec, UNCLICKED_SCALE)
                        true // 返回 true 以消费事件
                    } else {
                        false // 返回 false 以继续接收事件
                    }
                }
                MotionEvent.ACTION_UP -> {
                    // 只有在手指在控件内部抬起时才触发逻辑
                    if (event.x >= 0 && event.x <= v.width && event.y >= 0 && event.y <= v.height) {
                        viewModel.setPageType(BaseConstant.TYPE_ISSUE_EDIT)
                        setEditTextFocus(binding.inputPhone, false)
                        setEditTextFocus(binding.inputDec, true)
                    }
                    ViewClickEffectUtils.animateScale(binding.inputFeedbackDec, UNCLICKED_SCALE)
                    true // 返回 true 以消费事件
                }
                MotionEvent.ACTION_CANCEL -> {
                    ViewClickEffectUtils.animateScale(binding.inputFeedbackDec, UNCLICKED_SCALE)
                    true // 返回 true 以消费事件
                }
                else -> false
            }
        }

        //进入手机编辑界面
        binding.inputFeedbackContact.setDebouncedOnClickListener {
            viewModel.setPageType(BaseConstant.TYPE_ISSUE_PHONE)
            setEditTextFocus(binding.inputDec, false)
            setEditTextFocus(binding.inputPhone, true)
        }
        ViewClickEffectUtils.addClickScale(binding.inputFeedbackContact)

        //问题描述输入框监听
        binding.inputDec.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.setInputDecStr(s.toString()) //输入
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.inputDec.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        //描述输入框删除按钮点击
        binding.inputDecClear.setDebouncedOnClickListener {
            binding.inputDec.setText("")
        }

        //手机号框监听
        binding.inputPhone.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var isDeleting = false
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // 在文本变化之前执行的操作
                isDeleting = count > after // 判断是否是删除操作
            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                // 在文本变化之后执行的操作
                if (isFormatting) return
                isFormatting = true

                val digits = editable.toString().replace(" ", "")
                val formattedText = when {
                    digits.length <= 3 -> digits
                    digits.length <= 7 -> "${digits.substring(0, 3)} ${digits.substring(3)}"
                    else -> CommonUtils.getFormattedPhone(digits)
                }
                // 3. 更新文本（避免无限循环）
                if (editable.toString() != formattedText) {
                    editable?.replace(0, editable.length, formattedText)

                    // 4. 调整光标位置（处理删除时的光标跳动）
                    val cursorPos = binding.inputPhone.selectionStart
                    if (isDeleting && cursorPos > 0 && editable?.get(cursorPos - 1) == ' ') {
                        binding.inputPhone.setSelection(cursorPos - 1)
                    } else {
                        binding.inputPhone.setSelection(binding.inputPhone.text!!.length)
                    }
                }
                viewModel.setInputPhoneStr(formattedText) //输入

                isFormatting = false
            }
        })

        binding.inputPhone.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        //问题输入框点击确认按钮
        binding.inputDecConfirm.setDebouncedOnClickListener {
            viewModel.setPageType(viewModel.getNoFeedbackListPhoneEdit())
        }
        ViewClickEffectUtils.addClickScale(binding.inputDecConfirm, CLICKED_SCALE_95)

        //手机号输入框点击确认按钮
        binding.phoneConfirm.setDebouncedOnClickListener {
            viewModel.setPageType(viewModel.getNoFeedbackListPhoneEdit())
        }
        ViewClickEffectUtils.addClickScale(binding.phoneConfirm, CLICKED_SCALE_95)

        //路线规划不合理 按钮点击
        binding.routePlanningReasonable.setDebouncedOnClickListener {
            viewModel.setInputDecStr(viewModel.inputDecStr.value + binding.routePlanningReasonable.text.toString()) //输入框字符串增加 路线规划不合理
        }
        ViewClickEffectUtils.addClickScale(binding.routePlanningReasonable, CLICKED_SCALE_95)

        //地图配色问题 按钮点击
        binding.mapColorMatchingIssue.setDebouncedOnClickListener {
            viewModel.setInputDecStr(viewModel.inputDecStr.value + binding.mapColorMatchingIssue.text.toString()) //输入框字符串增加 地图配色问题
        }
        ViewClickEffectUtils.addClickScale(binding.mapColorMatchingIssue, CLICKED_SCALE_95)

        //网络无法连接 按钮点击
        binding.netNotConnect.setDebouncedOnClickListener {
            viewModel.setInputDecStr(viewModel.inputDecStr.value + binding.netNotConnect.text.toString()) //输入框字符串增加 网络无法连接
        }
        ViewClickEffectUtils.addClickScale(binding.netNotConnect, CLICKED_SCALE_95)

        //提交按钮点击
        binding.report.setDebouncedOnClickListener {
            viewModel.sendReqFeedbackReport() //用户反馈-错误上报
        }
        ViewClickEffectUtils.addClickScale(binding.report, CLICKED_SCALE_95)

        //toast显示
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) {
            if (it == BaseConstant.LOGIN_STATE_SUCCESS && (viewModel.inputPhoneStr.value == null || TextUtils.isEmpty(viewModel.inputPhoneStr.value))) {
                viewModel.getPhoneStr()  //重新获取账号手机号码
            }
        }

        viewModel.showLoading.observe(viewLifecycleOwner) {
            loadingUtil.cancelLoading(onCancelClick = { viewModel.setShowLoading(false) })
            if (it) {
                loadingUtil.showLoading(
                    requireContext().resources.getString(R.string.sv_setting_submitting_feedback_information_for_you),
                    onItemClick = {//主动关闭上传等待框
                        toastUtil.showToast(context?.getString(R.string.sv_setting_report_has_been_cancelled) ?: "")
                        viewModel.setShowLoading(false)
                    })
            }
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            issueFeedbackAdapter.notifyDataSetChanged()
            binding.inputPhone.setClearDrawable(if (it) R.drawable.selector_ic_delete_circle_night else R.drawable.selector_ic_delete_circle_day)
        }
    }

    // 判断两个 RadioButton 是否相邻
    private fun areAdjacent(lastId: Int, currentId: Int): Boolean {
        return when {
            (lastId == R.id.problem_one && currentId == R.id.problem_two) ||
                    (lastId == R.id.problem_two && currentId == R.id.problem_one) -> true
            (lastId == R.id.problem_two && currentId == R.id.problem_three) ||
                    (lastId == R.id.problem_three && currentId == R.id.problem_two) -> true
            else -> false
        }
    }

    //返回操作
    private fun doBack() {
        when (viewModel.pageType.value) {
            BaseConstant.TYPE_PAGE_ISSUE_POS, BaseConstant.TYPE_PAGE_ISSUE_INTERNET, BaseConstant.TYPE_PAGE_ISSUE_DATA_DOWNLOAD, BaseConstant.TYPE_PAGE_ISSUE_BROADCAST, BaseConstant.TYPE_PAGE_ISSUE_OTHER -> {
                lifecycleScope.launch {
                    lastTargetX = 0
                    lastCheckedId = null
                    viewModel.resetDefault(BaseConstant.TYPE_PAGE_ISSUE) //回到反馈列表/或者指定界面
                    setEditTextFocus(binding.inputDec, false)
                    setEditTextFocus(binding.inputPhone, false)
                    binding.indicator.animate()
                        .x(lastTargetX.toFloat())
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                    binding.problemLayoutTab.check(R.id.problem_one)
                }
            }

            BaseConstant.TYPE_PAGE_ISSUE_PHONE, BaseConstant.TYPE_PAGE_ISSUE_EDIT_DEC -> {
                viewModel.setPageType(viewModel.getNoFeedbackListPhoneEdit())
                viewModel.judeShowPhone() //手机编辑界面退出时，判断手机号码显示
            }

            else -> {
                viewModel.getPhoneStr()  //重新进入错误界面时，获取账号手机号码
                findNavController().navigateUp()
            }
        }
    }

    //EditText 设置焦点
    private fun setEditTextFocus(edit: EditText, isFocus: Boolean) {
        if (isFocus) {
            edit.requestFocus()
            edit.setFocusableInTouchMode(true)
            KeyboardUtil.showKeyboard(edit)
        } else {
            edit.clearFocus()
            edit.setFocusableInTouchMode(false)
            KeyboardUtil.hideKeyboard(edit)
        }
    }
}