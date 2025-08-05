package com.desaysv.psmap.model.bean

import com.desaysv.psmap.base.utils.BaseConstant


data class IssueFeedbackBean(
    var type: Int = BaseConstant.TYPE_ISSUE_POS,
    var name: String? = null,
    var dec: String? = null
)


