package com.desaysv.psmap.model.bean

/**
 * 轨迹路书详情
 */
data class AhaTankDetailBean(
    var code: Int = 0,
    var msg: String? = null,
    var data: TankData? = null,
    var traceId: String? = null
)

class TankMarkers {
    var caption: String? = null

    var logo: String? = null

    var images: List<String>? = null

    var time: Int = 0

    var markAt: String? = null

    var lng: Double = 0.0

    var lat: Double = 0.0

    var cityName: String? = null

    var address: String? = null
}

// UserData 类
data class UserData(
    var id: Int = 0,
    var avatar: String? = null,
    var nickname: String? = null
)

// OtherData 类
data class TankOtherData(
    var fav: Boolean = false,
    var favNum: Int = 0,
    var detailUrl: String? = null
)

// CommonData 类
data class CommonData(
    var title: String? = null,
    var description: String? = null,
    var logo: String? = null
)

// Text 类
data class Text(
    var text: String? = null
)

// Image 类
data class Image(
    var url: String? = null,
    var thumb: String? = null
)

// WebPage 类
data class WebPage(
    var url: String? = null
)

// WxMini 类
data class WxMini(
    var webPageUrl: String? = null,
    var userName: String? = null,
    var path: String? = null
)

// ShareData 类
data class ShareData(
    var text: Text? = null,
    var image: Image? = null,
    var webPage: WebPage? = null,
    var wxMini: WxMini? = null
)

// ShareInfo 类
data class ShareInfo(
    var canShare: Boolean = false,
    var shareType: String? = null,
    var commonData: CommonData? = null,
    var shareData: ShareData? = null
)

// Data 类
data class TankData(
    var id: Int = 0,
    var caption: String? = null,
    var logo: String? = null,
    var description: String? = null,
    var speed: Int = 0,
    var distance: Int = 0,
    var asl: Int = 0,
    var time: Int = 0,
    var markers: List<TankMarkers>? = null,
    var finished: Int = 0,
    var createAt: String? = null,
    var userData: UserData? = null,
    var otherData: TankOtherData? = null,
    var fav: Boolean = false,
    var shareInfo: ShareInfo? = null
)
