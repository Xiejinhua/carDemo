package com.autosdk.bussiness.push.listener;

import com.autonavi.gbl.user.msgpush.model.TeamPushMsg;
import com.autonavi.gbl.user.msgpush.model.TeamUploadResponseMsg;

/**
 * 组队消息通知监听器
 */
public interface TeamMessageListener {

    /**
     * 组队推送消息通知
     * @param teamPushMsg
     */
    void notifyTeamPushMessage(TeamPushMsg teamPushMsg);

    /**
     * 组队位置上报返回消息通知
     * @param teamUploadResponseMsg
     */
    void notifyTeamUploadResponseMessage(TeamUploadResponseMsg teamUploadResponseMsg);

}
