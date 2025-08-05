package com.autonavi.auto.common.shadow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.autonavi.auto.skin.NightModeGlobal;

/**
 * Created by AutoSdk.
 */

public class ShadowViewController {

    private boolean mIsShowShadowAdapter = true;
    protected Context mContext;
    private View mView;
    private boolean mIsShowShadow = false;
    /**
     * 阴影的颜色-白天
     */
    private int mShadowColor = Color.TRANSPARENT;
    /**
     * 阴影的颜色-黑夜
     */
    private int mShadowColorNight = Color.TRANSPARENT;
    /**
     * 阴影的大小范围
     */
    private float mShadowRadius = 0;

    /**
     * 阴影 x 轴的偏移量
     */
    private int mShadowDx = 0;

    /**
     * 阴影 y 轴的偏移量
     */
    private int mShadowDy = 0;

    /**
     * 阴影圆角
     */
    private float mShadowCorner;
    private float mShadowCornerTopLeft;
    private float mShadowCornerTopRight;
    private float mShadowCornerBottomLeft;
    private float mShadowCornerBottomRight;

    /**
     * 阴影shape类型：oval、rectangle。。。
     */
    private String mShadowShapeType;

    public ShadowViewController(View view, AttributeSet attrs) {
        mView = view;
        mContext = mView.getContext();
        //initShadowView(attrs,view);
    }

    private void initShadowView(AttributeSet attrs, View view) {
        if (null == attrs || null == view) {
            return;
        }

        /*TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.autoShadow);
        if (typedArray != null) {
            boolean isShowShadow = typedArray.getBoolean(R.styleable.autoShadow_shadowVisibility,false);
            mIsShowShadow = isShowShadow && mIsShowShadowAdapter;
            if (mIsShowShadow) {
                view.setWillNotDraw(false);
                mShadowColor = typedArray.getColor(R.styleable.autoShadow_shadowColor,
                    mContext.getResources().getColor( android.R.color.black));
                mShadowColorNight = typedArray.getColor(R.styleable.autoShadow_shadowColorNight,-1);
                mShadowRadius = typedArray.getDimension(R.styleable.autoShadow_shadowRadius, 0);
                mShadowDx = (int)typedArray.getDimension(R.styleable.autoShadow_shadowDx, 0);
                mShadowDy = (int)typedArray.getDimension(R.styleable.autoShadow_shadowDy, 0);
                mShadowCorner = typedArray.getDimension(R.styleable.autoShadow_shadowCorner, mContext.getResources().getDimension(R.dimen.auto_dimen2_4));
                mShadowCornerTopLeft = typedArray.getDimension(R.styleable.autoShadow_shadowCornerTopLeft, mContext.getResources().getDimension(R.dimen.auto_dimen2_0));
                mShadowCornerTopRight = typedArray.getDimension(R.styleable.autoShadow_shadowCornerTopRight, mContext.getResources().getDimension(R.dimen.auto_dimen2_0));
                mShadowCornerBottomLeft = typedArray.getDimension(R.styleable.autoShadow_shadowCornerBottomLeft, mContext.getResources().getDimension(R.dimen.auto_dimen2_0));
                mShadowCornerBottomRight = typedArray.getDimension(R.styleable.autoShadow_shadowCornerBottomRight, mContext.getResources().getDimension(R.dimen.auto_dimen2_0));
                //mShadowViewId = typedArray.getResourceId(R.styleable.autoShadow_shadowViewId,0);
                mShadowShapeType = typedArray.getString(R.styleable.autoShadow_shadowShapeType);
            }
            Log.d("hlf", "initShadowView: mIsShowShadow=" + mIsShowShadow);
            typedArray.recycle();
        }*/
    }

    public void draw(Canvas canvas, View view) {
        if (!mIsShowShadow || null == canvas || null == view) {
            return;
        }
        //如果shape是圆形，则取宽度作为corner
        if (null != mShadowShapeType && "oval".equalsIgnoreCase(mShadowShapeType)) {
            mShadowCorner = view.getWidth();
        }
        int shadowColor = mShadowColor;
        //如果当前是黑夜，并且黑夜阴影有数据，则使用黑夜的阴影色值，否则默认使用白天的阴影色值
        if (NightModeGlobal.isNightMode() && mShadowColorNight != -1) {
            shadowColor = mShadowColorNight;
        }
        Log.d("hlf", "draw:mShadowCorner= " + mShadowCorner);
        if (mShadowCorner > 0) {
            //绘制阴影
            ShadowHelper.draw(canvas, view,
                    Config.obtain()
                            .color(shadowColor)
                            .leftTopCorner((int) mShadowCorner)
                            .rightTopCorner((int) mShadowCorner)
                            .leftBottomCorner((int) mShadowCorner)
                            .rightBottomCorner((int) mShadowCorner)
                            .radius(mShadowRadius)
                            .xOffset(mShadowDx)
                            .yOffset(mShadowDy)
            );
        } else {
            //绘制阴影
            ShadowHelper.draw(canvas, view,
                    Config.obtain()
                            .color(shadowColor)
                            .leftTopCorner((int) mShadowCornerTopLeft + 3)
                            .rightTopCorner((int) mShadowCornerTopRight + 3)
                            .leftBottomCorner((int) mShadowCornerBottomLeft + 3)
                            .rightBottomCorner((int) mShadowCornerBottomRight + 3)
                            .radius(mShadowRadius)
                            .xOffset(mShadowDx)
                            .yOffset(mShadowDy)
            );
        }
    }

    public void setShadowColor(int shadowColor) {
        this.mShadowColor = shadowColor;
    }

    public void setShadowColorNight(int shadowColorNight) {
        this.mShadowColorNight = shadowColorNight;
    }

    public void setShadowRadius(float shadowRadius) {
        this.mShadowRadius = shadowRadius;
    }

    public void setShadowDx(int shadowDx) {
        this.mShadowDx = shadowDx;
    }

    public void setShadowDy(int shadowDy) {
        this.mShadowDy = shadowDy;
    }

    public void setShadowCorner(float shadowCorner) {
        this.mShadowCorner = shadowCorner;
    }

    public void setShadowCornerTopLeft(float shadowCornerTopLeft) {
        this.mShadowCornerTopLeft = shadowCornerTopLeft;
    }

    public void setShadowCornerTopRight(float shadowCornerTopRight) {
        this.mShadowCornerTopRight = shadowCornerTopRight;
    }

    public void setShadowCornerBottomLeft(float shadowCornerBottomLeft) {
        this.mShadowCornerBottomLeft = shadowCornerBottomLeft;
    }

    public void setShadowCornerBottomRight(float shadowCornerBottomRight) {
        this.mShadowCornerBottomRight = shadowCornerBottomRight;
    }

    public void setShadowShapeType(String shadowShapeType) {
        this.mShadowShapeType = shadowShapeType;
    }

    public void setVisibility(int visibility) {
        if (visibility == View.GONE) {
            mIsShowShadow = false;
            ShadowHelper.resetShadow();
            mView.invalidate();
        } else {
            mIsShowShadow = true;
            mView.invalidate();
        }
    }
}
