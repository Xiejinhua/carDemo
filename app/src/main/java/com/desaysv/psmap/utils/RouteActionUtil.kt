package com.desaysv.psmap.utils

import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.common.path.model.AssistantAction
import com.autonavi.gbl.common.path.model.MainAction
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R

/**
 * @author 谢锦华
 * @time 2024/10/16
 * @description
 */

object RouteActionUtil {

    //============================tbt转向图标定义对照表============================================

    const val DIR_ICON_NULL: Int = 0 //!< 无定义,                
    const val DirIcon_Car: Int = 1 //!< 自车图标,                
    const val DirIcon_Turn_Left: Int = 2 //!< 左转图标,                
    const val DirIcon_Turn_Right: Int = 3 //!< 右转图标,                
    const val DirIcon_Slight_Left: Int = 4 //!< 左前方图标,                
    const val DirIcon_Slight_Right: Int = 5 //!< 右前方图标,                
    const val DirIcon_Turn_Hard_Left: Int = 6 //!< 左后方图标,                
    const val DirIcon_Turn_Hard_Right: Int = 7 //!< 右后方图标,                
    const val DirIcon_UTurn: Int = 8 //!< 左转掉头图标,                
    const val DirIcon_Continue: Int = 9 //!< 直行图标,                
    const val DirIcon_Way: Int = 10 //!< 到达途经点图标,               
    const val DirIcon_Entry_Ring: Int = 11 //!< 进入环岛图标,               
    const val DirIcon_Leave_Ring: Int = 12 //!< 驶出环岛图标,               
    const val DirIcon_SAPA: Int = 13 //!< 到达服务区图标,               
    const val DirIcon_TollGate: Int = 14 //!< 到达收费站图标,               
    const val DirIcon_Destination: Int = 15 //!< 到达目的地图标,               
    const val DirIcon_Tunnel: Int = 16 //!< 进入隧道图标,                
    const val DirICon_Roundabout_Left_In: Int = 17 //!< 左侧进入环岛,                 
    const val DirICon_Roundabout_Left_Out: Int = 18 //!< 左侧驶出环岛
    const val DirICon_TYPE_START: Int = 19 //!< 起点
    const val DirICon_TYPE_END: Int = 20 //!< 终点
    const val DirICon_midd0_detail: Int = 21 //!< 一个途经点
    const val DirICon_midd1_detail: Int = 22 //!< 第一个途经点
    const val DirICon_midd2_detail: Int = 23 //!< 第二个途经点
    const val DirICon_midd3_detail: Int = 24 //!< 第三个途经点

    /**
     * 取导航转向动作
     * 1.在辅助动作为进入隧道的时候将主动做转化成进入隧道
     * 2.在辅助动作为服务区的时候将主动做转化成经过服务区
     * 3.在辅助动作为收费站的时候将主动作转化成经过收费站
     * 4.在辅助动作为到达目的地的时候将主动作转化成到达目的地
     * 5.在辅助动作为经过经过途经点时将主动做转化成经过途经点
     * 6.其他情况下以实际导航主动作为主
     * @return
     */
    fun getMainAction(mainAction: Int, assiAction: Int): Int {

        val assiActionMap = mapOf(
            AssistantAction.AssiActionEntryTunnel to DirIcon_Tunnel,
            AssistantAction.AssiActionArriveServiceArea to DirIcon_SAPA,
            AssistantAction.AssiActionArriveTollGate to DirIcon_TollGate,
            AssistantAction.AssiActionArriveWay to DirIcon_Way,
            AssistantAction.AssiActionArriveDestination to DirIcon_Destination
        )

        val mainActionMap = mapOf(
            MainAction.MainActionTurnLeft to DirIcon_Turn_Left,
            MainAction.MainActionTurnRight to DirIcon_Turn_Right,
            MainAction.MainActionSlightLeft to DirIcon_Slight_Left,
            MainAction.MainActionMergeLeft to DirIcon_Slight_Left,
            MainAction.MainActionSlightRight to DirIcon_Slight_Right,
            MainAction.MainActionMergeRight to DirIcon_Slight_Right,
            MainAction.MainActionTurnHardLeft to DirIcon_Turn_Hard_Left,
            MainAction.MainActionTurnHardRight to DirIcon_Turn_Hard_Right,
            MainAction.MainActionUTurn to DirIcon_UTurn,
            MainAction.MainActionContinue to DirIcon_Continue,
            MainAction.MainActionEntryRing to DirIcon_Entry_Ring,
            MainAction.MainActionLeaveRing to DirIcon_Leave_Ring
        )

        return assiActionMap[assiAction] ?: mainActionMap[mainAction] ?: DirIcon_Continue
    }

