package com.autonavi.auto.skin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.autonavi.auto.common.shadow.IShadowView;
import com.autonavi.auto.common.shadow.ShadowViewController;
import com.autonavi.auto.skin.impl.TextViewSkinAdapter;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */
public class SkinButton extends Button implements ISkin, ISkin.ITextViewSkin, IShadowView {


    private TextViewSkinAdapter mWrapper;
    private ShadowViewController mShadowController;

    public SkinButton(Context context) {
        super(context);
        init(null);
    }

    public SkinButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @SuppressWarnings("deprecation")
    public SkinButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mWrapper = new TextViewSkinAdapter(this, attrs);
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
    public void setBackgroundResource(int resid) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();
        super.setBackgroundResource(resid);
        setPadding(left, top, right, bottom);
    }

    @Override
    public void setBackground(int dayResId, int nightResId) {
        mWrapper.setBackground(dayResId, nightResId);
    }

    @Override
    public void setTextColor(int dayColorResId, int nightColorResId) {
        mWrapper.setTextColor(dayColorResId, nightColorResId);
    }

    @Override
    public void setHintTextColor(int dayColorResId, int nightColorResId) {
        mWrapper.setHintTextColor(dayColorResId, nightColorResId);
    }
}
