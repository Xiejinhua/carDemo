package com.desaysv.psmap.base.business

import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.autonavi.gbl.aosclient.model.GAddressPredictRequestParam
import com.autonavi.gbl.aosclient.model.GAddressPredictResponseParam
import com.autonavi.gbl.aosclient.observer.ICallBackAddressPredict
import com.autonavi.gbl.common.model.Coord2DDouble
import com.autonavi.gbl.information.travel.model.TravelBeforeNaviRecommendResult
import com.autonavi.gbl.information.travel.model.TravelRecommendBeforeNaviRequest
import com.autonavi.gbl.information.travel.model.TravelRecommendCertainty
import com.autonavi.gbl.user.forcast.model.ArrivedType
import com.autonavi.gbl.user.forcast.model.ArrivedType.ArrivedType1
import com.autonavi.gbl.user.forcast.model.ForcastArrivedData
import com.autonavi.gbl.user.forcast.model.ForcastArrivedParam
import com.autonavi.gbl.user.forcast.model.OftenArrivedItem
import com.autonavi.gbl.user.forcast.observer.IForcastServiceObserver
import com.autonavi.gbl.user.model.UserLoginInfo
import com.autonavi.gbl.util.model.Date
import com.autonavi.gbl.util.model.DateTime
import com.autonavi.gbl.util.model.Time
import com.autosdk.bussiness.account.ForecastController
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.common.POIFactory
import com.autosdk.bussiness.information.InformationController
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.Result
import com.desaysv.psmap.base.utils.Status
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * 预测模块 和 信息模块初始化
 * forecastController 和 InformationController都写在这里
 */
