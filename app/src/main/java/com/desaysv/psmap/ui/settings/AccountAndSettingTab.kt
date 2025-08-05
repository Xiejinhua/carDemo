package com.desaysv.psmap.ui.settings

import androidx.annotation.IntDef

/**
 * 跳转设置子fragment时需要要设置的标签
 * @date 2024/1/18
 */
@IntDef(*[AccountAndSettingTab.ACCOUNT, AccountAndSettingTab.SETTING, AccountAndSettingTab.QR_LOGIN, AccountAndSettingTab.MOBILE_LOGIN])
annotation class AccountAndSettingTab {
    companion object {
        /**
         * 个人中心主界面
         */
        const val ACCOUNT = 1

        /**
         * 导航设置界面
         */
        const val SETTING = 2

        /**
         * 高德二维码界面
         */
        const val QR_LOGIN = 3

        /**
         * 高德手机号码登录界面
         */
        const val MOBILE_LOGIN = 4
    }
}
