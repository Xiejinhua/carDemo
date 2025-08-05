package com.desaysv.psmap.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = primaryDay,
    onPrimary = onPrimaryDay,
    secondary = secondaryDay,
    onSecondary = onSecondaryDay,
    onSecondaryContainer = onSecondaryContainerDay,
    tertiary = tertiaryDay,
    onTertiary = onTertiaryDay,
    surfaceVariant = surfaceVariantDay,
    primaryContainer = primaryContainerDay,
    secondaryContainer = secondaryContainerDay,
    onPrimaryContainer = onPrimaryContainerDay,
    onTertiaryContainer = onTertiaryContainerDay,
    tertiaryContainer = tertiaryContainerDay,
    onBackground = onBackgroundDay,
    errorContainer = errorContainerDay,
    onErrorContainer = onErrorContainerDay,
    outline = lineDay
)

private val LightColorScheme = lightColorScheme(
    primary = primaryNight,
    onPrimary = onPrimaryNight,
    secondary = secondaryNight,
    onSecondary = onSecondaryNight,
    onSecondaryContainer = onSecondaryContainerNight,
    tertiary = tertiaryNight,
    onTertiary = onTertiaryNight,
    surfaceVariant = surfaceVariantNight,
    primaryContainer = primaryContainerNight,
    secondaryContainer = secondaryContainerNight,
    onPrimaryContainer = onPrimaryContainerNight,
    onTertiaryContainer = onTertiaryContainerNight,
    tertiaryContainer = tertiaryContainerNight,
    onBackground = onBackgroundNight,
    errorContainer = errorContainerNight,
    onErrorContainer = onErrorContainerNight,
    outline = lineNight

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

// 定义额外的颜色
data class ExtraColors(
    val cruiseLaneBg: Color,
    val scaleLine: Color
)

// 创建 CompositionLocal
val LocalExtraColors = compositionLocalOf {
    ExtraColors(
        cruiseLaneBg = Color.Unspecified,
        scaleLine = Color.Unspecified
    )
}

// 扩展 MaterialTheme 以访问额外颜色
val MaterialTheme.extraColors: ExtraColors
    @Composable
    get() = LocalExtraColors.current

@Composable
fun DsDefaultTheme(
    isNight: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    // 定义额外的颜色
    val extraColors = ExtraColors(
        cruiseLaneBg = if (isNight) Color(0x33D0D6DD) else Color(0x33929CAA), // 自定义
        scaleLine = if (isNight) Color(0x4DFFFFFF) else Color(0x99252525) // 自定义
    )

    val colorScheme = when {
        // Dynamic color is available on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isNight) dynamicLightColorScheme(context) else dynamicDarkColorScheme(context)
        }

        isNight -> LightColorScheme
        else -> DarkColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme
    ) {
        CompositionLocalProvider(
            LocalExtraColors provides extraColors,
            content = content
        )
    }
}