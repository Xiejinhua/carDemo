package com.desaysv.psmap.base.bean

/**
 * @author 张楠
 * @time 2024/12/18
 * @description
 */
open class SearchThrowable(message: String, open val errorCode: Int = 0) : Throwable(message) {
}