@Singleton
class ForecastBusiness @Inject constructor(
    private val forecastController: ForecastController,
    private val locationBusiness: LocationBusiness,
    private val dataBusiness: MapDataBusiness,
    private val mapController: MapController,
    private val gson: Gson,
    private val informationController: InformationController,
    private val userBusiness: UserBusiness
) : IForcastServiceObserver {

    @Inject
    lateinit var searchBusiness: SearchBusiness

    fun init(path: String): Boolean {
        informationController.initService()
        val flag = forecastController.init(path)
        Timber.i("init $flag")
        forecastController.addForecastObserver(this)
        return flag
    }

    fun unInit() {
        forecastController.unInit()
        forecastController.unregisterForecastObserver()
        informationController.unInit()
        Timber.i("unInit")
    }

    override fun onInit(result: Int) {
        super.onInit(result)
        if (!TextUtils.isEmpty(BaseConstant.uid))
            setLogin()
        Timber.i("onInit result=$result")
    }

    override fun onSetLoginInfo(result: Int) {
        super.onSetLoginInfo(result)
        Timber.i("onSetLoginInfo result=$result")
    }

    fun getArrivedDataList(@ArrivedType1 type: Int): ArrayList<OftenArrivedItem>? {
        return forecastController.getArrivedDataList(type)
    }

    fun setLogin() {
        Timber.i("forecastSetLogin uid=${BaseConstant.uid}")
        if (!TextUtils.isEmpty(BaseConstant.uid))
            forecastController.setLogin(UserLoginInfo(BaseConstant.uid))
    }

    suspend fun getOnlineForecastArrivedData(): Result<List<POI>> {
        Timber.i("getOnlineForecastArrivedData")
        if (TextUtils.isEmpty(BaseConstant.uid))
            return Result.error("not login!")
        return withContext(Dispatchers.IO) {
            withTimeoutOrNull(8000) {
                suspendCancellableCoroutine { continuation ->
                    val param = ForcastArrivedParam().apply {
                        val lastLocation = locationBusiness.getLastLocation()
                        this.adCode =
                            dataBusiness.getAdCodeByLonLat(
                                lastLocation.longitude,
                                lastLocation.latitude
                            )
                                .toString()
                        this.userLoc = Coord2DDouble(lastLocation.longitude, lastLocation.latitude)
                        this.nLevel =
                            mapController.getZoomLevel(SurfaceViewID.SURFACE_VIEW_ID_MAIN).toInt()
                        this.userId = BaseConstant.uid
                        Timber.i("getOnlineForecastArrivedData longitude=${lastLocation.longitude} adCode=$adCode nLevel=$nLevel userId=$userId")
                    }
                    forecastController.getOnlineForecastArrivedData(
                        param,
                        object : IForcastServiceObserver {
                            override fun onForcastArrivedData(data: ForcastArrivedData?) {
                                val pois = arrayListOf<POI>()
                                data?.run {
                                    Timber.i(
                                        "getOnlineForecastArrivedData onForcastArrivedData ${
                                            gson.toJson(
                                                data
                                            )
                                        }"
                                    )
                                    if (!this.others.isNullOrEmpty()) {
                                        this.others[0].run {
                                            pois.add(POIFactory.createPOI().apply {
                                                id = wstrPoiID
                                                point = GeoPoint(stDisplayCoord.lon, stDisplayCoord.lat)
                                                name = wstrPoiName
                                                addr = wstrAddress
                                            })
                                        }
                                    }
                                    this.home?.run {
                                        if (!TextUtils.isEmpty(wstrPoiName)) {
                                            pois.add(POIFactory.createPOI().apply {
                                                id = wstrPoiID
                                                point = GeoPoint(stDisplayCoord.lon, stDisplayCoord.lat)
                                                name = wstrPoiName
                                                addr = wstrAddress
                                            })
                                        }
                                    }
                                    this.company?.run {
                                        if (!TextUtils.isEmpty(wstrPoiName)) {
                                            pois.add(POIFactory.createPOI().apply {
                                                id = wstrPoiID
                                                point = GeoPoint(stDisplayCoord.lon, stDisplayCoord.lat)
                                                name = wstrPoiName
                                                addr = wstrAddress
                                            })
                                        }
                                    }
                                }
                                continuation.resume(Result.success(pois))
                            }
                        })
                }
            } ?: Result.error("Time Out!")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addLocalArrivedData(@ArrivedType1 type: Int = ArrivedType.ForcastLocal, poi: POI) {
        forecastController.addLocalArrivedData(type, OftenArrivedItem().apply {
            wstrPoiName = poi.name // POI 名称
            wstrPoiID = poi.id // PoiID (0<=length<512)
            wstrAddress = poi.addr // Poi地址
            stDisplayCoord = Coord2DDouble(poi.point.longitude, poi.point.latitude)
            stNaviCoord = Coord2DDouble(poi.point.longitude, poi.point.latitude)
            dateTime = DateTime().apply {
                val currentDateTime = LocalDateTime.now()
                date = Date().apply {
                    year = currentDateTime.year.toShort()
                    month = currentDateTime.month.value.toByte()
                    day = currentDateTime.dayOfMonth.toByte()
                    week = currentDateTime.dayOfWeek.value.toByte()
                }
                time = Time().apply {
                    hour = currentDateTime.hour.toByte()
                    minute = currentDateTime.minute.toByte()
                    second = currentDateTime.second.toByte()
                }
            }
        })
    }

    fun delLocalArrivedData(@ArrivedType1 type: Int, name: String?) {
        forecastController.delLocalArrivedData(type, name)
    }

    /**
     * 下面是信息服务功能
     */

    /**
     * 快速出发
     */
    suspend fun requestTravelRecommendBeforeNavi(): POI? {
        Timber.i("requestTravelRecommendBeforeNavi")
        if (TextUtils.isEmpty(BaseConstant.uid)) {
            Timber.i("requestTravelRecommendBeforeNavi fail no login")
            return null
        }
        return withContext(Dispatchers.IO) {
            val result = withTimeoutOrNull(8000) {
                suspendCancellableCoroutine { continuation ->
                    informationController.requestTravelRecommendBeforeNavi(TravelRecommendBeforeNaviRequest().apply {
                        certainty = TravelRecommendCertainty.Strong
                        count = 3
                    }) { result ->
                        var poi: POI? = null
                        if (result is TravelBeforeNaviRecommendResult) {
                            result.poiInfos?.forEach { bean ->
                                if (!TextUtils.isEmpty(bean.poiId)) {
                                    poi = POIFactory.createPOI().apply {
                                        id = bean.poiId
                                        name = bean.poiName
                                    }
                                    return@forEach
                                }
                            }
                        }
                        continuation.resume(Result.success(poi))
                    }
                }
            } ?: Result.error("Time Out!")

            when (result.status) {
                Status.SUCCESS -> {
                    if (result.data == null) {
                        Timber.i("requestTravelRecommendBeforeNavi result POI is null")
                        return@withContext null
                    } else {
                        result.data.run {
                            val searchResult = searchBusiness.poiIdSearchV2(this)
                            when (searchResult.status) {
                                Status.SUCCESS -> {
                                    searchResult.data?.poiList?.run {
                                        if (this.isNotEmpty()) {
                                            this[0]?.basicInfo?.run {
                                                result.data.id = this.poiId
                                                result.data.addr = this.address
                                                result.data.point = GeoPoint(this.location.lon, this.location.lat)
                                                return@withContext result.data
                                            }
                                        }
                                        Timber.i("requestTravelRecommendBeforeNavi searchResult POI is null")
                                        return@withContext null

                                    }
                                }

                                else -> {
                                    Timber.i(
                                        "requestTravelRecommendBeforeNavi searchResult fail ${result.data} ${
                                            result
                                                .throwable?.toString()
                                        }"
                                    )
                                    return@withContext null
                                }
                            }
                        }
                    }

                }

                else -> {
                    Timber.i(
                        "requestTravelRecommendBeforeNavi fail ${result.data} ${
                            result
                                .throwable?.toString()
                        }"
                    )
                    return@withContext null
                }

            }
        }
    }

    /**
     * 预测家/公司地址
     */
    suspend fun requestAddressPredict(): Map<String, POI> {
        Timber.i("requestAddressPredict uid=${BaseConstant.uid}")
        if (TextUtils.isEmpty(BaseConstant.uid))
            return emptyMap()
        return withContext(Dispatchers.IO) {
            withTimeoutOrNull(8000) {
                suspendCancellableCoroutine { continuation ->
                    val requestParam = GAddressPredictRequestParam().apply {
                        uid = BaseConstant.uid
                        queryType = BaseConstant.REQ_ADDRESS_QUERY_TYPE
                        label = BaseConstant.REQ_ADDRESS_LABEL_HOME_COMPANY
                    }
                    userBusiness.sendReqAddressPredict(requestParam, object : ICallBackAddressPredict {
                        override fun onRecvAck(data: GAddressPredictResponseParam?) {
                            Timber.i(
                                "requestAddressPredict data=${
                                    gson.toJson(
                                        data?.vctPredictList
                                    )
                                }"
                            )
                            val pois = mutableMapOf<String, POI>()
                            data?.vctPredictList?.forEach { ppoi ->
                                if (!TextUtils.isEmpty(ppoi.label))
                                    pois[ppoi.label] = ConverUtils.converPredictInfoToPoi(ppoi)
                            }
                            continuation.resume(pois)
                        }
                    })
                }
            } ?: emptyMap()
        }
    }
}