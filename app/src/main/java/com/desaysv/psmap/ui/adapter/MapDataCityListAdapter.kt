package com.desaysv.psmap.ui.adapter

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.OperationType
import com.autonavi.gbl.data.model.OperationType.OperationType1
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_READY
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING
import com.autosdk.bussiness.manager.SDKManager
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.databinding.ItemOfflineCityChildDatalistBinding
import com.desaysv.psmap.databinding.ItemOfflineProvinceParentDatalistBinding
import com.desaysv.psmap.databinding.ItemOfflineSearchParentDatalistBinding
import com.desaysv.psmap.model.bean.ProvinceDataBean
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.vh.ChildVH
import com.desaysv.psmap.ui.adapter.vh.GroupVH

/**
 * @author 王漫生
 * @project：个性化地图——离线地图城市列表Adapter
 */
class MapDataCityListAdapter(
    private var mapDataBusiness: MapDataBusiness,
    mGroup: ArrayList<ProvinceDataBean>
) :
    ExpandableAdapter<GroupVH<ProvinceDataBean, ViewDataBinding>, ChildVH<ProvinceDataBean, ViewDataBinding>>() {
    private var mGroup: ArrayList<ProvinceDataBean>
    private var downLoadClickListener: OnMapDataItemClickListener? = null
    private var currentCityAdCode: Int
    private val TYPE_PROVINCE = 1
    private val TYPE_CITY = 2

    init {
        this.mGroup = mGroup
        val location = SDKManager.getInstance().locController.lastLocation
        currentCityAdCode = mapDataBusiness.getAdCodeByLonLat(location.longitude, location.latitude)
    }

    fun updateData(mGroup: ArrayList<ProvinceDataBean>) {
        this.mGroup = mGroup
        notifyDataSetChanged()
    }

    //获取搜索列表"search"item数量
    fun getSearchItemCount(): Int {
        if (mGroup.size > 0) {
            var count = 0
            for (i in mGroup.indices) {
                if ("search" == mGroup[i].name) {
                    count++
                }
            }
            return count
        }
        return 0
    }

    override val groupCount: Int
        get() = mGroup.size

    override fun getChildCount(groupIndex: Int): Int {
        return if ("search" == mGroup[groupIndex].name) {
            0
        } else {
            mGroup[groupIndex].cityItemInfos.size
        }
    }

    public override fun getGroupItemViewType(groupIndex: Int): Int {
        return if ("search" == mGroup[groupIndex].name) {
            TYPE_PROVINCE
        } else {
            TYPE_CITY
        }
    }

    public override fun getChildItemViewType(groupIndex: Int, childIndex: Int): Int {
        return TYPE_CITY
    }

    public override fun onCreateGroupViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GroupVH<ProvinceDataBean, ViewDataBinding> {
        return if (viewType == TYPE_PROVINCE) {
            GroupVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_offline_search_parent_datalist,
                    parent,
                    false
                )
            )
        } else {
            GroupVH(
                DataBindingUtil.inflate(
                    LayoutInflater.from(parent.context),
                    R.layout.item_offline_province_parent_datalist,
                    parent,
                    false
                )
            )
        }
    }

    @SuppressLint("CheckResult")
    override fun onBindGroupViewHolder(
        holder: GroupVH<ProvinceDataBean, ViewDataBinding>?,
        groupIndex: Int
    ) {
        if (holder != null) {
            if (getGroupItemViewType(groupIndex) == TYPE_CITY) {
                val provinceOfflineDataListBinding: ItemOfflineProvinceParentDatalistBinding =
                    holder.binding as ItemOfflineProvinceParentDatalistBinding
                provinceOfflineDataListBinding.provinceDataBean = mGroup[groupIndex]
                provinceOfflineDataListBinding.isNight = NightModeGlobal.isNightMode()
                val taskStatusCode =
                    mapDataBusiness.getTaskStatusCode(mGroup[groupIndex].cityItemInfos)
                provinceOfflineDataListBinding.taskStatusCode = taskStatusCode
                provinceOfflineDataListBinding.isExpanded = isExpanded(groupIndex)
                provinceOfflineDataListBinding.hasNewVersion =
                    mapDataBusiness.hasNewVersion(mGroup[groupIndex].cityItemInfos)
                provinceOfflineDataListBinding.executePendingBindings()

                //group点击判断是否展示child
                provinceOfflineDataListBinding.root.setDebouncedOnClickListener(300L, onClick = {
                    if (isExpanded(groupIndex)) {
                        provinceOfflineDataListBinding.isExpanded = false
                        collapseGroup(groupIndex)
                    } else {
                        provinceOfflineDataListBinding.isExpanded = true
                        expandGroup(groupIndex)
                        downLoadClickListener?.onGroupClick(groupIndex)
                    }
                })
            } else {
                val itemOfflineSearchParentDataListBinding =
                    holder.binding as ItemOfflineSearchParentDatalistBinding
                itemOfflineSearchParentDataListBinding.isNight = NightModeGlobal.isNightMode()
                itemOfflineSearchParentDataListBinding.cityItemInfo =
                    mGroup[groupIndex].cityItemInfos[0]
                itemOfflineSearchParentDataListBinding.currentCityAdCode = currentCityAdCode
                itemOfflineSearchParentDataListBinding.cityDownLoadItem =
                    mapDataBusiness.getCityDownLoadItem(
                        DownLoadMode.DOWNLOAD_MODE_NET,
                        mGroup[groupIndex].cityItemInfos[0].cityAdcode
                    )
                itemOfflineSearchParentDataListBinding.workingQueueAdCodeList =
                    mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)
                itemOfflineSearchParentDataListBinding.executePendingBindings()

                //下载icon点击操作
                itemOfflineSearchParentDataListBinding.pbLoadSl.setDebouncedOnClickListener(
                    300L,
                    onClick = {
                        if (downLoadClickListener != null && groupIndex != -1 && groupIndex < mGroup.size) {
                            downLoadClickListener!!.onStartClick(
                                groupIndex,
                                0,
                                mGroup[groupIndex].cityItemInfos[0].cityAdcode
                            )
                        }
                    })
                ViewClickEffectUtils.addClickScale(itemOfflineSearchParentDataListBinding.pbLoadSl, CLICKED_SCALE_95)
                //item点击操作
                itemOfflineSearchParentDataListBinding.clItemchildOfflineCity.setDebouncedOnClickListener(
                    300L,
                    onClick = {
                        if (downLoadClickListener != null && groupIndex != -1 && groupIndex < mGroup.size) {
                            downLoadClickListener!!.onItemClick(
                                groupIndex,
                                0,
                                mGroup[groupIndex].cityItemInfos[0].cityAdcode
                            )
                        }
                    })
            }
        }
    }

    public override fun onCreateChildViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChildVH<ProvinceDataBean, ViewDataBinding> {
        return ChildVH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_offline_city_child_datalist,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    public override fun onBindChildViewHolder(
        holder: ChildVH<ProvinceDataBean, ViewDataBinding>?,
        groupIndex: Int,
        childIndex: Int
    ) {
        if (holder != null) {
            val childOfflineCityOfflineDataListBinding: ItemOfflineCityChildDatalistBinding =
                holder.binding as ItemOfflineCityChildDatalistBinding
            val isAllProvince =
                TextUtils.equals(mGroup[groupIndex].cityItemInfos[childIndex].cityName, "全省地图")
            var isAllStartEnable = false
            var isAllPauseEnable = false
            if (isAllProvince) {
                val provinceInfo =
                    mapDataBusiness.getProvinceInfo(mGroup[groupIndex].cityItemInfos[childIndex].belongedProvince)
                isAllStartEnable = mapDataBusiness.findDownLoadItemAnyTaskState(
                    provinceInfo!!.provAdcode,
                    TASK_STATUS_CODE_PAUSE,
                    TASK_STATUS_CODE_READY
                )
                isAllPauseEnable = mapDataBusiness.findDownLoadItemAnyTaskState(
                    provinceInfo!!.provAdcode,
                    TASK_STATUS_CODE_DOING,
                    TASK_STATUS_CODE_WAITING
                )
                childOfflineCityOfflineDataListBinding.isAllStartEnable = isAllStartEnable
                childOfflineCityOfflineDataListBinding.isAllPauseEnable = isAllPauseEnable
                childOfflineCityOfflineDataListBinding.provinceFullZipSize =
                    mapDataBusiness.getProvinceFullZipSize(provinceInfo.provAdcode)
            } else {
                childOfflineCityOfflineDataListBinding.provinceFullZipSize = 0L
            }
            childOfflineCityOfflineDataListBinding.isNight = NightModeGlobal.isNightMode()
            childOfflineCityOfflineDataListBinding.cityItemInfo =
                mGroup[groupIndex].cityItemInfos[childIndex]
            childOfflineCityOfflineDataListBinding.currentCityAdCode = currentCityAdCode
            childOfflineCityOfflineDataListBinding.cityDownLoadItem =
                mapDataBusiness.getCityDownLoadItem(
                    DownLoadMode.DOWNLOAD_MODE_NET,
                    mGroup[groupIndex].cityItemInfos[childIndex].cityAdcode
                )
            childOfflineCityOfflineDataListBinding.workingQueueAdCodeList =
                mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)
            childOfflineCityOfflineDataListBinding.isAllProvince = isAllProvince
            childOfflineCityOfflineDataListBinding.executePendingBindings()

            //下载icon点击操作
            childOfflineCityOfflineDataListBinding.pbLoadSl.setDebouncedOnClickListener(
                300L,
                onClick = {
                    if (downLoadClickListener != null && groupIndex != -1 && groupIndex < mGroup.size) {
                        downLoadClickListener!!.onStartClick(
                            groupIndex,
                            childIndex,
                            mGroup[groupIndex].cityItemInfos[childIndex].cityAdcode
                        )
                    }
                })
            ViewClickEffectUtils.addClickScale(childOfflineCityOfflineDataListBinding.pbLoadSl, CLICKED_SCALE_95)
            //item点击操作
            childOfflineCityOfflineDataListBinding.leftLayout.setDebouncedOnClickListener(
                300L,
                onClick = {
                    if (downLoadClickListener != null && groupIndex != -1 && groupIndex < mGroup.size) {
                        if (isAllProvince) {
                            if (isAllStartEnable) {
                                val provinceInfo =
                                    mapDataBusiness.getProvinceInfo(mGroup[groupIndex].cityItemInfos[childIndex].belongedProvince)
                                downLoadClickListener!!.onAllTaskOperateClick(
                                    provinceInfo!!.provAdcode,
                                    OperationType.OPERATION_TYPE_START
                                )
                            } else if (isAllPauseEnable) {
                                val provinceInfo =
                                    mapDataBusiness.getProvinceInfo(mGroup[groupIndex].cityItemInfos[childIndex].belongedProvince)
                                downLoadClickListener!!.onAllTaskOperateClick(
                                    provinceInfo!!.provAdcode,
                                    OperationType.OPERATION_TYPE_PAUSE
                                )
                            }
                        } else {
                            downLoadClickListener!!.onItemClick(
                                groupIndex,
                                childIndex,
                                mGroup[groupIndex].cityItemInfos[childIndex].cityAdcode
                            )
                        }

                    }
                })
            //全省地图-全部开始
            childOfflineCityOfflineDataListBinding.allStart.setDebouncedOnClickListener(
                300L,
                onClick = {
                    if (downLoadClickListener != null && groupIndex != -1 && groupIndex < mGroup.size) {
                        val provinceInfo =
                            mapDataBusiness.getProvinceInfo(mGroup[groupIndex].cityItemInfos[childIndex].belongedProvince)
                        downLoadClickListener!!.onAllTaskOperateClick(
                            provinceInfo!!.provAdcode,
                            OperationType.OPERATION_TYPE_START
                        )
                    }
                })
            ViewClickEffectUtils.addClickScale(childOfflineCityOfflineDataListBinding.allStart, CLICKED_SCALE_95)
            //全省地图-全部暂停
            childOfflineCityOfflineDataListBinding.allPause.setDebouncedOnClickListener(
                300L,
                onClick = {
                    if (downLoadClickListener != null && groupIndex != -1 && groupIndex < mGroup.size) {
                        val provinceInfo =
                            mapDataBusiness.getProvinceInfo(mGroup[groupIndex].cityItemInfos[childIndex].belongedProvince)
                        downLoadClickListener!!.onAllTaskOperateClick(
                            provinceInfo!!.provAdcode,
                            OperationType.OPERATION_TYPE_PAUSE
                        )
                    }
                })
            ViewClickEffectUtils.addClickScale(childOfflineCityOfflineDataListBinding.allPause, CLICKED_SCALE_95)
        }
    }

    fun setDownLoadClickListener(downLoadClickListener: OnMapDataItemClickListener?) {
        this.downLoadClickListener = downLoadClickListener
    }

    interface OnMapDataItemClickListener {
        fun onStartClick(groupPosition: Int, childPosition: Int, arCode: Int)
        fun onItemClick(groupPosition: Int, childPosition: Int, arCode: Int)

        /**
         * 列表group点击
         */
        fun onGroupClick(groupPosition: Int)

        //全部开始，全部暂停 点击操作
        fun onAllTaskOperateClick(arCode: Int, @OperationType1 operateType: Int)
    }
}
