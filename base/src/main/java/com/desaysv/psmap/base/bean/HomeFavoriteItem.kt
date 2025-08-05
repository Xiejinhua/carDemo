package com.desaysv.psmap.base.bean

import androidx.annotation.IntDef
import com.autosdk.bussiness.common.POI

data class HomeFavoriteItem(
    @HomeFavoriteItemType
    var type: Int,
    var poi: POI
)

@IntDef(
    HomeFavoriteItemType.NULL_HOME,
    HomeFavoriteItemType.NULL_COMPANY,
    HomeFavoriteItemType.HOME,
    HomeFavoriteItemType.COMPANY,
    HomeFavoriteItemType.FAVORITE,
    HomeFavoriteItemType.NULL_FAVORITE,
)
@Retention(AnnotationRetention.SOURCE)
annotation class HomeFavoriteItemType {
    companion object {
        const val NULL_HOME = 1

        const val NULL_COMPANY = 2

        const val HOME = 3

        const val COMPANY = 4

        const val FAVORITE = 5

        const val NULL_FAVORITE = 6

    }
}
