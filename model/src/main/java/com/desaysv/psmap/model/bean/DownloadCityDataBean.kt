package com.desaysv.psmap.model.bean

import com.autonavi.gbl.data.model.CityItemInfo

/**
 * @author 王漫生
 * @project：离线地图下载管理城市数据bean
 */
data class DownloadCityDataBean(var type: Int, var cityItemInfo: CityItemInfo, var arCodes: ArrayList<Int> = arrayListOf())

const val TYPE_PROVINCE = 1
const val TYPE_CITY = 2
