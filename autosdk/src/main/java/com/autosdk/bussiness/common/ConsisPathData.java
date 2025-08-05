package com.autosdk.bussiness.common;

import java.util.ArrayList;

import android.text.TextUtils;
import com.autonavi.gbl.common.model.CalcRouteResultData;
import com.autonavi.gbl.common.path.DrivePathDecoder;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.route.model.ConsisPathIdentity;
import com.autosdk.bussiness.navi.NaviController;
import com.autosdk.bussiness.navi.route.model.PathResultDataInfo;
import com.autosdk.bussiness.navi.route.model.RouteCarResultData;

/**
 * 多屏路线数据对象
 * 不包含路綫实际路线数据，只保存数据属性
 *
 * @author AutoSdk
 */
public class ConsisPathData {

    public int driveGuideDataLength = -1;
    public int drivePlanDataLength = -1;
    public int drivePoiDataLength = -1;
    public String planChannelId;
    public ArrayList<String> arrayPathId;


    /**
     * 转换字节数组
     *
     * @param data 字节数组
     * @return 自定义路线数据集合
     */
    public RouteCarResultData transformPathData(byte[] data) {
        if (data != null && data.length > 0) {
            byte[] driveGuideData = new byte[driveGuideDataLength];
            System.arraycopy(data, 0, driveGuideData, 0, driveGuideDataLength);
            byte[] drivePlanData = new byte[drivePlanDataLength];
            System.arraycopy(data, driveGuideDataLength, drivePlanData, 0, drivePlanDataLength);
            byte[] drivePoiData = new byte[drivePoiDataLength];
            System.arraycopy(data, driveGuideDataLength + drivePlanDataLength, drivePoiData, 0, drivePoiDataLength);
            CalcRouteResultData calcRouteResultData = new CalcRouteResultData();
            calcRouteResultData.driveGuideData = driveGuideData;
            calcRouteResultData.drivePlanData = drivePlanData;
            ArrayList<PathInfo> pathResult = DrivePathDecoder.decodeMultiRouteData(calcRouteResultData);
            ConsisUserData consisUserData = new ConsisUserData();
            RouteCarResultData routeCarResultData = consisUserData.restore(drivePoiData);
            PathResultDataInfo pathResultDataInfo = new PathResultDataInfo();
            pathResultDataInfo.calcRouteResultData= calcRouteResultData;
            routeCarResultData.setPathResultDataInfo(pathResultDataInfo);
            routeCarResultData.setPathResult(pathResult);
            return routeCarResultData;
        }
        return null;
    }

    /**
     * RsuSdk MainSdk
     *
     * @return
     */
    public boolean matchOrNot(String previousString) {
        if (!TextUtils.isEmpty(previousString)) {
            String planChannelName = "Sdk";
            String[] params = previousString.split(planChannelName);
            if (params.length > 1) {
                return planChannelId.equals(params[0] + planChannelName) && (arrayPathId.contains(params[1]) || TextUtils.join("", arrayPathId.toArray()).contains(params[1]));
            }
        }
        return false;
    }

    public boolean matchOrNot(ArrayList<ConsisPathIdentity> consisPathIdentities) {
        if (consisPathIdentities != null && arrayPathId != null) {
            StringBuffer stringBuffer = new StringBuffer();
            for (ConsisPathIdentity consisPathIdentity : consisPathIdentities) {
                stringBuffer.append(consisPathIdentity.pathId);
            }
            if(NaviController.getInstance().isNaving()){
                if (stringBuffer.toString().equals(TextUtils.join("",arrayPathId.toArray()))){
                    return true;
                }
            }else {
                if (TextUtils.join("",arrayPathId.toArray()).contains(stringBuffer.toString())){
                    return true;
                }
            }

        }

        return false;
    }

    private String  getArrayPathIdString() {
        if (arrayPathId != null) {
            return TextUtils.join("",arrayPathId.toArray());
        }
        return "";
    }

    @Override
    public String toString() {
        return "ConsisPathData{" +
                "driveGuideDataLength=" + driveGuideDataLength +
                ", drivePlanDataLength=" + drivePlanDataLength +
                ", drivePoiDataLength=" + drivePoiDataLength +
                ", planChannelId='" + planChannelId + '\'' +
                ", arrayPathId=" + getArrayPathIdString() +
                '}';
    }
}
