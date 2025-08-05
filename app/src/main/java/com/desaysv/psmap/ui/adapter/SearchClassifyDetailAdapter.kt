package com.desaysv.psmap.ui.adapter

import android.text.TextUtils
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.search.model.SearchChildCategoryInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemClassifyDetailBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import timber.log.Timber

class SearchClassifyDetailAdapter<T>(
    private var selectPosition: Int = -1, //选中的item，默认0
) : BaseQuickAdapter<T, BaseDataBindingHolder<ItemClassifyDetailBinding>>(R.layout.item_classify_detail) {

    override fun convert(holder: BaseDataBindingHolder<ItemClassifyDetailBinding>, item: T) {
        // 获取 Binding
        holder.dataBinding?.let { binding ->
            binding.executePendingBindings()
            binding.stvTextLocation.run {
                if (holder.layoutPosition == selectPosition) {
                    this.ellipsize = TextUtils.TruncateAt.END
                    this.marqueeRepeatLimit = -1
                    this.setBackgroundResource(
                        if (NightModeGlobal.isNightMode()) R.drawable
                            .shape_bg_search_result_child_checked_night else R.drawable
                            .shape_bg_search_result_child_checked_day
                    )
                    this.setTextColor(
                        this.resources.getColor(
                            if (NightModeGlobal.isNightMode()) com.desaysv.psmap
                                .model.R.color.primaryNight else com.desaysv.psmap
                                .model.R.color.primaryDay
                        )
                    )
                } else {
                    this.ellipsize = TextUtils.TruncateAt.END
                    this.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.shape_bg_search_result_child_night else R.drawable.shape_bg_search_result_child_day)
                    this.setTextColor(
                        this.resources.getColor(
                            if (NightModeGlobal.isNightMode()) com.desaysv.psmap
                                .model.R.color.onPrimaryNight else com.desaysv.psmap
                                .model.R.color.onPrimaryDay
                        )
                    )
                }
                if (item is SearchChildCategoryInfo) {
                    this.text = item.baseInfo.name
                }
                //子POI点击操作
                this.setOnClickListener {
                    if (holder.layoutPosition != -1 && listener != null) {
                        listener?.onItemClick(holder.layoutPosition)
                    }
                }
            }
            ViewClickEffectUtils.addClickScale(binding.stvTextLocation, CLICKED_SCALE_95)
        }
    }

    /**
     * 切换选中状态
     * P.S. 若之前已选中,则取消选中
     *
     * @param selectPosition
     * @return 最终是否选中, true-选中,false-未选中
     */
    fun toggleSelectPosition(selectPosition: Int): Boolean {
        Timber.i("toggleSelectPosition $selectPosition")
        if (this.selectPosition == selectPosition) { // 当前已选中,这取消选中
            this.selectPosition = -1
        } else { // 当前未选中,这选中
            this.selectPosition = selectPosition
        }
        notifyDataSetChanged()
        return this.selectPosition >= 0
    }

    /**
     * 从POI子点点击选中
     *
     * @param selectPosition
     * @return
     */
    fun setSelectPosition(selectPosition: Int): Boolean {
        this.selectPosition = selectPosition
        notifyDataSetChanged()
        return true
    }

    fun selectPosition(): Int = selectPosition


    private var listener: OnItemClickListener? = null

    fun setOnListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

}