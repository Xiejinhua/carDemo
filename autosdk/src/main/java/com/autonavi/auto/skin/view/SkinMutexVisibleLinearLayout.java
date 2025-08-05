package com.autonavi.auto.skin.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class SkinMutexVisibleLinearLayout extends LinearLayout {

    private int[] mMutexResIds;

    private ViewGroup mParentView;

    public SkinMutexVisibleLinearLayout(Context context) {
        this(context,null,0);
    }

    public SkinMutexVisibleLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SkinMutexVisibleLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewParent parent = this.getParent();
        if (parent instanceof ViewGroup) {
            mParentView = (ViewGroup) parent;
        }
    }

    public void mutexWith(int ...ids){
        mMutexResIds = ids;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mMutexResIds != null && mMutexResIds.length > 0) {
            for (int resId : mMutexResIds) {
                View target = findViewFromParent(resId);
                if (target != null) {
                    int mutexVisibility = visibility == VISIBLE ? GONE : VISIBLE;
                    target.setVisibility(mutexVisibility);
                }
            }
        }
    }

    private View findViewFromParent(int resId) {
        if (mParentView == null) {
            return null;
        }
        return mParentView.findViewById(resId);
    }
}
