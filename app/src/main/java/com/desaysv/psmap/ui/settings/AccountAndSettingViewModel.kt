package com.desaysv.psmap.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.NavigationSettingBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 个人中心&设置界面ViewModel
 */
@HiltViewModel
class AccountAndSettingViewModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val navigationSettingBusiness: NavigationSettingBusiness,
    @ProvidesHiltModule.DefaultMapCommand private val mapCommandImpl: IMapCommand
) : ViewModel() {
    val showMoreInfo = settingAccountBusiness.showMoreInfo //显示提示文言
    val isMapSetting = navigationSettingBusiness.isMapSetting ////是否在地图设置设置界面
    val tabSelect = MutableLiveData(-1) //0：个人中心 1：设置

    //外部指令
    val mapCommand = mapCommandImpl.getMapCommand()

    //设置提示文言
    fun setShowMoreInfo(moreInfo: MoreInfoBean) {
        settingAccountBusiness.setShowMoreInfo(moreInfo)
    }

    fun notifyMapCommandResult(type: MapCommandType, result: Any?) {
        mapCommandImpl.notifyMapCommandResult(type, result)
    }
}