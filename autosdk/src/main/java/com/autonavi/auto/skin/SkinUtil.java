package com.autonavi.auto.skin;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.autosdk.R;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */
public class SkinUtil {

    /**
     * 获取布局中得自定的皮肤attr属性
     *
     * @param context
     * @param attrs
     * @return
     */

    public static
    SkinItems initSkinAttrs(Context context, AttributeSet attrs) {

        SkinItems skinItems = new SkinItems();
        if (attrs == null) {
            return skinItems;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.autoSkin);
        skinItems.setTextColor(getResBean(array, R.styleable.autoSkin_textColor4Skin, R.styleable.autoSkin_textColor4Night));
        skinItems.setBackground(getResBean(array, R.styleable.autoSkin_background4Skin, R.styleable.autoSkin_background4Night));
        skinItems.setSrc(getResBean(array, R.styleable.autoSkin_src4Skin, R.styleable.autoSkin_src4Night));
        skinItems.setDrawableBottom(getResBean(array, R.styleable.autoSkin_drawableBottom4Skin, R.styleable.autoSkin_drawableBottom4Night));
        skinItems.setDrawableLeft(getResBean(array, R.styleable.autoSkin_drawableLeft4Skin, R.styleable.autoSkin_drawableLeft4Night));
        skinItems.setDrawableTop(getResBean(array, R.styleable.autoSkin_drawableTop4Skin, R.styleable.autoSkin_drawableTop4Night));
        skinItems.setDrawableRight(getResBean(array, R.styleable.autoSkin_drawableRight4Skin, R.styleable.autoSkin_drawableRight4Night));
        skinItems.setTextColorHint(getResBean(array, R.styleable.autoSkin_textColorHint4Skin, R.styleable.autoSkin_textColorHint4Night));
        skinItems.setProgressDrawable(getResBean(array, R.styleable.autoSkin_progressDrawable4Skin, R.styleable.autoSkin_progressDrawable4Night));
        skinItems.setThumb(getResBean(array, R.styleable.autoSkin_thumb4Skin, R.styleable.autoSkin_thumb4Night));
        skinItems.setSvgColor(getResBean(array, R.styleable.autoSkin_svgColor4Skin, R.styleable.autoSkin_svgColor4Night));
        array.recycle();

        return skinItems;
    }

    /**
     * 获取昼夜的资源id，要求2个同时存在才处理
     *
     * @param array
     * @param dayResIndex
     * @param nightResIndex
     * @return
     */
    private static ResBean getResBean(TypedArray array, int dayResIndex, int nightResIndex) {
        int dayResId = array.getResourceId(dayResIndex, -1);
        if (dayResId == -1) {
            return null;
        }
        int nightResId = array.getResourceId(nightResIndex, -1);
        if (nightResId == -1) {
            return null;
        }
        ResBean resBean = new ResBean();
        resBean.setDefaultResId(dayResId);
        resBean.setNightResId(nightResId);
        return resBean;
    }

    /**
     * 设置字体颜色
     *
     * @param view
     * @param dayColorResId
     * @param nightColorResId
     */
    public static void setTextColor(View view, int dayColorResId, int nightColorResId) {
        if (view == null){
            return;
        }
        if (!(view instanceof ISkin.ITextViewSkin)) {
            return;
        }
        ISkin.ITextViewSkin textViewSkin = (ISkin.ITextViewSkin) view;
        textViewSkin.setTextColor(dayColorResId, nightColorResId);
    }

    /**
     * 设置字体颜色
     *
     * @param view
     * @param dayResId
     * @param nightResId
     */
    public static void setBackgroudResource(View view, int dayResId, int nightResId) {
        if (view == null) {
            return;
        }
        if (!(view instanceof ISkin.IViewSkin)) {
            return;
        }
        ISkin.IViewSkin textViewSkin = (ISkin.IViewSkin) view;
        textViewSkin.setBackground(dayResId, nightResId);
    }

    /**
     * 设置字体颜色
     *
     * @param view
     * @param dayResId
     * @param nightResId
     */
    public static void setImageResource(View view, int dayResId, int nightResId) {
        if (view == null) {
            return;
        }
        if (!(view instanceof ISkin.IImageViewSkin)) {
            return;
        }
        ISkin.IImageViewSkin imageViewSkin = (ISkin.IImageViewSkin) view;
        imageViewSkin.setImageResource(dayResId, nightResId);
    }
}
