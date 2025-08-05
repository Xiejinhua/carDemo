package com.autosdk.bussiness.push.listener;

import com.autonavi.gbl.user.msgpush.model.DestinationPushMsg;
import com.autonavi.gbl.user.msgpush.model.LinkStatusPushMsg;
import com.autonavi.gbl.user.msgpush.model.PlanPrefPushMsg;
import com.autonavi.gbl.user.msgpush.model.QuitNaviPushMsg;

/**
 * 手车互联相关推送消息
 * @author AutoSDK
 */
public interface LinkPushMessageListener {

    /**
     * 目的地消息推送接收
     * @param destinationPushMsg
     */
    void notifyDestinationPushMessage(DestinationPushMsg destinationPushMsg);

    /**
     * 手车互联状态消息
     * @param linkStatusPushMsg
     */
    void notifyLinkStatusPushMessage(LinkStatusPushMsg linkStatusPushMsg);

    /**
     * 退出导航消息
     * @param quitNaviPushMsg
     */
    void notifyQuitNaviPushMessage(QuitNaviPushMsg quitNaviPushMsg);

    /**
     * 路线偏好消息
     * @param planPrefPushMsg
     */
    void notifyPlanPrefPushMessage(PlanPrefPushMsg planPrefPushMsg);
}
