package com.desaysv.psmap.ui.adapter

import android.view.View
import android.view.ViewGroup
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.POI
import com.autosdk.common.utils.ResUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemResultChildBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import timber.log.Timber

class SearchResultChildAdapter(
    private var selectPosition: Int = -1, //选中的item，默认0
) : BaseQuickAdapter<POI, BaseDataBindingHolder<ItemResultChildBinding>>(R.layout.item_result_child) {

    var isSearchResult = false
    fun updateData(poiList: List<POI>?) {
        poiList?.let { setList(it) }
        notifyDataSetChanged()
    }

    override fun convert(holder: BaseDataBindingHolder<ItemResultChildBinding>, item: POI) {
        // 获取 Binding
        holder.dataBinding?.let { binding ->
            binding.executePendingBindings()
            binding.isNight = NightModeGlobal.isNightMode()
            binding.stvTextLocation.run {
                if (holder.layoutPosition == selectPosition) {
                    binding.clRoot.setBackgroundResource(
                        if (NightModeGlobal.isNightMode()) R.drawable
                            .shape_bg_search_result_child_checked_night else R.drawable
                            .shape_bg_search_result_child_checked_day
                    )
                    this.setTextColor(
                        this.resources.getColor(
                            if (NightModeGlobal.isNightMode()) com.desaysv.psmap
                                .model.R.color.onPrimaryContainerNight else com.desaysv.psmap
                                .model.R.color.onPrimaryContainerDay
                        )
                    )
                } else {
                    binding.clRoot.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.shape_bg_search_result_child_night else R.drawable.shape_bg_search_result_child_day)
                    this.setTextColor(
                        this.resources.getColor(
                            if (NightModeGlobal.isNightMode()) com.desaysv.psmap
                                .model.R.color.onPrimaryNight else com.desaysv.psmap
                                .model.R.color.onTertiaryDay
                        )
                    )
                }
                var name = data[holder.layoutPosition].name
                if (name.contains("(")) {
                    name = name.substring(name.indexOf("(") + 1, name.lastIndexOf(")"))
                }
                this.text = name

                if (this.text.length > 8) {
                    this.textSize = 26f
                }
                //子POI点击操作
                binding.stvTextLocation.setOnClickListener {
                    if (holder.layoutPosition != -1 && listener != null) {
                        listener?.onItemClick(holder.layoutPosition)
                    }
                }
                ViewClickEffectUtils.addClickScale(binding.stvTextLocation, CLICKED_SCALE_95)
            }
            val ly1 = binding.clRoot.layoutParams as ViewGroup.MarginLayoutParams
            if (holder.layoutPosition > 1) {
                ly1.topMargin = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_16)
            } else {
                ly1.topMargin = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_0)
            }
            if (isSearchResult) {
                ly1.width = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_240)
                ly1.height = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_72)
                binding.tvChildRatio.visibility = View.GONE
            } else {
                ly1.width = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_240)
                ly1.height = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_116)
                if (item.ratio != 0.toDouble()) {
                    binding.tvChildRatio.text =
                        String.format("%.0f%%", item.ratio)
                    binding.tvChildRatio.visibility = View.VISIBLE
                } else {
                    binding.tvChildRatio.visibility = View.GONE
                }
            }
            binding.clRoot.layoutParams = ly1
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
            notifyItemChanged(selectPosition)
            this.selectPosition = -1
        } else { // 当前未选中,这选中
            if (this.selectPosition >= 0) { // 如果之前有选中,则取消之前的选中
                notifyItemChanged(this.selectPosition)
            }
            this.selectPosition = selectPosition
            notifyItemChanged(selectPosition)
        }
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

    fun setOnSearchResultChildListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

}