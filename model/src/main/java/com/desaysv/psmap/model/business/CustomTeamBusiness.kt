package com.desaysv.psmap.model.business

import android.Manifest
import android.app.Application
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.MotionEvent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.layer.model.BizAGroupType
import com.autonavi.gbl.layer.model.BizUserType
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
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.base.business.GroupObserverBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.PermissionReqController
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.bean.CustomGroupMember
import com.desaysv.psmap.model.bean.TeamPushData
import com.google.gson.Gson
import com.txzing.sdk.TeamManager
import com.txzing.sdk.bean.DestinationMsg
import com.txzing.sdk.bean.DisbandMsg
import com.txzing.sdk.bean.ExitCallMsg
import com.txzing.sdk.bean.HistoryFriendResponse
import com.txzing.sdk.bean.InviteMsg
import com.txzing.sdk.bean.JoinCallMsg
import com.txzing.sdk.bean.JoinTeamMsg
import com.txzing.sdk.bean.KickedMsg
import com.txzing.sdk.bean.ModifyRemarkMsg
import com.txzing.sdk.bean.OnlineMsg
import com.txzing.sdk.bean.PositionMsg
import com.txzing.sdk.bean.QuitMsg
import com.txzing.sdk.bean.TeamForbiddenMsg
import com.txzing.sdk.bean.TeamInfoResponse
import com.txzing.sdk.bean.TransferTeamMsg
import com.txzing.sdk.bean.UserForbiddenMsg
import com.txzing.sdk.bean.UserInfo
import com.txzing.sdk.interfaces.AudioFocusCallback
import com.txzing.sdk.interfaces.HttpCallback
import com.txzing.sdk.interfaces.ServeEventCallback
import com.txzing.sdk.interfaces.StreamCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 组队主界面业务
 */
