package com.desaysv.psmap.ui.route.compose

import android.content.res.Configuration
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.widget.route.model.RoutePathItemContent
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import timber.log.Timber

/**
 * @author 谢锦华
 * @time 2024/1/13
 * @description 路线数据列表
 */
@Composable
fun RouteComposeListView(
    routeList: List<RoutePathItemContent>,
    selectedItemIndex: MutableState<Int>,
    onContinueClicked: (position: Int) -> Unit,
    onRouteDetailsClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    LazyColumn(modifier = Modifier.background(Color.Transparent)) {
        itemsIndexed(routeList) { index, routeData ->
            RouteComposeView(index, routeData, selectedItemIndex, onContinueClicked, onRouteDetailsClicked)
            //item下添加下划线
            val dp1 = dimensionResource(id = R.dimen.sv_dimen_1)
            val dp40 = dimensionResource(id = R.dimen.sv_dimen_40)
//            if (index < routeList.size - 1) {
            Divider(color = MaterialTheme.colorScheme.outline, thickness = dp1, modifier = Modifier.padding(start = dp40, end = dp40))
//            }
        }
    }
}

@Composable
fun RouteComposeView(
    index: Int,
    route: RoutePathItemContent,
    selectedItemIndex: MutableState<Int>,
    onContinueClicked: (position: Int) -> Unit,
    onRouteDetailsClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val isSelected = index == selectedItemIndex.value
    var interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val dp16 = dimensionResource(id = R.dimen.sv_dimen_16)
    val dp181 = dimensionResource(id = R.dimen.sv_dimen_181)

    Timber.i("RouteComposeView is called ")
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onContinueClicked(index)
                selectedItemIndex.value = index
            }
            .background(
                color = if (isSelected || isPressed) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
            )
            .fillMaxWidth()
            .height(dp181)
    ) {
        constraintScheme(index, route, interactionSource, isSelected, isPressed, onRouteDetailsClicked = {
            Timber.i("RouteComposeView onRouteDetailsClicked isSelected = $isSelected ")
//            if (!isSelected) {
//                onContinueClicked(index)
//                selectedItemIndex.value = index
//            } else {
            onRouteDetailsClicked()
//            }
        })
    }
}


/**
 * 路线数据界面
 */
