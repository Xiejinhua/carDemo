package com.desaysv.psmap.ui.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemSearchCategoryBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchCategoryBean
import timber.log.Timber

class SearchCategoryAdapter :
    BaseQuickAdapter<SearchCategoryBean, BaseDataBindingHolder<ItemSearchCategoryBinding>>(R.layout.item_search_category) {
    val spanCount = 4
    override fun convert(
        holder: BaseDataBindingHolder<ItemSearchCategoryBinding>,
        item: SearchCategoryBean
    ) {
        // 获取 Binding
        holder.dataBinding?.run {
            executePendingBindings()
            clAutoSearchMoreChildView.setBackground(item.imgDay, item.imgNight)
            clAutoSearchMoreChildView.setBackgroundResource(if (NightModeGlobal.isNightMode()) item.imgNight else item.imgDay)
            clAutoSearchMoreChildView.setDebouncedOnClickListener {
                onItemClickListener?.onItemClick(holder.layoutPosition)
            }
            clAutoSearchMoreChildView.contentDescription = item.name
            ViewClickEffectUtils.addClickScale(clAutoSearchMoreChildView, CLICKED_SCALE_93)
        }
    }

    fun updateData(poiList: List<SearchCategoryBean>?) {
        poiList?.let { setList(it) }
        notifyDataSetChanged()
    }

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val leftSpacing: Int,
        private val topSpacing: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == RecyclerView.NO_POSITION) return
            val column = position % spanCount
            val isFirstRow = position < spanCount //检查是否为第一行
            val isFirstColumn = column == 0 //检查是否为第一列
            Timber.d("getItemOffsets() position: $position, column: $column, isFirstRow: $isFirstRow, isFirstColumn: $isFirstColumn")

            if (isFirstRow) {
                outRect.top = 0
                outRect.bottom = 0
            } else {
                outRect.top = topSpacing
                outRect.bottom = 0
            }

            outRect.left = leftSpacing / 2
            outRect.right = leftSpacing / 2

            Timber.d("getItemOffsets() outRect: $outRect")
        }
    }

}