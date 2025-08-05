package com.desaysv.psmap.model.bean

data class EngineerLogStatusBean(
    var isPosLog: Boolean,
    var isAlcLog: Boolean,
    var logLevel: Int,
    var clusterLog: Boolean,
    var ttsLog: Boolean,
    var alcLogLevel: Int
)