@Singleton
class CustomTeamBusiness @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val userGroupBusiness: UserGroupBusiness,
    private val pushMessageBusiness: PushMessageBusiness,
    private val groupObserverBusiness: GroupObserverBusiness,
    private val mINaviRepository: INaviRepository,
    private val mapBusiness: MapBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val settingComponent: ISettingComponent,
    private val netWorkManager: NetWorkManager,
    private val mLocationBusiness: LocationBusiness,
    private val permissionReqController: PermissionReqController,
    private val app: Application,
    private val gson: Gson,
) {
    //使用JT的用户id换取TXZ的用户id
    private val _userId = MutableLiveData(0)
    val userId: LiveData<Int> = _userId

    //捷途个人中心账号id
    private val _jtUserId = MutableLiveData(0)
    val jtUserId: LiveData<Int> = _jtUserId

    //队伍信息
    private val _teamInfo = MutableLiveData<TeamInfoResponse?>(null)
    val teamInfo: LiveData<TeamInfoResponse?> = _teamInfo

    //队友信息
    private val _userInfoList = MutableLiveData<List<UserInfo>?>(null)
    val userInfoList: LiveData<List<UserInfo>?> = _userInfoList

    //个人信息
    private val _userInfo = MutableLiveData<UserInfo?>(null)
    val userInfo: LiveData<UserInfo?> = _userInfo

    //对讲队伍信息
    private val _joinCallList = MutableLiveData<List<UserInfo>?>(null)
    val joinCallList: LiveData<List<UserInfo>?> = _joinCallList

    //邀请队友信息
    private val _inviteUserInfoList = MutableLiveData<List<UserInfo>?>(null)
    val inviteUserInfoList: LiveData<List<UserInfo>?> = _inviteUserInfoList

    //移除队友列表
    private var _removeUserInfoList = MutableLiveData<List<UserInfo>?>(null)
    val removeUserInfoList: LiveData<List<UserInfo>?> = _removeUserInfoList

    //历史好友
    private val _historyFriend = MutableLiveData<List<CustomGroupMember?>?>(null)
    val historyFriend: LiveData<List<CustomGroupMember?>?> = _historyFriend

    //历史好友类型 0加载中 1获取成功无数据  2获取成功有数据 3失败
    private val _inviteType = MutableLiveData(0)
    val inviteType: LiveData<Int> = _inviteType

    //用户退出状态 0无 1被踢出队伍 2解散队伍
    private val _userKickedType = MutableLiveData(BaseConstant.GROUP_EXIT_DEFAULT_TYPE)
    val userKickedType: LiveData<Int> = _userKickedType

    //是否能发出邀请
    private val _inviteSelectable = MutableLiveData(false)
    val inviteSelectable: LiveData<Boolean> = _inviteSelectable


    //是否加入对讲
    private val _isJoinCall = MutableLiveData(false)
    val isJoinCall: LiveData<Boolean> = _isJoinCall

    //是否全队禁言
    private val _isAllForbidden = MutableLiveData(false)
    val isAllForbidden: LiveData<Boolean> = _isAllForbidden

    //是否自己被禁言
    private val _isMineForbidden = MutableLiveData(false)
    val isMineForbidden: LiveData<Boolean> = _isMineForbidden

    //是否全员免打扰
    private val _isAllSpeakerMute = MutableLiveData(false)
    val isAllSpeakerMute: LiveData<Boolean> = _isAllSpeakerMute

    //是否开启方控辅助
    private val _isWheelAssist = MutableLiveData(false)
    val isWheelAssist: LiveData<Boolean> = _isWheelAssist

    private val _srsToken = MutableLiveData<String>(null)
    val srsToken: LiveData<String> = _srsToken

    val setToast = MutableLiveData<String>() //toast显示

    private var getTeamJob: Job? = null

    /**
     * 禁言状态 0: 正常，1: 禁言
     */
    private final val USER_STATUS_NORMAL = 0
    private final val USER_STATUS_FORBIDDEN = 1


    private var mIsFocus = false //是否为焦点
    private var isFirst = true //是否第一次进我的组队页面
    private var isFirstAddAGroupMembers = true //是否第一次添加车队
    var mCurrentId = "" //当前选中的队友id
    private var mTeamId: String? = null
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
    private var routeNaviMembers = ArrayList<GroupMember>()
    val showPreview = MutableLiveData<Boolean>()

    //是否为队长
    private val _isLeader = MutableLiveData(false)
    val isLeader: LiveData<Boolean> = _isLeader

    //是否显示移除队友布局
    private val _isRemoveMemberShow = MutableLiveData(false)
    val isRemoveMemberShow: LiveData<Boolean> = _isRemoveMemberShow

    val mUserId = MutableLiveData<String>()
    val mMembersFocusList = ArrayList<Boolean>()

    private val _mPOI = MutableLiveData<POI?>() //目的地
    val mPOI: LiveData<POI?> = _mPOI

    private val _mMemberShow = MutableLiveData(false)
    val mMemberShow: LiveData<Boolean> = _mMemberShow

    private val _isShowBackCar = MutableLiveData(false)
    val isShowBackCar: LiveData<Boolean> = _isShowBackCar

    //队伍口令密码
    private val _teamPassword = MutableLiveData("")
    val teamPassword: LiveData<String> = _teamPassword

    //通话时长
    private val _startCallTimne = MutableLiveData("")
    val startCallTimne: LiveData<String> = _startCallTimne

    //我的队伍--title
    private val _myTeamStr = MutableLiveData("0")
    val myTeamStr: LiveData<String> = _myTeamStr

    //对讲频道--title
    private val _joinCallStr = MutableLiveData("0")
    val joinCallStr: LiveData<String> = _joinCallStr

    //我的设置--title
    private val _teamSettingStr = MutableLiveData("")
    val teamSettingStr: LiveData<String> = _teamSettingStr

    //显示设置布局
    private val _showTeamSetting = MutableLiveData(false)
    val showTeamSetting: LiveData<Boolean> = _showTeamSetting

    //显示对讲频道
    private val _showJoinCall = MutableLiveData(false)
    val showJoinCall: LiveData<Boolean> = _showJoinCall

    //显示昵称布局
    private val _showNickname = MutableLiveData(false)
    val showNickname: LiveData<Boolean> = _showNickname

    //移除队员一栏是否显示
    private val _captainRemoveLayout = MutableLiveData(false)
    val captainRemoveLayout: LiveData<Boolean> = _captainRemoveLayout

    //显示退出组队，还是解散组队
    private val _teamDisbandStr = MutableLiveData("")
    val teamDisbandStr: LiveData<String> = _teamDisbandStr

    //修改昵称
    private val _nicknameStr = MutableLiveData("")
    val nicknameStr: LiveData<String> = _nicknameStr

    //我的昵称
    private val _myNickname = MutableLiveData("")
    val myNickname: LiveData<String> = _myNickname

    //显示移除队友布局
    private val _showRemovePlayer = MutableLiveData(false)
    val showRemovePlayer: LiveData<Boolean> = _showRemovePlayer

    //是否可以编辑目的地
    private val _setDestination = MutableLiveData(false)
    val setDestination: LiveData<Boolean> = _setDestination

    //没有目的地--队长显示
    private val _noDestination = MutableLiveData(false)
    val noDestination: LiveData<Boolean> = _noDestination

    //编辑地址按钮是否显示
    private val _showEdit = MutableLiveData(false)
    val showEdit: LiveData<Boolean> = _showEdit

    //目的地名称
    private val _mPOIName = MutableLiveData("")
    val mPOIName: LiveData<String> = _mPOIName

    //关闭组队界面
    private val _finishFragment = MutableLiveData(false)
    val finishFragment: LiveData<Boolean> = _finishFragment

    //更新队伍中某个人信息
    private val _updateMembersData = MutableLiveData<Int>()
    val updateMembersData = _updateMembersData

    //更新队伍列表信息
    private val _updateALLMembersData = MutableLiveData("")
    val updateALLMembersData: LiveData<String> = _updateALLMembersData

    //更新删除队友列表数据
    private val _updateRemoveMembers = MutableLiveData<ArrayList<GroupMember>>()
    val updateRemoveMembers = _updateRemoveMembers

    //更新组队目的地
    private val _updateGroupDestination = MutableLiveData(false)
    val updateGroupDestination: LiveData<Boolean> = _updateGroupDestination

    //组队相关请求是否成功
    private val _updateGroupResponseResult = MutableLiveData(false)
    val updateGroupResponseResult: LiveData<Boolean> = _updateGroupResponseResult

    //是否邀请成功
    private val _inviteIsSuccess = MutableLiveData<Boolean>()
    val inviteIsSuccess: LiveData<Boolean> = _inviteIsSuccess

    //队长转让消息
    private val _teamTransfer = MutableLiveData("")
    val teamTransfer: LiveData<String> = _teamTransfer

    //加入队伍结果
    private val _joinTeamResult = MutableLiveData(false)
    val joinTeamResult: LiveData<Boolean> = _joinTeamResult

    //创建队伍结果
    private val _createTeamResult = MutableLiveData(false)
    val createTeamResult: LiveData<Boolean> = _createTeamResult

    //修改昵称结果
    private val _modifyRemarkResult = MutableLiveData(false)
    val modifyRemarkResult: LiveData<Boolean> = _modifyRemarkResult


    val updateGroupDestinationSuccess = groupObserverBusiness.updateGroupDestinationSuccess //组队设置目的地成功
    var lastGroupResponseInfo: GroupResponseInfo? = null //上一次数据

    // 通话开始时间戳
    private val _startCallTime = MutableLiveData<Long?>()
    val startCallTime: MutableLiveData<Long?> get() = _startCallTime

    // 通话时长显示
    private val _callDuration = MutableLiveData<String>()
    val callDuration: MutableLiveData<String> get() = _callDuration

    // 显示通话弹窗
    private val _showFloatingView = MutableLiveData(false)
    val showFloatingView: LiveData<Boolean> = _showFloatingView

    // 显示邀请组队弹条
    private val _showPushCardView = MutableLiveData<TeamPushData>()
    val showPushCardView: LiveData<TeamPushData> = _showPushCardView

    // 用于更新通话时长的 Handler
    private val durationHandler = Handler(Looper.getMainLooper())
    private val durationRunnable = object : Runnable {
        override fun run() {
            updateCallDuration()
            durationHandler.postDelayed(this, 1000) // 每秒更新一次
        }
    }

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
                _isShowBackCar.postValue(true)
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

    fun initData() {
        isFirst = true
        if (!isServeConnected()) connectServe()

        clearAllItems() //移除组队图层扎标
        getTeamByUserId()
        _mMemberShow.postValue(false)
        userGroupBusiness.removeClickObserver(iLayerClickObserver)
        userGroupBusiness.addClickObserver(iLayerClickObserver)
        userGroupBusiness.addGestureObserver(mapGestureObserver) //注册图层手势监听
        doStartPositionTimer() //启动定时器

//        userGroupBusiness.aMapLayer?.showFlyLine(false)
        userGroupBusiness.aMapLayer?.setCarVisible(false)
        userGroupBusiness.aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
        userGroupBusiness.aGroupLayer?.setVisible(BizAGroupType.BizAGroupTypeAGroup.toLong(), true)
        userGroupBusiness.aGroupLayer?.setVisible(BizAGroupType.BizAGroupTypeEndPoint.toLong(), true)
        userGroupBusiness.aUserBehaviorLayer?.setVisible(BizUserType.BizUserTypeFavoriteMain.toLong(), false) //隐藏用户收藏点扎标
    }

    fun onCleared() {
        _mMemberShow.postValue(false)
        userGroupBusiness.removeGestureObserver(mapGestureObserver) //取消注册图层手势监听
        backToCarTimer?.cancel() // 在ViewModel销毁时停止定时器

        isFirst = true
        doStopPositionTimer() //取消定时器
        if (userGroupBusiness.aGroupLayer != null) {
            removeItem() //删除自己地图上的扎点
        }
        if (userGroupBusiness.aGroupLayer != null)
            userGroupBusiness.removeClickObserver(iLayerClickObserver)

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
//            updateAGroupMember(userInfoList.value, false, -1);
            userGroupBusiness.aMapLayer?.setCarVisible(false)
            userGroupBusiness.aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
            userGroupBusiness.aUserBehaviorLayer?.setVisible(BizUserType.BizUserTypeFavoriteMain.toLong(), false)
            doStartPositionTimer() //启动定时器
        } else {
            Timber.i("onHiddenChanged hidden")
            doStopPositionTimer() //取消定时器
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
            _updateMembersData.postValue(position)//更新队伍中某个人信息
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
            _updateMembersData.postValue(lastSelectPosition)//更新队伍中某个人信息
        }
    }

    /**
     * 设置初始化队伍信息
     */
    fun setGroupData(teamInfoResponse: TeamInfoResponse?, list: List<UserInfo>?) {
        if (teamInfoResponse == null || list.isNullOrEmpty()) {
            _mMemberShow.postValue(false)
            return
        }

        val joinCallList = list.filter { u: UserInfo -> u.voice_status == 1 }
        //标题栏内容显示
        if (pageType == BaseConstant.GROUP_DEFAULT_TYPE) {
            _myTeamStr.postValue(list.size.toString())
            _teamSettingStr.postValue("")
        } else if (pageType == BaseConstant.GROUP_START_CALL_TYPE) {
            _joinCallStr.postValue(joinCallList.size.toString())
            _teamSettingStr.postValue("")
        }

        //个人信息
        val userInfo = list.find { it.user_id == userId.value }
        _userInfo.postValue(userInfo)
        val name = userInfo?.run {
            when {
                remark.isNotEmpty() -> remark
                nick_name.isNotEmpty() -> nick_name
                else -> ""
            }
        } ?: ""
        _myNickname.postValue(name)

        //队伍是否禁言
        _isAllForbidden.postValue(teamInfoResponse.all_forbidden == 1)
        _isMineForbidden.postValue(userInfo?.status == 1)

        //队伍口令
        _teamPassword.postValue(teamInfoResponse.code)

        //是否为队长
        val isLeader = teamInfoResponse.master_user_id == userId.value
        _isLeader.postValue(isLeader)

        //添加队友加入队伍item
        val member = UserInfo()
        member.nick_name = "添加"
        member.user_id = 999
        val membersList = ArrayList<UserInfo>()
        membersList.add(member)
        membersList.addAll(list)

        Timber.i("setGroupData mSelectPosition:$mSelectPosition")
        mMembersFocusList.clear() //清空选中列表
        for (i in membersList.indices) {
            if (i == mSelectPosition) {
                mMembersFocusList.add(i, true)
            } else {
                mMembersFocusList.add(i, false)
            }
        }

        _userInfoList.postValue(list) //设置我的队伍队友列表
        _inviteUserInfoList.postValue(membersList) //设置我的队伍邀请列表
        _joinCallList.postValue(joinCallList) //设置我的队伍对讲列表
//        userGroupBusiness.setMembersList(teamInfoResponse, list, userId.value)

        _mMemberShow.postValue(true)

        if (teamInfoResponse.address_name.isNotEmpty()) {
            Timber.i("setDestination setGroupData destination")
            //目的地坐标
            if (teamInfoResponse.lon.isNotEmpty() && teamInfoResponse.lat.isNotEmpty()) {
                mDesLon = teamInfoResponse.lon.toDouble()
                mDesLat = teamInfoResponse.lat.toDouble()
//                setEndPoint(mDesLon, mDesLat, 0)
            } else {
                mDesLon = 0.0
                mDesLat = 0.0
            }

            val poi = POI()
            poi.name = teamInfoResponse.address_name
            poi.point = GeoPoint(teamInfoResponse.lon.toDouble(), teamInfoResponse.lat.toDouble())
            poi.addr = teamInfoResponse.address_info
            _mPOI.postValue(poi)
            _mPOIName.postValue("目的地：" + poi.name)
        } else {
            _mPOI.postValue(null)
            _mPOIName.postValue("")
        }
    }


    private fun goRemoveMember() {
        _removeUserInfoList.postValue(null)
        val userList = userInfoList.value
        userList?.filter { it.user_id != userId.value }.let { removeUserList ->
            if (removeUserList?.isNotEmpty() == true) {
                _removeUserInfoList.postValue(removeUserList)
                _isRemoveMemberShow.postValue(true)
            } else {
                _isRemoveMemberShow.postValue(false)
            }
        }
    }


    /**
     * 更新车队信息
     *//*
    private fun updateAGroupMember(members: List<UserInfo>?, updateOne: Boolean, mPosition: Int) {
        Timber.d("updateAGroupMember members:${gson.toJson(members)} updateOne:$updateOne mPosition:$mPosition")
        if (members.isNullOrEmpty()) {
            return
        }
        userGroupBusiness.aGroupLayer?.updateStyle()
        *//*if (userGroupBusiness.checkDifferent(userInfoList.value ?: listOf(), members)) { //已有组队图层不重复添加
            Timber.d("addAGroupMembers userGroupBusiness.checkDifferent(mMembersList, members)")
            userGroupBusiness.aGroupLayer?.updateStyle()
            return
        }*//*

        var isUpdateOne = updateOne
        Timber.d("updateTimes：$updateTimes mPosition:$mPosition")
        if (updateTimes == 60) {
            userGroupBusiness.aGroupLayer?.clearAllItems()
            updateTimes = 0
//            userGroupBusiness.syncUpdate(members) //更新队伍信息--列表
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
            member.id = members[mPosition].user_id.toString()
            member.priority = mPosition
            member.mPos3D.lon = members[mPosition].latest_lon.toDouble()
            member.mPos3D.lat = members[mPosition].latest_lat.toDouble()
            userGroupBusiness.getGroupHeadUpdateAGroupMember(members[mPosition].head_img, defaultHead, member)
        } else {
            //添加车队
            if (members == null || members.size <= 0) {
                Timber.i(" updateAGroupMember members == null || members.size <= 0 ")
            } else {
                for (i in members.indices) {
                    val member = BizAGroupBusinessInfo()
                    member.id = members[i].user_id.toString()
                    member.priority = i
                    member.mPos3D.lon = members[i].latest_lon.toDoubleOrNull() ?: 0.0
                    member.mPos3D.lat = members[i].latest_lat.toDoubleOrNull() ?: 0.0
                    userGroupBusiness.getGroupHeadUpdateAGroupMember(members[i].head_img, defaultHead, member)
                }
            }
        }
        if (isFirst) {
            isFirst = false
            showPreview.postValue(true) //地图扎点在右侧  全览
        }
        updateTimes++
    }*/

    /**
     * 组队预览
     */
    fun aGroupShowPreview() {
        val members = userInfoList.value
        if (!members.isNullOrEmpty()) {
            val points = ArrayList<PointD>()
            for (member in members) {
                val pointD = PointD()
                pointD.x = member.latest_lon.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().longitude
                pointD.y = member.latest_lat.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().latitude
                points.add(pointD)
            }

            if (mDesLon != 0.0 && mDesLat != 0.0) {
                val pointD = PointD()
                pointD.x = mDesLon
                pointD.y = mDesLat
                points.add(pointD)
            }

            Timber.d("aGroupShowPreview members:${gson.toJson(members)} points:${gson.toJson(points)}")

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
    }

    /**
     * 组队poi放大
     */
    fun setPoiFocus(id: String, index: Int, isFocus: Boolean) {
        Timber.i("setPoiFocus id:$id index:$index isFocus:$isFocus")
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
        if (type == 1) {
            showPreview.postValue(true) //地图扎点在右侧  全览
        }
        userGroupBusiness.aGroupLayer?.setEndPoint(lon, lat)
    }

    private fun setMapCenter(index: Int) {
        Timber.i("setMapCenter index:$index")
        val inviteUserSize = inviteUserInfoList.value?.size ?: 0
        val userInfoList = userInfoList.value
        var mIndex = index
        if (mIndex >= inviteUserSize) {
            mIndex = inviteUserSize - 1
        }
        if (mIndex == -1) {
            mIndex = 0
        }
        Timber.i("setMapCenter inviteUserSize:$inviteUserSize mIndex:$mIndex userInfoList.size:${userInfoList?.size}")
        if (!userInfoList.isNullOrEmpty() && userInfoList.size > mIndex) {
            mapBusiness.setMapCenter(
                userInfoList[mIndex].latest_lon.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().longitude,
                userInfoList[mIndex].latest_lat.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().latitude
            )
        }
    }

    //导航中15s无操作，去掉选中状态
    fun setNavingFocus() {
        userGroupBusiness.aGroupLayer?.clearFocus(BizAGroupType.BizAGroupTypeAGroup.toLong())
        setSelectedMemberFalse()
        mSelectPosition = -1
    }

    /**
     * 修改队伍属性  目的地设置
     */
    fun reqUpdateDestination(poi: POI) {
        Timber.d("reqUpdateDestination poi:${gson.toJson(poi)}")
        groupScope.launch {
            delay(300)
            if (netWorkManager.isNetworkConnected()) {
                _updateGroupDestination.postValue(true)
                delay(300)
                setTeamDestination(poi.point.longitude.toString(), poi.point.latitude.toString(), poi.name, poi.addr)
            } else {
                setToast.postValue(ResUtil.getString(com.autosdk.R.string.agroup_set_destination_no_network))
            }
        }
    }

    /**
     * 更新队友和图层信息
     * int position, TeamMember teamMember
     */
    /*private fun updateMember(mPoistion: Int) {
        groupScope.launch {
            _mMemberShow.postValue(true)
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
//            userGroupBusiness.syncUpdate(routeNaviMembers) //更新队伍信息--列表
            updateAGroupMember(routeNaviMembers, true, mPoistion)
        }
    }*/

    //设置mMembersFocusList数据都为false
    fun setListDataFalse() {
        for (i in mMembersFocusList.indices) {
            mMembersFocusList[i] = false
        }
    }

    //队伍列表点击
    fun onItemClick(id: String, position: Int) {
        Timber.i("onItemClick id:$id position:$position")
        lastSelectPosition = position
        setPoiFocus(id, position - 1, true)
        setSelectedMember(position)
        userGroupBusiness.aMapLayer?.setFollowMode(false)
        resetBackToCarTimer()
    }

    fun toGetPageType(): Int {
        return pageType
    }

    //点击返回键操作
    fun doBack() {
        Timber.i("doBack pageType:$pageType")
        when (pageType) {
            BaseConstant.GROUP_SETTING_TYPE,
            BaseConstant.GROUP_START_CALL_TYPE,
            BaseConstant.GROUP_CHANGE_USERNAME_TYPE -> {
                doBackMyTeam()
            }

            BaseConstant.GROUP_REMOVE_MEMBERS_TYPE -> {
                doBackRemovePlayer()
            }

            BaseConstant.GROUP_START_CALL_SETTING_TYPE,
            BaseConstant.GROUP_START_CALL_CHANGE_USERNAME_TYPE
            -> {
                tvJoinCall()
            }

        }
    }

    //退出组队界面时，回到默认布局，下次进入组队界面显示默认布局
    fun defaultPage() {
        pageType = BaseConstant.GROUP_DEFAULT_TYPE
        _teamSettingStr.postValue("")
        _showTeamSetting.postValue(false)
        _setDestination.postValue(false)
        _showJoinCall.postValue(false)
        _noDestination.postValue(false)
        mHidden = false
        lastGroupResponseInfo = null
    }

    //设置按钮点击操作
    fun doSettingIcon() {
        pageType = if (pageType == BaseConstant.GROUP_DEFAULT_TYPE) {
            BaseConstant.GROUP_SETTING_TYPE
        } else {
            BaseConstant.GROUP_START_CALL_SETTING_TYPE
        }
        _myTeamStr.postValue("")
        _joinCallStr.postValue("")
        _teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_settings))
        _showTeamSetting.postValue(true)
        _showJoinCall.postValue(false)
    }

    //回到自身位置
    fun btnBackCar() {
        val userInfoList = userInfoList.value
        _isShowBackCar.postValue(false)
        setPoiFocus(mCurrentId, -1, false) //取消地图选择
        setSelectedMemberFalse()
        mapBusiness.goToDefaultPosition(false)
        userInfo.value?.let { member ->
            mapBusiness.setMapCenter(
                member.latest_lon.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().longitude,
                member.latest_lat.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().latitude
            )
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
        pageType = if (pageType == BaseConstant.GROUP_DEFAULT_TYPE) {
            BaseConstant.GROUP_CHANGE_USERNAME_TYPE
        } else {
            BaseConstant.GROUP_START_CALL_CHANGE_USERNAME_TYPE
        }
        _myTeamStr.postValue("")
        _joinCallStr.postValue("")
        _teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_change_username))
        _showTeamSetting.postValue(false)
        _showNickname.postValue(true)
        _showJoinCall.postValue(false)
        _nicknameStr.postValue("")
    }

    //点击了 加入对讲
    fun tvJoinCall() {
        pageType = BaseConstant.GROUP_START_CALL_TYPE
        _myTeamStr.postValue("")
        _joinCallStr.postValue((userInfoList.value?.count { it.voice_status == 1 }?.takeIf { it > 0 }?.toString() ?: "")) //语音聊天状态 0:不在语音群聊，1:进入语音群聊
        _teamSettingStr.postValue("")
        _showTeamSetting.postValue(false)
        _showNickname.postValue(false)
        _showJoinCall.postValue(true)
        _nicknameStr.postValue("")
    }

    //点击了 移除组员的返回
    fun doBackRemovePlayer() {
        pageType = BaseConstant.GROUP_SETTING_TYPE
        _myTeamStr.postValue("")
        _teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_player_settings))
        _showTeamSetting.postValue(true)
        _showNickname.postValue(false)
        _showRemovePlayer.postValue(false)
    }

    //返回到我的队伍主页
    fun doBackMyTeam() {
        pageType = BaseConstant.GROUP_DEFAULT_TYPE
        _myTeamStr.postValue((userInfoList.value?.size?.takeIf { it > 0 }?.toString() ?: ""))
        _teamSettingStr.postValue("")
        _showTeamSetting.postValue(false)
        _showNickname.postValue(false)
        _showJoinCall.postValue(false)
        if (isJoinCall.value == true) _showFloatingView.postValue(true)
    }

    //点击移除组员
    fun doRemoveMember() {
        pageType = BaseConstant.GROUP_REMOVE_MEMBERS_TYPE
        _myTeamStr.postValue("")
        _teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_remove_members_title))
        _showTeamSetting.postValue(false)
        _showNickname.postValue(false)
        _showRemovePlayer.postValue(true)
        goRemoveMember()
    }

    //设置昵称--点击确定按钮 确定修改昵称
    fun tvOk(nickname: String) {
        when {
            !netWorkManager.isNetworkConnected() -> {
                setToast.postValue(ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
            }

            nickname.isNullOrBlank() -> {
                setToast.postValue(ResUtil.getString(R.string.sv_group_main_set_nickname_tip_empty))
            }

            nickname == getMyNickName() -> {
                setToast.postValue(ResUtil.getString(R.string.sv_group_main_set_same_nickname))
            }

            else -> {
                modifyRemark(nickname)
            }
        }
    }

    //解散或者退出队伍
    fun quitOrDismiss() {
        if (isLeader.value == true) {
            disbandTeam() //解散队伍
        } else {
            quitTeam() //退出队伍
        }
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
        userGroupBusiness.aGroupLayer?.setVisible(true)
    }

    //判断是否为队长，显示对应布局
    fun judeLeaderShowLayout(isLeader: Boolean) {
        if (isLeader) {
            //是队长
            _captainRemoveLayout.postValue(true)
            _teamDisbandStr.postValue(ResUtil.getString(R.string.sv_group_disband_the_team))
        } else {
            //队员
            _captainRemoveLayout.postValue(false)
            _teamDisbandStr.postValue(ResUtil.getString(R.string.sv_group_leave_the_team))
        }
    }

    fun setNickname(name: String) {
        _nicknameStr.postValue(name)
    }

    //判断显示目的地横条
    fun judeHasDestination(poi: POI?) {
        if (poi != null) {
            _setDestination.postValue(true)
            _noDestination.postValue(false)
            if (isLeader.value!!) {
                _showEdit.postValue(true)
            } else {
                _showEdit.postValue(false)
            }
        } else if (isLeader.value == true) {
            _setDestination.postValue(false)
            _noDestination.postValue(true)
        } else {
            _setDestination.postValue(false)
            _noDestination.postValue(false)
        }
    }

    fun getMembers(): ArrayList<GroupMember> {
        return mMembers
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

    /**
     * 清空组队图层
     */
    fun clearAllItems() {
        isFirstAddAGroupMembers = true
        userGroupBusiness.clearAllItems()
    }

    //同行SDK适配接口
    private val serveEventCallback = object : ServeEventCallback {
        override fun onConnected() {
            Timber.i("serveEventCallback onConnected 连接成功")
            getTeamByUserId()
        }

        override fun onFailure(code: Int, msg: String) {
            Timber.i("serveEventCallback onFailure 连接失败 code=$code,msg=$msg")
        }

        override fun onDisconnected(code: Int, msg: String) {
            Timber.i("serveEventCallback onDisconnected 断开连接 code=$code,msg=$msg")
            if (isJoinCall.value == true) {
                _isJoinCall.postValue(false)
                TeamManager.getInstance().removeAllCallUser()
            }
        }

        override fun onDestinationChange(msg: DestinationMsg) {
            Timber.i("serveEventCallback onDestinationChange 队伍目的地变化 msg=${gson.toJson(msg)}")
            mDesLon = msg.lon.toDouble()
            mDesLat = msg.lat.toDouble()
            getTeamByUserId()
            _updateGroupResponseResult.postValue(true)
            _showPushCardView.postValue(
                TeamPushData(
                    ResUtil.getString(com.autosdk.R.string.message_leader_set_des),
                    msg.address_name,
                    ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_goto),
                    poi = POI().apply {
                        name = msg.address_name
                        point = GeoPoint(msg.lat.toDouble(), msg.lon.toDouble())
                        addr = msg.address_info
                    }
                )
            )
        }

        override fun onUserPositionChange(msg: PositionMsg) {
            Timber.i("serveEventCallback onUserPositionChange 用户位置变化变化 msg=${gson.toJson(msg)}")
            if (msg.team_id == teamInfo.value?.team_id) {
                userGroupBusiness.updateAGroupPosition(teamInfo.value, userInfoList.value, msg.user_position, userId.value)
            }
        }

        override fun onTeamDisband(msg: DisbandMsg) {
            Timber.i("serveEventCallback onTeamDisband 队伍解散 msg=${gson.toJson(msg)}")
            if (isLeader.value == true) {
                setToast.postValue(ResUtil.getString(R.string.sv_group_team_disband_success))
                leaveTeam()
            } else {
                _userKickedType.postValue(BaseConstant.GROUP_EXIT_TEAM_DISBAND_TYPE)
                _showPushCardView.postValue(
                    TeamPushData(
                        ResUtil.getString(com.autosdk.R.string.organizeteam_text_bar),
                        ResUtil.getString(com.autosdk.R.string.message_team_dismiss_group_tips),
                        ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_got_it)
                    )
                )
                leaveTeam(false)
            }
        }

        override fun onUserKicked(msg: KickedMsg) {
            Timber.i("serveEventCallback onUserKicked 用户被踢出队伍 msg=${gson.toJson(msg)}")
            val removeList = ArrayList<UserInfo>(removeUserInfoList.value ?: ArrayList())
            for (user in msg.user_infos) {
                if (user.user_id == userId.value) {
                    if (isJoinCall.value == true) {
                        TeamManager.getInstance().removeAllCallUser()
                    }
                    _userKickedType.postValue(BaseConstant.GROUP_EXIT_USER_KICK_TYPE)
                    _showPushCardView.postValue(
                        TeamPushData(
                            ResUtil.getString(com.autosdk.R.string.organizeteam_text_bar),
                            ResUtil.getString(com.autosdk.R.string.message_team_user_kick_tips),
                            ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_got_it)
                        )
                    )
                    leaveTeam(false)
                    break
                } else {
                    TeamManager.getInstance().removeCallUser(user.user_id)
                    removeList.removeIf { u: UserInfo -> u.user_id == user.user_id }
                    Timber.i("serveEventCallback onUserKicked removeList=${gson.toJson(removeList)}")
                }
            }
            _removeUserInfoList.postValue(removeList)
            Timber.i("serveEventCallback onUserKicked removeUserInfoList=${gson.toJson(removeUserInfoList.value)}")
            userGroupBusiness.aGroupLayer?.clearAllItems()
            if (removeList.isEmpty()) {
                _isRemoveMemberShow.postValue(false)
            }
            setPoiFocus(mCurrentId, -1, false) //取消地图选择
            isFirst = true
            getTeamByUserId() //获取队伍信息
        }

        override fun onUserForbiddenChange(msg: UserForbiddenMsg) {
            Timber.i("serveEventCallback onUserForbiddenChange 用户禁言变化 msg=${gson.toJson(msg)}")
            if (msg.user_id == userId.value) {
                TeamManager.getInstance().setMicrophoneMute(msg.isIs_forbidden, object : AudioFocusCallback {
                    override fun onAudioFocusChange(p0: Int) {
                        Timber.i("serveEventCallback setMicrophoneMute onAudioFocusChange p0=$p0")
                    }
                })
                _isMineForbidden.postValue(msg.isIs_forbidden)
            }
            getTeamByUserId(false)
        }

        override fun onTeamForbiddenChange(msg: TeamForbiddenMsg) {
            Timber.i("serveEventCallback onTeamForbiddenChange 队伍禁言变化 msg=${gson.toJson(msg)}")
            TeamManager.getInstance().setMicrophoneMute(msg.isIs_forbidden, object : AudioFocusCallback {
                override fun onAudioFocusChange(p0: Int) {
                    Timber.i("serveEventCallback setMicrophoneMute onAudioFocusChange p0=$p0")
                }

            })
            _isAllForbidden.postValue(msg.isIs_forbidden)
            if (isLeader.value != true) _isMineForbidden.postValue(msg.isIs_forbidden)
            getTeamByUserId(false)
        }

        override fun onOnlineStatusChange(msg: OnlineMsg) {
            Timber.i("serveEventCallback onOnlineStatusChange 用户在线状态变化 msg=${gson.toJson(msg)}")

            historyFriend.value?.let { friends ->
                friends.forEach { friend ->
                    friend?.apply {
                        if (user_id == msg.user_id) {
                            isOnline_status = msg.isOnline_status
                        }
                    }
                }
                _historyFriend.postValue(friends)
            }

            getTeamByUserId()
        }

        override fun onUserQuit(msg: QuitMsg) {
            Timber.i("serveEventCallback onUserQuit 用户退出队伍 msg=${gson.toJson(msg)}")
            if (msg.user_id == userId.value) {
                if (isJoinCall.value == true) {
                    TeamManager.getInstance().removeAllCallUser()
                }
                leaveTeam()
            } else {
                TeamManager.getInstance().removeCallUser(msg.user_id)
                getTeamByUserId()
            }
        }

        override fun onInviteTeam(msg: InviteMsg) {
            Timber.i("serveEventCallback onInviteTeam 邀请加入队伍 msg=${gson.toJson(msg)}")
            _showPushCardView.postValue(
                TeamPushData(
                    msg.nick_name,
                    ResUtil.getString(com.autosdk.R.string.message_invite_text),
                    ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_join),
                    msg.code
                )
            )
        }

        override fun onUserJoinCall(msg: JoinCallMsg) {
            Timber.i("serveEventCallback onUserJoinCall 进入聊天 msg=${gson.toJson(msg)}")
            if (msg.user_id == userId.value) { //判断是否为自己加入群聊
                _isJoinCall.postValue(true)
                if (!TextUtils.isEmpty(msg.srs_token)) {
                    _srsToken.postValue(msg.srs_token)
                }
                // 记录通话开始时间
                _startCallTime.postValue(msg.time_stamp)
                // 开始更新通话时长
                durationHandler.post(durationRunnable)
                val userInfoList: List<Int> = userInfoList.value?.filter { u: UserInfo -> u.voice_status == 1 }?.map { obj: UserInfo -> obj.user_id }
                    ?: ArrayList()
                TeamManager.getInstance().addAllCallUser(userInfoList, teamInfo.value?.team_id ?: 0, userId.value ?: 0, srsToken.value)
                tvJoinCall()
                getTeamByUserId()
            } else {
                if (isJoinCall.value == true) {
                    TeamManager.getInstance().addCallUser(msg.user_id, teamInfo.value?.team_id ?: 0, userId.value ?: 0, srsToken.value)
                    getTeamByUserId()
                }
            }
        }

        override fun onUserExitCall(msg: ExitCallMsg) {
            Timber.i("serveEventCallback onUserExitCall 退出聊天 msg=${gson.toJson(msg)}")
            if (msg.user_id == userId.value) {
                _isJoinCall.postValue(false)
                // 停止更新通话时长
                durationHandler.removeCallbacks(durationRunnable)
                // 重置通话开始时间
                _startCallTime.postValue(null)
                doBack()
            }
        }

        override fun onTeamTransfer(msg: TransferTeamMsg) {
            Timber.i("serveEventCallback onTeamTransfer 成为队伍新队长 msg=${gson.toJson(msg)}")
            if (msg.new_master_user_id == userId.value) {
                val oldMaster = userInfoList.value?.find { it.user_id == msg.old_master_user_id }
                val oldMasterName = when {
                    !oldMaster?.remark.isNullOrEmpty() -> oldMaster?.remark
                    !oldMaster?.nick_name.isNullOrEmpty() -> oldMaster?.nick_name
                    else -> ""
                }
                Timber.i("serveEventCallback onTeamTransfer oldMaster =${gson.toJson(oldMaster)} oldMasterName=$oldMasterName")
                _teamTransfer.postValue(oldMasterName)
            }
            getTeamByUserId()
        }

        override fun onUserModifyRemark(msg: ModifyRemarkMsg) {
            Timber.i("serveEventCallback onUserModifyRemark 用户修改备注名 msg=${gson.toJson(msg)}")
            /*getTeamByUserId()
            pageType = BaseConstant.GROUP_SETTING_TYPE
            _myTeamStr.postValue("")
            _teamSettingStr.postValue(ResUtil.getString(R.string.sv_group_player_settings))
            _showTeamSetting.postValue(true)
            _showNickname.postValue(false)
            showPreview.postValue(true) //地图扎点在右侧  全览
            _nicknameStr.postValue("")*/
        }

        override fun onUserJoinTeam(msg: JoinTeamMsg) {
            Timber.i("serveEventCallback onUserJoinTeam 用户加入队伍 msg=${gson.toJson(msg)}")
            getTeamByUserId()
        }

    }

    fun initSDK(app: Application) {
        TeamManager.getInstance().init(app)
    }

    /**
     * 设置使用JT的用户id换取的TXZ用户id。
     * 该方法会将传入的用户id通过LiveData进行发布，以便观察者可以监听用户id的变化。
     *
     * @param id 要设置的TXZ用户id。
     */
    fun resetUserId() {
        _userId.postValue(0)
        _jtUserId.postValue(0)
    }

    /**
     * 使用JT用户ID登录同行SDK，并获取对应的TXZ用户ID。
     * 该方法调用 [TeamManager] 单例的 `loginUser` 方法进行用户登录操作，登录结果通过回调返回。
     * 若登录成功，会将获取到的TXZ用户ID通过 `_userId` 进行发布；若登录失败，则将 `_userId` 的值设为0。
     *
     * @param jtUserId JT用户的ID，必传参数。
     * @param userName 用户的名称，可选参数，默认为空字符串。
     * @param headImg 用户头像的URL，可选参数，默认为空字符串。
     */
    fun loginUser(jtUserId: String, userName: String = "", headImg: String = "") {
        Timber.i("loginUser jtUserId:$jtUserId userName:$userName headImg:$headImg")
        TeamManager.getInstance().loginUser(jtUserId, userName, headImg, object : HttpCallback<Int> {
            override fun onSuccess(userId: Int) {
                Timber.i("loginUser onSuccess userId:$userId")
                _userId.postValue(userId)
                _jtUserId.postValue(jtUserId.toInt())
                if (!isServeConnected()) connectServe(userId)
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("loginUser onFailure code:$code message:$message")
                _userId.postValue(0)
                _jtUserId.postValue(0)
            }

        })
    }

    /**
     * 根据用户ID获取队伍信息。
     * 首先检查当前用户ID是否有效，若有效且当前队伍信息为空，
     * 则调用 [TeamManager] 的 `getTeamByUserId` 方法获取队伍信息，
     * 并通过回调处理获取结果；若用户ID无效，则打印提示信息。
     */
    fun getTeamByUserId(updateAGroupMember: Boolean = true) {
        Timber.i("getTeamByUserId")
        getTeamJob?.cancel()
        getTeamJob = groupScope.launch {
            TeamManager.getInstance().getTeamByUserId(userId.value ?: 0, object : HttpCallback<TeamInfoResponse?> {
                override fun onSuccess(teamInfoResponse: TeamInfoResponse?) {
                    Timber.i("getTeamByUserId onSuccess teamInfoResponse:${gson.toJson(teamInfoResponse)}")
                    _teamInfo.postValue(teamInfoResponse)
                    if (teamInfoResponse != null) {
                        if (teamInfoResponse.team_id > 0) {
                            // 按条件排序：自己(0) → 队长(1) → 在线成员(2) → 离线成员(3)
                            val sortList = getSortList(teamInfoResponse)
                            setGroupData(teamInfoResponse, sortList)
                            doStartPositionTimer()
                            if (isFirstAddAGroupMembers) {
                                userGroupBusiness.addAGroupMembers(teamInfoResponse, sortList, userId.value)
                                userGroupBusiness.setEndPoint(mPOI.value)
                                isFirstAddAGroupMembers = false
                            } else if (updateAGroupMember) {
                                userGroupBusiness.updateAGroupInfo(teamInfoResponse, sortList, userId.value)
                            }
                        } else {
                            Timber.i("setOnGroupServiceObserver groupResponseInfo.team.teamId isEmpty")
                            doStopPositionTimer()
                            userGroupBusiness.showGroupBtn.postValue(false)
                            _finishFragment.postValue(true)
                        }
                    }
                }

                override fun onFailure(code: Int, message: String) {
                    Timber.i("getTeamByUserId onFailure code:$code message:$message")
                    _teamInfo.postValue(null)
                }
            })
        }
    }

    /**
     * 创建队伍的方法。
     * 首先检查当前用户的ID是否有效，若有效则调用 [TeamManager] 的 `createTeam` 方法创建队伍，
     * 并通过回调处理创建结果；若用户ID无效，则打印提示信息。
     */
    fun createTeam() {
        Timber.i("createTeam")
        TeamManager.getInstance().createTeam(userId.value ?: 0, "", "", "", object : HttpCallback<TeamInfoResponse> {
            override fun onSuccess(teamInfoResponse: TeamInfoResponse?) {
                Timber.i("createTeam onSuccess teamInfoResponse:${gson.toJson(teamInfoResponse)}")
                _teamInfo.postValue(teamInfoResponse)
                _createTeamResult.postValue(true)
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("createTeam onFailure code:$code message:$message")
                _teamInfo.postValue(null)
                _createTeamResult.postValue(false)
            }
        })
    }

    /**
     * 尝试使用给定的组队口令加入队伍。
     * 在执行加入操作前，会先检查用户是否已登录。若已登录，则调用 [TeamManager] 的 `joinTeam` 方法进行加入操作；
     * 若未登录，则提示用户需要登录企业账号。
     *
     * @param code 用于加入队伍的组队口令。
     */
    fun joinTeam(code: String) {
        Timber.i("joinTeam code:$code")
        TeamManager.getInstance().joinTeam(userId.value ?: 0, code, "", "", object : HttpCallback<TeamInfoResponse?> {
            override fun onSuccess(teamInfoResponse: TeamInfoResponse?) {
                Timber.i("joinTeam joinTeam teamInfoResponse:${gson.toJson(teamInfoResponse)}")
                _teamInfo.postValue(teamInfoResponse)
                _joinTeamResult.postValue(true)
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("joinTeam onFailure code:$code message:$message")
                _teamInfo.postValue(null)
                _joinTeamResult.postValue(false)
            }
        })
    }

    /**
     * 退出当前队伍的方法。
     * 首先检查用户是否已登录以及队伍信息是否存在，若条件满足则调用 [TeamManager] 的 `quitTeam` 方法进行退出操作，
     * 并通过回调处理退出结果。
     */
    fun quitTeam() {
        Timber.i("quitTeam")
        TeamManager.getInstance().quitTeam(userId.value ?: 0, teamInfo.value?.team_id ?: 0, object : HttpCallback<Boolean> {
            override fun onSuccess(aBoolean: Boolean) {
                Timber.i("quitTeam onSuccess aBoolean:$aBoolean ${if (aBoolean) "退出队伍成功" else "退出队伍失败"}")
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("quitTeam onFailure code:$code message:$message")
            }
        })
    }

    /**
     * 获取历史好友列表的方法。
     * 首先检查用户ID是否有效，若有效则调用 [TeamManager] 的 `getHistoryFriends` 方法获取历史好友列表，
     * 并通过回调处理获取结果；若用户ID无效，则打印提示信息。
     */
    fun getHistoryFriends() {
        Timber.i("getHistoryFriends")
        _inviteType.postValue(0)
        TeamManager.getInstance().getHistoryFriends(
            userId.value ?: 0,
            object : HttpCallback<List<HistoryFriendResponse?>?> {
                override fun onSuccess(historyFriendResponses: List<HistoryFriendResponse?>?) {
                    Timber.i("getHistoryFriends onSuccess historyFriendResponses:${gson.toJson(historyFriendResponses)}")
                    if (!historyFriendResponses.isNullOrEmpty()) {
                        val userInfoList = userInfoList.value
                        val historyFriendList = historyFriendResponses.map { convertToGroupMember(userInfoList, it) }
                        _historyFriend.postValue(historyFriendList)
                        _inviteType.postValue(2)
                    } else {
                        _historyFriend.postValue(null)
                        _inviteType.postValue(1)
                        Timber.i("getHistoryFriends onSuccess 没有历史好友")
                    }
                }

                override fun onFailure(code: Int, message: String) {
                    Timber.i("getHistoryFriends onFailure code:$code message:$message")
                    _historyFriend.postValue(null)
                    _inviteType.postValue(3)
                }
            })
    }

    /**
     * 解散队伍的方法。
     * 首先检查用户ID是否有效，若有效则调用 [TeamManager] 的 `disbandTeam` 方法解散队伍，
     * 并通过回调处理解散结果；若用户ID无效，则打印提示信息。
     */
    fun disbandTeam() {
        Timber.i("disbandTeam")
        TeamManager.getInstance().disbandTeam(userId.value ?: 0, object : HttpCallback<Boolean> {
            override fun onSuccess(aBoolean: Boolean) {
                Timber.i("disbandTeam onSuccess aBoolean:$aBoolean ${if (aBoolean) "解散队伍成功" else "解散队伍失败"}")
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("disbandTeam onFailure code:$code message:$message")
            }
        })
    }

    /**
     * 转让队伍队长权限的方法。
     * 首先检查当前用户ID是否有效，若有效则调用 [TeamManager] 的 `transferTeam` 方法将队伍队长权限转让给指定用户，
     * 并通过回调处理转让结果；若当前用户ID无效，则打印提示信息。
     *
     * @param otherUserId 接收队长权限的目标用户ID。
     */
    fun transferTeam(otherUserId: Int) {
        Timber.i("transferTeam otherUserId:$otherUserId")
        TeamManager.getInstance().transferTeam(otherUserId, userId.value ?: 0, object : HttpCallback<Boolean> {
            override fun onSuccess(aBoolean: Boolean) {
                Timber.i("transferTeam onSuccess aBoolean:$aBoolean ${if (aBoolean) "转让成功" else "转让失败"}")
                /*if (aBoolean) {
                    getTeamByUserId()
                }*/
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("transferTeam onFailure code:$code message:$message")
            }
        })
    }

    /**
     * 邀请指定用户加入队伍的方法。
     * 该方法会从当前队伍信息和用户信息中获取队伍 ID 和邀请者 ID，
     * 调用 [TeamManager] 的 `inviteMembers` 方法向指定用户发送邀请，
     * 并通过回调处理邀请结果。
     *
     * @param userIds 要邀请加入队伍的用户 ID 列表。
     */
    fun inviteMembers(userIds: List<Int>) {
        Timber.i("inviteMembers inviteMembers userIds:${gson.toJson(userIds)}")
        TeamManager.getInstance().inviteMembers(teamInfo.value?.team_id ?: 0, userId.value ?: 0, userIds, object : HttpCallback<Boolean> {
            override fun onSuccess(aBoolean: Boolean) {
                Timber.i("inviteMembers onSuccess aBoolean:$aBoolean ${if (aBoolean) "发送邀请成功" else "发送邀请失败"}")
                _inviteIsSuccess.postValue(aBoolean)
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("inviteMembers onFailure code:$code message:$message")
                _inviteIsSuccess.postValue(false)
            }
        })
    }

    /**
     * 修改队伍备注的方法。
     * 此方法会调用 [TeamManager] 的 `modifyRemark` 方法，为指定队伍设置备注信息。
     * 若操作成功或失败，会通过回调记录日志并将结果通过 LiveData 发布。
     *
     * @param shortName 要设置的队伍备注信息。
     */
    fun modifyRemark(shortName: String) {
        Timber.i("modifyRemark shortName:$shortName")
        TeamManager.getInstance().modifyRemark(userId.value ?: 0, teamInfo.value?.team_id ?: 0, shortName, object : HttpCallback<Boolean> {
            override fun onSuccess(aBoolean: Boolean) {
                Timber.i("modifyRemark onSuccess aBoolean:$aBoolean ${if (aBoolean) "设置备注成功" else "设置备注失败"}")
                _modifyRemarkResult.postValue(aBoolean)
                if (aBoolean) {
                    getTeamByUserId()
                    doBack()
                }
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("modifyRemark onFailure code:$code message:$message")
                _modifyRemarkResult.postValue(false)
            }
        })
    }

    /**
     * 设置对指定用户的禁用状态。
     * 该方法调用 [TeamManager] 的 `setForbidden` 方法，为指定用户设置禁用或解禁状态，
     * 并通过回调处理操作结果，将结果通过 LiveData 发布。
     *
     * @param otherUserId 要设置禁用状态的目标用户 ID。
     * @param isForbidden 是否禁用该用户，`true` 表示禁用，`false` 表示解禁。
     */
    fun setForbidden(otherUserId: Int, isForbidden: Boolean) {
        Timber.i("setForbidden otherUserId:$otherUserId isForbidden:$isForbidden")
        TeamManager.getInstance().setForbidden(otherUserId, userId.value ?: 0, isForbidden, object : HttpCallback<Boolean> {
            override fun onSuccess(aBoolean: Boolean) {
                Timber.i("setForbidden onSuccess aBoolean:$aBoolean")
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("setForbidden onFailure code:$code message:$message")
            }
        })
    }

    /**
     * 从队伍中踢出指定用户的方法。
     * 该方法调用 [TeamManager] 的 `kickTeam` 方法，将指定用户 ID 列表中的用户从当前队伍中踢出，
     * 并通过回调处理操作结果，打印成功或失败的日志信息。
     *
     * @param userIds 要踢出队伍的用户 ID 列表。
     */
    fun kickTeam(userIds: List<Int>) {
        Timber.i("kickTeam userIds:${gson.toJson(userIds)}")
        TeamManager.getInstance().kickTeam(userIds, userId.value ?: 0, object : HttpCallback<Boolean> {
            override fun onSuccess(aBoolean: Boolean) {
                Timber.i("kickTeam onSuccess aBoolean:$aBoolean ${if (aBoolean) "踢出成功" else "踢出失败"}")
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("kickTeam onFailure code:$code message:$message")
            }
        })
    }

    /**
     * 设置队伍目的地的方法。
     * 该方法调用 [TeamManager] 单例的 `setTeamDestination` 方法，为当前队伍设置目的地信息。
     * 若操作成功或失败，会通过回调打印相应的日志信息。
     *
     * @param lon 目的地的经度，以字符串形式表示。
     * @param lat 目的地的纬度，以字符串形式表示。
     * @param addressName 目的地的名称。
     * @param addressInfo 目的地的详细地址信息。
     */
    fun setTeamDestination(
        lon: String,
        lat: String,
        addressName: String,
        addressInfo: String
    ) {
        Timber.i("setTeamDestination lon:$lon lat:$lat addressName:$addressName addressInfo:$addressInfo")
        TeamManager.getInstance().setTeamDestination(userId.value ?: 0, lon, lat, addressName, addressInfo, object : HttpCallback<Boolean> {
            override fun onSuccess(aBoolean: Boolean) {
                Timber.i("setTeamDestination onSuccess aBoolean:$aBoolean ${if (aBoolean) "设置队伍目的地成功" else "设置队伍目的地失败"}")
                if (!aBoolean) _updateGroupResponseResult.postValue(false)
            }

            override fun onFailure(code: Int, message: String) {
                Timber.i("setTeamDestination onFailure code:$code message:$message")
                _updateGroupResponseResult.postValue(false)
            }
        })
    }

    /**
     * 连接服务的方法。
     * 该方法会记录当前用户 ID 日志，并调用 [TeamManager] 单例的 `connectServe` 方法，
     * 尝试与服务进行连接，将用户 ID 以及服务事件回调对象传递给连接方法。
     */
    fun connectServe(id: Int = userId.value ?: 0) {
        Timber.i("connectServe userId:${userId.value} id:$id")
        if (id != 0) {
            TeamManager.getInstance().connectServe(id, serveEventCallback)
        }
    }

    /**
     * 检查与服务的连接状态。
     * 该方法调用 [TeamManager] 单例的 `isServeConnected` 方法获取当前服务连接状态，
     * 并使用 Timber 打印连接状态信息，最后返回该状态。
     *
     * @return 若服务已连接返回 `true`，否则返回 `false`。
     */
    fun isServeConnected(): Boolean {
        val isServeConnect = TeamManager.getInstance().isServeConnected
        Timber.i("isServeConnected isServeConnect:$isServeConnect")
        return isServeConnect
    }

    /**
     * 断开与服务的连接。
     * 该方法调用 [TeamManager] 单例的 `disconnectServe` 方法断开与服务的连接。
     */
    fun disconnectServe() {
        Timber.i("disconnectServe")
        TeamManager.getInstance().disconnectServe()
    }

    /**
     * 上传当前位置信息到队伍服务。
     * 该方法会先检查与服务的连接状态以及队伍信息是否存在，
     * 若连接正常且队伍信息有效，则获取当前位置并上传到队伍服务；
     * 否则，打印相应的提示信息。
     */
    fun uploadPosition() {
        if (isServeConnected()) {
            val teamInfo = teamInfo.value
            if (teamInfo != null) {
                val location = mLocationBusiness.getLastLocation()
                val lon = location.longitude.toString()
                val lat = location.latitude.toString()
                Timber.i("uploadPosition lon:$lon lat:$lat")
                TeamManager.getInstance().uploadPosition(teamInfo.team_id, lon, lat)
            } else {
                Timber.i("uploadPosition 请先获取队伍信息")
            }
        } else {
            Timber.i("uploadPosition 请先连接服务再上传位置信息")
        }
    }

    /**
     * 启动队伍通话功能。
     * 该方法调用 [TeamManager] 单例的 `startCall` 方法，尝试在当前队伍中启动通话功能。
     * 会传递当前用户 ID 和队伍 ID 给 `startCall` 方法，并通过回调处理通话启动的结果。
     * 若启动成功，会打印成功日志；若启动失败，会打印包含用户 ID 和错误信息的失败日志。
     */
    fun startCall() {
        Timber.i("startCall")
        when {
            !netWorkManager.isNetworkConnected() -> {
                setToast.postValue(ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
            }

            !isServeConnected() -> {
                connectServe()
                setToast.postValue(ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
            }

            else -> {
                TeamManager.getInstance().startCall(userId.value ?: 0, teamInfo.value?.team_id ?: 0, object : StreamCallback {
                    override fun onSuccess(userId: Int) {
                        Timber.i("startCall onSuccess userId:$userId")
                    }

                    override fun onFailure(userId: Int, message: String) {
                        Timber.i("startCall onFailure userId:$userId message:$message")
                    }
                })
            }
        }
    }

    /**
     * 向队伍通话中添加指定用户。
     * 该方法会记录要添加用户的 ID 日志，然后调用 [TeamManager] 单例的 `addCallUser` 方法，
     * 将指定用户添加到当前队伍的通话中。若队伍信息或用户 ID 不存在，则使用默认值。
     *
     * @param addedUserId 要添加到队伍通话中的用户 ID。
     */
    fun addCallUser(addedUserId: Int = userId.value ?: 0) {
        Timber.i("addCallUser addedUserId:$addedUserId isJoinCall:${isJoinCall.value}")
        if (isJoinCall.value == true) {
            locationPermissionUse(true)
            TeamManager.getInstance().addCallUser(addedUserId, teamInfo.value?.team_id ?: 0, userId.value ?: 0, srsToken.value)
        }
    }

    /**
     * 设置全队扬声器静音状态。
     * 该方法会调用 [TeamManager] 单例的 `setSpeakerMute` 方法设置全队扬声器的静音状态，
     * 并通过 LiveData `_isAllSpeakerMute` 发布当前的静音状态，以便观察者监听。
     *
     * @param mute 是否静音，`true` 表示静音，`false` 表示取消静音。
     */
    fun setSpeakerMute(mute: Boolean) {
        Timber.i("setSpeakerMute mute:$mute")
        if (isJoinCall.value == true) TeamManager.getInstance().setSpeakerMute(mute)
        _isAllSpeakerMute.postValue(mute)
    }

    /**
     * 设置方控辅助功能的开关状态。
     * 该方法会记录日志并通过 LiveData 发布方控辅助功能的开关状态，
     * 以便观察者可以监听此状态的变化。
     *
     * @param open 方控辅助功能的开关状态，`true` 表示开启，`false` 表示关闭。
     */
    fun setWheelAssist(open: Boolean) {
        Timber.i("setWheelAssist open:$open")
        _isWheelAssist.postValue(open)
    }

    /**
     * 设置队伍禁言状态。
     * 仅当用户处于对讲状态且为队长时，才能执行此操作。
     * 调用 [TeamManager] 的 `setTeamForbidden` 方法设置队伍禁言状态，并通过回调处理操作结果。
     *
     * @param isForbidden 是否禁言队伍，`true` 表示禁言，`false` 表示解禁。
     */
    fun setTeamForbidden(isForbidden: Boolean) {
        Timber.i("setTeamForbidden isForbidden:$isForbidden")
        if (isJoinCall.value == true && isLeader.value == true) {
            TeamManager.getInstance()
                .setTeamForbidden(userId.value ?: 0, isForbidden, object : HttpCallback<Boolean> {
                    override fun onSuccess(aBoolean: Boolean) {
                        Timber.i("setTeamForbidden onSuccess aBoolean:$aBoolean")
                    }

                    override fun onFailure(code: Int, message: String) {
                        Timber.i("setTeamForbidden onFailure code:$code message:$message")
                    }
                })
        }
    }

    /**
     * 停止队伍通话。
     * 该方法会先检查用户是否处于通话状态，若处于通话状态，则调用 [TeamManager] 的 `stopCall` 方法停止通话。
     * 根据通话停止的结果，若成功则调用 `doBack` 方法执行返回操作。
     */
    fun stopCall() {
        Timber.i("stopCall isJoinCall:${isJoinCall.value}")
        when {
            !netWorkManager.isNetworkConnected() -> {
                setToast.postValue(ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
            }

            !isServeConnected() -> {
                connectServe()
                setToast.postValue(ResUtil.getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
            }

            isJoinCall.value == true -> {
                val result = TeamManager.getInstance().stopCall(userId.value ?: 0, teamInfo.value?.team_id ?: 0)
                Timber.i("stopCall result:$result")
            }

        }
    }

    fun removeCallUser(removedUserId: Int = userId.value ?: 0) {
        Timber.i("removeCallUser removedUserId:$removedUserId isJoinCall:${isJoinCall.value}")
        if (isJoinCall.value == true) {
            locationPermissionUse(false)
            TeamManager.getInstance().removeCallUser(removedUserId)
        }
    }

    /**
     * 开始5s一次上报自己位置
     */
    private fun doStartPositionTimer() {
        Timber.i("doStartPositionTimer")
        positionHandler.removeCallbacks(runnable)
        positionHandler.post(runnable)
    }

    /**
     * 取消定时器
     */
    private fun doStopPositionTimer() {
        Timber.i("doStopPositionTimer")
        positionHandler.removeCallbacks(runnable)
    }


    private val positionHandler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            uploadPosition()
            positionHandler.postDelayed(this, 5000)
        }
    }

    fun getMyNickName(): String {
        val name = userInfoList.value?.find { it.user_id == userId.value }?.run {
            when {
                remark.isNotEmpty() -> remark
                nick_name.isNotEmpty() -> nick_name
                else -> ""
            }
        } ?: ""
        return name
    }

    // 更新通话时长
    private fun updateCallDuration() {
        val startTime = _startCallTime.value ?: return
        val currentTime = System.currentTimeMillis()
        val duration = currentTime - startTime
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
        _callDuration.value = String.format("%02d:%02d", minutes, seconds)
    }

    fun leaveTeam(isFinishFragment: Boolean = true) {
        Timber.d("leaveTeam")
        //重置界面显示
        pageType = BaseConstant.GROUP_DEFAULT_TYPE
        _teamSettingStr.postValue("")
        _showTeamSetting.postValue(false)
        _setDestination.postValue(false)
        _showJoinCall.postValue(false)
        _noDestination.postValue(false)
        mHidden = false

        //重置队伍数据
        _isLeader.postValue(false)
        _teamInfo.postValue(null)
        _inviteUserInfoList.postValue(null)
        _userInfoList.postValue(null)
        _historyFriend.postValue(null)
        _joinCallList.postValue(null)
        _removeUserInfoList.postValue(null)
        _userInfo.postValue(null)
        _isJoinCall.postValue(false)

        clearAllItems()
        doStopPositionTimer()
        userGroupBusiness.showGroupBtn.postValue(false)
        if (isFinishFragment) {
            _finishFragment.postValue(true)
        }
    }

    /**
     * 根据传入的标志位，请求或释放录音权限。
     * 该方法会在协程作用域 `groupScope` 中启动一个协程，调用 `permissionReqController` 的 `notificationPermissionUse` 方法，
     * 来请求或释放 `Manifest.permission.RECORD_AUDIO` 权限。
     *
     * @param isUsed 一个布尔值，`true` 表示请求录音权限，`false` 表示释放录音权限。
     */
    fun locationPermissionUse(isUsed: Boolean) {
        groupScope.launch {
            permissionReqController.notificationPermissionUse(app, Manifest.permission.RECORD_AUDIO, isUsed)
        }
    }

    /**
     * 将HistoryFriendResponse转换为CustomGroupMember
     */
    private fun convertToGroupMember(
        userInfoList: List<UserInfo>?,
        historyFriend: HistoryFriendResponse?
    ): CustomGroupMember? {
        if (historyFriend == null) return null
        return CustomGroupMember().apply {
            // 根据实际字段映射
            user_id = historyFriend.user_id // 用户ID
            nick_name = historyFriend.nick_name // 昵称
            head_img = historyFriend.head_img // 头像URL
            isTeam = userInfoList?.any { it.user_id == historyFriend.user_id } ?: false
            isOnline_status =
                userInfoList?.find { it.user_id == historyFriend.user_id }?.isOnline_status ?: false
        }
    }

    /**
     * 对队伍成员列表进行排序，排序规则：自己 → 队长 → 在线成员 → 离线成员
     * @param teamInfo 队伍信息对象，包含成员列表和队长ID
     * @return 按规则排序后的成员列表
     */
    fun getSortList(teamInfo: TeamInfoResponse): List<UserInfo> {
        // 按条件排序：自己(0) → 队长(1) → 在线成员(2) → 离线成员(3)
        return teamInfo.user_info_list.sortedWith(compareBy { member ->
            when {
                member.user_id == userId.value -> 0  // 自己排第一位
                member.user_id == teamInfo.master_user_id -> 1       // 队长排第二位
                member.isOnline_status -> 2   // 在线成员排第三位
                else -> 3                             // 离线成员排最后
            }
        })
    }
}