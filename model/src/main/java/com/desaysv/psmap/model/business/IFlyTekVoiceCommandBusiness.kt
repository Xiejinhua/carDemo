package com.desaysv.psmap.model.business

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import com.autonavi.gbl.aosclient.model.GTrafficRestrictRequestParam
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem
import com.autosdk.adapter.SdkAdapterManager
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.widget.route.constant.ConfigRoutePreference
import com.autosdk.bussiness.widget.setting.SettingComponent
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.AutoState
import com.desaysv.ivi.vdb.client.VDBus
import com.desaysv.ivi.vdb.event.id.vr.VDEventVR
import com.desaysv.ivi.vdb.event.id.vr.VDValueVR
import com.desaysv.ivi.vdb.event.id.vr.bean.VDVRPipeLine
import com.desaysv.psmap.base.business.ActivationMapBusiness
import com.desaysv.psmap.base.business.AosBusiness
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.def.InitSdkResultType
import com.desaysv.psmap.base.def.MapModeType
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.NavigationUtil
import com.desaysv.psmap.base.utils.SearchCommonUtils
import com.desaysv.psmap.base.utils.Status
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.model.bean.MapCommandParamType
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.bean.iflytek.DataInfo
import com.desaysv.psmap.model.bean.iflytek.DataList
import com.desaysv.psmap.model.bean.iflytek.IntentResultIFlytek
import com.desaysv.psmap.model.bean.iflytek.MapStateDataIFlyTek
import com.desaysv.psmap.model.bean.iflytek.MapStatusInfoIFlyTek
import com.desaysv.psmap.model.bean.iflytek.OperationTypeSlots
import com.desaysv.psmap.model.bean.iflytek.PoiIFlyTek
import com.desaysv.psmap.model.bean.iflytek.SingleIntentIFlyTek
import com.desaysv.psmap.model.bean.iflytek.SlotsAlongSearch
import com.desaysv.psmap.model.bean.iflytek.SlotsCancelPassAway
import com.desaysv.psmap.model.bean.iflytek.SlotsCarNumberQuery
import com.desaysv.psmap.model.bean.iflytek.SlotsCollect
import com.desaysv.psmap.model.bean.iflytek.SlotsExit
import com.desaysv.psmap.model.bean.iflytek.SlotsNaviInfo
import com.desaysv.psmap.model.bean.iflytek.SlotsPageRank
import com.desaysv.psmap.model.bean.iflytek.SlotsPassAway
import com.desaysv.psmap.model.bean.iflytek.SlotsPosRank
import com.desaysv.psmap.model.bean.iflytek.SlotsQuery
import com.desaysv.psmap.model.bean.iflytek.SlotsRoutePlan
import com.desaysv.psmap.model.bean.iflytek.SlotsTnsType
import com.desaysv.psmap.model.bean.iflytek.SlotsTravelPlanCollect
import com.desaysv.psmap.model.bean.iflytek.SlotsTravelPlanOpen
import com.desaysv.psmap.model.bean.iflytek.SlotsTravelPlanQuery
import com.desaysv.psmap.model.bean.iflytek.SlotsUsrPoiModify
import com.desaysv.psmap.model.bean.iflytek.SlotsUsrPoiQuery
import com.desaysv.psmap.model.bean.iflytek.SlotsUsrPoiSet
import com.desaysv.psmap.model.bean.iflytek.State
import com.desaysv.psmap.model.bean.iflytek.TravelPlanConstants
import com.desaysv.psmap.model.bean.iflytek.TravelPlanConstants.JETOUR_CUSTOM_CATEGORY_MAP
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 讯飞语音指令业务
 */
