package com.desaysv.psmap.base.data

import android.graphics.Rect
import com.autonavi.gbl.aosclient.model.GRestrictRule
import com.autonavi.gbl.common.path.option.PathInfo
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.callback.IRouteResultCallBack
import com.autosdk.bussiness.navi.route.model.RouteCarResultData
import com.autosdk.bussiness.widget.route.model.RoutePathItemContent

/**
 * @author 谢锦华
 * @time 2024/1/10
 * @description 路线提供的接口
 */
interface IRouteRepository {
    //开始路径规划
    fun planRoute(
        startPoi: POI,
        endPoi: POI,
        midPois: ArrayList<POI>?,
        routeResultCallBack: IRouteResultCallBack
    ): Long

    //手车联动 线路还原
    fun planAimRoutePushMsgRoute(
        pushMsg: AimRoutePushMsg?,
        routeResultCallBack: IRouteResultCallBack
    ): Long

    //获取路线
    fun getVariantPathWrapList(mRouteCarResult: RouteCarResultData?): ArrayList<PathInfo>

    //获取路线数据
    fun getItemContentList(pathWrapList: List<PathInfo>?): ArrayList<RoutePathItemContent>

    //设置全览
    fun showPreview()

    fun showPreview(curSegIdx: Long, curLinkIdx: Long, curPointIdx: Long)

    fun getMapPreviewRect(): Rect

    //设置限行全览
    fun showRestrictPreview(rule: GRestrictRule)
}
