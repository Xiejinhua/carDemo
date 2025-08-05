package com.desaysv.psmap.model.bean.standard

/**
 * 语⾳设置--静音操作
 */
data class VoiceMuteOperaData(
    val actionType: Int,
    val operaType: Int, //1永久静⾳ 2临时静⾳ 3取消永久静⾳ 4取消临时静⾳
) : ResponseDataData()
