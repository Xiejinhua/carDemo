package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemMyAhaTankDetailBinding
import com.desaysv.psmap.model.bean.TankMarkers

/**
 * 轨迹路书详情-相关的Adapter
 */
class MyAhaTankDetailDayAdapter :
    BaseQuickAdapter<TankMarkers?, BaseDataBindingHolder<ItemMyAhaTankDetailBinding>>(R.layout.item_my_aha_tank_detail) {

    fun onRefreshData(list: List<TankMarkers>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult")
    override fun convert(
        holder: BaseDataBindingHolder<ItemMyAhaTankDetailBinding>,
        item: TankMarkers?
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