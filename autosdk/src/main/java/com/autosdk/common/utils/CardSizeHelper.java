package com.autosdk.common.utils;

import androidx.annotation.IntDef;

import com.autosdk.common.display.DisplayInfo;
import com.autosdk.common.display.ScreenMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

/**
 * Created by AutoSdk.
 * 卡片宽度根据屏幕大小动态调整
 */

public class CardSizeHelper {
    static final private String TAG = CardSizeHelper.class.getSimpleName();

    /**
     * 卡片类型
     */
    @IntDef({ CardType.DRIVE_CROSS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CardType {
        //路口放大图
        int DRIVE_CROSS = 1009;
    }

    private static class RadioItem {
        final float landscapeRadio;
        final float portraitRadio;
        final float landscapeWideRadio;

        RadioItem(float landscapeRadio, float portraitRadio, float landscapeWideRadio){
            this.landscapeRadio = landscapeRadio;
            this.portraitRadio = portraitRadio;
            this.landscapeWideRadio = landscapeWideRadio;
        }

        float getRadio(@ScreenMode int screenMode){
            switch (screenMode) {
                case ScreenMode.LANDSCAPE:
                    return landscapeRadio;
                case ScreenMode.LANDSCAPE_WIDE:
                    return landscapeWideRadio;
                case ScreenMode.PORTRAIT:
                    return portraitRadio;
                default:
                    break;
            }
            return 0.4833984375f;
        }
    }

    /**
     * 各种卡片比例值定义
     */
    static class CardWidthRadio {
        /**
         * 路口放大图
         */
        final static private float DRIVE_CROSS_RADIO_LANDSCAPE =0.4833984375f;
        final static private float DRIVE_CROSS_RADIO_PORTRAIT = 0.4833984375f;
        final static private float DRIVE_CROSS_RADIO_LANDSCAPEWIDE =0.4833984375f;

        final static RadioItem DRIVE_CROSS = new RadioItem(
            DRIVE_CROSS_RADIO_LANDSCAPE, DRIVE_CROSS_RADIO_PORTRAIT, DRIVE_CROSS_RADIO_LANDSCAPEWIDE);
    }

    static private int calcCardWidth(RadioItem cardRadio, DisplayInfo info){
        float radio = cardRadio.getRadio(info.screenMode);
        float width = info.appWidth * radio;
        return (int)width;
    }

    /**
     * 获取卡片宽度
     */
    public static int getCardWidth(@CardType int type, DisplayInfo info){
        RadioItem cardRadio;
        switch (type) {
            case CardType.DRIVE_CROSS:
                cardRadio = CardWidthRadio.DRIVE_CROSS;
                break;
            default:
                Timber.d("CardType[%s] is not found", type);
                return -1;
        }

        int width = calcCardWidth(cardRadio, info);
        Timber.d("screenMode=%s, cardType=%s, radio=%s, cardWidth=%s",info.screenMode, type, cardRadio, width);
        return width;
    }
}
