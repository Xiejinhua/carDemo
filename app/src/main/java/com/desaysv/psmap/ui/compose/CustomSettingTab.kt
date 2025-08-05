package com.desaysv.psmap.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.ui.theme.DsDefaultTheme

/**
 * 设置tab
 */
@Composable
fun CustomSettingTab(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    inlineTextContent: Map<String, InlineTextContent>,
    selectTab: Boolean,
    onSelectListener: () -> Unit = {},
    height: Dp = dimensionResource(id = R.dimen.sv_dimen_90)
) {
    val context = LocalContext.current
    val dp14 = dimensionResource(id = R.dimen.sv_dimen_14)
    val dp60 = dimensionResource(id = R.dimen.sv_dimen_60)
    val dp400 = dimensionResource(id = R.dimen.sv_dimen_400)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(dp400, height)
            .background(
                color = colorResource(id = getTabBg(selectTab || isPressed)),
                shape = RoundedCornerShape(dp14)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelectListener
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = colorResource(id = getTabFontColor(selectTab)),
            fontSize = CommonUtils.getAutoDimenValueSP(context, R.dimen.sv_dimen_28).sp,
            modifier = Modifier
                .padding(start = dp60)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onSelectListener
                ), inlineContent = inlineTextContent
        )
    }

}

fun getTabFontColor(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        if (isSelect) android.R.color.white else com.desaysv.psmap.model.R.color.customColorSettingRbCheckedNight
    } else {
        com.desaysv.psmap.model.R.color.onPrimaryDay
    }
}

fun getTabBg(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        if (isSelect) com.desaysv.psmap.model.R.color.customColorNotConfirmNight else android.R.color.transparent
    } else {
        if (isSelect) com.desaysv.psmap.model.R.color.customColorSettingRbBgDay else android.R.color.transparent
    }
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
fun CustomSettingTabPreview() {
    DsDefaultTheme {
        val dp31 = dimensionResource(id = R.dimen.sv_dimen_31)
        val dp32 = dimensionResource(id = R.dimen.sv_dimen_32)
        val dp63 = dimensionResource(id = R.dimen.sv_dimen_63)
        val settingString = buildAnnotatedString {
            appendInlineContent(id = "imageId")
            append(stringResource(id = com.desaysv.psmap.R.string.sv_setting_rb0))
        }
        val settingIcon = painterResource(
            id = if (NightModeGlobal.isNightMode())
                com.desaysv.psmap.R.drawable.ic_setting_rb_selected_night else com.desaysv.psmap.R.drawable.ic_setting_rb_selected_day
        )
        val inlineContentSettingMap = mapOf(
            "imageId" to InlineTextContent(
                Placeholder(width = dp63.value.sp, height = dp32.value.sp, PlaceholderVerticalAlign.Center)
            ) {
                Image(
                    painter = settingIcon,
                    modifier = Modifier
                        .padding(end = dp31)
                        .fillMaxSize(),
                    contentDescription = ""
                )
            }
        )
        CustomSettingTab(text = settingString, inlineTextContent = inlineContentSettingMap, selectTab = true)
    }
}