package com.desaysv.psmap.adapter.tbt.utils

import android.graphics.drawable.Drawable
import com.desaysv.psmap.adapter.R
import java.lang.reflect.Field

/**
 * @author 谢锦华
 * @time 2025/01/13
 * @description
 */
object NaviUiUtils {
    val TAG: String = "NaviUiUtils"

    fun getRoadSignBitmap(maneuverId: Int, isNextThum: Boolean): String {
        var imageResId = ""
        val hudResPrefix = "global_image_hud_"
        var iconResName = "sou"
        val nextThum = "_next"
        iconResName = hudResPrefix + iconResName + (if (isNextThum) nextThum else "")
        if (maneuverId == 65) { //1076B新增，65靠左图标
            imageResId = iconResName + (6 + maneuverId)
            return imageResId
        } else if (maneuverId == 66) { //1076B新增，66靠右图标
            imageResId = iconResName + (4 + maneuverId)
        } else {
            imageResId = iconResName + maneuverId
        }

        return imageResId
    }

    fun getDrawableID(icon: String, isNightMode: Boolean): Int {
        val night = if (isNightMode) "_night" else ""
        var f: Field? = null
        var drawableId = 0
        try {
            f = R.drawable::class.java.getDeclaredField(icon + night)
            drawableId = f.getInt(Drawable::class.java)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return drawableId
    }
}