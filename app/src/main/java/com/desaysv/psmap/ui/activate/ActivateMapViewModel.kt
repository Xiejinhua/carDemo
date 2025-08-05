package com.desaysv.psmap.ui.activate

import android.app.Application
import android.provider.Settings
import android.text.TextUtils
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.base.business.ActivationMapBusiness
import com.desaysv.psmap.base.business.InitSDKBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 激活ViewModel
 */
@HiltViewModel
class ActivateMapViewModel @Inject constructor(
    private val application: Application,
    private val activationMapBusiness: ActivationMapBusiness,
    private val initSDKBusiness: InitSDKBusiness,
    private val netWorkManager: NetWorkManager,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val carInfoProxy: ICarInfoProxy
) : ViewModel() {
    private val isShowActivateLayout = activationMapBusiness.isShowActivateLayout
    private val isShowAgreementLayout = activationMapBusiness.isShowAgreementLayout
    val activateResult = activationMapBusiness.activateResult
    val isActivating = activationMapBusiness.isActivating
    val activeLayoutType: MutableLiveData<Int> = MutableLiveData<Int>(2) //0.手动激活 1.激活成功 2.激活失败 3.激活方式选择界面--默认
    val uuidNumber: MutableLiveData<String> = MutableLiveData<String>("")
    val engineVersion: MutableLiveData<String> = MutableLiveData<String>("")
    val showSerialNumber = MutableLiveData(true) //手动激活--是否显示序列号布局true.显示 false.不显示
    val serialNumberInput = MutableLiveData("") //序列号输入监听
    val activeCodeInput = MutableLiveData("") //激活码界面输入监听
    val failType = activationMapBusiness.failType //失败类型
    val failTip = activationMapBusiness.failTip //失败提示
    val isNight = skyBoxBusiness.themeChange()
    val screenStatus = carInfoProxy.getScreenStatus()

    val showActivateAgreement = MediatorLiveData<Pair<Boolean, Boolean>>().apply {
        addSource(isShowActivateLayout) { isShowActivate ->
            val isShowAgreement = isShowAgreementLayout.value ?: false
            value = Pair(isShowActivate, isShowAgreement)
        }
        addSource(isShowAgreementLayout) { isShowAgreement ->
            val isShowActivate = isShowActivateLayout.value ?: false
            value = Pair(isShowActivate, isShowAgreement)
        }
    }

    init {
        updateUUIDNumber() //显示UUID
        updateEngineVersion() //显示版本号
        if (!netWorkManager.isNetworkConnected()) {
            activationMapBusiness.noNet() //没有网络提示
        }
    }

    //重新进入激活界面，判断是否需要重新初始化
    fun reInit() {
        Timber.d(" reInit ")
        if (!activationMapBusiness.isActivate()) { //未激活
            activationMapBusiness.unActiveInit()
            activationMapBusiness.dataInit()
            activationMapBusiness.refreshLayout()
            if (activationMapBusiness.isActivate() && !initSDKBusiness.isInitSuccess()) {
                if (CommonUtils.isVehicle()) {
                    Settings.Global.putInt(application.contentResolver, BaseConstant.KEY_GLOBAL_ACTIVATE_FLAG, BaseConstant.GLOBAL_ACTIVATE_YES) //已激活
                }
                initSDKBusiness.activatedInitMap(true)
                Timber.d(" reInit initMapServiceSDK")
            }
        }

    }

    /**
     * 网络激活
     */
    fun netActivate() {
        Timber.d("netActivate")
        activationMapBusiness.netActivate()
    }

    /**
     * 手动激活
     * @param serialNumber
     * @param activateCode
     */
    fun manualActivate(serialNumber: String, activateCode: String) {
        Timber.d("manualActivate $serialNumber activateCode:$activateCode")
        if (TextUtils.isEmpty(serialNumber) || TextUtils.isEmpty(activateCode)) {
            Timber.d("manualActivate 序列号或者激活码为空")
            return
        }
        activationMapBusiness.manualActivate(serialNumber, activateCode)
    }

    //显示UUID
    fun updateUUIDNumber() {
        uuidNumber.postValue(carInfoProxy.uuid ?: "")
    }

    //显示版本号
    private fun updateEngineVersion() {
        engineVersion.postValue(activationMapBusiness.getMapEngineVersion())
    }

    fun stopActivate() {
        Timber.d("stopActivate");
        activationMapBusiness.stopActivate()
    }
}