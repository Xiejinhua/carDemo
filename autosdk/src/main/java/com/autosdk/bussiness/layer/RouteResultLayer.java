package com.autosdk.bussiness.layer;

import android.text.TextUtils;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.Coord3DDouble;
import com.autonavi.gbl.common.model.ElecInfoConfig;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.common.path.model.AvoidJamCloudControl;
import com.autonavi.gbl.common.path.model.ChargeStationInfo;
import com.autonavi.gbl.common.path.model.ForbiddenCloudControl;
import com.autonavi.gbl.common.path.model.RoutePoints;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.layer.BizAreaControl;
import com.autonavi.gbl.layer.BizCarControl;
import com.autonavi.gbl.layer.BizControlService;
import com.autonavi.gbl.layer.BizGuideRouteControl;
import com.autonavi.gbl.layer.BizSearchControl;
import com.autonavi.gbl.layer.CustomPointLayerItem;
import com.autonavi.gbl.layer.RouteBlockLayerItem;
import com.autonavi.gbl.layer.RouteForbiddenLayerItem;
import com.autonavi.gbl.layer.RouteJamPointLayerItem;
import com.autonavi.gbl.layer.RoutePathPointItem;
import com.autonavi.gbl.layer.RouteRestAreaLayerItem;
import com.autonavi.gbl.layer.RouteTrafficEventTipsLayerItem;
import com.autonavi.gbl.layer.RouteViaRoadLayerItem;
import com.autonavi.gbl.layer.RouteWeatherLayerItem;
import com.autonavi.gbl.layer.SearchAlongWayLayerItem;
import com.autonavi.gbl.layer.model.BizAreaType;
import com.autonavi.gbl.layer.model.BizCarType;
import com.autonavi.gbl.layer.model.BizLineBusinessInfo;
import com.autonavi.gbl.layer.model.BizPathInfoAttrs;
import com.autonavi.gbl.layer.model.BizPolygonBusinessInfo;
import com.autonavi.gbl.layer.model.BizRouteDrawCtrlAttrs;
import com.autonavi.gbl.layer.model.BizRouteMapMode;
import com.autonavi.gbl.layer.model.BizRouteRestAreaInfo;
import com.autonavi.gbl.layer.model.BizRouteRestrictInfo;
import com.autonavi.gbl.layer.model.BizRouteType;
import com.autonavi.gbl.layer.model.BizRouteViaRoadInfo;
import com.autonavi.gbl.layer.model.BizRouteWeatherInfo;
import com.autonavi.gbl.layer.model.BizSearchExitEntrancePoint;
import com.autonavi.gbl.layer.model.BizSearchType;
import com.autonavi.gbl.layer.model.RouteDrawStyle;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.BaseLayer;
import com.autonavi.gbl.map.layer.LayerItem;
import com.autonavi.gbl.map.layer.RouteLayerItem;
import com.autonavi.gbl.map.layer.model.CarLoc;
import com.autonavi.gbl.map.layer.model.CarMode;
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo;
import com.autonavi.gbl.map.layer.model.LayerItemType;
import com.autonavi.gbl.map.layer.model.PathMatchInfo;
import com.autonavi.gbl.map.layer.model.RouteLayerScene;
import com.autonavi.gbl.map.layer.model.ScaleInfo;
import com.autonavi.gbl.map.layer.model.ScalePriority;
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver;
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle;
import com.autonavi.gbl.route.model.WeatherLabelItem;
import com.autosdk.bussiness.adapter.bean.AdapterCarEnergyInfo;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.task.TaskManager;
import com.autosdk.bussiness.common.utils.ElectricInfoConverter;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.search.utils.NumberUtil;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;
import com.autosdk.common.utils.SdkNetworkUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import timber.log.Timber;

/**
 * @brief 定义类RouteLayerImpl, 路径规划相关图层
 */
public class RouteResultLayer extends HMIBaseLayer implements ILayerClickObserver {

    private static final String TAG = "RouteResultLayer";

    /**
     * 沿途搜结果POI默认类型，包含加油站、厕所、维修站、ATM
     */
    public static final int POINT_TYPE_ALONG_WAY_DEFAULT = 1;

    /**
     * 沿途搜结果POI 服务区类型
     */
    public static final int POINT_TYPE_ALONG_WAY_REST_AREA = 2;

    private BizGuideRouteControl mGuideRouteControl;
    private BizCarControl mCarControl;
    private BizAreaControl mAreaControl;
    private BizSearchControl mSearchControl;

    private RoutePoints mPathPoints;
    private ArrayList<PathInfo> mPathResult;
    private int mCurSelectIndex = 0;

    public static final int DEFAULT_ERR_CODE = -9527;

    private OnRouteClickListener mOnRouteClickListener;

    private OnAlongWayPointClickListener mOnAlongWayPointClickListener;

    private OnWayPointClickListener mOnWayPointClickListener;
    private OnTrafficEventClickListener mOnTrafficEventClickListener;
    private OnWeatherClickListener mOnWeatherClickListener;
    private OnRouteViaRoadClickListener mOnRouteViaRoadClickListener;
    private ArrayList<Long> mRouteElectricStationLayerIds = new ArrayList<>();
    private String mChargeStationFocusId;

