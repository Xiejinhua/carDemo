package com.autosdk.bussiness.account;

import android.text.TextUtils;

import com.autonavi.gbl.search.model.SearchNearestResult;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.user.model.BehaviorDataType;
import com.autonavi.gbl.user.syncsdk.model.SyncEventType;
import com.autonavi.gbl.user.syncsdk.model.SyncMode;
import com.autonavi.gbl.user.syncsdk.model.SyncRet;
import com.autonavi.gbl.user.usertrack.UserTrackService;
import com.autonavi.gbl.user.usertrack.model.BehaviorDurationType;
import com.autonavi.gbl.user.usertrack.model.BehaviorFileType;
import com.autonavi.gbl.user.usertrack.model.FootprintDeleteRecordResult;
import com.autonavi.gbl.user.usertrack.model.FootprintNaviRecord;
import com.autonavi.gbl.user.usertrack.model.FootprintNaviRecordParam;
import com.autonavi.gbl.user.usertrack.model.FootprintNaviRecordResult;
import com.autonavi.gbl.user.usertrack.model.FootprintSummaryResult;
import com.autonavi.gbl.user.usertrack.model.FootprintSwitchResult;
import com.autonavi.gbl.user.usertrack.model.GpsTrackDepthInfo;
import com.autonavi.gbl.user.usertrack.model.GpsTrackPoint;
import com.autonavi.gbl.user.usertrack.model.HistoryRouteItem;
import com.autonavi.gbl.user.usertrack.model.SearchHistoryItem;
import com.autonavi.gbl.user.usertrack.observer.IGpsInfoGetter;
import com.autonavi.gbl.user.usertrack.observer.IUserTrackObserver;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.common.GeoPoint;
import com.autosdk.bussiness.common.task.TaskManager;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.bussiness.search.SearchCallback;
import com.autosdk.bussiness.search.SearchController;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

/**
 * 用户行为服务M层
 * 需要同时初始化SyncSdkController
 */
public class UserTrackController implements IUserTrackObserver, IGpsInfoGetter {
    private static final String TAG = "UserTrackController";
    private UserTrackService mUserTrackService;

    private UserTrackController() {

    }

    // gps信息获取类
    private ArrayList<IGpsInfoGetter> iGpsInfoGetters = new ArrayList<>();


    public void registerGpsInfoGetter(IGpsInfoGetter gpsInfoGetter) {
        if(!iGpsInfoGetters.contains(gpsInfoGetter)){
            iGpsInfoGetters.add(gpsInfoGetter);
        }
    }

    public void unregisterGpsInfoGetter(IGpsInfoGetter gpsInfoGetter) {
        if(iGpsInfoGetters.contains(gpsInfoGetter)){
            iGpsInfoGetters.remove(gpsInfoGetter);
        }
    }

    @Override
    public GpsTrackPoint getGpsTrackPoint() {
        if (iGpsInfoGetters != null && !iGpsInfoGetters.isEmpty() && iGpsInfoGetters.get(0)!=null) {
            return iGpsInfoGetters.get(0).getGpsTrackPoint();
        }
        return null;
    }

    private static class UserTrackControllerHolder {
        private static UserTrackController instance = new UserTrackController();
    }

    public static UserTrackController getInstance() {
        return UserTrackControllerHolder.instance;
    }

    /**
     * 初始化轨迹服务
     */
    public void initService() {
        if (mUserTrackService == null) {
            mUserTrackService = (UserTrackService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.UserTrackSingleServiceID);
            mUserTrackService.init(this);
            mUserTrackService.addObserver(this);
        }
    }

