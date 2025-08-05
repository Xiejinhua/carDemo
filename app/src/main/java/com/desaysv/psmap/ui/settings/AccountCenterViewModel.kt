package com.desaysv.psmap.ui.settings

import android.app.Application
import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.user.account.model.AccountProfile
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 个人中心主界面ViewModel
 */
@HiltViewModel
class AccountCenterViewModel @Inject constructor(
    private val application: Application,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val userGroupBusiness: UserGroupBusiness,
    private val netWorkManager: NetWorkManager,
    private val gson: Gson,
    private val carInfo: ICarInfoProxy,
    private val pushMessageBusiness: PushMessageBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val customTeamBusiness: CustomTeamBusiness
) : ViewModel() {
    val isNight = MutableLiveData(NightModeGlobal.isNightMode())
    val isNetworkConnected = MutableLiveData(netWorkManager.isNetworkConnected())
    val defaultAvatar = MutableLiveData<Drawable>()
    val loginLoading = settingAccountBusiness.loginLoading
    val userName = settingAccountBusiness.userName
    var avatar = settingAccountBusiness.avatar
    val setToast = settingAccountBusiness.setToast
    val footprintLoading = settingAccountBusiness.footprintLoading //足迹信息加载loading状态
    val cityNumber = settingAccountBusiness.cityNumber //足迹-城市数量 比如5
    val allCityGe = settingAccountBusiness.allCityGe //足迹-城市 描述【个城市】
    val cityDescription = settingAccountBusiness.cityDescription //足迹-城市描述 比如超过56%的用户
    val guideDis = settingAccountBusiness.guideDis // 足迹-导航公里数，比如1064
    val guideDisUnit = settingAccountBusiness.guideDisUnit // 足迹-导航公里数单位，比如公里
    val guideDistanceDescription = settingAccountBusiness.guideDistanceDescription // 足迹-导航公里数描述，比如相当于淮河的长度
    val myMessageRed = MutableLiveData(false) //我的消息tab未读红点
    val vehicleNum = MutableLiveData("") //车牌
    val teamInfo = customTeamBusiness.teamInfo //组队信息
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)
    val licensePlateChange = carInfo.getLicensePlateChange() //通知系统车牌号变化

    val loginState = loginLoading.switchMap { state ->
        MutableLiveData<Boolean>().apply {
            value = when (state) {
                BaseConstant.LOGIN_STATE_SUCCESS -> true
                BaseConstant.LOGOUT_STATE_LOADING -> true
                else -> false
            }
        }
    }

    //获取本地保存的组队ID
    fun getTeamByUserId(updateAGroupMember: Boolean = true) {
        customTeamBusiness.getTeamByUserId(updateAGroupMember)
    }

    //方便网络变化重新加载头像
    fun updateAvatar() {
        avatar = settingAccountBusiness.avatar
    }

    init {
        settingAccountBusiness.addMessageListener()//注册消息推送
//        userGroupBusiness.addObserver(BaseConstant.TYPE_GROUP_SETTING)
        getUserData() //数据初始化，比如加载用户头像
    }

    override fun onCleared() {
        super.onCleared()
        settingAccountBusiness.removeMessageListener()//移除消息推送
        defaultFootSummary() //退出界面需要重置足迹数据
    }

    fun getAccountProfile(): AccountProfile? {
        return settingAccountBusiness.getAccountProfile() //账号信息
    }

    /**
     * 退出登录
     */
    fun signOut() {
        if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
            val result = settingAccountBusiness.requestUnBindAccount(settingAccountBusiness.getAccount()?.id ?: "", carInfo.sNCode)
            Timber.i("bindInfoChanged result: $result account.id:${settingAccountBusiness.getAccount()?.id ?: ""}")
            if (result != Service.ErrorCodeOK) {
                settingAccountBusiness.logoutFail() //退出账号失败
            }
        } else {
            settingAccountBusiness.signOut()
        }
    }

    /**
     * 用户数据初始化，比如加载用户头像,名称
     */
    private fun getUserData() {
        viewModelScope.launch {
            val accountProfile = getAccountProfile()
            if (accountProfile != null) { //有账号信息，更新头像和名称
                Timber.d(" getUserData account：${gson.toJson(accountProfile)}")
                if (accountProfile.carLoginFlag) {
                    userName.postValue(application.getString(R.string.sv_setting_login_name_hi) + accountProfile.nickname)
                    avatar.postValue(accountProfile.avatar)
                    loginLoading.postValue(BaseConstant.LOGIN_STATE_SUCCESS)
                } else {
                    Timber.d(" getUserData carLoginFlag false")
                    judeLoginOrDeleteUserData(false)
                }
            } else {
                Timber.d(" getUserData error")
                judeLoginOrDeleteUserData(true)
            }
        }
    }

    private fun judeLoginOrDeleteUserData(carLoginFlag: Boolean) {
        viewModelScope.launch {
            if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
                //账号为null，有绑定信息时尝试快速登录
                val linkageDto = settingAccountBusiness.linkageDto()
                val accountInfo = settingAccountBusiness.getAccount()
                if (settingAccountBusiness.isLoggedIn() && accountInfo != null && linkageDto != null && TextUtils.equals(linkageDto.status, "1")) {
                    settingAccountBusiness.requestQuickLogin(accountInfo.id, linkageDto.linkageId)
                } else {
                    if (!carLoginFlag) {
                        setToast.postValue(application.getString(R.string.sv_setting_login_invalid))
                    }
                    settingAccountBusiness.deleteUserData(false)
                }
            } else {
                if (!carLoginFlag) {
                    setToast.postValue(application.getString(R.string.sv_setting_login_invalid))
                }
                settingAccountBusiness.deleteUserData(false)
            }
        }
    }

    //获取消息红点
    fun getMessageData() {
        viewModelScope.launch {
            Timber.i("getMessageData")
            myMessageRed.postValue(false)
            val send2carMsgs = if (settingAccountBusiness.isLogin()) pushMessageBusiness.getSend2carPushMsg() else arrayListOf() //获取send2car消息列表
            val teamPushMsgMessages: ArrayList<TeamPushMsg>? = null //获取组队消息集合
            val uid = getAccountProfile()?.uid ?: ""
            if (send2carMsgs != null && send2carMsgs.size > 0) {
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
            if (teamPushMsgMessages != null && teamPushMsgMessages.size > 0) {
                for (i in 0 until teamPushMsgMessages.size) {
                    if (!teamPushMsgMessages[i].isReaded) {
                        myMessageRed.postValue(true)
                        break
                    }
                }
            }
        }
    }

    //获取组队状态
    fun getTeamUserStatus() {
        userGroupBusiness.reqStatus()
    }

    //进入个人中心判断释放登录和是否有网络，已经登录没有网络显示failShowFootSummary
    fun firstFailShowFootSummary() {
        settingAccountBusiness.firstFailShowFootSummary()
    }

    //获取足迹信息
    fun requestFootSummary() {
        settingAccountBusiness.requestFootSummary()
    }

    //退出界面需要重置足迹数据
    private fun defaultFootSummary() {
        settingAccountBusiness.defaultFootSummary()
    }

    //获取车牌号数据
    fun getVehicleNumberLimit() {
        val number = navigationSettingBusiness.getLicensePlateNumber()
        vehicleNum.postValue(if (TextUtils.isEmpty(number)) application.getString(com.desaysv.psmap.R.string.sv_setting_rb4) else number)
        Timber.d(" getVehicleNumber vehicleNum:$number")
    }

    fun initStrategy() {
        navigationSettingBusiness.initStrategy()
    }

    fun initTeam() {
        val accountInfo = settingAccountBusiness.getAccount()
        val isLogin = settingAccountBusiness.isLoggedIn()
        if (isLogin && accountInfo != null) {
            if ((customTeamBusiness.userId.value ?: 0) == 0
                || accountInfo.id != customTeamBusiness.jtUserId.value.toString()
                || accountInfo.nickname != customTeamBusiness.userInfo.value?.nick_name
                || accountInfo.avatar != customTeamBusiness.userInfo.value?.head_img
            ) {
                customTeamBusiness.disconnectServe()
                customTeamBusiness.leaveTeam(false)
                customTeamBusiness.loginUser(accountInfo.id, accountInfo.nickname ?: "", accountInfo.avatar ?: "")
            } else if ((customTeamBusiness.teamInfo.value?.team_id ?: 0) == 0) {
                getTeamByUserId()
            }
        }
    }
}