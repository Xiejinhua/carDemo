package com.desaysv.psmap.base.utils

import com.autonavi.gbl.common.path.model.AlongWayProbeConfig
import com.autonavi.gbl.common.path.model.ProbeNaviInfo
import com.autonavi.gbl.common.path.option.PathInfo
import com.autonavi.gbl.search.model.NearestPoi
import com.autonavi.gbl.search.model.NearestRoad
import com.autonavi.gbl.search.model.SearchRoadId
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI

/**
 * @author 张楠
 * @time 2024/2/26
 * @description 搜索相关工具类
 */
object SearchCommonUtils {

    //设置搜索类型或者获取沿途搜索分类
    //目前AutoSDK沿途搜SearchAlongWayParam.keyword支持加油站、中石化、中石油、壳牌、美孚、ATM(不区分大小写)、维修站、厕所、充电站、加气站、美食、停车场、服务区的沿途POI搜索。
    // 其他类型可以通过category字段传入对应的typecode来实现。
    //离线沿途仅支持加油站、维修站、加气站、厕所、充电站等关键字，不支持category。
    fun getAlongName(name: String): String {
        return when (name) {
            "餐饮" -> "美食"
            "洗手间", "卫生间" -> "厕所"
            "修车店", "汽修站" -> "维修站"
            "充电桩" -> "充电站"
            else -> name
        }
    }

    /**
     * 获取路线上坐标点(抽稀点)
     *
     * @param drivePathAccessor
     * @param startPoi
     * @return
     */
    fun getAlongwaySearchGeoline(drivePathAccessor: PathInfo, startPoi: POI): List<GeoPoint> {
        val geolinePointList: MutableList<GeoPoint> = ArrayList()
        //沿途搜起点segmentIndex和linkindex默认从0开始，但如果是从中途搜，
        // 导航场景下，可通过OnUpdateNaviInfo回调接口回调的NaviInfo中的curSegIdx和curLinkIdx来传入
        val config = AlongWayProbeConfig()
        val info = ProbeNaviInfo()
        info.isNavi = false
        val probeResult = drivePathAccessor.buildRarefyPoint(config, info) // 获取抽稀形状点
        val pointList = probeResult.geolinePoints
        if (pointList != null) {
            for (i in pointList.indices) {
                val geoPoint = GeoPoint(pointList[i].lon, pointList[i].lat)
                geolinePointList.add(geoPoint)
            }
        }
        return geolinePointList
    }

    /**
     * 离线指定引导路径道路 ，离线搜索条件，离线搜索必填
     *
     * @param pathInfo
     * @return
     */
    fun getAlongwaySearchGuideRoads(pathInfo: PathInfo): java.util.ArrayList<SearchRoadId>? {
        val segmentCount = pathInfo.segmentCount
        val guideRoads = java.util.ArrayList<SearchRoadId>()
        for (i in 0 until segmentCount) {
            val segmentInfo = pathInfo.getSegmentInfo(i)
            if (segmentInfo != null) {
                val linkCount = segmentInfo.linkCount
                for (j in 0 until linkCount) {
                    val linkInfo = segmentInfo.getLinkInfo(j)
                    if (linkInfo != null) {
                        val roadId = SearchRoadId()
                        roadId.roadId = linkInfo.tpid.toInt().toLong()
                        roadId.tileId = linkInfo.tileID.toInt().toLong()
                        roadId.urId = linkInfo.urid.toLong()
                        guideRoads.add(roadId)
                    }
                }
            }
        }
        return guideRoads
    }

    //长按地图选点，返回poi按照距离排序
    fun invertOrderList(list: java.util.ArrayList<NearestPoi>?): java.util.ArrayList<NearestPoi>? {
        if (list != null && list.size > 1) {
            //做一个冒泡排序，大的在数组的前列
            var one: Int
            var two: Int
            var tempDate: NearestPoi
            for (i in 0 until list.size - 1) {
                for (j in i + 1 until list.size) {
                    one = list[i].distance
                    two = list[j].distance
                    if (one > two) {
                        tempDate = list[i]
                        list[i] = list[j]
                        list[j] = tempDate
                    }
                }
            }
        }
        return list
    }

    fun invertOrderNearestRoadList(list: java.util.ArrayList<NearestRoad>?): java.util.ArrayList<NearestRoad>? {
        if (list != null && list.size > 1) {
            //做一个冒泡排序，大的在数组的前列
            var one: Int
            var two: Int
            var tempDate: NearestRoad
            for (i in 0 until list.size - 1) {
                for (j in i + 1 until list.size) {
                    one = list[i].distance
                    two = list[j].distance
                    if (one > two) {
                        tempDate = list[i]
                        list[i] = list[j]
                        list[j] = tempDate
                    }
                }
            }
        }
        return list
    }
}