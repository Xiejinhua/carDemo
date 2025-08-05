package com.desaysv.psmap.ui.settings.trip

import android.graphics.Rect
import androidx.lifecycle.ViewModel
import com.autosdk.bussiness.account.bean.TrackItemBean
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.TripBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 行程详情ViewModel
 */
@HiltViewModel
class MyTripDetailViewModel @Inject constructor(
    private val tripBusiness: TripBusiness,
    private val settingAccountBusiness: SettingAccountBusiness
) : ViewModel() {
    val time = tripBusiness.time //时间 比如：2023/07/17  15:33·
    val type = tripBusiness.type //类型 比如：导航
    val startName = tripBusiness.startName //起点名称 比：钱氏饭店
    val endName = tripBusiness.endName //终点名称 比：万达广场
    val averageSpeed = tripBusiness.averageSpeed //平均速度 比：69km/h
    val drivingDuration = tripBusiness.drivingDuration //驾驶时长 比：00:04:50
    val allTrip = tripBusiness.allTrip //驾驶里程 比：13.8公里
    val maximumSpeed = tripBusiness.maximumSpeed //最快速度 比：13.8公里
    val showToast = tripBusiness.showToast
    val finishPage = tripBusiness.finishPage
    val loginLoading = settingAccountBusiness.loginLoading

    fun getTrackItemBean(): TrackItemBean? {
        return tripBusiness.getTrackItemBean()
    }

    fun setTrackItemBean(trackItemBean: TrackItemBean?) {
        tripBusiness.setTrackItemBean(trackItemBean)
    }

    fun setRect(mapRect: Rect?) {
        tripBusiness.setRect(mapRect)
    }

    override fun onCleared() {
        super.onCleared()
        tripBusiness.onCleared()
    }

    fun initData() {
        tripBusiness.initData()
    }

    fun onDestroyView() {
        tripBusiness.onDestroyView()
    }

    fun deleteById(id: String?) {
        tripBusiness.deleteById(id)
    }
}