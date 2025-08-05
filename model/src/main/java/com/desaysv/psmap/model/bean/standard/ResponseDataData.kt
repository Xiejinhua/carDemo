package com.desaysv.psmap.model.bean.standard

open class ResponseDataData {
    open var resultCode: Int = StandardJsonConstant.ResponseResult.OK.code
    open var errorMessage: String = StandardJsonConstant.ResponseResult.OK.msg
}