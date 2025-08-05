package com.autosdk.bussiness.widget.navi;

import static com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamCamera;
import static com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamCommon;
import static com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamCrossing;
import static com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamElecVehicleCharge;
import static com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamNavi;
import static com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamTR;
import static com.autonavi.gbl.guide.model.guidecontrol.Type.GuideParamVehicle;

import android.location.Location;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.gbl.common.model.CalcRouteResultData;
import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.common.model.ElecInfoConfig;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.common.path.DrivePathDecoder;
import com.autonavi.gbl.common.path.model.POIInfo;
import com.autonavi.gbl.common.path.model.PointType;
import com.autonavi.gbl.common.path.model.RoutePoint;
import com.autonavi.gbl.common.path.model.RoutePoints;
import com.autonavi.gbl.common.path.option.POIForRequest;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.common.path.option.RouteType;
import com.autonavi.gbl.guide.model.CrossImageInfo;
import com.autonavi.gbl.guide.model.NaviPath;
import com.autonavi.gbl.guide.model.NaviType;
import com.autonavi.gbl.guide.model.SceneFlagType;
import com.autonavi.gbl.guide.model.guidecontrol.CameraParam;
import com.autonavi.gbl.guide.model.guidecontrol.CommonParam;
import com.autonavi.gbl.guide.model.guidecontrol.Param;
import com.autonavi.gbl.guide.model.guidecontrol.Type;
import com.autonavi.gbl.layer.model.BizCarType;
import com.autonavi.gbl.layer.model.DynamicLevelType;
import com.autonavi.gbl.layer.model.EagleEyeStyle;
import com.autonavi.gbl.map.layer.model.LayerIconAnchor;
import com.autonavi.gbl.map.layer.model.LayerIconType;
import com.autonavi.gbl.map.layer.model.LayerTexture;
import com.autonavi.gbl.map.model.MsgDataBuildingFocus;
import com.autonavi.gbl.map.model.PreviewParam;
import com.autonavi.gbl.pos.model.LocInfo;
import com.autonavi.gbl.route.model.RouteControlKey;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.POIFactory;
import com.autosdk.bussiness.common.task.TaskManager;
import com.autosdk.bussiness.common.utils.ElectricInfoConverter;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.bussiness.layer.DrivingLayer;
import com.autosdk.bussiness.layer.RouteEndAreaLayer;
import com.autosdk.bussiness.layer.RouteResultLayer;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.manager.PathInfoManager;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.navi.NaviController;
import com.autosdk.bussiness.navi.route.RouteRequestController;
import com.autosdk.bussiness.navi.route.callback.IRouteResultCallBack;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;
import com.autosdk.bussiness.navi.route.model.RouteRequestParam;
import com.autosdk.bussiness.navi.route.utils.RouteLifecycleMonitor;
import com.autosdk.bussiness.widget.route.RouteComponent;
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil;
import com.autosdk.bussiness.widget.setting.SettingComponent;
import com.autosdk.common.AutoConstant;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by AutoSdk on 2021/6/9.
 **/
public class NaviComponent {

    private static final String TAG = NaviComponent.class.getSimpleName();
    // 起点-终点-途经点
    protected POIInfo startInfo = new POIInfo();
    protected POIInfo endInfo = new POIInfo();
    protected POIInfo viaInfo = new POIInfo();
    //路线信息
    protected NaviPath mNaviPath;


    public static NaviComponent mInstance;
    /**
     * 是否正在真实导航
     */
    public static boolean isNaviCompnent = false;

    private ArrayList<POI> mAlongWayPoiList;

    private boolean mIsStartNavi = false;

    private boolean isMultiPreviousRoute = false;

    private boolean isNaviPreviousRoute = false;

    private boolean isNewRoute = false;

    private boolean isNewMultiRoute = false;

    /**
     * 是否设置了引导信息
     */
    private boolean hasSetNaviPath = false;

    private boolean mIsCruise = false;//是否正在巡航

    private final Object setRouteLock = new Object();

    public static NaviComponent getInstance() {
        if (mInstance == null) {
            mInstance = new NaviComponent();
        }
        return mInstance;
    }

    public ArrayList<POI> getmAlongWayPoiList() {
        return mAlongWayPoiList;
    }

    public void setmAlongWayPoiList(ArrayList<POI> mAlongWayPoiList) {
        this.mAlongWayPoiList = mAlongWayPoiList;
    }

    public boolean isStartNavi() {
        return mIsStartNavi;
    }

    public void setIsStartNavi(boolean mIsStartNavi) {
        this.mIsStartNavi = mIsStartNavi;
    }

    public boolean isMultiPreviousRoute() {
        return isMultiPreviousRoute;
    }

    public void setMultiPreviousRoute(boolean multiPreviousRoute) {
        isMultiPreviousRoute = multiPreviousRoute;
    }

    public boolean isHasSetNaviPath() {
        return hasSetNaviPath;
    }

    public void setHasSetNaviPath(boolean hasSetNaviPath) {
        this.hasSetNaviPath = hasSetNaviPath;
    }

    public boolean isNaviPreviousRoute() {
        return isNaviPreviousRoute;
    }

    public void setNaviPreviousRoute(boolean naviPreviousRoute) {
        isNaviPreviousRoute = naviPreviousRoute;
    }

