package com.autosdk.bussiness.widget.ui.util;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;

import com.autonavi.auto.common.view.AutoGuideLine;
import com.autonavi.auto.common.view.AutoGuideLine.AutoGuideLineParams;
import com.autosdk.bussiness.widget.BusinessApplicationUtils;

import timber.log.Timber;

/**
 * Created by AutoSdk.
 * @author AutoSDK
 */

public class AutoGuideLineHelper {
    final static String TAG = "AutoGuideLineHelper";

    /**
     * 超宽屏宽高比阀值
     */
    final public static float SUPER_WIDE_RADIO = 3f;

    /**
     * 根据当前屏幕类型，更新View中所有GuideLine
     * @param view AutoGuideLine或者ViewGroup
     * @param screenMode 当前屏幕类型
     */
    public static void updateGuideLine(View view, @ScreenMode int screenMode){
        if (view instanceof AutoGuideLine){
            updateGuideLineImpl((AutoGuideLine)view, screenMode);
        }else if (view instanceof ViewGroup){
            ViewGroup viewGroup = ((ViewGroup)view);
            int count = viewGroup.getChildCount();
            for (int i=0; i<count; i++){
                updateGuideLine(viewGroup.getChildAt(i), screenMode);
            }
        }
    }

    private static void updateGuideLineImpl(AutoGuideLine guideLine, int screenMode){
        if (guideLine == null){
            return;
        }

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();
        if (layoutParams == null){
            return;
        }

        float percent = calcGuideLinePercent(guideLine, screenMode);

        if ((percent != AutoGuideLine.UNSPECIFIED_VALUE) && (percent != layoutParams.guidePercent)){
            layoutParams.guidePercent = percent;
            boolean isLayoutRequested = guideLine.isLayoutRequested();
            Timber.d("guideline id = %s, isLayoutRequested = %s"
                , Integer.toHexString(guideLine.getId()), isLayoutRequested);
            guideLine.setLayoutParams(layoutParams);
            if (isLayoutRequested){
                guideLine.getParent().requestLayout();
            }
        }
    }

    /**
     * 比例值过滤器
     * @param oriPercent 原始比例
     * @param screenMode 屏幕类型
     * @return 过滤后比例
     */
    private static float percentFilter(float oriPercent, @ScreenMode int screenMode){
        if (oriPercent == AutoGuideLine.UNSPECIFIED_VALUE){
            return oriPercent;
        }

        /**
         * 超宽屏【W/H>3】，大卡片宽度：W = 3H * 80%
         * 转换guideLine比例：percent = 3H/W * oriPercent
         */
        if (screenMode == ScreenMode.LANDSCAPE_WIDE){
            final float displayWidth = BusinessApplicationUtils.mScreenWidth;
            final float displayHeight = BusinessApplicationUtils.mScreenHeight;
            if (displayWidth / displayHeight > SUPER_WIDE_RADIO){
                return oriPercent * SUPER_WIDE_RADIO * displayHeight / displayWidth;
            }
        }

        return oriPercent;
    }


    static private float calcGuideLinePercent(AutoGuideLine guideLine, @ScreenMode int screenMode){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) guideLine.getLayoutParams();

        AutoGuideLineParams autoGuideLineParams = guideLine.getAutoGuideLineParams();
        if (autoGuideLineParams == null){
            return layoutParams.guidePercent;
        }

        //从自定义属性获取比例
        float percent = AutoGuideLine.UNSPECIFIED_VALUE;
        if (screenMode == ScreenMode.LANDSCAPE){
            percent = autoGuideLineParams.guidePercent4Landscape;
        } else if (screenMode == ScreenMode.LANDSCAPE_WIDE){
            percent = autoGuideLineParams.guidePercent4LandscapeWide;
        }else if (screenMode == ScreenMode.PORTRAIT){
            percent = autoGuideLineParams.guidePercent4Portrait;
        }else if (screenMode == ScreenMode.SQUARE){
            percent = autoGuideLineParams.guidePercent4Square;
        }

        //如果没有配置则使用默认值
        if (percent == AutoGuideLine.UNSPECIFIED_VALUE){
            percent = autoGuideLineParams.guidePercent4Default;
        }

        //比例值过滤
        if (layoutParams.orientation == ConstraintLayout.LayoutParams.VERTICAL){
            /**
             * guideLine在卡片的左侧时，guideline的比例值需要转成卡片宽度的比例值来计算
             */
            if (judgeGuideLineOnLeftOrRight(autoGuideLineParams)){
                percent = 1 - percentFilter(1-percent, screenMode);
            }else {
                percent = percentFilter(percent, screenMode);
            }
        }

        return percent;
    }

    /**
     * 判断guideLine是在卡片的左侧还是右侧
     **/
    static private boolean judgeGuideLineOnLeftOrRight(AutoGuideLineParams params){
        /**
         * 横屏的比例比宽屏大，则在guideLine在卡片的左侧
         */
        return params.guidePercent4Landscape < params.guidePercent4LandscapeWide
            && params.guidePercent4Landscape != AutoGuideLine.UNSPECIFIED_VALUE;
    }

    /**
     * 通过卡片约束的guideLine比例，获取指定屏幕类型下的卡片宽度
     * @param guideline 约束卡片的guideLine
     * @param screenMode 屏幕类型
     * @return 卡片的宽度
     */
    static public int getCardWidthByGuideLine(@NonNull Guideline guideline, @ScreenMode int screenMode){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        if (layoutParams.orientation != ConstraintLayout.LayoutParams.VERTICAL){
            throw new IllegalArgumentException("Must be a vertical Guideline");
        }

        if (guideline instanceof AutoGuideLine){
            float percent = calcGuideLinePercent((AutoGuideLine)guideline, screenMode);
            return (int)(percent * BusinessApplicationUtils.mScreenWidth);
        }else {
            return (int)(layoutParams.guidePercent * BusinessApplicationUtils.mScreenWidth);
        }
    }

    static public int getCardWidthByGuideLine(@NonNull Guideline guideline){
        return getCardWidthByGuideLine(guideline, ScreenMode.LANDSCAPE);
    }

}
