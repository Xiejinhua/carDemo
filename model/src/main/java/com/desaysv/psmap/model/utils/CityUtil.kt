package com.desaysv.psmap.model.utils

object CityUtil {

    // 判断是否是直辖市
    private fun isMunicipality(adcode: Int): Boolean {
        //110000 北京
        //120000 天津
        //310000 上海
        //500000 重庆
        //810000 香港特别行政区
        //820000 澳门特别行政区
        return setOf(110000, 120000, 310000, 500000, 810000, 820000).contains(adcode)
    }

    fun isProvince(adcode: Int): Boolean {
        // 省级行政区域通常以0000为结尾
        return adcode % 10000 == 0 && !isMunicipality(adcode)
    }
}