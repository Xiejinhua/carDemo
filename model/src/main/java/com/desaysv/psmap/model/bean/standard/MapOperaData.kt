package com.desaysv.psmap.model.bean.standard

data class MapOperaData(
    /**
     * 视图设置： 0：2D⻋头向上 1：2D正北朝上 2：3D⻋头向上 3：视图轮流切换
     * 路况操作： 0：开启路况 1：关闭路况 缩放操作： 0：放⼤地图 1：缩⼩地图
     * 2：最⼤⽐例尺 3：最⼩⽐例尺
     */
    val operaType: Int = -1,

    /**
     * 地图操作类型 ： 0：实时路况 1：缩放地图 2：视图设置
     */
    val actionType: Int = -1,

    /**
     * 地图缩放倍数（3～19）
     */
    val operaValue: Int = 0,

    /**
     * 预留字段
     */
    val tempValue: String = "",
) : ResponseDataData()