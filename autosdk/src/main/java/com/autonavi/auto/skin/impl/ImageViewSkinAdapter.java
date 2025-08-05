package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.autonavi.auto.skin.ResBean;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */
public class ImageViewSkinAdapter extends ViewSkinAdapter<ImageView> {

    protected SkinWrapper4Src mSrcWrapper;

    protected ImageViewSkinAdapter(Context context, SkinItems skinProperter) {
        super(context, skinProperter);
    }

    public ImageViewSkinAdapter(View view, AttributeSet attrs) {
        super(view, attrs);
    }

    public static ISkin.ISkinAdapter build(Context context, SkinItems skinProperter) {
        ImageViewSkinAdapter wrapper = new ImageViewSkinAdapter(context, skinProperter);
        return wrapper;
    }

    @Override
    public void initSkinImpl(View view) {
//        Timber.d("tag_skin", "initSkin = %s id=0x%s", view, Integer.toHexString(view.getId()));
        if (mSkinProperter == null) {
            return;
        }
        mSrcWrapper = new SkinWrapper4Src();
        mSrcWrapper.init(mContext, mSkinProperter);
    }

    @Override
    public void applyImpl(boolean isNight) {
        super.applyImpl(isNight);
        if (mSrcWrapper != null) {
            mSrcWrapper.apply(view, isNight);
        }
    }

    /**
     * 设置前景资源
     */
    public void setImageResource(int dayResId, int nightResId) {
        ResBean bean = new ResBean();
        bean.setDefaultResId(dayResId);
        bean.setNightResId(nightResId);
        mSkinProperter.setSrc(bean);
        if (mSrcWrapper == null) {
            mSrcWrapper = new SkinWrapper4Src();
        }
        mSrcWrapper.init(mContext, mSkinProperter);
        onUpdateRes();
    }
}
