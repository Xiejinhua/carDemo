package com.desaysv.psmap.ui.settings.offlinedata

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.business.OfflineDataBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 数据罐装-U盘下载离线地图ViewModel
 */
@HiltViewModel
class UsbOfflineMapViewModel @Inject constructor(
    private val offlineDataBusiness: OfflineDataBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val application: Application
) : ViewModel() {
    val enough = offlineDataBusiness.enough //空间提示
    val showToast = offlineDataBusiness.showToast //toast提示
    val usbMount = offlineDataBusiness.usbMount //U盘挂载状态 true.挂载
    var usbMapDataObserverImp = offlineDataBusiness.usbMapDataObserverImp //离线地图监听Observer
    val checkDataInDiskStateStr = offlineDataBusiness.checkDataInDiskStateStr //U盘数据状态说明
    val usbRequestDataListCheckSuccess = offlineDataBusiness.usbRequestDataListCheckSuccess //U盘模式--检测数据列表回调
    val loading = offlineDataBusiness.usbDataLoading //加载状态
    val hasData = offlineDataBusiness.hasUsbDataData //是否有罐装数据
    val hasUsbBaseData = offlineDataBusiness.hasUsbBaseData //是否有基础包罐装数据
    val usbDirectDataNum = offlineDataBusiness.usbDirectDataNum //直辖市罐装数据个数
    val usbProvDataNum = offlineDataBusiness.usbProvDataNum //省份罐装数据个数
    val usbCityDataNum = offlineDataBusiness.usbCityDataNum //城市罐装数据个数
    val usbSpecialDataNum = offlineDataBusiness.usbSpecialDataNum //特别行政区罐装数据个数
    val usbDataProgress = offlineDataBusiness.usbDataProgress //U盘更新进度
    val usbDataError = offlineDataBusiness.usbDataError //U盘中离线数据是否有问题
    val usbDataDownLoaded = offlineDataBusiness.usbDataDownLoaded //U盘中离线数据是否已经全部罐装

    val isNight = skyBoxBusiness.themeChange()
    val checkDataInDiskState = MutableLiveData<Int>() //U盘数据状态

    val usbDirectDataStr = usbDirectDataNum.switchMap { data -> // 直辖市个数
        MutableLiveData<String>().apply {
            value = String.format(application.getString(R.string.sv_mapdata_usb_direct), data)
        }
    }
    val usbProvDataStr = usbProvDataNum.switchMap { data -> // 省份个数
        MutableLiveData<String>().apply {
            value = String.format(application.getString(R.string.sv_mapdata_usb_province_info), data)
        }
    }
    val usbCityDataStr = usbCityDataNum.switchMap { data -> // 城市个数
        MutableLiveData<String>().apply {
            value = String.format(application.getString(R.string.sv_mapdata_usb_city_num), data)
        }
    }
    val usbSpecialDataStr = usbSpecialDataNum.switchMap { data -> // 特别行政区个数
        MutableLiveData<String>().apply {
            value = String.format(application.getString(R.string.sv_mapdata_usb_special), data)
        }
    }
    val usbDataProgressStr = usbDataProgress.switchMap { data -> // 下载进度
        MutableLiveData<String>().apply {
            value = "$data%"
        }
    }

    //数据罐装-U盘数据状态判断
    fun setCheckDataInDiskState(state: Int) {
        offlineDataBusiness.setCheckDataInDiskState(state)
    }

    //停止U盘下载
    fun stopUsbDownload() {
        offlineDataBusiness.stopUsbDownload()
    }

    //退出罐装界面，数据状态恢复默认值
    fun defaultUsbState() {
        offlineDataBusiness.defaultUsbState()
    }
}