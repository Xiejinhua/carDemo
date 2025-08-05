package com.desaysv.psmap.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.R

/**
 * 导航设置开关背景（打开关闭背景）
 */
@Composable
fun CustomSwitchBg(
    modifier: Modifier = Modifier,
    width: Dp = dimensionResource(id = R.dimen.sv_dimen_397),
    height: Dp = dimensionResource(id = R.dimen.sv_dimen_70),
    background: Color = colorResource(id = com.desaysv.psmap.model.R.color.surfaceVariantNight),
    radius: Dp = dimensionResource(id = R.dimen.sv_dimen_0),
    shadowRadius: Dp = dimensionResource(id = R.dimen.sv_dimen_10)
) {
    val color =
        colorResource(id = if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight else com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalDay)
    Box(
        modifier = modifier
            .size(width, height)
            .background(color = background, shape = RoundedCornerShape(radius))
            .shadow(
                elevation = shadowRadius,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.sv_dimen_4)),
                spotColor = color,
                ambientColor = color
            )
    )
}