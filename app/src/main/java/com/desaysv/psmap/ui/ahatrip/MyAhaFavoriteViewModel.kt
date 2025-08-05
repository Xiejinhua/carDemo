package com.desaysv.psmap.ui.ahatrip

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.model.business.AhaTripBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 路书收藏ViewModel
 */
@HiltViewModel
class MyAhaFavoriteViewModel @Inject constructor(
    private val skyBoxBusiness: SkyBoxBusiness,
    private val ahaTripBusiness: AhaTripBusiness,
    private val ahaTripImpl: AhaTripImpl
): ViewModel() {
    val themeChange = skyBoxBusiness.themeChange()
    val selectTab = MutableLiveData(0) //顶部tab选择 0.共创路书 1.精选路书 2.轨迹路书
    val setToast = ahaTripBusiness.setToast //toast
    val guideCollectList = ahaTripBusiness.guideCollectList //收藏共创路书列表数据
    val guideCollectLoading = ahaTripBusiness.guideCollectLoading //收藏共创路书加载loading
    val deleteGuideCollectResult = ahaTripBusiness.deleteGuideCollectResult //收藏共创路书删除结果
    val lineCollectList = ahaTripBusiness.lineCollectList //精选路书列表数据
    val lineCollectLoading = ahaTripBusiness.lineCollectLoading //精选路书加载loading
    val deleteLineCollectResult = ahaTripBusiness.deleteLineCollectResult //收藏精选路书删除结果
    val tankCollectList = ahaTripBusiness.tankCollectList //收藏轨迹路书列表数据
    val tankCollectLoading = ahaTripBusiness.tankCollectLoading //收藏轨迹路书加载loading
    val deleteTankCollectResult = ahaTripBusiness.deleteTankCollectResult //收藏轨迹路书删除结果

    val guideScrollToPosition = MutableLiveData(0) //收藏共创路书列表滚动位置
    val guideAllList = ahaTripBusiness.guideCollectAllList //收藏共创路书列表总数据
    var guideCurrentPage = 1 //收藏共创路书列表页码
    val guideMaxPage = ahaTripBusiness.guideCollectMaxPage //收藏共创路书列表总页码

    val lineScrollToPosition = MutableLiveData(0) //收藏精选路书列表滚动位置
    val lineAllList = ahaTripBusiness.lineCollectAllList //收藏精选路书列表总数据
    var lineCurrentPage = 1 //收藏精选路书列表页码
    val lineMaxPage = ahaTripBusiness.lineCollectMaxPage //收藏精选路书列表总页码

    val tankScrollToPosition = MutableLiveData(0) //收藏轨迹路书列表滚动位置
    val tankAllList = ahaTripBusiness.tankCollectAllList //收藏轨迹路书列表总数据
    var tankCurrentPage = 1 //收藏轨迹路书列表页码
    val tankMaxPage = ahaTripBusiness.tankCollectMaxPage //收藏轨迹路书列表总页码

    override fun onCleared() {
        super.onCleared()
        ahaTripBusiness.guideCollectAllList.postValue(arrayListOf())
        ahaTripBusiness.lineCollectAllList.postValue(arrayListOf())
        ahaTripBusiness.tankCollectAllList.postValue(arrayListOf())
    }

    fun setIsGuideFav(isGuideFav: Boolean){
        ahaTripBusiness.setIsGuideFav(isGuideFav)
    }

    fun setIsLineFav(isLineFav: Boolean){
        ahaTripBusiness.setIsLineFav(isLineFav)
    }

    //我的-我的收藏共创路书列表
    fun requestMineCollectGuideList(){
        viewModelScope.launch {
            ahaTripImpl.requestMineCollectGuideList(guideCurrentPage, 10)
        }
    }

    //我的-我的收藏（路书(非共创路书)、轨迹、景点）
    fun requestMineCollectList(type: Int) {//路书1、轨迹12、景点3
        viewModelScope.launch {
            ahaTripImpl.requestMineCollectList(if (type == 1) lineCurrentPage else tankCurrentPage, 10, type)
        }
    }

    //请求收藏/取消收藏
    fun requestFavorite(id: String, type: Int){
        viewModelScope.launch {
            ahaTripImpl.requestFavorite(type.toString(), id, true)
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
}