package com.desaysv.psmap.ui.settings.vehicle

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.ISettingComponent
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.PlateNumberUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 我的车辆ViewModel
 */
@HiltViewModel
class VehicleViewModel @Inject constructor(
    private val settingComponent: ISettingComponent,
    private val sharePreferenceFactory: SharePreferenceFactory,
    private val settingAccountBusiness: SettingAccountBusiness
) : ViewModel() {
    val clickVehicleEnable = MutableLiveData(false) //添加车牌按钮是否可点击
    val limitEnable = MutableLiveData(false) //限行开关选择状态
    val limitChecked = MutableLiveData(false) //限行开关选择状态
    val isNight = MutableLiveData(NightModeGlobal.isNightMode()) //是否是黑夜模式
    val loginLoading: LiveData<Int> = settingAccountBusiness.loginLoading //1:登录中 2:登录成功 3:登录失败
    val setToast = MutableLiveData<String>()
    val hideSoftInput = MutableLiveData<Boolean>()
    val showData = MutableLiveData<Boolean>()

    var vehicleNum: String? = null //车牌号

    //获取车牌号和限行数据
    fun getVehicleNumberLimit() {
        vehicleNum = settingComponent.getConfigKeyPlateNumber()
        val isEmptyNumber = !TextUtils.isEmpty(vehicleNum)
        Timber.d(" getVehicleNumber isEmptyNumber:$isEmptyNumber vehicleNum:$vehicleNum")
        clickVehicleEnable.postValue(isEmptyNumber);
        limitEnable.postValue(isEmptyNumber)
        limitChecked.postValue(isEmptyNumber && settingComponent.getConfigKeyAvoidLimit())
    }

    //限行开关操作
    fun limitOperation(isChecked: Boolean) {
        settingComponent.setConfigKeyAvoidLimit(if (isChecked) 1 else 0)
        limitChecked.postValue(isChecked)
    }

    //添加车牌号
    fun doVehicle() {
        if (TextUtils.isEmpty(vehicleNum)) {
            setToast.postValue("车牌号不能为空")
            return
        }
        if (PlateNumberUtil.isNumeric(vehicleNum!!)) {
            setToast.postValue("车牌号不能以数字开头，请检查后重新输入")
            return
        }
        if (!PlateNumberUtil.isPlateNumber(vehicleNum!!)) {
            setToast.postValue("车牌号无效，请重新输入")
            return
        }
        savePlateNumber(vehicleNum!!);//保存车牌号
        limitEnable.postValue(true)
        setToast.postValue("车牌号设置成功")
    }

    //保存车牌号
    fun savePlateNumber(num: String) {
        settingComponent.setConfigKeyPlateNumber(num)
    }

}