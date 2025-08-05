package com.desaysv.psmap.base.net.bean

import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import java.util.UUID

data class CarPreparationRequestBody(
    var version: String = "0100", //协议版本号，不可为空
    var businessId: String = "100", //业务 ID，不可为空
    var serviceType: String = "backupCar", //服务类型
    var globalId: String = CommonUtils.getSystemProperties(BaseConstant.GLOBAL_ID, "").toString(),// 认证 ID，不可为空
    var requestId: String = UUID.randomUUID().toString().replace("-", ""), //请求 ID，不可为空
    var data: CarPreparationData = CarPreparationData()
)

data class CarPreparationData(
    var backupId: String = "", //一键备车行程 ID
    var userId: String = "", //用户 ID
    var lon: String = "", //经度(GPS)
    var lat: String = ""
) //维度(GPS)
