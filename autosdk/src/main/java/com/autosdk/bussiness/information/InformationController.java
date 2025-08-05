package com.autosdk.bussiness.information;

import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.model.RectDouble;
import com.autonavi.gbl.common.model.RectInt;
import com.autonavi.gbl.information.InformationService;
import com.autonavi.gbl.information.model.InformationInitParam;
import com.autonavi.gbl.information.nearby.NearbyRecommendControl;
import com.autonavi.gbl.information.nearby.NearbyRecommendSession;
import com.autonavi.gbl.information.nearby.model.NearbyRecommendParam;
import com.autonavi.gbl.information.nearby.model.NearbyRecommendResult;
import com.autonavi.gbl.information.nearby.model.NearbyRecommendSessionInitParam;
import com.autonavi.gbl.information.trade.TradeControl;
import com.autonavi.gbl.information.trade.TradeCoupon;
import com.autonavi.gbl.information.trade.model.ObtainCouponRequest;
import com.autonavi.gbl.information.trade.model.ObtainableCouponRequest;
import com.autonavi.gbl.information.trade.observer.IObtainCouponObserver;
import com.autonavi.gbl.information.trade.observer.IObtainableCouponObserver;
import com.autonavi.gbl.information.travel.TravelControl;
import com.autonavi.gbl.information.travel.TravelRecommend;
import com.autonavi.gbl.information.travel.model.TravelRecommendBeforeNaviRequest;
import com.autonavi.gbl.information.travel.model.TravelRecommendInitParam;
import com.autonavi.gbl.information.travel.observer.ITravelRecommendObserver;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.OperatorPosture;
import com.autonavi.gbl.search.model.SearchTabInfo;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autonavi.gbl.util.model.TaskResult;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.map.MapController;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.search.NearbyRecommendObserver;
import com.autosdk.bussiness.search.SearchCallback;
import com.autosdk.bussiness.search.SearchCallbackWrapper;
import com.autosdk.bussiness.search.request.SearchLocInfo;
import com.autosdk.bussiness.search.utils.SearchDataConvertUtils;

import java.util.HashMap;

import timber.log.Timber;

public class InformationController {

    private static final String TAG = "InformationController";
    private InformationService mInformationService;
    private TravelRecommend travelRecommend = null;

    private HashMap<Integer, TradeCoupon> tradeCouponHashMap = new HashMap<>();

    private InformationController() {

    }

    private static class InformationControllerHolder {
        private static InformationController instance = new InformationController();
    }

    public static InformationController getInstance() {
        return InformationControllerHolder.instance;
    }

    /**
     * 初始化信息服务
     */
    public void initService() {
        if (mInformationService == null) {
            mInformationService = (InformationService) ServiceMgr.getServiceMgrInstance()
                    .getBLService(SingleServiceID.InformationSingleServiceID);
            InformationInitParam informationInitParam = new InformationInitParam();
            int result = mInformationService.init(informationInitParam);
            Timber.d("init informationService result=%s", result);
        }
    }

    public void unInit() {
        if (mInformationService != null) {
            /**销毁场景推荐实体*/
            if (null != travelRecommend) {
                TravelControl travelControl = mInformationService.getTravelControl();
                if (null != travelControl) {
                    travelControl.destroyTravelRecommend(travelRecommend);
                    travelRecommend = null;
                    Timber.d("unInit destroyTravelRecommend");
                }
            }
            if (!tradeCouponHashMap.isEmpty()) {
                TradeControl tradeControl = mInformationService.getTradeControl();
                if (null != tradeControl) {
                    for (Integer key : tradeCouponHashMap.keySet()) {
                        if (null != tradeCouponHashMap.get(key)) {
                            tradeControl.destroyCoupon(tradeCouponHashMap.get(key));
                        }
                    }
                    Timber.d("unInit destroyCoupon");
                }
            }
            mInformationService.unInit();
            mInformationService = null;
        }
    }


