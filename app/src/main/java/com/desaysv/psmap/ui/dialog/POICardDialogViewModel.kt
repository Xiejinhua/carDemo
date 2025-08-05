package com.desaysv.psmap.ui.dialog

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.business.BluetoothBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 通用对话框ViewModel
 */
@HiltViewModel
class POICardDialogViewModel @Inject constructor(
    private val mapBusiness: MapBusiness,
    @ApplicationContext context: Context,
    private val userBusiness: UserBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val mBluetoothBusiness: BluetoothBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
    val mapPointCard = mapBusiness.mapPointCard
    val poiCardDistanceAndTime: LiveData<String> = mapPointCard.map { value ->
        value?.let {
            if ((!TextUtils.isEmpty(it.poi.arriveTimes))) {
                it.poi.distance + " • " + it.poi.arriveTimes
            } else {
                it.poi.distance
            }
        }.toString()
    }

    val trafficPicUrl: LiveData<String> = mapPointCard.map { value ->
        value?.traffic_picurl?.run {
            if (this.contains(",http")) {
                this.split(",http")[0]
            } else this
        } ?: ""
    }

    val poiCardIsMyCar: LiveData<Boolean> = mapPointCard.map { value ->
        value?.let {
            it.cardType == MapPointCardData.PoiCardType.TYPE_CAR_LOC
        } == true
    }

    val poiName: LiveData<String> = mapPointCard.map { value ->
        value?.let {
            if (it.cardType == MapPointCardData.PoiCardType.TYPE_CAR_LOC)
                context.getString(R.string.sv_map_my_position)
            else it.poi.name
        } ?: ""
    }

    val phoneNumber: LiveData<String> = mapPointCard.map { value ->
        value?.let {
            if (it.cardType == MapPointCardData.PoiCardType.TYPE_CAR_LOC)
                ""
            else it.poi.phone
        } ?: ""
    }

    val poiCardIsFavorite: LiveData<Boolean> = mapPointCard.map { value ->
        value?.run {
            var fPoi = poi
            if (!poi.childPois.isNullOrEmpty() && this.poi.childIndex != -1) {
                fPoi = poi.childPois[this.poi.childIndex]
            }
            userBusiness.isFavorited(fPoi)
        } == true
    }

    val poiCardIsTrafficCard: LiveData<Boolean> = mapPointCard.map { value ->
        value?.let {
            it.cardType == MapPointCardData.PoiCardType.TYPE_TRAFFIC
        } == true
    }

    val trafficCardDesc: LiveData<String> = mapPointCard.map { value ->
        var desc = ""
        value?.let { data ->
            /*if (!TextUtils.isEmpty(data.address)) {
                desc = desc.plus(data.address)
            }
            if (!TextUtils.isEmpty(data.traffic_desc)) {
                desc = desc.plus("\n" + data.traffic_desc)
            }*/
            if (!TextUtils.isEmpty(data.traffic_infotimeseg)) {
                desc = desc.plus(
                    context.getString(
                        R.string.sv_map_traffic_detail_dtime_format,
                        data.traffic_infotimeseg
                    ) + "\n"
                )
            }
            if (!TextUtils.isEmpty(data.traffic_infostartdate)) {
                desc = desc.plus(
                    context.getString(
                        R.string.sv_map_traffic_detail_stime_format,
                        data.traffic_infostartdate
                    ) + "\n"
                )
            }
            if (!TextUtils.isEmpty(data.traffic_infoenddate)) {
                desc = desc.plus(
                    context.getString(
                        R.string.sv_map_traffic_detail_etime_format,
                        data.traffic_infoenddate
                    )
                )
            }

            Timber.i("trafficCardDesc $desc")
        }
        desc
    }

    val hasTrafficPicture: LiveData<Boolean> = mapPointCard.map { value ->
        value?.let {
            !TextUtils.isEmpty(it.traffic_picurl)
        } == true
    }

    val trafficUpdateTime: LiveData<String> = mapPointCard.map { value ->
        var desc = ""
        value?.let { data ->
            if (!TextUtils.isEmpty(data.traffic_lastupdate)) {
                desc = context.getString(
                    R.string.sv_map_traffic_detail_nicktime_format,
                    data.traffic_nick,
                    CommonUtils.strTimeFromNow(context, data.traffic_lastupdate!!.toLong())
                )
            }
        }
        Timber.i("trafficUpdateTime $desc")
        desc
    }

    val poiCardOpenTime: LiveData<String> = mapPointCard.map { value ->
        value?.let {
            if ((!TextUtils.isEmpty(it.poi.deepinfo))) {
                it.poi.deepinfo
            } else {
                ""
            }
        }.toString()
    }

    val showError: LiveData<Boolean> = mapPointCard.map { value ->
        value?.showError == true
    }

    val showLoading: LiveData<Boolean> = mapPointCard.map { value ->
        value?.showLoading == true
    }

    private val _showChild = MutableLiveData(false)
    val showChild: LiveData<Boolean> = _showChild

    private val _moreChild = MutableLiveData(false)
    val moreChild: LiveData<Boolean> = _moreChild

    val themeChange: LiveData<Boolean> = skyBoxBusiness.themeChange()

    val defaultTrafficPic = MutableLiveData<Drawable>()

    val mapToast = mapBusiness.toast


    fun showChild(show: Boolean, moreChild: Boolean) {
        _showChild.value = show
        _moreChild.value = moreChild
    }

    fun updatePointCardChildPoiIndex(index: Int) {
        mapBusiness.updatePointCardChildPoiIndex(index)
    }

    fun addOrDelFavorite() {
        viewModelScope.launch {
            mapBusiness.poiCardAddOrDelFavorite()
        }
    }

    fun hideMapPointCard() {
        mapBusiness.hideMapPointCard(true)
    }

    fun retrySearchPoiCardInfo() {
        mapBusiness.retrySearchPoiCardInfo()
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        return settingAccountBusiness.isLogin()
    }

    //登录提示
    fun loginToast() {
        settingAccountBusiness.toSetToast(R.string.sv_setting_please_scan_qr)
    }

    //电话拨打
    fun onPhoneCall(phone: String) = mBluetoothBusiness.callPhone(phone)

    fun refreshMapPointCard() = mapBusiness.refreshMapPointCard()

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
}