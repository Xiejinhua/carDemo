package com.desaysv.psmap.ui.settings.settingconfig

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.widget.route.constant.ConfigRoutePreference
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.CommonConfigValue
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.databinding.LayoutNaviPreviewBinding
import com.desaysv.psmap.databinding.LayoutSettingSwitchBinding
import com.desaysv.psmap.databinding.LayoutTtsModeBinding
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.ui.compose.CustomButton
import com.desaysv.psmap.ui.compose.CustomImageTextButton
import com.desaysv.psmap.ui.compose.CustomNaviDefaultPreference
import com.desaysv.psmap.ui.compose.CustomNaviRoutePreference
import com.desaysv.psmap.ui.compose.SettingLimitSwitch
import com.desaysv.psmap.ui.compose.SettingSwitch
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import timber.log.Timber

/**
 * 导航功能菜单Compose布局
 */
@Composable
fun NavigationFunctionMenuScreen(
    modifier: Modifier = Modifier,
    bindingSwitch: LayoutSettingSwitchBinding,
    scaleSwitch: LayoutSettingSwitchBinding,
    bindingTtsMode: LayoutTtsModeBinding,
    bindingNaviPreview: LayoutNaviPreviewBinding,
    viewModel: NavigationFunctionMenuViewModel = hiltViewModel<NavigationFunctionMenuViewModel>(),
    noNetUseLimit: (isCheck: Boolean) -> Unit,
    startSettingCar: () -> Unit,
    onDropBy: () -> Unit, //进入顺路搜
    onRouteRefresh: () -> Unit, //路线刷新
    onRouteShare: () -> Unit //行程分享
) {
    LazyColumn {
        item {
            NaviTopMenu(modifier, viewModel, onDropBy, onRouteRefresh, onRouteShare) //顶部实时路况布局
        }
        item {
            NaviRoutePreferenceMenu(modifier, viewModel) //路线偏好
        }
        item {
            SetTtsModeMenu(modifier, viewModel, bindingTtsMode) //播报模式
        }
        item {
            ChangeMapMenu(modifier, viewModel) //视角切换
        }
        item {
            ScaleSettingMenu(modifier, viewModel, scaleSwitch) //自动比例尺设置
        }
        item {
            SetLimitMenu(modifier, bindingSwitch, viewModel, noNetUseLimit, startSettingCar) //限行
        }
        item {
            SetAllPreviewMenu(modifier, viewModel, bindingNaviPreview) //全程路况预览
        }
    }

}

//顶部实时路况布局
@Composable
fun NaviTopMenu(
    modifier: Modifier = Modifier,
    viewModel: NavigationFunctionMenuViewModel,
    onDropBy: () -> Unit, //进入顺路搜
    onRouteRefresh: () -> Unit, //路线刷新
    onRouteShare: () -> Unit //行程分享
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (roadCondition, waypoints, reflash, share) = createRefs()
        val dpn10 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_n10)
        val dp16 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16)
        val dp160 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_160)
        val dp304 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_304)
        val tmc = viewModel.tmc.observeAsState()
        val isNight by viewModel.isNight.observeAsState()

        CustomImageTextButton(modifier = modifier
            .constrainAs(roadCondition) {
                start.linkTo(parent.start, dp16)
                top.linkTo(parent.top, dpn10)
            }, type = if (tmc.value == CommonConfigValue.KEY_ROAT_OPEN) "实时路况关#关闭实时路况#实时路况关闭#实时路况" else "实时路况开#开启实时路况#实时路况开启#实时路况",
            width = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176),
            height = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176),
            icon = if (isNight == true) {
                if (tmc.value == 1) R.drawable.ic_nav_tmc_select_night else R.drawable.ic_nav_tmc_night
            } else {
                if (tmc.value == 1) R.drawable.ic_nav_tmc_select_day else R.drawable.ic_nav_tmc_day
            },
            name = stringResource(R.string.sv_setting_road_condition),
            background = if (isNight == true) R.drawable.bg_navi_top_menu_night else R.drawable.bg_navi_top_menu_day,
            onClick = { //实时路况操作
                viewModel.setConfigKeyRoadEvent(if (tmc.value == 1) 0 else 1)
            }
        )
        CustomImageTextButton(modifier = modifier
            .constrainAs(waypoints) {
                start.linkTo(parent.start, dp160)
                top.linkTo(roadCondition.top)
            }, type = stringResource(R.string.sv_route_navi_drop_by_point),
            width = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176),
            height = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176),
            icon = if (isNight == true) R.drawable.ic_top_menu_search_night else R.drawable.ic_top_menu_search_day,
            name = stringResource(R.string.sv_route_navi_drop_by_point),
            background = if (isNight == true) R.drawable.bg_navi_top_menu_night else R.drawable.bg_navi_top_menu_day,
            onClick = {
                onDropBy() //顺路搜操作
            }
        )
        CustomImageTextButton(modifier = modifier
            .constrainAs(reflash) {
                start.linkTo(parent.start, dp304)
                top.linkTo(waypoints.top)
            }, type = stringResource(com.autosdk.R.string.qrcode_text_reflash),
            width = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176),
            height = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176),
            icon = if (isNight == true) R.drawable.ic_top_menu_refresh_night else R.drawable.ic_top_menu_refresh_day,
            name = stringResource(com.autosdk.R.string.qrcode_text_reflash),
            background = if (isNight == true) R.drawable.bg_navi_top_menu_night else R.drawable.bg_navi_top_menu_day,
            onClick = {
                onRouteRefresh() //刷新操作
            }
        )
        CustomImageTextButton(modifier = modifier
            .constrainAs(share) {
                end.linkTo(parent.end, dp16)
                top.linkTo(reflash.top)
            }, type = stringResource(com.autosdk.R.string.routecarresult_text_route_share),
            width = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176),
            height = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176),
            icon = if (isNight == true) R.drawable.ic_route_share_night else R.drawable.ic_route_share_day,
            name = stringResource(com.autosdk.R.string.routecarresult_text_route_share),
            background = if (isNight == true) R.drawable.bg_navi_top_menu_night else R.drawable.bg_navi_top_menu_day,
            onClick = {
                onRouteShare() //行程分享操作
            }
        )
    }
}

