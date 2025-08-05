package com.desaysv.psmap.ui.settings.settingconfig

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.user.behavior.model.ConfigKey
import com.autosdk.bussiness.widget.setting.SettingConst
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.databinding.LayoutSettingSwitchBinding
import com.desaysv.psmap.databinding.LayoutTtsModeBinding
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.ui.compose.CustomNaviRoutePreference
import com.desaysv.psmap.ui.compose.SettingSwitch
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import timber.log.Timber

/**
 * 导航设置项Compose
 */
@Composable
fun NaviBroadcastScreen(
    modifier: Modifier = Modifier,
    viewModel: NaviBroadcastViewModel,
    onCardClick: () -> Unit = {},
    bindingSwitch: LayoutSettingSwitchBinding,
    bindingAhaScenicSwitch: LayoutSettingSwitchBinding,
    bindingTtsMode: LayoutTtsModeBinding
) {
    LazyColumn {
        item {
            SetTtsMode(modifier, viewModel, bindingTtsMode) //播报模式
        }
        item {
            VolumeControl(modifier, viewModel, onCardClick) //语音播报
        }
        item {
            SettingCruiseTts(modifier, viewModel) //巡航播报
        }
        item {
            CruiseBroadcastSwitch(modifier, viewModel, bindingSwitch) //巡航播报开关
        }
        item {
            AhaScenicBroadcastSwitch(modifier, viewModel, bindingAhaScenicSwitch) //巡航景点播报开关
        }
    }
}

