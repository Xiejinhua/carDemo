package com.desaysv.psmap.ui.ahatrip

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 捷途权益和路书首页tab ViewModel
 */
@HiltViewModel
class JetourCustomViewModel @Inject constructor(): ViewModel() {
    val tabSelect = MutableLiveData(0) //0：捷途权益 1：路书
}