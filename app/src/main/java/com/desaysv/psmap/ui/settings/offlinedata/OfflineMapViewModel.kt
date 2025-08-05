package com.desaysv.psmap.ui.settings.offlinedata

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autonavi.gbl.data.model.CityDownLoadItem
import com.autonavi.gbl.data.model.CityItemInfo
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.bean.ProvinceDataBean
import com.desaysv.psmap.model.business.OfflineDataBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 离线地图主界面ViewModel
 */
@HiltViewModel
class OfflineMapViewModel @Inject constructor(
    private val offlineDataBusiness: OfflineDataBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
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

    //当前定位城市MutableLiveData数据
    val currentCityItemInfo = offlineDataBusiness.currentCityItemInfo
    val currentCityDownLoadItem = offlineDataBusiness.currentCityDownLoadItem
    val workingQueueAdCodeList = offlineDataBusiness.workingQueueAdCodeList

    //基础包MutableLiveData数据
    val baseDownLoadItem = offlineDataBusiness.baseDownLoadItem

    //附近城市推荐MutableLiveData数据
    val nearCityNumber = offlineDataBusiness.nearCityNumber //附近城市个数
    val nearCitySize = offlineDataBusiness.nearCitySize //附近城市总大小
    val nearCityDownLoadState = offlineDataBusiness.nearCityDownLoadState //附近城市下载状态

    val showViewLoadManager = offlineDataBusiness.showViewLoadManager //下载管理一栏是否显示
    val showSearch = MutableLiveData(false) //是否显示搜索列表相关布局
    val refreshSearchList = MutableLiveData<Boolean>()//刷新搜索列表
    val searchButtonType = offlineDataBusiness.searchButtonType //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
    val showSearchList = offlineDataBusiness.showSearchList //是否显示搜索列表 -1.空白不显示， 0.列表为空 1.显示搜索列表
    val mSearchList = offlineDataBusiness.mSearchList //搜索列表数据
    val downLoadingSize = offlineDataBusiness.downLoadingSize //正在下载数量

    val isNight = skyBoxBusiness.themeChange()
    var upDateCityListView = MutableLiveData<Boolean>()
    var savedInstanceState = MutableLiveData<BooleanArray>()

    fun getCurrentCityAdCode(): Int {
        return offlineDataBusiness.getCurrentCityAdCode()
    }

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

    //获取基础包和城市推荐卡片数据
    fun initData() {
        offlineDataBusiness.getBasePackageInfo() //获取基础包数据和状态
        offlineDataBusiness.updateNearCityCard() //附近推荐卡片数据及状态
        upDateCityListView.postValue(true)
    }

    //获取CityItemInfo
    fun getCityItemInfo(adCode: Int): CityItemInfo? {
        return offlineDataBusiness.getCityItemInfo(adCode)
    }

    //获取当前定位城市CityDownLoadItem
    fun getCurrentCityCityDownLoadItem(): CityDownLoadItem? {
        return offlineDataBusiness.getCurrentCityCityDownLoadItem()
    }

    //刷新离线数据，更新UI
    fun refreshOfflineData() {
        offlineDataBusiness.getCurrentCityInfo() //获取当前定位城市数据
        offlineDataBusiness.setHeadViewLoadManagerVisible() //设置下载管理显隐
        initData()//获取基础包和城市推荐卡片数据
        if (showSearchList.value == 1) { //是否显示搜索列表 -1.空白不显示， 0.列表为空 1.显示搜索列表
            refreshSearchList.postValue(true) //刷新搜索列表
        }
    }

    //根据adCode下载城市数据
    fun dealListDownLoad(adCode: Int) {
        offlineDataBusiness.dealListDownLoad(adCode)
    }

    //基础包下载
    fun downLoadBase() {
        offlineDataBusiness.downLoadBase()
    }

    //附近城市全部下载或者暂停
    fun downLoadNearCity(downLoad: Int) {
        offlineDataBusiness.downLoadNearCity(downLoad)
    }

    //获取离线城市列表数据
    fun getAllCityData() {
        offlineDataBusiness.getAllCityData()
    }

    //获取离线城市列表GroupList数组
    fun getGroupList(): ArrayList<ProvinceDataBean> {
        return offlineDataBusiness.mGroupList
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

    //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
    fun setSearchButtonType(type: Int) {
        offlineDataBusiness.setSearchButtonType(type)
    }

    fun onInputKeywordChanged(keyword: String) {
        showSearch.postValue(!TextUtils.isEmpty(keyword))
        if (TextUtils.isEmpty(keyword)) {
            setSearchButtonType(0) //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
        } else {
            offlineDataBusiness.getDateWithSearchContent(keyword) //获取搜索列表
        }
    }
}