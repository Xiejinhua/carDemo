package com.desaysv.psmap.ui.adapter

import com.autosdk.bussiness.common.POI
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemDriverReportParkingListBinding
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener

class DriverReportParkingAdapter :
    BaseQuickAdapter<POI, BaseDataBindingHolder<ItemDriverReportParkingListBinding>>(R.layout.item_driver_report_parking_list) {

    override fun convert(
        holder: BaseDataBindingHolder<ItemDriverReportParkingListBinding>,
        item: POI
    ) {
        // 获取 Binding
        holder.dataBinding?.run {
            this.item = item
            this.position = holder.layoutPosition + 1
            executePendingBindings()
            reportCl.setDebouncedOnClickListener {
                onItemClickListener?.onGoThere(getItem(holder.layoutPosition))
            }
        }
    }

    fun updateData(poiList: List<POI>?) {
        poiList?.let { setList(it) }
        notifyDataSetChanged()
    }

    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    interface OnItemClickListener {

        /**
         * 进行路线规划
         *
         * @param poi
         */
        fun onGoThere(poi: POI?)

    }

}