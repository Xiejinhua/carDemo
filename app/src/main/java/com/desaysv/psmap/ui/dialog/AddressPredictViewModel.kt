package com.desaysv.psmap.ui.dialog

import androidx.lifecycle.ViewModel
import com.autonavi.gbl.aosclient.model.GAddressPredictResponseParam
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 家/公司预测位置ViewModel
 */
@HiltViewModel
class AddressPredictViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val skyBoxBusiness: SkyBoxBusiness
) : ViewModel() {
    val predictType = settingAccountBusiness.predictType //类型 1.家 2.公司
    val predictAddress = settingAccountBusiness.predictAddress //地址
    val predictPoi = settingAccountBusiness.predictPoi //预测地址转成POI
    val isNight = skyBoxBusiness.themeChange()

    //设置 预测用户家/公司的位置 数据
    fun initData(aAddressPredictResponseParam: GAddressPredictResponseParam?) {
        settingAccountBusiness.setAddressPredictData(aAddressPredictResponseParam)
    }
}