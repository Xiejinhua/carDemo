package com.desaysv.psmap.ui.adapter

import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.POI
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemSearchHistoryBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean

class SearchHistoryAdapter :
    BaseQuickAdapter<SearchHistoryBean, BaseDataBindingHolder<ItemSearchHistoryBinding>>(R.layout.item_search_history) {

    override fun convert(
        holder: BaseDataBindingHolder<ItemSearchHistoryBinding>,
        item: SearchHistoryBean
    ) {
        // 获取 Binding
        holder.dataBinding?.run {
            this.item = item
            this.isNight = NightModeGlobal.isNightMode()
            executePendingBindings()
            clAutoSearchMoreChildView.setDebouncedOnClickListener {
                onItemClickListener?.onItemClick(getItem(holder.layoutPosition))
            }
            clAutoSearchMoreChildView.setOnLongClickListener {
                onItemClickListener?.onItemLongClick(getItem(holder.layoutPosition)) ?: false
            }
            tvHistoryFavorite.setDebouncedOnClickListener {
                onItemClickListener?.onFavorite(holder.layoutPosition)
            }
            tvHistoryDelete.setDebouncedOnClickListener {
                onItemClickListener?.onHistoryDelete(getItem(holder.layoutPosition))
            }
            ivAdd.setDebouncedOnClickListener {
                onItemClickListener?.onItemClick(getItem(holder.layoutPosition))
            }
            ViewClickEffectUtils.addClickScale(ivHistory, CLICKED_SCALE_90)
            ViewClickEffectUtils.addClickScale(tvHistoryFavorite, CLICKED_SCALE_93)
            ViewClickEffectUtils.addClickScale(tvHistoryDelete, CLICKED_SCALE_93)
            ViewClickEffectUtils.addClickScale(ivAdd, CLICKED_SCALE_90)
        }
    }

    fun updateData(poiList: List<SearchHistoryBean>?) {
        poiList?.let { setList(it) }
        notifyDataSetChanged()
    }

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {

        /**
         * 点击item
         *
         * @param position  选中的item
         */
        fun onItemClick(bean: SearchHistoryBean)

        /**
         * 长按item
         *
         * @param position  选中的item
         */
        fun onItemLongClick(bean: SearchHistoryBean): Boolean

        /**
         * 进行收藏
         *
         * @param position
         */
        fun onFavorite(position: Int)

        /**
         * 进行路线规划
         *
         * @param poi
         */
        fun onGoThere(poi: POI?)

        /**
         * 删除该地点
         *
         * @param poi
         */
        fun onHistoryDelete(bean: SearchHistoryBean)

    }

}