package com.desaysv.psmap.model.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.autosdk.adapter.AdapterConstants.ACTION
import com.desaysv.psmap.model.business.BroadcastCommandBusiness
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * @author 张楠
 * @time 2025/03/03
 * @description
 */
@AndroidEntryPoint
class AutoNaviBroadcastReceiver(private val broadcastCommandBusiness: BroadcastCommandBusiness) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("AutoNaviBroadcastReceiver onReceive action: ${intent.action}")
        if (TextUtils.equals(ACTION, intent.action)) {
            broadcastCommandBusiness.parseCommand(intent)
        }
    }
}