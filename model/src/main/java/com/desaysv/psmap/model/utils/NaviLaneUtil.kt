package com.desaysv.psmap.model.utils

import android.util.SparseIntArray
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.model.R
import timber.log.Timber

/**
 * @author 谢锦华
 * @time 2024/2/22
 * @description 获取车道线
 */

object NaviLaneUtil {
    private val laneResId = SparseIntArray(200)
    private val laneCrossResId = SparseIntArray(200)

    fun setLaneImg(key: Int): Int {
        try {
            Timber.i(" setLaneImg key:$key")
            val icon = laneResId[key and 0x00ff00ff]
            if (icon != 0) {
                return icon
            } else {
                Timber.i(" setLaneImg icon:0")
                if (NightModeGlobal.isNightMode()) {
                    return R.drawable.global_image_landfront_0_night
                } else {
                    return R.drawable.global_image_landfront_0
                }
            }
        } catch (e: Exception) {
            Timber.i(" setLaneImg Exception e:%s", e.message)
        }
        return -1
    }

    fun getTollBoothsResId(key: Int): Int {
        return when (key) {
            2 -> if (NightModeGlobal.isNightMode()) R.drawable.global_image_landfront_etc else R.drawable.global_image_landfront_etc_day///**< 支持ETC收费车道 */
            else -> {
                if (NightModeGlobal.isNightMode()) R.drawable.global_image_landfront_toll else R.drawable.global_image_landfront_toll_day///**< 普通收费车道 */
            }
        }
    }

