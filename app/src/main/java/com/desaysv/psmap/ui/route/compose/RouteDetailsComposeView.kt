package com.desaysv.psmap.ui.route.compose

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.widget.route.model.NaviStationItemData
import com.desaysv.psmap.base.R
import com.desaysv.psmap.ui.compose.CustomCheckbox
import com.desaysv.psmap.ui.compose.utils.overScrollVertical
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import com.desaysv.psmap.utils.RouteActionUtil
import com.desaysv.psmap.utils.RouteSegmentUtil
import timber.log.Timber

/**
 * @author 谢锦华
 * @time 2024/10/18
 * @description
 */

@Composable
fun RouteDetailsListComposeView(
    naviStationFatalist: List<NaviStationItemData>,
    parrySelect: Boolean,
    onToastClicked: () -> Unit,
    onContinueClicked: (naviStationFata: NaviStationItemData, isParrySelect: Boolean) -> Unit
) {
    val dp660 = dimensionResource(id = R.dimen.sv_dimen_660)
    val mf =
        if (naviStationFatalist.size > 7) {
            Modifier
                .background(Color.Transparent)
                .requiredHeightIn(max = dp660)//设置compose最大高度
                .overScrollVertical()
        } else {
            Modifier
                .background(Color.Transparent)
                .requiredHeightIn(max = dp660)//设置compose最大高度
        }
    LazyColumn(
        modifier = mf
    ) {
        itemsIndexed(naviStationFatalist) { index, naviStationFata ->
            RouteDetailsComposeView(index, naviStationFata, naviStationFatalist, parrySelect, onToastClicked, onContinueClicked)
        }
    }
}

@Composable
fun RouteDetailsComposeView(
    index: Int,
    naviStationFata: NaviStationItemData,
    naviStationFatalist: List<NaviStationItemData>,
    parrySelect: Boolean,
    onToastClicked: () -> Unit,
    onContinueClicked: (naviStationFata: NaviStationItemData, isParrySelect: Boolean) -> Unit,
) {
    Timber.i("RouteDetailsComposeView is called ")
    var isChecked by rememberSaveable { mutableStateOf(false) }
    var interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val dp0 = dimensionResource(id = R.dimen.sv_dimen_0)
    val dp2 = dimensionResource(id = R.dimen.sv_dimen_2)
    val sp34 = dimensionResource(id = R.dimen.sv_dimen_34).value.sp
    val sp30 = dimensionResource(id = R.dimen.sv_dimen_30).value.sp
    val dp12 = dimensionResource(id = R.dimen.sv_dimen_12)
    val dp40 = dimensionResource(id = R.dimen.sv_dimen_40)
    val dp64 = dimensionResource(id = R.dimen.sv_dimen_64)
    val dp16 = dimensionResource(id = R.dimen.sv_dimen_16)
    val dp19 = dimensionResource(id = R.dimen.sv_dimen_19)

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                if (parrySelect) {
                    onContinueClicked(naviStationFata, false)
                }
            }
    ) {
        // 创建引用
        val (spacerFirst, spacerSecond, spacerNull, actionIconImg, selectImg, spacerTop, roadNameLL, spacerBottom) = createRefs()
        val color = MaterialTheme.colorScheme.outline
        if (naviStationFata.desType != RouteSegmentUtil.TYPE_START) {
            Canvas(modifier = Modifier
                .width(dp2) // 设置线宽
                .fillMaxHeight()
                .constrainAs(spacerFirst) {
                    start.linkTo(spacerNull.start)
                    end.linkTo(spacerNull.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(spacerNull.top)
                    height = Dimension.fillToConstraints  // 设置高度自适应
                }) {
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f) // 设置虚线样式
                drawLine(
                    color = color, // 线的颜色
                    start = Offset(x = size.width / 2, y = 0f),
                    end = Offset(x = size.width / 2, y = size.height),
                    strokeWidth = 2f,
                    pathEffect = dashPathEffect
                )
            }
        }

        if (naviStationFata.desType != RouteSegmentUtil.TYPE_END) {
            Canvas(modifier = Modifier
                .width(dp2) // 设置线宽
                .fillMaxHeight()
                .constrainAs(spacerSecond) {
                    start.linkTo(spacerNull.start)
                    end.linkTo(spacerNull.end)
                    top.linkTo(spacerNull.bottom)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints  // 设置高度自适应
                }) {
                val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f) // 设置虚线样式
                drawLine(
                    color = color, // 线的颜色
                    start = Offset(x = size.width / 2, y = 0f),
                    end = Offset(x = size.width / 2, y = size.height),
                    strokeWidth = 2f,
                    pathEffect = dashPathEffect
                )
            }
        }
        Spacer(modifier = Modifier
            .size(if (naviStationFata.desType == RouteSegmentUtil.TYPE_START) dp0 else dp40)
            .constrainAs(spacerTop) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                top.linkTo(parent.top)
            })
        //透明的控件，为了给竖线一个参考位置
        Spacer(modifier = Modifier
            .size(dp64)
            .constrainAs(spacerNull) {
                start.linkTo(parent.start)
                top.linkTo(spacerTop.bottom)
                bottom.linkTo(spacerBottom.top)
            }
        )

        if (naviStationFata.desType == RouteSegmentUtil.TYPE_GROUP && parrySelect) {
            CustomCheckbox(
                Modifier.constrainAs(selectImg) {
                    start.linkTo(parent.start)
                    top.linkTo(spacerTop.bottom)
                    bottom.linkTo(spacerBottom.top)
                },
                selectIcon = if (NightModeGlobal.isNightMode())
                    com.desaysv.psmap.R.drawable.route_browser_fragment_icon_select_night else
                    com.desaysv.psmap.R.drawable.route_browser_fragment_icon_select_day,

                unSelectIcon = if (NightModeGlobal.isNightMode())
                    com.desaysv.psmap.R.drawable.route_browser_fragment_icon_unselect_night else
                    com.desaysv.psmap.R.drawable.route_browser_fragment_icon_unselect_day,
                checked = isChecked,
                contentDescription = naviStationFata.roadName,
                onCheckedChange = {
                    val naviStationFataSelect = naviStationFatalist.filter { it.isSelect } as ArrayList<NaviStationItemData>
                    if (!naviStationFata.isSelect && naviStationFataSelect.size == 3) {
                        onToastClicked()
                    } else {
                        isChecked = !isChecked
                        naviStationFata.isSelect = isChecked
                        onContinueClicked(naviStationFata, true)
                    }
                },
                checkboxSize = dp64
            )
        }
