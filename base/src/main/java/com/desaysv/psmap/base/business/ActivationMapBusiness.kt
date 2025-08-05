package com.desaysv.psmap.base.business

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import com.autonavi.gbl.activation.ActivationModule
import com.autonavi.gbl.servicemanager.ServiceMgr
import com.autonavi.gbl.util.errorcode.Activation
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.activate.ActivateController
import com.autosdk.bussiness.common.utils.FileUtils
import com.autosdk.bussiness.location.LocationController
import com.autosdk.common.AutoConstant
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.BuildConfig
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.net.RetrofitRepository
import com.desaysv.psmap.base.net.bean.BaseRequestBody
import com.desaysv.psmap.base.utils.ActiveErrorUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.google.gson.Gson
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Author : wangmansheng
 * Date : 2024-1-14
 * Description : 激活业务
 */
@Singleton
class ActivationMapBusiness @Inject constructor(
    private val application: Application,
    private val locationController: LocationController,
    private val activateController: ActivateController,
    private val retrofitRepository: RetrofitRepository,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val netWorkManager: NetWorkManager,
    private val carInfo: ICarInfoProxy,
    private val gson: Gson
) {

    private val isOpenActivation = true //根据SDK版本来判断是否开启激活功能==先屏蔽激活页
    var isOpenAgreement = false //此次是否同意协议
    private var sdkActivateCode = Activation.AUTO_UNKNOWN_ERROR//激活结果
    private var mActivateResultListener: IActivateResultListener? = null

    private val _isShowActivateLayout = MutableLiveData<Boolean>()
    val isShowActivateLayout = _isShowActivateLayout

    private val _isShowAgreementLayout = MutableLiveData<Boolean>()
    val isShowAgreementLayout = _isShowAgreementLayout

    private val _activateResult = MutableLiveData<Int>() //激活结果 0.成功 1.手动激活失败 2.网络激活失败 3.usb激活失败
    val activateResult = _activateResult

    private val _isActivating = MutableLiveData<Boolean>()//是否激活中
    val isActivating = _isActivating

    private val _postActivateResult = MutableLiveData<Boolean>()//请求后台进行激活统计
    val postActivateResult = _postActivateResult

    val showActivateAgreement = MutableLiveData(true)//在激活或者地图提示界面

    val failType = MutableLiveData("") //失败类型
    val failTip = MutableLiveData("") //失败提示

    fun toSetPostActivateResult(value: Boolean) {
        _postActivateResult.postValue(value)
    }

    //没有网络提示
    fun noNet() {
        failType.postValue("网络异常，联网激活失败")
        failTip.postValue("您可以重试联网激活，或选择手动激活的方式进行激活")
    }

    fun initActivation() {
        activateController.setNotifyNetActivate { resultCode, _ ->
            run {
                _isActivating.postValue(false)
                Timber.d("onNetActivateResponse is %s", ActiveErrorUtils.activeErrorMessage(resultCode))
                sdkActivateCode = resultCode
                if (Service.ErrorCodeOK == resultCode) {
                    _activateResult.postValue(0) //激活结果 0.成功 1.手动激活失败 2.网络激活失败 3.usb激活失败
                    notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_NET, "联网激活成功") //通知外部激活结果
                    _postActivateResult.postValue(true)
                } else {
                    if (!netWorkManager.isNetworkConnected()) {
                        noNet() //没有网络提示
                    } else {
                        failType.postValue("联网激活失败（" + ActiveErrorUtils.activeErrorMessage(sdkActivateCode) + "）")
                        failTip.postValue("设备号 " + carInfo.uuid)
                    }
                    _activateResult.postValue(2) //激活结果 0.成功 1.手动激活失败 2.网络激活失败 3.usb激活失败
                    notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_NET, ActiveErrorUtils.activeErrorMessage(sdkActivateCode)) //通知外部激活结果
                }
                refreshLayout() //通知刷新UI
            }
        }

        if (isOpenActivation) {
            Timber.d("测试版不用激活 ===========>> Close Activation");
            sdkActivateCode = Service.ErrorCodeOK
        } else {
            sdkActivateCode = activateController.activateStatus
        }
        Timber.i("init sdkActivateCode is $sdkActivateCode")
        refreshLayout()//通知刷新UI
    }

    fun setThisTimeAgreement(isOpenAgreement: Boolean) {
        this.isOpenAgreement = isOpenAgreement
    }


    fun getActivationModule(): ActivationModule {
        return activateController.activationService
    }

    /**
     * 当上层传入的设备ID不为32位时，按照以下逻辑将设备ID变为32位，此逻辑与激活后台逻辑一致，否则无法完成激活。
     * case A：机器码不足16位或正好16位的，先补零至16位（在原id后面补0），然后转换为ANSCII码，生成32位的UUID；
     * case B：机器码超过16位的，小于32位的，在原id后面补0至32位；
     * case C：等于32位，直接使用。
     * case D：大于32位，只取前32位。
     *
     * @param id TUID导航激活码
     * @return
     */
    fun getDigitCompletion(id: String?): String {
        return getActivationModule().digitCompletion(id) ?: ""
    }

    //激活初始化
    fun dataInit(): Int { // 初始化、检查激活状态
        return if (!isOpenActivation) {
            activateController.init(carInfo.uuid)
        } else {
            Timber.d("dataInit close  Activation function")
            Service.ErrorCodeOK
        }
    }

    //激活界面反初始化
    fun unActiveInit() {
        activateController.unActiveInit()
    }

    //激活反初始化
    fun unInit() {
        activateController.unInit()
    }

    /**
     * 是否不再提示地图协议
     * @return true/false
     */
    fun isAgreement(): Boolean {
        val mapAgreement = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.active)?.getBooleanValue(
            MapSharePreference.SharePreferenceKeyEnum.mapAgreement, false
        )
        Timber.d("isAgreement: %s", mapAgreement)
        return mapAgreement ?: false
    }

    fun isActivate(): Boolean {
        if (isOpenActivation) {
            Timber.d("===========>> Close Activation");
            return true;
        }
        val isActivate = activateController.isActivate
        if (!isActivate) {
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.active)?.putBooleanValue(
                MapSharePreference.SharePreferenceKeyEnum.mapAgreement, false
            )
        }
        return isActivate
    }

    /**
     * 同意协议
     */
    fun doAgreementYes(noMoreTips: Boolean) {
        Timber.d("doAgreementYes noMoreTips is $noMoreTips")
        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.active)?.putBooleanValue(
            MapSharePreference.SharePreferenceKeyEnum.mapAgreement, noMoreTips
        )
        setThisTimeAgreement(true)
    }

    /**
     * 判断是否启动激活功能
     * @return
     */
    fun isStartActivate(): Boolean = activateController.isStartActivate

    /**
     * 网络激活
     *
     * @return code
     */
    fun netActivate(): Int {
        if (!netWorkManager.isNetworkConnected()) {
            noNet() //没有网络提示
            notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_NET, "network not ok") //通知外部激活结果
            _activateResult.postValue(2) //激活结果 0.成功 1.手动激活失败 2.网络激活失败 3.usb激活失败
            return Activation.AUTO_UNKNOWN_ERROR
        }
        _isActivating.postValue(true)
        val i = if (CommonUtils.isVehicle()) {
            activateController.netActivate()
        } else {
            val activeSnCode = BuildConfig.emulatorActiveSnCode
            val activeCode = BuildConfig.emulatorActiveCode
            manualActivate(activeSnCode, activeCode)
        }
        Timber.i("netActivate is $i")
        if (i != Service.ErrorCodeOK) {
            _isActivating.postValue(false)
            failType.postValue("请求失败,请重新尝试！")
            failTip.postValue("设备号 " + carInfo.uuid)
        }
        return i
    }

    /**
     * U盘激活
     */
    fun usbActivate() {
        Timber.d("usbActivate")
        _isActivating.postValue(true)
        //获取U盘路径
//        val existUdiskList = MediaDataManager.getInstance().existUdiskList //获取已连接U盘
        val existUdiskList = ArrayList<String>() //获取已连接U盘
        if (existUdiskList == null || existUdiskList.size == 0) {
            _isActivating.postValue(false)
            if (!netWorkManager.isNetworkConnected()) {
                noNet() //没有网络提示
            } else {
                failType.postValue("请插入U盘！")
                failTip.postValue("设备号 " + carInfo.uuid)
            }
            notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_USB, "not found usb devices") //通知外部激活结果
            return
        }

        //扫描U盘激活文件
        var activateFile: File? = null
        for (usbPath in existUdiskList) {
            Timber.d("usbPath is $usbPath")
            if (!TextUtils.isEmpty(usbPath)) {
                //File checkFile = new File(usbPath + CarInfoProxyManager.instance().getSNCode() +"/ReLoginCode.csv");
                val checkFile = File("$usbPath/desayMapActivate/ReLoginCode.csv") //test
                if (checkFile.exists() && checkFile.isFile) {
                    activateFile = checkFile
                    break
                }
            }
        }
        if (activateFile == null) {
            _isActivating.postValue(false)
            if (!netWorkManager.isNetworkConnected()) {
                noNet() //没有网络提示
            } else {
                failType.postValue("检测不到激活文件，请检查U盘！")
                failTip.postValue("设备号 " + carInfo.uuid)
            }
            notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_USB, "not found activate file in usb device") //通知外部激活结果
            return
        }

        //检查激活文件
        val activateDir = File(AutoConstant.MAP_ACTIVE_DIR)
        if (!activateDir.exists()) {
            val mkdirs = activateDir.mkdirs()
            Timber.d("mkdirs is $mkdirs")
        } else if (activateDir.isFile) {
            val delete = activateDir.delete()
            val mkdirs = activateDir.mkdirs()
            Timber.d("activateDir.isFile()$delete$mkdirs")
        } else {
            FileUtils.deleteDir(activateDir) //清空目录
        }
        //拷贝激活文件
        val copy = FileUtils.copy(activateFile.absolutePath, AutoConstant.MAP_ACTIVE_DIR + "ReLoginCode.csv")
        _isActivating.postValue(false)
        Timber.d("copy $copy")
        if (copy != 0) {
            if (!netWorkManager.isNetworkConnected()) {
                noNet() //没有网络提示
            } else {
                failType.postValue("拷贝激活文件失败，请重试！")
                failTip.postValue("设备号 " + carInfo.uuid)
            }
            notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_USB, "activate file copy fail") //通知外部激活结果
            return
        }
        sdkActivateCode = activateController.bathActivate() //批量激活
        Timber.d("sdkActivateCode is $sdkActivateCode")
        if (sdkActivateCode != Service.ErrorCodeOK) {
            if (!netWorkManager.isNetworkConnected()) {
                noNet() //没有网络提示
            } else {
                failType.postValue(ActiveErrorUtils.activeErrorMessage(sdkActivateCode) + ", 请检查激活文件是否正确！")
                failTip.postValue("设备号 " + carInfo.uuid)
            }
        }
        _activateResult.postValue(if (sdkActivateCode == Service.ErrorCodeOK) 0 else 3) //激活结果 0.成功 1.手动激活失败 2.网络激活失败 3.usb激活失败
        refreshLayout() //通知刷新UI
        notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_USB, ActiveErrorUtils.activeErrorMessage(sdkActivateCode)) //通知外部激活结果
        if (isActivate()) {
            _postActivateResult.postValue(true)
        }
    }

    /**
     * 手动激活
     *
     * @return code
     */
    fun manualActivate(szSerialNumber: String?, szActivateCode: String?): Int {
        _isActivating.postValue(true)
        val activateReturnParam = activateController.manualActivate(szSerialNumber, szActivateCode)
        Timber.i("manualActivate code activeErrorMessage:${ActiveErrorUtils.activeErrorMessage(activateReturnParam.iErrorCode)} szOutputCode:${activateReturnParam.szOutputCode}")
        _isActivating.postValue(false)
        val code = activateReturnParam.iErrorCode
        Timber.i("manualActivate code code:$code")
        if (Service.ErrorCodeOK == code) {
            sdkActivateCode = Service.ErrorCodeOK
            _activateResult.postValue(0) //激活结果 0.成功 1.手动激活失败 2.网络激活失败 3.usb激活失败
            notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_MANUAL, "手动激活成功") //通知外部激活结果
            _postActivateResult.postValue(true)
        } else {
            sdkActivateCode = code
            _activateResult.postValue(1) //激活结果 0.成功 1.手动激活失败 2.网络激活失败 3.usb激活失败
            if (!netWorkManager.isNetworkConnected()) {
                noNet() //没有网络提示
            } else {
                failType.postValue("手动激活失败（" + ActiveErrorUtils.activeErrorMessage(sdkActivateCode) + "）")
                failTip.postValue("设备号 " + carInfo.uuid)
            }
            notifyActivateResultListener(BaseConstant.AUTO_ACTIVATE_TYPE_MANUAL, ActiveErrorUtils.activeErrorMessage(sdkActivateCode)) //通知外部激活结果
        }
        refreshLayout() //通知刷新UI
        return code
    }

    /**
     * 停止激活
     */
    fun stopActivate() {
        _isActivating.postValue(false)
        Timber.i("stopActivate")
    }

    /**
     * 获取引擎版本号码
     */
    fun getMapEngineVersion(): String {
        val version = ServiceMgr.getEngineVersion()
        Timber.i("getMapEngineVersion $version")
        return "引擎版本: $version"
    }


    fun refreshLayout() {
        Timber.d("refreshLayout isActivate %s ,isAgreement %s ,isOpenAgreement %s", isActivate(), isAgreement(), isOpenAgreement)
        val activate = isActivate()
        val agreement = isAgreement()
        _isShowActivateLayout.postValue(!activate)
        _isShowAgreementLayout.postValue(!isOpenAgreement && activate && !agreement)
    }

    private suspend fun doPostRequest(params: BaseRequestBody) {
        retrofitRepository.requestActive(params).collect {
            Timber.d("doPostRequest ${gson.toJson(it)}")
        }
    }

    private fun notifyActivateResultListener(type: Int, message: String) {
        if (mActivateResultListener != null) {
            Timber.d("notifyActivateResultListener %s %s %s ", isActivate(), type, message)
            mActivateResultListener!!.activateResult(type, isActivate(), message)
        }
    }

    fun registerActivateResultListener(activateResultListener: IActivateResultListener) {
        Timber.d("registerActivateResultListener")
        mActivateResultListener = activateResultListener
    }

    fun unregisterActivateResultListener() {
        Timber.d("unregisterActivateResultListener")
        mActivateResultListener = null
    }


    interface IActivateResultListener {
        fun activateResult(type: Int, flag: Boolean, msg: String?)
    }

}