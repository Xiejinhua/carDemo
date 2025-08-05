package com.desaysv.psmap.model.bean.standard

data class MapStateInfoData(
    /**
     * 地图状态信息类型 0：地图视⻆ 1：导航所在⻚⾯ 2：导航前后台查询 3：
     * mqtt初始化状态查询 4：AR中控状态查询 5：AR仪表状态查询 6：导航状态
     * 查询 7：运⾏状态查询 8:当前地图⽐例尺等级(500及以上版本⽀持)
     *
     */
    var stateType: Int,

    /**
     * 具体状态值 -1：⾮法值 当stateType=0地图视⻆时； stateValue的值 0：3D
     * 视⻆，1：2D北⾸上，2：2D⻋⾸上 当stateType=1导航所在⻚⾯时
     * stateValue的值 3：路线规划⻚ 当stateType=2导航前后台查询 stateValue的
     * 值 0：导航在后台，1：导航在lue的值，0：⾮AR状态 1：AR巡航（仪表）2：
     * AR导航（仪表）前台 当stateType=3mqtt初始化状态查询时，stateValue的值
     * 0：未完成，1：已完成 当stateType=4 AR中控状态查询时，stateValue的值
     * 0：⾮AR状态，1：AR巡航（功能暂未⽀持，先预留字段） 2：AR导航 当
     * stateType=5 AR仪表状态查询时，stateValue的值 0：⾮AR状态，1：AR巡航
     * 2：AR导航 当stateType=6 导航状态查询时，stateValue的值 0：导航，1：
     * ⾮导航 2：模拟导航 当stateType=7 运⾏状态查询时，stateValue的值 1：⾃
     * 启 2：启动 当stateType=8 运⾏状态查询时, stateValue的值,奥迪项⽬为1-
     * 19(2000km-10m),AUTO项⽬为3-19(1000km-10m) (500及以上版本⽀持)
     *
     */
    var stateValue: Int = -1,
) : ResponseDataData()
