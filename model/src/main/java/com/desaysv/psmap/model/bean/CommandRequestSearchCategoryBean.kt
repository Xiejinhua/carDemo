package com.desaysv.psmap.model.bean

import android.os.Bundle
import android.os.Parcelable
import com.autonavi.gbl.data.model.CityItemInfo
import com.autosdk.bussiness.common.POI
import com.desaysv.psmap.model.utils.Biz
import kotlinx.parcelize.Parcelize

@Parcelize
class CommandRequestSearchCategoryBean(
    @Type
    var type: Int = 0,
    var keyword: String? = null,
    var poi: POI? = null,
    var city: CityItemInfo? = null,
    var isVoiceSearch: Boolean = false
) : Parcelable {

    class Builder {
        private val commandRequestSearchBean: CommandRequestSearchCategoryBean = CommandRequestSearchCategoryBean()

        fun setType(type: Int): Builder {
            commandRequestSearchBean.type = type
            return this
        }

        fun setCity(city: CityItemInfo?): Builder {
            commandRequestSearchBean.city = city
            return this
        }

        fun setPoi(poi: POI?): Builder {
            commandRequestSearchBean.poi = poi
            return this
        }

        fun setKeyword(word: String?): Builder {
            commandRequestSearchBean.keyword = word
            return this
        }

        fun setIsVoiceSearch(isVoiceSearch: Boolean):Builder {
            commandRequestSearchBean.isVoiceSearch = isVoiceSearch
            return this
        }

        fun build(): CommandRequestSearchCategoryBean {
            return commandRequestSearchBean
        }
    }


    annotation class Type {
        companion object {
            const val SEARCH_AROUND = 1 //poi点周边搜
            const val SEARCH_ALONG_WAY = 2 //沿途搜
            const val SEARCH_TEAM_DESTINATION = 3 //组队出行目的地搜索
        }
    }

    open fun toBundle() = Bundle().apply {
        putParcelable(Biz.KEY_BIZ_SEARCH_CATEGORY_LIST, this@CommandRequestSearchCategoryBean)
    }
}
