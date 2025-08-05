package com.desaysv.psmap.ui.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.business.SatelliteInformationBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 通用对话框ViewModel
 */
@HiltViewModel
class GnssInfoDialogViewModel @Inject constructor(
    private val satelliteInformationBusiness: SatelliteInformationBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {

    val isNight = skyBoxBusiness.themeChange()

    val locationOK = satelliteInformationBusiness.locationState

    val satelliteSnrInfo = satelliteInformationBusiness.satelliteSnrInfo

    val gnssCount: LiveData<Int> = satelliteInformationBusiness.satelliteCountInfo.map {
        it[0]
    }
    val gnssAvailableCount: LiveData<Int> = satelliteInformationBusiness.satelliteCountInfo.map {
        it[1]
    }

    val beidouCount: LiveData<Int> = satelliteInformationBusiness.satelliteCountInfo.map {
        it[2]
    }

    val gpsCount: LiveData<Int> = satelliteInformationBusiness.satelliteCountInfo.map {
        it[3]
    }

    val glonassCount: LiveData<Int> = satelliteInformationBusiness.satelliteCountInfo.map {
        it[4]
    }

    val ufoCount: LiveData<Int> = satelliteInformationBusiness.satelliteCountInfo.map {
        it[5]
    }

    val dateTime: LiveData<String> = satelliteInformationBusiness.locationInfo.map {
        it[0]
    }

    val speed: LiveData<String> = satelliteInformationBusiness.locationInfo.map {
        it[1]
    }

    val bearing: LiveData<String> = satelliteInformationBusiness.locationInfo.map {
        it[2]
    }

    val address: LiveData<String> = satelliteInformationBusiness.address

    val gnssLocationInfo = satelliteInformationBusiness.satelliteLocationInfo

    init {
        Timber.i("init")
        satelliteInformationBusiness.init()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("onCleared")
        satelliteInformationBusiness.release()
    }

}