    protected RouteResultLayer(@SurfaceViewID.SurfaceViewID1 int nSurfaceViewID, BizControlService bizService, MapView mapView) {
        super(nSurfaceViewID);
        if (null != bizService && null != mapView) {
            mGuideRouteControl = bizService.getBizGuideRouteControl(mapView);
            mCarControl = bizService.getBizCarControl(mapView);
            mAreaControl = bizService.getBizAreaControl(mapView);
            mSearchControl = bizService.getBizSearchControl(mapView);
        }

    }

    private void drawSinglePath(int selectIndex) {
        if (mPathResult == null || mPathResult.size() <= selectIndex) {
            return;
        }
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearPaths();
        }
        mCurSelectIndex = selectIndex;
        /**< 准备线数据 */
        ArrayList<BizPathInfoAttrs> mBizPathInfoAttrs = new ArrayList<>();
        BizRouteDrawCtrlAttrs mBizRouteDrawCtrlAttrs = new BizRouteDrawCtrlAttrs();
        /**< 是否要绘制 */
        mBizRouteDrawCtrlAttrs.mIsDrawPath = true;
        //是否绘制电子眼
        mBizRouteDrawCtrlAttrs.mIsDrawPathCamera = false;
        //是否要绘制路线上的交通灯
        mBizRouteDrawCtrlAttrs.mIsDrawPathTrafficLight = true;
        //是否要绘制转向箭头
        mBizRouteDrawCtrlAttrs.mIsNewRouteForCompareRoute = true;
        //是否要显示
        mBizRouteDrawCtrlAttrs.mIsVisible = true;
        //是否要打开交通事件显示开关，默认为开
        mBizRouteDrawCtrlAttrs.mIsTrafficEventOpen = true;
        mBizRouteDrawCtrlAttrs.mIsHighLightRoadName = true;
        BizPathInfoAttrs attrs = new BizPathInfoAttrs(mPathResult.get(selectIndex), mBizRouteDrawCtrlAttrs);
        mBizPathInfoAttrs.add(attrs);

        // 路线绘制风格
        RouteDrawStyle mainRouteDrawStyle = new RouteDrawStyle();
        // 是否导航 路径规划中为false
        mainRouteDrawStyle.mIsNavi = false;
        // 是否离线
        mainRouteDrawStyle.mIsOffLine = !SdkNetworkUtil.getInstance().isNetworkConnected();
        // 图模式 主图 或 鹰眼
        mainRouteDrawStyle.mRouteMapMode = BizRouteMapMode.BizRouteMapModeMain;
        // 路线业务场景 单指线
        mainRouteDrawStyle.mRouteScene = RouteLayerScene.RouteLayerSceneNormal;
        // 是否是多备选模式
        mainRouteDrawStyle.mIsMultipleMode = false;

