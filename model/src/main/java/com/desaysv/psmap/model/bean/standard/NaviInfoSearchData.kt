package com.desaysv.psmap.model.bean.standard

/**
 * @author 谢锦华
 * @time 2025/3/24
 * @description 查询引导信息  PROTOCOL_INFO_NOTIFY_SEARCHNAVINFO: Int = 0 = 80163
 */

data class NaviInfoSearchData(
    val requestType: Int = 0,//请求类型 0:请求引导信息 1:请求测速摄像头信息（500及以上版本⽀持）
) : ResponseDataData()

data class NaviInfoSearchResponse(
    //    /**
//     * 电⼦眼类型 电⼦眼类型，对应的值为int类型 *
//     * 0 测速摄像头
//     * 1为监控摄像头
//     * 2为闯红灯拍照 *
//     * 3为违章拍照 *
//     * 4为公交专⽤道摄像头 *
//     * 5为应急⻋道摄像头 *
//     * 6为⾮机动⻋道拍照 *
//     * 7为区间测速路段【（暂未使⽤）ps：3.x版本后新增】 *
//     * 8为区间测速起始 （ps：3.x版本后新增）（4.x版本不⽀持） *
//     * 9为区间测速结束 （ps：3.x版本后新增）（4.x版本不⽀持） *
//     * 10为流动测速电⼦眼ps：3.x版本后新增） *
//     * 11ETC电⼦眼 *
//     * 12压线 *
//     * 13礼让⾏⼈ *
//     * 14违规占⻋道（仅下发，前端不展示） *
//     * 15闯红灯细类 *
//     * 16公交⻋道细类 *
//     * 17应急⻋道细类 *
//     * 18系安全带 *
//     * 19拨打电话 *
//     * 20⾮机动⻋道 *
//     * 21违法停⻋ *
//     * 22违规⽤灯仅下发，前端不展示） *
//     * 23违规过路⼝（仅下发，前端不展示） *
//     * 24禁⽌鸣笛 *
//     * 25逆向⾏驶 *
//     * 26违规过铁道路⼝ *
//     * 27尾号限⾏ *
//     * 28⻋间距抓拍 *
//     * 29HOV⻋道 *
//     * 30环保限⾏ 备注：0~11为电⼦眼⼤类类型，12~24为违章电⼦眼的细类类型。11-24类型仅510及以上⽀持。25-30类型仅6.0及以上⽀持。
//     */
    var cameraType: Int = 0,
    var cameraID: Int = 0,//电⼦眼唯⼀标识（510及以上版本⽀持）
    var cameraPenalty: Boolean = false,//是否⾼罚电⼦眼（510及以上⽀持） true：是 false：否
    var cameraSpeed: Int = 0,//电⼦眼限速度，对应的值为int类型，⽆限速则为0，单位：公⾥/⼩时
    var cameraDist: Int = 0,//距离最近的电⼦眼距离，对应的值为int类型，单位：⽶
    var isOverspeed: Boolean = false,//当前⻋速是否超过电⼦眼限速 true：是 false：否
    var sapaDist: Int = 0,//距离最近服务区的距离，对应的值为int类型，单位：⽶
    var sapaETA: Int = 0,//到达最近的服务区的预计⽤时，单位：秒
    var sapaName: String = "",//距离最近的服务区名称
    var sapaNum: Int = 0,//服务区个数
    var sapaType: Int = 0,//距离最近的服务区类型： 0：⾼速服务区 1：其他服务设施（收费站、停⻋区 等）
    var tollDist: Int = 0,//距离最近收费站的距离，对应的值为int类型，单位：⽶
    var tollETA: Int = 0,//距离最近的收费站名称
    var tollName: String = ""//到达最近的收费站的预计⽤时，单位：秒
) : ResponseDataData()