//路线偏好
@Composable
fun NaviRoutePreferenceMenu(
    modifier: Modifier = Modifier,
    viewModel: NavigationFunctionMenuViewModel
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (ctvStrategyDefault, ctvStrategyMoney, ctvStrategyFreewayYes, ctvStrategyTmc, ctvStrategyFreewayNo, ctvStrategyQuick, ctvStrategyBig) = createRefs()
        val dp16 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16)
        val dp20 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_20)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val rsDefaultSelected = viewModel.rsDefaultSelected.observeAsState()
        val rsMoneySelected = viewModel.rsMoneySelected.observeAsState()
        val rsFreewayYesSelected = viewModel.rsFreewayYesSelected.observeAsState()
        val rsTmcSelected = viewModel.rsTmcSelected.observeAsState()
        val rsFreewayNoSelected = viewModel.rsFreewayNoSelected.observeAsState()
        val rsFreewayQuickSelected = viewModel.rsFreewayQuickSelected.observeAsState()
        val rsFreewayBigSelected = viewModel.rsFreewayBigSelected.observeAsState()
        val hasNetworkConnected by viewModel.isNetworkConnected.observeAsState()
        val isNight by viewModel.isNight.observeAsState()

        CustomNaviDefaultPreference(modifier = modifier
            .constrainAs(ctvStrategyDefault) {
                start.linkTo(parent.start, dp40)
                top.linkTo(parent.top, dp20)
            }, type = stringResource(R.string.sv_route_navi_gaode_recommends),
            isSelect = rsDefaultSelected.value == true,
            isNight = isNight == true,
            icon = if (isNight == true) R.drawable.ic_route_remommend_icon_night else {if (rsDefaultSelected.value == true) R.drawable.ic_route_remommend_icon_select_day else R.drawable.ic_route_remommend_icon_day},
            name = stringResource(R.string.sv_route_navi_gaode_recommends),
            onClick = {//智能推荐操作
                viewModel.preferSelect(
                    ConfigRoutePreference.PREFERENCE_DEFAULT,
                    rsDefaultSelected.value ?: false
                )
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(ctvStrategyTmc) {
                start.linkTo(ctvStrategyDefault.start)
                top.linkTo(ctvStrategyDefault.bottom, dp16)
            }, type = stringResource(R.string.sv_route_navi_avoid_congestion),
            isSelect = rsTmcSelected.value == true,
            isNight = isNight == true,
            hasNetworkConnected = hasNetworkConnected == true,
            icon = if (isNight == true) R.drawable.ic_route_avoid_congestion_svg_icon_night else {
                if (rsTmcSelected.value == true) R.drawable.ic_route_avoid_congestion_svg_icon_select_day else R.drawable.ic_route_avoid_congestion_svg_icon_day
            },
            name = stringResource(R.string.sv_route_navi_avoid_congestion),
            onClick = { //躲避拥堵操作
                viewModel.preferSelect(
                    ConfigRoutePreference.PREFERENCE_AVOID_JAN,
                    rsTmcSelected.value ?: false
                )
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(ctvStrategyFreewayYes) {
                start.linkTo(ctvStrategyTmc.end, dp16)
                top.linkTo(ctvStrategyTmc.top)
            }, type = stringResource(R.string.sv_route_navi_sing_highway),
            isSelect = rsFreewayYesSelected.value == true,
            isNight = isNight == true,
            hasNetworkConnected = true,
            icon = if (isNight == true) R.drawable.ic_route_high_speed_svg_icon_night
            else {
                if (rsFreewayYesSelected.value == true) R.drawable.ic_route_high_speed_svg_icon_select_day else R.drawable.ic_route_high_speed_svg_icon_day
            },
            name = stringResource(R.string.sv_route_navi_sing_highway),
            onClick = { //高速优先操作
                viewModel.preferSelect(
                    ConfigRoutePreference.PREFERENCE_USING_HIGHWAY,
                    rsFreewayYesSelected.value ?: false
                )
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(ctvStrategyFreewayNo) {
                start.linkTo(ctvStrategyFreewayYes.end, dp16)
                top.linkTo(ctvStrategyFreewayYes.top)
            }, type = stringResource(R.string.sv_route_navi_avoid_highway),
            isSelect = rsFreewayNoSelected.value == true,
            isNight = isNight == true,
            hasNetworkConnected = true,
            icon = if (isNight == true) R.drawable.ic_route_nohigh_speed_svg_icon_night
            else {
                if (rsFreewayNoSelected.value == true) R.drawable.ic_route_nohigh_speed_svg_icon_select_day else R.drawable.ic_route_nohigh_speed_svg_icon_day
            },
            name = stringResource(R.string.sv_route_navi_avoid_highway),
            onClick = { //不走高速操作
                viewModel.preferSelect(
                    ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY,
                    rsFreewayNoSelected.value ?: false
                )
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(ctvStrategyMoney) {
                start.linkTo(ctvStrategyTmc.start)
                top.linkTo(ctvStrategyTmc.bottom, dp16)
            }, type = stringResource(R.string.sv_route_navi_charge_less),
            isSelect = rsMoneySelected.value == true,
            isNight = isNight == true,
            hasNetworkConnected = true,
            icon = if (isNight == true) R.drawable.ic_route_avoid_charging_svg_icon_night
            else {
                if (rsMoneySelected.value == true) R.drawable.ic_route_avoid_charging_svg_icon_select_day else R.drawable.ic_route_avoid_charging_svg_icon_day
            },
            name = stringResource(R.string.sv_route_navi_charge_less),
            onClick = { //少收费操作
                viewModel.preferSelect(
                    ConfigRoutePreference.PREFERENCE_AVOID_CHARGE,
                    rsMoneySelected.value ?: false
                )
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(ctvStrategyBig) {
                start.linkTo(ctvStrategyMoney.end, dp16)
                top.linkTo(ctvStrategyMoney.top)
            }, type = stringResource(R.string.sv_route_navi_sing_route_width),
            isSelect = rsFreewayBigSelected.value == true,
            isNight = isNight == true,
            hasNetworkConnected = hasNetworkConnected == true,
            icon = if (isNight == true) R.drawable.ic_route_width_first_icon_svg_night
            else {
                if (rsFreewayBigSelected.value == true) R.drawable.ic_route_width_first_svg_icon_select_day else R.drawable.ic_route_width_first_svg_icon_day
            },
            name = stringResource(R.string.sv_route_navi_sing_route_width),
            onClick = { //大路优先操作
                viewModel.preferSelect(
                    ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST,
                    rsFreewayBigSelected.value ?: false
                )
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(ctvStrategyQuick) {
                start.linkTo(ctvStrategyBig.end, dp16)
                top.linkTo(ctvStrategyBig.top)
            }, type = stringResource(R.string.sv_route_navi_sing_speed_first),
            isSelect = rsFreewayQuickSelected.value == true,
            isNight = isNight == true,
            hasNetworkConnected = hasNetworkConnected == true,
            icon = if (isNight == true) R.drawable.ic_route_speed_first_svg_icon_night
            else {
                if (rsFreewayQuickSelected.value == true) R.drawable.ic_route_speed_first_svg_icon_select_day else R.drawable.ic_route_speed_first_svg_icon_day
            },
            name = stringResource(R.string.sv_route_navi_sing_speed_first),
            onClick = { //速度最快操作
                viewModel.preferSelect(
                    ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST,
                    rsFreewayQuickSelected.value ?: false
                )
            }
        )
    }
}

//播报模式
@Composable
fun SetTtsModeMenu(
    modifier: Modifier = Modifier,
    viewModel: NavigationFunctionMenuViewModel,
    bindingTtsMode: LayoutTtsModeBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (naviSetTtsMode, moreInfo, broadcastLayout) = createRefs()
        val context = LocalContext.current
        val dp10 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_10)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp48 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_48)
        val dp57 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_57)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val volumeModel = viewModel.volumeModel.observeAsState()
        val isNight by viewModel.isNight.observeAsState()
        val tag = "layoutTtsModeBindingMenu"
        var lastCheckedId = viewModel.lastCheckedId.observeAsState()
        var lastTargetX = viewModel.lastTargetX.observeAsState()

        Text(
            text = stringResource(id = R.string.sv_setting_navi_tts_mode),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night else com.desaysv.psmap.model.R.color.customColorRbBgDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
            modifier = modifier
                .constrainAs(naviSetTtsMode) {
                    start.linkTo(parent.start, dp40)
                    top.linkTo(parent.top, dp57)
                }
        )
        Image(painter = painterResource(id = if (isNight == true) R.drawable.ic_more_info_tip_night else R.drawable.ic_more_info_tip_day),
            contentDescription = "", modifier = modifier
                .size(dp64)
                .constrainAs(moreInfo) {
                    top.linkTo(parent.top, dp48)
                    end.linkTo(parent.end, dp40)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.setShowMoreInfo(MoreInfoBean(context.getString(R.string.sv_setting_navi_tts_mode), context.getString(if (volumeModel.value == SettingConst.BROADCAST_DETAIL) R.string.sv_setting_broadcast_detail else if (volumeModel.value == SettingConst.BROADCAST_EASY) R.string.sv_setting_broadcast_simple else R.string.sv_setting_broadcast_minimalism)))
                    }
                )
        )
        AndroidView(
            factory = {
                // 使用findViewWithTag来查找视图
                val viewToRemove = bindingTtsMode.root.findViewWithTag<View>(tag)
                // 检查视图是否存在，并移除它
                if (viewToRemove != null) {
                    // 获取视图的父视图
                    val parent = viewToRemove.parent
                    if (parent != null) {
                        try {
                            (parent as ViewGroup).removeView(viewToRemove)
                            Timber.e(" removeView ");
                        } catch (e: Exception) {
                            Timber.e("catch Exception:${e.message}");
                        }
                    }
                }
                bindingTtsMode.root.tag = tag
                bindingTtsMode.root
            },
            update = {
                bindingTtsMode.selectTab = volumeModel.value
                bindingTtsMode.isNight = isNight == true
                Timber.d(" initBinding selectTab:%s", volumeModel.value)
                when (volumeModel.value) {
                    SettingConst.BROADCAST_DETAIL -> { //设置播报模式 详细播报
                        bindingTtsMode.indicator.animate()
                            .x(0f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                    }

                    SettingConst.BROADCAST_EASY -> { //设置播报模式 简单播报
                        bindingTtsMode.indicator.animate()
                            .x(187f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                    }

                    SettingConst.BROADCAST_MINIMALISM -> { //设置播报模式 极简播报
                        bindingTtsMode.indicator.animate()
                            .x(374f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                    }
                }

                bindingTtsMode.broadcastTab.setOnCheckedChangeListener { group, checkedId ->
                    val checkedButton: RadioButton = group.findViewById(checkedId)
                    // 计算指示条应该移动到的位置
                    val targetX: Int = checkedButton.left + (checkedButton.width - bindingTtsMode.indicator.width) / 2
                    // 判断动画持续时间
                    val duration = if (lastCheckedId.value != null && areAdjacentMenu(lastCheckedId.value!!, checkedId)) {
                        200 // 相邻的 RadioButton
                    } else {
                        300 // 非相邻的 RadioButton
                    }
                    Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                    // 创建平移动画
                    if (targetX == 0 && checkedId != R.id.broadcastDetail){
                        bindingTtsMode.indicator.animate()
                            .x(lastTargetX.value?.toFloat() ?: 0f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                    } else {
                        bindingTtsMode.indicator.animate()
                            .x(targetX.toFloat())
                            .setDuration(duration.toLong())
                            .setInterpolator(FastOutSlowInInterpolator())
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    viewModel.lastCheckedId.postValue(checkedId) // 更新上一个选中的 ID
                                    viewModel.lastTargetX.postValue(targetX)
                                    when (checkedId) {
                                        R.id.broadcastDetail -> { //设置播报模式 详细播报
                                            viewModel.setupVolumeModel(SettingConst.BROADCAST_DETAIL)
                                        }

                                        R.id.broadcastSimple -> { //设置播报模式 简单播报
                                            viewModel.setupVolumeModel(SettingConst.BROADCAST_EASY)
                                        }

                                        R.id.broadcastMin -> { //设置播报模式 极简播报
                                            viewModel.setupVolumeModel(SettingConst.BROADCAST_MINIMALISM)
                                        }
                                    }
                                }
                            })
                            .start()
                    }
                }
            },
            modifier = modifier.constrainAs(broadcastLayout) {
                start.linkTo(naviSetTtsMode.start)
                top.linkTo(naviSetTtsMode.bottom, dp10)
            }
        )
    }
}