    /**
     * @param item 搜索历史记录信息
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * c
     * thread:mutil
     */
    public int addSearchHistory(SearchHistoryItem item, @SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.addSearchHistory(item, mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 获取搜索历史记录列表
     * @param[out] data             搜索历史记录信息列表
     * 云同步服务器对每个账户的历史记录操作保存有一定的数量限制（80条）\n
     * 超过这一限制即不会保存。也就是说如果在一端A不断的产生搜索历史记录，而另一端B从未同步过，则会出现B首次同步时，同步到的搜索历史记录不全的问题。
     * thread:mutil
     */
    public ArrayList<SearchHistoryItem> getSearchHistory() {
        if (mUserTrackService != null) {
            return mUserTrackService.getSearchHistory();
        }
        return null;
    }

    /**
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 删除所有搜索历史记录
     * thread:mutil
     */
    public int clearSearchHistory(@SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.clearSearchHistory(mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param item 搜索历史记录信息
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 删除指定搜索历史记录
     * thread:mutil
     */
    public int delSearchHistory(SearchHistoryItem item, @SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.delSearchHistory(item, mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param item 历史路线信息
     * @param mode 同步方式
     * @return ErrorCode       返回GBL模块错误码
     * - ErrorCodeOK   成功
     * - 其他          失败（参考ErrorCode定义）
     * 添加历史路线
     * thread:mutil
     */
    public int addHistoryRoute(HistoryRouteItem item, @SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.addHistoryRoute(item, mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }


    /**
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 获取历史路线列表
     * @param[out] data             历史路线信息列表
     * 云同步服务器对每个账户的历史路线操作保存有一定的数量限制（80条）\n
     * 超过这一限制即不会保存。也就是说如果在一端A不断的产生历史路线，而另一端B从未同步过，则会出现B首次同步时，同步到的历史路线不全的问题。
     * thread:mutil
     */
    public ArrayList<HistoryRouteItem> getHistoryRoute() {
        if (mUserTrackService != null) {
            return mUserTrackService.getHistoryRoute();
        }
        return null;
    }

    /**
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 删除所有历史路线
     * thread:mutil
     */
    public int clearHistoryRoute(@SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.clearHistoryRoute(mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param item 历史路线信息
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 删除指定历史路线
     * thread:mutil
     */
    public int delHistoryRoute(HistoryRouteItem item, @SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.delHistoryRoute(item, mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param type 行为数据类型
     * @param id   行为数据id
     * @param data 行为数据
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 设置行为数据
     * thread:mutil
     */
    public int setBehaviorData(@BehaviorDataType.BehaviorDataType1 int type, String id, String data, @SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.setBehaviorData(type, id, data, mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param type 行为数据类型
     * @param id   行为数据id
     * @return dice::String16   行为数据
     * 获取行为数据
     * thread:mutil
     */
    public String getBehaviorData(@BehaviorDataType.BehaviorDataType1 int type, String id) {
        if (mUserTrackService != null) {
            return mUserTrackService.getBehaviorData(type, id);
        }
        return null;
    }

    /**
     * @param type 行为数据类型
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 删除行为数据
     * thread:mutil
     */
    public int clearBehaviorData(@BehaviorDataType.BehaviorDataType1 int type, @SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.clearBehaviorData(type, mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param type 行为数据类型
     * @param id   行为数据id
     * @param mode 同步方式
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 删除指定id行为数据
     * thread:mutil
     */
    public int delBehaviorData(@BehaviorDataType.BehaviorDataType1 int type, String id, @SyncMode.SyncMode1 int mode) {
        if (mUserTrackService != null) {
            return mUserTrackService.delBehaviorData(type, id, mode);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param type 行为数据类型
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 获取行为数据id列表
     * @param[out] ids              行为数据id列表
     * thread:mutil
     */
    public int[] getBehaviorDataIds(@BehaviorDataType.BehaviorDataType1 int type) {
        if (mUserTrackService != null) {
            FootprintNaviRecordParam footprintNaviRecordParam = new FootprintNaviRecordParam();
            footprintNaviRecordParam.minNaviDist = 0;
            footprintNaviRecordParam.maxCount = 50;
            int footprintNaviRecordList = mUserTrackService.getFootprintNaviRecordList(footprintNaviRecordParam);
            return mUserTrackService.getBehaviorDataIds(type);
        }


        return null;
    }

    /**
     * @param type 行为数据类型
     * @param id   行为数据id
     * @return dice::String16   行为数据
     * 通过id获取行为数据
     * thread:mutil
     */
    public String getBehaviorDataById(@BehaviorDataType.BehaviorDataType1 int type, int id) {
        if (mUserTrackService != null) {
            return mUserTrackService.getBehaviorDataById(type, id);
        }
        return null;
    }

    /**
     * @param type 行为数据类型
     * @return int32_t          总时长（单位：秒）
     * 获取总时长（单位：秒）
     * thread:multi
     */
    public int getTotalDuration(@BehaviorDataType.BehaviorDataType1 int type) {
        if (mUserTrackService != null) {
            return mUserTrackService.getTotalDuration(type);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param type     行为数据类型
     * @param duration 行为数据时长类型
     * @return int32_t          总里程（单位：m）
     * 获取总里程（单位：m）
     * thread:multi
     */
    public int getTotalDistance(@BehaviorDataType.BehaviorDataType1 int type, @BehaviorDurationType.BehaviorDurationType1 int duration) {
        if (mUserTrackService != null) {
            return mUserTrackService.getTotalDistance(type, duration);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param type 行为数据类型
     * @return ErrorCode        返回GBL模块错误码
     * - ErrorCodeOK    成功
     * - 其他            失败（参考ErrorCode定义）
     * 获取指定类型未完结轨迹记录id列表
     * @param[out] ids              未完结轨迹记录id列表
     * thread:multi
     */
    public int[] getIncompleteTrailIds(@BehaviorDataType.BehaviorDataType1 int type) {
        if (mUserTrackService != null) {
            return mUserTrackService.getIncompleteTrailIds(type);
        }
        return null;
    }

    public int obtainGpsTrackInfo(String savePath, String fileName) {
        if (mUserTrackService != null) {
            return mUserTrackService.obtainGpsTrackDepInfo(savePath, fileName);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    /**
     * @param type 行为数据类型
     * @param id   行为数据id
     * @param file 行为数据文件类型
     * @return dice::String16   文件路径
     * 获取指定id文件路径
     * thread:multi
     */
    public String getFilePath(@BehaviorDataType.BehaviorDataType1 int type, String id, @BehaviorFileType.BehaviorFileType1 int file) {
        if (mUserTrackService != null) {
            return mUserTrackService.getFilePath(type, id, file);
        }
        return null;
    }

    private List<String> mUnfinishList;
    /**
     * 处理未完成的轨迹并开始轨迹
     *
     * 删除180天文件－－－处理未完成－－－开始轨迹
     */
    public void startTrackAndhandleUnfinishTrace(final String path , final String fileName) {

        mUnfinishList = new ArrayList<>();

        TaskManager.run(new Runnable() {
            @Override
            public void run() {
                int[] lists =  getIncompleteTrailIds(BehaviorDataType.BehaviorTypeTrailDriveForAuto);
                if(lists != null){

                    for(int i=0; i<lists.length;i++){
                        int index = lists[i];
                        String str = getBehaviorDataById(BehaviorDataType.BehaviorTypeTrailDriveForAuto, index);
                        if (!TextUtils.isEmpty(str)) {
                            try {
                                JSONObject json = new JSONObject(str);
                                String trackFileName = json.getString("trackFileName");
                                mUnfinishList.add(trackFileName);
                                obtainGpsTrackInfo(path,trackFileName);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                }
                startGpsTrack(path , fileName ,5000);
            }
        });
    }

    /**
     * 处理未完成的轨迹
     *
     * 删除180天文件－－－处理未完成
     */
    public void handleUnfinishTrace(final String path) {

        mUnfinishList = new ArrayList<>();

        TaskManager.run(new Runnable() {
            @Override
            public void run() {
                int[] lists =  getIncompleteTrailIds(BehaviorDataType.BehaviorTypeTrailDriveForAuto);
                if(lists != null){

                    for(int i=0; i<lists.length;i++){
                        int index = lists[i];
                        String str = getBehaviorDataById(BehaviorDataType.BehaviorTypeTrailDriveForAuto, index);
                        if (!TextUtils.isEmpty(str)) {
                            try {
                                JSONObject json = new JSONObject(str);
                                String trackFileName = json.getString("trackFileName");
                                mUnfinishList.add(trackFileName);
                                obtainGpsTrackInfo(path,trackFileName);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                }
            }
        });
    }

    public int startGpsTrack(String psSavePath, String psFileName, long un32MsecRate) {
        if (mUserTrackService != null) {
            return mUserTrackService.startGpsTrack(psSavePath, psFileName, un32MsecRate);
        }
        return Service.AUTO_UNKNOWN_ERROR;
    }

    public String getmCurrentFileName() {
        return mCurrentFileName;
    }

    public void closeGpsTrack(String psSavePath) {
        int returnValue = Service.AUTO_UNKNOWN_ERROR;
        if (mUserTrackService != null && !TextUtils.isEmpty(mCurrentFileName)) {
            returnValue = mUserTrackService.closeGpsTrack(psSavePath, mCurrentFileName);
        }
        mCurrentFileName = "";
        Timber.i("closeGpsTrack returnValue = %d", returnValue);
    }
    // ============================ UserTrackService 观察回调 start ================================


    public static final int STATUS_CODE_CREATE = 0;
    public static final int STATUS_CODE_APPEND = 1;
    public static final int STATUS_CODE_FAILURE = -1;
    private final static int REVERSE_START = 8;
    private final static int REVERSE_FASTEST = 9;
    private final static int REVERSE_END = 10;
    public String mCurrentFileName = "";
    /**
     * @param status 状态
     * - -1    失败
     * - 0     成功：新建轨迹文件进行打点
     * - 1     成功：在已存在的轨迹文件继续追加打点
     * @param psSavePath    轨迹文件的存放路径
     * @param fileName    轨迹文件名称
     * 开启Gps轨迹生成的回调通知
     * thread:main
     */
    @Override
    public void onStartGpsTrack(int status, String psSavePath, String fileName) {
        if (status == STATUS_CODE_FAILURE) {
            return;
        }

        if (status == STATUS_CODE_CREATE || status == STATUS_CODE_APPEND) {
            mCurrentFileName = fileName;
            JSONObject json = new JSONObject();
            String rideRunType = mCurrentFileName.split("_")[1];
            try {
                json.put("id", fileName);
                json.put("type", 403);
                json.put("rideRunType", rideRunType);
                json.put("trackFileName", fileName);
                json.put("endLocation", "");
                json.put("version", 1);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            saveTraceToDB(fileName, json.toString());
        } else {
        }
    }

    /**
     * 将轨迹的深度信息保存到数据库
     */
    private void saveTraceToDB(String itemId, String jsonString) {
        int result = setBehaviorData(BehaviorDataType.BehaviorTypeTrailDriveForAuto, itemId, jsonString,
                SyncMode.SyncModeNow);
    }

    /**
     * @param n32SuccessTag 状态
     *                      - -1       失败
     *                      - 0        成功结束轨迹文件生成，并返回深度信息
     * @param psSavePath    轨迹文件的存放路径
     * @param psFileName    轨迹文件名称
     * @param depInfo       轨迹深度信息
     *                      结束Gps轨迹文件生成的回调通知
     *                      thread:main
     */
    @Override
    public void onCloseGpsTrack(int n32SuccessTag, String psSavePath, String psFileName, GpsTrackDepthInfo depInfo) {
        if (n32SuccessTag >= 0) {
            saveTrace(psSavePath, psFileName, depInfo, "", "", "");
        } else {
            deleteTrack(psSavePath, psFileName);
        }
    }

    private static final int MINIMUM_REQUIRED_DISTANCE = 1000;
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        }
    };

    private void saveTrace(String psSavePath, String filename, GpsTrackDepthInfo gGpsTrackDepInfo, String startPoiName, String maxSpeedPoiName, String endPoiName) {
        if (gGpsTrackDepInfo != null) {
            // 逆地理

            JSONObject json = new JSONObject();
            String rideRunType = filename.split("_")[1];

            try {
                long runDistance = gGpsTrackDepInfo.distance;
//                小于1km不记录
                if (runDistance < MINIMUM_REQUIRED_DISTANCE) {

                    deleteTrack(psSavePath, filename);

                } else {
                    json.put("id", filename);
                    json.put("type", 403);
                    json.put("rideRunType", Integer.parseInt(rideRunType));
                    json.put("timeInterval", gGpsTrackDepInfo.duration);
                    json.put("runDistance", gGpsTrackDepInfo.distance);

                    //单位 km／h
                    json.put("maxSpeed", (int)gGpsTrackDepInfo.trackPoints.get(gGpsTrackDepInfo.fastestIndex).f32Speed + "");

                    String sTime = DATE_FORMAT.get().format(gGpsTrackDepInfo.trackPoints.get(0).n64TickTime);
                    json.put("startTime", sTime);

                    String eTime = DATE_FORMAT.get().format((gGpsTrackDepInfo.trackPoints.get(gGpsTrackDepInfo.trackPoints.size() - 1).n64TickTime));
                    json.put("endTime", eTime);
                    json.put("trackFileName", filename);
                    json.put("startPoiName", startPoiName);
                    json.put("endPoiName", endPoiName);
                    JSONObject startLoc = new JSONObject();
                    startLoc.put("x", gGpsTrackDepInfo.trackPoints.get(0).f64Longitude + "");
                    startLoc.put("y", gGpsTrackDepInfo.trackPoints.get(0).f64Latitude + "");


                    json.put("startLocation", startLoc.toString());

                    JSONObject endLoc = new JSONObject();
                    endLoc.put("x", gGpsTrackDepInfo.trackPoints.get(gGpsTrackDepInfo.trackPoints.size() - 1).f64Longitude + "");
                    endLoc.put("y", gGpsTrackDepInfo.trackPoints.get((gGpsTrackDepInfo.trackPoints.size() - 1)).f64Latitude + "");


                    json.put("endLocation", endLoc.toString());
//                       maxSpeedTime

                    String mTime = DATE_FORMAT.get().format(gGpsTrackDepInfo.trackPoints.get(gGpsTrackDepInfo.fastestIndex).n64TickTime);
                    json.put("maxSpeedTime", mTime);

                    JSONObject maxSpeedLoc = new JSONObject();
                    maxSpeedLoc.put("x", gGpsTrackDepInfo.trackPoints.get(gGpsTrackDepInfo.fastestIndex).f64Longitude + "");
                    maxSpeedLoc.put("y", gGpsTrackDepInfo.trackPoints.get(gGpsTrackDepInfo.fastestIndex).f64Latitude + "");
                    json.put("maxSpeedLocation", maxSpeedLoc.toString());
                    json.put("maxSpeedPoiName", maxSpeedPoiName);

                    int updatetime = (int) (gGpsTrackDepInfo.trackPoints.get(0).n64TickTime / 1000);
                    json.put("updateTime", updatetime);

                    json.put("version", 1);


                    saveTraceToDB(filename, json.toString());

                    // 如果 起点，终点，最高速度点  为空，就进行逆地理
                    if (TextUtils.isEmpty(startPoiName) && TextUtils.isEmpty(maxSpeedPoiName)) {
                        reversePoi(REVERSE_START, psSavePath, filename, gGpsTrackDepInfo, startPoiName, maxSpeedPoiName, endPoiName);
                    } else {
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    private void reversePoi(final int reverseType, final String psSavePath, final String s1, final GpsTrackDepthInfo gGpsTrackDepInfo, final String startPoiName, final String maxSpeedPoiName, final String endPoiName) {
        GeoPoint geoPoint = new GeoPoint();
        if (reverseType == REVERSE_START) {
            geoPoint.setLonLat(gGpsTrackDepInfo.trackPoints.get(0).f64Longitude, gGpsTrackDepInfo.trackPoints.get(0).f64Latitude);
        } else if (reverseType == REVERSE_FASTEST) {
            geoPoint.setLonLat(gGpsTrackDepInfo.trackPoints.get(gGpsTrackDepInfo.fastestIndex).f64Longitude, gGpsTrackDepInfo.trackPoints.get(gGpsTrackDepInfo.fastestIndex).f64Latitude);
        } else {
            geoPoint.setLonLat(gGpsTrackDepInfo.trackPoints.get((gGpsTrackDepInfo.trackPoints.size() - 1)).f64Longitude, gGpsTrackDepInfo.trackPoints.get((gGpsTrackDepInfo.trackPoints.size() - 1)).f64Latitude);
        }
        SearchController.getInstance().nearestSearch(geoPoint, new SearchCallback<SearchNearestResult>() {

            @Override
            public void onSuccess(SearchNearestResult data) {
                super.onSuccess(data);
                if (reverseType == REVERSE_START) {
                    if (!data.poi_list.isEmpty()) {
                        reversePoi(REVERSE_FASTEST, psSavePath, s1, gGpsTrackDepInfo, data.poi_list.get(0).address, "", "");
                    } else {
                        reversePoi(REVERSE_FASTEST, psSavePath, s1, gGpsTrackDepInfo, "地图选点", "", "");
                    }
                } else if (reverseType == REVERSE_FASTEST) {
                    if (!data.poi_list.isEmpty()) {
                        reversePoi(REVERSE_END, psSavePath, s1, gGpsTrackDepInfo, startPoiName, data.poi_list.get(0).address, "");
                    } else {
                        reversePoi(REVERSE_END, psSavePath, s1, gGpsTrackDepInfo, startPoiName, "地图选点", "");
                    }
                } else {
                    if (!data.poi_list.isEmpty()) {
                        saveTrace(psSavePath, s1, gGpsTrackDepInfo, startPoiName, maxSpeedPoiName, data.poi_list.get(0).address);
                    } else {
                        saveTrace(psSavePath, s1, gGpsTrackDepInfo, startPoiName, maxSpeedPoiName, "地图选点");
                    }
                }
            }

            @Override
            public void onFailure(int errCode, String msg) {
                super.onFailure(errCode, msg);
                if (reverseType == REVERSE_START) {
                    reversePoi(REVERSE_FASTEST, psSavePath, s1, gGpsTrackDepInfo, "地图选点", "", "");
                } else if (reverseType == REVERSE_FASTEST) {
                    reversePoi(REVERSE_END, psSavePath, s1, gGpsTrackDepInfo, startPoiName, "地图选点", "");
                } else {
                    saveTrace(psSavePath, s1, gGpsTrackDepInfo, startPoiName, maxSpeedPoiName, "地图选点");
                }
            }
        });
    }

    /**
     * 删除一条轨迹记录 ： 包括json 信息和gps文件
     *
     * @param trackName
     */
    private void deleteTrack(String psSavePath, String trackName) {
        deleteTrackDBItem(trackName);
        FileUtils.deleteFile(psSavePath + "/" + trackName);
    }

    /**
     * 删除单条轨迹
     *
     * @param strItemId
     */
    public void deleteTrackDBItem(String strItemId) {
        delBehaviorData(BehaviorDataType.BehaviorTypeTrailDriveForAuto, strItemId, SyncMode
                .SyncModeNow);
    }

    private IUserTrackObserver iUserTrackObserver;

    public void setUserTrackListener(IUserTrackObserver iUserTrackObserver) {
        this.iUserTrackObserver = iUserTrackObserver;
    }

    public void removeUserTrackListener() {
        iUserTrackObserver = null;
    }

    /**
     * @param n32SuccessTag 状态
     *                      - -1    失败
     *                      - 0        成功获取深度信息
     * @param psSavePath    轨迹文件的存放路径
     * @param psFileName    轨迹文件名称
     * @param depInfo       轨迹深度信息
     *                      获取Gps轨迹文件深度信息的回调通知
     *                      thread:main
     */
    @Override
    public void onGpsTrackDepInfo(int n32SuccessTag, String psSavePath, String psFileName, GpsTrackDepthInfo depInfo) {
        if (null != iUserTrackObserver) {
            iUserTrackObserver.onGpsTrackDepInfo(n32SuccessTag, psSavePath, psFileName, depInfo);
        }else {
            if (n32SuccessTag >= 0) {
                if (mUnfinishList != null && mUnfinishList.contains(psFileName)) {
                    mUnfinishList.remove(psFileName);
                    saveTrace(psSavePath,psFileName, depInfo, "", "", "");
                }
            } else {
                deleteTrack(psSavePath,psFileName);
            }
        }

    }

    /**
     * @param eventType 同步SDK回调事件类型
     * @param exCode    同步SDK返回值
     *                  获取轨迹数据同步回调通知
     */
    @Override
    public void notify(@SyncEventType.SyncEventType1 int eventType, @SyncRet.SyncRet1 int exCode) {
    }

    @Override
    public void onFootprintSwitch(FootprintSwitchResult footprintSwitchResult) {

    }

    @Override
    public void onFootprintSummary(FootprintSummaryResult footprintSummaryResult) {

    }

    @Override
    public void onFootprintNaviRecordList(FootprintNaviRecordResult footprintNaviRecordResult) {
        //足迹汇总代码
        ArrayList<FootprintNaviRecord> record = footprintNaviRecordResult.data.record;
        int distance = 0;
        Map<String,FootprintNaviRecord> map = new HashMap<>();
        for (int i = 0; i < record.size(); i++) {
            FootprintNaviRecord naviRecord = record.get(i);
            map.put(naviRecord.month,naviRecord);
        }
        for(Map.Entry<String,FootprintNaviRecord> entry :map.entrySet()){
            distance += entry.getValue().monthDistance;
        }
    }

    @Override
    public void onFootprintDeleteRecord(FootprintDeleteRecordResult footprintDeleteRecordResult) {

    }

    public void destroyUserTrackService() {
        if (mUserTrackService != null) {
            mUserTrackService.removeObserver(this);
            mUserTrackService = null;
        }
    }
}
