package com.desaysv.psmap.model.bean

import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.search.model.SearchNearestResult
import com.autosdk.bussiness.common.POI

data class MapCommandBean(
    val mapCommandType: MapCommandType,
    val data: String? = null,
    val poi: POI? = null,
    val fromPoi: POI? = null,
    val viaPoi: List<POI>? = null,
    val searchPoiList: List<POI>? = null,
    val searchNearestResult: SearchNearestResult? = null,
    val commandRequestRouteNaviBean: CommandRequestRouteNaviBean? = null,
    val range: String? = null,
    val pair: Pair<String, Int>? = null,
    val cityItemInfo: CityItemInfo? = null,
    val day: String? = null,
    val isVoice: Boolean? = null,
)

enum class MapCommandType {
    /**
     * 搜索类相关或者其它需要结果的需要返回执行结果
     */
    KeyWordSearch,
    AroundSearch,
    AlongWaySearch,
    StartPlanRoute,
    NaviToHome,
    NaviToWork,
    RequestCityAreaInfo,
    RequestHomeAddress,
    RequestCompanyAddress,
    StartNavi,
    StartNaviWhenHasRoute,
    ChooseRoute,
    OpenSearchPage,
    OpenNaviPage,
    OpenSettingPage,
    OpenFavoritePage,
    OpenGroupPage,
    MoveAppToBack,
    ShowPoiCard,
    ShowPoiDetail,
    OpenModifyHomeCompanyAddressPage,
    SearchHomeCompanyAddressResultPage,
    KeyWordSearchForCollect,
    Confirm,
    CloseSettingPage,
    CancelPassAwayOne,
    CancelPassAwayIndex,
    CancelPassAwayAll,
    PosRank,
    PageRank,
    KeyWordSearchViaEnd,
    NaviToFavorite,
    SearchAhaTrip,
    AhaTripCollect,
    OpenAhaTripDetailPage,
    VoicePlanRouting,

    //前缀IOV_VOICE 是对接IOV语音jar包专用,其他模块请勿使用
    IOV_VOICE_KeyWordSearch,
    IOV_VOICE_AroundSearch,
    IOV_VOICE_NearestSearch,
    IOV_VOICE_WhereAmI,
    IOV_VOICE_AlongWaySearch,
    IOV_VOICE_RequestHomeAddress,
    IOV_VOICE_RequestCompanyAddress,

}

enum class MapCommandParamType {
    Search,
}


