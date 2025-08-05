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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import timber.log.Timber

//导航设置--车标罗盘Item
@Composable
fun SettingCompassItem(
    modifier: Modifier = Modifier,
    width: Dp = dimensionResource(id = R.dimen.sv_dimen_264),
    height: Dp = dimensionResource(id = R.dimen.sv_dimen_90),
    text: String = "",
    fontSize: Int = R.dimen.sv_dimen_34,
    icon: Int = com.desaysv.psmap.R.drawable.ic_car_has_compass_day,
    isSelect: Boolean = false,
    description: String = "",
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val alphaValue = if (isPressed.value) 0.8f else 1.0f
    val onDebounceClick = debounce(150L, rememberCoroutineScope()) {
        onClick()
    }
    val scale by animateFloatAsState(
        targetValue = if (isPressed.value) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
        ), label = ""
    )
    Timber.i("SettingCompassItem isSelect:$isSelect")
    ConstraintLayout(
        modifier = modifier
            .alpha(alphaValue)
            .semantics {
                contentDescription = description
            }
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onDebounceClick
            )
    ) {
        val (bg, tip) = createRefs()
        val modifierItem = Modifier
        Image(painter = painterResource(icon),
            contentDescription = "",
            modifier = modifierItem
                .size(width, height)
                .background(
                    color = colorResource(id = customButtonItemBg(isSelect)),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.sv_dimen_12)),
                )
                .border(
                    width = dimensionResource(id = R.dimen.sv_dimen_2),
                    color = colorResource(id = customSwitchButtonBg(isSelect)),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.sv_dimen_12))
                )
                .alpha(alphaValue)
                .padding(dimensionResource(id = R.dimen.sv_dimen_4_8_2))
                .constrainAs(bg) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
        )

        Text(
            text = text,
            color = colorResource(id = cruiseSelectTxtColor(isSelect)),
            fontSize = CommonUtils.getAutoDimenValueSP(context, fontSize).sp,
            modifier = modifierItem.constrainAs(tip) {
                start.linkTo(bg.start)
                bottom.linkTo(bg.bottom)
            }
        )
    }
}

//巡航设置选择文字颜色处理
fun cruiseSelectTxtColor(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        com.desaysv.psmap.model.R.color.onSecondaryNight
    } else {
        if (isSelect) com.desaysv.psmap.model.R.color.primaryDay else com.desaysv.psmap.model.R.color.onPrimaryDay
    }
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun SettingCompassItemPreview() {
    DsDefaultTheme {
        SettingCompassItem()
    }
}