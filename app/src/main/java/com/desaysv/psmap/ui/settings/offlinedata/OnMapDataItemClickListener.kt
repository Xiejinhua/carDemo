package com.desaysv.psmap.ui.settings.offlinedata

/**
 * Created by wangmansheng.
 */
interface OnMapDataItemClickListener {
    fun onStartClick(groupPosition: Int, childPosition: Int, arCode: Int)
    fun onItemClick(groupPosition: Int, childPosition: Int, arCode: Int)

    /**
     * 列表group点击
     */
    fun onGroupClick(groupPosition: Int)
}
