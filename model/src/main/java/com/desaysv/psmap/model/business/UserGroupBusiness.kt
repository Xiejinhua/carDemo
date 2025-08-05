package com.desaysv.psmap.model.business

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.layer.model.BizAGroupBusinessInfo
import com.autonavi.gbl.layer.model.BizAGroupType
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.OperatorPosture
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.observer.IMapGestureObserver
import com.autonavi.gbl.user.group.model.GroupDestination
import com.autonavi.gbl.user.group.model.GroupFriend
import com.autonavi.gbl.user.group.model.GroupMember
import com.autonavi.gbl.user.group.model.GroupResponseCreate
import com.autonavi.gbl.user.group.model.GroupResponseFriendList
import com.autonavi.gbl.user.group.model.GroupResponseInfo
import com.autonavi.gbl.user.group.model.GroupResponseInvite
import com.autonavi.gbl.user.group.model.GroupResponseInviteQRUrl
import com.autonavi.gbl.user.group.model.GroupResponseJoin
import com.autonavi.gbl.user.group.model.GroupResponseKick
import com.autonavi.gbl.user.group.model.GroupResponseStatus
import com.autonavi.gbl.user.group.model.GroupResponseUpdate
import com.autonavi.gbl.user.group.model.GroupResponseUrlTranslate
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.autonavi.gbl.user.msgpush.model.TeamUploadResponseMsg
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.model.BinaryStream
import com.autosdk.bussiness.account.UserGroupController
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.layer.AGroupLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.layer.UserBehaviorLayer
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.map.SurfaceViewID.SurfaceViewID1
import com.autosdk.bussiness.push.listener.TeamMessageListener
import com.autosdk.common.AutoConstant
import com.autosdk.common.SdkApplicationUtils
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.common.utils.UploadPositionHandler
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.bean.GroupObserverResultBean
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.GroupObserverBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.bean.HistoryFriendSelectBean
import com.desaysv.psmap.model.bean.UserInfoDifference
import com.desaysv.psmap.model.bean.UserPositionDifference
import com.desaysv.psmap.model.layerstyle.DynamicStyleUtil
import com.google.gson.Gson
import com.txzing.sdk.bean.TeamInfoResponse
import com.txzing.sdk.bean.UserInfo
import com.txzing.sdk.bean.UserPosition
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 用户组队模块
 */
