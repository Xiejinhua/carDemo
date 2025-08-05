package com.desaysv.psmap.base.bean


/**
 * @author 谢锦华
 * @time 2024/2/19
 * @description 光柱图数据类
 */

data class TmcModelInfoBean(
    val tmcTotalDistance: Long = 0,
    val tmcRestDistance: Long = 0,
    val lightBarItems: ArrayList<MapLightBarItem>? = null
)
