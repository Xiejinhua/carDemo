package com.desaysv.psmap.ui.group

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject


/**
 * 组队出行-邀请好友ViewModel
 */
@HiltViewModel
class InviteJoinViewModel @Inject constructor(
    private val application: Application,
    private val userGroupBusiness: UserGroupBusiness,
    private val mINaviRepository: INaviRepository,
    private val customTeamBusiness: CustomTeamBusiness,
    private val mapBusiness: MapBusiness,
    private val mNaviBusiness: NaviBusiness,
    @ProvidesHiltModule.DefaultMapCommand private val mapCommand: IMapCommand
) : ViewModel() {
    // LiveData
    val type = customTeamBusiness.inviteType //历史好友类型 0加载中 1获取成功无数据  2获取成功有数据 3失败
    val inviteSelectable = customTeamBusiness.inviteSelectable //是否能发出邀请
    val isSuccess = customTeamBusiness.inviteIsSuccess //是否邀请成功
    val isNight = MutableLiveData(NightModeGlobal.isNightMode())
    val setToast = userGroupBusiness.setToast //toast提示信息
    val qrcode = userGroupBusiness.qrcode //二维码
    val showQrcodeType = userGroupBusiness.showQrcodeType //加载二维码状态 0加载中 1生成二维码成功 2失败
    val updateFriendListPosition = userGroupBusiness.updateFriendListPosition //根据position更新好友列表

    val historyFriend = customTeamBusiness.historyFriend //历史好友
    val userInfoList = customTeamBusiness.userInfoList //用户信息列表

    val teamPassword = customTeamBusiness.teamPassword //队伍口令密码
    val finishFragment = customTeamBusiness.finishFragment //关闭组队界面
    val userKickedType = customTeamBusiness.userKickedType //用户退出状态 0无 1被踢出队伍 2解散队伍

    val qrTips = showQrcodeType.switchMap { state ->
        MutableLiveData<String>().apply {
            value = when (state) {
                0 -> application.getString(com.desaysv.psmap.base.R.string.sv_common_qr_code_refresh)
                2 -> application.getString(com.autosdk.R.string.login_text_refresh)
                else -> ""
            }
        }
    }

    fun initData() {
        setCarVisible(false)
        customTeamBusiness.getHistoryFriends()
//        userGroupBusiness.initInviteData()
    }

    fun setCarVisible(visible: Boolean) {
        userGroupBusiness.aMapLayer?.setCarVisible(visible)
        userGroupBusiness.aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
    }

    override fun onCleared() {
        super.onCleared()
        userGroupBusiness.removeGroupObserverWithType(BaseConstant.TYPE_GROUP_INVITE_JOIN)
    }

    //界面Hidden处理
    fun onHiddenChanged(hidden: Boolean) {
        userGroupBusiness.onInviteHiddenChanged(hidden)
        setCarVisible(hidden)
    }

    /**
     * 获取队伍信息
     */
    fun getTeamInfo() {
        customTeamBusiness.getTeamByUserId()
    }

    /**
     * 发出邀请
     */
    fun inviteMembers(userIds: List<Int>) {
        if (userIds.isNotEmpty()) {
            customTeamBusiness.inviteMembers(userIds)
        }
    }

    /**
     * 获取历史好友列表。
     * 此方法会调用 [CustomTeamBusiness] 类中的 `getHistoryFriends` 方法，
     * 从而触发获取历史好友信息的操作。获取到的历史好友信息可通过
     * [customTeamBusiness.historyFriend] 这个 LiveData 进行观察。
     */
    fun getHistoryFriends() {
        customTeamBusiness.getHistoryFriends()
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

    fun startRoute(poi: POI) {
        Timber.i("startRoute poi name:${poi.name} longitude:${poi.point.longitude} latitude:${poi.point.latitude}")
        mapCommand.startPlanRoute(
            poi.name,
            poi.point.longitude,
            poi.point.latitude
        )
    }
}
