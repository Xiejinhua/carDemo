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
 * @description 离线地图--已下载城市界面ViewModel
 */
@HiltViewModel
class OfflineMapDownloadedViewModel @Inject constructor(
    private val offlineDataBusiness: OfflineDataBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
    val updateOperated = offlineDataBusiness.updateOperated //更新下载状态
    val updateAllData = offlineDataBusiness.updateAllData //更新数据
    val updatePercent = offlineDataBusiness.updatePercent //更新下载Percent数据
    val onHiddenChanged = offlineDataBusiness.onHiddenChanged //界面Hidden监听

    val isNight = skyBoxBusiness.themeChange()
    val isLoading = MutableLiveData(true) //是否加载中
    val hasDownloadedData = MutableLiveData<Boolean>() //判断是否有已下载数据
    val hasUpdateData = MutableLiveData<Boolean>() //判断是否有更新数据
    val updateTip = MutableLiveData("") //更新提示 比如：更新全部1个城市 2.8M
    val downloadedTip = MutableLiveData("") //已下载提示 比如：以下2个城市下载成功

    //根据adCode下载城市数据
    fun dealListDownLoad(adCode: Int) {
        offlineDataBusiness.dealListDownLoad(adCode)
    }

    //开始下载，继续下载等操作
    fun downLoadClick(cityAdCode: ArrayList<Int>, operationType: Int) {
        offlineDataBusiness.downLoadClick(cityAdCode, operationType)
    }

    //重新load数据
    fun converNewDownLoadData(arCodes: ArrayList<Int>): ArrayList<DownloadCityDataBean>? {
        return offlineDataBusiness.converNewDownLoadData(arCodes)
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

    //更新对话框
    fun showUpdateDialog(bundle: Bundle) {
        offlineDataBusiness.showUpdateDialog.postValue(bundle)
    }
}