    /**
     * 获取父路线转向图标ID
     * @param action
     * @param isRightPassArea
     * @return
     */
    fun getRouteGroupNaviActionIcon(action: Int, isRightPassArea: Boolean): Int {
        return when (action) {
            DIR_ICON_NULL -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group9 else R.drawable.global_image_action_group9_day
            DirIcon_Turn_Left -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group2 else R.drawable.global_image_action_group2_day
            DirIcon_Turn_Right -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group3 else R.drawable.global_image_action_group3_day
            DirIcon_Slight_Left -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group4 else R.drawable.global_image_action_group4_day
            DirIcon_Slight_Right -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group5 else R.drawable.global_image_action_group5_day
            DirIcon_Turn_Hard_Left -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group6 else R.drawable.global_image_action_group6_day
            DirIcon_Turn_Hard_Right -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group7 else R.drawable.global_image_action_group7_day
            DirIcon_UTurn -> if (isRightPassArea) {
                if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group8 else R.drawable.global_image_action_group8_day
            } else {
                if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group19 else R.drawable.global_image_action_group19_day
            }

            DirIcon_Continue -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group9 else R.drawable.global_image_action_group9_day
            DirIcon_SAPA -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group13 else R.drawable.global_image_action_group13_day
            DirIcon_TollGate -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group14 else R.drawable.global_image_action_group14_day
            DirIcon_Entry_Ring -> if (isRightPassArea) {
                if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group11 else R.drawable.global_image_action_group11_day
            } else {
                if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group15 else R.drawable.global_image_action_group15_day
            }

            DirIcon_Leave_Ring -> if (isRightPassArea) {
                if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group12 else R.drawable.global_image_action_group12_day
            } else {
                if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group16 else R.drawable.global_image_action_group16_day
            }

            DirICon_TYPE_START -> if (NightModeGlobal.isNightMode()) R.drawable.route_browser_fragment_icon_start else R.drawable.route_browser_fragment_icon_start_day
            DirICon_TYPE_END -> if (NightModeGlobal.isNightMode()) R.drawable.route_browser_fragment_icon_end else R.drawable.route_browser_fragment_icon_end_day
            DirICon_midd0_detail -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_grouppoint else R.drawable.global_image_action_grouppoint_day
            DirICon_midd1_detail -> if (NightModeGlobal.isNightMode()) R.drawable.bubble_midd1_detail else R.drawable.bubble_midd1_detail_day
            DirICon_midd2_detail -> if (NightModeGlobal.isNightMode()) R.drawable.bubble_midd2_detail else R.drawable.bubble_midd2_detail_day
            DirICon_midd3_detail -> if (NightModeGlobal.isNightMode()) R.drawable.bubble_midd3_detail else R.drawable.bubble_midd3_detail_day
            else -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_action_group9 else R.drawable.global_image_action_group9_day
        }
    }

    fun getNaviActionStr(action: Int): String {
        return when (action) {
            DirIcon_Turn_Left -> ResUtil.getString(R.string.sv_route_navi_action_turnleft)
            DirIcon_Turn_Right -> ResUtil.getString(R.string.sv_route_navi_action_turnright)
            DirIcon_Slight_Left -> ResUtil.getString(R.string.sv_route_navi_action_left_front)
            DirIcon_Slight_Right -> ResUtil.getString(R.string.sv_route_navi_action_right_front)
            DirIcon_Turn_Hard_Left -> ResUtil.getString(R.string.sv_route_navi_action_left_back)
            DirIcon_Turn_Hard_Right -> ResUtil.getString(R.string.sv_route_navi_action_right_back)
            DirIcon_UTurn -> ResUtil.getString(R.string.sv_route_navi_action_turn_left_back)
            DirIcon_Continue -> ResUtil.getString(R.string.sv_route_navi_action_along)
            DirIcon_SAPA -> ResUtil.getString(R.string.sv_route_navi_action_along_rest)
            DirIcon_TollGate -> ResUtil.getString(R.string.sv_route_navi_action_along_charge)
            DirIcon_Entry_Ring -> ResUtil.getString(R.string.sv_route_navi_action_enter_ring)
            DirIcon_Leave_Ring -> ResUtil.getString(R.string.sv_route_navi_action_leave_ring)
            else -> ResUtil.getString(R.string.sv_route_navi_action_along)
        }
    }
}