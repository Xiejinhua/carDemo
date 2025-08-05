package com.autosdk.bussiness.push;


import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.user.model.UserLoginInfo;
import com.autonavi.gbl.user.msgpush.MsgPushService;
import com.autonavi.gbl.user.msgpush.model.AimPoiPushMsg;
import com.autonavi.gbl.user.msgpush.model.AimPushMsg;
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg;
import com.autonavi.gbl.user.msgpush.model.AutoPushMsg;
import com.autonavi.gbl.user.msgpush.model.DestinationPushMsg;
import com.autonavi.gbl.user.msgpush.model.LinkStatusPushMsg;
import com.autonavi.gbl.user.msgpush.model.MsgPushInitParam;
import com.autonavi.gbl.user.msgpush.model.MsgPushStatus;
import com.autonavi.gbl.user.msgpush.model.MsgPushType;
import com.autonavi.gbl.user.msgpush.model.ParkPushMsg;
import com.autonavi.gbl.user.msgpush.model.PaymentCapPushMsg;
import com.autonavi.gbl.user.msgpush.model.PlanPrefPushMsg;
import com.autonavi.gbl.user.msgpush.model.QuitNaviPushMsg;
import com.autonavi.gbl.user.msgpush.model.SafeSharePushMsg;
import com.autonavi.gbl.user.msgpush.model.SceneSendType;
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg;
import com.autonavi.gbl.user.msgpush.model.TeamUploadMsg;
import com.autonavi.gbl.user.msgpush.model.TeamUploadResponseMsg;
import com.autonavi.gbl.user.msgpush.model.Tripod2CarPushMsg;
import com.autonavi.gbl.user.msgpush.observer.IMsgPushServiceObserver;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.push.listener.AimPushMessageListener;
import com.autosdk.bussiness.push.listener.AutoPushMessageListener;
import com.autosdk.bussiness.push.listener.LinkPushMessageListener;
import com.autosdk.bussiness.push.listener.ParkPushMessageListener;
import com.autosdk.bussiness.push.listener.TeamMessageListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

/**
 * 消息推送控制器
 * 功能说明:
 *  * <p>一、背景说明:</p>
 *  * 消息推送包括了消息服务连接状态getMsgPushStatus()，组队消息通知TeamMessageListener、send2car、路线
 *  * <p>二、主要方法:</p>
 *  * <strong>1.初始化、反初始化</strong><br/>
 *  * init(String dataPath);    destory();<br/>
 *  * <p/>
 */
public class PushController implements IMsgPushServiceObserver {
    private List<TeamMessageListener> mTeamMessageListeners = new CopyOnWriteArrayList<>();

    private List<AutoPushMessageListener> mAutoPushMessageListeners = new CopyOnWriteArrayList<>();

    private List<AimPushMessageListener> mAimPushMessageListeners = new CopyOnWriteArrayList<>();

    private List<ParkPushMessageListener> mParkPushMessageListeners = new CopyOnWriteArrayList<>();

    private List<LinkPushMessageListener> mLinkPushMessageListeners = new CopyOnWriteArrayList<>();

    //同步锁
    private Object mLinkPushMessageLock = new Object();
    private Object mTeamMessageLock = new Object();
    private Object mAutoPushMessageLock = new Object();
    private Object mParkPushMessageLock = new Object();
    private Object mAimPushMessageLock = new Object();

    public MsgPushService mMsgPushService;

    /**
     * 消息推送链接状态
     */
    public int mMsgPushStatus = MsgPushStatus.MsgPushStatusDisconnected;
    private int mInitCode;

    private static class PushManagerHolder {
        private static PushController mInstance = new PushController();
    }

    private PushController() {
    }

    public static PushController getInstance() {
        return PushManagerHolder.mInstance;
    }

    /**
     * Push服务初始化
     * @param dataPath
     * @param mqttKey
     * @return
     */
    public void initPush(String dataPath, String mqttKey) {
        Timber.d("push init dataPath = %s", dataPath);
        mMsgPushService = (MsgPushService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.MsgPushSingleServiceID);
        mMsgPushService.addObserver(this);
        MsgPushInitParam msgPushParam = new MsgPushInitParam();
        // 消息存储数据库路径，设置目录要有文件创建、读写权限
        msgPushParam.dataPath = dataPath;
        // Mqtt消息推送服务链接Key
        msgPushParam.mqttKey = mqttKey;
        mInitCode = mMsgPushService.init(msgPushParam);
        Timber.d("push init dataPath = %s, result = %s", dataPath, mInitCode);
    }

