package com.desaysv.psmap.ui.adapter

import androidx.databinding.DataBindingUtil
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.DownLoadMode
import com.autosdk.bussiness.manager.SDKManager
import com.chad.library.adapter.base.BaseDelegateMultiAdapter
import com.chad.library.adapter.base.delegate.BaseMultiTypeDelegate
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.databinding.ItemOfflineCityDownloadingDownloadedBinding
import com.desaysv.psmap.databinding.ItemOfflineHeaderDownloadingDownloadedBinding
import com.desaysv.psmap.model.bean.DownloadCityDataBean
import com.desaysv.psmap.model.bean.TYPE_CITY
import com.desaysv.psmap.model.bean.TYPE_PROVINCE
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener

import com.desaysv.psmap.ui.settings.offlinedata.OnMapDataItemClickListener

/**
 * @author 王漫生
 * @project：个性化地图——离线地图DownLoadingAdapter
 */
class DataDownLoadingManagerAdapter(
    private var mapDataBusiness: MapDataBusiness,
    data: MutableList<DownloadCityDataBean>? = null
) : BaseDelegateMultiAdapter<DownloadCityDataBean, BaseViewHolder>(data) {
    private var onItemClickListener: OnMapDataItemClickListener? = null
    private var currentCityAdCode: Int

    init {
        //设置代理，通过内部类返回类型
        setMultiTypeDelegate(object : BaseMultiTypeDelegate<DownloadCityDataBean>() {
            override fun getItemType(data: List<DownloadCityDataBean>, position: Int): Int {
                return data[position].type
            }
        })
        //将类型和布局绑定
        getMultiTypeDelegate()
            ?.addItemType(TYPE_PROVINCE, R.layout.item_offline_header_downloading_downloaded)
            ?.addItemType(TYPE_CITY, R.layout.item_offline_city_downloading_downloaded)

        val location = SDKManager.getInstance().locController.lastLocation
        currentCityAdCode = mapDataBusiness.getAdCodeByLonLat(location.longitude, location.latitude)
    }

    fun setOnItemClickListener(onItemClickListener: OnMapDataItemClickListener?) {
        this.onItemClickListener = onItemClickListener
    }

    override fun convert(holder: BaseViewHolder, item: DownloadCityDataBean) {
        when (holder.itemViewType) {
            TYPE_PROVINCE -> {
                val binding: ItemOfflineHeaderDownloadingDownloadedBinding =
                    DataBindingUtil.bind(holder.itemView)!!
                binding.cityItemInfo = item.cityItemInfo
                binding.executePendingBindings()

                //item点击操作
                binding.root.setDebouncedOnClickListener(300L, onClick = {
                    if (onItemClickListener != null && holder.layoutPosition != -1 && holder.layoutPosition < data.size) {
                        onItemClickListener!!.onItemClick(
                            holder.layoutPosition,
                            -100,
                            item.cityItemInfo.cityAdcode
                        )
                    }
                })
            }

            TYPE_CITY -> {
                val binding: ItemOfflineCityDownloadingDownloadedBinding =
                    DataBindingUtil.bind(holder.itemView)!!
                binding.isNight = NightModeGlobal.isNightMode()
                binding.cityItemInfo = item.cityItemInfo
                binding.currentCityAdCode = currentCityAdCode
                binding.cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(
                    DownLoadMode.DOWNLOAD_MODE_NET,
                    item.cityItemInfo.cityAdcode
                )
                binding.workingQueueAdCodeList =
                    mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)
                binding.executePendingBindings()


                //下载icon点击操作
                binding.pbLoadSl.setDebouncedOnClickListener(300L, onClick = {
                    if (onItemClickListener != null && holder.layoutPosition != -1 && holder.layoutPosition < data.size) {
                        onItemClickListener!!.onStartClick(
                            holder.layoutPosition,
                            holder.layoutPosition,
                            item.cityItemInfo.cityAdcode
                        )
                    }
                })
                ViewClickEffectUtils.addClickScale(binding.pbLoadSl, CLICKED_SCALE_95)

                //item点击操作
                binding.clItemchildOfflineCity.setDebouncedOnClickListener(300L, onClick = {
                    if (onItemClickListener != null && holder.layoutPosition != -1 && holder.layoutPosition < data.size) {
                        onItemClickListener!!.onItemClick(
                            holder.layoutPosition,
                            holder.layoutPosition,
                            item.cityItemInfo.cityAdcode
                        )
                    }
                })
            }

            else -> {}
        }
    }
}



