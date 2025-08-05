package com.desaysv.psmap.model.bean.standard

data class QueryRestrictedInfoOperaData(

    val requestType: Int = 0, //限⾏查询类型 0：限⾏尾号查询 1：限⾏政策查询 （仅⽀持⻚⾯跳转，不⽀持 政策详情透出）

    val cityName: String = "", //输⼊需要搜索限⾏的城市名。 “城市名”需要全称，例如“北京市”

    val adCode: Int = 0,

    var restrictedNumber: String = "" //如果所查询的城市⽀持尾号限⾏，则对外透出限⾏的尾号

) : ResponseDataData()

