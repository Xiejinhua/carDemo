package com.desaysv.psmap.base.tracking

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 埋点工具类
 */
object EventTrackingUtils {
    private lateinit var eventTracking: IEventTrack

    private var enableEventTracking = false

    enum class EventName {
        App_Open,//应用打开
        App_Close,//应用关闭
        Nav_Start,//导航相关
        Nav_Finish,//导航结束
        Search_Click,//搜索
        Map_Set,//地图设置
        SurroundSearch_Click,//周边搜索
        OnthewaySearch_Click,//顺路搜索
        JetourOnly_Click,//捷途专属
        RoadBookSearch_Click,//路书搜索
        IntSearch_Click//探趣搜索
    }

    enum class EventValueName {
        OpsMode,
        OpsTime,
        ClsMode,
        ClsTime,
        StartTime,
        Departure,
        Destination,
        MappageForm,
        HomeClick,
        CompanyClick,
        FavoritesClick,
        TeamClick,
        GPSLocClick,
        OnerefuelClick,
        TravelShareClick,
        MailShareTime,
        SearchType,
        EndTime,
        MuteSet,
        MuteClick,
        TowardSet,
        TowardClick,
        RoutePerferSet,
        AvoidLimitSw,
        OverviewModeSet,
        BroadcastModeSet,
        BroadcastSet,
        CruiseBroadcastSet,
        CBSw,
        VCSet,
        WordSize,
        RoadCondSw,
        FpointSw,
        AutscalebarSw,
        IntenNavSw,
        LoginStatus,
        LogonTime,
        LogoutTime,
        LoginType,
        WeChatStatus,
        AutoRecordSw,
        PhoneToCarStatus,
        SearchTime,
        SearchCategory,
        JetourOnlyClick,
        JetourEQuity,
        JetourEQuityClick,
        JetourRbookClick,
        JetourInterestClick
    }

    fun init(@ApplicationContext context: Context, eventTrack: IEventTrack, enableTracking: Boolean) {
        enableEventTracking = enableTracking
        Timber.i("init enableEventTracking = enableEventTracking")
        if (!enableEventTracking)
            return
        eventTracking = eventTrack
        eventTracking.init(context)
    }

    /**
     * 多参数
     */
    fun trackEvent(eventName: EventName, params: Map<EventTrackingUtils.EventValueName, Any>) {
        if (!enableEventTracking)
            return
        Timber.i("trackEvent eventName=$eventName")
        MainScope().launch { eventTracking.trackEvent(eventName, params) }
    }

    /**
     * 单参数
     */
    fun trackEvent(eventName: EventName, param: Pair<EventTrackingUtils.EventValueName, Any>) {
        if (!enableEventTracking)
            return
        Timber.i("trackEvent eventName=$eventName")
        MainScope().launch { eventTracking.trackEvent(eventName, mapOf(param)) }
    }
}