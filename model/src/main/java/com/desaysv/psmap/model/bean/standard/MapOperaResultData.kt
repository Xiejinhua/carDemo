package com.desaysv.psmap.model.bean.standard

data class MapOperaResultData(
    /**
     * 0：⽆效值 1：打开路况 2：关闭路况 3：放⼤地图 4：缩⼩地图
     */
    var operateType: Int = 0,

    /**
     * 操作是否成功
     */
    var isSuccess: Boolean = true,

    /**
     *  能否继续缩放（仅在缩放操作时有效）
     */
    var isCanZoom: Boolean = true,

    ) : ResponseDataData()