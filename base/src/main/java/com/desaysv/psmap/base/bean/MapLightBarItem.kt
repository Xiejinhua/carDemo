package com.desaysv.psmap.base.bean

import android.os.Parcel
import android.os.Parcelable

data class MapLightBarItem(
    var status: Int = 0,
    var length: Int = 0,
    var timeOfSeconds: Long = 0L,
    var startSegmentIdx: Int = 0,
    var startLinkIdx: Int = 0,
    var startLinkStatus: Long = 0,
    var endSegmentIdx: Int = 0,
    var endLinkIndex: Int = 0,
    var endLinkStatus: Long = 0
) : Parcelable {


    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(status)
        parcel.writeInt(length)
        parcel.writeLong(timeOfSeconds)
        parcel.writeInt(startSegmentIdx)
        parcel.writeInt(startLinkIdx)
        parcel.writeLong(startLinkStatus)
        parcel.writeInt(endSegmentIdx)
        parcel.writeInt(endLinkIndex)
        parcel.writeLong(endLinkStatus)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<MapLightBarItem> {
            override fun createFromParcel(parcel: Parcel): MapLightBarItem {
                return MapLightBarItem(parcel)
            }

            override fun newArray(size: Int): Array<MapLightBarItem?> {
                return arrayOfNulls(size)
            }
        }
    }
}