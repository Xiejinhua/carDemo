package com.desaysv.psmap.model.bean.standard

/**
 * 搜索结果列表状态透出 （80034）应答类
 */
data class SearchResultListChangeData(
    val poinum: Int = 10, //0，1，2, 3 列表POI个数
    val isFirstPage: Boolean, //是否是第⼀⻚ true：是第⼀⻚ false：不是第⼀⻚
    val isLastPage: Boolean, //是否是最后⼀⻚ true：是最后⼀⻚ false：不是最后⼀⻚
    val choice: Int = -1, //⽤户⼿动POI结果选择,最多⼗项，索引从0开始,-1为没有⼿动选择操作
    val planRoute: Boolean = false, //⽤户⼿动POI结果选择,最多⼗项，索引从0开始,-1为没有⼿动选择操作
    val back: Boolean = false, //是否点击返回按键 true:点击返回 false:未点击
    val isListTop: Boolean = false, //是否处于列表顶部即第⼀条 true:处于列表顶部 false:未处于列表顶部
    val isListBottom: Boolean = false, //是否处于列表底部即最后⼀条 true:处于列表底部 false:未处于列表底部
    val curPage: Int = 1, //当前⻚数，从1开始
    val totalPage: Int = 1, //总⻚数
    val poiResult: PoiResult? = null //Poi结果信息
) : ResponseDataData()

