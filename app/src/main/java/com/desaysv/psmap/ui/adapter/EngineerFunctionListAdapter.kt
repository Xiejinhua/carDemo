package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemEngineerFunctionBinding

/**
 * @author 朱志鹏
 * @Desc 工程模式功能列表
 */
class EngineerFunctionListAdapter :
    BaseQuickAdapter<String?, BaseDataBindingHolder<ItemEngineerFunctionBinding>>(R.layout.item_engineer_function) {
    private var callback: ItemClickCallback? = null
    private var selectIndex = 0
    fun onRefreshData(list: List<String>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    fun notifyDataChangedByClick(position: Int) {
        val oldIndex = selectIndex
        selectIndex = position
        notifyItemChanged(oldIndex)
        notifyItemChanged(selectIndex)
    }

    @SuppressLint("CheckResult")
    override fun convert(
        holder: BaseDataBindingHolder<ItemEngineerFunctionBinding>,
        item: String?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.tvName.text = item
            binding.tvName.setTextColor(
                if (NightModeGlobal.isNightMode()) binding.tvName.resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryNight)
                else binding.tvName.resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryDay)
            )

            if (selectIndex == holder.layoutPosition) {
                binding.item.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.shape_bg_search_result_night else R.drawable.shape_bg_search_result_day)
            } else {
                binding.item.setBackgroundResource(android.R.color.transparent)
            }
            binding.ivMore.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_group_more_night else R.drawable.selector_ic_group_more_day)
            binding.line.setBackgroundResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.lineNight else com.desaysv.psmap.model.R.color.lineDay)
            binding.executePendingBindings()
            //item点击操作
            binding.item.setOnClickListener {
                if (holder.layoutPosition != -1) {
                    callback!!.onItemClick(holder.layoutPosition)
                }
            }
        }
    }

    /**
     * 设置item点击事件
     */
    fun setOnItemClickListener(favoriteItemImpl: ItemClickCallback?) {
        this.callback = favoriteItemImpl
    }

    interface ItemClickCallback {
        /**
         * 列表第几项
         */
        fun onItemClick(position: Int)
    }
}
