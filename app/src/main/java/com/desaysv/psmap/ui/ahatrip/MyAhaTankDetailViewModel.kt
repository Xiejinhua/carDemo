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
import com.desaysv.psmap.model.bean.TankData
import com.desaysv.psmap.model.business.AhaTripBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 轨迹路书详情ViewModel
 */
@HiltViewModel
class MyAhaTankDetailViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val ahaTripBusiness: AhaTripBusiness,
    private val ahaTripImpl: AhaTripImpl): ViewModel(){
    val themeChange = skyBoxBusiness.themeChange()
    val setToast = ahaTripBusiness.setToast //toast
    val tankDetail = ahaTripBusiness.tankDetail //轨迹路书详情数据
    val tankDetailLoading = ahaTripBusiness.tankDetailLoading //轨迹路书详情加载loading
    val isFav = ahaTripBusiness.isTankFav //轨迹路书是否已经收藏
    val isTankFavChange = ahaTripBusiness.isTankFavChange //轨迹路书是否已经收藏--变化
    val tankList = ahaTripBusiness.tankList //轨迹路书列表数据
    val totalDistance = MutableLiveData<String>() //总里程
    val description = MutableLiveData<String>() //简介
    val hasNode = MutableLiveData<Boolean>() //是否有行程点
    val markTime = MutableLiveData<String>() //记录时间
    val title = MutableLiveData<String>() //标题
    val tankCollectList = ahaTripBusiness.tankCollectList //收藏轨迹路书列表数据
    val deleteTankCollectResult = ahaTripBusiness.deleteTankCollectResult //收藏轨迹路书删除结果

    private var tankId = ""

    fun setTankId(tankId: String){
        this.tankId = tankId
    }

    fun setDescriptionData(dataDTO: TankData){
        title.postValue("住在" + dataDTO.caption)
        markTime.postValue(dataDTO.createAt + "")
        totalDistance.postValue("${dataDTO.distance}km")
        description.postValue(dataDTO.description ?: "")
    }

    //轨迹路书详情
    fun requestMineGuideDetail(){
        viewModelScope.launch {
            ahaTripImpl.requestMineTankDetail(tankId)
        }
    }

    //轨迹路书请求收藏/取消收藏
    fun requestFavorite(id: String, isMineFav: Boolean){
        viewModelScope.launch {
            ahaTripImpl.requestFavorite(12.toString(), id, isMineFav)
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

    //轨迹详情图层扎标和画线
    fun setTankNodePointLine(){
        ahaTripBusiness.setTankNodePointLine()
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