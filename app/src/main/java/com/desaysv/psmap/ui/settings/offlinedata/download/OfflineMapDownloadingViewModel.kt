package com.desaysv.psmap.ui.settings.offlinedata.download

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.bean.DownloadCityDataBean
import com.desaysv.psmap.model.business.OfflineDataBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 离线地图--下载中城市界面ViewModel
 */
@HiltViewModel
class OfflineMapDownloadingViewModel @Inject constructor(
    private val offlineDataBusiness: OfflineDataBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
    val updateOperated = offlineDataBusiness.updateOperated //更新下载状态
    val updateAllData = offlineDataBusiness.updateAllData //更新数据
    val updatePercent = offlineDataBusiness.updatePercent //更新下载Percent数据
    val onHiddenChanged = offlineDataBusiness.onHiddenChanged //界面Hidden监听

    val isNight = skyBoxBusiness.themeChange()
    val isLoading = MutableLiveData(true) //是否加载中
    val hasData = MutableLiveData<Boolean>() //判断是否有数据
    val isAllPause = MutableLiveData(true) //判断是否全部暂停
    val isAllStart = MutableLiveData(true) //判断是否全部下载

    //暂停所有下载任务
    fun pauseAllTask() {
        offlineDataBusiness.pauseAllTask()
    }

    //开始所有暂停任务
    fun startAllTask() {
        offlineDataBusiness.startAllTask()
    }

    //重新load数据
    fun converNewDownLoadData(arCodes: ArrayList<Int>): ArrayList<DownloadCityDataBean>? {
        return offlineDataBusiness.converNewDownLoadData(arCodes)
    }

    //根据adCode下载城市数据
    fun dealListDownLoad(adCode: Int) {
        offlineDataBusiness.dealListDownLoad(adCode)
    }

    //显示删除对话框
    fun showDeleteDialog(bundle: Bundle) {
        offlineDataBusiness.showDeleteDialog.postValue(bundle)
    }

    //显示暂停对话框
    fun showPauseAskDialog(bundle: Bundle) {
        offlineDataBusiness.showPauseAskDialog.postValue(bundle)
    }

    //显示继续对话框
    fun showContinueDialog(bundle: Bundle) {
        offlineDataBusiness.showContinueDialog.postValue(bundle)
    }
}