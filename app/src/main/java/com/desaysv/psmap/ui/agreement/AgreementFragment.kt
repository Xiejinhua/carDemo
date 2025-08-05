package com.desaysv.psmap.ui.agreement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autosdk.common.AutoStatus
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.databinding.FragmentAgreementBinding
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * @author 王漫生
 * @description 高德鲸图界面
 */
@AndroidEntryPoint
class AgreementFragment : Fragment() {
    private lateinit var binding: FragmentAgreementBinding
    private val viewModel by viewModels<AgreementViewModel>()

    private var callback: OnBackPressedCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAgreementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        onBackPressedCallback();//系统返回键监听
        initEventOperation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        callback = null
    }


    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun initEventOperation() {
        // 同意按钮
        binding.agreementConfirm.setDebouncedOnClickListener {
            viewModel.doAgreementYes(viewModel.checkState.value ?: false)
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.agreementConfirm, CLICKED_SCALE_95)

        // 退出按钮
        binding.agreementCancel.setDebouncedOnClickListener {
            requireActivity().finish()
            AutoStatusAdapter.sendStatus(AutoStatus.NOT_AGREE_AGREEMENT)
        }

        //服务条款
        binding.agreementService.setDebouncedOnClickListener {
            findNavController().navigate(
                R.id.to_agreementDetailFragment,
                Bundle().apply {
                    putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 0)
                }
            )
        }

        //隐私权政策
        binding.agreementPolicy.setDebouncedOnClickListener {
            findNavController().navigate(
                R.id.to_agreementDetailFragment,
                Bundle().apply {
                    putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 1)
                }
            )
        }

        //高德账号服务个人信息处理规则
        binding.agreementAccount.setDebouncedOnClickListener {
            findNavController().navigate(
                R.id.to_agreementDetailFragment,
                Bundle().apply {
                    putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 2)
                }
            )
        }

        binding.cbNoMoreTips.setDebouncedOnClickListener {
            val checkState = viewModel.checkState.value ?: false
            viewModel.setCheckState(!checkState)
        }
        binding.noMoreTips.setDebouncedOnClickListener {
            val checkState = viewModel.checkState.value ?: false
            viewModel.setCheckState(!checkState)
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

}