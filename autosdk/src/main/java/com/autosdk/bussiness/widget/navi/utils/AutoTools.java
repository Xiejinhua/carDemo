package com.autosdk.bussiness.widget.navi.utils;

import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.ImageView;

import com.autonavi.gbl.aosclient.model.GSubTraEventDetail;
import com.autosdk.R;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 工具类
 *
 * @author AutoSDK
 */
public class AutoTools {
    private static final String TAG = "AutoTools";

    /**
     * @param poiView
     * @param poiType 6位数字，前两位大类，中两位中类，最后两位小类
     * @return
     */
    public static void getPoiIconIdCate(ImageView poiView, String poiType) {
        int defaultResId;

        if (poiView == null) {
            return;
        }
        Resources resources = poiView.getResources();
        String drawableName;
        int drawableId = 0;

        if (TextUtils.isEmpty(poiType) || poiType.length() != 6) {
            drawableName = "index_auto_poicard_ic_default";
            defaultResId = resources.getIdentifier(drawableName, "drawable", poiView.getContext().getPackageName());
            poiView.setBackgroundResource(defaultResId);
            return;
        }

        int endIndex = poiType.length();
        while (endIndex >= 2) {
            drawableName = "global_image_auto_poicard_ic_" + poiType.substring(0, endIndex);
            drawableId = resources.getIdentifier(drawableName, "drawable", poiView.getContext().getPackageName());
            if (drawableId != 0) {
                break;
            }
            endIndex = endIndex - 2;
        }
        if (drawableId == 0) {
            drawableName = "index_auto_poicard_ic_default";
            drawableId = resources.getIdentifier(drawableName, "drawable", poiView.getContext().getPackageName());
        }
        poiView.setBackgroundResource(drawableId);
    }

    /*    *//**
     * 根据layerTag获取交通事件对应的icon
     *
     * @return 资源id
     *//*
    public static int getTrafficIconId(GSubTraEventDetail detail) {
        if (TextUtils.isEmpty(detail.iconstyle)) {
            return getTrafficIconId(detail.layertag);
        }
        if ("10011:13".equals(detail.iconstyle)) {
            return R.drawable.auto_traffic_10011_13;
        } else if ("10011:14".equals(detail.iconstyle)) {
            return R.drawable.auto_traffic_10011_14;
        } else if ("10011:15".equals(detail.iconstyle)) {
            return R.drawable.auto_traffic_11031;
        } else {
            return getTrafficIconId(detail.layertag);
        }
    }

    *//**
     * 根据layerTag获取交通事件对应的icon
     *
     * @return 资源id
     *//*
    public static int getTrafficIconId(int layerTag) {
        int stringId;
        switch (layerTag) {
            case 11050:
                stringId = R.drawable.auto_traffic_11050;
                break;
            case 11070:
                stringId = R.drawable.auto_traffic_11070;
                break;
            case 11031:
                stringId = R.drawable.auto_traffic_11031;
                break;
            case 11060:
                stringId = R.drawable.auto_traffic_11060;
                break;
            case 11071:
                stringId = R.drawable.auto_traffic_11071;
                break;
            case 11011:
                stringId = R.drawable.auto_traffic_11011;
                break;
            case 11010:
                stringId = R.drawable.auto_traffic_11010;
                break;
            case 11012:
                stringId = R.drawable.auto_traffic_11012;
                break;
            case 11040:
                stringId = R.drawable.auto_traffic_11040;
                break;
            case 11100:
                stringId = R.drawable.auto_traffic_11100;
                break;
            case 11033:
                stringId = R.drawable.auto_traffic_11033;
                break;
            case 11021:
                stringId = R.drawable.auto_traffic_11021;
                break;
            case 404302:
                stringId = R.drawable.auto_traffic_404302;
                break;
            case 409302:
            case 502302:
            case 503302:
                stringId = R.drawable.auto_traffic_409302;
                break;
            case 501302:
                stringId = R.drawable.auto_traffic_501302;
                break;
            default:
                stringId = R.drawable.auto_traffic_11010;
                break;

        }
        return stringId;
    }*/

    /**
     * 根据layerTag获取交通事件对应的String
     *
     * @return 交通事件名称
     */
    public static String getTrafficName(GSubTraEventDetail detail) {
        if (TextUtils.isEmpty(detail.eventname)) {
            return getTrafficName(detail.layertag);
        }
        return detail.eventname;
    }

    /**
     * 根据layerTag获取交通事件对应的String
     *
     * @return 交通事件名称
     */
    public static String getTrafficName(int layerTag) {
        int stringId;
        switch (layerTag) {
            case 11050:
                stringId = R.string.poicard_traffic_11050;
                break;
            case 11070:
                stringId = R.string.poicard_traffic_11070;
                break;
            case 11031:
                stringId = R.string.poicard_traffic_11031;
                break;
            case 11060:
                stringId = R.string.poicard_traffic_11060;
                break;
            case 11071:
                stringId = R.string.poicard_traffic_11071;
                break;
            case 11011:
                stringId = R.string.poicard_traffic_11011;
                break;
            case 11010:
                stringId = R.string.poicard_traffic_11010;
                break;
            case 11012:
                stringId = R.string.poicard_traffic_11012;
                break;
            case 11040:
                stringId = R.string.poicard_traffic_11040;
                break;
            case 11100:
                stringId = R.string.poicard_traffic_11100;
                break;
            case 11033:
                stringId = R.string.poicard_traffic_11033;
                break;
            case 11021:
                stringId = R.string.poicard_traffic_11021;
                break;
            case 404302:
                stringId = R.string.poicard_traffic_404302;
                break;
            case 409302:
                stringId = R.string.poicard_traffic_409302;
                break;
            case 502302:
                stringId = R.string.poicard_traffic_502302;
                break;
            case 503302:
                stringId = R.string.poicard_traffic_503302;
                break;
            case 501302:
                stringId = R.string.poicard_traffic_501302;
                break;
            default:
                stringId = R.string.poicard_traffic_11010;
                break;

        }
        return BusinessApplicationUtils.getApplication().getString(stringId);
    }

    /**
     * 判断是不是封路事件
     *
     * @param layerTag
     * @return
     */
    public static boolean isBlockTraffic(String layerTag) {
        if ("404302".equals(layerTag)) {
            return true;
        } else if ("409302".equals(layerTag)) {
            return true;
        } else if ("502302".equals(layerTag)) {
            return true;
        } else if ("503302".equals(layerTag)) {
            return true;
        } else if ("501302".equals(layerTag)) {
            return true;
        } else if ("11050".equals(layerTag)) {
            return true;
        }
        return false;
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
        String createTime = sdf2.format(time * 1000L);
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

    /**
     * 时间戳转换成年月日格式
     *
     * @param time 时间戳，需要毫秒值
     * @return
     */
    public static String formatDate(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        date.setTime(time);
        return formatter.format(date);
    }

}
