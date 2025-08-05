package com.desaysv.psmap.ui.settings.settingconfig

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 导航功能菜单ViewModel
 */
@HiltViewModel
class NavigationFunctionMenuViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val naviBusiness: NaviBusiness,
    private val settingComponent: ISettingComponent,
    private val netWorkManager: NetWorkManager,
    private val application: Application,
    private val carInfo: ICarInfoProxy,
) : ViewModel() {
    val tmc = navigationSettingBusiness.tmc //路况 1. 打开 0.关闭

    val rsDefaultSelected = navigationSettingBusiness.rsDefaultSelected
    val rsTmcSelected = navigationSettingBusiness.rsTmcSelected
    val rsMoneySelected = navigationSettingBusiness.rsMoneySelected
    val rsFreewayNoSelected = navigationSettingBusiness.rsFreewayNoSelected
    val rsFreewayYesSelected = navigationSettingBusiness.rsFreewayYesSelected
    val rsFreewayQuickSelected = navigationSettingBusiness.rsFreewayQuickSelected
    val rsFreewayBigSelected = navigationSettingBusiness.rsFreewayBigSelected
    val saveStrategy = navigationSettingBusiness.saveStrategy

    val volumeModel = navigationSettingBusiness.volumeModel //1.详细播报 2.简洁播报 3.极简
    val mapType = navigationSettingBusiness.mapType //0 小地图  1 光柱图 2 极简
    val scaleChecked = navigationSettingBusiness.scaleChecked
    val viewOfMap = navigationSettingBusiness.viewOfMap // 0: 2D车首上; 1: 3D车首上; 2: 2D北上
    val setToast = navigationSettingBusiness.setToast //显示toast
    val setNaviToast = naviBusiness.setToast //显示NaviBusiness toast

    val vehicleNum = MutableLiveData("") //车牌
    val limitChecked = MutableLiveData(false) //限行开关选择状态
    val isNight = MutableLiveData(NightModeGlobal.isNightMode()) //是否是黑夜模式
    val showData = MutableLiveData<Boolean>()
    val isNetworkConnected = MutableLiveData(netWorkManager.isNetworkConnected()) //是否有网络

    val loginLoading: LiveData<Int> = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val onHiddenChanged = settingAccountBusiness.onHiddenChanged //界面Hidden监听
    val showMoreInfo = settingAccountBusiness.showMoreInfo //显示提示文言
    val licensePlateChange = carInfo.getLicensePlateChange() //通知系统车牌号变化

    val lastCheckedId = MutableLiveData<Int>(null)
    val lastTargetX = MutableLiveData(0)
    val previewLastTargetX = MutableLiveData(0)
    val previewSelectTab = MutableLiveData<Boolean>()

    //设置导航设置数据
    fun setNavigationSettingData() {
        viewModelScope.launch(Dispatchers.IO) {
            navigationSettingBusiness.getConfigKeyRoadEvent() //获取tmc开关  1开 0 关
            navigationSettingBusiness.getConfigKeyPlanPref() //获取云端的路线偏好配置
            navigationSettingBusiness.getConfigKeyBroadcastMode() //获取云端的播报模式
            navigationSettingBusiness.getConfigKeyMapviewMode() //获取云端的地图视角
            navigationSettingBusiness.refreshOverviewRoads() //获取本地的路况概览模式 鹰眼图或者光柱图
            navigationSettingBusiness.refreshScale() //获取本地的智能比例尺数据
            showData.postValue(true)
        }
    }

    //保存tmc开关  1开 0 关
    fun setConfigKeyRoadEvent(model: Int) {
        navigationSettingBusiness.setConfigKeyRoadEvent(model)
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

    //设置播报模式
    fun setupVolumeModel(model: Int) { //播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
        when (model) {
            1 -> {
                setToast.postValue(
                    String.format(
                        application.getString(R.string.sv_setting_switched), application.getString(
                            R.string.sv_setting_navi_tts_mode3
                        )
                    )
                )
            }

            2 -> {
                setToast.postValue(
                    String.format(
                        application.getString(R.string.sv_setting_switched), application.getString(
                            R.string.sv_setting_navi_tts_mode1
                        )
                    )
                )
            }

            else -> {
                setToast.postValue(
                    String.format(
                        application.getString(R.string.sv_setting_switched), application.getString(
                            R.string.sv_setting_navi_tts_mode2
                        )
                    )
                )
            }
        }
        navigationSettingBusiness.setConfigKeyBroadcastMode(model)
    }

    //智能比例尺开关操作
    fun scaleOperation(isChecked: Boolean) {
        navigationSettingBusiness.scaleOperation(isChecked)
        naviBusiness.setAutoZoom(isChecked)
    }

    //设置地图视角
    fun toSetupViewModel(model: Int) {
        navigationSettingBusiness.setConfigKeyMapviewMode(model)
    }

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        settingAccountBusiness.setShowMoreInfo(moreInfo)
    }

    //重新开始路径规划
    fun restartPlanRoute() {
        naviBusiness.restartPlanRoute(true, BaseConstant.Type.NEED_REQUEST_RX_PLAN_MANUAL_REFRESH)
    }

    //取消请求
    fun abortRequestTaskId() {
        naviBusiness.abortRequestTaskId()
    }
}