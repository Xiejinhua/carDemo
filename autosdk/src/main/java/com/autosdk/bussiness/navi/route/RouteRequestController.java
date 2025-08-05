package com.autosdk.bussiness.navi.route;

import static com.autonavi.gbl.common.path.model.PointType.PointTypeStart;
import static com.autonavi.gbl.common.path.option.ParalleType.paralleTypeMainSide;
import static com.autonavi.gbl.common.path.option.ParalleType.paralleTypeOverhead;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeDamagedRoad;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeLimitForbid;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeLimitForbidOffLine;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeLimitLine;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeMutiRouteRequest;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeParallelRoad;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeTMC;

import android.location.Location;
import android.text.TextUtils;

import com.autonavi.gbl.aosclient.model.RouteDisplayPoints;
import com.autonavi.gbl.aosclient.model.RoutePathProjectPoints;
import com.autonavi.gbl.common.path.model.POIInfo;
import com.autonavi.gbl.common.path.model.PointType;
import com.autonavi.gbl.common.path.model.RouteLimitInfo;
import com.autonavi.gbl.common.path.option.CurrentNaviInfo;
import com.autonavi.gbl.common.path.option.CurrentPositionInfo;
import com.autonavi.gbl.common.path.option.POIForRequest;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.common.path.option.RouteConstrainCode;
import com.autonavi.gbl.common.path.option.RouteOption;
import com.autonavi.gbl.common.path.option.RouteType;
import com.autonavi.gbl.common.path.option.UserAvoidInfo;
import com.autonavi.gbl.guide.model.NaviInfo;
import com.autonavi.gbl.guide.model.NaviPath;
import com.autonavi.gbl.guide.model.guidecontrol.Param;
import com.autonavi.gbl.guide.model.guidecontrol.Type;
import com.autonavi.gbl.map.layer.model.CarLoc;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.pos.model.LocParallelRoadInfo;
import com.autonavi.gbl.pos.model.LocSwitchRoadType;
import com.autonavi.gbl.route.RouteService;
import com.autonavi.gbl.route.model.PathResultData;
import com.autonavi.gbl.route.model.RouteAlongServiceAreaInfo;
import com.autonavi.gbl.route.model.RouteControlKey;
import com.autonavi.gbl.route.model.RouteRestorationOption;
import com.autonavi.gbl.route.model.RouteServiceAreaInfo;
import com.autonavi.gbl.route.observer.IRouteResultObserver;
import com.autonavi.gbl.route.observer.IRouteServiceAreaObserver;
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg;
import com.autonavi.gbl.user.msgpush.model.MobileRouteParam;
import com.autonavi.gbl.user.msgpush.model.MobileVehicleInfo;
import com.autonavi.gbl.user.msgpush.model.RoutepathrestorationPathInfo;
import com.autonavi.gbl.util.errorcode.Route;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.POIFactory;
import com.autosdk.bussiness.common.utils.ElectricInfoConverter;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.navi.NaviController;
import com.autosdk.bussiness.navi.route.callback.IRouteResultCallBack;
import com.autosdk.bussiness.navi.route.model.PathResultDataInfo;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;
import com.autosdk.bussiness.navi.route.model.RouteRequestParam;
import com.autosdk.bussiness.navi.route.model.TruckRouteRequestParam;
import com.autosdk.bussiness.navi.route.utils.RouteLifecycleMonitor;
import com.autosdk.bussiness.navi.route.utils.RouteOptionUtil;
import com.autosdk.bussiness.widget.navi.NaviComponent;
import com.autosdk.common.utils.SdkNetworkUtil;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

/**
 * 路线规划管理类
 */
public class RouteRequestController {
    private static final long INVALID_REQUEST_ID = -1;

    private RouteCarResultData mCarRouteResult;

    private boolean mNeedMonitorRouteLife = true;

    private IRouteResultCallBack mRouteResultCallBack;

    private final Object mRouteResultLock = new Object();

    private final ConcurrentHashMap<Long, RouteOption> mRouteOptions = new ConcurrentHashMap<>();

    public void setCarRouteResult(RouteCarResultData mCarRouteResult) {
        this.mCarRouteResult = mCarRouteResult;
    }

    public RouteCarResultData getCarRouteResult() {
        return this.mCarRouteResult;
    }

    public RouteOption getRouteOption(long requestID) {
        return mRouteOptions.get(requestID);
    }

