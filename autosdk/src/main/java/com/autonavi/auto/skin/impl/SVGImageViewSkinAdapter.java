package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.autonavi.auto.skin.ResBean;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkin;
import com.autonavi.auto.skin.view.SkinSVGImageView;

/**
 * Created by AutoSdk.
 */
public class SVGImageViewSkinAdapter extends ViewSkinAdapter<ImageView> {

    protected SkinWrapper4Svg mSrcWrapper;

    protected SVGImageViewSkinAdapter(Context context, SkinItems skinProperter) {
        super(context, skinProperter);
    }

    public SVGImageViewSkinAdapter(View view, AttributeSet attrs) {
        super(view, attrs);
    }

    public static ISkin.ISkinAdapter build(Context context, SkinItems skinProperter) {
        SVGImageViewSkinAdapter wrapper = new SVGImageViewSkinAdapter(context, skinProperter);
        return wrapper;
    }

    @Override
    public void initSkinImpl(View view) {
//        Timber.d("tag_skin", "initSkin = %s id=0x%s", view, Integer.toHexString(view.getId()));
        if (mSkinProperter == null) {
            return;
        }
        mSrcWrapper = new SkinWrapper4Svg();
        mSrcWrapper.init(mContext, mSkinProperter);
    }

    @Override
    public void applyImpl(boolean isNight) {
        if (mSrcWrapper != null) {
            mSrcWrapper.apply(view, isNight);
        }
    }

    /**
     * 设置前景资源
     */
    public void setSvgColor(int dayResId, int nightResId) {
        ResBean bean = new ResBean();
        bean.setDefaultResId(dayResId);
        bean.setNightResId(nightResId);
        mSkinProperter.setSvgColor(bean);
        if (mSrcWrapper == null) {
            mSrcWrapper = new SkinWrapper4Svg();
        }
        mSrcWrapper.init(mContext, mSkinProperter);
        onUpdateRes();
    }

    public void updateSvgDefault(){
        onUpdateRes();
    }

    public void setSvgColorSelected(SkinSVGImageView skinSVGImageView, boolean selected,boolean isSelectedColor) {
        if (mSrcWrapper == null) {
            return;
        }
        mSrcWrapper.setSelected(skinSVGImageView,selected,isSelectedColor);
        onUpdateRes();
    }

    public void setSvgColorPressed(SkinSVGImageView skinSVGImageView, boolean pressed) {
        if (mSrcWrapper == null) {
            return;
        }
        mSrcWrapper.setPressed(skinSVGImageView,pressed);
        onUpdateRes();
    }

    public void setSvgColorEnabled(SkinSVGImageView skinSVGImageView, boolean enabled) {
        if (mSrcWrapper == null) {
            return;
        }
        mSrcWrapper.setEnabled(skinSVGImageView,enabled);
        onUpdateRes();
    }
}
