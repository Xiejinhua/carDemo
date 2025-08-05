package com.autosdk.bussiness.navi.route;

import com.autonavi.gbl.util.errorcode.Route;

import java.util.HashMap;
import java.util.Map;


public class RouteRequestErrorCode {

    /**
     * 获取算路失败提示文案
     */
    public static String getErrorMes(int errorCode) {
        String errorMes = "路线规划失败，errorCode = " + errorCode;
        if (RESULT_MESSAGE.containsKey(errorCode)) {
            return RESULT_MESSAGE.get(errorCode);
        }

        return errorMes;
    }

    /**
     * 此场景特殊处理
     * 在线算路失败且有避开路段
     * @param hasAvoid
     * @param errorCode
     * @return
     */
    public static String getErrorMes(boolean hasAvoid,int errorCode) {
        if (errorCode == Route.ErrorCodeOnlineFail && hasAvoid) {
            return "途经点在规避路段，请求失败";
        }else {
            return getErrorMes(errorCode);
        }
    }

    public static final Map<Integer, String> RESULT_MESSAGE = new HashMap<>();

    static {
        // 错误码对应信息
        RESULT_MESSAGE.put(Route.AUTO_UNKNOWN_ERROR, "规划失败，非法操作错误");
        RESULT_MESSAGE.put(Route.ErrorCodeDecoderUninited, "规划失败，解码器未初始化");
        RESULT_MESSAGE.put(Route.ErrorCodeNetworkError, "规划失败，网络错误");
        RESULT_MESSAGE.put(Route.ErrorCodeStartPointError, "规划失败，起点不在支持范围内");
        RESULT_MESSAGE.put(Route.ErrorCodeIlleageProtocol, "规划失败，请求协议非法");
        RESULT_MESSAGE.put(Route.ErrorCodeCallCenterError, "规划失败，从呼叫中心获取信息失败");
        RESULT_MESSAGE.put(Route.ErrorCodeEndPointError, "规划失败，终点不在支持范围内");
        RESULT_MESSAGE.put(Route.ErrorCodeEncodeFailure, "规划失败，服务端编码失败");
        RESULT_MESSAGE.put(Route.ErrorCodeLackPreviewData, "规划失败，缺少概览数据");
        RESULT_MESSAGE.put(Route.ErrorCodeBufferError, "规划失败，数据格式错误");
        RESULT_MESSAGE.put(Route.ErrorCodeStartNoRoad, "规划失败，起点抓路失败");
        RESULT_MESSAGE.put(Route.ErrorCodeEndNoRoad, "规划失败，终点抓路失败");
        RESULT_MESSAGE.put(Route.ErrorCodeHalfwayNoRoad, "规划失败，途经点抓路失败");
        RESULT_MESSAGE.put(Route.ErrorCodeOnlineFail, "规划失败，在线算路失败");
        RESULT_MESSAGE.put(Route.ErrorCodeOfflineRouteFailure, "规划失败，离线算路失败");
        RESULT_MESSAGE.put(Route.ErrorCodeUserCancel, "规划失败，用户取消算路");
        RESULT_MESSAGE.put(Route.ErrorCodeNetworkTimeout, "规划失败，网络请求超时");
        RESULT_MESSAGE.put(Route.ErrorCodeNoNewwork, "规划失败，无网络连接");
        RESULT_MESSAGE.put(Route.ErrorCodeLackStartCityData, "规划失败，当前城市未下载离线数据");
        RESULT_MESSAGE.put(Route.ErrorCodeTooFar, "规划失败，相邻两个行程点直接距离过长");
        RESULT_MESSAGE.put(Route.ErrorCodeLackWayCityData, "规划失败，途经城市缺少数据");
        RESULT_MESSAGE.put(Route.ErrorCodeViaPointError, "规划失败，中途点不在支持范围内");
        RESULT_MESSAGE.put(Route.ErrorCodeUpdatingData, "规划失败，正在更新数据");
        RESULT_MESSAGE.put(Route.ErrorCodeSlilentRouteNotMeetCriteria, "规划失败，静默算路不满足条件:限时禁行、道路关闭、车牌限行");
        RESULT_MESSAGE.put(Route.ErrorCodeLackEndCityData, "规划失败，终点所在城市无数据");
        RESULT_MESSAGE.put(Route.ErrorCodeLackViaCityData, "规划失败，途经点所在城市无数据");
        RESULT_MESSAGE.put(Route.ErrorCodeoOfflineRouteCalculating, "规划失败，离线算路正在进行中，无法进行新的计算");
        RESULT_MESSAGE.put(Route.ErrorCodeoOfflineRouteParamError, "规划失败，离线算路参数错误，无法进行新的计算");
        RESULT_MESSAGE.put(Route.ErrorCodeNoRouteEncode, "规划失败，没有路线被编码");
        RESULT_MESSAGE.put(Route.ErrorCodeNoBackupRoute, "规划失败，多路线模式（routemode=14）下没有算出备选路");
        RESULT_MESSAGE.put(Route.ErrorCodeMainRouteEmptyOrRestoreFail, "规划失败，多路线模式（routemode=14）主路线ID列表为空或者还原失败");
        RESULT_MESSAGE.put(Route.ErrorCodeDynamicRouteNoBetter, "规划失败，动态导航没有算出更优路线");
        RESULT_MESSAGE.put(Route.ErrorCodeNoBetterAbnormalBackupRoute, "规划失败，异常动作引起的更新备选路，算出来的路依然没有消除异常动作");
        RESULT_MESSAGE.put(Route.ErrorCodeNoBetterFastBackupRoute, "规划失败，前方拥堵引起的备选路更新，没有更好的能躲避拥堵的路线");
        RESULT_MESSAGE.put(Route.ErrorCodeNoSaferRoute, "规划失败，前方有危险事件 (routemode=22) 引起的重算时，没有算出安全的路线");
        RESULT_MESSAGE.put(Route.ErrorCodeCalcRouteTimeOut, "规划失败，算路超时");
        RESULT_MESSAGE.put(Route.ErrorCodeParallelRouteFail, "规划失败，主辅路线或高架上下切换失败");
        RESULT_MESSAGE.put(Route.ErrorCodeRestoreFail, "规划失败，服务路线还原失败");
        RESULT_MESSAGE.put(Route.ErrorCodeHaveHighLevelTaskWorking, "规划失败，算路当前有更高优先级的任务进行中,不能开始低优先级算路");
        RESULT_MESSAGE.put(Route.ErrorCodeStartNoSupportElectricBike, "规划失败，起点不支持电动自行车算路");
        RESULT_MESSAGE.put(Route.ErrorCodeEndNoSupportElectricBike, "规划失败，终点不支持电动自行车算路");
        RESULT_MESSAGE.put(Route.ErrorCodeCrossCityNoSupportMotorcycle, "规划失败，不支持跨城市规划摩托车路线");
        RESULT_MESSAGE.put(Route.ErrorCodeCityNoSupportMotorcycle, "规划失败，所在城市不支持摩托车算路");
        RESULT_MESSAGE.put(Route.ErrorCodeRouteReqNotExist, "规划失败，算路请求不存在");
        RESULT_MESSAGE.put(Route.ErrorCodeRespTypeNotDef, "规划失败，算路应答类型未指定");
        RESULT_MESSAGE.put(Route.ErrorCodeRouteReqOverMax, "规划失败，超过请求数的上限");
        RESULT_MESSAGE.put(Route.ErrorCodeRouteReqExist, "规划失败，算路请求已存在");
        RESULT_MESSAGE.put(Route.ErrorCodeDataIsNull, "规划失败，用于解码的数据为null");
        RESULT_MESSAGE.put(Route.ErrorCodeUncompressFail, "规划失败，解压失败");
        RESULT_MESSAGE.put(Route.ErrorCodeCanNotFoundPathIDInFirst, "规划失败，二次算路中的pathID在首次中找不到");
        RESULT_MESSAGE.put(Route.ErrorCodeHorusCanNotFoundPathIDInFirst, "规划失败，horus上云的pathID在首次中找不到");
        RESULT_MESSAGE.put(Route.ErrorCodePBPathNumIsNotEqualToFirst, "规划失败，二次下行协议中的路线个数和首次不一致");
        RESULT_MESSAGE.put(Route.ErrorCodeRequestStateODDError, "规划失败，ODD下发失败");
        RESULT_MESSAGE.put(Route.ErrorCodeRequestNotExist, "规划失败，算路请求不存在");
        RESULT_MESSAGE.put(Route.ErrorCodeRequestAlreadyExist, "规划失败，算路请求已经存在");
        RESULT_MESSAGE.put(Route.ErrorCodeRequestSendErr, "规划失败，发送失败");
        RESULT_MESSAGE.put(Route.ErrorCodeOverMaxRequest, "规划失败，超过并发算路数最大值");
        RESULT_MESSAGE.put(Route.ErrorCodeConsisOfflineReqWithoutCustomId, "规划失败，开启了一致性功能，发起离线算路，没有调用IRouteOption::SetOfflineReqCustomIdentityId设置离线算路标识");
        RESULT_MESSAGE.put(Route.ErrorCodeUnicastFail, "规划失败，开启了一致性功能，向远端发送算路失败");
        RESULT_MESSAGE.put(Route.ErrorCodeConsisSyncRouteTimeOut, "规划失败，开启了一致性功能，同步路线超时失败");
        RESULT_MESSAGE.put(Route.ErrorCodeNullPointer, "规划失败，空指针导致无法算路");
        RESULT_MESSAGE.put(Route.ErrorCodeLackingStartPoi, "规划失败，缺失起点POI");
        RESULT_MESSAGE.put(Route.ErrorCodeLackingEndPoi, "规划失败，缺失终点POI");
        RESULT_MESSAGE.put(Route.ErrorCodeSameStartEnd, "规划失败，相同的起点终点");
        RESULT_MESSAGE.put(Route.ErrorCodeSameStartVia, "规划失败，相同的起点中途点");
        RESULT_MESSAGE.put(Route.ErrorCodeSameViaEnd, "规划失败，相同的中途点终点");
        RESULT_MESSAGE.put(Route.ErrorCodeSameVia, "规划失败，相同的中途点");
        RESULT_MESSAGE.put(Route.ErrorCodeInvalidVia, "规划失败，没有对应的中途点");
        RESULT_MESSAGE.put(Route.ErrorCodeChangeEndSameViaEnd, "规划失败，相同的中途点终点(改变目的地触发的算路)");
        RESULT_MESSAGE.put(Route.ErrorCodeInRoutePlaning, "规划失败，当前处于算路请求中, 发起的算路类型被舍弃");
        RESULT_MESSAGE.put(Route.ErrorCodeTurnBackLessDistance, "规划失败，反向算路距离不足1KM");
        RESULT_MESSAGE.put(Route.ErrorCodeWait, "规划失败，已加入队列，在队列中等待执行");
        RESULT_MESSAGE.put(Route.ErrorCodeEmptyQueue, "规划失败，队列为空，无法发起算路");
        RESULT_MESSAGE.put(Route.ErrorCodeCanNotFindOldReqId, "规划失败，TBT引擎发起的重算，找不到之前旧的redId");
        RESULT_MESSAGE.put(Route.ErrorCodeInnerRerouteParamFail, "规划失败，引擎内部发起的重算，内部参数组织失败");
        RESULT_MESSAGE.put(Route.ErrorCodeParallelRoadRerouteParamFail, "规划失败，组件内部发起的平行路切换重算，内部参数组织失败");
        RESULT_MESSAGE.put(Route.ErrorCodeAosRestorationFailed, "规划失败，在线路线还原失败");
        RESULT_MESSAGE.put(Route.ErrorCodePriorityConfigError, "规划失败，优先级碰撞配置错误导致的算路失败");
        RESULT_MESSAGE.put(Route.ErrorCodeRequestIdRepeat, "规划失败，指定的算路请求requestId重复");
        RESULT_MESSAGE.put(Route.ErrorCodeCurNaviPathNull, "规划失败，未从Guide模块接收到当前主引导路线");
        RESULT_MESSAGE.put(Route.ErrorCodeCurNaviPoiNull, "规划失败，获取不到当前的途经点/终点信息");
        RESULT_MESSAGE.put(Route.ErrorCodeRouteServiceRespFail, "规划失败，对BL算路服务发起算路请求失败");
        RESULT_MESSAGE.put(Route.ErrorCodeUnknown, "规划失败，未知错误");
    }
}
