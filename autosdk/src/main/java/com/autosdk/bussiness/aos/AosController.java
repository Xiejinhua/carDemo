package com.autosdk.bussiness.aos;

import com.autonavi.gbl.aosclient.BLAosDataTool;
import com.autonavi.gbl.aosclient.BLAosService;
import com.autonavi.gbl.aosclient.model.BLAosCookie;
import com.autonavi.gbl.aosclient.model.CEtaRequestReponseParam;
import com.autonavi.gbl.aosclient.model.CEtaRequestRequestParam;
import com.autonavi.gbl.aosclient.model.GAddressPredictRequestParam;
import com.autonavi.gbl.aosclient.model.GDriveReportSmsRequestParam;
import com.autonavi.gbl.aosclient.model.GDriveReportUploadRequestParam;
import com.autonavi.gbl.aosclient.model.GFeedbackReportRequestParam;
import com.autonavi.gbl.aosclient.model.GHolidayListRequestParam;
import com.autonavi.gbl.aosclient.model.GLbpEventSyncCommonRequestParam;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryRequestParam;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryResponseParam;
import com.autonavi.gbl.aosclient.model.GQRCodeConfirmRequestParam;
import com.autonavi.gbl.aosclient.model.GRangeSpiderRequestParam;
import com.autonavi.gbl.aosclient.model.GReStrictedAreaRequestParam;
import com.autonavi.gbl.aosclient.model.GSendToPhoneRequestParam;
import com.autonavi.gbl.aosclient.model.GTrafficEventDetailRequestParam;
import com.autonavi.gbl.aosclient.model.GTrafficRestrictRequestParam;
import com.autonavi.gbl.aosclient.model.GWorkdayListRequestParam;
import com.autonavi.gbl.aosclient.model.GWsAosDestinationSearchRequestParam;
import com.autonavi.gbl.aosclient.model.GWsNavigationDynamicDataRequestParam;
import com.autonavi.gbl.aosclient.model.GWsNavigationDynamicDataResponseParam;
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinQrcodeRequestParam;
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinStatusRequestParam;
import com.autonavi.gbl.aosclient.model.GWsPpAutoWeixinUnbindRequestParam;
import com.autonavi.gbl.aosclient.model.GWsShieldSearchRanklistCityRequestParam;
import com.autonavi.gbl.aosclient.model.GWsShieldSearchRanklistLandingRequestParam;
import com.autonavi.gbl.aosclient.model.GWsShieldSearchRanklistPortalRequestParam;
import com.autonavi.gbl.aosclient.model.GWsTcPoiInfoRequestParam;
import com.autonavi.gbl.aosclient.model.GWsTserviceTeamResponseMember;
import com.autonavi.gbl.aosclient.model.GWsUserviewFootprintSummaryRequestParam;
import com.autonavi.gbl.aosclient.observer.ICallBackAddressPredict;
import com.autonavi.gbl.aosclient.observer.ICallBackDriveReportSms;
import com.autonavi.gbl.aosclient.observer.ICallBackDriveReportUpload;
import com.autonavi.gbl.aosclient.observer.ICallBackEtaRequest;
import com.autonavi.gbl.aosclient.observer.ICallBackFeedbackReport;
import com.autonavi.gbl.aosclient.observer.ICallBackHolidayList;
import com.autonavi.gbl.aosclient.observer.ICallBackLbpEventSyncCommon;
import com.autonavi.gbl.aosclient.observer.ICallBackNavigationEtaquery;
import com.autonavi.gbl.aosclient.observer.ICallBackQRCodeConfirm;
import com.autonavi.gbl.aosclient.observer.ICallBackRangeSpider;
import com.autonavi.gbl.aosclient.observer.ICallBackReStrictedArea;
import com.autonavi.gbl.aosclient.observer.ICallBackSendToPhone;
import com.autonavi.gbl.aosclient.observer.ICallBackTrafficEventDetail;
import com.autonavi.gbl.aosclient.observer.ICallBackTrafficRestrict;
import com.autonavi.gbl.aosclient.observer.ICallBackWorkdayList;
import com.autonavi.gbl.aosclient.observer.ICallBackWsAosDestinationSearch;
import com.autonavi.gbl.aosclient.observer.ICallBackWsNavigationDynamicData;
import com.autonavi.gbl.aosclient.observer.ICallBackWsPpAutoWeixinQrcode;
import com.autonavi.gbl.aosclient.observer.ICallBackWsPpAutoWeixinStatus;
import com.autonavi.gbl.aosclient.observer.ICallBackWsPpAutoWeixinUnbind;
import com.autonavi.gbl.aosclient.observer.ICallBackWsShieldSearchRanklistCity;
import com.autonavi.gbl.aosclient.observer.ICallBackWsShieldSearchRanklistLanding;
import com.autonavi.gbl.aosclient.observer.ICallBackWsShieldSearchRanklistPortal;
import com.autonavi.gbl.aosclient.observer.ICallBackWsTcPoiInfo;
import com.autonavi.gbl.aosclient.observer.ICallBackWsUserviewFootprintSummary;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.ServiceInitStatus;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.widget.search.EtaQueryCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

