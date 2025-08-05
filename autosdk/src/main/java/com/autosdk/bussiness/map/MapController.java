package com.autosdk.bussiness.map;

import static com.autonavi.gbl.map.layer.model.OpenLayerID.OpenLayerIDRouteTraffic;
import static com.autosdk.bussiness.map.SurfaceViewID.SURFACE_VIEW_ID_EX3;
import static com.autosdk.bussiness.map.SurfaceViewID.SURFACE_VIEW_ID_INVALID;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.gbl.map.MapDevice;
import com.autonavi.gbl.map.MapService;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.adapter.MapHelper;
import com.autonavi.gbl.map.adapter.MapSurfaceView;
import com.autonavi.gbl.map.layer.model.OpenLayerID;
import com.autonavi.gbl.map.model.DeviceAttribute;
import com.autonavi.gbl.map.model.EGLDeviceID;
import com.autonavi.gbl.map.model.InitMapParam;
import com.autonavi.gbl.map.model.MapControllerStatesType;
import com.autonavi.gbl.map.model.MapEngineID;
import com.autonavi.gbl.map.model.MapFontInfo;
import com.autonavi.gbl.map.model.MapModelDtoConstants;
import com.autonavi.gbl.map.model.MapParameter;
import com.autonavi.gbl.map.model.MapPositionParam;
import com.autonavi.gbl.map.model.MapResourceParam;
import com.autonavi.gbl.map.model.MapSkyboxParam;
import com.autonavi.gbl.map.model.MapStyleMode;
import com.autonavi.gbl.map.model.MapStyleParam;
import com.autonavi.gbl.map.model.MapStyleTime;
import com.autonavi.gbl.map.model.MapViewParam;
import com.autonavi.gbl.map.model.MapViewPortParam;
import com.autonavi.gbl.map.model.MapViewStateType;
import com.autonavi.gbl.map.model.MapZoomScaleMode;
import com.autonavi.gbl.map.model.MapviewMode;
import com.autonavi.gbl.map.model.MapviewModeParam;
import com.autonavi.gbl.map.observer.IBLMapViewProxy;
import com.autonavi.gbl.map.observer.IDeviceObserver;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.NetworkStatus;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.BuildConfig;
import com.autosdk.bussiness.map.Observer.DeviceObserver;
import com.autosdk.bussiness.map.Observer.MapEventObserver;
import com.autosdk.bussiness.map.Observer.MapGestureObserver;
import com.autosdk.bussiness.map.Observer.MapViewObserver;
import com.autosdk.bussiness.map.Observer.TextTextureObserver;
import com.autosdk.bussiness.widget.setting.SettingConst;
import com.autosdk.common.AutoConstant;
import com.autosdk.common.utils.DayStatusSystemUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * M层主图渲染控制器
 */
public class MapController {

    /**
     * 日志输出TAG
     */
    private static String TAG = MapController.class.getSimpleName();

    /**
     * SDK地图渲染服务
     */
    private MapService mapService;

    /**
     * 屏幕视图最大数
     */
    public static int SURFACEVIEW_MAX_COUNT = SURFACE_VIEW_ID_EX3;

    /**
     * 程序运行环境上下文信息内容
     */
    private Context mContext;

    /**
     * 多SDK多实例模式
     */
    public static final int MULTISCREEN_TYPE_MULTI_SDK = 2;

    /**
     * 私有构造函数
     */
    private MapController() {
    }

    /**
     * 控制器单例
     */
    private static class MapCtrlHolder {
        private static MapController mInstance = new MapController();
    }

    /**
     * 控制器单例获取
     */
    public static MapController getInstance() {
        return MapController.MapCtrlHolder.mInstance;
    }

    /**
     * 屏幕视图队列
     */
    private ArrayList<MapSurfaceView> mapSurfaceViewArrayList = new ArrayList<MapSurfaceView>();
    /**
     * 屏幕视图操作观察者队列
     */
    private ArrayList<MapViewObserver> mapViewObserverArrayList = new ArrayList<MapViewObserver>();
    /**
     * 屏幕视图手势操作观察者队列
     */
    private ArrayList<MapGestureObserver> mapGestureObserverArrayList = new ArrayList<MapGestureObserver>();
    /**
     * 屏幕视图字体变化观察者队列
     */
    private ArrayList<TextTextureObserver> textTextureObserverArrayList = new ArrayList<TextTextureObserver>();
    /**
     * 设备变化观察者队列
     */
    private ArrayList<DeviceObserver> deviceObserverArrayList = new ArrayList<>();

    public void init(Context context, String strDataPath) {
        mContext = context;
        // 获取渲染服务
        if (null == mapService) {
            mapService = (MapService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.MapSingleServiceID);
        }
        if (null == mapService) {
            Timber.e(new NullPointerException(), "SDK mapService is null");
            return;
        }
        // 初始化渲染服务
        InitMapParam mapParam = new InitMapParam();
        mapParam.dataPath = strDataPath + "/data/"; //AutoConstant.PATH + "/data/";
        mapParam.basePath = "http://mps.amap.com:80/";
        mapParam.assetPath = strDataPath + "/MapAsset/";
        String path = mapParam.dataPath + "mapcache/vmap4res/deviceprofile.data";
        mapParam.configBuffer = getConfigBuffer(path);

        MapFontInfo mapFontInfo = new MapFontInfo();
        mapFontInfo.fontName = "font_cn";
        mapFontInfo.fontPath = AutoConstant.FONT_DIR + "font_cn.ttf";
        mapParam.fontParam.overlayFontInfoList.add(mapFontInfo);
        //设置AmapNumber-Bold路径
        MapFontInfo mapFontInfo2 = new MapFontInfo();
        mapFontInfo2.fontName = "AmapNumber-Bold";
        mapFontInfo2.fontPath = AutoConstant.FONT_DIR + "AmapNumber-Bold.ttf";
        mapParam.fontParam.overlayFontInfoList.add(mapFontInfo2);
        //设置Oswald-Regular路径
        MapFontInfo mapFontInfo3 = new MapFontInfo();
        mapFontInfo3.fontName = "Oswald-Regular";
        mapFontInfo3.fontPath = AutoConstant.FONT_DIR + "Oswald-Regular.ttf";
        mapParam.fontParam.overlayFontInfoList.add(mapFontInfo3);
        //设置Roboto-Bold路径
        MapFontInfo mapFontInfo4 = new MapFontInfo();
        mapFontInfo4.fontName = "Roboto-Bold";
        mapFontInfo4.fontPath = AutoConstant.FONT_DIR + "Roboto-Bold.ttf";
        mapParam.fontParam.overlayFontInfoList.add(mapFontInfo4);
        int ret = mapService.initMap(mapParam);
        Timber.d(TAG, "initMap: ret = " + ret);
    }

