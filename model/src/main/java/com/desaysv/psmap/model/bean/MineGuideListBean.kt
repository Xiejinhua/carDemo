package com.desaysv.psmap.model.bean

/**
 * 共创路书list bean
 */
data class MineGuideListBean(
    var code: Int = 0,
    var msg: String? = null,
    var data: MineGuideData? = null
)

// Meta Class
data class Meta(
    var currentPage: Int = 0,
    var maxPage: Int = 0,
    var itemCount: Int = 0,
    var pageSize: Int = 0
)

// List Class
data class MineGuideList(
    var id: Int = 0,
    var caption: String? = null,
    var logo: String? = null,
    var startCity: String? = null,
    var endCity: String? = null,
    var totalDay: Int = 0,
    var unfinished: Boolean = false,
    var distanceInMeters: Int = 0,
    var hasVideo: Boolean = false,
    var editUrl: String? = null, // 修改为 String 类型，因为 JSON 中通常是字符串
    var isFav: Boolean = false
)

// Data Class
data class MineGuideData(
    var meta: Meta? = null,
    var list: List<MineGuideList>? = null
)
