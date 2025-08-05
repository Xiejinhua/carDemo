package com.autonavi.auto.skin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ScrollView;

import com.autonavi.auto.common.shadow.IShadowView;
import com.autonavi.auto.common.shadow.ShadowViewController;
import com.autonavi.auto.skin.impl.ViewSkinAdapter;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */
public class SkinScrollView extends ScrollView implements ISkin, ISkin.IViewSkin, IShadowView {


    private ViewSkinAdapter mWrapper;
    private ShadowViewController mShadowController;

    public SkinScrollView(Context context) {
        super(context);
        init(null);
    }

    public SkinScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SkinScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);

    }

    private void init(AttributeSet attrs) {
        mWrapper = new ViewSkinAdapter(this, attrs);
        mWrapper.updateView(this);
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

}
