package com.desaysv.psmap.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.desaysv.psmap.R

@Composable
fun CustomCheckbox(
    modifier: Modifier = Modifier,
    selectIcon: Int = R.drawable.ic_choice_pressed,
    unSelectIcon: Int = R.drawable.ic_choice,
    checkboxSize: Dp = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32),
    checked: Boolean,
    contentDescription: String = "",
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Image(
        painter = painterResource(
            id = when {
                isPressed -> selectIcon
                checked -> selectIcon
                else -> unSelectIcon
            }
        ),
        contentDescription = contentDescription,
        modifier = modifier
            .size(checkboxSize)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onCheckedChange(!checked) }
            )
    )
}