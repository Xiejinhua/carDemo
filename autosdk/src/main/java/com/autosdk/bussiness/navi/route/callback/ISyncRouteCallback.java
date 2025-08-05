package com.autosdk.bussiness.navi.route.callback;

import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.util.model.BinaryStream;

import java.util.ArrayList;

/**
 * 远端接收路线数据
 * @author AutoSDK
 */
public interface ISyncRouteCallback {

    void onSyncRouteCallback(long syncReqId, String planChannelId, ArrayList<PathInfo> pathInfoList, BinaryStream userData, int errCode);
}
