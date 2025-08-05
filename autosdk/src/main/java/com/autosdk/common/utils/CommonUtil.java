package com.autosdk.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.autonavi.gbl.pos.model.GPSDatetime;
import com.autosdk.R;
import com.autosdk.bussiness.common.utils.DeviceIdUtils;
import com.autosdk.common.SdkApplicationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CommonUtil {

    private static final String TAG = "CommonUtil";

    /**
     * 获取当前线程信息, 线程名/id/是否主线程等
     */
    public static String getThreadInfo() {
        Thread thread = Thread.currentThread();
        return "name:" + thread.getName() + ",id:" + thread.getId() + ",isMain:" + isMainThread();
    }

    /**
     * 判断当前是否在主线程
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    /**
     * 获取主线程id
     */
    public static long getMainThreadId() {
        return Looper.getMainLooper().getThread().getId();
    }

    /**
     * 获取当前线程id
     */
    public static long getcurrentThreadId() {
        return Thread.currentThread().getId();
    }

    //获取设备号
    @SuppressLint("HardwareIds")
    public static String getDeviceID(Context context) {
        return DeviceIdUtils.getDeviceId();
    }

    public static String getIMEINew(Context context) {
        //we make this look like a valid IMEI
        String imei = "35" +
            Build.BOARD.length() % 10 +
            Build.BRAND.length() % 10 +
            Build.CPU_ABI.length() % 10 +
            Build.DEVICE.length() % 10 +
            Build.DISPLAY.length() % 10 +
            Build.HOST.length() % 10 +
            Build.ID.length() % 10 +
            Build.MANUFACTURER.length() % 10 +
            Build.MODEL.length() % 10 +
            Build.PRODUCT.length() % 10 +
            Build.TAGS.length() % 10 +
            Build.TYPE.length() % 10 +
            Build.USER.length() % 10; //13 digits
        return imei;
    }

    /**
     * 距离单位转换：米转公里
     *
     * @param dis
     * @return
     */
    public static String distanceUnitTransform(long dis) {
        StringBuffer sb = new StringBuffer();
        int distance = (int) dis;
        if (distance >= 1000) {
            int kiloMeter = distance / 1000;
            int leftMeter = distance % 1000;
            leftMeter = leftMeter / 100;
            if (kiloMeter > 100) {
                sb.append(kiloMeter);
                sb.append(getResources().getString(com.autosdk.R.string.km));
            } else if (leftMeter > 0) {
                sb.append(kiloMeter);
                sb.append(".");
                sb.append(leftMeter);
                sb.append(getResources().getString(com.autosdk.R.string.km));
            } else {
                sb.append(kiloMeter);
                sb.append(getResources().getString(com.autosdk.R.string.km));
            }
        } else {
            sb.append(distance);
            sb.append(getResources().getString(com.autosdk.R.string.route_meter));
        }
        return sb.toString();
    }
    @SuppressLint("DefaultLocale")
    public static String distanceUnitTransformKm(long dis) {
        StringBuffer sb = new StringBuffer();
        int distance = (int) dis;
        if (distance >= 1000) {
            int kiloMeter = distance / 1000;
            sb.append(kiloMeter);
            sb.append("km");
        } else if (distance >= 100){
            sb.append(String.format("%.1f", distance / 1000.0));
            sb.append("km");
        }else {
            sb.append(distance);
            sb.append("m");
        }

        return sb.toString();
    }

    public static Resources getResources() {
        return SdkApplicationUtils.getApplication().getApplicationContext().getResources();
    }

    public static String switchFromSecond(int second) {
        String restTime = "";
        String minuteString = SdkApplicationUtils.getApplication().getString(R.string.minute);
        String hourString = SdkApplicationUtils.getApplication().getString(R.string.hour);

        int minute = (second + 30) / 60;

        if (minute < 60) { // 小于1小时
            if (minute == 0) {
                restTime = "<1" + minuteString;
            } else {
                restTime = minute + minuteString;
            }
        } else { // 大于小于1小时，小于24小时
            int hour = minute / 60;
            minute = minute % 60;
            if (minute > 0) {
                restTime = hour + hourString + minute + minuteString;
            } else {
                restTime = hour + hourString;
            }
        }

        return restTime;
    }

    public static boolean isCarNumber(String carnumber) {
   /*
   1.常规车牌号：仅允许以汉字开头，后面可录入六个字符，由大写英文字母和阿拉伯数字组成。如：粤B12345；
   2.武警车牌：允许前两位为大写英文字母，后面可录入五个或六个字符，由大写英文字母和阿拉伯数字组成，其中第三位可录汉字也可录大写英文字母及阿拉伯数字，第三位也可空，如：WJ警00081、WJ京1234J、WJ1234X。
   3.最后一个为汉字的车牌：允许以汉字开头，后面可录入六个字符，前五位字符，由大写英文字母和阿拉伯数字组成，而最后一个字符为汉字，汉字包括“挂”、“学”、“警”、“军”、“港”、“澳”。如：粤Z1234港。
   4.新军车牌：以两位为大写英文字母开头，后面以5位阿拉伯数字组成。如：BA12345。
       */
        /**
         * 新能源车
         *      组成：省份简称（1位汉字）+发牌机关代号（1位字母）+序号（6位），总计8个字符，序号不能出现字母I和字母O
         *      通用规则：不区分大小写，第一位：省份简称（1位汉字），第二位：发牌机关代号（1位字母）
         *      序号位：
         *      小型车，第一位：只能用字母D或字母F，第二位：字母或者数字，后四位：必须使用数字
         *      ---([DF][A-HJ-NP-Z0-9][0-9]{4})
         *      大型车，前五位：必须使用数字，第六位：只能用字母D或字母F。
         *       ----([0-9]{5}[DF])
         */
        String carnumRegex = "^([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[警京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼]{0,1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1})$";
        if (TextUtils.isEmpty(carnumber)) {
            return false;
        } else {
            return carnumber.matches(carnumRegex);
        }
    }

    /**
     * 时间转换 几分钟前，几小时前
     *
     * @param time
     * @return
     */
    public static String switchTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        String createDate = null;
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createTime = sdf2.format(time);
        Date date = null;
        try {
            date = sdf2.parse(createTime);
            long differenceValue = System.currentTimeMillis() - date.getTime();
            if (differenceValue < 3600000) {
                if ((differenceValue / 1000 / 60) == 0) {
                    createDate = "刚刚";
                } else {
                    createDate = (differenceValue / 1000 / 60) + "分钟前";
                }

            } else if (differenceValue > 3600000) {
                if (differenceValue < 86400000) {
                    createDate = (differenceValue / 1000 / 60 / 60) + "小时前";
                } else {
                    createDate = sdf.format(time);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return createDate;
    }

    public static long parseGpsDateTime(GPSDatetime gpsDatetime) {
        if(gpsDatetime.year == 0){
            return  System.currentTimeMillis();
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpleDateFormat.parse(gpsDatetime.year + "-" + gpsDatetime.month + "-" + gpsDatetime.day + " " + gpsDatetime.hour + ":" + gpsDatetime.minute + ":" + gpsDatetime.second);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return System.currentTimeMillis();
        }
    }

    /**
     * 时间转换 日期
     *
     * @return
     */
    public static String switchDate(String date) {
        String switchTime = "";
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);

            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int second = c.get(Calendar.SECOND);
            int dayweek = c.get(Calendar.DAY_OF_WEEK);
            if (isNow(date)) {
                String hh = hour < 10 ? "0" + hour : hour + "";
                String mm = minute < 10 ? "0" + minute : minute + "";
                switchTime = "今天 " + hh + ":" + mm;
            } else {
                switchTime = month + "月" + day + "日";
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return switchTime;
    }

    public static String formatTimeBySecond(int second) {
        String restTime = "";
        String minuteString = SdkApplicationUtils.getApplication().getString(R.string.minute);
        String hourString = SdkApplicationUtils.getApplication().getString(R.string.hour);

        int minute = (second + 30) / 60;

        if (minute < 60) { // 小于1小时
            if (minute == 0) {
                restTime = "<1" + minuteString;
            } else {
                restTime = minute + minuteString;
            }
        } else { // 大于小于1小时，小于24小时
            int hour = minute / 60;
            minute = minute % 60;
            if (minute > 0) {
                restTime = hour + hourString + minute + minuteString;
            } else {
                restTime = hour + hourString;
            }
        }

        return restTime;
    }

    /**
     * 是否是今天
     *
     * @return
     */
    public static boolean isNow(String time) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = formatter.parse(time);
            Date now = new Date();
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            String nowDay = sf.format(now);
            String day = sf.format(date);
            return day.equals(nowDay);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //获取总页码
    public static int getPagerCount(int listSize, int pagerSize) {
        if (listSize == 0 || pagerSize == 0) {
            return 1;
        }

        if (listSize % pagerSize == 0) {
            return listSize / pagerSize;
        } else {
            return listSize / pagerSize + 1;
        }
    }

    private static final String[] CHINESE_NUMBERS = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] CHINESE_UNITS = {"", "十", "百", "千", "万", "亿"};

    //阿拉伯数字转字符串
    public static String numberToChinese(int number) {
        if (number == 0) {
            return CHINESE_NUMBERS[0];
        }

        StringBuilder chineseStr = new StringBuilder();
        int unitIndex = 0;
        boolean needZero = false;

        while (number > 0) {
            int digit = number % 10;
            if (digit > 0) {
                if (needZero) {
                    chineseStr.insert(0, CHINESE_NUMBERS[0]);
                }
                chineseStr.insert(0, CHINESE_UNITS[unitIndex]);
                chineseStr.insert(0, CHINESE_NUMBERS[digit]);
                needZero = false;
            } else {
                if (!needZero && unitIndex != 0 && unitIndex != 4) {
                    chineseStr.insert(0, CHINESE_NUMBERS[digit]);
                    needZero = true;
                }
            }
            number /= 10;
            unitIndex++;
        }

        return chineseStr.toString();
    }

}
