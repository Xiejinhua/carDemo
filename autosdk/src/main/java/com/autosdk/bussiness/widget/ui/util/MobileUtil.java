package com.autosdk.bussiness.widget.ui.util;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by AutoSdk on 2020/10/23.
 **/
public class MobileUtil {
    /**
     * 电信号码格式验证 手机段： 133,149,153,173,177,180,181,189,199,1349,1410,1700,1701,1702
     **/
    private static final String TELECOM_PATTERN = "(?:^(?:\\+86)?1(?:33|49|53|7[37]|8[019]|99|95)\\d{8}$)|(?:^(?:\\+86)?1349\\d{7}$)|(?:^(?:\\+86)?1410\\d{7}$)|(?:^(?:\\+86)?170[0-2]\\d{7}$)";

    /**
     * 联通号码格式验证 手机段：130,131,132,145,146,155,156,166,171,175,176,185,186,1704,1707,1708,1709
     **/
    private static final String UNICOM_PATTERN = "(?:^(?:\\+86)?1(?:3[0-2]|4[56]|5[56]|66|7[156]|8[56]|95)\\d{8}$)|(?:^(?:\\+86)?170[47-9]\\d{7}$)";

    /**
     * 移动号码格式验证
     * 手机段：134,135,136,137,138,139,147,148,150,151,152,157,158,159,178,182,183,184,187,188,195,198,1440,1703,1705,1706
     **/
    private static final String MOBILE_PATTERN = "(?:^(?:\\+86)?1(?:3[4-9]|4[78]|5[0-27-9]|78|8[2-478]|98|95)\\d{8}$)|(?:^(?:\\+86)?1440\\d{7}$)|(?:^(?:\\+86)?170[356]\\d{7}$)";

    /**
     * 手机号码校验
     *
     * @param phone
     * @return
     */
    public static boolean checkPhone(String phone) {
        if (!TextUtils.isEmpty(phone)) {
            if (checkMobile(phone) || checkUnicom(phone) || checkTelecom(phone)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 移动手机号码校验
     *
     * @param phone
     * @return
     */
    public static boolean checkMobile(String phone) {
        if (!TextUtils.isEmpty(phone)) {
            Pattern regexp = Pattern.compile(MOBILE_PATTERN);
            if (regexp.matcher(phone).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 联通手机号码校验
     *
     * @param phone
     * @return
     */
    public static boolean checkUnicom(String phone) {
        if (!TextUtils.isEmpty(phone)) {
            Pattern regexp = Pattern.compile(UNICOM_PATTERN);
            if (regexp.matcher(phone).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 电信手机号码校验
     *
     * @param phone
     * @return
     */
    public static boolean checkTelecom(String phone) {
        if (!TextUtils.isEmpty(phone)) {
            Pattern regexp = Pattern.compile(TELECOM_PATTERN);
            if (regexp.matcher(phone).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 隐藏手机号中间四位
     *
     * @param phone
     * @return java.lang.String
     */
    public static String hideMiddleMobile(String phone) {
        if (!TextUtils.isEmpty(phone)) {
            phone = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }
        return phone;
    }

    /**
     * 手机号码校验
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        String pattern = "^1[3-9]\\d{9}$";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(phoneNumber);
        return matcher.matches();
    }

}
