package com.desaysv.psmap.ui.route.restrict.compose

import android.content.res.Configuration
import android.text.Html
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.aosclient.model.GRestrictRule
import com.desaysv.psmap.base.R
import com.desaysv.psmap.ui.compose.utils.overScrollVertical
import com.desaysv.psmap.ui.route.restrict.RestrictViewModel
import com.desaysv.psmap.ui.theme.DsDefaultTheme

/**
 * @author 谢锦华
 * @time 2024/11/21
 * @description
 */
@Composable
fun RestrictComposeListView(viewModel: RestrictViewModel) {
    val restrictCityList by viewModel.restrictCityList.observeAsState(emptyList())

    val dp1003 = dimensionResource(id = R.dimen.sv_dimen_1003)
    val selectedMainItemIndex = rememberSaveable { mutableStateOf(0) }
    val selectedChildItemIndex = rememberSaveable { mutableStateOf(0) }
    LazyColumn(
        modifier = Modifier
            .background(Color.Transparent)
            .requiredHeightIn(max = dp1003)//设置compose最大高度
    ) {
        itemsIndexed(restrictCityList) { mainIndex, restrictData ->
            val composedTitle = "${restrictData.cityName}政策"
            Column {
                restrictData.rules.forEachIndexed { childIndex, restrictRuleData ->
                    RestrictComposeView(
                        mainIndex, childIndex, composedTitle,
                        restrictRuleData, selectedMainItemIndex,
                        selectedChildItemIndex, viewModel
                    )
                }
            }

            RestrictBottomComposePreview(
                mainIndex, composedTitle, restrictData.ruleNums,
                selectedMainItemIndex, selectedChildItemIndex, viewModel
            )

            //item下添加下划线
            val dp0 = dimensionResource(id = R.dimen.sv_dimen_0)
            val dp1 = dimensionResource(id = R.dimen.sv_dimen_1)
            val dp30 = dimensionResource(id = R.dimen.sv_dimen_30)
            val dp40 = dimensionResource(id = R.dimen.sv_dimen_40)
            if (mainIndex < restrictCityList.size - 1) {
                Divider(
                    color = Color.Transparent, thickness = dp1,
                    modifier = Modifier.padding(start = dp40, end = dp40, top = if (mainIndex == 0) dp0 else dp30)
                )
            }
        }
    }

}

@Composable
fun RestrictComposeView(
    mainIndex: Int,
    childIndex: Int,
    composedTitle: String,
    rules: GRestrictRule,
    selectedMainItemIndex: MutableState<Int>,
    selectedChildItemIndex: MutableState<Int>,
    viewModel: RestrictViewModel
) {

    val interactionSource = remember { MutableInteractionSource() }

    val dp0 = dimensionResource(id = R.dimen.sv_dimen_0)
    val dp1 = dimensionResource(id = R.dimen.sv_dimen_1)
    val dp8 = dimensionResource(id = R.dimen.sv_dimen_8)
    val dp10 = dimensionResource(id = R.dimen.sv_dimen_10)
    val dp12 = dimensionResource(id = R.dimen.sv_dimen_12)
    val dp16 = dimensionResource(id = R.dimen.sv_dimen_16)
    val sp24 = dimensionResource(id = R.dimen.sv_dimen_24).value.sp
    val dp24 = dimensionResource(id = R.dimen.sv_dimen_24)
    val dp30 = dimensionResource(id = R.dimen.sv_dimen_30)
    val sp30 = dimensionResource(id = R.dimen.sv_dimen_30).value.sp
    val dp40 = dimensionResource(id = R.dimen.sv_dimen_40)
    val sp34 = dimensionResource(id = R.dimen.sv_dimen_34).value.sp
    val dp92 = dimensionResource(id = R.dimen.sv_dimen_92)


    val title = "$composedTitle${childIndex + 1}"
    val isItemSelected = (mainIndex == selectedMainItemIndex.value) && (childIndex == selectedChildItemIndex.value)
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = dp40, end = dp40, top = if (mainIndex == 0 && childIndex == 0) dp0 else dp30)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                viewModel.onRuleSelected(mainIndex, childIndex)
                selectedMainItemIndex.value = mainIndex
                selectedChildItemIndex.value = childIndex
            }
    ) {

        // 创建引用
        val (titleText, restrictTipText, timeTipText, timeText,
            summaryTipText, summaryText, descTipText, descText) = createRefs()
        val mainColor = if (isItemSelected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimary
        //标题
        Text(
            text = title,
            color = mainColor,
            fontSize = sp34,
            modifier = Modifier
                .constrainAs(titleText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )

        //限行提示  是否在限行
        val isHighLight = rules.effect == 1
        ConstraintLayout(modifier = Modifier
            .requiredWidthIn(min = dp92)
            .height(dp40)
            .background(
                if (isHighLight)
                    MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(dp8),
            )
            .border(
                width = dp1,
                color = if (isHighLight)
                    MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(dp8)
            )
            .constrainAs(restrictTipText) {
                top.linkTo(titleText.top)
                start.linkTo(titleText.end, margin = dp16)
                bottom.linkTo(titleText.bottom)
            }) {
            val (tipText) = createRefs()
            Text(
                text = if (isHighLight) "限行中" else "未限行",
                color = if (isHighLight)
                    MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onPrimary,
                fontSize = sp24,
                modifier = Modifier.constrainAs(tipText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start, margin = dp10)
                    end.linkTo(parent.end, margin = dp10)
                    bottom.linkTo(parent.bottom)
                }
            )
        }

        //限行时间
        val isShowTime = rules.time.isNotEmpty()
        if (isShowTime) {
            Text(
                text = stringResource(com.desaysv.psmap.R.string.sv_route_navi_restrict_time),
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = sp30,
                modifier = Modifier
                    .constrainAs(timeTipText) {
                        top.linkTo(titleText.bottom, margin = dp30)
                        start.linkTo(titleText.start)
                    }
            )
            val times = rules.time.split(";")
            val timeSb = StringBuilder()
            times.forEachIndexed { index, time ->
                if (time.isNotEmpty()) {
                    timeSb.append(time)
                    if (index != times.size - 1) {
                        timeSb.append("\n")
                    }
                }
            }
            val timeResult = timeSb.toString()
            BasicText(
                text = timeResult,
                style = TextStyle(
                    color = mainColor,
                    fontSize = sp30
                ),
                modifier = Modifier
                    .constrainAs(timeText) {
                        top.linkTo(timeTipText.bottom, margin = dp12)
                        start.linkTo(timeTipText.start)
                    }
            )
        }

        //限行区域
        val isSummary = rules.summary.isNotEmpty()
        if (isSummary) {
            Text(
                text = stringResource(com.desaysv.psmap.R.string.sv_route_navi_restrict_region),
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = sp30,
                modifier = Modifier
                    .constrainAs(summaryTipText) {
                        top.linkTo(timeText.bottom, margin = dp24)
                        start.linkTo(timeText.start)
                    }
            )
            val spanned = Html.fromHtml(rules.summary, Html.FROM_HTML_MODE_LEGACY)
            BasicText(
                text = spanned.toString(),
                style = TextStyle(
                    color = mainColor,
                    fontSize = sp30
                ),
                modifier = Modifier
                    .constrainAs(summaryText) {
                        top.linkTo(summaryTipText.bottom, margin = dp12)
                        start.linkTo(summaryTipText.start)
                    }
            )
        }
        //限行规定
        val isDesc = rules.desc.isNotEmpty()
        if (isDesc) {
            Text(
                text = stringResource(com.desaysv.psmap.R.string.sv_route_navi_restrict_stipulate),
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = sp30,
                modifier = Modifier
                    .constrainAs(descTipText) {
                        top.linkTo(summaryText.bottom, margin = dp24)
                        start.linkTo(summaryText.start)
                    }
            )
            val descSpanned = Html.fromHtml(rules.desc, Html.FROM_HTML_MODE_LEGACY)
            BasicText(
                text = descSpanned.toString(),
                style = TextStyle(
                    color = mainColor,
                    fontSize = sp30
                ),
                modifier = Modifier
                    .constrainAs(descText) {
                        top.linkTo(descTipText.bottom, margin = dp12)
                        start.linkTo(descTipText.start)
                    }
            )
        }

    }
}

