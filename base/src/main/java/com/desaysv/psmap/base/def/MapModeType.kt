package com.desaysv.psmap.base.def

import androidx.annotation.IntDef
import com.autosdk.common.CommonConfigValue

/**
 * 初始化SDK状态
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@IntDef(
    MapModeType.VISUALMODE_UNKNOW,
    MapModeType.VISUALMODE_2D_CAR,
    MapModeType.VISUALMODE_3D_CAR,
    MapModeType.VISUALMODE_2D_NORTH,
)
annotation class MapModeType {
    companion object {
        const val VISUALMODE_UNKNOW: Int = -1
        const val VISUALMODE_2D_CAR: Int = CommonConfigValue.VISUALMODE_2D_CAR
        const val VISUALMODE_3D_CAR = CommonConfigValue.VISUALMODE_3D_CAR
        const val VISUALMODE_2D_NORTH = CommonConfigValue.VISUALMODE_2D_NORTH
    }

}

