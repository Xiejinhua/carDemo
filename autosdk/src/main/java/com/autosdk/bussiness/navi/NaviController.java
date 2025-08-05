package com.autosdk.bussiness.navi;

import static com.autonavi.gbl.common.path.model.PointType.PointTypeStart;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeDamagedRoad;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeLimitForbid;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeLimitForbidOffLine;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeLimitLine;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeManualRefresh;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeMutiRouteRequest;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeSwitchMode;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeTMC;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeUpdateCityData;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeVoiceAddViaPoint;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeVoiceChangeDest;
import static com.autonavi.gbl.common.path.option.RouteType.RouteTypeYaw;
import static com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamNavi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.ElecInfoConfig;
import com.autonavi.gbl.common.path.model.ElecVehicleETAInfo;
import com.autonavi.gbl.common.path.model.LightBarItem;
import com.autonavi.gbl.common.path.model.POIInfo;
import com.autonavi.gbl.common.path.model.PointType;
import com.autonavi.gbl.common.path.model.RouteLimitInfo;
import com.autonavi.gbl.common.path.model.TollGateInfo;
import com.autonavi.gbl.common.path.option.CurrentPositionInfo;
import com.autonavi.gbl.common.path.option.POIForRequest;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.common.path.option.RouteConstrainCode;
import com.autonavi.gbl.common.path.option.RouteOption;
import com.autonavi.gbl.guide.GuideService;
import com.autonavi.gbl.guide.model.CrossImageInfo;
import com.autonavi.gbl.guide.model.CruiseCongestionInfo;
import com.autonavi.gbl.guide.model.CruiseEventInfo;
import com.autonavi.gbl.guide.model.CruiseFacilityInfo;
import com.autonavi.gbl.guide.model.CruiseInfo;
import com.autonavi.gbl.guide.model.CruiseTimeAndDist;
import com.autonavi.gbl.guide.model.DriveEventTip;
import com.autonavi.gbl.guide.model.DriveReport;
import com.autonavi.gbl.guide.model.DynamicOperationDisplayEvent;
import com.autonavi.gbl.guide.model.ExitDirectionInfo;
import com.autonavi.gbl.guide.model.ExitDirectionResponseData;
import com.autonavi.gbl.guide.model.FileOperationEvent;
import com.autonavi.gbl.guide.model.LaneInfo;
import com.autonavi.gbl.guide.model.LightBarDetail;
import com.autonavi.gbl.guide.model.LightBarInfo;
import com.autonavi.gbl.guide.model.LockScreenTip;
import com.autonavi.gbl.guide.model.ManeuverConfig;
import com.autonavi.gbl.guide.model.ManeuverIconResponseData;
import com.autonavi.gbl.guide.model.ManeuverInfo;
import com.autonavi.gbl.guide.model.MixForkInfo;
import com.autonavi.gbl.guide.model.NaviCameraExt;
import com.autonavi.gbl.guide.model.NaviCongestionInfo;
import com.autonavi.gbl.guide.model.NaviFacility;
import com.autonavi.gbl.guide.model.NaviGreenWaveCarSpeed;
import com.autonavi.gbl.guide.model.NaviInfo;
import com.autonavi.gbl.guide.model.NaviIntervalCameraDynamicInfo;
import com.autonavi.gbl.guide.model.NaviOddInfo;
import com.autonavi.gbl.guide.model.NaviPath;
import com.autonavi.gbl.guide.model.NaviRoadFacility;
import com.autonavi.gbl.guide.model.NaviType;
import com.autonavi.gbl.guide.model.NaviWeatherInfo;
import com.autonavi.gbl.guide.model.PathTrafficEventInfo;
import com.autonavi.gbl.guide.model.RouteTrafficEventInfo;
import com.autonavi.gbl.guide.model.SAPAInquireResponseData;
import com.autonavi.gbl.guide.model.SocolEventInfo;
import com.autonavi.gbl.guide.model.SoundInfo;
import com.autonavi.gbl.guide.model.SuggestChangePathReason;
import com.autonavi.gbl.guide.model.TMCIncidentReport;
import com.autonavi.gbl.guide.model.TrafficLightCountdown;
import com.autonavi.gbl.guide.model.TrafficSignal;
import com.autonavi.gbl.guide.model.guidecontrol.NaviParam;
import com.autonavi.gbl.guide.model.guidecontrol.Param;
import com.autonavi.gbl.guide.model.guidecontrol.Type;
import com.autonavi.gbl.guide.observer.IContinueGuideInfoObserver;
import com.autonavi.gbl.guide.observer.ICruiseObserver;
import com.autonavi.gbl.guide.observer.INaviObserver;
import com.autonavi.gbl.guide.observer.ISoundPlayObserver;
import com.autonavi.gbl.layer.model.BizLayerUtil;
import com.autonavi.gbl.map.layer.model.LayerItemType;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.route.RouteConsisAdditionService;
import com.autonavi.gbl.route.RouteService;
import com.autonavi.gbl.route.model.BLRerouteRequestInfo;
import com.autonavi.gbl.route.model.ConsisPathBinaryData;
import com.autonavi.gbl.route.model.ConsisPathIdentity;
import com.autonavi.gbl.route.model.PathResultData;
import com.autonavi.gbl.route.model.RouteCollisionSolution;
import com.autonavi.gbl.route.model.RouteInitParam;
import com.autonavi.gbl.route.model.RouteRestorationOption;
import com.autonavi.gbl.route.model.RouteSerialParallelState;
import com.autonavi.gbl.route.model.WeatherLabelItem;
import com.autonavi.gbl.route.observer.INaviRerouteObserver;
import com.autonavi.gbl.route.observer.IRouteConsisAdditionObserver;
import com.autonavi.gbl.route.observer.IRouteResultObserver;
import com.autonavi.gbl.route.observer.IRouteServiceAreaObserver;
import com.autonavi.gbl.route.observer.IRouteWeatherObserver;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.account.AccountController;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.utils.CommonUtil;
import com.autosdk.bussiness.common.utils.ElectricInfoConverter;
import com.autosdk.bussiness.layer.DrivingLayer;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.manager.PathInfoManager;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.navi.constant.NaviConstant;
import com.autosdk.bussiness.navi.route.RouteRequestController;
import com.autosdk.bussiness.navi.route.callback.ISyncRouteCallback;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;
import com.autosdk.bussiness.navi.route.utils.RouteOptionUtil;
import com.autosdk.bussiness.search.utils.NumberUtil;
import com.autosdk.common.storage.MapSharePreference;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

/**
 * * 引导模块M层
 * * <p>
 * * 通过 {@link # getInstance()} 来获取NaviController实例
 * * 通过 {@link #init()} 来初始化tbt,算路及引导服务,及注册BL算路,巡航,引导监听
 * * 通过 {@link #uninit()} 来反初始化tbt,算路及引导服务,各个监听
 * * 通过 {@link # startNavi()} 是否成功进行导航（真实导航、模拟导航、巡航）
 * * 通过 {@link # reRouteOption()} 正常偏航、多备选、躲避拥堵、TMC重算参数配置
 * * 通过 {@link # getTmcItemsInfo()} 光柱信息数据获取
 * * 引导信息INaviObserver回调通过NaviMessenger统一转UI线程
 * * <p>
 * * 注意：反初始化在退出app前调用
 * * 各个注册监听{@link # registerRouteObserver()} 使用完毕后记得反注册 {@link # unregisterRouteObserver()}
 * Created by AutoSdk
 */
public class NaviController implements IRouteResultObserver, ICruiseObserver, INaviObserver, ISoundPlayObserver, IRouteWeatherObserver, INaviRerouteObserver, IRouteConsisAdditionObserver, IContinueGuideInfoObserver {
    private static final String TAG = NaviController.class.getSimpleName();
    private RouteService mRouteService;
    private GuideService mGuideService;
    //route观察者
    private IRouteResultObserver mRouteObserver;
    //多屏route观察者
    private IRouteResultObserver mMultiRouteObserver;
    //Guide观察者
    private List<INaviObserver> mNaviObserverSet;
    //巡航的监听
    private ICruiseObserver mCruiseObserver;

    /**
     * 路线服务
     */
    private RouteConsisAdditionService mRouteConsisAdditionService;
    /**
     * 引导信息回调播报观察者
     */
    private ISoundPlayObserver mSoundPlayObserver;
    public final NaviMessenger mMessenger;
    private int guide = -9999;
    private long route = -9999;
    private long pos = -9999;
    //引导id,唯一标识
    private long mNaviId;
    //引导类型
    private int mGuideType = -1;
    //是否已经开始导航
    private boolean isNaving = false;
    //当前的车辆类型
    private int guideVehicle = 0;
    //是否超速状态
    private boolean isOverSpeed = false;
    //最后一次的道路限速值
    private int mLimitSpeed = -1;

