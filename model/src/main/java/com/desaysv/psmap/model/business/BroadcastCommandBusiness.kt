package com.desaysv.psmap.model.business

import android.content.Intent
import android.text.TextUtils
import com.autosdk.adapter.AdapterConstants.EXTRA_DLAT
import com.autosdk.adapter.AdapterConstants.EXTRA_DLON
import com.autosdk.adapter.AdapterConstants.EXTRA_DNAME
import com.autosdk.adapter.AdapterConstants.KEYWORDS
import com.autosdk.adapter.AdapterConstants.KEY_TYPE
import com.autosdk.adapter.AdapterConstants.SOURCE_APP
import com.desaysv.psmap.model.di.ProvidesHiltModule
import com.desaysv.psmap.model.impl.IMapCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author 张楠
 * @time 2025/03/05
 * @description 处理外部应用的广播命令
 */
@Singleton
class BroadcastCommandBusiness @Inject constructor(
    @ProvidesHiltModule.DefaultMapCommand private val mapCommand: IMapCommand
) {
    private val ioScope = CoroutineScope(Dispatchers.IO + Job())

    fun parseCommand(intent: Intent) {
        Timber.d("parseCommand() called with: intent = $intent")
        val keyType = intent.getIntExtra(KEY_TYPE, 0)
        Timber.d("parseCommand() called with: keyType = $keyType")
        when (keyType) {
            10036 -> {
                //可以使用包名做白名单过过滤，待后续开发
                val sourceApp = intent.getStringExtra(SOURCE_APP)

                val keyWord = intent.getStringExtra(KEYWORDS)
                if (keyWord != null && !TextUtils.isEmpty(keyWord)) {
                    mapCommand.keywordSearch(keyWord)
                }
            }

            10007 -> {
                //可以使用包名做白名单过过滤，待后续开发
                val sourceApp = intent.getStringExtra(SOURCE_APP)

                val lat = intent.getDoubleExtra(EXTRA_DLAT, 0.0)
                val lon = intent.getDoubleExtra(EXTRA_DLON, 0.0)
                val name = intent.getStringExtra(EXTRA_DNAME)
                Timber.d("parseCommand: lat = $lat, lon = $lon, name = $name")
                if (lat != 0.0 && lon != 0.0 && name != null && !TextUtils.isEmpty(name)) {
                    mapCommand.startPlanRoute(name, lon, lat)
                }
            }
        }
    }
}