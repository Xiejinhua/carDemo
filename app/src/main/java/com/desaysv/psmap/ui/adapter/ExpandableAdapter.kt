package com.desaysv.psmap.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import java.util.BitSet

/**
 * Created by wangmansheng
 */
abstract class ExpandableAdapter<G : RecyclerView.ViewHolder, C : RecyclerView.ViewHolder> :
    NestedAdapter<G, C>() {
    public val isCollapsed = BitSet()
    public override fun getSafeChildCount(groupIndex: Int): Int {
        return if (isExpanded(groupIndex)) {
            super.getSafeChildCount(groupIndex)
        } else 0
    }

    fun collapseGroup(groupIndex: Int) {
        if (groupIndex < 0 || groupIndex >= safeGroupCount) {
            return
        }
        if (isExpanded(groupIndex)) {
            notifyChildItemRangeRemoved(groupIndex, 0, getSafeChildCount(groupIndex))
            isCollapsed.set(groupIndex)
        }
    }

    fun expandGroup(groupIndex: Int) {
        Timber.d("groupIndex:$groupIndex")
        if (groupIndex < 0 || groupIndex >= safeGroupCount) {
            return
        }
        if (isExpanded(groupIndex) == false) {
            isCollapsed.clear(groupIndex)
            notifyChildItemRangeInserted(groupIndex, 0, getSafeChildCount(groupIndex))
        }
    }

    fun isExpanded(groupIndex: Int): Boolean {
        return if (groupIndex < 0 || groupIndex >= safeGroupCount) {
            false
        } else !isCollapsed[groupIndex]
    }

    fun collapseAllGroup() {
        val groupCount = safeGroupCount
        isCollapsed[0, groupCount] = true
        notifyDataSetChanged()
    }

    fun expandAllGroup() {
        isCollapsed.clear()
        notifyDataSetChanged()
    }
}
