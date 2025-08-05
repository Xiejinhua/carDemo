package com.autosdk.bussiness.search.utils;

import android.text.TextUtils;

import java.text.NumberFormat;

import androidx.annotation.Nullable;

public class NumberUtil {

    /**
     * 字符串转为10进制的数字
     * @param defaultValue 若转换失败,则返回默认值
     */
    public static int str2Int(@Nullable String src, int defaultValue) {
        return str2Int(src, 10, defaultValue);
    }

    /**
     * 字符串转数字
     *
     * @param radix        进制,默认10
     * @param defaultValue 若转换失败,则返回默认值
     */
    public static int str2Int(@Nullable String src, int radix, int defaultValue) {
        if (TextUtils.isEmpty(src)) {
            return defaultValue;
        }

        try {
            assert src != null;
            return Integer.parseInt(src, radix);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static long str2Long(@Nullable String src, int radix, long defaultValue) {
        if (TextUtils.isEmpty(src)) {
            return defaultValue;
        }

        try {
            assert src != null;
            return Long.parseLong(src, radix);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static float str2Float(@Nullable String src, float defaultValue) {
        if (TextUtils.isEmpty(src)) {
            return defaultValue;
        }

        try {
            assert src != null;
            return Float.parseFloat(src);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static double str2Double(@Nullable String src, double defaultValue) {
        if (TextUtils.isEmpty(src)) {
            return defaultValue;
        }

        try {
            assert src != null;
            return Double.parseDouble(src);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * double的科学计数法转 string
     */
    public static String scientificNotation2str(double d) {
        String dstr = "" + d;
        if (!dstr.toLowerCase().contains("e")) {
            return dstr;
        }

        NumberFormat nf = NumberFormat.getInstance();
        // 是否以逗号隔开, 默认true以逗号隔开,如[123,456,789.128]
        nf.setGroupingUsed(false);
        // 结果未做任何处理
        return nf.format(d);
    }
}
