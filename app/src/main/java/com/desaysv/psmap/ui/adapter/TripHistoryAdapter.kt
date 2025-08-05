package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import com.autosdk.bussiness.account.bean.TrackItemBean
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemTripBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.floor


class TripHistoryAdapter : BaseQuickAdapter<TrackItemBean?, BaseDataBindingHolder<ItemTripBinding>>(R.layout.item_trip) {
    private var onTrackClickListener: OnTrackClickListener? = null
    fun onRefreshData(list: List<TrackItemBean>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult", "SimpleDateFormat", "SetTextI18n")
    override fun convert(
        holder: BaseDataBindingHolder<ItemTripBinding>,
        item: TrackItemBean?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.executePendingBindings()

            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
            val time: String = simpleDateFormat.format(Date(item!!.updateTime * 1000L))
            val startPoiName = item.startPoiName
            val endPoiName = item.endPoiName

            binding.time.text = "$time·"

            binding.type.text =
                if (item.rideRunType == 0) binding.type.resources.getString(com.desaysv.psmap.model.R.string.sv_setting_cruise_txt) else binding.type.resources.getString(
                    com.desaysv.psmap.model.R.string.sv_setting_navi
                )

            binding.startName.text = if (TextUtils.isEmpty(startPoiName) || TextUtils.equals(
                    startPoiName,
                    binding.startName.resources.getString(com.desaysv.psmap.model.R.string.sv_setting_map_select_point)
                )
            ) binding.startName.resources.getString(com.desaysv.psmap.model.R.string.sv_setting_map_point) else startPoiName

            binding.endName.text = if (TextUtils.isEmpty(endPoiName) || TextUtils.equals(
                    endPoiName,
                    binding.endName.resources.getString(com.desaysv.psmap.model.R.string.sv_setting_map_select_point)
                )
            ) binding.endName.resources.getString(com.desaysv.psmap.model.R.string.sv_setting_map_point) else endPoiName
            val totalDistance = item.runDistance
            if (totalDistance == 0) {
                binding.mileageTv.text = "0" + binding.mileageTv.resources.getString(com.desaysv.psmap.base.R.string.sv_common_km)
            } else {
                val km = floor(totalDistance / 1000.0 * 10) / 10
                binding.mileageTv.text = km.toString() + binding.mileageTv.resources
                    .getString(com.desaysv.psmap.base.R.string.sv_common_km)
            }
            binding.item.setDebouncedOnClickListener {
                if (null != onTrackClickListener) {
                    onTrackClickListener!!.onClick(item)
                }
            }
            binding.delete.setDebouncedOnClickListener {
                if (null != onTrackClickListener) {
                    onTrackClickListener!!.onDeleteClick(item)
                }
            }
            ViewClickEffectUtils.addClickScale(binding.delete, CLICKED_SCALE_95)
        }
    }

    fun setOnTrackClickListener(onTrackClickListener: OnTrackClickListener?) {
        this.onTrackClickListener = onTrackClickListener
    }

    interface OnTrackClickListener {
        fun onClick(item: TrackItemBean?)
        fun onDeleteClick(item: TrackItemBean?)
    }
}