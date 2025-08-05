package com.desaysv.psmap.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 设置主界面ViewModel
 */
@HiltViewModel
class SettingMainViewModel @Inject constructor() : ViewModel() {
    val tabSelect = MutableLiveData(0) //0：导航
}