@Singleton
class UserGroupBusiness @Inject constructor(
    private val userGroupController: UserGroupController,
    private val uploadPositionHandler: UploadPositionHandler,
    private val mLayerController: LayerController,
    private val mMapController: MapController,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val pushMessageBusiness: PushMessageBusiness,
    private val groupObserverBusiness: GroupObserverBusiness,
    private val mINaviRepository: INaviRepository,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val cruiseBusiness: CruiseBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val netWorkManager: NetWorkManager,
    private val mLocationBusiness: LocationBusiness,
    private val gson: Gson,
    @ApplicationContext private val context: Context
) {
    private val userGroupScope = CoroutineScope(Dispatchers.Default + Job())

    //主图
    val mMapView: MapView? by lazy {
        mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //组队图层Layer
    val aGroupLayer: AGroupLayer? by lazy {
        mLayerController.getAGroupLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //地图图层MapLayer
    val aMapLayer: MapLayer? by lazy {
        mLayerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //用户行为图层UserBehavior
    val aUserBehaviorLayer: UserBehaviorLayer? by lazy {
        mLayerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //队友信息
    val userInfoList = MutableLiveData<List<UserInfo>?>(null)

    val showGroupBtn = MutableLiveData<Boolean>() //组队按钮是否显示
    val gotoGroupModel = MutableLiveData<String>() //创建队伍界面进入组队出行主界面

    //    val showPushCardView = MutableLiveData<TeamPushBean>() //显示邀请组队弹条
    val responseStatusHasGroup = MutableLiveData<Int>() //0.创建或加入队伍界面--获取组队状态--判断已经在队里了 1.创建或加入队伍界面--创建队伍--判断已经在队里了 2.创建或加入队伍界面--加入队伍--判断已经在队里了
    val updateGroupResponseResult = groupObserverBusiness.updateGroupResponseResult //组队相关请求是否成功

    val cruiseSpeed = cruiseBusiness.cruiseSpeed
    val naviSpeed = mNaviBusiness.naviSpeed

    private var inHomeFragment = false
    private var mMembersList: List<UserInfo> = emptyList()
    private var teamInfo: TeamInfoResponse? = null
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
    private var clickType = "" //"0" 创建队伍,非""和"0"是加入队伍

    // 组队邀请界面变量
    val inviteSelect = MutableLiveData<Int>() //是否能发出邀请
    val setToast = MutableLiveData<String>() //toast提示信息
    val qrcode = MutableLiveData<BinaryStream>() //二维码
    val showQrcodeType = MutableLiveData<Int>() //加载二维码状态 0加载中 1生成二维码成功 2失败
    val friendList = MutableLiveData<ArrayList<GroupFriend>>() //好友列表
    val updateFriendListPosition = MutableLiveData<Int>() //根据position更新好友列表
    val teamHasQuitOrDismiss = MutableLiveData<Int>() //队伍状态，0正常，1无效，2解散，3移除或退出，4队伍信息变化
    var members = ArrayList<GroupMember>() // 成员列表
    var mHistoryFriendSelectBean: ArrayList<HistoryFriendSelectBean> = ArrayList()

    private var isInGroupModule = false //是否显示组队头像
    private val defaultHead: String = "global_image_team_default_head"

    //组队头像缓存
    private val mGroupBitmaps: MutableMap<String, Bitmap> = HashMap()

    fun containsKey(url: String): Boolean = mGroupBitmaps.containsKey(url)

    fun getBitmapByUrl(url: String): Bitmap? = mGroupBitmaps[url]

    //获取本地保存的组队ID
    private fun getTeamId(): String {
        try {
            Timber.i(
                "getTeamId TEAM_ID:${BaseConstant.TEAM_ID} teamId:${
                    mapSharePreference.getStringValue(
                        MapSharePreference.SharePreferenceKeyEnum.teamId,
                        ""
                    )
                }"
            )
        } catch (e: Exception) {
            Timber.e("getTeamId Exception:${e.message}")
        }
        return if (TextUtils.isEmpty(BaseConstant.TEAM_ID)) mapSharePreference.getStringValue(
            MapSharePreference.SharePreferenceKeyEnum.teamId,
            ""
        ) else BaseConstant.TEAM_ID
    }

    fun setInHomeFragment(inHome: Boolean) {
        Timber.i("setInHomeFragment $inHome")
        inHomeFragment = inHome
        if (inHomeFragment)
            loginToGetTeamUserStatus()
    }

    fun setClickType(clickType: String) {
        this.clickType = clickType
        groupObserverBusiness.setClickType(clickType)
    }

    //用于判断activity是否可见
    fun onHiddenChanged(hidden: Boolean) {
        if (hidden) {
            removeGroupObserverWithType(BaseConstant.TYPE_GROUP_MAIN)
            pushMessageBusiness.removeTeamMessageListener(teamMessageListener)
        } else {
            getTeamUserStatus("onHiddenChanged") //获取组队状态
            doTeamMessageListener()  //TeamMessageListener监听
        }
    }

    //TeamMessageListener监听
    fun doTeamMessageListener() {
        pushMessageBusiness.removeTeamMessageListener(teamMessageListener)
        pushMessageBusiness.addTeamMessageListener(teamMessageListener)
    }

    /**
     * 是否组队加入队伍
     */
    fun getTeamUserStatus(name: String) {
        Timber.d(" getTeamUserStatus name:$name")
        removeGroupObserverWithType(BaseConstant.TYPE_GROUP_MAIN)
        addGroupObserverWithType(BaseConstant.TYPE_GROUP_MAIN)
        reqStatus()
    }

    //增加组队回调--组队业务类型-针对界面功能的，比如组队消息弹条
    fun addGroupObserverWithType(businessType: Int) {
//        setOnGroupServiceObserver(businessType)
//        addObserver(businessType)
    }

    //移除组队回调--组队业务类型-针对界面功能的，比如组队消息弹条
    fun removeGroupObserverWithType(businessType: Int) {
        /*groupObserverBusiness.removeOnGroupServiceObserver(businessType)
        removeObserver(businessType)*/
    }

    private fun setOnGroupServiceObserver(businessType: Int) {
        groupObserverBusiness.setOnGroupServiceObserver(businessType, object : GroupObserverBusiness.OnGroupServiceObserver {
            override fun onObserverResult(resultBean: GroupObserverResultBean) {
                userGroupScope.launch {
                    when (resultBean.businessType) {
                        BaseConstant.TYPE_GROUP_MAIN -> {
                            when (resultBean.observerType) {
                                BaseConstant.TYPE_GROUP_STATUS -> { //获取组队状态回调
                                    val response = resultBean.data as GroupResponseStatus
                                    if (resultBean.errCode != Service.ErrorCodeOK) {
                                        showGroupBtn.postValue(false)
//                                        mMembersList = ArrayList<GroupMember>()
                                        return@launch
                                    }
                                    if (!TextUtils.isEmpty(response.teamId)) { // 当前已有队伍，请求队伍信息
                                        mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, response.teamId)
                                        BaseConstant.TEAM_ID = response.teamId
                                        showGroupBtn.postValue(true)
                                    } else {
                                        doStop() // 停止上报位置定时器
                                        showGroupBtn.postValue(false)
//                                        mMembersList = ArrayList<GroupMember>()
                                    }
                                }

                                BaseConstant.TYPE_GROUP_INFO -> { //获取队伍信息回调
                                    val response = resultBean.data as GroupResponseInfo
                                    updateGroupInfo(response)
                                    if (!TextUtils.isEmpty(response.team.teamId)) {
                                        doStartGroupPosition(true) //开始5s一次上报自己位置
//                                        addAGroupMembers(response.members)
                                    }

                                    judeUpdateDestination(response) //判断是否设置或者更新了目的地
                                }
                            }
                        }

                        BaseConstant.TYPE_GROUP_CREATE -> {
                            when (resultBean.observerType) {
                                BaseConstant.TYPE_GROUP_STATUS -> { //获取队伍状态回调
                                    setTaskId(-1) //回调就设置-1
                                    updateGroupResponseResult.postValue(true)
                                    val response = resultBean.data as GroupResponseStatus
                                    saveTeamId(response.teamId) //保存组队ID
                                    if (!TextUtils.isEmpty(response.teamId)) { // 当前已有队伍，请求队伍信息
                                        if (TextUtils.isEmpty(clickType)) { //"0" 创建队伍,非""和"0"是加入队伍
                                            responseStatusHasGroup.postValue(0) //0.创建或加入队伍界面--获取组队状态--判断已经在队里了 1.创建或加入队伍界面--创建队伍--判断已经在队里了 2.创建或加入队伍界面--加入队伍--判断已经在队里了
                                        } else if (TextUtils.equals(clickType, "0")) {
                                            responseStatusHasGroup.postValue(1) //0.创建或加入队伍界面--获取组队状态--判断已经在队里了 1.创建或加入队伍界面--创建队伍--判断已经在队里了 2.创建或加入队伍界面--加入队伍--判断已经在队里了
                                        } else {
                                            responseStatusHasGroup.postValue(2) //0.创建或加入队伍界面--获取组队状态--判断已经在队里了 1.创建或加入队伍界面--创建队伍--判断已经在队里了 2.创建或加入队伍界面--加入队伍--判断已经在队里了
                                        }
                                    } else {
                                        handleNoTeamIdResponse()
                                    }
                                }

                                BaseConstant.TYPE_GROUP_CREATE_FUN -> { //创建队伍回调通知
                                    setTaskId(-1) //回调就设置-1
                                    updateGroupResponseResult.postValue(true)
                                    val response = resultBean.data as GroupResponseCreate
                                    when (response.code) {
                                        1 -> { //成功
                                            if (!TextUtils.isEmpty(response.team.teamId)) {
                                                publishTeamInfo() //上报组队位置信息
                                                doStartGroupPosition(false) //开始5s一次上报自己位置
                                                saveTeamId(response.team.teamId) //保存组队ID
                                                handleTeamIdResponse(response.team.teamId)
                                            }
                                        }

                                        2002, 2009, 2010 -> { //用户已经在自己的队伍中 ,用户已经在其他的队伍中 2009//用户已经在自己的队伍中
                                            val teamId: String = response.team.teamId
                                            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(BaseConstant.TEAM_ID) || TextUtils.isEmpty(
                                                    mapSharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                                                )
                                            ) {
                                                reqStatus() //获取组队状态
                                            } else {
                                                saveTeamId(response.team.teamId) //保存组队ID
                                                responseStatusHasGroup.postValue(1) //0.创建或加入队伍界面--获取组队状态--判断已经在队里了 1.创建或加入队伍界面--创建队伍--判断已经在队里了 2.创建或加入队伍界面--加入队伍--判断已经在队里了
                                            }
                                        }
                                    }
                                }

                                BaseConstant.TYPE_GROUP_JOIN -> { //加入队伍结果回调通知
                                    setTaskId(-1) //回调就设置-1
                                    updateGroupResponseResult.postValue(true)
                                    val response = resultBean.data as GroupResponseJoin
                                    when (response.code) {
                                        1 -> { //成功
                                            if (!TextUtils.isEmpty(response.team.teamId)) {
                                                publishTeamInfo() //上报组队位置信息
                                                doStartGroupPosition(false) //开始5s一次上报自己位置
                                                saveTeamId(response.team.teamId) //保存组队ID
                                                handleTeamIdResponse(response.team.teamId)
                                            }
                                        }

                                        2002, 2009, 2010 -> { //用户已经在他队伍中 ,用户已经在自己的队伍中 ,用户已经在其他的队伍中
                                            val teamId: String = response.team.teamId
                                            if (TextUtils.isEmpty(teamId) || TextUtils.isEmpty(BaseConstant.TEAM_ID) || TextUtils.isEmpty(
                                                    mapSharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, "")
                                                )
                                            ) {
                                                reqStatus() //获取组队状态
                                            } else {
                                                saveTeamId(response.team.teamId) //保存组队ID
                                                responseStatusHasGroup.postValue(2) //0.创建或加入队伍界面--获取组队状态--判断已经在队里了 1.创建或加入队伍界面--创建队伍--判断已经在队里了 2.创建或加入队伍界面--加入队伍--判断已经在队里了
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        BaseConstant.TYPE_GROUP_INVITE_JOIN -> {
                            when (resultBean.observerType) {
                                BaseConstant.TYPE_GROUP_INFO -> { //获取队伍信息结果回调通知
                                    val response = resultBean.data as GroupResponseInfo
                                    if (!TextUtils.isEmpty(response.team.teamId)) {
                                        reqFriendList() //获取历史好友
                                        if (response.members.size > 0) {
                                            members.clear()
                                            members.addAll(response.members)
                                        }
                                    }
                                }

                                BaseConstant.TYPE_GROUP_FRIEND_LIST -> { //获取历史好友回调通知
                                    val response = resultBean.data as GroupResponseFriendList
                                    response.run {
                                        if (resultBean.errCode != Service.ErrorCodeOK || code != 1) {
//                                            inviteType.postValue(3)
                                            return@launch
                                        }

                                        if (friends != null && friends.size > 0) {
//                                            inviteType.postValue(2)
//                                            setInviteData(response.friends)
                                        } else {
//                                            inviteType.postValue(1)
                                            inviteSelect.postValue(-1)
                                        }
                                    }
                                }

                                BaseConstant.TYPE_GROUP_INVITE -> { //获取历史好友回调通知
                                    val response = resultBean.data as GroupResponseInvite
//                                    inviteIsSuccess.postValue(response.code == 1)
                                }

                                BaseConstant.TYPE_GROUP_INVITE_QRURL -> { //获取队伍口令二维码链接回调通知
                                    val response = resultBean.data as GroupResponseInviteQRUrl
                                    response.run {
                                        if (code != 1) {
                                            showQrcodeType.postValue(2) //加载二维码状态 0加载中 1生成二维码成功 2失败
                                            return@launch
                                        }
                                    }
                                    if (response != null) {
                                        reqUrlTranslate(response.url)
                                    } else {
                                        showQrcodeType.postValue(2) //加载二维码状态 0加载中 1生成二维码成功 2失败
                                    }
                                }

                                BaseConstant.TYPE_GROUP_URL_TRANSLATE -> { //链接转为二维码回调通知
                                    val response = resultBean.data as GroupResponseUrlTranslate
                                    response.run {
                                        if (response.code != 1) {
                                            showQrcodeType.postValue(2) //加载二维码状态 0加载中 1生成二维码成功 2失败
                                            return@launch
                                        }
                                    }
                                    if (response != null) {
                                        showQrcodeType.postValue(1) //加载二维码状态 0加载中 1生成二维码成功 2失败
                                        qrcode.postValue(response.data)
                                    } else {
                                        showQrcodeType.postValue(2) //加载二维码状态 0加载中 1生成二维码成功 2失败
                                    }
                                }
                            }
                        }
                    }
                }
            }
        })
    }

    //判断是否设置或者更新了目的地
    private fun judeUpdateDestination(response: GroupResponseInfo) {
        try {
            if (TextUtils.equals(response.team.leaderId, settingAccountBusiness.getAccountProfile()?.uid ?: "")) {
                mapSharePreference.put(MapSharePreference.SharePreferenceKeyEnum.mDesLon, "0.0")
                mapSharePreference.put(MapSharePreference.SharePreferenceKeyEnum.mDesLat, "0.0")
            } else {
                val coord2DDouble =
                    OperatorPosture.mapToLonLat(response.team.destination.display.lon.toDouble(), response.team.destination.display.lat.toDouble())
                val mDesLon = mapSharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.mDesLon, "0.0") //目的地经纬度坐标
                val mDesLat = mapSharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.mDesLat, "0.0") //目的地经纬度坐标

                var isDesUpdate = false //地址是否更新
                if (response.team.destination.display.lon != 0 && response.team.destination.display.lat != 0) {
                    if (BigDecimal.valueOf(mDesLon.toDouble())
                            .compareTo(BigDecimal.valueOf(coord2DDouble.lon)) != 0 && BigDecimal.valueOf(mDesLat.toDouble())
                            .compareTo(BigDecimal.valueOf(coord2DDouble.lat)) != 0
                    ) {
                        isDesUpdate = true
                    }
                    /*if (TextUtils.equals(mDesLon, "0.0") && TextUtils.equals(mDesLat, "0.0")) {//第一次設置
                        showPushCardView.postValue(TeamPushBean(1, TeamPushMsg(), response.team.destination))
                    } else if (isDesUpdate) { //更新了目的地
                        showPushCardView.postValue(TeamPushBean(1, TeamPushMsg(), response.team.destination))
                    }*/
                    mapSharePreference.put(MapSharePreference.SharePreferenceKeyEnum.mDesLon, coord2DDouble.lon.toString())
                    mapSharePreference.put(MapSharePreference.SharePreferenceKeyEnum.mDesLat, coord2DDouble.lat.toString())
                } else {
                    mapSharePreference.put(MapSharePreference.SharePreferenceKeyEnum.mDesLon, "0.0")
                    mapSharePreference.put(MapSharePreference.SharePreferenceKeyEnum.mDesLat, "0.0")
                }
            }
        } catch (e: Exception) {
            Timber.i("judeUpdateDestination Exception:${e.message}")
            mapSharePreference.put(MapSharePreference.SharePreferenceKeyEnum.mDesLon, "0.0")
            mapSharePreference.put(MapSharePreference.SharePreferenceKeyEnum.mDesLat, "0.0")
        }
    }

    //保存组队ID
    private fun saveTeamId(teamId: String) {
        val mTeamId = if (TextUtils.isEmpty(teamId)) if (TextUtils.isEmpty(BaseConstant.TEAM_ID)) mapSharePreference.getStringValue(
            MapSharePreference.SharePreferenceKeyEnum.teamId,
            ""
        ) else BaseConstant.TEAM_ID else teamId
        mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.teamId, mTeamId)
        BaseConstant.TEAM_ID = mTeamId
    }

    //创建或加入队伍发现已经再队伍中啦--后续进入组队界面
    fun createJoinButHasGroup() {
        publishTeamInfo() //上报组队位置信息
        doStartGroupPosition(false) //开始5s一次上报自己位置
        handleTeamIdResponse(getTeamId())
    }

    private fun handleTeamIdResponse(teamId: String) {
        aMapLayer?.setCarVisible(false)
        aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
        gotoGroupModel.postValue(teamId)
    }

    private fun handleNoTeamIdResponse() {
        if (TextUtils.isEmpty(clickType)) { //"0" 创建队伍,非""和"0"是加入队伍
            Timber.d("handleNoTeamIdResponse clickType isEmpty")
        } else if (TextUtils.equals(clickType, "0")) {
            setTaskId(reqCreate(null).toLong())
        } else {
            setTaskId(reqJoin(clickType).toLong())
        }
    }

    /**
     * 是否进入组队界面
     */
    fun setInGroupModule(isInGroupModule: Boolean) {
        this.isInGroupModule = isInGroupModule
        Timber.d("onDestinationChangedListener isInGroupModule = $isInGroupModule mMembersList = ${gson.toJson(mMembersList)}")
        if (isInGroupModule) {
            val memberList = mMembersList
            if (memberList.isNotEmpty()) {
                //导航页不显示组队队长，需要更新
//                UserComponent.getInstance().updateAllGroupMember(ArrayList(memberList), aGroupLayer, true)
                aGroupLayer?.setVisible(BizAGroupType.BizAGroupTypeAGroup.toLong(), true)
            }
        } else {
            aGroupLayer?.setVisible(BizAGroupType.BizAGroupTypeAGroup.toLong(), false)
        }
    }

    fun setMembersList(teamInfoResponse: TeamInfoResponse?, memberList: List<UserInfo>?, userId: Int?) {
        mMembersList = memberList ?: emptyList()
        DynamicStyleUtil.mMembersList = mMembersList
        DynamicStyleUtil.teamInfo = teamInfoResponse
        DynamicStyleUtil.userId = userId
    }

    fun getMembersList() = mMembersList

    //组队消息通知监听器
    private val teamMessageListener = object : TeamMessageListener {
        override fun notifyTeamPushMessage(teamPushMsg: TeamPushMsg?) {
            teamPushMsg?.let {
                if (!TextUtils.isEmpty(it.title)) {
                    /* showPushCardView.postValue(TeamPushBean(0, it, GroupDestination()))
                     if (TextUtils.equals(it.content.type, "QUIT") || TextUtils.equals(it.content.type, "DISMISS")) {
                         showGroupBtn.postValue(false)
 //                        mMembersList = ArrayList<GroupMember>()
                     }*/
                }
            }
        }

        override fun notifyTeamUploadResponseMessage(teamUploadResponseMsg: TeamUploadResponseMsg?) {
            userGroupScope.launch {
                if (teamUploadResponseMsg == null) {
                    return@launch
                }
                //state 队伍状态，0正常，1无效，2解散，3移除或退出，4队伍信息变化
                teamHasQuitOrDismiss.postValue(teamUploadResponseMsg.state)
                if (teamUploadResponseMsg.state == 1 || teamUploadResponseMsg.state == 2 || teamUploadResponseMsg.state == 3) {
                    doStop() //取消位置上传定时器
                    showGroupBtn.postValue(false)
                    aGroupLayer?.clearAllItems()
//                    mMembersList = ArrayList<GroupMember>()
                } else {
                    if (inHomeFragment)
                        getTeamUserStatus("notifyTeamUploadResponseMessage")
                }
            }
        }

    }

    /**
     * 添加车队
     * @param data
     */
    fun addAGroupMembers(teamInfoResponse: TeamInfoResponse?, data: List<UserInfo>?, userId: Int?) {
        Timber.i("addAGroupMembers data = ${gson.toJson(data)}")
        if (data.isNullOrEmpty()) {
            return
        }
        setMembersList(teamInfoResponse, data, userId)
        aGroupLayer?.clearAllItems()
        //添加车队
        val memberList = ArrayList<BizAGroupBusinessInfo>()
        val tempMembersList = ArrayList<UserInfo>()
        if (data.isNotEmpty()) {
            for (i in data.indices) {
                val member = BizAGroupBusinessInfo()
                member.id = data[i].user_id.toString()
                member.priority = i
                member.mPos3D.lon = data[i].latest_lon.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().longitude
                member.mPos3D.lat = data[i].latest_lat.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().latitude
                memberList.add(member)
                tempMembersList.add(data[i])
            }
        }

        for (i in tempMembersList.indices) {
            getGroupHeadUpdateAGroupMember(
                tempMembersList[i].head_img,
                if (NightModeGlobal.isNightMode()) R.drawable.ic_default_avatar_night else R.drawable.ic_default_avatar_day,
                memberList[i]
            )
        }
        aGroupLayer?.addAGroupMembers(memberList)
    }

    /**
     * 更新车队信息
     */
    fun updateAGroupInfo(teamInfoResponse: TeamInfoResponse?, members: List<UserInfo>?, userId: Int?) {
        Timber.d("updateAGroupInfo members:${gson.toJson(members)}")
        if (members.isNullOrEmpty()) {
            return
        }
        val differences = findUserInfoDifferences(mMembersList, members)
        if (differences.isEmpty()) {
            return
        }
        setMembersList(teamInfoResponse, members, userId)

        val defaultHead =
            if (NightModeGlobal.isNightMode()) R.drawable.ic_default_avatar_night else R.drawable.ic_default_avatar_day


        // 实际业务逻辑：从图层中移除该成员
        val onlyInOldListIds = differences.onlyInOldList.map { it.user_id.toString() }.toTypedArray()
        aGroupLayer?.removeItems(onlyInOldListIds)


        val updateList = differences.changedElements.map { it.second } + differences.onlyInNewList
        updateList.forEachIndexed { index, userInfo ->
            val member = BizAGroupBusinessInfo()
            member.id = userInfo.user_id.toString()
            member.priority = index
            member.mPos3D.lon = userInfo.latest_lon.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().longitude
            member.mPos3D.lat = userInfo.latest_lat.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().latitude
            getGroupHeadUpdateAGroupMember(userInfo.head_img, defaultHead, member)
        }
        /*differences.onlyInNewList.forEach { newInfo ->
            Timber.d("添加新元素 onlyInNewList: ${newInfo.user_id}")
            // 实际业务逻辑：向图层中添加该成员
            val member = BizAGroupBusinessInfo()
            member.id = newInfo.user_id.toString()
            member.priority = 0
            member.mPos3D.lon = newInfo.latest_lon.toDouble()
            member.mPos3D.lat = newInfo.latest_lat.toDouble()
            getGroupHeadUpdateAGroupMember(newInfo.head_img, defaultHead, member)
        }

        differences.changedElements.forEach { (oldInfo, newInfo) ->
            Timber.d("更新元素 changedElements oldInfo:${gson.toJson(oldInfo)} newInfo:${gson.toJson(newInfo)}")
            // 实际业务逻辑：更新该成员的位置、在线状态等
            val member = BizAGroupBusinessInfo()
            member.id = newInfo.user_id.toString()
            member.priority = 0
            member.mPos3D.lon = newInfo.latest_lon.toDouble()
            member.mPos3D.lat = newInfo.latest_lat.toDouble()
            getGroupHeadUpdateAGroupMember(newInfo.head_img, defaultHead, member)
        }*/
    }

    /**
     * 更新车队成员坐标
     */
    fun updateAGroupPosition(teamInfoResponse: TeamInfoResponse?, members: List<UserInfo>?, userPositions: List<UserPosition>, userId: Int?) {
        Timber.d("updateAGroupPosition members:${gson.toJson(members)}")
        if (members.isNullOrEmpty()) {
            return
        }
        val differences = findUserPositionDifferences(mMembersList, userPositions)
        Timber.d("updateAGroupPosition differences:${differences.isEmpty()}")
        if (differences.isEmpty()) {
            Timber.d("updateAGroupPosition 没有变化")
            return
        }
        Timber.d("updateAGroupPosition newList:${gson.toJson(differences.newList)}")
        setMembersList(teamInfoResponse, differences.newList, userId)

        val defaultHead =
            if (NightModeGlobal.isNightMode()) R.drawable.ic_default_avatar_night else R.drawable.ic_default_avatar_day

        differences.changedElements.forEachIndexed { index, userInfo ->
            val member = BizAGroupBusinessInfo()
            member.id = userInfo.user_id.toString()
            member.priority = index
            member.mPos3D.lon = userInfo.latest_lon.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().longitude
            member.mPos3D.lat = userInfo.latest_lat.toDoubleOrNull() ?: mLocationBusiness.getLastLocation().latitude
            getGroupHeadUpdateAGroupMember(userInfo.head_img, defaultHead, member)
        }
    }

    fun setEndPoint(poi: POI?) {
        poi?.let {
            aGroupLayer?.setEndPoint(poi.point.rawLongitude, poi.point.rawLatitude)
        }
    }

    fun getGroupHeadUpdateAGroupMember(imageUrl: String, defaultHead: Int, businessInfo: BizAGroupBusinessInfo) {
        Glide.with(context).asBitmap()
            .load(imageUrl)
            .apply(RequestOptions.circleCropTransform().skipMemoryCache(false))
            .placeholder(defaultHead)
            .error(defaultHead)
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Bitmap>, isFirstResource: Boolean): Boolean {
                    Timber.d("onLoadFailed")
                    updateAGroupMember(
                        false, imageUrl, BitmapFactory.decodeResource(
                            SdkApplicationUtils.getApplication().resources, defaultHead
                        ), businessInfo, aGroupLayer
                    )
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    updateAGroupMember(true, imageUrl, CommonUtils.imageZoom(resource), businessInfo, aGroupLayer)
                    return false
                }
            })
            .preload()
    }

    /**
     * 退出组队界面--回到主图等
     */
    fun leaveAGroupMembers() {
        /*if (mMembersList == null || mMembersList.size <= 0) {
            return
        }*/
        aGroupLayer?.clearAllItems()
        /*//添加车队
        val memberList = ArrayList<BizAGroupBusinessInfo>()
        if (mMembersList.isNotEmpty()) {
            for (i in mMembersList.indices) {
                if (TextUtils.equals(mMembersList[i].user_id.toString(), settingAccountBusiness.getAccountProfile()?.uid ?: "")) {
                    Timber.d("addAGroupMembers 主图不显示自己组队头像")
                } else {
                    val member = BizAGroupBusinessInfo()
                    member.id = mMembersList[i].user_id.toString()
                    member.priority = i
                    member.mPos3D.lon = mMembersList[i].latest_lon.toDouble()
                    member.mPos3D.lat = mMembersList[i].latest_lat.toDouble()
                    memberList.add(member)
                }
            }
        }
        aGroupLayer?.addAGroupMembers(memberList) //图层更新组队*/
    }

    //用户已经登录去获取组队状态
    private fun loginToGetTeamUserStatus() {
        if (settingAccountBusiness.isLogin()) {
            getTeamUserStatus("loginToGetTeamUserStatus") //获取组队状态
            doTeamMessageListener()  //TeamMessageListener监听
        }
    }

    // ============================ 组队邀请界面相关 ================================
    //init初始化
    fun initInviteData() {
        addGroupObserverWithType(BaseConstant.TYPE_GROUP_INVITE_JOIN)
        if (netWorkManager.isNetworkConnected()) {
            requestInviteQrUrl() //获取群号的二维码链接
            getTeamInfo() //获取队伍信息
        } else {
//            inviteType.postValue(3)
            showQrcodeType.postValue(2) //加载二维码状态 0加载中 1生成二维码成功 2失败
        }
    }

    //邀请界面Hidden处理
    fun onInviteHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            addGroupObserverWithType(BaseConstant.TYPE_GROUP_INVITE_JOIN)
        } else {
            removeGroupObserverWithType(BaseConstant.TYPE_GROUP_INVITE_JOIN)
        }
    }

    /**
     * 获取队伍信息
     */
    fun getTeamInfo() {
        reqGroupInfo(getTeamId())
    }


    /**
     * 请求口令二维码链接
     */
    fun requestInviteQrUrl() {
        showQrcodeType.postValue(0) //加载二维码状态 0加载中 1生成二维码成功 2失败
        reqInviteQrUrl(getTeamId())
    }

    // ============================ UserGroupController管理类相关 ================================
    /**
     * 注册组队监听
     */
    fun addObserver(businessType: Int) {
        /*groupObserverBusiness.removeObserver(businessType)
        groupObserverBusiness.addObserver(businessType)*/
    }

    /**
     * 移除组队监听
     */
    fun removeObserver(businessType: Int) {
        groupObserverBusiness.removeObserver(businessType)
    }

    /**
     * 获取组队状态
     */
    fun reqStatus(): Int {
        return pushMessageBusiness.reqStatus()
    }

    /**
     * 创建队伍
     * @param dest 队伍目的地
     */
    fun reqCreate(dest: GroupDestination?): Int {
        return userGroupController.reqCreate(dest)
    }

    /**
     * 加入队伍
     * @param teamNum 队伍口令
     */
    fun reqJoin(teamNum: String?): Int {
        return pushMessageBusiness.reqJoin(teamNum)
    }

    /**
     * 获取队伍信息
     * @param teamId 队伍唯一ID
     */
    fun reqGroupInfo(teamId: String?) {
        userGroupController.reqGroupInfo(teamId)
    }

    /**
     * 取消请求
     */
    fun abortRequest() {
        userGroupController.abortRequest()
    }

    //taskId 设置
    fun setTaskId(id: Long) {
        userGroupController.setTaskId(id)
    }

    /**
     * 上报组队位置信息
     */
    fun publishTeamInfo() {
        uploadPositionHandler.publishTeamInfo()
    }

    /**
     * 开始5s一次上报自己位置
     */
    fun doStartGroupPosition(delayDefault: Boolean = true) {
        Timber.e("doStartGroupPosition delayDefault:$delayDefault")
        try {
            if (delayDefault) {
                uploadPositionHandler.doStart(5000)
            } else if ((cruiseSpeed.value != null && cruiseSpeed.value!!.toInt() >= 15) || (naviSpeed.value != null && naviSpeed.value!!.toInt() >= 15)) {
                uploadPositionHandler.doStart(1000)
            } else {
                uploadPositionHandler.doStart(5000)
            }
        } catch (e: Exception) {
            Timber.e("doStartGroupPosition Exception:${e.message}")
        }
    }

    /**
     * 取消定时器
     */
    fun doStop() {
        uploadPositionHandler.doStop()
    }

    //更新队伍信息--加入队伍
    fun updateGroupInfo(data: GroupResponseJoin) {
        userGroupController.updateGroupInfo(data)
    }

    //更新队伍信息
    fun updateGroupInfo(data: GroupResponseInfo) {
        userGroupController.updateGroupInfo(data)
    }

    //更新队伍信息--修改队伍信息
    fun updateGroupInfo(data: GroupResponseUpdate) {
        userGroupController.updateGroupInfo(data)
    }

    //更新队伍信息--队长踢人
    fun updateGroupInfo(data: GroupResponseKick) {
        userGroupController.updateGroupInfo(data)
    }

    //更新队伍信息--列表
    fun syncUpdate(memberList: ArrayList<GroupMember>?) {
        userGroupController.syncUpdate(memberList)
    }

    /**
     * 清空队伍信息
     */
    fun clearTeamInfo() {
        userGroupController.clearTeamInfo()
    }

    /**
     * 修改队伍内昵称
     * @param nickName 新队伍昵称
     */
    fun reqSetNickName(nickName: String?) {
        userGroupController.reqSetNickName(nickName)
    }

    /**
     * 解散队伍
     * @param teamId 队伍唯一ID
     */
    fun reqDissolve(teamId: String?) {
        setTaskId(userGroupController.reqDissolve(teamId).toLong())
    }

    /**
     * 退出队伍
     * @param teamId 队伍唯一ID
     */
    fun reqQuit(teamId: String?) {
        setTaskId(userGroupController.reqQuit(teamId).toLong())
    }

    /**
     * 队长踢人
     * @param teamId 队伍唯一ID
     * @param kicks 踢除成员UID列表
     */
    fun reqKick(teamId: String?, kicks: ArrayList<String>?) {
        userGroupController.reqKick(teamId, kicks)
    }

    /**
     * 修改队伍信息
     * @param teamId 队伍唯一ID
     */
    fun reqUpdateDestination(poi: POI, teamId: String?) {
        val dest = GroupDestination() //目的地
        dest.name = poi.name
        dest.address = poi.addr
        dest.poiId = poi.id
        val coord2DDouble = OperatorPosture.lonLatToMap(poi.point.longitude, poi.point.latitude)
        dest.display.lon = java.lang.Double.valueOf(coord2DDouble.x).toInt() // P20坐标 经度
        dest.display.lat = java.lang.Double.valueOf(coord2DDouble.y).toInt() // P20坐标 经度
        setTaskId(userGroupController.reqUpdate(teamId, dest, "", "").toLong())
    }

    /**
     * 获取所有队伍成员列表-包含队长
     */
    fun getAllMemberList(): ArrayList<GroupMember>? {
        return userGroupController.allMemberList
    }

    fun checkDifferent(list1: ArrayList<GroupMember>, list2: ArrayList<GroupMember>): Boolean {
        if (list1.size != list2.size) {
            return false
        }
        val listString1: MutableList<String> = java.util.ArrayList()
        for (entity in list1) {
            listString1.add(entity.uid)
        }
        val listString2: MutableList<String> = java.util.ArrayList()
        for (entity2 in list2) {
            listString2.add(entity2.uid)
        }
        //如果是false证明有不同的
        return !listString2.retainAll(listString1)
    }

    fun checkDifferent(list1: List<UserInfo>, list2: List<UserInfo>): Boolean {
        if (list1.size != list2.size) {
            return false
        }
        val listString1: MutableList<String> = java.util.ArrayList()
        for (entity in list1) {
            listString1.add(entity.user_id.toString())
        }
        val listString2: MutableList<String> = java.util.ArrayList()
        for (entity2 in list2) {
            listString2.add(entity2.user_id.toString())
        }
        //如果是false证明有不同的
        return !listString2.retainAll(listString1)
    }

    /**
     * 获取历史好友
     */
    fun reqFriendList() {
        userGroupController.reqFriendList()
    }

    /**
     * 邀请好友组队
     */
    fun reqInvite(teamId: String?, invitees: ArrayList<String>): Int {
        return userGroupController.reqInvite(teamId, invitees)
    }

    /**
     * 请求口令二维码链接
     * @param teamId 队伍唯一ID
     */
    fun reqInviteQrUrl(teamId: String?) {
        userGroupController.reqInviteQrUrl(teamId)
    }

    /**
     * 请求口令二维码链接
     * @param teamNumUrl 队伍口令二维码链接
     */
    fun reqUrlTranslate(teamNumUrl: String?) {
        userGroupController.reqUrlTranslate(teamNumUrl)
    }

    fun getMemberIndex(uid: String): Int {
        return userGroupController.getMemberIndex(uid)
    }

    // ============================ LayerController管理类相关 ================================
    //组队图层Layer
    fun getAGroupLayer(@SurfaceViewID1 nSurfaceViewID: Int): AGroupLayer? {
        return mLayerController.getAGroupLayer(nSurfaceViewID)
    }

    //清除所有扎点
    fun clearAllItems() {
        aGroupLayer?.clearAllItems()
    }

    //取消ClickObserver
    fun removeClickObserver(observer: ILayerClickObserver) {
        aGroupLayer?.removeClickObserver(observer)
    }

    //注册ClickObserver
    fun addClickObserver(observer: ILayerClickObserver) {
        aGroupLayer?.addClickObserver(observer)
    }

    // ============================ MapController管理类相关 ================================
    //注册图层手势监听
    fun addGestureObserver(mapGestureObserver: IMapGestureObserver) {
        mMapView?.addGestureObserver(mapGestureObserver)
    }

    //取消注册图层手势监听
    fun removeGestureObserver(mapGestureObserver: IMapGestureObserver) {
        mMapView?.removeGestureObserver(mapGestureObserver)
    }

    /**
     * 移除移图操作观察者
     * @param surfaceViewID 屏幕视图ID
     * left          视口左上角坐标left
     * top           视口左上角坐标top
     */
    fun setMapLeftTop(@SurfaceViewID1 surfaceViewID: Int) {
        mMapController.setMapLeftTop(
            surfaceViewID,
            AutoConstant.mScreenWidth / 2,
            if (mMapController.getMapMode(surfaceViewID) == 0) AutoConstant.mScreenHeight / 2 else AutoConstant.mScreenHeight * 2 / 3
        )
    }

    fun updateAGroupMember(successful: Boolean, url: String, bitmap: Bitmap, businessInfo: BizAGroupBusinessInfo?, aGroupLayer: AGroupLayer?) {
        if (successful) {
            mGroupBitmaps[url] = bitmap
        } else {
            mGroupBitmaps[defaultHead] = bitmap
        }
        aGroupLayer!!.updateAGroupMember(businessInfo)
    }

    /**
     * 对比两个List<UserInfo>的差异
     * @param oldList 原始列表
     * @param newList 目标列表
     * @return 差异结果（新增、删除、字段变化的元素）
     */
    private fun findUserInfoDifferences(oldList: List<UserInfo>?, newList: List<UserInfo>?): UserInfoDifference {
        val originalList = oldList.orEmpty()
        val targetList = newList.orEmpty()

        // 构建user_id到UserInfo的映射，便于快速查找
        val originalMap = originalList.associateBy { it.user_id.toString() }
        val targetMap = targetList.associateBy { it.user_id.toString() }

        // 提取所有user_id用于对比
        val originalIds = originalMap.keys
        val targetIds = targetMap.keys

        // 仅存在于列表1的元素
        val onlyInOldList = originalIds.subtract(targetIds).mapNotNull { originalMap[it] }

        // 仅存在于列表2的元素
        val onlyInNewList = originalIds.subtract(targetIds).mapNotNull { targetMap[it] }

        // 同一user_id但字段变化的元素
        val changedElements = mutableListOf<Pair<UserInfo, UserInfo>>()
        val commonIds = originalIds.intersect(targetIds)
        for (id in commonIds) {
            val oldInfo = originalMap[id]
            val newInfo = targetMap[id]
            if (oldInfo != null && newInfo != null && !isSameUserInfo(oldInfo, newInfo)) {
                changedElements.add(oldInfo to newInfo)
            }
        }

        return UserInfoDifference(onlyInOldList, onlyInNewList, changedElements)
    }

    /**
     * 对比两个List<UserInfo>的差异
     * @param userList 原始列表
     * @param updateList 目标列表
     * @return 差异结果（新增、删除、字段变化的元素）
     */
    private fun findUserPositionDifferences(userList: List<UserInfo>?, positionList: List<UserPosition>?): UserPositionDifference {
        val originalList = userList.orEmpty()
        val targetList = positionList.orEmpty()

        // 构建user_id到UserInfo的映射，便于快速查找
        val originalMap = originalList.associateBy { it.user_id.toString() }
        val targetMap = targetList.associateBy { it.user_id.toString() }

        // 提取所有user_id用于对比
        val originalIds = originalMap.keys
        val targetIds = targetMap.keys

        // 同一user_id但字段变化的元素
        val changedElements = mutableListOf<UserInfo>()
        val commonIds = originalIds.intersect(targetIds)
        for (id in commonIds) {
            val userInfo = originalMap[id]
            val userPosition = targetMap[id]
            Timber.d("findUserPositionDifferences id:$id isSamePosition:${isSamePosition(userInfo!!, userPosition!!)}")
            if (userInfo != null && userPosition != null && !isSamePosition(userInfo, userPosition)) {
                userInfo.latest_lon = userPosition.lon
                userInfo.latest_lat = userPosition.lat
                changedElements.add(userInfo)
            }
        }
        return UserPositionDifference(originalList, changedElements)
    }

    /**
     * 判断两个UserInfo是否完全相同（字段级对比）
     */
    private fun isSameUserInfo(oldUserInfo: UserInfo, newUserInfo: UserInfo): Boolean {
        return oldUserInfo.user_id == newUserInfo.user_id &&
                oldUserInfo.latest_lon == newUserInfo.latest_lon &&
                oldUserInfo.latest_lat == newUserInfo.latest_lat &&
                oldUserInfo.isOnline_status == newUserInfo.isOnline_status &&
                oldUserInfo.time_stamp == newUserInfo.time_stamp &&
                oldUserInfo.head_img == newUserInfo.head_img
        // 可根据实际需求补充其他需要对比的字段
    }

    private fun isSamePosition(userInfo: UserInfo, userPosition: UserPosition): Boolean {
        return userPosition.lon == userInfo.latest_lon &&
                userPosition.lat == userInfo.latest_lat
    }
}