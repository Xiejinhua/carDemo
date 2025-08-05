package com.desaysv.psmap.model.di

import android.app.Application
import android.content.Context
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.bussiness.widget.setting.SettingComponent
import com.autosdk.common.tts.IAutoPlayer
import com.desaysv.psmap.base.business.ActivationMapBusiness
import com.desaysv.psmap.base.business.EngineerBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SearchBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.SpeechSynthesizeBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.impl.IModelBusinessProxy
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.model.business.ModelBusinessManager
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.OutputDataBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.SettingComponentBusiness
import com.desaysv.psmap.model.business.TtsPlayBusiness
import com.desaysv.psmap.model.car.SimulationCarInfoManager
import com.desaysv.psmap.base.common.EVManager
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SimplePOIController
import com.desaysv.psmap.base.impl.AhaTripImpl
import com.desaysv.psmap.base.impl.AudioFocusManager
import com.desaysv.psmap.model.business.AhaTripBusiness
import com.desaysv.psmap.model.business.AhaTripManager
import com.desaysv.psmap.model.business.CustomTeamBusiness
import com.desaysv.psmap.model.car.CheryPlatformCarInfoManager
import com.desaysv.psmap.model.impl.DefaultMapCommandImpl
import com.desaysv.psmap.model.impl.IMapCommand
import com.desaysv.psmap.model.utils.NaviFocusManager
import com.desaysv.psmap.model.voice.tts.T1NAutoPlayTtsService
import com.dji.navigation.AdasSupportBusiness
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ProvidesHiltModule {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class DefaultMapCommand

    @Singleton
    @Provides
    fun provideICarInfoProxy(
        application: Application,
        engineerBusiness: EngineerBusiness,
        sharePreferenceFactory: SharePreferenceFactory
    ): ICarInfoProxy {
        return if (CommonUtils.isVehicle()) {
            CheryPlatformCarInfoManager(application, engineerBusiness)
        } else {
            SimulationCarInfoManager(engineerBusiness, sharePreferenceFactory)
        }
    }

    @Singleton
    @Provides
    fun provideISettingComponent(
        settingComponent: SettingComponent,
        application: Application,
        sharePreferenceFactory: SharePreferenceFactory
    ): ISettingComponent {
        return SettingComponentBusiness(settingComponent, application, sharePreferenceFactory)
    }

    @Singleton
    @Provides
    fun bindITTSPlayer(
        @ApplicationContext context: Context,
        speechSynthesizeBusiness: SpeechSynthesizeBusiness,
        settingComponent: ISettingComponent
    ): IAutoPlayer {
        return T1NAutoPlayTtsService(context, speechSynthesizeBusiness, settingComponent)
    }

    @Singleton
    @DefaultMapCommand
    @Provides
    fun bindDefaultMapCommandImpl(
        @ApplicationContext context: Context,
        mapBusiness: MapBusiness,
        searchBusiness: SearchBusiness,
        naviRepository: INaviRepository,
        mRouteRequestController: RouteRequestController,
        skyBoxBusiness: SkyBoxBusiness,
        userBusiness: UserBusiness,
        mLocationBusiness: LocationBusiness,
        mTtsPlayBusiness: TtsPlayBusiness,
        mNaviBusiness: NaviBusiness,
        mNavigationSettingBusiness: NavigationSettingBusiness,
        mRouteBusiness: RouteBusiness,
        settingAccountBusiness: SettingAccountBusiness,
        application: Application,
        iCarInfoProxy: ICarInfoProxy,
        activationMapBusiness: ActivationMapBusiness,
        customTeamBusiness: CustomTeamBusiness
    ): IMapCommand {
        return DefaultMapCommandImpl(
            context,
            mapBusiness,
            searchBusiness,
            naviRepository,
            mRouteRequestController,
            skyBoxBusiness,
            userBusiness,
            mLocationBusiness,
            mTtsPlayBusiness,
            mNaviBusiness,
            mNavigationSettingBusiness,
            mRouteBusiness,
            settingAccountBusiness,
            application,
            iCarInfoProxy,
            activationMapBusiness,
            customTeamBusiness
        )
    }

    @Singleton
    @Provides
    fun provideIModelBusinessProxy(
        ttsPlayBusiness: TtsPlayBusiness,
        outputDataBusiness: OutputDataBusiness,
        evManager: EVManager,
        simplePOIController: SimplePOIController,
        iCarInfoProxy: ICarInfoProxy,
        adasSupportBusiness: AdasSupportBusiness
    ): IModelBusinessProxy {
        return ModelBusinessManager(
            ttsPlayBusiness,
            outputDataBusiness,
            evManager,
            simplePOIController,
            iCarInfoProxy,
            adasSupportBusiness
        )
    }

    @Singleton
    @Provides
    fun provideAudioFocusManager(
        application: Application
    ): AudioFocusManager {
        return NaviFocusManager(application)
    }

    @Singleton
    @Provides
    fun provideAhaTripImpl(
        application: Application,
        iCarInfoProxy: ICarInfoProxy,
        locationBusiness: LocationBusiness,
        mapDataBusiness: MapDataBusiness,
        ahaTripBusiness: AhaTripBusiness,
        settingAccountBusiness: SettingAccountBusiness,
        netWorkManager: NetWorkManager,
        gson: Gson
    ): AhaTripImpl {
        return AhaTripManager(application, iCarInfoProxy, locationBusiness, mapDataBusiness, ahaTripBusiness, settingAccountBusiness, netWorkManager, gson)
    }
}