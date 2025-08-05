package com.desaysv.psmap.model.bean

data class JetourPoi(
    val address_name: String? = null,
    val area: String? = null,
    val city: String? = null,
    val id: String? = null,
    val latitude: Double,
    val location_type_name: String? = null,
    val longitude: Double,
    val province: String? = null,
    val venue_name: String? = null,
    var distance: Int? = null,
    var schema: String? = null,
)