// 判断两个 RadioButton 是否相邻
private fun areAdjacentMenu(lastId: Int, currentId: Int): Boolean {
    return when {
        (lastId == R.id.broadcastDetail && currentId == R.id.broadcastSimple) ||
                (lastId == R.id.broadcastSimple && currentId == R.id.broadcastDetail) -> true
        (lastId == R.id.broadcastSimple && currentId == R.id.broadcastMin) ||
                (lastId == R.id.broadcastMin && currentId == R.id.broadcastSimple) -> true
        else -> false
    }
}

//限行
@Composable
fun SetLimitMenu(
    modifier: Modifier = Modifier,
    bindingSwitch: LayoutSettingSwitchBinding,
    viewModel: NavigationFunctionMenuViewModel,
    noNetUseLimit: (isCheck: Boolean) -> Unit,
    startSettingCar: () -> Unit
) {
    val dp2 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_2)
    val dp3 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_3)
    val dp4 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_4)
    val dp5 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_5)
    val dp6 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_6)
    val dp8 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_8)
    val dp10 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_10)
    val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
    val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
    val dp65 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_65)
    val dp80 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_80)
    ConstraintLayout(modifier = modifier
        .padding(top = dp65)
        .fillMaxWidth()
        .height(dp80)) {
        val (title, vehicleNumBg, switchLimit) = createRefs()
        val context = LocalContext.current
        val vehicleNum by viewModel.vehicleNum.observeAsState()
        val limitChecked by viewModel.limitChecked.observeAsState()
        val isNight by viewModel.isNight.observeAsState()
        val loginLoading by viewModel.loginLoading.observeAsState()
        val showOrUpdateData by viewModel.showData.observeAsState()
        val hasNetworkConnected by viewModel.isNetworkConnected.observeAsState()
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = tween(
                durationMillis = 200,
                easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
            ), label = ""
        )
        LaunchedEffect(loginLoading) {
            Timber.d(" setProvince showData")
            viewModel.getVehicleNumberLimit() //获取限行数据
        }
        LaunchedEffect(showOrUpdateData) {
            Timber.d(" setProvince showData")
            viewModel.getVehicleNumberLimit() //获取限行数据
        }

        SettingLimitSwitch(
            modifier
                .constrainAs(switchLimit) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start, dp40)
                },
            vehicleNumber = vehicleNum ?: "",
            isNetworkConnected = hasNetworkConnected == true,
            check = limitChecked == true,
            isNight = isNight == true,
            binding = bindingSwitch,
            setOnCheckedChangeListener = {
                when (it) {
                    1 -> {
                        Timber.d(" isNetworkConnected limitChecked:$it")
                        viewModel.setToast.postValue("网络状态不佳，无法使用避开限行功能")
                        noNetUseLimit(bindingSwitch.switchChange.isChecked)
                    }

                    2 -> {
                        Timber.d(" 跳转到系统设置车牌号编辑界面 limitChecked:$it")
                        startSettingCar()
                    }

                    else -> {
                        viewModel.limitOperation(it == 3)
                    }
                }
            }
        )

        Text(
            text = stringResource(id = R.string.sv_setting_my_car_number_limit),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_34).sp,
            modifier = modifier
                .constrainAs(title) {
                    start.linkTo(switchLimit.end, dp32)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }.clearAndSetSemantics {  }
        )

        if (!TextUtils.isEmpty(vehicleNum)) {
            Box(modifier = modifier
                .scale(scale)
                .background(color = colorResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorCarBgBlueNight else com.desaysv.psmap.model.R.color.customColorCarBgBlueDay), shape = RoundedCornerShape(dp8))
                .alpha(if (limitChecked == true) 1.0f else 0.4f)
                .constrainAs(vehicleNumBg) {
                    end.linkTo(parent.end, dp40)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { //跳转到系统设置车牌号编辑界面
                        startSettingCar()
                    }
                )) {
                Text(
                    text = if (TextUtils.isEmpty(vehicleNum) || vehicleNum?.length!! < 3) "" else vehicleNum?.substring(0, 2) + "·" + vehicleNum?.substring(2),
                    color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.primaryDay),
                    fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_36).sp,
                    modifier = modifier
                        .padding(dp4)
                        .border(
                            width = dp2,
                            color = colorResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorBtnVehicleNumBorderNight else com.desaysv.psmap.model.R.color.primaryDay),
                            shape = RoundedCornerShape(dp6)
                        )
                        .alpha(if (limitChecked == true) 1.0f else 0.4f)
                        .padding(horizontal = dp10)
                        .padding(top = dp3, bottom = dp5)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { //跳转到系统设置车牌号编辑界面
                                startSettingCar()
                            }
                        )
                )
            }
        }
    }
}

