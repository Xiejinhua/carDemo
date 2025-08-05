package com.desaysv.psmap.ui.adapter

import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.CityItemInfo
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemSearchCityCategoryBinding
import com.desaysv.psmap.model.bean.ProvinceDataBean
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import timber.log.Timber


class SearchCityCategoryAdapter(
    public var selectPos: Int = 0, //选中的item  默认选中第一个0
) : BaseQuickAdapter<ProvinceDataBean, BaseDataBindingHolder<ItemSearchCityCategoryBinding>>(R.layout.item_search_city_category) {
    override fun convert(
        holder: BaseDataBindingHolder<ItemSearchCityCategoryBinding>,
        item: ProvinceDataBean
    ) {
        // 获取 Binding
        holder.dataBinding?.run {
            this.isSelect = holder.layoutPosition == selectPos
            this.cityCategory = item
            this.isNight = NightModeGlobal.isNightMode()
//            val layoutParam = this.top.layoutParams as ConstraintLayout.LayoutParams
//            layoutParam.height =
//                if (isSelect) ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_68)
//                else if (holder.layoutPosition == selectPos + 1) ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_90)
//                else ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_96)
//            this.top.layoutParams = layoutParam

            this.ivOfflineCityArrow.setDebouncedOnClickListener {
                onCityClickListener?.OnArrowClick(holder.layoutPosition)
            }
            this.top.setDebouncedOnClickListener {
                onCityClickListener?.OnArrowClick(holder.layoutPosition)
            }
            childPoiLayout(this, holder.layoutPosition)
            if ("常用城市" == item.name) {
                this.isSelect = true
                this.ivOfflineCityArrow.visibility = View.GONE
            } else {
                this.ivOfflineCityArrow.visibility = View.VISIBLE
            }
            this.executePendingBindings()
            ViewClickEffectUtils.addClickScale(ivOfflineCityArrow, CLICKED_SCALE_90)
        }
    }

    //子POI 初始化
    private fun childPoiLayout(dataBinding: ItemSearchCityCategoryBinding, parentPosition: Int) {
        val childPoisAdapter = SearchCityAdapter()
        dataBinding.cityList.layoutManager =
            GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        dataBinding.cityList.adapter = childPoisAdapter
        childPoisAdapter.let { adapter ->
            adapter.setSearchCityCategory(true)
            adapter.setList(data[parentPosition].cityItemInfos)
            adapter.setOnCityClickListener(object : SearchCityAdapter.OnItemClickListener {
                override fun onItemClick(cityItemInfo: CityItemInfo) {
                    onCityClickListener?.OnCityClick(cityItemInfo)
                }
            })
        }
    }

    /**
     * 设置选中的pos
     */
    fun setSelection(position: Int) {
        Timber.i("setSelection() called with: position = $position")
        if (selectPos == position) {
            selectPos = -1
            notifyItemChanged(position)
        } else {
            notifyItemChanged(selectPos)
            selectPos = position
            notifyItemChanged(position)
        }
    }

    private var onCityClickListener: OnCityClickListener? = null

    fun setOnCityClickListener(listener: OnCityClickListener) {
        this.onCityClickListener = listener
    }

    interface OnCityClickListener {
        fun OnCityClick(cityItemInfo: CityItemInfo)
        fun OnArrowClick(position: Int)
    }
}