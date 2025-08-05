package com.desaysv.psmap.model.bean.iflytek

import com.google.gson.JsonObject

data class SingleIntentIFlyTek(
    val sid: String,
    val service: String,
    val operation: String,
    val rc: Int? = null,
    val text: String,
    val tts_play_type: String? = null,
    val ttsAction: String? = null,
    val receipt: String? = null,
    val intentIndex: Int = 0,
    val semantic: JsonObject
)