@Composable
fun constraintScheme(
    index: Int, route: RoutePathItemContent,
    interactionSource: MutableInteractionSource,
    isSelected: Boolean,
    isPressed: Boolean,
    onRouteDetailsClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val dp1 = dimensionResource(id = R.dimen.sv_dimen_1)
    val dp3 = dimensionResource(id = R.dimen.sv_dimen_3)
    val dp4 = dimensionResource(id = R.dimen.sv_dimen_4)
    val dp8 = dimensionResource(id = R.dimen.sv_dimen_8)
    val dp20 = dimensionResource(id = R.dimen.sv_dimen_20)
    val dp21 = dimensionResource(id = R.dimen.sv_dimen_21)
    val dp24 = dimensionResource(id = R.dimen.sv_dimen_24)
    val sp24 = dimensionResource(id = R.dimen.sv_dimen_24).value.sp
    val dp26 = dimensionResource(id = R.dimen.sv_dimen_26)
    val dp30 = dimensionResource(id = R.dimen.sv_dimen_30)
    val sp32 = dimensionResource(id = R.dimen.sv_dimen_32).value.sp
    val sp30 = dimensionResource(id = R.dimen.sv_dimen_30).value.sp
    val dp32 = dimensionResource(id = R.dimen.sv_dimen_32)
    val sp40 = dimensionResource(id = R.dimen.sv_dimen_40).value.sp
    val dp40 = dimensionResource(id = R.dimen.sv_dimen_40)
    val sp42 = dimensionResource(id = R.dimen.sv_dimen_42).value.sp
    val dp56 = dimensionResource(id = R.dimen.sv_dimen_56)
    val dp64 = dimensionResource(id = R.dimen.sv_dimen_64)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = dp40)
    ) {

        // 创建引用
        val (titleBox, spacerCenter, spacerBottom, timeText, disText, trafficText, moneyText, routeDetailsSelectImge) = createRefs()
        /**
         *  Chain链接约束,Compose提供了ChainStyle
         *  1.Spread:链条中每个元素均分整个parent空间。
         *  2.SpreadInside:链条中首尾元素紧贴边界，剩下每个元素平分整个parent空间。
         *  3.Packed:链条在所有元素聚焦到中间。
         */
        createVerticalChain(timeText, spacerCenter, disText, spacerBottom, chainStyle = ChainStyle.Packed)
        val isHighLight = index == 0 || (route.content.isNotEmpty() && !route.title.contains("备选"))
        //标题
        ConstraintLayout(modifier = Modifier
            .height(dp40)
            .requiredWidthIn(min = dp56)
            .background(
                Color.Transparent,
                shape = RoundedCornerShape(dp4),
            )
            .border(
                width = dp1,
                color = if (isHighLight)
                    MaterialTheme.colorScheme.onTertiaryContainer
                else MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(dp4)
            )
            .constrainAs(titleBox) {
                top.linkTo(timeText.top)
                start.linkTo(timeText.end, margin = dp24)
                bottom.linkTo(timeText.bottom)
            }) {
            val (titleText) = createRefs()
            Text(
                text = route.title,
                color =
                if (isHighLight)
                    MaterialTheme.colorScheme.onTertiaryContainer
                else
                    MaterialTheme.colorScheme.onBackground,
                fontSize = sp24,
                modifier = Modifier
                    .height(dp40)
                    .constrainAs(titleText) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start, margin = dp8)
                        end.linkTo(parent.end, margin = dp8)
                        bottom.linkTo(parent.bottom)
                    }
            )
        }
        Spacer(modifier = Modifier
            .height(dp26)
            .constrainAs(spacerCenter) {}) // 添加间距

        //行驶路程用时
        Text(
            text = CommonUtils.getTimeStr(context, route.travelTime),
            color = if (isSelected || isPressed)
                MaterialTheme.colorScheme.onTertiaryContainer
            else MaterialTheme.colorScheme.onPrimary,
            fontSize = sp42,
            modifier = Modifier
                .constrainAs(timeText) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
        )
        Spacer(modifier = Modifier
            .height(dp3)
            .constrainAs(spacerBottom) {}) // 添加间距
        //行驶路程公里数
        Text(
            text = CommonUtils.routeResultDistanceEnglish(context, route.length),
            color = if (isSelected || isPressed)
                MaterialTheme.colorScheme.onTertiaryContainer
            else MaterialTheme.colorScheme.onPrimary,
            fontSize = sp32,
            modifier = Modifier
                .constrainAs(disText) {
                    start.linkTo(timeText.start)
                }
        )
        //红绿灯个数
        val trafficLightCountString = buildAnnotatedString {
            appendInlineContent(id = "imageId")
            append(route.trafficLightCount.toString())
        }
        val trafficLightCountImagePainter =
            painterResource(
                id = if (NightModeGlobal.isNightMode())
                    com.desaysv.psmap.R.drawable.ic_route_traffic_lights_night
                else
                    com.desaysv.psmap.R.drawable.ic_route_traffic_lights_day
            )
        val trafficLightCountImagePainterPress =
            painterResource(
                id = if (NightModeGlobal.isNightMode())
                    com.desaysv.psmap.R.drawable.ic_route_traffic_lights_press_night
                else
                    com.desaysv.psmap.R.drawable.ic_route_traffic_lights_press_day
            )

        val inlineContentTrafficLightMap = mapOf(
            "imageId" to InlineTextContent(
                Placeholder(width = sp40, height = sp40, PlaceholderVerticalAlign.Center)
            ) {
                Image(
                    painter = if (isSelected || isPressed)
                        trafficLightCountImagePainterPress
                    else trafficLightCountImagePainter,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentDescription = ""
                )
            }
        )
        Text(
            text = trafficLightCountString,
            color = if (isSelected || isPressed)
                MaterialTheme.colorScheme.onTertiaryContainer
            else MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            fontSize = sp32,
            modifier = Modifier.constrainAs(trafficText) {
                top.linkTo(disText.top)
                start.linkTo(disText.end, margin = dp21)
                bottom.linkTo(disText.bottom)
            }, inlineContent = inlineContentTrafficLightMap
        )

        //高速费
        val highwayMoneyString = buildAnnotatedString {
            appendInlineContent(id = "imageId")
            append(if (route.tollCost.toInt() == 0) "0" else route.tollCost.toString())
        }
        val imagePainter =
            painterResource(
                id = if (NightModeGlobal.isNightMode())
                    com.desaysv.psmap.R.drawable.ic_route_cost_night
                else
                    com.desaysv.psmap.R.drawable.ic_route_cost_day
            )
        val imagePainterPress =
            painterResource(
                id = if (NightModeGlobal.isNightMode())
                    com.desaysv.psmap.R.drawable.ic_route_cost_press_night
                else
                    com.desaysv.psmap.R.drawable.ic_route_cost_press_day
            )

        val inlineContentHighwayMoneyMap = mapOf(
            "imageId" to InlineTextContent(
                Placeholder(width = sp40, height = sp40, PlaceholderVerticalAlign.Center)
            ) {
                Image(
                    painter = if (isSelected || isPressed) imagePainterPress else imagePainter,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentDescription = ""
                )
            }
        )
        Text(
            text = highwayMoneyString,
            color = if (isSelected || isPressed)
                MaterialTheme.colorScheme.onTertiaryContainer
            else MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center,
            fontSize = sp32,
            modifier = Modifier.constrainAs(moneyText) {
                top.linkTo(trafficText.top)
                start.linkTo(trafficText.end, margin = dp8)
                bottom.linkTo(trafficText.bottom)
            }, inlineContent = inlineContentHighwayMoneyMap
        )
        if (isSelected) {
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.90f else 1f,
                animationSpec = tween(
                    durationMillis = 200,
                    easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
                ), label = ""
            )
            Image(painter = painterResource(
                id = if (NightModeGlobal.isNightMode())
                    com.desaysv.psmap.R.drawable.ic_route_details_select_night
                else
                    com.desaysv.psmap.R.drawable.ic_route_details_select_day
            ),
                contentDescription = "",
                modifier = Modifier
                    .alpha(if (isPressed) 0.8f else 1.0f)
                    .scale(scale)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        onRouteDetailsClicked()
                    }
                    .constrainAs(routeDetailsSelectImge) {
                        end.linkTo(parent.end, margin = dp20)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    }
                    .size(dp64)
            )
        }
    }
}

