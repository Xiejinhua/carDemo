package com.desaysv.psmap.model.bean.standard

/**
 * 关键字搜索 （30300）、 周边搜索 （30301）应答类
 */
data class SearchResponseData(
    val poiResult: PoiResult? = null //Poi结果信息
) : ResponseDataData()

data class PoiResult(
    val Citysuggestion: Citysuggestion, //推荐城市
    val Count: Int, // 搜索结果返回最大个数,poi结果列表个数
    val Pois: List<Poi>?, //poi结果列表
    val categories: List<Category>? //⼦POI信息（430及以上版本⽀持）
)

data class Citysuggestion(
    val Citycount: Int? = null, // 推荐城市数量
    val SuggestionCityDetail: List<SuggestionCityDetail>? = null//城市建议相关列表
)

data class Poi(
    val Address: String?, //poi地址描述
    val Latitude: Double, //纬度
    val Name: String?, //poi名称
    val Poiid: String?, //poi唯⼀码
    val Tel: String?, //电话
    val Typecode: String?,//poi类型码，如：050118
    val biz_ext: BizExt, //停⻋场深度信息
    val childPoiList: List<ChildPoi>?, //⼦POI信息（430及以上版本⽀持）
    val distaceToSearchLocation: Int?, //距离，单位：⽶ （特指搜索中⼼点距离搜索结果poi的距离）
    val distance: Int?, //距离单位是⽶
    val enteryList: List<EnteryX>?, //到达点（⼊⼝）经纬度列表
    val homecopType: Int?, //0：默认普通 1：家 2：公司
    val longitude: Double //经度
)

data class Category(
    val categoryItems: List<CategoryItem>?,
    val checkedvalue: String, //当前选中的分类值
    val ctype: String?, //分类 or 排序 分类:category 排序:filter
    val name: String //分类名称
)

data class SuggestionCityDetail(
    val Cityname: String, // 城市名称
    val Citynum: Int // 共多少条结果
)

data class BizExt(
    val category: Int? = null, //POI类别 0 : 停⻋场 1 ：加油站 2 ：其他（酒店、商场、电影院、KTV、景点等） 3：充电站（430及以上版本）
    val tag: String? = null, //酒店星级信息，有才会显示 停⻋场-类型 加油站-品牌 酒店-星级
    val taginfo: String? = null //深度信息内容 Object: 当category为0 、2、3（停⻋场、酒店、充电站）时taginfo类型为Object List: 当category为1 （加油站）时taginfo类型为list
)

data class ChildPoi(
    val Address: String, //poi地址描述
    val Latitude: Double, //纬度
    val Name: String, //poi名称
    val Poiid: String, //poi唯⼀码
    val Tel: String? = null, //电话
    val Typecode: String?, //poi类型码，如：050118
    val biz_ext: BizExt?, //深度信息,TypeCode字段为酒店、停⻋场、加油站才会有
    val distance: Int, //距离单位是⽶
    val enteryList: List<EnteryX>?, //到达点（⼊⼝）经纬度列表
    val homecopType: Int, //0：默认普通 1：家 2：公司
    val longitude: Double //经度
)

data class EnteryX(
    val entry_latitude: Double, //纬度
    val entry_longitude: Double //经度
)

data class CategoryItem(
    val categoryItems: List<CategoryItemX>?, //⼦分类
    val name: String, //分类名称
    val value: String //分类值
)

data class CategoryItemX(
    val name: String, //分类名称
    val value: String //分类值
)