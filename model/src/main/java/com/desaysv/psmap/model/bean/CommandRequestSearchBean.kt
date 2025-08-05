package com.desaysv.psmap.model.bean

import android.os.Bundle
import android.os.Parcelable
import com.autonavi.gbl.data.model.CityItemInfo
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.model.utils.Biz
import kotlinx.parcelize.Parcelize

@Parcelize
data class CommandRequestSearchBean(
    @Type
    var type: Int = 0,
    var keyword: String? = null,
    var poi: POI? = null,
    var city: CityItemInfo? = null,
    var range: String? = null,
    var day: String? = null,
    var shouldShowKeyboard: Boolean? = null,
    var isVoiceSearch: Boolean = false,
) : Parcelable {

    class Builder {
        private val commandRequestSearchBean: CommandRequestSearchBean = CommandRequestSearchBean()

        fun setType(type: Int): Builder {
            commandRequestSearchBean.type = type
            return this
        }

        fun setPoi(poi: POI?): Builder {
            commandRequestSearchBean.poi = poi
            return this
        }

        fun setCity(city: CityItemInfo?): Builder {
            commandRequestSearchBean.city = city
            return this
        }

        fun setKeyword(word: String?): Builder {
            commandRequestSearchBean.keyword = word
            return this
        }

        fun setRange(range: String?): Builder {
            commandRequestSearchBean.range = range
            return this
        }

        fun setDay(day: String?): Builder {
            commandRequestSearchBean.day = day
            return this
        }

        fun setShouldShowKeyboard(shouldShowKeyboard: Boolean?): Builder {
            commandRequestSearchBean.shouldShowKeyboard = shouldShowKeyboard
            return this
        }

        fun setIsVoiceSearch(isVoiceSearch: Boolean): Builder {
            commandRequestSearchBean.isVoiceSearch = isVoiceSearch
            return this
        }

        fun build(): CommandRequestSearchBean {
            return commandRequestSearchBean
        }
    }


    annotation class Type {
        companion object {
            const val SEARCH_AROUND = 1 //周边搜
            const val SEARCH_KEYWORD = 2 //关键字搜
            const val SEARCH_HOME = 3 //家的地址搜索
            const val SEARCH_COMPANY = 4 //公司的地址搜索
            const val SEARCH_TEAM_DESTINATION = 5 //组队出行目的地搜索
            const val SEARCH_CHARGE = 6 //一键补能（加油站、充电站 ）
            const val SEARCH_TRIP_CITY = 7 //路书城市搜索
            const val SEARCH_CUSTOM_POI = 8 //捷途分类咖啡店
            const val SEARCH_KEYWORD_COLLECT = 9 //语音收藏点搜索
            const val SEARCH_JETOURPOI_SCHEMA = 10 //捷途探趣-抖音activity的Schema
            const val SEARCH_CUSTOM_CATEGORY = 11 //捷途界面-分类界面-指定分类界面
            const val SEARCH_CUSTOM_AHA_TRIP = 12 //捷途界面-路书界面
        }
    }

    fun toBundle() = Bundle().apply {
        putParcelable(Biz.KEY_BIZ_SEARCH_REQUEST, this@CommandRequestSearchBean)
    }
}