/*=============================================分割线 下面是预览代码 测试用================================================*/
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    name = "Dark Mode",
    widthDp = 640, heightDp = 550
)
@Composable
fun GreetingPreview() {
    val routeList = arrayListOf<RoutePathItemContent>()
    routeList.add(RoutePathItemContent().apply {
        // 为routePathItemContent添加值
        title = "速度最快高速多"
        content = "速度最快高速多"
        travelTime = 91555
        length = 5303
        trafficLightCount = 13
        tollCost = 0
        // 添加其他属性的赋值
    })
    routeList.add(RoutePathItemContent().apply {
        // 为routePathItemContent添加值
        title = "速度最"
        content = "速度最"
        travelTime = 91555
        length = 5303
        trafficLightCount = 13
        tollCost = 3
        // 添加其他属性的赋值
    })
    routeList.add(RoutePathItemContent().apply {
        // 为routePathItemContent添加值
        title = "速度最"
        content = ""
        travelTime = 91555
        length = 5303
        trafficLightCount = 13
        tollCost = 3
        // 添加其他属性的赋值
    })
    val selectedItemIndex = rememberSaveable { mutableStateOf(0) }
    DsDefaultTheme(true) {
        RouteComposeListView(routeList, selectedItemIndex,
            onContinueClicked = {
            })
    }
}

