package com.desaysv.psmap.model.bean.standard

import com.desaysv.psmap.model.bean.standard.StandardJsonConstant.SERVER_VERSION

data class StandardJsonProtocolData<T>(
    var protocolId: Int,
    var requestCode: String? = "",
    var responseCode: String? = "",
    var needResponse: Boolean = false,
    var versionName: String = SERVER_VERSION,
    var requestAuthor: String = "",
    var messageType: String,
    var statusCode: Int = 0,
    var data: T,
)
