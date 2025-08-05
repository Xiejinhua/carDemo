package com.desaysv.psmap.ui.settings.login

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 二维码登录 ViewModel
 */
@HiltViewModel
class LoginQrViewModel @Inject constructor(
    private val appliaction: Application,
    private val settingAccountBusiness: SettingAccountBusiness
) : ViewModel() {
    val showLoginLayout = settingAccountBusiness.showLoginLayout //二维码loading 1加载圈 2成功 3失败
    val loginQrImage = settingAccountBusiness.loginQrImage //登陆二维码
    val qrTips = settingAccountBusiness.qrTips //提示
    val showQrTip = settingAccountBusiness.showQrTip //提示是否显示
    val failTip = settingAccountBusiness.failTip //错误提示
    val isNight = MutableLiveData(NightModeGlobal.isNightMode())
    val bindText = MutableLiveData(R.string.sv_setting_account_binding_service_agreement)
    val isOpenBind = MutableLiveData(false) //是否显示协议

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
        settingAccountBusiness.abortQrLoginConfirmRequest()
        settingAccountBusiness.quitQrImageTimeOutCountDown()
    }

    /**
     * 获取二维码
     */
    fun getQRImage() {
        settingAccountBusiness.showLoginLayout.postValue(1)
        settingAccountBusiness.showQrTip.postValue(true)
        settingAccountBusiness.qrTips.postValue(appliaction.resources.getString(com.desaysv.psmap.base.R.string.sv_common_qr_code_refresh))
        settingAccountBusiness.getQRImage()
    }
}