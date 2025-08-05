package com.autosdk.bussiness.account;

import com.autonavi.gbl.aosclient.BLAosService;
import com.autonavi.gbl.aosclient.model.GWsTserviceInternalLinkAutoReportRequestParam;
import com.autonavi.gbl.aosclient.model.GWsTserviceInternalLinkCarGetRequestParam;
import com.autonavi.gbl.aosclient.model.GWsTserviceInternalLinkCarReportRequestParam;
import com.autonavi.gbl.aosclient.observer.ICallBackWsTserviceInternalLinkAutoReport;
import com.autonavi.gbl.aosclient.observer.ICallBackWsTserviceInternalLinkCarGet;
import com.autonavi.gbl.aosclient.observer.ICallBackWsTserviceInternalLinkCarReport;
import com.autosdk.bussiness.account.bean.LinkCarLocation;
import com.autosdk.bussiness.aos.AosController;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * 手车互联
 */
public class LinkCarController {
    private static class LinkCarManagerHolder {
        private static LinkCarController mInstance = new LinkCarController();
    }

    private LinkCarController() {
    }

    public static LinkCarController getInstance() {
        return LinkCarManagerHolder.mInstance;
    }

    /**
     * 获取手机状态
     * @param aosCallback
     */
    public void getLinkPhoneStatus(ICallBackWsTserviceInternalLinkCarGet aosCallback) {
        if(getAosService() != null) {
            GWsTserviceInternalLinkCarGetRequestParam aosRequest = new GWsTserviceInternalLinkCarGetRequestParam();
            aosRequest.appType = "1";
            getAosService().sendReqWsTserviceInternalLinkCarGet(aosRequest, aosCallback);
        }
    }

    /**
     * 上报车机信息--750SDK手车互联功能中的上报逻辑已下沉到SDK，不再需要HMI进行上报
     * @param aosRequest
     * @param aosCallbackRef
     */
    public void startLinkCarReport(GWsTserviceInternalLinkCarReportRequestParam aosRequest, ICallBackWsTserviceInternalLinkCarReport aosCallbackRef) {
        if(getAosService() != null) {
            getAosService().sendReqWsTserviceInternalLinkCarReport(aosRequest, aosCallbackRef);
        }
    }

    private BLAosService getAosService() {
        return AosController.getInstance().getAosService();
    }

    /**
     * 上报停车位置
     * @param linkCarLocation
     */
    public boolean startReportCarLocation(LinkCarLocation linkCarLocation, ICallBackWsTserviceInternalLinkAutoReport aosCallbackRef) {
        GWsTserviceInternalLinkAutoReportRequestParam param = new GWsTserviceInternalLinkAutoReportRequestParam();
        param.bizType = 1;
        JSONObject naviLocInfoJson = new JSONObject();
        JSONObject dataJson = new JSONObject();
        try {
            naviLocInfoJson.put("lon", linkCarLocation.getCarLoc().getLongitude());
            naviLocInfoJson.put("lat", linkCarLocation.getCarLoc().getLatitude());
            dataJson.put("naviLocInfo", naviLocInfoJson);
            dataJson.put("plateNum", linkCarLocation.getPlateNum());
            dataJson.put("parkStatus", linkCarLocation.getParkStatus());
        } catch (JSONException e) {
            Timber.d("startReportCarLocation e:%s", e.getMessage());
            return false;
        }
        param.data = dataJson.toString();
        if(getAosService() != null) {
            long taskId = getAosService().sendReqWsTserviceInternalLinkAutoReport(param, aosCallbackRef);
            return taskId > 0;
        }
        return false;
    }
}
