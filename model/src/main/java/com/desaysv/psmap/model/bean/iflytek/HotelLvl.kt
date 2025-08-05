package com.desaysv.psmap.model.bean.iflytek

data class HotelLvl(
    /*
     "hotelLvl": {
                "type": "SPOT",
                "ref": "ZERO",
                "direct": "+",
                "offset": "5"
            }
     */
    val type: String,
    val ref: String,
    val direct: String,
    val offset: String
)
