package com.autonavi.auto.skin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

import com.autonavi.auto.common.shadow.IShadowView;
import com.autonavi.auto.common.shadow.ShadowViewController;
import com.autonavi.auto.skin.impl.SeekBarSkinAdapter;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */

public class SkinSeekBar extends SeekBar implements ISkin,ISkin.IProgressBarViewSkin,ISkin.ISeekBarViewSkin, IShadowView {

    private SeekBarSkinAdapter mWrapper;
    private ShadowViewController mShadowController;
    private Drawable mThumbInSkin; //增加一个Thumb用来获取，getThumb有api限制，Skin增加一个变量来存储

    public SkinSeekBar(Context context) {
        super(context);
        init(null);
    }

    public SkinSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public SkinSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mWrapper = new SeekBarSkinAdapter(this, attrs);
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
    public void setProgressDrawable(int dayResId, int nightResId) {
        mWrapper.setProgressDrawable(dayResId, nightResId);
    }

    @Override
    public void setThumb(int dayResId, int nightResId) {
        mWrapper.setThumb(dayResId, nightResId);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumbInSkin = thumb;
    }

    public Drawable getThumbSkin(){
        return mThumbInSkin;
    }
}
