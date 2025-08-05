package com.autosdk.bussiness.data;

import static com.autonavi.gbl.data.model.DownLoadMode.DOWNLOAD_MODE_NET;

import com.autonavi.gbl.data.VoiceService;
import com.autonavi.gbl.data.model.DataType;
import com.autonavi.gbl.data.model.DownLoadMode;
import com.autonavi.gbl.data.model.OperationType;
import com.autonavi.gbl.data.model.TaskStatusCode;
import com.autonavi.gbl.data.model.Voice;
import com.autonavi.gbl.data.model.VoiceInitConfig;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.data.observer.IVoiceDataObserver;
import com.autosdk.common.AutoConstant;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 导航语音--标准，明星，方言
 */
public class VoiceDataController implements IVoiceDataObserver {

    private VoiceService mVoiceService;
    private int mInitCode;
    private static List<IVoiceDataObserver> mVoiceDataObservers = new ArrayList<>();

    public static VoiceDataController getInstance() {
        return VoiceDataControllerHolder.instance;
    }

    private static class VoiceDataControllerHolder {
        private static VoiceDataController instance = new VoiceDataController();
    }

    private VoiceDataController() {

    }

    public void initService() {
        mVoiceService = (VoiceService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.VoiceDataSingleServiceID);
        VoiceInitConfig voiceInitConfig = new VoiceInitConfig();
        voiceInitConfig.configfilePath = AutoConstant.VOICE_CONF_DIR;
        // 讯飞语音数据最终存储目录
        voiceInitConfig.flytekStoredPath = AutoConstant.FLYTEK_STORED_PATH;
        // MIT数据最终存储目录 目前对外开放的AutoSDK项目只支持讯飞语音，MIT语音暂未对外开放
//        voiceInitConfig.mitStoredPath = AutoConstant.MIT_STORED_PATH;

        mInitCode = mVoiceService.init(voiceInitConfig, this);
        mVoiceService.addNetDownloadObserver(this);
        Timber.d("initVoiceService: mInitCode = " + mInitCode);
    }

    /**
     * 是否初始化成功
     */
    public boolean isInitSuccess() {
        return mInitCode == Service.ErrorCodeOK;
    }

    public void unInit() {
        if(mVoiceService!=null){
            mVoiceService.unInit();
            mVoiceService = null;
        }
    }
    @Override
    public void onInit(int downLoadMode, int dataType, int opCode) {
        if (downLoadMode == DOWNLOAD_MODE_NET && dataType == DataType.DATA_TYPE_VOICE && opCode == Service.ErrorCodeOK) {
            Timber.d("mVoiceService 初始化成功");
        } else {
            Timber.d("mVoiceService 初始化失败");
        }
        for (IVoiceDataObserver observer : mVoiceDataObservers) {
            observer.onInit(downLoadMode, dataType, opCode);
        }
        Timber.d("mVoiceService onInit: downLoadMode=" + downLoadMode
                + "dataType=" + dataType + "opCode=" + opCode);
    }

    public int requestDataListCheck(int downLoadMode, String path) {
        if (mVoiceService != null) {
            return mVoiceService.requestDataListCheck(downLoadMode, path, this);
        }
        return 0;
    }

    public int requestDataImage(int downLoadMode,int voiceId) {
        if (mVoiceService != null) {
            return mVoiceService.requestDataImage(downLoadMode, voiceId, this);
        }
        return 0;
    }

    public void abortRequestDataListCheck(@DownLoadMode.DownLoadMode1 int downloadMode) {
        if (mVoiceService != null) {
            mVoiceService.abortRequestDataListCheck(downloadMode);
        }
    }

    public void abortRequestDataImage(@DownLoadMode.DownLoadMode1 int downloadMode,int voiceId) {
        if (mVoiceService != null) {
            mVoiceService.abortRequestDataImage(downloadMode,voiceId);
        }
    }
    public  ArrayList<Integer> getVoiceIdList(int downLoadMode) {
        if (mVoiceService != null) {
            return mVoiceService.getVoiceIdList(downLoadMode);
        }
        return null;
    }

    public  ArrayList<Integer> getVoiceIdList(int downLoadMode, int engineType) {
        if (mVoiceService != null) {
            return mVoiceService.getVoiceIdList(downLoadMode, engineType);
        }
        return null;
    }
    public Voice getVoice(int downLoadMode, int voiceId) {
        if (mVoiceService != null) {
            return mVoiceService.getVoice(downLoadMode, voiceId);
        }
        return null;
    }

    public void operate(@DownLoadMode.DownLoadMode1 int downLoadMode, @OperationType.OperationType1 int opType, ArrayList<Integer> voiceIdDiyLst) {
        if (mVoiceService != null) {
            mVoiceService.operate(downLoadMode, opType, voiceIdDiyLst);
        }
    }

    public void operateWorkingQueue(@DownLoadMode.DownLoadMode1 int downLoadMode, @OperationType.OperationType1 int opType, int engineType) {
        if (mVoiceService != null) {
            mVoiceService.operateWorkingQueue(downLoadMode, opType, engineType);
        }
    }

    // ============================ 观察者 ================================
    public void registerVoiceDataObserver(IVoiceDataObserver l) {
        mVoiceDataObservers.add(l);
    }

    public void unregisterVoiceDataObserver(IVoiceDataObserver l) {
        if (mVoiceDataObservers.contains(l)) {
            mVoiceDataObservers.remove(l);
        }
    }
    @Override
    public void onOperated(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, @OperationType.OperationType1 int opType,
                           ArrayList<Integer> opreatedIdList) {
        for (IVoiceDataObserver observer : mVoiceDataObservers) {
            observer.onOperated(downLoadMode, dataType, opType, opreatedIdList);
        }
    }

    @Override
    public void onDownLoadStatus(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id,
                                 @TaskStatusCode.TaskStatusCode1 int taskCode,  int opCode) {
        for (IVoiceDataObserver observer : mVoiceDataObservers) {
            observer.onDownLoadStatus(downLoadMode, dataType, id, taskCode, opCode);
        }
    }

    @Override
    public void onPercent(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id, int percentType, float percent) {
        for (IVoiceDataObserver observer : mVoiceDataObservers) {
            observer.onPercent(downLoadMode, dataType, id, percentType, percent);
        }
    }


    @Override
    public void onRequestDataListCheck(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType,  int opCode) {
        for (IVoiceDataObserver observer : mVoiceDataObservers) {
            observer.onRequestDataListCheck(downLoadMode, dataType, opCode);
        }
    }

    @Override
    public void onDownloadImage(int itemId, int opErrCode, String strFilePath, int dataType) {
        for (IVoiceDataObserver observer : mVoiceDataObservers) {
            observer.onDownloadImage(itemId, opErrCode, strFilePath,dataType);
        }
    }

}