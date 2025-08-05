package com.desaysv.psmap.ui.settings.mobileinternet

import android.app.Application
import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.user.account.model.AccountProfile
import com.autonavi.gbl.util.errorcode.common.Service
import com.desaysv.psmap.R
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 手车互联互联ViewModel
 */
@HiltViewModel
class MobileInternetViewModel @Inject constructor(
    private val application: Application,
    private val userGroupBusiness: UserGroupBusiness,
    private val gson: Gson,
    private val carInfo: ICarInfoProxy,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val netWorkManager: NetWorkManager
) : ViewModel() {
    val showLoginLayout = settingAccountBusiness.showLoginLayout //二维码loading 1加载圈 2成功 3失败
    val loginQrImage = settingAccountBusiness.loginQrImage //登陆二维码
    val showQrTip = settingAccountBusiness.showQrTip //提示是否显示
    val loginLoading = settingAccountBusiness.loginLoading
    val userName = settingAccountBusiness.userName
    var avatar = settingAccountBusiness.avatar
    val setToast = settingAccountBusiness.setToast
    val defaultAvatar = MutableLiveData<Drawable>()
    val isNight = MutableLiveData(NightModeGlobal.isNightMode())
    val isNetworkConnected = MutableLiveData(netWorkManager.isNetworkConnected())
    val showLoginPage = MutableLiveData(false) //显示登录界面

    val loginTip = showLoginLayout.switchMap { state ->
        MutableLiveData<String>().apply {
            value = when (state) {
                1 -> application.resources.getString(R.string.sv_setting_qr_tip_5)
                2 -> application.resources.getString(R.string.sv_setting_qr_tip_5)
                else -> application.resources.getString(R.string.sv_setting_qr_tip_6)
            }
        }
    }

    val qrTips = showLoginLayout.switchMap { state ->
        MutableLiveData<String>().apply {
            value = when (state) {
                1 -> application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_qr_code_refresh)
                2 -> application.resources.getString(com.desaysv.psmap.model.R.string.sv_setting_scan_code_login)
                else -> application.resources.getString(com.autosdk.R.string.login_text_refresh)
            }
        }
    }

    val loginState = loginLoading.switchMap { state ->
        MutableLiveData<Boolean>().apply {
            value = when (state) {
                BaseConstant.LOGIN_STATE_SUCCESS -> true
                BaseConstant.LOGOUT_STATE_LOADING -> true
                else -> false
            }
        }
    }

    init {
        getUserData() //数据初始化，比如加载用户头像
    }

    override fun onCleared() {
        super.onCleared()
        settingAccountBusiness.abortQrLoginConfirmRequest()
    }

    /**
     * 获取二维码
     */
    fun getQRImage() {
        settingAccountBusiness.showLoginLayout.postValue(1)
        settingAccountBusiness.showQrTip.postValue(true)
        settingAccountBusiness.qrTips.postValue(application.resources.getString(com.desaysv.psmap.base.R.string.sv_common_qr_code_refresh))
        settingAccountBusiness.getQRImage()
    }

    //方便网络变化重新加载头像
    fun updateAvatar() {
        avatar = settingAccountBusiness.avatar
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
                userName.postValue(application.getString(com.desaysv.psmap.model.R.string.sv_setting_login_name_hi) + accountProfile.nickname)
                avatar.postValue(accountProfile.avatar)
                loginLoading.postValue(BaseConstant.LOGIN_STATE_SUCCESS)
            } else {
                Timber.d(" getUserData error")
                if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
                    //账号为null，有绑定信息时尝试快速登录
                    val linkageDto = settingAccountBusiness.linkageDto()
                    val accountInfo = settingAccountBusiness.getAccount()
                    if (settingAccountBusiness.isLoggedIn() && accountInfo != null && linkageDto != null && TextUtils.equals(
                            linkageDto.status,
                            "1"
                        )
                    ) {
                        settingAccountBusiness.requestQuickLogin(accountInfo.id, linkageDto.linkageId)
                    } else {
                        settingAccountBusiness.deleteUserData(false)
                    }
                } else {
                    settingAccountBusiness.deleteUserData(false)
                }
            }
        }
    }

    //获取组队状态
    fun getTeamUserStatus() {
        userGroupBusiness.reqStatus()
    }

    //获取足迹信息
    fun requestFootSummary() {
        settingAccountBusiness.requestFootSummary()
    }
}