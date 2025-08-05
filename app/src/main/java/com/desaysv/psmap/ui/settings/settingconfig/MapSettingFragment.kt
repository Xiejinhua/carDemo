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
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.common.CommonConfigValue
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.LayoutFontSizeBinding
import com.desaysv.psmap.databinding.LayoutSettingSwitchBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 地图设置界面
 */
@AndroidEntryPoint
class MapSettingFragment : Fragment() {
    private val viewModel: MapSettingViewModel by viewModels()
    private var isFirstOpen = true
    private lateinit var roadConditionSwitch: LayoutSettingSwitchBinding
    private lateinit var favorSwitch: LayoutSettingSwitchBinding
    private lateinit var scaleSwitch: LayoutSettingSwitchBinding
    private lateinit var bindingFontSize: LayoutFontSizeBinding

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        roadConditionSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context))
        favorSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context))
        scaleSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context))
        bindingFontSize = LayoutFontSizeBinding.inflate(LayoutInflater.from(context))
        return ComposeView(requireContext()).apply {
            setContent {
                val themeChange by skyBoxBusiness.themeChange().unPeek().observeAsState(
                    NightModeGlobal.isNightMode()
                )
                DsDefaultTheme(themeChange) {
                    MapSettingScreen(
                        Modifier,
                        viewModel,
                        roadConditionSwitch,
                        favorSwitch,
                        scaleSwitch,
                        bindingFontSize
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

        viewModel.onHiddenChanged.unPeek().observe(viewLifecycleOwner) {
            viewModel.getConfigKeyDayNightMode()   //获取日夜模式开关
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            skyBoxBusiness.updateView(roadConditionSwitch.root, true)
            skyBoxBusiness.updateView(favorSwitch.root, true)
            skyBoxBusiness.updateView(scaleSwitch.root, true)
            skyBoxBusiness.updateView(bindingFontSize.root, true)
        }

        viewModel.tmc.unPeek().observe(viewLifecycleOwner) {
            roadConditionSwitch.switchChange.contentDescription = if (it == CommonConfigValue.KEY_ROAT_OPEN) "实时路况关#实时路况关闭#关闭实时路况#实时路况" else "实时路况开#实时路况开启#开启实时路况#实时路况"
        }

        viewModel.favoriteChecked.unPeek().observe(viewLifecycleOwner) {
            favorSwitch.switchChange.contentDescription = if (it) "收藏点标注关#收藏点标注关闭#关闭收藏点标注#收藏点标注" else "收藏点标注开#收藏点标注开启#开启收藏点标注#收藏点标注"
        }

        viewModel.scaleChecked.unPeek().observe(viewLifecycleOwner) {
            scaleSwitch.switchChange.contentDescription = if (it) "自动比例尺关#自动比例尺关闭#关闭自动比例尺#自动比例尺" else "自动比例尺开#自动比例尺开启#开启自动比例尺#自动比例尺"
        }
    }

}