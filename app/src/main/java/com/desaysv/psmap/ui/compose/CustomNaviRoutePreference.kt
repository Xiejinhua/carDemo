package com.desaysv.psmap.ui.compose

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.utils.CommonUtils

/**
 * 导航设置-路线偏好自定义控件
 */
@Composable
fun CustomNaviRoutePreference(
    modifier: Modifier = Modifier,
    type: String = "ctvStrategyDefault",
    width: Dp = dimensionResource(id = R.dimen.sv_dimen_176),
    height: Dp = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_120),
    isSelect: Boolean = false,
    isNight: Boolean = false,
    hasNetworkConnected: Boolean = true,
    icon: Int = com.desaysv.psmap.R.drawable.ic_route_avoid_congestion_svg_icon_night,
    iconSize: Dp = dimensionResource(id = R.dimen.sv_dimen_64),
    iconTop: Dp = dimensionResource(id = R.dimen.sv_dimen_12),
    name: String = "",
    shadowRadius: Dp = dimensionResource(id = R.dimen.sv_dimen_10),
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val modifierLayout: Modifier = Modifier
    val color = colorResource(id = if (isNight) com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight else com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalDay)
    val onDebounceClick = debounce(150L, rememberCoroutineScope()) {
        onClick()
    }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
        ), label = ""
    )
    ConstraintLayout(
        modifier = modifier
            .size(width, height)
            .alpha(if (!hasNetworkConnected) 0.4f else if (isPressed) 0.8f else 1.0f)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = hasNetworkConnected,
                onClick = onDebounceClick
            )
    ) {
        val (bg, image, text) = createRefs()
        Box(modifier = modifierLayout
            .fillMaxSize()
            .shadow(
                elevation = shadowRadius,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.sv_dimen_4)),
                spotColor = color,
                ambientColor = color
            )
            .background(
                color = colorResource(id = customDefaultButtonItemBg(isSelect)),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.sv_dimen_12)),
            )
            .constrainAs(bg) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
            }
            .alpha(if (!hasNetworkConnected) 0.4f else if (isPressed) 0.8f else 1.0f)
        )
        Image(painter = painterResource(id = icon),
            contentDescription = "",
            modifier = modifierLayout
                .size(iconSize)
                .constrainAs(image) {
                    start.linkTo(bg.start)
                    end.linkTo(bg.end)
                    top.linkTo(bg.top, iconTop)
                }
                .alpha(if (!hasNetworkConnected) 0.4f else if (isPressed) 0.8f else 1.0f)
        )
        Text(
            text = name,
            color = colorResource(id = if (isNight) {
                if (isSelect) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.customColorWhite80Night
            } else { if (isSelect) com.desaysv.psmap.model.R.color.primaryDay else com.desaysv.psmap.model.R.color.customColorRbBgDay}),
            fontSize = CommonUtils.getAutoDimenValueSP(context, R.dimen.sv_dimen_24).sp,
            modifier = modifierLayout
                .constrainAs(text) {
                    start.linkTo(bg.start)
                    end.linkTo(bg.end)
                    top.linkTo(image.bottom)
                }
        )
    }
}

@Composable
fun setTextColor(): Int {
    return if (NightModeGlobal.isNightMode()) {
        com.desaysv.psmap.model.R.color.onSecondaryContainerNight
    } else {
        com.desaysv.psmap.model.R.color.onSecondaryContainerDay
    }
}

@Composable
fun CustomNaviDefaultPreference(
    modifier: Modifier = Modifier,
    type: String = "ctvStrategyDefault",
    width: Dp = dimensionResource(id = R.dimen.sv_dimen_560),
    height: Dp = dimensionResource(id = R.dimen.sv_dimen_90),
    isSelect: Boolean = false,
    isNight: Boolean = false,
    icon: Int = com.desaysv.psmap.R.drawable.ic_route_avoid_congestion_svg_icon_night,
    iconSize: Dp = dimensionResource(id = R.dimen.sv_dimen_48),
    iconLeft: Dp = dimensionResource(id = R.dimen.sv_dimen_186),
    name: String = "",
    textLeft: Dp = dimensionResource(id = R.dimen.sv_dimen_20),
    shadowRadius: Dp = dimensionResource(id = R.dimen.sv_dimen_10),
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val modifierLayout: Modifier = Modifier
    val color = colorResource(id = if (isNight) com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight else com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalDay)
    val onDebounceClick = debounce(150L, rememberCoroutineScope()) {
        onClick()
    }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
        ), label = ""
    )
    ConstraintLayout(
        modifier = modifier
            .size(width, height)
            .alpha(if (isPressed) 0.8f else 1.0f)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onDebounceClick
            )
    ) {
        val (bg, image, text) = createRefs()
        Box(modifier = modifierLayout
            .fillMaxSize()
            .shadow(
                elevation = shadowRadius,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.sv_dimen_4)),
                spotColor = color,
                ambientColor = color
            )
            .background(
                color = colorResource(id = customDefaultButtonItemBg(isSelect)),
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.sv_dimen_12)),
            )
            .constrainAs(bg) {
                start.linkTo(parent.start)
                top.linkTo(parent.top)
            }
            .alpha(if (isPressed) 0.8f else 1.0f)
        )
        Image(painter = painterResource(id = icon),
            contentDescription = "",
            modifier = modifierLayout
                .size(iconSize)
                .constrainAs(image) {
                    start.linkTo(bg.start, iconLeft)
                    top.linkTo(bg.top)
                    bottom.linkTo(bg.bottom)
                }
                .alpha(if (isPressed) 0.8f else 1.0f)
        )
        Text(
            text = name,
            color = colorResource(id = if (isNight) {
                if (isSelect) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.customColorWhite80Night
            } else { if (isSelect) com.desaysv.psmap.model.R.color.primaryDay else com.desaysv.psmap.model.R.color.customColorRbBgDay}),
            fontSize = CommonUtils.getAutoDimenValueSP(context, R.dimen.sv_dimen_30).sp,
            modifier = modifierLayout
                .constrainAs(text) {
                    start.linkTo(image.end, textLeft)
                    top.linkTo(bg.top)
                    bottom.linkTo(bg.bottom)
                }
                .alpha(if (isPressed) 0.8f else 1.0f)
        )
    }
}