        if (mGuideRouteControl != null) {
            mGuideRouteControl.setPassGreyMode(false);
            mGuideRouteControl.setPathDrawStyle(mainRouteDrawStyle);
            mGuideRouteControl.setPathPoints(mPathPoints);
            // 更新引导路线数据
            mGuideRouteControl.setPathInfos(mBizPathInfoAttrs, 0);
            // 绘制路线以及路线上的元素
            mGuideRouteControl.updatePaths();
            mGuideRouteControl.setSelectedPathIndex(0);
        }
    }

    /**
     * 绘制所有路线
     */
    public void drawPaths() {
        long pathCount = mPathResult == null ? 0 : mPathResult.size();
        ArrayList<BizPathInfoAttrs> paths = new ArrayList<>(); /**< 准备线数据 */
        for (int i = 0; i < pathCount; i++) {
            BizRouteDrawCtrlAttrs mBizRouteDrawCtrlAttrs = new BizRouteDrawCtrlAttrs();
            //是否要绘制
            mBizRouteDrawCtrlAttrs.mIsDrawPath = true;
            //是否绘制电子眼
            mBizRouteDrawCtrlAttrs.mIsDrawPathCamera = false;
            //是否要绘制路线上的交通灯
            mBizRouteDrawCtrlAttrs.mIsDrawPathTrafficLight = true;
            //是否要绘制转向箭头
            mBizRouteDrawCtrlAttrs.mIsNewRouteForCompareRoute = true;
            //是否要显示
            mBizRouteDrawCtrlAttrs.mIsVisible = true;
            //是否要打开交通事件显示开关，默认为开
            mBizRouteDrawCtrlAttrs.mIsTrafficEventOpen = true;
            mBizRouteDrawCtrlAttrs.mIsHighLightRoadName = false;
            if (i == mCurSelectIndex) {
                mBizRouteDrawCtrlAttrs.mIsHighLightRoadName = true;
            }
            BizPathInfoAttrs mBizPathInfoAttrs = new BizPathInfoAttrs(mPathResult.get(i), mBizRouteDrawCtrlAttrs);
            paths.add(i, mBizPathInfoAttrs);
        }

        // 路线绘制风格
        RouteDrawStyle mainRouteDrawStyle = new RouteDrawStyle();
        // 是否导航 路径规划中为false
        mainRouteDrawStyle.mIsNavi = false;
        // 是否离线
        mainRouteDrawStyle.mIsOffLine = !SdkNetworkUtil.getInstance().isNetworkConnected();
        // 图模式 主图 或 鹰眼
        mainRouteDrawStyle.mRouteMapMode = BizRouteMapMode.BizRouteMapModeMain;
        // 路线业务场景 单指线
        mainRouteDrawStyle.mRouteScene = RouteLayerScene.RouteLayerSceneNormal;
        // 是否是多备选模式
        mainRouteDrawStyle.mIsMultipleMode = true;

        if (mGuideRouteControl != null) {
            mGuideRouteControl.setPassGreyMode(false);
            mGuideRouteControl.setPathDrawStyle(mainRouteDrawStyle);
            mGuideRouteControl.setPathPoints(mPathPoints);
            // 更新引导路线数据
            mGuideRouteControl.setPathInfos(paths, mCurSelectIndex);
            // 绘制路线以及路线上的元素
            mGuideRouteControl.updatePaths();
            //大于100km的路线，且多备选开启、日志开启时，很容易出现打印日志anr，故保持与Auto一致，路线页面关闭图层的多备选标签显示
            mGuideRouteControl.setVisible(BizRouteType.BizRouteTypeGuideLabel, false);
            AdapterCarEnergyInfo energyInfo = BusinessApplicationUtils.getElectricInfo().carEnergyInfo;
            ElecInfoConfig config = ElectricInfoConverter.getElecInfoConfig();
            if (config != null) {
                //电动车
                if (energyInfo != null && energyInfo.isCharge) {
                    mGuideRouteControl.setVisible(BizRouteType.BizRouteTypeEnergyEmptyPoint, false);
                    mGuideRouteControl.setVisible(BizRouteType.BizRouteTypeEnergyRemainPoint, false);
                } else {
                    mGuideRouteControl.setVisible(BizRouteType.BizRouteTypeEnergyEmptyPoint, true);
                    mGuideRouteControl.setVisible(BizRouteType.BizRouteTypeEnergyRemainPoint, true);
                }
            }
        }

    }

    /**
     * 第一次绘制路线页面路线，内部默认高亮第1条路线，需要高亮其他路线，需调用switchSelectedPath接口
     *
     * @param pathResult
     * @param pathPoints
     */
    public void drawRoute(ArrayList<PathInfo> pathResult, RoutePoints pathPoints, int selectIndex) {
        deletePathResult();
        mPathResult = pathResult;
        mPathPoints = pathPoints;
        removeClickObserver(this);
        addClickObserver(this);
        mCurSelectIndex = selectIndex;
        drawPaths();
    }

    /**
     * 限行路线绘制，修改业务场景为 RouteLayerScene.RouteLayerSceneLimit
     */
    public void setRouteScene(int routeScene) {
        RouteDrawStyle mainRouteDrawStyle = new RouteDrawStyle();
        mainRouteDrawStyle.mRouteScene = routeScene; // 路线业务场景 单指线
        if (mGuideRouteControl != null) {
            mGuideRouteControl.setPathDrawStyle(mainRouteDrawStyle);
            mGuideRouteControl.updatePaths(); // 绘制路线以及路线上的元素
        }
    }

    public void updateStyle() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearPathsCacheStyle();
            mGuideRouteControl.updateStyle();
        }

        if (mAreaControl != null) {
            mAreaControl.updateStyle();
        }

        if (mSearchControl != null) {
            mSearchControl.updateStyle();
        }
    }


    public CarLoc getCarLocation() {
        CarLoc carInfo = new CarLoc();
        if (mCarControl != null) {
            carInfo = mCarControl.getCarPosition();
        }

        return carInfo;
    }

    public Coord2DDouble getCarPosition() {
        Coord2DDouble carPos = new Coord2DDouble();
        if (null != mCarControl) {
            CarLoc carLoc = mCarControl.getCarPosition();
            if (carLoc.vecPathMatchInfo.size() > 0) {
                PathMatchInfo pathMatchInfo = carLoc.vecPathMatchInfo.get(0);
                carPos.lat = pathMatchInfo.latitude;
                carPos.lon = pathMatchInfo.longitude;
            }
        }

        return carPos;
    }

    public void clearEnergyEmptyPoint() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearAllItems(BizRouteType.BizRouteTypeEnergyEmptyPoint);
        }
    }

    public void clearPathsCacheData() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearPathsCacheData();
        }
    }

    public void clearAllItems(long bizType) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearAllItems(bizType);
        }
    }

    public void clearAllItems() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearAllItems();
        }

        if (mAreaControl != null) {
            mAreaControl.clearAllItems();
        }

        removeClickObserver(this);
    }

    public void clearAllPaths() {
        Timber.i("clearAllPaths");
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearPaths();
            mGuideRouteControl.clearPathsCacheData();
        }

        if (mAreaControl != null) {
            mAreaControl.clearAllItems();
        }
        removeClickObserver(this);
    }

    public void setCarVisible(boolean bVisible) {
        if (mCarControl != null) {
            mCarControl.setVisible(bVisible);
        }
    }

    /**
     * 路线页面切换高亮显示的路线
     *
     * @param index
     */
    public void switchFocusPath(int index) {
        mCurSelectIndex = index;
        drawPaths();
    }

    /**
     * 绘制指定路线
     *
     * @param index
     */
    public void selectRoute(int index) {
        drawSinglePath(index);
    }

    public int getSelectedPathIndex() {
        return mCurSelectIndex;
    }

    public RectDouble getPathPointsBound(RoutePoints pathPoints) {
        if (mGuideRouteControl == null) {
            RectDouble ret = new RectDouble();
            return ret;
        }
        return BizGuideRouteControl.getPathPointsBound(pathPoints);
    }

    public RectDouble getPathResultBound(ArrayList<PathInfo> pathResult) {
        if (mGuideRouteControl == null) {
            RectDouble ret = new RectDouble();
            return ret;
        }
        return BizGuideRouteControl.getPathResultBound(pathResult);
    }

    /**
     * @brief 设置车标模式
     * @param[in] nBusinessType   具体Auto图层业务类型,BizCarType
     * @note thread：multi
     */
    public void setCarMode(@CarMode.CarMode1 int carMode) {
        if (null != mCarControl) {
            mCarControl.setCarMode(carMode, false);
        }
    }

    /**
     * @brief 通知更新Style,"car_layer_style"
     * @param[in] nBusinessType   具体Auto图层业务类型,BizCarType
     * @note thread：multi
     */
    public void updateCarStyle(@BizCarType.BizCarType1 int nBusinessType) {
        if (null != mCarControl) {
            mCarControl.updateStyle(nBusinessType);
        }
    }

    /**
     * @brief 设置跟随模式、自由模式
     * @param[in] bFollow   true 跟随模式   false 自由模式
     * @note 跟随模式是用于当GPS信号输入的时候, 地图中心是否跟GPS位置同步变化; true：地图中心和车标同步变化；false：地图中心不跟车标一起变化；
     * @note thread：multi
     */
    public void setFollowMode(boolean bFollow) {
        if (null != mCarControl) {
            mCarControl.setFollowMode(bFollow);
        }
    }

    /**
     * @brief 设置预览模式
     * @param[in] bPreview  是否预览模式
     */
    public void setPreviewMode(boolean bPreview) {
        mCarControl.setPreviewMode(bPreview);
    }

    public void addClickObserver(ILayerClickObserver observer) {
        if (null != mGuideRouteControl) {
            mGuideRouteControl.addClickObserver(observer);
        }
        if (null != mAreaControl) {
            mAreaControl.addClickObserver(observer);
        }

    }

    public void removeClickObserver(ILayerClickObserver observer) {
        if (null != mGuideRouteControl) {
            mGuideRouteControl.removeClickObserver(observer);
        }
        if (null != mAreaControl) {
            mAreaControl.removeClickObserver(observer);
        }
    }


    /**
     * @return void                    无返回值
     * @brief 更新搜索线
     * @param[in] endAreaBusinessInfo    终点区域信息
     * @note thread: main
     */
    public void updateSearchLine(ArrayList<BizLineBusinessInfo> bizLineBusinessInfos) {
        if (mSearchControl == null) {
            return;
        }
        mSearchControl.updateSearchLine(bizLineBusinessInfos);
    }

    /**
     * @return void                    无返回值
     * @brief 多边形区域图层
     * @param[in] endAreaBusinessInfo    终点区域信息
     * @note thread: main
     */
    public void updateSearchPolygon(BizPolygonBusinessInfo businessInfo) {
        if (mSearchControl == null) {
            return;
        }
        mSearchControl.updateSearchPolygon(businessInfo);
    }

    public void updateSearchExitEntrancePoi(ArrayList<BizSearchExitEntrancePoint> exitEntrancePoints) {
        if (mSearchControl == null) {
            return;
        }
        mSearchControl.updateSearchExitEntrancePoi(exitEntrancePoints);
    }

    public void clearSearchItems(int clearType) {
        if (mSearchControl == null) {
            return;
        }
        mSearchControl.clearAllItems(clearType);
    }

    @Override
    public void onBeforeNotifyClick(BaseLayer baseLayer, LayerItem layerItem, ClickViewIdInfo clickViewIdInfo) {

    }

    @Override
    public void onNotifyClick(BaseLayer baseLayer, LayerItem layerItem, ClickViewIdInfo clickViewIdInfo) {
        int itemType = layerItem.getItemType();
        int businessType = layerItem.getBusinessType();
        Timber.d("====onNotifyClick itemType = %s, businessType = %s", itemType, businessType);
        if (itemType == LayerItemType.LayerItemPathType && businessType == BizRouteType.BizRouteTypePath) {
            //点击路线的回调
            RouteLayerItem routeLayerItem = (RouteLayerItem) layerItem;
            String id = routeLayerItem.getID();
            long pathCount = mPathResult.size();
            ArrayList<PathInfo> paths = new ArrayList<>();
            for (int i = 0; i < pathCount; i++) {
                PathInfo pathInfo = mPathResult.get(i);
                paths.add(pathInfo);
                long pathId = DEFAULT_ERR_CODE;
                if (pathInfo != null) {
                    pathId = pathInfo.getPathID();
                }
                long curId = NumberUtil.str2Long(id, 10, DEFAULT_ERR_CODE);
                if (curId == pathId && curId != DEFAULT_ERR_CODE) {
                    final int selectIndex = i;
                    TaskManager.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mOnRouteClickListener != null) {
                                mOnRouteClickListener.onRouteClick(selectIndex);
                            }
                        }
                    });
                }
            }
        }
        if ("along_search_add_click".equals(clickViewIdInfo.poiMarkerClickViewId)) {
            CustomPointLayerItem alongTipItem = (CustomPointLayerItem) layerItem;
            mOnAlongWayPointClickListener.onTipAddClick(alongTipItem.getMValue());
        }
        if (itemType == LayerItemType.LayerItemPointType) {
            //扎点点击事件
            if (businessType == BizRouteType.BizRouteTypeRestArea) {
                //服务区扎点
                RouteRestAreaLayerItem alongItem = (RouteRestAreaLayerItem) layerItem;
                if (mOnAlongWayPointClickListener != null) {
                    mOnAlongWayPointClickListener.onPointClick(POINT_TYPE_ALONG_WAY_REST_AREA, alongItem.getID());
                }
            } else if (businessType == BizSearchType.BizSearchTypePoiAlongRoute) {
                //沿途搜其他类型扎点
                SearchAlongWayLayerItem alongRouteItem = (SearchAlongWayLayerItem) layerItem;
                if (mOnAlongWayPointClickListener != null) {
                    mOnAlongWayPointClickListener.onPointClick(POINT_TYPE_ALONG_WAY_DEFAULT, alongRouteItem.getID());
                }
            } else if (businessType == BizRouteType.BizRouteTypeViaPoint) {
                //途经点
                final RoutePathPointItem routePathPointItem = (RoutePathPointItem) layerItem;
                Timber.d("====onNotifyClick id = %s", routePathPointItem.getID());
                if (mOnWayPointClickListener != null) {
                    int index = Integer.parseInt(routePathPointItem.getID());
                    mOnWayPointClickListener.onWayPointClick(index);
                }
            } else if (businessType == BizRouteType.BizRouteTypeWeather) {
                //天气overlay点击
                RouteWeatherLayerItem weatherItem = (RouteWeatherLayerItem) layerItem;
                WeatherLabelItem mWeatherInfo = weatherItem.getMWeatherInfo();
                Timber.d("====BizRouteTypeWeather id = %s", weatherItem.getID());
                if (mOnWeatherClickListener != null) {
                    mOnWeatherClickListener.onWeatherPointClick(Integer.parseInt(weatherItem.getID()));
                }
            } else if (businessType == BizRouteType.BizRouteTypeViaRoad) {
                if (mOnRouteViaRoadClickListener != null && layerItem instanceof RouteViaRoadLayerItem) {
                    RouteViaRoadLayerItem viaRoadLayerItem = (RouteViaRoadLayerItem) layerItem;
                    mOnRouteViaRoadClickListener.onRouteViaRoadClick(Integer.parseInt(viaRoadLayerItem.getID()));
                }
            }

        }
        if (businessType == BizRouteType.BizRouteTypeTrafficBlock || businessType == BizRouteType.BizRouteTypeTrafficBlockOuter) {//路线上、线外封路事件图层
            if (layerItem instanceof RouteBlockLayerItem) {
                RouteBlockLayerItem routeBlockLayerItem = (RouteBlockLayerItem) layerItem;
                if (null != routeBlockLayerItem.getMEventCloud() && null != routeBlockLayerItem.getMEventCloud().detail) {
                    final long eventID = routeBlockLayerItem.getMEventCloud().detail.eventID;
                    if (mOnTrafficEventClickListener != null) {
                        mOnTrafficEventClickListener.onTrafficEventClick(eventID, routeBlockLayerItem.getPosition());
                    }
                }
            }
        }
        if (businessType == BizRouteType.BizRouteTypeJamPoint) {
            //拥堵事件图层（路线外） mAvoidJamCloud就有拥堵相关详细信息，无需发起详情请求
            if (layerItem instanceof RouteJamPointLayerItem) {
                RouteJamPointLayerItem routeJamPointLayerItem = (RouteJamPointLayerItem) layerItem;
                final AvoidJamCloudControl avoidJamCloudControl = routeJamPointLayerItem.getMAvoidJamCloud();
                if (null != mOnTrafficEventClickListener && null != avoidJamCloudControl) {
                    mOnTrafficEventClickListener.onRouteJamPointClick(avoidJamCloudControl);
                }
            }
        }
        if (businessType == BizRouteType.BizRouteTypeTrafficEventTip) {
            //路线上事件Tip图层
            if (layerItem instanceof RouteTrafficEventTipsLayerItem) {
                RouteTrafficEventTipsLayerItem routeTrafficEventTipsLayerItem = (RouteTrafficEventTipsLayerItem) layerItem;
                final long eventID = routeTrafficEventTipsLayerItem.getMTrafficEventTipsInfo().mTrafficIncident.ID;
                if (mOnTrafficEventClickListener != null) {
                    mOnTrafficEventClickListener.onTrafficEventClick(eventID, routeTrafficEventTipsLayerItem.getPosition());
                }
            }
        }
        if (businessType == BizRouteType.BizRouteTypeForbidden) {
            //禁行图层  getMForbiddenCloud中就有禁行相关的详细信息，比如禁行原因、道路、类型等，具体字段含义见注释
            if (layerItem instanceof RouteForbiddenLayerItem) {
                RouteForbiddenLayerItem routeForbiddenLayerItem = (RouteForbiddenLayerItem) layerItem;
                final ForbiddenCloudControl forbiddenCloudControl = routeForbiddenLayerItem.getMForbiddenCloud();
                if (null != mOnTrafficEventClickListener && null != forbiddenCloudControl) {
                    mOnTrafficEventClickListener.onForbiddenDetailClick(forbiddenCloudControl);
                }
            }
        }
    }

    @Override
    public void onAfterNotifyClick(BaseLayer baseLayer, LayerItem layerItem, ClickViewIdInfo clickViewIdInfo) {

    }

    /**
     * 设置路线点击事件
     *
     * @param l
     */
    public void setOnRouteClickListener(OnRouteClickListener l) {
        this.mOnRouteClickListener = l;
    }


    /**
     * 路径点击回调
     */
    public interface OnRouteClickListener {
        void onRouteClick(int index);
    }

    /**
     * 路径点击回调
     */
    public interface OnAlongWayPointClickListener {
        /**
         * 沿途搜POI 扎点点击事件回调
         *
         * @param type POI的类型，分为默认和服务区，RouteResultLayer.POINT_TYPE_ALONG_WAY_DEFAULT 和 RouteResultLayer.POINT_TYPE_ALONG_WAY_REST_AREA
         * @param id   POI唯一识别码
         */
        void onPointClick(int type, String id);

        void onTipAddClick(String id);
    }

    /**
     * 途经点扎点点击事件监听
     */
    public interface OnWayPointClickListener {
        /**
         * 途经点 扎点点击回调
         *
         * @param index 途经点下标
         */
        void onWayPointClick(int index);
    }

    /**
     * 天气扎点点击事件监听
     */
    public interface OnWeatherClickListener {
        /**
         * 途经点 扎点点击回调
         *
         * @param index 途经点下标
         */
        void onWeatherPointClick(int index);
    }

    public interface OnRouteViaRoadClickListener {
        void onRouteViaRoadClick(int index);
    }

    /**
     * 设置途经点扎点点击事件监听
     *
     * @param l
     */
    public void setOnWayPointClickListener(OnWayPointClickListener l) {
        mOnWayPointClickListener = l;
    }

    /**
     * 设置沿途搜索结果POI点的点击事件
     *
     * @param l
     */
    public void setOnAlongWayPointClickListener(OnAlongWayPointClickListener l) {
        this.mOnAlongWayPointClickListener = l;
        if (mOnAlongWayPointClickListener != null) {
            LayerController.getInstance().getSearchLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).addClickObserver(this);
            LayerController.getInstance().getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).addClickObserver(this);
        } else {
            LayerController.getInstance().getSearchLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).removeClickObserver(this);
            LayerController.getInstance().getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).removeClickObserver(this);
        }
    }


    /**
     * 路径线上及路径线外点击交通事件监听
     */
    public interface OnTrafficEventClickListener {
        /**
         * 交通事件监听
         *
         * @param eventID 交通事件id
         */
        void onTrafficEventClick(long eventID, Coord3DDouble position);

        /**
         * 拥堵事件图层
         *
         * @param avoidJamCloudControl
         */
        void onRouteJamPointClick(AvoidJamCloudControl avoidJamCloudControl);

        /**
         * 禁行事件图层
         */
        void onForbiddenDetailClick(ForbiddenCloudControl forbiddenCloudControl);
    }

    /**
     * 设置路径线上及路径线外点击交通事件监听
     *
     * @param l
     */
    public void setOnTrafficEventClickListener(OnTrafficEventClickListener l) {
        mOnTrafficEventClickListener = l;
    }

    /**
     * 将服务区POI添加到图层上显示
     */
    public void updateRouteRestAreaInfo(ArrayList<BizRouteRestAreaInfo> bizRouteRestAreaInfos) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.updateRouteRestAreaInfo(bizRouteRestAreaInfos);
        }
    }

    /**
     * 清除服务区扎点
     */
    public void clearRouteRestArea() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearAllItems(BizRouteType.BizRouteTypeRestArea);
        }
    }

    /**
     * 清除焦点
     *
     * @param bizType
     */
    public void clearFocus(long bizType) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearFocus(bizType);
        }
    }

    /**
     * 分段预览
     *
     * @param index
     */
    public void moveToPathSegment(int index) {
        if (mGuideRouteControl != null) {
            ArrayList<Long> segments = new ArrayList<>();
            Integer indexInteger = Integer.valueOf(index);
            segments.add(indexInteger.longValue());
            mGuideRouteControl.setPathArrowSegment(segments);
            mGuideRouteControl.updatePathArrow();
            mGuideRouteControl.moveToPathSegment(index + 1);
        }
    }

    /**
     * 使用场景：预览GroupSegment时，路径线变为蓝色(路线页面避让模式点击groupSegment)
     *
     * @param groupSegIndex
     */
    public void updateRouteDodgeLine(int groupSegIndex) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.updateRouteDodgeLine(groupSegIndex);
        }
    }

    /**
     * 添加沿途天气图层
     *
     * @param weatherInfos
     */
    public void updateRouteWeatherInfo(ArrayList<BizRouteWeatherInfo> weatherInfos) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.updateRouteWeatherInfo(weatherInfos);
        }
    }

    /**
     * 清除沿途天气扎点
     */
    public void clearRouteWeatherOverlay() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearAllItems(BizRouteType.BizRouteTypeWeather);
        }
    }

    /**
     * 添加途径路图层
     *
     * @param routeViaRoadInfos
     */
    public void updateViaRoadInfo(ArrayList<BizRouteViaRoadInfo> routeViaRoadInfos) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.updateRouteViaRoadInfo(routeViaRoadInfos);
        }
    }

    /**
     * 清除途径路扎点
     */
    public void clearViaRoadOverlay() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.clearAllItems(BizRouteType.BizRouteTypeViaRoad);
        }
    }

    /**
     * 获取途径路所有Items
     *
     * @return
     */
    public ArrayList<LayerItem> getViaRoadAllItems() {
        if (mGuideRouteControl != null) {
            return mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeViaRoad).getAllItems();
        }
        return null;
    }

    /**
     * 设置途经路焦点
     *
     * @param id
     */
    public void setViaRoadLayerItemFocus(String id) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeViaRoad).setFocus(id, true);
        }
    }


    /**
     * 设置沿途天气POI点的点击事件
     *
     * @param l
     */
    public void setOnWeatherClickListener(OnWeatherClickListener l) {
        this.mOnWeatherClickListener = l;
    }

    public void setOnRouteViaRoadClickListener(OnRouteViaRoadClickListener l) {
        this.mOnRouteViaRoadClickListener = l;
    }

    /**
     * 添加限行图层
     *
     * @param restrictInfo
     */
    public void updateRouteRestrict(BizRouteRestrictInfo restrictInfo) {
        if (mAreaControl != null) {
            mAreaControl.updateRouteRestrict(restrictInfo);
        }
    }

    /**
     * 清除限行相关图层
     */
    public void clearRouteRestRestrict() {
        if (mAreaControl != null) {
            mAreaControl.clearAllItems(BizAreaType.BizAreaTypeRestrictPolygon);
            mAreaControl.clearAllItems(BizAreaType.BizAreaTypeRestrictPolyline);
        }
    }

    public void showChargeStationOnRoute(final PathInfo pathInfo, ArrayList<POI> midPois, boolean isInnerStyle) {
        //已存在充电站扎标，不需要重复添加绘制
        if (mRouteElectricStationLayerIds.size() > 0 || isInnerStyle) {
            return;
        }
        ArrayList<ChargeStationInfo> chargeStationInfo = pathInfo.getChargeStationInfo();
        if (chargeStationInfo != null) {
            //去除途经点是充电站导致图元叠加
            if (midPois != null) {
                for (int i = 0; i < midPois.size(); i++) {
                    Iterator<ChargeStationInfo> iterator = chargeStationInfo.iterator();
                    while (iterator.hasNext()) {
                        ChargeStationInfo next = iterator.next();
                        if (next.poiID.equals(midPois.get(i).getId())) {
                            iterator.remove();
                        }
                    }
                }
            }
            MapView mapView = SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
            IPrepareLayerStyle prepareLayerStyle = SDKManager.getInstance().getLayerController().getPrepareLayerStyle(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
            final ArrayList<LayerItem> items = new ArrayList<>();
            final BaseLayer stationInfoLayer = new BaseLayer("", mapView);
            stationInfoLayer.setStyle(prepareLayerStyle);
            mapView.getLayerMgr().addLayer(stationInfoLayer);
            for (int i = 0; i < chargeStationInfo.size(); i++) {
                ChargeStationInfo stationInfo = chargeStationInfo.get(i);
                ChargeStationLayerItem item = new ChargeStationLayerItem();
                Coord3DDouble coord3DDouble = new Coord3DDouble(stationInfo.projective.lon / 3600000.0, stationInfo.projective.lat / 3600000.0, 0);
                if (item != null) {
                    String id = stationInfo.poiID;
                    if (id.isEmpty()) {
                        id = String.valueOf(i);
                    }
                    item.setInfo(stationInfo);
                    //id必须唯一
                    item.setID(id);
                    // 由客户端定义的点业务类型
                    item.setBusinessType(ElectricBusinessTypePoint.CHARGE_STATION);
                    item.setPriority(i);
                    item.setPosition(coord3DDouble);
                    item.setScale(ScalePriority.ScalePriorityLocal, new ScaleInfo(0.5, 0.5, 0.5));
                    items.add(item);
                }
            }
            stationInfoLayer.addClickObserver(new ILayerClickObserver() {
                @Override
                public void onBeforeNotifyClick(BaseLayer baseLayer, LayerItem layerItem, ClickViewIdInfo clickViewIdInfo) {
                    if (!layerItem.getID().equals(mChargeStationFocusId)) {
                        cancelChargeStationFocus();
                    }
                }

                @Override
                public void onNotifyClick(BaseLayer baseLayer, LayerItem layerItem, ClickViewIdInfo clickViewIdInfo) {
                    ChargeStationLayerItem item = (ChargeStationLayerItem) layerItem;
                    createChargeStationDetailItem(baseLayer, layerItem);
                    mChargeStationFocusId = item.getID();
                    SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getOperatorPosture().
                            setMapCenter(item.getPosition().lon, item.getPosition().lat, 0, true, false);
                }

                @Override
                public void onAfterNotifyClick(BaseLayer baseLayer, LayerItem layerItem, ClickViewIdInfo clickViewIdInfo) {

                }
            });
            stationInfoLayer.addItems(items);
            mRouteElectricStationLayerIds.add(stationInfoLayer.getLayerID());
        }
    }


    public void createChargeStationDetailItem(BaseLayer baseLayer, LayerItem layerItem) {
        ChargeStationLayerItem item = new ChargeStationLayerItem();
        ChargeStationLayerItem pointLayerItem = (ChargeStationLayerItem) layerItem;
        Coord3DDouble coord3DDouble = new Coord3DDouble(pointLayerItem.getStationInfo().projective.lon / 3600000.0, pointLayerItem.getStationInfo().projective.lat / 3600000.0, 0);
        item.setInfo(pointLayerItem.getStationInfo());
        item.setPosition(coord3DDouble);
        item.setID(pointLayerItem.getID() + "_detail");
        item.setPriority(1000);
        item.setBusinessType(ElectricBusinessTypePoint.CHARGE_STATION_DETAIL);
        item.setScale(ScalePriority.ScalePriorityLocal, new ScaleInfo(0.8, 0.8, 0.8));
        baseLayer.addItem(item);
    }

    public void cancelChargeStationFocus() {
        if (mRouteElectricStationLayerIds != null && mRouteElectricStationLayerIds.size() > 0) {
            for (int i = 0; i < mRouteElectricStationLayerIds.size(); i++) {
                Long id = mRouteElectricStationLayerIds.get(i);
                if (!TextUtils.isEmpty(mChargeStationFocusId)) {
                    SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getLayerMgr().getLayerByID(BigInteger.valueOf(id)).setFocus(mChargeStationFocusId, false);
                    SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getLayerMgr().getLayerByID(BigInteger.valueOf(id)).removeItem(mChargeStationFocusId + "_detail");
                }
            }
        }
    }

    public void cleanChargeStationDetailOnRoute() {
        if (mRouteElectricStationLayerIds != null && mRouteElectricStationLayerIds.size() > 0) {
            for (int i = 0; i < mRouteElectricStationLayerIds.size(); i++) {
                Long id = mRouteElectricStationLayerIds.get(i);
                SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getLayerMgr().getLayerByID(BigInteger.valueOf(id)).clearAllItems();
            }
        }
    }


    public void cleanChargeStationOnRoute() {
        if (mRouteElectricStationLayerIds != null && mRouteElectricStationLayerIds.size() > 0) {
            Iterator<Long> iterator = mRouteElectricStationLayerIds.iterator();
            while (iterator.hasNext()) {
                Long id = iterator.next();
                SDKManager.getInstance().getMapController().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).getLayerMgr().getLayerByID(BigInteger.valueOf(id)).clearAllItems();
                iterator.remove();
            }
        }
    }

    public void setRestAreaFocus(String id, boolean isFocus) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeRestArea).setFocus(id, isFocus);
        }
    }

    public void clearRestAreaAllFocus() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeRestArea).clearFocus();
        }
    }

    public void clearWeatherAllFocus() {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.getRouteLayer(BizRouteType.BizRouteTypeWeather).clearFocus();
        }
    }

    /**
     * @brief 设置图元为焦点
     */
    public void setFocus(long bizType, String strID, boolean bFocus) {
        if (mAreaControl != null) {
            mAreaControl.setFocus(bizType, strID, bFocus);
        }
    }

    /**
     * @brief 设置图元显隐
     */
    public void setVisible(long bizType, boolean bVisible) {
        if (mGuideRouteControl != null) {
            mGuideRouteControl.setVisible(bizType, bVisible);
        }
    }

    public void deletePathResult() {
        if (null != mPathResult) {
            mPathResult = null;
        }
    }
}
