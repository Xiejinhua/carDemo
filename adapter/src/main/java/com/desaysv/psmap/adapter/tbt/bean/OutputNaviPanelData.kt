package com.desaysv.psmap.adapter.tbt.bean


data class OutputNaviPanelData(
    var exitNumber: String = "",//出口编号
    var exitDirectionInfo: String = "",//出口信息
    var exitVisible: Boolean = false,//是否显示出口
    var naviLaneVisible: Boolean = false,//是否显示车道线
    var naviLaneInfo: ArrayList<TBTLaneInfoBean> = ArrayList(),//车道线信息
    var routeRequesting: Boolean = false,//是否处于算路中
    var distanceNextRoute: String = "", //距离下一个路口距离
    var distanceNextRouteUnit: String = "", //距离下一个路口距离 单位
    var nextRouteName: String = "", //距离下一个路口名称
    var distanceNextCross: Int = 0, //显示到下一条道路的路口大图的距离
    var timeAndDistance: String = "",//剩余时间和距离
    var arriveTime: String = "",//到达时间
    var crossViewVisible: Boolean = false,//是否显示路口放大图
    var nearThumInfoVisible: Boolean = false,//(接近)进阶动作信息显示隐藏通知
    var nearRoadName: String = "",//进阶动作 路口名称
    var turnIconID: Int = 0,//转向动作图标
    var nearThumTurnIconID: Int = 0,//更新(接近)进阶动作图标
    var cityInfo: CityItemInfo = CityItemInfo()//城市信息
)
