package com.desaysv.psmap.ui.navi

import android.app.Application
import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.NavigationUtil
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.TripBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/12/25
 * @description 驾驶行为报告ViewModel
 */
@HiltViewModel
class DriverReportViewModel @Inject constructor(
    private val mNaviBusiness: NaviBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val mTripBusiness: TripBusiness,
    private val mSettingAccountBusiness: SettingAccountBusiness,
    private val settingComponent: ISettingComponent,
    private val application: Application
) : ViewModel() {

    //驾车导航过程 统计信息
    private val naviStatisticsInfo = mNaviBusiness.naviStatisticsInfo

    //进行终点周边搜  停车场推荐
    val parkingRecommend = mNaviBusiness.parkingRecommend

    val isParkingRecommendEmpty = parkingRecommend.map { it.isEmpty() }

    private val _btTip = MutableLiveData<String>()
    val btTip: LiveData<String> = _btTip

    //驾驶里程
    val mileageDriven = naviStatisticsInfo.map { info ->
        info?.let {
            NavigationUtil.meterToStrEnglish(application, it.drivenDist.toLong())
        }
    }

    //驾驶时长
    val drivingTime = naviStatisticsInfo.map { info ->
        info?.let {
            NavigationUtil.formatTime(it.drivenTime)
        }
    }

    //平均速度
    val averageSpeed = naviStatisticsInfo.map { info ->
        info?.let {
            "${it.averageSpeed}km/h"
        }
    }

    //最高速度
    val fastestSpeed = naviStatisticsInfo.map { info ->
        info?.let {
            "${it.highestSpeed}km/h"
        }
    }


    override fun onCleared() {
        super.onCleared()
        mNaviBusiness.cleanParkingRecommend()
        mNaviBusiness.cleanNaviStatisticsInfo()
        mTripBusiness.onCleared(isBackCurrentCarPosition = false)
    }

    /**
     * 设置按钮文字
     */
    fun setBtTip(tip: String) {
        Timber.i("setBtTip $tip")
        _btTip.postValue(tip)
    }

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        //路线规划监听初始化
        mRouteBusiness.outsideInit()
        viewModelScope.launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }

    fun setRect(mapRect: Rect?) {
        mTripBusiness.setRect(mapRect)
    }

    /**
     * 获取行程报告轨迹
     */
    fun getTripReportTrack(): Boolean {
        if (settingComponent.getAutoRecord() != 0) {
            Timber.i("getTripReportTrack: Auto record is disabled getAutoRecord = ${settingComponent.getAutoRecord()}")
            return false
        }
        return mTripBusiness.getTripReportTrack()
    }
}

