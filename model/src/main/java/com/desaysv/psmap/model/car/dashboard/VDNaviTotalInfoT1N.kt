package com.desaysv.psmap.model.car.dashboard

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.desaysv.ivi.vdb.event.VDEvent

data class VDNaviTotalInfoT1N(
    var TotalDistance: Int = 0,
    var DistanceUint: Int = 0,
    var RemDistance: Int = 0,
    var RemDistanceUint: Int = 0,
    var TimeLeft: Int = 0,
    var ArrivalTime: String? = null,
    var Week: String? = null,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        TotalDistance = parcel.readInt(),
        DistanceUint = parcel.readInt(),
        RemDistance = parcel.readInt(),
        RemDistanceUint = parcel.readInt(),
        TimeLeft = parcel.readInt(),
        ArrivalTime = parcel.readString(),
        Week = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(TotalDistance)
        parcel.writeInt(DistanceUint)
        parcel.writeInt(RemDistance)
        parcel.writeInt(RemDistanceUint)
        parcel.writeInt(TimeLeft)
        parcel.writeString(ArrivalTime)
        parcel.writeString(Week)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VDNaviTotalInfoT1N> {
        override fun createFromParcel(parcel: Parcel): VDNaviTotalInfoT1N {
            return VDNaviTotalInfoT1N(parcel)
        }

        override fun newArray(size: Int): Array<VDNaviTotalInfoT1N?> {
            return arrayOfNulls(size)
        }

        fun getValue(event: VDEvent?): VDNaviTotalInfoT1N? {
            event?.payload?.setClassLoader(VDNaviTotalInfoT1N::class.java.classLoader)
            return event?.payload?.getParcelable("data")
        }

        fun createPayload(value: VDNaviTotalInfoT1N): Bundle {
            val payload = Bundle()
            payload.setClassLoader(VDNaviTotalInfoT1N::class.java.classLoader)
            payload.putParcelable("data", value)
            return payload
        }

        fun createEvent(eventId: Int, value: VDNaviTotalInfoT1N): VDEvent {
            val payload = createPayload(value)
            return VDEvent(eventId, payload)
        }
    }

    override fun toString(): String {
        return "VDNaviTotalInfoT1N(TotalDistance=$TotalDistance, DistanceUint=$DistanceUint, RemDistance=$RemDistance, RemDistanceUint=$RemDistanceUint, TimeLeft=$TimeLeft, ArrivalTime=$ArrivalTime)"
    }
}