//播报模式
@Composable
fun SetTtsMode(
    modifier: Modifier = Modifier,
    viewModel: NaviBroadcastViewModel,
    bindingTtsMode: LayoutTtsModeBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (naviSetTtsMode, moreInfo, broadcastLayout) = createRefs()
        val context = LocalContext.current
        val dp24 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp49 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_49)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val volumeModel = viewModel.volumeModel.observeAsState()
        val isNight = viewModel.isNight.observeAsState()
        val tag = "layoutTtsModeBinding"
        var lastCheckedId = viewModel.lastCheckedId.observeAsState()
        var lastTargetX = viewModel.lastTargetX.observeAsState()

        Text(
            text = stringResource(id = R.string.sv_setting_navi_tts_mode),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night else com.desaysv.psmap.model.R.color.customColorRbBgDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
            modifier = modifier
                .constrainAs(naviSetTtsMode) {
                    start.linkTo(parent.start, dp40)
                    top.linkTo(parent.top, dp49)
                }
        )
        Image(painter = painterResource(id = if (NightModeGlobal.isNightMode()) R.drawable.ic_more_info_tip_night else R.drawable.ic_more_info_tip_day),
            contentDescription = "", modifier = modifier
                .size(dp64)
                .constrainAs(moreInfo) {
                    top.linkTo(parent.top, dp40)
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
                bindingTtsMode.isNight = isNight.value == true
                Timber.d(" initBinding selectTab:%s", volumeModel.value)
                bindingTtsMode.indicator.animate().setListener(null) // 先清除之前的动画监听器，防止冲突
                bindingTtsMode.broadcastTab.setOnCheckedChangeListener(null) // 清除之前的监听器，防止触发之前的逻辑
                when (volumeModel.value) {
                    SettingConst.BROADCAST_DETAIL -> { //设置播报模式 详细播报
                        bindingTtsMode.indicator.animate()
                            .x(0f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                        bindingTtsMode.broadcastTab.check(R.id.broadcastDetail)
                    }

                    SettingConst.BROADCAST_EASY -> { //设置播报模式 简单播报
                        bindingTtsMode.indicator.animate()
                            .x(187f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                        bindingTtsMode.broadcastTab.check(R.id.broadcastSimple)
                    }

                    SettingConst.BROADCAST_MINIMALISM -> { //设置播报模式 极简播报
                        bindingTtsMode.indicator.animate()
                            .x(374f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                        bindingTtsMode.broadcastTab.check(R.id.broadcastMin)
                    }
                }

                bindingTtsMode.broadcastTab.setOnCheckedChangeListener { group, checkedId ->
                    val checkedButton: RadioButton = group.findViewById(checkedId)
                    // 计算指示条应该移动到的位置
                    val targetX: Int = checkedButton.left + (checkedButton.width - bindingTtsMode.indicator.width) / 2
                    // 判断动画持续时间
                    val duration = if (lastCheckedId.value != null && areAdjacent(lastCheckedId.value!!, checkedId)) {
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
                                            Timber.i("onAnimationEnd R.id.broadcastDetail")
                                            viewModel.setupVolumeModel(SettingConst.BROADCAST_DETAIL)
                                        }

                                        R.id.broadcastSimple -> { //设置播报模式 简单播报
                                            Timber.i("onAnimationEnd R.id.broadcastSimple")
                                            viewModel.setupVolumeModel(SettingConst.BROADCAST_EASY)
                                        }

                                        R.id.broadcastMin -> { //设置播报模式 极简播报
                                            Timber.i("onAnimationEnd R.id.broadcastMin")
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
                top.linkTo(naviSetTtsMode.bottom, dp24)
            }
        )
    }
}

// 判断两个 RadioButton 是否相邻
private fun areAdjacent(lastId: Int, currentId: Int): Boolean {
    return when {
        (lastId == R.id.broadcastDetail && currentId == R.id.broadcastSimple) ||
                (lastId == R.id.broadcastSimple && currentId == R.id.broadcastDetail) -> true
        (lastId == R.id.broadcastSimple && currentId == R.id.broadcastMin) ||
                (lastId == R.id.broadcastMin && currentId == R.id.broadcastSimple) -> true
        else -> false
    }
}

//巡航播报
@Composable
fun  SettingCruiseTts(
    modifier: Modifier = Modifier,
    viewModel: NaviBroadcastViewModel
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (settingCruise, moreInfo, conditionsAhead, eyeBroadcast, safety) = createRefs()
        val context = LocalContext.current
        val dp16 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16)
        val dp24 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp48 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_48)
        val dp57 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_57)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val dp120 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_120)
        val dp176 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_176)
        val roadConditionsAhead = viewModel.roadConditionsAhead.observeAsState()
        val electronicEyeBroadcast = viewModel.electronicEyeBroadcast.observeAsState()
        val safetyReminder = viewModel.safetyReminder.observeAsState()
        val isNight by viewModel.isNight.observeAsState()

        Text(
            text = stringResource(id = R.string.sv_setting_cruise),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night else com.desaysv.psmap.model.R.color.customColorRbBgDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
            modifier = modifier
                .constrainAs(settingCruise) {
                    start.linkTo(parent.start, dp40)
                    top.linkTo(parent.top, dp57)
                }.clearAndSetSemantics {  }
        )
        Image(painter = painterResource(id = if (NightModeGlobal.isNightMode()) R.drawable.ic_more_info_tip_night else R.drawable.ic_more_info_tip_day),
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
                        viewModel.setShowMoreInfo(MoreInfoBean(context.getString(R.string.sv_setting_cruise), context.getString(R.string.sv_setting_cruise_tip)))
                    }
                )
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(conditionsAhead) {
                top.linkTo(settingCruise.bottom, dp24)
                start.linkTo(settingCruise.start)
            }, type = stringResource(R.string.sv_setting_road_conditions_ahead),
            width = dp176,
            height = dp120,
            isSelect = roadConditionsAhead.value ?: false,
            isNight = isNight == true,
            icon = if (isNight == true) R.drawable.ic_set_road_conditions_night else {
                if (roadConditionsAhead.value == true) R.drawable.ic_set_road_conditions_select_day else R.drawable.ic_set_road_conditions_day
            },
            name = stringResource(R.string.sv_setting_road_conditions_ahead),
            onClick = { //前方路况 开关
                viewModel.cruiseBroadcastSelect(ConfigKey.ConfigKeyRoadWarn, roadConditionsAhead.value ?: false)
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(eyeBroadcast) {
                bottom.linkTo(conditionsAhead.bottom)
                start.linkTo(conditionsAhead.end, dp16)
            }, type = stringResource(R.string.sv_setting_electronic_eye_broadcast),
            width = dp176,
            height = dp120,
            isSelect = electronicEyeBroadcast.value ?: false,
            isNight = isNight == true,
            icon = if (isNight == true) R.drawable.ic_set_electronic_eye_broadcast_night else {
                if (electronicEyeBroadcast.value == true) R.drawable.ic_set_electronic_eye_broadcast_select_day else R.drawable.ic_set_electronic_eye_broadcast_day
            },
            name = stringResource(R.string.sv_setting_electronic_eye_broadcast),
            onClick = { //电子眼播报 开关
                viewModel.cruiseBroadcastSelect(ConfigKey.ConfigKeySafeBroadcast, electronicEyeBroadcast.value ?: false)
            }
        )
        CustomNaviRoutePreference(modifier = modifier
            .constrainAs(safety) {
                bottom.linkTo(conditionsAhead.bottom)
                start.linkTo(eyeBroadcast.end, dp16)
            }, type = stringResource(R.string.sv_setting_safety_reminder),
            width = dp176,
            height = dp120,
            isSelect = safetyReminder.value ?: false,
            isNight = isNight == true,
            icon = if (isNight == true) R.drawable.ic_set_safety_reminder_night else {
                if (safetyReminder.value == true) R.drawable.ic_set_safety_reminder_select_day else R.drawable.ic_set_safety_reminder_day
            },
            name = stringResource(R.string.sv_setting_safety_reminder),
            onClick = { //安全提醒 开关
                viewModel.cruiseBroadcastSelect(ConfigKey.ConfigKeyDriveWarn, safetyReminder.value ?: false)
            }
        )
    }
}

