package com.autosdk.bussiness.manager;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.autonavi.gbl.common.path.model.POIInfo;
import com.autonavi.gbl.common.path.model.PointType;
import com.autonavi.gbl.common.path.option.POIForRequest;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.common.path.option.RouteOption;
import com.autonavi.gbl.common.path.option.RouteType;
import com.autonavi.gbl.guide.model.NaviPath;
import com.autonavi.gbl.route.model.ConsisPathBinaryData;
import com.autonavi.gbl.route.model.ConsisPathIdentity;
import com.autonavi.gbl.route.model.PathResultData;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autosdk.bussiness.common.ConsisPathData;
import com.autosdk.bussiness.common.ConsisUserData;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.utils.FileUtils;
import com.autosdk.bussiness.navi.NaviController;
import com.autosdk.bussiness.navi.route.model.PathResultDataInfo;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;
import com.autosdk.bussiness.navi.route.utils.RouteOptionUtil;
import com.autosdk.bussiness.widget.navi.NaviComponent;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.autonavi.gbl.common.path.model.PointType.PointTypeStart;

import timber.log.Timber;

/**
 * 用来处理多屏一致性路线
 *
 * @author AutoSdk
 */
public class PathInfoManager {

    private static final String TAG = "PathInfoManager";

    /**
     * 主屏发起算路回调的路线标志
     */
    public static final String MASTER_SDK = "MainSdk";

    /**
     * 删除主副屏路线
     */
    public static final int DELETE_ALL_ROUTE = 0;

    /**
     * 删除主屏发起算路的路线
     */
    public static final int DELETE_MASTER_ROUTE = 1;

    /**
     * 删除副屏发起算路的路线
     */
    public static final int DELETE_SIDE_ROUTE = 2;

    private static PathInfoManager mInstance = null;

    /**
     * 用来做临时保存的路线相关数据
     */
    private RouteCarResultData mRouteCarResultData;

    /**
     * 缓存上一次的路线ID
     */
    private String mPreviousRouteDataIds;

    /**
     * 缓存上一次的路线Index
     */
    private int mPreviousRouteDataFocusIndex;

    /**
     * 缓存其他屏最后一次的路线Index
     */
    private int mRouteFocusIndex;

    /**
     * 路线数据存储路径
     */
    private String mMultiRouteDataPath;

    /**
     * 临时保存多屏回调的算路参数
     */
    private RouteOption mTempRouteOption;

    private ArrayList<RouteOption> mTempRouteOptionList;

    /**
     * 管理路线数据的基础属性对象
     */
    private ArrayList<ConsisPathData> mConsisPathDataList;

    /**
     * 缓存Fragment之间的路线信息，防止使用序列化的方式时出现bundle过大问题。
     */
    private WeakReference<RouteCarResultData> mIntentRouteCarResultData;

    private CountDownLatch mCountDownLatch;

    /**
     * 多屏是否连接
     */
    private boolean mIsMultiSdkConnected = false;

    private final int LOCK_TIME_OUT = 5;

    private boolean mIsMultiSDK = false;

    private PathInfoManager() {
        mConsisPathDataList = new ArrayList<>();
    }

    public static synchronized PathInfoManager getInstance() {
        if (mInstance == null) {
            mInstance = new PathInfoManager();
        }
        return mInstance;
    }

    public void setRouteFocusIndex(int index) {
        this.mRouteFocusIndex = index;
    }

    /**
     * 设置路线缓存路径
     *
     * @param path
     */
    public void setPath(String path) {
        this.mMultiRouteDataPath = path + "multi_route_data/";
    }

    /**
     * 多屏是否连接
     *
     * @param mIsMultiSdkConnected
     */
    public void setMultiSdkConnected(boolean mIsMultiSdkConnected) {
        this.mIsMultiSdkConnected = mIsMultiSdkConnected;
    }

    /**
     * 是否多实例sdk
     *
     * @param mIsMultiSDK
     */
    public void setIsMultiSdk(boolean mIsMultiSDK) {
        this.mIsMultiSDK = mIsMultiSDK;
    }


