package com.desaysv.psmap.ui.group

import android.Manifest
import android.app.Application
import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.map.model.MapviewModeParam
import com.autonavi.gbl.user.group.model.GroupMember
import com.autosdk.R
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.map.SurfaceViewID.SurfaceViewID1
import com.autosdk.bussiness.widget.navi.utils.ResUtil
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.MyTeamBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import com.txzing.sdk.bean.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 组队出行主界面ViewModel
 */
@HiltViewModel
class MyTeamViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val mapBusiness: MapBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val myTeamBusiness: MyTeamBusiness,
    private val customTeamBusiness: CustomTeamBusiness,
    private val iCarInfoProxy: ICarInfoProxy,
    private val app: Application,
    @ProvidesHiltModule.DefaultMapCommand private val mapCommand: IMapCommand
) : ViewModel() {
    var loginLoading = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val zoomInEnable = mapBusiness.zoomInEnable //地图是否可以继续放大
    val zoomOutEnable = mapBusiness.zoomOutEnable //地图是否可以继续缩小
    val showPreview = customTeamBusiness.showPreview
    val mMemberShow = customTeamBusiness.mMemberShow
    val isJoinCall = customTeamBusiness.isJoinCall
    val mPOI = customTeamBusiness.mPOI
    val updateMembersData = customTeamBusiness.updateMembersData //更新队伍中某个人信息
    val updateALLMembersData = myTeamBusiness.updateALLMembersData //更新队伍列表信息
    val removeUserInfoList = customTeamBusiness.removeUserInfoList //更新删除队友列表数据
    val setToast = customTeamBusiness.setToast //toast内容
    val finishFragment = customTeamBusiness.finishFragment //关闭组队界面
    val myTeamStr = customTeamBusiness.myTeamStr //我的队伍--title
    val joinCallStr = customTeamBusiness.joinCallStr //对讲频道--title
    val teamSettingStr = customTeamBusiness.teamSettingStr //我的设置--title
    val setDestination = customTeamBusiness.setDestination //是否可以编辑目的地
    val noDestination = customTeamBusiness.noDestination //没有目的地--队长显示
    val mPOIName = customTeamBusiness.mPOIName //目的地名称
    val showEdit = myTeamBusiness.showEdit //编辑地址按钮是否显示
    val teamPassword = customTeamBusiness.teamPassword //队伍口令密码
    val showTeamSetting = customTeamBusiness.showTeamSetting //显示设置布局
    val showJoinCall = customTeamBusiness.showJoinCall //显示对讲频道
    val captainRemoveLayout = customTeamBusiness.captainRemoveLayout //移除队员一栏是否显示
    val teamDisbandStr = customTeamBusiness.teamDisbandStr //显示退出组队，还是解散组队
    val showNickname = customTeamBusiness.showNickname //显示昵称布局
    val nicknameStr = customTeamBusiness.nicknameStr //修改昵称
    val nickname = customTeamBusiness.myNickname //我的昵称
    val showRemovePlayer = customTeamBusiness.showRemovePlayer //显示移除队友布局
    val mRemoveMemberShow = customTeamBusiness.isRemoveMemberShow
    val isShowBackCar = myTeamBusiness.isShowBackCar
    val screenStatus = iCarInfoProxy.getScreenStatus() //分屏状态监听
    val updateGroupDestination = customTeamBusiness.updateGroupDestination //更新组队目的地
    val updateGroupResponseResult = customTeamBusiness.updateGroupResponseResult //组队设置目的地成功
    val modifyRemarkResult = customTeamBusiness.modifyRemarkResult //修改昵称结果
    val callDuration = customTeamBusiness.callDuration //通话时长
    val isAllForbidden = customTeamBusiness.isAllForbidden //队伍是否禁言
    val isAllSpeakerMute = customTeamBusiness.isAllSpeakerMute //队伍免打扰
    val isWheelAssist = customTeamBusiness.isWheelAssist //方控辅助

    val isNight = MutableLiveData(NightModeGlobal.isNightMode())

    val userInfoList = customTeamBusiness.userInfoList //更新队伍中某个人信息
    val inviteUserInfoList = customTeamBusiness.inviteUserInfoList //包含邀请好友的队伍信息
    val joinCallList = customTeamBusiness.joinCallList //加入对讲列表
    val teamInfo = customTeamBusiness.teamInfo //队伍信息
    val isLeader = customTeamBusiness.isLeader //是否为队长
    val isMineForbidden = customTeamBusiness.isMineForbidden //自己禁言
    val teamTransfer = customTeamBusiness.teamTransfer //队长转让消息
    val userKickedType = customTeamBusiness.userKickedType //用户退出状态 0无 1被踢出队伍 2解散队伍

    // 用于控制遮罩层的显示和隐藏 0:不显示 1：显示准备中 2：显示正在对讲
    val showCallMaskType = MutableLiveData(0)
    private var startTime: Long = 0

    private var job: Job? = null

    //放大地图
    fun zoomIn() {
        mapBusiness.mapZoomIn()
    }

    //缩小地图
    fun zoomOut() {
        mapBusiness.mapZoomOut()
    }

    fun initData() {
        customTeamBusiness.initData()
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
        viewModelScope.launch {
            customTeamBusiness.onCleared()
        }
    }

    //界面Hidden处理
    fun onHiddenChanged(hidden: Boolean, destroy: Boolean) {
        customTeamBusiness.onHiddenChanged(hidden, destroy)
    }

    fun onResume() {
        customTeamBusiness.onResume()
    }

    fun getMembersFocusList(): ArrayList<Boolean> {
        return customTeamBusiness.mMembersFocusList
    }

    fun setListDataFalse() {
        customTeamBusiness.setListDataFalse()
    }

    fun getLastSelectPosition(): Int {
        return customTeamBusiness.lastSelectPosition
    }

    fun getUserId(): String {
        return customTeamBusiness.userId.value.toString()
    }

    fun getLeader(): String {
        return teamInfo.value?.master_user_id.toString()
    }

    //队伍列表点击
    fun onItemClick(id: String, position: Int) {
        customTeamBusiness.onItemClick(id, position)
    }

    //队伍头像
    fun onRefreshHead(position: Int) {
        myTeamBusiness.onRefreshHead(position)
    }

    fun getPageType(): Int {
        return customTeamBusiness.toGetPageType()
    }

    //点击返回键操作
    fun doBack() {
        customTeamBusiness.doBack()
    }

    //设置按钮点击操作
    fun doSettingIcon() {
        customTeamBusiness.doSettingIcon()
    }

    //退出通话按钮点击操作
    fun stopCall() {
        customTeamBusiness.stopCall()
    }

    //回到自身位置
    fun btnBackCar() {
        customTeamBusiness.btnBackCar()
    }

    //全览按钮
    fun fullView() {
        customTeamBusiness.fullView()
    }

    //点击了 修改昵称
    fun nickNameTv() {
        customTeamBusiness.nickNameTv()
    }

    //点击了 加入对讲
    fun joinCall() {
        customTeamBusiness.startCall()
    }

    //点击了 按住说话
    fun addCallUser() {
        customTeamBusiness.addCallUser()
    }

    //松开 按住说话
    fun removeCallUser() {
        customTeamBusiness.removeCallUser()
    }

    //设置昵称--点击确定按钮 确定修改昵称
    fun modifyRemark(name: String) {
        customTeamBusiness.modifyRemark(name)
    }

    //解散或者退出队伍
    fun quitOrDismiss() {
        customTeamBusiness.quitOrDismiss()
    }

    //点击移除组员
    fun doRemoveMember() {
        customTeamBusiness.doRemoveMember()
    }

    fun getPoi(): LiveData<POI?> {
        return customTeamBusiness.mPOI
    }

    fun setRect(rect: Rect?) {
        customTeamBusiness.toSetRect(rect)
    }

    fun setMessage(myMessage: Int) {
        customTeamBusiness.setMessage(myMessage)
    }

    //全览
    fun showPreview() {
        customTeamBusiness.showPreview()
    }

    //判断是否为队长，显示对应布局
    fun judeLeaderShowLayout(isLeader: Boolean) {
        customTeamBusiness.judeLeaderShowLayout(isLeader)
    }

    fun setNickname(name: String) {
        customTeamBusiness.setNickname(name)
    }

    //判断显示目的地横条
    fun judeHasDestination(poi: POI?) {
        customTeamBusiness.judeHasDestination(poi)
    }

    fun getMembers(): ArrayList<GroupMember> {
        return myTeamBusiness.getMembers()
    }

    fun getRemoveMembers(): ArrayList<GroupMember> {
        return myTeamBusiness.getRemoveMembers()
    }

    /**
     * 移除组员
     */
    fun kickTeam(userInfo: UserInfo) {
        customTeamBusiness.kickTeam(listOf(userInfo.user_id))
    }

    /**
     * 移除列表组员
     */
    fun kickTeam(userIds: List<Int>) {
        customTeamBusiness.kickTeam(userIds)
    }

    /**
     * 转让队长
     */
    fun transferTeam(otherUserId: Int) {
        customTeamBusiness.transferTeam(otherUserId)
    }

    fun removeItem() {
        customTeamBusiness.removeItem()
    }

    fun toGetTeamPassword(): String? {
        return customTeamBusiness.getTeamPassword()
    }

    fun startRoute(poi: POI) {
        Timber.i("startRoute poi name:${poi.name} longitude:${poi.point.longitude} latitude:${poi.point.latitude}")
        mapCommand.startPlanRoute(
            poi.name,
            poi.point.longitude,
            poi.point.latitude
        )
    }

    //退出组队界面时，回到默认布局，下次进入组队界面显示默认布局
    fun defaultPage() {
        customTeamBusiness.defaultPage()
        clearAllItems()
    }

    /**
     * 移除移图操作观察者
     */
    fun setMapLeftTop() {
        customTeamBusiness.setMapLeftTop()
    }

    /**
     * 清空组队图层
     */
    fun clearAllItems() {
        customTeamBusiness.clearAllItems()
    }

    fun getMyNickname(): String {
        return customTeamBusiness.getMyNickName()
    }

    fun setTeamForbidden(isForbidden: Boolean) {
        customTeamBusiness.setTeamForbidden(isForbidden)
    }

    fun setSpeakerMute(mute: Boolean) {
        customTeamBusiness.setSpeakerMute(mute)
    }

    fun setWheelAssist(open: Boolean) {
        customTeamBusiness.setWheelAssist(open)
    }

    // 处理按住按钮事件
    fun onHoldButtonPressed() {
        Timber.d("onHoldButtonPressed")
        job = viewModelScope.launch {
            showCallMaskType.postValue(1)
            startTime = System.currentTimeMillis()
            delay(500)
            addCallUser()
            showCallMaskType.postValue(2)
        }
    }

    // 处理松开按钮事件
    fun onHoldButtonReleased() {
        Timber.d("onHoldButtonReleased")
        job?.cancel()
        if (job?.isActive == false) {
            val endTime = System.currentTimeMillis()
            val elapsedTime = endTime - startTime
            if (elapsedTime < 500) {
                setToast.postValue(app.getString(R.string.agroup_main_text_call_time_too_short))
            }
        }
        removeCallUser()
        showCallMaskType.postValue(0)
    }

    fun isNavigating(): Boolean {
        return mNaviBusiness.isNavigating()
    }

    fun setMapMode(
        @SurfaceViewID1 surfaceViewID: Int,
        modeParam: MapviewModeParam?,
        bAnimation: Boolean
    ) {
        mapBusiness.setMapMode(surfaceViewID, modeParam, bAnimation)
    }

    fun backToMap() {
        if (mNaviBusiness.isNavigating()) {
            mapBusiness.backToNavi.postValue(true)
        } else {
            mapBusiness.backToMap.postValue(true)
        }
    }

    fun leaveTeam() {
        customTeamBusiness.leaveTeam()
    }

    /**
     * 设置指定用户的禁言状态。
     *
     * @param otherUserId 要设置禁言状态的用户的 ID。
     * @param isForbidden 是否禁言该用户，`true` 表示禁言，`false` 表示取消禁言。
     */
    fun setForbidden(otherUserId: Int, isForbidden: Boolean) {
        customTeamBusiness.setForbidden(otherUserId, isForbidden)
    }
}