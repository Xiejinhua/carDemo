package com.desaysv.psmap.ui.route

import androidx.lifecycle.ViewModel
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.map.SurfaceViewID
import com.desaysv.psmap.base.business.RouteBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/12/25
 * @description 云控信息 避开拥堵 禁行ViewModel
 */
@HiltViewModel
class PriorTipViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val layerController: LayerController,
) : ViewModel() {

    private val customLayer: CustomLayer by lazy {
        layerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    //云控信息 避开拥堵 禁行
    var avoidTrafficJamsBean = mRouteBusiness.avoidTrafficJamsBean

    override fun onCleared() {
        super.onCleared()
        customLayer.hideCustomTypePoint1()
    }
}