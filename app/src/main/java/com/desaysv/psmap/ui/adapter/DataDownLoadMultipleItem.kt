package com.desaysv.psmap.ui.adapter

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 * 离线数据下载列表类型--用于多布局
 */
data class DataDownLoadMultipleItem(override val itemType: Int) : MultiItemEntity {
    companion object {
        const val TYPE_PROVINCE = 1
        const val TYPE_CITY = 2
    }
}