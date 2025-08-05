package com.desaysv.psmap.base.business

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.aosclient.observer.ICallBackWsTserviceInternalLinkAutoReport
import com.autonavi.gbl.user.group.model.GroupResponseJoin
import com.autonavi.gbl.user.group.model.GroupResponseStatus
import com.autonavi.gbl.user.msgpush.model.AimPoiPushMsg
import com.autonavi.gbl.user.msgpush.model.AimPushMsg
import com.autonavi.gbl.user.msgpush.model.AimRoutePushMsg
import com.autonavi.gbl.user.msgpush.model.MsgPushType
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.autosdk.bussiness.account.LinkCarController
import com.autosdk.bussiness.account.UserGroupController
import com.autosdk.bussiness.account.bean.LinkCarLocation
import com.autosdk.bussiness.push.PushController
import com.autosdk.bussiness.push.listener.AimPushMessageListener
import com.autosdk.bussiness.push.listener.LinkPushMessageListener
import com.autosdk.bussiness.push.listener.TeamMessageListener
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.base.bean.GroupObserverResultBean
import com.desaysv.psmap.base.utils.BaseConstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 高德消息推送
 * 比如手机POI或者路线推送，组队消息推送
 */
@Singleton
class PushMessageBusiness @Inject constructor(
    private val pushController: PushController,
    private val userGroupController: UserGroupController,
    private val linkCarController: LinkCarController,
    private val groupObserverBusiness: GroupObserverBusiness
) {
    val closeView = MutableLiveData<Boolean>() //关闭组队消息推送卡片
    private var coroutineScope: CoroutineScope? = null
    private var mTeamNumber = "" //队伍口令

    /**
     * push服务初始化
     */
    fun initPush() {
        pushController.initPush(AutoConstant.PUSH_DIR, "xomUQFiUgt5F8MZci07vil3Ajt85oaWedLxH8fL0")
    }

    /**
     * 初始化是否成功
     */
    fun isInitSuccess(): Boolean {
        return pushController.isInitSuccess
    }

    /**
     * 开始消息监听所有push消息，当前登录用户ID，传空只能接收 运营消息
     * 当未登录账号时，可先传""，账号登录后，则再次调用该接口
     * @param userId
     */
    fun startListen(userId: String?) {
        pushController.startListen(userId)
    }

    //停止监听
    fun stopListener() {
        pushController.stopListener()
    }

    /**
     * 加入队伍
     * @param teamNum 队伍口令
     */
    fun reqJoin(teamNum: String?): Int {
        return userGroupController.reqJoin(teamNum)
    }

    /**
     * 获取组队状态
     */
    fun reqStatus(): Int {
        return userGroupController.reqStatus()
    }

    /**
     * 注册send2car 推送监听
     * @param listener
     */
    fun addSend2carPushMsgListener(listener: AimPushMessageListener?) {
        pushController.addSend2carPushMsgListener(listener)
    }

    /**
     * 移除send2car 推送监听
     * @param listener
     */
    fun removeSend2carPushMsgListener(listener: AimPushMessageListener?) {
        pushController.removeSend2carPushMsgListener(listener)
    }

    /**
     * 注册组队消息通知
     * @param listener
     */
    fun addTeamMessageListener(listener: TeamMessageListener?) {
        pushController.addTeamMessageListener(listener)
    }

    /**
     * 移除组队消息通知
     * @param listener
     */
    fun removeTeamMessageListener(listener: TeamMessageListener?) {
        pushController.removeTeamMessageListener(listener)
    }

    /**
     * 标记消息已读
     */
    fun markMessageAsRead(type: Int, id: Long): Int {
        return pushController.markMessageAsRead(type, id)
    }

    /**
     * 删除send2car单个记录
     * @param type
     * @param messageId
     */
    fun deleteSend2carMsg(@MsgPushType.MsgPushType1 type: Int, messageId: Long) {
        pushController.deleteSend2carMsg(type, messageId)
    }

    /**
     * 删除组队消息单个记录
     * @param messageId
     */
    fun deleteTeamPushMsgMessages(messageId: Long) {
        pushController.deleteTeamPushMsgMessages(messageId)
    }

    /**
     * 获取send2car消息列表
     * @return
     */
    fun getSend2carMsg(): ArrayList<AimPoiPushMsg>? {
        return pushController.send2carMsg
    }

    /**
     * 获取路线消息列表
     * @return
     */
    fun getAimRoutePushMsg(): ArrayList<AimRoutePushMsg>? {
        return pushController.aimRoutePushMsg
    }

    /**
     * 获取send2car消息列表
     * @return
     */
    fun getSend2carPushMsg(): ArrayList<AimPushMsg>? {
        return pushController.getSend2carPushMsg()
    }

    fun isPoiMsg(aimPushMsg: AimPushMsg?): Boolean {
        return aimPushMsg?.aimPoiMsg != null && !TextUtils.isEmpty(aimPushMsg.aimPoiMsg.createTime)
    }

    fun isRouteMsg(aimPushMsg: AimPushMsg?): Boolean {
        return aimPushMsg?.aimRouteMsg != null && !TextUtils.isEmpty(aimPushMsg.aimRouteMsg.createTime)
    }

    /**
     * 获取组队推送消息
     * @return
     */
    fun getTeamPushMsgMessages(): ArrayList<TeamPushMsg>? {
        return pushController.teamPushMsgMessages
    }

    /**
     * 上报停车位置
     * @param linkCarLocation
     */
    fun startReportCarLocation(linkCarLocation: LinkCarLocation?, aosCallbackRef: ICallBackWsTserviceInternalLinkAutoReport?): Boolean {
        return linkCarController.startReportCarLocation(linkCarLocation, aosCallbackRef)
    }

    /**
     * 注册手车互联推送消息通知监听
     * @param listener
     */
    fun addLinkPushMessageListener(listener: LinkPushMessageListener?) {
        pushController.addLinkPushMessageListener(listener)
    }

    /**
     * 是否send2car poi消息
     * @param aimPoiPushMsg
     * @return
     */
    fun isSend2CarPoiMsg(aimPoiPushMsg: AimPoiPushMsg?): Boolean {
        return pushController.isSend2CarPoiMsg(aimPoiPushMsg)
    }

    fun isSend2CaRouteMsg(aimRoutePushMsg: AimRoutePushMsg?): Boolean {
        return pushController.isSend2CaRouteMsg(aimRoutePushMsg)
    }

    // ============================ 其他 ================================
    //关闭Observer
    fun removeObserver() {
        groupObserverBusiness.removeObserver(BaseConstant.TYPE_GROUP_PUSH_MESSAGE)
        groupObserverBusiness.removeOnGroupServiceObserver(BaseConstant.TYPE_GROUP_PUSH_MESSAGE)
    }

    //开启Observer并获取组队状态
    fun addObserverAndReqStatus(teamNumber: String) {
        mTeamNumber = teamNumber
        coroutineScope = CoroutineScope(Dispatchers.Main)
        groupObserverBusiness.removeOnGroupServiceObserver(BaseConstant.TYPE_GROUP_PUSH_MESSAGE)
        setOnGroupServiceObserver()
        groupObserverBusiness.addObserverAndReqStatus(teamNumber, BaseConstant.TYPE_GROUP_PUSH_MESSAGE)
        reqStatus()
    }

    // 在 View 销毁时取消协程
    fun scopeCancel() {
        coroutineScope?.cancel()
    }

    private fun setOnGroupServiceObserver() {
        groupObserverBusiness.setOnGroupServiceObserver(BaseConstant.TYPE_GROUP_PUSH_MESSAGE, object : GroupObserverBusiness.OnGroupServiceObserver {
            override fun onObserverResult(resultBean: GroupObserverResultBean) {
                coroutineScope?.launch {
                    if (resultBean.businessType == BaseConstant.TYPE_GROUP_PUSH_MESSAGE) {
                        val observerType = resultBean.observerType
                        when (observerType) {
                            BaseConstant.TYPE_GROUP_STATUS -> { //获取组队状态回调
                                val response = resultBean.data as GroupResponseStatus
                                // 在协程中执行异步任务
                                withContext(Dispatchers.IO) {
                                    if (!TextUtils.isEmpty(response.teamId)) { // 当前已有队伍，请求队伍信息
                                        closeView.postValue(true) //关闭卡片
                                    }
                                }
                            }

                            BaseConstant.TYPE_GROUP_JOIN -> { //加入队伍回调
                                val response = resultBean.data as GroupResponseJoin
                                // 在协程中执行异步任务
                                withContext(Dispatchers.IO) {
                                    if (response.code == 1) { //成功
                                        UserGroupController.getInstance().updateGroupInfo(response)
                                        if (!TextUtils.isEmpty(response.team.teamId)) {
                                            closeView.postValue(true) //关闭卡片
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            when (response.code) {
                                                2001, //用户未登录
                                                2006, 2007, //您来晚了，队伍已经解散 2.队伍不存在
                                                2008, //队伍人数已经达到上限
                                                2009, //用户已经在自己的队伍中
                                                2002, 2010 -> { //用户已经在他队伍中 ,用户已经在自己的队伍中 ,用户已经在其他的队伍中
                                                    closeView.postValue(false) //关闭卡片
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }

}