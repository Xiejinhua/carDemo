package com.autosdk.bussiness.widget.navi.utils;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autosdk.R;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;
import com.autosdk.common.AutoConstant;
import com.autosdk.common.SdkApplicationUtils;

import java.lang.reflect.Field;

import timber.log.Timber;

/**
 * 导航UI控件Utils
 */
public class NaviUiUtil {
    // 导航转向图标宽度信息
    public static int turnIconSize = ResUtil.dipToPixel(SdkApplicationUtils.getApplication(), 155);
    public static int nextTurnIconSize = ResUtil.dipToPixel(SdkApplicationUtils.getApplication(), 100);

    public static Bitmap getRoadSignBitmap(byte[] roadSrc, int width, int height, int maneuverId, int roundNum, boolean isNextThum) {
        return getRoadSignBitmap(roadSrc, width, height, maneuverId, roundNum, true, isNextThum);
    }


    /**
     * 无在线数据（roadSrc为空）及isHud为true 时获取本地图片资源
     */
    public static Bitmap getRoadSignBitmap(byte[] roadSrc, int width, int height, int maneuverId, int roundNum, boolean isHud, boolean isNextThum) {
        Bitmap roadSignBmp;
        int imageResId;
        String hudResPrefix = "global_image_hud_";
        String iconResName = "sou";
        String nextThum = "_next";
        if (isHud) {
            iconResName = hudResPrefix + iconResName + (isNextThum ? nextThum : "");
        }

        if (roundNum > 0) {
            /*11进入环岛图标，右侧通行地区的逆时针环岛,12驶出环岛图标，右侧通行地区的逆时针环岛*/
            if (maneuverId == 12 || maneuverId == 11) {
                imageResId = getDrawableID(iconResName + (49 + roundNum), isNextThum);
                roadSignBmp = BitmapFactory.decodeResource(BusinessApplicationUtils.getApplication().getResources(), imageResId);
                return roadSignBmp;
            } else if (maneuverId == 18 || maneuverId == 17) {
                /*17进入环岛图标，左侧通行地区的顺时针环岛,18驶出环岛图标，左侧通行地区的顺时针环岛*/
                imageResId = getDrawableID(iconResName + (59 + roundNum), isNextThum);
                roadSignBmp = BitmapFactory.decodeResource(BusinessApplicationUtils.getApplication().getResources(), imageResId);
                return roadSignBmp;
            }
        }
        if (maneuverId == 65) {
            //1076B新增，65靠左图标
            imageResId = getDrawableID(iconResName + (6 + maneuverId), isNextThum);
            roadSignBmp = BitmapFactory.decodeResource(BusinessApplicationUtils.getApplication().getResources(), imageResId);
            return roadSignBmp;
        } else if (maneuverId == 66) {
            //1076B新增，66靠右图标
            imageResId = getDrawableID(iconResName + (4 + maneuverId), isNextThum);
            roadSignBmp = BitmapFactory.decodeResource(BusinessApplicationUtils.getApplication().getResources(), imageResId);
            return roadSignBmp;
        }

//        if (roadSrc != null && roadSrc.length > 0) {
//            //使用高德回调的图片，纯白色的，如有日夜模式不同颜色转向图标就注释掉
//            Timber.i(" getDrawableID: bytesToBimap is autoMap");
//            roadSignBmp = bytesToBimap(roadSrc);
//        } else {
        imageResId = getDrawableID((iconResName + maneuverId), isNextThum);
        roadSignBmp = BitmapFactory.decodeResource(BusinessApplicationUtils.getApplication().getResources(), imageResId);
//        }

        return roadSignBmp;

    }

    public static Bitmap bytesToBimap(byte[] b) {
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }


    public static int getDrawableID(String icon, boolean isNextThum) {
        String night = NightModeGlobal.isNightMode() ? "_night" : "";
        Timber.i(" getDrawableID: %s", (icon + night));
        Field f = null;
        int drawableId = 0;
        try {
            f = R.drawable.class.getDeclaredField(icon + night); // FIXME A crash here! Caused by: java.lang.NoSuchFieldException: hud_sou0, on 2015-09-17
            drawableId = f.getInt(R.drawable.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return drawableId;
    }

    public static ViewGroup.MarginLayoutParams getViewLayoutParmas(View view) {
        ViewGroup.MarginLayoutParams lp;

        if (view == null) {
            return null;
        }

        ViewGroup parentView = (ViewGroup) view.getParent();
        if (parentView == null) {
            return null;
        }

        if (parentView instanceof RelativeLayout) {
            lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        } else if (parentView instanceof LinearLayout) {
            lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        } else if (parentView instanceof FrameLayout) {
            lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        } else {
            return null;
        }

        return lp;

    }


    public static void showView(View view) {
        setViewVisibility(view, View.VISIBLE);
    }

    public static void hideView(View view) {
        setViewVisibility(view, View.GONE);
    }

    public static void invisibleView(View view) {
        setViewVisibility(view, View.INVISIBLE);
    }

    public static void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public static void setTextViewGravity(TextView view, int gravity) {
        if (view != null) {
            view.setGravity(gravity);
        }
    }

    /**
     * 设置TextView显示内容
     */
    public static void setTextViewContent(TextView tv, String content) {
        if (tv == null || TextUtils.isEmpty(content)) {
            return;
        }
        tv.setText(content);
    }

    @SuppressLint("NewApi")//引用处已经做了版本兼容
    public static void setTranslationY(View view, float translationY) {
        view.setTranslationY(translationY);
    }

}
