package com.desaysv.psmap.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemGnssSnrBinding

class GnssSnrListAdapter : BaseQuickAdapter<Int, BaseDataBindingHolder<ItemGnssSnrBinding>>(
    R.layout
        .item_gnss_snr
) {
    fun updateData(list: List<Int>) {
        setList(list)
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseDataBindingHolder<ItemGnssSnrBinding>, progress: Int) {
        // 获取 Binding
        holder.dataBinding?.run {
            if (progress <= 27) {
                this.pbSnr.progressDrawable = ContextCompat.getDrawable(
                    this.pbSnr.context, if (!NightModeGlobal.isNightMode()) R.drawable
                        .progress_gnss_snr_low_day else R.drawable
                        .progress_gnss_snr_low_night
                )
            } else if (progress <= 42) {
                this.pbSnr.progressDrawable = ContextCompat.getDrawable(
                    this.pbSnr.context,
                    if (!NightModeGlobal.isNightMode()) R.drawable.progress_gnss_snr_medium_day
                    else R.drawable
                        .progress_gnss_snr_medium_night
                )
            } else {
                this.pbSnr.progressDrawable = ContextCompat.getDrawable(
                    this.pbSnr.context,
                    if (!NightModeGlobal.isNightMode()) R.drawable.progress_gnss_snr_high_day else R
                        .drawable
                        .progress_gnss_snr_high_night
                )
            }
            if (holder.layoutPosition != data.size - 1) {
                val ly = this.pbSnr.layoutParams as ConstraintLayout.LayoutParams
                ly.marginEnd =
                    this.pbSnr.resources.getDimensionPixelSize(com.desaysv.psmap.base.R.dimen.sv_dimen_40)
                this.pbSnr.layoutParams = ly
            }
            this.pbSnr.progress = progress
        }
    }
}