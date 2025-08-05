package com.autosdk.bussiness.account;

import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.user.model.BehaviorDataType;
import com.autonavi.gbl.user.syncsdk.SyncSdkService;
import com.autonavi.gbl.user.syncsdk.model.SyncEventType;
import com.autonavi.gbl.user.syncsdk.model.SyncRet;
import com.autonavi.gbl.user.syncsdk.model.SyncSdkServiceParam;
import com.autonavi.gbl.user.syncsdk.observer.ISyncSDKServiceObserver;
import com.autonavi.gbl.util.model.ServiceInitStatus;
import com.autonavi.gbl.util.model.SingleServiceID;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 同步服务M层
 */
public class SyncSdkController implements ISyncSDKServiceObserver {
    private int syncSdk = ServiceInitStatus.AUTO_UNKNOWN_ERROR;

    private SyncSdkService mSyncSdkService;

    private static class SyncSdkHolder {
        private static SyncSdkController mInstance = new SyncSdkController();
    }


    private SyncSdkController() {
    }

    public static SyncSdkController getInstance() {
        return SyncSdkHolder.mInstance;
    }

    public SyncSdkService getSyncSdkService() {
        return mSyncSdkService;
    }

    /**
     * 初始化tbt，在UI线程中初始化。
     */
    public boolean init(String cacheDir) {
        initSyncSdkService(cacheDir);
        return isBehaviorSuccess();
    }

    private void initSyncSdkService(String cacheDir) {
        if (mSyncSdkService != null) {
            return;
        }

        mSyncSdkService = (SyncSdkService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.SyncSdkSingleServiceID);
        SyncSdkServiceParam param = new SyncSdkServiceParam();
        param.dataPath = cacheDir;
        File file = new File(param.dataPath,"403");
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            Timber.i("initSyncSdkService: mkdirs = " + mkdirs);
        }
        int initCode = mSyncSdkService.init(param);
        Timber.i("initSyncSdkService: init1=" + initCode);
        syncSdk = mSyncSdkService.isInit();

        Timber.i("initSyncSdkService: init2=" + syncSdk);

        mSyncSdkService.addObserver(this);
    }

    public boolean isBehaviorSuccess() {
        return syncSdk == 3;
    }

    public void uninit() {
        iSyncSDKServiceObservers.clear();
        //不再回调任何信息
        destorySyncSDKService();
        syncSdk = -9999;
    }


    private void destorySyncSDKService() {
        if (mSyncSdkService != null) {
            mSyncSdkService.removeObserver(this);
            mSyncSdkService = null;
        }
    }


    // ============================ UserTrackService 观察者 ================================
    private static final List<ISyncSDKServiceObserver> iSyncSDKServiceObservers = new ArrayList<>();

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 添加轨迹服务观察者
     * @param[in] ob:              轨迹服务观察者
     * @note thread:mutil
     */
    public void registerISyncSDKServiceObserver(ISyncSDKServiceObserver ob) {
        iSyncSDKServiceObservers.add(ob);
    }

    /**
     * @return void
     * @brief 移除轨迹服务观察者
     * @param[in] ob:              轨迹服务观察者
     * @note thread:mutil
     */
    public void unregisterISyncSDKServiceObserver(ISyncSDKServiceObserver ob) {
        iSyncSDKServiceObservers.remove(ob);
    }


    // ============================ UserTrackService 服务方法 ================================

    /**
     * @return const dice::String16  版本号
     * @brief 获取版本号
     * @note thread:multi
     */
    public String getVersion() {
        if (mSyncSdkService != null) {
            return mSyncSdkService.getVersion();
        }

        return null;
    }

    /**
     * @return void
     * @brief 日志开关
     * @param[in] nLevel           日志层级
     * @note thread:multi
     */
    public void logSwitch(int nLevel) {
        if (mSyncSdkService != null) {
            mSyncSdkService.setSDKLogLevel(nLevel);
        }
    }


    /**
     * @return dice::String16   同步SDK版本信息
     * @brief 获取同步SDK版本信息
     * @note thread:multi
     */
    public String getSDKVersion() {
        if (mSyncSdkService != null) {
            return mSyncSdkService.getSDKVersion();
        }
        return null;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 手动开始同步
     * @note thread:multi
     */
    public int startSync() {
        if (mSyncSdkService != null) {
            return mSyncSdkService.startSync();
        }
        return -2;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 是否正在同步
     * @note thread:multi
     */
    public int isSyncing() {
        if (mSyncSdkService != null) {
            return mSyncSdkService.isSyncing();
        }
        return -2;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 是否触发合并
     * @param[in] merge            是否触发合并
     * @note thread:multi
     */
    public int confirmMerge(boolean merge) {
        if (mSyncSdkService != null) {
            return mSyncSdkService.confirmMerge(merge);
        }
        return -2;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * @brief 是否触发合并指定行为数据类型
     * @param[in] type             行为数据类型
     * @param[in] merge            是否触发合并
     * @note thread:multi
     */
    public int confirmMerge(@BehaviorDataType.BehaviorDataType1 int type, boolean merge) {
        if (mSyncSdkService != null) {
            return mSyncSdkService.confirmMerge(type, merge);
        }
        return -2;
    }

    // ============================ ISyncSDKServiceObserver 观察回调 ================================

    /**
     * @return void
     * @brief 同步SDK回调通知
     * @param[in] eventType        同步SDK回调事件类型
     * @param[in] exCode           同步SDK返回值
     */
    @Override
    public void notify(@SyncEventType.SyncEventType1 int eventType, @SyncRet.SyncRet1 int exCode) {
        Timber.d(eventType + "");
        for (ISyncSDKServiceObserver observer : iSyncSDKServiceObservers) {
            observer.notify(eventType,exCode);
        }
    }
}