@Composable
fun RestrictBottomComposePreview(
    mainIndex: Int,
    composedTitle: String,
    ruleNums: Int,
    selectedMainItemIndex: MutableState<Int>,
    selectedChildItemIndex: MutableState<Int>,
    viewModel: RestrictViewModel
) {
    val dp11 = dimensionResource(id = R.dimen.sv_dimen_11)
    val dp21 = dimensionResource(id = R.dimen.sv_dimen_21)
    val sp30 = dimensionResource(id = R.dimen.sv_dimen_30).value.sp
    val dp40 = dimensionResource(id = R.dimen.sv_dimen_40)
    val dp64 = dimensionResource(id = R.dimen.sv_dimen_64)
    val interactionSource = remember { MutableInteractionSource() }
    val isOpenMore = rememberSaveable { mutableStateOf(false) }
    if (ruleNums > 1) {
        Row(
            modifier = Modifier
                .padding(top = dp21, start = dp40)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    val isOpen = isOpenMore.value.not()
                    viewModel.setOpenAndStowRestrictInfo(mainIndex, isOpen)
                    isOpenMore.value = isOpen
                    if (isOpen) {
                        viewModel.onRuleSelected(mainIndex, 1)
                        selectedMainItemIndex.value = mainIndex
                        selectedChildItemIndex.value = 1
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "更多$composedTitle",
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = sp30
            )
            val isNightMode = NightModeGlobal.isNightMode()
            val arrowImage = when {
                isOpenMore.value && isNightMode -> com.desaysv.psmap.R.drawable.ic_arrow_restrict_up_night
                isOpenMore.value -> com.desaysv.psmap.R.drawable.ic_arrow_restrict_up_day
                isNightMode -> com.desaysv.psmap.R.drawable.ic_arrow_restrict_down_night
                else -> com.desaysv.psmap.R.drawable.ic_arrow_restrict_down_day
            }
            Image(
                painter = painterResource(id = arrowImage),
                contentDescription = "",
                modifier = Modifier
                    .size(dp64)
                    .padding(start = dp11)
            )
        }

    }
}


/*=============================================分割线 下面是预览代码 测试用================================================*/
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    widthDp = 544, heightDp = 631
)
@Composable
fun RestrictComposePreview() {
    val rules = GRestrictRule().apply {
        effect = 1 //限行政策state
        time = "2021年1月11日起，工作日和周末全天限行（节假日限制）;7:00-19:00限行" //限行时间
        summary = "西樵山风景区（从西樵山北门（西樵镇综治大楼侧）起及西樵山南门（南海博物馆侧）起所有风景区去专用道路）" //限行区域
        desc = "客车限行<br/>需办理佛山客车通行证"//限行规定
    }
    val selectedMainItemIndex = rememberSaveable { mutableStateOf(0) }
    val selectedChildItemIndex = rememberSaveable { mutableStateOf(1) }
    DsDefaultTheme(true) {
        RestrictComposeView(
            0, 0,
            "佛山市政策", rules, selectedMainItemIndex, selectedChildItemIndex,
            viewModel = hiltViewModel<RestrictViewModel>()
        )
    }
}