    /**
     * 接收新的路线数据
     *
     * @param pathResultData
     * @param pathInfos
     */
    public RouteCarResultData coverBlPathDataInfoToHMI(PathResultData pathResultData, ArrayList<PathInfo> pathInfos) {
        Timber.d("====multi start coverBlPathDataInfoToHMI =  %s, mTempRouteOption =  %s", pathInfoIds2String(pathInfos), mTempRouteOption);
        RouteOption routeOption = getRouteOption(pathResultData);
        if (routeOption != null) {
            lock();
            RouteCarResultData tempRouteCarResultData = new RouteCarResultData();
            //偏航类型时多屏同步的参数不包含HMI需要的信息，此时需重新设置RouteCarResultData信息
            if (pathResultData.type == RouteType.RouteTypeYaw && mRouteCarResultData != null && mRouteCarResultData.getToPOI() != null && mRouteCarResultData.getFromPOI() != null) {
                tempRouteCarResultData.setToPOI(mRouteCarResultData.getToPOI());
                tempRouteCarResultData.setFromPOI(mRouteCarResultData.getFromPOI());
                tempRouteCarResultData.setMidPois(mRouteCarResultData.getMidPois());
            } else {
                POIForRequest poiForRequest = routeOption.getPOIForRequest();
                POIInfo startInfo = poiForRequest.getPoint(PointType.PointTypeStart, 0);
                POIInfo endInfo = poiForRequest.getPoint(PointType.PointTypeEnd, 0);
                tempRouteCarResultData.setFromPOI(RouteOptionUtil.poiInfoToPOI(startInfo, PointType.PointTypeStart));
                tempRouteCarResultData.setToPOI(RouteOptionUtil.poiInfoToPOI(endInfo, PointType.PointTypeEnd));
                tempRouteCarResultData.setMidPois(null);
                ArrayList<POI> midPois = new ArrayList<>();
                if (poiForRequest.getPointSize(PointType.PointTypeVia) > 0) {
                    for (int i = 0; i < poiForRequest.getPointSize(PointType.PointTypeVia); i++) {
                        POIInfo viaInfo = poiForRequest.getPoint(PointType.PointTypeVia, i);
                        midPois.add(i, RouteOptionUtil.poiInfoToPOI(viaInfo, PointType.PointTypeVia));
                    }
                    tempRouteCarResultData.setMidPois(midPois);
                }
            }
            tempRouteCarResultData.setRouteConstrainCode(routeOption.getConstrainCode());
            tempRouteCarResultData.setRouteStrategy(routeOption.getRouteStrategy());
//            mTempRouteOption = null;
            mTempRouteOptionList.remove(routeOption);
            tempRouteCarResultData.setPathResultDataInfo(pathResultDataToInfo(pathResultData));
            tempRouteCarResultData.setIsOffline(pathResultData.isLocal);
            tempRouteCarResultData.setArrayPathId(arrayPathInfoId2ArrayString(pathInfos));
            tempRouteCarResultData.setPathResult(pathInfos);
            keepPathInfoData(tempRouteCarResultData);
            Timber.d("end coverBlPathDataInfoToHMI");
            release();
            return tempRouteCarResultData;
        }
        return null;
    }


