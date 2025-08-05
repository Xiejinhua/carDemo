package com.desaysv.psmap.model.impl

interface IMapDataOutputCallback {
    fun onMapData(pkg: String, jsonData: String)
    fun onMapByteData(pkg: String?, jsonData: String?, byteArray: ByteArray?)
    fun onMapDataToAllPackage(jsonData: String)
}