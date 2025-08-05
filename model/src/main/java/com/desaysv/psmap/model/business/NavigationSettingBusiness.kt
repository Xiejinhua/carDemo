package com.desaysv.psmap.model.business

import android.annotation.SuppressLint
import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.data.model.MapDataFileType
import com.autonavi.gbl.data.model.MapNum
import com.autonavi.gbl.data.observer.IMapNumObserver
import com.autonavi.gbl.guide.model.guidecontrol.Param
import com.autonavi.gbl.guide.model.guidecontrol.Type
import com.autonavi.gbl.route.model.RouteControlKey
import com.autonavi.gbl.user.behavior.model.ConfigKey
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.layer.MapLayer
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.widget.route.constant.ConfigRoutePreference
import com.autosdk.bussiness.widget.route.utils.AutoRouteUtil
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.AutoStatus
import com.autosdk.common.CommonConfigValue
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.model.R
import com.desaysv.psmap.model.utils.DataCleanUtils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 导航设置相关业务
 * 比如路线偏好
 */
@Singleton
class NavigationSettingBusiness @Inject constructor(
    private val settingComponent: ISettingComponent,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val settingAccountBusiness: SettingAccountBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val userBusiness: UserBusiness,
    private val mapBusiness: MapBusiness,
    private val application: Application,
    private val mLayerController: LayerController,
    private val locationBusiness: LocationBusiness,
    private val speechSynthesizeBusiness: SpeechSynthesizeBusiness,
    private val iCarInfoProxy: ICarInfoProxy,
    private val mNaviController: NaviController,
    private val naviRepository: INaviRepository
) {
    val rsDefaultSelected = MutableLiveData(false)
    val rsTmcSelected = MutableLiveData(false)
    val rsMoneySelected = MutableLiveData(false)
    val rsFreewayNoSelected = MutableLiveData(false)
    val rsFreewayYesSelected = MutableLiveData(false)
    val rsFreewayQuickSelected = MutableLiveData(false)
    val rsFreewayBigSelected = MutableLiveData(false)
    val saveStrategy = MutableLiveData<Boolean>()
    val preferenceName = MutableLiveData<String>()
    val incrementalRouteNotice = MutableLiveData<Boolean>()//路线重算通知

    val roadConditionsAhead = MutableLiveData(false) //前方路况 默认关闭
    val electronicEyeBroadcast = MutableLiveData(false) //电子眼播报 默认关闭
    val safetyReminder = MutableLiveData(false) //安全提醒 默认关闭
    val cruiseBroadcast = MutableLiveData<Boolean>() //巡航播报开关 false.关闭 true.打开
    val ahaScenicBroadcast = MutableLiveData<Boolean>() //巡航景点推荐 false.关闭 true.打开
    val viewOfMap = MutableLiveData<Int>() // 0: 2D车首上; 1: 3D车首上; 2: 2D北上
    val tmc = MutableLiveData<Int>() //路况 1. 打开 0.关闭
    val mediaType = MutableLiveData(-1) //0. 降低 1.不变
    val volumeModel = MutableLiveData<Int>() //1.详细播报 2.简洁播报 3.极简
    val volumeMute = MutableLiveData<Int>() //1.静音 0.非静音
    val naviParkType = MutableLiveData<Int>() //1. 打开 0.关闭
    val dayNightType = MutableLiveData<Int>() //16. 自动 17.白天 18.夜间
    val dayNightSwitch = MutableLiveData<Boolean>() //工程模式是否打开地图内部的日夜模式设置，基线默认打开
    val mapType = MutableLiveData<Int>() //0 小地图  1 光柱图 2 极简
    val mapFont = MutableLiveData<Int>() //地图字体大小 1标准 2大
    val personalizationCar = MutableLiveData<Int>() //个性化车标设置 1.捷途车标；2.汽车；3.普通车标
    val showCarCompass = MutableLiveData<Boolean>() //是否显示车标罗盘 true打开 false关闭
    val intentionNavigation = MutableLiveData<Boolean>() //保存意图导航状态 true打开 false关闭
    val appCache = MutableLiveData<String>() //APP缓存
    val setToast = MutableLiveData<String>() //显示toast

    val walkEnable = MutableLiveData<Boolean>() //步行导航开关选择状态
    val walkChecked = MutableLiveData<Boolean>() //步行导航开关选择状态

    val favoriteChecked = MutableLiveData<Boolean>() //收藏点开关选择状态
    val favoriteEnable = MutableLiveData<Boolean>() //收藏点开关选择状态

    val scaleChecked = MutableLiveData<Boolean>() //智能比例尺开关选择状态
    val publicationStr = MutableLiveData<String>() //出版物审图号信息
    val internetStr = MutableLiveData<String>() //互联网审图号信息
    val dataFileVersionStr = MutableLiveData<String?>() //数据版本号

    val version = MutableLiveData(String.format(application.getString(R.string.sv_setting_about_engine_version), BaseConstant.JT_VERSION)) //版本号
    val isMapSetting = MutableLiveData(false) //是否在地图设置设置界面
    val returnAllSetting = MutableLiveData<Boolean>() //恢复出厂设置

    var routePreference = "0" //路线偏好策略
    private var volumeModelInt = 0 //播报模式
    private var volumeMuteInt = 0 //静音模式
    private var viewModelInt = 0 //默认视图
    private var dayNightCheck = false
    private var mContextNum = 2
    private var mNum = 0
    private val internet = "internet"
    private val publication = "publication"
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    //地图图层MapLayer
    private val aMapLayer: MapLayer? by lazy {
        mLayerController.getMapLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
    }

    fun setIncrementalRouteNotice(isIncrementalRouteNotice: Boolean = false) {
        incrementalRouteNotice.postValue(isIncrementalRouteNotice)
    }

    /**
     * 获取路线偏好
     */
    fun getConfigKeyPlanPref() {
        setIncrementalRouteNotice()
        routePreference = try {
            settingComponent.getConfigKeyPlanPref()
        } catch (e: NumberFormatException) {
            "0"
        }
        Timber.d("setData routePreference: $routePreference")
        refreshPreference(routePreference)
        getPreferenceName(routePreference)
    }

    /**
     * 刷新偏好设置
     * @param prefer
     */
    private fun refreshPreference(prefer: String) {
        when (prefer) {
            ConfigRoutePreference.PREFERENCE_DEFAULT -> rsDefaultSelected.postValue(true)
            ConfigRoutePreference.PREFERENCE_AVOID_JAN -> rsTmcSelected.postValue(true)
            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE -> rsMoneySelected.postValue(true)
            ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY -> rsFreewayNoSelected.postValue(true)
            ConfigRoutePreference.PREFERENCE_USING_HIGHWAY -> rsFreewayYesSelected.postValue(true)
            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE -> {
                rsTmcSelected.postValue(true)
                rsMoneySelected.postValue(true)
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY -> {
                rsTmcSelected.postValue(true)
                rsFreewayNoSelected.postValue(true)
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY -> {
                rsTmcSelected.postValue(true)
                rsFreewayYesSelected.postValue(true)
            }

            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY -> {
                rsMoneySelected.postValue(true)
                rsFreewayNoSelected.postValue(true)
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY -> {
                rsTmcSelected.postValue(true)
                rsMoneySelected.postValue(true)
                rsFreewayNoSelected.postValue(true)
            }

            ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST -> rsFreewayQuickSelected.postValue(true)

            ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST -> rsFreewayBigSelected.postValue(true)

            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST -> {
                rsTmcSelected.postValue(true)
                rsFreewayQuickSelected.postValue(true)
            }

            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST -> {
                rsTmcSelected.postValue(true)
                rsFreewayBigSelected.postValue(true)
            }

            else -> {}
        }
    }

    /**
     * 偏好点击事件处理
     * @param prefer
     */
    fun preferSelect(prefer: String, check: Boolean) {
        Timber.i("preferSelect is called prefer = $prefer，check = $check")
        when (prefer) {
            ConfigRoutePreference.PREFERENCE_DEFAULT -> defaultSelect() //选择智能推荐/都不选择时默认智能推荐
            ConfigRoutePreference.PREFERENCE_AVOID_JAN -> if (check) {
                rsTmcSelected.postValue(false)
            } else {
                rsDefaultSelected.postValue(false)
                rsTmcSelected.postValue(true)
            }

            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE -> {
                rsDefaultSelected.postValue(false)
                rsFreewayYesSelected.postValue(false)
                rsFreewayQuickSelected.postValue(false)
                rsFreewayBigSelected.postValue(false)
                rsMoneySelected.postValue(!check)
            }

            ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY -> if (check) {
                rsFreewayNoSelected.postValue(false)
            } else {
                rsDefaultSelected.postValue(false)
                rsFreewayNoSelected.postValue(true)
                rsFreewayYesSelected.postValue(false)
                rsFreewayQuickSelected.postValue(false)
                rsFreewayBigSelected.postValue(false)
            }

            ConfigRoutePreference.PREFERENCE_USING_HIGHWAY -> //高速优先与避免收费、不走高速互斥
                if (check) {
                    rsFreewayYesSelected.postValue(false)
                } else {
                    rsDefaultSelected.postValue(false)
                    rsFreewayYesSelected.postValue(true)
                    rsMoneySelected.postValue(false)
                    rsFreewayNoSelected.postValue(false)
                    rsFreewayQuickSelected.postValue(false)
                    rsFreewayBigSelected.postValue(false)
                }

            ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST -> //速度最快
                if (check) {
                    rsFreewayQuickSelected.postValue(false)
                } else {
                    rsFreewayQuickSelected.postValue(true)
                    rsDefaultSelected.postValue(false)
                    rsMoneySelected.postValue(false)
                    rsFreewayNoSelected.postValue(false)
                    rsFreewayYesSelected.postValue(false)
                    rsFreewayBigSelected.postValue(false)
                }

            ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST -> //大路优先
                if (check) {
                    rsFreewayBigSelected.postValue(false)
                } else {
                    rsFreewayBigSelected.postValue(true)
                    rsDefaultSelected.postValue(false)
                    rsMoneySelected.postValue(false)
                    rsFreewayNoSelected.postValue(false)
                    rsFreewayYesSelected.postValue(false)
                    rsFreewayQuickSelected.postValue(false)
                }

            else -> {}
        }
        saveStrategy.postValue(true)
    }

    //选择智能推荐/都不选择时默认智能推荐
    private fun defaultSelect() {
        rsDefaultSelected.postValue(true)
        rsTmcSelected.postValue(false)
        rsMoneySelected.postValue(false)
        rsFreewayNoSelected.postValue(false)
        rsFreewayYesSelected.postValue(false)
        rsFreewayQuickSelected.postValue(false)
        rsFreewayBigSelected.postValue(false)
    }

    fun initStrategy() {
        Timber.i("initStrategy")
        rsDefaultSelected.postValue(false)
        rsTmcSelected.postValue(false)
        rsMoneySelected.postValue(false)
        rsFreewayNoSelected.postValue(false)
        rsFreewayYesSelected.postValue(false)
        rsFreewayQuickSelected.postValue(false)
        rsFreewayBigSelected.postValue(false)
    }


    /**
     * 检测偏好设置，并判断是否触发返回重新算路
     */
    @SuppressLint("BinaryOperationInTimber")
    fun checkAndSavePrefer(pref: String = "", isRouteNotice: Boolean = true) {
        Timber.i("checkAndSavePrefer pref = $pref，isRouteNotice = $isRouteNotice")
        var preference: String
        if (TextUtils.isEmpty(pref)) {
            Timber.i(
                "checkAndSavePrefer rsDefaultSelected = ${rsDefaultSelected.value!!}，rsTmcSelected = ${rsTmcSelected.value!!}" +
                        "，rsMoneySelected = ${rsMoneySelected.value!!}，rsFreewayNoSelected = ${rsFreewayNoSelected.value!!}，rsFreewayYesSelected = ${rsFreewayYesSelected.value!!}" +
                        "，rsFreewayQuickSelected = ${rsFreewayQuickSelected.value!!}，rsFreewayBigSelected = ${rsFreewayBigSelected.value!!}"
            )
            preference = when {
                rsDefaultSelected.value!! -> ConfigRoutePreference.PREFERENCE_DEFAULT //智能推荐
                rsTmcSelected.value!! && !rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_JAN //躲避拥堵
                rsTmcSelected.value!! && rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE //躲避拥堵+避免收费
                rsTmcSelected.value!! && !rsMoneySelected.value!! && rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY //躲避拥堵+不走高速
                rsTmcSelected.value!! && !rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY //躲避拥堵+高速优先
                rsTmcSelected.value!! && rsMoneySelected.value!! && rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY //躲避拥堵+避免收费+不走高速
                !rsTmcSelected.value!! && rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_CHARGE //避免收费
                !rsTmcSelected.value!! && rsMoneySelected.value!! && rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY //避免收费+不走高速
                !rsTmcSelected.value!! && !rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_USING_HIGHWAY //高速优先
                !rsTmcSelected.value!! && !rsMoneySelected.value!! && rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY //不走高速
                rsTmcSelected.value!! && !rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST  //躲避拥堵+速度最快
                rsTmcSelected.value!! && !rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST //躲避拥堵+大路优先
                !rsTmcSelected.value!! && !rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && rsFreewayQuickSelected.value!! && !rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST //速度最快
                !rsTmcSelected.value!! && !rsMoneySelected.value!! && !rsFreewayNoSelected.value!! && !rsFreewayYesSelected.value!! && !rsFreewayQuickSelected.value!! && rsFreewayBigSelected.value!! -> ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST //大路优先
                else -> {
                    Timber.i("checkAndSavePrefer else")
                    defaultSelect() //选择智能推荐/都不选择时默认智能推荐
                    ConfigRoutePreference.PREFERENCE_DEFAULT
                }
            }
        } else {
            preference = pref
            Timber.i("checkAndSavePrefer pref is change")
            routePreference = preference
            settingComponent.setConfigKeyPlanPref(routePreference)
            initStrategy()
            refreshPreference(routePreference)
            getPreferenceName(routePreference)
            setIncrementalRouteNotice(isRouteNotice)
        }
        if (routePreference != preference) {
            Timber.i("checkAndSavePrefer is change")
            routePreference = preference
            settingComponent.setConfigKeyPlanPref(routePreference)
            getPreferenceName(routePreference)
            setIncrementalRouteNotice(isRouteNotice)
            evenTackingRoutePerferSet(preference) //埋点事件追踪
        }
    }

    /**
     * 埋点事件追踪
     * //0:全部关闭; 1:高德推荐; 2:躲避拥堵;3:高速优先;4:不走高速;5:少收费;6:大路优先;7:速度最快;
     */
    private fun evenTackingRoutePerferSet(preference: String = "") {
        Timber.i("evenTackingRoutePerferSet is called preference=$preference")
        val params = when (preference) {
            //"智能推荐"
            ConfigRoutePreference.PREFERENCE_DEFAULT -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 1)
            )
            //"躲避拥堵"
            ConfigRoutePreference.PREFERENCE_AVOID_JAN -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 2)
            )
            //"避免收费"
            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 5)
            )
            //"不走高速"
            ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 4)
            )
            //"高速优先"
            ConfigRoutePreference.PREFERENCE_USING_HIGHWAY -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 3)
            )
            // "躲避拥堵、避免收费"
            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 2),
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 5)
            )
            // "躲避拥堵、不走高速"
            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 2),
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 4)
            )
            // "躲避拥堵、高速优先"
            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 2),
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 3)
            )
            //"避免收费、不走高速"
            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 5),
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 4)
            )
            // "躲避拥堵、避免收费、不走高速"
            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 2),
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 5),
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 4)
            )
            //"速度最快"
            ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 7)
            )
            //"大路优先"
            ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 6)
            )
            //"躲避拥堵、速度最快"
            ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 2),
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 7)
            )
            // "躲避拥堵、大路优先"
            ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST -> listOf(
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 2),
                Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 6)
            )

            else -> listOf(Pair(EventTrackingUtils.EventValueName.RoutePerferSet, 1))//"智能推荐"
        }
        params.forEach { pair ->
            EventTrackingUtils.trackEvent(EventTrackingUtils.EventName.Map_Set, mapOf(pair))
        }

    }

    //获取路线偏好名称
    private fun getPreferenceName(preference: String) {
        val name = AutoRouteUtil.getPlanShowInfoFromInt(preference)
        Timber.i("getPreferenceName = $name")
        preferenceName.postValue(name)
    }

    //获取云端的巡航播报
    fun refreshCruiseBroadcast() {
        setCruiseData(settingComponent.getConfigKeyRoadWarn(), roadConditionsAhead, BaseConstant.roadConditionsAhead) //巡航播报前方路况  0：off； 1：on
        setCruiseData(
            settingComponent.getConfigKeySafeBroadcast(),
            electronicEyeBroadcast,
            BaseConstant.electronicEyeBroadcast
        ) //巡航播报电子眼播报  0：off； 1：on
        setCruiseData(settingComponent.getConfigKeyDriveWarn(), safetyReminder, BaseConstant.safetyReminder) //巡航播报电子眼播报  0：off； 1：on
    }

    /**
     * 巡航播报 点击事件处理
     * @param prefer
     */
    fun cruiseBroadcastSelect(prefer: Int, check: Boolean) {
        Timber.d(" cruiseBroadcastSelect check:$check")
        var roadConditionsAheadValue = ""
        var electronicEyeBroadcastValue = ""
        var safetyReminderValue = ""
        when (prefer) {
            ConfigKey.ConfigKeyRoadWarn -> {
                toggleCruiseData(check, roadConditionsAhead, BaseConstant.roadConditionsAhead)
                settingComponent.setConfigKeyRoadWarn(if (check) 0 else 1) //巡航播报前方路况  0：off； 1：on
                roadConditionsAheadValue = if (check) "0" else ""
                electronicEyeBroadcastValue = if (settingComponent.getConfigKeySafeBroadcast() == 1) "1" else ""
                safetyReminderValue = if (settingComponent.getConfigKeyDriveWarn() == 1) "1" else ""
            }

            ConfigKey.ConfigKeySafeBroadcast -> {
                toggleCruiseData(check, electronicEyeBroadcast, BaseConstant.electronicEyeBroadcast)
                settingComponent.setConfigKeySafeBroadcast(if (check) 0 else 1) //巡航播报电子眼播报  0：off； 1：on
                roadConditionsAheadValue = if (settingComponent.getConfigKeyRoadWarn() == 1) "1" else ""
                electronicEyeBroadcastValue = if (check) "1" else ""
                safetyReminderValue = if (settingComponent.getConfigKeyDriveWarn() == 1) "1" else ""
            }

            ConfigKey.ConfigKeyDriveWarn -> {
                toggleCruiseData(check, safetyReminder, BaseConstant.safetyReminder)
                settingComponent.setConfigKeyDriveWarn(if (check) 0 else 1) //巡航播报安全提醒  0：off； 1：on
                roadConditionsAheadValue = if (settingComponent.getConfigKeyRoadWarn() == 1) "1" else ""
                electronicEyeBroadcastValue = if (settingComponent.getConfigKeySafeBroadcast() == 1) "1" else ""
                safetyReminderValue = if (check) "2" else ""
            }
        }
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.CruiseBroadcastSet, roadConditionsAheadValue),
                Pair(EventTrackingUtils.EventValueName.CruiseBroadcastSet, electronicEyeBroadcastValue),
                Pair(EventTrackingUtils.EventValueName.CruiseBroadcastSet, safetyReminderValue)
            )
        )
    }

    private fun setCruiseData(configValue: Int, liveData: MutableLiveData<Boolean>, fileName: String) {
        val value = configValue == 1
        liveData.postValue(value)
        Timber.d(" setData configValue:$configValue")
        CustomFileUtils.saveFile(if (value) "1" else "0", fileName)
    }

    private fun toggleCruiseData(check: Boolean, liveData: MutableLiveData<Boolean>, fileName: String) {
        val value = !check
        liveData.postValue(value)
        CustomFileUtils.saveFile(if (value) "1" else "0", fileName)
    }

    //预留接口，后续正式项目考虑是否使用
    fun initCloseCruiseBroadcast() {
        //第一次安装地图默认关闭全部巡航播报
        val roadConditionsAhead: String? = CustomFileUtils.getFile(BaseConstant.roadConditionsAhead)
        val electronicEyeBroadcast: String? = CustomFileUtils.getFile(BaseConstant.electronicEyeBroadcast)
        val safetyReminder: String? = CustomFileUtils.getFile(BaseConstant.safetyReminder)

        settingComponent.setConfigKeyRoadWarn(roadConditionsAhead?.toIntOrNull() ?: 0) //巡航播报前方路况  0：off； 1：on
        settingComponent.setConfigKeySafeBroadcast(electronicEyeBroadcast?.toIntOrNull() ?: 0) //巡航播报电子眼播报  0：off； 1：on
        settingComponent.setConfigKeyDriveWarn(safetyReminder?.toIntOrNull() ?: 0) //巡航播报安全提醒  0：off； 1：on
    }

    //获取本地的导航媒体音设置
    fun refreshMapMedia() {
        val configMediaType = if (settingAccountBusiness.isLogin()) {
            mapSharePreference.getIntValue(BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.userMediaType.toString(), 1)
        } else {
            mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.mediaType, 1)
        }
        val mapMedia: String? = CommonUtils.getSystemProperties("persist.map.media.type", configMediaType.toString())
        if (TextUtils.isEmpty(mapMedia)) {
            mediaType.postValue(1)
            CommonUtils.setSystemProperties("persist.map.media.type", "1")
            Timber.d(" mediaType value:   == 1")
        } else {
            try {
                mediaType.postValue(mapMedia!!.toInt())
                Timber.d(" mediaType value:$mapMedia")
            } catch (e: Exception) {
                mediaType.postValue(1)
                Timber.e(" mediaType Exception:%s", e.message)
            }
        }
    }

    //导航播报时媒体音
    fun setupNaviMedia(type: Int, isLoginChange: Boolean) { //0. 降低 1.不变
        val mediaTypeKey = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.userMediaType.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.mediaType.toString()
        }

        val configMediaType = if (isLoginChange) {
            mapSharePreference.getIntValue(mediaTypeKey, 1)
        } else {
            type
        }

        mapSharePreference.putIntValue(mediaTypeKey, configMediaType)
        CommonUtils.setSystemProperties("persist.map.media.type", configMediaType.toString())
        mediaType.postValue(configMediaType)
    }

    /**
     * 导航播报模式  播报模式。 1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
     * @param intValue
     */
    fun setConfigKeyBroadcastMode(intValue: Int) {
        volumeModelInt = intValue
        Timber.d(" setConfigKeyBroadcastMode setupVolumeModel $volumeModelInt")
        settingComponent.setConfigKeyBroadcastMode(volumeModelInt)
        volumeModel.postValue(volumeModelInt)
        if (naviRepository.isNavigating()) {
            Timber.d(" setConfigKeyBroadcastMode isNavigating")
            //配置算路参数，具体定义RouteControlKey，其他配置可查看开发指南
            // 设置GuideService
            val ttsParam = Param()
            ttsParam.type = Type.GuideParamTTSPlay
            when (volumeModelInt) {
                SettingConst.BROADCAST_EASY -> {
                    mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "4")
                    ttsParam.tts.style = 4 // 4代表新简洁播报
                }

                SettingConst.BROADCAST_MINIMALISM -> {
                    mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "6")
                    ttsParam.tts.style = 6 // 4代表极简播报
                }

                else -> {
                    mNaviController.routeControl(RouteControlKey.RouteControlKeyPlayStyle, "2")
                    ttsParam.tts.style = 2 // 4代表详细播报
                }
            }
            mNaviController.setGuideParam(ttsParam)
        }

        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.BroadcastModeSet, when(volumeModelInt){
                    SettingConst.BROADCAST_EASY -> {
                        1
                    }
                    SettingConst.BROADCAST_MINIMALISM -> {
                        2
                    }
                    else -> {
                        0
                    }
                })
            )
        )
    }

    /**
     * 获取导航播报模式
     */
    fun getConfigKeyBroadcastMode(): Int {
        volumeModelInt = settingComponent.getConfigKeyBroadcastMode()
        Timber.d(" setData volumeModelInt:$volumeModelInt")
        volumeModel.postValue(volumeModelInt)
        return volumeModelInt
    }

    /**
     * 导航静音模式  静音 1.静音 0.非静音
     * @param intValue
     */
    fun setConfigKeyMute(intValue: Int, fromPageClick: Boolean = false) {
        volumeMuteInt = intValue
        Timber.d(" setConfigKeyMute $volumeMuteInt")
        settingComponent.setConfigKeyMute(volumeMuteInt)
        volumeMute.postValue(volumeMuteInt)
        if (intValue == 1) {
            speechSynthesizeBusiness.muteToStopPlay()
        } else if (iCarInfoProxy.getVolume(BaseConstant.VOLUME_TYPE_NAVIGATION) == 0) {
            iCarInfoProxy.setVolume(BaseConstant.VOLUME_TYPE_NAVIGATION, 3)
        }
        if (intValue == 0){
            speechSynthesizeBusiness.synthesize(application.getString(R.string.sv_setting_start_broadcasting_for_you), false)
        }
        AutoStatusAdapter.sendStatus(if (intValue == 1) AutoStatus.MUTED else AutoStatus.CANCEL_MUTE)
        if (fromPageClick) {
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Map_Set,
                mapOf(
                    Pair(EventTrackingUtils.EventValueName.MuteClick, System.currentTimeMillis()),
                    Pair(EventTrackingUtils.EventValueName.MuteSet, intValue)
                )
            )
        }
    }

    /**
     * 导航静音模式 静音 1.静音 0.非静音
     */
    fun getConfigKeyMute(): Int {
        volumeMuteInt = settingComponent.getConfigKeyMute()
        Timber.d(" setData volumeMuteInt:$volumeMuteInt")
        volumeMute.postValue(volumeMuteInt)
        return volumeMuteInt
    }

    /**
     * 保存地图模式
     */
    fun setConfigKeyMapviewMode(value: Int) {
        viewModelInt = value
        Timber.d(" setupViewModel $viewModelInt")
        viewOfMap.postValue(viewModelInt)
        mapBusiness.backCurrentCarPosition(false)
        mapBusiness.switchMapViewMode(viewModelInt)
//        mapLayerProvider.settingSetMapMode(viewModelInt)
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.TowardClick, System.currentTimeMillis()),
                Pair(EventTrackingUtils.EventValueName.TowardSet, value)
            )
        )
    }

    /**
     * 获取地图模式
     */
    fun getConfigKeyMapviewMode(): Int {
        viewModelInt = settingComponent.getConfigKeyMapviewMode()
        Timber.d(" setData viewModelInt:$viewModelInt")
        viewOfMap.postValue(viewModelInt)
        return viewModelInt
    }

    //获取本地的目的地停车场推荐设置
    fun refreshNaviParkType() {
        if (settingAccountBusiness.isLogin()) {
            naviParkType.postValue(
                mapSharePreference.getIntValue(
                    BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.userParkType.toString(),
                    1
                )
            )
        } else {
            naviParkType.postValue(mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.parkNavi, 1))
        }
    }

    //目的地停车场推荐
    fun setupNaviPark(type: Int) { //1. 打开 0.关闭
        if (settingAccountBusiness.isLogin()) {
            mapSharePreference.putIntValue(BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.userParkType.toString(), type)
        } else {
            mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.parkNavi, type)
        }
        naviParkType.postValue(type)
    }

    /**
     * 设置日夜模式
     */
    fun setConfigKeyDayNightMode(intValue: Int) {
//        if (dayNightCheck) {
//            settingComponent.setConfigKeyDayNightMode(intValue)
//            skyBoxBusiness.updateDayNightStatus(getDayNightType(intValue))
//            dayNightType.postValue(intValue)
//        }
    }

    /**
     * 获取日夜模式
     */
    fun getConfigKeyDayNightMode(): Int {
//        val mCurrentMode = settingComponent.getConfigKeyDayNightMode()
//        dayNightCheck = mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.openDayNight, true)
//        dayNightSwitch.postValue(dayNightCheck)
//        if (dayNightCheck) {
//            dayNightType.postValue(mCurrentMode)
//            Timber.d(" getDayNightCheckData dayNightType:%s", mCurrentMode)
//        }
//        return mCurrentMode
        return if (com.autosdk.BuildConfig.dayNightBySystemUI) SettingConst.MODE_DEFAULT else SettingConst.MODE_NIGHT
    }

    private fun getDayNightType(number: Int): SkyBoxBusiness.DAY_NIGHT_STATUS {
        when (number) {
            SettingConst.MODE_DAY -> return SkyBoxBusiness.DAY_NIGHT_STATUS.DAY
            SettingConst.MODE_NIGHT -> return SkyBoxBusiness.DAY_NIGHT_STATUS.NIGHT
            SettingConst.MODE_DEFAULT -> return SkyBoxBusiness.DAY_NIGHT_STATUS.AUTO
        }
        return SkyBoxBusiness.DAY_NIGHT_STATUS.DAY
    }

    //获取本地的路况概览模式 0 小地图  1 光柱图 2 极简
    fun refreshOverviewRoads() {
        mapType.postValue(mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.overviewRoads, 2))
    }

    //设置全程路况概况 0 小地图  1 光柱图 2 极简
    fun setupMapType(type: Int) {
        Timber.d("setupMapType type:$type")
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.overviewRoads, type)
        mapType.postValue(type)
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.OverviewModeSet, if (mapType.value == 2) 0 else 1)
            )
        )
    }

    /**
     * 同步地图字体大小 1标准 2大
     */
    fun getMapFont() {
        val fontSize = settingComponent.getMapFont()
        mapFont.postValue(fontSize)
        mapBusiness.setupMapFont(fontSize) //获取地图字体大小
    }

    /**
     * 保存地图字体大小 1标准 2大
     */
    fun setMapFont(type: Int) {
        Timber.d("setMapFont type:$type")
        settingComponent.setMapFont(type)
        mapFont.postValue(type)
        mapBusiness.setupMapFont(type) //获取地图字体大小
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.WordSize, if (type == CommonConfigValue.KEY_MAP_FONT_NORMAL) 0 else 1)
            )
        )
    }

    //根据账号是否登录设置 步行导航，收藏点
    fun judeUserInfoUpdateSetting() {
        if (settingAccountBusiness.isLogin()) {
            val walkSwitch = mapSharePreference.getBooleanValue(MapSharePreference.SharePreferenceKeyEnum.walkSwitch, true)
            walkEnable.postValue(true)
            walkChecked.postValue(walkSwitch)
            favoriteEnable.postValue(true)
            favoriteChecked.postValue(settingComponent.getConfigKeyMyFavorite() == 1)
            Timber.d(
                " judeUserInfoUpdateSetting walkSwitch:%s getConfigKeyMyFavorite: %s",
                walkSwitch,
                settingComponent.getConfigKeyMyFavorite() == 1
            )
        } else {
            walkEnable.postValue(false)
            walkChecked.postValue(false)
            favoriteEnable.postValue(false)
            favoriteChecked.postValue(false)
            Timber.d(" judeUserInfoUpdateSetting not login ")
        }
    }

    //获取收藏点配置项
    fun getConfigKeyMyFavorite() {
        favoriteEnable.postValue(true)
        favoriteChecked.postValue(settingComponent.getConfigKeyMyFavorite() == 1)
        Timber.d(" getConfigKeyMyFavorite  getConfigKeyMyFavorite: %s", settingComponent.getConfigKeyMyFavorite() == 1)
    }

    //步行导航开关操作
    fun walkOperation(isChecked: Boolean) {
        if (settingAccountBusiness.isLogin()) {
            Timber.d(" walkOperation  $isChecked")
            mapSharePreference.putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.walkSwitch, isChecked)
            walkChecked.postValue(isChecked)
        }
    }

    //收藏点开关操作
    fun favoriteOperation(isChecked: Boolean) {
//        if (settingAccountBusiness.isLogin()) {
        val value = if (isChecked) 1 else 0
        Timber.d(" favoriteOperation  %s value:%s", isChecked, value)
        settingComponent.setConfigKeyMyFavorite(value)
        userBusiness.showAllFavoritesItem(isChecked)
        favoriteChecked.postValue(isChecked)
//        }
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.FpointSw, if (isChecked) 1 else 0)
            )
        )
    }

    //获取本地的智能比例尺数据
    fun refreshScale() {
        scaleChecked.postValue(mapSharePreference.getIntValue(MapSharePreference.SharePreferenceKeyEnum.mapAutoScale, 1) == 1)
    }

    //智能比例尺开关操作
    fun scaleOperation(isChecked: Boolean) {
        Timber.d("isChecked: $isChecked")
        mapSharePreference.putIntValue(MapSharePreference.SharePreferenceKeyEnum.mapAutoScale, if (isChecked) 1 else 0)
        scaleChecked.postValue(isChecked)

        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.AutscalebarSw, if (isChecked) 1 else 0)
            )
        )
    }

    /**
     * 获取tmc开关  1开 0 关
     */
    fun getConfigKeyRoadEvent(): Int {
        val value = settingComponent.getConfigKeyRoadEvent()
        Timber.d(" getConfigKeyRoadEvent:$value")
        tmc.postValue(value)
        return value
    }

    /**
     * 保存tmc开关  1开 0 关
     */
    fun setConfigKeyRoadEvent(value: Int) {
        Timber.d(" setConfigKeyRoadEvent:$value")
        settingComponent.setConfigKeyRoadEvent(value)
        tmc.postValue(value)
        mapBusiness.setTmcVisible(value == CommonConfigValue.KEY_ROAT_OPEN)
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.RoadCondSw, value)
            )
        )
    }

    /**
     * 同步巡航播报开关 false.关闭 true.打开
     */
    fun getCruiseBroadcastSwitch(): Boolean {
        val value = settingComponent.getCruiseBroadcastSwitch()
        Timber.d(" getCruiseBroadcastSwitch:$value")
        cruiseBroadcast.postValue(value)
        return value
    }

    /**
     * 保存巡航播报开关 false.关闭 true.打开
     */
    fun setCruiseBroadcastSwitch(value: Boolean) {
        Timber.d(" setCruiseBroadcastSwitch:$value")
        settingComponent.setCruiseBroadcastSwitch(value)
        cruiseBroadcast.postValue(value)
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.CBSw, if (value) 1 else 0)
            )
        )
    }

    /**
     * 同步巡航景点推荐开关 false.关闭 true.打开
     */
    fun getAhaScenicBroadcastSwitch(): Boolean {
        val value = settingComponent.getAhaScenicBroadcastSwitch()
        Timber.d(" getAhaScenicBroadcastSwitch:$value")
        ahaScenicBroadcast.postValue(value)
        return value
    }

    /**
     * 保存巡航景点推荐开关 false.关闭 true.打开
     */
    fun setAhaScenicBroadcastSwitch(value: Boolean) {
        Timber.d(" setAhaScenicBroadcastSwitch:$value")
        settingComponent.setAhaScenicBroadcastSwitch(value)
        ahaScenicBroadcast.postValue(value)
    }

    /**
     * 保存是否显示车标罗盘 true打开 false关闭
     */
    fun setShowCarCompass(value: Boolean) {
        coroutineScope.launch {
            showCarCompass.postValue(value)
            settingComponent.setShowCarCompass(value)
            aMapLayer?.updateCarStyle()
            mapBusiness.mainMapView.resetTickCount(1)
            EventTrackingUtils.trackEvent(
                EventTrackingUtils.EventName.Map_Set,
                mapOf(
                    Pair(EventTrackingUtils.EventValueName.VCSet, if (value) 0 else 1)
                )
            )
        }
    }

    /**
     * 同步是否显示车标罗盘 true打开 false关闭
     */
    fun getShowCarCompass(): Boolean {
        val value = settingComponent.getShowCarCompass()
        showCarCompass.postValue(value)
        aMapLayer?.updateCarStyle()
        mapBusiness.mainMapView.resetTickCount(1)
        return value
    }

    /**
     * 保存意图导航状态 true打开 false关闭
     */
    fun setIntentionNavigation(value: Boolean) {
        intentionNavigation.postValue(value)
        settingComponent.setIntentionNavigation(value)
        EventTrackingUtils.trackEvent(
            EventTrackingUtils.EventName.Map_Set,
            mapOf(
                Pair(EventTrackingUtils.EventValueName.IntenNavSw, if (value) 1 else 0)
            )
        )
    }

    /**
     * 同步意图导航状态 true打开 false关闭
     */
    fun getIntentionNavi(): Boolean {
        val value = settingComponent.getIntentionNavigation()
        intentionNavigation.postValue(value)
        return value
    }

    /**
     * 保存个性化车标设置 1.捷途车标；2.汽车；3.普通车标
     */
    fun setCarPersonalization(value: Int) {
        personalizationCar.postValue(value)
        settingComponent.setCarPersonalization(value)
    }

    /**
     * 同步个性化车标设置 1.捷途车标；2.汽车；3.普通车标
     */
    fun getCarPersonalization(): Int {
        val value = settingComponent.getCarPersonalization()
        personalizationCar.postValue(value)
        return value
    }

    // 清除应用缓存
    fun clearCache(isDefaultSettings: Boolean) {
        DataCleanUtils.clearAllCache(application)
        getCacheSize() // 获取应用缓存大小
        if (isDefaultSettings) {
            setToast.postValue(application.getString(R.string.sv_setting_default))
        } else {
            setToast.postValue(application.getString(com.autosdk.R.string.setting_had_clear_memory))
        }
    }


    // 获取应用缓存大小
    @SuppressLint("StringFormatInvalid")
    fun getCacheSize(): String {
        try {
            val totalCacheSize = DataCleanUtils.getTotalCacheSize(application)
            Timber.i("getCacheSize size:$totalCacheSize")
            val data = if (TextUtils.equals(
                    totalCacheSize,
                    "0B"
                )
            ) application.getString(R.string.sv_setting_cache_0) else String.format(
                application.getString(R.string.sv_setting_has_cache),
                totalCacheSize
            )
            appCache.postValue(data)
            return data
        } catch (e: Exception) {
            Timber.i("getCacheSize ${e.message}")
            val size = application.getString(R.string.sv_setting_cache_0)
            appCache.postValue(size)
            return size
        }
    }

    //获取离线数据版本号
    private fun getDataFileVersion() {
        val lastLocation = locationBusiness.getLastLocation()
        val adCode = mapDataBusiness.getAdCodeByLonLat(lastLocation.longitude, lastLocation.latitude)
        val dataFileVersion = mapDataBusiness.getDataFileVersion(adCode, MapDataFileType.MAP_DATA_TYPE_FILE_MAP)
        dataFileVersionStr.postValue(dataFileVersion)
        Timber.i("getDataFileVersion adCode:$adCode dataFileVersion:$dataFileVersion")
    }

    //请求审图号
    fun requestMapNum() {
        mapDataBusiness.setMapNumObserver(mapNumObserver)
        mNum = 0
        val mapNumStruct = MapNum()
        mapNumStruct.strKey = if (mContextNum == 2) publication else internet
        if (mapDataBusiness.requestMapNum(mapNumStruct) != 0) {
            getLocalMapNum() //获取本地保存的审图号
        }
        getDataFileVersion()
    }

    fun abortRequestMapNum() {
        mapDataBusiness.removeMapNumObserver(mapNumObserver)
        mapDataBusiness.abortRequestMapNum()
    }

    //地图数据审图号联网获取观察者
    private var mapNumObserver = IMapNumObserver { opErrCode: Int, mapNum: MapNum? ->
        showMapNum(mapNum) //监听回调显示审图号
    }

    //获取本地保存的审图号
    private fun getLocalMapNum() {
        val internetMapNum: String = mapSharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.internetMapNum, "")
        val publicationMapNum: String = mapSharePreference.getStringValue(MapSharePreference.SharePreferenceKeyEnum.publicationMapNum, "")
        if (mContextNum == 1) {
            if (!TextUtils.isEmpty(internetMapNum)) {
                internetStr.postValue(internetMapNum)
            } else {
                internetStr.postValue(application.getString(R.string.sv_setting_internet_map_num_fail))
            }
        } else if (mContextNum == 2) {
            if (!TextUtils.isEmpty(internetMapNum)) {
                internetStr.postValue(internetMapNum)
            } else {
                internetStr.postValue(application.getString(R.string.sv_setting_internet_map_num_fail))
            }
            if (!TextUtils.isEmpty(publicationMapNum)) {
                publicationStr.postValue(publicationMapNum)
            } else {
                publicationStr.postValue(application.getString(R.string.sv_setting_publication_map_num_fail))
            }
        }
    }

    //监听回调显示审图号
    private fun showMapNum(mapNum: MapNum?) {
        Timber.i("showMapNum mapNum:${Gson().toJson(mapNum)}")
        if (mapNum != null && !TextUtils.isEmpty(mapNum.strContent)) {
            val mapContent = mapNum.strContent
            if (mContextNum == 1) {
                internetStr.postValue(mapContent.toString())
                mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.internetMapNum, mapContent.toString())
            } else if (mContextNum == 2) {
                mNum++
                if (TextUtils.equals(mapNum.strKey, publication)) {
                    publicationStr.postValue(mapContent)
                    mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.publicationMapNum, mapContent)
                } else if (TextUtils.equals(mapNum.strKey, internet)) {
                    internetStr.postValue(mapContent)
                    mapSharePreference.putStringValue(MapSharePreference.SharePreferenceKeyEnum.internetMapNum, mapContent)
                }
                if (mNum != mContextNum) {
                    //需要数据返回再请求第二项
                    val mapNumStruct = MapNum()
                    mapNumStruct.strKey = internet
                    mapDataBusiness.requestMapNum(mapNumStruct)
                    return
                }
            }
        } else {
            getLocalMapNum() //获取本地保存的审图号
        }
    }

    /**
     * 获取车牌号
     * 引用 vdbus_extra.jar
     */
    fun getLicensePlateNumber(): String {
        return settingComponent.getLicensePlateNumber()
    }
}