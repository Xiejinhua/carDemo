package com.desaysv.psmap.model.bean

import com.txzing.sdk.bean.UserInfo

// 定义UserPosition差异结果数据类
data class UserPositionDifference(
    val newList: List<UserInfo> = emptyList(),  // 新列表的元素
    val changedElements: List<UserInfo> = emptyList()  // 同一user_id但字段变化的元素对 (旧, 新)
) {
    fun isEmpty(): Boolean {
        return changedElements.isEmpty()
    }
}