package com.desaysv.psmap.ui.adapter

import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemSearchAlongWayResultBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchHistoryBean
import com.desaysv.psmap.ui.search.bean.SearchResultBean

class SearchAlongWayResultAdapter(
    private var selectPosition: Int = -1, //选中的item，默认0
) : BaseQuickAdapter<SearchResultBean, BaseDataBindingHolder<ItemSearchAlongWayResultBinding>>(R.layout.item_search_along_way_result) {

    fun updateData(searchResultBeanList: List<SearchResultBean>?) {
        searchResultBeanList?.let { setList(searchResultBeanList) }
        notifyDataSetChanged()
    }

    fun updateData(bean: SearchResultBean, position: Int) {
        data[position] = bean
        notifyItemChanged(position)
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemSearchAlongWayResultBinding>,
        item: SearchResultBean
    ) {
        // 获取 Binding
        holder.dataBinding?.let { binding ->
            item.isSelect = holder.layoutPosition == selectPosition
            binding.item = item
            binding.position = holder.layoutPosition + 1
            binding.isNight = NightModeGlobal.isNightMode()
            binding.top.setDebouncedOnClickListener {
                setSelection(holder.layoutPosition)
            }
            binding.gotoThere.setDebouncedOnClickListener {
                listener?.onItemRightClick(getItem(holder.layoutPosition))
            }
            binding.executePendingBindings()
            ViewClickEffectUtils.addClickScale(binding.gotoThere, CLICKED_SCALE_90)
        }
    }

    /**
     * 设置选中的pos
     */
    fun setSelection(position: Int) {
        if (selectPosition == position) {
            val item = data[selectPosition]
//            selectPosition = -1
//            notifyItemChanged(position)
            listener?.onItemClick(-1, item)
        } else {
            notifyItemChanged(selectPosition)
            selectPosition = position
            val item = data[selectPosition]
            notifyItemChanged(position)
            listener?.onItemClick(position, item)
        }
    }

    /**
     * 当前选中的pos
     */
    fun getSelection(): Int {
        return selectPosition
    }

    /**
     * 当前选中的poi item对象
     */
    fun getSelectionPoi() = if (selectPosition == -1) null else getItem(selectPosition).poi

    fun reSetSelect() {
        selectPosition = -1
    }

    private var listener: OnItemClickListener? = null

    fun setOnSearchResultChildListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, resultBean: SearchResultBean)

        /**
         * 点击item的右侧图标
         *
         * @param SearchHistoryBean  选中的item
         */
        fun onItemRightClick(bean: SearchResultBean)
    }

}