//巡航播报开关
@Composable
fun CruiseBroadcastSwitch(
    modifier: Modifier = Modifier,
    viewModel: NaviBroadcastViewModel,
    bindingSwitch: LayoutSettingSwitchBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (broadcast, moreInfo, broadcastLayout) = createRefs()
        val context = LocalContext.current
        val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val dp75 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_75)
        val cruiseBroadcast = viewModel.cruiseBroadcast.observeAsState()
        val isNight = viewModel.isNight.observeAsState()
        SettingSwitch(
            modifier
                .constrainAs(broadcastLayout) {
                    top.linkTo(parent.top, dp75)
                    start.linkTo(parent.start, dp40)
                },
            check = cruiseBroadcast.value == true,
            isNight = isNight.value == true,
            binding = bindingSwitch,
            setOnCheckedChangeListener = {
                viewModel.setCruiseBroadcastSwitch(it)
            }
        )
        Text(
            text = stringResource(id = R.string.sv_setting_cruise_broadcast),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_34).sp,
            modifier = modifier
                .constrainAs(broadcast) {
                    start.linkTo(broadcastLayout.end, dp32)
                    top.linkTo(broadcastLayout.top)
                    bottom.linkTo(broadcastLayout.bottom)
                }.clearAndSetSemantics {  }
        )
        Image(painter = painterResource(id = if (NightModeGlobal.isNightMode()) R.drawable.ic_more_info_tip_night else R.drawable.ic_more_info_tip_day),
            contentDescription = "", modifier = modifier
                .size(dp64)
                .constrainAs(moreInfo) {
                    top.linkTo(broadcastLayout.top)
                    bottom.linkTo(broadcastLayout.bottom)
                    end.linkTo(parent.end, dp40)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.setShowMoreInfo(MoreInfoBean(context.getString(R.string.sv_setting_cruise_broadcast), context.getString(R.string.sv_setting_cruise_broadcast_tip)))
                    }
                )
        )
    }
}

//巡航景点播报开关
@Composable
fun AhaScenicBroadcastSwitch(
    modifier: Modifier = Modifier,
    viewModel: NaviBroadcastViewModel,
    bindingSwitch: LayoutSettingSwitchBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (broadcast, moreInfo, broadcastLayout) = createRefs()
        val context = LocalContext.current
        val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val dp82 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_82)
        val ahaScenicBroadcast = viewModel.ahaScenicBroadcast.observeAsState()
        val isNight = viewModel.isNight.observeAsState()
        SettingSwitch(
            modifier
                .constrainAs(broadcastLayout) {
                    top.linkTo(parent.top, dp82)
                    start.linkTo(parent.start, dp40)
                },
            check = ahaScenicBroadcast.value == true,
            isNight = isNight.value == true,
            binding = bindingSwitch,
            setOnCheckedChangeListener = {
                viewModel.setAhaScenicBroadcastSwitch(it)
            }
        )
        Text(
            text = stringResource(id = R.string.sv_setting_aha_scenic_broadcast),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_34).sp,
            modifier = modifier
                .constrainAs(broadcast) {
                    start.linkTo(broadcastLayout.end, dp32)
                    top.linkTo(broadcastLayout.top)
                    bottom.linkTo(broadcastLayout.bottom)
                }.clearAndSetSemantics {  }
        )
        Image(painter = painterResource(id = if (NightModeGlobal.isNightMode()) R.drawable.ic_more_info_tip_night else R.drawable.ic_more_info_tip_day),
            contentDescription = "", modifier = modifier
                .size(dp64)
                .constrainAs(moreInfo) {
                    top.linkTo(broadcastLayout.top)
                    bottom.linkTo(broadcastLayout.bottom)
                    end.linkTo(parent.end, dp40)
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        viewModel.setShowMoreInfo(MoreInfoBean(context.getString(R.string.sv_setting_aha_scenic_broadcast), context.getString(R.string.sv_setting_aha_scenic_broadcast_tip)))
                    }
                )
        )
    }
}

