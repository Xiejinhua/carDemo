package com.desaysv.psmap.model.utils

import timber.log.Timber
import java.util.regex.Pattern

/**
 * Author : wangmansheng
 * Description : 车辆号码检测工具
 */
object PlateNumberUtil {
    /**
     * 判断是否已数字开头
     * @param str
     * @return
     */
    fun isNumeric(str: String): Boolean {
        return Character.isDigit(str[1])
    }

    /**
     * 车牌号码是否正确
     * @param num
     * @return
     */
    fun isPlateNumber(num: String): Boolean {
        return if (num.length == 8) { //新能源--普通八位判断
            if (ALL_LETTER_PLATE_PATTERN.matcher(num).matches()) { //省份后带7位字母
                Timber.d("isPlateNumber 省份后带7位字母")
                false
            }
//            else if (ALL_NUMBER_PLATE_PATTERN.matcher(num).matches()) { //省份后1位字母+6位数字
//                Timber.d("isPlateNumber 省份后1位字母+6位数字")
//                false
//            }
            else {
                NORMAL_ENERGY_PLATE_NUMBER_PATTERN.matcher(num).matches()
            }
        } else {
            PLATE_NUMBER_PATTERN.matcher(num).matches()
        }
    }

    /**
     * 车牌号码Pattern
     */
    private val PLATE_NUMBER_PATTERN: Pattern =
        Pattern //            .compile("^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$");
            .compile("^[京津冀鲁晋蒙辽吉黑沪渝川贵云藏陕甘青琼苏浙皖闽赣豫鄂湘粤桂宁港澳台警领使新学A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$")

    /**
     * 新能源车牌号码Pattern
     */
    private val ENERGY_PLATE_NUMBER_PATTERN = Pattern
        .compile(
            "^([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[a-zA-Z](([DF]((?![IO])[a-zA-Z0-9](?![IO]))[0-9]{4})|([0-9]{5}[DF]))" +
                    "|[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1})$"
        )

    /**
     * 新能源车牌号码Pattern--普通八位判断
     */
    private val NORMAL_ENERGY_PLATE_NUMBER_PATTERN = Pattern
        .compile("^[京津冀鲁晋蒙辽吉黑沪渝川贵云藏陕甘青琼苏浙皖闽赣豫鄂湘粤桂宁港澳台警领使新学A-Z]{1}[a-zA-Z]{1}([a-zA-Z0-9](?![IO])){6}$")

    /**
     * 车牌号码Pattern--省份后带7位字母
     */
    private val ALL_LETTER_PLATE_PATTERN = Pattern
        .compile("^[京津冀鲁晋蒙辽吉黑沪渝川贵云藏陕甘青琼苏浙皖闽赣豫鄂湘粤桂宁港澳台警领使新学A-Z]{1}([a-zA-Z](?![IO])){7}$")

    /**
     * 新能源车牌号码Pattern--省份后1位字母+6位数字
     */
    private val ALL_NUMBER_PLATE_PATTERN = Pattern
        .compile("^[京津冀鲁晋蒙辽吉黑沪渝川贵云藏陕甘青琼苏浙皖闽赣豫鄂湘粤桂宁港澳台警领使新学A-Z]{1}[a-zA-Z]{1}[0-9]{6}$")
}
