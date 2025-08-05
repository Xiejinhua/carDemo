package com.desaysv.psmap.base.net.bean

import com.desaysv.psmap.base.net.NetworkConstants
import com.desaysv.psmap.base.net.utils.SignUtil
import java.io.Serializable

open class BaseRequestBody : Serializable {
    open val mapParam: HashMap<String, Any>
        /**
         * 数据组装
         * @return
         */
        get() {
            val params = HashMap<String, Any>()
            val header = SignUtil.sign(this, NetworkConstants.APP_ID)?.let {
                BaseRequestHeader(
                    NetworkConstants.APP_ID,
                    it,
                    System.currentTimeMillis()
                )
            }
            params[NetworkConstants.KEY_HEADER] = header!!
            params[NetworkConstants.KEY_BODY] = this
            return params
        }
}
