package com.desaysv.psmap.base.app

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import android.os.Handler
import timber.log.Timber


class ForegroundCallbacks private constructor() : ActivityLifecycleCallbacks {
    private var isForeground = false
    private var paused = true
    private val handler = Handler()
    private val listeners: MutableList<Listener> = mutableListOf()
    private var check: Runnable? = null
    val CHECK_DELAY: Long = 500

    interface Listener {
        fun onBecameForeground()
        fun onBecameBackground()
    }

    companion object {
        private var instance: ForegroundCallbacks? = null

        fun getInstance(application: Application): ForegroundCallbacks {
            if (instance == null) {
                instance = ForegroundCallbacks()
                application.registerActivityLifecycleCallbacks(instance)
            }
            return instance!!
        }
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    val isBackground: Boolean
        get() = !isForeground

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        //TODO("Not yet implemented")
    }

    override fun onActivityStarted(p0: Activity) {
        //TODO("Not yet implemented")
    }

    override fun onActivityResumed(activity: Activity) {
        paused = false
        val wasBackground = !isForeground
        isForeground = true
        check?.let {
            handler.removeCallbacks(it)
        }
        if (wasBackground) {
            listeners.forEach { listener ->
                try {
                    listener.onBecameForeground()
                } catch (e: Exception) {
                    Timber.i(e.toString())
                }
            }
        }
    }

    override fun onActivityPaused(activity: Activity) {
        paused = true
        check?.let {
            handler.removeCallbacks(it)
        }
        check = Runnable {
            if (isForeground && paused) {
                isForeground = false
                listeners.forEach { listener ->
                    try {
                        listener.onBecameBackground()
                    } catch (e: Exception) {
                        Timber.i(e.toString())
                    }
                }
            }
        }
        handler.postDelayed(check!!, CHECK_DELAY)
    }

    override fun onActivityStopped(p0: Activity) {
        //TODO("Not yet implemented")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        //TODO("Not yet implemented")
    }

    override fun onActivityDestroyed(p0: Activity) {
        //TODO("Not yet implemented")
    }
}


