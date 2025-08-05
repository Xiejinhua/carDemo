package com.desaysv.psmap.model.bean

import android.os.Bundle
import android.os.Parcelable
import com.desaysv.psmap.model.utils.Biz
import kotlinx.parcelize.Parcelize

/**
 * 界面业务类型相关的数据类
 *@author uidq3334
 *@emily weilon.huang@foxmail.com
 *@date 2023/8/10
 */
@Parcelize
data class ModifyPoiBean(
    val poiName: String = "",
    val type: Int = 1, //1添加途经点，2修改途经点，3修改终点
    val position: Int = 0, //第几个途经点

    /**
     * 其他特殊字段
     */
    val extras: Map<String, String?>? = null
) : Parcelable {
    fun toBundle() = Bundle().apply {
        putParcelable(Biz.KEY_BIZ_ROUTE_CHANGE_END_VIA_POI, this@ModifyPoiBean)
    }
}
