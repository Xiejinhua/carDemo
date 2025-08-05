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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.utils.CommonUtils

//自定义Button
@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    description: String = "",
    text: String = "",
    textColor: Color = colorResource(id = com.desaysv.psmap.model.R.color.onPrimaryNight),
    textSize: Dp = dimensionResource(id = R.dimen.sv_dimen_28),
    width: Dp = dimensionResource(id = R.dimen.sv_dimen_300),
    height: Dp = dimensionResource(id = R.dimen.sv_dimen_66),
    background: Color = colorResource(id = com.desaysv.psmap.model.R.color.primaryContainerNight),
    enabled: Boolean = true,
    radius: Dp = dimensionResource(id = R.dimen.sv_dimen_0),
    borderWidth: Dp = dimensionResource(id = R.dimen.sv_dimen_0),
    borderColor: Color = Color.Transparent,
    borderRadius: Dp = dimensionResource(id = R.dimen.sv_dimen_0),
    wrapContentSize: Alignment = Alignment.Center,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
        ), label = ""
    )
    Box(
        modifier = modifier
            .size(width, height)
            .alpha(if (isPressed) 0.8f else 1.0f)
            .scale(scale)
            .semantics {
                contentDescription = description
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(borderRadius)
            )
            .background(color = background, shape = RoundedCornerShape(radius)),
        contentAlignment = wrapContentSize
    ) {
        Text(
            text = text,
            style = TextStyle(
                color = textColor,
                fontSize = textSize.value.sp
            )
        )
    }
}

//有icon和文字的button，上icon下文字
@Composable
fun CustomImageTextButton(
    modifier: Modifier = Modifier,
    type: String = "ctvStrategyDefault",
    width: Dp = dimensionResource(id = R.dimen.sv_dimen_128),
    height: Dp = dimensionResource(id = R.dimen.sv_dimen_128),
    icon: Int = com.desaysv.psmap.R.drawable.ic_route_avoid_congestion_icon_night,
    iconSize: Dp = dimensionResource(id = R.dimen.sv_dimen_80),
    iconTop: Dp = dimensionResource(id = R.dimen.sv_dimen_20),
    name: String = "",
    background: Int = com.desaysv.psmap.R.drawable.bg_navi_top_menu_day,
    borderWidth: Dp = dimensionResource(id = R.dimen.sv_dimen_0),
    borderColor: Color = Color.Transparent,
    borderRadius: Dp = dimensionResource(id = R.dimen.sv_dimen_0),
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val modifierLayout: Modifier = Modifier
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
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .semantics {
                contentDescription = type
            }
            .scale(scale)
    ) {
        val (bg, image, text) = createRefs()
        Image(painter = painterResource(id = background),
            contentDescription = "",
            modifier = modifierLayout
                .size(width, height)
                .border(
                    width = borderWidth,
                    color = borderColor,
                    shape = RoundedCornerShape(borderRadius)
                )
                .constrainAs(bg) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
                .alpha(if (isPressed) 0.7f else 1.0f)
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
                .alpha(if (isPressed) 0.7f else 1.0f)
        )
        Text(
            text = name,
            color = colorResource(id = setTextColor()),
            fontSize = CommonUtils.getAutoDimenValueSP(context, R.dimen.sv_dimen_24).sp,
            modifier = modifierLayout
                .constrainAs(text) {
                    start.linkTo(bg.start)
                    end.linkTo(bg.end)
                    top.linkTo(image.bottom)
                }
                .alpha(if (isPressed) 0.7f else 1.0f)
                .clearAndSetSemantics {  }
        )
    }
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun CustomImageTextButtonPreview() {
    CustomImageTextButton(onClick = {

    })
}