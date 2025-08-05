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
import com.desaysv.psmap.model.bean.GuideData
import com.desaysv.psmap.model.business.AhaTripBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 共创路书详情ViewModel
 */
@HiltViewModel
class MyAhaTripDetailViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val ahaTripBusiness: AhaTripBusiness,
    @ProvidesHiltModule.DefaultMapCommand private val defaultMapCommand: IMapCommand,
    private val ahaTripImpl: AhaTripImpl): ViewModel(){
    val themeChange = skyBoxBusiness.themeChange()
    val setToast = ahaTripBusiness.setToast //toast
    val guideDetail = ahaTripBusiness.guideDetail //共创路书详情数据
    val guideDetailLoading = ahaTripBusiness.guideDetailLoading //共创路书详情加载loading
    val selectTab = MutableLiveData(0) //tab选中标志
    val selectCheckedId = MutableLiveData(0) //选择的tab id
    val isFav = ahaTripBusiness.isGuideFav //是否已经收藏
    val isGuideFavChange = ahaTripBusiness.isGuideFavChange //共创路书是否已经收藏--变化
    val totalDay = MutableLiveData("0天") //总天数
    val startEndCity = MutableLiveData<String>() //起始终点城市
    val totalDistance = MutableLiveData<String>() //总里程
    val description = MutableLiveData<String>() //简介
    val hasNode = MutableLiveData<Boolean>() //是否有行程点
    val guideList = ahaTripBusiness.guideList //共创路书列表数据
    val guideCollectList = ahaTripBusiness.guideCollectList //收藏共创路书列表数据
    val deleteGuideCollectResult = ahaTripBusiness.deleteGuideCollectResult //收藏共创路书删除结果
    val gotoAhaScenicDetail = ahaTripBusiness.gotoAhaScenicDetail //进入景点详情页
    val gotoAhaDayDetail = ahaTripBusiness.gotoAhaDayDetail //进入对应天详情

    val mapCommand = defaultMapCommand.getMapCommand()

    private var guideId = ""

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

    fun setGuideId(guideId: String){
        this.guideId = guideId
    }

    fun setDescriptionData(dataDTO: GuideData){
        startEndCity.postValue(dataDTO.startCity + "-" + dataDTO.endCity)
        totalDistance.postValue("往返" + dataDTO.distanceInMeters + "km")
        description.postValue(dataDTO.description ?: "")
    }

    //共创路书详情
    fun requestMineGuideDetail(){
        viewModelScope.launch {
            ahaTripImpl.requestMineGuideDetail(guideId)
        }
    }

    //共创路书请求收藏/取消收藏
    fun requestFavorite(id: String, isMineFav: Boolean){
        viewModelScope.launch {
            ahaTripImpl.requestFavorite(4.toString(), id, isMineFav)
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

    //共创路书简介图层扎标和画线
    fun setGuidePointLine(){
        ahaTripBusiness.setGuidePointLine()
    }

    //DAY几图层扎标和画线
    fun setGuideDayNodePointLine(selectTab: Int){
        ahaTripBusiness.setGuideDayNodePointLine(selectTab)
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