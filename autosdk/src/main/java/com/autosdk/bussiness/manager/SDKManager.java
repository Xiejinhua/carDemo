package com.autosdk.bussiness.manager;

import android.app.Application;

import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.servicemanager.model.ALCGroup;
import com.autonavi.gbl.servicemanager.model.BLInitParam;
import com.autonavi.gbl.servicemanager.model.BaseInitParam;
import com.autonavi.gbl.util.model.KeyValue;
import com.autonavi.gbl.util.observer.IPlatformInterface;
import com.autosdk.bussiness.account.AccountController;
import com.autosdk.bussiness.account.BehaviorController;
import com.autosdk.bussiness.account.SyncSdkController;
import com.autosdk.bussiness.account.UserGroupController;
import com.autosdk.bussiness.account.UserTrackController;
import com.autosdk.bussiness.aos.AosController;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.bussiness.data.HotUpdateController;
import com.autosdk.bussiness.data.MapDataController;
import com.autosdk.bussiness.data.ThemeDataController;
import com.autosdk.bussiness.information.InformationController;
import com.autosdk.bussiness.layer.CardController;
import com.autosdk.bussiness.layer.LayerController;
import com.autosdk.bussiness.location.LocationController;
import com.autosdk.bussiness.map.MapController;
import com.autosdk.bussiness.navi.NaviController;
import com.autosdk.bussiness.push.PushController;
import com.autosdk.bussiness.scene.SceneModuleController;
import com.autosdk.bussiness.search.SearchController;
import com.autosdk.bussiness.search.SearchControllerV2;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;

import java.util.ArrayList;

import timber.log.Timber;

public class SDKManager {
    private static final String LOG_TAG = SDKManager.class.getSimpleName();
    private static SDKManager mInstance = null;
    private MapController mMapController = null;
    private IPlatformDepends mDepends = null;

    private SDKManager() {
    }

    public static synchronized SDKManager getInstance() {
        if (mInstance == null) {
            mInstance = new SDKManager();
        }
        return mInstance;
    }


    public synchronized MapController getMapController() {
        return MapController.getInstance();
    }

    public synchronized LayerController getLayerController() {
        return LayerController.getInstance();
    }

    public synchronized LocationController getLocController() {
        return LocationController.getInstance();
    }

    public synchronized NaviController getNaviController() {
        return NaviController.getInstance();
    }

    /**
     * 获取地图数据控制器
     */
    public synchronized MapDataController getMapDataController() {
        return MapDataController.getInstance();
    }

    /**
     * 获取搜索控制器
     */
    public synchronized SearchController getSearchController() {
        return SearchController.getInstance();
    }

    public synchronized SearchControllerV2 getSearchControllerV2() {
        return SearchControllerV2.getInstance();
    }

    /**
     * 初始化SDK
     */
    public synchronized int initBaseLibs(Application context, IPlatformDepends depends, SDKInitParams initParams) {

        int nRet = -1;
        if (null == context || null == depends || null == initParams) {
            return nRet;
        }
        mDepends = depends;
        nRet = initBaseLibs(initParams);
        return nRet;
    }

    /**
     * 反初始化SDK
     */
    public synchronized void unInit() {
        Timber.d("unInit");
        NaviController.getInstance().uninit();
        LocationController.getInstance().uninitLocEngine();
        SearchController.getInstance().uninit();
        SearchControllerV2.getInstance().uninit();
        LayerController.getInstance().uninit();
        CardController.unInitCardController();
        MapController.getInstance().uninit();
        AccountController.getInstance().uninit();
        SyncSdkController.getInstance().uninit();
        UserTrackController.getInstance().destroyUserTrackService();
        BehaviorController.getInstance().uninit();
        MapDataController.getInstance().unInit();
        ThemeDataController.getInstance().unInit();
        AosController.getInstance().unInit();
        UserGroupController.getInstance().unInit();
        HotUpdateController.getInstance().unInit();
        PushController.getInstance().destroy();
        SceneModuleController.getInstance().unInit();
        InformationController.getInstance().unInit();
        unitBL();
        unitLib();
        Timber.d("unInit end");
    }

    /**
     * 获取SDK版本号
     *
     * @return
     */
    public String getVersion() {
        return ServiceMgr.getVersion();
    }

    public String getEngineVersion() {
        return ServiceMgr.getEngineVersion();
    }

    public String getMapDataEngineVersion() {
        return MapDataController.getInstance().getEngineVersion();
    }

