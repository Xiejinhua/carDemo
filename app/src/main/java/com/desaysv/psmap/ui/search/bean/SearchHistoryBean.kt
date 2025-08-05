package com.desaysv.psmap.ui.search.bean

import com.autosdk.bussiness.common.POI

/**
 * @author 张楠
 * @date 2024-01-25
 * @project：个性化地图——搜索记录及导航记录bean
 */
data class SearchHistoryBean(
    var type: Int = 0, //类型 1.导航记录 2.搜索记录 3.预搜索 4.搜索途经点中的记录 5.顺路搜预搜索 6.添加（家、公司、组队目的地）点历史记录
    var updateTime: Long,
    var poi: POI?,
    var midPois: ArrayList<POI>? = null, //历史路径中的途经点
    var routeId: String? = null, //历史路线数据ID，删除时使用
    var isFavorite: Boolean = false,  //poi是否收藏
    var isHomeorCompany: Boolean = false  //poi是否是家或公司
)



