package com.desaysv.psmap.model.car.dashboard

data class VDNaviDisplayClusterT1N(
    var NaviFrontDeskStatus: String = "false",
    var DisplayCluster: String = "false",
    var Perspective: Int = 0,
    var PerspectiveResult: String = "false",
    var RequestDisplayNaviArea: String = "false"
)