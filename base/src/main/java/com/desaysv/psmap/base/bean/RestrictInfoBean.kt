package com.desaysv.psmap.base.bean

import com.autonavi.gbl.aosclient.model.GReStrictedAreaResponseParam

/**
 * 限行类
 */
class RestrictInfoBean {
    var isRestricted = false
    var title: String? = null
    var tips: String? = null
    var policyName: String? = null
    var cityName: String? = null
    var type = 0
    var isNaving = false
    var cityAdcode = 0
    private var gReStrictedAreaResponseParam: GReStrictedAreaResponseParam? = null
    fun getgReStrictedAreaResponseParam(): GReStrictedAreaResponseParam? {
        return gReStrictedAreaResponseParam
    }

    fun setgReStrictedAreaResponseParam(gReStrictedAreaResponseParam: GReStrictedAreaResponseParam?) {
        this.gReStrictedAreaResponseParam = gReStrictedAreaResponseParam
    }
}
