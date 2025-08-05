package com.autosdk.bussiness.push.listener;

import com.autonavi.gbl.user.msgpush.model.AimPoiPushMsg;
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg;

/**
 * send2car 消息接收监听器，包含了POI和路线
 * @author AutoSDk
 */
public interface AimPushMessageListener {

    /**
     * POI消息接收
     * @param aimPoiPushMsg
     */
    void notifyPoiPushMessage(AimPoiPushMsg aimPoiPushMsg);

    /**
     * 路线消息推送接收
     * @param aimPoiPushMsg
     */
    void notifyRoutePushMessage(AimRoutePushMsg aimPoiPushMsg);

    /**
     * 标识是否send2car
     */
     boolean isSend2Car();
}
