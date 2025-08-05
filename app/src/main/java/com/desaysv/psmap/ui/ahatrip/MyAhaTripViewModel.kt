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
 * 我的路书ViewModel
 */
@HiltViewModel
class MyAhaTripViewModel @Inject constructor(
    private val skyBoxBusiness: SkyBoxBusiness,
    private val ahaTripBusiness: AhaTripBusiness,
    private val ahaTripImpl: AhaTripImpl): ViewModel() {
    val themeChange = skyBoxBusiness.themeChange()
    val selectTab = MutableLiveData(true) //顶部tab选择 true.共创路书 false.轨迹路书
    val setToast = ahaTripBusiness.setToast //toast
    val guideList = ahaTripBusiness.guideList //共创路书列表数据
    val guideLoading = ahaTripBusiness.guideLoading //共创路书加载loading
    val deleteGuideResult = ahaTripBusiness.deleteGuideResult //共创路书删除结果
    val tankList = ahaTripBusiness.tankList //轨迹路书列表数据
    val tankLoading = ahaTripBusiness.tankLoading //轨迹路书加载loading
    val deleteTankResult = ahaTripBusiness.deleteTankResult //轨迹路书删除结果

    val guideScrollToPosition = MutableLiveData(0) //共创路书列表滚动位置
    val guideAllList = ahaTripBusiness.guideAllList //共创路书列表总数据
    var guideCurrentPage = 1 //共创路书列表页码
    val guideMaxPage = ahaTripBusiness.guideMaxPage //共创路书列表总页码

    val tankScrollToPosition = MutableLiveData(0) //轨迹路书列表滚动位置
    val tankAllList = ahaTripBusiness.tankAllList //轨迹路书列表总数据
    var tankCurrentPage = 1 //轨迹路书列表页码
    val tankMaxPage = ahaTripBusiness.tankMaxPage //轨迹路书列表总页码

    override fun onCleared() {
        super.onCleared()
        ahaTripBusiness.guideAllList.postValue(arrayListOf())
        ahaTripBusiness.tankAllList.postValue(arrayListOf())
    }

    fun setIsGuideFav(isGuideFav: Boolean){
        ahaTripBusiness.setIsGuideFav(isGuideFav)
    }

    fun setIsTankFav(isTankFav: Boolean){
        ahaTripBusiness.setIsTankFav(isTankFav)
    }

    //我的-我制作的共创路书列表
    fun requestMineGuideList(){
        viewModelScope.launch {
            ahaTripImpl.requestMineGuideList(guideCurrentPage, 10)
        }
    }

    //删除我的共创
    fun requestMineGuideDelete(id: String) {
        viewModelScope.launch {
            ahaTripImpl.requestMineGuideDelete(id)
        }
    }

    //我的-我制作的轨迹列表
    fun requestMineTankList() {
        viewModelScope.launch {
            ahaTripImpl.requestMineTankList(tankCurrentPage, 10)
        }
    }

    //删除我的轨迹
    fun requestMineTankDelete(id: String){
        viewModelScope.launch {
            ahaTripImpl.requestMineTankDelete(id)
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