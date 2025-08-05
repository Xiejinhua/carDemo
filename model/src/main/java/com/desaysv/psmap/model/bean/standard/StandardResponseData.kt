package com.desaysv.psmap.model.bean.standard

/**
 *响应消息
 */
data class StandardResponseData(
    val pkg: String,
    val protocolId: Int,
    val responseCode: String?,
    val messageType: String = "response",
    val statusCode: Int = 0,
    val needResponse: Boolean = false,
    var data: ResponseDataData
)
