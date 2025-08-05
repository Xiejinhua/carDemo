package com.desaysv.psmap.ui.route.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout

class TouchConstraintLayout : ConstraintLayout {
    private var callBack: OnDispatchTouchEventCallBack? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (null != callBack) {
            callBack!!.dispatchTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    fun setCallBack(callBack: OnDispatchTouchEventCallBack?) {
        this.callBack = callBack
    }

    interface OnDispatchTouchEventCallBack {
        fun dispatchTouchEvent(ev: MotionEvent?)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        callBack = null
    }
}
