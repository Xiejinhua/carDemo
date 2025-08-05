package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemMyAhaLineDetailDayBinding
import com.desaysv.psmap.model.bean.GuideNode

/**
 * 共创路书详情-day相关的Adapter
 */
class MyAhaLineDetailDayAdapter :
    BaseQuickAdapter<GuideNode?, BaseDataBindingHolder<ItemMyAhaLineDetailDayBinding>>(R.layout.item_my_aha_line_detail_day) {

    fun onRefreshData(list: List<GuideNode>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult")
    override fun convert(
        holder: BaseDataBindingHolder<ItemMyAhaLineDetailDayBinding>,
        item: GuideNode?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.item = item
            binding.position = (holder.layoutPosition + 1).toString()
            binding.isNight = NightModeGlobal.isNightMode()
            binding.executePendingBindings()
        }
    }
}