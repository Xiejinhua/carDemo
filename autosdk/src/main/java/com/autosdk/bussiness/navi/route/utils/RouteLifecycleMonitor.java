package com.autosdk.bussiness.navi.route.utils;

import com.autonavi.gbl.common.path.option.PathInfo;
import com.autosdk.bussiness.navi.NaviController;

import java.util.ArrayList;
import java.util.Iterator;

import timber.log.Timber;

public class RouteLifecycleMonitor {

    public static final String TAG = "RouteLifecycleMonitor";

    private ArrayList<PathInfo> mPathResult;

    private static class SinglonHolder {
        private static RouteLifecycleMonitor instance = new RouteLifecycleMonitor();
    }

    public static RouteLifecycleMonitor getInstance() {
        return SinglonHolder.instance;
    }

    /**
     * cache结果的时候会将引用计数置成1.
     *
     * @param result 算路结果
     */
    public void setPathResult(final ArrayList<PathInfo> result) {
        // 清除上一次老路线缓存
        destoryPathResult();
        //无效的结果不需要cache
        if (result == null || result.size() <= 0) {
            return;
        }
        mPathResult = result;
        Timber.i("cacheCalRouteResult:result size = %s", result.size());
    }

    public ArrayList<PathInfo> getPathResult() {
        Timber.i("getPathResult:result = %s", mPathResult == null ? "mPathResult is Null" : mPathResult.size());
        return mPathResult;
    }

    //删除指定路线，如经过备选路口
    public synchronized void deletePath(ArrayList<Long> pathIDList) {
        if (pathIDList == null || pathIDList.size() <= 0 || mPathResult == null) {
            Timber.e("deletePath:pathIDList fail");
            return;
        }
        Timber.i("deletePath:pathIDList = %s", pathIDList);
        Timber.i("deletePath:mPathResult = %s", mPathResult.size());
        Iterator<PathInfo> it = mPathResult.iterator();
        while (it.hasNext()) {
            PathInfo pathInfo = it.next();
            long pathID = pathInfo.getPathID();
            if (pathIDList.contains(pathID)) {
                it.remove();
            }
        }
        Timber.i("deletePath:mPathResult = %s", mPathResult.size());
    }

    /**
     * 路线结果释放
     */
    public void destoryPathResult() {
        Timber.i(" destoryPathResult is called ");
        NaviController.getInstance().deletePath(mPathResult);
    }
}
