package com.desaysv.psmap.model.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;

import com.autonavi.auto.skin.view.SkinRelativeLayout;
import com.desaysv.psmap.model.R;

/**
 * 可以设置阴影的布局
 * 需要在父布局使用android:clipChildren="false"才能显示
 */
public class ShadowLayout extends SkinRelativeLayout {
    private static final String TAG = "ShadowLayout";

    private Paint mPaint = new Paint();
    private RectF mRect = new RectF();
    //控件背景的类型，0代表是单纯的颜色，1代表背景图是shape
    private int mBgType = -1;

    private int mShadowColor = Color.BLACK;
    private float mShadowRadius = 0;
    private float mShadowDx = 0;
    private float mShadowDy = 0;
    private float mRadius = 0;

    public ShadowLayout(Context context) {
        this(context, null);
    }

    public ShadowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 读取设置的阴影的属性
     *
     * @param attrs 从其中获取设置的值
     */
    private void init(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ShadowLayout);
        if (typedArray != null) {
            mShadowColor = typedArray.getColor(R.styleable.ShadowLayout_shadowColor, Color.BLACK);
            mShadowRadius = typedArray.getDimension(R.styleable.ShadowLayout_shadowRadius, 0);
            mShadowDx = typedArray.getDimension(R.styleable.ShadowLayout_shadowDx, 0);
            mShadowDy = typedArray.getDimension(R.styleable.ShadowLayout_shadowDy, 0);
            mRadius = typedArray.getDimension(R.styleable.ShadowLayout_radius, 0);
            typedArray.recycle();
        }
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
    }

    public void updateConfig(float radius, float dx, float dy, int shadowColor) {
        mPaint.setShadowLayer(radius, dx, dy, shadowColor);
    }

    public void setShadowColor(int shadowColor) {
        mShadowColor = shadowColor;
        mPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
        invalidate(); // 重新绘制以应用新的阴影颜色
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(TAG, "onLayout: changed = [" + changed + "], l = [" + l + "], t = [" + t + "], r = ["
                + r + "], b = [" + b + "]");
        super.onLayout(changed, l, t, r, b);
        mRect.right = getWidth();
        mRect.bottom = getHeight();
        Drawable drawable = getBackground();
        if (drawable != null) {
            if (drawable instanceof ColorDrawable) {
                mPaint.setColor(((ColorDrawable) drawable).getColor());
                mBgType = 0;
            } else if (drawable instanceof GradientDrawable) { //shape属于GradientDrawable
                ColorStateList colorStateList = ((GradientDrawable) drawable).getColor();
                Log.d(TAG, "onLayout: colorStateList = " + colorStateList);
                final int color;
                if (colorStateList == null) {
                    color = Color.TRANSPARENT;
                } else {
                    final int[] stateSet = drawable.getState();
                    //拿到shape中的颜色
                    color = colorStateList.getColorForState(stateSet, 0);
                    mBgType = 1;
                }
                mPaint.setColor(color);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBgType == 0) {
            //画矩形
            canvas.drawRect(mRect, mPaint);
        } else if (mBgType == 1) {
            //画圆角矩形
            canvas.drawRoundRect(mRect, mRadius, mRadius, mPaint);//第二个参数是x半径，第三个参数是y半径
        }
        Log.i(TAG, "onDraw: ");
        super.onDraw(canvas);
    }

}