package com.desaysv.psmap.model.bean

/**
 * 轨迹路书list bean
 */
data class MineTankListBean(
    var code: Int = 0,
    var msg: String? = null,
    var data: MineTankData? = null
)

// Meta Class
data class TankMeta(
    var currentPage: Int = 0,
    var maxPage: Int = 0,
    var itemCount: Int = 0,
    var pageSize: Int = 0
)

// List Class
data class MineTankList(
    var id: Int = 0,
    var caption: String? = null,
    var logo: String? = null,
    var distance: Int = 0,
    var time: String = "",
    var finished: Int = 1,
    var detailUrl: String? = null,
    var isFav: Boolean = false
)

// Data Class
data class MineTankData(
    var meta: TankMeta? = null,
    var list: List<MineTankList>? = null
)
