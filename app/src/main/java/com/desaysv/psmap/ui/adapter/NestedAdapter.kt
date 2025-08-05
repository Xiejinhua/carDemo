package com.desaysv.psmap.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

/**
 * Created by wangmansheng
 * 分组adapter，支持多种group布局，多种child布局
 */
abstract class NestedAdapter<G : RecyclerView.ViewHolder, C : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val groupCount = safeGroupCount
        for (i in 0 until groupCount) {
            var type = getGroupItemViewType(i)
            if (type == -viewType) {
                return onCreateGroupViewHolder(parent, type)
            }
            val childCount = getSafeChildCount(i)
            for (j in 0 until childCount) {
                type = getChildItemViewType(i, j)
                if (type == viewType) {
                    return onCreateChildViewHolder(parent, type)
                }
            }
        }
        throw IllegalArgumentException("Invalid viewType: $viewType")
    }


    override fun getItemId(position: Int): Long {
        val groupCount = safeGroupCount
        var count = 0
        for (i in 0 until groupCount) {
            if (count == position) {
                return getGroupItemId(i)
            }
            count++
            val childCount = getSafeChildCount(i)
            if (count + childCount > position) {
                return getChildItemId(position - count)
            }
            count += childCount
        }
        return super.getItemId(position)
    }

    fun getGroupItemId(position: Int): Long {
        return RecyclerView.NO_ID
    }

    fun getChildItemId(position: Int): Long {
        return RecyclerView.NO_ID
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val groupCount = safeGroupCount
        var count = 0
        for (i in 0 until groupCount) {
            if (count == position) {
                onBindGroupViewHolder(holder as G, i)
                return
            }
            count++
            val childCount = getSafeChildCount(i)
            if (count + childCount > position) {
                onBindChildViewHolder(holder as C, i, position - count)
                return
            }
            count += childCount
        }
    }

    override fun getItemViewType(position: Int): Int {
        val groupCount = safeGroupCount
        var count = 0
        for (i in 0 until groupCount) {
            if (count == position) {
                val groupType = getGroupItemViewType(i)
                require(groupType > 0) { "GroupItemViewType can not be less than 1 ！" }
                return -groupType
            }
            count++
            val childCount = getSafeChildCount(i)
            if (count + childCount > position) {
                val childType = getChildItemViewType(i, position - count)
                require(childType > 0) { "ChildItemViewType can not be less than 1 ！" }
                return childType
            }
            count += childCount
        }
        return 0
    }

    override fun getItemCount(): Int {
        val groupCount = safeGroupCount
        var count = groupCount
        for (i in 0 until groupCount) {
            count += getSafeChildCount(i)
        }
        return count
    }

    val safeGroupCount: Int
        get() {
            val count = groupCount
            require(count >= 0) { "GroupCount can not be less than 0 !" }
            return count
        }

    open fun getSafeChildCount(groupIndex: Int): Int {
        val count = getChildCount(groupIndex)
        require(count >= 0) { "ChildCount can not be less than 0 !" }
        return count
    }

    /*
     * update group item only,do not update child which belong to this group
     *
     */
    fun notifyGroupItemChanged(groupIndex: Int) {
        val position = realGroupItemPosition(groupIndex)
        if (position == -1) {
            return
        }
        notifyItemChanged(position)
    }

    /*
     * update group,include child which belong to this group
     *
     */
    fun notifyGroupChanged(groupIndex: Int) {
        val from = realGroupItemPosition(groupIndex)
        if (from == -1) {
            return
        }
        val count = getSafeChildCount(groupIndex)
        notifyItemRangeChanged(from, count + 1)
    }

    /*
     * update one child only
     *
     */
    fun notifyChildItemChanged(groupIndex: Int, childIndex: Int) {
        notifyChildItemRangeChanged(groupIndex, childIndex, 1)
    }

    /*
     * only update children which belong to this group,from  childIndex to childIndex+itemCount,
     *
     */
    fun notifyChildItemRangeChanged(groupIndex: Int, childIndex: Int, itemCount: Int) {
        var itemCount = itemCount
        if (itemCount <= 0) {
            return
        }
        val childPosition = realChildItemPosition(groupIndex, childIndex)
        if (childPosition == -1) {
            return
        }
        val childCount = getSafeChildCount(groupIndex)
        if (childIndex >= childCount) {
            return
        }
        if (childCount < childIndex + itemCount) {
            itemCount = childCount - childIndex
        }
        notifyItemRangeChanged(childPosition, itemCount)
    }

    fun notifyChildItemInserted(groupIndex: Int, childIndex: Int) {
        notifyChildItemRangeInserted(groupIndex, childIndex, 1)
    }

    fun notifyChildItemRangeInserted(groupIndex: Int, childIndex: Int, itemCount: Int) {
        Timber.d("notifyChildItemRangeInserted groupIndex:" + groupIndex + " childIndex:" + childIndex + " itemCount:" + itemCount)
        if (itemCount <= 0 || groupIndex < 0 || childIndex < 0) {
            return
        }
        val groupCount = safeGroupCount
        if (groupIndex >= groupCount) {
            return
        }
        val childCount = getSafeChildCount(groupIndex)
        if (childCount < childIndex) {
            return
        }
        var position = 0
        var i = 0
        while (i < groupIndex) {
            position++
            position += getSafeChildCount(i)
            i++
        }
        position += childIndex
        position++
        notifyItemRangeInserted(position, itemCount)
    }

    fun notifyChildItemRemoved(groupIndex: Int, childIndex: Int) {
        notifyChildItemRangeRemoved(groupIndex, childIndex, 1)
    }

    fun notifyChildItemRangeRemoved(groupIndex: Int, childIndex: Int, itemCount: Int) {
        var itemCount = itemCount
        if (itemCount <= 0) {
            return
        }
        val childPosition = realChildItemPosition(groupIndex, childIndex)
        if (childPosition == -1) {
            return
        }
        val childCount = getSafeChildCount(groupIndex)
        if (childIndex >= childCount) {
            return
        }
        if (childCount < childIndex + itemCount) {
            itemCount = childCount - childIndex
        }
        notifyItemRangeRemoved(childPosition, itemCount)
    }

    private fun realGroupItemPosition(groupIndex: Int): Int {
        val groupCount = safeGroupCount
        if (groupIndex >= groupCount || groupIndex < 0) {
            return -1
        }
        var count = 0
        for (i in 0 until groupIndex) {
            count++
            count += getSafeChildCount(i)
        }
        return count
    }

    private fun realChildItemPosition(groupIndex: Int, childIndex: Int): Int {
        val childCount = getSafeChildCount(groupIndex)
        if (childIndex >= childCount || childIndex < 0) {
            return -1
        }
        val groupPosition = realGroupItemPosition(groupIndex)
        return if (groupPosition == -1) {
            -1
        } else groupPosition + childIndex + 1
    }

    protected abstract val groupCount: Int
    abstract fun getChildCount(groupIndex: Int): Int
    protected open fun getGroupItemViewType(groupIndex: Int): Int {
        return 1
    }

    protected open fun getChildItemViewType(groupIndex: Int, childIndex: Int): Int {
        return 1
    }

    protected abstract fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): G
    protected abstract fun onBindGroupViewHolder(holder: G?, groupIndex: Int)
    protected abstract fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): C
    protected abstract fun onBindChildViewHolder(holder: C?, groupIndex: Int, childIndex: Int)
    protected fun <T> getGroupData(groupIndex: Int): T? {
        return null
    }

    protected fun <T> getChildData(groupIndex: Int, childIndex: Int): T? {
        return null
    }
}
