package com.desaysv.psmap.model.bean.standard

/**
 * 查询前⽅路况
 */
data class TRManualOperaData(
    val iFrontDistance: Int = 0, //查询前⽅路况的距离。预留字段。暂时默认为0.
    val isThirdparty: Boolean = true, //是否第三⽅调⽤（字段预留，暂未使⽤）
    val ttsBroadcast: Int = 0, //tts播报⽅(由谁播报) 0：auto 1：系统
    var frontTrafficInfo: String = "" //前⽅路况信息描述如：“前⽅，去往虹桥机场⽅向，有2.8公⾥拥堵，⾏驶缓慢”
) : ResponseDataData()
