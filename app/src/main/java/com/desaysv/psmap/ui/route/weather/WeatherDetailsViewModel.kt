package com.desaysv.psmap.ui.route.weather

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.utils.NavigationUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/12/25
 * @description 沿途天气详情卡片ViewModel
 */
@HiltViewModel
class WeatherDetailsViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val application: Application,
    private val mMapDataBusiness: MapDataBusiness
) : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        mRouteBusiness.unitWeatherLabelItem()
    }

    //单个沿途天气数据
    val weatherLabelItem = mRouteBusiness.weatherLabelItem

    val weatherIcon = weatherLabelItem.map {
        it?.let { item -> getWeatherIcon(item.mWeatherID) }
    }

    val weatherCityName = weatherLabelItem.map {
        it?.let { item -> mMapDataBusiness.getCityInfo(item.mCityID)?.cityName ?: "" }
    }

    val weatherDistanceEta = weatherLabelItem.map {
        it?.let { item ->
            "${NavigationUtil.meterToStr(application, item.mDistance)}·${
                AutoRouteUtil.getScheduledTime(
                    application,
                    item.mEta,
                    false
                )
            }"
        }
    }

    val weatherSourceUpdate = weatherLabelItem.map {
        it?.let { item -> application.getString(R.string.sv_route_weather_text_update, getUpdateMinutes(item.mTimestamp)) }
    }

    /**
     * 获取天气icon
     */
    private fun getWeatherIcon(mWeatherID: Int): Int {
        // 根据UED划分天气五大类别分别设置对应背景色：晴天、雷雨、冰雪霜冻、沙尘风暴、雾霾
        when (mWeatherID) {
            /* 台风预警 */
            1 -> return R.drawable.ic_weather_typhoon_1
            /* 暴雨预警 */
            2 -> return R.drawable.ic_weather_rainstorm_2
            /* 暴雪预警 */
            3 -> return R.drawable.ic_weather_blizzard_3
            /* 大风预警 */
            5 -> return R.drawable.ic_weather_gale_5
            /* 沙尘暴预警 */
            6 -> return R.drawable.ic_weather_dust_storm_6
            /*  雷电预警 */
            9 -> return R.drawable.ic_weather_thunder_9
            /*  冰雹预警 */
            10 -> return R.drawable.ic_weather_hail_10
            /*  霜冻预警 */
            11 -> return R.drawable.ic_weather_frost_11
            /*  大雾预警 */
            12 -> return R.drawable.ic_weather_fog_12
            /*  13: 霾预警,  16: 灰霾预警 */
            13, 16 -> return R.drawable.ic_weather_haze_13_16
            /*  14: 道路结冰预警,  21: 道路冰雪预警 */
            14, 21 -> return R.drawable.ic_weather_road_ice_14_21
            /*  雷雨大风预警 */
            17 -> return R.drawable.ic_weather_thunder_gale_17
            /* 晴天 */
            100 -> return if (NightModeGlobal.isNightMode()) R.drawable.ic_weather_sunny_100_night else R.drawable.ic_weather_sunny_100_day
            /* 多云 */
            101 -> return R.drawable.ic_weather_cloudy_101
            /* 102: 少云, 103: 晴间多云 */
            102, 103 -> if (NightModeGlobal.isNightMode()) R.drawable.ic_weather_sunny_cloudy_102_103_night else R.drawable.ic_weather_sunny_cloudy_102_103_day
            /* 阴 */
            104 -> return R.drawable.ic_weather_yintian_104
            /* 200: 有风, 201: 平静, 202: 微风, 203: 和风, 204: 清风 */
            200, 201, 202, 203, 204 -> return R.drawable.ic_weather_windy_200_204
            /* 205: 强风/劲风, 206: 疾风, 207: 大风, 208: 烈风 */
            205, 206, 207, 208 -> return R.drawable.ic_weather_gale_205_208
            /* 209: 风暴, 210: 狂暴风,213: 热带风暴, */
            209, 210, 213 -> return R.drawable.ic_weather_storm_209_210_213
            /*  211: 飓风, 212: 龙卷风 */
            211, 212 -> return R.drawable.ic_weather_tornado_211_212
            /*   300: 阵雨, 306: 中雨 */
            300, 306 -> return R.drawable.ic_weather_moderate_rain_300_306
            /* 301: 强阵雨, 307: 大雨 */
            301, 307 -> return R.drawable.ic_weather_heavy_rain_301_307
            /* 雷阵雨 */
            302 -> return R.drawable.ic_weather_thunder_storm_302
            /* 强雷阵雨 */
            303 -> return R.drawable.ic_weather_strong_thunder_storm_303
            /* 雷阵雨伴有冰雹 */
            304 -> return R.drawable.ic_weather_thunder_storm_hail_304
            /*  305: 小雨, 309: 毛毛雨/细雨 */
            305, 309 -> return R.drawable.ic_weather_light_rain_305_309
            /*   308: 极端降雨, 310: 暴雨, 311: 大暴雨, 312: 特大暴雨  */
            308, 310, 311, 312 -> return R.drawable.ic_weather_rainstorm_308_312
            /* 313: 冻雨, 1001: 冰粒, 1002: 冰针, 1003: 冰雹 */
            313, 1001, 1002, 1003 -> return R.drawable.ic_weather_hail_313_1001_1002_1003
            /* 小雪 */
            400 -> return R.drawable.ic_weather_light_snow_400
            /* 401: 中雪, 407: 阵雪 */
            401, 407 -> return R.drawable.ic_weather_moderate_snow_401_407
            /* 402: 大雪   403：暴雪 */
            402, 403 -> return R.drawable.ic_weather_blizzard_402_403
            /*  404: 雨夹雪, 405: 雨雪天气, 406: 阵雨夹雪 */
            404, 405, 406 -> return R.drawable.ic_weather_sleet_404_405_406
            /*  薄雾 */
            500 -> return R.drawable.ic_weather_mist_500
            /*   大雾 */
            501 -> return R.drawable.ic_weather_fog_501
            /* 雾霾 */
            502 -> return R.drawable.ic_weather_haze_502
            /* 扬沙 */
            503 -> return R.drawable.ic_weather_jansha_503
            /* 浮尘 */
            504 -> return R.drawable.ic_weather_dust_504
            /* 沙尘暴 */
            507 -> return R.drawable.ic_weather_dust_storm_507
            /*  强沙尘暴 */
            508 -> return R.drawable.ic_weather_strong_sandstorms_508
            /* 热 */
            900 -> return R.drawable.ic_weather_hot_900
            /* 冷 */
            901 -> return R.drawable.ic_weather_cold_901
            /* 1004: 雷暴, 1005: 雷电 */
            1004, 1005 -> return R.drawable.ic_weather_thunderstorm_1004_1005

        }
        return if (NightModeGlobal.isNightMode()) R.drawable.ic_weather_sunny_100_night else R.drawable.ic_weather_sunny_100_day
    }

    private fun getUpdateMinutes(mTimestamp: Long): Int {
        val nowSeconds = System.currentTimeMillis() / 1000L
        var spanMinutes: Long = 0
        if (nowSeconds >= mTimestamp) {
            spanMinutes = (nowSeconds - mTimestamp) / 60
        }
        return if (spanMinutes < 1) 1 else spanMinutes.toInt()
    }

}