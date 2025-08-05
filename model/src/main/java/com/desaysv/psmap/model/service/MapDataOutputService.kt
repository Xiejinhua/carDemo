package com.desaysv.psmap.model.service

import android.app.Service
import android.content.Intent
import android.os.DeadObjectException
import android.os.IBinder
import android.os.IBinder.DeathRecipient
import android.text.TextUtils
import com.desaysv.psmap.adapter.IMapData
import com.desaysv.psmap.adapter.IMapDataCallback
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.model.business.OutputDataBusiness
import com.desaysv.psmap.model.impl.IMapDataOutputCallback
import com.dji.navigation.AdasSupportBusiness
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject


@AndroidEntryPoint
class MapDataOutputService : Service(), IMapDataOutputCallback {
    private val mMapDataCallbacks = ConcurrentHashMap<String, IMapDataCallback>()

    private val deathRecipients: MutableMap<String, MyDeathRecipient> = mutableMapOf()

    @Inject
    lateinit var outputDataBusiness: OutputDataBusiness

    @Inject
    lateinit var adasSupportBusiness: AdasSupportBusiness

    @Inject
    lateinit var iCarInfoProxy: ICarInfoProxy

    @Inject
    lateinit var initSDKBusiness: InitSDKBusiness

    override fun onBind(intent: Intent): IBinder {
        Timber.i("onBind")
        outputDataBusiness.registerMapDataOutputCallback(this)
        adasSupportBusiness.registerMapDataOutputCallback(this)
        return mapDataOutput
    }

    override fun onDestroy() {
        super.onDestroy()
        outputDataBusiness.unregisterMapDataOutputCallback(this)
        adasSupportBusiness.unregisterMapDataOutputCallback(this)
        Timber.i("onDestroy")
    }

    inner class MyDeathRecipient(// 客户端标识
        private val pkg: String
    ) : DeathRecipient {
        override fun binderDied() {
            // 处理 Binder 死亡逻辑
            Timber.i("Binder died for client: $pkg")
            // 根据 clientId 移除对应的回调
            mMapDataCallbacks.remove(pkg)
        }
    }

    private val mapDataOutput: IMapData.Stub = object : IMapData.Stub() {

        override fun sendMassage(pkg: String, massage: String) {
            Timber.i("sendMassage")
            if (!initSDKBusiness.isInitSuccess()){
                Timber.i("sendMassage not InitSuccess")
                return
            }
            outputDataBusiness.parseCommandMassage(pkg, massage)
            if (iCarInfoProxy.isJetOurGaoJie()) {
                adasSupportBusiness.parseCommandMassage(pkg, massage)
            }
        }

        override fun getNaviStatus(): Int {
            return outputDataBusiness.getNaviStatus()
        }

        override fun registerMapDataCallback(pkg: String, callback: IMapDataCallback?) {
            Timber.i("registerMapDataCallback pkg = $pkg")
            callback?.let {
                mMapDataCallbacks[pkg] = callback
                try {
                    val deathRecipient = MyDeathRecipient(pkg)
                    callback.asBinder().linkToDeath(deathRecipient, 0)
                    deathRecipients[pkg] = deathRecipient
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }
        }

        override fun unregisterMapDataCallback(pkg: String) {
            Timber.i("unregisterMapDataCallback pkg = $pkg")
            mMapDataCallbacks.remove(pkg)?.let { callback ->
                deathRecipients.remove(pkg)?.let {
                    try {
                        callback.asBinder().unlinkToDeath(it, 0)
                    } catch (e: Exception) {
                        Timber.w(e)
                    }
                }
            }
        }
    }

    override fun onMapData(pkg: String, jsonData: String) {
        try {
            mMapDataCallbacks[pkg]?.onMassage(pkg, jsonData)
        } catch (e: Exception) {
            Timber.w(e)
        }
    }

    override fun onMapDataToAllPackage(jsonData: String) {
        mMapDataCallbacks.map {
            try {
                it.value.onMassage(it.key, jsonData)
            } catch (e: DeadObjectException) {
                Timber.e("Remote service is dead")
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }

    override fun onMapByteData(pkg: String?, jsonData: String?, byteArray: ByteArray?) {
        if (TextUtils.isEmpty(pkg)) {
            mMapDataCallbacks.map {
                try {
                    it.value.onByteMassage(it.key, jsonData, byteArray)
                } catch (e: Exception) {
                    Timber.w(e)
                }
            }
        } else {
            try {
                mMapDataCallbacks[pkg]?.onByteMassage(pkg, jsonData, byteArray)
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }
}