package com.desaysv.psmap.ui.settings.trip

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autosdk.bussiness.account.bean.TrackItemBean
import com.autosdk.common.utils.AutoGuideLineHelper
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMyTripDetailBinding
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 行程详情
 */
@AndroidEntryPoint
class MyTripDetailFragment : Fragment() {
    private lateinit var binding: FragmentMyTripDetailBinding
    private val viewModel: MyTripDetailViewModel by viewModels()

    private var customDialogFragment: CustomDialogFragment? = null

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyTripDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.onDestroyView()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val trackItemBeam = arguments?.getSerializable(Biz.TO_TRIP_DETAIL_INFO) as? TrackItemBean ?: TrackItemBean()
        viewModel.setTrackItemBean(trackItemBeam)
        viewModel.setRect(getMapPreviewRect())
        viewModel.initData()
    }

    private fun initEventOperation() {
        //退出该界面
        binding.backTitle.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.backTitle, CLICKED_SCALE_90)

        //删除操作
        binding.tripDelete.setDebouncedOnClickListener {
            clearRecordTripDialog() //删除该记录弹框
        }
        ViewClickEffectUtils.addClickScale(binding.tripDelete, CLICKED_SCALE_90)

        //toast显示
        viewModel.showToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        //删除成功关闭界面
        viewModel.finishPage.unPeek().observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) {
            if (it == BaseConstant.LOGIN_STATE_GUEST || it == BaseConstant.LOGIN_STATE_LOADING || it == BaseConstant.LOGIN_STATE_FAILED) {
                dismissCustomDialog()
            }
        }
    }

    private fun getMapPreviewRect(): Rect {
        val rect = Rect()
        val margin = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_24)
        rect.left =
            AutoGuideLineHelper.getCardWidthByGuideLine(binding.glVertical) + ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_552)
        rect.top = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_152) + margin
        rect.right = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_68) + 2 * margin
        rect.bottom = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_104) + margin
        return rect
    }

    //删除该记录弹框
    private fun clearRecordTripDialog() {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle(requireContext().getString(R.string.sv_setting_sure_clear_this_trip)).setContent(
            requireContext().getString(
                R.string.sv_setting_clear_no_trip_forever
            )
        )
            .doubleButton(
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            )
            .setOnClickListener {
                if (it) {
                    viewModel.deleteById(viewModel.getTrackItemBean()!!.id)
                }
            }.apply {
                show(this@MyTripDetailFragment.childFragmentManager, "clearRecordTripDialog")
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