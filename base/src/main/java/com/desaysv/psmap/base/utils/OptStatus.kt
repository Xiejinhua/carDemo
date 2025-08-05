package com.desaysv.psmap.base.utils

/**
 * Created by AutoSdk on 2020/11/11.
 */
object OptStatus {
    const val AUTO_UNKNOWN_ERROR = Int.MIN_VALUE

    /**
     * 正常
     */
    const val OPT_DONE = 0

    /**
     * 无网络连接
     */
    const val OPT_NET_DISCONNECT = OPT_DONE + 1

    /**
     * 下载网络异常
     */
    const val OPT_DOWNLOAD_NET_ERROR = OPT_NET_DISCONNECT + 1

    /**
     * 剩余空间小于阈值
     */
    const val OPT_NO_SPACE_LEFTED = OPT_DOWNLOAD_NET_ERROR + 1

    /**
     * 下载量可能大于剩余空间
     */
    const val OPT_SPACE_NOT_ENOUGHT = OPT_NO_SPACE_LEFTED + 1

    /** 操作异常   */
    const val OPT_ERROR = OPT_SPACE_NOT_ENOUGHT + 1
}
