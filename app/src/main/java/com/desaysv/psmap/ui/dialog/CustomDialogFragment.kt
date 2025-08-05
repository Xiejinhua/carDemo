package com.desaysv.psmap.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.DialogCustomBinding
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @date 2024-01-20
 * @project：通用对话框
 */
@AndroidEntryPoint
class CustomDialogFragment private constructor() : DialogFragment() {
    private lateinit var binding: DialogCustomBinding
    private val viewModel by viewModels<CustomDialogViewModel>()
    private var title: String = ""
    private var content: String = ""
    private var confirm: String? = null
    private var cancel: String? = null
    private var know: String = ""
    private var isTeam = false
    private var isMoreLine = false
    private lateinit var mListener: (isOk: Boolean) -> Unit // 自定义接口OnDialogListener
    private var countDownTimes = 0L
    private var showCloseBtn: Boolean = false
    private var windowManager: WindowManager? = null

    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(countDownTimes, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                if (countDownTimes > 0) {// 在每秒钟的间隔中更新UI，显示剩余秒数
                    if (isTeam) {
                        binding.tvContent1.text = "($secondsRemaining)秒后离开此界面"
                    } else {
                        if (TextUtils.isEmpty(know)) {
                            binding.cancelTv.text = cancel ?: resources.getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
                                .plus("(" + secondsRemaining.toString() + "S)")
                        } else {
                            binding.knowTv.text = know.plus("(" + secondsRemaining.toString() + "S)")
                        }
                    }
                }
            }

            override fun onFinish() { // 倒计时完成后执行的操作
                if (countDownTimes > 0) {
                    setClickResult(true)
                }
            }
        }
    }

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCustomBinding.inflate(inflater, container, false)
        skyBoxBusiness.updateView(binding.root, true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause")
        countDownTimer.cancel()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        removeViewFromWindow()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView")
        countDownTimer.cancel()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        doWindowManager() //窗口操作
        dialog?.setCanceledOnTouchOutside(false) //空白处不能取消动画
        isCancelable = false //返回键不能取消

        viewModel.initData(
            title, content, confirm ?: resources.getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
            cancel ?: resources.getString(com.desaysv.psmap.base.R.string.sv_common_cancel), know, showCloseBtn, isMoreLine
        )
        if (countDownTimes > 0) {
            countDownTimer.cancel()
            countDownTimer.start()
        }
    }

    //窗口操作
    private fun doWindowManager() {
        // 初始化 WindowManager
        val displayManager = requireContext().getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(0) // 根据 displayId 获取 Display 对象
        val displayContext: Context = requireContext().createDisplayContext(display)

        // 创建 WindowLayoutParams
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,  //设置层级
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // 检查视图是否已经有父视图
        if (binding.root.parent != null) {
            (binding.root.parent as ViewGroup).removeView(binding.root)
        }

        if (windowManager == null) {
            windowManager = displayContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }
        binding.root.accessibilityPaneTitle = "v2swindow@psmapDialog"
        // 通过 WindowManager 添加视图到窗口
        windowManager?.addView(binding.root, params)
    }

    //消除窗口
    private fun removeViewFromWindow() {
        try {
            windowManager?.removeView(binding.root)
        } catch (e: Exception) {
            Timber.e("removeViewFromWindow exception: ${e.message}")
        }
    }

    private fun initEventOperation() {
        //取消按钮
        binding.cancel.setDebouncedOnClickListener { setClickResult(false) }
        ViewClickEffectUtils.addClickScale(binding.cancel, CLICKED_SCALE_95)

        //确定按钮
        binding.ok.setDebouncedOnClickListener { setClickResult(true) }
        ViewClickEffectUtils.addClickScale(binding.ok, CLICKED_SCALE_95)

        //知道了按钮
        binding.know.setDebouncedOnClickListener { setClickResult(true) }
        ViewClickEffectUtils.addClickScale(binding.know, CLICKED_SCALE_95)

        binding.closeIv.setDebouncedOnClickListener { setClickResult(false) }
        binding.clRoot.setDebouncedOnClickListener { setClickResult(false) }
        ViewClickEffectUtils.addClickScale(binding.closeIv, CLICKED_SCALE_90)

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }
    }


    private fun setClickResult(isOk: Boolean) {
        countDownTimer.cancel()
        dismissAllowingStateLoss()
        mListener(isOk)
        removeViewFromWindow()
    }

    fun setContent(text: String): CustomDialogFragment {
        content = text
        return this
    }

    fun setCloseButton(isShow: Boolean): CustomDialogFragment {
        showCloseBtn = isShow
        return this
    }

    fun setTitle(text: String): CustomDialogFragment {
        title = text
        return this
    }

    fun setCountDown(time: Long = 10000): CustomDialogFragment {
        if (time < 1000)
            countDownTimes = 1000
        countDownTimes = time
        return this
    }

    fun singleButton(text: String): CustomDialogFragment {
        know = text
        return this
    }

    fun doubleButton(mConfirm: String, mCancel: String): CustomDialogFragment {
        confirm = mConfirm
        cancel = mCancel
        return this
    }

    fun setIsTeam(mIsTeam: Boolean): CustomDialogFragment {
        isTeam = mIsTeam
        return this
    }

    fun setMoreLine(moreLine: Boolean): CustomDialogFragment {
        isMoreLine = moreLine
        return this
    }

    fun setOnClickListener(onItemClick: (isOk: Boolean) -> Unit = {}): CustomDialogFragment { // 为确定按钮和取消按钮设置监听器
        mListener = onItemClick
        return this
    }

    companion object {
        fun builder(): CustomDialogFragment {
            return CustomDialogFragment()
        }
    }

}