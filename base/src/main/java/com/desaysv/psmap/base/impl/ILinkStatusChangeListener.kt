package com.desaysv.psmap.base.impl

/**
 * 手车连接状态通知
 * @author AutoSDK
 */
interface ILinkStatusChangeListener {
    /**
     * 手车手机连接状态变化通知
     * @param isLink
     */
    fun notifyLinkPhoneStatus(isLink: Boolean)
}
