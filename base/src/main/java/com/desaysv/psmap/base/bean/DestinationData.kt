package com.desaysv.psmap.base.bean

import com.autosdk.bussiness.common.POI

//手车互联变更目的地或者途经点
data class DestinationData(val poi: POI?, val sendType: Int, val mPlanPref: String)
