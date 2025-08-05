package com.desaysv.psmap.ui.settings.offlinedata

import android.os.Bundle
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.model.business.OfflineDataBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 离线地图附近城市推荐ViewModel
 */
@HiltViewModel
class NearbyCityViewModel @Inject constructor(private val offlineDataBusiness: OfflineDataBusiness) : ViewModel() {
    val enough = offlineDataBusiness.enough //空间提示
    val toDownloadDialog = offlineDataBusiness.toDownloadDialog //是否用流量下载
    val showToast = offlineDataBusiness.showToast //toast提示
    var mapDataObserverImp = offlineDataBusiness.mapDataObserverImp //离线地图监听Observer
    val showDeleteDialog = offlineDataBusiness.showDeleteDialog //显示删除对话框
    val showPauseAskDialog = offlineDataBusiness.showPauseAskDialog //显示暂停对话框
    val showContinueDialog = offlineDataBusiness.showContinueDialog //显示继续对话框
    val showUpdateDialog = offlineDataBusiness.showUpdateDialog //更新对话框
    val tipsEnterUnzip = offlineDataBusiness.tipsEnterUnzip //解压
    var tipsAdCodes = offlineDataBusiness.tipsAdCodes

    val updateOperated = offlineDataBusiness.updateOperated //更新下载状态
    val updateAllData = offlineDataBusiness.updateAllData //更新数据
    val updatePercent = offlineDataBusiness.updatePercent //更新下载Percent数据

    /**
     * 初始化当前城市数据
     * 分为验证前验证后
     */
    fun onRequestDataListCheckResult() {
        offlineDataBusiness.onRequestDataListCheckResult()
    }

    //开始下载，继续下载等操作
    fun downLoadClick(cityAdCode: ArrayList<Int>, operationType: Int) {
        offlineDataBusiness.downLoadClick(cityAdCode, operationType)
    }

    //刪除操作
    fun dataDeleteOperation(cityAdCode: ArrayList<Int>?) {
        offlineDataBusiness.dataDeleteOperation(cityAdCode)
    }


    //确定可以流量下载
    fun downLoadConfirmFlow(cityAdCode: ArrayList<Int>, operationType: Int) {
        offlineDataBusiness.downLoadConfirmFlow(cityAdCode, operationType)
    }

    //刷新离线数据，更新UI
    fun refreshOfflineData() {
        offlineDataBusiness.getCurrentCityInfo() //获取当前定位城市数据
        offlineDataBusiness.updateNearCityCard() //附近推荐卡片数据及状态
    }

    //根据adCode下载城市数据
    fun dealListDownLoad(adCode: Int) {
        offlineDataBusiness.dealListDownLoad(adCode)
    }

    //获取附近城市AdCode列表
    fun getVecNearAdCodeList(): ArrayList<Int> = offlineDataBusiness.getVecNearAdCodeList()

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