package com.desaysv.psmap.base.business

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.user.group.model.GroupResponseCreate
import com.autonavi.gbl.user.group.model.GroupResponseDissolve
import com.autonavi.gbl.user.group.model.GroupResponseFriendList
import com.autonavi.gbl.user.group.model.GroupResponseInfo
import com.autonavi.gbl.user.group.model.GroupResponseInvite
import com.autonavi.gbl.user.group.model.GroupResponseInviteQRUrl
import com.autonavi.gbl.user.group.model.GroupResponseJoin
import com.autonavi.gbl.user.group.model.GroupResponseKick
import com.autonavi.gbl.user.group.model.GroupResponseQuit
import com.autonavi.gbl.user.group.model.GroupResponseSetNickName
import com.autonavi.gbl.user.group.model.GroupResponseStatus
import com.autonavi.gbl.user.group.model.GroupResponseUpdate
import com.autonavi.gbl.user.group.model.GroupResponseUrlTranslate
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.account.UserGroupController
import com.autosdk.bussiness.account.observer.GroupServiceObserver
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.utils.UploadPositionHandler
import com.desaysv.psmap.base.bean.GroupObserverResultBean
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.utils.BaseConstant
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 组队接口回调业务处理类
 */
@Singleton
class GroupObserverBusiness @Inject constructor(
    private val userGroupController: UserGroupController,
    private val uploadPositionHandler: UploadPositionHandler,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val application: Application,
    private val gson: Gson
) {
    private var groupServiceObservers: List<BusinessGroupServiceObserver> = CopyOnWriteArrayList()
    private val userGroupScope = CoroutineScope(Dispatchers.Default + Job())
    val setToast = MutableLiveData<String>() //toast信息
    val updateGroupResponseResult = MutableLiveData<Boolean>() //组队相关请求是否成功
    val updateGroupDestinationSuccess = MutableLiveData(-1) //组队设置目的地成功
    private var mTeamNumber = "" //队伍口令
    private var onGroupServiceObserver = ArrayList<Pair<Int, OnGroupServiceObserver>>()
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
    private var clickType = "" //"0" 创建队伍,非""和"0"是加入队伍
    val isPushMessageLiveData = MutableLiveData<Boolean>() //组队消息弹条

    fun setClickType(clickType: String) {
        this.clickType = clickType
    }

    //taskId 设置
    fun setTaskId(id: Long) {
        userGroupController.setTaskId(id)
    }

    fun addObserver(businessType: Int) {
        Timber.d("businessType:$businessType")
        userGroupController.addObserver(BusinessGroupServiceObserver().apply {
            this.businessType = businessType
        })
        groupServiceObservers = userGroupController.groupServiceObservers as List<BusinessGroupServiceObserver>
    }

    fun removeObserver(businessType: Int) {
        val observer = getBusinessGroupServiceObserver(businessType)
        if (null != observer) {
            userGroupController.removeObserver(observer)
            groupServiceObservers = userGroupController.groupServiceObservers as List<BusinessGroupServiceObserver>
        }
    }

    //组队弹条开启Observer并获取组队状态
    fun addObserverAndReqStatus(teamNumber: String, businessType: Int) {
        mTeamNumber = teamNumber
        removeObserver(businessType)
        addObserver(businessType)
    }

    private fun getBusinessGroupServiceObserver(businessType: Int): BusinessGroupServiceObserver? {
        for (observer in groupServiceObservers) {
            if (businessType == observer.businessType) {
                return observer
            }
        }
        return null
    }

    private fun getOnGroupServiceObserver(businessType: Int): Pair<Int, OnGroupServiceObserver>? {
        for (observer in onGroupServiceObserver) {
            if (businessType == observer.first) {
                return observer
            }
        }
        return null
    }

    private fun isPushMessage(businessType: Int): Boolean {
        return businessType == BaseConstant.TYPE_GROUP_PUSH_MESSAGE
    }

    private fun isGroupCreate(businessType: Int): Boolean {
        return businessType == BaseConstant.TYPE_GROUP_CREATE
    }

    private fun isGroupMainMap(businessType: Int): Boolean {
        return businessType == BaseConstant.TYPE_GROUP_MAIN
    }

    inner class BusinessGroupServiceObserver : GroupServiceObserver() {
        override fun getBusinessType(): Int {
            return super.getBusinessType()
        }

        override fun setBusinessType(businessType: Int) {
            super.setBusinessType(businessType)
        }

        override fun onNotify(i: Int, l: Long, groupResponseStatus: GroupResponseStatus) {
            userGroupScope.launch {
                Timber.d("GroupResponseStatus:$i,$l")
                when (businessType) {
                    BaseConstant.TYPE_GROUP_PUSH_MESSAGE, BaseConstant.TYPE_GROUP_CREATE, BaseConstant.TYPE_GROUP_MAIN -> {
                        if (isGroupMainMap(businessType)) {
                            if (i != Service.ErrorCodeOK) {
                                Timber.e("getTeamUserStatus error:null")
                                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                                    GroupObserverResultBean(
                                        businessType,
                                        BaseConstant.TYPE_GROUP_STATUS,
                                        i,
                                        l,
                                        groupResponseStatus
                                    )
                                )
                                return@launch
                            }
                        } else {
                            if (groupResponseStatus.code != 1) {
                                if (isGroupCreate(businessType)) {
                                    if (TextUtils.isEmpty(clickType)) { //"0" 创建队伍,非""和"0"是加入队伍
                                        Timber.d(" groupResponseStatus clickType isEmpty")
                                    } else if (TextUtils.equals(clickType, "0")) {
                                        setToast.postValue("创建队伍失败")
                                    } else {
                                        setToast.postValue("加入队伍失败")
                                    }
                                } else {
                                    setToast.postValue(if (!TextUtils.isEmpty(groupResponseStatus.message)) groupResponseStatus.message else "获取队伍状态失败了")
                                }
                                if (isGroupCreate(businessType)) {
                                    updateGroupResponseResult.postValue(false)
                                    setTaskId(-1)
                                }
                                return@launch
                            }
                        }
                        if (isPushMessage(businessType)) {
                            // 在协程中执行异步任务
                            withContext(Dispatchers.IO) {
                                if (TextUtils.isEmpty(groupResponseStatus.teamId)) {
                                    if (TextUtils.isEmpty(mTeamNumber)) {
                                        return@withContext
                                    }
                                    userGroupController.reqJoin(mTeamNumber)
                                } else {
                                    setToast.postValue(application.getString(com.autosdk.R.string.agroup_had_join_dialog_title))
                                }
                            }
                        }
                    }

                    BaseConstant.TYPE_GROUP_SETTING -> {
                        Timber.d("groupResponseStatus TYPE_GROUP_SETTING ${gson.toJson(groupResponseStatus)}")
                        if (TextUtils.isEmpty(groupResponseStatus.teamId)) {
                            mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                            BaseConstant.TEAM_ID = ""
                        } else {
                            mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, groupResponseStatus.teamId)
                            BaseConstant.TEAM_ID = groupResponseStatus.teamId
                        }
                    }
                }
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_STATUS,
                        i,
                        l,
                        groupResponseStatus
                    )
                )
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseCreate: GroupResponseCreate) {
            userGroupScope.launch {
                Timber.d("GroupResponseCreate:$i,$l")
                if (i != Service.ErrorCodeOK) {
                    Timber.e("GroupResponseCreate error: null")
                    return@launch
                }

                when (businessType) {
                    BaseConstant.TYPE_GROUP_CREATE -> {
                        when (groupResponseCreate.code) {
                            1 -> { //成功
                                if (!TextUtils.isEmpty(groupResponseCreate.team.teamId)) {
                                    setToast.postValue("创建队伍成功")
                                }
                            }

                            2001 -> setToast.postValue("您还没登录呢，请先扫码登录") //用户未登录
                            else -> setToast.postValue("创建队伍失败")
                        }
                        setTaskId(-1)
                        updateGroupResponseResult.postValue(true)
                    }
                }
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_CREATE_FUN,
                        i,
                        l,
                        groupResponseCreate
                    )
                )
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseDissolve: GroupResponseDissolve) {
            userGroupScope.launch {
                when (businessType) {
                    BaseConstant.TYPE_GROUP_MY -> {
                        setTaskId(-1)
                        updateGroupResponseResult.postValue(true)
                        if (groupResponseDissolve.code != 1) {
                            setToast.postValue(if (!TextUtils.isEmpty(groupResponseDissolve.message)) groupResponseDissolve.message else "请求解散队伍失败了")
                            return@launch
                        }

                        setToast.postValue("解散队伍成功")
                        mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                        BaseConstant.TEAM_ID = ""
                    }
                }
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_DISSOLVE,
                        i,
                        l,
                        groupResponseDissolve
                    )
                )
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseJoin: GroupResponseJoin) {
            userGroupScope.launch {
                when (businessType) {
                    BaseConstant.TYPE_GROUP_PUSH_MESSAGE, BaseConstant.TYPE_GROUP_MY_MESSAGE, BaseConstant.TYPE_GROUP_CREATE -> {
                        // 在协程中执行异步任务
                        withContext(Dispatchers.IO) {
                            if (isGroupCreate(businessType)) {
                                Timber.d("GroupResponseJoin:$i,$l")
                                if (i != Service.ErrorCodeOK) {
                                    Timber.e("GroupResponseJoin error: null")
                                    setTaskId(-1)
                                    updateGroupResponseResult.postValue(false)
                                    setToast.postValue("队伍不存在或已失效")
                                    return@withContext
                                }
                            }
                            if (isGroupCreate(businessType)) {
                                setTaskId(-1)
                                updateGroupResponseResult.postValue(true)
                            }
                            if (groupResponseJoin.code == 1) { //成功
                                Timber.d(" paraTeamJoinAndShow HANDLER_AOS_TEAMJOIN teamId: ${groupResponseJoin.team.teamId}")
                                if (isPushMessage(businessType) || isGroupCreate(businessType)) {
                                    userGroupController.updateGroupInfo(groupResponseJoin)
                                }
                                if (!TextUtils.isEmpty(groupResponseJoin.team.teamId)) {
                                    withContext(Dispatchers.Main) {
                                        setToast.postValue("加入队伍成功")
                                    }
                                    mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, groupResponseJoin.team.teamId)
                                    BaseConstant.TEAM_ID = groupResponseJoin.team.teamId
                                    if (isPushMessage(businessType)) {
                                        uploadPositionHandler.publishTeamInfo()
                                        isPushMessageLiveData.postValue(true)
                                    }
                                } else {
                                    setToast.postValue("队伍不存在或已失效")
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    when (groupResponseJoin.code) {
                                        2001 -> { //用户未登录
                                            setToast.postValue("您还没登录呢，请先扫码登录")
                                        }

                                        2006, 2007 -> { //您来晚了，队伍已经解散 2.队伍不存在
                                            setToast.postValue("无法加入，队伍过期或解散")
                                            mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                                            BaseConstant.TEAM_ID = ""
                                        }

                                        2008 -> { //队伍人数已经达到上限
                                            setToast.postValue("无法加入，队伍已满员")
                                            mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                                            BaseConstant.TEAM_ID = ""
                                        }

                                        else -> {
                                            setToast.postValue("队伍不存在或已失效")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_JOIN,
                        i,
                        l,
                        groupResponseJoin
                    )
                )
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseQuit: GroupResponseQuit) {
            userGroupScope.launch {
                when (businessType) {
                    BaseConstant.TYPE_GROUP_MY -> {
                        setTaskId(-1)
                        updateGroupResponseResult.postValue(true)
                        if (groupResponseQuit != null) {
                            if (groupResponseQuit.code != 1) {
                                setToast.postValue(if (!TextUtils.isEmpty(groupResponseQuit.message)) groupResponseQuit.message else "请求退出队伍失败了")
                                return@launch
                            }
                            setToast.postValue("退出队伍成功")
                            mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                            BaseConstant.TEAM_ID = ""
                        }
                    }
                }
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_QUIT,
                        i,
                        l,
                        groupResponseQuit
                    )
                )
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseInvite: GroupResponseInvite) {
            userGroupScope.launch {
                when (businessType) {
                    BaseConstant.TYPE_GROUP_INVITE_JOIN -> {
                        Timber.d("GroupResponseInvite:$i,$l")
                        getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                            GroupObserverResultBean(
                                businessType,
                                BaseConstant.TYPE_GROUP_INVITE,
                                i,
                                l,
                                groupResponseInvite
                            )
                        )
                        if (groupResponseInvite.code != 1) {
                            setToast.postValue(if (!TextUtils.isEmpty(groupResponseInvite.message)) groupResponseInvite.message else "邀请好友加入队伍失败了")
                            return@launch
                        }
                    }
                }
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseKick: GroupResponseKick) {
            userGroupScope.launch {
                Timber.d("GroupResponseKick i:$i, l:$l")
                when (businessType) {
                    BaseConstant.TYPE_GROUP_MY -> {
                        if (groupResponseKick.code != 1) {
                            setToast.postValue(if (!TextUtils.isEmpty(groupResponseKick.message)) groupResponseKick.message else "队长踢人请求失败了")
                            return@launch
                        }
                    }
                }
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_KICK,
                        i,
                        l,
                        groupResponseKick
                    )
                )
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseInfo: GroupResponseInfo) {
            userGroupScope.launch {
                when (businessType) {
                    BaseConstant.TYPE_GROUP_MAIN, BaseConstant.TYPE_GROUP_INVITE_JOIN -> {
                        Timber.d("GroupResponseInfo:$i,$l")
                        if (i != Service.ErrorCodeOK) {
                            Timber.e("GroupResponseInfo i != Service.ErrorCodeOK")
                            return@launch
                        }
                        if (groupResponseInfo.code != 1) {
                            return@launch
                        }
                    }

                    BaseConstant.TYPE_GROUP_MY -> {
                        Timber.d(" groupResponseInfo: ${gson.toJson(groupResponseInfo)}")
                        if (groupResponseInfo.code != 1) {
                            val message = when (groupResponseInfo.code) {
                                2006, 2007, 2013 -> "队伍已被解散"
                                else -> "获取队伍信息失败，请重试"
                            }
                            setToast.postValue(if (TextUtils.isEmpty(groupResponseInfo.message)) message else groupResponseInfo.message)
                            mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                            BaseConstant.TEAM_ID = ""
                            getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                                GroupObserverResultBean(
                                    businessType,
                                    BaseConstant.TYPE_GROUP_INFO,
                                    i,
                                    l,
                                    groupResponseInfo
                                )
                            )
                            return@launch
                        }

                        if (TextUtils.isEmpty(groupResponseInfo.team.teamId)) {
                            mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                            BaseConstant.TEAM_ID = ""
                        }
                    }
                }
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_INFO,
                        i,
                        l,
                        groupResponseInfo
                    )
                )
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseUpdate: GroupResponseUpdate) {
            userGroupScope.launch {
                Timber.i("组队设置目的地成功 businessType:$businessType")
                when (businessType) {
                    BaseConstant.TYPE_GROUP_MY -> {
                        setTaskId(-1)
                        updateGroupResponseResult.postValue(true)
                        if (groupResponseUpdate.code != 1) {
                            setToast.postValue(if (!TextUtils.isEmpty(groupResponseUpdate.message)) groupResponseUpdate.message else "队伍目的地设置失败了")
                        } else {
                            setToast.postValue("队长设置了目的地")
                            if (updateGroupDestinationSuccess.value!! > 100) {
                                Timber.i("组队设置目的地成功 1")
                                updateGroupDestinationSuccess.postValue(0)
                            } else {
                                Timber.i("组队设置目的地成功 2")
                                updateGroupDestinationSuccess.postValue(updateGroupDestinationSuccess.value!! + 1)
                            }
                        }
                    }
                }
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseSetNickName: GroupResponseSetNickName) {
            userGroupScope.launch {
                when (businessType) {
                    BaseConstant.TYPE_GROUP_MY -> {
                        if (groupResponseSetNickName.code != 1) {
                            setToast.postValue(if (TextUtils.isEmpty(groupResponseSetNickName.message)) "更改昵称失败，请稍后重试" else groupResponseSetNickName.message)
                            return@launch
                        }
                        setToast.postValue("修改队伍信息完成")
                    }
                }
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_NICK_NAME,
                        i,
                        l,
                        groupResponseSetNickName
                    )
                )
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseFriendList: GroupResponseFriendList) {
            userGroupScope.launch {
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_FRIEND_LIST,
                        i,
                        l,
                        groupResponseFriendList
                    )
                )
                when (businessType) {
                    BaseConstant.TYPE_GROUP_INVITE_JOIN -> {
                        Timber.d("GroupResponseFriendList:$i,$l")
                        if (i != Service.ErrorCodeOK) {
                            Timber.e("GroupResponseFriendList i != Service.ErrorCodeOK")
                            return@launch
                        }
                        if (groupResponseFriendList.code != 1) {
                            setToast.postValue(if (!TextUtils.isEmpty(groupResponseFriendList.message)) groupResponseFriendList.message else "请求历史好友信息列表失败了")
                            return@launch
                        }
                    }
                }
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseInviteQRUrl: GroupResponseInviteQRUrl) {
            userGroupScope.launch {
                getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                    GroupObserverResultBean(
                        businessType,
                        BaseConstant.TYPE_GROUP_INVITE_QRURL,
                        i,
                        l,
                        groupResponseInviteQRUrl
                    )
                )
                when (businessType) {
                    BaseConstant.TYPE_GROUP_INVITE_JOIN -> {
                        groupResponseInviteQRUrl.run {
                            if (code != 1) {
                                setToast.postValue("请求口令二维码链接失败了")
                                return@launch
                            }
                        }
                    }
                }
            }
        }

        override fun onNotify(i: Int, l: Long, groupResponseUrlTranslate: GroupResponseUrlTranslate) {
            userGroupScope.launch {
                when (businessType) {
                    BaseConstant.TYPE_GROUP_INVITE_JOIN -> {
                        getOnGroupServiceObserver(businessType)?.second?.onObserverResult(
                            GroupObserverResultBean(
                                businessType,
                                BaseConstant.TYPE_GROUP_URL_TRANSLATE,
                                i,
                                l,
                                groupResponseUrlTranslate
                            )
                        )
                        if (groupResponseUrlTranslate.code != 1) {
                            setToast.postValue(if (!TextUtils.isEmpty(groupResponseUrlTranslate.message)) groupResponseUrlTranslate.message else "请求口令二维码链接失败了")
                            return@launch
                        }
                    }
                }
            }
        }
    }

    interface OnGroupServiceObserver {
        fun onObserverResult(resultBean: GroupObserverResultBean)
    }

    fun setOnGroupServiceObserver(businessType: Int, observer: OnGroupServiceObserver) {
        onGroupServiceObserver.add(Pair(businessType, observer))
    }

    fun removeOnGroupServiceObserver(businessType: Int) {
        val observer = getOnGroupServiceObserver(businessType)
        if (null != observer) {
            onGroupServiceObserver.remove(observer)
        }
    }
}