/**
 * Created by AutoSdk on 2020/11/9.
 **/
public class AosController {
    public BLAosService mBLAosService;
    //可达范围
    private ICallBackRangeSpider iCallBackRangeSpider;
    //交通事件详情
    private ICallBackTrafficEventDetail iCallBackTrafficEventDetail;
    //抵达时间距离
    private ICallBackEtaRequest iCallBackEtaRequest;
    private ArrayList<GWsTserviceTeamResponseMember> mMembersList = new ArrayList<>();
    //车牌号限行
    private ICallBackTrafficRestrict iCallBackTrafficRestrict;

    //用户足迹信息
    private ICallBackWsUserviewFootprintSummary iCallBackUserviewFootprintSummary;
    //抵达时间距离
    private ICallBackNavigationEtaquery iCallBackNavigationEtaquery;
    //行前拥堵
    private ICallBackWsNavigationDynamicData iCallBackWsNavigationDynamicData;
    //自定义行前拥堵回调
    private AosRequestCallBack<GWsNavigationDynamicDataResponseParam> mDynamicCallback;

    private EtaQueryCallback mINavigationEtaqueryListener = null;

    private List<EtaQueryModel> mEtaQueryModelSet;

    /**
     * 保持ICallBackNavigationEtaquery为单例
     */
    private ICallBackNavigationEtaquery mICallBackNavigationEtaquery = new ICallBackNavigationEtaquery() {
        @Override
        public void onRecvAck(GNavigationEtaqueryResponseParam pResponse) {
            EtaQueryModel reqModel = getEtaQueryModel(pResponse.mReqHandle);
            if (null != reqModel && null != reqModel.listener) {
                reqModel.listener.onRecvAck(pResponse);
                unRegisterEtaQueryModel(reqModel);
            }
        }
    };

    private static class AosManagerHolder {
        private static AosController mInstance = new AosController();
    }

    private AosController() {
    }

    public static AosController getInstance() {
        return AosManagerHolder.mInstance;
    }


    public int initAosService() {
        int init = ServiceInitStatus.ServiceNotInit;
        if (mBLAosService == null) {
            mBLAosService = (BLAosService) ServiceMgr.getServiceMgrInstance()
                    .getBLService(SingleServiceID.AosClientSingleServiceID);
            init = mBLAosService.isInit();
        }
        Timber.d("initAosService: init = " + init);
        return init;
    }

    /**
     * @return BLAosCookie cookie信息
     * @brief 域名hostname为BL内部设置的，HMI没有域名字段,获取cookie
     * @note thread:multi
     */
    public BLAosCookie getCookie() {
        if (mBLAosService != null) {
            return mBLAosService.getCookie();
        }

        return null;
    }

