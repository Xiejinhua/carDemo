package com.desaysv.psmap.ui.settings.settingconfig

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 地图设置界面ViewModel
 */
@HiltViewModel
class MapSettingViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val naviBusiness: NaviBusiness
) : ViewModel() {
    val tmc = navigationSettingBusiness.tmc //路况 1. 打开 0.关闭
    val viewOfMap = navigationSettingBusiness.viewOfMap // 0: 2D车首上; 1: 3D车首上; 2: 2D北上
    val dayNightType = navigationSettingBusiness.dayNightType //16. 自动 17.白天 18.夜间
    val dayNightSwitch = navigationSettingBusiness.dayNightSwitch //工程模式是否打开地图内部的日夜模式设置，基线默认打开
    val mapFont = navigationSettingBusiness.mapFont //地图字体大小 1标准 2大
    val favoriteChecked = navigationSettingBusiness.favoriteChecked //收藏点开关选择状态
    val showCarCompass = navigationSettingBusiness.showCarCompass //是否显示车标罗盘 true打开 false关闭
    val personalizationCar = navigationSettingBusiness.personalizationCar //个性化车标设置 1.捷途车标；2.汽车；3.普通车标
    val scaleChecked = navigationSettingBusiness.scaleChecked

    val loginLoading: LiveData<Int> = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val onHiddenChanged = settingAccountBusiness.onHiddenChanged //界面Hidden监听
    val isNight = skyBoxBusiness.themeChange()
    var savedScrollIndex = MutableLiveData(0) //滑动参数保存
    var savedScrollOffset = MutableLiveData(0) //滑动参数保存
    val lastTargetX = MutableLiveData(0)

    //设置导航设置数据
    fun setNavigationSettingData() {
        viewModelScope.launch(Dispatchers.IO) {
            navigationSettingBusiness.getConfigKeyRoadEvent() //获取tmc开关  1开 0 关
            navigationSettingBusiness.getConfigKeyMapviewMode() //获取云端的地图视角
            navigationSettingBusiness.getConfigKeyDayNightMode() //获取日夜模式开关
            navigationSettingBusiness.getMapFont() //同步地图字体大小 1标准 2大
            navigationSettingBusiness.getConfigKeyMyFavorite() //获取收藏点配置项
            navigationSettingBusiness.refreshScale() //获取本地的智能比例尺数据
            navigationSettingBusiness.getShowCarCompass() //同步是否显示车标罗盘 true打开 false关闭
            navigationSettingBusiness.getCarPersonalization() //同步个性化车标设置 1.捷途车标；2.汽车；3.普通车标
        }
    }

    //获取日夜模式开关
    fun getConfigKeyDayNightMode() {
        navigationSettingBusiness.getConfigKeyDayNightMode()
    }

    //保存tmc开关  1开 0 关
    fun setConfigKeyRoadEvent(model: Int) {
        navigationSettingBusiness.setConfigKeyRoadEvent(model)
    }

    //设置地图视角
    fun toSetupViewModel(model: Int) {
        navigationSettingBusiness.setConfigKeyMapviewMode(model)
    }

    //设置日夜模式
    fun setupDayNightType(number: Int) {
        navigationSettingBusiness.setConfigKeyDayNightMode(number)
    }

    /**
     * 保存地图字体大小 1标准 2大
     */
    fun setMapFont(type: Int) {
        navigationSettingBusiness.setMapFont(type)
    }

    //收藏点开关操作
    fun favoriteOperation(isChecked: Boolean) {
        navigationSettingBusiness.favoriteOperation(isChecked)
    }

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        settingAccountBusiness.setShowMoreInfo(moreInfo)
    }

    /**
     * 保存是否显示车标罗盘 true打开 false关闭
     */
    fun setShowCarCompass(value: Boolean) {
        Timber.i("setShowCarCompass value:$value")
        navigationSettingBusiness.setShowCarCompass(value)
    }

    /**
     * 保存个性化车标设置 1.捷途车标；2.汽车；3.普通车标
     */
    fun setCarPersonalization(value: Int) {
        navigationSettingBusiness.setCarPersonalization(value)
    }

    //智能比例尺开关操作
    fun scaleOperation(isChecked: Boolean) {
        navigationSettingBusiness.scaleOperation(isChecked)
        if (naviBusiness.isNavigating())
            naviBusiness.setAutoZoom(isChecked)
    }
}