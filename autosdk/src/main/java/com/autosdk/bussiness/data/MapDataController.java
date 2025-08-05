package com.autosdk.bussiness.data;

import static com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_DELETE;

import com.autonavi.gbl.data.MapDataService;
import com.autonavi.gbl.data.model.AdminCode;
import com.autonavi.gbl.data.model.Area;
import com.autonavi.gbl.data.model.AreaExtraInfo;
import com.autonavi.gbl.data.model.CityDownLoadItem;
import com.autonavi.gbl.data.model.CityItemInfo;
import com.autonavi.gbl.data.model.DataErrorType;
import com.autonavi.gbl.data.model.DataInitParam;
import com.autonavi.gbl.data.model.DataType;
import com.autonavi.gbl.data.model.DownLoadMode;
import com.autonavi.gbl.data.model.InitConfig;
import com.autonavi.gbl.data.model.MapDataFileType;
import com.autonavi.gbl.data.model.MergedStatusInfo;
import com.autonavi.gbl.data.model.OperationType;
import com.autonavi.gbl.data.model.ProvinceInfo;
import com.autonavi.gbl.data.model.TaskStatusCode;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.data.observer.IMapDataObserver;

import java.util.ArrayList;

import androidx.annotation.Nullable;

import timber.log.Timber;

/**
 * MapDataService相关
 */
public class MapDataController implements IMapDataObserver {
    private MapDataService mMapDataService;

    private int mInitCode;

    public static MapDataController getInstance() {
        return MapDataControllerHolder.instance;
    }


    private static class MapDataControllerHolder {
        private static MapDataController instance = new MapDataController();
    }

    private MapDataController() {

    }

    public void initService(String strConfigfilePath, String mapStrdownloadpath, int mapDataMode) {
        mMapDataService = (MapDataService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.MapDataSingleServiceID);
        InitConfig mMapDataServiceInitConfig = new InitConfig();
        //下载路径，设置后没用
        mMapDataServiceInitConfig.strStoredPath = "";
        //all_city_compile.json、global.db文件目录
        mMapDataServiceInitConfig.strConfigfilePath = strConfigfilePath;
        //数据下载存放路径，目前仅地图下载支持该参数配置，语音下载暂不支持
        mMapDataServiceInitConfig.strDownloadPath = mapStrdownloadpath;
        ArrayList<DataInitParam> extendedList = new ArrayList<>(1);
        mMapDataServiceInitConfig.extendedParamList = extendedList;

        // 【4.1.8】离线初始化加“是否遍历本地城市数据”开关
        // 初始化时检测本地数据版本功能开关
        DataInitParam dataInitParam = new DataInitParam();
        dataInitParam.strName = "check_local_version";
        dataInitParam.strValue = "1";
        extendedList.add(dataInitParam);
        //模块初始化观察者
        //mMapDataServiceInitConfig.mapDataMode = MapDataMode.MAP_DATA_MODE_BASE;
        mMapDataServiceInitConfig.mapDataMode = mapDataMode;
        mInitCode = mMapDataService.init(mMapDataServiceInitConfig, this);
        mMapDataService.addNetDownloadObserver(this);
        mMapDataService.addUsbDownloadObserver(this);
        //设置异常数据监听观察者
        mMapDataService.setErrorDataObserver(this);
        mMapDataService.setIMergedStatusInfoObserver(this);
        Timber.d("initService: mInitCode = " + mInitCode);
    }


    // ============================ 观察者 ================================
    private IMapDataObserver mapDataObserver;

    public void registerMapDataObserver(IMapDataObserver lMapDataObserver) {
        mapDataObserver = lMapDataObserver;
    }

    public void unregisterMapDataObserver() {
        mapDataObserver = null;
    }

    public IMapDataObserver getMapDataObserver() {
        return mapDataObserver;
    }


    public void unInit() {
        if (mMapDataService != null) {
            mMapDataService.unInit();
            //移除所有观察者
            unregisterMapDataObserver();
            mMapDataService = null;
        }
    }

