package com.desaysv.psmap.model.utils

import com.desaysv.psmap.model.R
import timber.log.Timber

/**
 * 定义输出车道线ID
 */
object OutputLaneInfo {
    private val laneInfoMap: MutableMap<Int, Pair<Int, Int>> = mutableMapOf()

    fun getLaneKanZiID(key: Int): Int {
        try {
            Timber.i(" setLaneImg key:$key")
            if (laneInfoMap.isEmpty())
                initOutputLaneInfo()
            val keyy = key and 0x00ff00ff
            return if (laneInfoMap.containsKey(keyy)) {
                laneInfoMap[keyy]!!.second
            } else {
                16
            }
        } catch (e: Exception) {
            Timber.i(" setLaneImg Exception e:%s", e.message)
        }
        return 0
    }

    /**
     * 对外输出定义
     */
    private fun initOutputLaneInfo() {
        //巡航车道
        laneInfoMap[0x000000ee] = Pair(R.drawable.global_image_auto_landback_0, 16)
        laneInfoMap[0x000100ee] = Pair(R.drawable.global_image_auto_landback_1, 17)
        laneInfoMap[0x000200ee] = Pair(R.drawable.global_image_auto_landback_2, 18)
        laneInfoMap[0x000300ee] = Pair(R.drawable.global_image_auto_landback_3, 19)
        laneInfoMap[0x000400ee] = Pair(R.drawable.global_image_auto_landback_4, 20)
        laneInfoMap[0x000500ee] = Pair(R.drawable.global_image_auto_landback_5, 21)
        laneInfoMap[0x000600ee] = Pair(R.drawable.global_image_auto_landback_6, 22)
        laneInfoMap[0x000700ee] = Pair(R.drawable.global_image_auto_landback_7, 23)
        laneInfoMap[0x000800ee] = Pair(R.drawable.global_image_auto_landback_8, 24)
        laneInfoMap[0x000900ee] = Pair(R.drawable.global_image_auto_landback_9, 25)
        laneInfoMap[0x000a00ee] = Pair(R.drawable.global_image_auto_landback_a, 26)
        laneInfoMap[0x000b00ee] = Pair(R.drawable.global_image_auto_landback_b, 27)
        laneInfoMap[0x000c00ee] = Pair(R.drawable.global_image_auto_landback_c, 28)
        laneInfoMap[0x000d00ee] = Pair(R.drawable.global_image_auto_landback_d, 29)
        laneInfoMap[0x000e00ee] = Pair(R.drawable.global_image_auto_landback_e, 30)
        laneInfoMap[0x001000ee] = Pair(R.drawable.global_image_auto_landback_10, 57)
        laneInfoMap[0x001100ee] = Pair(R.drawable.global_image_auto_landback_11, 58)
        laneInfoMap[0x001200ee] = Pair(R.drawable.global_image_auto_landback_12, 59)
        laneInfoMap[0x001300ee] = Pair(R.drawable.global_image_auto_landback_13, 60)
        laneInfoMap[0x001400ee] = Pair(R.drawable.global_image_auto_landback_14, 61)
        laneInfoMap[0x001500ee] = Pair(R.drawable.global_image_auto_landback_15, 62)
        laneInfoMap[0x001600ee] = Pair(R.drawable.global_image_auto_landback_0, 16)//未规划
        laneInfoMap[0x001700ee] = Pair(R.drawable.global_image_auto_landback_17, 63)
        laneInfoMap[0x001800ee] = Pair(R.drawable.global_image_auto_landback_0, 89)//专用车道
        laneInfoMap[0x001900ee] = Pair(R.drawable.global_image_auto_landback_19, 86)
        //背景车道
        laneInfoMap[0x000000ff] = Pair(R.drawable.global_image_landback_0, 1)
        laneInfoMap[0x000100ff] = Pair(R.drawable.global_image_landback_1, 2)
        laneInfoMap[0x000200ff] = Pair(R.drawable.global_image_landback_2, 3)
        laneInfoMap[0x000300ff] = Pair(R.drawable.global_image_landback_3, 4)
        laneInfoMap[0x000400ff] = Pair(R.drawable.global_image_landback_4, 5)
        laneInfoMap[0x000500ff] = Pair(R.drawable.global_image_landback_5, 6)
        laneInfoMap[0x000600ff] = Pair(R.drawable.global_image_landback_6, 7)
        laneInfoMap[0x000700ff] = Pair(R.drawable.global_image_landback_7, 8)
        laneInfoMap[0x000800ff] = Pair(R.drawable.global_image_landback_8, 9)
        laneInfoMap[0x000900ff] = Pair(R.drawable.global_image_landback_9, 10)
        laneInfoMap[0x000a00ff] = Pair(R.drawable.global_image_landback_a, 11)
        laneInfoMap[0x000b00ff] = Pair(R.drawable.global_image_landback_b, 12)
        laneInfoMap[0x000c00ff] = Pair(R.drawable.global_image_landback_c, 13)
        laneInfoMap[0x000d00ff] = Pair(R.drawable.global_image_landback_d, 14)
        laneInfoMap[0x000e00ff] = Pair(R.drawable.global_image_landback_e, 15)
        laneInfoMap[0x001000ff] = Pair(R.drawable.global_image_landback_10, 50)
        laneInfoMap[0x001100ff] = Pair(R.drawable.global_image_landback_11, 51)
        laneInfoMap[0x001200ff] = Pair(R.drawable.global_image_landback_12, 52)
        laneInfoMap[0x001300ff] = Pair(R.drawable.global_image_landback_13, 53)
        laneInfoMap[0x001400ff] = Pair(R.drawable.global_image_landback_14, 54)
        laneInfoMap[0x001500ff] = Pair(R.drawable.global_image_landback_15, 55)
        laneInfoMap[0x001600ff] = Pair(R.drawable.global_image_landback_0, 1)//未规划
        laneInfoMap[0x001700ff] = Pair(R.drawable.global_image_landback_17, 56)
        laneInfoMap[0x001800ff] = Pair(R.drawable.global_image_landback_0, 89)//专用车道
        laneInfoMap[0x001900ff] = Pair(R.drawable.global_image_landback_19, 86)
        //前景车道
        laneInfoMap[0x00000000] = Pair(R.drawable.global_image_landfront_0, 16)
        laneInfoMap[0x00010001] = Pair(R.drawable.global_image_landfront_1, 17)
        laneInfoMap[0x00020000] = Pair(R.drawable.global_image_landfront_20, 31)
        laneInfoMap[0x00020001] = Pair(R.drawable.global_image_landfront_21, 32)
        laneInfoMap[0x00030003] = Pair(R.drawable.global_image_landfront_3, 19)
        laneInfoMap[0x00040000] = Pair(R.drawable.global_image_landfront_40, 33)
        laneInfoMap[0x00040003] = Pair(R.drawable.global_image_landfront_43, 34)
        laneInfoMap[0x00050005] = Pair(R.drawable.global_image_landfront_5, 21)
        laneInfoMap[0x00060001] = Pair(R.drawable.global_image_landfront_61, 35)
        laneInfoMap[0x00060003] = Pair(R.drawable.global_image_landfront_63, 36)
        laneInfoMap[0x00070000] = Pair(R.drawable.global_image_landfront_70, 37)
        laneInfoMap[0x00070001] = Pair(R.drawable.global_image_landfront_71, 38)
        laneInfoMap[0x00070003] = Pair(R.drawable.global_image_landfront_73, 39)
        laneInfoMap[0x00080008] = Pair(R.drawable.global_image_landfront_8, 24)
        laneInfoMap[0x00090000] = Pair(R.drawable.global_image_landfront_90, 40)
        laneInfoMap[0x00090005] = Pair(R.drawable.global_image_landfront_95, 41)
        laneInfoMap[0x000a0000] = Pair(R.drawable.global_image_landfront_a0, 42)
        laneInfoMap[0x000a0008] = Pair(R.drawable.global_image_landfront_a8, 43)
        laneInfoMap[0x000b0001] = Pair(R.drawable.global_image_landfront_b1, 44)
        laneInfoMap[0x000b0005] = Pair(R.drawable.global_image_landfront_b5, 45)
        laneInfoMap[0x000c0003] = Pair(R.drawable.global_image_landfront_c3, 46)
        laneInfoMap[0x000c0008] = Pair(R.drawable.global_image_landfront_c8, 47)
        laneInfoMap[0x000d000d] = Pair(R.drawable.global_image_landfront_d, 29)
        laneInfoMap[0x000e0001] = Pair(R.drawable.global_image_landfront_e1, 48)
        laneInfoMap[0x000e0005] = Pair(R.drawable.global_image_landfront_e5, 49)
        laneInfoMap[0x00100000] = Pair(R.drawable.global_image_landfront_100, 72)
        laneInfoMap[0x00100001] = Pair(R.drawable.global_image_landfront_101, 73)
        laneInfoMap[0x00100005] = Pair(R.drawable.global_image_landfront_105, 74)
        laneInfoMap[0x00110003] = Pair(R.drawable.global_image_landfront_113, 75)
        laneInfoMap[0x00110005] = Pair(R.drawable.global_image_landfront_115, 76)
        laneInfoMap[0x00120001] = Pair(R.drawable.global_image_landfront_121, 77)
        laneInfoMap[0x00120003] = Pair(R.drawable.global_image_landfront_123, 78)
        laneInfoMap[0x00120005] = Pair(R.drawable.global_image_landfront_125, 79)
        laneInfoMap[0x00130000] = Pair(R.drawable.global_image_landfront_130, 80)
        laneInfoMap[0x00130003] = Pair(R.drawable.global_image_landfront_133, 81)
        laneInfoMap[0x00130005] = Pair(R.drawable.global_image_landfront_135, 82)
        laneInfoMap[0x00140001] = Pair(R.drawable.global_image_landfront_141, 83)
        laneInfoMap[0x00140008] = Pair(R.drawable.global_image_landfront_148, 84)
        laneInfoMap[0x00150015] = Pair(R.drawable.global_image_landfront_15, 62)
        laneInfoMap[0x00160016] = Pair(R.drawable.global_image_landfront_0, 16)//未规划
        laneInfoMap[0x00170017] = Pair(R.drawable.global_image_landfront_17, 63)
        laneInfoMap[0x00180018] = Pair(R.drawable.global_image_landfront_0, 89)//专用车道
        laneInfoMap[0x00190019] = Pair(R.drawable.global_image_landfront_19, 86)
    }
}