    /**
     * 获取SessionId
     *
     * @return
     */
    public String getSessionId() {
        BLAosCookie aosCookie = getCookie();
        if (aosCookie != null) {
            return BLAosDataTool.getSessionid(aosCookie.mCookie);
        }
        return null;
    }

    /**
     * 可达范围
     */
    public long sendReqRangeSpider(GRangeSpiderRequestParam pAosRequest, ICallBackRangeSpider callBackRangeSpider) {
        this.iCallBackRangeSpider = callBackRangeSpider;
        return mBLAosService.sendReqRangeSpider(pAosRequest, iCallBackRangeSpider);
    }

    /**
     * 交通事件详情
     */
    public long sendReqTrafficEventDetail(GTrafficEventDetailRequestParam pAosRequest, ICallBackTrafficEventDetail callBackTrafficEventDetail) {
        iCallBackTrafficEventDetail = callBackTrafficEventDetail;
        return mBLAosService.sendReqTrafficEventDetail(pAosRequest, iCallBackTrafficEventDetail);
    }


    /**
     * 抵达时间
     */
    public long sendReqNavigationEtaquery(GNavigationEtaqueryRequestParam cEtaRequestRequestParam, EtaQueryCallback listener) {
        if (mBLAosService == null || listener == null) {
            Timber.d("sendReqNavigationEtaquery mBLAosService == null || listener == null.");
            return -1;
        }
        Timber.d("sendReqNavigationEtaquery start");
        long reqID = mBLAosService.sendReqNavigationEtaquery(cEtaRequestRequestParam, mICallBackNavigationEtaquery);
        EtaQueryModel model = new EtaQueryModel();
        model.requestId = reqID;
        model.listener = listener;
        registerEtaQueryModel(model);
        Timber.d("sendReqNavigationEtaquery end, reqID:%s", reqID);
        return reqID;
    }

    /**
     * 添加行前拥堵数据回调观察者
     *
     * @param callBack
     */
    public void addReqWsNavigationDynamicDataObserver(AosRequestCallBack<GWsNavigationDynamicDataResponseParam> callBack) {
        this.mDynamicCallback = callBack;
    }

    /**
     * 移除行前拥堵数据回调观察者
     */
    public void removeReqWsNavigationDynamicDataObserver() {
        this.mDynamicCallback = null;
    }

