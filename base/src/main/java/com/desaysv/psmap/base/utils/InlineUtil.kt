package com.desaysv.psmap.base.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import timber.log.Timber

/**
 *@author uidq3334
 *@emily weilon.huang@foxmail.com
 *@date 2023/1/17
 */

/**
 * 过滤倒灌数据
 */
fun <X> LiveData<X>.unPeek(): LiveData<X> {
    var isFirst = true
    val outputLiveData = MediatorLiveData<X>()
    val isNotSet = CommonUtils.getPrivate<Int>(this, "mVersion") == -1
    outputLiveData.addSource(this) { currentValue ->
        try {
            if (isFirst) {
                isFirst = false
                if (isNotSet) {
                    outputLiveData.value = currentValue
                }
            } else {
                outputLiveData.value = currentValue
            }
        } catch (e: Exception) {
            Timber.i("outputLiveData e:${e.message}")
        }
    }
    return outputLiveData
}