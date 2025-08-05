package com.desaysv.psmap.ui.route.compose

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.base.R
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import timber.log.Timber

/**
 * @author 谢锦华
 * @time 2024/1/19
 * @description 搜索列表
 */
@Composable
fun SearchComposeListView(
    searchList: List<POI>,
    selectedItemIndex: MutableState<Int>,
    onContinueClicked: (position: Int) -> Unit
) {
    val context = LocalContext.current
    val dp600 = dimensionResource(id = R.dimen.sv_dimen_600)
    LazyColumn(
        modifier = Modifier
            .background(Color.Transparent)
            .requiredHeightIn(max = dp600)//设置compose最大高度
    ) {
        itemsIndexed(searchList) { index, searchPoi ->
            SearchComposeView(index, searchPoi, selectedItemIndex, onContinueClicked)
            //item下添加下划线
            val dp2 = dimensionResource(id = R.dimen.sv_dimen_2)
            val dp16 = dimensionResource(id = R.dimen.sv_dimen_16)
            if (index < searchList.size - 1) {
                Divider(color = MaterialTheme.colorScheme.outline, thickness = dp2, modifier = Modifier.padding(start = dp16, end = dp16))
            }
        }
    }
}

@Composable
fun SearchComposeView(
    index: Int,
    searchPoi: POI,
    selectedItemIndex: MutableState<Int>,
    onContinueClicked: (position: Int) -> Unit
) {
    val context = LocalContext.current
    val isSelected = index == selectedItemIndex.value
    var interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val dp16 = dimensionResource(id = R.dimen.sv_dimen_16)
    val dp120 = dimensionResource(id = R.dimen.sv_dimen_120)

    Timber.i("SearchComposeView is called ")
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                selectedItemIndex.value = index
                onContinueClicked(index)
            }
            .background(
                color = if (isSelected || isPressed) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                shape = RoundedCornerShape(dp16)
            )
            .fillMaxWidth()
            .height(dp120)
    ) {
        searchConstraintScheme(searchPoi)
    }
}


/**
 * 路线数据界面
 */
@Composable
fun searchConstraintScheme(searchPoi: POI) {
    val context = LocalContext.current
    val dp16 = dimensionResource(id = R.dimen.sv_dimen_16)
    val dp21 = dimensionResource(id = R.dimen.sv_dimen_21)
    val sp24 = dimensionResource(id = R.dimen.sv_dimen_24).value.sp
    val sp28 = dimensionResource(id = R.dimen.sv_dimen_28).value.sp

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = dp21, start = dp16, end = dp16)
    ) {

        // 创建引用
        val (poiNameText, addressText) = createRefs()

        //POIName
        Text(
            text = searchPoi.name,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = sp28,
            modifier = Modifier
                .constrainAs(poiNameText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints  // 设置宽度自适应
                },
            maxLines = 1, // 设置最大行数
            overflow = TextOverflow.Ellipsis
        )
        //address
        Text(
            text = searchPoi.addr,
            color = MaterialTheme.colorScheme.onSecondary,
            fontSize = sp24,
            modifier = Modifier
                .constrainAs(addressText) {
                    start.linkTo(poiNameText.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom, margin = dp21)
                    width = Dimension.fillToConstraints  // 设置宽度自适应
                },
            maxLines = 1, // 设置最大行数
            overflow = TextOverflow.Ellipsis
        )
    }
}

/*=============================================分割线 下面是预览代码 测试用================================================*/
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Dark Mode",
    widthDp = 1920, heightDp = 1080
)
@Composable
fun SearchPreview() {
    val searchList = arrayListOf<POI>()
    searchList.add(POI().apply {
        name = "鸟巢"
        addr = "三季度卡的卡号点拉会"
    })
    searchList.add(POI().apply {
        name = "sdad"
        addr = "阿阿斯顿阿斯达阿斯达"
    })
    searchList.add(POI().apply {
        name = " 阿斯达啊"
        addr = "三季阿阿斯顿阿斯度卡的卡号点拉会"
    })

    val selectedItemIndex = rememberSaveable { mutableStateOf(0) }
    DsDefaultTheme(true) {
        SearchComposeListView(
            searchList, selectedItemIndex,
            onContinueClicked = {
            })
    }
}