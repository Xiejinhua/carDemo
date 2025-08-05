package com.desaysv.psmap.model.bean

data class CustomGroupMember(
    var user_id: Int = 0,
    var nick_name: String? = null,
    var remark: String? = null,
    var status: Int = 0,
    var voice_status: Int = 0,
    var isOnline_status: Boolean = false,
    var latest_lon: String? = null,
    var latest_lat: String? = null,
    var time_stamp: Long = 0,
    var head_img: String? = null,
    var isTeam: Boolean = false,
    var isLeader: Boolean = false,
)