package com.desaysv.psmap.base.net.bean

data class CarPreparationResult(
    var code: String = "", //状态值
    var message: String = "", //响应信息
    var requestId: String = "", //响应时间戳
    var responseTime: String = ""
) //本次业务服务的请求 ID
