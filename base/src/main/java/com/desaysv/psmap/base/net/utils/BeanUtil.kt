package com.desaysv.psmap.base.net.utils

import com.google.gson.Gson
import timber.log.Timber

object BeanUtil {
    /**
     * 通过反射获取obj对应的map
     * @param obj
     * @return
     */
    @JvmStatic
    fun objectToMap(obj: Any?): Map<String?, Any?>? {
        if (obj == null) {
            return null
        }
        val gson = Gson()
        var map: MutableMap<String?, Any?>? = null
        try {
            map = HashMap()
            val declaredFields = obj.javaClass.declaredFields
            for (field in declaredFields) {
                field.isAccessible = true
                if (field[obj] is List<*>) {
                    map[field.name] = gson.toJson(field[obj])
                    continue
                }
                map[field.name] = field[obj]
            }
        } catch (e: IllegalAccessException) {
            Timber.e(e.toString())
        }
        return map
    }
}