    //判断初始化是否成功
    public boolean isInitSuccess() {
        return mInitCode == 0;
    }

    /**
     * 开始消息监听所有push消息，当前登录用户ID，传空只能接收 运营消息
     * 当未登录账号时，可先传""，账号登录后，则再次调用该接口
     * @param userId
     */
    public void startListen(String userId) {
        if(mMsgPushService == null) {
            return;
        }
        mMsgPushService.stopListen();
        // 开始消息监听
        UserLoginInfo info = new UserLoginInfo();
        // 设置当前登录用户ID，传空只能接收 运营消息
        info.userId = userId;
        int result = mMsgPushService.startListen(info);
        Timber.d("push startListen userId = %s, result = %s", userId, result);
    }

    public void stopListener(){
        if (mMsgPushService == null) {
            return;
        }
        mMsgPushService.stopListen();
    }

    /**
     * 反初始化Push服务
     */
    public void destroy() {
        Timber.d("push destroy");
        mTeamMessageListeners.clear();
        mAimPushMessageListeners.clear();
        mAutoPushMessageListeners.clear();
        mParkPushMessageListeners.clear();
        mLinkPushMessageListeners.clear();
        mMsgPushService.removeObserver(this);
        mMsgPushStatus = MsgPushStatus.MsgPushStatusDisconnected;
        mMsgPushService.stopListen();
    }

    @Override
    public void notifyStatus(@MsgPushStatus.MsgPushStatus1 int msgPushStatus) {
        Timber.d("push notifyStatus msgPushStatus = %s", msgPushStatus);
        mMsgPushStatus = msgPushStatus;
    }

    @Override
    public void notifyMessage(AutoPushMsg autoPushMsg) {
        if(mAutoPushMessageListeners != null && mAutoPushMessageListeners.size() > 0) {
            for(int i = 0;i < mAutoPushMessageListeners.size();i++) {
                mAutoPushMessageListeners.get(i).notifyMessage(autoPushMsg);
            }
        }
    }
    AimPoiPushMsg aimPoiPushMsg;
    @Override
    public void notifyMessage(AimPoiPushMsg aimPoiPushMsg) {
        Timber.d("notifyMessage.AimPoiPushMsg aimPoiPushMsg = %s;aimPoiPushMsg.sendType = %s;mAimPushMessageListeners.size() = %s", aimPoiPushMsg.content.name, aimPoiPushMsg.sendType, mAimPushMessageListeners.size());
        boolean isContainSend2CarListener = false;
        if(mAimPushMessageListeners != null && mAimPushMessageListeners.size() > 0) {
            for(int i = 0;i < mAimPushMessageListeners.size();i++) {
                mAimPushMessageListeners.get(i).notifyPoiPushMessage(aimPoiPushMsg);
                if (mAimPushMessageListeners.get(i).isSend2Car()) {
                    isContainSend2CarListener = true;
                }
            }
        }

        if (!isContainSend2CarListener && isSend2CarPoiMsg(aimPoiPushMsg)){
            Timber.d("notifyMessage.AimPoiPushMsg cache aimPoiPushMsg.");
            this.aimPoiPushMsg = aimPoiPushMsg;
        }else {
            this.aimPoiPushMsg = null;
        }
    }

    AimRoutePushMsg mAimRoutePushMsg;
    @Override
    public void notifyMessage(AimRoutePushMsg aimRoutePushMsg) {
        Timber.d("notifyMessage.AimRoutePushMsg aimRoutePushMsg.sendType = %s;mAimPushMessageListeners.size() = %s", aimRoutePushMsg.sendType, mAimPushMessageListeners.size());
        boolean isContainSend2CarListener = false;
        if(mAimPushMessageListeners != null && mAimPushMessageListeners.size() > 0) {
            for(int i = 0;i < mAimPushMessageListeners.size();i++) {
                mAimPushMessageListeners.get(i).notifyRoutePushMessage(aimRoutePushMsg);
                if (mAimPushMessageListeners.get(i).isSend2Car()) {
                    isContainSend2CarListener = true;
                }
            }
        }

        if (!isContainSend2CarListener && isSend2CaRouteMsg(aimRoutePushMsg)){
            Timber.d("notifyMessage.AimRoutePushMsg cache aimPoiPushMsg.");
            this.mAimRoutePushMsg = aimRoutePushMsg;
        }else {
            this.mAimRoutePushMsg = null;
        }
    }

