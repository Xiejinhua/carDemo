package com.autonavi.auto.skin.view;

import android.content.Context;
import android.util.AttributeSet;

import com.autonavi.auto.common.view.AutoEditText;
import com.autonavi.auto.skin.impl.TextViewSkinAdapter;
import com.autonavi.auto.skin.inter.ISkin;

/**
 * Created by AutoSdk.
 */
public class SkinImeEditText extends AutoEditText implements ISkin, ISkin.ITextViewSkin {
    /**
     * /*
     * 修改字体信息
     */
    private TextViewSkinAdapter mWrapper;

    public SkinImeEditText(Context context) {
        super(context);
        init(null);
    }

    public SkinImeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    @SuppressWarnings("deprecation")
    public SkinImeEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mWrapper = new TextViewSkinAdapter(this, attrs);
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