//视角切换
@Composable
fun ChangeMapMenu(
    modifier: Modifier = Modifier,
    viewModel: NavigationFunctionMenuViewModel
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (settingCruise, naviView2d, naviView2dNorth, naviView3d) = createRefs()
        val context = LocalContext.current
        val dp16 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16)
        val dp24 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp57 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_57)
        val dp120 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_120)
        val dp176 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176)
        val viewOfMap = viewModel.viewOfMap.observeAsState()
        val isNight by viewModel.isNight.observeAsState()

        Text(
            text = stringResource(id = R.string.sv_setting_navi_view),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night else com.desaysv.psmap.model.R.color.customColorRbBgDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
            modifier = modifier
                .constrainAs(settingCruise) {
                    start.linkTo(parent.start, dp40)
                    top.linkTo(parent.top, dp57)
                }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(naviView2d) {
                top.linkTo(settingCruise.bottom, dp24)
                start.linkTo(settingCruise.start)
            }, type = stringResource(R.string.sv_setting_navi_view_2d),
            width = dp176,
            height = dp120,
            isSelect = viewOfMap.value == 0,
            isNight = isNight == true,
            icon = if (isNight == true) R.drawable.ic_car_mode_2d_night else {
                if (viewOfMap.value == 0) R.drawable.ic_car_mode_2d_select_day else R.drawable.ic_car_mode_2d_day
            },
            name = stringResource(R.string.sv_setting_navi_view_2d),
            onClick = { //设置地图视角 2D车头朝上
                viewModel.toSetupViewModel(0)
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(naviView2dNorth) {
                bottom.linkTo(naviView2d.bottom)
                start.linkTo(naviView2d.end, dp16)
            }, type = stringResource(R.string.sv_setting_navi_view_2d_north),
            width = dp176,
            height = dp120,
            isSelect = viewOfMap.value == 2,
            isNight = isNight == true,
            icon = if (isNight == true) R.drawable.ic_car_mode_north_night else {
                if (viewOfMap.value == 2) R.drawable.ic_car_mode_north_select_day else R.drawable.ic_car_mode_north_day
            },
            name = stringResource(R.string.sv_setting_navi_view_2d_north),
            onClick = { //设置地图视角 2D北朝上
                viewModel.toSetupViewModel(2)
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(naviView3d) {
                bottom.linkTo(naviView2dNorth.bottom)
                start.linkTo(naviView2dNorth.end, dp16)
            }, type = stringResource(R.string.sv_setting_navi_view_3d),
            width = dp176,
            height = dp120,
            isSelect = viewOfMap.value == 1,
            isNight = isNight == true,
            icon = if (isNight == true) R.drawable.ic_car_mode_3d_night else {
                if (viewOfMap.value == 1) R.drawable.ic_car_mode_3d_select_day else R.drawable.ic_car_mode_3d_day
            },
            name = stringResource(R.string.sv_setting_navi_view_3d),
            onClick = { //设置地图视角 3D车头朝上
                viewModel.toSetupViewModel(1)
            }
        )
    }
}

