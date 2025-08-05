package com.autosdk.bussiness.data;

import static com.autonavi.gbl.data.model.HotUpdateFileType.HOTUPDATE_FILE_GLOBAL;

import com.autonavi.gbl.data.HotUpdateService;
import com.autonavi.gbl.data.model.AutoInitResponseData;
import com.autonavi.gbl.data.model.DetailListParser;
import com.autonavi.gbl.data.model.HotUpdateCheckParam;
import com.autonavi.gbl.data.model.HotUpdateFileType;
import com.autonavi.gbl.data.model.MapNum;
import com.autonavi.gbl.data.observer.IHotUpdateCheckObserver;
import com.autonavi.gbl.data.observer.IHotUpdateFileObserver;
import com.autonavi.gbl.data.observer.IMapNumObserver;
import com.autonavi.gbl.guide.GuideService;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.common.task.TaskManager;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * HotUpdateService相关
 */
public class HotUpdateController implements IMapNumObserver,
        IHotUpdateFileObserver, IHotUpdateCheckObserver {
    private HotUpdateService mHotUpdateService;

    public static HotUpdateController getInstance() {
        return HotUpdateControllerHolder.instance;
    }


    private static class HotUpdateControllerHolder {
        private static HotUpdateController instance = new HotUpdateController();
    }

    private HotUpdateController() {

    }


    //请求文件热更回调
    private static List<IHotUpdateFileObserver> hotUpdateFileObservers = new ArrayList<>();
    //热更新初始化回调
    private static List<IHotUpdateCheckObserver> hotUpdateCheckObservers = new ArrayList<>();
    //地图数据审图号联网获取观察者
    private static List<IMapNumObserver> mapNumObservers = new ArrayList<>();

    public void setHotUpdateCheckObserver(IHotUpdateCheckObserver hotUpdateCheckObserver) {
        hotUpdateCheckObservers.add(hotUpdateCheckObserver);
    }

    public void removeHotUpdateCheckObserver(IHotUpdateCheckObserver hotUpdateCheckObserver) {
        hotUpdateCheckObservers.remove(hotUpdateCheckObserver);
    }

    public void setMapNumObserver(IMapNumObserver mapNumObserver) {
        mapNumObservers.add(mapNumObserver);
    }

    public void removeMapNumObserver(IMapNumObserver mapNumObserver) {
        mapNumObservers.remove(mapNumObserver);
    }

    public void setHotUpdateFileObserver(IHotUpdateFileObserver hotUpdateFileObserver) {
        hotUpdateFileObservers.add(hotUpdateFileObserver);
    }

    public void removeHotUpdateFileObserver(IHotUpdateFileObserver hotUpdateFileObserver) {
        hotUpdateFileObservers.remove(hotUpdateFileObserver);
    }

    public String path;

    public void initHotUpdateService(String path) {
        this.path = path;
        mHotUpdateService = (HotUpdateService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID
                .HotUpdateSingleServiceID);

        //热更新 global.db 引擎配置文件所在目录
        mHotUpdateService.init();
        HotUpdateCheckParam checkParam = new HotUpdateCheckParam();
        checkParam.strTbtVersion = getStrTbtVersion();
        requestCheckHotUpdate(checkParam);
    }


    public void unInit() {
        if (mHotUpdateService != null) {
            mHotUpdateService.unInit();
            mHotUpdateService = null;
            hotUpdateCheckObservers.clear();
            hotUpdateFileObservers.clear();
            mapNumObservers.clear();
        }
    }

    public String getVersion() {
        if (mHotUpdateService != null) {
            return mHotUpdateService.getVersion();
        }

        return null;
    }

    /**
     * 灌装开始前初始化
     */
    public DetailListParser initDataFill(String strDetaiListFile) {
        if (mHotUpdateService != null) {
            return mHotUpdateService.initDataFill(strDetaiListFile);
        }
        return null;
    }

    /**
     * 中断网络请求aos审图号信息
     */
    public void abortRequestMapNum() {
        if (mHotUpdateService != null) {
            mHotUpdateService.abortRequestMapNum();
        }
    }


    /**
     * 离线地图数据下载专用接口
     * 多次调用本接口时，前一次的调用传入的pObserver对象地址会被下一次调用传入的替换。
     * 调用者传入的pObserver对象地址, 当执行AbortRequestMapNum，或者UnInit，再或者IServiceMgr.UnInitBL后，将不再被使用。
     * thread: main
     *
     * @return int32_t          是否成功发起获取aos审图号的网络请求
     * -  1 发起网络请求成功，并通过pObserver回调结果
     * -  0 发起网络请求失败
     * -  -1 pObserver 观察者为空
     * @brief 网络请求aos审图号信息
     * @param[in/out] localMapNum      本地审图号信息
     */
    public int requestMapNum(MapNum localMapNum) {
        if (mHotUpdateService != null) {
            return mHotUpdateService.requestMapNum(localMapNum, this);
        }

        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param nTaskId 请求任务Id
     *                thread:multi
     * @brief 取消热更新请求
     */
    public void abortRequestHotUpdate(int nTaskId) {
        if (mHotUpdateService != null) {
            mHotUpdateService.abortRequestHotUpdate(nTaskId);
        }
    }

    /**
     * thread: main
     *
     * @return 请求的TaskId > 0 成功, 可用于abort该请求
     * - 0 调用失败
     * - -1 观察者为空
     * - -2 参数有误
     * @brief 请求检查热更新文件
     */
    public int requestCheckHotUpdate(HotUpdateCheckParam param) {
        if (mHotUpdateService != null) {
            return mHotUpdateService.requestCheckHotUpdate(param, this);
        }

        return Service.AUTO_UNKNOWN_ERROR;
    }


    public String getStrTbtVersion() {
        return GuideService.getEngineVersion();
    }


    /**
     * @param fileType   文件类型
     * @param strDstPath 下载保存路径
     *                   当 fileType 为 HOTUPDATE_FILE_GLOBAL strDstPath 无效，BL内部获取引擎指定的路径
     *                   该函数需要在 RequestHotUpdateInit 成功后调用
     * @return 请求的TaskId > 0 成功, 可用于abort该请求
     * - 0 调用失败
     * - -1 观察者为空
     * - -2 参数有误
     * - -3 当前文件已经是最新
     * @brief 请求下载热更新文件
     */
    public int requestHotUpdateFile(@HotUpdateFileType.HotUpdateFileType1 int fileType, String strDstPath) {
        if (mHotUpdateService != null) {
            return mHotUpdateService.requestHotUpdateFile(fileType, strDstPath, this);
        }

        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @return @param[out]
     * - ErrorCodeOK  成功
     * - 其他          失败（参考ErrorCode定义）
     * @brief 重置热更文件版本号
     * @param[in] fileType        文件类型 热更以后，根据传文件类型的index
     * @note 重置后将清除 bl_cach 中对应文件的版本信息，下次调用热更检查 服务端下发新的版本号
     */
    int resetFileVersion(@HotUpdateFileType.HotUpdateFileType1 int fileType) {
        return mHotUpdateService.resetFileVersion(fileType);
    }


    /**
     * @param fileType   文件类型
     * @param strDstPath 文件保存路径
     *                   当 fileType 为 HOTUPDATE_FILE_GLOBAL strDstPath 无效，BL内部获取引擎指定的路径
     *                   该函数需要在 RequestHotUpdateFile 成功后调用
     * @return ErrorCode       返回GBL模块错误码
     * - ErrorCodeOK  成功
     * - 其他          失败（参考ErrorCode定义）
     * @brief 保存热更新文件到指定目录
     */
    public int saveHotUpdateFile(@HotUpdateFileType.HotUpdateFileType1 int fileType, String strDstPath) {
        if (mHotUpdateService != null) {
            return mHotUpdateService.saveHotUpdateFile(fileType, strDstPath);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    @Override
    public void onInitNotify( int opErrCode, AutoInitResponseData data) {
        for (IHotUpdateCheckObserver observer : hotUpdateCheckObservers) {
            observer.onInitNotify(opErrCode, data);
        }
        if (Service.ErrorCodeOK == opErrCode) {
            TaskManager.post(new Runnable() {
                @Override
                public void run() {
                    requestHotUpdateGlobalDB();
                }
            });
            Timber.i("Service.ErrorCodeOK");
        } else {
            Timber.i("RequestCheckHotUpdate error");
        }
    }

    private void requestHotUpdateGlobalDB() {
        requestHotUpdateFile(HOTUPDATE_FILE_GLOBAL, path);
    }

    @Override
    public void onHotUpdateFile(@HotUpdateFileType.HotUpdateFileType1 int fileType,  int opErrCode, String filePath) {
        for (IHotUpdateFileObserver observer : hotUpdateFileObservers) {
            observer.onHotUpdateFile(fileType, opErrCode, filePath);
        }
        if (Service.ErrorCodeOK == opErrCode) {
            if (HOTUPDATE_FILE_GLOBAL == fileType) {
                saveHotUpdateFile(fileType, path);
            }
        }
    }

    @Override
    public void onRequestMapNum(int i, MapNum mapNum) {
        for (IMapNumObserver observer : mapNumObservers) {
            observer.onRequestMapNum(i, mapNum);
        }
    }
}
