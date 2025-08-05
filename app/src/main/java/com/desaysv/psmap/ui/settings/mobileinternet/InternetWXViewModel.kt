package com.desaysv.psmap.ui.settings.mobileinternet

import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.user.account.model.AccountProfile
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.MobileInternetWXBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 微信互联ViewModel
 */
@HiltViewModel
class InternetWXViewModel @Inject constructor(
    private val mobileInternetWXBusiness: MobileInternetWXBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
    val showWeChatLayout = mobileInternetWXBusiness.showWeChatLayout //微信界面类型 1：显示绑定二维码 2：显示绑定loading界面 3：显示微信用户信息 4：失败界面
    val qrImage = mobileInternetWXBusiness.qrImage //绑定二维码
    val qrCodeRefresh = mobileInternetWXBusiness.qrCodeRefresh //二维码更新
    val loginLoading = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val defaultAvatar = MutableLiveData<Drawable>()
    var avatar = settingAccountBusiness.avatar
    val showMoreInfo = settingAccountBusiness.showMoreInfo //显示提示文言
    val setToast = mobileInternetWXBusiness.setToast//toast提示
    val tip = mobileInternetWXBusiness.tip //提示
    val isNight = skyBoxBusiness.themeChange()

    init {
        getUserData() //数据初始化，比如加载用户头像
    }

    /**
     * 用户数据初始化，比如加载用户头像,名称
     */
    private fun getUserData() {
        viewModelScope.launch {
            val accountProfile = getAccountProfile()
            if (accountProfile != null) { //有账号信息，更新头像和名称
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

    fun getAccountProfile(): AccountProfile? {
        return settingAccountBusiness.getAccountProfile() //账号信息
    }

    //方便网络变化重新加载头像
    fun updateAvatar() {
        avatar = settingAccountBusiness.avatar
    }

    //获取微信绑定状态
    fun initWXStatusData() {
        mobileInternetWXBusiness.initWXStatusData()
    }

    //点击重试--重新获取二维码
    fun getRetryQrCode() {
        mobileInternetWXBusiness.getRetryQrCode()
    }

    //点击关闭微信互联
    fun getToUnBind() {
        mobileInternetWXBusiness.getToUnBind()
    }

    //轮询当前二维码扫描状态
    fun sendReqQRCodeConfirm() {
        mobileInternetWXBusiness.sendReqQRCodeConfirm()
    }

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        settingAccountBusiness.setShowMoreInfo(moreInfo)
    }
}