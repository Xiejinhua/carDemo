package com.desaysv.psmap.base.bean

import com.autosdk.bussiness.common.POI

data class HomeCardTipsData(
    var type: HomeCardTipsType,
    var title: String,
    var content: String,
    var buttonText: String,
    var timeStamp: Long = System.currentTimeMillis(),
    var poi: POI? = null
)

enum class HomeCardTipsType {
    CONTINUE_NAVI,
    OPEN_TRAFFIC_RESTRICTION,
    TRAFFIC_RESTRICTION,
    DOWNLOAD_OFFLINE_DATA,
    LONG_TERM_OFFLINE,
    LOW_FUEL,
    SEND_TO_CAR_POI,//手机推送POI
    SEND_TO_CAR_ROUTE,//手机推送路线
    FORECAST_POI,//猜你想去
    TRAVEL_RECOMMEND_POI,//快速出发
    PREDICT_HOME,
    PREDICT_COMPANY,
}
