package com.desaysv.psmap.model.utils

import android.os.SystemClock
import android.view.View

/**
 * Author : wangmansheng
 * Date : 2024-1-11
 * Description : 点击事件防抖
 */

fun View.setDebouncedOnClickListener(
    debounceDuration: Long = 500L,
    onClick: (View) -> Unit
) {
    var lastClickTime: Long = 0

    setOnClickListener { view ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime >= debounceDuration) {
            lastClickTime = currentTime
            onClick(view)
        }
    }
}


