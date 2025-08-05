package com.desaysv.psmap.base.handle

import android.location.Location
import android.text.TextUtils
import com.autonavi.gbl.aosclient.observer.ICallBackWsTserviceInternalLinkAutoReport
import com.autosdk.bussiness.account.bean.LinkCarLocation
import com.autosdk.bussiness.common.GeoPoint
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.ISettingComponent
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 30秒上报停车位置
 */
@Singleton
class ReportCarLocHandler @Inject constructor(
    private val mLocationBusiness: LocationBusiness,
    private val pushMessageBusiness: PushMessageBusiness,
    private val settingComponent: ISettingComponent,
    private val sharePreferenceFactory: SharePreferenceFactory
) {
    companion object {
        private const val DELAYED_TIME = (30 * 1000).toLong()
    }

    private var timer: Timer? = null
    private var task: TimerTask? = null
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)

    /**
     * 开始30s一次上报自己位置
     */
    fun doStart() {
        timer = Timer()
        task = object : TimerTask() {
            override fun run() {
                startReportCarLocation(0)
            }
        }
        timer!!.schedule(task, 0, DELAYED_TIME)
    }

    /**
     * 取消定时器
     */
    fun doStop() {
        if (task != null) {
            task!!.cancel()
        }
        if (timer != null) {
            timer = null
            task = null
        }
        iCallBackWsTServiceInternalLinkAutoReport = null
    }

    /**
     * 上报停车位置
     * @param parkStatus 1停车，0非停车
     */
    fun startReportCarLocation(parkStatus: Int) {
        val location: Location = mLocationBusiness.getLastLocation()
        val linkCarLocation = LinkCarLocation()
        linkCarLocation.carLoc = GeoPoint(location.longitude, location.latitude)
        //车牌信息通过用户模块获取
        val configKeyPlateNumber = getConfigKeyPlateNumber()
        Timber.d("====link configKeyPlateNumber = %s", configKeyPlateNumber)
        if (!TextUtils.isEmpty(configKeyPlateNumber)) {
            linkCarLocation.plateNum = configKeyPlateNumber
            linkCarLocation.parkStatus = parkStatus
            pushMessageBusiness.startReportCarLocation(linkCarLocation, iCallBackWsTServiceInternalLinkAutoReport)
            //                mHandler.sendEmptyMessageDelayed(TASK_TIMER, DELAYED_TIME);
        } else {
            //车牌为空不上传，继续轮询获取车牌
//                mHandler.sendEmptyMessageDelayed(TASK_TIMER, DELAYED_TIME);
        }
    }

    private var iCallBackWsTServiceInternalLinkAutoReport: ICallBackWsTserviceInternalLinkAutoReport? =
        ICallBackWsTserviceInternalLinkAutoReport { response -> //停车上报回调
            Timber.d("====link onRecvAck GWsTServiceInternalLinkAutoReportResponseParam message = %s, result = %s", response.message, response.result)
        }

    private fun getConfigKeyPlateNumber(): String {
        return settingComponent.getLicensePlateNumber()
    }
}
