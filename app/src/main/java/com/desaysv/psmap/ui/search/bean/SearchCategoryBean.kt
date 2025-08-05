package com.desaysv.psmap.ui.search.bean

/**
 * @author 张楠
 * @date 2024-01-25
 * @project：个性化地图——搜索子分类bean
 */
data class SearchCategoryBean(
    var name: String,
    var category: String? = null,
    var imgDay: Int = 0,
    var imgNight: Int = 0
)
