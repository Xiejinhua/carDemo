package com.autosdk.bussiness.widget.route.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.autonavi.gbl.common.path.option.RouteConstrainCode;
import com.autonavi.gbl.common.path.option.RouteStrategy;
import com.autosdk.R;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.common.utils.ElectricInfoConverter;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;
import com.autosdk.bussiness.widget.navi.utils.ResUtil;
import com.autosdk.bussiness.widget.route.constant.ConfigRoutePreference;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import timber.log.Timber;

/**
 * 路线转换工具
 *
 * @author AutoSDK
 */
public class AutoRouteUtil {

    /**
     * 获取时间字符串
     *
     * @param second
     * @return
     */
    public static String getTimeStr(long second) {
        int minute = (int) ((second + 30) / 60);
        String restTime = "";
        Resources res = BusinessApplicationUtils.getApplication().getResources();
        if (minute < 60) {
            //小于1小时
            if (minute == 0) {
                minute = 1;
            }
            restTime = minute + res.getString(R.string.route_minutes);
        } else {
            //小于1天
            int hour = minute / 60;
            restTime = hour + res.getString(R.string.route_hour);
            minute = minute % 60;
            if (minute > 0) {
                restTime = restTime + minute + res.getString(R.string.route_minutes);
            }
        }
        return restTime;
    }

    /**
     * 根据产品定义的策略，路线规划页面不做TBT策略取整处理，直接四舍五入取值
     *
     * @param dis
     * @return
     */
    public static String routeResultDistance(long dis) {
        StringBuffer sb = new StringBuffer();
        int distance = (int) dis;
        if (distance >= 1000) {
            int kiloMeter = distance / 1000;
            int leftMeter = distance % 1000;
            leftMeter = leftMeter / 100;
            if (kiloMeter > 100) {
                sb.append(kiloMeter);
                sb.append(ResUtil.getString(com.autosdk.R.string.km));
            } else if (leftMeter > 0) {
                sb.append(kiloMeter);
                sb.append(".");
                sb.append(leftMeter);
                sb.append(ResUtil.getString(com.autosdk.R.string.km));
            } else {
                sb.append(kiloMeter);
                sb.append(ResUtil.getString(com.autosdk.R.string.km));
            }
        } else {
            sb.append(distance);
            sb.append(ResUtil.getString(com.autosdk.R.string.route_meter));
        }
        return sb.toString();
    }

