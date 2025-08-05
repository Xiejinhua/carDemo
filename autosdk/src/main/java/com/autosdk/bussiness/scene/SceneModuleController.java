package com.autosdk.bussiness.scene;

import java.util.ArrayList;

import com.autonavi.gbl.common.model.FatigueInfo;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.scene.BaseSceneModule;
import com.autonavi.gbl.scene.SceneModuleService;
import com.autonavi.gbl.scene.dynamic.DynamicCloudShowInfoModule;
import com.autonavi.gbl.scene.model.InitSceneModuleParam;
import com.autonavi.gbl.scene.model.ModuleInitStatus;
import com.autonavi.gbl.scene.model.SceneModuleID;
import com.autonavi.gbl.scene.observer.IDynamicCloudShowInfoObserver;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.ServiceInitStatus;
import com.autonavi.gbl.util.model.SingleServiceID;

public class SceneModuleController {

    private static final String TAG = "SceneModuleController";
    private SceneModuleService mSceneModuleService;

    private SceneModuleController() {

    }

    private static class SceneModuleControllerHolder {
        private static SceneModuleController instance = new SceneModuleController();
    }

    public static SceneModuleController getInstance() {
        return SceneModuleController.SceneModuleControllerHolder.instance;
    }

    public void init() {
        if (mSceneModuleService == null) {
            // 获取场景组件服务（一级服务）
            mSceneModuleService = (SceneModuleService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.SceneModuleSingleServiceID);
            // 统一初始化场景组件服务
            InitSceneModuleParam param = new InitSceneModuleParam();
            //开启动态运营组件
            param.bEnableDynamicCloudShowInfoModule = true;
            mSceneModuleService.init(param);
        }
    }

    public void addDynamicCloudShowInfoObserver(IDynamicCloudShowInfoObserver iDynamicCloudShowInfoObserver){
        DynamicCloudShowInfoModule dynamicCloudShowInfoModule = assignSceneModule(SceneModuleID.SceneModuleCloudShowInfoID);
        if (isSceneModuleInit(dynamicCloudShowInfoModule) && iDynamicCloudShowInfoObserver != null) {
            dynamicCloudShowInfoModule.addDynamicCloudShowInfoObserver(iDynamicCloudShowInfoObserver);
        }
    }

    private boolean isSceneModuleInit(BaseSceneModule baseSceneModule){
        return baseSceneModule != null && baseSceneModule.isInit() == ModuleInitStatus.ModuleInitDone;
    }


    private <T extends BaseSceneModule> T assignSceneModule(@SceneModuleID.SceneModuleID1 int type){
        if (mSceneModuleService != null && mSceneModuleService.isInit() == ServiceInitStatus.ServiceInitDone) {
            return (T)mSceneModuleService.getModuleByType(type);
        }
        return null;
    }

    /**
     * 请求动态tip信息
     * @param pathInfo
     */
    public void requestTipsInfo(PathInfo pathInfo){
        DynamicCloudShowInfoModule dynamicCloudShowInfoModule = assignSceneModule(SceneModuleID.SceneModuleCloudShowInfoID);
        if (pathInfo != null && isSceneModuleInit(dynamicCloudShowInfoModule)) {
            dynamicCloudShowInfoModule.requestTipsInfo(pathInfo);
        }
    }


    /**
     * 上报疲劳度
     * @param fatigueInfoList
     */
    public void reportFatigueInfo(ArrayList<FatigueInfo> fatigueInfoList){
        DynamicCloudShowInfoModule dynamicCloudShowInfoModule = assignSceneModule(SceneModuleID.SceneModuleCloudShowInfoID);
        if(isSceneModuleInit(dynamicCloudShowInfoModule)) {
            boolean result = dynamicCloudShowInfoModule.dynamicFatigueNotify(fatigueInfoList);
        }
    }


    public void removeDynamicCloudShowInfoObserver(IDynamicCloudShowInfoObserver iDynamicCloudShowInfoObserver){
        DynamicCloudShowInfoModule dynamicCloudShowInfoModule = assignSceneModule(SceneModuleID.SceneModuleCloudShowInfoID);
        if (dynamicCloudShowInfoModule != null && iDynamicCloudShowInfoObserver != null) {
            dynamicCloudShowInfoModule.removeDynamicCloudShowInfoObserver(iDynamicCloudShowInfoObserver);
        }
    }

    public void unInit() {
        if (mSceneModuleService != null) {
            mSceneModuleService.unInit();
            mSceneModuleService = null;
        }
    }

}
