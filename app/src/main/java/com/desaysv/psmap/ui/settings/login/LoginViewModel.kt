package com.desaysv.psmap.ui.settings.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 登录 ViewModel
 */
@HiltViewModel
class LoginViewModel @Inject constructor(private val settingAccountBusiness: SettingAccountBusiness) : ViewModel() {
    val codeSuccess = settingAccountBusiness.codeSuccess //true 扫码成功 关闭登录框
    val setToast = settingAccountBusiness.setToast
    val tabSelect = MutableLiveData(0) //0：二维码登录 1：验证码登录
}