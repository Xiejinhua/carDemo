package com.desaysv.psmap.base.business

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.data.model.DataType
import com.autonavi.gbl.data.model.DownLoadMode.DOWNLOAD_MODE_NET
import com.autonavi.gbl.data.model.DownLoadMode.DownLoadMode1
import com.autonavi.gbl.data.model.OperationType
import com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_DELETE
import com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_PAUSE
import com.autonavi.gbl.data.model.OperationType.OPERATION_TYPE_START
import com.autonavi.gbl.data.model.OperationType.OperationType1
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_CHECKING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_ERR
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_READY
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_UNZIPPED
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_UNZIPPING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING
import com.autonavi.gbl.data.model.Voice
import com.autonavi.gbl.data.model.VoiceEngineType
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.common.ThirdParty
import com.autosdk.bussiness.data.VoiceDataController
import com.autosdk.bussiness.data.observer.VoiceObserverImp
import com.autosdk.common.AutoConstant
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.OptStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


/**
 * 导航语音数据业务
 */
@Singleton
class VoiceDataBusiness @Inject constructor(
    private val voiceDataController: VoiceDataController,
    private val application: Application,
    private val netWorkManager: NetWorkManager
) {
    val dataListCheckResult = MutableLiveData<Boolean>() //检测云端语音数据列表是否成功
    val voiceList = MutableLiveData<MutableList<Voice>>()//voice数据列表--用于界面更新
    val updateImageResult = MutableLiveData<Int>()//头像更新回调
    val enough = MutableLiveData<Boolean>() //空间提示
    val toDownloadDialog = MutableLiveData<ArrayList<Int>>() //是否用流量下载
    val tipsEnterUnzip = MutableLiveData<Int>() //解压
    val showToast = MutableLiveData<String>() //toast提示
    val updateOperated = MutableLiveData<Int>()//更新下载状态
    val updateAllData = MutableLiveData<Boolean>() //更新数据
    val updatePercent = MutableLiveData<Boolean>() //更新下载Percent数据
    val setUseVoice = MutableLiveData<Voice>() //使用该语音包

    var voiceDataList = mutableListOf<Voice>() //语音列表数据结合，包括头像，名称等
    var isFlowDownload = false //true.流量下载暂停， false.进行流量下载
    var tipsVoiceIds: ArrayList<Int> = arrayListOf()
    var lastRefreshTime: Long = 0
    private val voiceDataScope = CoroutineScope(Dispatchers.IO + Job())
    var flowDownloadJob: Job? = null

    //服务初始化
    fun initService() {
        voiceDataController.initService()
        registerVoiceObserver()
    }

    /**
     * 是否初始化成功
     */
    fun isInitSuccess(): Boolean {
        return voiceDataController.isInitSuccess
    }

    fun unInit() {
        voiceDataController.unInit()
        unregisterVoiceObserver()
    }

    fun requestDataListCheck(downLoadMode: Int, path: String?): Int {
        return voiceDataController.requestDataListCheck(downLoadMode, path)
    }

    fun requestDataImage(downLoadMode: Int, voiceId: Int): Int {
        return voiceDataController.requestDataImage(downLoadMode, voiceId)
    }

    fun abortRequestDataListCheck(@DownLoadMode1 downloadMode: Int) {
        voiceDataController.abortRequestDataListCheck(downloadMode)
    }

    fun abortRequestDataImage(@DownLoadMode1 downloadMode: Int, voiceId: Int) {
        voiceDataController.abortRequestDataImage(downloadMode, voiceId)
    }

    fun getVoiceIdList(downLoadMode: Int): ArrayList<Int>? {
        return voiceDataController.getVoiceIdList(downLoadMode)
    }

    fun getVoiceIdList(downLoadMode: Int, engineType: Int): ArrayList<Int>? {
        return voiceDataController.getVoiceIdList(downLoadMode, engineType)
    }

    fun getVoice(downLoadMode: Int, voiceId: Int): Voice? {
        return voiceDataController.getVoice(downLoadMode, voiceId)
    }

    fun operate(@DownLoadMode1 downLoadMode: Int, @OperationType1 opType: Int, voiceIdDiyLst: ArrayList<Int>?) {
        voiceDataController.operate(downLoadMode, opType, voiceIdDiyLst)
    }

    fun operateWorkingQueue(@DownLoadMode1 downLoadMode: Int, @OperationType1 opType: Int, engineType: Int) {
        voiceDataController.operateWorkingQueue(downLoadMode, opType, engineType)
    }

    fun registerVoiceDataObserver() {
        voiceDataController.registerVoiceDataObserver(voiceObserverImp)
    }

    fun unregisterVoiceDataObserver() {
        voiceDataController.unregisterVoiceDataObserver(voiceObserverImp)
    }

    private fun registerVoiceObserver() {
        voiceDataController.registerVoiceDataObserver(voiceObserver)
    }

    private fun unregisterVoiceObserver() {
        voiceDataController.unregisterVoiceDataObserver(voiceObserver)
    }

    //获取voiceId集合并判断是否有数据
    fun getVoiceIdListJudeData() {
        getVoiceIdList() //获取voiceId集合并
        todoRequestDataImage() //头像请求
    }

    //获取voiceId集合并
    fun getVoiceIdList() {
        voiceDataList.clear()
        val voiceIds = getVoiceIdList(DOWNLOAD_MODE_NET) ?: mutableListOf()
        val sizeLength = voiceIds.size > 0
        if (sizeLength) {
            for (index in voiceIds.indices) {
                val voice = getVoice(DOWNLOAD_MODE_NET, voiceIds[index])
                if (voice?.hidden == 1) {
                    Timber.i("getVoiceList hidden:1 隐藏包，此时可不下发。")
                } else {
                    voiceDataList.add(voice ?: Voice())
                }
            }
        }
        voiceDataList.add(0, Voice().apply {
            this.id = -1
            this.name = application.getString(R.string.sv_setting_default_woman)
        })
        voiceDataList.sortWith(compareByDescending { it.isRecommended })
        voiceList.postValue(voiceDataList) //voice数据列表--用于界面更新
    }

    //头像请求
    private fun todoRequestDataImage() {
        for (item in voiceDataList) {
            if (item.id != -1)
                requestDataImage(DOWNLOAD_MODE_NET, item.id)
        }
    }

    //退出界面，取消头像下载
    fun todoAbortRequestDataImage() {
        for (item in voiceDataList) {
            if (item.id != -1)
                abortRequestDataImage(DOWNLOAD_MODE_NET, item.id)
        }
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
    private fun dataOperation(voiceIds: ArrayList<Int>?, operationType: Int) {
        //文件大小 MB
        val fileSize: Double = CustomFileUtils.getFileOrFilesSize(AutoConstant.PATH, BaseConstant.SIZE_TYPE_MB)
        Timber.e(" dataOperation fileSize:$fileSize")
        //下载离线地图判断一下当前地图分区大小，如果超过39G左右就限制不给下载，为地图的使用预留空间
        if (fileSize > 39936) {
            enough.postValue(true)
        } else {
            if (netWorkManager.isNetworkConnected()) {
                operate(DOWNLOAD_MODE_NET, operationType, voiceIds)
            } else if (OPERATION_TYPE_START == operationType) {
                showToast.postValue(application.getString(com.autosdk.R.string.record_no_error))
            } else {
                operate(DOWNLOAD_MODE_NET, operationType, voiceIds)
            }
        }
    }

    fun processOptStatus(status: Int) {
        when (status) {
            OptStatus.OPT_NO_SPACE_LEFTED -> {
                // 1. 暂停正在下载的数据
                operateWorkingQueue(DOWNLOAD_MODE_NET, OPERATION_TYPE_PAUSE, VoiceEngineType.VOICE_ENGINE_TYPE_FLYTEK)// 获取正在下载的城市数据
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
    fun downLoadClick(voiceIds: ArrayList<Int>, operationType: Int) {
        voiceDataScope.launch {
            if (OPERATION_TYPE_PAUSE == operationType) {
                isFlowDownload = true
                dataOperation(voiceIds, operationType)
            } else {
                if (!netWorkManager.isWifiConnected() && netWorkManager.isMobileConnected() && netWorkManager.isNetworkConnected()) {
                    if (OPERATION_TYPE_START == operationType) {
                        toDownloadDialog.postValue(voiceIds)
                    } else {
                        isFlowDownload = true
                        dataOperation(voiceIds, operationType)
                    }
                } else {
                    isFlowDownload = true
                    dataOperation(voiceIds, operationType)
                }
            }
        }
    }

    //确定可以流量下载
    fun downLoadConfirmFlow(cityAdCode: ArrayList<Int>, operationType: Int) {
        voiceDataScope.launch {
            isFlowDownload = true
            dataOperation(cityAdCode, operationType)
        }
    }

    //tipsVoiceIds数据过滤
    fun filterTipsVoiceIds(voiceId: Int) {
        if (tipsVoiceIds != null && tipsVoiceIds.size > 0) {
            tipsVoiceIds = tipsVoiceIds.filter { it != voiceId } as ArrayList<Int>
        }
    }

    //下载按钮操作
    fun dealWithVoice(voice: Voice) {
        var operationType = OperationType.AUTO_UNKNOWN_ERROR
        when (voice.taskState) {
            TASK_STATUS_CODE_ERR, TASK_STATUS_CODE_READY, TASK_STATUS_CODE_PAUSE -> operationType = OPERATION_TYPE_START
            TASK_STATUS_CODE_WAITING, TASK_STATUS_CODE_DOING -> operationType = OPERATION_TYPE_PAUSE
            TASK_STATUS_CODE_CHECKING, TASK_STATUS_CODE_UNZIPPING, TASK_STATUS_CODE_UNZIPPED -> {}
            TASK_STATUS_CODE_SUCCESS -> operationType = OPERATION_TYPE_DELETE
            else -> {}
        }
        if (operationType != OperationType.AUTO_UNKNOWN_ERROR) {
            if (operationType == OPERATION_TYPE_DELETE) {
                setUseVoice.postValue(voice)
            } else {
                val voiceIds = ArrayList<Int>()
                voiceIds.add(voice.id)
                if (operationType == OPERATION_TYPE_START) {
                    downLoadClick(voiceIds, OPERATION_TYPE_START)
                } else {
                    dataOperation(voiceIds, operationType)
                }
            }
        }
        if (voice.taskState == TASK_STATUS_CODE_UNZIPPING) {
            sizeOfTheSpace() //判断空间大小
        }
    }

    private val voiceObserverImp = object : VoiceObserverImp() {
        override fun onInit(downLoadMode: Int, dataType: Int, opCode: Int) {
            if (downLoadMode == DOWNLOAD_MODE_NET && dataType == DataType.DATA_TYPE_VOICE && opCode == Service.ErrorCodeOK) {
                // 初始化成功，继续操作
                requestDataListCheck(DOWNLOAD_MODE_NET, "")
            } else {
                // 初始化失败，其他处理
                Timber.d("mVoiceService 初始化失败")
            }
        }

        override fun onRequestDataListCheck(downLoadMode: Int, dataType: Int, opCode: Int) {
            if (downLoadMode == DOWNLOAD_MODE_NET && dataType == DataType.DATA_TYPE_VOICE && opCode == Service.ErrorCodeOK) {
                dataListCheckResult.postValue(true)
                getVoiceIdListJudeData() //获取voiceId集合并判断是否有数据
            } else {
                dataListCheckResult.postValue(false)
            }
        }

        override fun onOperated(downLoadMode: Int, dataType: Int, opType: Int, opreatedIdList: java.util.ArrayList<Int>?) {
            if (downLoadMode == DOWNLOAD_MODE_NET && dataType == DataType.DATA_TYPE_VOICE) {
                voiceDataScope.launch {
                    updateOperated.postValue(opType)//更新下载状态
                }
            }
        }

        override fun onDownLoadStatus(downLoadMode: Int, dataType: Int, id: Int, taskCode: Int, opCode: Int) {
            voiceDataScope.launch {
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
                    filterTipsVoiceIds(id)//tipsVoiceIds数据过滤
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
            voiceDataScope.launch {
                val now: Long = System.currentTimeMillis()
                if (now - lastRefreshTime > 500) {
                    updatePercent.postValue(true)//更新下载Percent数据
                    lastRefreshTime = now
                }
            }
        }

        override fun onDownloadImage(itemId: Int, opErrCode: Int, strFilePath: String?, dataType: Int) {
            voiceDataScope.launch {
                delay(200)
                if (dataType == DataType.DATA_TYPE_VOICE && opErrCode == Service.ErrorCodeOK) {
                    val index = voiceDataList.indexOfFirst { it.id == itemId }
                    if (index != -1) {
                        Timber.i("onDownloadImage voice:$itemId, strFilePath:$strFilePath index:$index")
                        updateImageResult.postValue(index)
                    }
                }
            }
        }
    }


    private val voiceObserver = object : VoiceObserverImp() {
        override fun onInit(downLoadMode: Int, dataType: Int, opCode: Int) {
            if (downLoadMode == DOWNLOAD_MODE_NET && dataType == DataType.DATA_TYPE_VOICE && opCode == Service.ErrorCodeOK) {
                // 初始化成功，继续操作
                requestDataListCheck(DOWNLOAD_MODE_NET, "")
            } else {
                // 初始化失败，其他处理
                Timber.d("mVoiceService 初始化失败")
            }
        }

        override fun onRequestDataListCheck(downLoadMode: Int, dataType: Int, opCode: Int) {
            if (downLoadMode == DOWNLOAD_MODE_NET && dataType == DataType.DATA_TYPE_VOICE && opCode == Service.ErrorCodeOK) {
                dataListCheckResult.postValue(true)
                getVoiceIdListJudeData() //获取voiceId集合并判断是否有数据
            } else {
                dataListCheckResult.postValue(false)
            }
        }
    }
}