    public boolean ismIsCruise() {
        return mIsCruise;
    }

    public void setmIsCruise(boolean mIsCruise) {
        this.mIsCruise = mIsCruise;
    }

    public boolean isNewRoute() {
        return isNewRoute;
    }

    public void setNewRoute(boolean newRoute) {
        isNewRoute = newRoute;
    }

    public boolean isNewMultiRoute() {
        return isNewMultiRoute;
    }

    public void setNewMultiRoute(boolean newMultiRoute) {
        isNewMultiRoute = newMultiRoute;
    }

    /**
     * 初始化引导导航参数
     */
    public void initGuideParam(String sNCode, boolean isLimit) {
        Param param = new Param();
        param.type = GuideParamNavi;//引导参数配置
        param.navi.naviScene = 0; //普通导航
        if (NaviController.getInstance().isPlayMode(PathInfoManager.getInstance().getRouteCarResultData())) {
            param.navi.model = 1; // 多路线导航（备选路重算需要开启）
        }
        NaviController.getInstance().setGuideParam(param);

        Param tmcParam = new Param();
        tmcParam.type = Type.GuideParamTMC;
        tmcParam.tmc.ETARestriction = isLimit;
        NaviController.getInstance().setGuideParam(tmcParam);
//        Param maneuverParam = new Param();
//        maneuverParam.type = GuideParamManeuverParam;
//        maneuverParam.maneuverParam.enableAutoObtain = true;
//        maneuverParam.maneuverParam.width = 100;
//        maneuverParam.maneuverParam.height = 100;
//        maneuverParam.maneuverParam.backColor = 0xffffffff;
//        maneuverParam.maneuverParam.roadColor = 0xffff0000;
//        maneuverParam.maneuverParam.arrowColor = 0xff00ff00;
//        NaviController.getInstance().setGuideParam(maneuverParam);

        int showRange[][] = new int[][]{{50, 500}, {40, 500}, {30, 200}};
        Param crossParam = new Param();
        crossParam.type = GuideParamCrossing;//放大图配置参数配置
        crossParam.crossing.enable3D = true; //  三维总开关
        crossParam.crossing.enableVectorImage = true; // 矢量图显示开
        crossParam.crossing.enableGridImage = true; // 栅格图显示开关
        crossParam.crossing.isMultiCross = true; // 是否一个路口支持多类型大图透出
        crossParam.crossing.isDayForUseSet = !NightModeGlobal.isNightMode();    // 昼夜模式
        NaviController.getInstance().setGuideParam(crossParam);

        Param camera = new Param();
        camera.type = GuideParamCamera;//摄像头配置参数
        CameraParam cameraParam = camera.camera;
        cameraParam.enable = true;       /* 打开摄像头显示 */
        cameraParam.maxCount = 5;        /* 摄像头显示个数为5个 */
        cameraParam.checkDistance = new int[]{1000, 1000, 500};
        cameraParam.checkDistance[0] = 700;    /* 高速公路 */
        cameraParam.checkDistance[1] = 500;    /* 主要大街、城市快速道 */
        cameraParam.checkDistance[2] = 500;    /* 其他道路 */
        NaviController.getInstance().setGuideParam(camera);

        CommonParam mCommonParam = new CommonParam();//公共参数配置
        mCommonParam.enableAuto = true;
        Param param2 = new Param();
        param2.type = GuideParamCommon;
        param2.common = mCommonParam;
        NaviController.getInstance().setGuideParam(param2);
        NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeySetDiuInfo, sNCode);
        NaviController.getInstance().routeControl(RouteControlKey.RouteControlKeySetInvoker, "navi");

        int mCurrentBroadcastMode = SettingComponent.getInstance().getConfigKeyBroadcastMode();//播报模式。 1：经典简洁播报； 2：新手详细播报，默认
        Timber.d(" initGuideParam mCurrentBroadcastMode:%s", mCurrentBroadcastMode);
        // 配置导航播报开关
        Param ttsParam = new Param();
        ttsParam.type = Type.GuideParamTTSPlay;
        if (mCurrentBroadcastMode == 1) {
            ttsParam.tts.style = 4;//设置导航播报模式，默认2，0无效  2新手播报(详细播报) 3英文播报 4:新简洁播
        } else if (mCurrentBroadcastMode == 3) {
            ttsParam.tts.style = 6;
        } else {
            ttsParam.tts.style = 2; //设置导航播报模式，默认2，0无效  2新手播报(详细播报) 3英文播报 4:新简洁播
        }
        if (NightModeGlobal.isNightMode()) {
            ttsParam.tts.isDay = false;
        }
        ttsParam.tts.enableADCode = true; //打开区域播报
        ttsParam.tts.fatiguedTTS = 2; //关闭疲劳驾驶  设置疲劳驾驶播报选项0:TBT自个控制播报 1 : TBT播报，但播报条件由第三方设置给TBT 2 : TBT完全不播报。默认值为0
        NaviController.getInstance().setGuideParam(ttsParam);

        Param gckVehicleParam = new Param();
        gckVehicleParam.type = Type.GuideParamVehicle;
        gckVehicleParam.vehicle.type = 2;
        NaviController.getInstance().setGuideParam(gckVehicleParam);