    private IRouteResultObserver mListener = new IRouteResultObserver() {
        @Override
        public void onNewRoute(PathResultData pathResultData, ArrayList<PathInfo> pathInfo, RouteLimitInfo routeLimitInfo) {
            NaviController.getInstance().unregisterRouteObserver(mListener);
            Timber.i("RouteRequestController onNewRoute PathCount：%s ，isLocal：%s", pathInfo.size(), pathResultData.isLocal);
            synchronized (RouteRequestController.this.mRouteResultLock) {
                if (null != mCarRouteResult) {
                    //多备选算路成功后，如果发现当前主路线已经切换，放弃此次算路结果
                    if (pathResultData.type == RouteType.RouteTypeMutiRouteRequest) {
                        NaviPath naviPath = NaviComponent.getInstance().getNaviPath();
                        if (naviPath == null) {
                            Timber.i("RouteTypeMutiRouteRequest naviPath == null");
                            return;
                        }
                        if (naviPath.vecPaths.size() > mCarRouteResult.getFocusIndex() && !pathInfo.isEmpty()) {
                            long mainPathID = naviPath.vecPaths.get(mCarRouteResult.getFocusIndex()).getPathID();
                            long curMainPath = pathInfo.get(0).getPathID();
                            Timber.i("preMainPathID: " + mainPathID + " curMainPathID: " + curMainPath);
                            if (mainPathID != curMainPath) {
                                Timber.i("main path change, ignore muti reroute");
                                return;
                            }
                        }
                    }

                    mCarRouteResult.setIsOffline(pathResultData.isLocal);
                    mCarRouteResult.setPathResult(pathInfo);
                    PathResultDataInfo pathResultDataInfo = new PathResultDataInfo();
                    pathResultDataInfo.errorCode = pathResultData.errorCode;
                    pathResultDataInfo.isChange = pathResultData.isChange;
                    pathResultDataInfo.isLocal = pathResultData.isLocal;
                    pathResultDataInfo.mode = pathResultData.mode;
                    pathResultDataInfo.calcRouteResultData.drivePlanData = pathResultData.calcRouteResultData.drivePlanData;
                    pathResultDataInfo.calcRouteResultData.driveGuideData = pathResultData.calcRouteResultData.driveGuideData;
                    pathResultDataInfo.requestId = pathResultData.requestId;
                    pathResultDataInfo.type = pathResultData.type;
                    mCarRouteResult.setPathResultDataInfo(pathResultDataInfo);
                    if (null != mRouteResultCallBack) {
                        mRouteResultCallBack.callback(mCarRouteResult, pathResultData.isLocal);
                    }
                    mRouteOptions.remove(pathResultData.requestId);
                }
            }
        }

        @Override
        public void onNewRouteError(PathResultData pathResultData, RouteLimitInfo routeLimitInfo) {
            Timber.i("RouteRequestController onNewRouteError errorCode = %s", pathResultData.errorCode);
            // 算路失败的情况下，BL组件内部是否重新发起离线算路。true 自动再次发起离线算路 false 算路结束，BL内部不会发起离线算路
            if (pathResultData.online2OfflineInfo.isRetryingRequestByBL) {
                return;
            }
            if ((RouteTypeTMC == pathResultData.type) || (RouteTypeLimitLine == pathResultData.type) || (RouteTypeMutiRouteRequest == pathResultData.type) ||
                    (RouteTypeDamagedRoad == pathResultData.type) || (RouteTypeLimitForbid == pathResultData.type) || (RouteTypeLimitForbidOffLine == pathResultData.type)
                    && Route.ErrorCodeSlilentRouteNotMeetCriteria == pathResultData.errorCode) {
                // 静默算路，失败RouteErrorcodeSlilentRouteNotMeetCriteria不需要处理
            } else {
                // 通知guideService算路失败
                NaviController.getInstance().notifyRerouteFail(pathResultData.type);
            }
            RouteOption routeOption = mRouteOptions.remove(pathResultData.requestId);
            if (pathResultData.errorCode == Route.ErrorCodeNetworkError) {
                //网络不好，在线算路失败，内部默认转离线
                if (routeOption != null) {
                    int constrainCode = routeOption.getConstrainCode();
                    Timber.i("ErrorCodeNetworkError %s", constrainCode);
                    routeOption.setConstrainCode(constrainCode | RouteConstrainCode.RouteCalcLocal);
                    long requestID = NaviController.getInstance().requestTbtRoute(routeOption);
                    mRouteOptions.put(requestID, routeOption);
                    if (mCarRouteResult != null)
                        mCarRouteResult.setIsOffline(true);//离线模式
                } else {
                    NaviController.getInstance().unregisterRouteObserver(mListener);
                    if (mRouteResultCallBack != null) {
                        mRouteResultCallBack.errorCallback(pathResultData.errorCode, null, pathResultData.isLocal);
                    }
                }
            } else if (pathResultData.errorCode == Route.ErrorCodeUserCancel) {
                Timber.i(" onNewRouteError ErrorCodeUserCancel ");
//                RouteOption.destroy(mRouteOption);
            } else {
                NaviController.getInstance().unregisterRouteObserver(mListener);
                if (mRouteResultCallBack != null) {
                    mRouteResultCallBack.errorCallback(pathResultData.errorCode, null, pathResultData.isLocal);
                }
            }
        }
    };

