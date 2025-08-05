package com.desaysv.psmap.base.tracking

import android.content.Context

interface IEventTrack {
    fun init(context: Context)
    suspend fun trackEvent(eventName: EventTrackingUtils.EventName, params: Map<EventTrackingUtils.EventValueName, Any>)
}