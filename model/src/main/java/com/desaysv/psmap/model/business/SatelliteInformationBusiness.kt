package com.desaysv.psmap.model.business

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import androidx.core.location.GnssStatusCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.bussiness.common.POIFactory
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.Status
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs


/**
 * 检查卫星和定位信息
 */
@Singleton
class SatelliteInformationBusiness @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchBusiness: SearchBusiness
) : LocationListener {
    //定位状态
    private val _locationState = MutableLiveData(false)
    val locationState: LiveData<Boolean> = _locationState

    //卫星棵树信息 总数，可用颗数，北斗，GPS，GLONASS，UFO
    private val _satelliteCountInfo = MutableLiveData(intArrayOf(0, 0, 0, 0, 0, 0))
    val satelliteCountInfo: LiveData<IntArray> = _satelliteCountInfo

    //定位信息 时间 速度 航向
    private val defaultLocationInfo = arrayOf("未知", "未知", "未知")
    private val _locationInfo = MutableLiveData(defaultLocationInfo)
    val locationInfo: LiveData<Array<String>> = _locationInfo

    private val _address = MutableLiveData("未知地址")
    val address: LiveData<String> = _address

    private val defaultSnrArr = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)//信噪比列表固定显示11个

    //信噪比列表
    private val _satelliteSnrInfo = MutableLiveData(defaultSnrArr)
    val satelliteSnrInfo: LiveData<IntArray> = _satelliteSnrInfo

    //卫星类型 高度角 方位角
    private val _satelliteLocationInfo = MutableLiveData<List<FloatArray>>(emptyList())
    val satelliteLocationInfo: LiveData<List<FloatArray>> = _satelliteLocationInfo

    private val locationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private var mHandlerThread: HandlerThread? = null

    private val sdf: SimpleDateFormat = SimpleDateFormat("HH:mm:ss yyyy年MM月dd日", Locale.getDefault())

    private var lat = 0.0
    private var lon = 0.0


    private val geocoder by lazy {
        Geocoder(context, Locale.getDefault())
    }

    @SuppressLint("MissingPermission")
    fun init() {
        Timber.i("init")
        if (mHandlerThread == null) {
            mHandlerThread = HandlerThread("SatelliteInfoHandlerThread")
            mHandlerThread!!.start()
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            0.0F,
            this,
            mHandlerThread!!.getLooper()
        )
        locationManager.registerGnssStatusCallback(mGnssStatusCallback, Handler(mHandlerThread!!.getLooper()))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Timber.i(
                "init isLocationEnabled=${locationManager.isLocationEnabled} isProviderEnabled=${
                    locationManager.isProviderEnabled(
                        LocationManager.GPS_PROVIDER
                    )
                }"
            )
        }


        /*test
        val data = mutableListOf(
            floatArrayOf(5f, 30f, 60f),
            floatArrayOf(1f, 60f, 100f),
            floatArrayOf(3f, 80f, 200f),
            floatArrayOf(4f, 50f, 200f)
        )
        _satelliteLocationInfo.postValue(data)*/
    }

    fun release() {
        Timber.i("release")
        lat = 0.0
        lon = 0.0
        mHandlerThread?.quit()
        mHandlerThread = null
        locationManager.removeUpdates(this)
        locationManager.unregisterGnssStatusCallback(mGnssStatusCallback)
        _locationState.postValue(false)
        _satelliteCountInfo.postValue(intArrayOf(0, 0, 0, 0, 0, 0))
        _locationInfo.postValue(defaultLocationInfo)
        _satelliteSnrInfo.postValue(defaultSnrArr)
        _satelliteLocationInfo.postValue(emptyList())
        _address.postValue("未知地址")

    }

    private val mGnssStatusCallback = object : GnssStatus.Callback() {

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            updateGnssInfo(status)
        }
    }

    override fun onLocationChanged(location: Location) {
        location.run {
            if (time > 0 && location.longitude != 0.0 && location.latitude != 0.0) {
                Timber.i("onLocationChanged ${location.time}  ${location.longitude} ${location.latitude}")
                val info = defaultLocationInfo.copyOf()
                val datetime = sdf.format(Date(location.time))
                info[0] = datetime
                if (location.hasSpeed())
                    info[1] = (speed * 3.6).toInt().toString().plus("km/h")
                if (location.hasBearing())
                    info[2] = getDirection(location.bearing)
                if (abs(lat - location.latitude) > 0.001 || abs(lon - location.longitude) > 0.001) {
                    lat = location.latitude
                    lon = location.longitude
                    MainScope().launch {
                        withContext(Dispatchers.IO) {
                            getAddress(lat, lon)
                        }
                    }
                }
                _locationInfo.postValue(info)
                _locationState.postValue(true)
            }

        }
    }

    private fun getDirection(bearing: Float): String {
        if (bearing >= 0 && bearing < 15 || bearing >= 345 && bearing < 360) {
            return "北 (N)"
        } else if (bearing >= 15 && bearing < 75) {
            return "东北 (NE)"
        } else if (bearing >= 75 && bearing < 105) {
            return "东 (E)"
        } else if (bearing >= 105 && bearing < 165) {
            return "东南 (SE)"
        } else if (bearing >= 165 && bearing < 195) {
            return "南 (S)"
        } else if (bearing >= 195 && bearing < 255) {
            return "西南 (SW)"
        } else if (bearing >= 255 && bearing < 285) {
            return "西 (W)"
        } else if (bearing >= 285 && bearing < 345) {
            return "西北 (NW)"
        }
        return "unknown"
    }

    private fun updateGnssInfo(status: GnssStatus) {
        Timber.i("updateGnssInfo")
        // 获取卫星颗数的默认最大值
        val maxSatellites = status.satelliteCount
        var availableCount = 0
        var beidouCount = 0
        var gpsCount = 0
        var glonassCount = 0
        var ufoCount = 0
        val srnMaxCount = defaultSnrArr.size
        val srnInfo = defaultSnrArr.copyOf()
        val gnssLocationInfo = mutableListOf<FloatArray>()
        var snrIndex = 0
        for (i in 0 until maxSatellites) {
            val gnssType = status.getConstellationType(i)
            when (gnssType) {
                GnssStatusCompat.CONSTELLATION_BEIDOU -> beidouCount++
                GnssStatusCompat.CONSTELLATION_GPS -> gpsCount++
                GnssStatusCompat.CONSTELLATION_GLONASS -> glonassCount++
                else -> ufoCount++
            }
            //val svid = status.getSvid(i)
            val elevation = status.getElevationDegrees(i)
            val azimuth = status.getAzimuthDegrees(i)
            val snr = status.getCn0DbHz(i)
            if (snr >= 28)
                availableCount++
            if (snrIndex < srnMaxCount && snr > 0) {
                srnInfo[snrIndex] = snr.toInt()
                snrIndex++
            }
            if (snr >= 28) {
                gnssLocationInfo.add(floatArrayOf(gnssType.toFloat(), elevation, azimuth))
            }
        }
        _satelliteCountInfo.postValue(
            intArrayOf(
                maxSatellites,
                availableCount,
                beidouCount,
                gpsCount,
                glonassCount,
                ufoCount
            )
        )
        _satelliteSnrInfo.postValue(srnInfo)
        _satelliteLocationInfo.postValue(gnssLocationInfo)
        Timber.i("updateGnssInfo ${_satelliteCountInfo.value}")
    }

    private suspend fun getAddress(longitude: Double, latitude: Double) {
        Timber.i("getAddress longitude=$longitude latitude=$latitude")
        val result = searchBusiness.nearestSearch(POIFactory.createPOI().apply {
            point = GeoPoint(
                latitude,
                longitude
            )
        })

        when (result.status) {
            Status.SUCCESS -> {
                Timber.i("getAddress SUCCESS")
                val poiList = SearchCommonUtils.invertOrderList(result.data?.poi_list)
                poiList?.run {
                    if (this.isNotEmpty() && !TextUtils.isEmpty(this[0].address)) {
                        val address = "在" + this[0].address + "附近"
                        _address.postValue(address)
                    }
                }
            }

            else -> {
                Timber.i("nearestSearch fail")
            }
        }
    }

}