//全程路况预览
@Composable
fun SetAllPreviewMenu(
    modifier: Modifier = Modifier,
    viewModel: NavigationFunctionMenuViewModel,
    bindingNaviPreview: LayoutNaviPreviewBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (naviSetAllPreview, previewTab, sampleMap, smallMap, roadCondition) = createRefs()
        val context = LocalContext.current
        val dp16 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16)
        val dp28 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_28)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp41 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_41)
        val dp48 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_48)
        val dp150 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_150)
        val dp272 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_272)
        val tabSelect = viewModel.previewSelectTab.observeAsState()
        val mapType = viewModel.mapType.observeAsState()
        val roadConditionInteractionSource = remember { MutableInteractionSource() }
        val smallMapInteractionSource = remember { MutableInteractionSource() }
        val roadConditionPressed = roadConditionInteractionSource.collectIsPressedAsState()
        val smallMapPressed = smallMapInteractionSource.collectIsPressedAsState()
        val isNight by viewModel.isNight.observeAsState()
        val previewLastTargetX = viewModel.previewLastTargetX.observeAsState()
        val tag = "layoutNaviPreviewBinding"

        LaunchedEffect(mapType.value) {
            viewModel.previewSelectTab.postValue(mapType.value == 2)
        }

        Timber.i("tabSelect:${tabSelect.value}")
        Text(
            text = stringResource(id = R.string.sv_setting_navi_all_preview),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night else com.desaysv.psmap.model.R.color.customColorRbBgDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
            modifier = modifier
                .constrainAs(naviSetAllPreview) {
                    start.linkTo(parent.start, dp40)
                    top.linkTo(parent.top, dp48)
                }
        )
        if (tabSelect.value != null){
            AndroidView(
                factory = {
                    // 使用findViewWithTag来查找视图
                    val viewToRemove = bindingNaviPreview.root.findViewWithTag<View>(tag)
                    // 检查视图是否存在，并移除它
                    if (viewToRemove != null) {
                        // 获取视图的父视图
                        val parent = viewToRemove.parent
                        if (parent != null) {
                            try {
                                (parent as ViewGroup).removeView(viewToRemove)
                                Timber.e(" removeView ");
                            } catch (e: Exception) {
                                Timber.e("catch Exception:${e.message}");
                            }
                        }
                    }
                    bindingNaviPreview.root.tag = tag
                    bindingNaviPreview.root
                },
                update = {
                    bindingNaviPreview.selectTab = tabSelect.value == true
                    bindingNaviPreview.isNight = isNight == true
                    Timber.d(" initBinding selectTab:%s", tabSelect.value)
                    when (tabSelect.value) {
                        true -> {
                            viewModel.previewLastTargetX.postValue(0)
                            bindingNaviPreview.indicator.animate()
                                .x(0f)
                                .setDuration(0)
                                .setInterpolator(FastOutSlowInInterpolator())
                                .start()
                            Timber.i("layoutTab 0 lastTargetX:${previewLastTargetX.value}")
                        }

                        else -> {
                            viewModel.previewLastTargetX.postValue(140)
                            bindingNaviPreview.indicator.animate()
                                .x(140f)
                                .setDuration(0)
                                .setInterpolator(FastOutSlowInInterpolator())
                                .start()
                            Timber.i("layoutTab 140 lastTargetX:${previewLastTargetX.value}")
                        }
                    }

                    bindingNaviPreview.naviPreviewRg.setOnCheckedChangeListener { group, checkedId ->
                        val checkedButton: RadioButton = group.findViewById(checkedId)
                        // 计算指示条应该移动到的位置
                        val targetX: Int = checkedButton.left + (checkedButton.width - bindingNaviPreview.indicator.width) / 2
                        // 判断动画持续时间
                        val duration = 200
                        Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:${previewLastTargetX.value}")
                        // 创建平移动画
                        if (targetX == 0 && checkedId != R.id.samplePreview){
                            bindingNaviPreview.indicator.animate()
                                .x(previewLastTargetX.value?.toFloat() ?: 0f)
                                .setDuration(0)
                                .setInterpolator(FastOutSlowInInterpolator())
                                .start()
                        } else {
                            bindingNaviPreview.indicator.animate()
                                .x(targetX.toFloat())
                                .setDuration(duration.toLong())
                                .setInterpolator(FastOutSlowInInterpolator())
                                .setListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(animation: Animator) {
                                        viewModel.previewLastTargetX.postValue(targetX)
                                        when (checkedId) {
                                            R.id.samplePreview -> { //极简
                                                viewModel.previewSelectTab.postValue(true)
                                            }

                                            R.id.classicPreview -> { //经典
                                                viewModel.previewSelectTab.postValue(false)
                                            }
                                        }
                                    }
                                })
                                .start()
                        }
                    }
                },
                modifier = modifier.constrainAs(previewTab) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(naviSetAllPreview.bottom, dp28)
                }
            )

            if (tabSelect.value == true) {
                Image(painter = painterResource(
                    id = if (NightModeGlobal.isNightMode()) {
                        if (mapType.value == 2) R.drawable.ic_map_sample_selected_night else R.drawable.ic_map_sample_night
                    } else {
                        if (mapType.value == 2) R.drawable.ic_map_sample_selected_day else R.drawable.ic_map_sample_day
                    }
                ),
                    contentDescription = "极简",
                    modifier = modifier
                        .constrainAs(sampleMap) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(previewTab.bottom, dp41)
                        }
                        .size(dp272, dp150)
                        .alpha(if (smallMapPressed.value) 0.8f else 1.0f)
                        .clickable(
                            interactionSource = smallMapInteractionSource,
                            indication = null,
                            onClick = { //设置全程路况概况 0 小地图  1 光柱图 2 极简
                                viewModel.setupMapType(2)
                            }
                        ).clearAndSetSemantics {  }
                )
            } else {
                Image(painter = painterResource(
                    id = if (NightModeGlobal.isNightMode()) {
                        if (mapType.value == 0) R.drawable.ic_map_small_selected_night else R.drawable.ic_map_small_night
                    } else {
                        if (mapType.value == 0) R.drawable.ic_map_small_selected_day else R.drawable.ic_map_small_day
                    }
                ),
                    contentDescription = "小地图",
                    modifier = modifier
                        .constrainAs(smallMap) {
                            start.linkTo(parent.start)
                            end.linkTo(roadCondition.start)
                            top.linkTo(previewTab.bottom, dp41)
                        }
                        .size(dp272, dp150)
                        .alpha(if (smallMapPressed.value) 0.8f else 1.0f)
                        .clickable(
                            interactionSource = smallMapInteractionSource,
                            indication = null,
                            onClick = { //设置全程路况概况 0 小地图  1 光柱图 2 极简
                                viewModel.setupMapType(0)
                            }
                        )
                )
                Image(painter = painterResource(
                    id = if (NightModeGlobal.isNightMode()) {
                        if (mapType.value == 1) R.drawable.ic_map_pillar_selected_night else R.drawable.ic_map_pillar_night
                    } else {
                        if (mapType.value == 1) R.drawable.ic_map_pillar_selected_day else R.drawable.ic_map_pillar_day
                    }
                ),
                    contentDescription = "路况条",
                    modifier = modifier
                        .constrainAs(roadCondition) {
                            start.linkTo(smallMap.end, dp16)
                            end.linkTo(parent.end)
                            top.linkTo(smallMap.top)
                        }
                        .size(dp272, dp150)
                        .alpha(if (roadConditionPressed.value) 0.8f else 1.0f)
                        .clickable(
                            interactionSource = roadConditionInteractionSource,
                            indication = null,
                            onClick = { //设置全程路况概况 0 小地图  1 光柱图 2 极简
                                viewModel.setupMapType(1)
                            }
                        )
                )
            }
        }
    }
}

