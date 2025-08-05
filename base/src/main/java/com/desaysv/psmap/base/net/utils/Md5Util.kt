package com.desaysv.psmap.base.net.utils

import timber.log.Timber
import java.security.MessageDigest

object Md5Util {
    private val hexDigits =
        charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    //Md5加密工具
    fun md5(s: String): String? {
        return try {
            val btInput = s.toByteArray(charset("UTF-8"))
            val mdInst = MessageDigest.getInstance("MD5")
            mdInst.update(btInput)
            val md = mdInst.digest()
            val j = md.size
            val str = CharArray(j * 2)
            var k = 0
            for (i in 0 until j) {
                val byte0 = md[i]
                str[k++] = hexDigits[byte0.toInt() ushr 4 and 0xf]
                str[k++] = hexDigits[byte0.toInt() and 0xf]
            }
            String(str)
        } catch (e: Exception) {
            Timber.e("catch Exception: ${e.message}")
            null
        }
    }
}
