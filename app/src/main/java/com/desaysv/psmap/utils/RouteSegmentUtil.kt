package com.desaysv.psmap.utils

import android.text.TextUtils
import com.autonavi.gbl.common.model.Coord2DInt32
import com.autonavi.gbl.common.path.model.AssistantAction
import com.autonavi.gbl.common.path.model.Formway
import com.autonavi.gbl.common.path.option.PathInfo
import com.autonavi.gbl.common.path.option.SegmentInfo
import com.autosdk.bussiness.navi.route.model.RouteCarResultData
import com.autosdk.bussiness.widget.route.model.NaviStationItemData
import com.autosdk.bussiness.widget.route.model.NaviStationItemData.SubItem
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.common.utils.CommonUtil
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R

/**
 * @author 谢锦华
 * @time 2024/10/16
 * @description
 */

object RouteSegmentUtil {
    const val TYPE_START: Int = 0 // 起点
    const val TYPE_GROUP: Int = 1 // 聚合路线
    const val TYPE_NORMAL: Int = 2 // 非聚合普通路线
    const val TYPE_END: Int = 3 // 终点

    fun getPathNaviStationList(pathInfo: PathInfo?, carPathRelult: RouteCarResultData): ArrayList<NaviStationItemData>? {
        val list = ArrayList<NaviStationItemData>()

        var previousRoadName: String? = null
        val onFootDistance: String? = null
        // String nextRoadName = null;
        if (pathInfo == null) {
            return null
        }
        val groupSegmentCount = pathInfo.groupSegmentCount
        val segmentCount = pathInfo.segmentCount
        var midPoiIndex = 0
        //起点
        val startItem = NaviStationItemData()
        startItem.desType = TYPE_START
        startItem.roadName = carPathRelult.fromPOI.name
        startItem.distanceDes = ""
        startItem.index = 0
        startItem.actionIcon = R.drawable.route_browser_fragment_icon_start
        startItem.navigtionAction = RouteActionUtil.DirICon_TYPE_START
        list.add(startItem)

        //聚合路段
        for (i in 0 until groupSegmentCount) {
            val groupSegment = pathInfo.getGroupSegment(i.toLong()) ?: return null
            val item = NaviStationItemData()
            item.desType = TYPE_GROUP
            item.index = (i + 1).toInt()
            if (TextUtils.isEmpty(groupSegment.roadName)) {
                item.roadName = "内部道路"
            } else {
                item.roadName = groupSegment.roadName
            }
            item.distanceDes = CommonUtil.distanceUnitTransform(groupSegment.length)
            item.groupDes = item.distanceDes
            //找出当前聚合路段下的路段
            val subList: MutableList<SubItem> = ArrayList()
            val startIndex = groupSegment.startSegmentIndex
            val endIndex = startIndex + groupSegment.segmentCount
            var groupTrafficLightNum = 0
            for (j in startIndex until endIndex) {
                val segmentInfo = pathInfo.getSegmentInfo(j.toLong()) ?: return null
                val subItem = SubItem()
                subItem.stationIndex = j
                val navigtionAction: Int = RouteActionUtil.getMainAction(segmentInfo.mainAction, segmentInfo.assistantAction)
                val actionIcon: Int = RouteActionUtil.getRouteGroupNaviActionIcon(navigtionAction, true)
                subItem.actionIcon = actionIcon
                subItem.groupActionIcon = actionIcon
                subItem.navigtionAction = navigtionAction
                subItem.actionDes = RouteActionUtil.getNaviActionStr(navigtionAction)
                groupTrafficLightNum += segmentInfo.trafficLightNum.toInt()
                subItem.distanceDes = AutoRouteUtil.routeResultDistance(segmentInfo.length)

                val sb = StringBuffer()
                sb.append(ResUtil.getString(R.string.sv_route_car_result_share_driving))
                    .append(subItem.distanceDes)
                    .append(subItem.actionDes)
                var routeLinkRoadName: String? = ""
                if (j + 1 < segmentCount) {
                    val segmentInfoNext = pathInfo.getSegmentInfo((j + 1).toLong())
                    if (segmentInfoNext != null) {
                        routeLinkRoadName = getNoCrossLinkRoadName(segmentInfoNext)
                    }
                } else {
                    routeLinkRoadName = ResUtil.getString(R.string.sv_route_car_result_share_to_end_poi)
                }

                val routeLinks = getLinks(segmentInfo)
                subItem.routeLinks = routeLinks
                subItem.routelinkPoints = getLinkPoint(segmentInfo)

                if (j < segmentCount) {
                    previousRoadName = toDBC(routeLinkRoadName)
                    if (TextUtils.isEmpty(previousRoadName)) {
                        if (navigtionAction != RouteActionUtil.DirIcon_Entry_Ring && navigtionAction != RouteActionUtil.DirIcon_Leave_Ring) {
                            previousRoadName = ResUtil.getString(R.string.sv_route_route_foot_navi_no_name_road)
                        }
                    }
                    if (!TextUtils.isEmpty(previousRoadName)) {
                        sb.append(ResUtil.getString(R.string.sv_route_car_result_share_enter))
                        sb.append(previousRoadName)
                    }
                }
                subItem.actionDes = sb.toString()
                subList.add(subItem)
                if (segmentInfo.assistantAction == AssistantAction.AssiActionArriveWay) {
                    // 处理多途经点信息
                    val midPois = carPathRelult.midPois
                    if (midPois != null && midPoiIndex < midPois.size) {
                        val midItem = SubItem()
                        val midPoi = midPois[midPoiIndex++]
                        if (midPoiIndex == 1) {
                            if (midPois.size == 1) {
                                midItem.actionIcon = R.drawable.global_image_action_grouppoint
                                midItem.navigtionAction = RouteActionUtil.DirICon_midd0_detail
                            } else {
                                midItem.actionIcon = R.drawable.bubble_midd1_detail
                                midItem.navigtionAction = RouteActionUtil.DirICon_midd1_detail
                            }
                        } else if (midPoiIndex == 2) {
                            midItem.actionIcon = R.drawable.bubble_midd2_detail
                            midItem.navigtionAction = RouteActionUtil.DirICon_midd2_detail
                        } else if (midPoiIndex == 3) {
                            midItem.actionIcon = R.drawable.bubble_midd3_detail
                            midItem.navigtionAction = RouteActionUtil.DirICon_midd3_detail
                        }
                        midItem.stationIndex = j
                        midItem.actionDes = (ResUtil
                            .getString(
                                R.string.sv_route_car_result_share_pass
                            )
                            .trim { it <= ' ' }
                                + midPoi.name)
                        subList.add(midItem)
                    }
                }
                if (item.actionIcon == -1) {
                    item.actionIcon = subItem.actionIcon // 将子列表的第一个值赋值给group
                }
                if (item.groupActionIcon == -1) {
                    item.groupActionIcon = subItem.groupActionIcon // 将子列表的第一个值赋值给group
                }
                if (item.navigtionAction == -1) {
                    item.navigtionAction = subItem.navigtionAction // 将子列表的第一个值赋值给group
                }
            }
            item.subList = subList
            //设置红绿灯
            if (groupTrafficLightNum > 0) {
                val trafficLightDes =
                    String.format(ResUtil.getString(R.string.sv_route_car_result_share_light_format), groupTrafficLightNum.toString() + "")
                item.groupTrafficDes = trafficLightDes
            } else {
                item.groupTrafficDes = ""
            }
            list.add(item)
        }
        //终点
        val endItem = NaviStationItemData()
        endItem.index = segmentCount.toInt()
        endItem.desType = TYPE_END
        endItem.roadName = "到达终点 ${carPathRelult.toPOI.name}"
        endItem.distanceDes = ""
        endItem.actionIcon = R.drawable.route_browser_fragment_icon_end
        endItem.navigtionAction = RouteActionUtil.DirICon_TYPE_END
        list.add(endItem)
        return list
    }