    /**
     * 优惠加油-优惠券-领取卡券接口
     */
    public TaskResult obtainCoupon(int bizType, ObtainCouponRequest param, IObtainCouponObserver observer) {
        if (null != mInformationService) {
            TradeControl tradeControl = mInformationService.getTradeControl();
            TradeCoupon tradeCoupon;
            if (null != tradeControl) {
                if (tradeCouponHashMap.containsKey(bizType) && null != tradeCouponHashMap.get(bizType)) {
                    tradeCoupon = tradeCouponHashMap.get(bizType);
                } else {
                    tradeCoupon = tradeControl.createCoupon(bizType);
                    tradeCouponHashMap.put(bizType, tradeCoupon);
                }
                if (null != tradeCoupon) {
                    TaskResult taskResult = tradeCoupon.request(param, observer);
                    return taskResult;
                }
            }
        }
        return null;
    }

    /**
     * 优惠加油-优惠券-可领取卡券查询接口
     */
    public TaskResult queryObtainableCoupon(int bizType, ObtainableCouponRequest param, IObtainableCouponObserver observer) {

        if (null != mInformationService) {
            TradeControl tradeControl = mInformationService.getTradeControl();
            TradeCoupon tradeCoupon;
            if (null != tradeControl) {
                if (tradeCouponHashMap.containsKey(bizType) && null != tradeCouponHashMap.get(bizType)) {
                    tradeCoupon = tradeCouponHashMap.get(bizType);
                } else {
                    tradeCoupon = tradeControl.createCoupon(bizType);
                    tradeCouponHashMap.put(bizType, tradeCoupon);
                }
                if (null != tradeCoupon) {
                    TaskResult taskResult = tradeCoupon.request(param, observer);
                    return taskResult;
                }
            }
        }
        return null;
    }


    /**
     * 快速出发
     */
    public TaskResult requestTravelRecommendBeforeNavi(TravelRecommendBeforeNaviRequest travelRecommendBeforeNaviRequest, ITravelRecommendObserver observer) {
        if (null != mInformationService) {
            TravelControl travelControl = mInformationService.getTravelControl();
            if (null != travelControl) {
                TravelRecommendInitParam travelRecommendInitParam = new TravelRecommendInitParam();
                if (null == travelRecommend) {
                    travelRecommend = travelControl.createTravelRecommend(travelRecommendInitParam);
                }
                if (null != travelRecommend) {
                    TaskResult taskResult = travelRecommend.request(travelRecommendBeforeNaviRequest, observer);
                    return taskResult;
                }
            }
        }
        return null;
    }

    public NearbyRecommendSession createSession() {
        if (null != mInformationService) {
            NearbyRecommendControl mNearbyRecommendControl = mInformationService.getNearbyRecommendControl();
            NearbyRecommendSession session = mNearbyRecommendControl.createSession(new NearbyRecommendSessionInitParam());
            return session;
        }
        return null;
    }

    public void destroySession(NearbyRecommendSession session) {
        NearbyRecommendControl mNearbyRecommendControl = mInformationService.getNearbyRecommendControl();
        if (mNearbyRecommendControl != null) {
            mNearbyRecommendControl.destroySession(session);
        }
    }

