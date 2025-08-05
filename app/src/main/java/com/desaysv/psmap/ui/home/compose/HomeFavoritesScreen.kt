package com.desaysv.psmap.ui.home.compose

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.POIFactory
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.HomeFavoriteItem
import com.desaysv.psmap.base.bean.HomeFavoriteItemType
import com.desaysv.psmap.ui.theme.DsDefaultTheme

@Composable
fun HomeFavoritesScreen(
    favorites: List<HomeFavoriteItem>,
    modifier: Modifier = Modifier,
    onItemClick: (type: HomeFavoriteItem) -> Unit = {},
    onIconClick: () -> Unit = {},
) {
    FavoriteList(favorites, modifier, onItemClick, onIconClick)
}

@Composable
private fun FavoriteList(
    favorites: List<HomeFavoriteItem>,
    modifier: Modifier = Modifier,
    onItemClick: (type: HomeFavoriteItem) -> Unit,
    onIconClick: () -> Unit,
) {
    val titleFontSize = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
    val backIcon =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_back_night else R.drawable.ic_back_day)
    val titleHeight = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_56)
    val backHeight = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_48)
    val dp20 = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_20)
    val interactionSource = remember { MutableInteractionSource() }
    val isPress by interactionSource.collectIsPressedAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_860))
    ) {
        Spacer(modifier = Modifier.height(dp20))
        Box(
            modifier = Modifier
                .padding(
                    start = dp20,
                    end = dp20,
                )
                .height(titleHeight)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart // 设置内容居中
        ) {
            Text(
                modifier = Modifier
                    .height(titleHeight)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                textAlign = TextAlign.Center,
                fontSize = titleFontSize.value.sp,
                text = LocalContext.current.getString(R.string.sv_map_favorite),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Image(
                painter = backIcon, contentDescription = "leftIcon", modifier = Modifier
                    .size(backHeight)
                    .clickable {
                        onIconClick.invoke()
                    }
                    .alpha(if (isPress) 0.8f else 1f)
            )
        }

        if (favorites.isNotEmpty() && favorites[0].type != HomeFavoriteItemType.NULL_FAVORITE) {
            val lazyGridState = rememberLazyGridState()
            val state = rememberScrollAreaState(lazyGridState)
            ScrollArea(state = state) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1), state = lazyGridState
                ) {
                    items(count = favorites.size) { index ->
                        FavoriteListItem(
                            index == favorites.size - 1,
                            favorites[index],
                            onItemClick,
                        )
                    }
                }

                if (favorites.size > 6) {
                    VerticalScrollbar(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .width(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_3))
                    ) {
                        Thumb(
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.outline,
                                RoundedCornerShape(33)
                            ),
                        )
                    }
                }

            }
        } else {
            val noneFavoriteIcon =
                painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_none_favorites_night else R.drawable.ic_none_favorites_day)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_784))
                    .wrapContentWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = noneFavoriteIcon, contentDescription = "noneFavoriteIcon",
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_150))
                )

                Text(
                    modifier = Modifier
                        .padding(
                            top = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
                        )
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = LocalContext.current.getString(R.string.sv_map_favorite_none),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_22).value.sp,
                )
            }
        }
    }

}

@Composable
private fun FavoriteListItem(
    isLast: Boolean,
    favorite: HomeFavoriteItem,
    onItemClick: (type: HomeFavoriteItem) -> Unit,
) {
    val homeIcon = painterResource(R.drawable.ic_home)
    val companyIcon = painterResource(R.drawable.ic_company)
    val favoriteIcon =
        painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_favorite_night else R.drawable.ic_favorite_day)

    /*
        val editIcon =
            painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_common_edit_night else R.drawable.ic_common_edit_day)
        val deleteIcon =
            painterResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_favorite_delete_night else R.drawable.ic_favorite_delete_day)
    */

    val leftIcon = when (favorite.type) {
        HomeFavoriteItemType.FAVORITE, HomeFavoriteItemType.NULL_FAVORITE -> favoriteIcon
        HomeFavoriteItemType.HOME, HomeFavoriteItemType.NULL_HOME -> homeIcon
        HomeFavoriteItemType.COMPANY, HomeFavoriteItemType.NULL_COMPANY -> companyIcon
        else -> {
            favoriteIcon
        }
    }

    val lineHeight = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_126)
    val marginStart = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_20)
    val marginEnd = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_32)
    val marginTopIcon = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_18)
    val marginTopText = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
    val textMarginH = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_4)
    val textSpacerH = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_8)
    val fontSizeName = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_24)
    val fontSizeAddr = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_22)
    val iconSize = dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_48)


    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(lineHeight)
            .clickable { onItemClick(favorite) }
    ) {
        val (leftIconRef, textRef, rightIconRef, dividerRef) = createRefs()

        Image(painter = leftIcon, contentDescription = "leftIcon", modifier = Modifier
            .size(iconSize)
            .constrainAs(leftIconRef) {
                top.linkTo(parent.top, margin = marginTopIcon)
                start.linkTo(parent.start, margin = marginStart)
            })

        Column(modifier = Modifier.constrainAs(textRef) {
            start.linkTo(leftIconRef.end, margin = textMarginH)
            top.linkTo(parent.top, margin = marginTopText)
            end.linkTo(parent.end, margin = marginEnd)
            width = Dimension.fillToConstraints
        }) {
            Text(
                fontSize = fontSizeName.value.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                text = favorite.poi.name,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(textSpacerH))
            Text(
                fontSize = fontSizeAddr.value.sp,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                text = favorite.poi.addr,
                color = MaterialTheme.colorScheme.onSecondary
            )
        }

        if (!isLast) {
            Divider(
                Modifier
                    .height(dimensionResource(id = com.desaysv.psmap.base.R.dimen.sv_dimen_1))
                    .constrainAs(dividerRef) {
                        start.linkTo(parent.start, margin = marginEnd)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end, margin = marginEnd)
                        width = Dimension.fillToConstraints
                    }, color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Dark Mode",
    widthDp = 582,
)
@Composable
fun HomeFavoritesPreview() {
    DsDefaultTheme() {
        val favorites = mutableListOf<HomeFavoriteItem>()
        /*favorites.add(
            0,
            HomeFavoriteItem(HomeFavoriteItemType.FAVORITE, POIFactory.createPOI().apply {
                id = "null_favorite"
                name = "暂无收藏点"
                addr = "暂无收藏点"
            })
        )
        favorites.add(
            1,
            HomeFavoriteItem(HomeFavoriteItemType.FAVORITE, POIFactory.createPOI().apply {
                id = "3"
                name = "暂无收藏点"
                addr = "暂无收藏点"
            })
        )*/
        favorites.add(
            0,
            HomeFavoriteItem(HomeFavoriteItemType.NULL_FAVORITE, POIFactory.createPOI().apply {
                id = "4"
                name = "暂无收藏点"
                addr = "暂无收藏点"
            })
        )
        FavoriteList(favorites, onItemClick = {}, onIconClick = {})
    }
}

