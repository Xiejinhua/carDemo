package com.desaysv.psmap.model.bean.standard

/**
 * 回地图
 */
data class BackToMapOperaData(val type: Int = 0) : ResponseDataData() //0：在后台-切换到前台，后台是什么⻚⾯，前台也是什么⻚⾯；在前台-如果在导航中则回导航沉浸态，如果⾮导航，则回主图回⻋位
