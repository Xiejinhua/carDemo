package com.desaysv.psmap.ui.settings.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 手机号码登录 ViewModel
 */
@HiltViewModel
class LoginMobileViewModel @Inject constructor(
    private val skyBoxBusiness: SkyBoxBusiness,
    private val settingAccountBusiness: SettingAccountBusiness
) : ViewModel() {
    val phoneNumber = settingAccountBusiness.phoneNumber //输入的手机号码
    val phoneError = settingAccountBusiness.phoneError //手机号码格式错误提示
    val verificationCode = settingAccountBusiness.verificationCode //输入的验证码
    val verificationClose = MutableLiveData(false) //验证码清除按钮是否显示
    val isNight = skyBoxBusiness.themeChange()
    val verificationCodeTime = settingAccountBusiness.verificationCodeTime //倒计时
    val verificationState = settingAccountBusiness.verificationState //验证码状态
    val verificationError = settingAccountBusiness.verificationError //验证码错误提示
    val loginBtnState = settingAccountBusiness.loginBtnState //登录按钮状态
    val checkState = settingAccountBusiness.checkState //协议勾选按钮状态

    override fun onCleared() {
        super.onCleared()
        settingAccountBusiness.mHandler.removeCallbacks(settingAccountBusiness.mVerifCodeRunnable)
    }

    //监听获取手机号码
    fun getPhoneNumber(number: String) {
        Timber.d("onTextChanged phoneNumber is $number")
        settingAccountBusiness.toSetPhoneNumber(number)
    }

    //监听验证码输入
    fun getVerificationCode(code: String) {
        Timber.d("onTextChanged phoneNumber is $code")
        settingAccountBusiness.toSetVerificationCode(code)
        verificationClose.value = code.isNotEmpty()
    }

    fun toSetVerificationCode(code: String) {
        settingAccountBusiness.toSetVerificationCode(code)
    }

    //验证账号是否存在,若存在就获取验证码
    fun requestAccountCheck() {
        settingAccountBusiness.requestAccountCheck()
    }

    fun setCheckState(check: Boolean) {
        settingAccountBusiness.setCheckState(check)
    }

    //进行登录
    fun mobileLogin() {
        settingAccountBusiness.mobileLogin()
    }

    //退出账号登录界面，验证码按钮文本恢复
    fun defaultVerificationCodeTip() {
        settingAccountBusiness.defaultVerificationCodeTip()
    }
}