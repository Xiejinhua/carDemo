package com.desaysv.psmap.ui.share

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.TripShareBusiness
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/5
 * @description 分享二维码ViewModel
 */
@HiltViewModel
class TripShareDialogViewModel @Inject constructor(
    private val mTripShareBusiness: TripShareBusiness,
    private val mSkyBoxBusiness: SkyBoxBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val gson: Gson,
    private val mRouteBusiness: RouteBusiness,
    private val mRouteRequestController: RouteRequestController,
) : ViewModel() {
    val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.route)
    var naviTripUrlLd: LiveData<String> = mTripShareBusiness.naviTripUrlLd
    val themeChange = mSkyBoxBusiness.themeChange()

    private val _shareType = MutableLiveData<Int>(0) //0：微信分享  1：短信分享
    val shareType: LiveData<Int> = _shareType

    private val _isClickSend = MutableLiveData<Boolean>(false)
    val isClickSend: LiveData<Boolean> = _isClickSend

    val setToast: LiveData<String> = mTripShareBusiness.setToast

    private val _phoneNumList = MutableLiveData<List<String>>(emptyList())
    val phoneNumList: LiveData<List<String>> = _phoneNumList

    // 0:无数据 1:刷新数据结果页
    val mDataRefresh = MutableLiveData<Int>(-1)

    val itemType = object : TypeToken<ArrayList<String>>() {}.type


    fun reqTripUrl() = mTripShareBusiness.reqTripUrl()

    fun setShareType(type: Int) = _shareType.postValue(type)

    fun sendSmsTripShare(phone: String) {
        val phoneNumber = CommonUtils.phoneNoSpace(phone)
        mTripShareBusiness.sendSmsTripShare(phoneNumber)
        setPhoneNumList(phoneNumber)
    }

    /**
     * 设置离线路线样式
     */
    fun setSwitchOffline() = mRouteBusiness.setSwitchOffline()

    fun setClickSendState(isClickSend: Boolean) = _isClickSend.postValue(isClickSend)

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val cleanPhone = CommonUtils.phoneNoSpace(phoneNumber)
        val regex = Regex("^1\\d{10}$")
        return regex.matches(cleanPhone)
    }

    fun clearPhoneNumList() {
        val key = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.phoneNumList.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.phoneNumList.toString()
        }
        mapSharePreference.putStringValue(key, "")
        getPhoneNumList()
    }

    /**
     * 保存短信分享手机历史
     */
    private fun setPhoneNumList(phone: String) {
        val arrayList = _phoneNumList.value?.toMutableList() ?: mutableListOf()
        // 添加新的 String 到 ArrayList 的第一项
        arrayList.add(0, phone)
        // 如果需要保持原有顺序，可以使用 LinkedHashSet
        val uniqueListMaintainOrder = arrayList.toSet().toMutableList()
        // 如果列表大小超过 50，则从末尾开始删除多余的号码
        while (uniqueListMaintainOrder.size > 50) {
            uniqueListMaintainOrder.removeAt(uniqueListMaintainOrder.size - 1)
        }
        val key = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.phoneNumList.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.phoneNumList.toString()
        }
        mapSharePreference.putStringValue(key, gson.toJson(uniqueListMaintainOrder))
        getPhoneNumList()
    }

    /**
     * 获取短信分享手机历史
     */
    fun getPhoneNumList() {
        val key = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.phoneNumList.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.phoneNumList.toString()
        }
        val list = mapSharePreference.getStringValue(key, "")
        val phoneList: List<String> = if (list.isNotEmpty()) gson.fromJson(list, itemType) else arrayListOf()
        Timber.i("getPhoneNumList = $phoneList， phoneList.size = ${phoneList.size}")
        _phoneNumList.postValue(phoneList)
        mDataRefresh.postValue(if (list.isNotEmpty()) 1 else 0)
    }

    /**
     * 重新规划路线
     */
    fun retryPlanRoute() {
        Timber.i("retryPlanRoute is called")
        val carRouteResult = mRouteRequestController.carRouteResult
        val start = carRouteResult?.fromPOI
        val end = carRouteResult?.toPOI
        val midPois = carRouteResult?.midPois
        if (start != null && end != null) {
            viewModelScope.launch {
                mRouteBusiness.planRoute(start, end, midPois, true)
            }
        }
    }
}