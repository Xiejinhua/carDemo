package com.desaysv.psmap.model.bean.iflytek

data class TimeSpan(
    val leftClosure: String?, //指定天数下限
    val rightClosure: String?, // 指定天数上限 如3-5天，则leftClosure为3、rightClosure为5
    val type: String?, //"RANGE"
)
