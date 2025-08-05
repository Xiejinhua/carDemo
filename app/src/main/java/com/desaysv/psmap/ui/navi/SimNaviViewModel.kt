package com.desaysv.psmap.ui.navi

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.guide.model.NaviType
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.common.tts.IAutoPlayer
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.business.LinkCarBusiness
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SmartDriveBusiness
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */
@HiltViewModel
class SimNaviViewModel @Inject constructor(
    naviBusiness: NaviBusiness,
    skyBoxBusiness: SkyBoxBusiness,
    gson: Gson,
    application: Application,
    mSettingComponent: ISettingComponent,
    private val iCarInfoProxy: ICarInfoProxy,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val mRouteRequestController: RouteRequestController,
    linkCarBusiness: LinkCarBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val iAutoPlayer: IAutoPlayer,
    private val smartDriveBusiness: SmartDriveBusiness
) : NaviViewModel(
    naviBusiness,
    skyBoxBusiness,
    gson,
    application,
    mSettingComponent,
    iCarInfoProxy,
    navigationSettingBusiness,
    mRouteRequestController,
    linkCarBusiness,
    iAutoPlayer,
    smartDriveBusiness
) {
    val simNaviSpeedType = MutableLiveData(1)
    val isResume = MutableLiveData(true)

    init {
        Timber.i("SimNaviViewModel init")
        naviBusiness.init(NaviType.NaviTypeSimulation)
    }


    override fun setRouteAndNavi(type: Int, mIsHome: Boolean) {
        super.setRouteAndNavi(type, mIsHome)
        setSimSpeed(BaseConstant.SIM_NAVI_SPEED_MEDIUM)
        simNaviSpeedType.postValue(1)
    }

    override fun onCleared() {
//        mNaviBusiness.unInit(NaviType.NaviTypeSimulation)
        mNaviBusiness.resetNaviCardData()
        mRouteBusiness.focusPathIndex.value?.let { focusPathIndex ->
            mRouteBusiness.selectPathByIndexOnMap(focusPathIndex)
        }
        iAutoPlayer.stop(true)
        Timber.i("SimNaviViewModel onCleared")
    }

    fun setSimSpeed(speed: Int) {
        mNaviBusiness.setSimSpeed(speed)
        when (speed) {
            BaseConstant.SIM_NAVI_SPEED_LOW -> {
                simNaviSpeedType.postValue(0)
            }

            BaseConstant.SIM_NAVI_SPEED_MEDIUM -> {
                simNaviSpeedType.postValue(1)
            }

            BaseConstant.SIM_NAVI_SPEED_HIGH -> {
                simNaviSpeedType.postValue(2)
            }
        }
    }

    /**
     * 恢复导航
     */
    fun resumeNavi() = mNaviBusiness.resumeNavi()

    fun isNaving() = mNaviBusiness.isNavigating()

    /**
     * 暂停导航
     */
    fun pauseNavi() = mNaviBusiness.pauseNavi()

    /**
     * 设置模拟导航状态
     */
    fun setSimNavigationStatus(resume: Boolean) {
        isResume.postValue(resume)
    }
}

