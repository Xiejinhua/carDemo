package com.autosdk.bussiness.activate;

import com.autonavi.gbl.activation.ActivationModule;
import com.autonavi.gbl.activation.model.ActivateReturnParam;
import com.autonavi.gbl.activation.model.ActivationInitParam;
import com.autonavi.gbl.activation.observer.INetActivateObserver;
import com.autonavi.gbl.util.errorcode.Activation;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autosdk.bussiness.activate.constant.ActivateConstant;
import com.autosdk.bussiness.activate.observer.ActivateNetObserver;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.common.AutoConstant;
import com.autosdk.common.SdkApplicationUtils;
import com.autosdk.common.utils.SdkNetworkUtil;

import timber.log.Timber;

/**
 * 激活模块M层
 */
public class ActivateController implements INetActivateObserver{

    private ActivationModule mActivaionService;

    private ActivateController() {

    }

    private static class ActivateControllerHolder {
        private static ActivateController instance = new ActivateController();
    }

    public static ActivateController getInstance() {
        return ActivateControllerHolder.instance;
    }

    /**
     * 初始化激活服务
     * @return
     */
    public int init(String deviceID) {
        if (mActivaionService == null) {
            mActivaionService = ActivationModule.getInstance();
        }
        ActivationInitParam initParam = new ActivationInitParam();
        //是否检查客户端编号
        initParam.isCheckClientNo = true;
        //是否检查项目编号
        initParam.isCheckModelNo = true;
        //是否支持批量激活
        initParam.isSupportVolumeAct = true;
        initParam.iProjectId = 0;
        //激活码长度为24，设置为其他值无法激活
        initParam.iCodeLength = 24;
        //设备编号   （最大长度 32）
        initParam.szDeviceID = deviceID;
        Timber.d("initParam szDeviceID = %s", initParam.szDeviceID);
        //激活文件保存路径
        initParam.szUserDataFileDir = AutoConstant.MAP_ACTIVE_DIR;
        Timber.d("initParam szUserDataFileDir =  %s", initParam.szUserDataFileDir);
        int resultCode;
        if (mActivaionService != null) {
            resultCode = mActivaionService.init(initParam);
        } else {
            return -1;
        }

        Timber.d("init Activate resultCode = %s", resultCode);

        if (mActivaionService != null){
            mActivaionService.setNetActivateObserver(this);
        }
        return resultCode;
    }

    /**
     * 判断是否启动激活功能
     * @return
     */
    public boolean isStartActivate() {
        String version = SDKManager.getInstance().getVersion();
        String[] strings = version.split("\\.");
        String twoStr = Integer.toBinaryString(Integer.parseInt(strings[3]));
        char fir = twoStr.charAt(twoStr.length() - 1);
        Timber.d("isStartActivate char = %s", fir);
        return fir == '1';
    }

    /**
     * 反初始化
     */
    public void unInit() {
        if (mActivaionService != null) {
            mActivaionService.setNetActivateObserver(null);
            mActivaionService.unInit();
            ActivationModule.destroyInstance();
            mActivaionService = null;
        }
    }

    /**
     * 反初始化
     */
    public void unActiveInit() {
        if (mActivaionService != null) {
            Timber.d(" returnParam unActiveInit ");
            mActivaionService.setNetActivateObserver(null);
            mActivaionService.unInit();
        }
    }

    /**
     * 获取激活状态
     * @return
     */
    public int getActivateStatus() {
        int status = Activation.AUTO_UNKNOWN_ERROR;
        if (mActivaionService != null){
            status = mActivaionService.getActivateStatus();
        }
        return status;
    }

    public boolean isActivate() {
        int status = Activation.AUTO_UNKNOWN_ERROR;
        if (mActivaionService != null){
            status = mActivaionService.getActivateStatus();
        }
        return status == Service.ErrorCodeOK;
    }

    /**
     * 手动激活
     * @return
     */
    public ActivateReturnParam manualActivate(String szSerialNumber, String szActivateCode) {
        ActivateReturnParam returnParam = new ActivateReturnParam();
        if (mActivaionService != null){
            returnParam = mActivaionService.manualActivate(szSerialNumber, szActivateCode);
        }else {
            Timber.d("returnParam mActivaionService == null");
            returnParam.iErrorCode = Activation.AUTO_UNKNOWN_ERROR;
        }
        Timber.d("returnParam iErrorCode = %s, szOutputCode = %s", returnParam.iErrorCode, returnParam.szOutputCode);
        return returnParam;
    }

    /**
     * 网络激活(请先判断网络)
     * @return
     */
    public int netActivate() {
        if (SdkNetworkUtil.getInstance().isNetworkConnected()) {
            if (mActivaionService != null){
                String szUserCode = "0000000000";
                int resultCode = mActivaionService.netActivate(szUserCode);
                Timber.d("netActivate resultCode = %s", resultCode);
                return resultCode;
            } else {
                if(mNetObserver != null) {
                    mNetObserver.notifyNetActivate(Service.ErrorCodeFailed, ActivateConstant.ACTIVATE_TYPE_MANUAL);
                }
                return Service.ErrorCodeFailed;
            }
        } else {
            if(mNetObserver != null) {
                mNetObserver.notifyNetActivate(Service.ErrorCodeFailed, ActivateConstant.ACTIVATE_TYPE_MANUAL);
            }
            return Service.ErrorCodeFailed;
        }
    }

    /**
     * 批量激活
     * 备注：ActivaionService.getActivateStatus接口集成了激活状态获取与批量激活接口
     */
    public int bathActivate() {
        if(mActivaionService != null) {
            return mActivaionService.getActivateStatus();
        }
        return Service.ErrorCodeNotInit;
    }

    private ActivateNetObserver mNetObserver;
    /**
     * 设置激活结果回调
     * @param netObserver
     */
    public void setNotifyNetActivate(ActivateNetObserver netObserver) {
        this.mNetObserver = netObserver;
    }

    @Override
    public void onNetActivateResponse(int activateErrCode) {
        if(mNetObserver != null) {
            //网络激活失败，往外透传
            mNetObserver.notifyNetActivate(activateErrCode, ActivateConstant.ACTIVATE_TYPE_NET);
        }
    }

    public ActivationModule getActivationService() {
        if (mActivaionService == null) {
            mActivaionService = ActivationModule.getInstance();
        }
        return mActivaionService;
    }
}