    private int initBaseLibs(SDKInitParams initParams) {
        BaseInitParam param = new BaseInitParam();

        /* 日志相关配置 */
        param.logFileName = initParams.logFileName;
        param.logPath = initParams.logPath;
        param.logLevel = initParams.logLevel;
        param.groupMask = ALCGroup.GROUP_MASK_ALL;
        param.async = initParams.bLogAsync;
        param.bLogcat = initParams.bSDKLogcat;

        /* 服务器类型，默认伟正式发布环境（不建议使用测试生产环境） */
        param.serverType = initParams.serverType;


        param.aosDBPath = initParams.cookieDBPath;
        param.assetPath = initParams.assetPath;
        param.cachePath = initParams.cachePath;
        param.userDataPath = initParams.userDataPath;
        param.channelName = initParams.channelName;
        param.checkMode = initParams.checkMode;

        param.setIPlatformInterface(mPlatformUtil);
        int resultCode = ServiceMgr.getServiceMgrInstance().initBaseLibs(param, BusinessApplicationUtils.getApplication());

        Timber.d("initBaseLibs resultCode = %s", resultCode);

        return resultCode;
    }

    public int initBL(Application context, SDKInitParams initParams) {
        BLInitParam blInitParam = new BLInitParam();
        //配置文件路径
        blInitParam.dataPath.cfgFilePath = initParams.cfgFilePath;
        //离线地图
        blInitParam.dataPath.offlinePath = initParams.offlinePath;
        //精品三维地图
        blInitParam.dataPath.off3DDataPath = initParams.offlinePath;
        //云加端存放路径
        blInitParam.dataPath.onlinePath = initParams.onlinePath;
        //配置车道级离线地图数据
        blInitParam.dataPath.lndsOfflinePath = initParams.lndsOfflinePath;

        FileUtils.createDir(blInitParam.dataPath.offlinePath);
        FileUtils.createDir(blInitParam.dataPath.onlinePath);

        int resultCode = ServiceMgr.getServiceMgrInstance().initBL(blInitParam, context);

        Timber.d("initBL resultCode = %s", resultCode);

        return resultCode;
    }


    public void unitLib() {
        ServiceMgr.getServiceMgrInstance().unInitBaseLibs();
    }

    private void unitBL() {
        ServiceMgr.getServiceMgrInstance().unInitBL();
    }

    RestPlatformInterface mPlatformUtil = new RestPlatformInterface();

    class RestPlatformInterface implements IPlatformInterface {
        @Override
        public void copyAssetFile(String assetFilePath, String destFilePath) {
        }

        @Override
        public float getDensity(int deviceId) {
            return 0.0f; //AutoApplication.getApplication().getResources().getDisplayMetrics().density;
        }

        @Override
        public int getDensityDpi(int deviceId) {
            return 0;//AutoApplication.getApplication().getResources().getDisplayMetrics().densityDpi;
        }

        @Override
        public int getNetStatus() {
            int netStatus = 0;
            if (null != mDepends) {
                netStatus = mDepends.getNetStatus();
            }
            return netStatus;
        }

        @Override
        public ArrayList<KeyValue> getCdnNetworkParam() {

            ArrayList<KeyValue> keyValues = new ArrayList<>();
            /**< diu    设备唯一号,android--imei, ios--IDFV */

            String strDiu = new String();
            if (null != mDepends) {
                strDiu = mDepends.getDIU();
            }

            keyValues.add(new KeyValue("diu", strDiu));
            return keyValues;
        }

        @Override
        public boolean getAosNetworkParam(ArrayList<KeyValue> arrayList) {

            String strDiu = new String();
            if (null != mDepends) {
                strDiu = mDepends.getDIU();
            }
            arrayList.add(new KeyValue("diu", strDiu));
            arrayList.add(new KeyValue("adiu", strDiu));
            if (null != mDepends) {
                arrayList.add(new KeyValue("client_network_class", mDepends.getNetStatus() + ""));//默认设置为wifi类型
            }else {
                arrayList.add(new KeyValue("client_network_class", "4"));//默认设置为wifi类型
            }
            return true;
        }

        @Override
        public String amapEncode(byte[] strInput) {
            String strTemp = new String();
            return strTemp;
        }

        @Override
        public String amapEncodeBinary(byte[] binaryInput) {
            String strTemp = new String();
            return strTemp;
        }

        @Override
        public String amapDecode(byte[] bytes) {
            String strTemp = new String();
            return strTemp;
        }

        @Override
        public boolean getAosSign(String s, String[] strings) {
            if (strings.length == 0) {
                return false;
            }
            strings[0] = "";        // 这是个出参，仅包含一个元素，需赋值过去
            return true;
        }
    }
}
