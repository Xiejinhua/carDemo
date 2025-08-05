package com.desaysv.psmap.base.net.utils

import timber.log.Timber
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object ShaUtil {
    /**
     * 加密sha256
     * @param str
     * @return
     */
    @JvmStatic
    fun getSHA256(str: String): String {
        val messageDigest: MessageDigest
        var encodestr = ""
        try {
            messageDigest = MessageDigest.getInstance("SHA-256")
            messageDigest.update(str.toByteArray(StandardCharsets.UTF_8))
            encodestr = byte2Hex(messageDigest.digest())
        } catch (e: NoSuchAlgorithmException) {
            Timber.e("ShaUtil-NoSuchAlgorithmException")
        }
        return encodestr
    }

    private fun byte2Hex(bytes: ByteArray): String {
        val stringBuffer = StringBuilder()
        var temp: String
        for (i in bytes.indices) {
            temp = Integer.toHexString(bytes[i].toInt() and 0xFF)
            if (temp.length == 1) {
                stringBuffer.append("0")
            }
            stringBuffer.append(temp)
        }
        return stringBuffer.toString()
    }
}
