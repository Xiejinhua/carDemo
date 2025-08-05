package com.desaysv.psmap.model.business

import android.graphics.Rect
import android.text.TextUtils
import android.view.MotionEvent
import androidx.lifecycle.MutableLiveData
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.layer.model.BizAGroupBusinessInfo
import com.autonavi.gbl.layer.model.BizAGroupType
import com.autonavi.gbl.layer.model.BizUserType
import com.autonavi.gbl.map.OperatorPosture
import com.autonavi.gbl.map.layer.BaseLayer
import com.autonavi.gbl.map.layer.LayerItem
import com.autonavi.gbl.map.layer.model.ClickViewIdInfo
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.model.PointD
import com.autonavi.gbl.map.model.PreviewParam
import com.autonavi.gbl.map.observer.IMapGestureObserver
import com.autonavi.gbl.user.group.model.GroupMember
import com.autonavi.gbl.user.group.model.GroupResponseInfo
import com.autonavi.gbl.user.msgpush.model.TeamMember
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.autonavi.gbl.user.msgpush.model.TeamUploadResponseMsg
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.push.listener.TeamMessageListener
import com.autosdk.bussiness.widget.route.utils.RectUtils
import com.autosdk.common.AutoStatus
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.base.bean.GroupObserverResultBean
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.GroupObserverBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.R
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 组队主界面业务
 */
