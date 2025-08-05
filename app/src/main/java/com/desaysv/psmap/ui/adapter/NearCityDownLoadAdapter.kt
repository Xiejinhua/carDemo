package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.data.model.DownLoadMode
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.databinding.ItemOfflineNearCityBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener


/**
 * @author 王漫生
 * @project：离线地图附近推荐列表adapter
 */
class NearCityDownLoadAdapter(mapDataBusiness: MapDataBusiness) :
    BaseQuickAdapter<CityItemInfo?, BaseDataBindingHolder<ItemOfflineNearCityBinding>>(R.layout.item_offline_near_city) {
    private var onItemClickListener: OnItemClickListener? = null
    private var mapDataBusiness: MapDataBusiness

    init {
        this.mapDataBusiness = mapDataBusiness
    }

    fun onRefreshData(list: List<CityItemInfo>?) {
        val size = list?.size ?: 0
        if (size > 0) {
            setList(list)
        }
        notifyDataSetChanged()
    }

    @SuppressLint("CheckResult")
    override fun convert(
        dataBindingHolder: BaseDataBindingHolder<ItemOfflineNearCityBinding>, item: CityItemInfo?
    ) {
        // 获取 Binding
        val binding = dataBindingHolder.dataBinding
        if (binding != null) {
            binding.cityItemInfo = item
            binding.cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(
                DownLoadMode.DOWNLOAD_MODE_NET,
                item!!.cityAdcode
            )
            binding.workingQueueAdCodeList =
                mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)
            binding.isNight = NightModeGlobal.isNightMode()
            binding.executePendingBindings()
            binding.pbLoadSl.setDebouncedOnClickListener {
                onItemClickListener?.onStartClick(item.cityAdcode)
            }
            ViewClickEffectUtils.addClickScale(binding.pbLoadSl, CLICKED_SCALE_95)
            binding.root.setDebouncedOnClickListener {
                if (onItemClickListener != null && dataBindingHolder.layoutPosition != -1 && dataBindingHolder.layoutPosition < data.size) {
                    onItemClickListener!!.onItemClick(
                        dataBindingHolder.layoutPosition,
                        item.cityAdcode
                    )
                }
            }
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, arCode: Int)
        fun onStartClick(arCode: Int)
    }
}