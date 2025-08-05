package com.desaysv.psmap.ui.settings

import androidx.annotation.IntDef

/**
 * 跳转设置子fragment时需要要设置的标签
 * @date 2024/1/18
 */
@IntDef(*[SettingTab.NAVI, SettingTab.BROADCAST, SettingTab.MAP_SETTING, SettingTab.OTHER_SETTING])
annotation class SettingTab {
    companion object {
        /**
         * 导航设置界面
         */
        const val NAVI = 0

        /**
         * 播报界面
         */
        const val BROADCAST = 1

        /**
         * 地图设置界面
         */
        const val MAP_SETTING = 2

        /**
         * 其他设置界面
         */
        const val OTHER_SETTING = 3
    }
}
