package com.desaysv.psmap.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.HomeCardTipsType
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description
 */

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mapBusiness: MapBusiness,
    private val userBusiness: UserBusiness,
    @ApplicationContext context: Context,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val cruiseBusiness: CruiseBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val mLocationBusiness: LocationBusiness,
    @ProvidesHiltModule.DefaultMapCommand private val defaultMapCommand: IMapCommand
) : ViewModel() {
    val themeChange = skyBoxBusiness.themeChange()
    val loginLoading: LiveData<Int> = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val setToast = settingAccountBusiness.setToast
    val tipsCardList = mapBusiness.tipsCardList

    private val _showFavoritesList = MutableLiveData(false)
    val showFavoritesList: LiveData<Boolean> = _showFavoritesList

    val roadName = cruiseBusiness.cruiseRouteName
    val cruiseSpeed = cruiseBusiness.cruiseSpeed
    val showCruise = cruiseBusiness.cruiseStatus
    val showCruiseLane = cruiseBusiness.showCruiseLane
    val cruiseLaneList = cruiseBusiness.cruiseLaneList

    val tipsCardTabVisibility = MutableLiveData(false)

    val homeTipsCardVisibility = MutableLiveData(false)

    val commutingScenariosData = mapBusiness.commutingScenariosData

    //限行详情信息
    val restrictInfoDetails = mapBusiness.restrictInfoDetails

    val mapCommand = defaultMapCommand.getMapCommand()

    //用户是否已经登录
    fun isLogin(): Boolean {
        return settingAccountBusiness.isLogin()
    }

    //登录提示
    fun loginToast() {
        settingAccountBusiness.toSetToast(R.string.sv_setting_please_scan_qr)
    }

    fun btFavoriteClick(isSelect: Boolean) {
        Timber.i("btFavoriteClick $isSelect")
        _showFavoritesList.postValue(isSelect)
        if (isSelect) {
            viewModelScope.launch {
                mapBusiness.updateHomeFavorites()
            }
        }
    }

    fun delFavorite(poi: POI) {
        Timber.i("delFavorite ${poi.name}")
        userBusiness.delFavorite(poi)
    }

    /**
     * 预测用户家/公司的位置
     */
    fun sendReqAddressPredict() {
        if (isLogin())
            settingAccountBusiness.sendReqAddressPredict(BaseConstant.REQ_ADDRESS_LABEL_HOME_COMPANY)
    }

    //设置为家
    fun addHome(poi: POI): Boolean {
        val isFavorite = userBusiness.isFavorited(poi)
        val homeList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
        val flag = if (isFavorite) {
            if (!homeList.isNullOrEmpty()) {
                userBusiness.removeFavorites(homeList)
            }
            val result = userBusiness.updateFavorite(ConverUtils.converPOIToFavoriteItem(poi, FavoriteType.FavoriteTypeHome), SyncMode.SyncModeNow)
            result == Service.ErrorCodeOK
        } else {
            if (!homeList.isNullOrEmpty()) {
                userBusiness.removeFavorites(homeList)
                userBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeHome)
            } else {
                userBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeHome)
            }
        }
        Timber.i("addHome $flag")
        return flag
    }

    //设置为公司
    fun addCompany(poi: POI): Boolean {
        val isFavorite = userBusiness.isFavorited(poi)
        val companyList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
        val flag = if (isFavorite) {
            if (!companyList.isNullOrEmpty()) {
                userBusiness.removeFavorites(companyList)
            }
            val result = userBusiness.updateFavorite(ConverUtils.converPOIToFavoriteItem(poi, FavoriteType.FavoriteTypeCompany), SyncMode.SyncModeNow)
            result == Service.ErrorCodeOK
        } else {
            if (!companyList.isNullOrEmpty()) {
                userBusiness.removeFavorites(companyList)
                userBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeCompany)
            } else {
                userBusiness.addFavorite(poi, type = FavoriteType.FavoriteTypeCompany)
            }
        }
        Timber.i("addCompany $flag")
        return flag
    }

    fun getHomePoi(): POI? {
        val homeList = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeHome, true)
        if (!homeList.isNullOrEmpty()) {
            return ConverUtils.converSimpleFavoriteItemToPoi(homeList[0])
        }
        return null
    }

    fun getCompanyPoi(): POI? {
        val list = userBusiness.getSimpleFavoriteList(FavoriteType.FavoriteTypeCompany, true)
        if (!list.isNullOrEmpty()) {
            return ConverUtils.converSimpleFavoriteItemToPoi(list[0])
        }
        return null
    }

    fun removeHomeCardTipsData(type: HomeCardTipsType) {
        mapBusiness.removeHomeCardTipsData(type)
    }

    fun removeRestrictInfoDetails() = mapBusiness.removeRestrictInfoDetails()

    fun continueSapaNavi(): Boolean {
        return mapBusiness.continueSapaNavi()
    }

    fun clearContinueSapaNavi() {
        mapBusiness.clearContinueSapaNavi()
    }

    fun showPoiCard(poi: POI) {
        mapBusiness.showPoiCard(poi)
    }

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        //路线规划监听初始化
        mRouteBusiness.outsideInit()
        viewModelScope.launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }

    /**
     * 发起导航
     */
    fun startNavi(commandBean: CommandRequestRouteNaviBean?) {
        Timber.i("startNavi is called")
        viewModelScope.launch {
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mNaviBusiness.setIsHome(true)
                mNaviBusiness.setParkingRecommendVisible(false)
                mNaviBusiness.planRoute(start, end, midPois, BaseConstant.Type.NEED_REQUEST_RX_PLAN_ROAD)
            }
        }
    }

    fun hideCommutingScenariosCard() {
        Timber.i("hideCommutingScenariosCard")
        mapBusiness.hideCommutingScenariosCard()
    }

    fun getLastLocation() = mLocationBusiness.getLastLocation()

    fun confirmNavi(type: MapCommandType) = defaultMapCommand.notifyMapCommandResult(type, "好的，已为您启动导航")
}