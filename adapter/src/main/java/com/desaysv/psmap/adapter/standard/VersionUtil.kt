package com.desaysv.psmap.adapter.standard

import android.util.Log
import org.json.JSONObject

object VersionUtil {
    const val CLIENT_VERSION: String = "1.0.0.20250307"
    private var serverVersion: String = ""

    fun getServerVersion(): String {
        return serverVersion
    }

    fun dealVersionCallback(json: JSONObject) {
        val data = json.get("data") as JSONObject
        val serverVersion = data.getString("versionName")
        VersionUtil.serverVersion = serverVersion
        val clientVersionParts = CLIENT_VERSION.split(".")
        val serverVersionParts = serverVersion.split(".")
        Log.w("VersionUtil", "clientVersionParts=$clientVersionParts serverVersionParts=$serverVersionParts")
        if (clientVersionParts.size != 4 || serverVersionParts.size != 4) {
            Log.e("VersionUtil", "dealVersion Version format error!")
        } else {
            if (serverVersionParts[0].toInt() != clientVersionParts[0].toInt()) {
                Log.e("VersionUtil", "dealVersion major 1 version inconsistency ")
            }
            if (serverVersionParts[1].toInt() > clientVersionParts[1].toInt()) {
                Log.w("VersionUtil", "dealVersion major 2 version inconsistency")
            }
            if (serverVersionParts[2].toInt() > clientVersionParts[2].toInt()) {
                Log.w("VersionUtil", "dealVersion major 3 version inconsistency")
            }
        }
    }
}