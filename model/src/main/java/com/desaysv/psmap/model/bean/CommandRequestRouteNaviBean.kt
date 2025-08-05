package com.desaysv.psmap.model.bean

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.navi.route.model.RouteCarResultData
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.utils.Biz

class CommandRequestRouteNaviBean() : Parcelable {
    @get:BaseConstant.Type
    @BaseConstant.Type
    var type = 0
    var start: POI? = null
    var end: POI? = null
    var midPois: ArrayList<POI>? = null
    var aimRoutePushMsg: AimRoutePushMsg? = null
    fun toBundle() = Bundle().apply {
        putParcelable(Biz.KEY_BIZ_ROUTE_START_END_VIA_POI_LIST, this@CommandRequestRouteNaviBean)
    }

    constructor(parcel: Parcel) : this() {
        type = parcel.readInt()
        start = parcel.readParcelable(POI::class.java.classLoader)
        end = parcel.readParcelable(POI::class.java.classLoader)
        midPois = java.util.ArrayList()
        parcel.readList(midPois!!, POI::class.java.classLoader)
        aimRoutePushMsg = parcel.readSerializable() as AimRoutePushMsg?
    }


    class Builder {
        private val commandRequestBean: CommandRequestRouteNaviBean = CommandRequestRouteNaviBean()

        fun setStart(start: POI?): Builder {
            commandRequestBean.start = start
            return this
        }

        fun setEnd(end: POI?): Builder {
            commandRequestBean.end = end
            return this
        }

        fun setMidPois(midPois: ArrayList<POI>?): Builder {
            commandRequestBean.midPois = midPois
            return this
        }

        fun build(start: POI?, end: POI, midPois: ArrayList<POI>? = null): CommandRequestRouteNaviBean {
            commandRequestBean.type = BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD//需要发起路径规划
            val location = LocationController.getInstance().lastLocation
            val startPoi = POIFactory.createPOI("我的位置", GeoPoint(location.longitude, location.latitude))
            commandRequestBean.start = start ?: startPoi
            commandRequestBean.midPois = midPois
            commandRequestBean.end = end
            return commandRequestBean
        }

        fun build(end: POI, midPois: ArrayList<POI>? = null): CommandRequestRouteNaviBean {
            commandRequestBean.type = BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD//需要发起路径规划
            val location = LocationController.getInstance().lastLocation
            val startPoi = POIFactory.createPOI("我的位置", GeoPoint(location.longitude, location.latitude))
            commandRequestBean.start = startPoi
            commandRequestBean.midPois = midPois
            commandRequestBean.end = end
            return commandRequestBean
        }

        fun buildMisPoi(end: POI, midPois: ArrayList<POI>? = null): CommandRequestRouteNaviBean {
            commandRequestBean.type = BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD_MISPOI//需要发起路径规划 添加途经点
            val location = LocationController.getInstance().lastLocation
            val startPoi = POIFactory.createPOI("我的位置", GeoPoint(location.longitude, location.latitude))
            commandRequestBean.start = startPoi
            commandRequestBean.midPois = midPois
            commandRequestBean.end = end
            return commandRequestBean
        }

        fun build(routeCarResultData: RouteCarResultData): CommandRequestRouteNaviBean {
            commandRequestBean.type = BaseConstant.Type.NEED_RX_PLAN_HAVE_SUCCESS//路径规划已经成功
            commandRequestBean.midPois = routeCarResultData.midPois
            commandRequestBean.start = routeCarResultData.fromPOI
            commandRequestBean.end = routeCarResultData.toPOI
            return commandRequestBean
        }

        fun buildByFile(routeCarResultData: RouteCarResultData): CommandRequestRouteNaviBean {
            commandRequestBean.type = BaseConstant.Type.NEED_FILE_DATA_HAVE_SUCCESS//从本地文件获取保存的路径规划的数据
            commandRequestBean.midPois = routeCarResultData.midPois
            commandRequestBean.start = routeCarResultData.fromPOI
            commandRequestBean.end = routeCarResultData.toPOI
            return commandRequestBean
        }

        fun buildByPushRoute(aimRoutePushMsg: AimRoutePushMsg): CommandRequestRouteNaviBean {
            commandRequestBean.type = BaseConstant.Type.NEED_PHONE_SEND_ROUTE_DATA//手机推送发送的数据
            commandRequestBean.aimRoutePushMsg = aimRoutePushMsg
            return commandRequestBean
        }

        fun buildNoNeedPlanRoute(routeCarResultData: RouteCarResultData): CommandRequestRouteNaviBean {
            commandRequestBean.type = BaseConstant.Type.NEED_NULL//路线规划界面不需要发起请求路线
            commandRequestBean.midPois = routeCarResultData.midPois
            commandRequestBean.start = routeCarResultData.fromPOI
            commandRequestBean.end = routeCarResultData.toPOI
            return commandRequestBean
        }
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeParcelable(start, flags)
        parcel.writeParcelable(end, flags)
        parcel.writeList(midPois)
        parcel.writeSerializable(aimRoutePushMsg)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommandRequestRouteNaviBean> {
        override fun createFromParcel(parcel: Parcel): CommandRequestRouteNaviBean {
            return CommandRequestRouteNaviBean(parcel)
        }

        override fun newArray(size: Int): Array<CommandRequestRouteNaviBean?> {
            return arrayOfNulls(size)
        }
    }
}
