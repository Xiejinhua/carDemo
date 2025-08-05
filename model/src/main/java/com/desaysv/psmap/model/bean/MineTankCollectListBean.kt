package com.desaysv.psmap.model.bean

/**
 * 收藏轨迹路书list bean
 */
data class MineTankCollectListBean(
    var code: Int = 0,
    var msg: String? = null,
    var data: MineTankCollectData? = null,
    var traceId: String? = null
)

data class MineTankCollectData(
    var meta: MineTankCollectMeta? = null,
    var list: List<TankCollectItem>? = null
)

data class TankCollectItem(
    var id: Int = 0,
    var uid: Int = 0,
    var nickname: String = "",
    var avatar: String = "",
    var caption: String = "",
    var logo: String = "",
    var time: String = "",
    var startCity: String = "",
    var endCity: String = "",
    var distance: String = "",
    var detailUrl: String = ""
)


data class MineTankCollectMeta(
    var currentPage: Int = 0,
    var maxPage: Int = 0,
    var itemCount: Int = 0,
    var pageSize: Int = 0
)
