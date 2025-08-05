package com.desaysv.psmap.ui.ahatrip

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.data.model.CityItemInfo
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.business.AhaTripBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 路书首页ViewModel
 */
@HiltViewModel
class AhaTripMainViewModel @Inject constructor(
    private val mapDataBusiness: MapDataBusiness,
    private val mLocationBusiness: LocationBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val ahaTripBusiness: AhaTripBusiness,
    private val ahaTripImpl: AhaTripImpl): ViewModel() {

    //searchBox的删除、loading状态
    val buttonType = MutableLiveData(0)//0隐藏 1显示删除按钮 2显示loading
    //输入的关键字
    val inputKeyWord = MutableLiveData("")//输入的关键字
    val selectTab = MutableLiveData(true) //顶部tab选择 true.天数 false.评分
    val selectType = selectTab.switchMap { data ->
        MutableLiveData<String>().apply {
            value = when (data) {
                true -> BaseConstant.AHA_TRIP_SORT_DAY
                else -> BaseConstant.AHA_TRIP_SORT_SCORE
            }
        }
    }

    val themeChange = skyBoxBusiness.themeChange()

    val setToast = ahaTripBusiness.setToast //toast
    val lineList = ahaTripBusiness.lineList //路书列表数据
    val lineLoading = ahaTripBusiness.lineLoading //路书加载loading
    val scrollToPosition = MutableLiveData(0) //滚动位置
    val lineAllList = ahaTripBusiness.lineAllList //路书列表总数据
    var lineCurrentPage = 1 //路书列表页码
    val lineMaxPage = ahaTripBusiness.lineMaxPage //路书列表总页码
    private var tripCityName = ""
    private var isLastShowSearchBoxEmpty = false

    override fun onCleared() {
        super.onCleared()
        ahaTripBusiness.lineAllList.postValue(arrayListOf())
    }

    fun setIsLineFav(isLineFav: Boolean){
        ahaTripBusiness.setIsLineFav(isLineFav)
    }

    //设置路书搜索城市名称
    fun setTripCityName(title: String) {
        tripCityName = title
    }

    fun getTripCityName(): String{
        return tripCityName
    }

    fun getLastShowSearchBoxEmpty(): Boolean{
        return isLastShowSearchBoxEmpty
    }

    //获取城市信息
    fun getCityItemInfo(): CityItemInfo?{
        return ahaTripBusiness.getCityItemInfo() ?: getCurrentCity()
    }

    private fun getCurrentCity(): CityItemInfo? {
        val currentCityAdCode = mapDataBusiness.getAdCodeByLonLat(getLastLocation().longitude, getLastLocation().latitude)
        return mapDataBusiness.getCityInfo(currentCityAdCode)
    }

    fun getLastLocation() = mLocationBusiness.getLastLocation()

    //搜索框文字改变时发起预搜索
    fun onInputKeywordChanged(keyWord: String) {
        inputKeyWord.postValue(keyWord)
        if (keyWord.isNotEmpty()) {
            buttonType.postValue(1)
        } else {
            buttonType.postValue(0)
        }
    }

    //请求路书列表
    fun requestLineList(sort: String, cityItemInfo: CityItemInfo, keyword: String, day: String){
        viewModelScope.launch {
            isLastShowSearchBoxEmpty = TextUtils.isEmpty(keyword)
            ahaTripImpl.requestLineList(lineCurrentPage, 10, sort, cityItemInfo, keyword, day)
        }
    }

    //跳转到路书APP
    fun goAhaHome(){
        try {
            ahaTripImpl.goHome(0, -1, "", -1)
        }catch (e: Exception){
            Timber.i("goAhaHome Exception:${e.message}")
        }
    }

    //判断当前是否是登录状态 true 登录 false 未登录
    fun isLogin(): Boolean {
        return ahaTripImpl.isLogin()
    }

    //注册阿哈登录回调
    fun registerLogin(){
        ahaTripImpl.registerLogin()
    }
}