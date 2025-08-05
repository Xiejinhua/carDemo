package com.desaysv.psmap.ui.home.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.desaysv.psmap.base.R
import com.desaysv.psmap.model.utils.NaviLaneUtil
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.desaysv.psmap.ui.theme.extraColors

/**
 * @author 谢锦华
 * @time 2024/2/22
 * @description 车道线列表
 */
@Composable
fun CruiseLaneComposeView(laneList: List<Int>) {
    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.extraColors.cruiseLaneBg,
                shape = RoundedCornerShape(
                    bottomEnd = dimensionResource(id = R.dimen.sv_dimen_12),
                    bottomStart = dimensionResource(id = R.dimen.sv_dimen_12)
                )
            ), horizontalArrangement =
        Arrangement
            .Center
    ) {
        itemsIndexed(laneList) { index, laneType ->
            val laneIcon = NaviLaneUtil.setLaneImg(laneType)
            NaviLaneComposeView(index, laneIcon, laneList.size)
        }
    }
}

@Composable
fun NaviLaneComposeView(index: Int, laneType: Int, size: Int) {
    val height = dimensionResource(id = R.dimen.sv_dimen_80)
    val width = dimensionResource(id = R.dimen.sv_dimen_58)
    val spanSize = dimensionResource(id = R.dimen.sv_dimen_24)
    val spanSize8 = dimensionResource(id = R.dimen.sv_dimen_8)
    Row(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        if (index != 0 && size < 10) {
            if (size <= 7) {
                Spacer(modifier = Modifier.width(spanSize))
            } else {
                Spacer(modifier = Modifier.width(spanSize8))
            }

        }
        Image(
            painter = painterResource(laneType),
            contentDescription = "NaviLaneImage",
            modifier = Modifier.size(width, height)
        )
    }
}


/*=============================================分割线 下面是预览代码 测试用================================================*/
@Preview(
    showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode", widthDp = 528, heightDp = 68
)
@Composable
fun NaviLanePreview() {
    val laneList = arrayListOf<Int>()
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_0)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_20)
    laneList.add(com.desaysv.psmap.model.R.drawable.global_image_landfront_21)
    DsDefaultTheme(true) {
        LazyRow(modifier = Modifier.background(Color.Black), horizontalArrangement = Arrangement.Center) {
            itemsIndexed(laneList) { index, laneType ->
                NaviLaneComposeView(index, laneType, 3)
            }
        }
    }
}