    public long sendReqWsNavigationDynamicData(GWsNavigationDynamicDataRequestParam pAosRequest) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqWsNavigationDynamicData(pAosRequest, getCallBackWsNavigationDynamicData());
        }
        return -1;
    }


    /**
     * 抵达时间
     */
    public long sendReqEtaRequestRequest(CEtaRequestRequestParam cEtaRequestRequestParam, AosRequestCallBack aosRequestCallBack) {
        if (getEtaCallBackWrapper() == null) {
            return -1;
        }
        getEtaCallBackWrapper().setAosRequestCallBack(aosRequestCallBack);
        return mBLAosService.sendReqEtaRequestRequest(cEtaRequestRequestParam, getEtaCallBackWrapper());
    }

    AbstractAosEtaCallBackRequestWrapper aosEtaCallBackRequestWrapper;

    public AbstractAosEtaCallBackRequestWrapper getEtaCallBackWrapper() {
        if (aosEtaCallBackRequestWrapper == null) {
            aosEtaCallBackRequestWrapper = new AbstractAosEtaCallBackRequestWrapper() {
                AosRequestCallBack<CEtaRequestReponseParam> aosRequestCallBack;

                @Override
                public void setAosRequestCallBack(AosRequestCallBack<CEtaRequestReponseParam> aosRequestCallBack) {
                    this.aosRequestCallBack = aosRequestCallBack;
                }

                @Override
                public void onRecvAck(CEtaRequestReponseParam cEtaRequestReponseParam) {
                    if (aosRequestCallBack != null) {
                        aosRequestCallBack.onSuccess(cEtaRequestReponseParam);
                    }
                    aosRequestCallBack = null;
                }
            };
        }
        return aosEtaCallBackRequestWrapper;
    }

    /**
     * 获取足迹信息
     */
    public long sendReqFootprintSummary(GWsUserviewFootprintSummaryRequestParam pAosRequest, ICallBackWsUserviewFootprintSummary iCallBackWsUserviewFootprintSummary) {
        iCallBackUserviewFootprintSummary = iCallBackWsUserviewFootprintSummary;
        return mBLAosService.sendReqWsUserviewFootprintSummary(pAosRequest, iCallBackUserviewFootprintSummary);
    }


    /**
     * 限行
     */
    public long sendReqTrafficRestrict(GTrafficRestrictRequestParam gTrafficRestrictRequestParam, ICallBackTrafficRestrict callBackTrafficRestrict) {
        iCallBackTrafficRestrict = callBackTrafficRestrict;
        return mBLAosService.sendReqTrafficRestrict(gTrafficRestrictRequestParam, iCallBackTrafficRestrict);
    }

    public void removeCallBackTrafficEventDetail() {
        iCallBackTrafficEventDetail = null;
    }

    public void removeCallBackTrafficRestrict() {
        iCallBackTrafficRestrict = null;
    }

    public void removeCallBackUserviewFootprintSummary() {
        iCallBackUserviewFootprintSummary = null;
    }

    public void removeCallBackWsNavigationDynamicData() {
        iCallBackWsNavigationDynamicData = null;
    }

    public void removeCallBackEtaRequest() {
        iCallBackEtaRequest = null;
        aosEtaCallBackRequestWrapper = null;
    }

    public void removeCallBackRangeSpider() {
        iCallBackRangeSpider = null;
    }

    public void unInit() {
        destroyAos();
    }

    private void destroyAos() {
        if (mBLAosService != null) {
            mBLAosService.unInit();
            mBLAosService = null;
            removeCallBackTrafficEventDetail();
            removeCallBackEtaRequest();
            removeCallBackTrafficRestrict();
            removeCallBackUserviewFootprintSummary();
            removeCallBackRangeSpider();
            removeCallBackWsNavigationDynamicData();
            if (mINavigationEtaqueryListener != null) {
                mINavigationEtaqueryListener = null;
            }
            if (mEtaQueryModelSet != null) {
                mEtaQueryModelSet.clear();
            }
            mEtaQueryModelSet = null;
        }
    }

    public void abortRequest(long abort) {
        if (mBLAosService != null) {
            mBLAosService.abortRequest(abort);
        }
    }

    /**
     * 请求限行详情
     *
     * @param aosRequest
     * @param callBackReStrictedArea
     * @return
     */
    public long requestRestrictedArea(GReStrictedAreaRequestParam aosRequest, ICallBackReStrictedArea callBackReStrictedArea) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqReStrictedArea(aosRequest, callBackReStrictedArea);
        }
        return -1;
    }

    /**
     * 城市列表
     *
     * @param param
     * @param callback
     * @return
     */
    public long sendReqWsShieldSearchRanklistCity(GWsShieldSearchRanklistCityRequestParam param, ICallBackWsShieldSearchRanklistCity callback) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqWsShieldSearchRanklistCity(param, callback);
        }
        return -1;
    }

    /**
     * 高德指南单类目落地页
     *
     * @param param
     * @param callback
     * @return
     */
    public long sendReqWsShieldSearchRanklistLanding(GWsShieldSearchRanklistLandingRequestParam param, ICallBackWsShieldSearchRanklistLanding callback) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqWsShieldSearchRanklistLanding(param, callback);
        }
        return -1;
    }

    /**
     * 高德指南全类目聚合页
     *
     * @param param
     * @param callback
     * @return
     */
    public long sendReqWsShieldSearchRanklistPortal(GWsShieldSearchRanklistPortalRequestParam param, ICallBackWsShieldSearchRanklistPortal callback) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqWsShieldSearchRanklistPortal(param, callback);
        }
        return -1;
    }

    private ICallBackWsNavigationDynamicData getCallBackWsNavigationDynamicData() {
        if (iCallBackWsNavigationDynamicData == null) {
            iCallBackWsNavigationDynamicData = new ICallBackWsNavigationDynamicData() {
                @Override
                public void onRecvAck(GWsNavigationDynamicDataResponseParam pResponse) {
                    if (mDynamicCallback != null) {
                        mDynamicCallback.onSuccess(pResponse);
                    }
                }
            };
        }
        return iCallBackWsNavigationDynamicData;
    }

    /**
     * 导航出行目的地阻断页
     *
     * @param param
     * @param callback
     * @return
     */
    public long sendReqWsAosDestinationSearch(GWsAosDestinationSearchRequestParam param, ICallBackWsAosDestinationSearch callback) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqWsAosDestinationSearch(param, callback);
        }
        return -1;
    }

    /**
     * 导航最后一公里推送手机
     *
     * @param param
     * @param callback
     * @return
     */
    public long sendReqSendToPhone(GSendToPhoneRequestParam param, ICallBackSendToPhone callback) {
        WeakReference<ICallBackSendToPhone> callBack = new WeakReference<>(callback);
        return mBLAosService.sendReqSendToPhone(param, callBack.get());
    }

    /**
     * 获取AOS服务
     *
     * @return
     */
    public BLAosService getAosService() {
        return mBLAosService;
    }

    /**
     * 获取假日信息
     *
     * @return
     */
    public long SendReqHolidayList(GHolidayListRequestParam param, ICallBackHolidayList callback) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqHolidayList(param, callback);
        }
        return -1;
    }

    /**
     * 数据上报
     *
     * @return
     */

    public long sendReqWsTcPoiInfo(GWsTcPoiInfoRequestParam param, ICallBackWsTcPoiInfo callback) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqWsTcPoiInfo(param, callback);
        }
        return -1;
    }

    /**
     * 向apmap push消息
     *
     * @return
     */

    public long sendReqLbpEventSyncCommon(GLbpEventSyncCommonRequestParam param, ICallBackLbpEventSyncCommon callback) {
        if (mBLAosService != null) {
            return mBLAosService.sendReqLbpEventSyncCommon(param, callback);
        }
        return -1;
    }

    public class EtaQueryModel {
        public long requestId;
        public EtaQueryCallback listener;
    }


    private void registerEtaQueryModel(EtaQueryModel requestModel) {
        if (mEtaQueryModelSet == null) {
            mEtaQueryModelSet = new CopyOnWriteArrayList<>();
        }
        if (!mEtaQueryModelSet.contains(requestModel)) {
            mEtaQueryModelSet.add(requestModel);
        }
    }

    private void unRegisterEtaQueryModel(EtaQueryModel requestModel) {
        if (mEtaQueryModelSet != null) {
            mEtaQueryModelSet.remove(requestModel);
        }
    }

    private EtaQueryModel getEtaQueryModel(long requestId) {
        EtaQueryModel routeCarRequestModel = null;
        if (mEtaQueryModelSet != null && mEtaQueryModelSet.size() > 0) {
            for (int i = 0; i < mEtaQueryModelSet.size(); i++) {
                EtaQueryModel tempModel = mEtaQueryModelSet.get(i);
                if (tempModel.requestId == requestId) {
                    routeCarRequestModel = tempModel;
                    break;
                }
            }
        }
        return routeCarRequestModel;
    }

    /**
     * 预测用户家/公司的位置
     *
     * @param pAosRequest
     * @param iCallBackAddressPredict
     * @return
     */
    public long sendReqAddressPredict(GAddressPredictRequestParam pAosRequest, ICallBackAddressPredict iCallBackAddressPredict) {
        WeakReference<ICallBackAddressPredict> callBack = new WeakReference<>(iCallBackAddressPredict);
        return mBLAosService.sendReqAddressPredict(pAosRequest, callBack.get());
    }

    /**
     * 获取节假日信息
     *
     * @param pAosRequest
     * @param iCallBackWorkdayList
     * @return
     */
    public long sendReqWorkdayList(GWorkdayListRequestParam pAosRequest, ICallBackWorkdayList iCallBackWorkdayList) {
        WeakReference<ICallBackWorkdayList> callBack = new WeakReference<>(iCallBackWorkdayList);
        return mBLAosService.sendReqWorkdayList(pAosRequest, callBack.get());
    }

    /**
     * 微信状态
     */
    public long sendReqWsPpAutoWeixinStatus(GWsPpAutoWeixinStatusRequestParam pAosRequest, ICallBackWsPpAutoWeixinStatus pAosCallbackRef) {
        WeakReference<ICallBackWsPpAutoWeixinStatus> callBack = new WeakReference<>(pAosCallbackRef);
        return mBLAosService.sendReqWsPpAutoWeixinStatus(pAosRequest, callBack.get());
    }

    /**
     * 微信互联--获取微信互联二维码
     */
    public long sendReqWsPpAutoWeixinQrcode(GWsPpAutoWeixinQrcodeRequestParam pAosRequest, ICallBackWsPpAutoWeixinQrcode pAosCallbackRef) {
        WeakReference<ICallBackWsPpAutoWeixinQrcode> callBack = new WeakReference<>(pAosCallbackRef);
        return mBLAosService.sendReqWsPpAutoWeixinQrcode(pAosRequest, callBack.get());
    }

    /**
     * 微信解绑
     */
    public long sendReqWsPpAutoWeixinUnbind(GWsPpAutoWeixinUnbindRequestParam pAosRequest, ICallBackWsPpAutoWeixinUnbind pAosCallbackRef) {
        WeakReference<ICallBackWsPpAutoWeixinUnbind> callBack = new WeakReference<>(pAosCallbackRef);
        return mBLAosService.sendReqWsPpAutoWeixinUnbind(pAosRequest, callBack.get());
    }

    /**
     * 网络请求,内存由HMI管理,sns,轮询当前二维码扫描状态
     */
    public long sendReqQRCodeConfirm(GQRCodeConfirmRequestParam pAosRequest, ICallBackQRCodeConfirm pAosCallbackRef) {
        WeakReference<ICallBackQRCodeConfirm> callBack = new WeakReference<>(pAosCallbackRef);
        return mBLAosService.sendReqQRCodeConfirm(pAosRequest, callBack.get());
    }

    /**
     * 网络请求,内存由HMI管理,oss,运营服务,驾车报平安,数据上报
     */
    public long sendReqDriveReport(GDriveReportUploadRequestParam pAosRequest, ICallBackDriveReportUpload pAosCallbackRef) {
        WeakReference<ICallBackDriveReportUpload> callBack = new WeakReference<>(pAosCallbackRef);
        return mBLAosService.sendReqDriveReport(pAosRequest, callBack.get());
    }

    /**
     * 网络请求,内存由HMI管理,oss,运营服务,驾车报平安,数据上报  短信分享
     */
    public long sendReqDriveReportSms(GDriveReportSmsRequestParam pAosRequest, ICallBackDriveReportSms pAosCallbackRef) {
        WeakReference<ICallBackDriveReportSms> callBack = new WeakReference<>(pAosCallbackRef);
        return mBLAosService.sendReqDriveReportSms(pAosRequest, callBack.get());
    }

    /**
     * 用户反馈-错误上报
     */
    public long sendReqFeedbackReport(GFeedbackReportRequestParam pAosRequest, ICallBackFeedbackReport callBackFeedbackReport) {
        WeakReference<ICallBackFeedbackReport> callBack = new WeakReference<>(callBackFeedbackReport);
        return mBLAosService.sendReqFeedbackReport(pAosRequest, callBack.get());
    }
}
