package com.desaysv.psmap.ui.ahatrip

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.base.impl.AhaTripImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 我的路书&收藏主页ViewModel
 */
@HiltViewModel
class MyAhaMainViewModel @Inject constructor(private val ahaTripImpl: AhaTripImpl): ViewModel() {
    val tabSelect = MutableLiveData(0) //0：我的路书 1：我的收藏

    //判断当前是否是登录状态 true 登录 false 未登录
    fun isLogin(): Boolean {
        return ahaTripImpl.isLogin()
    }
}