    @Override
    public void notifyMessage(ParkPushMsg parkPushMsg) {
        if(mParkPushMessageListeners != null && mParkPushMessageListeners.size() > 0) {
            for(int i = 0;i < mParkPushMessageListeners.size();i++) {
                mParkPushMessageListeners.get(i).notifyMessage(parkPushMsg);
            }
        }
    }

    @Override
    public void notifyMessage(TeamPushMsg teamPushMsg) {
        if(mTeamMessageListeners != null && mTeamMessageListeners.size() > 0) {
            for(int i = 0;i < mTeamMessageListeners.size();i++) {
                mTeamMessageListeners.get(i).notifyTeamPushMessage(teamPushMsg);
            }
        }
    }

    @Override
    public void notifyMessage(TeamUploadResponseMsg teamUploadResponseMsg) {
        if(mTeamMessageListeners != null && mTeamMessageListeners.size() > 0) {
            for(int i = 0;i < mTeamMessageListeners.size();i++) {
                mTeamMessageListeners.get(i).notifyTeamUploadResponseMessage(teamUploadResponseMsg);
            }
        }
    }

    @Override
    public void notifyMessage(Tripod2CarPushMsg tripod2CarPushMsg) {

    }

    @Override
    public void notifyMessage(LinkStatusPushMsg linkStatusPushMsg) {
        if(mLinkPushMessageListeners != null && mLinkPushMessageListeners.size() > 0) {
            for(int i = 0;i < mLinkPushMessageListeners.size();i++) {
                mLinkPushMessageListeners.get(i).notifyLinkStatusPushMessage(linkStatusPushMsg);
            }
        }
    }

    @Override
    public void notifyMessage(QuitNaviPushMsg quitNaviPushMsg) {
        if(mLinkPushMessageListeners != null && mLinkPushMessageListeners.size() > 0) {
            for(int i = 0;i < mLinkPushMessageListeners.size();i++) {
                mLinkPushMessageListeners.get(i).notifyQuitNaviPushMessage(quitNaviPushMsg);
            }
        }
    }

    @Override
    public void notifyMessage(PlanPrefPushMsg planPrefPushMsg) {
        if(mLinkPushMessageListeners != null && mLinkPushMessageListeners.size() > 0) {
            for(int i = 0;i < mLinkPushMessageListeners.size();i++) {
                mLinkPushMessageListeners.get(i).notifyPlanPrefPushMessage(planPrefPushMsg);
            }
        }
    }

    @Override
    public void notifyMessage(SafeSharePushMsg safeSharePushMsg) {

    }

    @Override
    public void notifyMessage(DestinationPushMsg destinationPushMsg) {
        Timber.d("DestinationPushMsg = %s", destinationPushMsg.content);
        if(mLinkPushMessageListeners != null && mLinkPushMessageListeners.size() > 0) {
            for(int i = 0;i < mLinkPushMessageListeners.size();i++) {
                mLinkPushMessageListeners.get(i).notifyDestinationPushMessage(destinationPushMsg);
            }
        }
    }

    @Override
    public void notifyMessage(PaymentCapPushMsg paymentCapPushMsg) {

    }

    /**
     * 注册组队消息通知
     * @param listener
     */
    public void addTeamMessageListener(TeamMessageListener listener) {
        if(mTeamMessageListeners == null) {
            mTeamMessageListeners = new CopyOnWriteArrayList<>();
        }
        synchronized (mTeamMessageLock) {
            if(!mTeamMessageListeners.contains(listener)) {
                this.mTeamMessageListeners.add(listener);
            }
        }
    }

    /**
     * 移除组队消息通知
     * @param listener
     */
    public void removeTeamMessageListener(TeamMessageListener listener) {
        if(mTeamMessageListeners == null) {
            return;
        }
        synchronized (mTeamMessageLock) {
            if(mTeamMessageListeners != null) {
                this.mTeamMessageListeners.remove(listener);
            }
        }
    }

