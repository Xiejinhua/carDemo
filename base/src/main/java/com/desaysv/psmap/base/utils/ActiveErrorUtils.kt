package com.desaysv.psmap.base.utils

import com.autonavi.gbl.util.errorcode.Activation

//激活失败提示
object ActiveErrorUtils {

    fun activeErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            Activation.AUTO_UNKNOWN_ERROR -> "非法操作错误"
            Activation.ErrorCodeCheckCode -> "激活码验证失败"
            Activation.ErrorCodeInputDeviceId -> "设备ID输入有误"
            Activation.ErrorCodeInputUserCode -> "序列号输入有误"
            Activation.ErrorCodeLoginCode -> "激活码输入有误"
            Activation.ErrorCodeFileSyncFail -> "生成激活文件失败"
            Activation.ErrorCodeFileFlushFail -> "激活文件保存失败"
            Activation.ErrorCodeNetActivateUnknown -> "未知错误--服务内部错误"
            Activation.ErrorCodeNetActivateSuccess -> "请求成功，返回网络激活码、序列号"
            Activation.ErrorCodeNetActivateUUIDNull -> "uuid不能为空"
            Activation.ErrorCodeNetActivateUUIDError -> "uuid位数错误，必须48位"
            Activation.ErrorCodeNetActivateItemNumNotExist -> "项目编号有误，不存在"
            Activation.ErrorCodeNetActivateItemNumNotNetActivate -> "项目编号对应的项目不是网络激活方式"
            Activation.ErrorCodeNetActiveDeviceIdNotExist -> "uuid对应的硬件号不存在"
            Activation.ErrorCodeNetActiveDeviceIdNotPass -> "订单还没有审批通过"
            Activation.ErrorCodeNetActiveOrdersNotEnough -> "订单数量不足"
            Activation.ErrorCodeNetActiveOverActivation -> "超过激活次数，一个硬件码至多激活三次"
            Activation.ErrorCodeNetActiveError -> "激活算法导致的失败"
            else -> "未知错误"
        }
    }
}
