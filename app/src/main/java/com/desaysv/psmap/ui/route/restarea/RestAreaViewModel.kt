package com.desaysv.psmap.ui.route.restarea

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.search.model.LinePoiServiceAreaChild
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/12/25
 * @description 云控信息 避开拥堵 禁行ViewModel
 */
@HiltViewModel
class RestAreaViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val application: Application,
) : ViewModel() {
    //单个沿途服务区数据
    val alongWayPoiDeepInfo = mRouteBusiness.alongWayPoiDeepInfo

    val address = alongWayPoiDeepInfo.map {
        it?.let { item -> item.addr.ifEmpty { item.name } }
    }
    val distanceAndTime = alongWayPoiDeepInfo.map {
        it?.let { item ->
            CommonUtils.getTimeAndKilometersStr(
                application,
                item.travelTime.toLong(),
                item.distance.toLong()
            )
        }
    }

    val isShowCharging = MutableLiveData(false)
    val isShowGasStation = MutableLiveData(false)
    val isShowRestaurant = MutableLiveData(false)
    val isShowMaintenance = MutableLiveData(false)
    val isShowToilet = MutableLiveData(false)
    val isShowStore = MutableLiveData(false)
    val isShowHotel = MutableLiveData(false)

    val isShowBgLl = MutableLiveData(false)//是否显示背景

    val gasStationName = MutableLiveData("")
    val gasStationIcon = MutableLiveData(0)

    override fun onCleared() {
        super.onCleared()
        mRouteBusiness.unitAlongWayPoiDeepInfo()
    }

    fun handleAlongWayPoiDeepInfo(linePoiServiceAreaChild: ArrayList<LinePoiServiceAreaChild>?) {
        isShowCharging.postValue(false)
        isShowGasStation.postValue(false)
        isShowRestaurant.postValue(false)
        isShowMaintenance.postValue(false)
        isShowToilet.postValue(false)
        isShowStore.postValue(false)
        isShowHotel.postValue(false)
        isShowBgLl.postValue(false)
        linePoiServiceAreaChild?.takeIf { it.isNotEmpty() }?.let { linePoiChildrens ->
            var accumulate = 0
            for (i in linePoiChildrens.indices) {
                val typeCodeStr = linePoiChildrens[i].childBase.typecode
                Timber.i("====handleServiceType===typeCodeStr = $typeCodeStr")
                if (TextUtils.isEmpty(typeCodeStr)) {
                    continue
                }
                val typeCodes = typeCodeStr.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (j in typeCodes.indices) {
                    val typeCode = typeCodes[j].toInt()
                    if (11100 == typeCode) {
                        //充电站
                        isShowCharging.postValue(true)
                        accumulate++
                    } else if (101 == typeCode / 100) {
                        //加油站信息
                        Timber.i("====服务区 gasInfo gasType = ${linePoiChildrens[i].gasType}, minMame = ${linePoiChildrens[i].childBase.minMame}")
                        var gasType = linePoiChildrens[i].gasType
                        if (TextUtils.isEmpty(gasType)) {
                            gasType = "加油站"
                        }
//                        mTvRestAreaGasInfo.setText(gasType.replace("\\|".toRegex(), " "))
                        accumulate++
                        isShowGasStation.postValue(true)
                        gasStationName.postValue(linePoiChildrens[i].childBase.name)
                        val minMame = linePoiChildrens[i].childBase.minMame
                        when (minMame) {
                            "中国石化" -> {
                                // 处理中国石化的逻辑
                                gasStationIcon.postValue(R.drawable.ic_route_sinopec)
                            }

                            "中国石油" -> {
                                // 处理中国石油的逻辑
                                gasStationIcon.postValue(R.drawable.ic_route_petro_china)
                            }

                            "壳牌" -> {
                                // 处理壳牌的逻辑
                                gasStationIcon.postValue(R.drawable.ic_route_shell)
                            }

                            "美孚" -> {
                                // 处理美孚的逻辑
                                gasStationIcon.postValue(R.drawable.ic_route_mobil)
                            }

                            else -> {
                                // 处理其他情况
                                gasStationIcon.postValue(R.drawable.ic_route_gas_station_default)
                            }
                        }

                    } else if (5 == typeCode / 10000) {
                        //餐馆
                        isShowRestaurant.postValue(true)
                        accumulate++
                    } else if (3 == typeCode / 10000) {
                        //维修
                        isShowMaintenance.postValue(true)
                        accumulate++
                    } else if (2003 == typeCode / 100) {
                        //厕所
                        isShowToilet.postValue(true)
                        accumulate++
                    } else if (6 == typeCode / 10000) {
                        //商店
                        isShowStore.postValue(true)
                        accumulate++
                    } else if (10 == typeCode / 10000) {
                        //住宿
                        isShowHotel.postValue(true)
                        accumulate++
                    }
                }
            }
            if (accumulate > 0) {
                isShowBgLl.postValue(true)
            }
        }
    }

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        viewModelScope.launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }
}