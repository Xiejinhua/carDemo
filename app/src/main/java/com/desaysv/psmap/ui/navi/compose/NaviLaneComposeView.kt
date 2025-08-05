package com.desaysv.psmap.ui.navi.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.bean.TBTLaneInfoBean
import com.desaysv.psmap.model.utils.NaviLaneUtil
import com.desaysv.psmap.ui.theme.DsDefaultTheme

/**
 * @author 谢锦华
 * @time 2024/2/22
 * @description 车道线列表
 */
@Composable
fun NaviLaneComposeListView(tbtLaneInfoList: List<TBTLaneInfoBean>, showCrossView: Boolean) {
    val context = LocalContext.current
    LazyRow(
        modifier = Modifier.background(Color.Transparent),
        horizontalArrangement = Arrangement.Center
    ) {
        itemsIndexed(tbtLaneInfoList) { index, tbtLaneInfo ->
            val laneIcon =
                if (tbtLaneInfo.isTollBoothsLane) {
                    if (showCrossView)
                        NaviLaneUtil.getCrossTollBoothsResId(tbtLaneInfo.laneAction)
                    else
                        NaviLaneUtil.getTollBoothsResId(tbtLaneInfo.laneAction)
                } else {
                    if (showCrossView)
                        NaviLaneUtil.setCrossLaneImg(tbtLaneInfo.laneAction)
                    else
                        NaviLaneUtil.setLaneImg(tbtLaneInfo.laneAction)
                }
            NaviLaneComposeView(laneIcon, tbtLaneInfo.isRecommend, showCrossView)
            //item右侧添加间隔
            val dp0 = dimensionResource(id = R.dimen.sv_dimen_0)
            val dp9 = dimensionResource(id = R.dimen.sv_dimen_9)
            val dp24 = dimensionResource(id = R.dimen.sv_dimen_24)
            if (index < tbtLaneInfoList.size - 1) {
                if (tbtLaneInfo.isTollBoothsLane) {
                    Spacer(modifier = Modifier.width(if (tbtLaneInfoList.size >= 8) dp9 else dp24))
                } else {
                    Spacer(modifier = Modifier.width(if (tbtLaneInfoList.size >= 8) if (tbtLaneInfoList.size > 9) dp0 else dp9 else dp24))
                }
            }
        }
    }
}

@Composable
fun NaviLaneComposeView(laneType: Int, isRecommend: Boolean, showCrossView: Boolean) {
    val context = LocalContext.current
    val dp58 = dimensionResource(id = R.dimen.sv_dimen_58)
    val dp80 = dimensionResource(id = R.dimen.sv_dimen_80)
    // 创建上下渐变的背景
    val gradientBrushDay = Brush.verticalGradient(
        colors = listOf(Color(0x33929CAA), Color(0x33929CAA))
    )
    val gradientBrushCrossDay = Brush.verticalGradient(
        colors = listOf(Color(0x4DFFFFFF), Color(0x4DFFFFFF))
    )
//    val gradientBrushNight = Brush.verticalGradient(
//        colors = listOf(Color(0x33929CAA), Color(0x33929CAA))
//    )
//    val gradientBrushCrossNight = Brush.verticalGradient(
//        colors = listOf(Color(0x00FFFFFF), Color(0x00FFFFFF))
//    )
    val transparent = Brush.verticalGradient(
        colors = listOf(Color.Transparent, Color.Transparent)
    )
    val colorBg =
        if (isRecommend)
            if (showCrossView) gradientBrushCrossDay else gradientBrushDay
        else
            transparent
    Box(
        modifier = Modifier
            .background(
                brush = colorBg,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.sv_dimen_8))
            )
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(laneType),
            contentDescription = "NaviLaneImage",
            modifier = Modifier.size(dp58, dp80)
        )
    }
}


/*=============================================分割线 下面是预览代码 测试用================================================*/
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Dark Mode",
    widthDp = 640, heightDp = 80
)
@Composable
fun NaviLanePreview() {
    val laneList = arrayListOf<Int>()
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_0)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_20)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_21)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_21)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_21)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_21)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_etc)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_toll)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_21)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_21)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_21)
    val context = LocalContext.current
    val selectedItemIndex = rememberSaveable { mutableStateOf(0) }
    DsDefaultTheme(true) {
        LazyRow(
            modifier = Modifier.background(Color.Transparent),
            horizontalArrangement = Arrangement.Center
        ) {
            itemsIndexed(laneList) { index, laneType ->
                NaviLaneComposeView(laneType, true, false)
                //item右侧添加间隔
                val dp0 = dimensionResource(id = R.dimen.sv_dimen_0)
                val dp9 = dimensionResource(id = R.dimen.sv_dimen_9)
                val dp24 = dimensionResource(id = R.dimen.sv_dimen_24)
                if (index < laneList.size - 1) {
                    Spacer(modifier = Modifier.width(if (laneList.size >= 8) if (laneList.size > 9) dp0 else dp9 else dp24))
                }
            }
        }
    }
}