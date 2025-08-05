package com.desaysv.psmap.adapter.tbt.bean


/**
 * @author 谢锦华
 * @time 2024/2/19
 * @description 车道线数据类
 */

data class TBTLaneInfoBean(
    var isTollBoothsLane: Boolean = false, // 是否为收费站车道  true：收费站车道  false：普通车道
    var isRecommend: Boolean = false, // 是否为推荐车道
    var laneAction: Int = -1, // 设置车道线图标
)
