package com.autosdk.bussiness.push.listener;

import com.autonavi.gbl.user.msgpush.model.AutoPushMsg;

/**
 * 运营推送消息通知监听器
 * @author AutoSDK
 */
public interface AutoPushMessageListener {

    /**
     * 运营推送消息通知
     * @param msg
     */
    void notifyMessage(AutoPushMsg msg);
}
