package com.desaysv.psmap.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.autonavi.auto.skin.NightModeGlobal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//垂直的Brush
fun verticalBrush(start: Color, end: Color): Brush {
    return Brush.linearGradient(
        colors = listOf(start, end),
        start = Offset(0f, 0f), // 起始位置在顶部
        end = Offset(0f, Float.POSITIVE_INFINITY) // 结束位置在底部
    )
}

//开关项背景设置
@Composable
fun customSwitchButtonBg(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        if (isSelect) com.desaysv.psmap.model.R.color.primaryContainerSecondaryNight else android.R.color.transparent
    } else {
        if (isSelect) com.desaysv.psmap.model.R.color.customColorRbSelectedBg60Day else android.R.color.transparent
    }
}

//开关项字体颜色设置
fun customSwitchItemFontColor(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        if (isSelect) com.desaysv.psmap.model.R.color.customColorMapDataNumNight else com.desaysv.psmap.model.R.color.customColorWhite80Night
    } else {
        if (isSelect) com.desaysv.psmap.model.R.color.customColorMapDataNumDay else com.desaysv.psmap.model.R.color.customColorRbBgDay
    }
}

//开关项背景设置
fun customSwitchItemBg(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        if (isSelect) com.desaysv.psmap.model.R.color.primaryContainerPressNight else android.R.color.transparent
    } else {
        if (isSelect) com.desaysv.psmap.model.R.color.primaryContainerDay else android.R.color.transparent
    }
}

//按鈕背景设置
fun customButtonItemBg(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        if (isSelect) android.R.color.transparent else com.desaysv.psmap.model.R.color.inversePrimaryNight
    } else {
        if (isSelect) android.R.color.transparent else com.desaysv.psmap.model.R.color.inversePrimaryDay
    }
}

fun customDefaultButtonItemBg(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        if (isSelect) com.desaysv.psmap.model.R.color.primaryContainerSecondaryNight else com.desaysv.psmap.model.R.color.backgroundSearchNight
    } else {
        if (isSelect) com.desaysv.psmap.model.R.color.primaryContainerSecondaryDay else com.desaysv.psmap.model.R.color.backgroundSearchDay
    }
}

//设置按钮选择背景
fun setSettingBtnBg(isSelect: Boolean): Int {
    return if (NightModeGlobal.isNightMode()) {
        com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight
    } else {
        if (isSelect) com.desaysv.psmap.model.R.color.customColorShadowColorDay else android.R.color.transparent
    }
}

//开关项背景设置
@Composable
fun customSwitchItemBrushBg(isSelect: Boolean): Brush {
    return if (NightModeGlobal.isNightMode()) {
        if (isSelect) verticalBrush(
            colorResource(id = com.desaysv.psmap.model.R.color.customColorReportBtnBgNight),
            colorResource(id = com.desaysv.psmap.model.R.color.customColorReportBtnBgNight)
        ) else
            verticalBrush(
                colorResource(id = com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight),
                colorResource(id = com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalNight)
            )
    } else {
        if (isSelect) verticalBrush(
            colorResource(id = com.desaysv.psmap.model.R.color.customColorSettingBtnBgDay),
            colorResource(id = com.desaysv.psmap.model.R.color.customColorSettingBtnBgDay)
        ) else
            verticalBrush(
                colorResource(id = com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalDay),
                colorResource(id = com.desaysv.psmap.model.R.color.customColorSettingBtnBgNormalDay)
            )
    }
}

//防抖
@Composable
fun debounce(
    delayMillis: Long,
    scope: CoroutineScope,
    onDebounce: () -> Unit
): () -> Unit {
    var debounceJob: Job? by remember { mutableStateOf(null) }

    return {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delayMillis)
            onDebounce()
        }
    }
}