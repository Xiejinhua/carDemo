package com.desaysv.psmap.ui.settings.poidetail

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.autosdk.bussiness.widget.route.model.RoutePathItemContent
import com.desaysv.psmap.R
import com.desaysv.psmap.base.bean.MapPointCardData
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 通用对话框ViewModel
 */
@HiltViewModel
class POIDetailViewModel @Inject constructor(
    private val mapBusiness: MapBusiness,
    @ApplicationContext context: Context,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {

    init {
        mapBusiness.setClickLabelMoveMap(false)
    }

    //是否请求路线中
    val isRequestRoute: LiveData<Boolean> = mRouteBusiness.isRequestRoute
    var pathListLiveData: LiveData<ArrayList<RoutePathItemContent>> = mRouteBusiness.pathListLiveData
    val pointDetail = mapBusiness.pointDetail
    val poiCardDistanceAndTime: LiveData<String> = pointDetail.map { value ->
        value?.let {
            if (!TextUtils.isEmpty(it.poi.arriveTimes)) it.poi.distance + " • " + it.poi.arriveTimes
            else it.poi.distance
        }.toString()
    }

    val poiName: LiveData<String> = pointDetail.map { value ->
        value?.let {
            if (it.cardType == MapPointCardData.PoiCardType.TYPE_CAR_LOC)
                context.getString(R.string.sv_map_my_position)
            else it.poi.name
        } ?: ""
    }

    val phoneNumber: LiveData<String> = pointDetail.map { value ->
        value?.let {
            if (it.cardType == MapPointCardData.PoiCardType.TYPE_CAR_LOC)
                ""
            else it.poi.phone
        } ?: ""
    }

    val poiCardIsTrafficCard: LiveData<Boolean> = pointDetail.map { value ->
        value?.let {
            it.cardType == MapPointCardData.PoiCardType.TYPE_TRAFFIC
        } == true
    }

    val showError: LiveData<Boolean> = pointDetail.map { value ->
        value?.showError == true
    }

    val showLoading: LiveData<Boolean> = pointDetail.map { value ->
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

    fun hidePointDetail() {
        mapBusiness.hidePointDetail()
    }

    //用户是否已经登录
    fun isLogin(): Boolean {
        return settingAccountBusiness.isLogin()
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

    //回车位
    fun backCurrentCarPositionOther() {
        mapBusiness.backCurrentCarPositionOther()
    }

    /**
     * 是否显示所有收藏扎点
     */
    fun showAllFavoritesItem() {
        mapBusiness.showAllFavoritesItem()
    }
}