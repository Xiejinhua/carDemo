package com.desaysv.psmap.model.impl

/**
 * 需要自定义实现不同搜索使用
 */
interface ICustomSearchCommand {
    /**
     * 关键字搜索
     */
    fun keywordSearch(keyword: String)

    /**
     * 关键字搜索带经纬度
     */
    fun keywordSearch(keyword: String, lon: Double, lat: Double)

    /**
     * 周边搜
     */
    fun aroundSearch(keyword: String)

    /**
     * 沿途搜索,需要在路线规划/导航中
     * 搜索沿途的加油站、4S店、服务区
     */
    fun alongRouteSearch(name: String)

    /**
     * 周边搜带经纬度
     */
    fun aroundSearch(keyword: String, lon: Double, lat: Double)

    /**
     * 逆地理搜索
     */
    fun nearestSearch(lon: Double, lat: Double)

    /**
     *在某个地点位置附近进行搜索
     *例如：成都孵化园附近的咖啡厅  咖啡厅放在keyword  成都孵化园放在locationNamen
     */
    fun aroundSearchByLocationName(keyword: String, locationName: String)

    /**
     * 请求家/公司地址
     */
    fun requestHomeOrWorkAddress(type: String)

    /**
     * 查找自车位置
     */
    fun whereAmI()

}