    private INaviRerouteObserver mNaviRerouteObserver;

    //多屏一致性离线算路id
    private int mOfflineReqCustomIdentityId = 0;

    /**
     * 沿途天气回调
     */
    private IRouteWeatherObserver mRouteWeatherObserver;

    private boolean mMultiConsistenceAvailable = false;

    private ISyncRouteCallback mSyncRouteCallback;
    private MapSharePreference preference;

    private IContinueGuideInfoObserver continueGuideInfoObserver;
    //沿途服务区动态运营通知
    private List<IRouteServiceAreaObserver> mRouteServiceAreaObserverSet;

    @Override
    public void onSyncRouteSuccess(long syncReqId, ArrayList<ConsisPathIdentity> identities) {
        Timber.d("onSyncRouteSuccess");
    }

    @Override
    public void onSyncRouteError(long syncReqId, int errorCode) {
        Timber.d("onSyncRouteError errorCode = %s", errorCode);
    }

    @Override
    public void onSyncRouteResult(long syncReqId, String planChannelId, ArrayList<PathInfo> pathInfoList, BinaryStream userData, int errCode) {
        Timber.d("onSyncRouteResult ");
        if (mSyncRouteCallback != null) {
            mSyncRouteCallback.onSyncRouteCallback(syncReqId, planChannelId, pathInfoList, userData, errCode);
        }
    }

    @Override
    public void onSyncRouteOption(RouteOption option) {
        PathInfoManager.getInstance().onSyncRouteOption(option);
    }

    @Override
    public void onGetNaviPath(ArrayList<ConsisPathIdentity> identities, ArrayList<PathInfo> paths) {
        //对比路线数据

        if (SurfaceViewID.isMultiPassenger) {//不是多实例SDK才进行判断
            if (SurfaceViewID.isKldMaster && identities.get(0).planChannelId.equals("MainSdk")) {
                return;
            }
        }
        // 根据多屏回调的参数获取路线数据
        ArrayList<PathInfo> pathList = PathInfoManager.getInstance().restorePathInfoData(identities);
        if (pathList == null) {
            return;
        }


//        RouteCarResultData data = PathInfoManager.getInstance().getRouteCarResultData();
//        if (data == null || data.getPathResult() == null) {
//            return;
//        }
//        boolean isResult = PathInfoManager.getInstance().consisPathIdentity(identities);
//        NaviPath naviPath = NaviComponent.getInstance().getNaviPath();
//        ArrayList<PathInfo> pathList ;
//        if (NaviComponent.getInstance().isMultiPreviousRoute() || NaviComponent.getInstance().isNaviPreviousRoute()){
//            if (!SurfaceViewID.isKldMaster){
//                pathList = PathInfoManager.getInstance().getPreviousRouteCarResultData().getPathResult();
//            } else {
//                if (naviPath == null || naviPath.getVecPaths() == null || naviPath.getVecPaths().size() == 0) {
//                    pathList = PathInfoManager.getInstance().getRouteCarResultData().getPathResult();
//                } else {
//                    if (NaviComponent.isNaviCompnent){
//                        pathList = PathInfoManager.getInstance().getRouteCarResultData().getPathResult();
//                    } else {
//                        pathList = naviPath.getVecPaths();
//                    }
//                }
//            }
//        } else {
//            if (!SurfaceViewID.isKldMaster){
//                if (!NaviComponent.getInstance().isNewMultiRoute()) {
//                    if (naviPath == null || naviPath.getVecPaths() == null || naviPath.getVecPaths().size() == 0) {
//                        pathList = PathInfoManager.getInstance().getRouteCarResultData().getPathResult();
//                    } else {
//                        pathList = naviPath.getVecPaths();
//                    }
//                } else {
//                    pathList = naviPath.getVecPaths();
//                }
//            } else {
//                if (!NaviComponent.getInstance().isNewRoute()) {
//                    if (naviPath == null || naviPath.getVecPaths() == null || naviPath.getVecPaths().size() == 0) {
//                        pathList = PathInfoManager.getInstance().getRouteCarResultData().getPathResult();
//                    } else {
//                        if (NaviComponent.isNaviCompnent){R
//                            pathList = PathInfoManager.getInstance().getRouteCarResultData().getPathResult();
//                        } else {
//                            pathList = naviPath.getVecPaths();
//                        }
//                    }
//                } else {
//                    pathList = naviPath.getVecPaths();
//                }
//            }
//        }


        if (pathList != null && pathList.size() > 0) {
            for (int idIdx = 0; idIdx < identities.size(); idIdx++) {
                ConsisPathIdentity iden = identities.get(idIdx);
                // pathList是HMI自己缓存的路线规划结果
                for (int hmiIdx = 0; hmiIdx < pathList.size(); hmiIdx++) {
                    PathInfo path = pathList.get(hmiIdx);
                    if (iden.isOnline == path.isOnline()) {
                        // 在线路线, 使用pathId判断
                        if (iden.isOnline) {
                            if (iden.pathId == path.getPathID()) {
                                paths.add(path);
                                break;
                            }
                        }
                    }
                }
            }
        }
        Timber.d("onGetNaviPath identities:" + PathInfoManager.getInstance().pathInfoIds3String(identities));
        Timber.d("onGetNaviPath paths:" + PathInfoManager.getInstance().pathInfoIds2String(paths));
    }

    private static class TBTManagerHolder {
        private static NaviController mInstance = new NaviController();
    }

    private NaviController() {
        mMessenger = NaviMessenger.getInstance();
    }

    public static NaviController getInstance() {
        return TBTManagerHolder.mInstance;
    }

    public void setContext(Context context) {
        preference = new MapSharePreference(context, MapSharePreference.SharePreferenceName.userSetting);
    }

    /**
     * 初始化tbt，在UI线程中初始化。
     */
    public boolean init() {
        initTBT();
        return isInitSuccess();
    }

    /**
     * 设置多屏一致性是否开启
     *
     * @param available
     */
    public void setMultiConsistenceAvailable(boolean available) {
        this.mMultiConsistenceAvailable = available;
    }

    public boolean isMultiConsistenceAvailable() {
        return mMultiConsistenceAvailable;
    }

    public String getOfflineReqCustomIdentityId() {
        return mMultiConsistenceAvailable ? "offline_" + (mOfflineReqCustomIdentityId++) : null;
    }

    private void initTBT() {
        initGuide();
        initRoute();
        LocationController.getInstance().bindPosServiceToGuide(mGuideService);
    }

    @SuppressLint("WrongConstant")
    private void initRoute() {
        mRouteService = (RouteService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.RouteSingleServiceID);
        Timber.d("initRouteService: mRouteService = " + mRouteService);
        RouteInitParam routeInitParam = new RouteInitParam();
        routeInitParam.rerouteParam.enableAutoReroute = !mMultiConsistenceAvailable;
        routeInitParam.rerouteParam.enableAutoSwitchParallelReroute = !mMultiConsistenceAvailable;
        routeInitParam.collisionParam.state = RouteSerialParallelState.RouteConcurrent;
        routeInitParam.collisionParam.solution = RouteCollisionSolution.AutoCollision;

        route = mRouteService.init(routeInitParam);

        Timber.d("initRouteService: route = " + route);
        //注册观察者
        mRouteService.addRouteResultObserver(this);
        //注册天气观察者
        mRouteService.addRouteWeatherObserver(this);
        // 注册行中重算观察者
        mRouteService.addRerouteObserver(this);
        mRouteConsisAdditionService = mRouteService.getRouteConsisAdditionService();
        if (mRouteConsisAdditionService != null) {
            //注册多屏一致性算路回调
            mRouteConsisAdditionService.addRouteConsisAdditionObserver(this);
        }
    }

    @SuppressLint("WrongConstant")
    private void initGuide() {
        mGuideService = (GuideService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.GuideSingleServiceID);
        //开启续航开关
        Param param = new Param();
        param.type = Type.GuideParamContinueParam;
        param.continueParam.enableContinue = true; //续航总开关，开启默认打开普通续航
        param.continueParam.enableSapaContinue = true; //服务区续航开关，true则打开服务区续航
        mGuideService.setParam(param);
        guide = mGuideService.init();
        Timber.d("initGuide: " + mGuideService);
        mGuideService.addCruiseObserver(this);
        mGuideService.addNaviObserver(this);
        mGuideService.addSoundPlayObserver(this);
        mGuideService.addContinueGuideInfoObserver(this);//注册服务区续航通知
        Timber.d("initGuideService: guide = " + guide);
    }

