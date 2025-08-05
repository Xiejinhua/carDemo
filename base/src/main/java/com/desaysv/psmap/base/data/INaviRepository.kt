package com.desaysv.psmap.base.data


/**
 * @author 谢锦华
 * @time 2024/1/10
 * @description 导航界面提供的接口
 */
interface INaviRepository {
    //是否为真实导航
    fun isRealNavi(): Boolean

    //是否为模拟导航
    fun isSimulationNavi(): Boolean

    //判断是否正在导航中
    fun isNavigating(): Boolean

    //判断是否在高速
    fun isHighWay(): Boolean

    //结束导航
    fun stopNavi()

    //获取导航状态
    fun getNaviStatus(): Int

    //判断是否路线规划中
    fun isPlanRouteing(): Boolean
}
