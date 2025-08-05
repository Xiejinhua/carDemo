package com.desaysv.psmap.ui.settings.settingconfig

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.common.CommonConfigValue
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.databinding.LayoutSettingSwitchBinding
import com.desaysv.psmap.databinding.LayoutFontSizeBinding
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.ui.compose.CustomSwitchBg
import com.desaysv.psmap.ui.compose.CustomSwitchItem
import com.desaysv.psmap.ui.compose.SettingCompassItem
import com.desaysv.psmap.ui.compose.SettingSwitch
import com.desaysv.psmap.ui.compose.customSwitchItemBg
import com.desaysv.psmap.ui.compose.customSwitchItemFontColor
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import timber.log.Timber

/**
 * 导航设置项Compose
 */
@Composable
fun MapSettingScreen(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel,
    roadConditionSwitch: LayoutSettingSwitchBinding,
    favorSwitch: LayoutSettingSwitchBinding,
    scaleSwitch: LayoutSettingSwitchBinding,
    bindingFontSize: LayoutFontSizeBinding
) {
    val savedIndex = viewModel.savedScrollIndex.observeAsState()
    val savedOffset = viewModel.savedScrollOffset.observeAsState()
    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = savedIndex.value ?: 0,
        initialFirstVisibleItemScrollOffset = savedOffset.value ?: 0
    )

    // 监听滑动状态的变化
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                viewModel.savedScrollIndex.postValue(index)
                viewModel.savedScrollOffset.postValue(offset)
            }
    }
    LazyColumn(
        state = scrollState
    ) {
        item {
            SettingCompass(modifier, viewModel) //罗盘
        }
        item {
            FontSize(modifier, viewModel, bindingFontSize) //地图文字大小设置
        }
        item {
            RoadCondition(modifier, viewModel, roadConditionSwitch) //路况开关设置
        }
        item {
            FavorSetting(modifier, viewModel, favorSwitch) //收藏点显示设置
        }
        item {
            ScaleSetting(modifier, viewModel, scaleSwitch) //自动比例尺设置
        }
    }
}

//个性化车标
@Composable
fun SettingCarIcon(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (title, jtCar, car, normal) = createRefs()
        val context = LocalContext.current
        val dp13 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_13)
        val dp16 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16)
        val dp19 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_19)
        val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
        val dp68 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_68)
        val dp146 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_146)
        val personalizationCar by viewModel.personalizationCar.observeAsState()

        Text(
            text = stringResource(id = com.autosdk.R.string.setting_main_text_3d_car_title),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_24).sp,
            modifier = modifier
                .constrainAs(title) {
                    start.linkTo(parent.start, dp32)
                    top.linkTo(parent.top, dp19)
                }
        )
        SettingCompassItem(modifier = modifier
            .constrainAs(jtCar) {
                top.linkTo(title.bottom, dp16)
                start.linkTo(title.start)
            },
            width = dp146,
            height = dp68,
            text = "",
            icon = if (NightModeGlobal.isNightMode()) R.drawable.ic_jt_car_night else R.drawable.ic_jt_car_day,
            isSelect = personalizationCar == 1,
            description = "捷途车标",
            onClick = {
                viewModel.setCarPersonalization(1)
            }
        )
        SettingCompassItem(modifier = modifier
            .constrainAs(car) {
                bottom.linkTo(jtCar.bottom)
                start.linkTo(jtCar.end, dp13)
            },
            width = dp146,
            height = dp68,
            text = "",
            icon = if (NightModeGlobal.isNightMode()) R.drawable.ic_car_night else R.drawable.ic_car_day,
            isSelect = personalizationCar == 2,
            description = "车模车标",
            onClick = {
                viewModel.setCarPersonalization(2)
            }
        )
        SettingCompassItem(modifier = modifier
            .constrainAs(normal) {
                bottom.linkTo(car.bottom)
                start.linkTo(car.end, dp13)
            },
            width = dp146,
            height = dp68,
            text = "",
            icon = if (NightModeGlobal.isNightMode()) R.drawable.ic_normal_car_night else R.drawable.ic_normal_car_day,
            isSelect = personalizationCar == 3,
            description = "普通车标",
            onClick = {
                viewModel.setCarPersonalization(3)
            }
        )
    }
}

