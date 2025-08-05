package com.desaysv.psmap.ui.settings.settingconfig

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.LayoutSettingSwitchBinding
import com.desaysv.psmap.databinding.LayoutTtsModeBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 地图设置界面
 */
@AndroidEntryPoint
class NaviBroadcastFragment : Fragment() {
    private val viewModel: NaviBroadcastViewModel by viewModels()
    private var isFirstOpen = true
    private lateinit var bindingSwitch: LayoutSettingSwitchBinding
    private lateinit var bindingAhaScenicSwitch: LayoutSettingSwitchBinding
    private lateinit var bindingTtsMode: LayoutTtsModeBinding

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bindingSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context))
        bindingAhaScenicSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context))
        bindingTtsMode = LayoutTtsModeBinding.inflate(LayoutInflater.from(context))
        return ComposeView(requireContext()).apply {
            setContent {
                val themeChange by skyBoxBusiness.themeChange().unPeek().observeAsState(
                    NightModeGlobal.isNightMode()
                )
                DsDefaultTheme(themeChange) {
                    NaviBroadcastScreen(
                        Modifier,
                        viewModel,
                        onCardClick = {
                            findNavController().navigate(R.id.to_voiceDataFragment)
                        },
                        bindingSwitch,
                        bindingAhaScenicSwitch,
                        bindingTtsMode
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUseVoice()
    }

    private fun initBinding() {
        lifecycleScope.launch {
            viewModel.setNavigationSettingData()//设置导航设置数据
        }
    }

    private fun initEventOperation() {
        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { integer ->
            if (integer == BaseConstant.LOGIN_STATE_GUEST || integer == BaseConstant.LOGIN_STATE_SUCCESS) {
                if (isFirstOpen) {
                    isFirstOpen = false
                } else {
                    viewModel.setNavigationSettingData()
                }
            }
        }

        //toast显示
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            skyBoxBusiness.updateView(bindingSwitch.root, true)
            skyBoxBusiness.updateView(bindingAhaScenicSwitch.root, true)
            skyBoxBusiness.updateView(bindingTtsMode.root, true)
        }

        viewModel.cruiseBroadcast.unPeek().observe(viewLifecycleOwner) {
            bindingSwitch.switchChange.contentDescription = if (it) "巡航后台播报关#巡航后台播报关闭#关闭巡航后台播报#巡航后台播报" else "巡航后台播报开#巡航后台播报开启#开启巡航后台播报#巡航后台播报#巡航后台播报"
        }

        viewModel.ahaScenicBroadcast.unPeek().observe(viewLifecycleOwner) {
            bindingAhaScenicSwitch.switchChange.contentDescription = if (it) "巡航景点推荐关#巡航景点推荐关闭#关闭巡航景点推荐#巡航景点推荐" else "巡航景点推荐开#巡航景点推荐开启#开启巡航景点推荐#巡航景点推荐"
        }

        viewModel.returnAllSetting.unPeek().observe(viewLifecycleOwner) {
            viewModel.getUseVoice()
        }
    }
}