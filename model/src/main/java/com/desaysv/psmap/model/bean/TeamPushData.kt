package com.desaysv.psmap.model.bean

import com.autosdk.bussiness.common.POI

//组队推送--包括消息推送或者目的地变更通知
data class TeamPushData(
    var title: String,
    var content: String,
    var btnText: String,
    var code: String? = null,
    var poi: POI? = null
) //type: 0.消息推送 1.目的地变更
