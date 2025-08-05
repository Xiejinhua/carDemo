package com.desaysv.psmap.model.bean.standard

data class RouteSelectOperaData(

    val selectType: Int = 0, //选择哪条路线 0: 当前选中路线的“开始导航”操作 1：选中第1条线路 2：选中第2条路线 3：选中第3条路线

    val isStartNavi: Boolean = false //路线结果⻚选择路线⽅案的同时是否开始导航

) : ResponseDataData()

