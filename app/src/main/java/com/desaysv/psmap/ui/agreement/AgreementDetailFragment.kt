package com.desaysv.psmap.ui.agreement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.databinding.FragmentAgreementDetailBinding
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * @author 王漫生
 * @description 高德协议界面
 */
@AndroidEntryPoint
class AgreementDetailFragment : Fragment() {
    private lateinit var binding: FragmentAgreementDetailBinding
    private var type = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAgreementDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        arguments?.run {
            type = getInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 0)
            binding.type = type
        }
    }

    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)
    }
}