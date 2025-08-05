package com.desaysv.psmap.model.bean.iflytek

data class MapStatusInfoIFlyTek(
    //屏幕ID，D01项目中控屏是0，吸顶屏是5，各应用需根据实际显示的屏幕传值
    val displayId: String = "0",
    val service: String = "mapU",
    //poiSearch和navi两个场景，分别表示 poi 搜索场景和导 航（包含路线规划）场景
    var scene: String = "poiSearch",
    val state: State
)