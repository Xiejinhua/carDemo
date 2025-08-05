package com.desaysv.psmap.model.bean.iflytek

import com.desaysv.psmap.base.R
import java.util.ArrayList
import java.util.HashMap

/**
 * 自驾路书查询的语音请求
 */
data class SlotsTravelPlanQuery(
    val endLoc: EndLoc,
    val startLoc: StartLoc,
    val property: Property?,
    val timeSpan: TimeSpan?,
    val datetimeDescr: String?, //节假日
    val tripType: String?,
)
object TravelPlanConstants {
    val  DATE_TIME_DESCR_LIST: Map<String,String> = HashMap<String,String>().apply {
        put("周末","2")
        put("五一","3,5")
        put("国庆","4,7")
        put("端午","2,3")
        put("春节","4,7")
        put("清明","2,3")
    }
    val JETOUR_CUSTOM_CATEGORY_MAP : Map<String,String> = HashMap<String,String>().apply {
        put("驿站","捷途驿站")
        put("越野场地","越野场地")
        put("美食","餐饮")
        put("景区","景区")
        put("酒店","住宿")
        put("冰雪季","当季精选")
        put("咖啡厅","咖啡店")
    }
}

