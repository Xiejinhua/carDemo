package com.desaysv.psmap.ui.agreement

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autosdk.common.AutoStatus
import com.desaysv.psmap.base.business.ActivationMapBusiness
import com.desaysv.psmap.base.business.AutoStatusAdapter
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.impl.ICarInfoProxy
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 高德鲸图ViewModel
 */
@HiltViewModel
class AgreementViewModel @Inject constructor(
    private val activationMapBusiness: ActivationMapBusiness,
    private val skyBoxBusiness: SkyBoxBusiness,
    private val carInfoProxy: ICarInfoProxy
) : ViewModel() {
    val checkState = MutableLiveData(true) //协议勾选按钮状态
    val isNight = skyBoxBusiness.themeChange()
    val screenStatus = carInfoProxy.getScreenStatus()

    fun setCheckState(check: Boolean) {
        checkState.postValue(check)
    }

    /**
     * 同意协议
     */
    fun doAgreementYes(noMoreTips: Boolean) {
        activationMapBusiness.doAgreementYes(noMoreTips)
        activationMapBusiness.refreshLayout()
        AutoStatusAdapter.sendStatus(AutoStatus.AGREE_AGREEMENT)
    }
}