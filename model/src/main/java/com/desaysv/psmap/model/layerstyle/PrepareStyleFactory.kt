package com.desaysv.psmap.model.layerstyle

import android.app.Application
import com.autonavi.gbl.layer.model.DynamicInitParam
import com.autonavi.gbl.layer.model.InnerStyleParam
import com.autonavi.gbl.layer.observer.IBizDynamicAdapter
import com.autonavi.gbl.map.MapView
import com.autonavi.gbl.map.layer.observer.IPrepareLayerStyle
import com.autosdk.bussiness.layer.dynamic.bean.FontBean
import com.autosdk.bussiness.layer.dynamic.bean.InitStyleDSLBean
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.base.auto.layerstyle.IPrepareStyleFactory
import com.google.gson.Gson
import javax.inject.Inject

class PrepareStyleFactory @Inject constructor(
    private val app: Application, private val gson: Gson
) : IPrepareStyleFactory {
    override fun getCustomPrepareStyleImpl(): IPrepareLayerStyle {
        return CustomPrepareStyleImpl(app)
    }

    override fun getPrepareLayerStyleImpl(@SurfaceViewID.SurfaceViewID1 surfaceViewID: Int): IPrepareLayerStyle {
        return PrepareLayerStyleImpl(app, surfaceViewID)
    }

    override fun getPrepareLayerStyleInnerImpl(
        mapview: MapView
    ): IPrepareLayerStyle {
        return PrepareLayerStyleInnerImpl(app, mapview, getInnerStyleParam())
    }

    override fun getDynamicInitParam(@SurfaceViewID.SurfaceViewID1 surfaceViewID: Int): DynamicInitParam {
        val initStyleDSLJson = gson.toJson(getInitStyleDSLBean())
        val initParam = DynamicInitParam()
        initParam.initStyleDSL = initStyleDSLJson
        initParam.dynamicAdapter = getDynamicLayerParamImpl(surfaceViewID)
        return initParam
    }

    private fun getInnerStyleParam(): InnerStyleParam {
        val param = InnerStyleParam()
        val path = AutoConstant.LAYER_ASSET_DIR
        param.layerAssetPath = path
        param.cardCmbPaths.add(path)
        return param
    }


    private fun getDynamicLayerParamImpl(@SurfaceViewID.SurfaceViewID1 surfaceViewID: Int): IBizDynamicAdapter {
        return DynamicLayerParamImpl(surfaceViewID)
    }

    private fun getInitStyleDSLBean(): InitStyleDSLBean {
        val path = AutoConstant.LAYER_ASSET_DIR
        val initStyleDSL = InitStyleDSLBean()
        initStyleDSL.asset_path = path
        initStyleDSL.cmb_name = "libcmb_LayerImages.so"
        val fontBeanList: MutableList<FontBean> = ArrayList()
        val fontBean1 = FontBean(path + "font/font_cn.ttf", "font_cn")
        val fontBean2 = FontBean(path + "font/Oswald-Regular.ttf", "Oswald-Regular")
        val fontBean3 = FontBean(path + "font/Roboto-Bold.ttf", "Roboto-Bold")
        fontBeanList.add(fontBean1)
        fontBeanList.add(fontBean2)
        fontBeanList.add(fontBean3)
        initStyleDSL.font_list = fontBeanList
        return initStyleDSL
    }
}