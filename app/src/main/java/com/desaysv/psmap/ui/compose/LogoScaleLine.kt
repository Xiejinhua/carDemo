package com.desaysv.psmap.ui.compose

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.ui.MainViewModel
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.desaysv.psmap.ui.theme.extraColors
import timber.log.Timber

@Composable
fun LogoScaleLine(
    viewModel: MainViewModel
) {
    val scaleFontSize = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_22).value.sp
    val height = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_40)
    val logo =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_auto_logo_night else R.drawable.ic_auto_logo_day)

    val scaleLineLength by viewModel.scaleLineLength.unPeek()
        .observeAsState(viewModel.getScaleLineLength())
    val zoomLevel by viewModel.zoomLevel.unPeek().observeAsState()
    Timber.i("scaleLineLength=$scaleLineLength zoomLevel=$zoomLevel")
    val themeChange by viewModel.themeChange.unPeek().observeAsState(NightModeGlobal.isNightMode())
    DsDefaultTheme(themeChange) {
        Timber.i("themeChange=$themeChange")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(scaleLineLength.dp)
                    .height(height), contentAlignment = Alignment.Center
            ) {
                DrawLine(MaterialTheme.extraColors.scaleLine)
                Text(
                    modifier = Modifier
                        .height(height)
                        .padding(bottom = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_8)),
                    text = viewModel.getScaleLineLengthDesc(),
                    color = MaterialTheme.extraColors.scaleLine,
                    fontSize = scaleFontSize
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_4)))
            Image(
                painter = logo, contentDescription = "leftIcon", modifier = Modifier
                    .size(
                        width = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_79),
                        height = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_18)
                    )
            )

        }

    }
}

@Composable
fun DrawLine(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 3f
        val strokeWidthHalf = strokeWidth / 2
        drawLine(
            color = color,
            start = Offset(strokeWidthHalf, size.height / 1.5f),
            end = Offset(strokeWidthHalf, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(strokeWidth, size.height - strokeWidthHalf),
            end = Offset(size.width - strokeWidth, size.height - strokeWidthHalf),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(size.width - strokeWidthHalf, size.height / 1.5f),
            end = Offset(size.width - strokeWidthHalf, size.height),
            strokeWidth = strokeWidth
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Dark Mode",
    widthDp = 145,
    heightDp = 60,
)
@Composable
fun LogoScaleLinePreview() {
    DsDefaultTheme() {
        val logo =
            painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_auto_logo_night else R.drawable.ic_auto_logo_day)
        val fontSize = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_28)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(72.dp), contentAlignment = Alignment.Center
            ) {
                DrawLinePreview()
                Text(modifier = Modifier.wrapContentWidth().wrapContentHeight(), text = "500米", fontSize = fontSize.value.sp, color = MaterialTheme.extraColors
                    .scaleLine)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Image(
                painter = logo, contentDescription = "leftIcon", modifier = Modifier
                    .size(
                        width = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_104),
                        height = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_22)
                    )
            )

        }

    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Dark Mode",
    widthDp = 150,
    heightDp = 32,
)
@Composable
fun DrawLinePreview() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 4f
        val strokeWidthHalf = strokeWidth / 2
        drawLine(
            color = Color.White,
            start = Offset(strokeWidthHalf, size.height / 1.5f),
            end = Offset(strokeWidthHalf, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color.White,
            start = Offset(strokeWidth, size.height - strokeWidthHalf),
            end = Offset(size.width - strokeWidth, size.height - strokeWidthHalf),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width - strokeWidthHalf, size.height / 1.5f),
            end = Offset(size.width - strokeWidthHalf, size.height),
            strokeWidth = strokeWidth
        )
    }
}