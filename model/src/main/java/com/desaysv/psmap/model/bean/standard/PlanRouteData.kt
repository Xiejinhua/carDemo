package com.desaysv.psmap.model.bean.standard

/**
 * @author 谢锦华
 * @time 2025/3/13
 * @description 语音指令-发起路线规划 （30402）
 */


data class PlanRouteData(
    //规划类型
    // 0：进⼊路线规划结果（如果auto不在前台，则先切换auto到前台，再进⼊路线规划结果⻚）
    // 1：传⼊终点直接开始导航
    // 2：不跳转⻚⾯，只透出信息（静默算路，不影响当前路线）
    // 3：传⼊起点、终点进⾏路线规划；
    // 4：单次模拟导航，传起点、中途点、终点与偏好设置后可直接模拟导航；（model协议不⽀持）
    // 5：巡航模拟导航，传起点、中途点、终点与偏好设置后可直接模拟导航（model协议不⽀持）
    // 6：进⼊路线规划⻚（前后台状态保持不变）仅⽀持sdk项⽬，520及以上版本⽀持
    val actionType: Int,

    val dev: Int,//坐标转换0：已经是⾼德坐标，不需转换 1：⾮⾼德坐标，需要转换（即将 WGS84坐标转成GCJ02坐标）

    //路线偏好（480以下版本）： -1:按地图内部设置的算路偏好 1:避免收费 2:多策略，不勾选任何偏好
    // 3:不⾛⾼速 4:躲避拥堵 5:避免收费+不⾛⾼速 6:躲避拥堵+不⾛⾼速 7:躲避拥堵+避免收费 8:躲避拥堵+避免收费+不⾛⾼速 20:⾼速优先 24:躲避拥堵+⾼速优先
    val strategy: Int = -1,

    //路线偏好:（480及以上版本）： 9:⼤路优先 10:速度最快 11:少收费 12:⾼德推荐
    // 13:不⾛⾼速 14:躲避拥堵 15:少收费+不⾛⾼速 16:躲避拥堵+不⾛⾼速 17:躲避拥堵+少收费 18:躲避拥堵+少收费+不⾛⾼速
    // 34:⾼速优先 39:躲避拥堵+⾼速优先 44:躲避拥堵+⼤路优先 45:躲避拥堵+速度最快
    val newStrategy: Int = -100,

    val startProtocolPoi: StartProtocolPoi?,//起点poi对象
    val endProtocolPoi: EndProtocolPoi?,//终点poi对象
    val midProtocolPois: List<MidProtocolPoi>?,//途经点poi对象
) : ResponseDataData()

data class EndProtocolPoi(
    val address: String,//poi地址
    val entryLatitude: Double,//到达点纬度 经纬度⼩数点后不得超过6位
    val entryLongitude: Double,//到达点经度 经纬度⼩数点后不得超过6位
    val latitude: Double,//Poi纬度 经纬度⼩数点后不得超过6位
    val longitude: Double,//poi经度 经纬度⼩数点后不得超过6位
    val midtype: Int,//途经点类型（417及以上版本⽀持） 0：普通点 1：家 2：公司
    val nTypeCode: String,//poi类型 搜索结果会透出
    val poiId: String,//poi唯⼀ID
    val poiName: String//poi名称（没有的话不要填，不能写死）
)

data class MidProtocolPoi(
    val address: String,//poi地址
    val entryLatitude: Double,//到达点纬度 经纬度⼩数点后不得超过6位
    val entryLongitude: Double,//到达点经度 经纬度⼩数点后不得超过6位
    val latitude: Double,//Poi纬度 经纬度⼩数点后不得超过6位
    val longitude: Double,//poi经度 经纬度⼩数点后不得超过6位
    val midtype: Int,//途经点类型（417及以上版本⽀持） 0：普通点 1：家 2：公司
    val nTypeCode: String,//poi类型 搜索结果会透出
    val poiId: String,//poi唯⼀ID
    val poiName: String//poi名称（没有的话不要填，不能写死）
)

data class StartProtocolPoi(
    val address: String,//poi地址
    val entryLatitude: Double,//到达点纬度 经纬度⼩数点后不得超过6位
    val entryLongitude: Double,//到达点经度 经纬度⼩数点后不得超过6位
    val latitude: Double,//Poi纬度 经纬度⼩数点后不得超过6位
    val longitude: Double,//poi经度 经纬度⼩数点后不得超过6位
    val midtype: Int,//途经点类型（417及以上版本⽀持） 0：普通点 1：家 2：公司
    val nTypeCode: String,//poi类型 搜索结果会透出
    val poiId: String,//poi唯⼀ID
    val poiName: String//poi名称（没有的话不要填，不能写死）
)