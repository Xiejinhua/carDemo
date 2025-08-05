package com.desaysv.psmap.base.net.bean

import com.desaysv.psmap.base.net.utils.Md5Util

class SdkActivationRequest(private var body: Body, private var header: Header) : BaseRequestBody() {
    private var sign: String? =
        Md5Util.md5("project=" + body.project + "&reqSeq=" + header.reqSeq + "&sn=" + body.sn + "&timestamp=" + header.timestamp)

    override val mapParam: HashMap<String, Any>
        /**
         * 数据组装
         * @return
         */
        get() {
            val params = HashMap<String, Any>()
            val header = HashMap<String, Any?>()
            val body = HashMap<String, Any?>()
            header["reqSeq"] = this.header.reqSeq
            header["timestamp"] = this.header.timestamp
            header["sign"] = sign
            params["header"] = header
            body["appName"] = this.body.appName
            body["sn"] = this.body.sn
            body["project"] = this.body.project
            body["modelId"] = this.body.modelId
            body["lon"] = this.body.lon
            body["lat"] = this.body.lat
            params["body"] = body
            return params
        }

    class Body(
        var modelId: String?,
        var appName: String?,
        var project: String?,
        var sn: String?,
        var lon: String?,
        var lat: String?
    )

    class Header(var reqSeq: String?, var timestamp: Long)
}
