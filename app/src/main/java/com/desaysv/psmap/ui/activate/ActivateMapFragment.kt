package com.desaysv.psmap.ui.activate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentActivateMapBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 激活界面
 */
@AndroidEntryPoint
class ActivateMapFragment : Fragment() {
    private lateinit var binding: FragmentActivateMapBinding
    private val viewModel by viewModels<ActivateMapViewModel>()

    private var mClickTimes = 0
    private var isFirst = true
    private var callback: OnBackPressedCallback? = null
    private var params: ConstraintLayout.LayoutParams? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    private var customDialogFragment: CustomDialogFragment? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.serialNumberInput.onSaveInstanceState(outState, "serial")
        binding.activeCodeInput.onSaveInstanceState(outState, "active")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentActivateMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.serialNumberInput.savedInstanceState(savedInstanceState, "serial")
        binding.activeCodeInput.savedInstanceState(savedInstanceState, "active")
        binding.serialNumberInput.setNight(NightModeGlobal.isNightMode())
        binding.activeCodeInput.setNight(NightModeGlobal.isNightMode())
        binding.serialNumberInput.setOnTouchListener(requireActivity())
        binding.activeCodeInput.setOnTouchListener(requireActivity())
        params = binding.manualLayout.layoutParams as ConstraintLayout.LayoutParams?
        KeyboardUtil.registerSoftInputChangedListener(requireActivity().window, softInputChangedListener)

