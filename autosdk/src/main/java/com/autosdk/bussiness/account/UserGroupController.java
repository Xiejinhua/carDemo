package com.autosdk.bussiness.account;

import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.user.account.model.AccountProfile;
import com.autonavi.gbl.user.group.GroupService;
import com.autonavi.gbl.user.group.model.GroupBaseInfo;
import com.autonavi.gbl.user.group.model.GroupDestination;
import com.autonavi.gbl.user.group.model.GroupMember;
import com.autonavi.gbl.user.group.model.GroupRequestCreate;
import com.autonavi.gbl.user.group.model.GroupRequestDissolve;
import com.autonavi.gbl.user.group.model.GroupRequestFriendList;
import com.autonavi.gbl.user.group.model.GroupRequestInfo;
import com.autonavi.gbl.user.group.model.GroupRequestInvite;
import com.autonavi.gbl.user.group.model.GroupRequestInviteQRUrl;
import com.autonavi.gbl.user.group.model.GroupRequestJoin;
import com.autonavi.gbl.user.group.model.GroupRequestKick;
import com.autonavi.gbl.user.group.model.GroupRequestQuit;
import com.autonavi.gbl.user.group.model.GroupRequestSetNickName;
import com.autonavi.gbl.user.group.model.GroupRequestStatus;
import com.autonavi.gbl.user.group.model.GroupRequestUpdate;
import com.autonavi.gbl.user.group.model.GroupRequestUrlTranslate;
import com.autonavi.gbl.user.group.model.GroupResponseCreate;
import com.autonavi.gbl.user.group.model.GroupResponseDissolve;
import com.autonavi.gbl.user.group.model.GroupResponseFriendList;
import com.autonavi.gbl.user.group.model.GroupResponseInfo;
import com.autonavi.gbl.user.group.model.GroupResponseInvite;
import com.autonavi.gbl.user.group.model.GroupResponseInviteQRUrl;
import com.autonavi.gbl.user.group.model.GroupResponseJoin;
import com.autonavi.gbl.user.group.model.GroupResponseKick;
import com.autonavi.gbl.user.group.model.GroupResponseQuit;
import com.autonavi.gbl.user.group.model.GroupResponseSetNickName;
import com.autonavi.gbl.user.group.model.GroupResponseStatus;
import com.autonavi.gbl.user.group.model.GroupResponseUpdate;
import com.autonavi.gbl.user.group.model.GroupResponseUrlTranslate;
import com.autonavi.gbl.util.errorcode.common.Service;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.account.observer.GroupServiceObserver;
import com.autosdk.bussiness.common.task.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import timber.log.Timber;

/**
 * 用户模块M层
 * 降低用户操作回调冗余
 * 在需要监听操作回调的地方添加GroupServiceObserver观察者
 */
public class UserGroupController {
    private final List<GroupServiceObserver> groupServiceObservers = new CopyOnWriteArrayList<>();

    private GroupService mGroupService;
    private UserGroupController() { }

    /**< 队伍基本信息的MD5值，与队伍名称，终点，解散时间有关 */
    private String teamStamp;
    /**< 成员基本信息的MD5值，与队伍人数，队员昵称，头像有关 */
    private String memberStamp;
    /**< 队伍基本信息 */
    private GroupBaseInfo baseInfo = null;
    /**< 队伍成员列表 */
    private ArrayList<GroupMember> mGroupMemberList = null;
    private AccountProfile userInfo = null;
    private long taskId = -1; //请求任务id

    private static class GroupControllerHolder {
        private static final UserGroupController instance = new UserGroupController();
    }

    public static UserGroupController getInstance() {
        return GroupControllerHolder.instance;
    }

    public List<GroupServiceObserver> getGroupServiceObservers() {
        return groupServiceObservers;
    }

     /**
     * 初始化组队服务
     */
    public void initService() {
        if (mGroupService == null) {
            mGroupService = (GroupService) ServiceMgr.getServiceMgrInstance().getBLService(SingleServiceID.GroupSingleServiceID);
            // 服务初始化
            int res = mGroupService.init();
            mGroupService.addObserver(groupServiceObserver);
            Timber.d("initGroupService: init=" + res);
        }
    }

    public void unInit() {
        if (mGroupService != null) {
            mGroupService.removeObserver(groupServiceObserver);
            groupServiceObservers.clear();
            mGroupService.unInit();
            mGroupService = null;
        }
    }

