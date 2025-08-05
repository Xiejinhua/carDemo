package com.desaysv.psmap.model.impl

import com.txzing.sdk.bean.UserInfo

/**
 * 列表item点击
 */
interface OnItemClickListener {
    /**
     * 列表第几项
     */
    fun onItemClick(position: Int)

    fun onItemClick(userInfo: UserInfo)
}
