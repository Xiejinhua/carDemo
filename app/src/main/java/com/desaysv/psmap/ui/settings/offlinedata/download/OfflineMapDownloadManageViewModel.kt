package com.desaysv.psmap.ui.settings.offlinedata.download

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.business.OfflineDataBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 离线地图--下载管理界面ViewModel
 */
@HiltViewModel
class OfflineMapDownloadManageViewModel @Inject constructor(private val offlineDataBusiness: OfflineDataBusiness,
                                                            private val skyBoxBusiness: SkyBoxBusiness) : ViewModel() {
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
    val downLoadingSize = offlineDataBusiness.downLoadingSize //正在下载数量

    val isNight = skyBoxBusiness.themeChange()

    val tabSelect = MutableLiveData(0) //0：正在下载 1：已下载

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

    //界面Hidden通知
    fun onHiddenChanged(hidden: Boolean) {
        offlineDataBusiness.onHiddenChanged.postValue(hidden)
    }

    //设置下载管理显隐
    fun setHeadViewLoadManagerVisible() {
        offlineDataBusiness.setHeadViewLoadManagerVisible()
    }
}