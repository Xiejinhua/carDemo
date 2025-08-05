package com.desaysv.psmap.base.utils

import com.autonavi.gbl.util.observer.IJniExceptionObserver
import timber.log.Timber

/**
 * 崩溃堆栈回调通知
 */
class HmiExceptionUtils : IJniExceptionObserver {
    override fun onException(exceptionInfo: String?) {
        Timber.d("onException exceptionInfo:$exceptionInfo")
    }
}
