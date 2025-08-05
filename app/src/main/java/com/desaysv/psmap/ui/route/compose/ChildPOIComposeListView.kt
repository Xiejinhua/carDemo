package com.desaysv.psmap.ui.route.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.base.R
import com.desaysv.psmap.ui.theme.DsDefaultTheme

/**
 * @author 谢锦华
 * @time 2025/1/23
 * @description
 */

@Composable
fun ChildPOIComposeListView(
    childPOIList: List<POI>,
    onContinueClicked: (endPoi: POI) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .background(Color.Transparent)
    ) {
        itemsIndexed(childPOIList) { index, childPOI ->
            ChildPOIComposeView(childPOI, onContinueClicked)
        }
    }
}

@Composable
fun ChildPOIComposeView(
    childPOI: POI,
    onContinueClicked: (endPoi: POI) -> Unit
) {
    val sp34 = dimensionResource(id = R.dimen.sv_dimen_34).value.sp
    val dp12 = dimensionResource(id = R.dimen.sv_dimen_12)
    val dp19 = dimensionResource(id = R.dimen.sv_dimen_19)
    val dp24 = dimensionResource(id = R.dimen.sv_dimen_24)
    val dp51 = dimensionResource(id = R.dimen.sv_dimen_51)
    val dp72 = dimensionResource(id = R.dimen.sv_dimen_72)
    val dp169 = dimensionResource(id = R.dimen.sv_dimen_169)
    var interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .height(dp72)
            .requiredWidthIn(dp169)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onContinueClicked(childPOI)
            }
            .background(
                color = if (isPressed) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                shape = RoundedCornerShape(dp12),
            ),
        contentAlignment = Alignment.Center // 设置内容居中
    ) {
        //POIName
        Text(
            text = childPOI.shortname,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = sp34,
            textAlign = TextAlign.Center, // 设置文本居中
            fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Normal, // 设置字体加粗
            modifier = Modifier
                .height(dp51)
                .padding(horizontal = dp19)
        )
    }
    Spacer(modifier = Modifier.width(dp24)) // 添加间距
}

/*=============================================分割线 下面是预览代码 测试用================================================*/
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    widthDp = 818, heightDp = 72
)
@Composable
fun ChildPOIPreview() {
    val searchList = arrayListOf<POI>()
    searchList.add(POI().apply {
        name = "鸟巢"
        addr = "三季度卡的卡号点拉会"
        shortname = "进站口"
    })
    searchList.add(POI().apply {
        name = "sdad"
        addr = "阿阿斯顿阿斯达阿斯达"
        shortname = "出站口"
    })
    searchList.add(POI().apply {
        name = " 阿斯达啊"
        addr = "三季阿阿斯顿阿斯度卡的卡号点拉会"
        shortname = "5P停车场5P停车场5P停车场"
    })

    DsDefaultTheme(true) {
        ChildPOIComposeListView(
            searchList,
            onContinueClicked = {
            })
    }
}