    /**
     * 高德附近推荐
     *
     * @param mSession
     * @param callback
     */
    public long nearbyRecommend(int pageNum, NearbyRecommendSession mSession, SearchCallback<NearbyRecommendResult> callback) {
        NearbyRecommendParam param = new NearbyRecommendParam();
        param.pageNumber = pageNum;

        SearchLocInfo locationToSearchLocInfo = SearchDataConvertUtils.getCurrentLocationToLocInfo();
        param.viewRegion = getGeoObj(new Coord2DDouble(locationToSearchLocInfo.lon, locationToSearchLocInfo.lat));
        if (mSession != null) {
            mNearbyRecommendObserver.setSearchCallbackWrapper(new SearchCallbackWrapper<>(callback));
            mSession.setLocation(new Coord2DDouble(locationToSearchLocInfo.lon, locationToSearchLocInfo.lat));
            return mSession.request(param, mNearbyRecommendObserver);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * 高德附近推荐
     *
     * @param point
     * @param mSession
     * @param callback
     */
    public long nearbyRecommend(int pageNum, NearbyRecommendSession mSession, GeoPoint point, SearchCallback<NearbyRecommendResult> callback) {
        NearbyRecommendParam param = new NearbyRecommendParam();
        param.pageNumber = pageNum;
        param.viewRegion = getGeoObj(new Coord2DDouble(point.getLongitude(), point.getLatitude()));
        if (mSession != null) {
            mNearbyRecommendObserver.setSearchCallbackWrapper(new SearchCallbackWrapper<>(callback));
            mSession.setLocation(new Coord2DDouble(point.getLongitude(), point.getLatitude()));
            return mSession.request(param, mNearbyRecommendObserver);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }


    /**
     * 高德附近推荐终止请求
     *
     * @param mSession
     * @param taskID   任务ID
     */
    public void abortNearbyRecommend(NearbyRecommendSession mSession, int taskID) {
        if (mSession != null) {
            mSession.abort(taskID);
        }
    }

    /**
     * 通知SDK tab曝光
     *
     * @param searchTabInfo 曝光的tab树
     * @return ErrorCode 错误码
     * - ErrorCodeOK  :    成功
     * @note - 标签一展示就调用
     * - param中的所有tab都算曝光, 与选中与否无关, 即 index字段不生效
     */
    public int onShowTab(NearbyRecommendSession mSession, SearchTabInfo searchTabInfo) {
        if (mSession != null) {
            return mSession.onShowTab(searchTabInfo);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * 切换tab标签
     *
     * @param searchTabInfo tab标签切换参数
     * @return ErrorCode 错误码
     * - ErrorCodeOK  :    成功
     * - ErrorCodeInvalidParam: 入参错误
     * @note - 切换标签之前，需要先调用Request接口
     * - param中的index字段即为点击的标签
     */
    public int clickTab(NearbyRecommendSession mSession, SearchTabInfo searchTabInfo) {
        if (mSession != null) {
            return mSession.clickTab(searchTabInfo);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /* 获取对角线坐标字符串 */
    private RectDouble getGeoObj(Coord2DDouble carPos) {
        // 1. 获取POI点的搜索区域范围
        /* 自车位经纬度转地图P20坐标 */
        MapView mapView = MapController.getInstance().getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN);
        // 获取地图姿态操作接口, com.autonavi.gbl.map.OperatorPosture
        OperatorPosture operatorPosture = mapView.getOperatorPosture();

        /* 获取当前显示的地图范围 */
        RectDouble mapBound = operatorPosture.getMapBound();

        RectInt mapRect = new RectInt();
        mapRect.left = (int) mapBound.left;
        mapRect.right = (int) mapBound.right;
        mapRect.top = (int) mapBound.top;
        mapRect.bottom = (int) mapBound.bottom;

        // 2. 将显示区域转换为对角线坐标字符串
        Coord2DDouble leftTop = OperatorPosture.mapToLonLat(mapRect.left, mapRect.top);
        Coord2DDouble bottomRight =
                OperatorPosture.mapToLonLat(mapRect.right, mapRect.bottom);

        String geoobj = "";
        geoobj += leftTop.lon + "|" + leftTop.lat + "|" + bottomRight.lon + "|" + bottomRight.lat;

        return mapBound;
    }

    private NearbyRecommendObserver mNearbyRecommendObserver = new NearbyRecommendObserver() {

        private SearchCallbackWrapper<NearbyRecommendResult> callbackWrapper;

        @Override
        public void setSearchCallbackWrapper(SearchCallbackWrapper<NearbyRecommendResult> callbackWrapper) {
            this.callbackWrapper = callbackWrapper;
        }

        @Override
        public void onResult(int taskId, NearbyRecommendResult result) {
            if (callbackWrapper == null) {
                return;
            }
            if (result.errorCode == Service.ErrorCodeOK) {
                callbackWrapper.onSuccess(result);
            } else {
                callbackWrapper.onFailure(result.errorCode, "taskId=" + taskId);
            }
            callbackWrapper.onComplete();
            callbackWrapper = null;
        }
    };

}
