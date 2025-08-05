package com.desaysv.psmap.ui.settings.about

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 关于界面ViewModel
 */
@HiltViewModel
@SuppressLint("StringFormatInvalid")
class AboutViewModel @Inject constructor(
    private val application: Application,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
    val jtVersion = MutableLiveData(String.format(application.getString(R.string.sv_setting_jt_version), BaseConstant.JT_VERSION)) //捷途软件版本号
    val publicationStr = navigationSettingBusiness.publicationStr //出版物审图号信息
    val internetStr = navigationSettingBusiness.internetStr //互联网审图号信息
    val dataFileVersionStr = navigationSettingBusiness.dataFileVersionStr //数据版本号
    val channelNumber = MutableLiveData(String.format(application.getString(R.string.sv_setting_channel_number), BaseConstant.CHANNEL_NUMBER))
    val icpRecordNumber = MutableLiveData("粤ICP备18074457号-35A")
    val appRecordNumber = MutableLiveData("粤ICP备18074457号-35A")
    val isNight = skyBoxBusiness.themeChange()
    var showMoreInfo = MutableLiveData(MoreInfoBean()) //显示提示文言

    override fun onCleared() {
        super.onCleared()
        navigationSettingBusiness.abortRequestMapNum()
    }

    //请求审图号
    fun requestMapNum() {
        viewModelScope.launch(Dispatchers.IO) {
            navigationSettingBusiness.abortRequestMapNum()
            navigationSettingBusiness.requestMapNum() //请求审图号
        }
    }

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        Timber.i("setShowMoreInfo moreInfo:${moreInfo.content}")
        showMoreInfo.postValue(moreInfo)
    }
}