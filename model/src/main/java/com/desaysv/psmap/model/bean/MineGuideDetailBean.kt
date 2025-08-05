package com.desaysv.psmap.model.bean

data class MineGuideDetail(
    var code: Int = 0,
    var msg: String? = null,
    var data: GuideData? = null
)

// GuideNode Class
data class GuideNode(
    var nodeId: Int = 0,
    var type: Int = 0,
    var typeId: Int = 0,
    var caption: String? = null,
    var logo: String? = null,
    var images: List<String>? = null,
    var provinceName: String? = null,
    var cityName: String? = null,
    var address: String? = null,
    var lng: Double = 0.0,
    var lat: Double = 0.0,
    var distance: Int = 0,
    var distanceInMeters: Int = 0,
    var description: String? = null
)

// GuideOtherData Class
data class GuideOtherData(
    var fav: Boolean = false,
    var groupTrip: Int = 0
)

// NodeList Class
data class NodeList(
    var dayId: Int = 0,
    var startCity: String? = null,
    var endCity: String? = null,
    var distance: Int = 0,
    var distanceInMeters: Int = 0,
    var distanceText: String? = null,
    var node: List<GuideNode>? = null,
)

// Data Class
data class GuideData(
    var id: Int = 0,
    var caption: String? = null,
    var images: List<String>? = null,
    var startProvince: String? = null,
    var startCity: String? = null,
    var endProvince: String? = null,
    var endCity: String? = null,
    var totalDay: Int = 0,
    var distanceInMeters: Int = 0,
    var distance: String? = null,
    var description: String? = null,
    var nodeList: List<NodeList>? = null,
    var otherData: GuideOtherData? = null
)