    public boolean isInitSuccess() {
        return route == 0 && guide == 0;
    }

    public void uninit() {
        Timber.d("naviManager uninit start...");

        //不再回调任何信息
        clearAllMessages();

        mRouteObserver = null;
        if (mNaviObserverSet != null) {
            mNaviObserverSet.clear();
        }
        mNaviObserverSet = null;
        mCruiseObserver = null;
        mSoundPlayObserver = null;
        mNaviRerouteObserver = null;
        destroyGuide();
        destroyRoute();

        route = -9999;
        guide = -9999;
        pos = -9999;
    }

    public GuideService getGuideService() {
        return mGuideService;
    }

    public RouteService getRouteService() {
        return mRouteService;
    }

    private void destroyRoute() {
        Timber.d("destroyRoute:" + mRouteService + " " + this);
        if (mRouteService != null) {
            mRouteService.removeRouteResultObserver(this);
            //注册天气观察者
            mRouteService.removeRouteWeatherObserver(this);

            mRouteService.removeRerouteObserver(this);
            mRouteService = null;
        }
        if (mRouteConsisAdditionService != null) {
            mRouteConsisAdditionService.removeRouteConsisAdditionObserver(this);
            mRouteConsisAdditionService = null;
        }
    }

    private void destroyGuide() {
        Timber.d("destroyGuide:" + mGuideService + " " + this);
        if (mGuideService != null) {
            mGuideService.removeCruiseObserver(this);
            mGuideService.removeNaviObserver(this);
            mGuideService.removeSoundPlayObserver(this);
            mGuideService.removeContinueGuideInfoObserver(this);
//            ServiceMgr.getServiceMgrInstance()
            mGuideService = null;
        }
    }


    /**
     * 配置引导参数
     *
     * @param param
     * @return
     */
    public boolean setGuideParam(Param param) {
        Timber.d("setGuideParam: " + mGuideService);
        return mGuideService != null && mGuideService.setParam(param);
    }

    public Param getParam(@Type.Type1 int type) {
        Timber.i("getParam type: " + type);
        return mGuideService.getParam(type);
    }

    /**
     * 路线规划参数配置
     *
     * @param key
     * @param value
     */
    public void routeControl(int key, String value) {
        if (isInitSuccess()) {
            mRouteService.control(key, value);
        }
    }


