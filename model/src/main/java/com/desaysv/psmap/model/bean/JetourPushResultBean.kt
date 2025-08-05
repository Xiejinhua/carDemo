package com.desaysv.psmap.model.bean

import com.autosdk.bussiness.common.POI

/**
 * 乐享出行结果bean
 */
data class JetourPushResultBean(var endPoi: POI? = null, var midPois: ArrayList<POI>? = null, var backupId: String? = null)