        setGuideParam();//参数配置示例,以下配置为实例及其注解
    }

    /**
     * 参数配置示例,以下配置为实例及其注解
     */
    public void setGuideParam() {

        //TR路况配置参数
        Param guideParamTR = new Param();
        guideParamTR.type = GuideParamTR;
        guideParamTR.tr.enable = true; //设置路况播报功能，默认true false：导航巡航均不播报路况、 OnUpdateCruiseFacility不回调 、OnUpdateElecCameraInfo不回调
        guideParamTR.tr.viewOpen = true; // 设置路况显示功能，默认true
        guideParamTR.tr.eventOpen = true; // 设置事件众验功能，默认true
        guideParamTR.tr.socolStatus = false; // 设置socol运行状态，默认 未运行 false
        NaviController.getInstance().setGuideParam(guideParamTR);

        //车辆配置参数
        Param guideParamVehicle = new Param();
        guideParamVehicle.type = GuideParamVehicle;
        //获取云端保存的车牌号
        guideParamVehicle.vehicle.vehicleId = AutoConstant.vehicleId; //设置车牌号
        guideParamVehicle.vehicle.type = 0; // 设置车辆类型，默认0，0:小车，1:货车, 2:纯电动车，3:纯电动货车，4:插电式混动汽车，5:插电式混动货车, 11:摩托车
        guideParamVehicle.vehicle.size = 0; // 设置货车的大小 1-微型货车 2-轻型/小型货车 3-中型货车 4-重型货车
        guideParamVehicle.vehicle.axis = 0; // 设置货车的轴数
        guideParamVehicle.vehicle.width = 0.0F; // 设置货车的轴数
        guideParamVehicle.vehicle.length = 0.0F; // 设置货车的长度 浮点数，单位：米
        guideParamVehicle.vehicle.height = 0.0F; // 设置货车的高度 浮点数，单位：米
        guideParamVehicle.vehicle.load = 0.0F; // 设置货车的最大载重 浮点数，单位：吨
        guideParamVehicle.vehicle.weight = 0.0F; // 设置货车的载重 浮点数，单位：吨
        if (ElectricInfoConverter.isElectric()) {
            ElecInfoConfig elecInfoConfig = ElectricInfoConverter.getElecInfoConfig();
            NaviController.getInstance().getGuideService().setElecInfoConfig(elecInfoConfig);
            Param guideParamElecVehicleCharge = new Param();
            guideParamElecVehicleCharge.type = GuideParamElecVehicleCharge;//新能源当前剩余电量设置
            guideParamElecVehicleCharge.elecVehicle.vehicleCharge = elecInfoConfig.vehicleCharge;
            NaviController.getInstance().setGuideParam(guideParamElecVehicleCharge);
            guideParamVehicle.vehicle.type = 2;
        }
        NaviController.getInstance().setGuideParam(guideParamVehicle);

        //简易三维配置参数 ,注意：需要主图同步设置简易三维
        Param guideParamEasy3d = new Param();
        guideParamEasy3d.type = Type.GuideParamEasy3d;
        guideParamEasy3d.easy3D.enableEasy3dRoute = false; //设置简易三维是否请求三维路线，默认false
        NaviController.getInstance().setGuideParam(guideParamEasy3d);

        //导航设施配置参数
        Param guideParamNaviFacility = new Param();
        guideParamNaviFacility.type = Type.GuideParamNaviFacility;
        NaviController.getInstance().setGuideParam(guideParamNaviFacility);
    }

    /**
     * 终点楼块 , 终点区域高亮
     *
     * @param endPoi 终点POI
     */
    public void hightlightEndArea(final POI endPoi) {
        //开启楼块高亮
        MsgDataBuildingFocus focus = new MsgDataBuildingFocus();
        focus.isHighLight = true;
        focus.mainKey = 50001;
        focus.subKey = 5;
        focus.lat = endPoi.getPoint().getLatitude();
        focus.lon = endPoi.getPoint().getLongitude();
        SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getOperatorBusiness().setHightlightBuilding(focus);
        RouteComponent.getInstance().drawEndPointArea(endPoi, null);
    }

    /**
     * 终点楼块 , 终点区域高亮
     *
     * @param lon
     * @param lat
     */
    public static void removeHightlightEndArea(final double lon, final double lat) {
        //取消楼块高亮
        MsgDataBuildingFocus focus = new MsgDataBuildingFocus();
        focus.isHighLight = false;
        focus.mainKey = 50001;
        focus.subKey = 5;
        focus.lat = lat;
        focus.lon = lon;
        SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getOperatorBusiness().setHightlightBuilding(focus);
        //清除DrivingLayer终点区域图层
        RouteEndAreaLayer routeEndAreaLayer = SDKManager.getInstance().getLayerController().getRouteEndAreaLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (routeEndAreaLayer == null) {
            return;
        }
        routeEndAreaLayer.clearAllRouteEndAreaLayer();
    }

    /**
     * 全览
     *
     * @param pathResult 路线结果集合
     */
    public void showPreview(ArrayList<PathInfo> pathResult) {
        DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        //关闭全览跟随模式
        drivingLayer.setFollowMode(false);
        //设置预览模式
        drivingLayer.setPreviewMode(true);
        drivingLayer.setIsShowPreview(true);
        RectDouble rectDouble = drivingLayer.getPathResultBound(pathResult);
        PreviewParam previewParam = new PreviewParam();
        previewParam.mapBound = rectDouble;
        previewParam.bUseRect = true;
        SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).showPreview(previewParam, true, 500, -1);
    }

    /**
     * 退出全览
     */
    public static void exitPreview() {
        DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        //开启全览跟随模式
        drivingLayer.setFollowMode(true);
        drivingLayer.setPreviewMode(false);
        drivingLayer.setIsShowPreview(false);
        SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).exitPreview(true);
    }

    /**
     * 显示路线充电站
     */
    public static void showChargeView(PathInfo currentPathInfo, boolean isInnerStyle) {
        RouteResultLayer drivingLayer = SDKManager.getInstance().getLayerController().getRouteResultLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        drivingLayer.showChargeStationOnRoute(currentPathInfo, null, isInnerStyle);
    }

    /**
     * 清除路线充电站
     */
    public void clearChargeView() {
        RouteResultLayer drivingLayer = SDKManager.getInstance().getLayerController().getRouteResultLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);

        if (drivingLayer != null) {
            drivingLayer.cleanChargeStationOnRoute();
        }
    }

    public void openFollowModeAndDynamicLevel() {
        DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        //动态比例尺
        drivingLayer.openDynamicLevel(false, DynamicLevelType.DynamicLevelGuide);
    }

    /**
     * 显示鹰眼图
     */
    public void showEagleEye(EagleEyeStyle eaglStyle) {
        DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        drivingLayer.initEagleEye(eaglStyle);
        drivingLayer.setEagleVisible(true);
    }

    /**
     * 鹰眼图显隐控制
     *
     * @param visible true-显示 false-隐藏
     */
    public void setEagleEyeVisible(boolean visible) {
        DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        drivingLayer.setEagleVisible(visible);
    }

    /**
     * 设置走过的途经点置灰
     *
     * @param passGrey 是否走过置灰，默认不置灰：true:置灰， false:不置灰
     */
    public void setViaPassGreyMode(boolean passGrey) {
        DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        Timber.i("setViaPassGreyMode is called passGrey = %s", passGrey);
        drivingLayer.setViaPassGreyMode(passGrey);
    }


    /**
     * 显示路口大图
     *
     * @param nSurfaceViewID 屏幕id
     * @param info           路口大图信息
     */
    public static void showCrossImage(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, CrossImageInfo info) {
        DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        if (info.type == 3 || info.type == 4) {
            //矢量图或者三维图
            drivingLayer.updateCross(info.dataBuf, info.type);
        } else if (info.type == 1) {
            //栅格图
            int RES_ID = 888888888;
            LayerTexture arrowImge = new LayerTexture();
            LayerTexture roadImage = new LayerTexture();
            arrowImge.dataBuff = new BinaryStream(info.arrowDataBuf);
            //栅格图箭头png
            arrowImge.iconType = LayerIconType.LayerIconTypePNG;
            arrowImge.resID = RES_ID;
            arrowImge.isGenMipmaps = false;
            arrowImge.isPreMulAlpha = true;
            arrowImge.isRepeat = false;
            arrowImge.anchorType = LayerIconAnchor.LayerIconAnchorLeftTop;

            roadImage.dataBuff = new BinaryStream(info.dataBuf);
            //栅格图背景图jpg
            roadImage.iconType = LayerIconType.LayerIconTypeJPG;
            roadImage.resID = RES_ID;
            roadImage.isGenMipmaps = false;
            roadImage.isPreMulAlpha = true;
            roadImage.isRepeat = false;
            roadImage.anchorType = LayerIconAnchor.LayerIconAnchorLeftTop;

            drivingLayer.setRasterImageData(arrowImge, roadImage);
        }
    }

    /**
     * 显示路口大图
     */
    public static void showCrossImage(CrossImageInfo info) {
        showCrossImage(SurfaceViewID.SURFACE_VIEW_ID_MAIN, info);
    }

    /**
     * 保存路线文件
     *
     * @param carResultData
     */
    public void saveRouteToFile(RouteCarResultData carResultData) {
        try {
            if (carResultData != null && carResultData.getPathResultDataInfo() != null && carResultData.getPathResultDataInfo().calcRouteResultData != null
                    && carResultData.getPathResultDataInfo().calcRouteResultData.drivePlanData != null) {
                FileUtils.writeToFile(carResultData.getPathResultDataInfo().calcRouteResultData.drivePlanData, AutoConstant.NAVI_PATH_DATA, false);
            }
            if (carResultData != null && carResultData.getPathResultDataInfo() != null && carResultData.getPathResultDataInfo().calcRouteResultData != null
                    && carResultData.getPathResultDataInfo().calcRouteResultData.driveGuideData != null) {
                FileUtils.writeToFile(carResultData.getPathResultDataInfo().calcRouteResultData.driveGuideData, AutoConstant.NAVI_GUIDE_PATH_DATA, false);
            }
        } catch (Exception e) {
            Timber.e("saveRouteToFile e %s", e.getMessage());
            deleteServicePathData();
        }

    }

    /**
     * 删除路线文件
     */
    public void deleteServicePathData() {
        Timber.i("deleteServicePathData is called");
        FileUtils.deleteFile(AutoConstant.NAVI_PATH_DATA);
        FileUtils.deleteFile(AutoConstant.NAVI_GUIDE_PATH_DATA);
    }

    /**
     * 对已保存的路线文件进行解码
     * NAVI_PATH_DATA      路线编码指定存储路径
     * NAVI_GUIDE_PATH_DATA guide编码指定存储路径
     */
    public ArrayList<PathInfo> decodeRouteData() {
        CalcRouteResultData calcRouteResultData = decodeCalcRouteResultData(AutoConstant.NAVI_PATH_DATA, AutoConstant.NAVI_GUIDE_PATH_DATA);
        ArrayList<PathInfo> pathResultsList = DrivePathDecoder.decodeMultiRouteData(calcRouteResultData);
        if (pathResultsList != null && pathResultsList.size() > 0) {
            return pathResultsList;
        }
        //路线还原
        return null;
    }

    /**
     * 对已保存的路线解析获取==PathResult
     */
    public ArrayList<PathInfo> getPathResultData(RouteCarResultData routeCarResultData) {
        if (routeCarResultData == null || routeCarResultData.getPathResultDataInfo() == null)
            return null;
        CalcRouteResultData calcRouteResultData = new CalcRouteResultData();
        calcRouteResultData.drivePlanData = routeCarResultData.getPathResultDataInfo().calcRouteResultData.drivePlanData;
        calcRouteResultData.driveGuideData = routeCarResultData.getPathResultDataInfo().calcRouteResultData.driveGuideData;
        ArrayList<PathInfo> pathResultsList = DrivePathDecoder.decodeMultiRouteData(calcRouteResultData);
        if (pathResultsList != null && pathResultsList.size() > 0) {
            return pathResultsList;
        }
        //路线还原
        return null;
    }

    public static CalcRouteResultData decodeCalcRouteResultData(String saveBinPath, String saveBinGuidePath) {
        //从文件读取路径二进制数据
        byte[] bytes = FileUtils.file2Byte(saveBinPath);
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        byte[] guideBytes = FileUtils.file2Byte(saveBinGuidePath);
        if (guideBytes == null || guideBytes.length == 0) {
            return null;
        }
        CalcRouteResultData calcRouteResultData = new CalcRouteResultData();
        calcRouteResultData.drivePlanData = bytes;
        calcRouteResultData.driveGuideData = guideBytes;
        return calcRouteResultData;
    }

