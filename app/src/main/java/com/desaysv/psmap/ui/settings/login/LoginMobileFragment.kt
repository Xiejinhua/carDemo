package com.desaysv.psmap.ui.settings.login

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.databinding.FragmentLoginMobileBinding
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 手机号码登录
 */
@AndroidEntryPoint
class LoginMobileFragment : Fragment() {
    private lateinit var binding: FragmentLoginMobileBinding
    private val viewModel: LoginMobileViewModel by viewModels()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginMobileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.setCheckState(false)
        viewModel.defaultVerificationCodeTip() //退出账号登录界面，验证码按钮文本恢复
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.inputPhone.setClearDrawable(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_delete_circle_night else R.drawable.selector_ic_delete_circle_day)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun initEventOperation() {
        //手机号码输入框监听
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
                viewModel.getPhoneNumber(formattedText) //监听获取手机号码

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

        //验证码输入框监听
        binding.inputVerification.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.getVerificationCode(s.toString().trim()) //监听验证码输入
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.inputVerification.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        //验证码清空按钮点击
        binding.verificationClose.setDebouncedOnClickListener {
            binding.inputVerification.setText("")
            viewModel.toSetVerificationCode("")
        }
        ViewClickEffectUtils.addClickScale(binding.verificationClose, CLICKED_SCALE_90)

        //获取验证码
        binding.getVerification.setDebouncedOnClickListener {
            viewModel.requestAccountCheck() //验证账号是否存在,若存在就获取验证码
        }
        ViewClickEffectUtils.addClickScale(binding.getVerification, CLICKED_SCALE_93)

        //协议勾选按钮
        binding.cbNoMoreTips.setDebouncedOnClickListener {
            val checkState = viewModel.checkState.value ?: false
            viewModel.setCheckState(!checkState)
        }

        //服务协议
        binding.agreementService.setDebouncedOnClickListener { //0.服务条款 1.隐私权政策 2.高德账号服务个人信息处理规则
            findNavController().navigate(R.id.to_agreementDetailFragment, Bundle().apply { putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 0) })
        }

        //隐私协议
        binding.agreementPolicy.setDebouncedOnClickListener { //0.服务条款 1.隐私权政策 2.高德账号服务个人信息处理规则
            findNavController().navigate(R.id.to_agreementDetailFragment, Bundle().apply { putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 1) })
        }
        binding.agreementPolicy1.setDebouncedOnClickListener { //0.服务条款 1.隐私权政策 2.高德账号服务个人信息处理规则
            findNavController().navigate(R.id.to_agreementDetailFragment, Bundle().apply { putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 1) })
        }

        //高德账号服务个人信息处理规则
        binding.agreementAccount.setDebouncedOnClickListener { //0.服务条款 1.隐私权政策 2.高德账号服务个人信息处理规则
            findNavController().navigate(R.id.to_agreementDetailFragment, Bundle().apply { putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 2) })
        }
        binding.agreementAccount1.setDebouncedOnClickListener { //0.服务条款 1.隐私权政策 2.高德账号服务个人信息处理规则
            findNavController().navigate(R.id.to_agreementDetailFragment, Bundle().apply { putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 2) })
        }

        //登录
        binding.login.setDebouncedOnClickListener {
            viewModel.mobileLogin()  //进行登录
        }
        ViewClickEffectUtils.addClickScale(binding.login, CLICKED_SCALE_95)

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            binding.inputPhone.setClearDrawable(if (it) R.drawable.selector_ic_delete_circle_night else R.drawable.selector_ic_delete_circle_day)
        }
    }
}