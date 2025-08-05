package com.desaysv.psmap.ui.share

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.widget.BusinessApplicationUtils
import com.autosdk.bussiness.widget.navi.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.ViewNaviTripshareFragmentBinding
import com.desaysv.psmap.model.utils.QrUtils
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.share.compose.PhoneNumComposeListView
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/5
 * @description 分享二维码弹框
 */
@AndroidEntryPoint
class TripShareDialogFragment : Fragment(), NetWorkManager.NetWorkChangeListener {
    private lateinit var binding: ViewNaviTripshareFragmentBinding
    private val viewModel by viewModels<TripShareDialogViewModel>()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness
    private var needRefresh = false

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewNaviTripshareFragmentBinding.inflate(inflater, container, false).apply {
            composeView.setContent {
                composeView()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        viewModel.getPhoneNumList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        netWorkManager.removeNetWorkChangeListener(this)
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        initEventOperation()
//        viewModel.reqTripUrl()
        netWorkManager.addNetWorkChangeListener(this)
        viewModel.setShareType(viewModel.shareType.value?:0)
    }

    private fun initEventOperation() {
        //关闭按钮
        binding.ivClose.setDebouncedOnClickListener { findNavController().navigateUp() }
        binding.ivQrCodeBg.setDebouncedOnClickListener { reqTripQrCode() }
        binding.tvShareWeChat.setDebouncedOnClickListener {
            viewModel.setShareType(0)
            binding.searchAroundEditText.text.clear()
        }
        binding.tvShareSms.setDebouncedOnClickListener {
            viewModel.setShareType(1)
        }
        binding.btSend.setDebouncedOnClickListener {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Nav_Start,
                mapOf(Pair(EventTrackingUtils.EventValueName.MailShareTime, System.currentTimeMillis()))
            )
            viewModel.sendSmsTripShare(binding.searchAroundEditText.text.toString())
        }
//        binding.skinTextView2.setDebouncedOnClickListener {
//            viewModel.clearPhoneNumList()
//        }
        binding.sivSearchDelete.setDebouncedOnClickListener {
            binding.searchAroundEditText.text.clear()
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run {
                skyBoxBusiness.updateView(this, true)
                if (viewModel.naviTripUrlLd.value.isNullOrEmpty()) {
                    if (needRefresh) {
                        showFail()
                    } else {
                        binding.ivQrCodeBg.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_qrcodes_failed else R.drawable.ic_qrcodes_failed_day)
                    }
                }
            }
        }
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }
        binding.searchAroundEditText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var isDeleting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 在文本变化之前执行的操作
                isDeleting = count > after // 判断是否是删除操作
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 在文本变化过程中执行的操作

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
                    val cursorPos = binding.searchAroundEditText.selectionStart
                    if (isDeleting && cursorPos > 0 && editable?.get(cursorPos - 1) == ' ') {
                        binding.searchAroundEditText.setSelection(cursorPos - 1)
                    } else {
                        binding.searchAroundEditText.setSelection(binding.searchAroundEditText.text.length)
                    }
                }
                viewModel.setClickSendState(viewModel.isValidPhoneNumber(formattedText))
                binding.sivSearchDelete.visibility = if (editable.toString().isEmpty()) View.GONE else View.VISIBLE

                isFormatting = false
            }
        })

        binding.searchAroundEditText.setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        viewModel.naviTripUrlLd.observe(viewLifecycleOwner) {
            Timber.i("TripShareDialogFragment naviTripUrlLd = $it")
            if (it.isNullOrEmpty()) {
                showFail()
            } else {
                showSuccess(it)
            }
        }
        viewModel.shareType.unPeek().observe(viewLifecycleOwner) {
            Timber.i("TripShareDialogFragment shareType = $it")
            val selectColor =
                resources.getColor(
                    if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight
                    else com.desaysv.psmap.model.R.color.onPrimaryDay
                )
            val noSelectColor =
                resources.getColor(
                    if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night
                    else com.desaysv.psmap.model.R.color.customColorRbBgDay
                )
            binding.tvShareWeChat.setTextColor(if (it == 0) selectColor else noSelectColor)
            binding.tvShareSms.setTextColor(if (it == 0) noSelectColor else selectColor)
            checkIndicator(it)
        }
    }

    private fun checkIndicator(shareType: Int, withAnimation: Boolean = true) {

        // 判断动画持续时间
        val duration = 200 // 非相邻的 RadioButton
        // 计算指示条应该移动到的位置
        var targetX = 0
        if (shareType == 0) {
            targetX = binding.tvShareWeChat.left + (binding.tvShareWeChat.width - binding.indicator.width) / 2
        } else {
            targetX = binding.tvShareSms.left + (binding.tvShareSms.width - binding.indicator.width) / 2
        }

        if (withAnimation) {
            binding.indicator.animate()
                .x(targetX.toFloat())
                .setDuration(duration.toLong())
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        } else {
            binding.indicator.animate()
                .x(targetX.toFloat())
                .setDuration(0L)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
    }

    private fun showFail() {
        binding.pbLoading.visibility = View.GONE
        binding.pbLoadingTip.visibility = View.GONE
        binding.ivQrCode.visibility = View.GONE
        binding.ivRefresh.visibility = View.VISIBLE
        binding.refreshBtTip.visibility = View.VISIBLE
        binding.refreshTip.visibility = View.VISIBLE
//        binding.ivQrCode.setImageResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_navi_btn_refresh_night else R.drawable.selector_navi_btn_refresh_day)
        binding.ivQrCodeBg.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_qrcodes_failed else R.drawable.ic_qrcodes_failed_day)
        binding.ivQrCodeBg.scaleType = ImageView.ScaleType.CENTER_INSIDE
        binding.tvTips.setText(R.string.sv_navi_tripshare_refresh)
        needRefresh = true
    }

    private fun showSuccess(url: String) {
        binding.pbLoading.visibility = View.GONE
        binding.ivRefresh.visibility = View.GONE
        binding.refreshBtTip.visibility = View.GONE
        binding.refreshTip.visibility = View.GONE
        binding.pbLoadingTip.visibility = View.GONE
        binding.ivQrCode.visibility = View.VISIBLE
        val bitmap: Bitmap? = QrUtils.createQRImage(
            url,
            ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_350),
            ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), com.desaysv.psmap.base.R.dimen.sv_dimen_350)
        )
        binding.ivQrCode.setImageBitmap(bitmap)
        binding.ivQrCodeBg.setBackgroundResource(R.drawable.shape_trip_share_bg_qr1)
        binding.ivQrCode.scaleType = ImageView.ScaleType.FIT_XY
        binding.tvTips.setText(R.string.sv_navi_tripshare_title)
    }


    private fun reqTripQrCode() {
        if (needRefresh) {
            needRefresh = false
            binding.ivQrCode.setImageBitmap(null)
            binding.ivRefresh.visibility = View.GONE
            binding.refreshBtTip.visibility = View.GONE
            binding.refreshTip.visibility = View.GONE
            binding.pbLoading.visibility = View.VISIBLE
            binding.pbLoadingTip.visibility = View.VISIBLE
            binding.tvTips.setText(R.string.sv_navi_tripshare_title)
            viewModel.reqTripUrl()
        }
    }

    /**
     * 手机号列表
     */
    @Composable
    private fun composeView() {
        val phoneNumList by viewModel.phoneNumList.unPeek().observeAsState(emptyList())
        val refreshView by viewModel.mDataRefresh.unPeek().observeAsState(-1)
        val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())
        DsDefaultTheme(themeChange) {
            PhoneNumComposeListView(phoneNumList, refreshView, onContinueClicked = {
                binding.searchAroundEditText.setText(it)
                binding.searchAroundEditText.clearFocus()
            })
        }
    }

    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        Timber.i(" onNetWorkChangeListener is called isNetworkConnected = $isNetworkConnected")
        viewModel.reqTripUrl()
        if (isNetworkConnected) {
            if (mRouteRequestController.carRouteResult.isOffline) {
                viewModel.retryPlanRoute()
            }
        } else {
            viewModel.setSwitchOffline()
        }
    }


}