//    /**
//     * 对已保存的路线文件进行解码
//     *
//     */
//    public static  ArrayList<PathInfo> decodeRouteData(byte[] bytes) {
//        //从文件读取路径二进制数据
//        if (bytes==null||bytes.length == 0) {
//            return null;
//        }
//        CalcRouteResultData calcRouteResultData = new CalcRouteResultData();
//        calcRouteResultData.drivePlanData = bytes;
//        ArrayList<PathInfo> pathResultsList = DrivePathDecoder.decodeMultiRouteData(calcRouteResultData);
//        if(pathResultsList != null && pathResultsList.size() > 0) {
//            return pathResultsList;
//        }
//        //路线还原
//        return null;
//    }

    /**
     * 续航设置客户端保存的最后位置，避免拿不到定位信号，无法获取引导信息
     */
    public void setContextPos() {
        Location location = SDKManager.getInstance().getLocController().getLastLocation();
        LocInfo locInfo = new LocInfo();
        try {
            if (location != null) {
                Coord3DDouble gpsPos = new Coord3DDouble(location.getLongitude(),
                        location.getLatitude(),
                        location.getAltitude());
                locInfo.gpsPos = gpsPos;
                locInfo.gpsCourse = location.getBearing();
                Timber.i("LastRouteUtils setContextPos success  %s", location.getBearing());
                LocationController.getInstance().setContextPos(gpsPos, location.getBearing());
            }
        } catch (Exception e) {
            Timber.e(" setLocationPos mLocInfo Exception:%s", e.getMessage());
        }
    }

    public LocInfo getLocationPos() {
        Location location = SDKManager.getInstance().getLocController().getLastLocation();
        LocInfo locInfo = new LocInfo();
        try {
            if (location != null) {
                Coord3DDouble gpsPos = new Coord3DDouble(location.getLongitude(),
                        location.getLatitude(),
                        location.getAltitude());
                locInfo.gpsPos = gpsPos;
                locInfo.gpsCourse = location.getBearing();
            }

        } catch (Exception e) {
            Timber.e(" getLocationPos mLocInfo Exception:%s", e.getMessage());
        }
        return locInfo;
    }

    /**
     * 更新当前路线图层
     *
     * @param result     是否成功原因（1:成功，2:失败,PathID无效,3:失败,因为id和当前主选路线一致）
     *                   通知用户切换主导航路线状态，客户端主动SelectMainPathID切换的回调状态
     * @param focusIndex 图层设置选中路线索引
     */
    public void selectMainPathStatus(int result, int focusIndex) {
        if (result == 1) {
            TaskManager.post(new Runnable() {
                @Override
                public void run() {
                    DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
                    if (drivingLayer == null) {
                        return;
                    }
                    drivingLayer.clearBizRouteTypeArrowLayer();//清除引导路线上的转向图标图层
                    drivingLayer.setSelectedPathIndex(focusIndex);//图层设置选中路线
                    drivingLayer.updatePaths();//路径线图层更新
                    drivingLayer.updateEaglePaths();//鹰眼路径线图层更新
                    DrivingLayer drivingLayer1 = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1);
                    if (drivingLayer1 != null) {
                        drivingLayer1.clearBizRouteTypeArrowLayer();//清除引导路线上的转向图标图层
                        drivingLayer1.setSelectedPathIndex(focusIndex);//图层设置选中路线
                        drivingLayer1.updatePaths();//路径线图层更新
                        drivingLayer1.updateEaglePaths();//鹰眼路径线图层更新
                    }
                }
            });
        }
    }

    /**
     * 导航刷新路线重新规划路线
     *
     * @param routeCarResultData  用于存放路线结果（包含了行程点信息）
     * @param routeResultCallBack 算路结果回调
     */
    public long reRoute(RouteCarResultData routeCarResultData, IRouteResultCallBack routeResultCallBack) {
        //获取当前最新的位置，更换启动重新进行算路
        Location location = LocationController.getInstance().getLastLocation();
        if (location == null) {
            return 0;
        }
        POI startPoi = POIFactory.createPOI("我的位置", new GeoPoint(location.getLongitude(), location.getLatitude()));
        if (routeCarResultData == null) {
            return 0;
        }
        POI endPoi = routeCarResultData.getToPOI();
        ArrayList<POI> midPois = routeCarResultData.getMidPois();
        RouteRequestParam routeRequestParam = new RouteRequestParam(startPoi, endPoi, midPois);
        routeRequestParam.invokerType = "navi";
        routeRequestParam.routeConstrainCode = routeCarResultData.getRouteConstrainCode();
        routeRequestParam.routeStrategy = routeCarResultData.getRouteStrategy();
        routeRequestParam.routeConstrainCode = routeCarResultData.getRouteConstrainCode();
        routeRequestParam.openAvoidLimit = routeCarResultData.isOpenAvoidLimit();
        routeRequestParam.carPlate = routeCarResultData.getCarPlate();
        long requestId = RouteRequestController.getInstance().requestRoute(routeRequestParam, routeResultCallBack);
        return requestId;
    }

    /**
     * 绘制路线
     *
     * @param routeCarResultData 用于存放路线结果（包含了行程点信息）
     * @param pathResult         路线结果
     * @param isHome             是否从首页进入
     * @param naviType           NaviTypeSimulation:模拟导航
     *                           NaviTypeGPS:真实导航
     * @param hasNetwork         是否有网络
     */
    public boolean setRoute(RouteCarResultData routeCarResultData, ArrayList<PathInfo> pathResult, boolean isHome, int naviType, boolean hasNetwork, int type) {
        return setRoute(routeCarResultData, pathResult, isHome, naviType, hasNetwork, false, type);
    }

    /**
     * 绘制路线
     *
     * @param routeCarResultData 用于存放路线结果（包含了行程点信息）
     * @param pathResult         路线结果
     * @param isHome             是否从首页进入(普通续航)
     * @param naviType           NaviTypeSimulation:模拟导航
     *                           NaviTypeGPS:真实导航
     * @param hasNetwork         是否有网络
     * @param continueSapaNavi   是否服务区续航
     */
    public boolean setRoute(RouteCarResultData routeCarResultData, ArrayList<PathInfo> pathResult, boolean isHome, int naviType, boolean hasNetwork, boolean continueSapaNavi, int commandRequestType) {
        synchronized (setRouteLock) {
            if (routeCarResultData == null || pathResult == null || pathResult.isEmpty()) {
                Timber.i("setRoute: routeCarResultData or pathResult is null or empty!");
                //理论上不存在
                return false;
            }
            // 判断PathInfo有效性
            for (PathInfo pathInfo : pathResult) {
                if (pathInfo == null || !pathInfo.isValid()) {
                    Timber.i("setRoute: Invalid pathInfo!");
                    return false;
                }
            }
            long pathCount = pathResult.size();
            Timber.i("onNewRoute: 路线个数 = %s  routeCarResultData.getFocusIndex() = %s", pathCount, routeCarResultData.getFocusIndex());

            mNaviPath = new NaviPath();
            PathInfo pathInfo;
            if (routeCarResultData.getFocusIndex() >= pathCount) {
                //规避异常情况
                pathInfo = pathResult.get(0);
            } else {
                pathInfo = pathResult.get(routeCarResultData.getFocusIndex());
            }

            long mTotalDistance = 0;
            if (pathInfo != null) {
                mTotalDistance = pathInfo.getLength() / 1000;
                //转化为km
            }
            Timber.i("onNewRoute: mTotalDistance = %s", mTotalDistance);
            ArrayList<PathInfo> newpPathResult = new ArrayList<>();
            ArrayList<PathInfo> newPathResultEx1 = new ArrayList<>();
            boolean isVia = false;
            //是否有途经点
            ArrayList<POI> mMidPois = routeCarResultData.getMidPois();
            if (mMidPois != null && !mMidPois.isEmpty()) {
                isVia = true;
            }

            Coord2DDouble startPoint = null;
            Coord2DDouble endPoint = null;

            POI startPoi = routeCarResultData.getFromPOI();
            if (startPoi != null && startPoi.getPoint() != null) {
                startInfo.realPos.lon = startPoi.getPoint().getLongitude();
                startInfo.realPos.lat = startPoi.getPoint().getLatitude();
                startInfo.name = startPoi.getName();
//            startPoint = new Coord2DDouble(startPoi.getPoint().getLongitude(), startPoi.getPoint().getLatitude());
            }

            POI endPoi = routeCarResultData.getToPOI();
            if (endPoi != null && endPoi.getPoint() != null) {
                endInfo.realPos.lon = endPoi.getPoint().getLongitude();
                endInfo.realPos.lat = endPoi.getPoint().getLatitude();
                endInfo.name = endPoi.getName();
//            endPoint = new Coord2DDouble(endPoi.getPoint().getLongitude(), endPoi.getPoint().getLatitude());
            }

            if (mTotalDistance > 100 || naviType == NaviType.NaviTypeSimulation || !hasNetwork || isVia) {
                newpPathResult.add(0, pathInfo);
                newPathResultEx1.add(0, pathInfo);
//            routeCarResultData.setFocusIndex(0);
            } else {
                newpPathResult = pathResult;
                newPathResultEx1.addAll(pathResult);
            }

//        double dis = 0;
//        if (startPoint != null && endPoint != null) {
//            dis = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint);
//        }

            POIForRequest poiForRequest = new POIForRequest();
            if (poiForRequest != null) {
                //设置起点
                poiForRequest.addPoint(PointType.PointTypeStart, startInfo);
                //设置终点
                poiForRequest.addPoint(PointType.PointTypeEnd, endInfo);
                if (isVia) {
                    for (int i = 0; i < mMidPois.size(); i++) {
                        viaInfo.realPos.lon = routeCarResultData.getMidPois().get(i).getPoint().getLongitude();
                        viaInfo.realPos.lat = routeCarResultData.getMidPois().get(i).getPoint().getLatitude();
                        //设置途经点
                        poiForRequest.addPoint(PointType.PointTypeVia, viaInfo);
                    }
                }
            }
            mNaviPath.vecPaths = newpPathResult;
            // 用于引擎偏航时组织终点
            //
            // 信息, 不影响路线绘制
            mNaviPath.point = poiForRequest;
            DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
            DrivingLayer drivingLayer1 = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_EX1);
            if (drivingLayer == null) {
                return false;
            }

            // 是否开启自动比例尺
//        drivingLayer.openDynamicLevel(isDynamicLevel, DynamicLevelType.DynamicLevelGuide);
//        drivingLayer.setFollowMode(true);
//        drivingLayer.setPreviewMode(false);
            drivingLayer.updateCarStyle(BizCarType.BizCarTypeGuide);
            if (drivingLayer1 != null) drivingLayer1.updateCarStyle(BizCarType.BizCarTypeGuide);
            String routePrefer = SettingComponent.getInstance().getConfigKeyPlanPref();
            //设置算路策略
            mNaviPath.strategy = AutoRouteUtil.getNaviStrategy(routePrefer);

            Timber.i(" commandRequestType = %s", commandRequestType);
            //设置算路类型
            if (routeCarResultData.getPathResultDataInfo() != null) {
                int type = routeCarResultData.getPathResultDataInfo().type;
                if (RouteType.RouteTypeAosRoute == type || RouteType.RouteTypeRestoration == type) {
                    mNaviPath.type = RouteType.RouteTypeCommon;
                } else {
                    switch (commandRequestType) {
                        case 5:
                            mNaviPath.type = RouteType.RouteTypeChangeJnyPnt;
                            break;
                        case 6:
                            mNaviPath.type = RouteType.RouteTypeManualRefresh;
                            break;
                        default:
                            mNaviPath.type = type;
                            break;
                    }
                }
            }

            if (isHome) {
                mNaviPath.type = RouteType.RouteTypeCommon;
                //设定续航场景
                mNaviPath.scene = SceneFlagType.SceneFlagTypeOrdinaryContinuation;
                if (continueSapaNavi)
                    mNaviPath.scene = SceneFlagType.SceneFlagTypeServiceAreaContinuation;
            } else {
                mNaviPath.scene = routeCarResultData.getSceneFlagType();
                routeCarResultData.setSceneFlagType(SceneFlagType.SceneFlagTypeNormal);
            }

            mNaviPath.mainIdx = newpPathResult.size() > 1 ? routeCarResultData.getFocusIndex() : 0;

            NaviController.getInstance().setNaviPath(mNaviPath);
            RoutePoints mPathPoints = new RoutePoints();
            mPathPoints.mStartPoints.clear();
            mPathPoints.mViaPoints.clear();
            mPathPoints.mEndPoints.clear();
            mPathPoints.mStartPoints.add(new RoutePoint(true, 0, 0,
                    new Coord3DDouble(startInfo.realPos.lon, startInfo.realPos.lat, 0.0)));
            if (mMidPois != null && mMidPois.size() > 0) {
                for (int i = 0; i < mMidPois.size(); i++) {
                    viaInfo.realPos.lon = routeCarResultData.getMidPois().get(i).getPoint().getLongitude();
                    viaInfo.realPos.lat = routeCarResultData.getMidPois().get(i).getPoint().getLatitude();
                    mPathPoints.mViaPoints.add(new RoutePoint(true, 0, 2,
                            new Coord3DDouble(viaInfo.realPos.lon, viaInfo.realPos.lat, 0.0)));
                }
            }
            mPathPoints.mEndPoints.add(new RoutePoint(true, 0, 1
                    , new Coord3DDouble(endInfo.realPos.lon, endInfo.realPos.lat, 0)));
            drivingLayer.drawRoute(mPathPoints, newpPathResult, newpPathResult.size() > 1 ? routeCarResultData.getFocusIndex() : 0, true, mTotalDistance);
            if (drivingLayer1 != null)
                drivingLayer1.drawRoute(mPathPoints, newPathResultEx1, 0, true, mTotalDistance);
            mPathPoints.mStartPoints.clear();
            mPathPoints.mViaPoints.clear();
            mPathPoints.mEndPoints.clear();
            return true;
        }
    }

    /**
     * 获取路线信息
     *
     * @return mNaviPath 路线信息
     */
    public synchronized NaviPath getNaviPath() {
        return mNaviPath;
    }


    public synchronized void deleteNaviPath() {
        if (mNaviPath != null) {
            mNaviPath = null;
        }
    }


    /**
     * 删除对应id的路线本地缓存 *
     *
     * @param pathIDList
     */
    public synchronized void setDeletePath(ArrayList<Long> pathIDList, RouteCarResultData routeCarResultData, boolean hasNetwork) {
        int size = pathIDList == null ? 0 : pathIDList.size();
        ArrayList<PathInfo> pathResult = RouteLifecycleMonitor.getInstance().getPathResult();
        if (pathResult == null || pathResult.isEmpty()) {
            return;
        }

        //先获取当前正在引导的路线id
        Timber.i("RouteRequestController setDeletePath mPathResult.size() = " + pathResult.size() + ", index = " + routeCarResultData.getFocusIndex());
        PathInfo focusIndexPath = pathResult.get(routeCarResultData.getFocusIndex());
        RouteLifecycleMonitor.getInstance().deletePath(pathIDList);
        ArrayList<PathInfo> vecPaths = RouteLifecycleMonitor.getInstance().getPathResult();
        int index = vecPaths.indexOf(focusIndexPath);
        if (index == -1) {
            index = 0;
        }
        //设置当前路线索引
        routeCarResultData.setFocusIndex(index);
        Timber.i("setDeletePath focusIndex=%s", routeCarResultData.getFocusIndex());
        setRoute(routeCarResultData, RouteLifecycleMonitor.getInstance().getPathResult(), false, NaviType.NaviTypeGPS, hasNetwork, 0);
        SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).updateEaglePaths();
    }

    /**
     * 更新路线
     *
     * @param routeCarResultData 用于存放路线结果（包含了行程点信息）
     * @param pathResult         路线结果
     */
    public void drawPathsPath(RouteCarResultData routeCarResultData, ArrayList<PathInfo> pathResult) {
        if (pathResult == null) {
            return;
        }
        if (pathResult.size() <= 0) {
            return;
        }
        long pathCount = pathResult.size();
        PathInfo pathInfo = null;
        if (routeCarResultData.getFocusIndex() > pathCount) {//规避异常情况
            pathInfo = pathResult.get(0);
        } else {
            pathInfo = pathResult.get(routeCarResultData.getFocusIndex());
        }
        long mTotalDistance = 0;
        if (pathInfo != null) {
            //转化为km
            mTotalDistance = pathInfo.getLength() / 1000;
        }
        ArrayList<POI> mMidPois = routeCarResultData.getMidPois();

        startInfo.realPos.lon = routeCarResultData.getFromPOI().getPoint().getLongitude();
        startInfo.realPos.lat = routeCarResultData.getFromPOI().getPoint().getLatitude();
        endInfo.realPos.lon = routeCarResultData.getToPOI().getPoint().getLongitude();
        endInfo.realPos.lat = routeCarResultData.getToPOI().getPoint().getLatitude();

        RoutePoints mPathPoints = new RoutePoints();
        mPathPoints.mStartPoints.clear();
        mPathPoints.mViaPoints.clear();
        mPathPoints.mEndPoints.clear();
        mPathPoints.mStartPoints.add(new RoutePoint(true, 0, 0,
                new Coord3DDouble(startInfo.realPos.lon, startInfo.realPos.lat, 0.0)));
        if (mMidPois != null && mMidPois.size() > 0) {
            for (int i = 0; i < mMidPois.size(); i++) {
                viaInfo.realPos.lon = routeCarResultData.getMidPois().get(i).getPoint().getLongitude();
                viaInfo.realPos.lat = routeCarResultData.getMidPois().get(i).getPoint().getLatitude();
                mPathPoints.mViaPoints.add(new RoutePoint(true, 0, 2,
                        new Coord3DDouble(viaInfo.realPos.lon, viaInfo.realPos.lat, 0.0)));
            }
        }
        mPathPoints.mEndPoints.add(new RoutePoint(true, 0, 1
                , new Coord3DDouble(endInfo.realPos.lon, endInfo.realPos.lat, 0)));
        DrivingLayer drivingLayer = SDKManager.getInstance().getLayerController().getDrivingLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        if (drivingLayer == null) {
            return;
        }
        drivingLayer.drawRoute(mPathPoints, pathResult, routeCarResultData.getFocusIndex(), true, mTotalDistance);
        mPathPoints.mStartPoints.clear();
        mPathPoints.mViaPoints.clear();
        mPathPoints.mEndPoints.clear();
        drivingLayer.updateEaglePaths();
    }

}
