package com.desaysv.psmap.ui.engineer

import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.autonavi.gbl.servicemanager.model.ALCLogLevel
import com.desaysv.psmap.base.business.EngineerBusiness
import com.desaysv.psmap.base.business.LocReplayBusiness
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.utils.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 工程模式主界面ViewModel
 */
@HiltViewModel
class EngineerViewModel @Inject constructor(
    private val locReplayBusiness: LocReplayBusiness,
    private val engineerBusiness: EngineerBusiness,
    private val mLocationBusiness: LocationBusiness,
    @ApplicationContext context: Context,
) : ViewModel() {
    val replayPosTest: LiveData<Boolean> = engineerBusiness.replayPosTest
    val gpsTest: LiveData<Boolean> = engineerBusiness.gpsTest
    val offDRBack: LiveData<Boolean> = engineerBusiness.offDRBack
    val elecContinue: LiveData<Boolean> = engineerBusiness.elecContinue

    val openStartPoint: LiveData<Boolean> = engineerBusiness.openStartPoint

    private val _selectIndex = MutableLiveData(0)
    val selectIndex: LiveData<Int> = _selectIndex

    private val _savePosition = MutableLiveData(false)
    val savePosition: LiveData<Boolean> = _savePosition

    private val _versionInfo = MutableLiveData("Version Info")
    val versionInfo: LiveData<String> = _versionInfo

    private val _savePosLogFlag = MutableLiveData(false)
    val savePosLogFlag: LiveData<Boolean> = _savePosLogFlag
    val blPosLog = engineerBusiness.blPosLog

    private val _saveBlLogLevelFlag = MutableLiveData(false)
    val saveBlLogLevelFlag: LiveData<Boolean> = _saveBlLogLevelFlag
    private val blLogLevel = engineerBusiness.blLogLevel

    private val _netWorkLogFlag = MutableLiveData(true)
    val netWorkLogFlag: LiveData<Boolean> = _netWorkLogFlag

    val blLogLevelName: LiveData<String> = blLogLevel.map { value ->
        when (value) {
            ALCLogLevel.LogLevelError -> "Error"

            ALCLogLevel.LogLevelWarn -> "Warn"

            ALCLogLevel.LogLevelInfo -> "Info"

            ALCLogLevel.LogLevelDebug -> "Debug"

            ALCLogLevel.LogLevelVerbose -> "Verbose"

            else -> "None"
        }
    }

    val sbFpsNormalProgress = MutableLiveData(engineerBusiness.engineerConfig.foreground_MapRenderModeNormal)
    val sbFpsNaviProgress = MutableLiveData(engineerBusiness.engineerConfig.foreground_MapRenderModeNavi)
    val sbFpsAnimationProgress = MutableLiveData(engineerBusiness.engineerConfig.foreground_MapRenderModeAnimation)
    val sbFpsGestureProgress = MutableLiveData(engineerBusiness.engineerConfig.foreground_MapRenderModeGestureAction)

    val sbFpsNormalBackProgress = MutableLiveData(engineerBusiness.engineerConfig.backend_MapRenderModeNormal)
    val sbFpsNaviBackProgress = MutableLiveData(engineerBusiness.engineerConfig.backend_MapRenderModeNavi)
    val sbFpsAnimationBackProgress = MutableLiveData(engineerBusiness.engineerConfig.backend_MapRenderModeAnimation)
    val sbFpsGestureBackProgress = MutableLiveData(engineerBusiness.engineerConfig.backend_MapRenderModeGestureAction)

    private val _uuidCheckFlag = MutableLiveData(engineerBusiness.engineerConfig.openMapTestUuid)
    val uuidCheckFlag: LiveData<Boolean> = _uuidCheckFlag

    private val _testUuid = MutableLiveData(engineerBusiness.engineerConfig.mapTestUuid)
    val testUuid: LiveData<String> = _testUuid

    init {
        _versionInfo.postValue(getVersionInfo(context))
    }

    fun setSelect(index: Int) {
        _selectIndex.postValue(index)
    }

    fun openTestUuid(open: Boolean): Boolean {
        Timber.i("openTestUuid $open")
        return if (TextUtils.isEmpty(testUuid.value)) {
            false
        } else {
            testUuid.value?.let {
                engineerBusiness.openTestUUID(open)
                _uuidCheckFlag.postValue(engineerBusiness.engineerConfig.openMapTestUuid)
            }
            true
        }
    }

    fun saveTestUuid(uuid: String): Boolean {
        if (TextUtils.isEmpty(uuid))
            return false
        if (uuid.trim().length < 15)
            return false
        Timber.i("saveTestUuid $uuid")
        engineerBusiness.setTestUUID(uuid)
        _testUuid.postValue(engineerBusiness.engineerConfig.mapTestUuid)
        return true
    }

    fun setRenderFps(
        isForeground: Boolean, mapRenderModeNormal: Int, mapRenderModeNavi: Int, mapRenderModeAnimation: Int, mapRenderModeGestureAction: Int
    ) {
        engineerBusiness.setRenderFps(isForeground, mapRenderModeNormal, mapRenderModeNavi, mapRenderModeAnimation, mapRenderModeGestureAction)
    }

    fun resetRenderFps() {
        engineerBusiness.resetRenderFps()
        sbFpsNormalProgress.postValue(engineerBusiness.engineerConfig.foreground_MapRenderModeNormal)
        sbFpsNaviProgress.postValue(engineerBusiness.engineerConfig.foreground_MapRenderModeNavi)
        sbFpsAnimationProgress.postValue(engineerBusiness.engineerConfig.foreground_MapRenderModeAnimation)
        sbFpsGestureProgress.postValue(engineerBusiness.engineerConfig.foreground_MapRenderModeGestureAction)

        sbFpsNormalBackProgress.postValue(engineerBusiness.engineerConfig.backend_MapRenderModeNormal)
        sbFpsNaviBackProgress.postValue(engineerBusiness.engineerConfig.backend_MapRenderModeNavi)
        sbFpsAnimationBackProgress.postValue(engineerBusiness.engineerConfig.backend_MapRenderModeAnimation)
        sbFpsGestureBackProgress.postValue(engineerBusiness.engineerConfig.backend_MapRenderModeGestureAction)
    }

    fun switchBlLogLevel() {
        val nextLevel = when (blLogLevel.value) {
            ALCLogLevel.LogLevelError -> {
                ALCLogLevel.LogLevelNone
            }

            ALCLogLevel.LogLevelInfo -> {
                ALCLogLevel.LogLevelError
            }

            ALCLogLevel.LogLevelDebug -> {
                ALCLogLevel.LogLevelInfo
            }

            ALCLogLevel.LogLevelVerbose -> {
                ALCLogLevel.LogLevelDebug
            }

            else -> {
                ALCLogLevel.LogLevelVerbose
            }
        }
        Timber.i("switchBlLogLevel nextLevel=$nextLevel")
        engineerBusiness.setBLLogLevel(nextLevel, saveBlLogLevelFlag.value ?: false)
    }

    fun switchSaveBlLogLevelFlag(save: Boolean) {
        Timber.i("switchSaveBlLogLevelFlag save=$save")
        _saveBlLogLevelFlag.postValue(save)
        blLogLevel.value?.let {
            engineerBusiness.setBLLogLevel(it, save)
        }

    }

    fun switchSaveBlPosLog(save: Boolean) {
        Timber.i("switchSaveBlPosLog save=$save")
        _savePosLogFlag.postValue(save)
        blPosLog.value?.let {
            engineerBusiness.switchPosRecord(it, save)
        }
    }

    fun switchBlPosLog(open: Boolean) {
        Timber.i("switchLlPosLog open=$open")
        engineerBusiness.switchPosRecord(open, savePosLogFlag.value ?: false)
    }

    fun switchNetWorkLog(open: Boolean) {
        Timber.i("switchNetWorkLog open=$open")
        engineerBusiness.switchNetWorkLog(open)
        _netWorkLogFlag.postValue(open)
    }

    private fun getVersionInfo(context: Context): String {
        return "Version information"
            .plus("\n\nAPP version:  ${AppUtils.getAppVersionName(context)}")
            .plus("\nAutoSDK version:  ${engineerBusiness.sdkVersion}")
            .plus("\nAutoSDK Engine version:  ${engineerBusiness.engineVersion}")
            .plus("\nMap Data Engine version:  ${engineerBusiness.mapDataEngineVersion}")
            .plus("\nPlatform Model:  ${Build.MODEL}")
            .plus("\nAndroid version:  ${Build.VERSION.RELEASE}")
            .plus("\nAndroid API:  ${Build.VERSION.SDK_INT}")
    }

    fun openEngineerPosition(open: Boolean) {
        engineerBusiness.openEngineerPosition(open)
    }

    fun savePosition(save: Boolean) {
        _savePosition.postValue(save)
    }

    fun setStartCarPosition(sLon: String, sLat: String): Boolean {
        return engineerBusiness.saveStartCarPosition(sLon, sLat, savePosition.value ?: false)
    }

    fun openGpsTest(open: Boolean) {
        Timber.i("openGpsTest open=$open")
        engineerBusiness.openGpsTest(open)
    }

    fun switchReplayPosTest(open: Boolean) {
        Timber.i("switchReplayPosTest open=$open")
        if (open) {
            viewModelScope.launch {
                locReplayBusiness.init()
                engineerBusiness.offDRBack(true)
                engineerBusiness.switchReplayPosTest(true)
            }
        } else {
            locReplayBusiness.unInit()
            engineerBusiness.offDRBack(false)
            engineerBusiness.switchReplayPosTest(false)
        }
        mLocationBusiness.pauseGPSInput(open)
    }

    fun offDRBack(isOff: Boolean) {
        engineerBusiness.offDRBack(isOff)
    }

    fun resetMap() {
        viewModelScope.launch {
            engineerBusiness.resetMap()
        }
    }

    fun setElecContinue(open: Boolean) {
        Timber.i("switchReplayPosTest open=$open")
        engineerBusiness.setElecContinue(open)
    }

}