//罗盘
@Composable
fun SettingCompass(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (title, show, hide) = createRefs()
        val context = LocalContext.current
        val dp24 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
        val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val showCarCompass by viewModel.showCarCompass.observeAsState()

        Text(
            text = stringResource(id = R.string.sv_setting_car_compass),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night else com.desaysv.psmap.model.R.color.customColorRbBgDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
            modifier = modifier
                .constrainAs(title) {
                    start.linkTo(parent.start, dp40)
                    top.linkTo(parent.top, dp40)
                }
        )
        SettingCompassItem(modifier = modifier
            .constrainAs(show) {
                top.linkTo(title.bottom, dp24)
                start.linkTo(title.start)
            },
            text = "",
            icon = if (NightModeGlobal.isNightMode()) R.drawable.ic_car_has_compass_night else R.drawable.ic_car_has_compass_day,
            isSelect = showCarCompass == true,
            description = "罗盘车标",
            onClick = {
                viewModel.setShowCarCompass(true)
            }
        )
        SettingCompassItem(modifier = modifier
            .constrainAs(hide) {
                bottom.linkTo(show.bottom)
                start.linkTo(show.end, dp32)
            },
            text = "",
            icon = if (NightModeGlobal.isNightMode()) R.drawable.ic_car_no_compass_night else R.drawable.ic_car_no_compass_day,
            isSelect = showCarCompass == false,
            description = "简易车标",
            onClick = {
                viewModel.setShowCarCompass(false)
            }
        )
    }
}

//路况开关设置
@Composable
fun RoadCondition(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel,
    bindingSwitch: LayoutSettingSwitchBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (roadConditionSwitch, conditionSwitchLayout) = createRefs()
        val context = LocalContext.current
        val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp67 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_67)
        val tmc = viewModel.tmc.observeAsState()
        val isNight = viewModel.isNight.observeAsState()
        SettingSwitch(
            modifier
                .constrainAs(conditionSwitchLayout) {
                    top.linkTo(parent.top, dp67)
                    start.linkTo(parent.start, dp40)
                },
            check = tmc.value == 1,
            isNight = isNight.value == true,
            binding = bindingSwitch,
            setOnCheckedChangeListener = {
                viewModel.setConfigKeyRoadEvent(if (it) CommonConfigValue.KEY_ROAT_OPEN else CommonConfigValue.KEY_ROAT_CLOSE)
            }
        )
        Text(
            text = stringResource(id = R.string.sv_setting_road_condition),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_34).sp,
            modifier = modifier
                .constrainAs(roadConditionSwitch) {
                    start.linkTo(conditionSwitchLayout.end, dp32)
                    top.linkTo(conditionSwitchLayout.top)
                    bottom.linkTo(conditionSwitchLayout.bottom)
                }.clearAndSetSemantics {  }
        )
    }
}

