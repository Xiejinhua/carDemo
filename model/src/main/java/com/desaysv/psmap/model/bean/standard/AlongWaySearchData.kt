package com.desaysv.psmap.model.bean.standard

/**
 * 沿途搜索 （30302）
 */
data class AlongWaySearchData(
    val requestType: Int? = 0, //请求类型 0:不拉起导航到前台 1:拉起导航到前台
    val alongSearchType: Int //沿途搜类型 1:厕所 2:ATM 3:维修站 4:加油站 5:充电站（329&418及以上版本⽀持） 6:加气站（430及以上版本⽀持） 7:美⻝（430及以上版本⽀持） 8:服务区（沿途搜服务区信息⽀持透出，500及以上版本奥迪新增需求）
)

const val ALONG_SEARCH_TYPE_RESTROOM = 1 //厕所
const val ALONG_SEARCH_TYPE_ATM = 2 //ATM
const val ALONG_SEARCH_TYPE_REPAIR = 3 //维修站
const val ALONG_SEARCH_TYPE_GAS_STATION = 4 //加油站
const val ALONG_SEARCH_TYPE_CHARGING_PILE = 5 //充电站
const val ALONG_SEARCH_TYPE_NATURAL_GAS_STATION = 6 //加气站
const val ALONG_SEARCH_TYPE_RESTAURANT = 7 //美⻝
const val ALONG_SEARCH_TYPE_SERVICE_AREA = 8 //服务区
