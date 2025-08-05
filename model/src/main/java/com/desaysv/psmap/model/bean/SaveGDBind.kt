package com.desaysv.psmap.model.bean

import com.desaysv.psmap.base.net.bean.BaseRequestBody
import org.apache.commons.codec.digest.DigestUtils
import java.util.Locale
import java.util.TreeSet

/**
 * 指高德账号绑定api接口
 */
class SaveGDBind(//车机账号id
    var vehicleId: String, //第三方账号id
    var bindId: String, //绑定状态,0表示未绑定,1表示已经绑定
    var status: String, //终端设备唯一编号
    var vehicleSN: String
) : BaseRequestBody() {
    var bindExt = "" //存储扩展信息，如第三方token
    private var sign: String = "" //签名，计算方式见目录

    init {
        genSign()
    }

    fun genSign() {
        val builder = StringBuilder().apply {
            val treeSet = TreeSet<String>()
            treeSet.add(vehicleId)
            treeSet.add(bindId)
            treeSet.add(status)
            treeSet.add(vehicleSN)
            for (value in treeSet) {
                append(value)
            }
        }
        sign = DigestUtils.md5Hex(builder.toString().toByteArray()).uppercase(Locale.getDefault())
    }

    override val mapParam: HashMap<String, Any>
        /**
         * 数据组装
         * @return
         */
        get() {
            return HashMap<String, Any>().apply {
                put("vehicleId", vehicleId)
                put("bindId", bindId)
                put("status", status)
                put("vehicleSN", vehicleSN)
                put("sign", sign)
            }
        }
}
