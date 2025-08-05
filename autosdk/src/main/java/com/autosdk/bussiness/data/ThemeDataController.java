package com.autosdk.bussiness.data;

import com.autonavi.gbl.data.ThemeService;
import com.autonavi.gbl.data.model.DataType;
import com.autonavi.gbl.data.model.DownLoadMode;
import com.autonavi.gbl.data.model.OperationType;
import com.autonavi.gbl.data.model.TaskStatusCode;
import com.autonavi.gbl.data.model.Theme;
import com.autonavi.gbl.data.model.ThemeInitConfig;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.data.observer.IThemeDataObserver;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by AutoSdk on 2020/12/2.
 **/
public class ThemeDataController implements IThemeDataObserver {

    private ThemeService mThemeService;
    private int mInitCode;
    private IThemeDataObserver mThemeDataObserver;

    public static ThemeDataController getInstance() {
        return ThemeDataControllerHolder.instance;
    }

    private static class ThemeDataControllerHolder {
        private static ThemeDataController instance = new ThemeDataController();
    }

    private ThemeDataController() {

    }

    public void initService(String strConfigfilePath, String themeStrdownloadpath, String dataVersion) {
        mThemeService = (ThemeService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.ThemeDataSingleServiceID);
        ThemeInitConfig themeInitConfig = new ThemeInitConfig();
        // themedata.json 配置文件所存放的目录
        themeInitConfig.configfilePath = strConfigfilePath;
        // 离线数据存储路径
        themeInitConfig.storedPath = themeStrdownloadpath;
        // 设置磁盘空间安全阈值（默认设置为80MB）
        themeInitConfig.thresholdValue = 80;
        //数据版本
        themeInitConfig.dataVersion = dataVersion;

        mInitCode = mThemeService.init(themeInitConfig, this);
        mThemeService.addNetDownloadObserver(this);
        Timber.d("initThemeService: mInitCode = " + mInitCode);
    }

    /**
     * 是否初始化成功
     */
    public boolean isInitSuccess() {
        return mInitCode == 1;
    }

    public void unInit() {
        if(mThemeService!=null){
            mThemeService.unInit();
            unregisterThemeDataObserver();
            mThemeService = null;
        }
    }
    @Override
    public void onInit(int downLoadMode, int dataType, int opCode) {
        if (downLoadMode == 0 && dataType == DataType.DATA_TYPE_THEME && opCode == 0) {
            Timber.d("ThemeService 初始化成功");
            requestDataListCheck(DownLoadMode.DOWNLOAD_MODE_NET, "");
        } else {
            Timber.d("ThemeService 初始化失败");
        }
        if (null != mThemeDataObserver) {
            mThemeDataObserver.onInit(downLoadMode, dataType, opCode);
        }
        Timber.d("mThemeService onInit: downLoadMode=" + downLoadMode
                + "dataType=" + dataType + "opCode=" + opCode);
    }

    public int requestDataListCheck(int downLoadMode, String path) {
        if (mThemeService != null) {
            return mThemeService.requestDataListCheck(downLoadMode, path, this);
        }
        return 0;
    }

    public int requestThemeImage(int downLoadMode,int themeId) {
        if (mThemeService != null) {
            return mThemeService.requestDataImage(downLoadMode, themeId, this);
        }
        return 0;
    }

    public void abortDataListCheck(@DownLoadMode.DownLoadMode1 int downloadMode) {
        if (mThemeService != null) {
            mThemeService.abortRequestDataListCheck(downloadMode);
        }
    }

    public void abortrequestThemeImag(@DownLoadMode.DownLoadMode1 int downloadMode,int themeId) {
        if (mThemeService != null) {
            mThemeService.abortRequestDataImage(downloadMode,themeId);
        }
    }
    public  ArrayList<Integer> getThemeIdList(int downLoadMode) {
        if (mThemeService != null) {
            return mThemeService.getThemeIdList(downLoadMode);
        }
        return null;
    }

    public  ArrayList<Theme> getThemeList(int downLoadMode) {
        if (mThemeService != null) {
            return mThemeService.getThemeList(downLoadMode);
        }
        return null;
    }

    public  ArrayList<Theme> getThemeList(int downLoadMode,String dataVersion) {
        if (mThemeService != null) {
            return mThemeService.getThemeList(downLoadMode,dataVersion);
        }
        return new ArrayList<>();
    }

    public void operate(@DownLoadMode.DownLoadMode1 int downLoadMode, @OperationType.OperationType1 int opType, ArrayList<Integer> themeIdDiyLst) {
        if (mThemeService != null) {
            mThemeService.operate(downLoadMode,opType,themeIdDiyLst);
        }
    }

    // ============================ 观察者 ================================
    public void registerThemeDataObserver(IThemeDataObserver l) {
        mThemeDataObserver = l;
    }

    public void unregisterThemeDataObserver() {
        mThemeDataObserver = null;
    }
    @Override
    public void onOperated(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, @OperationType.OperationType1 int opType,
                           ArrayList<Integer> opreatedIdList) {
        if (null != mThemeDataObserver) {
            mThemeDataObserver.onOperated(downLoadMode, dataType, opType, opreatedIdList);
        }
    }

    @Override
    public void onDownLoadStatus(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id,
                                 @TaskStatusCode.TaskStatusCode1 int taskCode,  int opCode) {
        if (null != mThemeDataObserver) {
            mThemeDataObserver.onDownLoadStatus(downLoadMode, dataType, id, taskCode, opCode);
        }
    }

    @Override
    public void onPercent(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id, int percentType, float percent) {
        if (null != mThemeDataObserver) {
            mThemeDataObserver.onPercent(downLoadMode, dataType, id, percentType, percent);
        }
    }


    @Override
    public void onRequestDataListCheck(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType,  int opCode) {
        if (null != mThemeDataObserver) {
            mThemeDataObserver.onRequestDataListCheck(downLoadMode, dataType, opCode);
        }
    }

    @Override
    public void onDownloadImage(int itemId, int opErrCode, String strFilePath, int dataType) {
        if (null != mThemeDataObserver) {
            mThemeDataObserver.onDownloadImage(itemId, opErrCode, strFilePath,dataType);
        }
    }

}
