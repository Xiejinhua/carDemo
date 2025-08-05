package com.desaysv.psmap.model.bean

data class ScenicSectorBean(
    val code: Int,
    val msg: String,
    val data: Data?,
    val traceId: String
)

data class Data(
    val id: Int,
    val caption: String,
    val logo: String,
    val geo: Geo,
    val distance: Double,
    val tts: String,
    val description: String
)

data class Geo(
    val lng: Double,
    val lat: Double
)
