package com.desaysv.psmap.base.def

import androidx.annotation.IntDef

/**
 * 初始化SDK状态
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@IntDef(
    InitSdkResultType.INIT,
    InitSdkResultType.FAIL,
    InitSdkResultType.OK,
    InitSdkResultType.FAIL_AAR_OVERDUE,
    InitSdkResultType.FAIL_ACTIVATE,
    InitSdkResultType.FAIL_INIT_ACTIVATE
)
annotation class InitSdkResultType {
    companion object {
        /**
         * 初始化状态
         */
        const val INIT = -99

        /**
         *初始化失败
         */
        const val FAIL = -1

        /**
         * 初始化成功
         */
        const val OK = 0

        /**
         *sdk aar过期
         */
        const val FAIL_AAR_OVERDUE = 1

        /**
         *激活失败
         */
        const val FAIL_ACTIVATE = 2

        /**
         *激活初始化失败
         */
        const val FAIL_INIT_ACTIVATE = 3
    }

}

/**
 * 初始化步骤
 */
/*
enum class InitSdkStep {
    CHECK_PERMISSION,
    CHECK_AAR,
    INIT_SDK_BASE,
    CHECK_ACTIVATE,
    INIT_SDK,
    INIT_SDK_SERVICE,
    INIT_SDK_MAP,
    INIT_FINISHED,
}*/