@Singleton
class MyTeamBusiness @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val userGroupBusiness: UserGroupBusiness,
    private val pushMessageBusiness: PushMessageBusiness,
    private val groupObserverBusiness: GroupObserverBusiness,
    private val mINaviRepository: INaviRepository,
    private val mapBusiness: MapBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val settingComponent: ISettingComponent,
    private val netWorkManager: NetWorkManager,
    private val gson: Gson
) {
    private var mIsFocus = false //是否为焦点
    private var isFirst = true //是否第一次进我的组队页面
    var mCurrentId = "" //当前选中的队友id
    private var mTeamId: String? = null
    var leader: String? = null
    private var mSelectPosition = -1
    var lastSelectPosition = -1
    private var backToCarTimer: Timer? = null
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
    var pageType = 0
    private var updateTimes = 0
    private var mPosition = 0
    private var mDesLon = 0.0 //目的地经纬度坐标
    private var mDesLat = 0.0 //目的地经纬度坐标
    private var mDesAddress = "" //目的地地址
    var rect: Rect? = null
    private var myMessage = 0 //是否是我的消息跳进去的
    private var mHidden: Boolean = false

    var mMembersList = ArrayList<GroupMember>()
    var mMembers = ArrayList<GroupMember>()
    private var removeMembers = ArrayList<GroupMember>()
    private var routeNaviMembers = ArrayList<GroupMember>()
    val showPreview = MutableLiveData<Boolean>()
    val mLeader = MutableLiveData<Boolean>()
    val mRemoveMemberShow = MutableLiveData<Boolean>()
    val mUserId = MutableLiveData<String>()
    val mPOI = MutableLiveData<POI?>()
    val removePosition = MutableLiveData<Int>()
    val mMembersFocusList = ArrayList<Boolean>()
    val mMemberShow = MutableLiveData(false)
    val isShowBackCar = MutableLiveData(false)
    val teamPassword = MutableLiveData("") //队伍口令密码
    val myTeamStr = MutableLiveData("0") //我的队伍--title
    val teamSettingStr = MutableLiveData("") //我的设置--title
    val showTeamSetting = MutableLiveData<Boolean>() //显示设置布局
    val showNickname = MutableLiveData<Boolean>() //显示昵称布局
    val captainRemoveLayout = MutableLiveData<Boolean>() //移除队员一栏是否显示
    val teamDisbandStr = MutableLiveData<String>() //显示退出组队，还是解散组队
    val nicknameStr = MutableLiveData("") //昵称
    val showRemovePlayer = MutableLiveData<Boolean>() //显示移除队友布局
    val setDestination = MutableLiveData<Boolean>() //是否可以编辑目的地
    val noDestination = MutableLiveData<Boolean>() //没有目的地--队长显示
    val showEdit = MutableLiveData<Boolean>() //编辑地址按钮是否显示
    val mPOIName = MutableLiveData<String>() //目的地名称
    val setToast = MutableLiveData<String>() //toast内容
    val finishFragment = MutableLiveData<Boolean>() //关闭组队界面
    val updateMembersData = MutableLiveData<Int>() //更新队伍中某个人信息
    val updateALLMembersData = MutableLiveData<String>() //更新队伍列表信息
    val updateRemoveMembers = MutableLiveData<ArrayList<GroupMember>>() //更新删除队友列表数据
    val updateGroupDestination = MutableLiveData<Boolean>() //更新组队目的地
    val updateGroupResponseResult = groupObserverBusiness.updateGroupResponseResult //组队相关请求是否成功
    val updateGroupDestinationSuccess = groupObserverBusiness.updateGroupDestinationSuccess //组队设置目的地成功
    var lastGroupResponseInfo: GroupResponseInfo? = null //上一次数据

    private val groupScope = CoroutineScope(Dispatchers.IO + Job())

    //手势Observer
    private val mapGestureObserver = object : IMapGestureObserver {
        override fun onDoublePress(p0: Long, p1: Long, p2: Long): Boolean {
            return false
        }

        override fun onSinglePress(p0: Long, p1: Long, p2: Long, p3: Boolean): Boolean {
            return false
        }

        //地图手势监听
        override fun onMotionEvent(engineId: Long, action: Int, px: Long, py: Long) {
            Timber.d("onMotionEvent action = $action")
            if (MotionEvent.ACTION_DOWN == action) {
                if (mIsFocus) {
                    setPoiFocus(mCurrentId, -1, false)
                    setSelectedMemberFalse()
                    mSelectPosition = -1
                }
            } else if (MotionEvent.ACTION_MOVE == action) {
                isShowBackCar.postValue(true)
            }
        }
    }

    private val iLayerClickObserver = object : ILayerClickObserver {
        override fun onNotifyClick(layer: BaseLayer?, pItem: LayerItem?, clickViewIds: ClickViewIdInfo?) {
            if (layer == null || pItem == null) {
                return
            }
            val businessType = pItem.businessType
            val id = pItem.id
            val index = pItem.priority
            Timber.d("iLayerClickObserver businessType: %s , id: %s, index: %s , mIsFocus: %s", businessType, id, index, mIsFocus)
            when (businessType) {
                BizAGroupType.BizAGroupTypeAGroup -> {
                    if (!mIsFocus) {
                        setPoiFocus(id, index, true)
                        setSelectedMember(index + 1)
                        mSelectPosition = index + 1
                        lastSelectPosition = mSelectPosition
                    } else {
                        setPoiFocus(id, index, false)
                        setSelectedMemberFalse()
                        mSelectPosition = -1
                    }
                    userGroupBusiness.aMapLayer?.setFollowMode(false)
                    resetBackToCarTimer()
                }

                else -> {}
            }
        }
    }

    //组队消息通知监听器
    private val teamMessageListener = object : TeamMessageListener {
        override fun notifyTeamPushMessage(teamPushMsg: TeamPushMsg?) {
            //TODO("Not yet implemented")
        }

        override fun notifyTeamUploadResponseMessage(teamUploadResponseMsg: TeamUploadResponseMsg?) {
            groupScope.launch {
                if (teamUploadResponseMsg != null) {
                    paraTeamUploadResponseMsg(teamUploadResponseMsg)
                }
            }
        }

    }

    private fun setOnGroupServiceObserver() {
        groupObserverBusiness.setOnGroupServiceObserver(BaseConstant.TYPE_GROUP_MY, object : GroupObserverBusiness.OnGroupServiceObserver {
            override fun onObserverResult(resultBean: GroupObserverResultBean) {
                groupScope.launch {
                    if (resultBean.businessType == BaseConstant.TYPE_GROUP_MY) {
                        val observerType = resultBean.observerType
                        when (observerType) {
                            BaseConstant.TYPE_GROUP_INFO -> { //获取队伍信息回调
                                val groupResponseInfo = resultBean.data as GroupResponseInfo
                                if (groupResponseInfo != null) {
                                    if (groupResponseInfo.code != 1) {
                                        if (lastGroupResponseInfo != null) {
                                            Timber.i("setOnGroupServiceObserver TYPE_GROUP_INFO lastGroupResponseInfo != null")
                                            lastGroupResponseInfo?.apply {
                                                userGroupBusiness.aGroupLayer?.clearAllItems()
                                                if (isFirst) {
                                                    userGroupBusiness.aGroupLayer?.setVisible(false)
                                                }
                                                userGroupBusiness.updateGroupInfo(this)
                                                setGroupData(lastGroupResponseInfo, userGroupBusiness.getAllMemberList())
                                                userGroupBusiness.publishTeamInfo() //上报组队位置信息
                                                userGroupBusiness.doStartGroupPosition(false) //开始5s一次上报自己位置

                                                val coord2DDouble = OperatorPosture.mapToLonLat(
                                                    this.team.destination.display.lon.toDouble(),
                                                    this.team.destination.display.lat.toDouble()
                                                )
                                                if (this.team.destination.display.lon != 0 && this.team.destination.display.lat != 0) {
                                                    mDesLon = coord2DDouble.lon
                                                    mDesLat = coord2DDouble.lat
                                                    setEndPoint(coord2DDouble.lon, coord2DDouble.lat, 0)
                                                } else {
                                                    mDesLon = 0.0
                                                    mDesLat = 0.0
                                                }
                                                updateAGroupMember(this.members, false, -1)
                                            }
                                        } else if (!netWorkManager.isNetworkConnected()) {
                                            setToast.postValue("网络异常，无法正常使用组队出行了")
                                            userGroupBusiness.doStop() //取消位置上传定时器
                                            userGroupBusiness.showGroupBtn.postValue(false)
                                            finishFragment.postValue(true)
                                        } else {
                                            setToast.postValue("功能异常，无法正常使用组队出行了")
                                            userGroupBusiness.doStop() //取消位置上传定时器
                                            userGroupBusiness.showGroupBtn.postValue(false)
                                            finishFragment.postValue(true)
                                        }
                                        return@launch
                                    }

                                    if (!TextUtils.isEmpty(groupResponseInfo.team.teamId)) {
                                        lastGroupResponseInfo = groupResponseInfo
                                        userGroupBusiness.aGroupLayer?.clearAllItems()
                                        if (isFirst) {
                                            userGroupBusiness.aGroupLayer?.setVisible(false)
                                        }
                                        userGroupBusiness.updateGroupInfo(groupResponseInfo)
                                        setGroupData(groupResponseInfo, userGroupBusiness.getAllMemberList())
                                        userGroupBusiness.publishTeamInfo() //上报组队位置信息
                                        userGroupBusiness.doStartGroupPosition(false) //开始5s一次上报自己位置

                                        val coord2DDouble = OperatorPosture.mapToLonLat(
                                            groupResponseInfo.team.destination.display.lon.toDouble(),
                                            groupResponseInfo.team.destination.display.lat.toDouble()
                                        )
                                        if (groupResponseInfo.team.destination.display.lon != 0 && groupResponseInfo.team.destination.display.lat != 0) {
                                            mDesLon = coord2DDouble.lon
                                            mDesLat = coord2DDouble.lat
                                            setEndPoint(coord2DDouble.lon, coord2DDouble.lat, 0)
                                        } else {
                                            mDesLon = 0.0
                                            mDesLat = 0.0
                                        }
                                        updateAGroupMember(groupResponseInfo.members, false, -1)
                                    } else {
                                        Timber.i("setOnGroupServiceObserver groupResponseInfo.team.teamId isEmpty")
                                        lastGroupResponseInfo = null
                                        userGroupBusiness.doStop() //取消位置上传定时器
                                        userGroupBusiness.showGroupBtn.postValue(false)
                                        finishFragment.postValue(true)
                                        if (!netWorkManager.isNetworkConnected()) {
                                            setToast.postValue("网络异常，无法正常使用组队出行了")
                                        } else {
                                            setToast.postValue("功能异常，无法正常使用组队出行了")
                                        }
                                    }
                                }
                            }

                            BaseConstant.TYPE_GROUP_NICK_NAME -> { //请求修改队伍中的昵称回调通知
                                getTeamInfo()
                                pageType = BaseConstant.GROUP_SETTING_TYPE
                                myTeamStr.postValue("")
                                teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_player_settings))
                                showTeamSetting.postValue(true)
                                showNickname.postValue(false)
                                showPreview.postValue(true) //地图扎点在右侧  全览
                                nicknameStr.postValue("")
                            }

                            BaseConstant.TYPE_GROUP_DISSOLVE -> { //请求解散队伍回调通知
                                userGroupBusiness.clearTeamInfo() //清空队伍信息
                                userGroupBusiness.aGroupLayer?.clearAllItems()
                                userGroupBusiness.doStop() //取消位置上传定时器
                                userGroupBusiness.showGroupBtn.postValue(false)
                                finishFragment.postValue(true)
                            }

                            BaseConstant.TYPE_GROUP_QUIT -> { //请求退出队伍回调通知
                                userGroupBusiness.clearTeamInfo() //清空队伍信息
                                userGroupBusiness.aGroupLayer?.clearAllItems()
                                userGroupBusiness.doStop() //取消位置上传定时器
                                userGroupBusiness.showGroupBtn.postValue(false)
                                finishFragment.postValue(true)
                            }

                            BaseConstant.TYPE_GROUP_KICK -> { //队长踢人请求回调通知
                                removeSuccess()
                                setPoiFocus(mCurrentId, -1, false) //取消地图选择
                                isFirst = true
                                getTeamInfo() //获取队伍信息
                            }
                        }
                    }
                }
            }
        })
    }

    fun initData() {
        isFirst = true
        mMemberShow.postValue(false)
        userGroupBusiness.clearAllItems()
        userGroupBusiness.removeClickObserver(iLayerClickObserver)
        setOnGroupServiceObserver()
        userGroupBusiness.addObserver(BaseConstant.TYPE_GROUP_MY)
        userGroupBusiness.addClickObserver(iLayerClickObserver)
        userGroupBusiness.addGestureObserver(mapGestureObserver) //注册图层手势监听
        val accountInfo = settingAccountBusiness.getAccountProfile()
        mUserId.postValue(accountInfo?.uid ?: "")
        pushMessageBusiness.addTeamMessageListener(teamMessageListener)
        getTeamInfo() //获取队伍信息
        userGroupBusiness.doStartGroupPosition(false) //开始5s一次上报自己位置

//        userGroupBusiness.aMapLayer?.showFlyLine(false)
        userGroupBusiness.aMapLayer?.setCarVisible(false)
        userGroupBusiness.aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
        userGroupBusiness.aGroupLayer?.setVisible(BizAGroupType.BizAGroupTypeAGroup.toLong(), true)
        userGroupBusiness.aGroupLayer?.setVisible(BizAGroupType.BizAGroupTypeEndPoint.toLong(), true)
        userGroupBusiness.aUserBehaviorLayer?.setVisible(BizUserType.BizUserTypeFavoriteMain.toLong(), false)
    }

    fun onCleared() {
        mMemberShow.postValue(false)
        userGroupBusiness.removeObserver(BaseConstant.TYPE_GROUP_MY)
        groupObserverBusiness.removeOnGroupServiceObserver(BaseConstant.TYPE_GROUP_MY)
        userGroupBusiness.removeGestureObserver(mapGestureObserver) //取消注册图层手势监听
        backToCarTimer?.cancel() // 在ViewModel销毁时停止定时器

        isFirst = true
        pushMessageBusiness.removeTeamMessageListener(teamMessageListener)
        userGroupBusiness.doStop() //取消位置上传定时器
        if (userGroupBusiness.aGroupLayer != null) {
            removeItem() //删除自己地图上的扎点
        }
        if (userGroupBusiness.aGroupLayer != null)
            userGroupBusiness.removeClickObserver(iLayerClickObserver)

//        userGroupBusiness.aMapLayer?.showFlyLine(true)
        userGroupBusiness.aMapLayer?.setCarVisible(true)
        userGroupBusiness.aMapLayer?.setFollowMode(true)
        userGroupBusiness.aGroupLayer?.setVisible(BizAGroupType.BizAGroupTypeEndPoint.toLong(), false)
        userGroupBusiness.mMapView?.exitPreview(true)
        setMapLeftTop() //移除移图操作观察者
        mapBusiness.backCurrentCarPosition(false)
        userGroupBusiness.aUserBehaviorLayer?.setVisible(BizUserType.BizUserTypeFavoriteMain.toLong(), true)
        mapBusiness.switchMapViewMode(settingComponent.getConfigKeyMapviewMode())//恢复视角
    }

    //界面Hidden处理
    fun onHiddenChanged(hidden: Boolean, destroy: Boolean) {
        mHidden = if (destroy) false else hidden
        if (!hidden) {
            Timber.i("onHiddenChanged !hidden")
            updateTimes = 30
            updateAGroupMember(mMembers, false, -1);
            userGroupBusiness.aMapLayer?.setCarVisible(false)
            userGroupBusiness.aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
            userGroupBusiness.aUserBehaviorLayer?.setVisible(BizUserType.BizUserTypeFavoriteMain.toLong(), false)
            setOnGroupServiceObserver()
            userGroupBusiness.addObserver(BaseConstant.TYPE_GROUP_MY)
            pushMessageBusiness.addTeamMessageListener(teamMessageListener)
            userGroupBusiness.doStartGroupPosition(false) //开始5s一次上报自己位置
        } else {
            Timber.i("onHiddenChanged hidden")
            userGroupBusiness.doStop() //取消位置上传定时器
            pushMessageBusiness.removeTeamMessageListener(teamMessageListener)
            userGroupBusiness.removeObserver(BaseConstant.TYPE_GROUP_MY)
            groupObserverBusiness.removeOnGroupServiceObserver(BaseConstant.TYPE_GROUP_MY)
            userGroupBusiness.aMapLayer?.setCarVisible(true)
            userGroupBusiness.aMapLayer?.setFollowMode(true)
            userGroupBusiness.aUserBehaviorLayer?.setVisible(BizUserType.BizUserTypeFavoriteMain.toLong(), true)
        }
    }

    fun onResume() {
        userGroupBusiness.aMapLayer?.setCarVisible(false)
        userGroupBusiness.aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
    }

    //开始回车位计时
    private fun startBackToCarTimer() {
        backToCarTimer = Timer()
        backToCarTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (mINaviRepository.isNavigating())
                    setNavingFocus()
                userGroupBusiness.aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
            }
        }, 0, 15 * 1000) // 每隔1秒执行一次
    }

    //重置回车位计时
    fun resetBackToCarTimer() {
        backToCarTimer?.cancel() // 取消之前的定时器
        backToCarTimer = null
        startBackToCarTimer() // 重新启动定时器
    }

    fun removeItem() {
        for (i in 0 until (userGroupBusiness.getAllMemberList()?.size ?: 0)) {
            if (TextUtils.equals(userGroupBusiness.getAllMemberList()?.get(i)?.uid, mUserId.value)) {
                userGroupBusiness.aGroupLayer?.removeItem(i.toString())
                break
            }
        }
    }

    /**
     * 设置队员显示背景
     *
     * @param position
     */
    fun setSelectedMember(position: Int) {
        groupScope.launch {
            for (i in 1 until mMembersFocusList.size) {
                mMembersFocusList[i] = i == position
            }
            updateMembersData.postValue(position)//更新队伍中某个人信息
        }
    }

    /**
     * 设置队员显示背景全部为false
     */
    fun setSelectedMemberFalse() {
        groupScope.launch {
            for (i in mMembersFocusList.indices) {
                mMembersFocusList[i] = false
            }
            updateMembersData.postValue(lastSelectPosition)//更新队伍中某个人信息
        }
    }

    /**
     * 获取队伍信息
     */
    fun getTeamInfo() {
        mTeamId = if (TextUtils.isEmpty(BaseConstant.TEAM_ID)) mapSharePreference.getStringValue(
            MapSharePreference.SharePreferenceKeyEnum.teamId,
            ""
        ) else BaseConstant.TEAM_ID
        userGroupBusiness.reqGroupInfo(mTeamId)
    }

    /**
     * 设置初始化队伍信息
     * @param groupResponseInfo
     */
    fun setGroupData(groupResponseInfo: GroupResponseInfo?, list: ArrayList<GroupMember>?) {
        if (groupResponseInfo == null || list == null || list.size < 0) {
            return
        }
        if (pageType == BaseConstant.GROUP_DEFAULT_TYPE) {
            myTeamStr.postValue(list.size.toString())
            teamSettingStr.postValue("")
        }
        teamPassword.postValue(groupResponseInfo.team.teamNumber)
        leader = groupResponseInfo.team.leaderId
        if (groupResponseInfo.team.leaderId == mUserId.value) {
            mLeader.postValue(true)
        } else {
            mLeader.postValue(false)
        }
        if (mMembers != null && mMembers.size > 0) {
            mMembersList.clear()
            mMembers.clear()
        }
        val member = GroupMember()
        member.nickName = "添加"
        member.uid = "999"
        member.online = true
        mMembersList.add(member)
        mMembersList.addAll(list)
        mMembers.addAll(list)
        if (mMembersFocusList != null) {
            for (i in mMembersList.indices) {
                if (i == mSelectPosition) {
                    mMembersFocusList.add(i, true)
                } else {
                    mMembersFocusList.add(i, false)
                }
            }
        }

        updateALLMembersData.postValue(groupResponseInfo.team.leaderId) //更新队伍列表信息

        mMemberShow.postValue(true)
        if (groupResponseInfo.team.destination != null && groupResponseInfo.team.destination.name != "") {
            Timber.i("setDestination setGroupData destination")
            val poi = POI()
            poi.name = groupResponseInfo.team.destination.name
            val coord2DDouble = OperatorPosture.mapToLonLat(
                groupResponseInfo.team.destination.display.lon.toDouble(),
                groupResponseInfo.team.destination.display.lat.toDouble()
            )
            poi.point = GeoPoint(coord2DDouble.lon, coord2DDouble.lat)
            poi.id = groupResponseInfo.team.destination.poiId
            poi.addr = groupResponseInfo.team.destination.address
            if (!isFirst) {
                mDesAddress = groupResponseInfo.team.destination.address
                if (mPOI.value == null) {
                    Timber.i("setDestination setGroupData destination mPOI.value == null")
                    setToast.postValue("队长设置了目的地")
                } else if (!TextUtils.equals(mPOI.value!!.id, groupResponseInfo.team.destination.poiId)) {
                    Timber.i("setDestination setGroupData destination poiId")
                    setToast.postValue("队长设置了目的地") //显示toast
                }
            } else if (!TextUtils.equals(
                    mDesAddress,
                    groupResponseInfo.team.destination.address
                ) && groupResponseInfo.team.leaderId != mUserId.value
            ) {
                Timber.i("setDestination setGroupData destination mDesAddress")
                mDesAddress = groupResponseInfo.team.destination.address
                setToast.postValue("队长设置了目的地") //显示toast
            }
            mPOI.postValue(poi)
            mPOIName.postValue("目的地：" + poi.name)
        } else {
            mPOI.postValue(null)
            mPOIName.postValue("")
        }
    }


    fun goRemoveMember() {
        removeMembers.clear()
        if (mMembers.size > 1) {
            for (mMember in mMembers) {
                if (!mMember.uid.equals(mUserId.value, ignoreCase = true)) {
                    removeMembers.add(mMember)
                }
            }
        }
        if (removeMembers.size > 0) {
            updateRemoveMembers.postValue(removeMembers)
            mRemoveMemberShow.postValue(false)
        } else {
            mRemoveMemberShow.postValue(true)
        }
    }

    /**
     * 更新车队信息
     */
    fun updateAGroupMember(members: ArrayList<GroupMember>?, updateOne: Boolean, mPosition: Int) {
        if (members == null || members.size <= 0) {
            return
        }
        if (userGroupBusiness.checkDifferent(mMembersList, members)) { //已有组队图层不重复添加
            Timber.d("addAGroupMembers userGroupBusiness.checkDifferent(mMembersList, members)")
            userGroupBusiness.aGroupLayer?.updateStyle()
            return
        }

        var isUpdateOne = updateOne
        Timber.d("updateTimes：$updateTimes mPosition:$mPosition")
        if (updateTimes == 60) {
            userGroupBusiness.aGroupLayer?.clearAllItems()
            updateTimes = 0
            userGroupBusiness.syncUpdate(members) //更新队伍信息--列表
            if (mPOI.value != null) {
                setEndPoint(mPOI.value!!.point.rawLongitude, mPOI.value!!.point.rawLatitude, 0)
            }
            setSelectedMemberFalse()
            isUpdateOne = false
        }
        val defaultHead =
            if (NightModeGlobal.isNightMode()) com.desaysv.psmap.base.R.drawable.ic_default_avatar_night else com.desaysv.psmap.base.R.drawable.ic_default_avatar_day
        if (isUpdateOne && mPosition > -1 && mPosition < members.size) {
            val member = BizAGroupBusinessInfo()
            member.id = members[mPosition].uid
            member.priority = mPosition
            member.mPos3D.lon = members[mPosition].locInfo.lon
            member.mPos3D.lat = members[mPosition].locInfo.lat
            userGroupBusiness.getGroupHeadUpdateAGroupMember(members[mPosition].imgUrl, defaultHead, member)
        } else {
            //添加车队
            if (members == null || members.size <= 0) {
                Timber.i(" updateAGroupMember members == null || members.size <= 0 ")
            } else {
                for (i in members.indices) {
                    val member = BizAGroupBusinessInfo()
                    member.id = members[i].uid
                    member.priority = i
                    member.mPos3D.lon = members[i].locInfo.lon
                    member.mPos3D.lat = members[i].locInfo.lat
                    userGroupBusiness.getGroupHeadUpdateAGroupMember(members[i].imgUrl, defaultHead, member)
                }
            }
        }
        if (isFirst) {
            isFirst = false
            showPreview.postValue(true) //地图扎点在右侧  全览
        }
        updateTimes++
    }

    /**
     * 组队预览
     */
    fun aGroupShowPreview() {
        if (mMembers.isEmpty()) return

        val points = ArrayList<PointD>()
        for (member in mMembers) {
            val pointD = PointD()
            pointD.x = member.locInfo.lon
            pointD.y = member.locInfo.lat
            points.add(pointD)
        }

        if (mDesLon != 0.0 && mDesLat != 0.0) {
            val pointD = PointD()
            pointD.x = mDesLon
            pointD.y = mDesLat
            points.add(pointD)
        }

        if (points.size == 1) {
            setMapCenter(0)
        } else {
            userGroupBusiness.aMapLayer?.setFollowMode(false)
            userGroupBusiness.aMapLayer?.setPreviewMode(true) //设置预览模式
            val previewParam = PreviewParam()
            val rectDouble = RectUtils.getGroupMemberBound(points)
            if (rectDouble != null && rect != null) {
                previewParam.mapBound = rectDouble
                previewParam.leftOfMap = rect!!.left
                previewParam.topOfMap = rect!!.top
                previewParam.screenLeft = rect!!.left
                previewParam.screenTop = rect!!.top
                previewParam.screenRight = rect!!.right
                previewParam.screenBottom = rect!!.bottom
                previewParam.bUseRect = true
            } else {
                previewParam.points = points
                previewParam.bUseRect = false
            }
            userGroupBusiness.mMapView?.showPreview(previewParam, true, 500, -1)
        }
    }

    /**
     * 组队poi放大
     */
    fun setPoiFocus(id: String, index: Int, isFocus: Boolean) {
        mCurrentId = id
        mIsFocus = isFocus
        if (-1 != index) {
            setMapCenter(index)
            userGroupBusiness.aGroupLayer?.setFocus(BizAGroupType.BizAGroupTypeAGroup.toLong(), mCurrentId, isFocus)
        } else {
            userGroupBusiness.aGroupLayer?.clearFocus(BizAGroupType.BizAGroupTypeAGroup.toLong())
        }
    }

    /**
     * 设置组队终点
     * type  0请求组队信息后设置目的地 , 1 更改目的地后设置终点
     */
    fun setEndPoint(lon: Double, lat: Double, type: Int) {
        mDesLon = lon
        mDesLat = lat
        if (type == 1) {
            showPreview.postValue(true) //地图扎点在右侧  全览
        }
        userGroupBusiness.aGroupLayer?.setEndPoint(lon, lat)
    }

    private fun setMapCenter(index: Int) {
        var mIndex = index
        if (mIndex >= mMembersList.size) {
            mIndex = mMembersList.size - 1
        }
        if (mIndex == -1) {
            mIndex = 0
        }
        if (mMembers.isNotEmpty() && mMembers.size > mIndex) {
            mapBusiness.setMapCenter(mMembers[mIndex].locInfo.lon, mMembers[mIndex].locInfo.lat)
        }
    }

    //导航中15s无操作，去掉选中状态
    fun setNavingFocus() {
        userGroupBusiness.aGroupLayer?.clearFocus(BizAGroupType.BizAGroupTypeAGroup.toLong())
        setSelectedMemberFalse()
        mSelectPosition = -1
    }


    /**
     * 修改队伍中的昵称
     *
     * @param nickName
     */
    fun modifyNickname() {
        userGroupBusiness.reqSetNickName(nicknameStr.value)
    }


    /**
     * 解散队伍
     */
    fun teamDismiss() {
        userGroupBusiness.reqDissolve(mTeamId)
    }

    /**
     * 退出队伍
     */
    fun teamQuit() {
        userGroupBusiness.reqQuit(mTeamId)
    }

    /**
     * 移除组员
     */
    fun teamKick(kickIds: String, mPosition: Int) {
        this.mPosition = mPosition
        userGroupBusiness.reqKick(mTeamId, ArrayList<String>().apply { add(kickIds) })
    }

    fun removeSuccess() {
        removeMembers.removeAt(mPosition)
        userGroupBusiness.aGroupLayer?.clearAllItems()
        updateRemoveMembers.postValue(removeMembers)
        if (removeMembers.size <= 0) {
            mRemoveMemberShow.postValue(true)
        }
    }

    /**
     * 修改队伍属性  目的地设置
     */
    fun reqUpdateDestination(mPOI: POI) {
        updateGroupDestination.postValue(true)
        userGroupBusiness.reqUpdateDestination(mPOI, mTeamId)
        AutoStatusAdapter.sendStatus(AutoStatus.TEAM_CHANGE_DEST)
    }

    //解析位置上传信息回调
    private fun paraTeamUploadResponseMsg(teamUploadResponseMsg: TeamUploadResponseMsg) {
        Timber.d(" paraTeamUploadResponseMsg  %s", gson.toJson(teamUploadResponseMsg))
        if (teamUploadResponseMsg.state == 0) { //state 队伍状态，0正常，1无效，2解散，3移除或退出，4队伍信息变化
            val groupMembers = teamUploadResponseMsg.groupMembers
            if (groupMembers.isNotEmpty()) {
                refreshTeamMemberList(groupMembers) //更新队友列表
            }
        } else if (teamUploadResponseMsg.state == 2) {
            setToast.postValue("队伍过期或解散")
            userGroupBusiness.aGroupLayer?.clearAllItems()
            userGroupBusiness.doStop() //取消位置上传定时器
            userGroupBusiness.showGroupBtn.postValue(false)
            finishFragment.postValue(true)
        } else if (teamUploadResponseMsg.state == 3) {
            userGroupBusiness.aGroupLayer?.clearAllItems()
            userGroupBusiness.doStop() //取消位置上传定时器
            userGroupBusiness.showGroupBtn.postValue(false)
            finishFragment.postValue(true)
        } else if (teamUploadResponseMsg.state == 4) { //state 队伍状态，0正常，1无效，2解散，3移除或退出，4队伍信息变化
            isFirst = true
            getTeamInfo() //组队-获取队伍信息
        } else {
            refreshTeamMemberList(teamUploadResponseMsg.groupMembers) //更新队友列表
        }
    }

    //更新队友列表
    private fun refreshTeamMemberList(groupMembers: ArrayList<TeamMember>) {
        for (i in 1 until mMembersList.size) {
            for (teamMember in groupMembers) {
                if (mMembersList[i] != null && TextUtils.equals(mMembersList[i].uid, teamMember.uid)) {
                    if (mMembersList[i].locInfo.lon != teamMember.locInfo.lon || mMembersList[i].locInfo.lat != teamMember.locInfo.lat || mMembersList[i].online != teamMember.online || mMembersList[i].locUpdateTime != teamMember.locationUpdateTime) {
                        mMembersList[i].locInfo.lon = teamMember.locInfo.lon
                        mMembersList[i].locInfo.lat = teamMember.locInfo.lat
                        mMembersList[i].online = teamMember.online
                        mMembersList[i].locUpdateTime = teamMember.locationUpdateTime
                        mMembers[i - 1].locInfo.lon = teamMember.locInfo.lon
                        mMembers[i - 1].locInfo.lat = teamMember.locInfo.lat
                        mMembers[i - 1].online = teamMember.online
                        mMembers[i - 1].locUpdateTime = teamMember.locationUpdateTime
                        updateMembersData.postValue(i)//更新队伍中某个人信息
                        updateMember(i - 1)
                    }
                    break
                }
            }
        }
    }

    /**
     * 更新队友和图层信息
     * int position, TeamMember teamMember
     */
    private fun updateMember(mPoistion: Int) {
        groupScope.launch {
            mMemberShow.postValue(true)
            routeNaviMembers.clear()
            if (mHidden) {
                for (member in mMembers) {
                    if (!member.uid.equals(mUserId.value, ignoreCase = true)) {
                        routeNaviMembers.add(member)
                    }
                }
            } else {
                routeNaviMembers.addAll(mMembers)
            }
            userGroupBusiness.syncUpdate(routeNaviMembers) //更新队伍信息--列表
            updateAGroupMember(routeNaviMembers, true, mPoistion)
        }
    }

    //设置mMembersFocusList数据都为false
    fun setListDataFalse() {
        for (i in mMembersFocusList.indices) {
            mMembersFocusList[i] = false
        }
    }

    //队伍列表点击
    fun onItemClick(id: String, position: Int) {
        lastSelectPosition = position
        setPoiFocus(id, position - 1, true)
        setSelectedMember(position)
        userGroupBusiness.aMapLayer?.setFollowMode(false)
        resetBackToCarTimer()
    }

    //队伍头像
    fun onRefreshHead(position: Int) {
        if (position > 0 && mMembers != null && position - 1 < mMembers.size) {
            Timber.d("onRefreshHead updateAGroupMember position: %s  mMembers: %s", (position - 1), mMembers.size)
            updateAGroupMember(mMembers, true, position - 1)
        }
    }

    //移除队员
    fun removePosition(position: Int) {
        removePosition.postValue(position)
    }

    fun toGetPageType(): Int {
        return pageType
    }

    //点击返回键操作
    fun doBack() {
        when (pageType) {
            BaseConstant.GROUP_SETTING_TYPE -> {
                pageType = BaseConstant.GROUP_DEFAULT_TYPE
                myTeamStr.postValue(mMembers.size.toString())
                teamSettingStr.postValue("")
                showTeamSetting.postValue(false)
            }

            BaseConstant.GROUP_CHANGE_USERNAME_TYPE -> {
                //点击了 修改昵称的返回
                pageType = BaseConstant.GROUP_SETTING_TYPE
                myTeamStr.postValue("")
                teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_player_settings))
                showTeamSetting.postValue(true)
                showNickname.postValue(false)
            }

            BaseConstant.GROUP_REMOVE_MEMBERS_TYPE -> {
                //点击了 移除组员的返回
                pageType = BaseConstant.GROUP_SETTING_TYPE
                myTeamStr.postValue("")
                teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_player_settings))
                showTeamSetting.postValue(true)
                showNickname.postValue(false)
                showRemovePlayer.postValue(false)
            }
        }
    }

    //退出组队界面时，回到默认布局，下次进入组队界面显示默认布局
    fun defaultPage() {
        pageType = BaseConstant.GROUP_DEFAULT_TYPE
        teamSettingStr.postValue("")
        showTeamSetting.postValue(false)
        setDestination.postValue(false)
        noDestination.postValue(false)
        mHidden = false
        lastGroupResponseInfo = null
    }

    //设置按钮点击操作
    fun doSettingIcon() {
        pageType = BaseConstant.GROUP_SETTING_TYPE
        myTeamStr.postValue("")
        teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_settings))
        showTeamSetting.postValue(true)
    }

    //回到自身位置
    fun btnBackCar() {
        isShowBackCar.postValue(false)
        setPoiFocus(mCurrentId, -1, false) //取消地图选择
        setSelectedMemberFalse()
        mapBusiness.goToDefaultPosition(false)
        if (mMembers.size > 0) {
            for (member in mMembers) {
                if (member.uid.equals(mUserId.value, ignoreCase = true)) {
                    mapBusiness.setMapCenter(member.locInfo.lon, member.locInfo.lat)
                    break
                }
            }
        }
    }

    //全览按钮
    fun fullView() {
        userGroupBusiness.aMapLayer?.setFollowMode(false)
        resetBackToCarTimer()
        setPoiFocus(mCurrentId, -1, false);//取消地图选择
        setSelectedMemberFalse()
        showPreview.postValue(true);//地图扎点在右侧  全览
    }

    //点击了 修改昵称
    fun nickNameTv() {
        pageType = BaseConstant.GROUP_CHANGE_USERNAME_TYPE
        myTeamStr.postValue("")
        teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_change_username))
        showTeamSetting.postValue(false)
        showNickname.postValue(true)
        if (mMembers.size > 0) {
            for (member in mMembers) {
                if (member.uid.equals(mUserId.value, ignoreCase = true)) {
                    nicknameStr.postValue(if (member.nickName != null) member.nickName else "")
                    break
                }
            }
        }
    }

    //设置昵称--点击确定按钮 确定修改昵称
    fun tvOk() {
        if (TextUtils.isEmpty(nicknameStr.value) || nicknameStr.value?.isEmpty() == true) {
            setToast.postValue(ResUtil.getString(R.string.sv_group_main_set_nickname_tip_empty));
            return
        }
        if (nicknameStr.value?.length!! > 16) {
            setToast.postValue(ResUtil.getString(R.string.sv_group_main_set_nickname_tips));
            return
        }
        modifyNickname()
    }

    //解散或者退出队伍
    fun quitOrDismiss() {
        if (mLeader.value!!) {
            teamDismiss() //解散队伍
        } else {
            teamQuit() //退出队伍
        }
    }

    //点击移除组员
    fun doRemoveMember() {
        pageType = BaseConstant.GROUP_REMOVE_MEMBERS_TYPE
        myTeamStr.postValue("")
        teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_remove_members))
        showTeamSetting.postValue(false)
        showNickname.postValue(false)
        showRemovePlayer.postValue(true)
        goRemoveMember()
    }

    fun getPoi(): MutableLiveData<POI?> {
        return mPOI
    }

    fun toSetRect(rect: Rect?) {
        this.rect = rect
    }

    fun setMessage(myMessage: Int) {
        this.myMessage = myMessage
    }

    //全览
    fun showPreview() {
        aGroupShowPreview()
        if (myMessage == 1) {//我的消息页面跳转的再获取一次 防止不出现地图扎点
            myMessage = 0;
            getTeamInfo();//获取组队信息
        }
        userGroupBusiness.aGroupLayer?.setVisible(true)
    }

    //判断是否为队长，显示对应布局
    fun judeLeaderShowLayout(isLeader: Boolean) {
        if (isLeader) {
            //是队长
            captainRemoveLayout.postValue(true)
            teamDisbandStr.postValue(ResUtil.getString(R.string.sv_group_disband_the_team))
        } else {
            //队员
            captainRemoveLayout.postValue(false)
            teamDisbandStr.postValue(ResUtil.getString(R.string.sv_group_leave_the_team))
        }
    }

    fun setNickname(name: String) {
        nicknameStr.postValue(name)
    }

    //判断显示目的地横条
    fun judeHasDestination(poi: POI?) {
        if (poi != null) {
            setDestination.postValue(true)
            noDestination.postValue(false)
            if (mLeader.value!!) {
                showEdit.postValue(true)
            } else {
                showEdit.postValue(false)
            }
        } else if (mLeader.value!!) {
            setDestination.postValue(false)
            noDestination.postValue(true)
        } else {
            setDestination.postValue(false)
            noDestination.postValue(false)
        }
    }

    fun getMembers(): ArrayList<GroupMember> {
        return mMembers
    }

    fun getRemoveMembers(): ArrayList<GroupMember> {
        return removeMembers
    }

    fun getMembersList(): ArrayList<GroupMember> {
        return mMembersList
    }

    fun getTeamPassword(): String? {
        return teamPassword.value
    }

    /**
     * 移除移图操作观察者
     */
    fun setMapLeftTop() {
        userGroupBusiness.setMapLeftTop(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    /**
     * 退出组队界面--回到主图等
     */
    fun leaveAGroupMembers() {
        userGroupBusiness.leaveAGroupMembers()
    }
}