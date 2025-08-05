package com.desaysv.psmap.ui.settings.message

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.user.group.model.GroupResponseJoin
import com.autonavi.gbl.user.msgpush.model.AimPoiPushMsg
import com.autonavi.gbl.user.msgpush.model.AimPushMsg
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autonavi.gbl.user.msgpush.model.MsgPushType
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.bean.GroupObserverResultBean
import com.desaysv.psmap.base.business.GroupObserverBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 我的消息ViewModel
 */
@HiltViewModel
class MessageViewModel @Inject constructor(
    private val pushMessageBusiness: PushMessageBusiness,
    private val userGroupBusiness: UserGroupBusiness,
    private val groupObserverBusiness: GroupObserverBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    @ProvidesHiltModule.DefaultMapCommand private val mapCommand: IMapCommand
) : ViewModel() {
    val hasAimData = MutableLiveData(false) //我的消息是否有数据
    val hasTeamData = MutableLiveData(false) //广播消息是否有数据
    val selectTab = MutableLiveData(true) //顶部tab选择 true.我的消息 false.广播消息
    val messageTip = MutableLiveData("") //消息为空时提示
    val isLoading = MutableLiveData(true)
    val myMessageRed = MutableLiveData(false) //我的消息tab未读红点
    val broadcastMessageRed = MutableLiveData(false) //广播消息tab未读红点
    val comeInTeam = MutableLiveData<Boolean>() //进入组队出行主界面
    val loginLoading = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val themeChange = skyBoxBusiness.themeChange()

    var send2carPushMessages: ArrayList<AimPushMsg> = ArrayList() //send2car消息列表
    var teamPushMsgMessages: ArrayList<TeamPushMsg>? = ArrayList() //组队消息集合
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)

    //获取本地保存的组队ID
    fun getTeamId(): String {
        return if (TextUtils.isEmpty(BaseConstant.TEAM_ID)) mapSharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "") else BaseConstant.TEAM_ID
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        return settingAccountBusiness.isLogin()
    }

    //空消息提示
    fun setEmptyMessageTip(tip: String) {
        messageTip.postValue(tip)
    }

    override fun onCleared() {
        super.onCleared()
        removeObserver()//移除组队监听
    }

    //获取消息列表
    fun initMessageData() {
        hasAimData.postValue(false)
        hasTeamData.postValue(false)
        isLoading.postValue(true)
        getMessageData()
    }

    //获取本地保存的消息列表
    @SuppressLint("CheckResult")
    fun getMessageData() {
        viewModelScope.launch {
            send2carPushMessages.clear()
            teamPushMsgMessages?.clear()
            myMessageRed.postValue(false)
            broadcastMessageRed.postValue(false)
            val send2carMsgs: ArrayList<AimPushMsg>? = if(isLogin()) pushMessageBusiness.getSend2carPushMsg() else arrayListOf() //获取send2car消息列表
            val teamPushMsgs: ArrayList<TeamPushMsg>? = null //获取组队消息集合
            val uid = settingAccountBusiness.getAccountProfile()?.uid ?: ""
            if (send2carMsgs != null && send2carMsgs.size > 0) {
                for (i in 0 until send2carMsgs.size) {
                    if (pushMessageBusiness.isPoiMsg(send2carMsgs[i]) && send2carMsgs[i].aimPoiMsg.userId == uid) {
                        send2carPushMessages.add(send2carMsgs[i])
                    } else if (pushMessageBusiness.isRouteMsg(send2carMsgs[i]) && send2carMsgs[i].aimRouteMsg.userId == uid) {
                        send2carPushMessages.add(send2carMsgs[i])
                    }
                }
                for (i in 0 until send2carMsgs.size) {
                    if (pushMessageBusiness.isPoiMsg(send2carMsgs[i])) {
                        if (send2carMsgs[i].aimPoiMsg.userId == uid && !send2carMsgs[i].aimPoiMsg.isReaded) {
                            myMessageRed.postValue(true)
                            break
                        }
                    } else if (pushMessageBusiness.isRouteMsg(send2carMsgs[i])) {
                        if (send2carMsgs[i].aimRouteMsg.userId == uid && !send2carMsgs[i].aimRouteMsg.isReaded) {
                            myMessageRed.postValue(true)
                            break
                        }
                    }
                }
            }
            if (teamPushMsgs != null && teamPushMsgs.size > 0) {
                Timber.i("teamPushMsgs?.size: ${teamPushMsgs.size}")
                teamPushMsgMessages?.addAll(teamPushMsgs)
            }
            if (teamPushMsgMessages != null && teamPushMsgMessages?.size!! > 0) {
                Timber.i("teamPushMsgMessages?.size: ${teamPushMsgMessages?.size!!}")
                for (i in 0 until teamPushMsgMessages?.size!!) {
                    if (!teamPushMsgMessages!![i].isReaded) {
                        broadcastMessageRed.postValue(true)
                        break
                    }
                }
            }
            hasAimData.postValue(send2carPushMessages.isNotEmpty())
            hasTeamData.postValue(teamPushMsgMessages?.isNotEmpty() == true)
            isLoading.postValue(false)
        }
    }

    fun selectAimPoi(aimPoiMsg: AimPoiPushMsg) {
        if (!aimPoiMsg.isReaded) {
            pushMessageBusiness.markMessageAsRead(MsgPushType.MsgPushTypeAimPoi, aimPoiMsg.messageId)
            getMessageData()
        }
    }

    fun selectAimRoute(aimRouteMsg: AimRoutePushMsg) {
        if (!aimRouteMsg.isReaded) {
            pushMessageBusiness.markMessageAsRead(MsgPushType.MsgPushTypeAimRoute, aimRouteMsg.messageId)
            getMessageData()
        }
    }

    fun selectTeam(teamPushMsg: TeamPushMsg) {
        if (!teamPushMsg.isReaded) {
            pushMessageBusiness.markMessageAsRead(MsgPushType.MsgPushTypeTeam, teamPushMsg.messageId)
            getMessageData()
        }
    }

    //我的消息删除记录
    fun toDeleteAimPushMsg(aimPushMsg: AimPushMsg) {
        if (pushMessageBusiness.isPoiMsg(aimPushMsg)) {
            pushMessageBusiness.deleteSend2carMsg(MsgPushType.MsgPushTypeAimPoi, aimPushMsg.aimPoiMsg.messageId)
        } else if (pushMessageBusiness.isRouteMsg(aimPushMsg)) {
            pushMessageBusiness.deleteSend2carMsg(MsgPushType.MsgPushTypeAimRoute, aimPushMsg.aimRouteMsg.messageId)
        }
        send2carPushMessages.remove(aimPushMsg)
        hasAimData.postValue(send2carPushMessages.isNotEmpty())
    }

    //广播消息删除记录
    fun toDeleteTeamPushMsg(teamPushMsg: TeamPushMsg?) {
        if (teamPushMsg != null) {
            pushMessageBusiness.deleteTeamPushMsgMessages(teamPushMsg.messageId)
        }
        teamPushMsgMessages?.remove(teamPushMsg)
        hasTeamData.postValue(teamPushMsgMessages?.isNotEmpty() == true)
    }

    /**
     * 组队-加入队伍
     */
    fun sendReqWsServiceTeamJoin(teamNumber: String?) {
        removeObserver() //移除组队监听
        setOnGroupServiceObserver()
        userGroupBusiness.reqJoin(teamNumber)
    }

    //移除组队监听
    private fun removeObserver() {
        userGroupBusiness.removeObserver(BaseConstant.TYPE_GROUP_MY_MESSAGE)
        groupObserverBusiness.removeOnGroupServiceObserver(BaseConstant.TYPE_GROUP_MY_MESSAGE)
    }

    private fun setOnGroupServiceObserver() {
        groupObserverBusiness.setOnGroupServiceObserver(BaseConstant.TYPE_GROUP_MY_MESSAGE, object : GroupObserverBusiness.OnGroupServiceObserver {
            override fun onObserverResult(resultBean: GroupObserverResultBean) {
                viewModelScope.launch {
                    if (resultBean.businessType == BaseConstant.TYPE_GROUP_MY_MESSAGE && resultBean.observerType == BaseConstant.TYPE_GROUP_JOIN) { //加入队伍回调
                        val response = resultBean.data as GroupResponseJoin
                        if (response.code == 1 && !TextUtils.isEmpty(response.team.teamId)) {
                            comeInTeam.postValue(true)
                        }
                    }
                }
            }
        })
    }

    fun showPoiCard(msg: AimPoiPushMsg) {
        Timber.i("showPoiCard")
        mapCommand.showPoiDetail(
            POIFactory.createPOI(
                msg.content.name,
                GeoPoint(msg.content.lon.toDouble() / 1000000, msg.content.lat.toDouble() / 1000000),
                msg.content.poiId
            ).apply {
                addr = msg.content.address
            })
    }

    fun startRoute(msg: AimRoutePushMsg) {
        Timber.i("startRoute")
        mapCommand.startPlanRoute(
            msg.content.routeParam.destination.name,
            msg.content.path.endPoints.points[0].lon.toDouble(),
            msg.content.path.endPoints.points[0].lat.toDouble()
        )
    }
}