    /**
     * 初始化接口入口
     */
    public ArrayList<Integer> init(MapControllerInitParam mapCtrlInitParam, int mayNightMode) {

        mContext = mapCtrlInitParam.mContext;

        // 获取渲染服务
        if (null == mapService) {
            mapService = (MapService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.MapSingleServiceID);
        }

        if (null == mapService) {
            Timber.e(new NullPointerException(), "SDK mapService is null");
            return null;
        }

        if (null == mapCtrlInitParam.mSurfaceViewParamArrayList) {
            Timber.e(new NullPointerException(), "SDK surfaceViewParamsArrayList is null");
            return null;
        }

        if (mapCtrlInitParam.mSurfaceViewParamArrayList.isEmpty()) {
            Timber.e(new Exception(), "SDK surfaceViewParamsArrayList is empty");
            return null;
        }

        // 初始化渲染服务
        /*前面 init 已经初始化过了
        InitMapParam mapParam = new InitMapParam();
        mapParam.dataPath = mapCtrlInitParam.mStrDataPath + "/data/";
        mapParam.basePath = "http://mps.amap.com:80/";
        mapParam.assetPath = mapCtrlInitParam.mStrDataPath + "/MapAsset/";
        String path = mapParam.dataPath + "mapcache/vmap4res/deviceprofile.data";
        mapParam.configBuffer = getConfigBuffer(path);
        int ret = mapService.initMap(mapParam);

        Timber.d("initMap: ret = " + ret);*/

        ArrayList<Integer> surfaceViewIDList = new ArrayList<Integer>();
        deviceObserverArrayList.clear();
        int size = mapCtrlInitParam.mSurfaceViewParamArrayList.size();
        for (int i = 0; i < size && i < SURFACEVIEW_MAX_COUNT; i++) {

            int mapSurfaceId = addSurfaceView(mapCtrlInitParam.mSurfaceViewParamArrayList.get(i), mayNightMode);
            if (SURFACE_VIEW_ID_INVALID != mapSurfaceId) {
                surfaceViewIDList.add(mapSurfaceId);
            }
        }

        return surfaceViewIDList;
    }

    /**
     * 添加屏幕视图
     */
    private @SurfaceViewID.SurfaceViewID1 int addSurfaceView(SurfaceViewParam params, int mayNightMode) {

        if (null == params) {
            Timber.e(new NullPointerException(), "input param glMapSurface is null");
            return SURFACE_VIEW_ID_INVALID;

        }

        if (null == params.glMapSurface) {
            Timber.e(new NullPointerException(), "input param glMapSurface is null");
            return SURFACE_VIEW_ID_INVALID;
        }

        int size = mapSurfaceViewArrayList.size();

        if (SURFACEVIEW_MAX_COUNT <= size) {
            Timber.e(new NullPointerException(), "mapSurfaceView count is full");
            return SURFACE_VIEW_ID_INVALID;
        }


        int nDeviceId = size;

        if (nDeviceId < EGLDeviceID.EGLDeviceIDDefault || EGLDeviceID.EGLDeviceIDCOUNT <= nDeviceId) {
            Timber.e(new Exception(), "mapDevice count is full");
            return SURFACE_VIEW_ID_INVALID;
        }

        MapDevice mapDevice = createDevice(nDeviceId);

        if (null == mapDevice) {
            Timber.e(new Exception(), "createDevice is failed");
            return SURFACE_VIEW_ID_INVALID;
        }

        int nEngineID = size * 2 + 1;

        if (nEngineID < MapEngineID.MapEngineIdMain || MapEngineID.MapEngineIdEx3EagleEye < nEngineID) {
            Timber.e(new Exception(), "mapDevice count is full");
            return SURFACE_VIEW_ID_INVALID;
        }

        MapView mapView = createMapView(mapDevice, nEngineID, params, mayNightMode);

        if (null == mapView) {
            Timber.e(new Exception(), "createMapView is failed");
            return SURFACE_VIEW_ID_INVALID;
        }

        DeviceObserver deviceObserver = new DeviceObserver(mapDevice, mapView);
        mapDevice.addDeviceObserver(deviceObserver);
        deviceObserverArrayList.add(deviceObserver);

        //将地图资源设置给View
        params.glMapSurface.setMapService(mapService);
        params.glMapSurface.setDefaultDevice(mapDevice);
        params.glMapSurface.setDefaultMapView(mapView);
        updateMapSurfaceViewInitColor(getFirstTimeNightMode(mayNightMode));
        params.glMapSurface.getGLSurfaceAttribute().isNeedInitDraw = false;
        mapSurfaceViewArrayList.add(params.glMapSurface);

        /** mapSurfaceId 和 DeviceId的关系*/
        int mapSurfaceId = nDeviceId + 1;

        return mapSurfaceId;
    }