@Singleton
class IFlyTekVoiceCommandBusiness @Inject constructor(
    private val application: Application,
    @ProvidesHiltModule.DefaultMapCommand private val defaultMapCommand: IMapCommand,
    private val userBusiness: UserBusiness,
    private val cruiseBusiness: CruiseBusiness,
    private val mapBusiness: MapBusiness,
    private val searchBusiness: SearchBusiness,
    private val settingComponent: SettingComponent,
    private val naviBusiness: NaviBusiness,
    private val routeBusiness: RouteBusiness,
    private val mRouteRequestController: RouteRequestController,
    private val initSDKBusiness: InitSDKBusiness,
    private val ahaTripImpl: AhaTripImpl,
    private val activationMapBusiness: ActivationMapBusiness,
    private val ttsPlayBusiness: TtsPlayBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val aosBusiness: AosBusiness,
    private val gson: Gson
) {
    private var mIsInit = false

    val scopeIFly = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun init() {
        Timber.i("init() called")
        if (mIsInit) {
            Timber.i("already initData")
            return
        }
        mIsInit = true

        SdkAdapterManager.getInstance().addNaviStatusListener { autoState ->
            when (autoState) {
                AutoState.FOREGROUND -> {
                    if (initSDKBusiness.isInitSuccess())
                        refreshMapStatusInfo(MapStatusInfoIFlyTek(state = State(activeStatus = ActiveStatus.fg.name)))
                }

                AutoState.BACKGROUND -> {
                    if (initSDKBusiness.isInitSuccess())
                        refreshMapStatusInfo(MapStatusInfoIFlyTek(state = State(activeStatus = ActiveStatus.bg.name)))
                }

                AutoState.ROUTE_START -> {
                    refreshMapStatusInfo(
                        MapStatusInfoIFlyTek(
                            scene = Scene.navi.name,
                            state = State(sceneStatus = SceneStatus.routing.name)
                        )
                    )
                }

                AutoState.ROUTE_STOP -> {
                    refreshMapStatusInfo()
                }
            }
        }

        naviBusiness.naviStatus.observeForever { naviState ->
            Timber.i("naviStatus=$naviState")
            when (naviState) {
                BaseConstant.NAVI_STATE_REAL_NAVING, BaseConstant.NAVI_STATE_SIM_NAVING -> {
                    refreshMapStatusInfo(
                        MapStatusInfoIFlyTek(
                            scene = Scene.navi.name,
                            state = State(sceneStatus = SceneStatus.navigation.name)
                        )
                    )
                }

                BaseConstant.NAVI_STATE_STOP_REAL_NAVI, BaseConstant.NAVI_STATE_STOP_SIM_NAVI -> {
                    refreshMapStatusInfo(
                        MapStatusInfoIFlyTek(
                            scene = Scene.poiSearch.name,
                            state = State(sceneStatus = SceneStatus.noNavi.name)
                        )
                    )
                }

                else -> {

                }
            }
        }

        routeBusiness.isRequestRoute.unPeek().observeForever { value ->
            Timber.i("isRequestRoute isRequestRoute is called")
            if (!value) {
                if (routeBusiness.routeErrorMessage.value?.isNotEmpty() == true) {
                    Timber.i("isRequestRoute routeErrorMessage is called")
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayOne, false)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayIndex, false)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayAll, false)
                    if (defaultMapCommand.checkMapCommandInQueue(MapCommandType.KeyWordSearchViaEnd)) {
                        defaultMapCommand.notifyMapCommandResult(MapCommandType.KeyWordSearchViaEnd, false)
                    } else {
                        defaultMapCommand.notifyMapCommandResult(MapCommandType.VoicePlanRouting, false)
                    }

                } else if (!routeBusiness.pathListLiveData.value.isNullOrEmpty()) {
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayOne, true)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayIndex, true)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayAll, true)
                    if (defaultMapCommand.checkMapCommandInQueue(MapCommandType.KeyWordSearchViaEnd)) {
                        defaultMapCommand.notifyMapCommandResult(MapCommandType.KeyWordSearchViaEnd, true)
                    } else {
                        defaultMapCommand.notifyMapCommandResult(MapCommandType.VoicePlanRouting, true)
                    }

                }
            }
        }

        naviBusiness.loadingView.observeForever { loadingView ->
            if (!loadingView) {
                if (naviBusiness.naviErrorMessage.value?.isNotEmpty() == true) {
                    Timber.i("loadingView naviErrorMessage is called")
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayOne, false)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayIndex, false)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayAll, false)
                } else {
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayOne, true)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayIndex, true)
                    defaultMapCommand.notifyMapCommandResult(MapCommandType.CancelPassAwayAll, true)

                }
            }
        }

        //处理页面返回的结果
        defaultMapCommand.registerMapCommandResultCallback { mapCommandType, request, result ->
            Timber.i("registerMapCommandResultCallback mapCommandType=$mapCommandType,request=$request,result=$result")
            when (mapCommandType) {
                MapCommandType.KeyWordSearch -> {
                    when {
                        result == null || (result is List<*> && result.isEmpty()) -> {
                            val intent = request as SingleIntentIFlyTek
                            val intentResult =
                                IntentResultIFlytek(intent.sid, false, "没找到目的地，换个说法吧", intent.intentIndex)
                            returnIntentResult(intentResult)
                        }

                        result is List<*> -> {
                            result as List<POI>
                            val intent = request as SingleIntentIFlyTek
                            val dataInfo = DataInfo(
                                end_poi = poiConvertToPoiIFlyTek(result[0])
                            )
                            val dataList = DataList(
                                result = result.map { poiConvertToPoiIFlyTek(it) },
                                semantic = intent.semantic
                            )

                            val mapStatusInfoIFlyTek = MapStatusInfoIFlyTek(
                                scene = Scene.poiSearch.name,
                                state = State(
                                    data = MapStateDataIFlyTek(
                                        dataInfo = dataInfo,
                                        dataList = dataList
                                    )
                                )
                            )

                            if (result.size > 1) {
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.moreTarget.name
                                refreshMapStatusInfo(mapStatusInfoIFlyTek)
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到多个目的地，可以说第几个或下一页", intent.intentIndex)
                                returnIntentResult(intentResult)
                            } else {
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.oneTarget.name
                                refreshMapStatusInfo(mapStatusInfoIFlyTek)
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到一个目的地，即将为您导航", intent.intentIndex)
                                returnIntentResult(intentResult)
                                scopeIFly.launch {
                                    delay(1000)
                                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.VoicePlanRouting, request)
                                    defaultMapCommand.startPlanRoute(result[0], null)
                                }
                            }
                        }
                    }
                }

                MapCommandType.AroundSearch -> {
                    when {
                        result == null || (result is List<*> && result.isEmpty()) -> {
                            val intent = request as SingleIntentIFlyTek
                            val intentResult =
                                IntentResultIFlytek(intent.sid, false, "没找到目的地，换个说法吧", intent.intentIndex)
                            returnIntentResult(intentResult)
                        }

                        result is List<*> -> {
                            result as List<POI>
                            val intent = request as SingleIntentIFlyTek
                            val dataInfo = DataInfo(
                                end_poi = poiConvertToPoiIFlyTek(result[0])
                            )
                            val dataList = DataList(
                                result = result.map { poiConvertToPoiIFlyTek(it) },
                                semantic = intent.semantic
                            )

                            val mapStatusInfoIFlyTek = MapStatusInfoIFlyTek(
                                scene = Scene.poiSearch.name,
                                state = State(
                                    data = MapStateDataIFlyTek(
                                        dataInfo = dataInfo,
                                        dataList = dataList
                                    )
                                )
                            )

                            if (result.size > 1) {
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.moreTarget.name
                                refreshMapStatusInfo(mapStatusInfoIFlyTek)
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到多个，可以说第几个", intent.intentIndex)
                                returnIntentResult(intentResult)
                            } else {
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.oneTarget.name
                                refreshMapStatusInfo(mapStatusInfoIFlyTek)
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到一个目的地，是否开始导航？", intent.intentIndex)
                                returnIntentResult(intentResult)
                            }
                        }
                    }
                }

                MapCommandType.SearchHomeCompanyAddressResultPage, MapCommandType.KeyWordSearchForCollect -> {
                    when {
                        result == null || (result is List<*> && result.isEmpty()) -> {
                            val intent = request as SingleIntentIFlyTek
                            val intentResult =
                                IntentResultIFlytek(intent.sid, false, "没找到该地点，换个说法吧", intent.intentIndex)
                            returnIntentResult(intentResult)
                        }

                        result is List<*> -> {
                            result as List<POI>
                            val intent = request as SingleIntentIFlyTek
                            val dataInfo = DataInfo(
                                end_poi = poiConvertToPoiIFlyTek(result[0])
                            )
                            val dataList = DataList(
                                result = result.map { poiConvertToPoiIFlyTek(it) },
                                semantic = intent.semantic
                            )

                            val mapStatusInfoIFlyTek = MapStatusInfoIFlyTek(
                                scene = Scene.poiSearch.name,
                                state = State(
                                    data = MapStateDataIFlyTek(
                                        dataInfo = dataInfo,
                                        dataList = dataList
                                    )
                                )
                            )

                            if (result.size > 1) {
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.moreTarget.name
                                refreshMapStatusInfo(mapStatusInfoIFlyTek)
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到多个，是第几个？", intent.intentIndex)
                                returnIntentResult(intentResult)
                            } else {
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.oneTarget.name
                                refreshMapStatusInfo(mapStatusInfoIFlyTek)
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到一个地址，是否要收藏？", intent.intentIndex)
                                returnIntentResult(intentResult)
                            }
                        }
                    }
                }

                MapCommandType.KeyWordSearchViaEnd -> {
                    when {
                        //路线规划后再去沿途搜索途经点
                        result is Boolean && request is Pair<*, *> && request.second is Pair<*, *> && request.first is MapCommandType -> {
                            val second = request.second as Pair<SingleIntentIFlyTek, String>
                            val intent = second.first
                            val viaName = second.second
                            Timber.i("After PlanRoute KeyWordSearchViaEnd request=$request result=$result")
                            if (result) {
                                scopeIFly.launch {
                                    delay(500)
                                    defaultMapCommand.alongRouteSearch(viaName)
                                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AlongWaySearch, intent)
                                }
                            } else {
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, false, "没找到路线", intent.intentIndex)
                                returnIntentResult(intentResult)
                            }
                        }

                        result == null || (result is List<*> && result.isEmpty()) -> {
                            val intent = request as SingleIntentIFlyTek
                            val intentResult =
                                IntentResultIFlytek(intent.sid, false, "没找到，换个说法吧", intent.intentIndex)
                            returnIntentResult(intentResult)
                        }

                        result is List<*> && request is Pair<*, *> -> {
                            result as List<POI>
                            val intent = request.first as SingleIntentIFlyTek
                            val viaName = request.second as String
                            val dataInfo = DataInfo(
                                end_poi = poiConvertToPoiIFlyTek(result[0])
                            )
                            val dataList = DataList(
                                result = result.map { poiConvertToPoiIFlyTek(it) },
                                semantic = intent.semantic
                            )

                            val mapStatusInfoIFlyTek = MapStatusInfoIFlyTek(
                                scene = Scene.poiSearch.name,
                                state = State(
                                    data = MapStateDataIFlyTek(
                                        dataInfo = dataInfo,
                                        dataList = dataList
                                    )
                                )
                            )

                            if (result.size > 1) {
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.moreTarget.name
                                refreshMapStatusInfo(mapStatusInfoIFlyTek)
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到多个目的地，可以说第几个或下一页", intent.intentIndex)
                                returnIntentResult(intentResult)
                                defaultMapCommand.addMapCommandResultWaitQueue(
                                    MapCommandType.KeyWordSearchViaEnd,
                                    Pair(MapCommandType.AlongWaySearch, Pair(intent, viaName))
                                )
                            } else {
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到一个目的地，开始规划路线", intent.intentIndex)
                                returnIntentResult(intentResult)
                                //直接路线规划
                                defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.VoicePlanRouting, request)
                                defaultMapCommand.startPlanRoute(result[0], null)
                                defaultMapCommand.addMapCommandResultWaitQueue(
                                    MapCommandType.KeyWordSearchViaEnd, Pair(MapCommandType.AlongWaySearch, Pair(intent, viaName))
                                )
                            }
                        }
                    }
                }

                MapCommandType.AlongWaySearch -> {
                    when {
                        result == null || (result is List<*> && result.isEmpty()) -> {
                            val intent = when (request) {
                                is Pair<*, *> -> {
                                    request.first as SingleIntentIFlyTek
                                }

                                else -> {
                                    request as SingleIntentIFlyTek
                                }
                            }

                            val type = when (request) {
                                is Pair<*, *> -> {
                                    request.second as Pair<*, *>
                                    (request.second as Pair<*, *>).first as VoiceAlongSearchType
                                }

                                else -> {
                                    VoiceAlongSearchType.NORMAL
                                }
                            }

                            val poiName = when (request) {
                                is Pair<*, *> -> {
                                    request.second as Pair<*, *>
                                    (request.second as Pair<*, *>).second as String
                                }

                                else -> {
                                    ""
                                }
                            }

                            val tips = when (type) {
                                VoiceAlongSearchType.NEAREST -> "没找到最近的$poiName，换个说法吧"
                                VoiceAlongSearchType.SHORTEST -> "没找到最顺路的$poiName，换个说法吧"
                                VoiceAlongSearchType.NORMAL -> "没找到途径点，换个说法吧"
                            }

                            returnIntentResult(IntentResultIFlytek(intent.sid, false, tips, intent.intentIndex))
                        }

                        result is List<*> -> {
                            result as List<POI>
                            val intent = when (request) {
                                is Pair<*, *> -> {
                                    request.first as SingleIntentIFlyTek
                                }

                                else -> {
                                    request as SingleIntentIFlyTek
                                }
                            }

                            val type = when (request) {
                                is Pair<*, *> -> {
                                    request.second as Pair<*, *>
                                    (request.second as Pair<*, *>).first as VoiceAlongSearchType
                                }

                                else -> {
                                    VoiceAlongSearchType.NORMAL
                                }
                            }

                            val poiName = when (request) {
                                is Pair<*, *> -> {
                                    request.second as Pair<*, *>
                                    (request.second as Pair<*, *>).second as String
                                }

                                else -> {
                                    ""
                                }
                            }

                            val dataInfo = DataInfo(
                                end_poi = poiConvertToPoiIFlyTek(result[0])
                            )
                            val dataList = DataList(
                                result = result.map { poiConvertToPoiIFlyTek(it) },
                                semantic = intent.semantic
                            )

                            val mapStatusInfoIFlyTek = MapStatusInfoIFlyTek(
                                scene = Scene.poiSearch.name,
                                state = State(
                                    data = MapStateDataIFlyTek(
                                        dataInfo = dataInfo,
                                        dataList = dataList
                                    )
                                )
                            )

                            val tips: String
                            if (result.size > 1) {
                                tips = when (type) {
                                    VoiceAlongSearchType.NEAREST -> "找到多个近的$poiName，可以说第几个或下一页"
                                    VoiceAlongSearchType.SHORTEST -> "找到多个顺路的$poiName，可以说第几个或下一页"
                                    VoiceAlongSearchType.NORMAL -> "找到多个途经点，可以说第几个或下一页"
                                }
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.moreTarget.name
                            } else {
                                tips = when (type) {
                                    VoiceAlongSearchType.NEAREST -> "找到最近的$poiName，是否添加为途经点？"
                                    VoiceAlongSearchType.SHORTEST -> "找到最顺路的$poiName，是否添加为途经点？"
                                    VoiceAlongSearchType.NORMAL -> "找到一个途经点，是否要添加？"
                                }
                                mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.oneTarget.name
                            }
                            refreshMapStatusInfo(mapStatusInfoIFlyTek)
                            returnIntentResult(IntentResultIFlytek(intent.sid, true, tips, intent.intentIndex))
                        }
                    }

                }

                MapCommandType.PosRank -> {
                    val intent = request as SingleIntentIFlyTek
                    if (result is Pair<*, *>) {
                        val flag = result.first as Boolean
                        val param = result.second
                        when (param) {
                            is String -> {
                                val intentResult = IntentResultIFlytek(intent.sid, flag, param, intent.intentIndex)
                                returnIntentResult(intentResult)
                            }

                            is POI -> {
                                defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.VoicePlanRouting, request)
                                defaultMapCommand.startPlanRoute(param, null)
                                returnIntentResult(IntentResultIFlytek(intent.sid, flag, "好的，开始规划路线", intent.intentIndex))
                            }

                            else -> {
                                Timber.i("PosRank else $param")
                            }
                        }
                        if (flag) refreshMapStatusInfo()
                    }
                }

                MapCommandType.PageRank -> {
                    when {
                        (result is Pair<*, *>) -> {
                            val intent = request as SingleIntentIFlyTek
                            val flag = result.first as Boolean
                            val tips = result.second as String
                            val intentResult = IntentResultIFlytek(intent.sid, flag, tips, intent.intentIndex)
                            returnIntentResult(intentResult)
                        }

                        result == null || (result is List<*> && result.isEmpty()) -> {
                            val intent = request as SingleIntentIFlyTek
                            val intentResult =
                                IntentResultIFlytek(intent.sid, false, "页面刷新失败", intent.intentIndex)
                            returnIntentResult(intentResult)
                        }

                        result is List<*> -> {
                            result as List<POI>
                            val intent = request as SingleIntentIFlyTek
                            val dataInfo = DataInfo(
                                end_poi = poiConvertToPoiIFlyTek(result[0])
                            )
                            val dataList = DataList(
                                result = result.map { poiConvertToPoiIFlyTek(it) },
                                semantic = intent.semantic
                            )

                            val mapStatusInfoIFlyTek = MapStatusInfoIFlyTek(
                                scene = Scene.poiSearch.name,
                                state = State(
                                    data = MapStateDataIFlyTek(
                                        dataInfo = dataInfo,
                                        dataList = dataList
                                    )
                                )
                            )

                            mapStatusInfoIFlyTek.state.sceneStatus = SceneStatus.moreTarget.name
                            refreshMapStatusInfo(mapStatusInfoIFlyTek)
                            val intentResult = IntentResultIFlytek(intent.sid, true, "您可以说第几个或者下一页", intent.intentIndex)
                            returnIntentResult(intentResult)
                        }
                    }
                }

                MapCommandType.Confirm -> {
                    val intent = request as SingleIntentIFlyTek
                    when {
                        (result is String) -> {
                            val intentResult = IntentResultIFlytek(intent.sid, true, result, intent.intentIndex)
                            returnIntentResult(intentResult)
                            refreshMapStatusInfo()
                        }

                        (result is Pair<*, *>) -> {
                            val flag = result.first as Boolean
                            val tips = result.second as String
                            val intentResult = IntentResultIFlytek(intent.sid, flag, tips, intent.intentIndex)
                            returnIntentResult(intentResult)
                            if (flag) refreshMapStatusInfo()
                        }
                    }
                }

                MapCommandType.CloseSettingPage -> {
                    val intent = request as SingleIntentIFlyTek
                    if (result is String) {
                        val intentResult = IntentResultIFlytek(intent.sid, true, result, intent.intentIndex)
                        returnIntentResult(intentResult)
                    }
                }

                MapCommandType.CancelPassAwayOne -> {
                    val intent = request as SingleIntentIFlyTek
                    if (result is Boolean) {
                        val intentResult =
                            IntentResultIFlytek(intent.sid, result, if (result) "已为您删除途经点" else "删除途经点失败", intent.intentIndex)
                        returnIntentResult(intentResult)
                    }
                }

                MapCommandType.CancelPassAwayIndex -> {
                    if (result is Boolean && request is Pair<*, *>) {
                        val intent = request.first as SingleIntentIFlyTek
                        val param = request.second
                        val tips = if (param is Int) {
                            if (result) "已为您删除第${param}个}途经点" else "删除途经点失败"
                        } else {
                            param as String
                            if (result) "已为您删除${param}途经点" else "删除${param}途经点失败"
                        }
                        val intentResult =
                            IntentResultIFlytek(
                                intent.sid,
                                result,
                                tips,
                                intent.intentIndex
                            )
                        returnIntentResult(intentResult)
                    }
                }

                MapCommandType.CancelPassAwayAll -> {
                    val intent = request as SingleIntentIFlyTek
                    if (result is Boolean) {
                        val intentResult =
                            IntentResultIFlytek(
                                intent.sid,
                                result,
                                if (result) "已删除全部途径点哦" else "删除途经点失败",
                                intent.intentIndex
                            )
                        returnIntentResult(intentResult)
                    }
                }

                MapCommandType.NaviToFavorite -> {
                    val intent = request as SingleIntentIFlyTek
                    when {
                        result == null || (result is ArrayList<*> && result.isEmpty()) -> {
                            val intentResult = IntentResultIFlytek(intent.sid, false, "没有收藏的地点哦", intent.intentIndex)
                            returnIntentResult(intentResult)
                        }

                        result is ArrayList<*> -> {
                            result as ArrayList<SimpleFavoriteItem>
                            if (result.size == 1) {
                                //只有一个收藏点,一般不会进这里
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "找到1个目的地${result[0].name}，即将为您导航", intent.intentIndex)
                                returnIntentResult(intentResult)
                            } else {
                                val dataInfo = DataInfo(
                                    end_poi = poiConvertToPoiIFlyTek(ConverUtils.converSimpleFavoriteToPoi(result[0]))
                                )
                                val dataList = DataList(
                                    result = result.map {
                                        ConverUtils.converSimpleFavoriteToPoi(it)
                                    }.map { poiConvertToPoiIFlyTek(it) }
                                )

                                val mapStatusInfoIFlyTek = MapStatusInfoIFlyTek(
                                    scene = Scene.poiSearch.name,
                                    state = State(
                                        sceneStatus = SceneStatus.moreTarget.name,
                                        data = MapStateDataIFlyTek(
                                            dataInfo = dataInfo,
                                            dataList = dataList
                                        )
                                    )
                                )
                                refreshMapStatusInfo(mapStatusInfoIFlyTek)
                                val intentResult =
                                    IntentResultIFlytek(intent.sid, true, "请在收藏夹页面选择地点", intent.intentIndex)
                                returnIntentResult(intentResult)
                            }

                        }
                    }
                }

                MapCommandType.OpenModifyHomeCompanyAddressPage -> {
                    val intent = request as SingleIntentIFlyTek
                    if (result is String) {
                        val intentResult = IntentResultIFlytek(intent.sid, true, result, intent.intentIndex)
                        returnIntentResult(intentResult)
                    }
                }

                MapCommandType.VoicePlanRouting -> {
                    val intent = request as SingleIntentIFlyTek
                    if (result is Boolean) {
                        val tips = if (result) {
                            when (mRouteRequestController.carRouteResult?.pathResult?.size) {
                                1 -> "找到1条路线，即将为您开启导航？"
                                2, 3 -> "要走第几个路线？"
                                else -> "没有找到路线，请重试"
                            }
                        } else "没有找到路线，请重试"
                        val intentResult = IntentResultIFlytek(intent.sid, result, tips, intent.intentIndex)
                        returnIntentResult(intentResult)
                    }
                }

                else -> {

                }

            }

        }

        if (!initSDKBusiness.isInitSuccess()) {
            unableUseMap()
            initSDKBusiness.getInitResult().observeForever { result ->
                if (result.code == InitSdkResultType.OK) {
                    refreshMapStatusInfo(MapStatusInfoIFlyTek(state = State(activeStatus = if (BaseConstant.MAP_APP_FOREGROUND) ActiveStatus.fg.name else ActiveStatus.bg.name)))
                }
            }
        } else {
            refreshMapStatusInfo(MapStatusInfoIFlyTek(state = State(activeStatus = if (BaseConstant.MAP_APP_FOREGROUND) ActiveStatus.fg.name else ActiveStatus.bg.name)))
        }

        //查询前⽅路况 tts播报
        cruiseBusiness.tRTtsBroadcast.unPeek().observeForever {
            if (!TextUtils.isEmpty(it)) {
                Timber.i("tRTtsBroadcast $it")
                setTTSResponse(it)
            }
        }
    }

    fun unInit() {
        Timber.i("unInit() called")
    }

    fun unableUseMap() {
        Timber.i("unableUseMap() called")
        refreshMapStatusInfo(MapStatusInfoIFlyTek(state = State(activeStatus = ActiveStatus.noExists.name)))
    }

    /**
     * 刷新地图状态
     */
    private fun refreshMapStatusInfo(mapStatusInfoIFlyTek: MapStatusInfoIFlyTek? = null) {
        val dataObject = mapStatusInfoIFlyTek
            ?: MapStatusInfoIFlyTek(
                scene = if (naviBusiness.isNavigating() || routeBusiness.isPlanRouteing()) Scene.navi.name else Scene.poiSearch.name,
                state = State(
                    sceneStatus = when {
                        routeBusiness.isInRouteFragment -> {
                            SceneStatus.routing.name
                        }

                        naviBusiness.isRealNavi() -> {
                            SceneStatus.navigation.name
                        }

                        else -> {
                            SceneStatus.noNavi.name
                        }
                    }
                )
            )
        val intent = Intent("com.iflytek.autofly.business.response").apply {
            `package` = "com.iflytek.cutefly.speechclient.hmi"
            val data = gson.toJson(dataObject)
            putExtra("data", data)
            Timber.i("refreshMapStatusInfo() called data:$data")
        }
        application.startService(intent)
    }

    /**
     * 解析语音指令
     */
    fun parseCommand(json: String) {
        Timber.i("${BaseConstant.APP_VERSION} parseCommand() called with: json = $json")
        scopeIFly.launch {
            withContext(Dispatchers.IO) {
                val intent = JsonParser.parseString(json).asJsonObject
                val service = intent.get("service").asString
                val operation = intent.get("operation").asString
                val sid = intent.get("sid").asString //唯一码 响应处理结果用到
                val text = intent.get("text").asString//语音识别文本
                //val user = intent.get("user").asString//主驾/副驾
                val ttsAction = intent.get("ttsAction")?.asString //进行TTS播报 false：不需要 true：需要
                val tts_play_type = intent.get("tts_play_type")?.asString
                val rc = intent.get("rc")?.asInt//语义状态。0：成功；1：无效请求；3：信源超时；4：拒识
                val receipt = intent.get("receipt")?.asString   //进行回执 false: 不需要 true： 需要
                val intentIndex = intent.get("intentIndex")?.asInt ?: -1//指令序号（单意图模式无需关注）
                Timber.i("parseCommand() called with: rc = $rc text = $text")
                val intents = arrayOf(
                    SingleIntentIFlyTek(
                        sid, service, operation, rc, text, tts_play_type, ttsAction, receipt, intentIndex, intent.getAsJsonObject
                            ("semantic")
                    )
                )
                //解析多个意图
                if (intent.get("moreResults")?.isJsonArray == true) {
                    intent.get("moreResults")?.asJsonArray?.forEach {
                        val singleIntent = it.asJsonObject
                        intents.plus(
                            SingleIntentIFlyTek(
                                sid,
                                singleIntent.get("service").asString,
                                singleIntent.get("operation").asString,
                                singleIntent.get("rc").asInt,
                                singleIntent.get("text").asString,
                                singleIntent.get("tts_play_type").asString,
                                singleIntent.get("ttsAction")?.asString ?: ttsAction,//不知道有没有
                                singleIntent.get("receipt")?.asString ?: receipt,
                                singleIntent.get("intentIndex").asInt,
                                singleIntent.getAsJsonObject
                                    ("semantic")
                            )
                        )
                    }
                }

                //处理每个意图
                intents.forEach {
                    parseIntentCommand(it)
                }
            }
        }
    }

    /**
     * 返回语义处理结果
     */
    private fun returnIntentResult(intentData: IntentResultIFlytek) {
        Timber.d("returnIntentResult() called with: data = $intentData")
        if (intentData.intentIndex == -1) {
            //单意图无需返回结果
            if (intentData.tips.isNotEmpty())
                setTTSResponse(intentData.tips)
        } else {
            val intent = Intent().apply {
                putExtra("data", gson.toJson(intentData))
                setAction("com.iflytek.auto.speechclient.multiIntent");
                setComponent(
                    ComponentName(
                        "com.iflytek.cutefly.speechclient.hmi",
                        "com.iflytek.auto.speechclient.sdk.SpeechClientService"
                    )
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                application.startForegroundService(intent);
            } else {
                application.startService(intent)
            }
        }
    }

    fun setTTSResponse(text: String) {
        Timber.i("setTTSResponse() called with: text = $text")
        try {
            val event = VDVRPipeLine.createEvent(VDEventVR.VR_NAVI, VDVRPipeLine().apply {
                pkgName = application.packageName
                key = VDValueVR.VRSemanticKey.VR_CONTROL_RESPONSE
                value = text
            })
            VDBus.getDefault().set(event)
        } catch (e: Exception) {
            Timber.e(e, "setTTSResponse ERROR")
        }
    }

    /**
     * 处理每个意图
     */
    private suspend fun parseIntentCommand(intent: SingleIntentIFlyTek) {
        Timber.i("parseIntentCommand() called with: intent = $intent")
        when (intent.service) {
            "mapU" -> {
                when (intent.operation) {
                    "QUERY" -> {
                        mapUQueryTask(intent)
                    }

                    "ALONG_SEARCH" -> {
                        mapUAlongSearchTask(intent)
                    }

                    "ROUTE_PLAN" -> {
                        mapURoutePlanTask(intent)
                    }

                    "REROUTE" -> {
                        mapURerouteTask(intent)
                    }

                    "OPEN", "DISPLAY_MODE_UNMUTE", "DISPLAY_MODE_MUTE", "DISPLAY_MODE_NOVICE", "DISPLAY_MODE_VETERAN", "DISPLAY_MODE_STANDARD",
                    "DISPLAY_MODE_DETAIL", "DISPLAY_MODE_CONCISION", "DISPLAY_MODE_EXTREMELY_CONCISION", "VIEW_TRANS" -> {
                        mapUTnsType(intent)
                    }

                    "USR_POI_QUERY" -> {
                        mapUUsrPoiQueryTask(intent)
                    }

                    "USR_POI_MODIFY" -> {
                        mapUUsrPoiModifyTask(intent)
                    }

                    "USR_POI_SET" -> {
                        mapUUsrPoiSetTask(intent)
                    }

                    "COLLECT" -> {
                        mapUCollectTask(intent)
                    }

                    "COLLECT_NAVIGATION_ROUTE" -> {
                        favoriteDestinationPOI(intent)
                    }

                    "CONFIRM" -> {
                        mapUConfirmTask(intent)
                    }

                    "BACK_MAP" -> {
                        mapUBackMapTask(intent)
                    }

                    "OPEN_SETTING", "OPEN_MAP_SETTING" -> {
                        mapUOpenSettingTask(intent)
                    }

                    "CLOSE_SETTING", "CLOSE_MAP_SETTING" -> {
                        mapUCloseSetting(intent)
                    }

                    "OPEN_MAP_COLLECT" -> {
                        mapUOpenMapCollectTask(intent)
                    }

                    "CANCEL_MAP", "CLOSE_MAP" -> {
                        //todo 关闭地图可能要退到后台？
                        mapUCancelMapTask(intent)
                    }

                    "VIEW_TRANS_3D_HEAD_UP", "VIEW_TRANS_2D_NORTH_UP", "VIEW_TRANS_2D_HEAD_UP" -> {
                        mapUViewTrans(intent)
                    }

                    "ZOOM_IN", "ZOOM_OUT" -> {
                        mapUZoomInOut(intent)
                    }

                    "OPEN_TRAFFIC_INFO", "CLOSE_TRAFFIC_INFO" -> {
                        mapUTrafficOpera(intent)
                    }

                    "OPEN_E_POLICE", "CLOSE_E_POLICE" -> {
                        mapUEpolice(intent)
                    }

                    "LOCATE" -> {
                        mapULocate(intent)
                    }

                    "LOCATE_ROAD" -> {
                        mapULocateRoad(intent)
                    }

                    "NAVI_INFO" -> {
                        mapUNaviInfo(intent)
                    }

                    "CANCEL_PASS_AWAY" -> {
                        mapUCancelPassAway(intent)
                    }

                    "CANCEL_ALL_PASS_AWAY" -> {
                        mapUCancelPassAway(intent, true)
                    }

                    "PASS_AWAY" -> {
                        mapUPassAway(intent)
                    }

                    "POS_RANK" -> {
                        mapUPosRank(intent)
                    }

                    "PAGE_RANK" -> {
                        mapUPageRank(intent)
                    }

                    "VIEW_EAGLE_EYE_MAP" -> {
                        mapUSetMiniMapMode(intent, 0)
                    }

                    "VIEW_HISTOGRAM_MAP" -> {
                        mapUSetMiniMapMode(intent, 1)
                    }

                    "EXIT" -> {
                        intent.semantic.getAsJsonObject("slots")?.let { slots ->
                            val slotsExit = gson.fromJson(slots.toString(), SlotsExit::class.java)
                            if (slotsExit.name == "地图") {
                                mapUCancelMapTask(intent)
                            }
                        }
                    }

                    "QUERY_TRAFFIC_INFO" -> {
                        mapUQueryTrafficInfo(intent)
                    }

                    else -> {
                        returnIntentResult(IntentResultIFlytek(intent.sid, false, "不支持的操作", intent.intentIndex))
                        Timber.w("Unknown operation: ${intent.operation}")
                    }
                }

            }

            "travelPlan" -> {
                when (intent.operation) {
                    "QUERY" -> {
                        //处理旅游计划查询
                        travelPlanQuery(intent)
                    }

                    "COLLECT", "CANCEL_COLLECT" -> {
                        //处理旅游计划收藏和取消
                        travelPlanCollect(intent)
                    }

                    "OPEN" -> {
                        //打开路书界面
                        travelPlanOpenPage(intent)
                    }
                }
            }

            "app" -> {
                when (intent.operation) {
                    "EXIT" -> {
                        intent.semantic.getAsJsonObject("slots")?.let { slots ->
                            val slotsExit = gson.fromJson(slots.toString(), SlotsExit::class.java)
                            if (slotsExit.name == "地图") {
                                mapUCancelMapTask(intent)
                            }
                        }
                    }
                }
            }

            "carNumber" -> {
                when (intent.operation) {
                    "QUERY" -> {
                        carNumberQuery(intent)
                    }
                }
            }

            else -> {
                returnIntentResult(IntentResultIFlytek(intent.sid, false, "不支持的操作", intent.intentIndex))
                Timber.w("Unknown service: ${intent.service}")
            }
        }
    }

    private suspend fun travelPlanQuery(intent: SingleIntentIFlyTek) {
        val slots = intent.semantic.getAsJsonObject("slots")
        if (slots == null) {
            Timber.w("travelPlanQuery() called with null slots in intent semantic")
            return
        }

        val slotsTravelPlan = gson.fromJson(slots.toString(), SlotsTravelPlanQuery::class.java)
        Timber.i("travelPlanQuery() called with slotsTravelPlan = ${gson.toJson(slotsTravelPlan)}")

        if (slotsTravelPlan.property?.serveType == "捷途优惠") {
            Timber.i("travelPlanQuery() 不支持进入捷途权益 ")
            val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
            result.tips = "不支持进入捷途权益"
            returnIntentResult(result)
            return
        }

        // 指定天数限制
        val day = getDayLimit(slotsTravelPlan)

        // 指定城市
        val cityItemInfo = getCityItemInfo(slotsTravelPlan.endLoc.city)
            ?: getCityItemInfo(slotsTravelPlan.endLoc.province)

        // 指定关键字
        val keyword = slotsTravelPlan.endLoc.ori_loc

        defaultMapCommand.searchAhaTrip(cityItemInfo, day, keyword)
        defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.SearchAhaTrip, intent)
    }

    /**
     * 获取指定天数限制
     * @param slotsTravelPlan 旅游计划查询的插槽信息
     * @return 天数限制字符串，如果未指定则返回 null
     */
    private fun getDayLimit(slotsTravelPlan: SlotsTravelPlanQuery): String? {
        val timeSpan = slotsTravelPlan.timeSpan
        return if (timeSpan?.leftClosure != null && timeSpan.rightClosure != null) {
            "${timeSpan.leftClosure},${timeSpan.rightClosure}"
        } else if (!slotsTravelPlan.datetimeDescr.isNullOrEmpty()) {
            // 指定节假日，需要转化为天数
            TravelPlanConstants.DATE_TIME_DESCR_LIST[slotsTravelPlan.datetimeDescr]
        } else {
            null
        }
    }

    /**
     * 根据城市或省份名称获取城市信息
     * @param name 城市或省份名称
     * @return 城市信息对象，如果未找到则返回 null
     */
    private fun getCityItemInfo(name: String?): CityItemInfo? {
        return name?.takeIf { it.isNotEmpty() }?.let {
            val adCode = mapDataBusiness.searchAdCode(it)?.firstOrNull()
            adCode?.let { code -> mapDataBusiness.getCityInfo(code) }
        }
    }

    private fun travelPlanCollect(intent: SingleIntentIFlyTek) {
        val slots = intent.semantic.getAsJsonObject("slots")
        if (slots == null) {
            Timber.w("travelPlanQuery() called with null slots in intent semantic")
            return
        }

        val slotsTravelPlan = gson.fromJson(slots.toString(), SlotsTravelPlanCollect::class.java)
        if (slotsTravelPlan.insType == "COLLECT") {
            defaultMapCommand.ahaTripCollect(true)
        } else if (slotsTravelPlan.insType == "CANCEL_COLLECT") {
            defaultMapCommand.ahaTripCollect(false)
        }
    }

    private fun travelPlanOpenPage(intent: SingleIntentIFlyTek) {
        Timber.i("travelPlanOpenPage() called with: intent = $intent")
        val slots = intent.semantic.getAsJsonObject("slots")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        val slotsTravelPlanOpen = gson.fromJson(slots.toString(), SlotsTravelPlanOpen::class.java)
        if (ahaTripImpl.isLogin()) {
            if (slotsTravelPlanOpen.source?.isNotEmpty() == true) {
                when (slotsTravelPlanOpen.source) {
                    "收藏" -> {
                        if (defaultMapCommand.openAhaTripDetailPage("收藏")) {
                            result.tips = "好的，已打开收藏夹"
                            returnIntentResult(result)
                        } else {
                            result.apply {
                                handleResult = false
                                tips = "当前页面不支持操作"
                            }
                            returnIntentResult(result)
                        }
                    }

                    "行程列表" -> {
                        if (defaultMapCommand.openAhaTripDetailPage("行程列表")) {
                            result.tips = "好的，已打开我的路书"
                            returnIntentResult(result)
                        } else {
                            result.apply {
                                handleResult = false
                                tips = "当前页面不支持操作"
                            }
                            returnIntentResult(result)
                        }
                    }
                }
            } else {
                Timber.i("travelPlanOpenPage() slotsTravelPlanOpen.source = null")
            }
        } else {
            result.apply {
                handleResult = false
                tips = "请先登录账号，再试试"
            }
            returnIntentResult(result)
        }
    }


    private fun mapUAlongSearchTask(intent: SingleIntentIFlyTek, type: VoiceAlongSearchType = VoiceAlongSearchType.NORMAL) {
        Timber.i("mapUAlongSearchTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (naviBusiness.isRealNavi()) {
            intent.semantic.getAsJsonObject("slots")?.let { slots ->
                val slotsAlongSearch = gson.fromJson(slots.toString(), SlotsAlongSearch::class.java)
                val poiName = slotsAlongSearch.endLoc?.ori_loc
                if (poiName.isNullOrEmpty()) {
                    result.apply {
                        handleResult = false
                        tips = "您想将哪里设置为途经点"
                    }
                    returnIntentResult(result)
                } else {
                    defaultMapCommand.alongRouteSearch(poiName)
                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AlongWaySearch, Pair(intent, Pair(type, poiName)))
                }
            }
        } else {
            result.apply {
                handleResult = false
                tips = "当前没有在导航中哦"
            }
            returnIntentResult(result)
        }
    }

    /**
     *service = "mapU"
     * operation = "QUERY"
     */
    private suspend fun mapUQueryTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUQueryTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsQuery = gson.fromJson(slots.toString(), SlotsQuery::class.java)

            slotsQuery.property?.hotelLvl?.offset?.let { offset ->
                Timber.i("mapUQueryTask() called with: slotsQuery.property.hotelLvl.offset = $offset")
                defaultMapCommand.putMapCommandParams(MapCommandParamType.Search, mapOf(Pair("hotelLvl", offset)))
            }

            when {
                slotsQuery.distanceDescr == "NEAREST" -> {
                    Timber.i("mapUQueryTask() called with: slotsQuery.distanceDescr == NEAREST")
                    mapUAlongSearchTask(intent, VoiceAlongSearchType.NEAREST)
                }

                slotsQuery.landmark != null -> {
                    //查询XX附近XXX
                    if (slotsQuery.landmark.ori_loc == CURRENT_ORI_LOC) {
                        //当前位置周边搜索
                        slotsQuery.endLoc.ori_loc.let {
                            Timber.i("aroundSearch called with: ori_loc = $it")
                            //添加到等待页面处理结果队列
                            defaultMapCommand.aroundSearch(it, true, "5000")
                            defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AroundSearch, intent)
                        }
                    } else {
                        val flag = defaultMapCommand.aroundSearchByLocationName(slotsQuery.endLoc.ori_loc, slotsQuery.landmark.ori_loc)
                        if (!flag) {
                            result.apply {
                                handleResult = false
                                tips = "查找失败，请重试！"
                            }
                            returnIntentResult(result)
                        } else {
                            //添加到等待页面处理结果队列
                            defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AroundSearch, intent)
                        }
                    }
                }

                !TextUtils.isEmpty(slotsQuery?.viaLoc?.ori_loc) -> {
                    //先去XXX，再去XXX
                    defaultMapCommand.keywordSearch(slotsQuery.endLoc.ori_loc, true)
                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.KeyWordSearchViaEnd, Pair(intent, slotsQuery.viaLoc!!.ori_loc))
                }

                else -> {
                    //直接关键字搜索
                    slotsQuery?.endLoc?.ori_loc?.let {
                        Timber.i("keywordSearch called with: ori_loc = $it")
                        defaultMapCommand.keywordSearch(it, true)
                        //添加到等待页面处理结果队列
                        defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.KeyWordSearch, intent)
                    } ?: run {
                        result.apply {
                            handleResult = false
                            tips = "导航去哪？"
                        }
                        returnIntentResult(result)
                    }
                }
            }
        }
    }

    /**
     *service = "mapU"
     *operation = "USR_POI_QUERY"
     */
    private suspend fun mapUUsrPoiQueryTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUUsrPoiQueryTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsUsrPoiQuery = gson.fromJson(slots.toString(), SlotsUsrPoiQuery::class.java)
            when {
                (slotsUsrPoiQuery.endLoc.ori_loc == HOME_ORI_LOC || slotsUsrPoiQuery.landmark?.ori_loc == HOME_ORI_LOC) -> {
                    //家相关操作
                    if (userBusiness.getHomePoi() == null) {
                        result.apply {
                            handleResult = false
                            tips = "请先设置家地址，再试试"
                        }
                        returnIntentResult(result)
                    } else {
                        when {
                            slotsUsrPoiQuery.viaLoc != null -> {
                                //先去<poi>再回(家|公司)，需要在路线规划页面中处理
                                defaultMapCommand.naviToHome()
                                val flag = defaultMapCommand.alongRouteSearch(slotsUsrPoiQuery.viaLoc.ori_loc)
                                defaultMapCommand.addMapCommandResultWaitQueue(
                                    MapCommandType.KeyWordSearchViaEnd,
                                    Pair(MapCommandType.AlongWaySearch, Pair(intent, slotsUsrPoiQuery.viaLoc.ori_loc))
                                )
                                if (flag) {
                                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AlongWaySearch, intent)
                                } else {
                                    result.apply {
                                        handleResult = false
                                        tips = "当前页面不支持操作"
                                    }
                                    returnIntentResult(result)
                                }
                            }

                            slotsUsrPoiQuery.landmark?.ori_loc == HOME_ORI_LOC -> {
                                //去家附近的<poi>
                                val flag = defaultMapCommand.aroundSearchByLocationName(slotsUsrPoiQuery.endLoc.ori_loc, HOME_ORI_LOC)
                                if (!flag) {
                                    result.apply {
                                        handleResult = false
                                        tips = "查找失败，请重试！"
                                    }
                                    returnIntentResult(result)
                                } else {
                                    //添加到等待页面处理结果队列
                                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AroundSearch, intent)
                                }
                            }

                            else -> {
                                //回家
                                defaultMapCommand.naviToHome()
                                result.tips = "这就回家"
                                returnIntentResult(result)
                            }

                        }
                    }
                }

                (slotsUsrPoiQuery.endLoc.ori_loc == COMPANY_ORI_LOC || slotsUsrPoiQuery.landmark?.ori_loc == COMPANY_ORI_LOC) -> {
                    //公司相关操作
                    if (userBusiness.getCompanyPoi() == null) {
                        result.apply {
                            handleResult = false
                            tips = "请先设置公司地址，再试试"
                        }
                        returnIntentResult(result)
                    } else {
                        when {
                            slotsUsrPoiQuery.viaLoc != null -> {
                                //先去<poi>再回(家|公司)，需要在路线规划页面中处理
                                defaultMapCommand.naviToWork()
                                val flag = defaultMapCommand.alongRouteSearch(slotsUsrPoiQuery.viaLoc.ori_loc)
                                defaultMapCommand.addMapCommandResultWaitQueue(
                                    MapCommandType.KeyWordSearchViaEnd,
                                    Pair(MapCommandType.AlongWaySearch, Pair(intent, slotsUsrPoiQuery.viaLoc.ori_loc))
                                )
                                if (flag) {
                                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AlongWaySearch, intent)
                                } else {
                                    result.apply {
                                        handleResult = false
                                        tips = "当前页面不支持操作"
                                    }
                                    returnIntentResult(result)
                                }
                            }

                            slotsUsrPoiQuery.landmark?.ori_loc == COMPANY_ORI_LOC -> {
                                //公司附近的<poi>
                                val flag = defaultMapCommand.aroundSearchByLocationName(slotsUsrPoiQuery.endLoc.ori_loc, COMPANY_ORI_LOC)
                                if (!flag) {
                                    result.apply {
                                        handleResult = false
                                        tips = "查找失败，请重试！"
                                    }
                                    returnIntentResult(result)
                                } else {
                                    //添加到等待页面处理结果队列
                                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AroundSearch, intent)
                                }
                            }

                            else -> {
                                //公司
                                defaultMapCommand.naviToWork()
                                result.tips = "出发去公司"
                                returnIntentResult(result)
                            }

                        }
                    }
                }

                else -> {
                    //导航到收藏地
                    when (val flag = defaultMapCommand.naviToFavorite()) {
                        "" -> {
                            result.apply {
                                handleResult = false
                                tips = "当前没有收藏地点哦"
                            }
                            returnIntentResult(result)
                        }

                        "openFavoritePage" -> {
                            //添加到等待页面处理结果队列
                            defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.NaviToFavorite, intent)
                        }

                        else -> {
                            //只有一个收藏点直接去导航了
                            result.tips = "找到1个目的地$flag，即将为您导航"
                            returnIntentResult(result)
                        }
                    }

                }
            }
        } ?: run {
            result.apply {
                handleResult = false
                tips = "不支持的操作"
            }
            returnIntentResult(result)
        }
    }

    private fun mapUViewTrans(intent: SingleIntentIFlyTek) {
        Timber.i("mapUViewTrans() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (!BaseConstant.MAP_APP_FOREGROUND) {
            result.apply {
                handleResult = false
                tips = "地图不在前台无法操作"
            }
            returnIntentResult(result)
            return
        }
        when (intent.operation) {
            "VIEW_TRANS_3D_HEAD_UP" -> {
                result.tips = "已切换到3D车头朝上"
                mapBusiness.switchMapViewMode(MapModeType.VISUALMODE_3D_CAR)
            }

            "VIEW_TRANS_2D_NORTH_UP" -> {
                result.tips = "已切换到2D北朝上"
                mapBusiness.switchMapViewMode(MapModeType.VISUALMODE_2D_NORTH)
            }

            "VIEW_TRANS_2D_HEAD_UP" -> {
                result.tips = "已切换到2D车头朝上"
                mapBusiness.switchMapViewMode(MapModeType.VISUALMODE_2D_CAR)
            }

            else -> {
                result.apply {
                    handleResult = false
                    tips = "不支持的操作"
                }
            }
        }
        returnIntentResult(result)
    }

    private fun mapUZoomInOut(intent: SingleIntentIFlyTek) {
        Timber.i("mapUZoomInOut() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        when (intent.operation) {
            "ZOOM_IN" -> {
                if (mapBusiness.isZoomInEnable()) {
                    mapBusiness.mapZoomIn()
                    result.tips = "已放大"
                } else {
                    result.tips = "已放到最大"
                }
            }

            "ZOOM_OUT" -> {
                if (mapBusiness.isZoomOutEnable()) {
                    mapBusiness.mapZoomOut()
                    result.tips = "已缩小"
                } else {
                    result.tips = "已缩到最小"
                }
            }

            else -> {
                result.apply {
                    handleResult = false
                    tips = "不支持的操作"
                }
            }
        }
        returnIntentResult(result)
    }

    private fun mapUTrafficOpera(intent: SingleIntentIFlyTek) {
        Timber.i("mapUTrafficOpera() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (!BaseConstant.MAP_APP_FOREGROUND) {
            result.apply {
                handleResult = false
                tips = "地图不在前台无法操作"
            }
            returnIntentResult(result)
            return
        }
        when (intent.operation) {
            "OPEN_TRAFFIC_INFO" -> {
                mapBusiness.setTmcVisible(true)
                result.tips = "已开启路况"
            }

            "CLOSE_TRAFFIC_INFO" -> {
                mapBusiness.setTmcVisible(false)
                result.tips = "已关闭路况"
            }

            else -> {
                result.apply {
                    tips = "不支持的操作"
                    handleResult = false
                }
            }
        }
        returnIntentResult(result)
    }

    /**
     * 电子眼提醒控制
     */
    private fun mapUEpolice(intent: SingleIntentIFlyTek) {
        Timber.i("mapUTrafficOpera() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        when (intent.operation) {
            "OPEN_E_POLICE" -> {
                settingComponent.setConfigKeySafeBroadcast(1) //巡航播报电子眼播报  0：off； 1：on
                result.tips = "已打开电子眼"
            }

            "CLOSE_E_POLICE" -> {
                settingComponent.setConfigKeySafeBroadcast(0) //巡航播报电子眼播报  0：off； 1：on
                result.tips = "已关闭电子眼"
            }

            else -> {
                result.apply {
                    tips = "不支持的操作"
                    handleResult = false
                }

            }
        }
        returnIntentResult(result)
    }

    private suspend fun mapULocate(intent: SingleIntentIFlyTek) {
        Timber.i("mapULocate() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsQuery = gson.fromJson(slots.toString(), SlotsQuery::class.java)
            when {
                //查询导航目的地名称
                (slotsQuery.endLoc.ori_loc == DESTINATION_ORI_LOC) -> {
                    mRouteRequestController.carRouteResult?.toPOI?.let {
                        result.tips = "目的地是" + it.name
                        returnIntentResult(result)
                    }
                }

                //我[现在]在哪[里]
                slotsQuery.endLoc.ori_loc == CURRENT_ORI_LOC -> {
                    val searchNearestResult = defaultMapCommand.nearestSearch(
                        defaultMapCommand.getLastLocation().longitude, defaultMapCommand
                            .getLastLocation()
                            .latitude
                    )
                    searchNearestResult?.let {
                        SearchCommonUtils.invertOrderList(searchNearestResult.poi_list)?.let { poiList ->
                            result.tips = "我们现在在" + poiList[0].name + "，地址" + poiList[0].address
                            returnIntentResult(result)
                        }
                    } ?: run {
                        result.apply {
                            handleResult = false
                            tips = "定位失败，请重试"
                            returnIntentResult(result)
                        }
                    }
                }

                slotsQuery.endLoc.ori_loc == CURRENT_ORI_LOC && slotsQuery.startLoc.ori_loc == CURRENT_ORI_LOC && slotsQuery.viaLoc?.ori_loc != null
                -> {
                    //todo 添加途经点，这里有点奇怪定位操作也能添加途经点
                    if (naviBusiness.isRealNavi() || routeBusiness.isPlanRouteing()) {
                        if ((mRouteRequestController.carRouteResult.midPois?.size ?: 0) < 15) {
                            val poiName = slotsQuery.viaLoc.ori_loc
                            if (poiName.isNotEmpty()) {
                                defaultMapCommand.alongRouteSearch(poiName)
                                defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AlongWaySearch, intent)
                            } else {
                                result.apply {
                                    handleResult = false
                                    tips = "您想将哪里设置为途经点"
                                }
                                returnIntentResult(result)
                            }
                        } else {
                            result.apply {
                                handleResult = false
                                tips = "您已添加了15个途经点，最多支持添加15个"
                            }
                            returnIntentResult(result)
                        }
                    } else {
                        result.apply {
                            handleResult = false
                            tips = "当前没有在导航中哦"
                        }
                        returnIntentResult(result)
                    }
                }

                //<poi>在(哪[里]|什么位置)
                else -> {
                    defaultMapCommand.keywordSearch(slotsQuery.endLoc.ori_loc, true)
                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.KeyWordSearch, intent)
                }
            }

        }
    }

    private suspend fun mapULocateRoad(intent: SingleIntentIFlyTek) {
        Timber.i("mapULocate() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        val searchNearestResult = defaultMapCommand.nearestSearch(
            defaultMapCommand.getLastLocation().longitude, defaultMapCommand
                .getLastLocation()
                .latitude
        )
        searchNearestResult?.let {
            SearchCommonUtils.invertOrderNearestRoadList(searchNearestResult.roadList)?.let { roadList ->
                result.tips = "我们现在在" + roadList[0].name
                returnIntentResult(result)
            }
        } ?: run {
            result.apply {
                handleResult = false
                tips = "定位失败，请重试"
            }
            returnIntentResult(result)
        }
    }

    private fun mapUUsrPoiModifyTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUUsrPoiModifyTask() called with: intent = $intent")
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsUsrPoiModify = gson.fromJson(slots.toString(), SlotsUsrPoiModify::class.java)
            when {
                slotsUsrPoiModify.endLoc.ori_loc == HOME_ORI_LOC -> {
                    //修改家的地址
                    val flag = defaultMapCommand.openModifyHomeCompanyAddressPage(HOME_ORI_LOC)
                    if (flag) {
                        //添加到等待页面处理结果队列
                        defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.OpenModifyHomeCompanyAddressPage, intent)
                    }
                }

                slotsUsrPoiModify.endLoc.ori_loc == COMPANY_ORI_LOC -> {
                    //修改公司的地址
                    val flag = defaultMapCommand.openModifyHomeCompanyAddressPage(COMPANY_ORI_LOC)
                    if (flag) {
                        //添加到等待页面处理结果队列
                        defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.OpenModifyHomeCompanyAddressPage, intent)
                    }
                }

                else -> {

                }
            }
        }
    }

    private fun mapUUsrPoiSetTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUUsrPoiSetTask() called with: intent = $intent")
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsUsrPoiSet = gson.fromJson(slots.toString(), SlotsUsrPoiSet::class.java)
            when {
                slotsUsrPoiSet.tag == HOME_ORI_LOC -> {
                    slotsUsrPoiSet.endLoc.ori_loc?.let {
                        if (naviBusiness.isRealNavi() && it == DESTINATION_ORI_LOC) {
                            favoriteDestinationPOI(intent, FavoriteType.FavoriteTypeHome)
                        } else {
                            //跳转家的地址搜索结果页
                            val flag = defaultMapCommand.searchHomeCompanyAddressResultPage(HOME_ORI_LOC, it)
                            if (flag) {
                                defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.SearchHomeCompanyAddressResultPage, intent)
                            }
                        }
                    }
                }

                slotsUsrPoiSet.tag == COMPANY_ORI_LOC -> {
                    slotsUsrPoiSet.endLoc.ori_loc.let {
                        if (naviBusiness.isRealNavi() && it == DESTINATION_ORI_LOC) {
                            favoriteDestinationPOI(intent, FavoriteType.FavoriteTypeCompany)
                        } else {
                            //跳转公司的地址搜索结果页
                            val flag = defaultMapCommand.searchHomeCompanyAddressResultPage(COMPANY_ORI_LOC, it)
                            if (flag) {
                                defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.SearchHomeCompanyAddressResultPage, intent)
                            }
                        }
                    }
                }

                slotsUsrPoiSet.endLoc.ori_loc == DESTINATION_ORI_LOC -> {
                    Timber.i("mapUUsrPoiSetTask favoriteDestinationPOI() DESTINATION_ORI_LOC")
                    favoriteDestinationPOI(intent)
                }

                else -> {
                    returnIntentResult(IntentResultIFlytek(intent.sid, false, "不支持的收藏操作", intent.intentIndex))
                }
            }
        }
    }

    private suspend fun mapUCollectTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUCollectTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsCollect = gson.fromJson(slots.toString(), SlotsCollect::class.java)
            when {
                //收藏当前地址为家/公司
                slotsCollect.tag == HOME_ORI_LOC || slotsCollect.tag == COMPANY_ORI_LOC -> {
                    val flag = defaultMapCommand.favoriteCurrentLocationPOI(slotsCollect.tag)
                    if (flag != null) {
                        result.tips = "已将${flag}设置为${slotsCollect.tag}"
                        returnIntentResult(result)
                    } else {
                        result.apply {
                            handleResult = false
                            tips = "收藏失败"
                        }
                        returnIntentResult(result)
                    }
                }

                //已收藏当前地点
                slotsCollect.insType == "COLLECT" && slotsCollect.endLoc == null && slotsCollect.startLoc == null -> {
                    //收藏当前地点
                    val flag = defaultMapCommand.favoriteCurrentLocationPOI(null)
                    when (flag) {
                        null -> {
                            result.apply {
                                handleResult = false
                                tips = "收藏失败"
                            }
                        }

                        "Home_Fail" -> {
                            result.apply {
                                handleResult = false
                                tips = "当前地址已经收藏为家，不能重复收藏哦"
                            }
                        }

                        "Company_Fail" -> {
                            result.apply {
                                handleResult = false
                                tips = "当前地址已经收藏公司，不能重复收藏哦"
                            }
                        }

                        else -> {
                            result.tips = "已收藏当前地点"
                            returnIntentResult(result)
                        }

                    }
                    returnIntentResult(result)
                }

                //收藏目的地
                slotsCollect.endLoc?.ori_loc == DESTINATION_ORI_LOC -> {
                    Timber.i("mapUCollectTask favoriteDestinationPOI() DESTINATION_ORI_LOC")
                    favoriteDestinationPOI(intent)
                }

                slotsCollect.startLoc?.ori_loc == CURRENT_ORI_LOC && slotsCollect.endLoc?.ori_loc != null -> {
                    //收藏<poi>，需要进入搜索结果页面后在提示要收藏那个，然后在收藏
                    defaultMapCommand.keyWordSearchForCollect(slotsCollect.endLoc.ori_loc)
                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.KeyWordSearchForCollect, intent)
                }
            }
        }
    }

    /**
     * 如果在路线规划页面直接选择第一条路线开始导航
     * 如果在导航中回到导航页面并且回车位
     */
    private fun mapUConfirmTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUConfirmTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (defaultMapCommand.confirm()) {
            defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.Confirm, intent)
        } else {
            result.apply {
                handleResult = false
                tips = "当前页面不支持操作"
            }
            returnIntentResult(result)
        }
    }

    /**
     * 返回地图
     */
    private suspend fun mapUBackMapTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUBackMapTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)

        val backCar = suspend {
            if (naviBusiness.isInitNavi()) { //导航中
                Timber.i("mapUBackMapTask() backToNavi")
                naviBusiness.naviBackCurrentCarPosition()
                mapBusiness.backToNavi.postValue(true) //回到地图主图
            } else {
                val isShowActivateLayout = activationMapBusiness.isShowActivateLayout
                val isShowAgreementLayout = activationMapBusiness.isShowAgreementLayout
                if (isShowActivateLayout.value == false || isShowAgreementLayout.value == false) {
                    Timber.i("mapUBackMapTask() backToMap")
                    mapBusiness.backToMap.postValue(true) //回到地图主图
                }
            }
            result.tips = "好的"
            returnIntentResult(result)
        }

        if (BaseConstant.MAP_APP_FOREGROUND) { //地图在前台
            backCar()
        } else { //地图在后台
            if (defaultMapCommand.openMap()) { //打开地图
                backCar()
            } else {
                result.apply {
                    handleResult = false
                    tips = "当前页面不支持操作"
                }
                returnIntentResult(result)
            }
        }
    }

    /**
     * 打开地图设置页面
     */
    private fun mapUOpenSettingTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUOpenSettingTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (defaultMapCommand.openSettingsPage()) {
            result.tips = "好的，已为您打开导航设置页"
            returnIntentResult(result)
        } else {
            result.apply {
                handleResult = false
                tips = "当前页面不支持操作"
            }
            returnIntentResult(result)
        }
    }

    /**
     * 打开收藏夹页面
     */
    private fun mapUOpenMapCollectTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUOpenMapCollectTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (defaultMapCommand.openFavoritePage()) {
            result.tips = "好的，已为您打开收藏夹"
            returnIntentResult(result)
        } else {
            result.apply {
                handleResult = false
                tips = "当前页面不支持操作"
            }
            returnIntentResult(result)
        }
    }

    private fun mapUCancelMapTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapUCancelMapTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (naviBusiness.isNavigating()) {
            naviBusiness.stopNavi()
            result.tips = "好的，已结束"
            mapBusiness.backToMap.postValue(true) //回到地图主图
        } else {
            if (mapBusiness.isInHomeFragment()) {
                result.tips = "当前不在导航中哦"
            } else {
                mapBusiness.backToMap.postValue(true) //回到地图主图
                result.tips = "好的"
            }
        }
        returnIntentResult(result)
    }

    /**
     * 导航信息
     */
    private fun mapUNaviInfo(intent: SingleIntentIFlyTek) {
        Timber.i("mapUNaviInfo() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsNaviInfo = gson.fromJson(slots.toString(), SlotsNaviInfo::class.java)
            if (!BaseConstant.MAP_APP_FOREGROUND) {
                defaultMapCommand.openMap()
            }
            when (slotsNaviInfo.naviInfo) {
                //查看全览
                "VIA_INFO" -> {
                    if (naviBusiness.isRealNavi()) {
                        mapBusiness.backToNavi.postValue(true) //回到导航页面
                        naviBusiness.showPreview()
                        result.tips = "好的"
                        returnIntentResult(result)
                    }
                }
                //告诉用户怎么走，比如 前方请左转
                "FORWARD" -> {
                    if (naviBusiness.isRealNavi()) {
                        if (ttsPlayBusiness.playNaviManual()) {
                            returnIntentResult(result)
                        } else {
                            result.handleResult = false
                            result.tips = "请稍后再试"
                            returnIntentResult(result)
                        }
                    }
                }

                "TIME_REMAIN", "DISTANCE_REMAIN" -> {
                    if (naviBusiness.isRealNavi()) {
                        naviBusiness.naviInfo.value?.let { naviInfo ->
                            val etaDistance = NavigationUtil.formatDistanceArray(naviInfo.routeRemain.dist)
                            var etcTime = NavigationUtil.switchFromSecond(naviInfo.routeRemain.time)
                            if (etcTime.last().toString() == "分")
                                etcTime += "钟"
                            result.tips = "距离目的地还有${etaDistance[0] + etaDistance[1]}，还需$etcTime}"
                            returnIntentResult(result)
                        }
                    }
                }

                "CURRENT_SPEED_LIMIT" -> {
                    result.tips = "未查到当前道路的限速值，请您安全驾驶"
                    if (naviBusiness.isRealNavi() && (naviBusiness.mCurrentRoadSpeed.value ?: 0) > 0) {
                        result.tips = "当前道路限速${naviBusiness.mCurrentRoadSpeed.value}"
                    }
                    returnIntentResult(result)
                }


            }
        }
    }

    /**
     * 关闭设置页面
     */
    private fun mapUCloseSetting(intent: SingleIntentIFlyTek) {
        Timber.i("mapUCloseSetting() called with: intent = $intent")
        if (defaultMapCommand.closeSettingsPage()) {
            defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.CloseSettingPage, intent)
        }
    }

    /**
     * 删除途经点
     */
    private fun mapUCancelPassAway(intent: SingleIntentIFlyTek, all: Boolean = false) {
        Timber.i("mapUCancelPassAway() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (naviBusiness.isRealNavi() || routeBusiness.isPlanRouteing()) {
            if (mRouteRequestController.carRouteResult.midPois.isNullOrEmpty()) {
                result.apply {
                    handleResult = false
                    tips = "还没有添加途经点哦"
                }
                returnIntentResult(result)
            } else {
                if (all) {
                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.CancelPassAwayAll, intent)
                    if (naviBusiness.isRealNavi()) {
                        naviBusiness.deleteViaPoi(null)
                    } else {
                        routeBusiness.deleteAllViaPoi()
                    }
                } else {
                    val passWaySize = mRouteRequestController.carRouteResult.midPois.size
                    intent.semantic.getAsJsonObject("slots")?.let { slots ->
                        val slotsCancelPassAway = gson.fromJson(slots.toString(), SlotsCancelPassAway::class.java)
                        when {
                            //没有指定删除哪个途经点
                            slotsCancelPassAway.posRank == null && slotsCancelPassAway.viaLoc == null -> {
                                if (passWaySize == 1) {
                                    defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.CancelPassAwayOne, intent)
                                    if (naviBusiness.isRealNavi()) {
                                        naviBusiness.deleteViaPoi(0)
                                    } else {
                                        routeBusiness.deleteViaPoi(0)
                                    }
                                } else {
                                    result.tips = "请问删除第几个途径点"
                                    returnIntentResult(result)
                                }
                            }

                            slotsCancelPassAway.posRank != null -> {
                                slotsCancelPassAway.posRank.offset?.toInt()?.let { offset ->
                                    if (offset > passWaySize || offset <= 0) {
                                        result.apply {
                                            handleResult = false
                                            tips = "只有${passWaySize}个途径点，请换个试试"
                                        }
                                        returnIntentResult(result)
                                    } else {
                                        defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.CancelPassAwayIndex, Pair(intent, offset))
                                        if (naviBusiness.isRealNavi()) {
                                            naviBusiness.deleteViaPoi(offset - 1)
                                        } else {
                                            routeBusiness.deleteViaPoi(offset - 1)
                                        }
                                    }
                                }
                            }

                            slotsCancelPassAway.viaLoc != null -> {
                                val poiName = slotsCancelPassAway.viaLoc.ori_loc
                                if (poiName.isEmpty()) {
                                    result.apply {
                                        result.handleResult = false
                                        result.tips = "没有这个途径点哦"
                                    }
                                    returnIntentResult(result)
                                } else {
                                    var indexPoi = -1
                                    var count = 0
                                    mRouteRequestController.carRouteResult.midPois.forEachIndexed { index, poi ->
                                        if (poi.name.contains(poiName)) {
                                            indexPoi = index
                                            count++
                                        }
                                    }
                                    when {
                                        count > 1 -> {
                                            result.tips = "找到多个相似的途经点，请说全称哦"
                                            result.handleResult = false
                                            returnIntentResult(result)
                                        }

                                        count == 1 -> {
                                            defaultMapCommand.addMapCommandResultWaitQueue(
                                                MapCommandType.CancelPassAwayIndex,
                                                Pair(intent, mRouteRequestController.carRouteResult.midPois[indexPoi].name)
                                            )
                                            if (naviBusiness.isRealNavi()) {
                                                naviBusiness.deleteViaPoi(indexPoi)
                                            } else {
                                                routeBusiness.deleteViaPoi(indexPoi)
                                            }
                                        }

                                        else -> {
                                            result.tips = "没有这个途径点哦"
                                            result.handleResult = false
                                            returnIntentResult(result)
                                        }
                                    }

                                }
                            }

                            else -> {}
                        }

                    }
                }
            }
        } else {
            result.apply {
                handleResult = false
                tips = "当前没有在导航中哦"
            }
            returnIntentResult(result)
        }
    }

    /**
     * 添加途经点
     */
    private fun mapUPassAway(intent: SingleIntentIFlyTek) {
        Timber.i("mapUPassAway() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        if (naviBusiness.isRealNavi() || routeBusiness.isPlanRouteing()) {
            if ((mRouteRequestController.carRouteResult.midPois?.size ?: 0) < 15) {
                intent.semantic.getAsJsonObject("slots")?.let { slots ->
                    val slotsPassAway = gson.fromJson(slots.toString(), SlotsPassAway::class.java)
                    when {
                        slotsPassAway.viaLoc != null -> {
                            val poiName = slotsPassAway.viaLoc.ori_loc
                            if (poiName.isNotEmpty()) {
                                defaultMapCommand.alongRouteSearch(poiName)
                                defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.AlongWaySearch, intent)
                            } else {
                                result.apply {
                                    handleResult = false
                                    tips = "您想将哪里设置为途经点"
                                }
                                returnIntentResult(result)
                            }
                        }

                        else -> {
                            result.apply {
                                handleResult = false
                                tips = "您想将哪里设置为途经点"
                            }
                            returnIntentResult(result)
                        }
                    }
                }
            } else {
                result.apply {
                    handleResult = false
                    tips = "您已添加了15个途经点，最多支持添加15个"
                }
                returnIntentResult(result)

            }
        } else {
            result.apply {
                handleResult = false
                tips = "当前没有在导航中哦"
            }
            returnIntentResult(result)
        }
    }

    /**
     * 选择第几个场景
     */
    private fun mapUPosRank(intent: SingleIntentIFlyTek) {
        Timber.i("mapUPosRank() called with: intent = $intent")
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsPosRank = gson.fromJson(slots.toString(), SlotsPosRank::class.java)
            slotsPosRank.posRank?.offset?.toInt()?.let { offset ->
                defaultMapCommand.posRank(Pair(slotsPosRank.posRank.direct!!, offset))
                defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.PosRank, intent)
            }
        }
    }

    /**
     * 下一页
     */
    private fun mapUPageRank(intent: SingleIntentIFlyTek) {
        Timber.i("mapUPageRank() called with: intent = $intent")
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsPageRank = gson.fromJson(slots.toString(), SlotsPageRank::class.java)
            slotsPageRank.pageRank?.direct?.let { direct ->
                defaultMapCommand.pageRank(Pair(direct, slotsPageRank.pageRank.offset!!.toInt()))
                defaultMapCommand.addMapCommandResultWaitQueue(MapCommandType.PageRank, intent)
            }
        }
    }

    /**
     * 收藏目的地
     */
    private fun favoriteDestinationPOI(
        intent: SingleIntentIFlyTek, @FavoriteType.FavoriteType1 type: Int = FavoriteType.FavoriteTypePoi
    ) {
        Timber.i("favoriteDestinationPOI() called with: intent = $intent, type = $type")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        mRouteRequestController.carRouteResult?.toPOI?.let { endPoi ->
            when (type) {
                FavoriteType.FavoriteTypeHome -> {
                    if (userBusiness.addFavorite(endPoi, type = type)) {
                        result.tips = "好的，已将家的地址设置为目的地${endPoi.name}"
                    } else {
                        result.tips = "当前目的地/路线不支持收藏"
                    }
                }

                FavoriteType.FavoriteTypeCompany -> {
                    if (userBusiness.addFavorite(endPoi, type = type)) {
                        result.tips = "好的，已将公司的地址设置为目的地${endPoi.name}"
                    } else {
                        result.tips = "当前目的地/路线不支持收藏"
                    }
                }

                else -> {
                    if (userBusiness.isFavorited(endPoi)) {
                        result.tips = "好的，收藏成功"
                    } else {
                        if (userBusiness.addFavorite(endPoi, type = type)) {
                            result.tips = "好的，收藏成功"
                        } else {
                            result.tips = "当前目的地/路线不支持收藏"
                        }
                    }
                }
            }
            returnIntentResult(result)
        } ?: run {
            result.tips = "当前目的地/路线可收藏"
            result.handleResult = false
            returnIntentResult(result)
        }
    }

    /**
     * 地图小地图模式
     */
    private fun mapUSetMiniMapMode(intent: SingleIntentIFlyTek, type: Int) {
        Timber.i("mapUSetMiniMapMode() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "好的，已为您切换成功", intent.intentIndex)
        if (naviBusiness.isRealNavi()) {
            when (type) {
                //鹰眼图
                0 -> {
                    navigationSettingBusiness.setupMapType(0)
                    returnIntentResult(result)
                }

                //光柱图
                1 -> {
                    navigationSettingBusiness.setupMapType(1)
                    returnIntentResult(result)
                }
            }
        }
    }

    private fun mapURoutePlanTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapURoutePlanTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsRoutePlan = gson.fromJson(slots.toString(), SlotsRoutePlan::class.java)
            slotsRoutePlan.routeCondition?.let { routeCondition ->
                if (routeCondition == "SHORTEST") {
                    Timber.i("parseIntentCommand() called with: slotsExit.routeCondition == SHORTEST")
                    if (slotsRoutePlan.endLoc != null) {
                        mapUAlongSearchTask(intent, VoiceAlongSearchType.SHORTEST)
                    } else {
                        result.handleResult = false
                        result.tips = "不支持此路线偏好"
                        returnIntentResult(result)
                        return
                    }
                } else {
                    if (!naviBusiness.isRealNavi() && !routeBusiness.isInRouteFragment) {
                        result.handleResult = false
                        result.tips = "当前不在导航中哦"
                        returnIntentResult(result)
                        return
                    }
                    when {
                        //高速优先
                        routeCondition == "HIGH_FIRST" -> {
                            navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_USING_HIGHWAY)
                            result.tips = "已切换为高速优先的路线了"
                        }
                        //不走高速
                        routeCondition == "NOT_HIGH_FIRST" -> {
                            navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY)
                            result.tips = "已切换为不走高速的路线了"
                        }
                        //躲避拥堵
                        routeCondition == "AVOID_ROUND" -> {
                            navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_JAN)
                            result.tips = "已切换为躲避拥堵的路线了"
                        }

                        //避免收费 | 避免收费
                        routeCondition == "CHEAPEST" || routeCondition == "FREE" -> {
                            navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE)
                            result.tips = "已切换为避免收费的路线了"
                        }
                        //推荐路线
                        routeCondition == "DEFAULT" || routeCondition == "INTELLECT_RECOMMEND" -> {
                            navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_DEFAULT)
                            result.tips = "已切换为推荐路线了"
                        }
                        //大路优先
                        routeCondition == "MAINROAD_FIRST" -> {
                            navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST)
                            result.tips = "已切换为大路优先的路线了"
                        }
                        //最快路线
                        routeCondition == "FASTEST" -> {
                            navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST)
                            result.tips = "已切换为最快路线了"
                        }

                        routeCondition == "LIGHT_LESS" -> {
                            navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST)
                            result.tips = "已切换成速度最快的路线了"
                        }

                        routeCondition.split("|").size == 2 -> {
                            when {
                                //不⾛⾼速且避免收费 | 少收费+不⾛⾼速
                                routeCondition.contains("NOT_HIGH_FIRST") &&
                                        (routeCondition.contains("CHEAPEST") || routeCondition.contains("FREE")) -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY)
                                    result.tips = "已切换为不走高速且避免收费的路线了"
                                }

                                //躲避收费和拥堵 | 躲避拥堵+少收费
                                routeCondition.contains("AVOID_ROUND") &&
                                        (routeCondition.contains("CHEAPEST") || routeCondition.contains("FREE")) -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY)
                                    result.tips = "已切换为躲避收费和拥堵的路线了"
                                }

                                //躲避拥堵+不⾛⾼速
                                routeCondition.contains("NOT_HIGH_FIRST") &&
                                        routeCondition.contains("AVOID_ROUND") -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY)
                                    result.tips = "已切换为躲避拥堵和不走高速的路线了"
                                }

                                //躲避拥堵且⾼速优先 | 躲避拥堵+⾼速优先
                                routeCondition.contains("AVOID_ROUND") &&
                                        routeCondition.contains("HIGH_FIRST") -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY)
                                    result.tips = "已切换为躲避拥堵且高速优先的路线了"
                                }

                                //躲避拥堵+⼤路优先
                                routeCondition.contains("AVOID_ROUND") &&
                                        routeCondition.contains("MAINROAD_FIRST") -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST)
                                    result.tips = "已切换为躲避拥堵和大路优先的路线了"
                                }

                                //躲避拥堵+速度最快
                                routeCondition.contains("AVOID_ROUND") &&
                                        routeCondition.contains("FASTEST") -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST)
                                    result.tips = "已切换为躲避拥堵和速度最快的路线了"
                                }

                                else -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_DEFAULT)
                                    result.tips = "不支持此路线偏好，为您切换到推荐路线"
                                }
                            }
                        }

                        routeCondition.split("|").size == 3 -> {
                            when {
                                //不⾛⾼速躲避收费和拥堵 | 躲避拥堵+少收费+不⾛⾼速
                                routeCondition.contains("NOT_HIGH_FIRST") &&
                                        (routeCondition.contains("CHEAPEST") || routeCondition.contains("FREE")) &&
                                        routeCondition.contains("AVOID_ROUND") -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY)
                                }

                                else -> {
                                    navigationSettingBusiness.checkAndSavePrefer(ConfigRoutePreference.PREFERENCE_DEFAULT)
                                    result.tips = "不支持此路线偏好，为您切换到推荐路线"
                                }
                            }
                        }

                        //红绿灯最少"LIGHT_LESS" 不支持
                        else -> {
                            result.handleResult = false
                            result.tips = "不支持此路线偏好"
                            returnIntentResult(result)
                            return
                        }

                    }

                    if (naviBusiness.isNavigating()) {
                        naviBusiness.onReRouteFromPlanPref(navigationSettingBusiness.routePreference)
                    }
                    returnIntentResult(result)
                }
            }

        }
    }

    /**
     * 切换路线
     */
    private fun mapURerouteTask(intent: SingleIntentIFlyTek) {
        Timber.i("mapURerouteTask() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        val switchResult = defaultMapCommand.switchRoute()
        result.handleResult = switchResult.first
        result.tips = switchResult.second
        returnIntentResult(result)
    }

    /**
     * 打开/关闭导航播报
     */
    private suspend fun mapUTnsType(intent: SingleIntentIFlyTek) {
        Timber.i("mapUTnsType() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            gson.fromJson(slots.toString(), SlotsTnsType::class.java).insType.let {
                when (it) {
                    "OPEN" -> {
                        mapUBackMapTask(intent)
                        return
                    }

                    "COLLECT" -> {
                        mapUOpenMapCollectTask(intent)
                        return
                    }

                    "DISPLAY_MODE_UNMUTE" -> {
                        navigationSettingBusiness.setConfigKeyMute(0)
                        result.tips = "已打开导航播报"
                    }

                    "DISPLAY_MODE_MUTE" -> {
                        navigationSettingBusiness.setConfigKeyMute(1)
                        result.tips = "已关闭导航播报"
                    }
                    //1：经典简洁播报； 2：新手详细播报，默认态； 3：极简播报
                    "DISPLAY_MODE_NOVICE" -> {
                        defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_DETAIL)
                        result.tips = "导航播报模式已设置为详细播报模式"
                    }

                    "DISPLAY_MODE_VETERAN" -> {
                        //老手 == 极简
                        defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_MINIMALISM)
                        result.tips = "导航播报模式已设置为极简播报模式"
                    }

                    "DISPLAY_MODE_STANDARD" -> {
                        defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_EASY)
                        result.tips = "导航播报模式已设置为经典简洁播报模式"
                    }

                    "DISPLAY_MODE_DETAIL" -> {
                        defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_DETAIL)
                        result.tips = "导航播报模式已设置为默认详细播报模式"
                    }

                    "DISPLAY_MODE_CONCISION" -> {
                        defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_EASY)
                        result.tips = "导航播报模式已设置为经典简洁播报模式"
                    }

                    "DISPLAY_MODE_EXTREMELY_CONCISION" -> {
                        defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_MINIMALISM)
                        result.tips = "导航播报模式已设置为极简播报模式"
                    }

                    "VIEW_TRANS" -> {
                        when (navigationSettingBusiness.getConfigKeyBroadcastMode()) {
                            SettingConst.BROADCAST_EASY -> {
                                defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_MINIMALISM)
                                result.tips = "导航播报模式已设置为极简播报模式"
                            }

                            SettingConst.BROADCAST_DETAIL -> {
                                defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_EASY)
                                result.tips = "导航播报模式已设置为简洁播报模式"
                            }

                            SettingConst.BROADCAST_MINIMALISM -> {
                                defaultMapCommand.setVoiceBroadcastMode(SettingConst.BROADCAST_DETAIL)
                                result.tips = "导航播报模式已设置为详细播报模式"
                            }
                        }
                    }

                    else -> {
                        result.handleResult = false
                        result.tips = "还不支持这个操作呢"
                    }
                }
                returnIntentResult(result)
            }
        }
    }

    private suspend fun carNumberQuery(intent: SingleIntentIFlyTek) {
        Timber.i("carNumberQuery() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsQuery = gson.fromJson(slots.toString(), SlotsCarNumberQuery::class.java)
            if (slotsQuery.datetime != null) {
                if (slotsQuery.datetime.dateOrig == null || slotsQuery.datetime.dateOrig == "今天") {
                    val number = navigationSettingBusiness.getLicensePlateNumber()
                    var cityNamee = ""
                    val adCode = slotsQuery.location?.city?.let { cityName ->
                        cityNamee = cityName
                        mapDataBusiness.searchAdCode(cityName)?.firstOrNull()
                    } ?: run {
                        mapDataBusiness.getAdCodeByLonLat(
                            defaultMapCommand.getLastLocation().longitude, defaultMapCommand.getLastLocation()
                                .latitude
                        )
                    }
                    cityNamee = if (TextUtils.isEmpty(cityNamee)) mapDataBusiness.getCityInfo(adCode)?.cityName ?: "" else cityNamee

                    Timber.i("carNumberQuery() called with: adCode = $adCode number = $number cityNamee = $cityNamee")

                    val strictedAreaInfo = getStrictedAreaInfo(number, adCode.toString())

                    if (!TextUtils.isEmpty(strictedAreaInfo)) {
                        aosBusiness.sendReqTrafficRestrict(GTrafficRestrictRequestParam().apply {
                            if (adCode != -1) {
                                Adcode = adCode.toLong()
                            }
                            CarPlate = number
                        }) { gTrafficRestrictResponseParam ->
                            if (gTrafficRestrictResponseParam?.Restrict != null) {
                                val tips1 =
                                    if (TextUtils.isEmpty(gTrafficRestrictResponseParam.Restrict.m_plateNo)) "" else "限行尾号" + gTrafficRestrictResponseParam.Restrict.m_plateNo

                                result.tips = "${cityNamee}限行政策，${strictedAreaInfo}$tips1"
                                result.handleResult = true
                            } else {
                                result.tips = "${cityNamee}限行政策，${strictedAreaInfo}"
                                result.handleResult = true
                            }
                            returnIntentResult(result)
                        }
                    } else {
                        result.tips = if (TextUtils.isEmpty(number)) "没有查到${cityNamee}的限行政策" else "您的车牌不限行,请放心行驶"
                        result.handleResult = true
                        returnIntentResult(result)
                    }

                } else {
                    result.tips = "不支持查询指定日期的限行信息"
                    result.handleResult = false
                    returnIntentResult(result)
                }
            } else {
                result.tips = "不支持的操作"
                result.handleResult = false
                returnIntentResult(result)
            }
        }

    }

    private suspend fun getStrictedAreaInfo(plateNumber: String, adCodes: String): String {
        var tips = ""
        val result = aosBusiness.getStrictedAreaInfo(plateNumber, adCodes)
        when (result.status) {
            Status.SUCCESS -> {
                Timber.i("getRestrictedDetail onSuccess")
                result.data?.let { data ->
                    data.cities?.forEach { city ->
                        //0、本地小客车； 1、外地小客车； 2、本地货车； 3、外地货车；
                        if (city.ruleType == 0 || city.ruleType == 1) {
                            city.rules?.forEach { rule ->
                                //0:全部车型，1：小客车；2：货车
                                if ((rule.vehicle == 0 || rule.vehicle == 1) && !TextUtils.isEmpty(rule.policyname)) {
                                    tips += rule.policyname + "，"
                                }
                            }
                        }

                    }
                }
            }

            Status.ERROR -> {
                Timber.i("getRestrictedDetail ERROR = ${result.throwable.toString()}")
            }

            else -> {
                Timber.i("getRestrictedDetail else is called")
            }
        }
        Timber.i("getStrictedAreaInfo tips = $tips")
        return tips.trim()
    }

    private fun mapUQueryTrafficInfo(intent: SingleIntentIFlyTek) {
        Timber.i("mapUQueryTrafficInfo() called with: intent = $intent")
        val result = IntentResultIFlytek(intent.sid, true, "", intent.intentIndex)
        intent.semantic.getAsJsonObject("slots")?.let { slots ->
            val slotsQueryTrafficInfo = gson.fromJson(slots.toString(), OperationTypeSlots::class.java)
            if (slotsQueryTrafficInfo.insType == "QUERY_TRAFFIC_INFO") {
                if (naviBusiness.isRealNavi() || cruiseBusiness.isCruising()) {
                    BaseConstant.TR_TTS_BROADCAST = 1
                    if (ttsPlayBusiness.playTrafficStatus()) {
                        returnIntentResult(result)
                    } else {
                        result.handleResult = false
                        result.tips = "未获取到前方路况，请稍后再试试哦"
                        returnIntentResult(result)
                    }
                } else {
                    result.handleResult = false
                    result.tips = "当前不在导航或巡航中哦"
                    returnIntentResult(result)
                }
            }
        }
    }


    companion object {
        const val CURRENT_ORI_LOC = "CURRENT_ORI_LOC"
        const val HOME_ORI_LOC = "家"
        const val COMPANY_ORI_LOC = "公司"
        const val DESTINATION_ORI_LOC = "目的地"

        fun poiConvertToPoiIFlyTek(poi: POI, isVia: Boolean = false): PoiIFlyTek {
            return PoiIFlyTek(
                poiid = poi.id,
                poitype = if (isVia) "viaLoc" else "endLoc",
                name = poi.name,
                address = poi.addr,
                distance = poi.distance,
                longitude = poi.point.longitude.toString(),
                latitude = poi.point.latitude.toString(),
            )
        }
    }

    enum class Scene {
        poiSearch,
        navi,
    }

    enum class ActiveStatus {
        fg,
        bg,
        noExists,
    }

    enum class SceneStatus {
        oneTarget,
        moreTarget,
        naviConfirm,
        noNavi,
        routing,
        navigation,
    }

    enum class VoiceAlongSearchType {
        NEAREST,
        SHORTEST,
        NORMAL
    }

}