    public MapDataService getMapDataService() {
        return mMapDataService;
    }


    /**
     * 根据经纬度解析获取城市adcode
     * 若解析失败,则返回0
     * 调用该函数只需要获取MapDataService即可，不要求初始化成功
     */
    public int getAdcodeByLonLat(double lon, double lat) {
        if (null == mMapDataService){
            return 0;
        }
        return mMapDataService.getAdcodeByLonLat(lon, lat);
    }

    /**
     * 通过adcode获取城市信息
     * 若解析失败,则返回null
     */
    @Nullable
    public CityItemInfo getCityInfo(int adCode) {
        return mMapDataService.getCityInfo(adCode);
    }

    /**
     * 获取所有城市下载信息
     */
    @Nullable
    public ArrayList<CityItemInfo> getCityList() {
        return mMapDataService.getCityInfoList();
    }

    /**
     * 获取所有离线数据版本信息
     */
    public String getAllDataFileVersion(int adCode) {
        return "adCode: " + adCode +
                "\nm1.ans:\t" + getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_MAP) +
                "\nm2.ans:\t" + getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_ROUTE) +
                "\nm2_lane.ans:\t" + getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_ROUTE_LANE) +
                "\nm2_adas.ans:\t" + getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_ROUTE_ADAS) +
                "\nm3.ans:\t" + getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_POI) +
                "\nm4_pro.ans:\t" + getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_3D) +
                "\nm5a.ans:\t" + getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_JV) +
                "\nm5b.ans:\t" + getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_JVLINK);
    }

    /**
     * <p>
     * 获取离线数据版本号
     */
    public String getDataFileVersion(int adCode, @MapDataFileType.MapDataFileType1 int fileType) {
        if (mMapDataService != null) {
            return mMapDataService.getDataFileVersion(adCode, fileType);
        }
        return null;
    }

    /**
     * 设置当前城市的行政编码
     */
    public void setCurrentCityAdcode(int adcode) {
        if (mMapDataService != null) {
            mMapDataService.setCurrentCityAdcode(adcode);
        }
    }

    public int getAdcode(int urcode) {
        if (mMapDataService != null) {
            return mMapDataService.getAdcode(urcode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * 匹配省份任一子城市离线数据的下载状态
     * @param adCode 省份行政编码
     * @param state 状态
     * @return
     */
    public boolean findDownLoadItemAnyTaskState(int adCode, @TaskStatusCode.TaskStatusCode1 int... state){
        ProvinceInfo provinceInfo = getProvinceInfo(adCode);
        if (null == provinceInfo || null == provinceInfo.cityInfoList || provinceInfo.cityInfoList.isEmpty()) {
            return false;
        }
        for (CityItemInfo itemInfo : provinceInfo.cityInfoList) {
            CityDownLoadItem item = getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, itemInfo.cityAdcode);
            if (item != null) {
                for (int j : state) {
                    if (j == item.taskState) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public long getProvinceFullZipSize(int adCode){
        long fullZipSize = 0L;
        ProvinceInfo provinceInfo = getProvinceInfo(adCode);
        if (provinceInfo != null && provinceInfo.cityInfoList != null) {
            for (CityItemInfo itemInfo : provinceInfo.cityInfoList) {
                CityDownLoadItem item = getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, itemInfo.cityAdcode);
                if (item != null) {
                    fullZipSize += item.nFullZipSize.longValue();
                }
            }
        }
        return fullZipSize;
    }

    /**
     * 检测磁盘中是否有效数据
     *
     * @param downLoadMode 数据下载模式
     * @param path         路径, 当【downLoadMode = DOWNLOAD_MODE_USB】时，path传入已知存在U盘路径。其他模式可以传""
     *                     离线地图数据下载专用接口
     *                     需要等Init的bl::IDataInitObserver * pObserver初始化OnInit回调后，才能调用本接口。
     *                     下载模式downLoadMode相同，多次调用本接口时，前一次的调用传入的pObserver对象地址会被下一次调用传入的替换。
     *                     调用者传入的pObserver对象地址, 当执行AbortRequestDataListCheck，或者UnInit，再或者IServiceMgr.UnInitBL后，将不再被使用。
     *                     thread: main
     * @return int32_t          是否成功发起检测数据列表的请求
     * -  0 发起请求成功，并通过pObserver回调结果
     */
    public int checkDataInDisk(int downLoadMode, String path) {
        if (mMapDataService != null) {
            return mMapDataService.checkDataInDisk(downLoadMode, path);
        }
        return 0;
    }

    /**
     * 检测数据列表
     *
     * @param downLoadMode 数据下载模式
     * @param path         路径, 当【downLoadMode = DOWNLOAD_MODE_USB】时，path传入已知存在U盘路径。其他模式可以传""
     *                     离线地图数据下载专用接口
     *                     需要等Init的bl::IDataInitObserver * pObserver初始化OnInit回调后，才能调用本接口。
     *                     下载模式downLoadMode相同，多次调用本接口时，前一次的调用传入的pObserver对象地址会被下一次调用传入的替换。
     *                     调用者传入的pObserver对象地址, 当执行AbortRequestDataListCheck，或者UnInit，再或者IServiceMgr.UnInitBL后，将不再被使用。
     *                     thread: main
     * @return int32_t          是否成功发起检测数据列表的请求
     * -  1 发起请求成功，并通过pObserver回调结果
     * -  0 发起请求失败
     * -  -1 pObserver 观察者为空
     * -  -2 当downLoadMode = DOWNLOAD_MODE_USB（U盘升级）时，U盘路径path 为空值.
     */
    public int requestDataListCheck(int downLoadMode, String path) {
        if (mMapDataService != null) {
            return mMapDataService.requestDataListCheck(downLoadMode, path, this);
        }
        return 0;
    }

    /**
     * 终止数据列表检测
     */
    public void abortDataListCheck(@DownLoadMode.DownLoadMode1 int downloadMode) {
        if (mMapDataService != null) {
            mMapDataService.abortRequestDataListCheck(downloadMode);
        }
    }

    public ArrayList<Integer> getAdcodeList(int downLoadMode, int areaType) {
        if (mMapDataService != null) {
            return mMapDataService.getAdcodeList(downLoadMode, areaType);
        }
        return null;
    }

    /**
     * 通过adcode获取城市下载项信息
     * <p>
     * 离线地图数据下载专用接口
     * 下载模式downLoadMode=DOWNLOAD_MODE_NET时，需要等【首次】RequestDataListCheck请求的观察者监听pObserver回调OnRequestDataListCheck后调用。
     * 下载模式downLoadMode=DOWNLOAD_MODE_USB时，需要每次等RequestDataListCheck请求的观察者监听pObserver回调OnRequestDataListCheck后调用。
     *
     * @param downLoadMode 数据下载模式
     * @param adcode       城市行政编码
     */
    public CityDownLoadItem getCityDownLoadItem(int downLoadMode, int adcode) {
        if (mMapDataService != null) {
            return mMapDataService.getCityDownLoadItem(downLoadMode, adcode);
        }
        return null;
    }

    /**
     * 取得等待中、下载中、暂停、解压中、重试状态下的所有城市adcode列表
     * <p>
     * 离线地图数据下载专用接口
     * 下载模式downLoadMode=DOWNLOAD_MODE_NET时，需要等【首次】RequestDataListCheck请求的观察者监听pObserver回调OnRequestDataListCheck后调用。
     * 下载模式downLoadMode=DOWNLOAD_MODE_USB时，需要每次等RequestDataListCheck请求的观察者监听pObserver回调OnRequestDataListCheck后调用。
     *
     * @param downLoadMode 数据下载模式
     */
    @Nullable
    public ArrayList<Integer> getWorkingQueueAdcodeList(int downLoadMode) {
        if (mMapDataService != null) {
            return mMapDataService.getWorkingQueueAdcodeList(downLoadMode);
        }
        return null;
    }

    public boolean isDataUpdatedOnServer(int downLoadMode) {
        if (mMapDataService != null) {
            return mMapDataService.isDataUpdatedOnServer(downLoadMode);
        }
        return false;
    }

    /**
     * 下载请先检查网络
     *
     * @param downLoadMode
     * @param opType
     * @param adcodeDiyLst
     */
    public void operate(int downLoadMode, int opType, ArrayList<Integer> adcodeDiyLst) {
        if (mMapDataService != null) {
            if (opType == OPERATION_TYPE_DELETE) {
                mMapDataService.operate(downLoadMode, opType, adcodeDiyLst);
            } else {
                mMapDataService.operate(downLoadMode, opType, adcodeDiyLst);
            }
        }
    }

    /**
     * 取消所有下载中的任务
     * 主要是页面销毁前先中止未完成的task,否则ANR
     *
     * @param adcodeList 要取消下载的adcode列表,若为null,则会取消所有未完成的task
     */
    public void cancelAllTask(@DownLoadMode.DownLoadMode1 int downloadMode, @Nullable ArrayList<Integer> adcodeList) {
        if (mMapDataService == null) {
            return;
        }

        if (adcodeList == null || adcodeList.isEmpty()) {
            adcodeList = mMapDataService.getWorkingQueueAdcodeList(downloadMode);
        }
        mMapDataService.operate(downloadMode, OperationType.OPERATION_TYPE_CANCEL, adcodeList);
    }

    /**
     * 暂停所有下载中的任务
     *
     * @param adcodeList 暂停下载操作 ， adcodeList为空时暂停当前进行中的adcodeList
     */
    public void pauseAllTask(@DownLoadMode.DownLoadMode1 int downloadMode, @Nullable ArrayList<Integer> adcodeList) {
        if (mMapDataService == null) {
            return;
        }

        if (adcodeList == null || adcodeList.isEmpty()) {
            adcodeList = mMapDataService.getWorkingQueueAdcodeList(downloadMode);
        }
        mMapDataService.operate(downloadMode, OperationType.OPERATION_TYPE_PAUSE, adcodeList);
    }

    /**
     * 开始所有下载中的任务
     *
     * @param adcodeList 继续下载操作 ， adcodeList为空时继续当前暂停中待继续的adcodeList
     */
    public void startAllTask(@DownLoadMode.DownLoadMode1 int downloadMode, @Nullable ArrayList<Integer> adcodeList) {
        if (mMapDataService == null) {
            return;
        }

        if (adcodeList == null || adcodeList.isEmpty()) {
            adcodeList = mMapDataService.getWorkingQueueAdcodeList(downloadMode);
        }
        mMapDataService.operate(downloadMode, OperationType.OPERATION_TYPE_START, adcodeList);
    }

    public int getUrcode(int adcode) {
        if (mMapDataService != null) {
            return mMapDataService.getUrcode(adcode);
        }

        return Service.AUTO_UNKNOWN_ERROR;
    }

    public ArrayList<ProvinceInfo> getProvinceInfoList() {
        if (mMapDataService != null) {
            return mMapDataService.getProvinceInfoList();
        }

        return null;
    }

    public ProvinceInfo getProvinceInfo(int adcode) {
        if (mMapDataService != null) {
            return mMapDataService.getProvinceInfo(adcode);
        }
        return null;
    }

    public ArrayList<Integer> searchAdcode(String s) {
        if (mMapDataService != null) {
            return mMapDataService.searchAdcode(s);
        }

        return null;
    }

    public Area getArea(@DownLoadMode.DownLoadMode1 int downloadMode, int adCode) {
        if (mMapDataService != null) {
            return mMapDataService.getArea(downloadMode, adCode);
        }
        return null;
    }

    public AreaExtraInfo getAreaExtraInfo(AdminCode adcode) {
        if (mMapDataService != null) {
            return mMapDataService.getAreaExtraInfo(adcode);
        }

        return null;
    }

    public int getTownAdcodeByLonLat(double dLon, double dLat) {
        if (mMapDataService != null) {
            return mMapDataService.getTownAdcodeByLonLat(dLon, dLat);
        }

        return Service.AUTO_UNKNOWN_ERROR;
    }

    public String getEngineVersion() {
        if (mMapDataService != null) {
            return mMapDataService.getEngineVersion();
        }
        return "";
    }

    /**
     * @return 1 成功，需等待pObserver回调初始化的结果\n
     * -  0 失败；
     * -  -1 pObserver 传入的观察者为空；
     * -  -2 config.strConfigfilePath 为空或不存在且创建失败；
     * -  -3 config.strConfigfilePath 文件夹下，不存在预置文件all_city_compile.json
     */
    public boolean isInitSuccess() {
        return mInitCode == 1;
    }

    @Override
    public void onOperated(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, @OperationType.OperationType1 int opType,
                           ArrayList<Integer> opreatedIdList) {
        if (null != mapDataObserver) {
            mapDataObserver.onOperated(downLoadMode, dataType, opType, opreatedIdList);
        }
    }

    @Override
    public void onDownLoadStatus(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id,
                                 @TaskStatusCode.TaskStatusCode1 int taskCode,  int opCode) {
        if (null != mapDataObserver) {
            mapDataObserver.onDownLoadStatus(downLoadMode, dataType, id, taskCode, opCode);
        }
    }

    @Override
    public void onPercent(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id, int percentType, float percent) {
        if (null != mapDataObserver) {
            mapDataObserver.onPercent(downLoadMode, dataType, id, percentType, percent);
        }
    }

    @Override
    public void onInit(int downLoadMode, int dataType, int opCode) {
        if (downLoadMode == 0 && dataType == 0 && opCode == 0) {
            Timber.d("MapDataService 初始化成功");
        } else {
            Timber.d("MapDataService 初始化失败");
        }
        if (null != mapDataObserver) {
            mapDataObserver.onInit(downLoadMode, dataType, opCode);
        }
        if (downLoadMode == DownLoadMode.DOWNLOAD_MODE_NET && dataType == DataType.DATA_TYPE_MAP) {
            Timber.d("MapDataService DOWNLOAD_MODE_NET requestDataListCheck");
            requestDataListCheck(DownLoadMode.DOWNLOAD_MODE_NET, "");
        }
        Timber.d("mMapDataService onInit: downLoadMode=" + downLoadMode
                + "dataType=" + dataType + "opCode=" + opCode);
    }

    @Override
    public void onErrorNotify(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id,
                              @DataErrorType.DataErrorType1 int errType, String errMsg) {
        if (null != mapDataObserver) {
            mapDataObserver.onErrorNotify(downLoadMode, dataType, id, errType, errMsg);
        }
    }

    @Override
    public void onErrorNotifyH(int i, int i1, int i2, int i3, String s) {
        if (mapDataObserver != null){
            mapDataObserver.onErrorNotifyH(i, i1, i2, i3, s);
        }
    }

    @Override
    public void onDeleteErrorData(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType, int id,  int opCode) {
        if (null != mapDataObserver) {
            mapDataObserver.onDeleteErrorData(downLoadMode, dataType, id, opCode);
        }
    }

    @Override
    public void onRequestDataListCheck(@DownLoadMode.DownLoadMode1 int downLoadMode, @DataType.DataType1 int dataType,  int opCode) {
        if (null != mapDataObserver) {
            mapDataObserver.onRequestDataListCheck(downLoadMode, dataType, opCode);
        }
    }

    @Override
    public void onMergedStatusInfo(MergedStatusInfo mergedStatusInfo) {
        if (null != mapDataObserver) {
            mapDataObserver.onMergedStatusInfo(mergedStatusInfo);
        }
    }
}
