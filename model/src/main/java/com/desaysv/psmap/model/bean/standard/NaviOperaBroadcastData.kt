package com.desaysv.psmap.model.bean.standard

/**
 * 导航播报模式设置
 */
data class NaviOperaBroadcastData(val naviBroadcastType: Int) : ResponseDataData() //1：简洁播报 2：详细播报 3：极简播报（V650开始⽀持）