//默认视图
@Composable
fun SetView(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (naviSetView, viewLayout, threeView, twoHead, twoNorth) = createRefs()
        val context = LocalContext.current
        val dp2 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_2)
        val dp4 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_4)
        val dp10 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_10)
        val dp30 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_30)
        val dp31 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_31)
        val dp35 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_35)
        val dp39 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_39)
        val dp60 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_60)
        val dp96 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_96)
        val viewOfMap = viewModel.viewOfMap.observeAsState()

        Text(
            text = stringResource(id = R.string.sv_setting_navi_view),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_24).sp,
            modifier = modifier
                .padding(top = dp30)
                .fillMaxWidth()
                .height(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_115))
                .background(
                    color = colorResource(
                        id = if (NightModeGlobal.isNightMode()) {
                            com.desaysv.psmap.model.R.color.customColorSetBgNight
                        } else {
                            com.desaysv.psmap.model.R.color.secondaryContainerDay
                        }
                    ),
                    shape = RoundedCornerShape(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16))
                )
                .padding(top = dp10, start = dp30)
                .constrainAs(naviSetView) {
                    start.linkTo(parent.start, dp60)
                    end.linkTo(parent.end, dp60)
                    top.linkTo(parent.top, dp10)
                    width = Dimension.fillToConstraints
                }
        )

        CustomSwitchBg(
            modifier.constrainAs(viewLayout) {
                end.linkTo(naviSetView.end, dp30)
                bottom.linkTo(naviSetView.bottom, dp10)
            }, width = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_298), height = dp39,
            background = colorResource(
                id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.surfaceVariantNight else
                    com.desaysv.psmap.model.R.color.primaryDay
            ), radius = dp35
        )
        CustomSwitchItem(
            modifier.constrainAs(threeView) {
                start.linkTo(viewLayout.start, dp4)
                top.linkTo(viewLayout.top)
                bottom.linkTo(viewLayout.bottom)
            },
            text = stringResource(id = R.string.sv_setting_navi_view_3d),
            fontSize = com.desaysv.psmap.base.R.dimen.sv_dimen_18,
            fontColor = customSwitchItemFontColor(viewOfMap.value == 1),
            background = customSwitchItemBg(viewOfMap.value == 1),
            width = dp96,
            height = dp31,
            radius = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_35),
            hasPressed = false,
            onClick = { //设置地图视角 3D车头朝上
                viewModel.toSetupViewModel(1)
            }
        )
        CustomSwitchItem(
            modifier.constrainAs(twoHead) {
                start.linkTo(threeView.end, dp2)
                top.linkTo(viewLayout.top)
                bottom.linkTo(viewLayout.bottom)
            },
            text = stringResource(id = R.string.sv_setting_navi_view_2d),
            fontSize = com.desaysv.psmap.base.R.dimen.sv_dimen_18,
            fontColor = customSwitchItemFontColor(viewOfMap.value == 0),
            background = customSwitchItemBg(viewOfMap.value == 0),
            width = dp96,
            height = dp31,
            radius = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_35),
            hasPressed = false,
            onClick = { //设置地图视角 2D车头朝上
                viewModel.toSetupViewModel(0)
            }
        )
        CustomSwitchItem(
            modifier.constrainAs(twoNorth) {
                end.linkTo(viewLayout.end, dp4)
                top.linkTo(viewLayout.top)
                bottom.linkTo(viewLayout.bottom)
            },
            text = stringResource(id = R.string.sv_setting_navi_view_2d_north),
            fontSize = com.desaysv.psmap.base.R.dimen.sv_dimen_18,
            fontColor = customSwitchItemFontColor(viewOfMap.value == 2),
            background = customSwitchItemBg(viewOfMap.value == 2),
            width = dp96,
            height = dp31,
            radius = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_35),
            hasPressed = false,
            onClick = { //设置地图视角 2D北朝上
                viewModel.toSetupViewModel(2)
            }
        )
    }
}

