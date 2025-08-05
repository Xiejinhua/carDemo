package com.autosdk.common.tts

interface ITTSListener {
    fun onTTSPlayBegin()

    fun onTTSPlayComplete()

    fun onTTSPlayInterrupted()

    fun onTTSPlayError()

    fun onTTSIsPlaying()
}