    public boolean isPlayMode(RouteCarResultData routeCarResultData) {
        if (routeCarResultData == null) {
            return true;
        }
        boolean isVia = false;
        //是否有途经点
        ArrayList<POI> mMidPois = routeCarResultData.getMidPois();
        if (mMidPois != null && mMidPois.size() > 0) {
            isVia = true;
        }

        POIInfo startInfo = new POIInfo();
        POIInfo endInfo = new POIInfo();

        Coord2DDouble startPoint = null;
        Coord2DDouble endPoint = null;

        POI startPoi = routeCarResultData.getFromPOI();
        if (startPoi != null && startPoi.getPoint() != null) {
            startInfo.realPos.lon = startPoi.getPoint().getLongitude();
            startInfo.realPos.lat = startPoi.getPoint().getLatitude();
            startPoint = new Coord2DDouble(startPoi.getPoint().getLongitude(), startPoi.getPoint().getLatitude());
        }

        POI endPoi = routeCarResultData.getToPOI();
        if (endPoi != null && endPoi.getPoint() != null) {
            endInfo.realPos.lon = endPoi.getPoint().getLongitude();
            endInfo.realPos.lat = endPoi.getPoint().getLatitude();
            endPoint = new Coord2DDouble(endPoi.getPoint().getLongitude(), endPoi.getPoint().getLatitude());
        }

        double dis = 0;
        if (startPoint != null && endPoint != null) {
            dis = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint);
        }
        if (dis > 50 * 1000 || isVia) {
            return false;
        }
        return true;
    }

    /**
     * 正常偏航、多备选、躲避拥堵、TMC重算参数配置
     *
     * @param mRouteCarResultData
     * @param routeOption
     * @return
     */
    public RouteOption reRouteOption(RouteCarResultData mRouteCarResultData, RouteOption routeOption) {
        // 设置算路参数，根据需求情况赋值
        routeOption.setConstrainCode(mRouteCarResultData.getRouteConstrainCode());
        // 重算时需要再次设置算路策略，算路策略根据偏好设置，一般为上一次客户端设置的路线偏好
        routeOption.setRouteStrategy(mRouteCarResultData.getRouteStrategy());
        // 重算原因
        int routeType = routeOption.getRouteType();
        switch (routeType) {
            //6 道路限行
            case RouteTypeLimitLine:
                //7 道路关闭
            case RouteTypeDamagedRoad:
                //10 更新城市数据引起的重算
            case RouteTypeUpdateCityData:
                //手动刷新
            case RouteTypeManualRefresh:
                // 语音更换目的地
            case RouteTypeVoiceChangeDest:
                //导航中驾驶模式切换（在线电动车专用）
            case RouteTypeSwitchMode:
                //语音追加途经点
            case RouteTypeVoiceAddViaPoint: {
                // 获取算路行程点
                POIForRequest poiForRequest = routeOption.getPOIForRequest();
                // 获取最新的定位信息，更新以下行程点信息
                LocInfo locInfo = LocationController.getInstance().getLocInfo();
                // todo:更新行程点
                // 起点设置
                Location location = SDKManager.getInstance().getLocController().getLastLocation();
                POIInfo startInfo = new POIInfo();
                startInfo.realPos.lon = location.getLongitude();
                startInfo.realPos.lat = location.getLatitude();
                startInfo.type = 0;
                poiForRequest.addPoint(PointTypeStart, startInfo);
                ;//更新起点（一般来源当前定位回调信息locInfo）
                POIInfo endInfo = new POIInfo();
                endInfo.realPos.lon = mRouteCarResultData.getToPOI().getPoint().getLongitude();
                endInfo.realPos.lat = mRouteCarResultData.getToPOI().getPoint().getLatitude();
                endInfo.type = 0;
                //更新终点,如果有变才改
                poiForRequest.addPoint(PointType.PointTypeEnd, endInfo);
                ArrayList<POI> midPois = mRouteCarResultData.getMidPois();
                if (midPois != null && midPois.size() > 0) {
                    for (int i = 0; i < midPois.size(); i++) {
                        POIInfo midInfo = RouteOptionUtil.poiToPOIInfo(midPois.get(i), PointType.PointTypeVia);
                        //要删除已经失效点途经点
                        poiForRequest.addPoint(PointType.PointTypeVia, midInfo);
                    }
                }
                if (locInfo == null) {
                    return null;
                }
                // 起点抓路优化
                poiForRequest.setDirection(locInfo.matchRoadCourse);
                poiForRequest.setReliability(locInfo.courseAcc);
                poiForRequest.setAngleType(locInfo.startDirType);
                poiForRequest.setAngleGps(locInfo.gpsDir);
                poiForRequest.setAngleComp(locInfo.compassDir);
                poiForRequest.setSpeed(locInfo.speed);
                poiForRequest.setLinkType(locInfo.matchInfo.get(0).linkType);
                poiForRequest.setFormWay(locInfo.matchInfo.get(0).formway);
                poiForRequest.setSigType(locInfo.startPosType);
                poiForRequest.setFittingDir(locInfo.fittingCourse);
                poiForRequest.setMatchingDir(locInfo.roadDir);
                poiForRequest.setFittingCredit(locInfo.fittingCourseAcc);
                poiForRequest.setPrecision(locInfo.posAcc);
                // 设置行程点信息
                routeOption.setPOIForRequest(poiForRequest);

                // 当前位置信息
                CurrentPositionInfo positionInfo = routeOption.getCurrentLocation();
                positionInfo.segmentIndex = locInfo.matchInfo.get(0).segmCur;
                positionInfo.linkIndex = locInfo.matchInfo.get(0).linkCur;
                positionInfo.pointIndex = locInfo.matchInfo.get(0).postCur;
                positionInfo.overheadFlag = 0;
                positionInfo.parallelRoadFlag = 0;
                // 设置当前导航位置信息
                routeOption.setCurrentLocation(positionInfo);
                break;
            }
            //偏航重算
            case RouteTypeYaw:
                //5 tmc引起的重算
            case RouteTypeTMC:
                //11 限时禁行引起的重算（在线）
            case RouteTypeLimitForbid:
                //13 限时禁行引起的重算（在线）
            case RouteTypeLimitForbidOffLine:
                //14 导航中请求备选路线
            case RouteTypeMutiRouteRequest:
                // 不需要重新设置行程点信息
                break;
            default:
                break;

        }
        return routeOption;
    }

    /**
     * 光柱信息数据获取
     *
     * @param lightBarInfo 光柱结构
     * @param passedIdx    为lightBar的索引，表示行驶过路段索引
     * @return
     */
    public List<LightBarItem> getTmcItemsInfo(long curPathId, int passedIdx, ArrayList<LightBarInfo> lightBarInfo) {
        if (lightBarInfo == null || lightBarInfo.size() <= 0) {
            return null;
        }
        // 查找属于当前pathid的ItemList
        List<LightBarItem> items = null;
        for (int i = 0; i < lightBarInfo.size(); i++) {
            if (curPathId == lightBarInfo.get(i).pathID) {
                items = lightBarInfo.get(i).itemList;
                break;
            }
        }
        //绘制TmcBar
        //tmcbar的显示跟是否是离线算路有关
        int noPassBarIndex = passedIdx;
        List<LightBarItem> tmcItemsInTmcBar = null;
        if (items != null && (int) noPassBarIndex < items.size()) {
            //计算Tmcbar需要的tmc,截取后半段TmcBarItems
            int size = items.size() - noPassBarIndex;
            tmcItemsInTmcBar = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                tmcItemsInTmcBar.add(items.get(noPassBarIndex + i));
            }
        }
        return tmcItemsInTmcBar;
    }

    /**
     * 获取当前设置的主路线的pathID
     *
     * @param pathWrap
     * @return
     */
    public long getPathId(PathInfo pathWrap) {
        long pathId = NaviConstant.DEFAULT_ERR_CODE;
        if (pathWrap != null) {
            pathId = pathWrap.getPathID();
        }
        return pathId;
    }

    /**
     * 获取路线长度
     *
     * @return
     */
    public long getTotalDistance(int index) {
        if (null != RouteRequestController.getInstance().getCarRouteResult().getPathResult()) {
            ArrayList<PathInfo> pathInfos = RouteRequestController.getInstance().getCarRouteResult().getPathResult();
            Timber.i("getTotalDistance index: %s", index);
            if (null != pathInfos && !pathInfos.isEmpty()) {
                Timber.i("getTotalDistance size: %s", pathInfos.size());
                PathInfo mPathInfo;
                if (pathInfos.size() == 1 || index == -1) {
                    //由于 超过100km 就只添加了一个路线 所以这里只能取0
                    mPathInfo = pathInfos.get(0);
                } else if (index < pathInfos.size()) {
                    mPathInfo = pathInfos.get(index);
                } else {
                    Timber.e("getTotalDistance index error!! use first path!");
                    mPathInfo = pathInfos.get(0);
                }
                if (mPathInfo == null) {
                    return 0;
                }
                return mPathInfo.getLength();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    // ============================ 观察者 ================================

    //RouteObserver
    public void registerRouteObserver(IRouteResultObserver l) {
        mRouteObserver = l;
    }

    public void registerMultiRouteObserver(IRouteResultObserver l) {
        mMultiRouteObserver = l;
    }

    public void unregisterMultiRouteObserver() {
        mMultiRouteObserver = null;
    }

    public void unregisterRouteObserver(IRouteResultObserver l) {
        mRouteObserver = null;
    }


    public IRouteResultObserver getRouteObservers() {
        //开启多屏一致性且非自身发起的算路
        return mRouteObserver == null && mMultiConsistenceAvailable ? mMultiRouteObserver : mRouteObserver;
    }

    /**
     * 添加引导信息回调
     *
     * @param l INaviObserver
     */
    public void registerNaviObserver(INaviObserver l) {
        if (mNaviObserverSet == null) {
            mNaviObserverSet = new CopyOnWriteArrayList<>();
        }
        if (!mNaviObserverSet.contains(l)) {
            mNaviObserverSet.add(l);
        }
    }

    public void unregisterNaviObserver(INaviObserver l) {
        if (mNaviObserverSet != null && mNaviObserverSet.contains(l)) {
            mNaviObserverSet.remove(l);
        }
    }

    public List<INaviObserver> getNaviObserver() {
        return mNaviObserverSet;
    }


    //CruiseObserver
    public void registerCruiseObserver(ICruiseObserver l) {
        mCruiseObserver = l;
    }

    public void registerSyncRouteCallback(ISyncRouteCallback iSyncRouteCallback) {
        mSyncRouteCallback = iSyncRouteCallback;
    }

    public void unRegisterSyncRouteCallback() {
        mSyncRouteCallback = null;
    }

    public void unregisterCruiseObserver(ICruiseObserver l) {
        mCruiseObserver = null;
    }

    public ICruiseObserver getCruiseObserver() {
        return mCruiseObserver;
    }

    // ============================ TBT 方法 ================================

    /**
     * 算路请求
     * 可能只有一条路线
     */
    public long requestTbtRoute(RouteOption routeOption) {
        // -1表示发起算路失败
        long requestId = -1;
        if (null != mRouteService) {
            updateRouteOptionSpecialProperties(routeOption);
            mGuideService.setParam(initGuideNaviParam(routeOption, new Param()));
            requestId = mRouteService.requestRoute(routeOption);
            Timber.d("requestRoute requestIds = %s", requestId);
        }
        return requestId;
    }

    /**
     * 统一在最终算路路口设置电动车参数
     *
     * @param routeOption
     */
    public void updateRouteOptionSpecialProperties(RouteOption routeOption) {
        //新能源算路参数配置
        checkElectricInfo(routeOption);
        //多屏一致性离线算路配置
        checkMultiOfflineRoute(routeOption);
    }

    private void checkMultiOfflineRoute(RouteOption routeOption) {
        int constrainCode = routeOption.getConstrainCode();
        Timber.i("checkMultiOfflineRoute constrainCode = %s", constrainCode);
        if ((constrainCode & RouteConstrainCode.RouteCalcLocal) == RouteConstrainCode.RouteCalcLocal) {
            //离线算路
            String offlineReqCustomIdentityId = getOfflineReqCustomIdentityId();
            if (!TextUtils.isEmpty(offlineReqCustomIdentityId)) {
                //多屏一致性离线算路
                routeOption.setOfflineReqCustomIdentityId(offlineReqCustomIdentityId);
            }
        }

    }

    private void checkElectricInfo(RouteOption routeOption) {
        ElecInfoConfig config = ElectricInfoConverter.getElecInfoConfig();
        if (ElectricInfoConverter.isElectric() && config != null) {
            int constrainCode = routeOption.getConstrainCode();
            //接续算路配置
            if ((constrainCode & RouteConstrainCode.RouteElecContinue) == RouteConstrainCode.RouteElecContinue) {
                Timber.i("checkElectricInfo constrainCode = %s", constrainCode);
                ElectricInfoConverter.setDefaultRouteContinueSetting(config);
            }
            mRouteService.setElecInfoConfig(config);
            mGuideService.setElecInfoConfig(config);
            Timber.i("setVehicleCharge vehicleCharge = %s", config.vehicleCharge);
            routeOption.setVehicleCharge(config.vehicleCharge);
        }
    }


    /**
     * 通知guideService算路失败
     *
     * @param type
     */
    public void notifyRerouteFail(int type) {

        if (null != mGuideService) {
            mGuideService.notifyRerouteFail(type);
        }
    }

    /**
     * 配置导航参数
     *
     * @param param 研发要求需要同步配置constrainCondition和requestRouteType到guideService
     */
    public Param initGuideNaviParam(RouteOption routeOption, Param param) {
        param.type = GuideParamNavi;
        NaviParam naviParam = param.navi;
        naviParam.constrainCondition = routeOption.getConstrainCode();
        naviParam.requestRouteType = routeOption.getRouteType();
        if (NaviController.getInstance().isPlayMode(PathInfoManager.getInstance().getRouteCarResultData())) {
            // 多路线导航
            naviParam.model = 1;
        }
        if (AccountController.getInstance().isLogin()) {
            boolean enableV2x = preference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.enableV2x, true);
            param.navi.v2x.enableCrossMeet = enableV2x; // 弯道会车预警
            param.navi.v2x.enableCurveMeet = enableV2x; // 无灯路口会车预警
            Timber.i(" enableV2x: %s", enableV2x);
        } else {
            param.navi.v2x.enableCrossMeet = false; // 弯道会车预警
            param.navi.v2x.enableCurveMeet = false; // 无灯路口会车预警
        }
        return param;
    }

    /**
     * 盲区会车预警
     *
     * @param enableV2x
     */
    public void openV2x(boolean enableV2x) {
        if (mRouteService == null) return;
        Timber.i(" enableV2x:$enableV2x");
        Param param = mGuideService.getParam(Type.GuideParamNavi);
        if (param == null) {
            Timber.i("getParam GuideParamNavi is null");
            return;
        }
        param.navi.v2x.enableCrossMeet = enableV2x; // 弯道会车预警
        param.navi.v2x.enableCurveMeet = enableV2x; // 无灯路口会车预警
        mGuideService.setParam(param);
    }

    /**
     * 取消路线规划
     */
    public boolean abortRoutePlan(long requestId) {
        if (null != mRouteService) {
            return mRouteService.abortRequest(requestId);
        }
        return false;
    }

    /**
     * 导航路线：主路线、备选路线或主路线+备选路线
     *
     * @param naviPath 导航路线，一条或多条
     *                 设置导航路线
     * @remark 当只有备选路线时，不会更新主路线
     */
    public void setNaviPath(NaviPath naviPath) {
        if (mGuideService == null) {
            return;
        }
        mGuideService.setNaviPath(naviPath);
    }

    /**
     * 切换路线获取当前路线索引
     *
     * @param mRouteCarResultData
     * @param itemType
     * @param id
     * @return
     */
    public Integer getTmpCurSelectIndex(RouteCarResultData mRouteCarResultData, int itemType, String id) {
        int tmpCurSelectIndex = -1;//当前路线索引
        Timber.i("currentPathID %s", mRouteCarResultData.getPathResult().get(mRouteCarResultData.getFocusIndex()).getPathID());
        Timber.i("selectMainPathID %s", id);
        if (LayerItemType.LayerItemPathType == itemType) {
            ArrayList<PathInfo> pathResult = mRouteCarResultData.getPathResult();
            if (pathResult == null) {
                Timber.w("selectMainPathID pathResult is null");
                return -1;  //理论上不存在
            }
            long pathCount = pathResult.size();
            // 点击切换多备选路线
            for (int i = 0; i < pathCount; i++) {
                PathInfo variantPathWrap = pathResult.get(i);
                final long pathId = getPathId(variantPathWrap);
                long curId = NumberUtil.str2Long(id, 10, NaviConstant.DEFAULT_ERR_CODE);
                if (curId == pathId && curId != NaviConstant.DEFAULT_ERR_CODE) {
                    tmpCurSelectIndex = i;
                    Timber.i("selectMainPathID %s", pathId);
                    if (tmpCurSelectIndex != mRouteCarResultData.getFocusIndex()) {
                        selectMainPathID(pathId);//通知引导服务更新主路线
                    }
                    break;
                }
            }
            return tmpCurSelectIndex;
        }
        Timber.w("selectMainPathID not found");
        return -1;
    }

    public void setNaviPathChoose(RouteCarResultData mRouteCarResultData, int index) {
        ArrayList<PathInfo> pathResult = mRouteCarResultData.getPathResult();
        if (pathResult == null) {
            return;
        }
        long pathCount = pathResult.size();
        // 点击切换多备选路线
        for (int i = 0; i < pathCount; i++) {
            if (i == index) {
                PathInfo variantPathWrap = pathResult.get(i);
                if (variantPathWrap != null) {
                    final long pathId = getPathId(variantPathWrap);
                    selectMainPathID(pathId);//通知引导服务更新主路线
                }
                break;
            }
        }
    }

    /**
     * 通知图层更新主路线
     *
     * @param mRouteCarResultData
     * @param curPathId
     */
    public void setChangeNaviPath(RouteCarResultData mRouteCarResultData, long curPathId) {
        ArrayList<PathInfo> pathResult = mRouteCarResultData.getPathResult();
        if (pathResult == null) {
            return;
        }
        long pathCount = pathResult.size();
        // 点击切换多备选路线
        for (int i = 0; i < pathCount; i++) {
            PathInfo pathInfo = pathResult.get(i);
            final long pathId = getPathId(pathInfo);
            Timber.i("setChangeNaviPath pathId = %s", pathId);
            if (curPathId == pathId && curPathId != NaviConstant.DEFAULT_ERR_CODE) {
                SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).setSelectedPathIndex(i);
                DrivingLayer drivingLayerEx1 =
                        SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1);
                if (drivingLayerEx1 != null)
                    drivingLayerEx1.setSelectedPathIndex(i);
                Timber.i("setChangeNaviPath index = %s", i);
                mRouteCarResultData.setFocusIndex(i);
                break;
            }
        }
    }

    /**
     * 语音通知图层更新主路线
     *
     * @param mRouteCarResultData
     * @param curPathId
     */
    public void setVoiceChangeNaviPath(RouteCarResultData mRouteCarResultData, long curPathId) {
        ArrayList<PathInfo> pathResult = mRouteCarResultData.getPathResult();
        if (pathResult == null) {
            return;
        }
        long pathCount = pathResult.size();
        // 点击切换多备选路线
        for (int i = 0; i < pathCount; i++) {
            PathInfo pathInfo = pathResult.get(i);
            final long pathId = getPathId(pathInfo);
            Timber.i("setChangeNaviPath pathId = %s", pathId);
            if (curPathId == pathId && curPathId != NaviConstant.DEFAULT_ERR_CODE) {
                SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).setSelectedPathIndex(i);
                DrivingLayer drivingLayerEx1 =
                        SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1);
                if (drivingLayerEx1 != null)
                    drivingLayerEx1.setSelectedPathIndex(i);
                Timber.i("setChangeNaviPath index = %s", i);
                mRouteCarResultData.setFocusIndex(i);
                selectMainPathID(pathId);//通知引导服务更新主路线
                break;
            }
        }
    }

    /**
     * @param pathID 切换为主路线的id
     *               推荐用户换路后，切换主路线;或者用户手动切换路线
     * @remark 多备选导航
     */
    public void selectMainPathID(long pathID) {
        if (mGuideService == null) {
            return;
        }
        mGuideService.selectMainPathID(pathID);
    }

    /**
     * 是否成功进行导航（真实导航、模拟导航、巡航）
     */
    public boolean startNavi(long naviId, int type) {
        mGuideType = type;
        mNaviId = naviId;
        if (!isNaving) {
            switch (type) {
                //真实导航
                case 0:
                    //模拟导航
                case 1:
                    //巡航
                case 2:
                    isNaving = startNavi(type);
                    break;
                default:
                    break;
            }
        } else {
            Timber.d("不要重复导航");
            return false;
        }
        return isNaving;
    }

    public boolean isNaving() {

        return isNaving;
    }

    public boolean startNavi(int type) {
        if (mGuideService == null) {
            return false;
        }
        if (mGuideService.startNavi(mNaviId, type)) {
            return true;
        }
        return false;
    }

    public boolean stopNavi() {
        Timber.i("NaviController: stopNavi");
        isNaving = false;
//        roadClass = RoadClass.RoadClassNULL;
        //不再回调任何信息
        Timber.e("停止导航___isNaving=%s", isNaving);
        if (mGuideType == NaviType.NaviTypeGPS) {
//            TrackUtils.trackEvent(TrackConstantEnum.END_NAVIGATION_TIME, "" + System.currentTimeMillis());
        }
        mGuideType = -1;
        return mGuideService != null && mGuideService.stopNavi(mNaviId);
    }

    /**
     * 不可在stopNavi中调用，有概率会导致消息onNaviStop消息被清除
     */
    public void clearAllMessages() {
        //不再回调任何信息
        if (mMessenger != null) {
            mMessenger.clearAllMessages();
        }
    }

    public boolean pauseNavi() {
        isNaving = false;
        return mGuideService != null && mGuideService.pauseNavi(mNaviId);
    }

    public boolean resumeNavi() {
        isNaving = true;
        return mGuideService != null && mGuideService.resumeNavi(mNaviId);
    }

    /**
     * 异步获取在线转向图标
     *
     * @param maneuverConfig
     * @return
     */
    public long obtainManeuverIconData(ManeuverConfig maneuverConfig) {
        if (maneuverConfig == null) {
            return -1;
        }
        Timber.d("====maneuverID = %s", maneuverConfig.maneuverID);
        return mGuideService.obtainManeuverIconData(maneuverConfig);
    }

    /**
     * 异步获取出口编号和路牌方向信息(要求当前车辆在高速或城市快速路上)
     *
     * @return
     * @Param isAnyExit   是否查询高速或城快任意出口
     * true  : 查找最近的高/快速“换线”或“下高快速”的出口;
     * false :  查找最近的“下高/快速”出口
     */
    public long obtainExitDirectionInfo(boolean isAnyExit) {
        return mGuideService.obtainExitDirectionInfo(isAnyExit);
    }

    /**
     * 异步获取转向图标信息
     *
     * @return
     * @details 使用者需要主动获取转向图标信息的情况下，通过maneuverConfig获取对应的信息
     * @Param isFindRemainPath
     * true  : 查询剩余路线上的服务区和收费站;
     * false :  只查询当前车辆所在高速路段上的服务区和收费站(要求当前车辆需要在高速上);
     */
    public long obtainSAPAInfo(boolean isFindRemainPath) {
        return mGuideService.obtainSAPAInfo(isFindRemainPath);
    }

    /**
     * 清除缓存getPath
     * 600以上版本，引擎有自动回收pathInfo，但是需要hmi层手动置空
     *
     * @param paths
     */
    public void deletePath(ArrayList<PathInfo> paths) {
        if (paths == null || paths.size() <= 0) {
            return;
        }
        Iterator<PathInfo> it = paths.iterator();
        while (it.hasNext()) {
            PathInfo pathInfo = it.next();
            pathInfo = null;
            it.remove();
        }
        Timber.i("deletePath==%s", paths.size());
    }

    //============================RouteObserver接口实现 start============================//

    /**
     * 算路成功时，通知并发送算路结果
     *
     * @param pathInfo       路径规划结果由客户端自行释放
     * @param routeLimitInfo 附带的路径数据
     */
    @Override
    public void onNewRoute(PathResultData pathResultData, ArrayList<PathInfo> pathInfo, RouteLimitInfo routeLimitInfo) {
        Timber.d("onNewRoute");
        ArrayList<PathInfo> pathInfos = new ArrayList<>();

        for (int i = 0; i < pathInfo.size(); i++) {
            PathInfo pathInfo1 = new PathInfo(pathInfo.get(i));
            pathInfos.add(pathInfo1);
        }
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_NEW_ROUTE);
        Object[] objs = new Object[3];
        objs[0] = pathResultData;
        objs[1] = pathInfos;
        objs[2] = routeLimitInfo;
        msg.obj = objs;
        mMessenger.sendMessage(msg);
    }

    /**
     * 算路失败通知
     * 错误码: pathResultData.errCode
     */
    @Override
    public void onNewRouteError(PathResultData pathResultData, RouteLimitInfo routeLimitInfo) {

        String log = "onNewRouteError: type=" + pathResultData.type
                + ",errorCode=" + pathResultData.errorCode
                + ",isLocal=" + pathResultData.isLocal
                + ",requestId=" + pathResultData.requestId
                + ",mainThreadId=" + CommonUtil.getMainThreadId()
                + ",curThreadId=" + CommonUtil.getcurrentThreadId();
        Timber.d(log);

        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_ERROR_ROUTE);
        Object[] objs = new Object[2];
        objs[0] = pathResultData;
        objs[1] = routeLimitInfo;
        msg.obj = objs;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onReroute(RouteOption rerouteOption) {
//        RouteOption routeOption = RouteOption.create();
//        routeOption.copy(rerouteOption);
//        //重新算路
//        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_REROTE);
//        msg.obj = routeOption;
//        mMessenger.sendMessage(msg);
    }

    //============================ICruiseObserver接口实现 start============================//
    @Override
    public void onUpdateCruiseFacility(ArrayList<CruiseFacilityInfo> facilityInfoList) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_CRUISE_FAC);
        msg.obj = facilityInfoList;
        mMessenger.sendMessage(msg);
    }

    /**
     * 传出自车前方电子眼信息
     *
     * @param cameraInfoList 电子眼信息数组
     */
    @Override
    public void onUpdateElecCameraInfo(ArrayList<CruiseFacilityInfo> cameraInfoList) {
        if (cameraInfoList != null) {
            Timber.d("onUpdateElecCameraInfo cameraInfo =" + cameraInfoList.size());
        }
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATE_ELEC_CAMERA_INFO);
        msg.obj = cameraInfoList;
        mMessenger.sendMessage(msg);
    }

    /**
     * 巡航过程中传出巡航状态下的信息
     * thread mutil
     *
     * @param noNaviInfor 巡航信息
     *                    1、只有GCKAutoFlag开关开启才会回调
     *                    2、需要有离线数据，并且定位匹配到道路
     */
    @Override
    public void onUpdateCruiseInfo(CruiseInfo noNaviInfor) {
        if (noNaviInfor != null) {
            Timber.d("onUpdateCruiseInfo roadName =" + noNaviInfor.roadName);
        }
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATE_CRUISE_INFO);
        msg.obj = noNaviInfor;
        mMessenger.sendMessage(msg);

    }

    /**
     * 巡航过程中传出巡航状态连续行驶的时间和距离
     *
     * @param info 连续行驶的时间和距离信息
     *             1、需开启非导航电子狗开关，通过startNavi(NaviTypeCruise)开启，需与stopNavi配对
     *             2、离线情况下，需要有离线数据
     */
    @Override
    public void onUpdateCruiseTimeAndDist(CruiseTimeAndDist info) {
        String content = "巡航连续行驶信息:\n时间:" + info.driveTime + "\n距离:" + info.driveTime;
        Timber.d(content);
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATE_CRUISE_TIME_AND_DIST);
        msg.obj = info;
        mMessenger.sendMessage(msg);
    }

    /**
     * 巡航过程中传出巡航状态下的拥堵区域信息 BL有内聚
     *
     * @param info 拥堵区域信息
     *             1、需开启非导航电子狗开关，通过startNavi(NaviTypeCruise)开启，需与stopNavi配对
     *             2、在线情况下；
     *             3、目前auto在导航下会用到事件的部分信息，所以导航下也会回到；
     */
    @Override
    public void onUpdateCruiseCongestionInfo(CruiseCongestionInfo info) {
        // 用于接收端更新设置
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_CRUISE_CONGESTION);
        msg.obj = info;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowCruiseLaneInfo(LaneInfo info) { // 显示巡航车道线信息
        Timber.d("onShowCruiseLaneInfo");
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SHOW_CRUISE_LANE_INFO);
        msg.obj = info;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onHideCruiseLaneInfo() {
        Timber.d("onHideCruiseLaneInfo");
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_HIDE_CRUISE_LANE_INFO);
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateCruiseEvent(CruiseEventInfo cruiseEventInfo) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_CRUISEEVENT);
        msg.obj = cruiseEventInfo;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateCruiseSocolEvent(SocolEventInfo socolEventInfo) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_CRUISESOCOL);
        msg.obj = socolEventInfo;
        mMessenger.sendMessage(msg);
    }


    //============================INaviObserver接口实现 start============================//
    @Override
    public void onUpdateNaviInfo(ArrayList<NaviInfo> naviInfoList) {
//        Timber.d("onUpdateNaviInfo ");
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATENAVIINFO);
        msg.obj = naviInfoList;
        mMessenger.sendMessage(msg);
    }

    /**
     * 导航过程中传出出口编号和出口方向信息
     *
     * @param boardInfo 当前导航信息数组
     * @remark 自车在高速和城市快速路并满足一定距离的情况下回调
     */
    @Override
    public void onUpdateExitDirectionInfo(ExitDirectionInfo boardInfo) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATEEXITINFO);
        msg.obj = boardInfo;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowCrossImage(CrossImageInfo info) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SHOWCROSSPIC);
        msg.obj = info;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowNaviCrossTMC(BinaryStream dataBuf) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SHOW_NAVI_CROSS_TMC);
        msg.obj = dataBuf;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onHideCrossImage(int type) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_HIDE_CROSS);
        msg.obj = type;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onPassLast3DSegment() {

    }


    /**
     * 导航过程中传出车道信息
     *
     * @param info 行车引导线信息
     * @remark 导航下，通知自车前方一定距离的行车引导线信息
     * @note thread mutil
     */
    @Override
    public void onShowNaviLaneInfo(LaneInfo info) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_LANEINFO);
        msg.obj = info;
        mMessenger.sendMessage(msg);
    }

    /**
     * 导航过程中通知隐藏车道信息
     *
     * @remark 与onShowNaviLaneInfo配对回调
     * @note thread mutil
     */
    @Override
    public void onHideNaviLaneInfo() {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_HIDELANE);
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowNaviManeuver(ManeuverInfo info) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SHOW_MANEUVER);
        msg.obj = info;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowNaviCameraExt(ArrayList<NaviCameraExt> naviCameraList) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SHOWCAMERA);
        msg.obj = naviCameraList;
        mMessenger.sendMessage(msg);
    }


    /**
     * 更新区间测试电子眼动态实时信息，当cameraDynamic为NULL或者count为0时，清除区间测试电子眼动态实时信息
     *
     * @param cameraDynamicList 区间测试电子眼信息数组
     */
    @Override
    public void onUpdateIntervalCameraDynamicInfo(ArrayList<NaviIntervalCameraDynamicInfo> cameraDynamicList) {
        // 回传回调完整数据
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_INTERVALCAMERADYNAMICINFO);
        msg.obj = cameraDynamicList;
        mMessenger.sendMessage(msg);
    }

    /**
     * 更新服务区信息，当serviceArea为NULL或者count为0时，清除服务区信息
     * 自车在高速上才有通知
     *
     * @param serviceAreaList 服务区信息数组，NaviFacility.sapaDetail 第六位增加充电站显示
     */
    @Override
    public void onUpdateSAPA(ArrayList<NaviFacility> serviceAreaList) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATESAPA);
        msg.obj = serviceAreaList;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onNaviArrive(long id, int naviType) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ARRIVE);
        Bundle bundle = new Bundle();
        bundle.putInt("type", naviType);
        bundle.putLong("id", id);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onNaviStop(long id, int naviType) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_STOPNAVI);
        Bundle bundle = new Bundle();
        bundle.putInt("type", naviType);
        bundle.putLong("id", id);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }


    @Override
    public void onUpdateViaPass(long viaIndex) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATE_VIA_PASS);
        Bundle bundle = new Bundle();
        bundle.putLong("viaIndex", viaIndex);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowLockScreenTip(LockScreenTip tip) {

    }

    @Override
    public void onDriveReport(DriveReport driveReport) {
        Timber.i("onDriveReport");
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_NAVI_REPORT);
        msg.obj = driveReport;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowDriveEventTip(ArrayList<DriveEventTip> list) {
        Timber.i("onShowDriveEventTip");
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_NAVI_DRIVEEVENT);
        msg.obj = list;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onCarOnRouteAgain() {

    }


    @Override
    public void onUpdateTMCLightBar(ArrayList<LightBarInfo> arrayList, LightBarDetail lightBarDetail, long l, boolean b) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATE_BAR);
        Object[] objs = new Object[2];
        objs[0] = arrayList;
        objs[1] = lightBarDetail;
        msg.obj = objs;
        Bundle bundle = new Bundle();
        bundle.putLong("passedIdx", l);
        bundle.putBoolean("dataStatus", b);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);

    }

    @Override
    public void onUpdateTMCCongestionInfo(NaviCongestionInfo info) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_CONGESTION);
        msg.obj = info;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateTREvent(ArrayList<PathTrafficEventInfo> pathsTrafficEventInfo, long pathCount) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATEEVENT);
        msg.obj = pathsTrafficEventInfo;
        Bundle bundle = new Bundle();
        bundle.putLong("act", pathCount);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateTRPlayView(RouteTrafficEventInfo info) {

    }

    @Override
    public void onShowTMCIncidentReport(TMCIncidentReport incident) {

    }

    @Override
    public void onHideTMCIncidentReport(int type) {

    }

    @Override
    public void onUpdateSocolText(String text) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATESOCOL);
        msg.obj = text;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateIsSupportSimple3D(boolean support) {

    }

    /**
     * 多路线导航，其他一条路线偏航的情况下
     * 删除对应id的路线, 如过分歧点，不需要重新设置naviPath给引擎
     * guide根据情况，通知删除id为pathID的路线
     *
     * @param pathIDList 路线id列表
     */
    @Override
    public void onDeletePath(ArrayList<Long> pathIDList) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_DELETE_PATH);
        msg.obj = pathIDList;
        mMessenger.sendMessage(msg);

    }

    /**
     * 通知将id为pathID的备选路线切换为主路线
     *
     * @param pathID
     */
    @Override
    public void onChangeNaviPath(long oldPathID, long pathID) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_CHANGE_NAVIPATH);
        Bundle bundle = new Bundle();
        bundle.putLong("oldPathID", oldPathID);
        bundle.putLong("pathID", pathID);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onMainNaviPath(PathInfo pathInfo) {
        // TODO: 2021/1/6 AR
    }

    /**
     * @param pathID 当前主导航路线id
     * @param result 是否成功原因（1:成功，2:失败,PathID无效,3:失败,因为id和当前主选路线一致）
     *               通知用户切换主导航路线状态，客户端主动SelectMainPathID切换的回调状态
     */
    @Override
    public void onSelectMainPathStatus(long pathID, int result) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SELECT_MAIN_PATH_STATUS_INFO);
        Bundle bundle = new Bundle();
        bundle.putLong("pathID", pathID);
        bundle.putInt("result", result);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    /**
     * 导航过程中通知建议用户切备选路线，TMC更新，备选路线更优。
     * 多路线导航，备选路线的时间比主选路线短的情况下，建议换路
     *
     * @param newPathID 新路线athId
     * @param oldPathID 旧路线PathId
     * @param reason    建议切换的原因
     */
    @Override
    public void onSuggestChangePath(long newPathID, long oldPathID, SuggestChangePathReason reason) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SUGGEST_CHANGE_PATH);
        msg.obj = reason;
        Bundle bundle = new Bundle();
        bundle.putLong("newPathID", newPathID);
        bundle.putLong("oldPathID", oldPathID);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }


    @Override
    public void onObtainManeuverIconData(ManeuverIconResponseData maneuverIconResponseData) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_MANEUVERICON);
        msg.obj = maneuverIconResponseData;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onObtainAdvancedManeuverIconData(ManeuverIconResponseData maneuverIconResponseData) {

    }

    @Override
    public void onObtainExitDirectionInfo(ExitDirectionResponseData exitDirectionResponseData) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_EXIT_DIRECTION);
        msg.obj = exitDirectionResponseData;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onObtainSAPAInfo(SAPAInquireResponseData sapaInquireResponseData) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SAPA_INQUIRE);
        msg.obj = sapaInquireResponseData;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowSameDirectionMixForkInfo(ArrayList<MixForkInfo> list) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SHOWMIXINFO);
        msg.obj = list;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onShowNaviWeather(ArrayList<NaviWeatherInfo> list) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_NAVIWEATHER);
        msg.obj = list;
        mMessenger.sendMessage(msg);
    }


    @Override
    public void onShowNaviFacility(ArrayList<NaviRoadFacility> list) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_NAVIFACILITY);
        msg.obj = list;
        mMessenger.sendMessage(msg);
    }

    /**
     * 用于骑行步导场景下透出一定距离范围内的收费站车道信息(距离默认高速1KM，快速500m，其他300米)
     *
     * @param tollGateInfo 收费站车道信息，为空代表关闭
     */
    @Override
    public void onShowTollGateLane(TollGateInfo tollGateInfo) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_GATELANE);
        msg.obj = tollGateInfo;
        mMessenger.sendMessage(msg);
    }

    /**
     * 传出红路灯交通信号信息。包括红绿灯的状态及对应的预计结束时间，红绿灯经纬度坐标等。
     *
     * @param arrayList 红绿灯的数据
     */
    @Override
    public void onUpdateTrafficSignalInfo(ArrayList<TrafficSignal> arrayList) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_TRAFFICSIGNALINFO);
        msg.obj = arrayList;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateTrafficLightCountdown(ArrayList<TrafficLightCountdown> list) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_TRAFFIC_LIGHT_COUNTDOWN);
        msg.obj = list;
        mMessenger.sendMessage(msg);
    }

    /**
     * 透出电动车ETA信息，仅在线支持。
     * <p>
     * //     * @param arrayList 新能源ETA信息
     */
    @Override
    public void onUpdateElecVehicleETAInfo(ArrayList<ElecVehicleETAInfo> arrayList) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_VEHICLEETAINFO);
        msg.obj = arrayList;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onCurrentRoadSpeed(int speed) {
        mLimitSpeed = speed;
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_SPEED);
        Bundle bundle = new Bundle();
        bundle.putInt("speed", speed);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateNaviSocolEvent(SocolEventInfo socolEventInfo) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_NAVISOCOL);
        msg.obj = socolEventInfo;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateChargeStationPass(long l) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_ON_UPDATE_CHARGE_STATION_PASS);
        msg.obj = l;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onUpdateDynamicOperationDisplayEvent(DynamicOperationDisplayEvent dynamicOperationDisplayEvent) {

    }

    @Override
    public void onQueryAppointLanesInfo(long l, ArrayList<LaneInfo> arrayList) {

    }

    @Override
    public void onFileOperationNotify(FileOperationEvent fileOperationEvent) {

    }

    @Override
    public void onUpdateNaviOddInfo(NaviOddInfo naviOddInfo) {

    }

    @Override
    public void onUpdateGreenWaveCarSpeed(ArrayList<NaviGreenWaveCarSpeed> list) {
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_GREEN_WAVE);
        msg.obj = list;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onPlayTTS(SoundInfo pInfo) {
        if (pInfo == null) {
            Timber.i("NaviManager-onPlayTTS: SoundInfo==null");
            return;
        }
        String str = pInfo.text;
        if (TextUtils.isEmpty(str)) {
            Timber.i("NaviManager-onPlayTTS: str==null");
            return;
        }
        Timber.i("NaviManager-onPlayTTS: str = %s", str);
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_TTS_PLAYING);
        msg.obj = pInfo;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onPlayRing(int type) {
        Timber.d("onPlayRing, type:%s", type);
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_RING_PLAYING);
        Bundle bundle = new Bundle();
        bundle.putInt("type", type);
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    @Override
    public boolean isPlaying() {
        if (mSoundPlayObserver != null) {
            return mSoundPlayObserver.isPlaying();
        }
        return false;
    }

    /**
     * 注册引导播报观察者
     *
     * @param l
     */
    public void registerTbtSoundPlayObserver(ISoundPlayObserver l) {
        mSoundPlayObserver = l;
    }

    /**
     * 反注册引导播报观察者
     *
     * @param l
     */
    public void unregisterTbtSoundPlayObserver(ISoundPlayObserver l) {
        mSoundPlayObserver = null;
    }

    /**
     * 获取引导播报观察者列表
     *
     * @return
     */
    protected ISoundPlayObserver getTbtSoundPlayOberver() {
        return mSoundPlayObserver;
    }

    @Override
    public void onWeatherUpdated(long requestId, ArrayList<WeatherLabelItem> arrayList) {
        Timber.d("onWeatherUpdated, requestId:%s", requestId);
        Message msg = mMessenger.newMessage(NaviConstant.HANDLER_WEATHER);
        Bundle bundle = new Bundle();
        bundle.putLong("requestId", requestId);
        msg.obj = arrayList;
        msg.setData(bundle);
        mMessenger.sendMessage(msg);
    }

    /**
     * 注册沿途天气回调
     *
     * @param l
     */
    public void registerRouteWeatherObserver(IRouteWeatherObserver l) {
        mRouteWeatherObserver = l;
    }

    /**
     * 反注册沿途天气回调
     *
     * @param l
     */
    public void unregisterRouteWeatherObserver(IRouteWeatherObserver l) {
        mRouteWeatherObserver = null;
    }

    /**
     * 获取沿途天气回调对象
     *
     * @return
     */
    protected IRouteWeatherObserver getRouteWeatherObserver() {
        return mRouteWeatherObserver;
    }

    /**
     * 请求沿途天气
     *
     * @param path
     * @return
     */
    public long requestPathWeather(PathInfo path) {
        if (mRouteService == null) {
            return -1;
        }
        return mRouteService.requestPathWeather(path);
    }

    public void registerNaviRerouteObserver(INaviRerouteObserver l) {
        mNaviRerouteObserver = l;
    }

    public void unregisterNaviRerouteObserver(INaviRerouteObserver l) {
        mNaviRerouteObserver = null;
    }

    public INaviRerouteObserver getNaviRerouteObserver() {
        return mNaviRerouteObserver;
    }


    @Override
    public void onModifyRerouteOption(RouteOption routeOption) {
        //有对接了行中自动重算，需在onModifyRerouteOption时重新设置当前电量
        if ((routeOption.getConstrainCode() & RouteConstrainCode.RouteElecContinue) == RouteConstrainCode.RouteElecContinue) {
            Timber.i("setVehicleCharge onModifyRerouteOption");
            updateRouteOptionSpecialProperties(routeOption);
        }
        Message msg = mMessenger.newMessage(NaviConstant.ON_MODIFY_REROUTE_OPTION);
        msg.obj = routeOption;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onRerouteInfo(BLRerouteRequestInfo blRerouteRequestInfo) {
        RouteOption reRouteOption = new RouteOption(blRerouteRequestInfo.option);

        BLRerouteRequestInfo rerouteRequestInfo = new BLRerouteRequestInfo();
        rerouteRequestInfo.requestId = blRerouteRequestInfo.requestId;
        rerouteRequestInfo.errCode = blRerouteRequestInfo.errCode;
        rerouteRequestInfo.option = reRouteOption;
        Message msg = mMessenger.newMessage(NaviConstant.ON_REROUTE_INFO);
        msg.obj = rerouteRequestInfo;
        mMessenger.sendMessage(msg);
    }

    @Override
    public void onSwitchParallelRoadRerouteInfo(BLRerouteRequestInfo blRerouteRequestInfo) {
        RouteOption reRouteOption = new RouteOption(blRerouteRequestInfo.option);

        BLRerouteRequestInfo rerouteRequestInfo = new BLRerouteRequestInfo();
        rerouteRequestInfo.requestId = blRerouteRequestInfo.requestId;
        rerouteRequestInfo.errCode = blRerouteRequestInfo.errCode;
        rerouteRequestInfo.option = reRouteOption;
        Message msg = mMessenger.newMessage(NaviConstant.ON_SWITCHPARALLELROADREROUTE_INFO);
        msg.obj = rerouteRequestInfo;
        mMessenger.sendMessage(msg);
    }

    /**
     * 同步路线数据至副屏
     *
     * @param data     路线数据
     * @param userData 自定义数据
     */
    public void syncOnlinePathToMultiSource(ConsisPathBinaryData data, BinaryStream userData) {
        if (mRouteConsisAdditionService != null) {
            mRouteConsisAdditionService.syncOnlinePathToMultiSource(data, userData, 10 * 1000);
        }
    }

    public int getGuideVehicle() {
        return guideVehicle;
    }

    public void setGuideVehicle(int guideVehicle) {
        this.guideVehicle = guideVehicle;
    }

    public void setOverSpeed(boolean isOverSpeed) {
        this.isOverSpeed = isOverSpeed;
    }

    public boolean getIsOverSpeed() {
        return isOverSpeed;
    }

    public int getmLimitSpeed() {
        return mLimitSpeed;
    }

    /**
     * 请求路线还原
     *
     * @param routeRestorationOption 路线还原算路参数
     * @param option                 引擎引导参数
     * @return
     */
    public long requestRouteRestoration(RouteRestorationOption routeRestorationOption, RouteOption option) {
        // -1表示发起算路失败
        long requestId = -1;

        if (null != mRouteService && null != mGuideService) {
            updateRouteOptionSpecialProperties(option);
            mGuideService.setParam(initGuideNaviParam(option, new Param()));
            requestId = mRouteService.requestRouteRestoration(routeRestorationOption);
            Timber.d("requestRoute requestIds = %s", requestId);
        }
        return requestId;
    }

    @Override
    public void continueGuideStartNotify() {
        if (continueGuideInfoObserver != null)
            continueGuideInfoObserver.continueGuideStartNotify();
    }

    @Override
    public void exitContinueGuideNotify() {
        if (continueGuideInfoObserver != null)
            continueGuideInfoObserver.exitContinueGuideNotify();
    }

    public void registerContinueGuideInfoObserver(IContinueGuideInfoObserver l) {
        continueGuideInfoObserver = l;
    }

    public void unregisterContinueGuideInfoObserver(IContinueGuideInfoObserver l) {
        continueGuideInfoObserver = null;
    }


    /**
     * 添加路线沿途服务区信息回调（动态运营能力返回）
     *
     * @param l INaviObserver
     */
    public void registerAlongServiceAreaObserver(IRouteServiceAreaObserver l) {
        if (mRouteServiceAreaObserverSet == null) {
            mRouteServiceAreaObserverSet = new CopyOnWriteArrayList<>();
        }
        if (!mRouteServiceAreaObserverSet.contains(l)) {
            mRouteServiceAreaObserverSet.add(l);
        }
    }

    /**
     * 反注册路线沿途服务区信息回调（动态运营能力返回）
     *
     * @param l
     */
    public void unregisterAlongServiceAreaObserver(IRouteServiceAreaObserver l) {
        if (mRouteServiceAreaObserverSet != null) {
            mRouteServiceAreaObserverSet.remove(l);
        }
    }

    public List<IRouteServiceAreaObserver> getAlongServiceAreaObserver() {
        return mRouteServiceAreaObserverSet;
    }

}
