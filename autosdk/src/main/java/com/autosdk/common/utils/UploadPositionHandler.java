package com.autosdk.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.layer.model.BizLayerUtil;
import com.autonavi.gbl.user.account.model.AccountProfile;
import com.autonavi.gbl.user.msgpush.model.TeamMember;
import com.autonavi.gbl.user.msgpush.model.TeamUploadMsg;
import com.autonavi.gbl.user.msgpush.model.TeamUploadResponseMsg;
import com.autosdk.BuildConfig;
import com.autosdk.R;
import com.autosdk.bussiness.account.AccountController;
import com.autosdk.bussiness.account.UserGroupController;
import com.autosdk.bussiness.common.task.TaskManager;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.push.PushController;
import com.autosdk.common.AutoConstant;

import timber.log.Timber;

/**
 * 组队5秒上报自己位置工具
 **/
public class UploadPositionHandler {

    private final static int TASK_TIMER = 10001;
    private int delayedTime = 5 * 1000;
    private static UploadPositionHandler mInstance;
    private int count=0;
    private boolean isShowPositionDrift=true;//300秒内不在重复弹出位置漂移提示
    protected Handler mPositionDriftHandler = new Handler(Looper.getMainLooper());
    private boolean[] positionDrift =new boolean[5];
    private int pushPosNum;
    private boolean isUploading=false;//是否正在上报

    public static UploadPositionHandler getInstance() {
        if (mInstance == null) {
            mInstance = new UploadPositionHandler();
        }
        return mInstance;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Timber.d("UploadPositionTaskTimer handler msg.what=%s", msg.what);
            if (msg.what == TASK_TIMER) {
                publishTeamInfo();
                mHandler.sendEmptyMessageDelayed(TASK_TIMER, delayedTime);
            }
        }
    };

    /**
     * 开始5s一次上报自己位置
     */
    public void doStart(int delay) {
        if (delay != delayedTime){
            isUploading = false;
        }
        if(isUploading){
            return;
        }
        if (null != mHandler) {
            Timber.e("doStartGroupPosition delay:%s delayedTime:%s", delay, delayedTime);
            if (delay != delayedTime){
                delayedTime = delay;
            }
            mHandler.removeCallbacksAndMessages(null);
            mHandler.sendEmptyMessageDelayed(TASK_TIMER, delayedTime);
            isUploading=true;
        }
    }


    /**
     * 取消定时器
     */
    public void doStop() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            delayedTime = 5 * 1000;
        }
        clearCount();
        isUploading=false;
    }

    /**
     * 上报组队位置信息
     */
    public void publishTeamInfo() {
        try{
            // 上报当前位置信息
            String teamId = UserGroupController.getInstance().getTeamId();
            String uid = "";
            if (AccountController.getInstance().getAccountInfo() != null) {
                uid = AccountController.getInstance().getAccountInfo().uid;
            }
            if (!"".equals(teamId) && !"".equals(uid)) {
                Location location = SDKManager.getInstance().getLocController().getLastLocation();
                TeamUploadMsg uploadMsg = new TeamUploadMsg();
                uploadMsg.uid = uid;   /**< 当前登录用户ID */
                uploadMsg.channel = BuildConfig.autoChannelId;         /**< 渠道id, 固定值 */
                uploadMsg.lon = location.getLongitude();  /**< 位置经度 */
                uploadMsg.lat = location.getLatitude();  /**< 位置纬度 */
                uploadMsg.teamid = teamId;  // 队伍id
                uploadMsg.teamStamp = UserGroupController.getInstance().getMemberStamp();  // 基础信息md5
                uploadMsg.memberStamp = UserGroupController.getInstance().getMemberStamp(); // 成员信息md5
                PushController.getInstance().publishTeamInfo(uploadMsg);
            }
        }catch (Exception e){
            Timber.i("publishTeamInfo Exception e: %s", e.getMessage());
        }
    }

    /**
     * 显示位置偏移toast提示
     * 策略：根据两次上报的位置（自己），算一个距离。如果距离超过2公里，就往一个数组里面记录一下true。（数组size为5）
     * 然后遍历数组，只要数组中联系2次 出现 true。就往HMI发送位置偏移。
     * 然后HMI这边就弹出message。并且300秒内不在重复弹出。300秒的逻辑如下：
     * @param teamUploadResponseMsg
     * @param oldPoint
     * @param context
     * @return
     */
    public Coord2DDouble positionDrift(TeamUploadResponseMsg teamUploadResponseMsg,Coord2DDouble oldPoint, final Context context) {
        AccountProfile userInfo = AccountController.getInstance().getAccountInfo();
        Coord2DDouble pushPoint = null;
        if(userInfo!=null&&teamUploadResponseMsg!=null){
            if(teamUploadResponseMsg.groupMembers!=null&&teamUploadResponseMsg.groupMembers.size()>0){
                for (int i=0;i<teamUploadResponseMsg.groupMembers.size();i++){
                    TeamMember teamMember=teamUploadResponseMsg.groupMembers.get(i);
                    if(userInfo.uid.equals(teamMember.uid)){
                        pushPoint = new Coord2DDouble(teamMember.locInfo.lon ,teamMember.locInfo.lat);
                    }
                }
                if(pushPoint!=null){
                    if(oldPoint!=null){
                        if(isShowPositionDrift){
                            pushPosNum++;
                            int index = pushPosNum % 5;
                            double dis = BizLayerUtil.calcDistanceBetweenPoints(oldPoint, pushPoint);
                            if(dis>=2000){//大于等于2000米, 发生偏移的情况
                                positionDrift[index]=true;
                            }else {
                                positionDrift[index]=false;
                            }
                            int count = 0;
                            for (int i = 0; i < 5; i++) {
                                if (positionDrift[i] == true)
                                {
                                    count++;
                                }
                            }
                            if (count >= 2){
                                TaskManager.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(context!=null){
                                            Toast.makeText(context,context.getString(R.string.agroup_position_drift),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                isShowPositionDrift=false;
                                if (mPositionDriftHandler != null&&runnable!=null) {
                                    mPositionDriftHandler.removeCallbacks(runnable);
                                    mPositionDriftHandler.postDelayed(runnable, 300000L);
                                }
                                pushPosNum=0;
                            }
                        }
                    }
                    return pushPoint;
                }
            }
        }
        return null;
    }

    protected Runnable runnable = new Runnable() {
        @Override
        public void run() {
            isShowPositionDrift=true;
        }
    };


    public void clearCount() {
        count=0;
    }


}