        onBackPressedCallback()//系统返回键监听
        initEventOperation()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            Timber.d("ActivateMapFragment onResume $isFirst")
            if (isFirst) {
                isFirst = false
                if (BaseConstant.UUID_EMPTY) {
                    Timber.d("onResume UUID_EMPTY")
                    viewModel.reInit();//重新进入激活界面，判断是否需要重新初始化
                }
                viewModel.netActivate()
                viewModel.updateUUIDNumber() //显示UUID
            } else {
                if (BaseConstant.UUID_EMPTY)
                    viewModel.reInit() //重新进入激活界面，判断是否需要重新初始化
                viewModel.updateUUIDNumber() //显示UUID
            }
        }
        KeyboardUtil.hideKeyboard(view)
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dismissCustomDialog()
        callback = null
        KeyboardUtil.unregisterSoftInputChangedListener(requireActivity().window)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d(" hidden:%s", hidden);
        if (hidden) {
            dismissCustomDialog()
        }
    }

    //系统返回键监听
    private fun onBackPressedCallback() {
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback as OnBackPressedCallback)
    }

    private fun initEventOperation() {
        //工程模式按钮点击
        binding.backDoor.setDebouncedOnClickListener {
            engineerBtnClick()
        }

        //工程模式按钮长按
        binding.backDoor.setOnLongClickListener {
            Timber.d("open backDoor setOnLongClickListener")
            if (mClickTimes == 5) {
                Timber.d("open backDoor setOnLongClickListener mClickTimes == 5")
                mClickTimes = 0
                NavHostFragment.findNavController(this@ActivateMapFragment).navigate(R.id.action_to_engineerFragment)
            }
            false
        }

        //激活方式界面选择手动激活
        binding.normalManual.setDebouncedOnClickListener {
            viewModel.activeLayoutType.postValue(0)//0.手动激活 1.激活成功 2.激活失败 3.激活方式选择界面--默认
        }
        ViewClickEffectUtils.addClickScale(binding.normalManual, CLICKED_SCALE_95)

        //激活方式界面选择联网激活
        binding.normalNet.setDebouncedOnClickListener {
            viewModel.netActivate()
        }
        ViewClickEffectUtils.addClickScale(binding.normalNet, CLICKED_SCALE_95)

        //重新进入手动激活
        binding.failActivateManual.setDebouncedOnClickListener {
            viewModel.activeLayoutType.postValue(0)//0.手动激活 1.激活成功 2.激活失败 3.激活方式选择界面--默认
        }
        ViewClickEffectUtils.addClickScale(binding.failActivateManual, CLICKED_SCALE_95)

        //重新进行网络激活
        binding.activateAgain.setDebouncedOnClickListener {
            viewModel.netActivate()
        }
        ViewClickEffectUtils.addClickScale(binding.activateAgain, CLICKED_SCALE_95)

        //序列号输入监听
        binding.serialNumberInput.setActiveInputImpl(object : ActiveInputNumberCodeView.ActiveInputImpl {
            override fun onActiveInput(text: String) {
                Timber.i("onActiveInput text:$text")
                viewModel.serialNumberInput.postValue(text)
            }
        })

        //序列号界面--下一步按钮点击
        binding.next.setDebouncedOnClickListener {
            viewModel.showSerialNumber.postValue(false)
        }
        ViewClickEffectUtils.addClickScale(binding.next, CLICKED_SCALE_95)

        //序列号界面重新联网激活按钮
        binding.serialNumberNet.setDebouncedOnClickListener {
            viewModel.netActivate()
        }
        ViewClickEffectUtils.addClickScale(binding.serialNumberNet, CLICKED_SCALE_95)

        //激活码界面输入监听
        binding.activeCodeInput.setActiveInputImpl(object : ActiveInputNumberCodeView.ActiveInputImpl {
            override fun onActiveInput(text: String) {
                viewModel.activeCodeInput.postValue(text)
            }
        })

        //激活界面激活按钮
        binding.activeCodeToActive.setDebouncedOnClickListener {
            viewModel.manualActivate(viewModel.serialNumberInput.value ?: "", viewModel.activeCodeInput.value ?: "")
        }
        ViewClickEffectUtils.addClickScale(binding.activeCodeToActive, CLICKED_SCALE_95)

        //激活界面--上一步按钮点击
        binding.previous.setDebouncedOnClickListener {
            viewModel.showSerialNumber.postValue(true)
        }
        ViewClickEffectUtils.addClickScale(binding.previous, CLICKED_SCALE_95)

        //激活状态
        viewModel.isActivating.unPeek().observe(viewLifecycleOwner) { isActivating ->
            Timber.d("isActivating is $isActivating")
        }

        //激活结果
        viewModel.activateResult.unPeek().observe(viewLifecycleOwner) { result: Int? ->
            Timber.i("getActivateResult() is $result")
            if (null != result) { //激活结果 0.成功 1.手动激活失败 2.网络激活失败 3.usb激活失败
                when (result) {
                    0 -> {
                        viewModel.activeLayoutType.postValue(1)//0.手动激活 1.激活成功 2.激活失败 3.激活方式选择界面--默认
                        findNavController().navigateUp()
                        findNavController().navigate(R.id.to_agreementFragment)
                    }

                    1 -> {
                        failDialog() //手动激活失败提示框
                    }

                    else -> {
                        viewModel.activeLayoutType.postValue(2)//0.手动激活 1.激活成功 2.激活失败 3.激活方式选择界面--默认
                    }
                }
            } else {
                viewModel.activeLayoutType.postValue(2)//0.手动激活 1.激活成功 2.激活失败 3.激活方式选择界面--默认
            }
        }

        //判断是否显示鲸图界面
        viewModel.showActivateAgreement.unPeek().observe(viewLifecycleOwner) { (isShowActivate, isShowAgreement) ->
            Timber.d("isShowActivate:$isShowActivate isShowAgreement：$isShowAgreement")
            // 在这里处理 isShowActivateLayout 和 isShowAgreementLayout 的变化
            if (viewModel.activeLayoutType.value != 1) {
                if (isShowActivate) {//显示激活界面
                    Timber.d("now is ActivateLayout")
                } else if (isShowAgreement) {//显示蓝鲸协议界面
                    findNavController().navigateUp()
                    findNavController().navigate(R.id.to_agreementFragment)
                }
            }
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            binding.serialNumberInput.setNight(NightModeGlobal.isNightMode())
            binding.activeCodeInput.setNight(NightModeGlobal.isNightMode())
        }

        //停止激活
        binding.confirmStop.setDebouncedOnClickListener {
            viewModel.stopActivate()
            viewModel.activeLayoutType.postValue(2)//0.手动激活 1.激活成功 2.激活失败 3.激活方式选择界面--默认
        }
    }

    private val softInputChangedListener: KeyboardUtil.OnSoftInputChangedListener =
        KeyboardUtil.OnSoftInputChangedListener { isSoftInputVisible ->
            params!!.bottomMargin =
                if (isSoftInputVisible > 0) isSoftInputVisible else resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_0)
            binding.manualLayout.layoutParams = params
        }

    //进入工程模式按钮点击计数
    private fun engineerBtnClick() {
        Timber.d("engineerBtnClick mClickTimes $mClickTimes")
        if (mClickTimes != 5) {
            mClickTimes++
        }
    }

    //手动激活失败提示框
    private fun failDialog() {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle("激活失败")
            .setContent("请检查序列号、激活码是否输入正确")
            .singleButton(requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_we_got_it))
            .setOnClickListener {
                Timber.i("failDialog 知道了")
            }.apply {
                show(this@ActivateMapFragment.childFragmentManager, "failDialog")
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