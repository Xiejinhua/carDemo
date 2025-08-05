package com.desaysv.psmap.model.bean.iflytek

import okio.Source

/**
 * 收藏路书的语音请求
 */
data class SlotsTravelPlanCollect(
    val insType: String?, //"COLLECT" ,"CANCEL_COLLECT"
)