    fun initLaneResId() {
        if (NightModeGlobal.isNightMode()) {
            //巡航车道
            laneResId.put(0x000000ee, R.drawable.global_image_auto_landback_0_night)
            laneResId.put(0x000100ee, R.drawable.global_image_auto_landback_1_night)
            laneResId.put(0x000200ee, R.drawable.global_image_auto_landback_2_night)
            laneResId.put(0x000300ee, R.drawable.global_image_auto_landback_3_night)
            laneResId.put(0x000400ee, R.drawable.global_image_auto_landback_4_night)
            laneResId.put(0x000500ee, R.drawable.global_image_auto_landback_5_night)
            laneResId.put(0x000600ee, R.drawable.global_image_auto_landback_6_night)
            laneResId.put(0x000700ee, R.drawable.global_image_auto_landback_7_night)
            laneResId.put(0x000800ee, R.drawable.global_image_auto_landback_8_night)
            laneResId.put(0x000900ee, R.drawable.global_image_auto_landback_9_night)
            laneResId.put(0x000a00ee, R.drawable.global_image_auto_landback_a_night)
            laneResId.put(0x000b00ee, R.drawable.global_image_auto_landback_b_night)
            laneResId.put(0x000c00ee, R.drawable.global_image_auto_landback_c_night)
            laneResId.put(0x000d00ee, R.drawable.global_image_auto_landback_d_night)
            laneResId.put(0x000e00ee, R.drawable.global_image_auto_landback_e_night)
            laneResId.put(0x001000ee, R.drawable.global_image_auto_landback_10_night)
            laneResId.put(0x001100ee, R.drawable.global_image_auto_landback_11_night)
            laneResId.put(0x001200ee, R.drawable.global_image_auto_landback_12_night)
            laneResId.put(0x001300ee, R.drawable.global_image_auto_landback_13_night)
            laneResId.put(0x001400ee, R.drawable.global_image_auto_landback_14_night)
            laneResId.put(0x001500ee, R.drawable.global_image_auto_landback_15_night)
            laneResId.put(0x001700ee, R.drawable.global_image_auto_landback_17_night)
            laneResId.put(0x001800ee, R.drawable.global_image_auto_landback_18_night)
            laneResId.put(0x001900ee, R.drawable.global_image_auto_landback_19_night)
            laneResId.put(0x00160016, R.drawable.global_image_auto_landback_0_night)
            //背景车道
            laneResId.put(0x000000ff, R.drawable.global_image_landback_0_night)
            laneResId.put(0x000100ff, R.drawable.global_image_landback_1_night)
            laneResId.put(0x000200ff, R.drawable.global_image_landback_2_night)
            laneResId.put(0x000300ff, R.drawable.global_image_landback_3_night)
            laneResId.put(0x000400ff, R.drawable.global_image_landback_4_night)
            laneResId.put(0x000500ff, R.drawable.global_image_landback_5_night)
            laneResId.put(0x000600ff, R.drawable.global_image_landback_6_night)
            laneResId.put(0x000700ff, R.drawable.global_image_landback_7_night)
            laneResId.put(0x000800ff, R.drawable.global_image_landback_8_night)
            laneResId.put(0x000900ff, R.drawable.global_image_landback_9_night)
            laneResId.put(0x000a00ff, R.drawable.global_image_landback_a_night)
            laneResId.put(0x000b00ff, R.drawable.global_image_landback_b_night)
            laneResId.put(0x000c00ff, R.drawable.global_image_landback_c_night)
            laneResId.put(0x000d00ff, R.drawable.global_image_landback_d_night)
            laneResId.put(0x000e00ff, R.drawable.global_image_landback_e_night)
            laneResId.put(0x001000ff, R.drawable.global_image_landback_10_night)
            laneResId.put(0x001100ff, R.drawable.global_image_landback_11_night)
            laneResId.put(0x001200ff, R.drawable.global_image_landback_12_night)
            laneResId.put(0x001300ff, R.drawable.global_image_landback_13_night)
            laneResId.put(0x001400ff, R.drawable.global_image_landback_14_night)
            laneResId.put(0x001500ff, R.drawable.global_image_landback_15_night)
            laneResId.put(0x001700ff, R.drawable.global_image_landback_17_night)
            laneResId.put(0x001800ff, R.drawable.global_image_landback_18_night)
            laneResId.put(0x001900ff, R.drawable.global_image_landback_19_night)
            laneResId.put(0x001600FF, R.drawable.global_image_landback_0_night)
            //前景车道
            laneResId.put(0x00000000, R.drawable.global_image_landfront_0_night)
            laneResId.put(0x00010001, R.drawable.global_image_landfront_1_night)
            laneResId.put(0x00020000, R.drawable.global_image_landfront_20_night)
            laneResId.put(0x00020001, R.drawable.global_image_landfront_21_night)
            laneResId.put(0x00030003, R.drawable.global_image_landfront_3_night)
            laneResId.put(0x00040000, R.drawable.global_image_landfront_40_night)
            laneResId.put(0x00040003, R.drawable.global_image_landfront_43_night)
            laneResId.put(0x00050005, R.drawable.global_image_landfront_5_night)
            laneResId.put(0x00060001, R.drawable.global_image_landfront_61_night)
            laneResId.put(0x00060003, R.drawable.global_image_landfront_63_night)
            laneResId.put(0x00070000, R.drawable.global_image_landfront_70_night)
            laneResId.put(0x00070001, R.drawable.global_image_landfront_71_night)
            laneResId.put(0x00070003, R.drawable.global_image_landfront_73_night)
            laneResId.put(0x00080008, R.drawable.global_image_landfront_8_night)
            laneResId.put(0x00090000, R.drawable.global_image_landfront_90_night)
            laneResId.put(0x00090005, R.drawable.global_image_landfront_95_night)
            laneResId.put(0x000a0000, R.drawable.global_image_landfront_a0_night)
            laneResId.put(0x000a0008, R.drawable.global_image_landfront_a8_night)
            laneResId.put(0x000b0001, R.drawable.global_image_landfront_b1_night)
            laneResId.put(0x000b0005, R.drawable.global_image_landfront_b5_night)
            laneResId.put(0x000c0003, R.drawable.global_image_landfront_c3_night)
            laneResId.put(0x000c0008, R.drawable.global_image_landfront_c8_night)
            laneResId.put(0x000d000d, R.drawable.global_image_landfront_d_night)
            laneResId.put(0x000e0001, R.drawable.global_image_landfront_e1_night)
            laneResId.put(0x000e0005, R.drawable.global_image_landfront_e5_night)
            laneResId.put(0x00100000, R.drawable.global_image_landfront_100_night)
            laneResId.put(0x00100001, R.drawable.global_image_landfront_101_night)
            laneResId.put(0x00100005, R.drawable.global_image_landfront_105_night)
            laneResId.put(0x00110003, R.drawable.global_image_landfront_113_night)
            laneResId.put(0x00110005, R.drawable.global_image_landfront_115_night)
            laneResId.put(0x00120001, R.drawable.global_image_landfront_121_night)
            laneResId.put(0x00120003, R.drawable.global_image_landfront_123_night)
            laneResId.put(0x00120005, R.drawable.global_image_landfront_125_night)
            laneResId.put(0x00130000, R.drawable.global_image_landfront_130_night)
            laneResId.put(0x00130003, R.drawable.global_image_landfront_133_night)
            laneResId.put(0x00130005, R.drawable.global_image_landfront_135_night)
            laneResId.put(0x00140001, R.drawable.global_image_landfront_141_night)
            laneResId.put(0x00140008, R.drawable.global_image_landfront_148_night)
            laneResId.put(0x00150015, R.drawable.global_image_landfront_15_night)
            laneResId.put(0x00170017, R.drawable.global_image_landfront_17_night)
            laneResId.put(0x00180018, R.drawable.global_image_landfront_18_night)
            laneResId.put(0x00190019, R.drawable.global_image_landfront_19_night)
        } else {
            //巡航车道
            laneResId.put(0x000000ee, R.drawable.global_image_auto_landback_0)
            laneResId.put(0x000100ee, R.drawable.global_image_auto_landback_1)
            laneResId.put(0x000200ee, R.drawable.global_image_auto_landback_2)
            laneResId.put(0x000300ee, R.drawable.global_image_auto_landback_3)
            laneResId.put(0x000400ee, R.drawable.global_image_auto_landback_4)
            laneResId.put(0x000500ee, R.drawable.global_image_auto_landback_5)
            laneResId.put(0x000600ee, R.drawable.global_image_auto_landback_6)
            laneResId.put(0x000700ee, R.drawable.global_image_auto_landback_7)
            laneResId.put(0x000800ee, R.drawable.global_image_auto_landback_8)
            laneResId.put(0x000900ee, R.drawable.global_image_auto_landback_9)
            laneResId.put(0x000a00ee, R.drawable.global_image_auto_landback_a)
            laneResId.put(0x000b00ee, R.drawable.global_image_auto_landback_b)
            laneResId.put(0x000c00ee, R.drawable.global_image_auto_landback_c)
            laneResId.put(0x000d00ee, R.drawable.global_image_auto_landback_d)
            laneResId.put(0x000e00ee, R.drawable.global_image_auto_landback_e)
            laneResId.put(0x001000ee, R.drawable.global_image_auto_landback_10)
            laneResId.put(0x001100ee, R.drawable.global_image_auto_landback_11)
            laneResId.put(0x001200ee, R.drawable.global_image_auto_landback_12)
            laneResId.put(0x001300ee, R.drawable.global_image_auto_landback_13)
            laneResId.put(0x001400ee, R.drawable.global_image_auto_landback_14)
            laneResId.put(0x001500ee, R.drawable.global_image_auto_landback_15)
            laneResId.put(0x001700ee, R.drawable.global_image_auto_landback_17)
            laneResId.put(0x001800ee, R.drawable.global_image_auto_landback_18)
            laneResId.put(0x001900ee, R.drawable.global_image_auto_landback_19)
            laneResId.put(0x00160016, R.drawable.global_image_auto_landback_0)
            //背景车道
            laneResId.put(0x000000ff, R.drawable.global_image_landback_0)
            laneResId.put(0x000100ff, R.drawable.global_image_landback_1)
            laneResId.put(0x000200ff, R.drawable.global_image_landback_2)
            laneResId.put(0x000300ff, R.drawable.global_image_landback_3)
            laneResId.put(0x000400ff, R.drawable.global_image_landback_4)
            laneResId.put(0x000500ff, R.drawable.global_image_landback_5)
            laneResId.put(0x000600ff, R.drawable.global_image_landback_6)
            laneResId.put(0x000700ff, R.drawable.global_image_landback_7)
            laneResId.put(0x000800ff, R.drawable.global_image_landback_8)
            laneResId.put(0x000900ff, R.drawable.global_image_landback_9)
            laneResId.put(0x000a00ff, R.drawable.global_image_landback_a)
            laneResId.put(0x000b00ff, R.drawable.global_image_landback_b)
            laneResId.put(0x000c00ff, R.drawable.global_image_landback_c)
            laneResId.put(0x000d00ff, R.drawable.global_image_landback_d)
            laneResId.put(0x000e00ff, R.drawable.global_image_landback_e)
            laneResId.put(0x001000ff, R.drawable.global_image_landback_10)
            laneResId.put(0x001100ff, R.drawable.global_image_landback_11)
            laneResId.put(0x001200ff, R.drawable.global_image_landback_12)
            laneResId.put(0x001300ff, R.drawable.global_image_landback_13)
            laneResId.put(0x001400ff, R.drawable.global_image_landback_14)
            laneResId.put(0x001500ff, R.drawable.global_image_landback_15)
            laneResId.put(0x001700ff, R.drawable.global_image_landback_17)
            laneResId.put(0x001800ff, R.drawable.global_image_landback_18)
            laneResId.put(0x001900ff, R.drawable.global_image_landback_19)
            laneResId.put(0x001600FF, R.drawable.global_image_landback_0)
            //前景车道
            laneResId.put(0x00000000, R.drawable.global_image_landfront_0)
            laneResId.put(0x00010001, R.drawable.global_image_landfront_1)
            laneResId.put(0x00020000, R.drawable.global_image_landfront_20)
            laneResId.put(0x00020001, R.drawable.global_image_landfront_21)
            laneResId.put(0x00030003, R.drawable.global_image_landfront_3)
            laneResId.put(0x00040000, R.drawable.global_image_landfront_40)
            laneResId.put(0x00040003, R.drawable.global_image_landfront_43)
            laneResId.put(0x00050005, R.drawable.global_image_landfront_5)
            laneResId.put(0x00060001, R.drawable.global_image_landfront_61)
            laneResId.put(0x00060003, R.drawable.global_image_landfront_63)
            laneResId.put(0x00070000, R.drawable.global_image_landfront_70)
            laneResId.put(0x00070001, R.drawable.global_image_landfront_71)
            laneResId.put(0x00070003, R.drawable.global_image_landfront_73)
            laneResId.put(0x00080008, R.drawable.global_image_landfront_8)
            laneResId.put(0x00090000, R.drawable.global_image_landfront_90)
            laneResId.put(0x00090005, R.drawable.global_image_landfront_95)
            laneResId.put(0x000a0000, R.drawable.global_image_landfront_a0)
            laneResId.put(0x000a0008, R.drawable.global_image_landfront_a8)
            laneResId.put(0x000b0001, R.drawable.global_image_landfront_b1)
            laneResId.put(0x000b0005, R.drawable.global_image_landfront_b5)
            laneResId.put(0x000c0003, R.drawable.global_image_landfront_c3)
            laneResId.put(0x000c0008, R.drawable.global_image_landfront_c8)
            laneResId.put(0x000d000d, R.drawable.global_image_landfront_d)
            laneResId.put(0x000e0001, R.drawable.global_image_landfront_e1)
            laneResId.put(0x000e0005, R.drawable.global_image_landfront_e5)
            laneResId.put(0x00100000, R.drawable.global_image_landfront_100)
            laneResId.put(0x00100001, R.drawable.global_image_landfront_101)
            laneResId.put(0x00100005, R.drawable.global_image_landfront_105)
            laneResId.put(0x00110003, R.drawable.global_image_landfront_113)
            laneResId.put(0x00110005, R.drawable.global_image_landfront_115)
            laneResId.put(0x00120001, R.drawable.global_image_landfront_121)
            laneResId.put(0x00120003, R.drawable.global_image_landfront_123)
            laneResId.put(0x00120005, R.drawable.global_image_landfront_125)
            laneResId.put(0x00130000, R.drawable.global_image_landfront_130)
            laneResId.put(0x00130003, R.drawable.global_image_landfront_133)
            laneResId.put(0x00130005, R.drawable.global_image_landfront_135)
            laneResId.put(0x00140001, R.drawable.global_image_landfront_141)
            laneResId.put(0x00140008, R.drawable.global_image_landfront_148)
            laneResId.put(0x00150015, R.drawable.global_image_landfront_15)
            laneResId.put(0x00170017, R.drawable.global_image_landfront_17)
            laneResId.put(0x00180018, R.drawable.global_image_landfront_18)
            laneResId.put(0x00190019, R.drawable.global_image_landfront_19)
        }
    }


