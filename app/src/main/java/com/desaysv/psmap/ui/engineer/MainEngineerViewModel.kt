package com.desaysv.psmap.ui.engineer

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.base.business.EngineerBusiness
import com.desaysv.psmap.base.business.LocReplayBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 工程模式主界面ViewModel
 */
@HiltViewModel
class MainEngineerViewModel @Inject constructor(
    private val locReplayBusiness: LocReplayBusiness,
    private val engineerBusiness: EngineerBusiness
) : ViewModel() {
    val locFileName = locReplayBusiness.locFileName

    val replayPosTest: LiveData<Boolean> = engineerBusiness.replayPosTest
    val gpsTest: LiveData<Boolean> = engineerBusiness.gpsTest

    val debugInfo = MutableLiveData("GPS info")

    val drInfo = MutableLiveData("dr info")

    private val _showDebugInfo = MutableLiveData(false)
    val showDebugInfo: LiveData<Boolean> = _showDebugInfo

    private val debugInfoTimer = object : CountDownTimer(1000 * 3600 * 24, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            Timber.i("onTick $millisUntilFinished")
            debugInfo.postValue(engineerBusiness.getDebugInfo())
            drInfo.postValue(engineerBusiness.sensorInfo())
        }

        override fun onFinish() {
            Timber.i("onFinish")
        }
    }

    fun showDebugInfo(show: Boolean) {
        Timber.i("showDebugInfo $show")
        _showDebugInfo.postValue(show)
        if (show) debugInfoTimer.start() else debugInfoTimer.cancel()
    }

    fun switchReplayLocFile() {
        locReplayBusiness.switchReplayLocFile()
    }

    fun startReplay() {
        locReplayBusiness.startReplay()
    }

    fun pauseReplay() {
        locReplayBusiness.pauseReplay()
    }

    fun resumeReplay() {
        locReplayBusiness.resumeReplay()
    }

    fun stopReplay() {
        locReplayBusiness.stopReplay()
    }

}