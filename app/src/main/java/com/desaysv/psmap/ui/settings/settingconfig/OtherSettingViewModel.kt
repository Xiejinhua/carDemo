package com.desaysv.psmap.ui.settings.settingconfig

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.data.model.Voice
import com.autonavi.gbl.user.behavior.model.ConfigKey
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.widget.route.constant.ConfigRoutePreference
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.AutoStatus
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 其他设置界面ViewModel
 */
@HiltViewModel
class OtherSettingViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val speechSynthesizeBusiness: SpeechSynthesizeBusiness,
    private val settingComponent: ISettingComponent,
    private val naviBusiness: NaviBusiness
) : ViewModel() {
    val version = navigationSettingBusiness.version //版本号
    val intentionNavigation = navigationSettingBusiness.intentionNavigation //保存意图导航状态 true打开 false关闭
    val appCache = navigationSettingBusiness.appCache //APP缓存
    val setToast = navigationSettingBusiness.setToast //显示toast

    val loginLoading: LiveData<Int> = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val onHiddenChanged = settingAccountBusiness.onHiddenChanged //界面Hidden监听
    val isNight = skyBoxBusiness.themeChange()

    //设置导航设置数据
    fun setNavigationSettingData() {
        viewModelScope.launch(Dispatchers.IO) {
            navigationSettingBusiness.getIntentionNavi() //同步意图导航状态 true打开 false关闭
            navigationSettingBusiness.getCacheSize() // 获取应用缓存大小
        }
    }

    //保存意图导航状态 true打开 false关闭
    fun setIntentionNavigation(value: Boolean) {
        navigationSettingBusiness.setIntentionNavigation(value)
    }

    //清除缓存
    fun toClearCache(isDefaultSettings: Boolean) {
        navigationSettingBusiness.clearCache(isDefaultSettings) // 清除应用缓存
    }

    //恢复出厂设置
    fun returnAllSetting() {
        navigationSettingBusiness.setShowCarCompass(true) //是否显示车标罗盘 false关闭
        navigationSettingBusiness.setConfigKeyDayNightMode(16)
        navigationSettingBusiness.setMapFont(1)
        navigationSettingBusiness.scaleOperation(true)
        if (naviBusiness.isNavigating())
            naviBusiness.setAutoZoom(true)
//        SettingComponent.getInstance().setCarLogos(0)
//        ConfigSettingUtils.setMapView3dBuild(1,mSettingMsp)
        settingComponent.setConfigKeyAvoidLimit(0)
        navigationSettingBusiness.preferSelect(ConfigRoutePreference.PREFERENCE_DEFAULT, true)
        navigationSettingBusiness.setupMapType(2)
        navigationSettingBusiness.setConfigKeyBroadcastMode(SettingConst.BROADCAST_DETAIL)
//        SettingComponent.getInstance().setConfigKeyRoadWarn(1)
        navigationSettingBusiness.cruiseBroadcastSelect(ConfigKey.ConfigKeyRoadWarn, false)
        navigationSettingBusiness.cruiseBroadcastSelect(ConfigKey.ConfigKeySafeBroadcast, false)
        navigationSettingBusiness.cruiseBroadcastSelect(ConfigKey.ConfigKeyDriveWarn, false)
//        ConfigSettingUtils.setThemeId(0,mSettingMsp)
//        ConfigSettingUtils.setThemePath("",mSettingMsp)
//        SdkAdapterManager.getInstance().sendNormalMessage(AutoState.RESTORE_SETTING_SUCCESS)
        navigationSettingBusiness.setConfigKeyRoadEvent(1)
        navigationSettingBusiness.setConfigKeyMapviewMode(MapModeType.VISUALMODE_2D_CAR)
//        ConfigSettingUtils.setVolume(100,mSettingMsp)
//        new MapSharePreference(MapSharePreference.SharePreferenceName.userSetting).putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.cruiseMute, false);
//        new MapSharePreference(MapSharePreference.SharePreferenceName.userSetting).putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.isReboot, true);
        navigationSettingBusiness.favoriteOperation(true)
        AutoStatusAdapter.sendStatus(AutoStatus.SETTING_RESET_OK)
        navigationSettingBusiness.setIntentionNavigation(false)
        navigationSettingBusiness.setCruiseBroadcastSwitch(true)
        navigationSettingBusiness.setAhaScenicBroadcastSwitch(true)
        val result = speechSynthesizeBusiness.todoSetVoice(Voice().apply { id = -1 })
        if (result == Service.ErrorCodeOK) {
            speechSynthesizeBusiness.setSpeechVoiceId(-1)
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Map_Set,
                mapOf(
                    Pair(EventTrackingUtils.EventValueName.BroadcastSet, "标准女音")
                )
            )
        }
        navigationSettingBusiness.returnAllSetting.postValue(true)
    }

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        settingAccountBusiness.setShowMoreInfo(moreInfo)
    }
}