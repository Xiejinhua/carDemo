package com.desaysv.psmap.base.impl

interface AudioFocusManager {
    fun addFocusChangedToPlayerOperation(onFocusChangedToPlayerOperation: OnFocusChangedToPlayerOperation)

    fun removeFocusChangedToPlayerOperation()

    fun requestAudioFocus(): Boolean

    fun abandonAudioFocus()

    fun hasFocus(): Boolean

    fun audioFocusStateDelayed(): Boolean

    fun resetAudioFocusState()
}