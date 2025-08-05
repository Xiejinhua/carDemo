package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.autonavi.auto.skin.NightModeGlobal
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemAhaCuratedBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.example.aha_api_sdkd01.manger.models.LineListModel
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * 精选路书列表Adapter
 */
class AhaCuratedAdapter : BaseQuickAdapter<LineListModel.DataDTO.ListDTO?, BaseDataBindingHolder<ItemAhaCuratedBinding>>(R.layout.item_aha_curated) {
    private var onClickListener: OnCuratedClickListener? = null

    fun onRefreshData(list: List<LineListModel.DataDTO.ListDTO>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun convert(
        holder: BaseDataBindingHolder<ItemAhaCuratedBinding>,
        item: LineListModel.DataDTO.ListDTO?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.item = item
            binding.isNight = NightModeGlobal.isNightMode()
            binding.dayNum.text = "${item?.totalDay}天"
            Glide.with(binding.image.context).asBitmap()
                .load(item?.logo)
                .apply(
                    RequestOptions.bitmapTransform(
                        RoundedCornersTransformation(
                            binding.image.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_12),
                            0
                        )
                    ).skipMemoryCache(false)
                )
                .placeholder(if (NightModeGlobal.isNightMode()) R.drawable.ic_aha_list_default_picture_night else R.drawable.ic_aha_list_default_picture_day) //加载中占位图
                .error(if (NightModeGlobal.isNightMode()) R.drawable.ic_aha_list_default_picture_night else R.drawable.ic_aha_list_default_picture_day)//加载错误占位图
                .into(binding.image)
            binding.executePendingBindings()

            binding.delete.setDebouncedOnClickListener {
                if (null != onClickListener) {
                    onClickListener!!.onDeleteClick(item)
                }
            }
            ViewClickEffectUtils.addClickScale(binding.delete, CLICKED_SCALE_95)
        }
    }

    fun setOnCuratedClickListener(onClickListener: OnCuratedClickListener?) {
        this.onClickListener = onClickListener
    }

    interface OnCuratedClickListener {
        fun onDeleteClick(item: LineListModel.DataDTO.ListDTO?)
    }
}