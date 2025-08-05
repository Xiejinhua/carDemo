package com.desaysv.psmap.base.impl

import com.desaysv.psmap.base.def.MapModeType

interface VehicleInfoCallback {
    /**
     * 电源状态变化通知
     */
    fun powerStatusChange(accOn: Boolean) {

    }

    /**
     * 注册大灯状态变化通知
     */
    fun lightStatusChange(status: Boolean) {

    }

    /**
     * 恢复出厂设置
     */
    fun factoryResetNotify(status: Boolean) {

    }

    /**
     * 低油量通知
     */
    fun lowFuelWarningNotify(status: Boolean) {

    }

    fun dashboardMapDisplayStatus(status: CarDashboardStatus) {

    }

    fun dashboardMapModeState(@MapModeType mode: Int) {

    }

    fun onCarDashboardTheme(theme: Int) {

    }
}