//语音播报
@Composable
fun VolumeControl(
    modifier: Modifier = Modifier,
    viewModel: NaviBroadcastViewModel,
    onCardClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val alphaValue = if (isPressed.value) 0.8f else 1.0f
    val useVoice by viewModel.useVoice.observeAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
        ), label = ""
    )
    ConstraintLayout(modifier = modifier
        .fillMaxWidth()
    ) {
        val (title, content) = createRefs()
        val context = LocalContext.current
        val dp2 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_2)
        val dp8 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_8)
        val dp12 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_12)
        val dp16 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16)
        val dp24 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
        val dp30 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_30)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp48 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_48)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val dp90 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_90)
        val dp560 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_560)
        Text(
            text = stringResource(id = com.autosdk.R.string.setting_main_text_volume_control),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night else com.desaysv.psmap.model.R.color.customColorRbBgDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
            modifier = modifier
                .constrainAs(title) {
                    start.linkTo(parent.start, dp40)
                    top.linkTo(parent.top, dp48)
                }
        )
        ConstraintLayout(modifier = modifier
            .alpha(alphaValue)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    onCardClick()
                }
            ).constrainAs(content) {
                top.linkTo(title.bottom, dp24)
                start.linkTo(parent.start, dp40)
            }
        ) {
            val (bg, leftIv, volumeType, more) = createRefs()
            Box(modifier = modifier
                .background(
                    color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.inversePrimaryNight else com.desaysv.psmap.model.R.color.inversePrimaryDay),
                    shape = RoundedCornerShape(dp12)
                )
                .border(
                    width = dp2,
                    color = colorResource(if (NightModeGlobal.isNightMode()) android.R.color.transparent else com.desaysv.psmap.model.R.color.customColorBlack10Day),
                    shape = RoundedCornerShape(dp12)
                )
                .size(dp560, dp90)
                .constrainAs(bg) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
            )
            Image(bitmap = if (TextUtils.isEmpty(useVoice?.imageFilePath)) {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_default_woman_head).asImageBitmap()
            } else {
                BitmapFactory.decodeFile(useVoice?.imageFilePath).asImageBitmap()
            },
                contentDescription = null,
                modifier = modifier
                    .size(dp64)
                    .constrainAs(leftIv) {
                        top.linkTo(bg.top)
                        bottom.linkTo(bg.bottom)
                        start.linkTo(bg.start, dp30)
                    }
            )

            Text(
                text = useVoice?.name ?: stringResource(id = com.desaysv.psmap.base.R.string.sv_setting_standard_female_voice),
                color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
                fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
                modifier = modifier
                    .constrainAs(volumeType) {
                        start.linkTo(leftIv.end, dp30)
                        top.linkTo(leftIv.top)
                        bottom.linkTo(leftIv.bottom)
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            onCardClick()
                        }
                    )
            )
            Image(painter = painterResource(id = if (NightModeGlobal.isNightMode()) R.drawable.ic_triangle_more_night else R.drawable.ic_triangle_more_day),
                contentDescription = null,
                modifier = modifier
                    .size(dp16)
                    .constrainAs(more) {
                        bottom.linkTo(bg.bottom, dp8)
                        end.linkTo(bg.end, dp8)
                    }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun VolumeControlPreview() {
    DsDefaultTheme {
        VolumeControl(modifier = Modifier.background(color = Color.White), viewModel = hiltViewModel<NaviBroadcastViewModel>())
    }
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun NaviBroadcastScreenPreview() {
    DsDefaultTheme {
        NaviBroadcastScreen(
            modifier = Modifier.background(color = Color.White),
            viewModel = hiltViewModel<NaviBroadcastViewModel>(),
            bindingSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(LocalContext.current)),
            bindingTtsMode = LayoutTtsModeBinding.inflate(LayoutInflater.from(LocalContext.current)),
            bindingAhaScenicSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(LocalContext.current))
        )
    }
}