package com.desaysv.psmap.ui.adapter

import android.graphics.Color
import android.text.TextUtils
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.search.model.SearchChildCategoryInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemClassifyGategoryBinding
import timber.log.Timber

class SearchClassifyCateGoryAdapter(
    private var selectPosition: Int = -1, //选中的item，默认0
) : BaseQuickAdapter<SearchChildCategoryInfo, BaseDataBindingHolder<ItemClassifyGategoryBinding>>(R.layout.item_classify_gategory) {

    fun updateData(poiList: List<SearchChildCategoryInfo>?) {
        poiList?.let { setList(it) }
        notifyDataSetChanged()
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemClassifyGategoryBinding>,
        item: SearchChildCategoryInfo
    ) {
        // 获取 Binding
        holder.dataBinding?.let { binding ->
            binding.executePendingBindings()
            binding.stvText.run {
                if (holder.layoutPosition == selectPosition) {
                    this.ellipsize = TextUtils.TruncateAt.MARQUEE
                    this.marqueeRepeatLimit = -1
                    this.setTextColor(
                        this.resources.getColor(
                            if (NightModeGlobal.isNightMode()) com.desaysv.psmap
                                .model.R.color.onPrimaryNight else com.desaysv.psmap
                                .model.R.color.onPrimaryDay
                        )
                    )
                } else {
                    this.ellipsize = TextUtils.TruncateAt.END
                    this.setTextColor(
                        this.resources.getColor(
                            if (NightModeGlobal.isNightMode()) com.desaysv.psmap
                                .model.R.color.onSecondaryNight else com.desaysv.psmap
                                .model.R.color.onSecondaryDay
                        )
                    )
                }
                this.text = data[holder.layoutPosition].baseInfo.name
                //子POI点击操作
                this.setOnClickListener {
                    if (holder.layoutPosition != -1 && listener != null) {
                        listener?.onItemClick(holder.layoutPosition)
                    }
                }
                if (selectPosition != -1) {
                    val params = this.layoutParams
                    when {
                        holder.layoutPosition == selectPosition -> {
                            params.height = if (selectPosition == 0) 80 else 60
                            this.layoutParams = params
                            setPadding(0, if (selectPosition == 0) 20 else 0, 20, 0)
                            this.setBackgroundColor(Color.TRANSPARENT)
                        }
                        holder.layoutPosition == selectPosition - 1 -> {
                            params.height = 120
                            this.layoutParams = params
                            setPadding(0, 0, 20, 20)
                            this.setBackgroundResource( if (NightModeGlobal.isNightMode()) R.drawable.ic_classify_item_bg_top_night else R.drawable.ic_classify_item_bg_top_day)
                            this.setBackground(R.drawable.ic_classify_item_bg_top_day,R.drawable.ic_classify_item_bg_top_night)
                        }
                        holder.layoutPosition == selectPosition + 1 -> {
                            params.height = 120
                            this.layoutParams = params
                            setPadding(0, 20, 20, 0)
                            this.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_classify_item_bg_bottom_night else R.drawable.ic_classify_item_bg_bottom_day)
                            this.setBackground(R.drawable.ic_classify_item_bg_bottom_day,R.drawable.ic_classify_item_bg_bottom_night)
                        }
                        holder.layoutPosition < selectPosition -> {
                            params.height = 100
                            this.layoutParams = params
                            setPadding(0, 0, 20, 0)
                            this.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_classify_item_bg_middle_night else R.drawable.ic_classify_item_bg_middle_day)
                            this.setBackground(R.drawable.ic_classify_item_bg_middle_day,R.drawable.ic_classify_item_bg_middle_night)
                        }
                        else -> {
                            params.height = 100
                            this.layoutParams = params
                            setPadding(0, 0, 20, 0)
                            this.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_classify_item_bg_middle_night else R.drawable.ic_classify_item_bg_middle_day)
                            this.setBackground(R.drawable.ic_classify_item_bg_middle_day,R.drawable.ic_classify_item_bg_middle_night)
                        }
                    }
                }

            }
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
        listener?.onSelectPosition(selectPosition)
        return true
    }

    fun selectPosition(): Int = selectPosition


    var listener: OnItemClickListener? = null

    fun setOnListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onSelectPosition(selectPosition: Int)
    }

}