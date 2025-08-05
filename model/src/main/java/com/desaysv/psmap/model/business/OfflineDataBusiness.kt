package com.desaysv.psmap.model.business

import android.app.Application
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.data.model.Area
import com.autonavi.gbl.data.model.AreaType
import com.autonavi.gbl.data.model.CityDownLoadItem
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.data.model.DataType
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.MapDataFileType
import com.autonavi.gbl.data.model.OperationType
import com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_DELETE
import com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_PAUSE
import com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_START
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_CHECKING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_ERR
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_READY
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_UNZIPPED
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_UNZIPPING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.common.ThirdParty
import com.autonavi.gbl.util.errorcode.data.Common.ErrorCodeUsbIncompatibleData
import com.autonavi.gbl.util.errorcode.data.Common.ErrorCodeUsbNoData
import com.autosdk.bussiness.data.observer.MapDataObserverImp
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.business.LocationBusiness
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.OptStatus
import com.desaysv.psmap.model.bean.DownloadCityDataBean
import com.desaysv.psmap.model.bean.ProvinceDataBean
import com.desaysv.psmap.model.bean.TYPE_CITY
import com.desaysv.psmap.model.bean.TYPE_PROVINCE
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 离线地图业务类
 */
@Singleton
class OfflineDataBusiness @Inject constructor(
    private val mapDataBusiness: MapDataBusiness,
    private val locationBusiness: LocationBusiness,
    private val application: Application,
    private val gson: Gson,
    private val netWorkManager: NetWorkManager
) {
    val enough = MutableLiveData<Boolean>() //空间提示
    val toDownloadDialog = MutableLiveData<ArrayList<Int>>() //是否用流量下载
    val tipsEnterUnzip = MutableLiveData<Int>() //解压
    val showToast = MutableLiveData<String>() //toast提示
    val currentCityItemInfo = MutableLiveData<CityItemInfo?>()
    val currentCityDownLoadItem = MutableLiveData<CityDownLoadItem?>()
    val workingQueueAdCodeList = MutableLiveData<ArrayList<Int>>()
    val baseDownLoadItem = MutableLiveData<CityDownLoadItem?>()
    val nearCityNumber = MutableLiveData<String>() //附近城市个数
    val nearCitySize = MutableLiveData<String>() //附近城市总大小
    val nearCityDownLoadState = MutableLiveData<Int>() //附近城市下载状态
    val updateOperated = MutableLiveData<Int>()//更新下载状态
    val updateAllData = MutableLiveData<Boolean>() //更新数据
    val updatePercent = MutableLiveData<Boolean>() //更新下载Percent数据

    val onHiddenChanged = MutableLiveData<Boolean>() //界面Hidden监听
    val showDeleteDialog = MutableLiveData<Bundle>() //显示删除对话框
    val showPauseAskDialog = MutableLiveData<Bundle>() //显示暂停对话框
    val showContinueDialog = MutableLiveData<Bundle>() //继续对话框
    val showUpdateDialog = MutableLiveData<Bundle>() //更新对话框
    val showViewLoadManager = MutableLiveData<Boolean>() //下载管理一栏是否显示
    val downLoadingSize = MutableLiveData(0) //正在下载数量

    val searchButtonType = MutableLiveData(0) //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
    val showSearchList = MutableLiveData(-1) //是否显示搜索列表 -1.空白不显示， 0.列表为空 1.显示搜索列表
    val mSearchList = ArrayList<ProvinceDataBean>()//搜索列表数据
    val usbMount = MutableLiveData(false) //U盘挂载状态 true.挂载
    val usbRequestDataListCheckSuccess = MutableLiveData<Boolean>() //U盘模式--检测数据列表回调
    val checkDataInDiskStateStr = MutableLiveData("") //U盘数据状态说明
    val usbDataLoading = MutableLiveData(true) //罐装数据加载状态
    val hasUsbDataData = MutableLiveData(false) //是否有罐装数据
    val hasUsbBaseData = MutableLiveData(false) //是否有基础包罐装数据
    val usbDirectDataNum = MutableLiveData(0) //直辖市罐装数据个数
    val usbProvDataNum = MutableLiveData(0) //省份罐装数据个数
    val usbCityDataNum = MutableLiveData(0) //城市罐装数据个数
    val usbSpecialDataNum = MutableLiveData(0) //特别行政区罐装数据个数
    val usbDataProgress = MutableLiveData(0) //U盘更新进度
    val usbDataError = MutableLiveData(false) //U盘中离线数据是否有问题
    val usbDataDownLoaded = MutableLiveData(false) //U盘中离线数据是否已经全部罐装

    var isFlowDownload = false //true.流量下载暂停， false.进行流量下载
    var location: Location? = null
    private var currentCityAdCode = 0
    var area: Area? = null
    var tipsAdCodes: ArrayList<Int> = arrayListOf()
    var lastRefreshTime: Long = 0
    private val offlineDataScope = CoroutineScope(Dispatchers.IO + Job())

    val mGroupList = ArrayList<ProvinceDataBean>()
    var usbDataAdCodes: ArrayList<Int> = arrayListOf() //U盘中离线数据AdCodes集合

    fun getCurrentCityAdCode(): Int {
        return currentCityAdCode
    }

    //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
    fun setSearchButtonType(type: Int) {
        searchButtonType.postValue(type)
    }

    //获取搜索列表
    @Synchronized
    fun getDateWithSearchContent(searchContent: String) {
        searchButtonType.postValue(2) //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
        mSearchList.clear()
        val allCity = ArrayList<Int>()
        val city = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_CITY)
        val direct = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_DIRECT)
        val special = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_SPECIAL)
        if (direct != null) {
            allCity.addAll(direct)
        }
        if (city != null) {
            allCity.addAll(city)
        }
        if (special != null) {
            allCity.addAll(special)
        }
        for (i in allCity.indices) {
            val cityItemInfo = mapDataBusiness.getCityInfo(allCity[i])
            if (cityItemInfo!!.cityName.lowercase(Locale.getDefault()).contains(searchContent.lowercase(Locale.getDefault())) ||
                cityItemInfo!!.pinyin.lowercase(Locale.getDefault()).replace(" ", "").contains(searchContent.lowercase(Locale.getDefault())) ||
                cityItemInfo!!.initial.lowercase(Locale.getDefault()).replace(" ", "").contains(searchContent.lowercase(Locale.getDefault()))
            ) {
                val cityItemInfos = ArrayList<CityItemInfo>()
                cityItemInfos.add(cityItemInfo)
                mSearchList.add(ProvinceDataBean("search", cityItemInfos))
            }
        }
        val country = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY) ?: arrayListOf()
        for (i in country.indices) {
            val cityItemInfo = mapDataBusiness.getCityInfo(country[i])
            cityItemInfo!!.initial = "jcgnb"
            cityItemInfo!!.pinyin = "jichugongnengbao"
            cityItemInfo!!.cityName = "基础功能包"
            if (cityItemInfo!!.cityName.lowercase(Locale.getDefault()).contains(searchContent.lowercase(Locale.getDefault())) ||
                cityItemInfo!!.pinyin.lowercase(Locale.getDefault()).contains(searchContent.lowercase(Locale.getDefault())) ||
                cityItemInfo!!.initial.lowercase(Locale.getDefault()).contains(searchContent.lowercase(Locale.getDefault()))
            ) {
                val cityItemInfos = ArrayList<CityItemInfo>()
                cityItemInfos.add(cityItemInfo)
                mSearchList.add(ProvinceDataBean("search", cityItemInfos))
            }
        }
        val prov = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_PROV) ?: arrayListOf()
        for (i in prov.indices) {
            val provinceInfo = mapDataBusiness.getProvinceInfo(prov[i])
            if (provinceInfo!!.provName.lowercase(Locale.getDefault()).contains(searchContent.lowercase(Locale.getDefault())) ||
                provinceInfo!!.provPinyin.lowercase(Locale.getDefault()).replace(" ", "").contains(searchContent.lowercase(Locale.getDefault())) ||
                provinceInfo!!.provInitial.lowercase(Locale.getDefault()).replace(" ", "").contains(searchContent.lowercase(Locale.getDefault()))
            ) {
                val cityItemInfo = CityItemInfo()
                cityItemInfo.cityName = "全省地图"
                cityItemInfo.cityAdcode = provinceInfo.provAdcode
                cityItemInfo.belongedProvince = provinceInfo.provAdcode
                provinceInfo.cityInfoList.add(0, cityItemInfo)
                mSearchList.add(ProvinceDataBean(provinceInfo.provName, provinceInfo.cityInfoList))
            }
        }
        searchButtonType.postValue(1) //搜索框是否显示删除，加载按钮状态 0.不显示删除，加载按钮 1.显示删除 2.显示加载
        showSearchList.postValue(if (mSearchList.size > 0) 1 else 0) //是否显示搜索列表 -1.空白不显示， 0.列表为空 1.显示搜索列表
    }

    //获取离线城市列表数据
    fun getAllCityData() {
        mGroupList.clear()
        val direct = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_DIRECT)
        val arrayList = ArrayList<CityItemInfo>()
        if (direct != null) {
            for (i in direct.indices) {
                mapDataBusiness.getCityInfo(direct[i])?.let { arrayList.add(it) }
            }
        }
        mGroupList.add(ProvinceDataBean("直辖市", arrayList))
        val prov = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_PROV)
        if (prov != null) {
            for (i in prov.indices) {
                val provinceInfo = mapDataBusiness.getProvinceInfo(prov[i])
                if (provinceInfo != null) {
                    val cityItemInfo = CityItemInfo()
                    cityItemInfo.cityName = "全省地图"
                    cityItemInfo.cityAdcode = provinceInfo.provAdcode
                    cityItemInfo.belongedProvince = provinceInfo.provAdcode
                    provinceInfo.cityInfoList.add(0, cityItemInfo)
                    mGroupList.add(ProvinceDataBean(provinceInfo.provName, provinceInfo.cityInfoList))
                }
            }
        }
        val special = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_SPECIAL)
        val specialArrayList = ArrayList<CityItemInfo>()
        if (special != null) {
            for (i in special.indices) {
                mapDataBusiness.getCityInfo(special[i])?.let { specialArrayList.add(it) }
            }
        }
        mGroupList.add(ProvinceDataBean("特别行政区", specialArrayList))
    }

    //下载状态判断
    private fun judeDownloadCode(opCode: Int) {
        if (opCode == ThirdParty.ErrorCodeNetCancel || opCode == ThirdParty.ErrorCodeNetFailed || opCode == ThirdParty.ErrorCodeNetUnreach) {
            showToast.postValue(application.getString(R.string.sv_common_network_anomaly_please_try_again))
        } else if (opCode >= ThirdParty.ErrorCodeZip && opCode <= ThirdParty.ErrorCodeUnZip) {
            if (!netWorkManager.isNetworkConnected()) {
                showToast.postValue(application.getString(R.string.sv_common_network_anomaly_please_try_again))
            } else {
                showToast.postValue(application.getString(R.string.sv_common_data_fail_please_try_again))
            }
        }
    }

    //下载操作--下载，暂停，删除等
    fun dataOperation(cityAdCode: ArrayList<Int>?, operationType: Int) {
        //文件大小 MB
        val fileSize: Double = CustomFileUtils.getFileOrFilesSize(AutoConstant.PATH, BaseConstant.SIZE_TYPE_MB)
        Timber.e(fileSize.toString())
        //下载离线地图判断一下当前地图分区大小，如果超过39G左右就限制不给下载，为地图的使用预留空间
        if (fileSize > 39936) {
            enough.postValue(true)
        } else {
            if (netWorkManager.isNetworkConnected()) {
                mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_NET, operationType, cityAdCode)
            } else if (OPERATION_TYPE_START == operationType) {
                showToast.postValue(application.getString(com.autosdk.R.string.record_no_error))
            } else {
                mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_NET, operationType, cityAdCode)
            }
        }
    }

    //刪除操作
    fun dataDeleteOperation(cityAdCode: ArrayList<Int>?) {
        mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_NET, OPERATION_TYPE_DELETE, cityAdCode)
    }

    fun processOptStatus(status: Int) {
        when (status) {
            OptStatus.OPT_NO_SPACE_LEFTED -> {
                // 1. 暂停正在下载的城市数据
                val cityAdCodes = mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)// 获取正在下载的城市数据
                mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_NET, OperationType.OPERATION_TYPE_PAUSE, cityAdCodes)
                enough.postValue(true)
            }

            OptStatus.OPT_SPACE_NOT_ENOUGHT -> enough.postValue(true)
            OptStatus.OPT_DOWNLOAD_NET_ERROR, OptStatus.OPT_NET_DISCONNECT -> showToast.postValue(application.getString(com.autosdk.R.string.record_no_error))
            else -> {}
        }
    }

    //判断空间大小
    fun sizeOfTheSpace() {
        //文件大小 MB
        val fileSize: Double = CustomFileUtils.getFileOrFilesSize(AutoConstant.PATH, BaseConstant.SIZE_TYPE_MB)
        Timber.e(fileSize.toString())
        //下载离线地图判断一下当前地图分区大小，如果超过39G左右就限制不给下载，为地图的使用预留空间
        if (fileSize > 39936) {
            enough.postValue(true)
        }
    }

    //开始下载，继续下载等操作
    fun downLoadClick(cityAdCode: ArrayList<Int>, operationType: Int) {
        offlineDataScope.launch {
            if (OPERATION_TYPE_PAUSE == operationType) {
                isFlowDownload = true
                dataOperation(cityAdCode, operationType)
            } else {
                if (!netWorkManager.isWifiConnected() && netWorkManager.isMobileConnected() && netWorkManager.isNetworkConnected()) {
                    if (OPERATION_TYPE_START == operationType) {
                        toDownloadDialog.postValue(cityAdCode)
                    } else {
                        isFlowDownload = true
                        dataOperation(cityAdCode, operationType)
                    }
                } else {
                    isFlowDownload = true
                    dataOperation(cityAdCode, operationType)
                }
            }
        }
    }

    //确定可以流量下载
    fun downLoadConfirmFlow(cityAdCode: ArrayList<Int>, operationType: Int) {
        offlineDataScope.launch {
            isFlowDownload = true
            dataOperation(cityAdCode, operationType)
        }
    }

    /**
     * 初始化当前城市数据
     * 分为验证前验证后
     */
    fun onRequestDataListCheckResult() {
        offlineDataScope.launch {
            getCurrentCityInfo() //获取当前定位城市数据
        }
    }

    //获取当前定位城市数据
    fun getCurrentCityInfo() {
        currentCityDownLoadItem.postValue(getCurrentCityCityDownLoadItem())
        area = mapDataBusiness.getArea(DownLoadMode.DOWNLOAD_MODE_NET, currentCityAdCode)
        currentCityItemInfo.postValue(getCityItemInfo(currentCityAdCode))
        workingQueueAdCodeList.postValue(mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET))
    }

    //获取CityItemInfo
    fun getCityItemInfo(adCode: Int): CityItemInfo? {
        return mapDataBusiness.getCityInfo(adCode)
    }

    //获取当前定位城市CityDownLoadItem
    fun getCurrentCityCityDownLoadItem(): CityDownLoadItem? {
        location = locationBusiness.getLastLocation()
        currentCityAdCode = mapDataBusiness.getAdCodeByLonLat(location!!.longitude, location!!.latitude)
        return mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, currentCityAdCode)
    }

    //获取基础包数据和状态
    fun getBasePackageInfo() {
        val baseCountry = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)
        val baseCityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, baseCountry?.get(0) ?: 0)
        baseDownLoadItem.postValue(baseCityDownLoadItem)
    }

    //基础包下载
    fun downLoadBase() {
        val baseCountry = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)
        val baseCityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, baseCountry?.get(0) ?: 0)
        dealWithCityDownLoadItem(baseCityDownLoadItem, baseCountry?.get(0) ?: 0)
    }

    //根据adCode下载城市数据
    fun dealListDownLoad(adCode: Int) {
        val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, adCode)
        dealWithCityDownLoadItem(cityDownLoadItem, adCode)
    }

    //附近推荐卡片数据及状态
    fun updateNearCityCard() {
        area?.run {
            var valueTotalSize: Long = 0
            for (integer in vecNearAdcodeList) {
                val downLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, integer)
                valueTotalSize += downLoadItem?.nFullZipSize?.toLong() ?: 0
            }
            nearCityDownLoadState.postValue(getNearTaskStatusCode())//获取附近城市推荐状态，方便进行全部下载，全部暂停，全部删除功能
            nearCityNumber.postValue(vecNearAdcodeList.size.toString() + "个城市") //附近城市个数
            nearCitySize.postValue(CustomFileUtils.formetFileSize(valueTotalSize)) //附近城市总大小
        }
    }

    //获取附近城市推荐状态，方便进行全部下载，全部暂停，全部删除功能
    fun getNearTaskStatusCode(): Int {
        var taskStatusCodeReady = 0 //附近推荐全新未下载标志
        var taskStatusCodeReadyUpdate = 0 //附近推荐更新标志
        var hasDownLoad = 0
        var hasOneDownLoad = false //true.有暂停的任务 false.都是下载或者完成的
        var taskStatusCodeProgress = 0 //用于判断是否在任务中，比如下载，等待中
        area?.run {
            for (i in 0 until vecNearAdcodeList.size) {
                val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, vecNearAdcodeList[i])
                if (cityDownLoadItem != null) {
                    if (cityDownLoadItem.taskState != TASK_STATUS_CODE_SUCCESS) {
                        hasDownLoad++
                    }
                    if (cityDownLoadItem.taskState == TASK_STATUS_CODE_READY) {
                        if (cityDownLoadItem.bUpdate) {
                            taskStatusCodeReadyUpdate += 1
                        } else {
                            taskStatusCodeReady += 1
                        }
                    } else if (cityDownLoadItem.taskState != TASK_STATUS_CODE_READY || cityDownLoadItem.taskState != TASK_STATUS_CODE_PAUSE || cityDownLoadItem.taskState != TASK_STATUS_CODE_SUCCESS || cityDownLoadItem.taskState != TASK_STATUS_CODE_ERR) {
                        taskStatusCodeProgress += 1
                    }
                }
            }
            for (i in 0 until vecNearAdcodeList.size) {
                val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, vecNearAdcodeList[i])
                if (cityDownLoadItem != null) {
                    if (nearItemStatus(cityDownLoadItem) == BaseConstant.OFFLINE_STATE_DOWNLOAD) {
                        hasOneDownLoad = true
                        break
                    }
                } else {
                    hasOneDownLoad = false
                }
            }
            return if (taskStatusCodeReady + taskStatusCodeReadyUpdate == vecNearAdcodeList.size) {
                if (taskStatusCodeReadyUpdate == vecNearAdcodeList.size) {
                    BaseConstant.OFFLINE_STATE_UPDATE //更新状态
                } else {
                    BaseConstant.OFFLINE_STATE_T0_DOWNLOAD //全新未下载状态
                }
            } else {
                if (hasDownLoad != 0) {
                    if (taskStatusCodeProgress == vecNearAdcodeList.size) {
                        if (hasOneDownLoad) {
                            BaseConstant.OFFLINE_STATE_PAUSE //暂停状态
                        } else {
                            BaseConstant.OFFLINE_STATE_DOWNLOAD //下载状态
                        }
                    } else {
                        BaseConstant.OFFLINE_STATE_PAUSE //暂停状态
                    }
                } else {
                    BaseConstant.OFFLINE_STATE_COMPLETE //完成状态
                }
            }
        }
        return BaseConstant.OFFLINE_STATE_T0_DOWNLOAD //全新未下载状态
    }

    private fun nearItemStatus(cityDownLoadItem: CityDownLoadItem?): Int { //1.下载 2.暂停 3.完成
        when (cityDownLoadItem!!.taskState) {
            TASK_STATUS_CODE_ERR -> return BaseConstant.OFFLINE_STATE_DOWNLOAD
            TASK_STATUS_CODE_READY -> return if (cityDownLoadItem.bUpdate) {
                BaseConstant.OFFLINE_STATE_COMPLETE
            } else {
                BaseConstant.OFFLINE_STATE_DOWNLOAD
            }

            TASK_STATUS_CODE_WAITING -> return BaseConstant.OFFLINE_STATE_PAUSE
            TASK_STATUS_CODE_DOING -> return BaseConstant.OFFLINE_STATE_PAUSE
            TASK_STATUS_CODE_PAUSE -> return BaseConstant.OFFLINE_STATE_DOWNLOAD
            TASK_STATUS_CODE_CHECKING -> return BaseConstant.OFFLINE_STATE_PAUSE
            TASK_STATUS_CODE_UNZIPPING -> return BaseConstant.OFFLINE_STATE_PAUSE
            TASK_STATUS_CODE_UNZIPPED -> return BaseConstant.OFFLINE_STATE_PAUSE
            TASK_STATUS_CODE_SUCCESS -> return BaseConstant.OFFLINE_STATE_COMPLETE
            else -> return BaseConstant.OFFLINE_STATE_DOWNLOAD
        }
    }

    //附近城市全部下载或者暂停
    fun downLoadNearCity(downLoad: Int) { //OFFLINE_STATE_UPDATE = 100 //更新状态  OFFLINE_STATE_T0_DOWNLOAD = 200 // 全新未下载状态
        // OFFLINE_STATE_DOWNLOAD = 300 //下载状态 OFFLINE_STATE_PAUSE = 400 //暂停状态 OFFLINE_STATE_COMPLETE = 500 //完成状态
        area?.run {
            when (downLoad) {
                BaseConstant.OFFLINE_STATE_UPDATE, BaseConstant.OFFLINE_STATE_T0_DOWNLOAD, BaseConstant.OFFLINE_STATE_PAUSE -> {
                    if (!netWorkManager.isWifiConnected() && netWorkManager.isMobileConnected() && netWorkManager.isNetworkConnected()) {
                        toDownloadDialog.postValue(vecNearAdcodeList)
                    } else {
                        isFlowDownload = true
                        mapDataBusiness.startAllTask(DownLoadMode.DOWNLOAD_MODE_NET, vecNearAdcodeList)
                    }
                }

                BaseConstant.OFFLINE_STATE_DOWNLOAD -> {
                    mapDataBusiness.pauseAllTask(DownLoadMode.DOWNLOAD_MODE_NET, vecNearAdcodeList)
                }

                BaseConstant.OFFLINE_STATE_COMPLETE -> {
                    mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_NET, OPERATION_TYPE_DELETE, vecNearAdcodeList)
                }
            }
        }
    }

    //获取附近城市AdCode列表
    fun getVecNearAdCodeList(adCode: Int? = null): ArrayList<Int> {
        return mapDataBusiness.getArea(DownLoadMode.DOWNLOAD_MODE_NET, adCode ?: currentCityAdCode)?.vecNearAdcodeList ?: arrayListOf()
    }

    fun dealWithCityDownLoadItem(cityDownLoadItem: CityDownLoadItem?, cityAdCode: Int) {
        var operationType = OperationType.AUTO_UNKNOWN_ERROR
        if (null == cityDownLoadItem) {
            return
        }
        when (cityDownLoadItem.taskState) {
            TASK_STATUS_CODE_ERR, TASK_STATUS_CODE_READY, TASK_STATUS_CODE_PAUSE -> operationType = OPERATION_TYPE_START
            TASK_STATUS_CODE_WAITING, TASK_STATUS_CODE_DOING -> operationType = OPERATION_TYPE_PAUSE
            TASK_STATUS_CODE_CHECKING, TASK_STATUS_CODE_UNZIPPING, TASK_STATUS_CODE_UNZIPPED -> {}
            TASK_STATUS_CODE_SUCCESS -> operationType = OPERATION_TYPE_DELETE
            else -> {}
        }
        if (operationType != OperationType.AUTO_UNKNOWN_ERROR) {
            if (operationType == OPERATION_TYPE_DELETE) {
                showToast.postValue("当前城市已下载")
            } else {
                val cityAdcode = ArrayList<Int>()
                cityAdcode.add(cityAdCode)
                if (cityAdcode.isNotEmpty()) {
                    if (operationType == OPERATION_TYPE_START) {
                        downLoadClick(cityAdcode, OPERATION_TYPE_START)
                    } else {
                        dataOperation(cityAdcode, operationType)
                    }
                }
            }
        }
        if (cityDownLoadItem.taskState == TASK_STATUS_CODE_UNZIPPING) {
            sizeOfTheSpace() //判断空间大小
        }
    }

    fun converNewDownLoadData(arcodes: ArrayList<Int>): ArrayList<DownloadCityDataBean>? {
        return try {
            val itemInfos = ArrayList<DownloadCityDataBean>()
            val downLoadingMap = HashMap<String, ArrayList<CityItemInfo>>()
            val direct = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_DIRECT)
            val special = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_SPECIAL)
            val country = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)
            for (integer in arcodes) {
                if (direct != null) {
                    if (direct.contains(integer)) {
                        val directCity = if (!downLoadingMap.containsKey("直辖市")) {
                            ArrayList()
                        } else {
                            downLoadingMap["直辖市"]!!
                        }
                        mapDataBusiness.getCityInfo(integer)?.let { directCity.add(it) }
                        downLoadingMap["直辖市"] = directCity
                    } else if (special != null) {
                        if (special.contains(integer)) {
                            val specialCity = if (!downLoadingMap.containsKey("特别行政区")) {
                                ArrayList()
                            } else {
                                downLoadingMap["特别行政区"]!!
                            }
                            mapDataBusiness.getCityInfo(integer)?.let { specialCity.add(it) }
                            downLoadingMap["特别行政区"] = specialCity
                        } else if (country != null) {
                            if (country.contains(integer)) { //基础包功能跳过
                                val countryCity = if (!downLoadingMap.containsKey("基础功能包")) {
                                    ArrayList()
                                } else {
                                    downLoadingMap["基础功能包"]!!
                                }
                                mapDataBusiness.getCityInfo(integer)?.let { countryCity.add(it) }
                                countryCity[0]!!.cityName = "基础功能包"
                                downLoadingMap["基础功能包"] = countryCity
                            } else {
                                val cityItemInfo = mapDataBusiness.getCityInfo(integer)
                                val provinceInfo = mapDataBusiness.getProvinceInfo(cityItemInfo!!.belongedProvince)
                                val normalCity = if (!provinceInfo?.let {
                                        downLoadingMap.containsKey(it.provName)
                                    }!!) {
                                    ArrayList()
                                } else {
                                    downLoadingMap[provinceInfo.provName]!!
                                }
                                normalCity.add(cityItemInfo)
                                downLoadingMap[provinceInfo.provName] = normalCity
                            }
                        }
                    }
                }
            }
            if (downLoadingMap.containsKey("基础功能包")) {
                downLoadingMap["基础功能包"]?.let {
                    itemInfos.add(DownloadCityDataBean(TYPE_CITY, it[0]))
                }
            }
            if (downLoadingMap.containsKey("直辖市")) {
                downLoadingMap["直辖市"]?.let {
                    val arCodes: ArrayList<Int> = arrayListOf()
                    val tempDownloadCityDataBean: ArrayList<DownloadCityDataBean> = arrayListOf()
                    for (data in it) {
                        tempDownloadCityDataBean.add(DownloadCityDataBean(TYPE_CITY, data))
                        arCodes.add(data.cityAdcode)
                    }
                    itemInfos.add(DownloadCityDataBean(TYPE_PROVINCE, CityItemInfo().apply { this.cityName = "直辖市" }, arCodes))
                    itemInfos.addAll(tempDownloadCityDataBean)
                }
            }
            if (downLoadingMap.containsKey("特别行政区")) {
                downLoadingMap["特别行政区"]?.let {
                    val arCodes: ArrayList<Int> = arrayListOf()
                    val tempDownloadCityDataBean: ArrayList<DownloadCityDataBean> = arrayListOf()
                    for (data in it) {
                        tempDownloadCityDataBean.add(DownloadCityDataBean(TYPE_CITY, data))
                        arCodes.add(data.cityAdcode)
                    }
                    itemInfos.add(DownloadCityDataBean(TYPE_PROVINCE, CityItemInfo().apply { this.cityName = "特别行政区" }, arCodes))
                    itemInfos.addAll(tempDownloadCityDataBean)
                }
            }
            for (key in downLoadingMap.keys) {
                if ("直辖市" != key && "特别行政区" != key && "基础功能包" != key) {
                    downLoadingMap[key]?.let {
                        val arCodes: ArrayList<Int> = arrayListOf()
                        val tempDownloadCityDataBean: ArrayList<DownloadCityDataBean> = arrayListOf()
                        for (data in it) {
                            tempDownloadCityDataBean.add(DownloadCityDataBean(TYPE_CITY, data))
                            arCodes.add(data.cityAdcode)
                        }
                        itemInfos.add(DownloadCityDataBean(TYPE_PROVINCE, CityItemInfo().apply { this.cityName = key }, arCodes))
                        itemInfos.addAll(tempDownloadCityDataBean)
                    }
                }
            }
            itemInfos
        } catch (e: Exception) {
            Timber.d(" converDownLoadData e:%s", e.message)
            ArrayList()
        }
    }

    fun pauseAllTask() {
        mapDataBusiness.operate(
            DownLoadMode.DOWNLOAD_MODE_NET,
            OPERATION_TYPE_PAUSE,
            mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)
        )
    }

    fun startAllTask() {
        if (!netWorkManager.isWifiConnected() && netWorkManager.isMobileConnected() && netWorkManager.isNetworkConnected()) {
            toDownloadDialog.postValue(mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET))
        } else {
            isFlowDownload = true
            dataOperation(mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET), OPERATION_TYPE_START)
        }
    }

    fun cancelAllTask() {
        mapDataBusiness.operate(
            DownLoadMode.DOWNLOAD_MODE_NET,
            OperationType.OPERATION_TYPE_CANCEL,
            mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)
        )
    }

    //离线地图监听Observer
    var usbMapDataObserverImp: MapDataObserverImp = object : MapDataObserverImp() {
        override fun onOperated(downLoadMode: Int, dataType: Int, opType: Int, opreatedIdList: ArrayList<Int>?) {
            if (downLoadMode == DownLoadMode.DOWNLOAD_MODE_USB && dataType == DataType.DATA_TYPE_MAP) {
                Timber.i("usbMapDataObserverImp onOperated")
                offlineDataScope.launch(Dispatchers.IO) {
                    usbDataOperate(false) //U盘中的离线数据操作-罐装开始或者暂停
                }
            }
        }

        override fun onDownLoadStatus(downLoadMode: Int, dataType: Int, id: Int, taskCode: Int, opCode: Int) {
            if (downLoadMode == DownLoadMode.DOWNLOAD_MODE_USB && dataType == DataType.DATA_TYPE_MAP) {
                Timber.i("usbMapDataObserverImp onDownLoadStatus")
                offlineDataScope.launch(Dispatchers.IO) {
                    usbDataOperate(false) //U盘中的离线数据操作-罐装开始或者暂停
                    if (opCode == com.autonavi.gbl.util.errorcode.common.System.ErrorCodeOutOfDisk) { // 磁盘空间不足
                        enough.postValue(true)
                    }
                    /** TaskStatusCode 处理  */
                    if (TASK_STATUS_CODE_ERR == taskCode && com.autonavi.gbl.util.errorcode.common.System.ErrorCodeOutOfDisk == opCode) {
                        mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_USB, OperationType.OPERATION_TYPE_PAUSE, usbDataAdCodes)
                        enough.postValue(true)
                    }
                    if (opCode >= ThirdParty.ErrorCodeZip && opCode <= ThirdParty.ErrorCodeUnZip) {
                        if (netWorkManager.isNetworkConnected()) {
                            showToast.postValue(application.getString(R.string.sv_common_data_fail_please_try_again))
                        }
                    }
                }
            }
        }

        override fun onPercent(downLoadMode: Int, dataType: Int, id: Int, percentType: Int, percent: Float) {
            offlineDataScope.launch(Dispatchers.IO) {
                Timber.i("usbMapDataObserverImp onPercent")
                val now: Long = System.currentTimeMillis()
                if (now - lastRefreshTime > 1000) {
                    usbDataOperate(false) //U盘中的离线数据操作-罐装开始或者暂停
                    lastRefreshTime = now
                }
            }
        }

        override fun onRequestDataListCheck(downLoadMode: Int, dataType: Int, opCode: Int) {
            offlineDataScope.launch(Dispatchers.IO) {
                if (downLoadMode == DownLoadMode.DOWNLOAD_MODE_USB && dataType == DataType.DATA_TYPE_MAP) { // 下载模式为U盘升级
                    Timber.i("usbMapDataObserverImp onRequestDataListCheck")
                    usbRequestDataListCheckSuccess.postValue(opCode == Service.ErrorCodeOK)
                    if (opCode == Service.ErrorCodeOK) {
                        getUsbMapData() //获取U盘中的离线数据
                        usbDataOperate(true) //U盘中的离线数据操作-罐装开始或者暂停
                    }
                }
            }
        }
    }

    //离线地图监听Observer
    var mapDataObserverImp: MapDataObserverImp = object : MapDataObserverImp() {
        override fun onOperated(downLoadMode: Int, dataType: Int, opType: Int, opreatedIdList: ArrayList<Int>?) {
            if (downLoadMode == DownLoadMode.DOWNLOAD_MODE_NET && dataType == DataType.DATA_TYPE_MAP) {
                offlineDataScope.launch {
                    updateOperated.postValue(opType)//更新下载状态
                }
            }
        }

        override fun onDownLoadStatus(downLoadMode: Int, dataType: Int, id: Int, taskCode: Int, opCode: Int) {
            offlineDataScope.launch {
                /** ThirdParty 处理  */
                if (opCode == ThirdParty.ErrorCodeNetUnreach) {    // 无网络
                    // 提示无网络链接toast
                    processOptStatus(OptStatus.OPT_NET_DISCONNECT)
                } else if (opCode == ThirdParty.ErrorCodeNetFailed) { // 网络异常
                    processOptStatus(OptStatus.OPT_DOWNLOAD_NET_ERROR)
                } else if (opCode == com.autonavi.gbl.util.errorcode.common.System.ErrorCodeOutOfDisk) { // 磁盘空间不足
                    processOptStatus(OptStatus.OPT_SPACE_NOT_ENOUGHT)
                }
                /** TaskStatusCode 处理  */
                if (TASK_STATUS_CODE_ERR == taskCode && com.autonavi.gbl.util.errorcode.common.System.ErrorCodeOutOfDisk == opCode) {
                    processOptStatus(OptStatus.OPT_NO_SPACE_LEFTED)
                }
                /** 通知全部数据变更,获取城市数据信息更新UI控件文案信息  */
                if (TASK_STATUS_CODE_SUCCESS == taskCode) {
                    filterTipsAdCodes(id)//tipsAdCodes数据过滤
                    updateAllData.postValue(true)//更新数据
                } else if (TASK_STATUS_CODE_PAUSE == taskCode) {
                    updateOperated.postValue(1)//更新下载状态
                } else if (TASK_STATUS_CODE_READY == taskCode) {
                    updateAllData.postValue(true)//更新数据
                }

                if (taskCode == TASK_STATUS_CODE_UNZIPPING) { //执行中
                    sizeOfTheSpace() //判断空间大小
                    tipsEnterUnzip.postValue(id)
                }

                judeDownloadCode(opCode) //下载状态判断
            }
        }

        override fun onPercent(downLoadMode: Int, dataType: Int, id: Int, percentType: Int, percent: Float) {
            offlineDataScope.launch {
                val now: Long = System.currentTimeMillis()
                if (now - lastRefreshTime > 500) {
                    updatePercent.postValue(true)//更新下载Percent数据
                    lastRefreshTime = now
                }
            }
        }

        override fun onRequestDataListCheck(downLoadMode: Int, dataType: Int, opCode: Int) {
            if (opCode == Service.ErrorCodeOK) {
                onRequestDataListCheckResult()
            } else {
                showToast.postValue("获取数据列表失败opCode：$opCode");
            }
        }
    }

    //tipsAdCodes数据过滤
    fun filterTipsAdCodes(adCode: Int) {
        if (tipsAdCodes != null && tipsAdCodes.size > 0) {
            tipsAdCodes = tipsAdCodes.filter { it != adCode } as ArrayList<Int>
        }
    }

    /**
     * 设置下载管理显隐
     */
    fun setHeadViewLoadManagerVisible() {
        val downLoadSize = mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)?.size ?: 0
        val cityItemInfos = mapDataBusiness.getCityList()
        var isHasDownLoadSuccess = false
        for (cityItemInfo in cityItemInfos!!) {
            val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, cityItemInfo.cityAdcode)
            if (cityDownLoadItem != null && (cityDownLoadItem.taskState == TASK_STATUS_CODE_SUCCESS || (cityDownLoadItem.bUpdate))) {
                isHasDownLoadSuccess = true
                break
            }
        }
        //判断基础包是否下载
        val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(
            DownLoadMode.DOWNLOAD_MODE_NET,
            mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)?.get(0) ?: 0
        )
        if (cityDownLoadItem != null && (cityDownLoadItem.taskState == TASK_STATUS_CODE_SUCCESS || (cityDownLoadItem.bUpdate))) {
            isHasDownLoadSuccess = true
        }
        showViewLoadManager.postValue(downLoadSize > 0 || isHasDownLoadSuccess)
        downLoadingSize.postValue(downLoadSize)
    }

    //数据罐装-U盘数据状态判断
    fun setCheckDataInDiskState(state: Int) {
        checkDataInDiskStateStr.postValue(
            when (state) {
                Service.ErrorCodeOK -> {//有罐装数据
                    Timber.i("setCheckDataInDiskState 有罐装数据")
                    ""
                }

                Service.ErrorCodeInvalidParam -> {//无效参数
                    application.getString(com.desaysv.psmap.model.R.string.sv_mapdata_usb_error_code_invalid_param)
                }

                ErrorCodeUsbNoData -> {//不存在有效数据
                    application.getString(com.desaysv.psmap.model.R.string.sv_mapdata_usb_error_code_usb_no_data)
                }

                ErrorCodeUsbIncompatibleData -> {//存在无法兼容的数据
                    application.getString(com.desaysv.psmap.model.R.string.sv_mapdata_usb_error_code_usb_incompatible_data)
                }

                else -> { //其他 失败
                    Timber.i("setCheckDataInDiskState fail state:$state")
                    application.getString(com.desaysv.psmap.model.R.string.sv_mapdata_usb_other_fail)
                }
            }
        )
    }

    //获取U盘中的离线数据
    fun getUsbMapData() {
        usbDataAdCodes.clear()
        val adCodeCountry = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_USB, AreaType.AREA_TYPE_COUNTRY) //基础功能包adcode
        val adCodeDirectList = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_USB, AreaType.AREA_TYPE_DIRECT) //获取直辖市adcode列表
        val adCodeProvLst = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_USB, AreaType.AREA_TYPE_PROV) //获取省份adcode列表（按拼音排序）
        val adCodeCityLst = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_USB, AreaType.AREA_TYPE_CITY) //获取一般普通城市adcode列表
        val adCodeSpecialList = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_USB, AreaType.AREA_TYPE_SPECIAL) //获取特别行政区adcode列表（按拼音排序）
        if (null != adCodeCountry) {
            val dataFileVersion = mapDataBusiness.getDataFileVersion(adCodeCountry[0], MapDataFileType.MAP_DATA_TYPE_FILE_MAP)
            Timber.i("getUsbMapData adCodeCountry dataFileVersion == null:${TextUtils.isEmpty(dataFileVersion)}")
            if (!TextUtils.isEmpty(dataFileVersion)) {
                val baseCityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_USB, adCodeCountry[0])
                Timber.i("getUsbMapData adCodeCountry bValidItem:${baseCityDownLoadItem?.bValidItem}")
                baseCityDownLoadItem?.let {
                    hasUsbBaseData.postValue(it.bValidItem)
                    usbDataAdCodes.add(it.adcode)
                }
            } else {
                hasUsbBaseData.postValue(false)
            }
        } else {
            hasUsbBaseData.postValue(false)
        }
        if (null != adCodeDirectList) {
            Timber.i("getUsbMapData adCodeDirectList:${gson.toJson(adCodeDirectList)}")
            usbDirectDataNum.postValue(adCodeDirectList.size)
            for (code in adCodeDirectList) {
                usbDataAdCodes.add(code)
            }
        } else {
            usbDirectDataNum.postValue(0)
        }
        if (null != adCodeProvLst) {
            Timber.i("getUsbMapData adCodeProvLst:${gson.toJson(adCodeProvLst)}")
            usbProvDataNum.postValue(adCodeProvLst.size)
        } else {
            usbProvDataNum.postValue(0)
        }
        if (null != adCodeCityLst) {
            Timber.i("getUsbMapData adCodeCityLst.size:${adCodeCityLst.size}")
            usbCityDataNum.postValue(adCodeCityLst.size)
            for (code in adCodeCityLst) {
                usbDataAdCodes.add(code)
            }
        } else {
            usbCityDataNum.postValue(0)
        }
        if (null != adCodeSpecialList) {
            Timber.i("getUsbMapData adCodeSpecialList:${gson.toJson(adCodeSpecialList)}")
            usbSpecialDataNum.postValue(adCodeSpecialList.size)
            for (code in adCodeSpecialList) {
                usbDataAdCodes.add(code)
            }
        } else {
            usbSpecialDataNum.postValue(0)
        }
        hasUsbDataData.postValue(usbDataAdCodes.size > 0)
        usbDataLoading.postValue(false)
    }

    //U盘中的离线数据操作-罐装开始或者暂停
    fun usbDataOperate(isRequestDataListCheck: Boolean) {
        Timber.i("usbMapDataObserverImp usbDataOperate")
        usbDataError.postValue(false)
        var hasDownloadNum = 0 //已经完成数量
        var dataError = false
        val size = usbDataAdCodes.size
        if (size > 0) {
            Timber.i("usbMapDataObserverImp usbDataOperate size > 0")
            for (adCode in usbDataAdCodes) {
                val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_USB, adCode)
                cityDownLoadItem?.let {
                    if (it.bValidItem) {
                        Timber.i("usbMapDataObserverImp usbDataOperate taskState:${it.taskState}")
                        if (it.taskState == TASK_STATUS_CODE_SUCCESS) {
                            hasDownloadNum++
                        }
                    } else {
                        dataError = true
                        usbDataError.postValue(true)
                    }
                }
            }
            Timber.i("usbMapDataObserverImp usbDataOperate hasDownloadNum:$hasDownloadNum usbDataAdCodes.size:$size progress：${hasDownloadNum * 100 / size}")
            usbDataProgress.postValue(hasDownloadNum * 100 / size)
        } else {
            usbDataProgress.postValue(0)
        }
        if (dataError) {
            mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_USB, OPERATION_TYPE_PAUSE, usbDataAdCodes)
        } else if (isRequestDataListCheck) {
            Timber.i("usbMapDataObserverImp usbDataOperate OPERATION_TYPE_START")
            mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_USB, OPERATION_TYPE_START, usbDataAdCodes)
        }
        if (hasDownloadNum == size) {//全部罐装完毕了
            usbDataDownLoaded.postValue(true)
        }
    }

    //停止U盘下载
    fun stopUsbDownload() {
        mapDataBusiness.operate(DownLoadMode.DOWNLOAD_MODE_USB, OPERATION_TYPE_START, usbDataAdCodes)
    }

    //退出罐装界面，数据状态恢复默认值
    fun defaultUsbState() {
        checkDataInDiskStateStr.postValue("") //U盘数据状态说明
        usbDataLoading.postValue(true) //罐装数据加载状态
        hasUsbDataData.postValue(false) //是否有罐装数据
        hasUsbBaseData.postValue(false) //是否有基础包罐装数据
        usbDirectDataNum.postValue(0) //直辖市罐装数据个数
        usbProvDataNum.postValue(0) //省份罐装数据个数
        usbCityDataNum.postValue(0) //城市罐装数据个数
        usbSpecialDataNum.postValue(0) //特别行政区罐装数据个数
        usbDataProgress.postValue(0) //U盘更新进度
        usbDataError.postValue(false) //U盘中离线数据是否有问题
        usbDataDownLoaded.postValue(false) //U盘中离线数据是否已经全部罐装
    }
}