package com.desaysv.psmap.base.auto.layerstyle

import com.autonavi.gbl.layer.model.DynamicInitParam
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle
import com.autosdk.bussiness.map.SurfaceViewID

interface IPrepareStyleFactory {
    fun getCustomPrepareStyleImpl(): IPrepareLayerStyle
    fun getPrepareLayerStyleImpl(@SurfaceViewID.SurfaceViewID1 surfaceViewID: Int): IPrepareLayerStyle

    fun getPrepareLayerStyleInnerImpl(
        mapview: MapView
    ): IPrepareLayerStyle

    fun getDynamicInitParam(@SurfaceViewID.SurfaceViewID1 surfaceViewID: Int): DynamicInitParam

}