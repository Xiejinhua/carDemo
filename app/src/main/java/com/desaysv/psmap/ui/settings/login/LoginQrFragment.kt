package com.desaysv.psmap.ui.settings.login

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentLoginQrBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 二维码登录
 */
@AndroidEntryPoint
class LoginQrFragment : Fragment() {
    private lateinit var binding: FragmentLoginQrBinding
    private val viewModel: LoginQrViewModel by viewModels()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginQrBinding.inflate(inflater, container, false)
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
        viewModel.getQRImage()//获取二维码
    }

    private fun initEventOperation() {
        //刷新
        binding.qrRefresh.setDebouncedOnClickListener {
            viewModel.getQRImage()
        }
        ViewClickEffectUtils.addClickScale(binding.qrRefresh, CLICKED_SCALE_93)

        //打开协议
//        binding.bind.setDebouncedOnClickListener {
//            viewModel.isOpenBind.postValue(true)
//        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            skyBoxBusiness.updateView(binding.root, true)
            if (viewModel != null) {
                viewModel.isNight.postValue(it)
            }
        }

        viewModel.showLoginLayout.unPeek().observe(viewLifecycleOwner) {
            Timber.i("showLoginLayout: $it")
        }
    }
}