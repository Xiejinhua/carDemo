package com.desaysv.psmap.model.bean

import com.txzing.sdk.bean.UserInfo

// 定义UserInfo差异结果数据类
data class UserInfoDifference(
    val onlyInOldList: List<UserInfo> = emptyList(),  // 仅存在于旧列表的元素
    val onlyInNewList: List<UserInfo> = emptyList(),  // 仅存在于新列表的元素
    val changedElements: List<Pair<UserInfo, UserInfo>> = emptyList()  // 同一user_id但字段变化的元素对 (旧, 新)
) {
    fun isEmpty(): Boolean {
        return onlyInOldList.isEmpty() && onlyInNewList.isEmpty() && changedElements.isEmpty()
    }
}