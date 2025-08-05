package com.desaysv.psmap.ui.settings.settingconfig

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.data.model.Voice
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 播报设置界面ViewModel
 */
@HiltViewModel
class NaviBroadcastViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val speechSynthesizeBusiness: SpeechSynthesizeBusiness,
    private val application: Application
) : ViewModel() {
    val roadConditionsAhead = navigationSettingBusiness.roadConditionsAhead //前方路况 默认关闭
    val electronicEyeBroadcast = navigationSettingBusiness.electronicEyeBroadcast //电子眼播报 默认关闭
    val safetyReminder = navigationSettingBusiness.safetyReminder //安全提醒 默认关闭
    val cruiseBroadcast = navigationSettingBusiness.cruiseBroadcast //巡航播报开关 false.关闭 true.打开
    val ahaScenicBroadcast = navigationSettingBusiness.ahaScenicBroadcast //巡航景点推荐 false.关闭 true.打开
    val volumeModel = navigationSettingBusiness.volumeModel //1.详细播报 2.简洁播报 3.极简
    val returnAllSetting = navigationSettingBusiness.returnAllSetting //恢复出厂设置

    val loginLoading: LiveData<Int> = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val onHiddenChanged = settingAccountBusiness.onHiddenChanged //界面Hidden监听
    val isNight = skyBoxBusiness.themeChange() //是否是黑夜模式
    val setToast = MutableLiveData<String>()
    val useVoice = MutableLiveData<Voice>()

    val lastCheckedId = MutableLiveData<Int>(null)
    val lastTargetX = MutableLiveData(0)

    //设置导航设置数据
    fun setNavigationSettingData() {
        viewModelScope.launch(Dispatchers.IO) {
            navigationSettingBusiness.refreshCruiseBroadcast() //获取云端的巡航播报
            navigationSettingBusiness.getCruiseBroadcastSwitch() //巡航播报开关
            navigationSettingBusiness.getConfigKeyBroadcastMode() //获取云端的播报模式
            navigationSettingBusiness.getAhaScenicBroadcastSwitch() //巡航景点推荐开关
        }
    }

    /**
     * 巡航播报 点击事件处理
     * @param prefer
     */
    fun cruiseBroadcastSelect(prefer: Int, check: Boolean) {
        navigationSettingBusiness.cruiseBroadcastSelect(prefer, check)
    }

    /**
     * 保存巡航播报开关 false.关闭 true.打开
     */
    fun setCruiseBroadcastSwitch(value: Boolean) {
        navigationSettingBusiness.setCruiseBroadcastSwitch(value)
    }

    /**
     * 保存巡航景点推荐开关 false.关闭 true.打开
     */
    fun setAhaScenicBroadcastSwitch(value: Boolean) {
        navigationSettingBusiness.setAhaScenicBroadcastSwitch(value)
    }

    //设置播报模式
    fun setupVolumeModel(model: Int) { //播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
        when (model) {
            1 -> {
                setToast.postValue(
                    String.format(
                        application.getString(R.string.sv_setting_switched),
                        application.getString(R.string.sv_setting_navi_tts_mode3)
                    )
                )
            }

            2 -> {
                setToast.postValue(
                    String.format(
                        application.getString(R.string.sv_setting_switched),
                        application.getString(R.string.sv_setting_navi_tts_mode1)
                    )
                )
            }

            else -> {
                setToast.postValue(
                    String.format(
                        application.getString(R.string.sv_setting_switched),
                        application.getString(R.string.sv_setting_navi_tts_mode2)
                    )
                )
            }
        }
        navigationSettingBusiness.setConfigKeyBroadcastMode(model)
    }

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        settingAccountBusiness.setShowMoreInfo(moreInfo)
    }

    //获取正在使用的Voice
    fun getUseVoice() {
        useVoice.postValue(speechSynthesizeBusiness.getUseVoice() ?: Voice())
    }
}