    /**
     * 添加HMI业务层回调
     * */
    public void addObserver(GroupServiceObserver groupServiceObserver) {
        if (null != mGroupService) {
            groupServiceObservers.add(groupServiceObserver);
        }
    }

    /**
     * 删除HMI业务层回调
     * */
    public void removeObserver(GroupServiceObserver groupServiceObserver) {
        if (null != mGroupService) {
            groupServiceObservers.remove(groupServiceObserver);
        }
    }

    public String getGroupChatId() {
        if (baseInfo != null) {
            return baseInfo.chatId;
        }
        return "";
    }

    public String getTeamStamp() { return teamStamp; }

    public String getMemberStamp() { return memberStamp; }

    public String getTeamId() {
        if (baseInfo != null) {
            return baseInfo.teamId;
        }
        return "";
    }

    public GroupBaseInfo getTeamInfo() {
        if (null != baseInfo) {
            return baseInfo;
        }
        return new GroupBaseInfo();
    }

    public GroupMember getMemberInfo(String uid) {
        GroupMember reMember = new GroupMember();
        for (GroupMember item: mGroupMemberList) {
            if (Objects.equals(uid, item.uid)) {
                reMember = item;
                break;
            }
        }
        return reMember;
    }

    public int getMemberIndex(String uid) {
        int i = 0;
        for (GroupMember item: mGroupMemberList) {
            if (Objects.equals(uid, item.uid)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 获取所有队伍成员列表-包含队长
     * 返回数据副本，防止外部修改内部数据，内部数据只受服务回调更新
     */
    public ArrayList<GroupMember> getAllMemberList() {
        if (null != mGroupMemberList) {
            return new ArrayList<>(mGroupMemberList);
        }
        return new ArrayList<>();
    }

    /**
     * 获取所有队成列表-不包含队长
     */
    public ArrayList<GroupMember> getMemberList() {
        ArrayList<GroupMember> reMembers = new ArrayList<>();
        if (null != baseInfo && null != mGroupMemberList) {
            for (GroupMember item: mGroupMemberList) {
                if (!Objects.equals(baseInfo.leaderId,item.uid)) {
                    reMembers.add(item);
                }
            }
        }
        return reMembers;
    }

    /**
     * 获取所有队成列表-如果是组员，自己排第一 队长排第二，其他排后面
     * 如果自己是队长不变
     */
    public void convertAllMemberList() {
        userInfo = AccountController.getInstance().getAccountInfo();
        if(userInfo==null){
            return;
        }
        String userId=userInfo.uid;
        ArrayList<GroupMember> reMembers = new ArrayList<>();
        if (null != baseInfo && null != mGroupMemberList) {
            for (GroupMember item: mGroupMemberList) {
                if (item != null && Objects.equals(userId, item.uid)) {
                    reMembers.add(item);
                }
            }
            if(!Objects.equals(userId, baseInfo.leaderId)){
                for (GroupMember item: mGroupMemberList) {
                    if (item != null && Objects.equals(baseInfo.leaderId,item.uid)) {
                        reMembers.add(item);
                    }
                }
            }
            for (GroupMember item: mGroupMemberList) {
                if (item != null && !Objects.equals(userId,item.uid)&&!Objects.equals(baseInfo.leaderId,item.uid)) {
                    reMembers.add(item);
                }
            }
        }
        mGroupMemberList = reMembers;
    }


    private synchronized void syncUpdate(String teamStamp, String memberStamp, GroupBaseInfo baseInfo, ArrayList<GroupMember> memberList) {
        this.teamStamp = teamStamp;
        this.memberStamp = memberStamp;
        this.baseInfo = null==baseInfo ? new GroupBaseInfo() : baseInfo;
        mGroupMemberList = null == memberList ? new ArrayList<>() : memberList;
        convertAllMemberList();
    }

    public synchronized void syncUpdate(ArrayList<GroupMember> memberList) {
        this.mGroupMemberList = null == memberList ? new ArrayList<>() : memberList;
        convertAllMemberList();
    }

    public void updateGroupInfo(GroupResponseCreate data) {
        syncUpdate(data.teamStamp, data.memberStamp, data.team, data.members);
    }

    public void updateGroupInfo(GroupResponseJoin data) {
        syncUpdate(data.teamStamp, data.memberStamp, data.team, data.members);
    }

    public void updateGroupInfo(GroupResponseInfo data) {
        if (data != null) {
            syncUpdate(data.teamStamp, data.memberStamp, data.team, data.members);
        }
    }

    public void updateGroupInfo(GroupResponseKick data) {
        syncUpdate(data.teamStamp, data.memberStamp, data.team, data.members);
    }

    public void updateGroupInfo(GroupResponseUpdate data) {
        if (data != null) {
            syncUpdate(data.teamStamp, data.memberStamp, data.team, data.members);
        }
    }

    public void clearTeamInfo() {
        syncUpdate("", "", null, null);
    }
    /**
     * 获取组队状态
     */
    public int reqStatus() {
        GroupRequestStatus request = new GroupRequestStatus();
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 创建队伍
     *
     * @param dest 队伍目的地
     */
    public int reqCreate(GroupDestination dest) {
        GroupRequestCreate request = new GroupRequestCreate();
        request.destination = dest;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 解散队伍
     *
     * @param teamId 队伍唯一ID
     */
    public int reqDissolve(String teamId) {
        GroupRequestDissolve request = new GroupRequestDissolve();
        request.teamId = teamId;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 加入队伍
     *
     * @param teamNum 队伍口令
     */
    public int reqJoin(String teamNum) {
        GroupRequestJoin request = new GroupRequestJoin();
        request.teamNumber = teamNum;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 退出队伍
     *
     * @param teamId 队伍唯一ID
     */
    public int reqQuit(String teamId) {
        GroupRequestQuit request = new GroupRequestQuit();
        request.teamId = teamId;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 邀请好友
     *
     * @param teamId 队伍唯一ID
     * @param invites 邀请成员UID列表
     */
    public int reqInvite(String teamId, ArrayList<String> invites) {
        GroupRequestInvite request = new GroupRequestInvite();
        request.teamId = teamId;
        request.inviteIds = invites;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 队长踢人
     *
     * @param teamId 队伍唯一ID
     * @param kicks 踢除成员UID列表
     */
    public int reqKick(String teamId, ArrayList<String> kicks) {
        GroupRequestKick request = new GroupRequestKick();
        request.teamId = teamId;
        request.kickIds = kicks;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 获取队伍信息
     *
     * @param teamId 队伍唯一ID
     */
    public int reqGroupInfo(String teamId) {
        GroupRequestInfo request = new GroupRequestInfo();
        request.teamId = teamId;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 修改队伍信息
     *
     * @param teamId 队伍唯一ID
     * @param dest 目的地
     * @param teamName 队伍名
     * @param announce 队伍公告
     */
    public int reqUpdate(String teamId, GroupDestination dest, String teamName, String announce) {
        GroupRequestUpdate request = new GroupRequestUpdate();
        request.teamId = teamId;
        request.destination = dest;
        if (baseInfo != null) {
            request.teamName = baseInfo.teamName;
            request.announcement = baseInfo.announcement.content;
        }
        if (!"".equals(teamName)) {
            request.teamName = teamName;
        }
        if (!"".equals(announce)) {
            request.announcement = announce;
        }
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 修改队伍内昵称
     *
     * @param nickName 新队伍昵称
     */
    public int reqSetNickName(String nickName) {
        GroupRequestSetNickName request = new GroupRequestSetNickName();
        request.teamNick = nickName;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 获取历史好友
     */
    public int reqFriendList() {
        GroupRequestFriendList request = new GroupRequestFriendList();
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 请求口令二维码链接
     *
     * @param teamId 队伍唯一ID
     */
    public int reqInviteQrUrl(String teamId) {
        GroupRequestInviteQRUrl request = new GroupRequestInviteQRUrl();
        request.teamId = teamId;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 请求口令二维码链接
     *
     * @param teamNumUrl 队伍口令二维码链接
     */
    public int reqUrlTranslate(String teamNumUrl) {
        GroupRequestUrlTranslate request = new GroupRequestUrlTranslate();
        request.url = teamNumUrl;
        checkThrowNull();
        return mGroupService.executeRequest(request);
    }

    /**
     * 取消请求
     */
    public void abortRequest() {
        if (taskId != -1){
            mGroupService.abortRequest(taskId);
            taskId = -1;
        }
    }

    //taskId 设置
    public void setTaskId(long id){
        taskId = id;
    }

    private void checkThrowNull() {
        if (null == mGroupService) {
            throw new NullPointerException("the groupService be null!!!");
        }
    }

    GroupServiceObserver groupServiceObserver = new GroupServiceObserver(){
        /**
         * @brief     获取队伍状态结果回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseStatus           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseStatus groupResponseStatus) {
            TaskManager.post(() -> {
                // 每秒触发并更新组队信息
                if (!groupResponseStatus.teamId.isEmpty()) {
                    reqGroupInfo(groupResponseStatus.teamId);
                }
                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseStatus);
                }
            });
        }

        /**
         * @brief     请求创建队伍回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseCreate           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseCreate groupResponseCreate) {
            TaskManager.post(() -> {
                if (errCode != Service.ErrorCodeOK || groupResponseCreate == null) {
                    Timber.d("GroupResponseCreate error:%s", errCode);
                    return;
                }

                updateGroupInfo(groupResponseCreate);

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseCreate);
                }
            });

        }

        /**
         * @brief     请求解散队伍回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseDissolve           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseDissolve groupResponseDissolve) {
            TaskManager.post(() -> {
                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseDissolve);
                }
            });
        }

        /**
         * @brief     请求加入队伍回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseJoin           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseJoin groupResponseJoin) {
            TaskManager.post(() -> {
                if (groupResponseJoin == null) {
                    Timber.d("GroupResponseStatus error:%s", errCode);
                    return;
                }
                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseJoin);
                }
            });
        }

        /**
         * @brief     请求退出队伍回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseQuit           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseQuit groupResponseQuit) {
            TaskManager.post(() -> {
                if (groupResponseQuit == null) {
                    Timber.d("GroupResponseQuit error:%s", errCode);
                }

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseQuit);
                }
            });
        }

        /**
         * @brief     邀请好友加入队伍回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseInvite           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseInvite groupResponseInvite) {
            TaskManager.post(() -> {
                if (groupResponseInvite == null) {
                    Timber.d("GroupResponseInvite error:%s", errCode);
                    return;
                }

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseInvite);
                }
            });
        }

        /**
         * @brief     队长踢人请求回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseKick           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseKick groupResponseKick) {
            TaskManager.post(() -> {
                if (groupResponseKick == null) {
                    Timber.d("GroupResponseKick error:%s", errCode);
                    return;
                }
                updateGroupInfo(groupResponseKick);

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseKick);
                }
            });
        }

        /**
         * @brief     获取队伍信息结果回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseInfo           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseInfo groupResponseInfo) {
            TaskManager.post(() -> {
                if (groupResponseInfo == null) {
                    Timber.d("GroupResponseInfo error:%s", errCode);
                    return;
                }

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseInfo);
                }
            });
        }

        /**
         * @brief     请求修改队伍属性回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseUpdate           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseUpdate groupResponseUpdate) {
            TaskManager.post(() -> {
                if (groupResponseUpdate == null) {
                    Timber.d("GroupResponseUpdate error:%s", errCode);
                }else {
                    updateGroupInfo(groupResponseUpdate);
                }
                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseUpdate);
                }
            });
        }

        /**
         * @brief     请求修改队伍中的昵称回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseSetNickName           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseSetNickName groupResponseSetNickName) {
            TaskManager.post(() -> {
                if (groupResponseSetNickName == null) {
                    Timber.d("GroupResponseSetNickName error:%s", errCode);
                    return;
                }

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseSetNickName);
                }
            });
        }

        /**
         * @brief     请求历史好友信息列表回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseFriendList           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseFriendList groupResponseFriendList) {
            TaskManager.post(() -> {
                if (groupResponseFriendList == null) {
                    Timber.d("GroupResponseFriendList error:%s", errCode);
                    return;
                }

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseFriendList);
                }
            });
        }

        /**
         * @brief     转换二维码链接为图片回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseInviteQRUrl           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseInviteQRUrl groupResponseInviteQRUrl) {
            TaskManager.post(() -> {
                if (groupResponseInviteQRUrl == null) {
                    Timber.d("GroupResponseInviteQRUrl error:%s", errCode);
                    return;
                }

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseInviteQRUrl);
                }
            });
        }

        /**
         * @brief     获取队伍状态结果回调通知
         * @param     errCode          错误码
         * @param     taskId           task id
         * @param     groupResponseUrlTranslate           请求响应数据
         */
        @Override
        public void onNotify(int errCode, long taskId, GroupResponseUrlTranslate groupResponseUrlTranslate) {
            TaskManager.post(() -> {
                if (groupResponseUrlTranslate == null) {
                    Timber.d("GroupResponseUrlTranslate error:%s", errCode);
                    return;
                }

                for (GroupServiceObserver observer : groupServiceObservers) {
                    observer.onNotify(errCode, taskId, groupResponseUrlTranslate);
                }
            });
        }
    };
}
