package com.desaysv.psmap.ui.share.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.ui.compose.utils.overScrollVertical
import com.desaysv.psmap.ui.theme.DsDefaultTheme

/**
 * @author 谢锦华
 * @time 2024/11/16
 * @description
 */

@Composable
fun PhoneNumComposeListView(
    phoneList: List<String>,
    refreshView: Int,
    onContinueClicked: (phone: String) -> Unit
) {
    val context = LocalContext.current
    if (refreshView == 1) {
        val mf =
            if (phoneList.size > 6) {
                Modifier
                    .background(Color.Transparent)
                    .fillMaxSize()
                    .overScrollVertical()
            } else {
                Modifier
                    .background(Color.Transparent)
                    .fillMaxSize()
            }
        LazyColumn(
            modifier = mf
        ) {
            itemsIndexed(phoneList) { index, phone ->
                PhoneNumComposeView(index, phone, onContinueClicked)
                //item下添加下划线
                val dp1 = dimensionResource(id = R.dimen.sv_dimen_1)
                val dp32 = dimensionResource(id = R.dimen.sv_dimen_32)
                if (index < phoneList.size - 1) {
                    Divider(color = MaterialTheme.colorScheme.outline, thickness = dp1, modifier = Modifier.padding(start = dp32, end = dp32))
                }
            }
        }
    } else {
        NotPhoneNumComposeView()
    }
}

@Composable
fun PhoneNumComposeView(
    index: Int,
    phone: String,
    onContinueClicked: (phone: String) -> Unit
) {
    var interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val dp20 = dimensionResource(id = R.dimen.sv_dimen_20)
    val sp36 = dimensionResource(id = R.dimen.sv_dimen_36).value.sp
    val dp40 = dimensionResource(id = R.dimen.sv_dimen_40)
    val dp56 = dimensionResource(id = R.dimen.sv_dimen_56)
    val dp120 = dimensionResource(id = R.dimen.sv_dimen_120)
    val formattedPhone = CommonUtils.getFormattedPhone(phone)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dp120)
            .padding(start = dp40, end = dp40),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formattedPhone,
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = sp36,
            modifier = Modifier.weight(1f) // 让 Text 占满剩余空间
        )
        Image(
            painter = painterResource(
                if (NightModeGlobal.isNightMode()) com.desaysv.psmap.R.drawable.ic_share_message_night
                else com.desaysv.psmap.R.drawable.ic_share_message_day
            ),
            contentDescription = "PhoneNumImg",
            modifier = Modifier
                .size(dp56)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    onContinueClicked(formattedPhone)
                }
        )

    }
}

@Composable
fun NotPhoneNumComposeView() {
    val dp40 = dimensionResource(id = R.dimen.sv_dimen_40)
    val dp86 = dimensionResource(id = R.dimen.sv_dimen_86)
    val dp200 = dimensionResource(id = R.dimen.sv_dimen_200)
    val sp28 = dimensionResource(id = R.dimen.sv_dimen_28).value.sp
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = dp86),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(
                if (NightModeGlobal.isNightMode()) com.desaysv.psmap.R.drawable.ic_screen_status_graph_text_night
                else com.desaysv.psmap.R.drawable.ic_screen_status_graph_text_day
            ),
            contentDescription = "NotPhoneNumImg",
            modifier = Modifier
                .size(dp200)
        )
        val tip = stringResource(com.desaysv.psmap.R.string.sv_navi_tripshare_no_historical_contacts)
        Text(
            text = tip,
            color = MaterialTheme.colorScheme.onSecondary,
            fontSize = sp28,
            modifier = Modifier
                .padding(top = dp40)
        )
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
fun PhoneNumPreview() {
    DsDefaultTheme(true) {
//        PhoneNumComposeView(0, "13512745568", onContinueClicked =
//        {
//
//        })
        NotPhoneNumComposeView()
    }
}
