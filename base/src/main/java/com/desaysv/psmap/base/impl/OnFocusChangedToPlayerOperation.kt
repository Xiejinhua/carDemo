package com.desaysv.psmap.base.impl

interface OnFocusChangedToPlayerOperation {
    fun isPlaying(): Boolean

    fun onPlay()

    fun onPause()
}