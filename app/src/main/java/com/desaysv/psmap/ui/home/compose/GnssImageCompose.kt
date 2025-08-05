package com.desaysv.psmap.ui.home.compose

import android.content.res.Configuration
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.location.GnssStatusCompat
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import timber.log.Timber

@Composable
fun GnssImageCompose(modifier: Modifier, gnssInfo: List<FloatArray>) {
    val context = LocalContext.current
    val beidouBitmap = BitmapFactory.decodeResource(
        context.resources, if (NightModeGlobal.isNightMode()) R.drawable
            .ic_gnss_beidou_night else R.drawable.ic_gnss_beidou_day
    ).asImageBitmap()
    val gpsBitmap = BitmapFactory.decodeResource(
        context.resources, if (NightModeGlobal.isNightMode()) R.drawable
            .ic_gnss_gps_night else R.drawable.ic_gnss_gps_day
    ).asImageBitmap()
    val glonassBitmap = BitmapFactory.decodeResource(
        context.resources, if (NightModeGlobal.isNightMode()) R.drawable
            .ic_gnss_glonass_night else R.drawable.ic_gnss_glonass_day
    ).asImageBitmap()
    val ufoBitmap = BitmapFactory.decodeResource(
        context.resources, if (NightModeGlobal.isNightMode()) R.drawable
            .ic_gnss_ufo_night else R.drawable.ic_gnss_ufo_day
    ).asImageBitmap()
    val imageSize = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_12).value.toInt()
    Timber.i("gnssInfo = $gnssInfo")
    Canvas(modifier = modifier) {
        gnssInfo.forEach { data ->
            val bitmap = when (data[0].toInt()) {
                GnssStatusCompat.CONSTELLATION_BEIDOU -> beidouBitmap
                GnssStatusCompat.CONSTELLATION_GPS -> gpsBitmap
                GnssStatusCompat.CONSTELLATION_GLONASS -> glonassBitmap
                else -> ufoBitmap
            }
            val s = size.width
            val elev = data[1]
            val azim = data[2]

            val radius = (s / 2.0f) * (1.0f - (elev / 90.0f))
            val angle = Math.toRadians(azim.toDouble())

            val x = ((s / 2.0f) + (radius * Math.sin(angle))).toInt()
            val y = ((s / 2.0f) - (radius * Math.cos(angle))).toInt()
            Timber.i("gnssInfo x=$x y=$y")
            drawImage(image = bitmap, dstOffset = IntOffset(x, y), dstSize = IntSize(imageSize, imageSize))
        }

    }

}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    widthDp = 210,
    heightDp = 210,
)
@Composable
fun GnssImageComposePreview() {
    DsDefaultTheme() {
        Box(Modifier.fillMaxSize()) {
            val data = mutableListOf(
                floatArrayOf(5f, 30f, 60f),
                floatArrayOf(1f, 60f, 100f),
                floatArrayOf(3f, 80f, 200f)
            )
            GnssImageCompose(Modifier.fillMaxSize(), data)
        }
    }
}