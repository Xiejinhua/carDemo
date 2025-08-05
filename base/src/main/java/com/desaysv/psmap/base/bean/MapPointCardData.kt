package com.desaysv.psmap.base.bean

import androidx.annotation.IntDef
import com.autosdk.bussiness.common.POI

/**
 * poi卡片数据类
 */
class MapPointCardData(@PoiCardType var cardType: Int, var poi: POI) {
    var showStatus: Boolean = false
    var showError: Boolean = false
    var showLoading: Boolean = false

    //交通信息
    var traffic_layertag: Int? = null
    var traffic_picurl: String? = null
    var traffic_head: String? = null
    var traffic_desc: String? = null
    var traffic_infotimeseg: String? = null
    var traffic_infostartdate: String? = null
    var traffic_infoenddate: String? = null
    var traffic_lastupdate: String? = null
    var traffic_nick: String? = null


    @IntDef(
        PoiCardType.TYPE_LONG_CLICK,
        PoiCardType.TYPE_LABEL,
        PoiCardType.TYPE_CAR_LOC,
        PoiCardType.TYPE_FAVORITE,
        PoiCardType.TYPE_BOTTLE,
        PoiCardType.TYPE_TRAFFIC,
        PoiCardType.TYPE_CHARGE,
        PoiCardType.TYPE_POI
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class PoiCardType {
        companion object {
            //地图上长按
            const val TYPE_LONG_CLICK = 1

            //地图上Label点击  这个有id和name
            const val TYPE_LABEL = 2

            //车标点击
            const val TYPE_CAR_LOC = 3

            //收藏点
            const val TYPE_FAVORITE = 4

            //漂流瓶
            const val TYPE_BOTTLE = 5

            //交通事件
            const val TYPE_TRAFFIC = 6

            //充电桩
            const val TYPE_CHARGE = 7

            //普通POI
            const val TYPE_POI = 8
        }
    }
}
