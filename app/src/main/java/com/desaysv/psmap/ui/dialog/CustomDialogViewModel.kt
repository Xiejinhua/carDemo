package com.desaysv.psmap.ui.dialog

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.desaysv.psmap.base.business.SkyBoxBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 通用对话框ViewModel
 */
@HiltViewModel
class CustomDialogViewModel @Inject constructor(private val skyBoxBusiness: SkyBoxBusiness) : ViewModel() {
    val title = MutableLiveData("")
    val showTitle = title.map {
        !TextUtils.isEmpty(it)
    }
    val content = MutableLiveData("")
    val cancel = MutableLiveData("")
    val confirm = MutableLiveData("")
    val know = MutableLiveData("")
    val showDoubleBtn = know.map {
        TextUtils.isEmpty(it)
    }
    val showCloseBtn = MutableLiveData(false)
    val isMoreLine = MutableLiveData(false)
    val isNight = skyBoxBusiness.themeChange()

    fun initData(mTitle: String, mContent: String, mConfirm: String, mCancel: String, mKnow: String, showClose: Boolean, moreLine: Boolean = false) {
        title.postValue(mTitle)
        content.postValue(mContent)
        confirm.postValue(mConfirm)
        cancel.postValue(mCancel)
        know.postValue(mKnow)
        showCloseBtn.postValue(showClose)
        isMoreLine.postValue(moreLine)
    }
}