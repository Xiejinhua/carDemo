package com.autosdk.bussiness.push.listener;

import com.autonavi.gbl.user.msgpush.model.ParkPushMsg;

/**
 * 停车场支付推送消息通知监听
 */
public interface ParkPushMessageListener {

    /**
     * 停车场支付推送消息通知
     * @param parkPushMsg
     */
    void notifyMessage(ParkPushMsg parkPushMsg);
}