    public static String getRouteStrategyDesc(Context context, String routePreference) {
        //高德推荐＞躲避拥堵＞高速优先＞不走高速＞少收费＞大路优先＞速度最快
        //0,2,16,8,4,64,32,16384
        final HashMap<Integer, Integer> sortMap = new HashMap(8) {{
            put(0, 1);
            put(2, 2);
            put(16, 3);
            put(8, 4);
            put(4, 5);
            put(64, 6);
            put(32, 7);
            put(16384, 8);
        }};

        String[] prefs = routePreference.split("\\|");
        List<String> prefList = Arrays.asList(prefs);
        //对偏好设置重新排序
        Collections.sort(prefList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int key1 = Integer.parseInt(o1);
                int key2 = Integer.parseInt(o2);
                if (sortMap.get(key1) == null || sortMap.get(key2) == null) {
                    return 1;
                }
                return sortMap.get(key1).compareTo(sortMap.get(key2));
            }
        });
        String desc = "";
        for (int i = 0; i < prefs.length; i++) {
            if (!TextUtils.isEmpty(desc)) {
                desc += "、";
            }
            if (ConfigRoutePreference.PREFERENCE_DEFAULT.equals(prefs[i])) {
                desc += context.getString(R.string.preference_default_desc);
            } else if (ConfigRoutePreference.PREFERENCE_AVOID_JAN.equals(prefs[i])) {
                desc += context.getString(R.string.preference_avoid_jan_desc);
            } else if (ConfigRoutePreference.PREFERENCE_AVOID_CHARGE.equals(prefs[i])) {
                desc += context.getString(R.string.preference_avoid_charge_desc);
            } else if (ConfigRoutePreference.PREFERENCE_USING_HIGHWAY.equals(prefs[i])) {
                desc += context.getString(R.string.preference_using_highway_desc);
            } else if (ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY.equals(prefs[i])) {
                desc += context.getString(R.string.preference_avoid_highway_desc);
            } else if (ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST.equals(prefs[i])) {
                desc += context.getString(R.string.preference_personal_speed_first_desc);
            } else if (ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST.equals(prefs[i])) {
                desc += context.getString(R.string.preference_personal_width_first);
            } else if (ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE.equals(prefs[i])) {
                desc += context.getString(R.string.preference_route_electric_desc);
            }
        }
        return desc;
    }

    /**
     * 获取路线偏好名称
     */
    public static String getPlanShowInfoFromInt(String prefer) {
        //2：躲避拥堵； 4：避免收费； 8：不走高速； 16：高速优先 32：速度最快  64：大路优先
        String value = "0";
        switch (prefer) {
            case ConfigRoutePreference.PREFERENCE_DEFAULT:
                value = "智能推荐";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_JAN:
                value = "躲避拥堵";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_CHARGE:
                value = "避免收费";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY:
                value = "不走高速";
                break;
            case ConfigRoutePreference.PREFERENCE_USING_HIGHWAY:
                value = "高速优先";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE:
                value = "躲避拥堵、避免收费";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY:
                value = "躲避拥堵、不走高速";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY:
                value = "躲避拥堵、高速优先";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY:
                value = "避免收费、不走高速";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY:
                value = "躲避拥堵、避免收费、不走高速";
                break;
            case ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST:
                value = "速度最快";
                break;
            case ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST:
                value = "大路优先";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST:
                value = "躲避拥堵、速度最快";
                break;
            case ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST:
                value = "躲避拥堵、大路优先";
                break;
            default:
                value = "智能推荐";
        }
        return value;
    }

    /**
     * 通过ConfigRoutePreference转成BL内部需要的类型
     *
     * @param routePreference
     * @return
     */
    public static @RouteStrategy.RouteStrategy1 int getRouteStrategy(String routePreference, boolean isNetworkConnected) {
        int routeStrategy = RouteStrategy.RouteStrategyGaodeBest;
        if (routePreference.equals(ConfigRoutePreference.PREFERENCE_DEFAULT)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalGaodeBest;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC2LessHighway;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC2Highway;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalLessMoney;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalLessMoney2LessHighway;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalLessHighway;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_USING_HIGHWAY)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalHighwayFirst;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE)) {
            routeStrategy = RouteStrategy.RequestRouteTypeTMCFree;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC2LessMondy2LessHighway;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalSpeedFirst;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalWidthFirst;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC2SpeedFirst;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC2WidthFirst;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalGaodeBest;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_USING_HIGHWAY)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalHighwayFirst;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_SPEED_FIRST)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalSpeedFirst;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN_AND_USING_HIGHWAY)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC2Highway;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN_AND_SPEED_FIRST)) {
            routeStrategy = RouteStrategy.RouteStrategyPersonalTMC2SpeedFirst;
        }

        if (!isNetworkConnected && routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN) && routePreference.equals(ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST)
                && routePreference.equals(ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST)) {
            //离线且包含躲避拥堵、速度最快、大路优先
            routeStrategy = RouteStrategy.RouteStrategyGaodeBest;
        }
        return routeStrategy;
    }

    /**
     * 新能源设置冗余电量，对终点电量>15不经行接续算路
     *
     * @param routeConstrainCode
     * @param chargeLeft
     * @return
     */
    public static int updateRouteConstrainCode(int routeConstrainCode, int chargeLeft) {
        if (routeConstrainCode == RouteConstrainCode.RouteElecContinue && chargeLeft > 15) {
            return RouteConstrainCode.RouteCalcMulti | RouteConstrainCode.RouteNetWorking;
        }
        return routeConstrainCode;
    }

    /**
     * 通过ConfigRoutePreference转成BL内部需要的类型
     *
     * @param routePreference
     * @return
     */
    public static @RouteConstrainCode.RouteConstrainCode1 int getRouteConstrainCode(String routePreference, boolean isOpenElecContinue, boolean isNetworkConnected) {
        int routeConstrainCode = RouteConstrainCode.RouteCalcMulti | RouteConstrainCode.RouteNetWorking;
        if (routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE) || routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN)
                || routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_SPEED_FIRST)
                || routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_USING_HIGHWAY)
                || routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN_AND_SPEED_FIRST)
                || routePreference.equals(ConfigRoutePreference.PREFERENCE_ELECTRIC_ROUTE_AND_AVOID_JAN_AND_USING_HIGHWAY)) {
            routeConstrainCode = 0x4000;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_DEFAULT) || routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN) || routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE) ||
                routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE) || routePreference.equals(ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST)
                || routePreference.equals(ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST) || routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST)
                || routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST)) {
            routeConstrainCode = RouteConstrainCode.RouteCalcMulti | RouteConstrainCode.RouteNetWorking;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY) || routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY) ||
                routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY) ||
                routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY)) {
            routeConstrainCode = RouteConstrainCode.RouteCalcMulti | RouteConstrainCode.RouteAvoidFreeway | RouteConstrainCode.RouteNetWorking;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY) || routePreference.equals(ConfigRoutePreference.PREFERENCE_USING_HIGHWAY)) {
            routeConstrainCode = RouteConstrainCode.RouteCalcMulti | RouteConstrainCode.RouteFreewayStrategy | RouteConstrainCode.RouteNetWorking;
        }
        if (ElectricInfoConverter.isElectric() && isOpenElecContinue) {
            // 是否开启接续算路
            int electricConstrain = RouteConstrainCode.RouteElecContinue | RouteConstrainCode.RouteMultiContinueCalc;
            routeConstrainCode |= electricConstrain;
        }
        if (!isNetworkConnected) {
            routeConstrainCode |= RouteConstrainCode.RouteCalcLocal;
            Timber.e(" 离线算路ConstrainCode添加 routeConstrainCode %s", routeConstrainCode);
        }
        return routeConstrainCode;
    }

    /**
     * 根据设置获取引导策略
     *
     * @return
     */
    public static @RouteConstrainCode.RouteConstrainCode1 int getNaviStrategy(String routePreference) {
        // 算路策略,可组合，0x00:无策略,0x01:避免收费,0x02:不走高速,0x04:高速优先,0x08:躲避拥堵,0x10:大路优先,0x20:速度最快,0F:大于此值认为数据无效,当做无策略处理
        int strategy = 0x00;
        if (routePreference.equals(ConfigRoutePreference.PREFERENCE_DEFAULT)) {
            //不设置-即多策略
            strategy = 0x00;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN)) {
            //躲避拥堵
            strategy = 0x08;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE)) {
            //避免收费
            strategy = 0x01;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_HIGHWAY)) {
            //不走高速
            strategy = 0x02;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_USING_HIGHWAY)) {
            //高速优先
            strategy = 0x04;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_PERSONAL_SPEED_FIRST)) {
            //速度最快
            strategy = 0x20;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_PERSONAL_WIDTH_FIRST)) {
            //大路优先
            strategy = 0x10;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE)) {
            //避免收费+躲避拥堵
            strategy = 0x01 | 0x08;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_HGIHWAY)) {
            //不走高速+躲避拥堵
            strategy = 0x02 | 0x08;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_USING_HIGHWAY)) {
            //高速优先+躲避拥堵
            strategy = 0x04 | 0x08;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_HIGHWAY)) {
            //避免收费+不走高速
            strategy = 0x01 | 0x02;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_CHARGE_HIGHWAY)) {
            //避免收费+不走高速+躲避拥堵
            strategy = 0x01 | 0x02 | 0x08;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_CHARGE_AND_SPEED_FIRST)) {
            //躲避拥堵+速度最快
            strategy = 0x08 | 0x20;
        } else if (routePreference.equals(ConfigRoutePreference.PREFERENCE_AVOID_JAN_AND_WIDTH_FIRST)) {
            //躲避拥堵+大路优先
            strategy = 0x08 | 0x10;
        }
        return strategy;
    }

    /**
     * 获取预计到达时间
     *
     * @param context
     * @param second  还需要多长时间到达(单位：秒)
     **/
    public static String getScheduledTime(Context context, long second, boolean isShowTimeInterval, boolean isNavi) {
        StringBuffer timeBuffer = new StringBuffer();
        if (!isNavi) {
            timeBuffer.append("预计");
        }
        long timeLong = System.currentTimeMillis() + second * 1000;

        boolean isHoleDay = DateFormat.is24HourFormat(context.getApplicationContext());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeLong);
        int dayIndex = calendar.get(Calendar.AM_PM);
        int dayDiff = getDayDiff(calendar);

        if (dayDiff == 1) {
            timeBuffer.append(ResUtil.getString(com.autosdk.R.string.date_tomorrow));
        } else if (dayDiff > 1) {
            String[] strarr = new String[]{
                    "",
                    ResUtil.getString(com.autosdk.R.string.sunday),
                    ResUtil.getString(com.autosdk.R.string.monday),
                    ResUtil.getString(com.autosdk.R.string.tuesday),
                    ResUtil.getString(com.autosdk.R.string.wednesday),
                    ResUtil.getString(com.autosdk.R.string.thursday),
                    ResUtil.getString(com.autosdk.R.string.friday),
                    ResUtil.getString(com.autosdk.R.string.saturday)
            };
            String weekStr = strarr[calendar.get(Calendar.DAY_OF_WEEK)];
            timeBuffer.append(weekStr);
        }

        String ampm = "";
        int hour = calendar.get(Calendar.HOUR);
        if (isHoleDay) {
            if (dayIndex != 0) {
                hour += 12;
            }
        } else {
            if (isShowTimeInterval) {
                if (hour == 0 && dayIndex == 1) {
                    ampm = ResUtil.getString(com.autosdk.R.string.midday);
                } else if (hour < 6) {
                    ampm = dayIndex == 0 ? ResUtil.getString(com.autosdk.R.string.early_morning) : ResUtil.getString(com.autosdk.R.string.afternoon);
                } else {
                    ampm = dayIndex == 0 ? ResUtil.getString(com.autosdk.R.string.morning) : ResUtil.getString(com.autosdk.R.string.evening);
                }
            }
        }
        int min = calendar.get(Calendar.MINUTE);
        String minStr;
        if (min < 10) {
            minStr = "0" + min;
        } else {
            minStr = String.valueOf(min);
        }
        String hourStr;
        /**
         * 下午0点转换成12点
         * 如果当前小时数为0 则转换为12
         */
        if (!isHoleDay) {
            hour = hour == 0 ? 12 : hour;
        }
        if (isHoleDay && hour < 10) {
            hourStr = "0" + hour;
        } else {
            hourStr = String.valueOf(hour);
        }
        String time = hourStr + ":" + minStr;
        timeBuffer.append(ampm).append(time);
        timeBuffer.append(ResUtil.getString(com.autosdk.R.string.arrival));
        Timber.d("getScheduledTime, second:%s, str:%s", second, timeBuffer.toString());
        return timeBuffer.toString();
    }

    /**
     * 获取预计到达时间
     *
     * @param context
     * @param second  还需要多长时间到达(单位：秒)
     **/
    public static String[] getScheduledTimeArr(Context context, long second, boolean isShowTimeInterval) {
        String[] timeBuffer = {"", "", ""};
        long timeLong = System.currentTimeMillis() + second * 1000;

        boolean isHoleDay = DateFormat.is24HourFormat(context.getApplicationContext());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeLong);
        int dayIndex = calendar.get(Calendar.AM_PM);
        int dayDiff = getDayDiff(calendar);

        if (dayDiff == 1) {
            timeBuffer[0] = ResUtil.getString(com.autosdk.R.string.date_tomorrow);
        } else if (dayDiff > 1) {
            String[] strarr = new String[]{
                    "",
                    ResUtil.getString(com.autosdk.R.string.sunday),
                    ResUtil.getString(com.autosdk.R.string.monday),
                    ResUtil.getString(com.autosdk.R.string.tuesday),
                    ResUtil.getString(com.autosdk.R.string.wednesday),
                    ResUtil.getString(com.autosdk.R.string.thursday),
                    ResUtil.getString(com.autosdk.R.string.friday),
                    ResUtil.getString(com.autosdk.R.string.saturday)
            };
            String weekStr = strarr[calendar.get(Calendar.DAY_OF_WEEK)];
            timeBuffer[0] = weekStr;
        }

        String ampm = "";
        int hour = calendar.get(Calendar.HOUR);
        if (isHoleDay) {
            if (dayIndex != 0) {
                hour += 12;
            }
        } else {
            if (isShowTimeInterval) {
                if (hour == 0 && dayIndex == 1) {
                    ampm = ResUtil.getString(com.autosdk.R.string.midday);
                } else if (hour < 6) {
                    ampm = dayIndex == 0 ? ResUtil.getString(com.autosdk.R.string.early_morning) : ResUtil.getString(com.autosdk.R.string.afternoon);
                } else {
                    ampm = dayIndex == 0 ? ResUtil.getString(com.autosdk.R.string.morning) : ResUtil.getString(com.autosdk.R.string.evening);
                }
            }
        }
        int min = calendar.get(Calendar.MINUTE);
        String minStr;
        if (min < 10) {
            minStr = "0" + min;
        } else {
            minStr = String.valueOf(min);
        }
        String hourStr;
        /**
         * 下午0点转换成12点
         * 如果当前小时数为0 则转换为12
         */
        if (!isHoleDay) {
            hour = hour == 0 ? 12 : hour;
        }
        if (isHoleDay && hour < 10) {
            hourStr = "0" + hour;
        } else {
            hourStr = String.valueOf(hour);
        }
        String time = hourStr + ":" + minStr;
        timeBuffer[1] = ampm;
        timeBuffer[2] = time;
        Timber.d("getScheduledTime, second:%s, str:%s", second, Arrays.toString(timeBuffer));
        return timeBuffer;
    }

    /**
     * 获取预计到达时间
     *
     * @param context
     * @param second  还需要多长时间到达(单位：秒)
     **/
    public static String getScheduledTime(Context context, long second, boolean isNavi) {
        return getScheduledTime(context, second, false, isNavi);
    }

    private static int getDayDiff(Calendar day) {
        /**
         * 今天日期
         */
        Calendar today = Calendar.getInstance();
        today.setTimeInMillis(System.currentTimeMillis());
        int dayDiff = day.get(Calendar.DAY_OF_YEAR);
        if (day.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
            dayDiff += getDaysInYear(today.get(Calendar.YEAR));
        }
        int todayDiff = today.get(Calendar.DAY_OF_YEAR);

        return dayDiff - todayDiff;
    }

    private static int getDaysInYear(int years) {
        Calendar cal = Calendar.getInstance();
        cal.set(years, Calendar.DECEMBER, 31);
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * 两个poi点是否相同
     *
     * @param poi
     * @param comparePoi
     * @return
     */
    public static boolean isSamePoi(POI poi, POI comparePoi) {
        if (null == poi || null == comparePoi) return false;
        return Objects.equals(poi.getId(), comparePoi.getId())
                || (Math.abs(poi.getPoint().getLongitude() - comparePoi.getPoint().getLongitude()) < 0.000001f
                && Math.abs(poi.getPoint().getLatitude() - comparePoi.getPoint().getLatitude()) < 0.000001f);
    }
}
