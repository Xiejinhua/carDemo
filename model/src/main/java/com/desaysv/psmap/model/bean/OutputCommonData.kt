package com.desaysv.psmap.model.bean

import com.desaysv.psmap.adapter.command.MassageType
import com.desaysv.psmap.base.utils.BaseConstant

data class OutputCommonData<T>(
    var version: String = BaseConstant.MAP_OUTPUT_DATA_VERSION,
    var massageType: MassageType,
    var data: T,
)
