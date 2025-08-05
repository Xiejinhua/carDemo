package com.desaysv.psmap.base.net.bean

import java.io.Serializable

class BaseRequestHeader(
    /**
     * 后台提供给调用者的应用Id
     */
    private val appKey: String,
    /**
     * 签名值,防参数篡改,签名业务参数
     */
    private val sign: String,
    /**
     * 毫秒级别时间戳(13位)
     */
    private val timestamp: Long
) : Serializable
