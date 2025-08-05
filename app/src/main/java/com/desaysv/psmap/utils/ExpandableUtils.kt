package com.desaysv.psmap.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.desaysv.psmap.ui.adapter.ExpandableAdapter

//Expandable折叠列表工具类
object ExpandableUtils {
    fun scrollToPosition(recyclerView: RecyclerView, adapter: ExpandableAdapter<*, *>, groupPosition: Int, childPosition: Int) {
        val adapterPosition = getChildPosition(adapter, groupPosition, childPosition)
        recyclerView.scrollToPosition(adapterPosition)
        recyclerView.post {
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
            val firstVisiblePosition = layoutManager!!.findFirstVisibleItemPosition()
            val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
            if (adapterPosition < firstVisiblePosition || adapterPosition > lastVisiblePosition) {
                recyclerView.smoothScrollToPosition(adapterPosition)
            }
        }
    }

    private fun getChildPosition(adapter: ExpandableAdapter<*, *>, groupPosition: Int, childPosition: Int): Int {
        var adapterPosition = 0
        for (i in 0 until groupPosition) {
            adapterPosition++
            if (adapter.isExpanded(i)) {
                adapterPosition += adapter.getChildCount(i)
            }
        }
        adapterPosition += childPosition + 1 // 加1是因为每个group都有一个header
        return adapterPosition
    }

    fun getChildAdapterPosition(adapter: ExpandableAdapter<*, *>, groupPosition: Int): Int {
        var adapterPosition = 0
        for (i in 0 until groupPosition) {
            if (adapter != null) {
                if (adapter.isExpanded(i)) {
                    adapterPosition += adapter.getChildCount(i)
                }
            }
        }
        adapterPosition += groupPosition
        return adapterPosition
    }
}
