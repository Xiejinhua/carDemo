package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.autonavi.auto.skin.NightModeGlobal
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemAhaTripHomeBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_97
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.example.aha_api_sdkd01.manger.models.LineListModel
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * 路书首页列表Adapter
 */
class AhaTripHomeAdapter :
    BaseQuickAdapter<LineListModel.DataDTO.ListDTO?, BaseDataBindingHolder<ItemAhaTripHomeBinding>>(R.layout.item_aha_trip_home) {
    private var onTripClickListener: OnTripClickListener? = null
    private var type = true // true.天数 false.评分
    private var inputKeyWord = ""
    private var isLastPage = false
    fun onRefreshData(list: List<LineListModel.DataDTO.ListDTO>?, type: Boolean = true, inputKeyWord: String = "", isLastPage: Boolean = false) {
        this.type = type
        this.inputKeyWord = inputKeyWord
        this.isLastPage = isLastPage
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun convert(
        holder: BaseDataBindingHolder<ItemAhaTripHomeBinding>,
        item: LineListModel.DataDTO.ListDTO?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.item = item
            binding.isNight = NightModeGlobal.isNightMode()
            binding.isLast = holder.layoutPosition == data.size - 1 && isLastPage
            if (type) {
                binding.dayTitle.text = "天数："
                binding.dayNum.text = "${item?.totalDay}天"
            } else {
                binding.dayTitle.text = "评分："
                binding.dayNum.text = "${item?.score}"
            }

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

            //item点击
            binding.itemCl.setDebouncedOnClickListener {
                if (null != onTripClickListener) {
                    onTripClickListener?.onItemClick(item)
                }
            }

            //打开路书按钮点击
            binding.openTripApp.setDebouncedOnClickListener {
                if (null != onTripClickListener) {
                    onTripClickListener?.onOpenAhaClick()
                }
            }
            ViewClickEffectUtils.addClickScale(binding.openTripApp, CLICKED_SCALE_97)
        }
    }

    fun setOnTripClickListener(onTripClickListener: OnTripClickListener?) {
        this.onTripClickListener = onTripClickListener
    }

    interface OnTripClickListener {
        fun onItemClick(item: LineListModel.DataDTO.ListDTO?)
        fun onOpenAhaClick()
    }
}