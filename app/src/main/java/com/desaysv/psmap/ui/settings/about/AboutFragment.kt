package com.desaysv.psmap.ui.settings.about

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.databinding.FragmentAboutBinding
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.settings.view.IcpPopWindow
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 关于界面
 */
@AndroidEntryPoint
class AboutFragment : Fragment() {
    private lateinit var binding: FragmentAboutBinding
    private val viewModel: AboutViewModel by viewModels()

    @Inject
    lateinit var icpPopWindow: IcpPopWindow

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        icpPopWindow.cancelIcpPop()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
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

        viewModel.requestMapNum()//请求审图号
        viewModel.setShowMoreInfo(MoreInfoBean())
    }

    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        //进入帮助界面
        binding.help.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_helpFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.help, CLICKED_SCALE_90)

        //高德服务条款
        binding.termsService.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_agreementDetailFragment, Bundle().apply { putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 0) })
        }
        ViewClickEffectUtils.addClickScale(binding.termsService, CLICKED_SCALE_95)

        //隐私协议
        binding.privacyPolicy.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_agreementDetailFragment, Bundle().apply { putInt(Biz.KEY_BIZ_AGREEMENT_TYPE, 1) })
        }
        ViewClickEffectUtils.addClickScale(binding.privacyPolicy, CLICKED_SCALE_95)

        binding.icpTitle.setDebouncedOnClickListener {
            viewModel.setShowMoreInfo(MoreInfoBean(BaseConstant.ICP_LINK, BaseConstant.ICP_LINK))
        }
        binding.icp.setDebouncedOnClickListener {
            viewModel.setShowMoreInfo(MoreInfoBean(BaseConstant.ICP_LINK, BaseConstant.ICP_LINK))
        }

        //判断是否显示提示文言
        viewModel.showMoreInfo.observe(viewLifecycleOwner) {
            Timber.i("showMoreInfo moreInfo:${it.content}")
            if (!TextUtils.isEmpty(it.content)) {
                icpPopWindow.showIcpPop(it)
            } else {
                icpPopWindow.cancelIcpPop()
            }
        }
    }

}