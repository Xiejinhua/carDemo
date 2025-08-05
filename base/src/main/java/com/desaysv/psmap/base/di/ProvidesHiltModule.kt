package com.desaysv.psmap.base.di

import android.app.Application
import com.autonavi.auto.skin.SkinManager
import com.autosdk.bussiness.account.AccountController
import com.autosdk.bussiness.account.BehaviorController
import com.autosdk.bussiness.account.ForecastController
import com.autosdk.bussiness.account.LinkCarController
import com.autosdk.bussiness.account.SyncSdkController
import com.autosdk.bussiness.account.UserGroupController
import com.autosdk.bussiness.account.UserTrackController
import com.autosdk.bussiness.activate.ActivateController
import com.autosdk.bussiness.aos.AosController
import com.autosdk.bussiness.authentication.AuthenticationController
import com.autosdk.bussiness.data.HotUpdateController
import com.autosdk.bussiness.data.MapDataController
import com.autosdk.bussiness.data.ThemeDataController
import com.autosdk.bussiness.data.VoiceDataController
import com.autosdk.bussiness.information.InformationController
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.location.LocationController
import com.autosdk.bussiness.location.LocationReplayController
import com.autosdk.bussiness.manager.SDKManager
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.navi.NaviController
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.navi.route.utils.RouteLifecycleMonitor
import com.autosdk.bussiness.push.PushController
import com.autosdk.bussiness.scene.SceneModuleController
import com.autosdk.bussiness.search.SearchController
import com.autosdk.bussiness.search.SearchControllerV2
import com.autosdk.bussiness.speech.SpeechSynthesizeController
import com.autosdk.bussiness.widget.mapview.MapViewComponent
import com.autosdk.bussiness.widget.navi.NaviComponent
import com.autosdk.bussiness.widget.route.RouteComponent
import com.autosdk.bussiness.widget.setting.SettingComponent
import com.autosdk.common.utils.DayStatusSystemUtil
import com.autosdk.common.utils.UploadPositionHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ProvidesHiltModule {
    @Singleton
    @Provides
    fun bindGson(): Gson {
        return GsonBuilder().serializeSpecialFloatingPointValues().create()
    }

    // ============================ SDK Controller相关 ================================
    @Singleton
    @Provides
    fun bindAccountController(application: Application): AccountController {
        AccountController.getInstance().setContext(application)
        return AccountController.getInstance()
    }

    @Singleton
    @Provides
    fun bindBehaviorController(): BehaviorController {
        return BehaviorController.getInstance()
    }

    @Singleton
    @Provides
    fun bindLinkCarController(): LinkCarController {
        return LinkCarController.getInstance()
    }

    @Singleton
    @Provides
    fun bindSyncSdkController(): SyncSdkController {
        return SyncSdkController.getInstance()
    }

    @Singleton
    @Provides
    fun bindUserGroupController(): UserGroupController {
        return UserGroupController.getInstance()
    }

    @Singleton
    @Provides
    fun bindUserTrackController(): UserTrackController {
        return UserTrackController.getInstance()
    }

    @Singleton
    @Provides
    fun bindAosController(): AosController {
        return AosController.getInstance()
    }

    @Singleton
    @Provides
    fun bindPushController(): PushController {
        return PushController.getInstance()
    }

    @Singleton
    @Provides
    fun bindMapDataController(): MapDataController {
        return MapDataController.getInstance()
    }

    @Singleton
    @Provides
    fun bindRouteRequestController(): RouteRequestController {
        return RouteRequestController.getInstance()
    }

    @Singleton
    @Provides
    fun bindLocationController(): LocationController {
        return LocationController.getInstance()
    }

    @Singleton
    @Provides
    fun bindNaviController(application: Application): NaviController {
        NaviController.getInstance().setContext(application)
        return NaviController.getInstance()
    }

    @Singleton
    @Provides
    fun bindSearchController(): SearchController {
        return SearchController.getInstance()
    }

    @Singleton
    @Provides
    fun bindSearchControllerV2(): SearchControllerV2 {
        return SearchControllerV2.getInstance()
    }

    @Singleton
    @Provides
    fun bindLayerController(): LayerController {
        return LayerController.getInstance()
    }

    @Singleton
    @Provides
    fun bindMapController(): MapController {
        return MapController.getInstance()
    }

    @Singleton
    @Provides
    fun bindThemeDataController(): ThemeDataController {
        return ThemeDataController.getInstance()
    }

    @Singleton
    @Provides
    fun bindHotUpdateController(): HotUpdateController {
        return HotUpdateController.getInstance()
    }

    @Singleton
    @Provides
    fun bindSceneModuleController(): SceneModuleController {
        return SceneModuleController.getInstance()
    }

    @Singleton
    @Provides
    fun bindInformationController(): InformationController {
        return InformationController.getInstance()
    }

    @Singleton
    @Provides
    fun bindSettingComponent(): SettingComponent {
        return SettingComponent.getInstance()
    }

    @Singleton
    @Provides
    fun bindActivateController(): ActivateController {
        return ActivateController.getInstance()
    }

    @Singleton
    @Provides
    fun bindSDKManager(): SDKManager {
        return SDKManager.getInstance()
    }

    @Singleton
    @Provides
    fun bindAuthenticationController(): AuthenticationController {
        return AuthenticationController.getInstance()
    }

    @Singleton
    @Provides
    fun bindMapViewComponent(): MapViewComponent {
        return MapViewComponent.getInstance()
    }

    @Singleton
    @Provides
    fun bindNaviComponent(): NaviComponent {
        return NaviComponent.getInstance()
    }

    @Singleton
    @Provides
    fun bindRouteComponent(): RouteComponent {
        return RouteComponent.getInstance()
    }

    @Singleton
    @Provides
    fun bindDayStatusSystemUtil(): DayStatusSystemUtil {
        return DayStatusSystemUtil.getInstance()
    }

    @Singleton
    @Provides
    fun bindSkinManager(): SkinManager {
        return SkinManager.getInstance()
    }

    @Singleton
    @Provides
    fun bindRouteLifecycleMonitor(): RouteLifecycleMonitor {
        return RouteLifecycleMonitor.getInstance()
    }

    @Singleton
    @Provides
    fun bindUploadPositionHandler(): UploadPositionHandler {
        return UploadPositionHandler.getInstance()
    }

    @Singleton
    @Provides
    fun bindLocationReplayController(): LocationReplayController {
        return LocationReplayController.getInstance()
    }

    @Singleton
    @Provides
    fun bindVoiceDataController(): VoiceDataController {
        return VoiceDataController.getInstance()
    }

    @Singleton
    @Provides
    fun bindForecastController(): ForecastController {
        return ForecastController.getInstance()
    }

    @Singleton
    @Provides
    fun bindSpeechSynthesizeController(): SpeechSynthesizeController {
        return SpeechSynthesizeController.getInstance()
    }
}
