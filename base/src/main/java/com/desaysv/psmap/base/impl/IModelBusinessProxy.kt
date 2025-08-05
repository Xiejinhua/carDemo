package com.desaysv.psmap.base.impl

/**
 * model business 提供接口给bese模块使用
 */
interface IModelBusinessProxy {
    suspend fun init()

    fun unInit()
}