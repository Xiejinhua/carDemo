package com.desaysv.psmap.ui.route.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.FragmentWeatherDetailsBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/12/25
 * @description 沿途天气详情卡片
 */
@AndroidEntryPoint
class WeatherDetailsFragment : Fragment() {
    private lateinit var binding: FragmentWeatherDetailsBinding
    private val viewModel by viewModels<WeatherDetailsViewModel>()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        initEventOperation()
    }

    private fun initEventOperation() {


        //关闭按钮
        binding.ivClose.setDebouncedOnClickListener { findNavController().navigateUp() }
        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }

    }

}