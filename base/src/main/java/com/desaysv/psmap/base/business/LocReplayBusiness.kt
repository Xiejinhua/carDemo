package com.desaysv.psmap.base.business

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.autosdk.bussiness.common.utils.AssetUtils
import com.autosdk.bussiness.location.LocationReplayController
import com.autosdk.common.AutoConstant
import com.autosdk.common.utils.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocReplayBusiness @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationReplayController: LocationReplayController
) {
    private val _locFileName = MutableLiveData("选择定位文件")
    val locFileName: LiveData<String> = _locFileName

    suspend fun init() {
        Timber.i("init")
        locationReplayController.initReplayService()
        withContext(Dispatchers.IO) {
            // 车道级导航回放文件
            AssetUtils.copyAssetsFolder(
                context,
                "loc_test/loc_replay/",
                AutoConstant.GPS_LANELOC_FOLDER,
                true
            )
        }
    }

    fun unInit() {
        locationReplayController.closeService()
        Timber.i("unInit")
    }

    var gpsLocFileIndex = -1
    var locFilePath: String? = null
    fun switchReplayLocFile() {
        val fileList = FileUtils.listSubFiles(AutoConstant.GPS_LANELOC_FOLDER)
        if (fileList == null || fileList.size == 0) {
            _locFileName.postValue("不存在定位文件")
            return
        }
        gpsLocFileIndex = (gpsLocFileIndex + 1) % fileList.size
        Timber.i("gpsLocFileIndex $gpsLocFileIndex")
        fileList[gpsLocFileIndex]?.let {
            locFilePath = it.absolutePath
            _locFileName.postValue(it.name)
            Timber.i("locFilePath $locFilePath")
            Timber.i("_locFileName ${it.name}")
        }

    }

    /**
     * 开始回放
     * @param locPath   需要回放的日志路径
     * @param replaySpeedTime 回放的速度
     */
    fun startReplay() {
        Timber.i("startReplay $locFilePath")
        locFilePath?.let {
            locationReplayController.startReplay(it, 8000)
        }
    }

    /**
     * 暂停回放
     */
    fun pauseReplay() {
        Timber.i("pauseReplay")
        locationReplayController.pauseReplay()
    }

    /**
     * 恢复回放
     */
    fun resumeReplay() {
        Timber.i("resumeReplay")
        locationReplayController.resumeReplay()
    }

    /**
     * 停止回放
     */
    fun stopReplay() {
        Timber.i("stopReplay")
        locationReplayController.stopReplay()
    }
}