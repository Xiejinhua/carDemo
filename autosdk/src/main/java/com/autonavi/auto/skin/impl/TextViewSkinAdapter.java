package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.autonavi.auto.skin.ResBean;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */
public class TextViewSkinAdapter extends ViewSkinAdapter<TextView> {
    /**
     * 字体颜色
     */
    protected SkinWrapper4TextColor mTextColorWrapper;
    /**
     * 四周的图片，drawableLeft,drawableTop,drawableRight,drawableBottom
     */
    protected SkinWrapper4DrawableCompound mDrawableCompound;

    protected TextViewSkinAdapter(Context context, SkinItems skinProperter) {
        super(context, skinProperter);
        mContext = context;
        this.mSkinProperter = skinProperter;
    }

    public TextViewSkinAdapter(View view, AttributeSet attrs) {
        super(view, attrs);
    }

    public static ISkin.ISkinAdapter build(Context context, SkinItems skinProperter) {
        TextViewSkinAdapter wrapper = new TextViewSkinAdapter(context, skinProperter);
        return wrapper;
    }

    @Override
    public void initSkinImpl(View view) {
        if (mSkinProperter == null) {
            return;
        }
        mTextColorWrapper = new SkinWrapper4TextColor();
        mTextColorWrapper.init(mContext, mSkinProperter);

        mDrawableCompound = new SkinWrapper4DrawableCompound();
        mDrawableCompound.init(mContext, mSkinProperter);
    }

    @Override
    public void applyImpl(boolean isNight) {
        super.applyImpl(isNight);
        if (mTextColorWrapper != null) {
            mTextColorWrapper.apply(view, isNight);
        }
        if (mDrawableCompound != null) {
            mDrawableCompound.apply(view, isNight);
        }
    }


    /**
     * 设置字体颜色
     */
    public void setTextColor(int dayColorResId, int nightColorResId) {
        ResBean bean = new ResBean();
        bean.setDefaultResId(dayColorResId);
        bean.setNightResId(nightColorResId);
        mSkinProperter.setTextColor(bean);
        if (mTextColorWrapper == null) {
            mTextColorWrapper = new SkinWrapper4TextColor();
        }
        mTextColorWrapper.init(mContext, mSkinProperter);
        onUpdateRes();
    }

    /**
     * 设置提示字体颜色
     */
    public void setHintTextColor(int dayColorResId, int nightColorResId) {
        ResBean bean = new ResBean();
        bean.setDefaultResId(dayColorResId);
        bean.setNightResId(nightColorResId);
        mSkinProperter.setTextColorHint(bean);
        if (mTextColorWrapper == null) {
            mTextColorWrapper = new SkinWrapper4TextColor();
        }
        mTextColorWrapper.init(mContext, mSkinProperter);
        onUpdateRes();
    }
}
