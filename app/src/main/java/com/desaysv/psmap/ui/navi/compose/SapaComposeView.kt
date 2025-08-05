package com.desaysv.psmap.ui.navi.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.guide.model.NaviFacility
import com.autonavi.gbl.guide.model.NaviFacilityType
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.NavigationUtil
import com.desaysv.psmap.ui.compose.utils.overScrollVertical
import com.desaysv.psmap.ui.theme.DsDefaultTheme

/**
 * @author 谢锦华
 * @time 2024/2/22
 * @description 服务区列表
 *
 * type: 1=模拟导航 2=真实导航 3=高速服务区详细页面
 */
@Composable
fun SapaComposeListView(sapaList: List<NaviFacility>, type: Int, onItemClick: (type: Int) -> Unit = {}) {
    val context = LocalContext.current
    val mf =
        if (sapaList.size > 2) {
            Modifier
                .background(Color.Transparent)
                .fillMaxSize()
                .overScrollVertical()
        } else {
            Modifier
                .background(Color.Transparent)
                .fillMaxSize()
        }
    LazyColumn(modifier = mf) {
        itemsIndexed(sapaList) { index, sapaData ->
            //type: 1=模拟导航 2=真实导航 3=高速服务区详细页面
            SapaComposeView(sapaData, type, onItemClick)
            //item上添加间隔
            val dp32 = dimensionResource(com.desaysv.psmap.base.R.dimen.sv_dimen_32)
            if (index < sapaList.size - 1) {
                Spacer(modifier = Modifier.height(dp32))
            }
        }
    }
}

