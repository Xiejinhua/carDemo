package com.desaysv.psmap.ui.settings.trip

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.autonavi.gbl.user.model.BehaviorDataType
import com.autonavi.gbl.user.syncsdk.model.SyncMode
import com.autonavi.gbl.util.errorcode.common.Service
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.TripBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 我的行程ViewModel
 */
@HiltViewModel
class MyTripViewModel @Inject constructor(
    private val application: Application,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val userBusiness: UserBusiness,
    private val tripBusiness: TripBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val settingComponent: ISettingComponent
) : ViewModel() {
    val loginLoading = settingAccountBusiness.loginLoading
    val stopSyncRefresh = tripBusiness.stopSyncRefresh
    val syncTrackHistory = settingAccountBusiness.syncTrackHistory
    val addNewData = tripBusiness.addNewData
    val isNotEmptyData = tripBusiness.isNotEmptyData
    val showTripSetting = MutableLiveData(false)
    val showToast = MutableLiveData<String>()
    val autoRecordTripSwitch = MutableLiveData<Int>() //自动记录行程
    val showMoreInfo = settingAccountBusiness.showMoreInfo //显示提示文言
    val isNight = skyBoxBusiness.themeChange()

    val loginState = loginLoading.switchMap { state ->
        MutableLiveData<Boolean>().apply {
            value = when (state) {
                BaseConstant.LOGIN_STATE_SUCCESS -> true
                BaseConstant.LOGOUT_STATE_LOADING -> true
                else -> false
            }
        }
    }

    val title = showTripSetting.switchMap { state ->
        MutableLiveData<String>().apply {
            value = when (state) {
                true -> application.getString(com.autosdk.R.string.trace_setting_text_tittle)
                else -> application.getString(R.string.sv_setting_rb15)
            }
        }
    }

    fun initData() {
        tripBusiness.registerISyncSDKServiceObserver()
        if (settingAccountBusiness.isLogin()) {
            refreshTripData()
            syncTrackHistory()
        } else {
            isNotEmptyData.postValue(false)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tripBusiness.unregisterISyncSDKServiceObserver()
    }

    fun refreshTripData(){
        tripBusiness.refreshTripData()
    }

    /**
     * 刷新用户信息
     */
    fun requestAccountProfilt() {
        userBusiness.requestAccountProfile()
    }

    //手动开始同步
    fun syncTrackHistory() {
        userBusiness.startSync()
    }

    fun clearTrackHistory() {
        val code = userBusiness.clearBehaviorData(BehaviorDataType.BehaviorTypeTrailDriveForAuto, SyncMode.SyncModeNow)
        if (code == Service.ErrorCodeOK) {
            showToast.postValue("行程纪录已清空")
            if (settingAccountBusiness.isLogin()) {
                refreshTripData()
                syncTrackHistory()
            } else {
                isNotEmptyData.postValue(false)
            }
        }
    }

    //删除指定id行为数据
    fun deleteById(id: String?): Boolean {
        return tripBusiness.deleteById(id) == Service.ErrorCodeOK
    }

    /**
     * 保存自动记录行程 0打开 1关闭
     */
    fun setAutoRecord(value: Int) {
        autoRecordTripSwitch.postValue(value)
        settingComponent.setAutoRecord(value)
        Timber.i("setAutoRecord value:$value")
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.AutoRecordSw, if (value == 0) 1 else 0)
            )
        )
    }

    /**
     * 同步自动记录行程 0打开 1关闭
     */
    fun getAutoRecord(): Int {
        val autoRecord = settingComponent.getAutoRecord()
        autoRecordTripSwitch.postValue(autoRecord)
        Timber.i("getAutoRecord autoRecord:$autoRecord")
        return autoRecord
    }

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        settingAccountBusiness.setShowMoreInfo(moreInfo)
    }
}