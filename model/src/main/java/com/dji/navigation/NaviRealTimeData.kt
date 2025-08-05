package com.dji.navigation

import android.os.Parcel
import android.os.Parcelable


class NaviRealTimeData(
    var mMessageType: MsgType = MsgType.MSG_TYPE_NULL,                          //消息类型
    var mNaviType: Status = Status.STATUS_NONE,                                 // MsgType.MSG_TYPE_NAVI_STATUS 在线导航模式
    var mPathId: Long = 0,                                                      // TBT 导航路径ID
    var mAdCode: Int = 0,                                                       // TBT 城市编码
    var mCurIconType: IconType = IconType.ICON_TYPE_NONE,                       // TBT 导航推荐动作
    var mAllLength: Int = 0,                                                    // TBT 导航全程长度[m]
    var mPathRetainDistance: Int = 0,                                           // TBT 路线剩余距离[m]
    var mCurLinkId: Int = 0,                                                    // TBT 当前小路段Link Id
    var mCurStepId: Int = 0,                                                    // TBT 当前导航段Step Id
    var mCurRoadName: String = "",                                              // TBT 当前道路名
    var mDistanceToNextStep: Int = 0,                                           // TBT 到下个Step的距离[m]
    var mDistanceToNextLink: Int = 0,                                           // TBT 到下个Link的距离[m]
    var mNextCloseIconType: IconType = IconType.ICON_TYPE_NONE,                 // TBT 下一个接近的导航推荐动作，无则填0即ICON_TYPE_NONE;
    var mNextCloseIconDistance: Int = 0,                                        // TBT 下一个接近的导航推荐动作增量距离 如随后30米左转填30，无则填0
    var mCurRoadClass: RoadClass = RoadClass.ROAD_CLASS_NULL,                   // MsgType.MSG_TYPE_ROAD_TYPE_AND_CLASS_GPS 当前道路等级
    var mCurLinkType: LinkType = LinkType.LINK_TYPE_NULL,                       // MsgType.MSG_TYPE_ROAD_TYPE_AND_CLASS_GPS 当前Link类型
    var mCurRoadType: FormWay = FormWay.FormayNULL,                             // MsgType.MSG_TYPE_ROAD_TYPE_AND_CLASS_GPS 当前道路类型
    var mCurPosition: GpsPoint3D? = null,                                       // MsgType.MSG_TYPE_ROAD_TYPE_AND_CLASS_GPS 当前位置Gps
    var mCurSpeedLimit: Int = 0,                                                // MsgType.MSG_TYPE_ROAD_SPEED_LIMIT 当前道路限速[km/h]
    var mLaneActions: LaneActions? = null,                                      // MsgType.MSG_TYPE_LANE 车道前背景
    var mCameraInfo: CameraInfo? = null,                                        // MsgType.MSG_TYPE_CAMERA_INFO 摄像头信息
    var mFacilities: AmapFacility? = null,                                      // MsgType.MSG_TYPE_ROAD_SITUATION 道路设施
    var mParallelRoadStatus: ParallelRoadStatus = ParallelRoadStatus.PARALLEL_ROAD_NONE,//MsgType.MSG_TYPE_PARALLELROAD_STATUS 平行路状态
    var mTollGateInfo: TollGateInfo? = null,                                    // MsgType.MSG_TYPE_SAPA_INFO 收费站信息
    var mPOIInfo: AmapPOIInfo? = null,                                          // MsgType.MSG_TYPE_POI_INFO 建图起点和终点名称信息
    var mParkingPOIName: String = "",                                           // MsgType.MSG_TYPE_PARKING_NAME 记忆泊车POI名称

) : Parcelable {
    constructor (parcel: Parcel) : this() {
        mMessageType = MsgType.get(parcel.readInt())
        mNaviType = Status.get(parcel.readInt())
        mCurIconType = IconType.get(parcel.readInt())
        mNextCloseIconType = IconType.get(parcel.readInt())
        mCurRoadClass = RoadClass.get(parcel.readInt())
        mCurLinkType = LinkType.get(parcel.readInt())
        mCurRoadType = FormWay.get(parcel.readInt())
        mDistanceToNextStep = parcel.readInt()
        mCurRoadName = parcel.readString().toString()
        mParkingPOIName = parcel.readString().toString()
        mNextCloseIconDistance = parcel.readInt()
        mAllLength = parcel.readInt()
        mPathRetainDistance = parcel.readInt()
        mCurPosition = parcel.readParcelable(GpsPoint3D::class.java.classLoader)
        mCurSpeedLimit = parcel.readInt()
        mCurLinkId = parcel.readInt()
        mCurStepId = parcel.readInt()
        mDistanceToNextLink = parcel.readInt()
        mCameraInfo = parcel.readParcelable(CameraInfo::class.java.classLoader)
        mFacilities = parcel.readParcelable(AmapFacility::class.java.classLoader)
        mTollGateInfo = parcel.readParcelable(TollGateInfo::class.java.classLoader)
        mPOIInfo = parcel.readParcelable(AmapPOIInfo::class.java.classLoader)
        mPathId = parcel.readLong();
        mParallelRoadStatus = ParallelRoadStatus.get(parcel.readInt())
        mAdCode = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(mMessageType.id)
        parcel.writeInt(mNaviType.id)
        parcel.writeInt(mCurIconType.id)
        parcel.writeInt(mNextCloseIconType.id)
        parcel.writeInt(mCurRoadClass.id)
        parcel.writeInt(mCurRoadType.id)
        parcel.writeInt(mCurLinkType.id)
        parcel.writeInt(mNextCloseIconDistance)
        parcel.writeInt(mDistanceToNextStep)
        parcel.writeInt(mAllLength)
        parcel.writeString(mCurRoadName)
        parcel.writeString(mParkingPOIName)
        parcel.writeInt(mPathRetainDistance)
        parcel.writeInt(mCurSpeedLimit)
        parcel.writeInt(mCurLinkId)
        parcel.writeInt(mCurStepId)
        parcel.writeInt(mDistanceToNextLink)
        parcel.writeParcelable(mCurPosition, flags)
        parcel.writeParcelable(mCameraInfo, flags)
        parcel.writeParcelable(mFacilities, flags)
        parcel.writeParcelable(mTollGateInfo, flags)
        parcel.writeParcelable(mPOIInfo, flags)
        parcel.writeLong(mPathId)
        parcel.writeInt(mParallelRoadStatus.id)
        parcel.writeInt(mAdCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NaviRealTimeData> {
        override fun createFromParcel(parcel: Parcel): NaviRealTimeData {
            return NaviRealTimeData(parcel)
        }

        override fun newArray(size: Int): Array<NaviRealTimeData?> {
            return arrayOfNulls(size)
        }
    }
}

data class LaneActions(
    val mDistanceToLaneActions: Double,                                         //到前背景车道的距离
    var mForegroundLaneType: ArrayList<LaneType>? = null,                       // 车道前景
    var mBackgroundLaneType: ArrayList<LaneType>? = null,                       // 车道背景
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readArrayList(LaneType::class.java.classLoader) as ArrayList<LaneType>?,
        parcel.readArrayList(LaneType::class.java.classLoader) as ArrayList<LaneType>?
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(mDistanceToLaneActions)
        parcel.writeList(mForegroundLaneType as ArrayList<*>?)
        parcel.writeList(mBackgroundLaneType as ArrayList<*>?)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LaneActions> {
        override fun createFromParcel(parcel: Parcel): LaneActions {
            return LaneActions(parcel)
        }

        override fun newArray(size: Int): Array<LaneActions?> {
            return arrayOfNulls(size)
        }
    }
}

data class GpsPoint3D(
    val latitude: Double,//纬度[度] (GCJ-02)
    val longitude: Double,//经度[度] (GCJ-02)
    val altitude: Double//高度[度] (GCJ-02)
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(altitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GpsPoint3D> {
        override fun createFromParcel(parcel: Parcel): GpsPoint3D {
            return GpsPoint3D(parcel)
        }

        override fun newArray(size: Int): Array<GpsPoint3D?> {
            return arrayOfNulls(size)
        }
    }
}


data class CameraInfo(
    val distance: Int,
    val type: CameraType = CameraType.CAMERA_TYPE_NULL,
    val mValue1: Int,
    val mValue2: Int,
    var mGpsPoint: GpsPoint3D? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        CameraType.get(parcel.readInt()),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(GpsPoint3D::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(distance)
        parcel.writeInt(type.id)
        parcel.writeInt(mValue1)
        parcel.writeInt(mValue2)
        parcel.writeParcelable(mGpsPoint, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CameraInfo> {
        override fun createFromParcel(parcel: Parcel): CameraInfo {
            return CameraInfo(parcel)
        }

        override fun newArray(size: Int): Array<CameraInfo?> {
            return arrayOfNulls(size)
        }
    }

}

/**
 * 路况数据类
 */
data class AmapFacility(
    val distance: Int,
    val type: FacilityType,
    val mValue1: Int,
    val mValue2: Int,
    var mGpsPoint: GpsPoint3D? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        FacilityType.get(parcel.readInt()),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readParcelable(GpsPoint3D::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(distance)
        parcel.writeInt(type.id)
        parcel.writeInt(mValue1)
        parcel.writeInt(mValue2)
        parcel.writeParcelable(mGpsPoint, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AmapFacility> {
        override fun createFromParcel(parcel: Parcel): AmapFacility {
            return AmapFacility(parcel)
        }

        override fun newArray(size: Int): Array<AmapFacility?> {
            return arrayOfNulls(size)
        }
    }

}

/**
 * 收费站信息类
 */
data class TollGateInfo(
    val isValid: Int, // 0:该信息无效，1：该信息有效
    val distance: Int, // 到收费站的距离
    var mLaneTypes: ArrayList<Int>? = null, // 收费站车道类型, 单个车道支持多种类型
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readArrayList(Int::class.java.classLoader) as ArrayList<Int>?
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(isValid)
        parcel.writeInt(distance)
        parcel.writeList(mLaneTypes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TollGateInfo> {
        override fun createFromParcel(parcel: Parcel): TollGateInfo {
            return TollGateInfo(parcel)
        }

        override fun newArray(size: Int): Array<TollGateInfo?> {
            return arrayOfNulls(size)
        }
    }
}

/**
 * 建图起点和终点名称信息
 */
data class AmapPOIInfo(
    val startPoiName: String = "",  // 起点名称
    val endPoiName: String = "",    // 终点名称
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(startPoiName)
        parcel.writeString(endPoiName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AmapPOIInfo> {
        override fun createFromParcel(parcel: Parcel): AmapPOIInfo {
            return AmapPOIInfo(parcel)
        }

        override fun newArray(size: Int): Array<AmapPOIInfo?> {
            return arrayOfNulls(size)
        }
    }
}


/**
 * 消息类型
 */
enum class MsgType(val id: Int) {
    MSG_TYPE_NULL(0),
    MSG_TYPE_NAVI_STATUS(1),        // 导航状态-在线导航模式
    MSG_TYPE_TBT(2),                // TBT
    MSG_TYPE_LANE(3),               // 车道前背景
    MSG_TYPE_ROAD_SPEED_LIMIT(4),   // 当前道路限速
    MSG_TYPE_ROAD_TYPE_AND_CLASS_GPS(5),// 当前道路类型、道路等级、link类型、位置Gps
    MSG_TYPE_CAMERA_INFO(6),        //摄像头信息
    MSG_TYPE_ROAD_SITUATION(7),     //道路设施
    MSG_TYPE_PARALLELROAD_STATUS(8),//平行路状态
    MSG_TYPE_SAPA_INFO(9),          //收费站信息
    MSG_TYPE_POI_INFO(10),          //建图起点和终点名称信息
    MSG_TYPE_PARKING_NAME(11);      //记忆泊车POI名称

    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

/**
 * 导航动作icon类型，实时数据使用
 */
enum class IconType(val id: Int) {
    ICON_TYPE_NONE(0),                       // 无定义（数值：0）自定义转向图标数组，请忽略这个元素，从左转图标开始
    ICON_TYPE_DEFAULT(1),                    // 自车图标（数值：1）自定义转向图标数组，请忽略这个元素，从左转图标开始
    ICON_TYPE_LEFT(2),                       // 左转图标（数值：2）
    ICON_TYPE_RIGHT(3),                      // 右转图标（数值：3）
    ICON_TYPE_LEFT_FRONT(4),                 // 左前方图标（数值：4）
    ICON_TYPE_RIGHT_FRONT(5),                // 右前方图标（数值：5）
    ICON_TYPE_LEFT_BACK(6),                  // 左后方图（数值：6）
    ICON_TYPE_RIGHT_BACK(7),                 // 右后方图标（数值：7）
    ICON_TYPE_LEFT_TURN_AROUND(8),           // 左转掉头图标（数值：8）
    ICON_TYPE_STRAIGHT(9),                   // 直行图标（数值：9）
    ICON_TYPE_ARRIVED_WAYPOINT(10),          // 到达途经点图标（数值：10）
    ICON_TYPE_ENTER_ROUNDABOUT(11),          // 进入环岛图标（数值：11）
    ICON_TYPE_OUT_ROUNDABOUT(12),            // 驶出环岛图标（数值：12）
    ICON_TYPE_ARRIVED_SERVICE_AREA(13),      // 到达服务区图标（数值：13）
    ICON_TYPE_ARRIVED_TOLLGATE(14),          // 到达收费站图标（数值：14）
    ICON_TYPE_ARRIVED_DESTINATION(15),       // 到达目的地图标（数值：15）
    ICON_TYPE_ARRIVED_TUNNEL(16),            // 到达隧道图标（数值：16）
    ICON_TYPE_ENTRY_LEFT_RING(17),           // 进入环岛，注意，这个是左侧通行地区的顺时针环岛（数值：17）
    ICON_TYPE_LEAVE_LEFT_RING(18),           // 驶出环岛，注意，这个是左侧通行地区的顺时针环岛（数值：18）
    ICON_TYPE_U_TURN_RIGHT(19),              // 右转掉头图标 ，注意，这个是左侧通行地区的掉头（数值：19）
    ICON_TYPE_SPECIAL_CONTINUE(20),          // 顺行图标（数值：20）
    ICON_TYPE_ENTRY_RING_LEFT(21),           // 标准小环岛,绕环岛左转,右侧通行地区的逆时针环岛（数值：21）
    ICON_TYPE_ENTRY_RING_RIGHT(22),          // 标准小环岛,绕环岛右转,右侧通行地区的逆时针环岛（数值：22）
    ICON_TYPE_ENTRY_RING_CONTINUE(23),       // 标准小环岛,绕环岛直行,右侧通行地区的逆时针环岛（数值：23）
    ICON_TYPE_ENTRY_RING_UTURN(24),          // 标准小环岛,绕环岛调头,右侧通行地区的逆时针环岛（数值：24）
    ICON_TYPE_ENTRY_LEFT_RING_LEFT(25),      // 标准小环岛,绕环岛左转,左侧通行地区的顺时针环岛（数值：25）
    ICON_TYPE_ENTRY_LEFT_RING_RIGHT(26),     // 标准小环岛 绕环岛右转，左侧通行地区的顺时针环岛（数值：26）
    ICON_TYPE_ENTRY_LEFT_RING_CONTINUE(27),  // 标准小环岛 绕环岛直行，左侧通行地区的顺时针环岛（数值：27）
    ICON_TYPE_ENTRY_LEFTRINGU_TURN(28),      // 标准小环岛 绕环岛调头，左侧通行地区的顺时针环岛（数值：28）
    ICON_TYPE_CROSSWALK(29),                 // 通过人行横道图标（数值：29）骑行、步行专有图标
    ICON_TYPE_OVERPASS(30),                  // 通过过街天桥图标（数值：30）骑行、步行专有图标
    ICON_TYPE_UNDERPASS(31),                 // 通过地下通道图标（数值：31）骑行、步行专有图标
    ICON_TYPE_SQUARE(32),                    // 通过广场图标（数值：32）骑行、步行专有图标
    ICON_TYPE_PARK(33),                      // 通过公园图标（数值：33）骑行、步行专有图标
    ICON_TYPE_STAIRCASE(34),                 // 通过扶梯图标（数值：34）骑行、步行专有图标
    ICON_TYPE_LIFT(35),                      // 通过直梯图标（数值：35）骑行、步行专有图标
    ICON_TYPE_CABLEWAY(36),                  // 通过索道图标（数值：36）骑行、步行专有图标
    ICON_TYPE_SKY_CHANNEL(37),               // 通过空中通道图标（数值：37）骑行、步行专有图标
    ICON_TYPE_CHANNEL(38),                   // 通过通道、建筑物穿越通道图标（数值：38）骑行、步行专有图标
    ICON_TYPE_WALK_ROAD(39),                 // 通过行人道路图标（数值：39）骑行、步行专有图标
    ICON_TYPE_CRUISE_ROUTE(40),              // 通过游船路线图标（数值：40）骑行、步行专有图标
    ICON_TYPE_SIGHTSEEING_BUSLINE(41),       // 通过观光车路线图标（数值：41）骑行、步行专有图标
    ICON_TYPE_SLIDEWAY(42),                  // 通过滑道图标（数值：42）骑行、步行专有图标
    ICON_TYPE_LADDER(43),                    // 通过阶梯图标（数值：43）骑行、步行专有图标
    ICON_TYPE_SLOPE(44),                     // 通过斜坡（数值：44）骑行、步行专有图标
    ICON_TYPE_BRIDGE(45),                    // 通过桥（数值：45）骑行、步行专有图标
    ICON_TYPE_SUBWAY(46),                    // 通过地铁通道（数值：47）骑行、步行专有图标
    ICON_TYPE_FERRY(47),                     // 通过轮渡（数值：46）骑行、步行专有图标
    ICON_TYPE_ENTER_BUILDING(48),            // 进入建筑物（数值：48）骑行、步行专有图标
    ICON_TYPE_LEAVE_BUILDING(49),            // 离开建筑物（数值：49）骑行、步行专有图标
    ICON_TYPE_BY_ELEVATOR(50),               // 电梯换层（数值：50）骑行、步行专有图标
    ICON_TYPE_BY_STAIR(51),                  // 楼梯换层（数值：51）骑行、步行专有图标
    ICON_TYPE_BY_ESCALATOR(52),              // 扶梯换层（数值：52）骑行、步行专有图标
    ICON_TYPE_LOW_TRAFFIC_CROSS(53),         // 非导航段通过红绿灯路口（数值：53）骑行、步行专有图标
    ICON_TYPE_LOW_CROSS(54),                 // 非导航段通过普通路口（数值：54）骑行、步行专有图标
    ICON_TYPE_HOUSING_ESTATE_INNER(55),      // 小区内部路偏航抑制态（数值：55/0x37）
    ICON_TYPE_WAYCHARGE_STATION(64),         // 到达充电站图标（数值：64/0x40） (无在线图标)
    ICON_TYPE_MERGE_LEFT(65),                // 靠左图标（数值：65/0x41），1076B新增
    ICON_TYPE_MERGE_RIGHT(66);               // 靠右图标（数值：66/0x42），1076B新增

    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

/**
 * LinkType，对应全局信息中的link_type
 */
enum class LinkType(val id: Int) {
    LINK_TYPE_NULL(-1),//无效
    LINK_TYPE_ORDINARY_TYPE(0),//普通道路
    LINK_TYPE_CHANNEL_TYPE(1),//航道
    LINK_TYPE_TUNNEL_TYPE(2),//隧道
    LINK_TYPE_BRIDGE_TYPE(3),//桥梁
    LINK_TYPE_VIADUCT_TYPE(4);//高架路

    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

/**
 * 当前道路类型
 */
enum class FormWay(val id: Int) {
    FormayNULL(-1),                 //无效
    FormwayDivisedLink(1),          //主路，上下线分离
    FormwayCrossLink(2),            //复杂节点内部道路
    FormwayJCT(3),                  //JCT: 连接不同道路道路或连接同一高速道路不同方向的匝道；高速道路连接的立交桥处有中央隔离到分上下行的主干线不制作为JCT
    FormwayRoundCircle(4),          //环岛
    FormwayServiceRoad(5),          //服务区：表示高速公路服务区的道路
    FormwaySlipRoad(6),             //引路：连接高速道路与一般道路、连接一般道路与一般道路的道路
    FormwaySideRoad(7),             //辅路
    FormwaySlipJCT(8),              //引路+JCT：引路与JCT重合部分的道路
    FormwayExitLink(9),             //出口
    FormwayEntranceLink(10),        //入口
    FormwayTurnRightLineA(11),      //右转专用道
    FormwayTurnRightLineB(12),      //右转专用道
    FormwayTurnLeftLineA(13),       //左转专用道
    FormwayTurnLeftLineB(14),       //左转专用道
    FormwayCommonLink(15),          //普通道路
    FormwayTurnLeftRightLine(16),   //左右转专用道：以不同道路为入口，既供车辆右转也供车辆左转使用的车道
    FormwayServiceJCTRoad(53),      //服务区+JCT：既是服务区内部道路也是连接高速道路与高速道路的JCT的道路
    FormwayServiceSlipRoad(56),     //服务区+引路：既是服务区内部道路也是连接高速道路与一般道路的引路的道路
    FormwayServiceSlipJCTRoad(58);  //服务区+引路+JCT：同时是服务区内部道路、引路、JCT的道路

    companion object {
        fun get(id: Int) = values().firstOrNull { it.id == id } ?: FormayNULL
    }
}

/**
 * 导航动作，对应全局信息中的 main_action
 */
enum class MainAction(val id: Int) {
    MainActionNULL(0x0),//无基本导航动作
    MainActionTurnLeft(0x1),//左转
    MainActionTurnRight(0x2),//右转
    MainActionSlightLeft(0x3),//向左前方行驶
    MainActionSlightRight(0x4),//向右前方行驶
    MainActionTurnHardLeft(0x5),//向左后方行驶
    MainActionTurnHardRight(0x6),//向右后方行驶
    MainActionUTurn(0x7),//左转调头
    MainActionContinue(0x8),//直行
    MainActionMergeLeft(0x9),//靠左
    MainActionMergeRight(0x0A),//靠右
    MainActionEntryRing(0x0B),//进入环岛
    MainActionLeaveRing(0x0C),//离开环岛
    MainActionSlow(0x0D),//减速行驶
    MainActionPlugContinue(0x0E),//插入直行（泛亚特有）
    MainActionEnterBuilding(0x41),//进入建筑物
    MainActionLeaveBuilding(0x42),//离开建筑物
    MainActionByElevator(0x43),//电梯换层
    MainActionByStair(0x44),//楼梯换层
    MainActionByEscalator(0x45);//扶梯换层

    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

/**
 * 导航动作，对应全局信息中的 assistant_action
 */
enum class AssistantAction(val id: Int) {
    ASSI_ACTION_NULL(0),                           // 无辅助导航动作
    ASSI_ACTION_ENTRY_MAIN(1),                     // 进入主路
    ASSI_ACTION_ENTRY_SIDE_ROAD(2),                // 进入辅路
    ASSI_ACTION_ENTRY_FREEWAY(3),                  // 进入高速
    ASSI_ACTION_ENTRY_SLIP(4),                     // 进入匝道
    ASSI_ACTION_ENTRY_TUNNEL(5),                   // 进入隧道
    ASSI_ACTION_ENTRY_CENTER_BRANCH(6),            // 进入中间岔道
    ASSI_ACTION_ENTRY_RIGHT_BRANCH(7),             // 进入右岔路
    ASSI_ACTION_ENTRY_LEFT_BRANCH(8),              // 进入左岔路
    ASSI_ACTION_ENTRY_RIGHT_ROAD(9),               // 进入右转专用道
    ASSI_ACTION_ENTRY_LEFT_ROAD(10),               // 进入左转专用道
    ASSI_ACTION_ENTRY_MERGE_CENTER(11),            // 进入中间道路
    ASSI_ACTION_ENTRY_MERGE_RIGHT(12),             // 进入右侧道路
    ASSI_ACTION_ENTRY_MERGE_LEFT(13),              // 进入左侧道路
    ASSI_ACTION_ENTRY_MERGE_RIGHT_Sild(14),        // 靠右行驶进入辅路
    ASSI_ACTION_ENTRY_MERGE_LEFT_Sild(15),         // 靠左行驶进入辅路
    ASSI_ACTION_ENTRY_MERGE_RIGHT_MAIN(16),        // 靠右行驶进入主路
    ASSI_ACTION_ENTRY_MERGE_LEFT_MAIN(17),         // 靠左行驶进入主路
    ASSI_ACTION_ENTRY_MERGE_RIGHT_RIGHT(18),       // 靠右行驶进入右转专用道
    ASSI_ACTION_ENTRY_FERRY(19),                   // 到达航道
    ASSI_ACTION_LEFT_FERRY(20),                    // 驶离轮渡
    ASSI_ACTION_ALONG_ROAD(23),                    // 沿当前道路行驶
    ASSI_ACTION_ALONG_SILD(24),                    // 沿辅路行驶
    ASSI_ACTION_ALONG_MAIN(25),                    // 沿主路行驶
    ASSI_ACTION_ARRIVE_EXIT(32),                   // 到达出口
    ASSI_ACTION_ARRIVE_SERVICE_AREA(33),           // 到达服务区
    ASSI_ACTION_ARRIVE_TOLL_GATE(34),              // 到达收费站
    ASSI_ACTION_ARRIVE_WAY(35),                    // 到达途经地
    ASSI_ACTION_ARRIVE_DESTINATION(36),            // 到达目的地的
    ASSI_ACTION_ARRIVE_CHARGING_STATION(37),       // 到达充电站,新能源汽车专用
    ASSI_ACTION_ENTRY_RING_LEFT_(48),              // 绕环岛左转
    ASSI_ACTION_ENTRY_RING_RIGHT(49),              // 绕环岛右转
    ASSI_ACTION_ENTRY_RING_CONTINUE(50),           // 绕环岛直行
    ASSI_ACTION_ENTRY_RING_UTURN(51),              // 绕环岛右转
    ASSI_ACTION_SMALL_RING_NOT_COUNT(52),          // 小环岛不数出口
    ASSI_ACTION_RIGHT_BRANCH_1(64),                // 到达复杂路口，走右边第一出口
    ASSI_ACTION_RIGHT_BRANCH_2(65),                // 到达复杂路口，走右边第二出口
    ASSI_ACTION_RIGHT_BRANCH_3(66),                // 到达复杂路口，走右边第三出口
    ASSI_ACTION_RIGHT_BRANCH_4(67),                // 到达复杂路口，走右边第四出口
    ASSI_ACTION_RIGHT_BRANCH_5(68),                // 到达复杂路口，走右边第五出口
    ASSI_ACTION_LEFT_BRANCH_1(69),                 // 到达复杂路口，走左边第一出口
    ASSI_ACTION_LEFT_BRANCH_2(70),                 // 到达复杂路口，走左边第二出口
    ASSI_ACTION_LEFT_BRANCH_3(71),                 // 到达复杂路口，走左边第三出口
    ASSI_ACTION_LEFT_BRANCH_4(72),                 // 到达复杂路口，走左边第四出口
    ASSI_ACTION_LEFT_BRANCH_5(73),                 // 到达复杂路口，走左边第五出口
    ASSI_ACTION_ENTER_ULINE(80),                   // 进入调头专用路
    ASSI_ACTION_PASS_CROSSWalk(90),                // 通过人行横道
    ASSI_ACTION_PASS_OVERPASS(91),                 // 通过过街天桥
    ASSI_ACTION_PASS_UNDERGROUND(92),              // 通过地下通道
    ASSI_ACTION_PASS_SQUARE(93),                   // 通过广场
    ASSI_ACTION_PASS_PARK(94),                     // 通过公园
    ASSI_ACTION_PASS_STAIRCASE(95),                // 通过扶梯
    ASSI_ACTION_PASS_LIFT(96),                     // 通过直梯
    ASSI_ACTION_PASS_CABLEWAY(97),                 // 通过索道
    ASSI_ACTION_PASS_SKY_CHANNEL(98),              // 通过空中通道
    ASSI_ACTION_PASS_CHANNEL(99),                  // 通过建筑物穿越通道
    ASSI_ACTION_PASS_WALKROAD(100),                // 通过行人道路
    ASSI_ACTION_PASS_BOAT_LINE(101),               // 通过游船路线
    ASSI_ACTION_PASS_SIGHT_SEEING_LINE(102),       // 通过观光车路线
    ASSI_ACTION_PASS_SKIDWAY(103),                 // 通过滑道
    ASSI_ACTION_PASS_LADDER(105),                  // 通过阶梯
    ASSI_ACTION_PASS_SLOPE(106),                   // 通过斜坡
    ASSI_ACTION_PASS_BRIDGE(107),                  // 通过桥
    ASSI_ACTION_PASS_FERRY(108),                   // 通过轮渡
    ASSI_ACTION_PASS_SUBWAY(109),                  // 通过地铁通道
    ASSI_ACTION_SOON_ENTER_Building(112),          // 即将进入建筑(当前未下发)
    ASSI_ACTION_SOON_LEAVE_Building(113),          // 即将离开建筑(当前未下发)
    ASSI_ACTION_ENTER_ROUNDABOUT(114),             // 进入环岛(骑步特有)
    ASSI_ACTION_LEAVE_ROUNDABOUT(115),             // 离开环岛(骑步特有)
    ASSI_ACTION_ENTER_PATH(116),                   // 进入小路
    ASSI_ACTION_ENTER_INNER(117),                  // 进入内部路
    ASSI_ACTION_ENTER_LEFT_BRANCH_TWO(118),        // 进入左侧第二岔路
    ASSI_ACTION_ENTER_LEFT_BRANCH_THREE(119),      // 进入左侧第三岔路
    ASSI_ACTION_ENTER_RIGHT_BRANCH_TWO(120),       // 进入右侧第二岔路
    ASSI_ACTION_ENTER_RIGHT_BRANCH_THREE(121),     // 进入右侧第三岔路
    ASSI_ACTION_ENTER_GAS_STATION(122),            // 进入加油站道路
    ASSI_ACTION_ENTER_HOUSING_ESTATE(123),         // 进入小区道路
    ASSI_ACTION_ENTER_PARK_ROAD(124),              // 进入园区道路
    ASSI_ACTION_ENTER_OVERHEAD(125),               // 上高架
    ASSI_ACTION_ENTER_CENTER_BRANCH_OVERHEAD(126), // 走中间岔路上高架
    ASSI_ACTION_ENTER_RIGHT_BRANCH_OVERHEAD(127),  // 走最右侧岔路上高架
    ASSI_ACTION_ENTER_LEFT_BRANCH_OVERHEAD(128),   // 走最左侧岔路上高架
    ASSI_ACTION_ALONG_STRAIGHT(129),               // 沿当前道路直行
    ASSI_ACTION_DOWN_OVERHEAD(130),                // 下高架
    ASSI_ACTION_ENTER_LEFT_OVERHEAD(131),          // 走左侧道路上高架
    ASSI_ACTION_ENTER_RIGHT_OVERHEAD(132),         // 走右侧道路上高架
    ASSI_ACTION_UP_TO_BRIDGE(133),                 // 上桥
    ASSI_ACTION_ENTER_PARKING(134),                // 进停车场
    ASSI_ACTION_ENTER_OVERPASS(135),               // 进入立交桥
    ASSI_ACTION_ENTER_BRIDGE(136),                 // 进入桥梁
    ASSI_ACTION_ENTER_UNDERPASS(137);              // 进入地下通道


    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

/**
 * 高德公版API文档网址
 * https://a.amap.com/lbs/static/unzip/Android_Navi_Doc/com/amap/api/navi/enums/RoadClass.html
 */
enum class RoadClass(val id: Int) {
    ROAD_CLASS_NULL(-1),               // 无效值
    ROAD_CLASS_HIGH_WAY(0),            // 高速公路
    ROAD_CLASS_NATIONAL_WAY(1),        // 国道
    ROAD_CLASS_PROVINCIAL_WAY(2),      // 省道
    ROAD_CLASS_COUNTRY_WAY(3),         // 县道
    ROAD_CLASS_TOWN_WAY(4),            // 乡公路
    ROAD_CLASS_COUNTY_AND_TOWN_WAY(5), // 县乡村内部道路
    ROAD_CLASS_EXPRESS_WAY(6),         // 主要大街、城市快速道
    ROAD_CLASS_MAIN_WAY(7),            // 主要道路
    ROAD_CLASS_MINOR_WAY(8),           // 次要道路
    ROAD_CLASS_COMMON_WAY(9),          // 普通道路
    ROAD_CLASS_NON_NAVI_WAY(10);       // 非导航道路

    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

enum class Status(val id: Int) {
    STATUS_NONE(0),         // 未导航
    STATUS_GPS(1),          // GPS导航
    STATUS_SIMULATION(2),   // 模拟导航
    STATUS_REPLANNING(3),   // 重新规划
    STATUS_CRUISING(4),     // 巡航
    STATUS_DEVIATED(5),     // 偏航
    STATUS_PLANNING(6);     // 规划中，还未点确认开始导航


    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

enum class ParallelRoadStatus(val id: Int) {
    PARALLEL_ROAD_NONE(0),        //不存在平行路
    PARALLEL_ROAD_SHOW(1);        // 存在平行路

    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

/**
 * 高德公版API文档网址
 * https://a.amap.com/lbs/static/unzip/Android_Navi_Doc/com/amap/api/navi/enums/LaneAction.html
 */
enum class LaneType(val id: Int) {
    LaneActionNULL(0xFF),//< 无对应车道 */
    LaneActionAhead(0),//< 直行 */
    LaneActionLeft(1),//< 左转 */
    LaneActionAheadLeft(2),// < 直行+左转 */
    LaneActionRight(3),// < 右转 */
    LaneActionAheadRight(4),//< 直行+右转 */
    LaneActionLUTurn(5), //< 左掉头 */
    LaneActionLeftRight(6),//< 左转+右转 */
    LaneActionAheadLeftRight(7),//< 直行+左转+右转 */
    LaneActionRUTurn(8),//< 右掉头 */
    LaneActionAheadLUTurn(9),//< 直行+左掉头 */
    LaneActionAheadRUTurn(10),//< 直行+右掉头 */
    LaneActionLeftLUTurn(11),//< 左转+左掉头 */
    LaneActionRightRUTurn(12),//< 右转+右掉头 */
    LaneActionLeftInAhead(13),//< 无效，保留 */
    LaneActionLeftLUturn(14),//< 无效，保留 */
    LaneActionReserved(15),//< 保留 */
    LaneActionAheadLeftLUTurn(16),//< 直行+左转+左掉头 */
    LaneActionRightLUTurn(17),//< 右转+左掉头 */
    LaneActionLeftRightLUTurn(18),//< 左转+右转+左掉头 */
    LaneActionAheadRightLUTurn(19),//< 直行+右转+左掉头 */
    LaneActionLeftRUTurn(20),//< 左转+右掉头 */
    LaneActionBus(21),//< 公交车道 */
    LaneActionEmpty(22), //< 空车道 */
    LaneActionVariable(23),//< 可变车道 */
    LaneActionDedicated(24),//< 专用车道 */
    LaneActionTidal(25);//< 潮汐车道 */

    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}

enum class FacilityType(val id: Int) {
    FACILITY_TYPE_NULL(0),                     // 未知类型
    FACILITY_TYPE_LEFT_INTER_FLOW(1),          // 左侧合流,左侧车辆交汇处
    FACILITY_TYPE_RIGHT_INTER_FLOW(2),         // 右侧合流,右侧车辆交汇处
    FACILITY_TYPE_SHARP_TURN(3),               // 急转弯
    FACILITY_TYPE_REVERSE_TURN(4),             // 反向转弯,反向弯路
    FACILITY_TYPE_LINKING_TURN(5),             // 连续转弯
    FACILITY_TYPE_ACCIDENT_AREA(6),            // 事故多发地,事故易发地段
    FACILITY_TYPE_FALLING_ROCKS(7),            // 注意落石
    FACILITY_TYPE_RAILWAY_CROSS(8),            // 铁路道口
    FACILITY_TYPE_SLIPPERY(9),                 // 易滑,路段易滑
    FACILITY_TYPE_MAX_SPEED_LIMIT(10),         // 最大限速标志
    FACILITY_TYPE_MIN_SPEED_LIMIT(11),         // 最小限速标志
    FACILITY_TYPE_VILLAGE(12),                 // 村庄
    FACILITY_TYPE_LEFT_NARROW(13),             // 左侧变窄
    FACILITY_TYPE_RIGHT_NARROW(14),            // 右侧变窄
    FACILITY_TYPE_DOUBLE_NARROW(15),           // 两侧变窄,道路两侧变窄
    FACILITY_TYPE_CROSS_WIND_AREA(16),         // 横风区
    FACILITY_TYPE_SCHOOL_ZONE(17),             // 前方学校
    FACILITY_TYPE_OVERTAKE_FORBID(18),         // 禁止超车
    FACILITY_TYPE_NARROW_BRIDGE(19),           // 窄桥
    FACILITY_TYPE_DOUBLE_DETOUR(20),           // 左右绕行
    FACILITY_TYPE_LEFT_DETOUR(21),             // 左侧绕行
    FACILITY_TYPE_RIGHT_DETOUR(22),            // 右侧绕行
    FACILITY_TYPE_LEFT_DANGEROUS(23),          // 左侧傍山险路,左侧靠山险路
    FACILITY_TYPE_RIGHT_DANGEROUS(24),         // 右侧傍山险路,右侧靠山险路
    FACILITY_TYPE_UPPER_STEEP(25),             // 上陡坡
    FACILITY_TYPE_DOWN_STEEP(26),              // 下陡坡
    FACILITY_TYPE_WATER_PAVEMENT(27),          // 过水路面
    FACILITY_TYPE_IRREGULARITY_PAVEMENT(28),   // 路面不平
    FACILITY_TYPE_AMBLE(29),                   // 慢行
    FACILITY_TYPE_ATTENTION_DANGER(30),        // 注意危险
    FACILITY_TYPE_ZEBRA_CROSSING(31),          // 人行横道
    FACILITY_TYPE_LEFT_SHARP_TURN(46),         // 左急转弯,向左急弯路
    FACILITY_TYPE_RIGHT_SHARP_TURN(47),        // 右急转弯,向右急弯路
    FACILITY_TYPE_LEFT_FALLING_ROCKS(48),      // 注意左侧落石
    FACILITY_TYPE_RIGHT_FALLING_ROCKS(49),     // 注意右侧落石
    FACILITY_TYPE_RAILWAY_CROSS_EXTEND(50),    // 铁道路口
    FACILITY_TYPE_RAILWAY_WITH_GATES(51),      // 有人看管的铁道路口,有人看管的铁路道口
    FACILITY_TYPE_RAILWAY_WITHOUT_GATES(52),   // 无人看管的铁道路口,无人看管的铁路道口
    FACILITY_TYPE_VARIABLE_SPEED(53),          // 可变限速标志, v3.2 add
    FACILITY_TYPE_OVER_PASS(58),               // 天桥,  v3.2 add
    FACILITY_TYPE_TRUCK_HEIGHT_LIMIT(81),      // 货车限高
    FACILITY_TYPE_TRUCK_WIDTH_LIMIT(82),       // 货车限宽
    FACILITY_TYPE_TRUCK_WEIGHT_LIMIT(83),      // 货车限重
    FACILITY_TYPE_SERVICE_AREA(89),            // 服务区
    FACILITY_TYPE_TOLL_GATE(90),               // 收费站
    FACILITY_TYPE_CHECKPOINT(91),              // 检查站
    FACILITY_TYPE_BUS_LANE(92),                // 公交车道设施
    FACILITY_TYPE_WINTER_OLYMPICS_LANE(128);   // 冬奥专用设施


    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}


enum class CameraType(val id: Int) {
    CAMERA_TYPE_NULL(0),                            // 无效值
    CAMERA_TYPE_ILLEGAL_USE_LIGHT(1),               // 违规用灯
    CAMERA_TYPE_ILLEGAL_USE_SAFETY_BELT(2),         // 不系安全带
    CAMERA_TYPE_DO_NOT_FOLLOW_LANE(3),              // 违规占车道
    CAMERA_TYPE_ILLEGAL_PASS_CROSS(4),              // 违规过路口
    CAMERA_TYPE_DIAL_PHONE_WHEN_DRIVING(5),         // 开车打手机
    CAMERA_TYPE_LANE_LIMIT_SPEED(6),                // 分车道限速
    CAMERA_TYPE_ULTRA_HIGH_SPEED(7),                // 超高速
    CAMERA_TYPE_VERY_LOW_SPEED(8),                  // 超低速
    CAMERA_TYPE_VARIABLE_SPEED(9),                  // 可变限速
    CAMERA_TYPE_TRAFFIC_LIGHT(10),                  // 闯红灯,闯红灯拍照
    CAMERA_TYPE_END_NUMBER_LIMIT(11),               // 尾号限行
    CAMERA_TYPE_ENVIRONMENTAL_LIMIT(12),            // 环保限行
    CAMERA_TYPE_BREACH_PROHIBITION_SIGN(13),        // 违反禁令标志
    CAMERA_TYPE_VIOLATE_PROHIBITED_MARKINGS(14),    // 违反禁止标线
    CAMERA_TYPE_COURTESY_CROSSING(15),              // 礼让行人
    CAMERA_TYPE_REVERSE_DRIVING(16),                // 逆向行驶
    CAMERA_TYPE_ILLEGAL_PARKING(17),                // 违章停车
    CAMERA_TYPE_BICYCLE_LANE(18),                   // 占用非机动车道，非机动车道拍照
    CAMERA_TYPE_BUSWAY(19),                         // 占用公交专用车道,公交专用道拍照
    CAMERA_TYPE_EMERGENCY_LANE(20),                 // 占用应急车道，应急车道拍照
    CAMERA_TYPE_HONK(21),                           // 禁止鸣笛
    CAMERA_TYPE_FLOW_SPEED(22),                     // 流动测速
    CAMERA_TYPE_COURTESY_CAR_CROSSING(23),          // 路口不让行-让车
    CAMERA_TYPE_RAILWAY_CROSSING(24),               // 违规过铁路道口
    CAMERA_TYPE_INTERVAL_VELOCITY_START(25),        // 区间测速起点
    CAMERA_TYPE_INTERVAL_VELOCITY_END(26),          // 区间测速终点
    CAMERA_TYPE_INTERVAL_VELOCITY_START_END(27),    // 区间测速起终点,测速摄像
    CAMERA_TYPE_CAR_SPACING(28),                    // 车间距抓拍
    CAMERA_TYPE_HOV_LANE(29),                       // HOV车道
    CAMERA_TYPE_OCCUPIED_LINE(30),                  // 压线抓拍
    CAMERA_TYPE_ETC(99),                            // ETC拍照
    CAMERA_TYPE_BREAK_RULE(100),                    // 无细类的违章,违章拍照，违章高发地
    CAMERA_TYPE_SURVEILLANCE(101);                  // 视频监控,监控摄像


    companion object {
        fun get(id: Int) = values().first { it.id == id }
    }
}