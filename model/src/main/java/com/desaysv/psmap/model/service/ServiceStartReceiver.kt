package com.desaysv.psmap.model.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Author : wangmansheng
 * Date : 2024-1-11
 * Description : 接收开机广播
 */
@AndroidEntryPoint
class ServiceStartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Timber.d(intent.action)
        if (TextUtils.equals("android.intent.action.BOOT_COMPLETED", intent.action)) {
            try {
                if (MapService.instance == null) {
                    val service = Intent(context, MapService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(service)
                    } else {
                        context.startService(service)
                    }
                }
            } catch (e: Exception) {
                Timber.e(" exception %s", e.toString())
            }
        }
    }
}