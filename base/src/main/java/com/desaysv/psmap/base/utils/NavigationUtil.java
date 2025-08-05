package com.desaysv.psmap.base.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;

import com.autonavi.gbl.common.path.model.RoadClass;
import com.autonavi.gbl.guide.model.NaviFacility;
import com.autonavi.gbl.guide.model.NaviInfo;
import com.autonavi.gbl.layer.model.EagleEyeParam;
import com.autonavi.gbl.layer.model.EagleEyeStyle;
import com.autonavi.gbl.map.model.EGLDeviceID;
import com.autonavi.gbl.map.model.MapColorParam;
import com.autonavi.gbl.map.model.MapViewParam;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autosdk.bussiness.map.SurfaceViewID;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;
import com.autosdk.bussiness.widget.navi.utils.ResUtil;
import com.desaysv.psmap.base.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class NavigationUtil {
    public static final String TAG = "NavigationUtil";
    private final static int HIGH_ENLARGE = 500;
    private final static int NORMAL_ENLARGE = 200;
    /**
     * 隧道内分叉
     */
    public final static short CROSS_NAV_TUNNEL_INNER = 1;
    /**
     * 隧道外分叉
     */
    public final static short CROSS_NAV_TUNNEL_OUT = 2;

    /**
     * 道路等级 高速路
     */
    public final static byte ROAD_CLASS_HIGH_SPEED = 0;
    /**
     * 道路等级 城市快速路 Urban rapid
     */
    public final static byte ROAD_CLASS_URBAN_RAPID = 6;

    /**
     * 检查NaviInfoPanel是否合法
     *
     * @param naviinfo
     * @return
     */
    public static boolean checkNaviInfoPanelLegal(NaviInfo naviinfo) {
        if (naviinfo == null) {
            Timber.d("checkNaviInfoPanelLegal naviInfo null");
            return false;
        }

        if (naviinfo.NaviInfoData == null || naviinfo.NaviInfoData.size() == 0) {
            Timber.d("checkNaviInfoPanelLegal naviinfo.NaviInfoData null");
            return false;
        }

        if (naviinfo.NaviInfoFlag > naviinfo.NaviInfoData.size() - 1) {
            Timber.d("checkNaviInfoPanelLegal naviinfo.NaviInfoFlag length out bound!");
            return false;
        }

        if (naviinfo.NaviInfoData.get(naviinfo.NaviInfoFlag) == null) {
            Timber.d("checkNaviInfoPanelLegal naviinfo.NaviInfoData[naviinfo.NaviInfoFlag] null");
            return false;
        }

        return true;
    }

    /**
     * 按照固定的策略取整距离数值，与TBT保持一致
     * 1）10公里级别向下取整；
     * 2）1公里级别的四舍五入；
     * 3）1公里以下的暂不修改。
     *
     * @param distance
     * @return
     */
    public static String[] formatDistanceArray(int distance) {
        String[] distancs = new String[2];
        if (distance >= 10000) {
            //10公里级
            distance = (distance / 1000) * 1000;
        } else if (distance >= 1000) {
            //1公里级，精确到小数点后一位
            distance = ((distance + 50) / 100) * 100;
        }

        if (distance >= 1000) {
            int kiloMeter = distance / 1000;
            int leftMeter = distance % 1000;
            leftMeter = leftMeter / 100;

            StringBuffer sb = new StringBuffer();

            if (leftMeter > 0) {
                sb.append(kiloMeter);
                sb.append(".");
                sb.append(leftMeter);
            } else {
                sb.append(kiloMeter);
            }
            distancs[0] = sb.toString();
            distancs[1] = ResUtil.getString(R.string.sv_common_km);
        } else {
            distancs[0] = String.valueOf(distance);
            distancs[1] = ResUtil.getString(R.string.sv_common_meter);
        }
        return distancs;
    }

    public static SpannableString getNormalSpannedString(String strFrom1, String strTo1, int size1, int size2, int color1, int color2) {
        if (TextUtils.isEmpty(strFrom1) || TextUtils.isEmpty(strTo1)) {
            return null;
        }

        SpannableString wordtoSpan = new SpannableString(strFrom1 + strTo1);
        int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;
        int start = 0;
        int end = strFrom1.length();
        wordtoSpan.setSpan(new AbsoluteSizeSpan(size1), start, end, flag);
        wordtoSpan.setSpan(new ForegroundColorSpan(color1), start, end, flag);
        wordtoSpan.setSpan(new StyleSpan(Typeface.NORMAL), start, end, flag);
        start = end;
        end += strTo1.length();
        wordtoSpan.setSpan(new AbsoluteSizeSpan(size2), start, end, flag);
        wordtoSpan.setSpan(new ForegroundColorSpan(color2), start, end, flag);

        return wordtoSpan;
    }

    public static String switchFromSecond(int second) {
        String restTime = "";
        String dayString = "";
        String minuteString = "分钟";
        String hourString = "时";

        int minute = (second + 30) / 60;

        if (minute < 60) {
            // 小于1小时
            if (minute == 0) {
                restTime = "<1" + minuteString;
            } else {
                restTime = minute + minuteString;
            }
        } else {
            // 大于小于1小时，小于24小时
            int hour = minute / 60;
            if (hour > 24) {
                int day = hour / 24;
                hour = hour % 24;
                dayString = day + "天";
            }
            minute = minute % 60;
            if (minute > 0) {
                restTime = dayString + hour + hourString + minute + minuteString;
            } else {
                restTime = dayString + hour + hourString;
            }
        }

        return restTime;
    }

    /**
     * 服务区详情列表时间转换
     *
     * @param second
     * @return
     */
    @SuppressLint("DefaultLocale")
    public static String sapaSwitchFromSecond(int second) {
        float minute = (float) (second + 30) / 60;

        if (minute < 60) {
            // 小于1小时
            if (minute == 0) {
                return "0";
            } else {
                minute = minute / 60;
            }
        } else {
            // 大于小于1小时，小于24小时
            minute = minute / 60;
        }

        return String.format("%.1f", minute);
    }


    /**
     * 获取轮播时间间隔
     */
    public static long getIntervalTime() {
        return 5000L;
    }

    /**
     * 卡片到达时间
     *
     * @param context
     * @param second
     * @return
     */
    public static String getScheduledTime(Context context, int second) {
        StringBuffer timeBuffer = new StringBuffer();
        long timeLong = System.currentTimeMillis() + second * 1000;
        boolean isHoleDay = DateFormat.is24HourFormat(context);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeLong);
        int dayIndex = calendar.get(Calendar.AM_PM);
        int min = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR);
        String hourStr;
        String minStr;
        if (min < 10) {
            minStr = "0" + min;
        } else {
            minStr = String.valueOf(min);
        }
        if (dayIndex == 1) {
            hour += 12;
        }
        if (isHoleDay && hour < 10) {
            hourStr = "0" + hour;
        } else {
            hourStr = String.valueOf(hour);
        }
        String time = hourStr + ":" + minStr;
        timeBuffer.append(time);
        return timeBuffer.toString();
    }

    /**
     * 卡片到达天数
     *
     * @param second
     * @return
     */
    public static int getScheduledDayNum(int second) {
        long timeLong = System.currentTimeMillis() + second * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeLong);
        int dayDiff = getDayDiff(calendar);
        return dayDiff;
    }

    public static int getDayDiff(Calendar day) {
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

    public static int getDaysInYear(int years) {
        Calendar cal = Calendar.getInstance();
        cal.set(years, Calendar.DECEMBER, 31);
        return cal.get(Calendar.DAY_OF_YEAR);
    }


    /**
     * 获取平均车速
     *
     * @param speed
     * @return
     */
    public static int getCameraSpeed(ArrayList<Short> speed) {
        if (speed == null || speed.size() == 0) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < speed.size(); i++) {
            Timber.d("getCameraSpeed: i=%s, speed=%s", i, speed.get(i));
            if (speed.get(i) == 0 || speed.get(i) == 0xff) {
                continue;
            }
            if (speed.get(i) > result) {
                result = speed.get(i);
            }
        }
        return result;
    }

    /**
     * 获取距离单位
     */
    public static String getDistanceUnit(int distance) {
        if (distance >= 1000) {
            return ResUtil.getString(com.autosdk.R.string.km);
        } else {
            return ResUtil.getString(com.autosdk.R.string.route_meter);
        }
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

    /**
     * 依据服务区的剩余距离进行排序
     */
    public static NaviFacility[] getSortedInfo(List<NaviFacility> infos) {
        NaviFacility[] result = new NaviFacility[2];
        if (infos == null || infos.size() == 0) {
            return result;
        }

        if (infos.size() == 1) {
            result[0] = infos.get(0);
            return result;
        }

        NaviFacility info1 = infos.get(0);
        NaviFacility info2 = infos.get(1);
        if (info1.remainDist <= info2.remainDist) {
            result[0] = info1;
            result[1] = info2;
        } else {
            result[0] = info2;
            result[1] = info1;
        }

        return result;
    }

    /**
     * 对距离进行转换
     */
    public static String getDistanceText(int distance) {
        if (distance < 1000) {
            return String.valueOf(distance);
        }

        // 小数点第二位四舍五入
        return String.format(Locale.getDefault(), "%.1f", distance / 1000f);
    }

    /**
     * 显示路口大图时获取当前路口剩余距离, HIGH_ENLARGE 500,HIGH_ENLARGE 200
     *
     * @param roadClass
     * @param nDistanceNextRoad
     * @return
     */
    public static int getCurRoadEnlargeDis(int roadClass, int nDistanceNextRoad) {

        int dis = nDistanceNextRoad;

        /**< 高速公路 */
        if (roadClass == RoadClass.RoadClassFreeway) {
            dis = HIGH_ENLARGE;
        }
        /**< 为城快道路 */
        else if (roadClass == RoadClass.RoadClassCitySpeedway) {
            dis = HIGH_ENLARGE;
        }
        /** 普通道路 */
        else if (roadClass == RoadClass.RoadClassNationalRoad ||    //!< 1 国道
                roadClass == RoadClass.RoadClassProvinceRoad ||    //!< 2 省道
                roadClass == RoadClass.RoadClassCountyRoad ||    //!< 3 县道
                roadClass == RoadClass.RoadClassRuralRoad ||    //!< 4 乡公路
                roadClass == RoadClass.RoadClassInCountyRoad ||    //!< 5 县乡村内部道路
                roadClass == RoadClass.RoadClassMainRoad ||       //!< 7 主要道路
                roadClass == RoadClass.RoadClassSecondaryRoad ||    //!< 8 次要道路
                roadClass == RoadClass.RoadClassCommonRoad ||    //!< 9 普通道路
                roadClass == RoadClass.RoadClassNonNaviRoad)           //!< 10 非导航道路
        {
            dis = NORMAL_ENLARGE;
        }

        Log.d("getCurRoadEnlargeDis", "getCurRoadEnlargeDis dis:" + dis + ", roadClass:" + roadClass);
        return dis;
    }

    /**
     * 鹰眼图初始化
     * 小地图展现/刷新策略
     *
     * @param squareBg     true-鹰眼背景图为方形 false-鹰眼图背景图为原型
     * @param drawViaPoint 是否绘制途经点
     */
    public static EagleEyeStyle getEagleStyle(@SurfaceViewID.SurfaceViewID1 int surfaceViewID, int deviceId, boolean squareBg, boolean drawViaPoint) {
        EagleEyeStyle eagleStyle = new EagleEyeStyle();
        eagleStyle.mapViewParam = new MapViewParam();
        eagleStyle.mapViewParam.deviceId = EGLDeviceID.EGLDeviceIDDefault;
        eagleStyle.mapViewParam.engineId = SurfaceViewID.transform2EyeEngineID(surfaceViewID);
        eagleStyle.mapViewParam.screenWidth = BusinessApplicationUtils.mScreenWidth;
        eagleStyle.mapViewParam.screenHeight = BusinessApplicationUtils.mScreenHeight;
        int r = ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), R.dimen.sv_dimen_300) -//鹰眼图路线显示范围
                ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), R.dimen.sv_dimen_100);//边距
        int tartgetWidth = (int) Math.sqrt((r * r + r * r));
        //需要减去车标的大小 避免比例尺过大
        tartgetWidth -= ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), R.dimen.sv_dimen_30);
        // 再减去一段距离，防止北字和起点或终点压盖
        tartgetWidth -= ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), R.dimen.sv_dimen_8);
        EagleEyeParam eagleEyeParam = eagleStyle.eagleEyeParam;
        eagleEyeParam.targetWidth = tartgetWidth;
        //末端全览距离，单位：米
        eagleEyeParam.endPreviewLength = 2000;

        int width = ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), R.dimen.sv_dimen_300);//鹰眼图大小
        int rightMargins = ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), R.dimen.sv_dimen_34);
        int bottomMargins = ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), R.dimen.sv_dimen_322);
        eagleStyle.mapViewParam.x = BusinessApplicationUtils.mScreenWidth - width - rightMargins;
        eagleStyle.mapViewParam.y = BusinessApplicationUtils.mScreenHeight - width - bottomMargins;
        eagleStyle.mapViewParam.width = width;
        eagleStyle.mapViewParam.height = width;
        //暂时使用默认值
        eagleStyle.mapViewParam.cacheCountFactor = 2f;
        if (surfaceViewID != SurfaceViewID.SURFACE_VIEW_ID_MAIN) {
            //非主屏需要另外配置
            eagleStyle.mapViewParam.deviceId = deviceId;
            eagleStyle.mapViewParam.engineId = SurfaceViewID.transform2EngineID(surfaceViewID) + 1;
            width = ResUtil.getAutoDimenValue(BusinessApplicationUtils.getApplication(), R.dimen.sv_dimen_174);
            eagleStyle.mapViewParam.x = BusinessApplicationUtils.mScreenWidth - width;
            eagleStyle.mapViewParam.y = BusinessApplicationUtils.mScreenHeight - width;
            eagleStyle.mapViewParam.width = width;
            eagleStyle.mapViewParam.height = width;
        }


        BinaryStream binaryStream = null;
        if (squareBg) {
            binaryStream = new BinaryStream(ResUtil.decodeAssetResData(BusinessApplicationUtils.getApplication(), "hawkeye/hawkeyemap_bg_square.png"));
        } else {
            binaryStream = new BinaryStream(ResUtil.decodeAssetResData(BusinessApplicationUtils.getApplication(), "hawkeye/hawkeyemap_bg.png"));
        }
        eagleEyeParam.dataBuff = binaryStream;
        Timber.i("getEagleStyle: binaryStream.buffer.length%s", binaryStream.buffer.length);
        //day
        eagleStyle.mapColorParamDay = new MapColorParam();
        eagleStyle.mapColorParamDay.fRed = 1f;
        eagleStyle.mapColorParamDay.fGreen = 1f;
        eagleStyle.mapColorParamDay.fBlue = 1f;
        eagleStyle.mapColorParamDay.fAlpha = 1f;

        //night
        eagleStyle.mapColorParamNight = new MapColorParam();
        eagleStyle.mapColorParamNight.fRed = 0.13f;
        eagleStyle.mapColorParamNight.fGreen = 0.16f;
        eagleStyle.mapColorParamNight.fBlue = 0.19f;
        eagleStyle.mapColorParamNight.fAlpha = 1f;

        //【4.1.8】小地图展现/刷新策略
        // 是否绘制鹰眼途经点图标，AR为true
        eagleEyeParam.isDrawViaPoint = drawViaPoint;
        // 末端全览的最大长度，AR为最大值
        eagleEyeParam.endPreviewLength = 0xFFFFFFFF;
        // 途经点策略剩余路径长度，用于控制刷新频率，3000M
        eagleEyeParam.viaUpdateLength = 3000;
        // 途经点全览，>3000M，刷新频率60秒
        eagleEyeParam.viaUpdateTimeG = 60;
        // 途经点全览，<=3000M，刷新频率10秒
        eagleEyeParam.viaUpdateTimeS = 10;
        // 终点策略剩余路径长度，用于控制刷新频率，3000M
        eagleEyeParam.endUpdateLength = 3000;
        // 终点全览，>3000M，刷新频率60秒
        eagleEyeParam.endUpdateTimeG = 60;
        // 终点全览，<=3000M，刷新频率10秒
        eagleEyeParam.endUpdateTimeS = 10;

        return eagleStyle;
    }

    /**
     * 鹰眼图初始化
     * 小地图展现/刷新策略
     *
     * @param squareBg     true-鹰眼背景图为方形 false-鹰眼图背景图为原型
     * @param drawViaPoint 是否绘制途经点
     */
    public static EagleEyeStyle getEagleStyle(boolean squareBg, boolean drawViaPoint) {
        return getEagleStyle(SurfaceViewID.SURFACE_VIEW_ID_MAIN, 0, squareBg, drawViaPoint);
    }

    /**
     * 是否存在近阶动作信息
     *
     * @return
     */
    public static boolean hasNextThumTip(NaviInfo naviInfo) {
        if (null == naviInfo || null == naviInfo.nextCrossInfo || naviInfo.nextCrossInfo.size() == 0) {
            return false;
        }
        if (naviInfo.nextCrossInfo.get(0) == null) {
            return false;
        }
        return true;
    }

    public static boolean isNeedNextThumTip(NaviInfo naviInfo) {
        if (null == naviInfo.nextCrossInfo || naviInfo.nextCrossInfo.size() == 0) {
            return false;
        }
        if (naviInfo.nextCrossInfo.get(0) == null) {
            return false;
        }

        boolean isNextThumTip = false;
        //检查道路等级限制
        switch (naviInfo.curRoadClass) {
            case ROAD_CLASS_HIGH_SPEED:
                //高速路
            case ROAD_CLASS_URBAN_RAPID:
                //城市快速路
                isNextThumTip = naviInfo.NaviInfoData.get(naviInfo.NaviInfoFlag).segmentRemain.dist <= 1000;
                break;
            default:
                isNextThumTip = naviInfo.NaviInfoData.get(naviInfo.NaviInfoFlag).segmentRemain.dist <= 500;
                //其他
                break;
        }
        return isNextThumTip;
    }

    public static String getCrossNavNormalTip(Context context, int segmentRemainDist) {
        String tip = context.getResources().getString(R.string.sv_navi_drive_cross_nav_normal_tip) + meterToStr(context, segmentRemainDist);
        return tip;
    }

    public static String meterToStr(Context context, long pathLength) {
        float disKm = pathLength / 1000f;
        if (pathLength >= 10000) {
            return context.getString(R.string.sv_common_km_d, (int) disKm);
        }
        return pathLength >= 1000
                ? context.getString(R.string.sv_common_km_f, disKm)
                : context.getString(R.string.sv_common_dis_m, pathLength);
    }

    public static String meterToStrEnglish(Context context, long pathLength) {
        float disKm = pathLength / 1000f;
        if (pathLength >= 10000) {
            return context.getString(R.string.sv_common_km_d_s, (int) disKm);
        }
        return pathLength >= 1000
                ? context.getString(R.string.sv_common_km_f_s, disKm)
                : context.getString(R.string.sv_common_dis_m_s, pathLength);
    }

    //路书使用，四舍五入
    public static String ahaMeterToStrEnglish(Context context, long pathLength) {
        float disKm = pathLength / 1000f;
        if (pathLength >= 10000) {
            // 四舍五入到整数
            return context.getString(R.string.sv_common_km_d_s, Math.round(disKm));
        }
        return pathLength >= 1000
                ? context.getString(R.string.sv_common_km_f_s, Math.round(disKm * 10f) / 10f) // 保留一位小数
                : context.getString(R.string.sv_common_dis_m_s, pathLength);
    }

    //时间转换 输出: 00:04:50
    public static String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }
}
