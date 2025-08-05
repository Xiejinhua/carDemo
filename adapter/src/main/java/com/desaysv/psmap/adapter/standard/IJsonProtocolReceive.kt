package com.desaysv.psmap.adapter.standard

interface IJsonProtocolReceive {
    fun received(jsonString: String?)

    fun receivedSync(jsonString: String?): String?
}