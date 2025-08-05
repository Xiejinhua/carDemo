package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import android.view.View
import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemAhaLineDetailDayBinding
import com.example.aha_api_sdkd01.manger.models.LineDetailModel
import com.google.gson.Gson
import timber.log.Timber

/**
 * 路书详情-day相关的Adapter
 */
class AhaLineDetailDayAdapter : BaseQuickAdapter<LineDetailModel.DataDTO.LineDataDTO.NodeDTO?, BaseDataBindingHolder<ItemAhaLineDetailDayBinding>>(
    R.layout.item_aha_line_detail_day
) {

    fun onRefreshData(list: List<LineDetailModel.DataDTO.LineDataDTO.NodeDTO>?) {
        Timber.i("onRefreshData list:${Gson().toJson(list)}")
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun convert(
        holder: BaseDataBindingHolder<ItemAhaLineDetailDayBinding>,
        item: LineDetailModel.DataDTO.LineDataDTO.NodeDTO?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.item = item
            binding.isNight = NightModeGlobal.isNightMode()
            binding.isLast = holder.layoutPosition == data.size - 1
            binding.position = (holder.layoutPosition + 1).toString()
            binding.distanceText.text = item?.distanceText
            binding.durationText.text = item?.durationText
            binding.durationLastText.text = item?.durationText
            if (item?.poiDetail?.playInterval == null) {
                binding.playInterval.text = binding.playInterval.resources.getString(R.string.sv_custom_aha_playInterval_auto)
            } else {
                binding.playInterval.text = "${(item.poiDetail?.playInterval ?: 0) / 60}小时"
            }
            binding.playInterval.visibility = if (item?.poiDetail?.playInterval == null) View.GONE else View.VISIBLE
            binding.executePendingBindings()
        }
    }
}