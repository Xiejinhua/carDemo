package com.desaysv.psmap.ui.compose

import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.ui.theme.DsDefaultTheme

/**
 * 导航设置开关项（文字（打开关闭）加背景）
 */
@Composable
fun CustomSwitchItem(
    modifier: Modifier = Modifier,
    text: String = "123",
    fontColor: Int = com.desaysv.psmap.model.R.color.onPrimaryNight,
    fontSize: Int = R.dimen.sv_dimen_26,
    width: Dp = dimensionResource(id = R.dimen.sv_dimen_193),
    height: Dp = dimensionResource(id = R.dimen.sv_dimen_62),
    background: Int = android.R.color.transparent,
    radius: Dp = dimensionResource(id = R.dimen.sv_dimen_0),
    hasPressed: Boolean = true,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val alphaValue = if (hasPressed) {
        if (isPressed.value) 0.8f else 1.0f
    } else 1.0f
    Box(
        modifier = modifier
            .size(width, height)
            .alpha(alphaValue)
            .background(color = colorResource(id = background), shape = RoundedCornerShape(radius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            color = colorResource(id = fontColor),
            fontSize = CommonUtils.getAutoDimenValueSP(context, fontSize).sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun CustomSwitchBrushItem(
    modifier: Modifier = Modifier,
    description: String = "",
    text: String = "123",
    fontColor: Int = com.desaysv.psmap.model.R.color.onPrimaryNight,
    fontSize: Int = R.dimen.sv_dimen_26,
    fontStyle: Boolean = false,
    width: Dp = dimensionResource(id = R.dimen.sv_dimen_168),
    height: Dp = dimensionResource(id = R.dimen.sv_dimen_86),
    bgPicture: Int = com.desaysv.psmap.R.drawable.ic_setting_btn_blank_bg,
    hasPressed: Boolean = true,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed = interactionSource.collectIsPressedAsState()
    val alphaValue = if (hasPressed) {
        if (isPressed.value) 0.8f else 1.0f
    } else 1.0f

    ConstraintLayout(modifier = modifier
        .size(width, height)) {
        val (btnBg, btnText) = createRefs()
        AndroidView(
            modifier = Modifier
                .size(width, height)
                .alpha(alphaValue)
                .constrainAs(btnBg) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            factory = { context -> ImageView(context).apply {
                setImageResource(bgPicture) // 加载 .9 图
                scaleType = ImageView.ScaleType.FIT_XY
            } },
            update = {
                it.setImageResource(bgPicture)
            }
        )
        Text(
            text = text,
            color = colorResource(id = fontColor),
            fontSize = CommonUtils.getAutoDimenValueSP(context, fontSize).sp,
            style = TextStyle(fontWeight = if (fontStyle) FontWeight(500) else FontWeight(350)), // 设置字体加粗
            modifier = Modifier.constrainAs(btnText) {
                top.linkTo(btnBg.top)
                bottom.linkTo(btnBg.bottom)
                start.linkTo(btnBg.start)
                end.linkTo(btnBg.end)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomSwitchItemPreview() {
    DsDefaultTheme {
        CustomSwitchItem(
            modifier = Modifier,
            text = stringResource(id = com.desaysv.psmap.R.string.sv_setting_navi_park_close),
            fontColor = customSwitchItemFontColor(true),
            background = customSwitchItemBg(true),
            radius = dimensionResource(id = R.dimen.sv_dimen_35)
        )
    }
}