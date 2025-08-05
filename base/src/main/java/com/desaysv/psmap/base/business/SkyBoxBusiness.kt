package com.desaysv.psmap.base.business

import android.app.Application
import android.content.res.Configuration
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.auto.skin.SkinManager
import com.autonavi.gbl.map.UtilDayStatusNotify
import com.autonavi.gbl.map.model.DAY_STATUS
import com.autonavi.gbl.map.model.MapModelDtoConstants
import com.autonavi.gbl.map.model.MapSkyboxParam
import com.autonavi.gbl.map.observer.IDayStatusListener
import com.autonavi.gbl.util.model.BinaryStream
import com.autosdk.bussiness.common.utils.AssetUtils
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.map.MapController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.bussiness.widget.setting.SettingConst
import com.autosdk.common.utils.DayStatusSystemUtil
import com.desaysv.psmap.base.BuildConfig
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.base.utils.BaseConstant
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author Zhu ZhiPeng
 * @date 2024年1月12日
 * @desc 日月模式管理类
 */
@Singleton
class SkyBoxBusiness @Inject constructor(
    private val dayStatusSystemUtil: DayStatusSystemUtil,
    private val mapController: MapController,
    private val settingComponent: ISettingComponent,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val layerController: LayerController,
    private val skinManager: SkinManager,
    private val application: Application
) : IDayStatusListener {

    private var lightsStatus: Boolean = false
    private var channelStatus: Boolean = false

    @DAY_STATUS.DAY_STATUS1
    private var curMode: Int = DAY_STATUS.AUTO_UNKNOWN_ERROR

    @DAY_STATUS.DAY_STATUS1
    private var autoMode: Int = DAY_STATUS.AUTO_UNKNOWN_ERROR

    private val themeChange: MutableLiveData<Boolean> = MutableLiveData(getFirstDayNight())

    private var mDayNightListener: (() -> Unit)? = null

    private val utilDayStatusNotify: UtilDayStatusNotify by lazy {
        UtilDayStatusNotify.getInstance()
    }

    private fun getFirstDayNight(): Boolean {
        if (BuildConfig.dayNightBySystemUI) {
            val currentNightMode = getSystemUIMode()
            Timber.i("currentNightMode = $currentNightMode")
            NightModeGlobal.setNightMode(currentNightMode == Configuration.UI_MODE_NIGHT_YES)
        }
        return NightModeGlobal.isNightMode()
    }

    fun init() {
        Timber.i("init")
        if (!BuildConfig.dayNightBySystemUI) {
            utilDayStatusNotify.addListener(this)
            utilDayStatusNotify.setSystemUtil(dayStatusSystemUtil)
            utilDayStatusNotify.start()
        }
    }

    fun unInit() {
        if (!BuildConfig.dayNightBySystemUI) {
            utilDayStatusNotify.stop()
            utilDayStatusNotify.removeListener(this)
        }
    }

    fun updateView(view: View, isRecursion: Boolean = false) {
        skinManager.updateView(view, isRecursion)
    }

    fun updateView(view: View, isNight: Boolean, isRecursion: Boolean = false) {
        skinManager.updateView(view, isNight, isRecursion)
    }

    /**
     * 设置车灯状态
     */
    suspend fun setLights(isOpen: Boolean) {
        Timber.i("setLights $isOpen")
        if (settingComponent.getConfigKeyDayNightMode() != SettingConst.MODE_DEFAULT || BuildConfig.dayNightBySystemUI)//自动模式才适配
            return
        lightsStatus = isOpen
        if (isOpen && !NightModeGlobal.isNightMode()) {
            Timber.i("setLights DAY_STATUS_NIGHT")
            updateDayNight(DAY_STATUS.DAY_STATUS_NIGHT)
        } else if (!isOpen) {
            Timber.i("setLights autoMode")
            updateDayNight(autoMode)
        }

    }

    /**
     * 设置隧道状态
     */
    suspend fun setChannel(isInChannel: Boolean) {
        Timber.i("setChannel $isInChannel")
        if (settingComponent.getConfigKeyDayNightMode() != SettingConst.MODE_DEFAULT || BuildConfig.dayNightBySystemUI)//自动模式才适配
            return
        channelStatus = isInChannel
        if (isInChannel && !NightModeGlobal.isNightMode()) {
            Timber.i("setChannel DAY_STATUS_NIGHT")
            updateDayNight(DAY_STATUS.DAY_STATUS_NIGHT)
        } else if (!isInChannel) {
            Timber.i("setChannel autoMode")
            updateDayNight(autoMode)

        }
    }

    fun themeChange(): LiveData<Boolean> {
        return themeChange
    }

    fun setThemeChange(isNight: Boolean) {
        themeChange.postValue(isNight)
    }

    /**
     * 登录账号后需要用到
     */
    fun refreshDayNightStatus() {
        val status = if (BuildConfig.dayNightBySystemUI) {
            if (getSystemUIMode() == Configuration.UI_MODE_NIGHT_YES) SettingConst.MODE_NIGHT else SettingConst.MODE_DAY
        } else settingComponent.getConfigKeyDayNightMode()
        Timber.i("refreshDayNightStatus status $status")
        when (status) {
            SettingConst.MODE_DAY -> updateDayNightStatus(DAY_NIGHT_STATUS.DAY)
            SettingConst.MODE_NIGHT -> updateDayNightStatus(DAY_NIGHT_STATUS.NIGHT)
            SettingConst.MODE_DEFAULT -> updateDayNightStatus(DAY_NIGHT_STATUS.AUTO)
        }
    }

    /**
     * 更新日夜模式
     */
    fun updateDayNightStatus(status: DAY_NIGHT_STATUS) {
        Timber.i("updateDayNightStatus $status")
        Timber.i("Dispatchers.Default launch status=$status")
        when (status) {
            DAY_NIGHT_STATUS.DAY -> {
                settingComponent.setConfigKeyDayNightMode(SettingConst.MODE_DAY)
                updateDayNight(DAY_STATUS.DAY_STATUS_DAY)
            }

            DAY_NIGHT_STATUS.NIGHT -> {
                settingComponent.setConfigKeyDayNightMode(SettingConst.MODE_NIGHT)
                updateDayNight(DAY_STATUS.DAY_STATUS_NIGHT)
            }

            DAY_NIGHT_STATUS.AUTO -> {
                settingComponent.setConfigKeyDayNightMode(SettingConst.MODE_DEFAULT)
                updateDayNight(autoMode)
            }
        }
    }

    override fun onDayStatus(@DAY_STATUS.DAY_STATUS1 mode: Int): Boolean {
        Timber.i("onDayStatus mode = $mode,configKeyDayNightMode = ${settingComponent.getConfigKeyDayNightMode()}")
        autoMode = mode
        if (channelStatus || lightsStatus) {
            Timber.i("onDayStatus channelStatus=$channelStatus lightsStatus=$lightsStatus")
            return true
        }
        return updateDayNight(mode)
    }

    /**
     * 获取系统日夜模式状态
     */
    private fun getSystemUIMode(): Int {
        val currentNightMode: Int = application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        Timber.i("systemIsNight currentNightMode = $currentNightMode")
        return currentNightMode
    }

    private fun updateDayNight(@DAY_STATUS.DAY_STATUS1 mode: Int): Boolean {
        Timber.i("updateDayNight mode = $mode,configKeyDayNightMode = ${settingComponent.getConfigKeyDayNightMode()}")
        mDayNightListener?.invoke()
        if (BuildConfig.dayNightBySystemUI) {
            when (mode) {
                DAY_STATUS.DAY_STATUS_DAY -> {
                    NightModeGlobal.setNightMode(false)
                }

                DAY_STATUS.DAY_STATUS_NIGHT -> {
                    NightModeGlobal.setNightMode(true)
                }

                else -> {
                    NightModeGlobal.setNightMode(getSystemUIMode() == Configuration.UI_MODE_NIGHT_YES)
                }
            }
        } else {
            when (settingComponent.getConfigKeyDayNightMode()) {
                SettingConst.MODE_DEFAULT -> {
                    NightModeGlobal.setNightMode(UtilDayStatusNotify.isNormalNight(mode))
                }

                SettingConst.MODE_DAY -> {
                    NightModeGlobal.setNightMode(false)
                }

                SettingConst.MODE_NIGHT -> {
                    NightModeGlobal.setNightMode(true)
                }
            }
        }
        setMapStyle(NightModeGlobal.isNightMode())
        layerController.updateStyle(SurfaceViewID.SURFACE_VIEW_ID_MAIN, NightModeGlobal.isNightMode())
        mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN).resetTickCount(1)
        if (BaseConstant.MULTI_MAP_VIEW) {
            layerController.updateStyle(SurfaceViewID.SURFACE_VIEW_ID_EX1, NightModeGlobal.isNightMode())
            mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_EX1).resetTickCount(1)
        }
        themeChange.postValue(NightModeGlobal.isNightMode())
        return updateSkyBoxResource(mode)
    }

    private fun setMapStyle(isNight: Boolean) {
        mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_MAIN)?.operatorStyle?.mapStyle?.let { mapstyle ->
            Timber.i("Main setMapStyle ${mapstyle.time}  mapstyle.state = ${mapstyle.state}")
            when (mapstyle.state) {
                MapModelDtoConstants.MAP_MODE_SUBSTATE_PREVIEW_CAR -> {
                    mapController.setMapStyle(
                        SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                        isNight,
                        MapController.EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_PLAN
                    )
                }

                MapModelDtoConstants.MAP_MODE_SUBSTATE_NAVI_CAR -> {
                    mapController.setMapStyle(
                        SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                        isNight,
                        MapController.EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NAVI
                    )
                }

                else -> {
                    mapController.setMapStyle(
                        SurfaceViewID.SURFACE_VIEW_ID_MAIN,
                        isNight,
                        MapController.EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NORMAL
                    )
                }
            }

        }

        if (BaseConstant.MULTI_MAP_VIEW) {
            mapController.getMapView(SurfaceViewID.SURFACE_VIEW_ID_EX1)?.operatorStyle?.mapStyle?.let { mapstyle ->
                Timber.i("Ex1 setMapStyle ${mapstyle.time}  mapstyle.state = ${mapstyle.state}")
                when (mapstyle.state) {
                    MapModelDtoConstants.MAP_MODE_SUBSTATE_PREVIEW_CAR -> {
                        mapController.setMapStyle(
                            SurfaceViewID.SURFACE_VIEW_ID_EX1,
                            isNight,
                            MapController.EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_PLAN
                        )
                    }

                    MapModelDtoConstants.MAP_MODE_SUBSTATE_NAVI_CAR -> {
                        mapController.setMapStyle(
                            SurfaceViewID.SURFACE_VIEW_ID_EX1,
                            isNight,
                            MapController.EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NAVI
                        )
                    }

                    else -> {
                        mapController.setMapStyle(
                            SurfaceViewID.SURFACE_VIEW_ID_EX1,
                            isNight,
                            MapController.EMapStyleStateType.E_MAP_STYLE_STATE_TYPE_NORMAL
                        )
                    }
                }
            }
        }
    }

    private fun updateSkyBoxResource(@DAY_STATUS.DAY_STATUS1 mode: Int): Boolean {
        //更新天空盒子
        if (curMode == mode) {
            Timber.d("updateSkyBoxResource same SkyBox")
            return true
        }
        curMode = mode
        val is3DRes = false
        var assetName3D = "skybox_day.dat"
        var assetName2D = "skybox_day.data"
        when (mode) {
            DAY_STATUS.DAY_STATUS_DAWN_1 -> {
                assetName3D = "skybox_morning1.dat"
                assetName2D = "skybox_morning1.data"
            }

            DAY_STATUS.DAY_STATUS_DAWN_2 -> {
                assetName3D = "skybox_morning2.dat"
                assetName2D = "skybox_morning2.data"
            }

            DAY_STATUS.DAY_STATUS_DAY -> {
                assetName3D = "skybox_day.dat"
                assetName2D = "skybox_day.data"
            }

            DAY_STATUS.DAY_STATUS_DUSK_1 -> {
                assetName3D = "skybox_dusk1.dat"
                assetName2D = "skybox_dusk1.data"
            }

            DAY_STATUS.DAY_STATUS_DUSK_2 -> {
                assetName3D = "skybox_dusk2.dat"
                assetName2D = "skybox_dusk2.data"
            }

            DAY_STATUS.DAY_STATUS_NIGHT -> {
                assetName3D = "skybox_night.dat"
                assetName2D = "skybox_night.data"
            }

            DAY_STATUS.AUTO_UNKNOWN_ERROR -> {
                assetName3D = "skybox_day.dat"
                assetName2D = "skybox_day.data"
            }
        }


        val resourceName = if (is3DRes) assetName3D else assetName2D
        if (TextUtils.isEmpty(resourceName)) {
            Timber.w("updateSkyBoxResource Fail: resourceName is empty")
            return false
        }

        val path = "blRes/MapAsset/$resourceName"
        val skyboxData: ByteArray? = AssetUtils.getAssetFileContent(application, path)

        if (skyboxData == null || skyboxData.isEmpty()) {
            return false
        }
        val mapSkyboxParam = MapSkyboxParam()
        mapSkyboxParam.isOn = true // 是否开启skybox
        mapSkyboxParam.is3DRes = is3DRes // 是否使用3D资源 is3DRes需要传false，【>=550版本资源层面不支持 3D资源】
        mapSkyboxParam.DataBuff = BinaryStream(skyboxData)
        val result =
            MapController.getInstance().setBaseMapSkyBoxVisible(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mapSkyboxParam)
        MapController.getInstance().refresh(SurfaceViewID.SURFACE_VIEW_ID_MAIN)
        if (BaseConstant.MULTI_MAP_VIEW) {
            val resultEx1 =
                MapController.getInstance().setBaseMapSkyBoxVisible(SurfaceViewID.SURFACE_VIEW_ID_EX1, mapSkyboxParam)
            MapController.getInstance().refresh(SurfaceViewID.SURFACE_VIEW_ID_EX1)
        }
        Timber.i("updateSkyBoxResource result: $result")
        return result
    }

    fun setDayNightChangeListener(listener: () -> Unit) {
        mDayNightListener = listener
    }

    enum class DAY_NIGHT_STATUS {
        DAY, NIGHT, AUTO
    }

}