package com.desaysv.psmap.base.data

import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author 谢锦华
 * @time 2024/2/1
 * @description
 */

@Singleton
class NaviRepository @Inject constructor(
    private val gson: Gson,
    private val mNaviBusiness: NaviBusiness,
    private val mRouteBusiness: RouteBusiness
) :
    INaviRepository {
    /**
     * 是否为真实导航
     */
    override fun isRealNavi(): Boolean {
        return mNaviBusiness.isRealNavi()
    }

    /**
     * 是否为模拟导航
     */
    override fun isSimulationNavi(): Boolean {
        return mNaviBusiness.isSimulationNavi()
    }

    /**
     * 判断是否正在导航中
     */
    override fun isNavigating(): Boolean {
        return mNaviBusiness.isNavigating()
    }

    /**
     * 判断是否在高速
     */
    override fun isHighWay(): Boolean {
        return mNaviBusiness.isHighWay()
    }

    /**
     * 判断是否路线规划中
     */
    override fun isPlanRouteing(): Boolean {
        return mRouteBusiness.isPlanRouteing()
    }

    /**
     * 结束导航
     */
    override fun stopNavi() {
        return mNaviBusiness.stopNavi()
    }

    override fun getNaviStatus(): Int {
        return mNaviBusiness.naviStatus.value ?: BaseConstant.NAVI_STATE_INIT_NAVI_STOP
    }

}