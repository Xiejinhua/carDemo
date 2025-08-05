package com.desaysv.psmap.model.business

import android.os.Build
import com.desaysv.psmap.base.impl.IModelBusinessProxy
import com.desaysv.psmap.base.common.EVManager
import com.desaysv.psmap.base.common.SimplePOIController
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.dji.navigation.AdasSupportBusiness
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ModelBusinessManager @Inject constructor(
    private val ttsPlayBusiness: TtsPlayBusiness,
    private val outputDataBusiness: OutputDataBusiness,
    private val evManager: EVManager,
    private val simplePOIController: SimplePOIController,
    private val iCarInfoProxy: ICarInfoProxy,
    private val adasSupportBusiness: AdasSupportBusiness

) : IModelBusinessProxy {
    override suspend fun init() {
        withContext(Dispatchers.Main) {
            //iovVoiceBusiness.init() T1N语音提供公版标准jar包
            //tts 初始化
            ttsPlayBusiness.init()
            outputDataBusiness.init()
            evManager.init()
            if (iCarInfoProxy.isJetOurGaoJie()) {
                //高阶车型开启ADAS数据传输
                adasSupportBusiness.init()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                simplePOIController.init()
            }
        }
    }

    override fun unInit() {
        ttsPlayBusiness.unInit()
        outputDataBusiness.unInit()
        if (iCarInfoProxy.isJetOurGaoJie()) {
            adasSupportBusiness.unInit()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            simplePOIController.unInit()
        }
    }
}