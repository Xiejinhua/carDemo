package com.desaysv.psmap.model.bean.standard

/**
 * 当前位置查询
 */
data class CurrentLocationOperaData(
    val type: Int = 0, //是否调起地图展示我的位置卡⽚0：调起客户端执⾏1：不调起客户端执⾏
    var myLocationName: String = "", //我的位置信息描述例如：“在厦⻔市软件园⼆期望海路59号附近”
    var poiName: String = "", //位置名称(兼容Android⼴播协议)
    var longitude: Double = 0.0, //当前位置的坐标，经度
    var latitude: Double = 0.0, //当前位置的坐标，纬度
    val countryName: String = "中国", //当前位置的国家名称，固定为“中国”
    val countryCode: String = "86", //当前位置的的国家代码，固定为“86”
    var provinceName: String = "", //当前位置的省份名
    var provinceCode: String = "", //当前位置的省份编码
    var cityCode: String = "", //当前位置的城市编码
    var districtCode: String = "", //当前位置的地区编码
    var fullAddress: String = "", //当前位置的详细地址（包含省份城市信息） 暂不⽀持
    var address: String = "", //当前位置的简要地址（不包含省份城市信息）
    var cityName: String = "", //城市名（520及以上⽀持）
    var districtName: String = "" //区县名（520及以上⽀持）
) : ResponseDataData() //是否调起地图展示我的位置卡⽚0：调起客户端执⾏1：不调起客户端执⾏
