package com.desaysv.psmap.base.utils

import android.app.Application
import android.text.TextUtils
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.path.option.PathInfo
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.pos.model.LocInfo
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.navi.route.model.RouteCarResultData
import com.autosdk.bussiness.widget.navi.NaviComponent
import com.autosdk.common.AutoConstant
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author 谢锦华
 * @time 2024/2/23
 * @description 保存最后一次位置
 */

@Singleton
class LastRouteUtils @Inject constructor(
    private val application: Application,
    private val gson: Gson,
    private val mNaviComponent: NaviComponent,
) {

    fun saveRouteToFile(mRouteCarResultData: RouteCarResultData?) {
        if (null != mRouteCarResultData) {
            try {
                val mLocInfo: LocInfo = mNaviComponent.locationPos //直接用当前位置
                Timber.i("LastRouteUtils savePath getLocInfo is ${gson.toJson(mLocInfo)}")
                CustomFileUtils.saveFile(gson.toJson(mRouteCarResultData), AutoConstant.routeCarResultData)
            } catch (e: Exception) {
                Timber.i("LastRouteUtils savePath e = ${e.message}")
            }
        } else {
            Timber.i("LastRouteUtils savePath mRouteCarResultData== null ")
        }
    }

    fun getLastRouteCarResultData(): Pair<RouteCarResultData, ArrayList<PathInfo>>? {
        val localData: String? = CustomFileUtils.getFile(AutoConstant.routeCarResultData)
        if (!TextUtils.isEmpty(localData)) {
            try {
                val lastRouteCarResultData = gson.fromJson(localData, RouteCarResultData::class.java)
                if (null != lastRouteCarResultData) {
                    val paths: ArrayList<PathInfo> = mNaviComponent.getPathResultData(lastRouteCarResultData)
                    if (paths != null && paths.isNotEmpty()) {
                        Timber.i(" pathResultData Count:${paths.size}")
                        if (null == lastRouteCarResultData.fromPOI || null == lastRouteCarResultData.toPOI || paths.size <= 0 || lastRouteCarResultData.pathResultDataInfo == null) {
                            Timber.w("getLastRouteCarResultData invalid path")
                            return null
                        }
                    } else {
                        Timber.i("getLastRouteCarResultData pathResult == null")
                        return null
                    }
                    val dis: Double = distanceTwoPoint(lastRouteCarResultData)
                    if (dis < 500) {
                        deleteRouteFile()
                        Timber.i("dis<500")
                        return null
                    }
                    return Pair(lastRouteCarResultData, paths)
                }
            } catch (e: Exception) {
                Timber.i("getLastRouteCarResultData e = ${e.message}")
            }
        }
        return null
    }

    fun deleteRouteFile() {
        Timber.i("deleteRouteFile is called")
        try {
            CustomFileUtils.saveFile("", AutoConstant.routeCarResultData)
        } catch (e: java.lang.Exception) {
            Timber.i("deleteRouteFile e = ${e.message}")
        }
    }

    fun setContextPos() {
        mNaviComponent.setContextPos()
    }

    fun distanceTwoPoint(resultData: RouteCarResultData?): Double {
        val location = LocationController.getInstance().lastLocation
        if (null == resultData || null == resultData.toPOI || null == resultData.toPOI.point || null == location) {
            return (-1).toDouble()
        }
        val toPOI = resultData.toPOI
        val to = Coord2DDouble()
        to.lat = toPOI.point.latitude
        to.lon = toPOI.point.longitude
        val from = Coord2DDouble()
        from.lat = location.latitude
        from.lon = location.longitude
        return BizLayerUtil.calcDistanceBetweenPoints(from, to)
    }
}