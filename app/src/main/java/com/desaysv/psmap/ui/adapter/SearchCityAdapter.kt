package com.desaysv.psmap.ui.adapter

import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.CityItemInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemCityBinding
import com.desaysv.psmap.model.utils.CityUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90

class SearchCityAdapter(
) : BaseQuickAdapter<CityItemInfo, BaseDataBindingHolder<ItemCityBinding>>(R.layout.item_city) {
    override fun convert(holder: BaseDataBindingHolder<ItemCityBinding>, item: CityItemInfo) {
        // 获取 Binding
        holder.dataBinding?.let { binding ->
            binding.executePendingBindings()
            binding.stvTextLocation.run {
                this.ellipsize = TextUtils.TruncateAt.END
                this.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.shape_bg_search_result_child_night else R.drawable.shape_bg_search_result_child_day)
                this.setTextColor(
                    if (NightModeGlobal.isNightMode()) ContextCompat.getColor(
                        this.context,
                        com.desaysv.psmap.model.R.color.onPrimaryNight
                    ) else
                        ContextCompat.getColor(
                            this.context,
                            com.desaysv.psmap.model.R.color.onPrimaryDay
                        )
                )
                val name = if (CityUtil.isProvince(item.cityAdcode) && isSearchCityCategory) {
                    "全省"
                } else {
                    item.cityName
                }
                this.text = name
                //子POI点击操作
                this.setOnClickListener {
                    if (holder.layoutPosition != -1 && listener != null) {
                        listener?.onItemClick(item)
                    }
                }
            }
            ViewClickEffectUtils.addClickScale(binding.stvTextLocation, CLICKED_SCALE_90)
        }
    }

    private var listener: OnItemClickListener? = null

    private var isSearchCityCategory: Boolean = false

    fun setSearchCityCategory(isSearchCityCategory: Boolean) {
        this.isSearchCityCategory = isSearchCityCategory
    }

    fun setOnCityClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(cityItemInfo: CityItemInfo)
    }
}