//黑夜白天
@Composable
fun SetDayNight(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel
) {
    val dayNightSwitch = viewModel.dayNightSwitch.observeAsState()
    if (dayNightSwitch.value == true) {
        ConstraintLayout(modifier = modifier.fillMaxWidth()) {
            val (naviSetDayNight, dayNightLayout, night, day, auto) = createRefs()
            val context = LocalContext.current
            val dp2 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_2)
            val dp4 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_4)
            val dp10 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_10)
            val dp30 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_30)
            val dp31 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_31)
            val dp35 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_35)
            val dp39 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_39)
            val dp60 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_60)
            val dp96 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_96)
            val dayNightType = viewModel.dayNightType.observeAsState()

            Text(
                text = stringResource(id = R.string.sv_setting_navi_day_night),
                color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay),
                fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_24).sp,
                modifier = modifier
                    .padding(top = dp30)
                    .fillMaxWidth()
                    .height(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_115))
                    .background(
                        color = colorResource(
                            id = if (NightModeGlobal.isNightMode()) {
                                com.desaysv.psmap.model.R.color.customColorSetBgNight
                            } else {
                                com.desaysv.psmap.model.R.color.secondaryContainerDay
                            }
                        ),
                        shape = RoundedCornerShape(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_16))
                    )
                    .padding(top = dp10, start = dp30)
                    .constrainAs(naviSetDayNight) {
                        start.linkTo(parent.start, dp60)
                        end.linkTo(parent.end, dp60)
                        top.linkTo(parent.top, dp10)
                        width = Dimension.fillToConstraints
                    }
            )

            CustomSwitchBg(
                modifier.constrainAs(dayNightLayout) {
                    end.linkTo(naviSetDayNight.end, dp30)
                    bottom.linkTo(naviSetDayNight.bottom, dp10)
                }, width = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_298), height = dp39,
                background = colorResource(
                    id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.surfaceVariantNight else
                        com.desaysv.psmap.model.R.color.primaryDay
                ), radius = dp35
            )
            CustomSwitchItem(
                modifier.constrainAs(night) {
                    start.linkTo(dayNightLayout.start, dp4)
                    top.linkTo(dayNightLayout.top)
                    bottom.linkTo(dayNightLayout.bottom)
                },
                text = stringResource(id = R.string.sv_setting_navi_night),
                fontSize = com.desaysv.psmap.base.R.dimen.sv_dimen_18,
                fontColor = customSwitchItemFontColor(dayNightType.value == 18),
                background = customSwitchItemBg(dayNightType.value == 18),
                width = dp96,
                height = dp31,
                radius = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_35),
                hasPressed = false,
                onClick = { //设置日夜模式 黑夜
                    viewModel.setupDayNightType(18)
                }
            )
            CustomSwitchItem(
                modifier.constrainAs(day) {
                    start.linkTo(night.end, dp2)
                    top.linkTo(dayNightLayout.top)
                    bottom.linkTo(dayNightLayout.bottom)
                },
                text = stringResource(id = R.string.sv_setting_navi_day),
                fontSize = com.desaysv.psmap.base.R.dimen.sv_dimen_18,
                fontColor = customSwitchItemFontColor(dayNightType.value == 17),
                background = customSwitchItemBg(dayNightType.value == 17),
                width = dp96,
                height = dp31,
                radius = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_35),
                hasPressed = false,
                onClick = { //设置日夜模式 白天
                    viewModel.setupDayNightType(17)
                }
            )
            CustomSwitchItem(
                modifier.constrainAs(auto) {
                    end.linkTo(dayNightLayout.end, dp4)
                    top.linkTo(dayNightLayout.top)
                    bottom.linkTo(dayNightLayout.bottom)
                },
                text = stringResource(id = R.string.sv_setting_navi_auto),
                fontSize = com.desaysv.psmap.base.R.dimen.sv_dimen_18,
                fontColor = customSwitchItemFontColor(dayNightType.value == 16),
                background = customSwitchItemBg(dayNightType.value == 16),
                width = dp96,
                height = dp31,
                radius = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_35),
                hasPressed = false,
                onClick = { //设置日夜模式 自动
                    viewModel.setupDayNightType(16)
                }
            )
        }
    }
}

//地图文字大小设置
@Composable
fun FontSize(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel,
    bindingFontSize: LayoutFontSizeBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (roadConditionSwitch, conditionSwitchLayout) = createRefs()
        val context = LocalContext.current
        val dp24 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val mapFont = viewModel.mapFont.observeAsState()
        val isNight = viewModel.isNight.observeAsState()
        val tag = "layoutFontSizeBinding"
        var lastTargetX = viewModel.lastTargetX.observeAsState()

        Text(
            text = stringResource(id = R.string.sv_setting_map_font),
            color = colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorWhite80Night else com.desaysv.psmap.model.R.color.onPrimaryDay),
            fontSize = CommonUtils.getAutoDimenValueSP(context, com.desaysv.psmap.base.R.dimen.sv_dimen_30).sp,
            modifier = modifier
                .constrainAs(roadConditionSwitch) {
                    start.linkTo(parent.start, dp40)
                    top.linkTo(parent.top, dp40)
                }
        )

        AndroidView(
            factory = {
                // 使用findViewWithTag来查找视图
                val viewToRemove = bindingFontSize.root.findViewWithTag<View>(tag)
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
                bindingFontSize.root.tag = tag
                bindingFontSize.root
            },
            update = {
                bindingFontSize.selectTab = mapFont.value
                bindingFontSize.isNight = isNight.value == true
                Timber.d(" initBinding selectTab:%s", mapFont.value)
                bindingFontSize.indicator.animate().setListener(null) // 先清除之前的动画监听器，防止冲突
                bindingFontSize.broadcastTab.setOnCheckedChangeListener(null) // 清除之前的监听器，防止触发之前的逻辑
                when (mapFont.value) {
                    1 -> { //标准字号
                        bindingFontSize.indicator.animate()
                            .x(0f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                        bindingFontSize.broadcastTab.check(R.id.switchOpen)
                    }

                    2 -> { //大字号
                        bindingFontSize.indicator.animate()
                            .x(280f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                        bindingFontSize.broadcastTab.check(R.id.switchClose)
                    }
                }

                bindingFontSize.broadcastTab.setOnCheckedChangeListener { group, checkedId ->
                    val checkedButton: RadioButton = group.findViewById(checkedId)
                    // 计算指示条应该移动到的位置
                    val targetX: Int = checkedButton.left + (checkedButton.width - bindingFontSize.indicator.width) / 2
                    Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                    // 创建平移动画
                    if (targetX == 0 && checkedId != R.id.switchOpen){
                        bindingFontSize.indicator.animate()
                            .x(lastTargetX.value?.toFloat() ?: 0f)
                            .setDuration(0)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .start()
                    } else {
                        bindingFontSize.indicator.animate()
                            .x(targetX.toFloat())
                            .setDuration(200)
                            .setInterpolator(FastOutSlowInInterpolator())
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    viewModel.lastTargetX.postValue(targetX)
                                    when (checkedId) {
                                        R.id.switchOpen -> { //标准字号
                                            viewModel.setMapFont(CommonConfigValue.KEY_MAP_FONT_NORMAL)
                                        }

                                        R.id.switchClose -> { //大字号
                                            viewModel.setMapFont(CommonConfigValue.KEY_MAP_FONT_BIG)
                                        }
                                    }
                                }
                            })
                            .start()
                    }
                }
            },
            modifier = modifier.constrainAs(conditionSwitchLayout) {
                start.linkTo(roadConditionSwitch.start)
                top.linkTo(roadConditionSwitch.bottom, dp24)
            }
        )
    }
}