    private static class RequestManagerHolder {
        private static RouteRequestController mInstance = new RouteRequestController();
    }

    private RouteRequestController() {
        NaviController.getInstance().registerAlongServiceAreaObserver(mRouteServiceAreaObserver);
    }

    public static RouteRequestController getInstance() {
        return RequestManagerHolder.mInstance;
    }

    /**
     * 路线请求接口（内部优先在线算路，失败后自动转离线算路）
     *
     * @param routeRequestParam   路线请求使用参数
     * @param routeResultCallBack 结果返回
     * @return 返回算路id，可通过abortRequest()方法取消
     */
    public long requestRoute(RouteRequestParam routeRequestParam, IRouteResultCallBack routeResultCallBack) {
        synchronized (RouteRequestController.this.mRouteResultLock) {
            mRouteResultCallBack = routeResultCallBack;
            RouteOption mRouteOption = getRouteOption(routeRequestParam);
            NaviController.getInstance().registerRouteObserver(mListener);
            long result = NaviController.getInstance().requestTbtRoute(mRouteOption);
            mRouteOptions.put(result, mRouteOption);
            return result;
        }
    }

    /**
     * 路线请求接口
     *
     * @param routeOption         重新算路参数
     * @param routeResultCallBack 结果返回
     * @return 返回算路id，可通过abortRequest()方法取消
     */
    public long requestReRoute(RouteOption routeOption, RouteRequestParam routeRequestParam, IRouteResultCallBack routeResultCallBack) {
        if (routeOption == null || routeRequestParam == null) {
            return INVALID_REQUEST_ID;
        }

        mRouteResultCallBack = routeResultCallBack;
        if (!TextUtils.isEmpty(routeRequestParam.invokerType)) {
            NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeySetInvoker, routeRequestParam.invokerType);
        }
        mCarRouteResult = new RouteCarResultData();
        mCarRouteResult.setFromPOI(routeRequestParam.startPOI);
        mCarRouteResult.setToPOI(routeRequestParam.endPOI);
        mCarRouteResult.setMidPois(routeRequestParam.midPois);
        mCarRouteResult.setRouteStrategy(routeOption.getRouteStrategy());
        mCarRouteResult.setRouteConstrainCode(routeOption.getConstrainCode());
        mCarRouteResult.setCarPlate(routeRequestParam.carPlate);
        mCarRouteResult.setOpenAvoidLimit(routeRequestParam.openAvoidLimit);
        NaviController.getInstance().registerRouteObserver(mListener);
        long result = NaviController.getInstance().requestTbtRoute(routeOption);
        mRouteOptions.put(result, routeOption);
        return result;
    }

    /**
     * 平行路切换路线请求接口
     *
     * @param routeRequestParam   路线请求使用参数
     * @param routeResultCallBack 结果返回
     * @return 返回算路id，可通过abortRequest()方法取消
     */
    public long requestSwitchRoute(RouteRequestParam routeRequestParam, BigInteger roadid, CarLoc pstLocInfo, LocParallelRoadInfo mLocParallelRoadInfo, int mSwitchRoadType,
                                   NaviInfo naviInfo, RouteCarResultData mRouteCarResultData, IRouteResultCallBack routeResultCallBack) {
        mRouteResultCallBack = routeResultCallBack;
        RouteOption mRouteOption = new RouteOption();
        mRouteOption.setRouteType(RouteTypeParallelRoad);
        mRouteOption.setConstrainCode(routeRequestParam.routeConstrainCode);
        mRouteOption.setRouteCalcNumber(routeRequestParam.routeCalcNumber);
        mRouteOption.setRouteStrategy(routeRequestParam.routeStrategy);
        if (!TextUtils.isEmpty(routeRequestParam.invokerType)) {
            NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeySetInvoker, routeRequestParam.invokerType);
        }
        //做成行程点信息
        POIForRequest poiForRequest = new POIForRequest();
        poiForRequest.reset();
        // 起点设置
        POIInfo startInfo = new POIInfo();
        startInfo.realPos.lon = routeRequestParam.startPOI.getPoint().getLongitude();
        startInfo.realPos.lat = routeRequestParam.startPOI.getPoint().getLatitude();
        POIInfo endInfo = RouteOptionUtil.poiToPOIInfo(routeRequestParam.endPOI, PointType.PointTypeEnd);
        //起点
        poiForRequest.addPoint(PointTypeStart, startInfo);
        //终点
        poiForRequest.addPoint(PointType.PointTypeEnd, endInfo);
        ArrayList<POI> midPois = routeRequestParam.midPois;
        if (midPois != null && midPois.size() > 0) {
            for (int i = 0; i < midPois.size(); i++) {
                POIInfo midInfo = RouteOptionUtil.poiToPOIInfo(midPois.get(i), PointType.PointTypeVia);
                poiForRequest.addPoint(PointType.PointTypeVia, midInfo);
            }
        }
        // 获取最新的定位信息，重新更新以下算路信息
        LocInfo info = LocationController.getInstance().getLocInfo();
        if (info != null) {
            poiForRequest.setDirection(info.matchRoadCourse);
            poiForRequest.setReliability(info.courseAcc);
            poiForRequest.setAngleType(info.startDirType);
            poiForRequest.setAngleGps(info.gpsDir);
            poiForRequest.setAngleComp(info.compassDir);
            poiForRequest.setSpeed(info.speed);
            poiForRequest.setLinkType(info.matchInfo.get(0).linkType);
            poiForRequest.setFormWay(info.matchInfo.get(0).formway);
            poiForRequest.setSigType(info.startPosType);
            poiForRequest.setFittingDir(info.fittingCourse);
            poiForRequest.setMatchingDir(info.roadDir);
            poiForRequest.setFittingCredit(info.fittingCourseAcc);
            poiForRequest.setPrecision(info.posAcc);
        }
        //平行路切换id
        poiForRequest.setPointRoadID(PointTypeStart, 0, roadid);
        mRouteOption.setPOIForRequest(poiForRequest);

        // 设置当前导航位置信息
        // 定位回调onLocInfoUpdate的最近一次信息pstLocInfo
        CurrentPositionInfo curLocation = new CurrentPositionInfo();
        curLocation.linkIndex = pstLocInfo.vecPathMatchInfo.get(0).nLinkCur;
        curLocation.pointIndex = pstLocInfo.vecPathMatchInfo.get(0).nPostCur;
        curLocation.segmentIndex = pstLocInfo.vecPathMatchInfo.get(0).nSegmCur;
        //0：无高架 1：车标在高架上（车标所在道路有对应高架下） 2：车标在高架下（车标所在道路有对应高架上）
        curLocation.overheadFlag = (short) mLocParallelRoadInfo.hwFlag;
        //0：无主辅路（车标所在道路旁无主辅路） 1：车标在主路（车标所在道路旁有辅路） 2：车标在辅路（车标所在道路旁有主路）
        curLocation.parallelRoadFlag = (short) mLocParallelRoadInfo.flag;
        mRouteOption.setCurrentLocation(curLocation);

        // 设置切换类型：高架切换 paralleTypeOverhead 还是 主辅路切换 paralleTypeMainSide，根据主辅路切换信息决定的
        if (mSwitchRoadType == LocSwitchRoadType.LocSwitchUpBridgeToDownBridge || mSwitchRoadType == LocSwitchRoadType.LocSwitchDownBridgeToUpBridge) {
            mRouteOption.setParalleType(paralleTypeOverhead);
        }
        if (mSwitchRoadType == LocSwitchRoadType.LocSwitchMainToSide || mSwitchRoadType == LocSwitchRoadType.LocSwitchSideToMain) {
            mRouteOption.setParalleType(paralleTypeMainSide);
        }
        // 设置当前导航剩余信息接口,来源：最近一次的Guide回调onUpdateNaviInfo
        if (naviInfo != null) {
            CurrentNaviInfo curNaviInfo = new CurrentNaviInfo();
            curNaviInfo.remainRouteTime = naviInfo.routeRemain.time;
            curNaviInfo.remainRouteDist = naviInfo.routeRemain.dist;
            curNaviInfo.remainSegmentDist = naviInfo.NaviInfoData.get(naviInfo.NaviInfoFlag).segmentRemain.dist;
            curNaviInfo.drivingRouteDist = naviInfo.driveDist;
            mRouteOption.setRemainNaviInfo(curNaviInfo);
        }
        if (mRouteCarResultData.getPathResult() == null || mRouteCarResultData.getPathResult().size() <= 0) {
            return -1;
        }

        PathInfo mPathInfo = mRouteCarResultData.getPathResult().get(mRouteCarResultData.getFocusIndex());
        // 设置上一次路线进来
        mRouteOption.setNaviPath(mPathInfo);
        mCarRouteResult = new RouteCarResultData();
        mCarRouteResult.setFromPOI(routeRequestParam.startPOI);
        mCarRouteResult.setToPOI(routeRequestParam.endPOI);
        mCarRouteResult.setMidPois(midPois);
        mCarRouteResult.setRouteStrategy(routeRequestParam.routeStrategy);
        mCarRouteResult.setRouteConstrainCode(routeRequestParam.routeConstrainCode);
        NaviController.getInstance().registerRouteObserver(mListener);
        long result = NaviController.getInstance().requestTbtRoute(mRouteOption);
        mRouteOptions.put(result, mRouteOption);
        return result;
    }

    /**
     * 手车联动 线路还原
     *
     * @param aimRoutePushMsg
     * @param routeResultCallBack
     */
    public long requestRouteRestoration(AimRoutePushMsg aimRoutePushMsg, IRouteResultCallBack routeResultCallBack) {
        mRouteResultCallBack = routeResultCallBack;
        // -1表示发起算路失败
        long requestId = -1;

        if (aimRoutePushMsg != null && aimRoutePushMsg.content != null) {
            RoutepathrestorationPathInfo path = aimRoutePushMsg.content.path;
            MobileRouteParam routeParam = aimRoutePushMsg.content.routeParam;
            mCarRouteResult = new RouteCarResultData();
            RouteOption mRouteOption = new RouteOption();

            // 路线还原
            RouteRestorationOption routeRestorationOption = new RouteRestorationOption();
            if (routeRestorationOption == null || mRouteOption == null) {
                Timber.d("requestRouteRestoration routeRestorationOption == null || mRouteOption == null.");
                return requestId;
            }

            POI startPOI = new POI();
            POI endPOI = new POI();
            ArrayList<POI> midPoint = new ArrayList<>();
            ArrayList<RouteDisplayPoints> routeDisplayPoints = new ArrayList<>();
            ArrayList<RoutePathProjectPoints> viaPathProjectPoints = null;

            if (routeParam != null) {
                if (routeParam.location != null) {
                    startPOI.setName(routeParam.location.name);
                    startPOI.setId(routeParam.location.poiId);
                    startPOI.setType(PointType.PointTypeStart);
                }

                if (routeParam.destination != null) {
                    endPOI.setName(routeParam.destination.name);
                    endPOI.setId(routeParam.destination.poiId);
                    endPOI.setType(PointType.PointTypeEnd);
                }

                mCarRouteResult.setRouteStrategy(Integer.parseInt(routeParam.type));
                mCarRouteResult.setCarPlate(routeParam.vehicle.plate);
                mRouteOption.setRouteType(routeParam.routeMode);
                mRouteOption.setRouteStrategy(Integer.parseInt(routeParam.type));

                //路线还原参数设置
                if (routeParam.destination != null) {
                    routeRestorationOption.setEndName(routeParam.destination.name);
                }
                routeRestorationOption.setContentOption(routeParam.contentOption);
                routeRestorationOption.setType(routeParam.type);

                MobileVehicleInfo vehicle = routeParam.vehicle;
                routeRestorationOption.setCarType(vehicle.type);
                routeRestorationOption.setCarSize(vehicle.size);
                routeRestorationOption.setCarHeight(vehicle.height);
                routeRestorationOption.setCarWidth(vehicle.width);
                routeRestorationOption.setCarLoad(vehicle.load);
                routeRestorationOption.setCarWeight(vehicle.weight);
                routeRestorationOption.setCarAxis(vehicle.axis);
                routeRestorationOption.setCarPlate(vehicle.plate);
            } else {
                Timber.d("requestRouteRestoration aimRoutePushMsg.content.routeParam == null");
            }

            if (path != null && routeParam != null) {
                if (path.startPoints.points.size() == 0) {
                    startPOI.setPoint(new GeoPoint(Double.parseDouble(routeParam.startPoints.get(0).lon),
                            Double.parseDouble(routeParam.startPoints.get(0).lat)));
                    endPOI.setPoint(new GeoPoint(Double.parseDouble(routeParam.endPoints.get(0).lon),
                            Double.parseDouble(routeParam.endPoints.get(0).lat)));
                    for (int j = 1, i = 0; j < routeParam.viaPoints.size(); j++, i++) {
                        POI points = new POI();
                        points.setPoint(new GeoPoint(Double.parseDouble(routeParam.viaPoints.get(j).lon), Double.parseDouble(routeParam.viaPoints.get(j).lat)));
                        if (routeParam.routeViaPoints != null && i < routeParam.routeViaPoints.size()) {
                            points.setName(routeParam.routeViaPoints.get(i).name);
                            points.setId(routeParam.routeViaPoints.get(i).poiId);
                            // 手机发来的可能和样板间含义不一致
                            points.setType(routeParam.routeViaPoints.get(i).type);
                            points.setTypeCode(routeParam.routeViaPoints.get(i).typeCode);
                        }
                        midPoint.add(points);
                        //路线还原参数设置
                        RouteDisplayPoints routeDisplayPoints1 = new RouteDisplayPoints();
                        routeDisplayPoints1.lat = routeParam.viaPoints.get(j).lat;
                        routeDisplayPoints1.lon = routeParam.viaPoints.get(j).lon;
                        routeDisplayPoints.add(routeDisplayPoints1);
                    }
                    //路线还原参数设置
                    routeRestorationOption.setPaths(path.paths);
                    routeRestorationOption.setStartPoints(routeParam.startPoints);
                    routeRestorationOption.setEndPoints(routeParam.endPoints);
                } else {
                    startPOI.setPoint(new GeoPoint(Double.parseDouble(path.startPoints.points.get(0).lon),
                            Double.parseDouble(path.startPoints.points.get(0).lat)));
                    endPOI.setPoint(new GeoPoint(Double.parseDouble(path.endPoints.points.get(0).lon),
                            Double.parseDouble(path.endPoints.points.get(0).lat)));

                    for (int j = 0; j < path.routeViaPoints.display_points.size(); j++) {
                        POI points = new POI();
                        points.setPoint(new GeoPoint(Double.parseDouble(path.routeViaPoints.display_points.get(j).lon), Double.parseDouble(path.routeViaPoints.display_points.get(j).lat)));
                        if (routeParam.routeViaPoints != null && j < routeParam.routeViaPoints.size()) {
                            points.setName(routeParam.routeViaPoints.get(j).name);
                            points.setId(routeParam.routeViaPoints.get(j).poiId);
                            // 手机发来的可能和样板间含义不一致
                            points.setType(routeParam.routeViaPoints.get(j).type);
                            points.setTypeCode(routeParam.routeViaPoints.get(j).typeCode);
                        }
                        midPoint.add(points);
                        //路线还原参数设置
                        RouteDisplayPoints routeDisplayPoints1 = new RouteDisplayPoints();
                        routeDisplayPoints1.lat = path.routeViaPoints.display_points.get(j).lat;
                        routeDisplayPoints1.lon = path.routeViaPoints.display_points.get(j).lon;
                        routeDisplayPoints.add(routeDisplayPoints1);
                    }

                    viaPathProjectPoints = new ArrayList<>(path.routeViaPoints.path_project_points);
                    //路线还原参数设置
                    routeRestorationOption.setPaths(path.paths);
                    routeRestorationOption.setStartPoints(path.startPoints.points);
                    routeRestorationOption.setEndPoints(path.endPoints.points);
                }

            } else {
                Timber.d("requestRouteRestoration aimRoutePushMsg.content.path == null");
            }

            mCarRouteResult.setFromPOI(startPOI);
            mCarRouteResult.setToPOI(endPOI);
            mCarRouteResult.setMidPois(midPoint);

            //做成行程点信息
            POIForRequest poiForRequest = new POIForRequest();
            POIInfo startInfo = RouteOptionUtil.poiToPOIInfo(startPOI, PointType.PointTypeStart);
            if (!startInfo.poiID.isEmpty()) {
                startInfo.type = 2;
            }
            POIInfo endInfo = RouteOptionUtil.poiToPOIInfo(endPOI, PointType.PointTypeEnd);
            if (!endInfo.poiID.isEmpty()) {
                endInfo.type = 2;
            }
            //起点
            poiForRequest.addPoint(PointType.PointTypeStart, startInfo);
            //终点
            poiForRequest.addPoint(PointType.PointTypeEnd, endInfo);
            for (POI poi : midPoint) {
                POIInfo midInfo = RouteOptionUtil.poiToPOIInfo(poi, PointType.PointTypeVia);
                if (!midInfo.poiID.isEmpty()) {
                    midInfo.type = 2;
                }
                poiForRequest.addPoint(PointType.PointTypeVia, midInfo);
                Timber.d("via point name:%s, id:%s", poi.getName(), poi.getId());
            }

            mRouteOption.setPOIForRequest(poiForRequest);

            //路线还原参数设置
            routeRestorationOption.setViaPoints(routeDisplayPoints, viaPathProjectPoints == null ? new ArrayList<>() : viaPathProjectPoints);
            routeRestorationOption.setRouteVer(RouteService.getRouteVersion());
            routeRestorationOption.setSdkVer(RouteService.getEngineVersion());
            routeRestorationOption.setNaviId(aimRoutePushMsg.content.naviId);

            NaviController.getInstance().registerRouteObserver(mListener);

            //关闭在线算路失败转离线算路
            mRouteOption.setTrySwitchToLocal(false);
            requestId = NaviController.getInstance().requestRouteRestoration(routeRestorationOption, mRouteOption);
            mRouteOptions.put(requestId, mRouteOption);
            return requestId;
        } else {
            Timber.d("requestRouteRestoration aimRoutePushMsg.content == null || aimRoutePushMsg = null");
        }
        return -1;
    }

    /**
     * 取消路径计算
     */
    public void abortRequest(long id) {
        boolean flag = NaviController.getInstance().abortRoutePlan(id);
        Timber.i("abortRequest %s", flag);
        if (flag)
            mRouteOptions.remove(id);
    }

    public RouteOption getRouteOption(RouteRequestParam routeRequestParam) {
        RouteOption routeOption = new RouteOption();
        //做成行程点信息
        POIForRequest poiForRequest = new POIForRequest();
        POIInfo startInfo = RouteOptionUtil.poiToPOIInfo(routeRequestParam.startPOI, PointType.PointTypeStart);
        POIInfo endInfo = RouteOptionUtil.poiToPOIInfo(routeRequestParam.endPOI, PointType.PointTypeEnd);
        //起点
        poiForRequest.addPoint(PointType.PointTypeStart, startInfo);
        //终点
        poiForRequest.addPoint(PointType.PointTypeEnd, endInfo);
        ArrayList<POI> midPois = routeRequestParam.midPois;
        if (midPois != null && midPois.size() > 0) {
            for (int i = 0; i < midPois.size(); i++) {
                POIInfo midInfo = RouteOptionUtil.poiToPOIInfo(midPois.get(i), PointType.PointTypeVia);
                poiForRequest.addPoint(PointType.PointTypeVia, midInfo);
            }
        }
        // 获取最新的定位信息，重新更新以下算路信息
        LocInfo info = LocationController.getInstance().getLocInfo();
        if (info != null) {
            poiForRequest.setDirection(info.matchRoadCourse);
            poiForRequest.setReliability(info.courseAcc);
            poiForRequest.setAngleType(info.startDirType);
            poiForRequest.setAngleGps(info.gpsDir);
            poiForRequest.setAngleComp(info.compassDir);
            poiForRequest.setSpeed(info.speed);
            poiForRequest.setLinkType(info.matchInfo.get(0).linkType);
            poiForRequest.setFormWay(info.matchInfo.get(0).formway);
            poiForRequest.setSigType(info.startPosType);
            poiForRequest.setFittingDir(info.fittingCourse);
            poiForRequest.setMatchingDir(info.roadDir);
            poiForRequest.setFittingCredit(info.fittingCourseAcc);
            poiForRequest.setPrecision(info.posAcc);
        }
        routeOption.setPOIForRequest(poiForRequest);
        routeOption.setRouteCalcNumber(routeRequestParam.routeCalcNumber);
        routeOption.setConstrainCode(routeRequestParam.routeConstrainCode);
        routeOption.setRouteStrategy(routeRequestParam.routeStrategy);

        if (routeRequestParam.avoidLinks != null && routeRequestParam.avoidLinks.size() > 0) {
            UserAvoidInfo userAvoidInfo = new UserAvoidInfo();
            userAvoidInfo.linkList = routeRequestParam.avoidLinks;
            userAvoidInfo.type = 4;
            routeOption.setUserAvoidInfo(userAvoidInfo);
            //当进行避开路段的算路，则关闭在线算路失败转离线算路
            routeOption.setTrySwitchToLocal(false);
        } else {
            routeOption.setTrySwitchToLocal(!NaviController.getInstance().isMultiConsistenceAvailable());
        }

        mCarRouteResult = new RouteCarResultData();
        mCarRouteResult.setFromPOI(routeRequestParam.startPOI);
        mCarRouteResult.setToPOI(routeRequestParam.endPOI);
        mCarRouteResult.setMidPois(midPois);
        mCarRouteResult.setRouteStrategy(routeRequestParam.routeStrategy);
        mCarRouteResult.setRouteConstrainCode(routeRequestParam.routeConstrainCode);
        mCarRouteResult.setAvoidLinks(routeRequestParam.avoidLinks);
        mCarRouteResult.setSceneResult(routeRequestParam.isCarSceneRequest);
        mCarRouteResult.setIsOffline(!SdkNetworkUtil.getInstance().isNetworkConnected());
        if (!TextUtils.isEmpty(routeRequestParam.invokerType)) {
            NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeySetInvoker, routeRequestParam.invokerType);
        }
        String mCarNoStr = routeRequestParam.carPlate;
        Param param = new Param();
        if (ElectricInfoConverter.isElectric()) {
            routeRequestParam.setVehicleType(RouteRequestParam.VEHICLE_TYPE_ELC_BUS);
        }
        param.vehicle.type = routeRequestParam.getVehicleType();
        NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeyVehicleType, String.valueOf(routeRequestParam.getVehicleType()));
        //限行
        //设置eta请求的躲避车辆限行,0表示关闭eta限行请求，1 表示打开eta限行请求
        String etaRestriction = routeRequestParam.openAvoidLimit ? "1" : "0";
        //设置是否避开限行
        mCarRouteResult.setOpenAvoidLimit(routeRequestParam.openAvoidLimit);
        if (!TextUtils.isEmpty(mCarNoStr)) {
            //设置请求时的车牌号
            mCarRouteResult.setCarPlate(mCarNoStr);
            NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeyVehicleID, mCarNoStr);
            param.vehicle.vehicleId = mCarNoStr;
        } else {
            mCarRouteResult.setCarPlate("");
            NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeyVehicleID, "");
        }
        NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeyETARestriction, etaRestriction);
        //部分的GuideService.setParam的开关配置，需要配合RouteService.control开关配置才能生效
        //具体查看https://yuque.antfin.com/lkoaqr/autosdk460/yhpalb
        //针对货车的处理
        //460 sdk不支持，490后开始支持
        if (routeRequestParam.getVehicleType() == RouteRequestParam.VEHICLE_TYPE_TRUCK) {
            TruckRouteRequestParam truckRouteRequestParam = (TruckRouteRequestParam) routeRequestParam;
            param.type = Type.GuideParamVehicle;
            if (!TextUtils.isEmpty(mCarNoStr)) {
                //货车车牌号
                NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeyTruckPlateInfo, mCarNoStr);
            }
            //货车信息(JSON) 例：{"height":"0.5","load":"0.5","width":"0.5","length":"0.5","weight":"0.5","size":"1","axis":"1","loadswitch":"1"}
            NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeyTrukInfo, truckRouteRequestParam.getRouteControlKeyTrukInfo());
            param.vehicle.height = truckRouteRequestParam.vehicleHeight;
            param.vehicle.load = truckRouteRequestParam.vehicleLoad;
            param.vehicle.width = truckRouteRequestParam.vehicleWidth;
            param.vehicle.length = truckRouteRequestParam.vehicleLength;
            param.vehicle.weight = truckRouteRequestParam.vehicleWeight;
            param.vehicle.size = truckRouteRequestParam.vehicleSize;
            param.vehicle.axis = truckRouteRequestParam.vehicleAxis;
            NaviController.getInstance().setGuideParam(param);
        }

        return routeOption;
    }

    /**
     * 导航重算获取算路请求参数
     */
    public RouteRequestParam getRouteRequestParam(RouteCarResultData mRouteCarResultData) {
        String endPoiName = "终点";
        if (mRouteCarResultData.getToPOI() != null && !TextUtils.isEmpty(mRouteCarResultData.getToPOI().getName())) {
            endPoiName = mRouteCarResultData.getToPOI().getName();
        }
        Location location = SDKManager.getInstance().getLocController().getLastLocation();
        POI startPoi = POIFactory.createPOI("我的位置", new GeoPoint(location.getLongitude(), location.getLatitude()));
        POI endPoi = POIFactory.createPOI(endPoiName, new GeoPoint(mRouteCarResultData.getToPOI().getPoint().getLongitude(), mRouteCarResultData.getToPOI().getPoint().getLatitude()), mRouteCarResultData.getToPOI().getId());
        ArrayList<POI> mMidPois = mRouteCarResultData.getMidPois();
        RouteRequestParam routeRequestParam = new RouteRequestParam(startPoi, endPoi, mMidPois);
        routeRequestParam.invokerType = "navi";
        return routeRequestParam;
    }

    public void registerRouteObserver(IRouteResultCallBack routeResultCallBack) {
        mRouteResultCallBack = routeResultCallBack;
        NaviController.getInstance().registerRouteObserver(mListener);
    }

    public void unRegisterRouteObserver() {
        mRouteResultCallBack = null;
    }

    private ArrayList<RouteAlongServiceAreaInfo> mRouteAlongServiceAreaInfoList;

    private final IRouteServiceAreaObserver mRouteServiceAreaObserver = new IRouteServiceAreaObserver() {
        @Override
        public void onUpdateAlongServiceArea(ArrayList<RouteAlongServiceAreaInfo> list) {
            if (mRouteAlongServiceAreaInfoList == null) {
                mRouteAlongServiceAreaInfoList = new ArrayList<>();
            }
            mRouteAlongServiceAreaInfoList.clear();
            mRouteAlongServiceAreaInfoList.addAll(list);
        }
    };

    public ArrayList<RouteServiceAreaInfo> getRouteAlongServiceArea(long pathID) {
        if (mRouteAlongServiceAreaInfoList == null || mRouteAlongServiceAreaInfoList.isEmpty()) {
            return null;
        }
        RouteAlongServiceAreaInfo routeAlongServiceAreaInfo = null;
        for (int i = 0; i < mRouteAlongServiceAreaInfoList.size(); i++) {
            RouteAlongServiceAreaInfo areaInfo = mRouteAlongServiceAreaInfoList.get(i);
            if (areaInfo.pathID == pathID) {
                routeAlongServiceAreaInfo = areaInfo;
                break;
            }
        }
        if (routeAlongServiceAreaInfo != null) {
            return routeAlongServiceAreaInfo.areaList;
        } else {
            return null;
        }
    }

    public boolean routeRequesting() {
        return !mRouteOptions.isEmpty();
    }

    /**
     * 退出线路规划页面/导航
     */
    public void destroy() {
        if (null != mCarRouteResult) {
            mCarRouteResult.setPathResultDataInfo(null);
        }
        mRouteOptions.clear();
        if (null != mRouteResultCallBack) {
            mRouteResultCallBack = null;
        }
        RouteLifecycleMonitor.getInstance().destoryPathResult();
        mCarRouteResult = null;
        if (mRouteAlongServiceAreaInfoList != null) {
            mRouteAlongServiceAreaInfoList.clear();
        }
        mRouteAlongServiceAreaInfoList = null;
        Timber.i("destroy is called");
    }
}