    /**
     * 注册运营推送消息通知
     * @param listener
     */
    public void addPushMessageListener(AutoPushMessageListener listener) {
        if(mAutoPushMessageListeners == null) {
            mAutoPushMessageListeners = new CopyOnWriteArrayList<>();
        }
        synchronized (mAutoPushMessageLock) {
            if(!mAutoPushMessageListeners.contains(listener)) {
                this.mAutoPushMessageListeners.add(listener);
            }
        }
    }

    /**
     * 移除运营推送消息通知
     * @param listener
     */
    public void removePushMessageListener(AutoPushMessageListener listener) {
        if(mAutoPushMessageListeners == null) {
            return;
        }
        synchronized (mAutoPushMessageLock) {
            if(mAutoPushMessageListeners != null) {
                this.mAutoPushMessageListeners.remove(listener);
            }
        }
    }

    /**
     * 注册send2car 推送监听
     * @param listener
     */
    public void addSend2carPushMsgListener(AimPushMessageListener listener) {
        if(mAimPushMessageListeners == null) {
            mAimPushMessageListeners = new CopyOnWriteArrayList<>();
        }
        synchronized (mAimPushMessageLock) {
            if(!mAimPushMessageListeners.contains(listener)) {
                this.mAimPushMessageListeners.add(listener);
            }
        }
    }

    /**
     * 移除send2car 推送监听
     * @param listener
     */
    public void removeSend2carPushMsgListener(AimPushMessageListener listener) {
        if(mAimPushMessageListeners == null) {
            return;
        }
        synchronized (mAimPushMessageLock) {
            if(mAimPushMessageListeners != null) {
                this.mAimPushMessageListeners.remove(listener);
            }
        }
    }

    /**
     * 注册停车场支付推送消息通知监听
     * @param listener
     */
    public void addParkPushMessageListener(ParkPushMessageListener listener) {
        if(mParkPushMessageListeners == null) {
            mParkPushMessageListeners = new CopyOnWriteArrayList<>();
        }
        synchronized (mParkPushMessageLock) {
            if(!mParkPushMessageListeners.contains(listener)) {
                this.mParkPushMessageListeners.add(listener);
            }
        }
    }

    /**
     * 移除停车场支付推送消息通知监听
     * @param listener
     */
    public void removeParkPushMessageListener(ParkPushMessageListener listener) {
        if(mParkPushMessageListeners == null) {
            return;
        }
        synchronized (mParkPushMessageLock) {
            if(mParkPushMessageListeners != null) {
                this.mParkPushMessageListeners.remove(listener);
            }
        }
    }

    /**
     * 注册手车互联推送消息通知监听
     * @param listener
     */
    public void addLinkPushMessageListener(LinkPushMessageListener listener) {
        if(mLinkPushMessageListeners == null) {
            mLinkPushMessageListeners = new CopyOnWriteArrayList<>();
        }
        synchronized (mLinkPushMessageLock) {
            if(!mLinkPushMessageListeners.contains(listener)) {
                this.mLinkPushMessageListeners.add(listener);
            }
        }
    }

    /**
     * 移除手车互联推送消息通知监听
     * @param listener
     */
    public void removeLinkPushMessageListener(LinkPushMessageListener listener) {
        if(mLinkPushMessageListeners == null) {
            return;
        }
        synchronized (mLinkPushMessageLock) {
            if(mLinkPushMessageListeners != null) {
                this.mLinkPushMessageListeners.remove(listener);
            }
        }
    }

    /**
     * 获取消息推送链接状态
     * @return
     */
    public @MsgPushStatus.MsgPushStatus1 int getMsgPushStatus() {
        Timber.d("push getMsgPushStatus msgPushStatus = %s", mMsgPushStatus);
        return mMsgPushStatus;
    }

    /**
     * 获取组队推送消息
     * @return
     */
    public ArrayList<TeamPushMsg> getTeamPushMsgMessages() {
        if(mMsgPushService!=null){
           return mMsgPushService.getTeamPushMsgMessages();
        }
        return null;
    }

