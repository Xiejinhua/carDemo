package com.desaysv.psmap.ui.ahatrip

import androidx.activity.result.ActivityResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.business.AhaTripBusiness
import com.example.aha_api_sdkd01.manger.models.LineDetailModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 路书详情ViewModel
 */
@HiltViewModel
class AhaTripDetailViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val ahaTripBusiness: AhaTripBusiness,
    private val ahaTripImpl: AhaTripImpl): ViewModel(){
    val themeChange = skyBoxBusiness.themeChange()
    val setToast = ahaTripBusiness.setToast //toast
    val lineDetail = ahaTripBusiness.lineDetail //路书详情数据
    val lineDetailLoading = ahaTripBusiness.lineDetailLoading //路书详情加载loading
    val selectTab = MutableLiveData(0) //tab选中标志
    val selectCheckedId = MutableLiveData(0) //选择的tab id
    val isFav = ahaTripBusiness.isLineFav //是否已经收藏
    val totalDay = MutableLiveData("0天") //总天数
    val startEndCity = MutableLiveData<String>() //起始终点城市
    val totalDistance = MutableLiveData<String>() //总里程
    val description = MutableLiveData<String>() //简介
    val hasNode = MutableLiveData<Boolean>() //是否有行程点
    val lineCollectList = ahaTripBusiness.lineCollectList //精选路书列表数据
    val deleteLineCollectResult = ahaTripBusiness.deleteLineCollectResult //收藏精选路书删除结果
    val isLineFavChange = ahaTripBusiness.isLineFavChange //路书是否已经收藏--变化
    val gotoAhaScenicDetail = ahaTripBusiness.gotoAhaScenicDetail //进入景点详情页
    val gotoAhaDayDetail = ahaTripBusiness.gotoAhaDayDetail //进入对应天详情

    private var lineId = ""

    init {
        ahaTripBusiness.customLayerAddClickObserver() //添加图层点击
    }

    override fun onCleared() {
        super.onCleared()
        ahaTripBusiness.customLayerRemoveClickObserver() //移除图层点击
    }

    fun setSelectCheckedId(checkedId: Int) {
        selectCheckedId.postValue(checkedId)
    }

    fun setLineId(lineId: String){
        this.lineId = lineId
    }

    fun setDescriptionData(dataDTO: LineDetailModel.DataDTO){
        startEndCity.postValue(dataDTO.startCityName + "-" + dataDTO.endCityName)
        totalDistance.postValue("往返" + dataDTO.totalDistance + "km")
        description.postValue(dataDTO.description)
    }

    //单条路书详情
    fun requestLineDetail(){
        viewModelScope.launch {
            ahaTripImpl.requestLineDetail(lineId)
        }
    }

    //路书请求收藏/取消收藏
    fun requestFavorite(id: String, isMineFav: Boolean){
        viewModelScope.launch {
            ahaTripImpl.requestFavorite(1.toString(), id, isMineFav)
        }
    }

    //判断当前是否是登录状态 true 登录 false 未登录
    fun isLogin(): Boolean{
        return ahaTripImpl.isLogin()
    }

    //注册阿哈登录回调
    fun registerLogin(){
        ahaTripImpl.registerLogin()
    }

    //跳转阿哈登录
    fun goLogin(result: ActivityResult){
        ahaTripImpl.goLogin(result)
    }

    //简介图层扎标和画线
    fun setLinePointLine(){
        ahaTripBusiness.setLinePointLine()
    }

    //DAY几图层扎标和画线
    fun setLineDayNodePointLine(selectTab: Int){
        ahaTripBusiness.setLineDayNodePointLine(selectTab)
    }

    fun removeAllLayerItems(bizType: Long) {
        ahaTripBusiness.removeAllLayerItems(bizType)
    }

    fun exitPreview(){
        ahaTripBusiness.exitPreview()
    }

    /**
     * 规划路线
     */
    fun planRoute(commandBean: CommandRequestRouteNaviBean?) {
        //路线规划监听初始化
        mRouteBusiness.outsideInit()
        viewModelScope.launch {
            if (mNaviBusiness.isNavigating()) {
                mNaviBusiness.stopNavi()
                delay(500)
            }
            val start = commandBean?.start
            val end = commandBean?.end
            val midPois = commandBean?.midPois
            if (start != null && end != null) {
                mRouteBusiness.planRoute(start, end, midPois)
            }
        }
    }
}