//自动比例尺设置
@Composable
fun ScaleSettingMenu(
    modifier: Modifier = Modifier,
    viewModel: NavigationFunctionMenuViewModel,
    scaleSwitch: LayoutSettingSwitchBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (broadcast, moreInfo, broadcastLayout) = createRefs()
        val context = LocalContext.current
        val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp56 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_56)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val dp65 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_65)
        val scaleChecked by viewModel.scaleChecked.observeAsState()
        val isNight = viewModel.isNight.observeAsState()

        SettingSwitch(
            modifier
                .constrainAs(broadcastLayout) {
                    top.linkTo(parent.top, dp65)
                    start.linkTo(parent.start, dp40)
                },
            check = scaleChecked == true,
            isNight = isNight.value == true,
            binding = scaleSwitch,
            setOnCheckedChangeListener = {
                viewModel.scaleOperation(it)
            }
        )

        Text(
            text = stringResource(id = R.string.sv_setting_navi_scale),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_34).sp,
            modifier = modifier
                .constrainAs(broadcast) {
                    start.linkTo(broadcastLayout.end, dp32)
                    top.linkTo(broadcastLayout.top)
                    bottom.linkTo(broadcastLayout.bottom)
                }.clearAndSetSemantics {  }
        )
        Image(painter = painterResource(id = if (isNight.value == true) R.drawable.ic_more_info_tip_night else R.drawable.ic_more_info_tip_day),
            contentDescription = "", modifier = modifier
                .size(dp64)
                .constrainAs(moreInfo) {
                    top.linkTo(parent.top, dp56)
                    end.linkTo(parent.end, dp40)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.setShowMoreInfo(MoreInfoBean(context.getString(R.string.sv_setting_navi_scale), if (scaleChecked == true) context.getString(R.string.sv_setting_navi_scale_child_open) else context.getString(
                            R.string.sv_setting_navi_scale_child_close
                        )))
                    }
                )
        )
    }
}

@Composable
private fun setPreviewTextColor(isSelect: Boolean): Color {
    return colorResource(
        id = if (NightModeGlobal.isNightMode()) {
            if (isSelect) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.customColorWhite80Night
        } else {
            if (isSelect) com.desaysv.psmap.model.R.color.customColorTitleDay else com.desaysv.psmap.model.R.color.customColorRbBgDay
        }
    )
}

@Composable
private fun setPreviewBtnBackgroundColor(isSelect: Boolean): Color {
    return colorResource(
        id = if (NightModeGlobal.isNightMode()) {
            if (isSelect) com.desaysv.psmap.model.R.color.customColorSettingBtnBgPressedNight else com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight
        } else {
            if (isSelect) com.desaysv.psmap.model.R.color.customColorSettingBtnBgPressedDay else com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalDay
        }
    )
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun NavigationFunctionMenuScreenPreview() {
    DsDefaultTheme {
        val context = LocalContext.current
        SetLimitMenu(modifier = Modifier.background(color = Color.White), bindingSwitch = LayoutSettingSwitchBinding.inflate(
            LayoutInflater.from(context)
        ), viewModel = hiltViewModel<NavigationFunctionMenuViewModel>(), noNetUseLimit = {}, startSettingCar = {})
    }
}