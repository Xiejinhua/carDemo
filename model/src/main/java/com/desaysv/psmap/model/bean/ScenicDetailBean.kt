package com.desaysv.psmap.model.bean

/**
 * 景点详情bean
 */
class ScenicDetailBean {
    var id: Int = 0
    var caption: String? = null
    var images: List<String>? = null
    var videos: List<String>? = null
    var score: Double = 0.0
    var tag: List<String>? = null
    var opentime: String? = null
    var playinterval: String? = null
    var phone: String? = null
    var distance: Double = 0.0
    var address: String? = null
    var level: String? = null
    var pricemode: Int = 0
    var price: String? = null
    var geo: Geo? = null
    var introduction: String? = null
    var introductionvoice: String? = null
    var fav: Boolean = false
    var canbooking: Boolean = false
    var bookurl: String? = null

    inner class Geo {
        var lng: Double = 0.0
        var lat: Double = 0.0
    }
}

data class ScenicDetailResponseBean(
    var code: Int = -1,
    var msg: String? = null,
    var data: ScenicDetailBean? = null,
    var traceid: String? = null
)