@Composable
fun SapaComposeView(sapaData: NaviFacility, type: Int, onItemClick: (type: Int) -> Unit = {}) {
    val context = LocalContext.current
    val sapaHeight = dimensionResource(com.desaysv.psmap.base.R.dimen.sv_dimen_180)
    val sapaDetailsHeight = dimensionResource(com.desaysv.psmap.base.R.dimen.sv_dimen_180)
    val sapaWidth = dimensionResource(com.desaysv.psmap.base.R.dimen.sv_dimen_640)
    val sapaDetailsWidth = dimensionResource(com.desaysv.psmap.base.R.dimen.sv_dimen_560)
    val iconSize = dimensionResource(if (type == 3) com.desaysv.psmap.base.R.dimen.sv_dimen_56 else com.desaysv.psmap.base.R.dimen.sv_dimen_64)
    val tollgateIconSize =
        dimensionResource(if (type == 3) com.desaysv.psmap.base.R.dimen.sv_dimen_56 else com.desaysv.psmap.base.R.dimen.sv_dimen_64)


    val serviceAreaBg =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_facility_service_area_bg_night else R.drawable.ic_navi_facility_service_area_bg_day)
    val companyBg =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_facility_tollgate_bg_night else R.drawable.ic_navi_facility_tollgate_bg_day)

    val chargingIcon =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_charging_night else R.drawable.ic_navi_sapa_charging_day)
    val gasIcon = painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_gas_night else R.drawable.ic_navi_sapa_gas_day)
    val hotelIcon = painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_hotel_night else R.drawable.ic_navi_sapa_hotel_day)
    val repairIcon = painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_repair_night else R.drawable.ic_navi_sapa_repair_day)
    val repastIcon = painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_repast_night else R.drawable.ic_navi_sapa_repast_day)
    val shoppingIcon =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_shopping_night else R.drawable.ic_navi_sapa_shopping_day)
    val toiletIcon = painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_toilet_night else R.drawable.ic_navi_sapa_toilet_day)
    val tollgateIcon =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_tollgate_night else R.drawable.ic_navi_sapa_tollgate_day)
    val serviceAreaIcon =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_navi_sapa_night else R.drawable.ic_navi_sapa_day)

    val textMarginH = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_20)
    val textSpacerH = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_6)
    val fontSizeName = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_34)
    val fontSizeContent = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_36)
    val fontSizeTimeContent = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)

    var interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    ConstraintLayout(
        modifier = Modifier
            .size(
                if (type == 3) sapaDetailsWidth else sapaWidth,
                if (type == 3) sapaDetailsHeight else sapaHeight
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onItemClick(sapaData.type) }
            )
            .alpha(if (isPressed && type == 2) 0.8f else 1f)
    ) {
        val (topBox, contentBox, remainDistText, remainTimeText) = createRefs()
        Image(
            painter = if (sapaData.type == NaviFacilityType.NaviFacilityTypeServiceArea) serviceAreaBg else companyBg,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Text(
            fontSize = fontSizeName.value.sp,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            text = sapaData.name,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.constrainAs(contentBox) {
                top.linkTo(parent.top, margin = if (type == 3) 32.dp else 28.dp)
                start.linkTo(parent.start, margin = if (type == 3) 20.dp else 30.dp)
                end.linkTo(remainDistText.start, margin = 40.dp)
                width = Dimension.fillToConstraints  // 设置宽度自适应
            }
        )

        // 为非数字部分设置字体大小
        val finalAnnotatedString = buildAnnotatedString {
            NavigationUtil.meterToStr(context, sapaData.remainDist.toLong()).forEach { char ->
                if (char.isDigit()) {
                    withStyle(style = SpanStyle(fontSize = fontSizeContent.value.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)) {
                        append(char.toString())
                    }
                } else {
                    withStyle(style = SpanStyle(fontSize = fontSizeName.value.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)) {
                        append(char.toString())
                    }
                }
            }
        }

        BasicText(
            text = finalAnnotatedString,
            modifier = Modifier.constrainAs(remainDistText) {
                end.linkTo(parent.end, margin = 24.dp)
                top.linkTo(parent.top, margin = if (type == 3) 32.dp else 64.dp)
            }
        )
        if (type == 3) {
            Text(
                fontSize = fontSizeTimeContent.value.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                text = CommonUtils.secondToStr(context, sapaData.remainTime),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.constrainAs(remainTimeText) {
                    top.linkTo(remainDistText.bottom, margin = 18.dp)
                    end.linkTo(remainDistText.end)
                }
            )
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(iconSize)
            .constrainAs(topBox) {
                bottom.linkTo(parent.bottom, margin = 29.dp)
                start.linkTo(parent.start)
                end.linkTo(remainTimeText.start, margin = 10.dp)
                width = Dimension.fillToConstraints  // 设置宽度自适应
            }
        ) {
            if (sapaData.type == NaviFacilityType.NaviFacilityTypeServiceArea) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(if (type == 3) 20.dp else 30.dp))
                    if ((sapaData.sapaDetail and 0b1000000) > 0) {
                        Image(
                            painter = chargingIcon, contentDescription = null, modifier = Modifier
                                .size(iconSize)
                        )
                    }
                    if ((sapaData.sapaDetail and 0b0000001) > 0) {
                        Image(
                            painter = gasIcon, contentDescription = null, modifier = Modifier
                                .size(iconSize)
                        )
                    }
                    if ((sapaData.sapaDetail and 0b0001000) > 0) {
                        Image(
                            painter = repairIcon, contentDescription = null, modifier = Modifier
                                .size(iconSize)
                        )
                    }
                    if ((sapaData.sapaDetail and 0b0000100) > 0) {
                        Image(
                            painter = toiletIcon, contentDescription = null, modifier = Modifier
                                .size(iconSize)
                        )
                    }
                    if ((sapaData.sapaDetail and 0b0000010) > 0) {
                        Image(
                            painter = repastIcon, contentDescription = null, modifier = Modifier
                                .size(iconSize)
                        )
                    }
                    if ((sapaData.sapaDetail and 0b0100000) > 0) {
                        Image(
                            painter = hotelIcon, contentDescription = null, modifier = Modifier
                                .size(iconSize)
                        )
                    }
                    if ((sapaData.sapaDetail and 0b0010000) > 0) {
                        Image(
                            painter = shoppingIcon, contentDescription = null, modifier = Modifier
                                .size(iconSize)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(if (type == 3) 20.dp else 30.dp))
                    Image(
                        painter = tollgateIcon, contentDescription = null, modifier = Modifier
                            .size(tollgateIconSize)
                    )
                }
            }
        }

    }

}


/*=============================================分割线 下面是预览代码 测试用================================================*/
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    widthDp = 640, heightDp = 510
)
@Composable
fun SapaPreview() {
    val sapaList = arrayListOf<NaviFacility>()
    sapaList.add(NaviFacility().apply {
        type = NaviFacilityType.NaviFacilityTypeServiceArea
        name = "仲恺服务区"
        sapaDetail = 0b1111111
        remainDist = 978
        remainTime = 1000
    })
    sapaList.add(NaviFacility().apply {
        type = NaviFacilityType.NaviFacilityTypeTollGate
        name = "收费站"
        remainDist = 978
        remainTime = 1000
    })
    val context = LocalContext.current
    val selectedItemIndex = rememberSaveable { mutableStateOf(0) }
    DsDefaultTheme(true) {
        SapaComposeListView(sapaList, 2, onItemClick = {

        })
    }
}