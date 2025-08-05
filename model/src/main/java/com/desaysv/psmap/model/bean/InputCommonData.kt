package com.desaysv.psmap.model.bean

data class InputCommonData(
    /**
     * 消息类型，必需使用枚举MassageType的名称
     */
    var massageType: String,
    /**
     * 数据
     */
    var data: String?,
)
