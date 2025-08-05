package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import com.autonavi.auto.skin.ResBean;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */

public class SeekBarSkinAdapter extends ViewSkinAdapter<SeekBar>{

    protected SkinWrapper4ProgressDrawable mProgressDrawableWrapper;
    protected SkinWrapper4Thumb mThumbWrapper;

    protected SeekBarSkinAdapter(Context context, SkinItems skinProperter) {
        super(context, skinProperter);
    }

    public SeekBarSkinAdapter(View view, AttributeSet attrs) {
        super(view, attrs);
    }

    public static ISkin.ISkinAdapter build(Context context, SkinItems skinProperter) {
        SeekBarSkinAdapter wrapper = new SeekBarSkinAdapter(context, skinProperter);
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
        mThumbWrapper = new SkinWrapper4Thumb();
        mThumbWrapper.init(mContext, mSkinProperter);
    }

    @Override
    public void applyImpl(boolean isNight) {
        super.applyImpl(isNight);
        if (mProgressDrawableWrapper != null) {
            mProgressDrawableWrapper.apply(view, isNight);
        }
        if(mThumbWrapper != null ){
            mThumbWrapper.apply(view, isNight);
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

    /**
     * 设置Thumb背景资源
     */
    public void setThumb(int dayResId, int nightResId) {
        ResBean bean = new ResBean();
        bean.setDefaultResId(dayResId);
        bean.setNightResId(nightResId);
        mSkinProperter.setThumb(bean);
        if (mThumbWrapper == null) {
            mThumbWrapper = new SkinWrapper4Thumb();
        }
        mThumbWrapper.init(mContext, mSkinProperter);
        onUpdateRes();
    }
}
