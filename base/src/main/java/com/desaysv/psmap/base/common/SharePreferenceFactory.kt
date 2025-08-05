package com.desaysv.psmap.base.common

import android.content.Context
import android.text.TextUtils
import com.autosdk.bussiness.common.utils.FileUtils
import com.autosdk.common.AutoConstant
import com.autosdk.common.storage.MapSharePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharePreferenceFactory @Inject constructor(@ApplicationContext context: Context) {
    private val mContext: Context = context
    private val mSpList =
        ConcurrentHashMap<MapSharePreference.SharePreferenceName, MapSharePreference>()

    fun getMapSharePreference(name: MapSharePreference.SharePreferenceName): MapSharePreference {
        if (mSpList.containsKey(name)) {
            return mSpList[name]!!
        }
        mSpList[name] = MapSharePreference(mContext, name)
        return mSpList[name]!!
    }

    fun getThemePath(): String {
        var path = getMapSharePreference(MapSharePreference.SharePreferenceName.userSetting)
            .getStringValue(MapSharePreference.SharePreferenceKeyEnum.themePath, "")
        if (TextUtils.isEmpty(path)) {
            path =
                if (FileUtils.checkFileExists(AutoConstant.DMAPASSET_DIR)) AutoConstant.DMAPASSET_DIR else AutoConstant.MAPASSET_DIR
        }
        return path
    }
}