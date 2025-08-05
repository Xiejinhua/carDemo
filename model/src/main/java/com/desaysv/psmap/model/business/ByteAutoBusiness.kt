package com.desaysv.psmap.model.business

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.common.model.RectDouble
import com.autonavi.gbl.layer.model.BizCustomPointInfo
import com.autonavi.gbl.layer.model.BizCustomTypePoint
import com.autonavi.gbl.layer.model.BizLayerUtil
import com.autonavi.gbl.map.layer.observer.ILayerClickObserver
import com.autonavi.gbl.map.model.PreviewParam
import com.autosdk.bussiness.layer.CustomLayer
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.common.utils.AssertUtils
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.net.NetworkConstants
import com.desaysv.psmap.base.net.URLConfig
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.bean.JetourPoi
import com.desaysv.psmap.model.net.bean.ByteAutoRequestBody
import com.desaysv.psmap.model.net.bean.ByteAutoResult
import com.desaysv.psmap.model.net.bean.Observation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.HmacUtils
import timber.log.Timber
import java.io.IOException
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import kotlin.coroutines.resume
import kotlin.math.max
import kotlin.math.min

//捷途探趣
@Singleton
class ByteAutoBusiness @Inject constructor(
    private val mLayerController: LayerController,
    private val application: Application,
    private val mLocationBusiness: LocationBusiness,
    private val mMapController: MapController,
    private val carInfo: ICarInfoProxy,
    private var gson: Gson
) {
    companion object {
        const val AK = "jetourdesaysvo4fsx25qdzesjyif0ox"
        const val SK = "oai8bsbck2j6bj68l7ze4c0d25o1yymk"
        const val CHANNEL = "jetour"
        const val APPID = "99183"
        const val ACTION_BYTEDANCE_ON_APP_FOREGROUND = "com.bytedance.byteautoservice3.ACTION_ON_APP_FOREGROUND"
        const val ACTION_BYTEDANCE_ON_APP_BACKGROUND = "com.bytedance.byteautoservice3.ACTION_ON_APP_BACKGROUND"
    }

    private lateinit var okHttpClient: OkHttpClient
    private var eventSource: EventSource? = null
    private var JetourPoiList: ArrayList<JetourPoi> = ArrayList()

    val isByteDanceForeground = MutableLiveData(false)

    private var mByteDanceBroadcastReceiver: ByteDanceBroadcastReceiver? = null

    private var isInit = false

    private val mCustomLayer: CustomLayer by lazy {
        mLayerController.getCustomLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    suspend fun init() = withContext(Dispatchers.IO) {
        if (isInit) {
            return@withContext
        }
        isInit = true
        okHttpClient = getOkHttpClient()
        JetourPoiList = getJetourPoiList()
        Timber.d("JetourPoiList size: ${JetourPoiList.size}")

        mByteDanceBroadcastReceiver = ByteDanceBroadcastReceiver(this@ByteAutoBusiness)

        Timber.d("  registerByteDanceBroadcastReceiver")
        val intentFilter = IntentFilter().apply {
            addAction(ACTION_BYTEDANCE_ON_APP_FOREGROUND)
            addAction(ACTION_BYTEDANCE_ON_APP_BACKGROUND)
        }
        application.registerReceiver(mByteDanceBroadcastReceiver, intentFilter)
    }

    //从assets的JetourPoiList.json文件中读取数据后，计算poi点与当前位置的距离写在distance中
    suspend fun fefreshJetourPoiListDistance(): ArrayList<JetourPoi> {
        return withContext(Dispatchers.IO) {
            JetourPoiList = getJetourPoiList()
            val location = mLocationBusiness.getLastLocation()
            val startPoint = Coord2DDouble(location.longitude, location.latitude)
            JetourPoiList.forEach { poi ->
                val endPoint = Coord2DDouble(poi.longitude, poi.latitude)
                poi.distance = BizLayerUtil.calcDistanceBetweenPoints(startPoint, endPoint).toInt()
            }
            JetourPoiList.sortBy { it.distance }
            Timber.d("JetourPoiList sorted by distance: ${JetourPoiList.map { it.distance }}")
            JetourPoiList
        }
    }

    //从assets的JetourPoiList.json文件中读取数据
    private suspend fun getJetourPoiList(): ArrayList<JetourPoi> {
        return withContext(Dispatchers.IO) {
            AssertUtils.readAssetsFile(application, "JetourPoiList.json")?.let { json ->
                val type = object : TypeToken<List<JetourPoi>>() {}.type
                return@withContext gson.fromJson(json, type)
            } ?: ArrayList()
        }
    }

    private fun getOkHttpClient(): OkHttpClient {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagerFactory.trustManagers, null)
        return OkHttpClient.Builder()
            .callTimeout(NetworkConstants.TIME_OUT_REQUEST.toLong(), TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .connectTimeout(NetworkConstants.TIME_OUT_CONNECT.toLong(), TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.TIME_OUT_READ.toLong(), TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.TIME_OUT_WRITE.toLong(), TimeUnit.SECONDS)
            .build()
    }

    suspend fun getByteAutoVideoSchemaByKeyword(keyword: String?): String? {
        val requestBody = ByteAutoRequestBody(
            query = "$keyword+越野",
            chat_id = "CG1000002" + ((Math.random() * 9 + 1).toInt() * 100000),
            intent_type = "douyin_video",
            intent_log_id = "",
            plugin_name = "video",
            question_id = "",
            plugin_params = "{\"data_source\":\"auto_service_encrypt\"}"
        )
        val result = getVideoByteAutoResult(requestBody)
        if (result?.observation.isNullOrEmpty()) {
            return null
        } else {
            val observation = gson.fromJson(result?.observation, Observation::class.java)
            return observation?.schema
        }
    }

    private suspend fun getVideoByteAutoResult(requestBody: ByteAutoRequestBody): ByteAutoResult? =
        withTimeoutOrNull(NetworkConstants.TIME_OUT_REQUEST * 1000L) {
            suspendCancellableCoroutine { continuation ->
                val queryMap = getHeaderMap(requestBody.chat_id)
                val sign = getSign(
                    "POST",
                    queryMap.entries.joinToString("&") { "${it.key}=${it.value}" },
                    SK,
                    gson.toJson(requestBody)
                )
                val request = Request.Builder()
                    .url(URLConfig.JETOUT_BYTE_AUTO_URL + URLConfig.JETOUT_BYTE_AUTO_GET_VIDEO_SCHEMA_URL + "?" + queryMap.entries.joinToString("&"))
                    .header("Accept", "text/event-stream")
                    .header("X-Signature", "$AK:$sign")
                    .header("X-Use-PPE", "1")
                    .header("X-Tt-Env", "ppe_vehicle_model_test")
                    .header("Content-Type", "application/json;charset=utf-8")
                    .post(gson.toJson(requestBody).toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull()))
                    .build()
                val factory = EventSources.createFactory(okHttpClient)
                eventSource = factory.newEventSource(request, object : EventSourceListener() {
                    override fun onOpen(eventSource: EventSource, response: Response) {
                        Timber.d("SSE 连接已建立: ${response.message}")
                    }

                    override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                        Timber.d("收到 SSE 事件: ID: $id, 类型: $type, 数据: $data")
                        try {
                            val result = gson.fromJson(data, ByteAutoResult::class.java)
                            if (result?.observation != null) {
                                Timber.d("observation 数据: ${result.observation}")
                                continuation.resume(result)
                            } else {
                                Timber.d("没有 observation 数据")
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "解析 SSE 数据失败, e = ${e.message}")
                        }
                    }

                    override fun onClosed(eventSource: EventSource) {
                        Timber.d("SSE 连接已关闭")
                    }

                    override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                        Timber.d("SSE 连接失败，状态码: ${response?.code}")
                        try {
                            Timber.d("响应体: ${response?.body?.string()}")
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        eventSource.cancel()
                    }
                })
            }
        }

    private fun getHeaderMap(chatId: String) = hashMapOf(
        "_timestamp" to (System.currentTimeMillis() / 1000).toString(),
        "_nonce" to (0..65535).random().toString(),
        "channel" to CHANNEL,
        "app_id" to APPID,
        "vehicle_id" to carInfo.uuid!!,
        "vehicle_type" to carInfo.partNum
    ).also { Timber.d("getHeaderMap headerMap: $it") }

    private fun getSign(method: String, queryStr: String, sk: String, body: String?): String {
        val sortedQueryStr = queryStr.split("&").sorted().joinToString("&")
        val sb = StringBuilder().apply {
            append(method).append("\n")
            append(sortedQueryStr).append("\n")
            if (!body.isNullOrEmpty()) append(DigestUtils.md5Hex(body)).append("\n")
        }
        val strToSign = sb.toString()
        val rawHmac = HmacUtils.hmacSha256(
            sk.toByteArray(Charsets.UTF_8),
            strToSign.toByteArray(Charsets.UTF_8)
        )
        return Base64().encodeToString(rawHmac)
    }

    class ByteDanceBroadcastReceiver(private val byteAutoBusiness: ByteAutoBusiness) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Timber.d("ByteDanceBroadcastReceiver onReceive action: ${intent.action}")
            if (TextUtils.equals(ACTION_BYTEDANCE_ON_APP_FOREGROUND, intent.action)) {
                byteAutoBusiness.isByteDanceForeground.postValue(true)
            } else if (TextUtils.equals(ACTION_BYTEDANCE_ON_APP_BACKGROUND, intent.action)) {
                byteAutoBusiness.isByteDanceForeground.postValue(false)
            }
        }
    }

    private var poiList: ArrayList<BizCustomPointInfo>? = null

    /**
     * 捷途探趣POI扎点（
     */
    fun updateJetourPoi(list: ArrayList<BizCustomPointInfo>) {
        Timber.i("updateJetourPoi list.size = ${list.size}")
        //保存当前显示的poi点，计算全览画面需要用到
        poiList = list
        mCustomLayer.showJetourListPoint(list)
    }

    fun setFocus(type: Int, id: String?, isFocus: Boolean) {
        mCustomLayer.setCustomLayerItemFocus(type,id,isFocus)
    }

    fun  clearAllItem() {
        mCustomLayer.removeLayerItems(BizCustomTypePoint.BizCustomTypePoint5.toLong())
    }

    /**
     * 显示全览
     *
     */
    fun showPreview(bAnimation: Boolean = true) {
        Timber.i("showPreview")
        getPoiAlongBound(poiList)?.let { mapRect ->
            val previewParam = PreviewParam().apply {
                leftOfMap = CommonUtils.getAutoDimenValue(
                    application, R.dimen.sv_dimen_0
                )
                topOfMap = 0
                screenLeft = CommonUtils.getAutoDimenValue(
                    application,R.dimen.sv_dimen_1544
                )
                screenTop = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_200)
                screenRight = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_200)
                screenBottom = CommonUtils.getAutoDimenValue(application, R.dimen.sv_dimen_100)
                bUseRect = true
                mapBound = mapRect
            }
            mMapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
                ?.showPreview(previewParam, bAnimation, 500, -1)
        }
    }

    /**
     * 添加/移除指定的图层点击观测者
     *
     * @param observer   图层点击observer,若为null,则不作操作
     * @param removeOnly true-仅移除指定的observer, false-添加指定的observer
     */
    fun setLayerClickObserver(observer: ILayerClickObserver?, removeOnly: Boolean) {
        if (observer == null ) {
            return
        }
        mCustomLayer.removeClickObserver(observer)
        if (!removeOnly) {
            mCustomLayer.addClickObserver(observer)
        }
    }


    /**
     * 计算搜索结果所有的Bound
     *
     * @param pois
     * @return
     */
    fun getPoiAlongBound(pois: List<BizCustomPointInfo>?): RectDouble? {
        if (pois.isNullOrEmpty()) {
            return null
        }
        try {
            var x1 = Double.MAX_VALUE
            var y1 = Double.MAX_VALUE
            var x2 = Double.MIN_VALUE
            var y2 = Double.MIN_VALUE
            for (i in pois.indices) {
                val oItem = pois[i]
                x1 = min(x1, oItem.mPos3D.lon)
                y1 = min(y1, oItem.mPos3D.lat)
                x2 = max(x2, oItem.mPos3D.lon)
                y2 = max(y2, oItem.mPos3D.lat)
            }
            val rect = RectDouble(x1, x2, y2, y1)
            return rect
        } catch (e: java.lang.Exception) {
            return null
        }
    }
}