    public boolean isSameNaviPath(NaviPath selfNaviPath, NaviPath multiNaviPath) {
        if (selfNaviPath != null && multiNaviPath != null) {
            ArrayList<PathInfo> selfVecPaths = selfNaviPath.vecPaths;
            ArrayList<PathInfo> multiVecPaths = multiNaviPath.vecPaths;
            if (selfVecPaths == null || multiVecPaths == null) {
                return false;
            }
            if (selfVecPaths.size() != multiVecPaths.size()) {
                return false;
            }
            for (int i = 0; i < selfVecPaths.size(); i++) {
                if (!isSamePathInfo(selfVecPaths.get(i), multiVecPaths.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private boolean isSamePathInfo(PathInfo selfPathInfo, PathInfo multiPathInfo) {
        Gson gson = new Gson();
        if (selfPathInfo != null && multiPathInfo != null && selfPathInfo.getPathID() == multiPathInfo.getPathID()) {
            return gson.toJson(selfPathInfo.getViaPointInfo()).equals(gson.toJson(multiPathInfo.getViaPointInfo()));
        }
        return false;
    }

    /**
     * 保存上一次的路线数据属性
     *
     * @param naviPath
     */
    public void keepPreviousData(NaviPath naviPath) {
        if (naviPath != null) {
            ArrayList<PathInfo> vecPaths = naviPath.vecPaths;
            if (vecPaths.size() != 0) {
                mPreviousRouteDataIds = TextUtils.join("", arrayPathInfoId2ArrayString(vecPaths).toArray());
                mPreviousRouteDataFocusIndex = (int) naviPath.mainIdx;
            }
        }
    }

    /**
     * 获取缓存的最新副屏算路获取的路线信息
     */
    public RouteCarResultData getLastSideRoute() {
        if (mConsisPathDataList == null || mConsisPathDataList.size() <= 0) {
            return null;
        }

        String fileName = null;
        ConsisPathData temp = null;
        for (ConsisPathData consisPathData : mConsisPathDataList) {
            if (consisPathData != null && !"MainSdk".equals(consisPathData.planChannelId) && consisPathData.arrayPathId != null) {
                fileName = mMultiRouteDataPath + consisPathData.planChannelId + TextUtils.join("", consisPathData.arrayPathId.toArray());
                temp = consisPathData;
            }
        }

        if (TextUtils.isEmpty(fileName)) {
            return null;
        }

        byte[] file2byte = FileUtils.file2Byte(mMultiRouteDataPath + temp.planChannelId + TextUtils.join("", temp.arrayPathId.toArray()));
        if (file2byte != null && file2byte.length > 0) {
            RouteCarResultData routeCarResultData = null;
            releasePathResult(routeCarResultData);
            routeCarResultData = temp.transformPathData(file2byte);
            resetPlanChannelId(temp.planChannelId, routeCarResultData);
            routeCarResultData.setFocusIndex(mPreviousRouteDataFocusIndex);
            return routeCarResultData;
        }

        return null;
    }

    /**
     * 路线还原
     *
     * @return
     */
    public boolean restore() {
        if (!TextUtils.isEmpty(mPreviousRouteDataIds)) {
            for (ConsisPathData consisPathData : mConsisPathDataList) {
                ArrayList<String> arrayPathId = consisPathData.arrayPathId;
                if (arrayPathId != null) {
                    if (consisPathData.matchOrNot(mPreviousRouteDataIds)) {
                        byte[] file2byte = FileUtils.file2Byte(mMultiRouteDataPath + consisPathData.planChannelId + TextUtils.join("", consisPathData.arrayPathId.toArray()));
                        if (file2byte != null && file2byte.length > 0) {
                            releasePathResult(mRouteCarResultData);
                            mRouteCarResultData = consisPathData.transformPathData(file2byte);
                            resetPlanChannelId(consisPathData.planChannelId, mRouteCarResultData);
                            mRouteCarResultData.setFocusIndex(mPreviousRouteDataFocusIndex);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    // 是否包含主屏路线
    public boolean containMasterRoute() {
        if (mConsisPathDataList.size() == 0) {
            return false;
        }

        String fileName = null;
        // 取最新的主屏路线
        for (ConsisPathData consisPathData : mConsisPathDataList) {
            String planChannelId = consisPathData.planChannelId;
            if (MASTER_SDK.equals(planChannelId) && consisPathData.arrayPathId != null) {
                fileName = mMultiRouteDataPath + consisPathData.planChannelId + TextUtils.join("", consisPathData.arrayPathId.toArray());
            }
        }

        return !TextUtils.isEmpty(fileName);
    }

    /**
     * 获取文件缓存中的最新主屏或副屏路线
     */
    public RouteCarResultData getLastRoute(boolean isMasterRoute) {
        if (mConsisPathDataList.size() <= 0) {
            return null;
        }

        RouteCarResultData routeCarResultData = null;
        String fileName = null;
        ConsisPathData temp = new ConsisPathData();
        // 获取最新的路线
        for (ConsisPathData consisPathData : mConsisPathDataList) {
            String planChannelId = consisPathData.planChannelId;
            if (consisPathData.arrayPathId != null) {
                boolean isValid = isMasterRoute && MASTER_SDK.equals(planChannelId) || (!isMasterRoute && !MASTER_SDK.equals(planChannelId));
                if (isValid) {
                    fileName = mMultiRouteDataPath + consisPathData.planChannelId + TextUtils.join("", consisPathData.arrayPathId.toArray());
                    temp = consisPathData;
                }
            }
        }

        byte[] file2byte = FileUtils.file2Byte(fileName);
        if (file2byte != null && file2byte.length > 0) {
            releasePathResult(routeCarResultData);
            routeCarResultData = temp.transformPathData(file2byte);
            resetPlanChannelId(temp.planChannelId, routeCarResultData);
            if (routeCarResultData != null) {
                routeCarResultData.setFocusIndex(mRouteFocusIndex);
                return routeCarResultData;
            }

            routeCarResultData = mRouteCarResultData;
        }

        return routeCarResultData;
    }

    /**
     * 保存路线数据至文件
     *
     * @param routeCarResultData
     */
    public void keepPathInfoData(RouteCarResultData routeCarResultData) {
        if (mIsMultiSDK) {//多实例sdk才进行存储数据
            writeRouteDataToFile(routeCarResultData);
        }
    }

    private void lock() {
        if (mCountDownLatch == null || mCountDownLatch.getCount() == 0) {
            mCountDownLatch = new CountDownLatch(1);
        }
    }

    private void await() {
        try {
            if (mCountDownLatch != null && mCountDownLatch.getCount() > 0) {
                mCountDownLatch.await(LOCK_TIME_OUT, TimeUnit.SECONDS);
                mCountDownLatch = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void release() {
        if (mCountDownLatch != null) {
            mCountDownLatch.countDown();
        }
    }

    /**
     * 根据多屏算路一致性，主副屏的算路结果应保持一致
     *
     * @param consisPathIdentities
     * @return
     */
    private boolean checkIsConsisPathDataExist(ArrayList<ConsisPathIdentity> consisPathIdentities) {
        if (mConsisPathDataList == null || mConsisPathDataList.size() == 0) {
            return false;
        }
        for (ConsisPathData consisPathData : mConsisPathDataList) {
            if (consisPathData.matchOrNot(consisPathIdentities)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 还原路线数据
     *
     * @param consisPathIdentities
     * @return
     */
    public ArrayList<PathInfo> restorePathInfoData(ArrayList<ConsisPathIdentity> consisPathIdentities) {
        //路线不一致的情况
        if (!checkIsConsisPathDataExist(consisPathIdentities)) {
            lock();
            await();
        }
        Timber.d("start restorePathInfoData");
        Timber.d(pathInfoIds3String(consisPathIdentities));
        for (ConsisPathData consisPathData : mConsisPathDataList) {
            Timber.d(consisPathData.toString());
            if (consisPathData.matchOrNot(consisPathIdentities)) {
                Timber.d("restorePathInfoData match");
                byte[] file2byte = FileUtils.file2Byte(mMultiRouteDataPath + consisPathData.planChannelId + TextUtils.join("", consisPathData.arrayPathId.toArray()));
                if (file2byte != null && file2byte.length > 0) {
                    releasePathResult(mRouteCarResultData);
                    mRouteCarResultData = consisPathData.transformPathData(file2byte);
                    //使用路线还原形式，需重新设置渠道
                    resetPlanChannelId(consisPathIdentities.get(0), mRouteCarResultData);
                    Timber.d("restorePathInfoData finish");
                    return mRouteCarResultData.getPathResult();
                }
            }
        }
        return null;
    }

    /**
     * 释放路线数据
     *
     * @param routeCarResultData
     */
    private void releasePathResult(RouteCarResultData routeCarResultData) {
        if (routeCarResultData != null) {
            releasePathResult(routeCarResultData.getPathResult());
        }
    }

    /**
     * 释放路线数据
     *
     * @param pathInfoArrayList
     */
    private void releasePathResult(ArrayList<PathInfo> pathInfoArrayList) {
        if (pathInfoArrayList != null) {
            NaviController.getInstance().deletePath(pathInfoArrayList);
        }
    }

    /**
     * 路线还原形式的channelId会发生变化，需要重新设置
     */
    private void resetPlanChannelId(ConsisPathIdentity consisPathIdentity, RouteCarResultData routeCarResultData) {
        resetPlanChannelId(consisPathIdentity.planChannelId, routeCarResultData);
    }

    private void resetPlanChannelId(String planChannelId, RouteCarResultData routeCarResultData) {
        if (routeCarResultData != null && routeCarResultData.getPathResult() != null) {
            for (PathInfo pathInfo : routeCarResultData.getPathResult()) {
                pathInfo.setPlanChannelId(planChannelId);
            }
        }
    }


    private void writeRouteDataToFile(RouteCarResultData routeCarResultData) {
        if (isLegalPathResultData(routeCarResultData)) {
            ArrayList<String> arrayPathId = routeCarResultData.getArrayPathId();
            //由自身算路得来的路线数据未包含arrayPathId，需设置
            if ((arrayPathId == null || arrayPathId.size() == 0) && isLegalData(routeCarResultData)) {
                arrayPathId = arrayPathInfoId2ArrayString(routeCarResultData.getPathResult());
            }
            if (arrayPathId == null || arrayPathId.size() == 0) {
                Timber.d("array path id is null");
                return;
            }
            String filePath = TextUtils.join("", arrayPathId.toArray());
            byte[] driveGuideData = routeCarResultData.getPathResultDataInfo().calcRouteResultData.driveGuideData;
            byte[] drivePlanData = routeCarResultData.getPathResultDataInfo().calcRouteResultData.drivePlanData;
            byte[] drivePoiData = new ConsisUserData().buildData(routeCarResultData);

            if (driveGuideData == null) {
                driveGuideData = new byte[0];
            }
            if (drivePlanData == null) {
                drivePlanData = new byte[0];
            }
            if (drivePoiData == null) {
                drivePoiData = new byte[0];
            }

            byte[] mergeByte = new byte[driveGuideData.length + drivePlanData.length + drivePoiData.length];
            //合并数组至一个数组
            System.arraycopy(driveGuideData, 0, mergeByte, 0, driveGuideData.length);
            System.arraycopy(drivePlanData, 0, mergeByte, driveGuideData.length, drivePlanData.length);
            System.arraycopy(drivePoiData, 0, mergeByte, driveGuideData.length + drivePlanData.length, drivePoiData.length);
            ConsisPathData consisPathData = new ConsisPathData();
            consisPathData.drivePoiDataLength = drivePoiData.length;
            consisPathData.driveGuideDataLength = driveGuideData.length;
            consisPathData.drivePlanDataLength = drivePlanData.length;
            consisPathData.planChannelId = arrayPathId.get(0);
            arrayPathId.remove(0);
            consisPathData.arrayPathId = arrayPathId;
            writeToFile(mergeByte, mMultiRouteDataPath + filePath, false);
            mConsisPathDataList.add(consisPathData);
        }
    }

    private void writeToFile(byte[] byteArr, @NonNull String fileRelPathAndName, boolean append) {
        long startTime = System.currentTimeMillis();
        if (TextUtils.isEmpty(fileRelPathAndName)) {
            return;
        }
        if (byteArr == null || byteArr.length == 0) {
            return;
        }

        Timber.d("writeToFile: fileName=%s", fileRelPathAndName);
        File file = new File(this.mMultiRouteDataPath);
        if (!file.exists()) {
            file.mkdirs();
        }

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileRelPathAndName);
            fileOutputStream.write(byteArr);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.safetyClose(fileOutputStream);
        }

        Timber.d((System.currentTimeMillis() - startTime) + "");
    }

    /**
     * 接收算路参数数据
     *
     * @param option
     */
    public void onSyncRouteOption(RouteOption option) {
        if (mTempRouteOptionList == null) {
            mTempRouteOptionList = new ArrayList<>();
        }
        RouteOption routeOption = new RouteOption(option);
        mTempRouteOptionList.add(routeOption);
    }


    public void coverBlPathDataToHMI(RouteCarResultData routeCarResultData, ArrayList<PathInfo> pil) {
        mRouteCarResultData = routeCarResultData;
        coverBlPathDataToHMI(routeCarResultData, routeCarResultData.getPathResultDataInfo(), pil);
    }

    /**
     * 自身算路回调
     *
     * @param rcrd
     * @param prd
     * @param pil
     */
    public void coverBlPathDataToHMI(RouteCarResultData rcrd, PathResultData prd, ArrayList<PathInfo> pil) {
        mRouteCarResultData = rcrd;
        mRouteCarResultData.setPathResultDataInfo(pathResultDataToInfo(prd));
        mRouteCarResultData.setIsOffline(prd != null && prd.isLocal);
        mRouteCarResultData.setPathResult(pil);
    }


    /**
     * 获取上一次的路线数据
     *
     * @return
     */
    public RouteCarResultData getPreviousRouteCarResultData() {
        return mRouteCarResultData;
    }

    public void deleteCacheRoute(int deleteType) {
        if (mConsisPathDataList.size() > 0) {
            if (deleteType == DELETE_ALL_ROUTE) {
                mConsisPathDataList.clear();
            } else {
                for (int i = mConsisPathDataList.size() - 1; i >= 0; i--) {
                    ConsisPathData consisPathData = mConsisPathDataList.get(i);
                    if (consisPathData != null) {
                        if ((deleteType == DELETE_MASTER_ROUTE && MASTER_SDK.equals(consisPathData.planChannelId))
                                || (deleteType == DELETE_SIDE_ROUTE && !MASTER_SDK.equals(consisPathData.planChannelId))) {
                            mConsisPathDataList.remove(i);
                        }
                    }
                }
            }

        }

        File[] files = FileUtils.listSubFiles(mMultiRouteDataPath);
        if (files != null && files.length > 0) {
            try {
                for (File file : files) {
                    if (file != null && file.exists() && file.isFile()) {
                        if (deleteType == DELETE_ALL_ROUTE
                                || (deleteType == DELETE_MASTER_ROUTE && file.getName().startsWith(MASTER_SDK))
                                || (deleteType == DELETE_SIDE_ROUTE && !file.getName().startsWith(MASTER_SDK))) {
                            file.delete();
                        }
                    }
                }
            } catch (Exception e) {
                Timber.d("deleteCacheRoute: Exception=%s", e.toString());
            }
        }
    }

    public void destroyHmiRouteCarResultData() {
        mConsisPathDataList.clear();
        mRouteFocusIndex = 0;
        NaviComponent.getInstance().deleteNaviPath();
        FileUtils.deleteDir(this.mMultiRouteDataPath);
        if (mRouteCarResultData != null) {
            ArrayList<PathInfo> pathResult = mRouteCarResultData.getPathResult();
            if (pathResult != null) {
                NaviController.getInstance().deletePath(pathResult);
            }
        }
        mRouteCarResultData = null;
    }

    public RouteCarResultData getRouteCarResultData() {
        return mRouteCarResultData;
    }

    private ArrayList<String> arrayPathInfoId2ArrayString(ArrayList<PathInfo> list) {
        ArrayList<String> arrayString = new ArrayList<>();
        if (list != null && list.size() > 0) {
            arrayString.add(list.get(0).getPlanChannelId());
            for (PathInfo pathInfo : list) {
                arrayString.add(String.valueOf(pathInfo.getPathID()));
            }
        }
        return arrayString;
    }

    public String pathInfoIds2String(ArrayList<PathInfo> list) {
        if (list != null && list.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            PathInfo firstPathInfo = list.get(0);
            stringBuilder.append(firstPathInfo.getPlanChannelId()).append("_").append(firstPathInfo.getPathID());
            for (int i = 0; i < list.size(); i++) {
                stringBuilder.append(list.get(i).getPathID());
            }
            return stringBuilder.toString();
        }
        return "";
    }

    public String pathInfoIds3String(ArrayList<ConsisPathIdentity> list) {
        if (list != null && list.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(list.get(0).planChannelId);
            for (int i = 0; i < list.size(); i++) {
                stringBuilder.append(list.get(i).pathId);
            }
            return stringBuilder.toString();
        }
        return "";
    }


    /**
     * 检查入参是否合法
     *
     * @param routeCarResultData
     * @return
     */
    private boolean isLegalData(RouteCarResultData routeCarResultData) {
        //路线相关检查
        if (routeCarResultData == null || routeCarResultData.getPathResultDataInfo() == null || routeCarResultData.getPathResult() == null || routeCarResultData.getPathResult().size() == 0) {
            return false;
        }
        //起终点检查
        if (routeCarResultData.getToPOI() == null || routeCarResultData.getFromPOI() == null) {
            return false;
        }

        return true;
    }

    /**
     * 检查入参是否合法，其中不包括对路线数据的检查
     *
     * @param routeCarResultData
     * @return
     */
    private boolean isLegalPathResultData(RouteCarResultData routeCarResultData) {
        //路线相关检查
        if (routeCarResultData == null || routeCarResultData.getPathResultDataInfo() == null) {
            return false;
        }
        //起终点检查
        if (routeCarResultData.getToPOI() == null || routeCarResultData.getFromPOI() == null) {
            return false;
        }

        return true;
    }


    public PathResultDataInfo pathResultDataToInfo(PathResultData pathResultData) {
        if (pathResultData == null) {
            return null;
        }
        PathResultDataInfo pathResultDataInfo = new PathResultDataInfo();
        pathResultDataInfo.errorCode = pathResultData.errorCode;
        pathResultDataInfo.isChange = pathResultData.isChange;
        pathResultDataInfo.isLocal = pathResultData.isLocal;
        pathResultDataInfo.mode = pathResultData.mode;
        pathResultDataInfo.calcRouteResultData.drivePlanData = pathResultData.calcRouteResultData.drivePlanData;
        pathResultDataInfo.calcRouteResultData.driveGuideData = pathResultData.calcRouteResultData.driveGuideData;
        pathResultDataInfo.requestId = pathResultData.requestId;
        pathResultDataInfo.type = pathResultData.type;
        pathResultDataInfo.routeRestorationData = pathResultData.routeRestorationData;
        return pathResultDataInfo;
    }

    /**
     * 同步路线数据给副屏
     * 使用场景：主屏先启动并规划路线进入路线规划页面，之后副屏启动。此时需要向主屏获取路线数据。主屏调用该方法
     */
    public void syncRouteToSideScreen() {
        RouteCarResultData routeCarResultData = PathInfoManager.getInstance().getLastRoute(true);
        if (routeCarResultData != null) {
            byte[] userData = new ConsisUserData().buildData(routeCarResultData);
            PathResultDataInfo pathResultDataInfo = routeCarResultData.getPathResultDataInfo();
            if (pathResultDataInfo != null && userData != null && userData.length > 0) {
                ConsisPathBinaryData consisPathBinaryData = new ConsisPathBinaryData();
                consisPathBinaryData.calcRouteResultData = routeCarResultData.getPathResultDataInfo().calcRouteResultData;
                BinaryStream binaryStream = new BinaryStream(userData);
                NaviController.getInstance().syncOnlinePathToMultiSource(consisPathBinaryData, binaryStream);
            }
        }
    }

    public RouteCarResultData getIntentRouteCarResultData() {
        if (null == mIntentRouteCarResultData) {
            return null;
        }
        RouteCarResultData tmpData = mIntentRouteCarResultData.get();
        /**
         * 用于页面跳转的路线临时保存，获取完主动释放
         */
        mIntentRouteCarResultData = null;
        return tmpData;
    }

    public void setIntentRouteCarResultData(RouteCarResultData mIntentRouteCarResultData) {
        this.mIntentRouteCarResultData = new WeakReference<>(mIntentRouteCarResultData);
    }

    /**
     * 多屏一致性：通过算路结果来归属屏和算路的requestID来获取对应的算路参数
     *
     * @param pathResultData
     * @return
     */
    private RouteOption getRouteOption(PathResultData pathResultData) {
        if (pathResultData == null) {
            return null;
        }
        if (mTempRouteOptionList == null) {
            mTempRouteOptionList = new ArrayList<>();
        }
        if (mTempRouteOptionList.isEmpty()) {
            return null;
        }
        RouteOption routeOption = null;
        for (int i = 0; i < mTempRouteOptionList.size(); i++) {
            RouteOption option = mTempRouteOptionList.get(i);
            if (Objects.equals(option.getConsisExternData(), pathResultData.planChannelId) && option.getRouteReqId() == pathResultData.requestId) {
                //通过算路结果来归属屏和算路的requestID来获取对应的算路参数
                routeOption = option;
                break;
            }
        }
        return routeOption;
    }

}
