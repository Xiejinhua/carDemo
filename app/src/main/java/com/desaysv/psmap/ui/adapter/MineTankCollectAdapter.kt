package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.autonavi.auto.skin.NightModeGlobal
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemAhaCollectTankBinding
import com.desaysv.psmap.model.bean.TankCollectItem
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import jp.wasabeef.glide.transformations.RoundedCornersTransformation

/**
 * 收藏轨迹路书列表Adapter
 */
class MineTankCollectAdapter : BaseQuickAdapter<TankCollectItem?, BaseDataBindingHolder<ItemAhaCollectTankBinding>>(R.layout.item_aha_collect_tank) {
    private var onClickListener: OnTankClickListener? = null
    private var isMineFav = false
    fun onRefreshData(list: List<TankCollectItem>?, isMineFav: Boolean) {
        this.isMineFav = isMineFav
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun convert(
        holder: BaseDataBindingHolder<ItemAhaCollectTankBinding>,
        item: TankCollectItem?
    ) {
        // 获取 Binding
        val binding = holder.dataBinding
        if (binding != null) {
            binding.item = item
            binding.isMineFav = isMineFav
            binding.dayNum.text = "${item?.time}"
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

    fun setOnTankClickListener(onClickListener: OnTankClickListener?) {
        this.onClickListener = onClickListener
    }

    interface OnTankClickListener {
        fun onDeleteClick(item: TankCollectItem?)
    }
}