    /****************************************以下---路口大图显示后车道线的资源 全白 *************************************************/
    fun setCrossLaneImg(key: Int): Int {
        try {
            Timber.i(" setCrossLaneImg key:$key")
            val icon = laneCrossResId[key and 0x00ff00ff]
            if (icon != 0) {
                return icon
            } else {
                Timber.i(" setCrossLaneImg icon:0")
                return R.drawable.global_image_landfront_0_night
            }
        } catch (e: Exception) {
            Timber.i(" setCrossLaneImg Exception e:%s", e.message)
        }
        return -1
    }

    fun getCrossTollBoothsResId(key: Int): Int {
        return when (key) {
            2 -> R.drawable.global_image_landfront_etc///**< 支持ETC收费车道 */
            else -> {
                R.drawable.global_image_landfront_toll///**< 普通收费车道 */
            }
        }
    }

    fun initCrossLaneResId() {
        //背景车道
        laneCrossResId.put(0x000000ff, R.drawable.global_image_landback_0_night)
        laneCrossResId.put(0x000100ff, R.drawable.global_image_landback_1_night)
        laneCrossResId.put(0x000200ff, R.drawable.global_image_landback_2_night)
        laneCrossResId.put(0x000300ff, R.drawable.global_image_landback_3_night)
        laneCrossResId.put(0x000400ff, R.drawable.global_image_landback_4_night)
        laneCrossResId.put(0x000500ff, R.drawable.global_image_landback_5_night)
        laneCrossResId.put(0x000600ff, R.drawable.global_image_landback_6_night)
        laneCrossResId.put(0x000700ff, R.drawable.global_image_landback_7_night)
        laneCrossResId.put(0x000800ff, R.drawable.global_image_landback_8_night)
        laneCrossResId.put(0x000900ff, R.drawable.global_image_landback_9_night)
        laneCrossResId.put(0x000a00ff, R.drawable.global_image_landback_a_night)
        laneCrossResId.put(0x000b00ff, R.drawable.global_image_landback_b_night)
        laneCrossResId.put(0x000c00ff, R.drawable.global_image_landback_c_night)
        laneCrossResId.put(0x000d00ff, R.drawable.global_image_landback_d_night)
        laneCrossResId.put(0x000e00ff, R.drawable.global_image_landback_e_night)
        laneCrossResId.put(0x001000ff, R.drawable.global_image_landback_10_night)
        laneCrossResId.put(0x001100ff, R.drawable.global_image_landback_11_night)
        laneCrossResId.put(0x001200ff, R.drawable.global_image_landback_12_night)
        laneCrossResId.put(0x001300ff, R.drawable.global_image_landback_13_night)
        laneCrossResId.put(0x001400ff, R.drawable.global_image_landback_14_night)
        laneCrossResId.put(0x001500ff, R.drawable.global_image_landback_15_night)
        laneCrossResId.put(0x001700ff, R.drawable.global_image_landback_17_night)
        laneCrossResId.put(0x001900ff, R.drawable.global_image_landback_19_night)
        laneCrossResId.put(0x001600FF, R.drawable.global_image_landback_0_night)
        //前景车道
        laneCrossResId.put(0x00000000, R.drawable.global_image_landfront_0_night)
        laneCrossResId.put(0x00010001, R.drawable.global_image_landfront_1_night)
        laneCrossResId.put(0x00020000, R.drawable.global_image_landfront_20_night)
        laneCrossResId.put(0x00020001, R.drawable.global_image_landfront_21_night)
        laneCrossResId.put(0x00030003, R.drawable.global_image_landfront_3_night)
        laneCrossResId.put(0x00040000, R.drawable.global_image_landfront_40_night)
        laneCrossResId.put(0x00040003, R.drawable.global_image_landfront_43_night)
        laneCrossResId.put(0x00050005, R.drawable.global_image_landfront_5_night)
        laneCrossResId.put(0x00060001, R.drawable.global_image_landfront_61_night)
        laneCrossResId.put(0x00060003, R.drawable.global_image_landfront_63_night)
        laneCrossResId.put(0x00070000, R.drawable.global_image_landfront_70_night)
        laneCrossResId.put(0x00070001, R.drawable.global_image_landfront_71_night)
        laneCrossResId.put(0x00070003, R.drawable.global_image_landfront_73_night)
        laneCrossResId.put(0x00080008, R.drawable.global_image_landfront_8_night)
        laneCrossResId.put(0x00090000, R.drawable.global_image_landfront_90_night)
        laneCrossResId.put(0x00090005, R.drawable.global_image_landfront_95_night)
        laneCrossResId.put(0x000a0000, R.drawable.global_image_landfront_a0_night)
        laneCrossResId.put(0x000a0008, R.drawable.global_image_landfront_a8_night)
        laneCrossResId.put(0x000b0001, R.drawable.global_image_landfront_b1_night)
        laneCrossResId.put(0x000b0005, R.drawable.global_image_landfront_b5_night)
        laneCrossResId.put(0x000c0003, R.drawable.global_image_landfront_c3_night)
        laneCrossResId.put(0x000c0008, R.drawable.global_image_landfront_c8_night)
        laneCrossResId.put(0x000d000d, R.drawable.global_image_landfront_d_night)
        laneCrossResId.put(0x000e0001, R.drawable.global_image_landfront_e1_night)
        laneCrossResId.put(0x000e0005, R.drawable.global_image_landfront_e5_night)
        laneCrossResId.put(0x00100000, R.drawable.global_image_landfront_100_night)
        laneCrossResId.put(0x00100001, R.drawable.global_image_landfront_101_night)
        laneCrossResId.put(0x00100005, R.drawable.global_image_landfront_105_night)
        laneCrossResId.put(0x00110003, R.drawable.global_image_landfront_113_night)
        laneCrossResId.put(0x00110005, R.drawable.global_image_landfront_115_night)
        laneCrossResId.put(0x00120001, R.drawable.global_image_landfront_121_night)
        laneCrossResId.put(0x00120003, R.drawable.global_image_landfront_123_night)
        laneCrossResId.put(0x00120005, R.drawable.global_image_landfront_125_night)
        laneCrossResId.put(0x00130000, R.drawable.global_image_landfront_130_night)
        laneCrossResId.put(0x00130003, R.drawable.global_image_landfront_133_night)
        laneCrossResId.put(0x00130005, R.drawable.global_image_landfront_135_night)
        laneCrossResId.put(0x00140001, R.drawable.global_image_landfront_141_night)
        laneCrossResId.put(0x00140008, R.drawable.global_image_landfront_148_night)
        laneCrossResId.put(0x00150015, R.drawable.global_image_landfront_15_night)
        laneCrossResId.put(0x00170017, R.drawable.global_image_landfront_17_night)
        laneCrossResId.put(0x00190019, R.drawable.global_image_landfront_19_night)

    }

}