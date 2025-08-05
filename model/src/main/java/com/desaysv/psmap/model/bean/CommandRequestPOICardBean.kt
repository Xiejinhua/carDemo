package com.desaysv.psmap.model.bean

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.desaysv.psmap.model.utils.Biz

class CommandRequestPOICardBean() : Parcelable {
    @get:Type
    @Type
    var type = Type.POI_CARD_ADD_POINT

    class Builder {
        private val commandRequestPOICardBean: CommandRequestPOICardBean = CommandRequestPOICardBean()

        fun setType(type: Int): Builder {
            commandRequestPOICardBean.type = type
            return this
        }

        fun build(): CommandRequestPOICardBean {
            return commandRequestPOICardBean
        }
    }


    annotation class Type {
        companion object {
            const val POI_CARD_ADD_POINT = 1 //地图选点界面
            const val POI_CARD_ROUTE_ADD_VIA = 2 //路线规划点击添加途经点界面
            const val POI_CARD_SEARCH_HOME = 3 //家的地址搜索
            const val POI_CARD_SEARCH_COMPANY = 4 //公司的地址搜索
            const val POI_CARD_SEARCH_TEAM_DESTINATION = 5 //组队出行目的地搜索
        }
    }

    constructor(parcel: Parcel) : this() {
        type = parcel.readInt()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommandRequestPOICardBean> {
        override fun createFromParcel(parcel: Parcel): CommandRequestPOICardBean {
            return CommandRequestPOICardBean(parcel)
        }

        override fun newArray(size: Int): Array<CommandRequestPOICardBean?> {
            return arrayOfNulls(size)
        }
    }

    open fun toBundle() = Bundle().apply {
        putParcelable(Biz.KEY_BIZ_SHOW_POI_CARD, this@CommandRequestPOICardBean)
    }
}