    /**
     * 删除组队消息单个记录
     * @param messageId
     */
    public void deleteTeamPushMsgMessages(long messageId) {
        if(mMsgPushService!=null){
            mMsgPushService.deleteMessage(MsgPushType.MsgPushTypeTeam, messageId);
        }
    }

    /**
     * 上报当前组队位置信息
     * @return
     */
    public void publishTeamInfo(TeamUploadMsg uploadMsg) {
        if(mMsgPushService!=null){
           mMsgPushService.publishTeamInfo(uploadMsg);
        }
    }

    /**
     * 获取send2car消息列表
     * @return
     */
    public ArrayList<AimPoiPushMsg> getSend2carMsg() {
        if(mMsgPushService!=null){
            return mMsgPushService.getAimPoiPushMessages();
        }
        return null;
    }

    /**
     * 删除send2car单个记录
     * @param type
     * @param messageId
     */
    public void deleteSend2carMsg(@MsgPushType.MsgPushType1 int type, long messageId) {
        if(mMsgPushService!=null){
//            mMsgPushService.deleteMessage(MsgPushType.MsgPushTypeAimPoi, aimPoiPushMsg.messageId);
            mMsgPushService.deleteMessage(type, messageId);
        }
    }


    /**
     * 获取send2car消息列表
     * @return
     */
    public ArrayList<AimPushMsg> getSend2carPushMsg() {
        if(mMsgPushService!=null){
            return mMsgPushService.getAimPushMsgCollection();
        }
        return null;
    }

    /**
     * 是否send2car poi消息
     * @param aimPoiPushMsg
     * @return
     */
    public boolean isSend2CarPoiMsg(AimPoiPushMsg aimPoiPushMsg){
        return aimPoiPushMsg != null && aimPoiPushMsg.sendType == SceneSendType.SceneTypeInvalid;
    }

    public boolean isSend2CaRouteMsg(AimRoutePushMsg aimRoutePushMsg){
        return aimRoutePushMsg != null && aimRoutePushMsg.sendType == SceneSendType.SceneTypeInvalid;
    }

    /**
     * 发送缓存send2car消息
     */
    public void sendCacheSend2CarMsg(){
        Timber.d("sendCacheSend2CarMsg aimPoiPushMsg=%s; mAimRoutePushMsg=%s",
                aimPoiPushMsg != null ? aimPoiPushMsg.content.name : "null" , mAimRoutePushMsg != null ? mAimRoutePushMsg.content.routeParam.naviId : "null");
        if(aimPoiPushMsg != null){
            if(mAimPushMessageListeners != null && mAimPushMessageListeners.size() > 0) {
                for(int i = 0;i < mAimPushMessageListeners.size();i++) {
                    mAimPushMessageListeners.get(i).notifyPoiPushMessage(aimPoiPushMsg);
                }
            }
            aimPoiPushMsg = null;
        }
        if(mAimRoutePushMsg != null){
            if(mAimPushMessageListeners != null && mAimPushMessageListeners.size() > 0) {
                for(int i = 0;i < mAimPushMessageListeners.size();i++) {
                    mAimPushMessageListeners.get(i).notifyRoutePushMessage(mAimRoutePushMsg);
                }
            }
            mAimRoutePushMsg = null;
        }
    }

    /**
     * 标记消息已读
     */
    public int markMessageAsRead(int type, long id) {
        if (mMsgPushService != null) {
            return mMsgPushService.markMessageAsRead(type, id);
        }
        return -2;
    }

    /**
     * 获取路线消息列表
     * @return
     */
    public ArrayList<AimRoutePushMsg> getAimRoutePushMsg() {
        if (mMsgPushService != null) {
            //判断没有手车互联的消息的代码
            ArrayList<AimRoutePushMsg> noHaveTraceIdRoutePushMsg = new ArrayList<>();
            if (null != mMsgPushService.getAimRoutePushMessages() && mMsgPushService.getAimRoutePushMessages().size() > 0) {
                for (AimRoutePushMsg message : mMsgPushService.getAimRoutePushMessages()) {
                    //发送分享消息的时候 traceId是空的
                    if (null != message && isSend2CaRouteMsg(message)) {
                        noHaveTraceIdRoutePushMsg.add(message);
                    }
                }
            }
            return noHaveTraceIdRoutePushMsg;
        }
        return null;
    }
}
