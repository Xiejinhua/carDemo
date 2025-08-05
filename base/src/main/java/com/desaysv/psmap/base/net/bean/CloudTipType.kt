package com.desaysv.psmap.base.net.bean

enum class CloudTipType {
    /**
     * 无
     */
    NONE,

    /**
     * 限行
     */
    TYPE_RESTRICT,

    /**
     * 道路关闭 封路
     */
    TYPE_ROAD_CLOSE,

    /**
     * 避开拥堵
     */
    TYPE_AVOID_JAM,

    /**
     * 禁行
     */
    TYPE_FORBIDDEN,

    /**
     * 节假日
     */
    TYPE_HOLIDAY,

    /**
     * 小路提醒
     */
    TYPE_NARROW,

    /**
     * 营业时间提醒
     */
    TYPE_BUSINESS_HOURS
}
