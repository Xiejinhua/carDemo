package com.desaysv.psmap.model.bean.standard

/**
 * 巡航播报模式设置
 */
data class CruiseBroadcastOperaData(
    val naviCruiseType: Int = 0, //0: 全部1：路况播报2：电⼦眼播报3：安全警示4: 前⽅路况+电⼦眼播报5: 前⽅路况+安全提醒6: 电⼦眼播报+安全提醒
    val operaType: Int = 0, //0：打开1：关闭
    var isSuccess: Boolean = false //0 设置失败1 成功
) : ResponseDataData()