    /**
     * 创建地图引擎虚拟设备
     */
    private MapDevice createDevice(int nDeviceID) {
        if (null == mapService) {
            Timber.e(new NullPointerException(), "SDK mapService is null");
            return null;
        }

        DeviceAttribute devAttribute = new DeviceAttribute();
        Timber.i("nDeviceID=%s", nDeviceID);
        if (nDeviceID == EGLDeviceID.EGLDeviceIDDefault) {
            //主屏 渲染模式
            //devAttribute.deviceWorkMode = EGLDeviceWorkMode.EGLDeviceWorkMode_WithThreadWithEGLContextDrawIn;
        } else {
            //扩展屏 渲染模式
            devAttribute.uiTaskDeviceId = nDeviceID;
            //devAttribute.deviceWorkMode = EGLDeviceWorkMode.EGLDeviceWorkMode_WithThreadWithEGLContextDrawIn;
        }
//        设置地图渲染模式为openGL3效果 星河效果需配置OpenGL3渲染
//        devAttribute.renderVendorType = MapRenderVendor.OpenGL3;

        IDeviceObserver deviceObserver = new DeviceObserver(null, null);
        MapDevice mapDevice = mapService.createDevice(nDeviceID, devAttribute, deviceObserver);

        if (null == mapDevice) {
            Timber.e(new NullPointerException(), "SDK mapDevice is null");
            return null;
        }

        return mapDevice;
    }

    /**
     * 创建地图引擎虚拟视图
     */
    private MapView createMapView(MapDevice mapDevice, long nEngineId, SurfaceViewParam params, int mayNightMode) {
        if (null == mapService) {
            Timber.e(new NullPointerException(), "SDK mapService is null");
            return null;
        }

        MapViewParam mapViewParam = new MapViewParam();
        mapViewParam.deviceId = mapDevice.getDeviceId();
        mapViewParam.engineId = nEngineId;
        mapViewParam.x = params.x;
        mapViewParam.y = params.y;
        mapViewParam.width = params.width;
        mapViewParam.height = params.height;
        mapViewParam.screenWidth = params.screenWidth;
        mapViewParam.screenHeight = params.screenHeight;
        mapViewParam.cacheCountFactor = 2.0F;
        mapViewParam.mapProfileName = "";//引擎全局配置表名称，默认设置为空即可  星河效果需配置 "mapprofile_fa1"
        mapViewParam.zoomScaleMode = MapZoomScaleMode.PhysicalAdaptiveMode;
        MapView mapView = mapService.createMapView(mapViewParam, new IBLMapViewProxy() {
            @Override
            public void reloadMapResource(long l, byte[] bytes, int i) {

            }

            @Override
            public void requireMapRender(long engineId, int needFrames, int adviseFPS) {
                if (null != mapService) {
                    MapView mapView = mapService.getMapView((int) engineId);
                    //mapService有被反初始化回收的风险,添加反初始化时代理置空,以及再次判空进行双重保护
                    if (null != mapView && null != mapService) {
                        MapDevice mapDevice = mapService.getDevice(mapView.getDeviceId());
                        if (null != mapDevice) {
                            mapDevice.resetTickCount(needFrames);
                        }
                    }
                }
            }

            @Override
            public byte[] requireMapResource(long l, MapResourceParam mapResourceParam) {
                return MapHelper.getMapAssetHelper().requireResource(mContext, mapResourceParam);
            }

            @Override
            public void onMapLogReporter(long l, int i, int i1, String s) {

            }

            @Override
            public void onSendBehaviorLog(long l, String s, String s1, String s2) {

            }

        }, null, null, null);

        setMapStyle(mapView, getFirstTimeNightMode(mayNightMode), EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NORMAL);
        setZoomLevel(mapView, 15.0f);
        setBaseMapIconVisible(mapView, OpenLayerIDRouteTraffic, true);
        setBaseMapPOIVisible(mapView, true);
        setBaseMapSample3DVisible(mapView, true);
        setBaseMapRoadNameVisible(mapView, true);
        setBuildingAnimateAlpha(mapView, true, true, 4);
        mapView.getOperatorBusiness().setMapZoomScaleAdaptive((int) mapViewParam.screenWidth, (int) mapViewParam.screenHeight, 0.0f);
        MapViewObserver mapViewObserver = new MapViewObserver(mapDevice, mapView);
        //地图视图观察者
        mapView.addMapviewObserver(mapViewObserver);
        mapViewObserverArrayList.add(mapViewObserver);

        MapGestureObserver mapGestureObserver = new MapGestureObserver(mapDevice, mapView);
        //地图手势观察者
        mapView.addGestureObserver(mapGestureObserver);
        mapGestureObserverArrayList.add(mapGestureObserver);

        TextTextureObserver textTextureObserver = new TextTextureObserver(mapDevice, mapView);
        //文字字模观察者
        mapView.setTextTextureObserver(textTextureObserver);
        textTextureObserverArrayList.add(textTextureObserver);

        mapView.getOperatorBusiness().setMapTextScale(1.3f);// 设置底图元素大小
        mapView.getOperatorGesture().enableSliding(true);

        return mapView;
    }

    /**
     * 地图底图样式状态枚举注解类型
     */
    public @interface EMapStyleStateType {
        /**
         * < 底图样式常规态
         */
        int E_MAP_STYLE_STATE_TYPE_NORMAL = 0;
        /**
         * < 路线规划样式态
         */
        int E_MAP_STYLE_STATE_TYPE_PLAN = 1;
        /**
         * < 导航引导样式态
         */
        int E_MAP_STYLE_STATE_TYPE_NAVI = 2;
    }

