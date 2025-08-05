package com.desaysv.psmap.ui.ahatrip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.business.AhaTripBusiness
import com.desaysv.psmap.model.business.BluetoothBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 景点详情ViewModel
 */
@HiltViewModel
class AhaScenicDetailViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val ahaTripBusiness: AhaTripBusiness,
    private val mBluetoothBusiness: BluetoothBusiness,
    private val ahaTripImpl: AhaTripImpl): ViewModel() {

    val scenicDetail = ahaTripBusiness.scenicDetail //景点详情数据
    val scenicDetailLoading = ahaTripBusiness.scenicDetailLoading //路书详情加载loading
    val themeChange = skyBoxBusiness.themeChange()
    private var scenicId = ""

    fun setScenicId(scenicId: String){
        this.scenicId = scenicId
    }

    //景区详情接口
    fun requestScenicDetail(){
        viewModelScope.launch {
            ahaTripImpl.requestScenicDetail(scenicId)
        }
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

    //景点详情扎点
    fun setScenicDetailPoint(){
        ahaTripBusiness.setScenicDetailPoint()
    }

    fun removeAllLayerItems(bizType: Long) {
        ahaTripBusiness.removeAllLayerItems(bizType)
    }

    //电话拨打
    fun onPhoneCall(phone: String) = mBluetoothBusiness.callPhone(phone)
}