    private fun getNoCrossLinkRoadName(segment: SegmentInfo): String? {
        var roadName: String? = null
        for (i in 0 until segment.linkCount) {
            val routeLink = segment.getLinkInfo(i.toLong()) ?: continue
            if (routeLink.formway != Formway.FormwayCrossLink) {
                roadName = routeLink.roadName
            }
        }
        return roadName
    }

    /**
     * 获取link段id
     * @param segment
     * @return
     */
    private fun getLinks(segment: SegmentInfo): ArrayList<Long> {
        val routeLinks = ArrayList<Long>()
        for (i in 0 until segment.linkCount) {
            val routeLink = segment.getLinkInfo(i.toLong()) ?: continue
            routeLinks.add(routeLink.get64TopoID().toLong())
        }
        return routeLinks
    }

    private fun getLinkPoint(segment: SegmentInfo): ArrayList<Coord2DInt32> {
        val groupLinkPoints = ArrayList<Coord2DInt32>()
        for (i in 0 until segment.linkCount) {
            val routeLink = segment.getLinkInfo(i.toLong()) ?: continue
            val points = routeLink.points
            groupLinkPoints.addAll(points)
        }
        return groupLinkPoints
    }

    /**
     * 全角转半角
     *
     * @param input String.
     * @return 半角字符串
     */
    private fun toDBC(input: String?): String? {
        if (TextUtils.isEmpty(input)) {
            return input
        }
        val c = input!!.toCharArray()
        for (i in c.indices) {
            if (c[i] == '\u3000') {
                c[i] = ' '
            } else if (c[i] in '！'..'～') {
                c[i] = (c[i].code - 65248).toChar()
            }
        }
        val returnString = String(c)
        return returnString
    }
}