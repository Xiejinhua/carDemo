package com.desaysv.psmap.ui.settings.settingconfig

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 导航设置ViewModel
 */
@HiltViewModel
class NaviSettingViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val naviBusiness: NaviBusiness,
    private val settingComponent: ISettingComponent,
    private val netWorkManager: NetWorkManager,
    private val carInfo: ICarInfoProxy
) : ViewModel() {
    val rsDefaultSelected = navigationSettingBusiness.rsDefaultSelected
    val rsTmcSelected = navigationSettingBusiness.rsTmcSelected
    val rsMoneySelected = navigationSettingBusiness.rsMoneySelected
    val rsFreewayNoSelected = navigationSettingBusiness.rsFreewayNoSelected
    val rsFreewayYesSelected = navigationSettingBusiness.rsFreewayYesSelected
    val rsFreewayQuickSelected = navigationSettingBusiness.rsFreewayQuickSelected
    val rsFreewayBigSelected = navigationSettingBusiness.rsFreewayBigSelected
    val saveStrategy = navigationSettingBusiness.saveStrategy

    val mapType = navigationSettingBusiness.mapType //0 小地图  1 光柱图 2 极简
    val setToast = navigationSettingBusiness.setToast //显示toast

    val vehicleNum = MutableLiveData("") //车牌
    val limitChecked = MutableLiveData(false) //限行开关选择状态
    val isNight = MutableLiveData(NightModeGlobal.isNightMode()) //是否是黑夜模式
    val showData = MutableLiveData<Boolean>()
    val isNetworkConnected = MutableLiveData(netWorkManager.isNetworkConnected()) //是否有网络

    val loginLoading: LiveData<Int> = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val onHiddenChanged = settingAccountBusiness.onHiddenChanged //界面Hidden监听
    val licensePlateChange = carInfo.getLicensePlateChange() //通知系统车牌号变化
    val previewLastTargetX = MutableLiveData(0)
    val previewSelectTab = MutableLiveData<Boolean>()

    //设置导航设置数据
    fun setNavigationSettingData() {
        viewModelScope.launch(Dispatchers.IO) {
            navigationSettingBusiness.getConfigKeyPlanPref() //获取云端的路线偏好配置
            navigationSettingBusiness.refreshOverviewRoads() //获取本地的路况概览模式 鹰眼图或者光柱图
            showData.postValue(true)
        }
    }

    /**
     * 偏好点击事件处理
     * @param prefer
     */
    fun preferSelect(prefer: String, check: Boolean) {
        navigationSettingBusiness.preferSelect(prefer, check)
    }

    fun initStrategy() {
        navigationSettingBusiness.initStrategy()
    }

    /**
     * 检测偏好设置，并判断是否触发返回重新算路
     */
    fun checkAndSavePrefer() {
        navigationSettingBusiness.checkAndSavePrefer()
    }

    //设置全程路况概况 0 小地图  1 光柱图 2 极简
    fun setupMapType(type: Int) {
        navigationSettingBusiness.setupMapType(type)
    }

    //更改路线偏好后重新算路
    fun onReRouteFromPlanPref(): Boolean {
        if (naviBusiness.isNavigating()) {
            naviBusiness.onReRouteFromPlanPref(navigationSettingBusiness.routePreference)
            return true
        }
        return false
    }

    //获取车牌号和限行数据
    fun getVehicleNumberLimit() {
        val number = navigationSettingBusiness.getLicensePlateNumber()
        vehicleNum.postValue(number)
        Timber.d(" getVehicleNumber vehicleNum:$number")
        limitChecked.postValue(settingComponent.getConfigKeyAvoidLimit())
    }

    //限行开关操作
    fun limitOperation(isChecked: Boolean) {
        settingComponent.setConfigKeyAvoidLimit(if (isChecked) 1 else 0)
        limitChecked.postValue(isChecked)
    }
}