package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import com.autonavi.auto.skin.ResBean;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */

public class ProgressBarSkinAdapter extends ViewSkinAdapter<ProgressBar>{

    protected SkinWrapper4ProgressDrawable mProgressDrawableWrapper;

    protected ProgressBarSkinAdapter(Context context, SkinItems skinProperter) {
        super(context, skinProperter);
    }

    public ProgressBarSkinAdapter(View view, AttributeSet attrs) {
        super(view, attrs);
    }

    public static ISkin.ISkinAdapter build(Context context, SkinItems skinProperter) {
        ProgressBarSkinAdapter wrapper = new ProgressBarSkinAdapter(context, skinProperter);
        return wrapper;
    }

    @Override
    public void initSkinImpl(View view) {
//        Timber.d("tag_skin", "initSkin = %s id=0x%s", view, Integer.toHexString(view.getId()));
        if (mSkinProperter == null) {
            return;
        }
        mProgressDrawableWrapper = new SkinWrapper4ProgressDrawable();
        mProgressDrawableWrapper.init(mContext, mSkinProperter);
    }

    @Override
    public void applyImpl(boolean isNight) {
        super.applyImpl(isNight);
        if (mProgressDrawableWrapper != null) {
            mProgressDrawableWrapper.apply(view, isNight);
        }
    }

    /**
     * 设置ProgressBar背景资源
     */
    public void setProgressDrawable(int dayResId, int nightResId) {
        ResBean bean = new ResBean();
        bean.setDefaultResId(dayResId);
        bean.setNightResId(nightResId);
        mSkinProperter.setProgressDrawable(bean);
        if (mProgressDrawableWrapper == null) {
            mProgressDrawableWrapper = new SkinWrapper4ProgressDrawable();
        }
        mProgressDrawableWrapper.init(mContext, mSkinProperter);
        onUpdateRes();
    }
}
