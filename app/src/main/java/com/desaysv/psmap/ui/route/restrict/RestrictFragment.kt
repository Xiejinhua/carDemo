package com.desaysv.psmap.ui.route.restrict

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentRouteRestrictBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.route.restrict.compose.RestrictComposeListView
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/11/20
 * @description
 */

@AndroidEntryPoint
class RestrictFragment : Fragment() {
    private lateinit var binding: FragmentRouteRestrictBinding
    private val viewModel by viewModels<RestrictViewModel>()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var gson: Gson

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("RestrictFragment onCreate() is called")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRouteRestrictBinding.inflate(inflater, container, false).apply {
            composeView.setContent {
                composeView()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    private fun initEventOperation() {
        //退出限行区域界面
        binding.ivClose.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)

        viewModel.restrictInfoDetails.observe(viewLifecycleOwner) { data ->
            data?.let {
                viewModel.setRouteRestrictBean(it)
            }
        }
    }

    @Composable
    private fun composeView() {
        val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())
        DsDefaultTheme(themeChange) {
            RestrictComposeListView(viewModel)
        }
    }
}