package com.desaysv.psmap.base.business

import com.autosdk.common.AutoStatus

/**
 * 发送地图状态中间适配器
 */
object AutoStatusAdapter {
    private var callback: IStatusCallback? = null

    fun sendStatus(@AutoStatus status: Int, statusDetails: Int = -1) {
        callback?.onStatus(status, statusDetails)
    }

    fun setStatusCallback(statusCallback: IStatusCallback?) {
        callback = statusCallback
    }

    interface IStatusCallback {
        fun onStatus(autoStatus: Int, statusDetails: Int)
    }
}