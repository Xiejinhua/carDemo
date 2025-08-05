package com.desaysv.psmap.model.bean.standard

/**
 * 搜索结果列表操作 （80023）
 */
data class SearchListOperaData(
    val poiIndex: Int? = null, //0，1，2, 3 ...(POI结果选择,最多⼗项，索引从0开始）
    val pageTurning: Int? = null, //0 : 上⼀⻚ 1 : 下⼀⻚
    val screenTurning: Int? = null, //0 : 上翻屛 1 ： 下翻屛
    val operateType: Int? = null, //操作类型：0查看poi；1去这⾥（路线规划）
) : ResponseDataData()

