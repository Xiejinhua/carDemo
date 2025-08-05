package com.desaysv.psmap.base.net.utils

import com.desaysv.psmap.base.net.utils.BeanUtil.objectToMap
import com.desaysv.psmap.base.net.utils.ShaUtil.getSHA256
import timber.log.Timber
import java.util.Locale
import java.util.TreeMap

object SignUtil {
    /**
     * 加密签名
     * @param object
     * @param key
     * @return
     */
    fun sign(`object`: Any?, key: String?): String? {
        val data = objectToMap(`object`)
        return if (data == null || data.isEmpty()) {
            null
        } else sign(data, key)
    }

    /**
     * 签名
     *
     * @param data
     * @param key  appid
     * @return
     */
    fun sign(data: Map<String?, Any?>?, key: String?): String? {
        if (data == null || data.isEmpty()) {
            return null
        }
        val buf = StringBuilder()
        val map = TreeMap(data)
        for ((k, v) in map) {
            if ("class" == k || "key" == k || "sign" == k) {
                continue
            }
            if (v == null || "" == v.toString()) {
                continue
            }
            buf.append(k)
            buf.append("=")
            buf.append(v)
            buf.append("&")
        }
        buf.append("key=").append(key)
        Timber.d("-------------- SHA256签名前 ------------- buf %s", buf.toString())
        return getSHA256(buf.toString()).uppercase(Locale.getDefault())
    }

    /**
     * @param data      验签的参数
     * @param keySecret 密钥
     * @return String 签名后的值
     * @Description： 停简单请求参数签名算法
     * @author Xiquan.Liu@desay-svautomotive.com
     * @Date 2019年7月31日上午10:03:58
     */
    fun easyParkingSign(
        data: Map<String, Any>?,
        keySecret: String
    ): String {
        val sb = StringBuilder()
        val map =
            TreeMap(data)
        val it: Iterator<Map.Entry<String, Any>> =
            map.entries.iterator()
        while (it.hasNext()) {
            val (k, v) = it.next()
            if ("signType" == k || "sign" == k) {
                continue
            }
            if (v == null || "" == v.toString()) {
                continue
            }
            sb.append(k)
            sb.append("=")
            sb.append(v)
            sb.append("&")
        }
        val strA = sb.substring(0, sb.length - 1)
        // TODO 密钥需要提供
        val strB = strA + keySecret
        return getSHA256(strB).lowercase(Locale.getDefault())
    }
}
