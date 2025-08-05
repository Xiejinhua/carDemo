package com.desaysv.psmap.model.car.dashboard

/**
 * @author ZZP
 * @description 红绿灯状态信息
 * @time 2025/5/23
 */
data class VDNaviLightStateInfoT1N(
    var TrafficLightIcon: Int = 0,//红绿灯类型
    var TrafficLightSecond: Int = 0,//红绿灯倒计时
    var TrafficLightRounds: Int = 0,//红绿灯轮次
) {
    fun clear() {
        TrafficLightIcon = 0
        TrafficLightSecond = 0
        TrafficLightRounds = 0
    }

    override fun toString(): String {
        return "TrafficLightIcon=$TrafficLightIcon, TrafficLightSecond=$TrafficLightSecond, TrafficLightRounds=$TrafficLightRounds"
    }
}