package com.desaysv.psmap.model.bean.standard

data class RouteChangeOperaData(

    val num: Long = 0, //选择切换的路线编号，对照80155协议中透出的编号上传

) : ResponseDataData()

