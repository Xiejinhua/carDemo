package com.desaysv.psmap.ui.adapter

import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemSearchAddHomeBinding
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean

class SearchAddHomeAdapter(
    private var selectPosition: Int = -1, //选中的item，默认-1
) : BaseQuickAdapter<SearchHistoryBean, BaseDataBindingHolder<ItemSearchAddHomeBinding>>(R.layout.item_search_add_home) {

    override fun convert(
        holder: BaseDataBindingHolder<ItemSearchAddHomeBinding>,
        item: SearchHistoryBean
    ) {
        // 获取 Binding
        holder.dataBinding?.run {
            this.item = item
            this.isSelected = holder.layoutPosition == selectPosition
            this.isNight = NightModeGlobal.isNightMode()
            executePendingBindings()
            this.ivSuggestionGotoThere.setDebouncedOnClickListener {
                onItemClickListener?.onItemRightClick(item)
            }
            this.clAutoSearchMoreChildView.setDebouncedOnClickListener {
                onItemClickListener?.onItemClick(item)
            }
        }
    }

    fun updateData(searchResultBeanList: List<SearchHistoryBean>?) {
        searchResultBeanList?.let { setList(searchResultBeanList) }
        notifyDataSetChanged()
    }

    fun updateData(bean: SearchHistoryBean, position: Int) {
        data[position] = bean
        notifyItemChanged(position)
    }

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {

        /**
         * 点击item
         *
         * @param position  选中的item的下标
         */
        fun onItemClick(bean: SearchHistoryBean)

        /**
         * 点击item的右侧图标
         *
         * @param SearchHistoryBean  选中的item
         */
        fun onItemRightClick(bean: SearchHistoryBean)

    }
}