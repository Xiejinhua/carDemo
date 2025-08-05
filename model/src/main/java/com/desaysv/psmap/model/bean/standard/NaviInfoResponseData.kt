package com.desaysv.psmap.model.bean.standard

/**
 * @author 谢锦华
 * @time 2025/3/14
 * @description 语音指令-引导信息主动透出 （30407） 应答类
 */


data class NaviInfoResponseData(
//    /**
//     * 为避免后续项⽬新增导航转向动作图标，导致以前项⽬未对接新增的转向动作
//     * 图标⽽引起仪表上显示的动作和主屏动作不⼀致的情况，新增addicon参数，
//     * 该参数在透出新增转向动作的同时按照以下规则透视原有逻辑下的对应动作，
//     * 例如，透出新增途径点动作70时，同时透出原有途径点动作10 多个图标 时使
//     * ⽤英⽂逗号分隔 550及以上 版本⽀持
//     */
    var addIcon: String = "",
//    /**
//     * 电⼦眼类型 电⼦眼类型，对应的值为int类型 *
//     * 0 测速摄像头
//     * 1为监控摄像头
//     * 2为闯红灯拍照 *
//     * 3为违章拍照 *
//     * 4为公交专⽤道摄像头 *
//     * 5为应急⻋道摄像头 *
//     * 6为⾮机动⻋道拍照 *
//     * 7为区间测速路段【（暂未使⽤）ps：3.x版本后新增】 *
//     * 8为区间测速起始 （ps：3.x版本后新增）（4.x版本不⽀持） *
//     * 9为区间测速结束 （ps：3.x版本后新增）（4.x版本不⽀持） *
//     * 10为流动测速电⼦眼ps：3.x版本后新增） *
//     * 11ETC电⼦眼 *
//     * 12压线 *
//     * 13礼让⾏⼈ *
//     * 14违规占⻋道（仅下发，前端不展示） *
//     * 15闯红灯细类 *
//     * 16公交⻋道细类 *
//     * 17应急⻋道细类 *
//     * 18系安全带 *
//     * 19拨打电话 *
//     * 20⾮机动⻋道 *
//     * 21违法停⻋ *
//     * 22违规⽤灯仅下发，前端不展示） *
//     * 23违规过路⼝（仅下发，前端不展示） *
//     * 24禁⽌鸣笛 *
//     * 25逆向⾏驶 *
//     * 26违规过铁道路⼝ *
//     * 27尾号限⾏ *
//     * 28⻋间距抓拍 *
//     * 29HOV⻋道 *
//     * 30环保限⾏ 备注：0~11为电⼦眼⼤类类型，12~24为违章电⼦眼的细类类型。11-24类型仅510及以上⽀持。25-30类型仅6.0及以上⽀持。
//     */
    var cameraType: Int = -1,
    var cameraDist: Int = 0,//距离最近的电⼦眼距离，对应的值为int类型，单位：⽶
    var cameraID: Int = -1,//电⼦眼唯⼀标识（510及以上版本⽀持）
    var cameraIndex: Int = -1,//下⼀个将要路过的电⼦眼编号，若为-1则对应的道路上没有电⼦眼
    var cameraPenalty: Boolean = false,//是否⾼罚电⼦眼（510及以上⽀持） true：是 false：否
    var cameraSpeed: Int = 0,//电⼦眼限速度，对应的值为int类型，⽆限速则为0，单位：公⾥/⼩时
    var newCamera: Boolean = false,//是否近期新增电⼦眼（520及以上⽀持） true：是 false：否
    var arrivePOILatitude: Double = 0.0,//终点POI到达点纬度( 5 2 0 及 以 上 ⽀ 持 ）
    var arrivePOILongitude: Double = 0.0,//终点POI到达点经度(520及以上⽀持）
    var arrivePOIType: String = "",//终点POI到达点类型(520及以上⽀持）
    var carDirection: Int = -1,//⾃⻋⽅向，对应的值为int类型，单位：度，以正北为基准，顺时针增加
    var carLatitude: Double = 0.0,//⾃⻋纬度，对应的值为double类型 （3.x版本不⽀持）
    var carLongitude: Double = 0.0,//⾃⻋经度，对应的值为double类型（3.x版本不⽀持）
    var curPointNum: Int = 0,//当前位置的前⼀个形状点号，对应的值为int类型，从0开始
    var curRoadName: String = "",//当前道路名称，对应的值为String类型
    var curSegNum: Int = 0,//当前⾃⻋所在Link，对应的值为int类型，从0开始
    var curSpeed: Int = 0,//当前⻋速，对应的值为int类型，单位：公⾥/⼩时
    var currentRoadTotalDis: Int = 0,//当前⻓度
    var endPOIAddr: String = "",//终点POI地址(520及以上⽀持）
    var endPOICityName: String = "",//终点所在城市名称（520及以上⽀持）
    var endPOIDistrictName: String = "",//终点所在区县名称（520及以上⽀持）
    var endPOILatitude: Double = 0.0,//终点POI纬度(520及以上⽀持）
    var endPOILongitude: Double = 0.0,//终点POI经度(520及以上⽀持）
    var endPOIName: String = "",//终点POI名称(520及以上⽀持）
    var endPOIType: String = "",//终点POI类型(520及以上⽀持）
    var etaText: String = "",//预计到达时间
    var exitDirectionInfo: String = "",//出⼝⽅向信息
    var exitNameInfo: String = "",//出⼝编号
    var icon: Int = 0,//导航转向图标
    var limitedSpeed: Int = 0,//当前道路速度限制，对应的值为int类型，单位：公⾥/⼩时
    var newIcon: Int = 0,//新的导航转向图标
    var nextNextAddIcon: String = "",//使⽤⽅法同addIcon 550及以上 版本⽀持
    var nextNextRoadName: String = "",//下下个路名名称
    var nextNextTurnIcon: Int = 0,//下下个路⼝转向图标
    var nextRoadNOAOrNot: Boolean = false,//下⼀道路是否包含NOA路段（460及以上⽀持）
    var nextRoadName: String = "",//下⼀道路名
    var nextRoadProgressPrecent: Int = 0,//导航到达下⼀路⼝进度条，整形 0～100
    var nextSapaDist: Int = 0,//距离前⽅第⼆个服务区的距离，对应的值为int类型，单位：⽶
    var nextSapaDistAuto: String = "",//转换后距离前⽅第⼆个服务区的距离，对应的值为String类型，由距离和单位组成
    var nextSapaName: String = "",//前⽅第⼆个服务区名称
    var nextSapaType: Int = 0,//前⽅第⼆个服务区类型
    var nextSegRemainDis: Int = 0,//距离下下个路⼝剩余距离,对应的值为int类型，单位：⽶
    var nextSegRemainDisAuto: String = "",//转换后下下个路⼝剩余距离,（带单位）（仅sdk项⽬⽀持）
    var nextSegRemainTime: Int = 0,//距离下下个路⼝剩余时间，对应的值为int类型，单位：秒
    var roadType: Int = 0,//当前道路类型
    var roundAboutNum: Int = 0,//环岛出⼝序号，对应的值为int类型，从0开始，只有在icon为11和12时有效，其余为⽆效值0
    var roundAllNum: Int = 0,//环岛出⼝个数，对应的值为int类型，只有在icon为11和12时有效，其余为⽆效值0
    var roundaboutOutAngle: Int = 0,//环岛出⼝度数，只有在icon类型为环岛的时候有效
    var routeAllDis: Int = 0,//路径总距离，对应的值为int类型，单位：⽶
    var routeAllTime: Int = 0,//路径总时间，对应的值为int类型，单位：秒
    var routeRemainDis: Int = 0,//路径剩余距离，对应的值为int类型，单位：⽶
    var routeRemainDistanceAuto: String = "",//转换后的路径剩余距离（带单位）
    var routeRemainTime: Int = 0,//路径剩余时间，对应的值为int类型，单位：秒
    var routeRemainTimeAuto: String = "",//转换后的路径剩余时间（带单位）
    var routeRemainTrafficLightNum: Int = 0,//路径剩余红绿灯个数，对应的值为int类型（610及以上⽀持）
    var sapaDist: Int = 0,//距离最近服务区的距离，对应的值为int类型，单位：⽶
    var sapaDistAuto: String = "",//转换后距离最近服务区的距离，对应的值为String类型，由距离和单位组成
    var sapaName: String = "",//距离最近的服务区名称
    var sapaNum: Int = 0,//服务区个数
    var sapaType: Int = 0,//距离最近的服务区类型： 0：⾼速服务区 1：其他服务设施（收费站、停⻋区等）
    var segAssistantAction: Int = 0,//当前导航段的辅助动作
    var segRemainDis: Int = 0,//当前导航段剩余距离，对应的值为int类型，单位：⽶
    var segRemainDisAuto: String = "",//转换后当前导航段剩余距离，对应的值为String类型，由距离和单位组成
    var segRemainTime: Int = 0,//当前导航段剩余时间，对应的值为int类型，单位：秒
    var trafficLightNum: Int = 0,//红绿灯个数，对应的值为int类型
    var type: Int = -1,//导航类型 当导航类型参数type值为0（真实导航），1（模拟导航），2（巡航）时，此参数取值有效；当type参数值为-1时，此参数取值⽆效
    var viaPOIArrivalTime: String = "",//到达最近⼀个途经点的时间 (530及以上⽀持）⽐如11点20分到达
    var viaPOIdistance: Int = 0,//到达最近⼀个途经点的距离，单位：米 (520及以上⽀持）
    var viaPOItime: Int = 0//到达最近⼀个途经点的时间，单位：秒 (520及以上⽀持）
) : ResponseDataData()

