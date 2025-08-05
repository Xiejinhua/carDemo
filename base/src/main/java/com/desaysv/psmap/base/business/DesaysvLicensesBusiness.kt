package com.desaysv.psmap.base.business

import com.desaysv.psmap.base.utils.CommonUtils
import com.ivi.Licenses.manager.LicensesManager
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 德赛软件激活业务
 */
@Singleton
class DesaysvLicensesBusiness @Inject constructor() {

    fun getLicensesManager(): LicensesManager = LicensesManager.getInstance() //实例

    private fun getLicenseVersion(): Int {
        return if (CommonUtils.isVehicle()) LicensesManager.getInstance().licenseVersion else 100 //get version  仅获取激活状态
    }

    fun isUsable(): Boolean {
        return if (CommonUtils.isVehicle()) {
            val licenseVersion = getLicenseVersion()
            Timber.i("desaysvLicensesInit licenseVersion: $licenseVersion")
            licenseVersion >= 0
        } else true //是否可用 < 0 不可用
    }
}