//        if (naviStationFata.desType != RouteSegmentUtil.TYPE_GROUP || !parrySelect) {
        Image(
            painter = painterResource(id = RouteActionUtil.getRouteGroupNaviActionIcon(naviStationFata.navigtionAction, true)),
            contentDescription = "",
            modifier = Modifier
                .constrainAs(actionIconImg) {
                    start.linkTo(selectImg.end, margin = dp19)
                    top.linkTo(spacerTop.bottom)
                    bottom.linkTo(spacerBottom.top)
                }
                .size(dp64)
        )
//        }

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .constrainAs(roadNameLL) {
                    start.linkTo(actionIconImg.end, margin = dp16)
                    end.linkTo(parent.end)
                    top.linkTo(actionIconImg.bottom)
                    bottom.linkTo(actionIconImg.top)
                    width = Dimension.fillToConstraints  // 设置宽度自适应
                }
        ) {
            //路名
            Text(
                text = naviStationFata.roadName,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = sp34,
                maxLines = 1, // 设置最大行数
                overflow = TextOverflow.Ellipsis,
            )
            //距离 + 红绿灯个数
            if (naviStationFata.desType == RouteSegmentUtil.TYPE_GROUP) {
                Spacer(modifier = Modifier.height(dp12)) // 设置间距
                Text(
                    text = "${naviStationFata.distanceDes}  ${naviStationFata.groupTrafficDes}",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = sp30,
                    maxLines = 1, // 设置最大行数
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier
            .size(if (naviStationFata.desType == RouteSegmentUtil.TYPE_END) dp0 else dp40)
            .constrainAs(spacerBottom) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            })

    }
}


/*=============================================分割线 下面是预览代码 测试用================================================*/
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    widthDp = 582, heightDp = 450
)
@Composable
private fun RouteDetailsPreview() {
    val routeList = arrayListOf<NaviStationItemData>()
    routeList.add(NaviStationItemData().apply {
        desType = RouteSegmentUtil.TYPE_START
        roadName = "我的位置"
        distanceDes = ""
        index = 0
        actionIcon = com.desaysv.psmap.R.drawable.route_browser_fragment_icon_start
        navigtionAction = RouteActionUtil.DirICon_TYPE_START
    })
    routeList.add(NaviStationItemData().apply {
        index = 1
        desType = RouteSegmentUtil.TYPE_GROUP
        roadName = "张戳四路"
        distanceDes = "15公里"
        actionIcon = com.desaysv.psmap.R.drawable.global_image_action_group9
        groupActionIcon = com.desaysv.psmap.R.drawable.global_image_action_group9
        navigtionAction = RouteActionUtil.DirIcon_Turn_Left
        actionDes = "直行"
        groupTrafficDes = "红绿灯3个"
        // 添加其他属性的赋值
    })
    routeList.add(NaviStationItemData().apply {
        index = 2
        desType = RouteSegmentUtil.TYPE_END
        roadName = "到达终点 张戳加油站"
        distanceDes = ""
        actionIcon = com.desaysv.psmap.R.drawable.route_browser_fragment_icon_end
        navigtionAction = RouteActionUtil.DirICon_TYPE_END
    })
    DsDefaultTheme(true) {
        RouteDetailsListComposeView(
            routeList,
            parrySelect = true,
            onToastClicked = {},
            onContinueClicked = { t, s ->

            })
    }
}