package com.autonavi.auto.skin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.autonavi.auto.common.shadow.IShadowView;
import com.autonavi.auto.common.shadow.ShadowViewController;
import com.autonavi.auto.skin.SkinManager;
import com.autonavi.auto.skin.impl.SVGImageViewSkinAdapter;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */
public class SkinSVGImageView extends ImageView implements ISkin, ISkin.ISVGSkin, IShadowView {


    private SVGImageViewSkinAdapter mWrapper;
    private ShadowViewController mShadowController;

    public SkinSVGImageView(Context context) {
        super(context);
        init(null);
    }

    public SkinSVGImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @SuppressWarnings("deprecation")
    public SkinSVGImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mWrapper = new SVGImageViewSkinAdapter(this, attrs);
        mWrapper.updateSvgDefault();
        mWrapper.updateView(this);
        initShadowView(attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        draw(canvas,this);
    }

    @Override
    public void initShadowView(AttributeSet attrs) {
        if (null == mShadowController) {
            mShadowController = new ShadowViewController(this, attrs);
        }
    }

    @Override
    public void draw(Canvas canvas, View view) {
        if (null != mShadowController) {
            mShadowController.draw(canvas,view);
        }
    }

    @Override
    public void setShadowVisibility(int visibility) {
        if (null != mShadowController) {
            mShadowController.setVisibility(visibility);
        }
    }

    @Override
    public ISkinAdapter getAdpter() {
        return mWrapper;
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        mWrapper.updateSvgDefault();
    }

    @Override
    public void setBackgroundResource(int resid) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        super.setBackgroundResource(resid);
        setPadding(left, top, right, bottom);
    }

    @Override
    public void setSVGColor(int dayResId, int nightResId) {
        mWrapper.setSvgColor(dayResId, nightResId);
        SkinManager.getInstance().updateView(this,true);
    }

    public void setSelected(boolean selected,boolean isSelectedColor) {
        super.setSelected(selected);
        mWrapper.setSvgColorSelected(this,selected,isSelectedColor);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mWrapper.setSvgColorSelected(this,selected,true);
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        mWrapper.setSvgColorPressed(this,pressed);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mWrapper.setSvgColorEnabled(this,enabled);
    }

    @Override
    public void setBackground(int dayResId, int nightResId) {
        mWrapper.setBackground(dayResId, nightResId);
    }
}
