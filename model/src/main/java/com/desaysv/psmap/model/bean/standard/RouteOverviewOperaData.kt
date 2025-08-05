package com.desaysv.psmap.model.bean.standard

data class RouteOverviewOperaData(
    val isShow: Int = 0 //0：进⼊全览 1：退出全览 2：如果全览则切换⾮全览，如果⾮全览则切换全览
) : ResponseDataData()