//收藏点显示设置
@Composable
fun FavorSetting(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel,
    favorSwitch: LayoutSettingSwitchBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (broadcast, moreInfo, broadcastLayout) = createRefs()
        val context = LocalContext.current
        val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp54 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_54)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val favoriteChecked by viewModel.favoriteChecked.observeAsState()
        val isNight = viewModel.isNight.observeAsState()
        SettingSwitch(
            modifier
                .constrainAs(broadcastLayout) {
                    top.linkTo(parent.top, dp54)
                    start.linkTo(parent.start, dp40)
                },
            check = favoriteChecked == true,
            isNight = isNight.value == true,
            binding = favorSwitch,
            setOnCheckedChangeListener = {
                viewModel.favoriteOperation(it)
            }
        )
        Text(
            text = stringResource(id = R.string.sv_setting_navi_favor),
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
                        viewModel.setShowMoreInfo(MoreInfoBean(context.getString(R.string.sv_setting_navi_favor), context.getString(R.string.sv_setting_navi_favor_child)))
                    }
                )
        )
    }
}

//自动比例尺设置
@Composable
fun ScaleSetting(
    modifier: Modifier = Modifier,
    viewModel: MapSettingViewModel,
    scaleSwitch: LayoutSettingSwitchBinding
) {
    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (broadcast, moreInfo, broadcastLayout) = createRefs()
        val context = LocalContext.current
        val dp32 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
        val dp40 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
        val dp54 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_54)
        val dp64 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_64)
        val scaleChecked by viewModel.scaleChecked.observeAsState()
        val isNight = viewModel.isNight.observeAsState()

        SettingSwitch(
            modifier
                .constrainAs(broadcastLayout) {
                    top.linkTo(parent.top, dp54)
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
                        viewModel.setShowMoreInfo(MoreInfoBean(context.getString(R.string.sv_setting_navi_scale), if (scaleChecked == true) context.getString(R.string.sv_setting_navi_scale_child_open) else context.getString(
                                R.string.sv_setting_navi_scale_child_close
                            )))
                    }
                )
        )
    }
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun MapSettingScreenPreview() {
    DsDefaultTheme {
        val viewModel = hiltViewModel<MapSettingViewModel>()
        MapSettingScreen(
            modifier = Modifier.background(color = Color.White),
            viewModel = viewModel,
            roadConditionSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(LocalContext.current)),
            favorSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(LocalContext.current)),
            scaleSwitch = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(LocalContext.current)),
            bindingFontSize = LayoutFontSizeBinding.inflate(LayoutInflater.from(LocalContext.current))
        )
    }
}