package com.desaysv.psmap.base.utils

import com.autonavi.gbl.util.errorcode.Route
import timber.log.Timber

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description 路线规划失败提示
 */
object RouteErrorCodeUtils {
    fun activeErrorMessage(errorCode: Int): String {
        var errorInt = -1
        try {
            errorInt = errorCode
        } catch (e: Exception) {
            Timber.e("RouteErrorCodeUtils catch Exception: ${e.message}")
        }
        return when (errorInt) {
            Route.ErrorCodeStartPointError -> "起点不在支持范围内"
            Route.ErrorCodeEndPointError -> "终点不在支持范围内"
            Route.ErrorCodeTooFar -> "相邻两个行程点直接距离过长"
            Route.ErrorCodeViaPointError -> "途经点不在支持范围内"
            Route.ErrorCodeStartNoRoad -> "起点抓路失败，请稍后再试"
            Route.ErrorCodeEndNoRoad -> "终点抓路失败，请稍后再试"
            Route.ErrorCodeHalfwayNoRoad -> "途经点抓路失败，请稍后再试"
            Route.ErrorCodeOnlineFail -> "在线算路失败，请稍后再试"
            Route.ErrorCodeOfflineRouteFailure -> "离线算路失败，请稍后再试"
            Route.ErrorCodeNetworkTimeout -> "网络请求超时，请稍后再试"
            Route.ErrorCodeNoNewwork -> "无网络连接，请稍后再试"
            Route.ErrorCodeSameVia, Route.ErrorCodeSameStartEnd, Route.ErrorCodeSameStartVia, Route.ErrorCodeSameViaEnd -> "起点、途经点或终点不能相同"
            Route.ErrorCodeInvalidVia -> "没有对应的途经点"
            Route.ErrorCodeChangeEndSameViaEnd -> "相同的途经点终点"
            Route.ErrorCodeNetworkError -> "网络错误，请稍后再试"
            Route.ErrorCodeLackStartCityData -> "起点所在城市无数据"
            Route.ErrorCodeLackWayCityData -> "途经城市缺少数据"
            Route.ErrorCodeLackEndCityData -> "终点所在城市无数据"
            Route.ErrorCodeLackViaCityData -> "途经点所在城市无数据"
            else -> "算路异常，请稍后再试"
        }
    }
}