    /**
     * 传入engineID常量值 如@MapEngineID.MapEngineIdMain，获取对于的mapview实例对象
     */
    public MapView getMapView(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        MapView mapView = null;
        if (null != mapService) {
            mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(nSurfaceViewID));
        }
        return mapView;
    }

    /**
     * 传入mapview实例，获取其所关联绑定的MapDevice实例
     */
    public MapDevice getMapDevice(MapView mapview) {

        if (null == mapview || null == mapService) {
            return null;
        }


        MapDevice mapDevice = mapService.getDevice(mapview.getDeviceId());
        return mapDevice;
    }

    public DeviceObserver getDeviceObserverArrayList(int position) {
        return deviceObserverArrayList.get(position);
    }

    /**
     * 清除所有虚拟设备观察者
     */
    private void clearDeviceObservers() {
        int size = deviceObserverArrayList.size();
        for (int i = 0; i < size; i++) {
            DeviceObserver deviceObserver = deviceObserverArrayList.get(i);
            MapDevice mapDevice = deviceObserver.getMapDevice();
            mapDevice.removeDeviceObserver(deviceObserver);
        }
        deviceObserverArrayList.clear();
    }

    /**
     * 清除所有手势操作观察者
     */
    private void clearMapGestureObservers() {
        int size = mapGestureObserverArrayList.size();
        for (int i = 0; i < size; i++) {
            MapGestureObserver mapGestureObserver = mapGestureObserverArrayList.get(i);
            MapView mapView = mapGestureObserver.getMapView();
            mapView.removeGestureObserver(mapGestureObserver);
        }
        mapGestureObserverArrayList.clear();
    }

    /**
     * 清除所有视图操作观察者
     */
    private void clearMapViewObservers() {
        int size = mapViewObserverArrayList.size();
        for (int i = 0; i < size; i++) {
            MapViewObserver mapViewObserver = mapViewObserverArrayList.get(i);
            MapView mapView = mapViewObserver.getMapView();
            mapView.removeMapviewObserver(mapViewObserver);
        }
        mapViewObserverArrayList.clear();
    }

    /**
     * 清除所有字体观察者
     */
    private void clearTextTextureObservers() {
        int size = textTextureObserverArrayList.size();
        for (int i = 0; i < size; i++) {
            TextTextureObserver textTextureObserver = textTextureObserverArrayList.get(i);
            MapView mapView = textTextureObserver.getMapView();
            mapView.setTextTextureObserver(null);
        }

        textTextureObserverArrayList.clear();
    }


    /**
     * 所有屏幕视图刷帧
     */
    public void refreshAll() {
        int size = mapSurfaceViewArrayList.size();
        for (int i = 0; i < size; i++) {
            int nEngineId = i * 2 + 1;
            if (null != mapService) {
                MapView mapView = mapService.getMapView(nEngineId);
                if (mapView != null) {
                    mapView.resetTickCount(1);
                }
            }
        }
    }

    /**
     * 屏幕视图刷帧
     *
     * @param nSurfaceViewID 屏幕视图ID
     */
    public void refresh(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(nSurfaceViewID));
            if (mapView != null) {
                mapView.resetTickCount(1);
            }
        }
    }

    /**
     * 读取地图渲染配置文件内容buffer
     *
     * @param filePath 文件路径
     */
    public byte[] getConfigBuffer(String filePath) {
        if (null == filePath) {
            return null;
        }

        InputStream is = null;

        File file = new File(filePath);
        if (!file.exists()) {
            AssetManager assetManager = mContext.getAssets();
            filePath = "blRes/MapAsset/deviceprofile.data";
            Log.i("cfgmgr 0", "GetConfigBuffer deviceprofile.data from " + filePath);
            try {
                is = assetManager.open(filePath);

                if (null == is) {
                    return null;
                }

                int count = is.available();
                if (0 == count) {
                    Log.i("cfgmgr 0", "GetConfigBuffer Error 0 ");
                    return null;
                }
                byte[] bufferByte = new byte[count];
                int readCount = 0;
                while (readCount < count) {
                    readCount += is.read(bufferByte, readCount, count - readCount);
                }
                Log.i("cfgmgr 0", "GetConfigBuffer deviceprofile.data cnt" + count);
                return bufferByte;
            } catch (IOException e) {
                return null;
            } catch (OutOfMemoryError e) {
                return null;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.i("cfgmgr 1", "GetConfigBuffer deviceprofile.data from" + filePath);
            try {
                is = new FileInputStream(file);
                int count = is.available();
                if (0 == count) {
                    return null;
                }
                byte[] bufferByte = new byte[count];
                int readCount = 0;
                while (readCount < count) {
                    readCount += is.read(bufferByte, readCount, count - readCount);
                }
                Log.i("cfgmgr 1", "GetConfigBuffer mapprofile.data cnt" + count);
                return bufferByte;
            } catch (IOException e) {
                return null;
            } catch (OutOfMemoryError e) {
                return null;
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 放大比例尺
     *
     * @param surfaceViewID 屏幕视图ID
     * @param bAnimation    是否携带动画
     * @param bSync         是否立即生效
     */
    public void mapZoomIn(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean bAnimation, boolean bSync) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            mapZoomIn(mapView, bAnimation, bSync);
        }
    }

    /**
     * 放大比例尺
     *
     * @param mapView    地图渲染虚拟视图
     * @param bAnimation 是否携带动画
     * @param bSync      是否立即生效
     */
    private void mapZoomIn(MapView mapView, boolean bAnimation, boolean bSync) {
        if (mapView != null) {
            mapView.mapZoomIn(bAnimation, bSync);
            mapView.resetTickCount(1);
        }
    }

    /**
     * 缩小比例尺
     *
     * @param surfaceViewID 屏幕视图ID
     * @param bAnimation    是否携带动画
     * @param bSync         是否立即生效
     */
    public void mapZoomOut(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean bAnimation, boolean bSync) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            mapZoomOut(mapView, bAnimation, bSync);
        }
    }

    /**
     * 缩小比例尺
     *
     * @param mapView    地图渲染虚拟视图
     * @param bAnimation 是否携带动画
     * @param bSync      是否立即生效
     */
    private void mapZoomOut(MapView mapView, boolean bAnimation, boolean bSync) {
        if (mapView != null) {
            mapView.mapZoomOut(bAnimation, bSync);
            mapView.resetTickCount(1);
        }
    }

    /**
     * 设置比例尺级别
     *
     * @param surfaceViewID 屏幕视图ID
     * @param level         比例尺级别
     */
    public void setZoomLevel(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, float level) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            setZoomLevel(mapView, level);
        }
    }

    public void setMaxZoomLevel(MapView mapView, float level) {
        if (null != mapService) {
            if (mapView != null) {
                mapView.getOperatorPosture().setMaxZoomLevel(level);
            }
        }
    }

    /**
     * 设置比例尺级别
     *
     * @param mapView 地图渲染虚拟视图
     * @param level   比例尺级别
     */
    private void setZoomLevel(MapView mapView, float level) {
        if (mapView != null) {
            mapView.getOperatorPosture().setZoomLevel(level);
            mapView.resetTickCount(1);
        }
    }

    /**
     * 获取当前比例尺级别
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public float getZoomLevel(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        float fLevel = 0f;
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            fLevel = getZoomLevel(mapView);
        }

        return fLevel;
    }

    /**
     * 获取当前比例尺级别
     *
     * @param mapView 地图渲染虚拟视图
     */
    private float getZoomLevel(MapView mapView) {
        float level = 0f;
        if (mapView != null) {
            level = mapView.getOperatorPosture().getZoomLevel();
        }
        return level;
    }

    /**
     * 获取当前比例尺
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public int getCurrentScale(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        int nScale = 0;
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            nScale = getCurrentScale(mapView);
        }

        return nScale;
    }

    /**
     * 获取当前比例尺
     *
     * @param mapView 地图渲染虚拟视图
     */
    private int getCurrentScale(MapView mapView) {
        int nScale = 0;
        if (mapView != null) {
            nScale = mapView.getOperatorScale().getCurrentScale();
        }
        return nScale;
    }

    /**
     * 获取指定地图比例尺所表示的地理长度
     * 返回指定比例尺所表示的地理长度（单位：米）
     */
    public int getScale(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, float level) {
        int nScale = 0;
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            nScale = getScale(mapView, level);
        }

        return nScale;
    }

    /**
     * 获取指定地图比例尺所表示的地理长度
     * 返回指定比例尺所表示的地理长度（单位：米）
     *
     * @param mapView 地图渲染虚拟视图
     */
    private int getScale(MapView mapView, float level) {
        int nScale = 0;
        if (mapView != null) {
            nScale = mapView.getOperatorScale().getScale((int) level);
        }
        return nScale;
    }

    /**
     * 获取比例尺线条的像素长度
     * 返回像素数量
     */
    public int getScaleLineLength(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        int nScale = 0;
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            nScale = getScaleLineLength(mapView);
        }

        return nScale;
    }

    /**
     * 获取比例尺线条的像素长度
     * 返回像素数量
     *
     * @param mapView 地图渲染虚拟视图
     */
    private int getScaleLineLength(MapView mapView) {
        int nScaleLine = 0;
        if (mapView != null) {
            nScaleLine = mapView.getOperatorScale().getScaleLineLength();
        }
        return nScaleLine;
    }

    /**
     * 获取最大比例尺
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public int getMaxScale(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        int nScale = 0;
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            nScale = getMaxScale(mapView);
        }

        return nScale;
    }

    /**
     * 获取最大比例尺
     *
     * @param mapView 地图渲染虚拟视图
     */
    private int getMaxScale(MapView mapView) {
        int nScale = 0;
        if (mapView != null) {
            nScale = (int) mapView.getOperatorPosture().getMaxScale();
        }
        return nScale;
    }

    /**
     * 获取最小比例尺
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public int getMinScale(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        int nScale = 0;
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            nScale = getMinScale(mapView);
        }

        return nScale;
    }

    /**
     * 获取最小比例尺
     *
     * @param mapView 地图渲染虚拟视图
     */
    private int getMinScale(MapView mapView) {
        int nScale = 0;
        if (mapView != null) {
            nScale = (int) mapView.getOperatorPosture().getMinScale();
        }
        return nScale;
    }

    /**
     * 比例尺是否可以继续放大
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public boolean isZoomInEnable(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        boolean zoomInEnable = getZoomLevel(surfaceViewID) < getMaxScale(surfaceViewID);
        return zoomInEnable;
    }

    /**
     * 比例尺是否可以继续缩小
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public boolean isZoomOutEnable(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        boolean zoomOutEnable = getZoomLevel(surfaceViewID) > getMinScale(surfaceViewID);
        return zoomOutEnable;
    }

    /**
     * 调整屏幕视图显示位置
     *
     * @param surfaceViewID 屏幕视图ID
     * @param portParam     屏幕视图显示位置参数
     * @param bSync         是否同步生效
     */
    public void setMapViewPort(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapViewPortParam portParam, boolean bSync) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (mapView != null) {
                mapView.setMapviewPort(portParam);
            }
        }
    }

    /**
     * 设置地图朝向模式
     *
     * @param surfaceViewID 屏幕视图ID
     * @param modeParam     模式参数
     * @param bAnimation    是否携带动画
     */
    public void setMapMode(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapviewModeParam modeParam, boolean bAnimation) {

        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (mapView != null) {
                mapView.setMapMode(modeParam, bAnimation);
            }
        }
    }

    /**
     * 获取地图朝向模式
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public int getMapMode(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (mapView != null) {
                return mapView.getMapMode();
            }
        }
        return MapviewMode.AUTO_UNKNOWN_ERROR;
    }

    /**
     * 返回自车位置
     *
     * @param surfaceViewID 屏幕视图ID
     * @param bAnimation    是否携带动画
     */
    public void goToDefaultPosition(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean bAnimation) {

        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (mapView != null) {
                MapPositionParam pos = new MapPositionParam();
                pos.lon = MapModelDtoConstants.FLOAT_INVALID_VALUE;
                pos.lat = MapModelDtoConstants.FLOAT_INVALID_VALUE;
                mapView.goToPosition(pos, bAnimation);
                mapView.resetTickCount(1);
            }
        }
    }

    /**
     * 设置地图地图样式类型
     *
     * @param surfaceViewID  屏幕视图ID
     * @param isNightMode    日夜模式
     * @param styleStateType 样式文件类型
     */
    public void setMapStyle(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean isNightMode, @EMapStyleStateType int styleStateType) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            setMapStyle(mapView, isNightMode, styleStateType);
        }
    }

    public void setMapStyleNotForce(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean isNightMode, @EMapStyleStateType int styleStateType) {
        if (null == mapService)
            return;
        MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
        MapStyleParam styleParam = mapView.getOperatorStyle().getMapStyle();
        int currentStyle = EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NORMAL;
        switch (styleParam.state) {
            case MapModelDtoConstants.MAP_MODE_SUBSTATE_PREVIEW_CAR:
                currentStyle = EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_PLAN;
                break;
            case MapModelDtoConstants.MAP_MODE_SUBSTATE_NAVI_CAR:
                currentStyle = EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NAVI;
                break;
            default:
                break;
        }
        Timber.i("styleStateType = %s currentStyle = %s", styleStateType, currentStyle);
        if (styleStateType != currentStyle) {
            setMapStyle(mapView, isNightMode, styleStateType);
        }
    }

    /**
     * 设置地图主题样式类型
     *
     * @param stylePath 样式文件路径
     */
    public void setMapStylePath(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, String stylePath) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (null != mapView) {
                mapView.getOperatorStyle().setMapStylePath(stylePath);
            }
        }
    }

    private boolean getFirstTimeNightMode(int mayNightMode) {
        boolean isNightMode = false;
        if (BuildConfig.dayNightBySystemUI) {
            isNightMode = getDayNightMode() == Configuration.UI_MODE_NIGHT_YES;
        } else {
            switch (mayNightMode) {
                case SettingConst.MODE_DEFAULT://自动模式
                    isNightMode = DayStatusSystemUtil.getInstance().firstTimeNightMode();
                    break;
                case SettingConst.MODE_DAY://白天模式
                    isNightMode = false;
                    break;
                case SettingConst.MODE_NIGHT://黑夜模式
                    isNightMode = true;
                    break;
                default:
                    break;
            }
        }
        Timber.i("getFirstTimeNightMode isNightMode = %s", isNightMode);
        NightModeGlobal.setNightMode(isNightMode);
        return isNightMode;
    }

    /**
     * 设置地图地图样式类型
     *
     * @param mapView        地图渲染虚拟视图
     * @param isNightMode    日夜模式
     * @param styleStateType 样式文件类型
     */
    private void setMapStyle(MapView mapView, boolean isNightMode, @EMapStyleStateType int styleStateType) {
        if (null != mapView) {
            MapStyleParam styleParam = new MapStyleParam();
            styleParam.time = isNightMode ? MapStyleTime.MapTimeNight : MapStyleTime.MapTimeDay;
            styleParam.mode = MapStyleMode.MapModeDefault;

            if (EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_PLAN == styleStateType) {
                styleParam.state = MapModelDtoConstants.MAP_MODE_SUBSTATE_PREVIEW_CAR;

            } else if (EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NAVI == styleStateType) {
                styleParam.state = MapModelDtoConstants.MAP_MODE_SUBSTATE_NAVI_CAR;

            } else {
                styleParam.state = MapModelDtoConstants.MAP_MODE_SUBSTATE_NORMAL;
            }
            styleParam.forceUpdate = true;
            boolean request = mapView.getOperatorStyle().setMapStyle(styleParam, false);
            Timber.i("setMapStyle request = " + request + " isNightMode = " + isNightMode);
            mapView.resetTickCount(1);
        }
    }


    /**
     * 地图底图路网上TMC实时路况
     *
     * @param surfaceViewID 屏幕视图ID
     * @param tmcVisible    是否可见
     */
    public void setTmcVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean tmcVisible) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            setTmcVisible(mapView, tmcVisible);
        }
    }

    /**
     * 地图底图路网上TMC实时路况
     *
     * @param mapView    地图渲染虚拟视图
     * @param tmcVisible 是否可见
     */
    private void setTmcVisible(MapView mapView, boolean tmcVisible) {
        if (mapView != null) {
            mapView.setControllerStatesOperator(MapControllerStatesType.MAP_CONTROLLER_ONOFF_TRAFFIC_STATE, tmcVisible ? 1 : 0, true);
            mapView.resetTickCount(1);
        }
    }

    /**
     * 地图显示地形阴影
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public void showTopography(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean isShow) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            showTopography(mapView, isShow);
        }
    }

    /**
     * 地图显示地形阴影
     *
     * @param mapView 地图渲染虚拟视图
     */
    private void showTopography(MapView mapView, boolean isShow) {
        if (mapView != null) {
            mapView.getOperatorBusiness().setMapViewState(MapViewStateType.MAP_STATE_IS_TOPOGRAPHY_SHOW, isShow);
            mapView.resetTickCount(1);
        }
    }

    /**
     * 地图显示球形地图
     *
     * @param surfaceViewID 屏幕视图ID
     */
    public void showEarthView(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean isShow, float maxLevel, float minLevel) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            showEarthView(mapView, isShow, maxLevel, minLevel);
        }
    }

    /**
     * 地图显示球形地图
     *
     * @param mapView 地图渲染虚拟视图
     */
    private void showEarthView(MapView mapView, boolean isShow, float maxLevel, float minLevel) {
        if (mapView != null) {
            mapView.showEarthView(isShow, maxLevel, minLevel);
            mapView.resetTickCount(1);
        }
    }

    /**
     * 地图底图路网是否显示
     *
     * @param surfaceViewID 屏幕视图ID
     * @param nLayerID      底图icon扎标类型
     * @param visible       是否可见
     */
    public void setBaseMapIconVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, @OpenLayerID.OpenLayerID1 int nLayerID, boolean visible) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));

            setBaseMapIconVisible(mapView, nLayerID, visible);
        }
    }

    /**
     * 地图底图路网是否显示
     *
     * @param mapView  地图渲染虚拟视图
     * @param nLayerID 底图icon扎标类型
     * @param visible  是否可见
     */
    private void setBaseMapIconVisible(MapView mapView, @OpenLayerID.OpenLayerID1 int nLayerID, boolean visible) {
        if (mapView != null) {
            mapView.getOperatorBusiness().showOpenLayer(nLayerID, visible);
        }
    }

    /**
     * 地图底图路网是否显示
     *
     * @param surfaceViewID 屏幕视图ID
     * @param visible       是否可见
     */
    public void setBaseMapRoadVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean visible) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            setBaseMapRoadVisible(mapView, visible);
        }
    }

    /**
     * 地图底图路网是否显示
     *
     * @param mapView 地图渲染虚拟视图
     * @param visible 是否可见
     */
    private void setBaseMapRoadVisible(MapView mapView, boolean visible) {
        if (mapView != null) {
            mapView.getOperatorBusiness().showMapRoad(visible);
        }
    }

    /**
     * 地图底图POI显示开关
     *
     * @param surfaceViewID 屏幕视图ID
     * @param visible       是否可见
     */
    public void setBaseMapPOIVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean visible) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            setBaseMapPOIVisible(mapView, visible);
        }
    }

    /**
     * 地图底图POI信息icon图标显示开关
     *
     * @param mapView 地图渲染虚拟视图
     * @param visible 是否可见
     */
    private void setBaseMapPOIVisible(MapView mapView, boolean visible) {
        if (mapView != null) {
            mapView.getOperatorBusiness().setLabelVisable(visible);
        }
    }

    /**
     * 地图底图路网道路名显示开关
     *
     * @param surfaceViewID 屏幕视图ID
     * @param visible       是否可见
     */
    public void setBaseMapRoadNameVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean visible) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            setBaseMapRoadNameVisible(mapView, visible);
        }
    }

    /**
     * 地图底图路网道路名显示开关
     *
     * @param mapView 地图渲染虚拟视图
     * @param visible 是否可见
     */
    private void setBaseMapRoadNameVisible(MapView mapView, boolean visible) {
        if (mapView != null) {
            mapView.getOperatorBusiness().setMapViewState(MapViewStateType.MAP_VIEWSTATE_IS_LABLE_ROADNAME_ON, visible);
        }
    }

    /**
     * 地图底图显示3D建筑开关
     *
     * @param surfaceViewID 屏幕视图ID
     * @param visible       是否可见
     */
    public void setBaseMapShowBuilding3DVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean visible) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            mapView.getOperatorBusiness().setMapViewState(MapViewStateType.MAP_VIEWSTATE_IS_BUILD_MODEL_ON, visible);
        }
    }

    /**
     * 地图底图显示3D建筑开关
     *
     * @param mapView 地图渲染虚拟视图
     * @param visible 是否可见
     */
    private void setBaseMapShowBuilding3DVisible(MapView mapView, boolean visible) {
        if (mapView != null) {
//            mapView.getOperatorBusiness().setMapViewState(MapViewStateType.MAP_VIEWSTATE_IS_BUILD_MODEL_ON, visible);
            //显示3D建筑
            mapView.getOperatorBusiness().showBuilding3D(visible);
        }
    }

    /**
     * 地图底图简易3D效果
     *
     * @param surfaceViewID 屏幕视图ID
     * @param visible       是否可见
     */
    public void setBaseMapSample3DVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean visible) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            setBaseMapSample3DVisible(mapView, visible);
        }
    }

    /**
     * 地图底图简易3D效果
     *
     * @param mapView 地图渲染虚拟视图
     * @param visible 是否可见
     */
    private void setBaseMapSample3DVisible(MapView mapView, boolean visible) {
        if (mapView != null) {
            mapView.getOperatorBusiness().setMapViewState(MapViewStateType.MAP_VIEWSTATE_IS_SIMPLE3D_ON, visible);
        }
    }

    /**
     * 设置建筑物高度和透明度增长动效
     *
     * @param surfaceViewID 屏幕视图ID
     * @param isHeightOn    高度增长动效开关
     * @param isAlphaOn     透明度增长动效开关
     * @param grownStep     增长步长, 有效值范围[0,100]
     */
    public void setBuildingAnimateAlpha(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean isHeightOn, boolean isAlphaOn, int grownStep) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            setBuildingAnimateAlpha(mapView, isHeightOn, isAlphaOn, grownStep);
        }
    }

    /**
     * 设置建筑物高度和透明度增长动效
     *
     * @param mapView    地图渲染虚拟视图
     * @param isHeightOn 高度增长动效开关
     * @param isAlphaOn  透明度增长动效开关
     * @param grownStep  增长步长, 有效值范围[0,100]
     */
    private void setBuildingAnimateAlpha(MapView mapView, boolean isHeightOn, boolean isAlphaOn, int grownStep) {
        if (mapView != null) {
            mapView.getOperatorBusiness().setBuildingAnimateAlpha(isHeightOn, isAlphaOn, grownStep);
        }
    }

    /**
     * 天空盒显示开关
     *
     * @param surfaceViewID  屏幕视图ID
     * @param mapSkyboxParam 天空盒参数
     */
    public boolean setBaseMapSkyBoxVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapSkyboxParam mapSkyboxParam) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (mapView != null) {
                return mapView.getOperatorBusiness().setMapSkyboxParam(mapSkyboxParam);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * 添加地图手势操作观察者
     *
     * @param surfaceViewID      屏幕视图ID
     * @param mapGestureObserver 手势操作观察者
     */
    public void addGestureObserver(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapGestureObserver mapGestureObserver) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (null != mapView && null != mapGestureObserver) {
                mapView.addGestureObserver(mapGestureObserver);
            }
        }
    }

    /**
     * 移除地图手势操作观察者
     *
     * @param surfaceViewID      屏幕视图ID
     * @param mapGestureObserver 手势操作观察者
     */
    public void removeGestureObserver(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapGestureObserver mapGestureObserver) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (null != mapView && null != mapGestureObserver) {
                mapView.removeGestureObserver(mapGestureObserver);
            }
        }
    }

    /**
     * 设置POI标注
     *
     * @param surfaceViewID 屏幕视图ID
     * @param type          地图操作业务类型
     * @param mapParameter  地图操作参数
     */
    public void setMapBusinessDataPara(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, int type, MapParameter mapParameter) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (mapView != null) {
                mapView.getOperatorBusiness().setMapBusinessDataPara(type, mapParameter);
            }
        }

    }

    /**
     * 地图精品三维是否显示
     *
     * @param surfaceViewID 屏幕视图ID
     * @param visible       是否可见
     */
    public void setRctVisible(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, boolean visible) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            mapView.getOperatorBusiness().setMapViewState(MapViewStateType.MAP_VIEWSTATE_IS_RCT_ON, visible);
        }
    }


    /**
     * 添加地图底图操作观察者
     *
     * @param surfaceViewID   屏幕视图ID
     * @param mapViewObserver 视图操作观察者监听
     */
    public void addMapViewObserver(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapViewObserver mapViewObserver) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (null != mapView && null != mapViewObserver) {
                mapView.addMapviewObserver(mapViewObserver);
            }
        }
    }

    /**
     * 移除地图底图操作观察者
     *
     * @param surfaceViewID   屏幕视图ID
     * @param mapViewObserver 视图操作观察者监听
     */
    public void removeMapViewObserver(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapViewObserver mapViewObserver) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (null != mapView && null != mapViewObserver) {
                mapView.removeMapviewObserver(mapViewObserver);
            }
        }
    }

    /**
     * 添加移图操作观察者
     *
     * @param surfaceViewID    屏幕视图ID
     * @param mapEventObserver 移图观察者监听
     */
    public void addMapEventObserver(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapEventObserver mapEventObserver) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (null != mapView && null != mapEventObserver) {
                mapView.addMapEventObserver(mapEventObserver);
            }
        }
    }


    /**
     * 移除移图操作观察者
     *
     * @param surfaceViewID    屏幕视图ID
     * @param mapEventObserver 观察者监听
     */
    public void removeMapEventObserver(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, MapEventObserver mapEventObserver) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
            if (null != mapView && null != mapEventObserver) {
                mapView.removeMapEventObserver(mapEventObserver);
            }
        }
    }

    /**
     * 移除移图操作观察者
     *
     * @param surfaceViewID 屏幕视图ID
     * @param left          视口左上角坐标left
     * @param top           视口左上角坐标top
     */
    public void setMapLeftTop(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, int left, int top) {
        MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(surfaceViewID));
        if (mapView != null) {
            mapView.setMapLeftTop(left, top);
        }
    }

    /**
     * 反初始化接口
     */
    public void uninit() {

        clearMapViewObservers();
        clearMapGestureObservers();
        clearTextTextureObservers();
        clearDeviceObservers();

        int size = mapSurfaceViewArrayList.size();

        for (int i = 0; i < size; i++) {
            int nEngineId = i * 2 + 1;
            if (null != mapService) {
                MapView mapView = mapService.getMapView(nEngineId);
                if (mapView != null) {
                    mapView.removeMapEngineObserver();
                    mapView.setMapViewProxy(null);
                }

                int nDeviceId = i;
                MapDevice mapDevice = mapService.getDevice(nDeviceId);
                if (mapDevice != null) {
                }
            }
        }

        mapSurfaceViewArrayList.clear();

        if (mapService != null) {
            mapService = null;
        }

        Timber.i("uninit");
    }

    /**
     * 设置渲染帧率
     *
     * @param nSurfaceViewID 屏幕视图ID
     */
    public void setRenderFpsByMode(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, int mode, int fps) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(nSurfaceViewID));
            if (mapView != null) {
                getMapDevice(mapView).setRenderFpsByMode(mode, fps);
            }
        }
    }

    /**
     * 渲染帧率是否暂停
     *
     * @param nSurfaceViewID 屏幕视图ID
     */
    public boolean isRenderPaused(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(nSurfaceViewID));
            if (mapView != null) {
                return getMapDevice(mapView).isRenderPaused();
            }
        }

        return false;
    }

    /**
     * 暂停渲染帧率
     *
     * @param nSurfaceViewID 屏幕视图ID
     */
    public void renderPause(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(nSurfaceViewID));
            if (mapView != null) {
                getMapDevice(mapView).renderPause();
            }
        }
    }

    /**
     * 恢复渲染帧率
     *
     * @param nSurfaceViewID 屏幕视图ID
     */
    public void renderResume(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID) {
        if (null != mapService) {
            MapView mapView = mapService.getMapView(SurfaceViewID.transform2EngineID(nSurfaceViewID));
            if (mapView != null) {
                getMapDevice(mapView).renderResume();
            }
        }
    }

    /**
     * 设置主图网络状态
     *
     * @param networkType
     */
    public void setNetworkType(@NetworkStatus.NetworkStatus1 int networkType) {
        if (null != mapService) {
            mapService.setNetworkType(networkType);
        }
    }

    public MapService getMapService() {
        return mapService;
    }

    /**
     * 当日夜模式修改时调用
     *
     * @param isNightMode
     */
    public void updateMapSurfaceViewInitColor(boolean isNightMode) {
        if (mapSurfaceViewArrayList != null && mapSurfaceViewArrayList.size() > 0) {
            Timber.i("updateMapSurfaceViewInitColor isNightMode = %s", isNightMode);
            mapSurfaceViewArrayList.get(0).getGLSurfaceAttribute().initColor = isNightMode ? Color.parseColor("#212529") : Color.parseColor("#EAF3F6");
        }
    }

    public int getDayNightMode() {
        if (BuildConfig.dayNightBySystemUI) {
            int currentNightMode = mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            Timber.i("onConfigurationChanged currentNightMode = %s", currentNightMode);
            return currentNightMode;
        }
        return Configuration.UI_MODE_NIGHT_NO;
    }

    public ArrayList<MapSurfaceView> getMapSurfaceViewArrayList() {
        return mapSurfaceViewArrayList;
    }
}