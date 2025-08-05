package com.autonavi.auto.common.shadow;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.autosdk.R;

public class ShadowView extends View {

    public static final int ALL = 0x1111;

    public static final int LEFT = 0x0001;

    public static final int TOP = 0x0010;

    public static final int RIGHT = 0x0100;

    public static final int BOTTOM = 0x1000;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private RectF mRectF = new RectF();

    /**
     * 阴影的颜色
     */
    private int mShadowColor = Color.TRANSPARENT;

    /**
     * 阴影的大小范围
     */
    private float mShadowRadius = 0;

    /**
     * 阴影 x 轴的偏移量
     */
    private float mShadowDx = 0;

    /**
     * 阴影 y 轴的偏移量
     */
    private float mShadowDy = 0;

    /**
     * 阴影显示的边界
     */
    private int mShadowSide = ALL;

    /**
     * 添加阴影的view id
     */
    private int mShadowViewId = 0;

    /**
     * 阴影圆角
     */
    private float mShadowCorner;

    /**
     * 阴影shape类型：oval、rectangle。。。
     */
    private String mShadowShapeType;

    public ShadowView(Context context) {
        this(context, null);
    }

    public ShadowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    /**
     * 调整阴影大小
     */
    private void reSizeView() {
        ViewGroup parentView = (ViewGroup)getParent();
        if (null != parentView) {
            View view = parentView.findViewById(mShadowViewId);
            if (null != view && view.getWidth() > 0 && view.getHeight() > 0) {
                view.bringToFront();
                //将阴影层级移到底部
                //moveToBack(this);

                ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams)getLayoutParams();
                layoutParams.width = view.getWidth() + (int)mShadowRadius * 2;
                layoutParams.height = view.getHeight() + (int)mShadowRadius * 2;
                setLayoutParams(layoutParams);

                //如果shape是圆形，则取宽度作为corner
                if ("oval".equalsIgnoreCase(mShadowShapeType)) {
                    mShadowCorner = view.getWidth();
                }
                Log.d("hlf", "setBackgroundCompat: width1=" + view.getWidth() + " heitht=" + view.getHeight());
                Log.d("hlf", "setBackgroundCompat1: width2=" + getWidth() + " heitht=" + getHeight());
            }
        }
    }

    private void moveToBack(View currentView) {
        ViewGroup viewGroup = ((ViewGroup) currentView.getParent());
        int index = viewGroup.indexOfChild(currentView);
        Log.d("hlf", "moveToBack: index==" + index);
        for(int i = 0; i<index; i++) {
            viewGroup.bringChildToFront(viewGroup.getChildAt(i));
        }
    }

    /**
     * 获取绘制阴影的位置，并为 ShadowLayout 设置 Padding 以为显示阴影留出空间
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        //reSizeView();

        float effect = mShadowRadius + dip2px(5);
        float rectLeft = 0;
        float rectTop = 0;
        float rectRight = this.getWidth();
        float rectBottom = this.getHeight();
        int paddingLeft = 0;
        int paddingTop = 0;
        int paddingRight = 0;
        int paddingBottom = 0;

        if (((mShadowSide & LEFT) == LEFT)) {
            rectLeft = effect;
            paddingLeft = (int) effect;
        }
        if (((mShadowSide & TOP) == TOP)) {
            rectTop = effect;
            paddingTop = (int) effect;
        }
        if (((mShadowSide & RIGHT) == RIGHT)) {
            rectRight = this.getWidth() - effect;
            paddingRight = (int) effect;
        }
        if (((mShadowSide & BOTTOM) == BOTTOM)) {
            rectBottom = this.getHeight() - effect;
            paddingBottom = (int) effect;
        }
        if (mShadowDy != 0.0f) {
            rectBottom = rectBottom - mShadowDy;
            paddingBottom = paddingBottom + (int) mShadowDy;
        }
        if (mShadowDx != 0.0f) {
            rectRight = rectRight - mShadowDx;
            paddingRight = paddingRight + (int) mShadowDx;
        }
        mRectF.left = rectLeft;
        mRectF.top = rectTop;
        mRectF.right = rectRight;
        mRectF.bottom = rectBottom;
        this.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
    }

    /**
     * 真正绘制阴影的方法
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRoundRect(mRectF,mShadowCorner,mShadowCorner,mPaint);
        //canvas.drawRect(mRectF, mPaint);
    }

    /**
     * 读取设置的阴影的属性
     *
     * @param attrs 从其中获取设置的值
     */
    private void init(AttributeSet attrs) {
        this.setBackgroundColor(Color.TRANSPARENT);

        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.autoShadow);
        if (typedArray != null) {
            mShadowColor = typedArray.getColor(R.styleable.autoShadow_shadowColor,
                getResources().getColor( android.R.color.black));
            mShadowRadius = typedArray.getDimension(R.styleable.autoShadow_shadowRadius, dip2px(0));
            mShadowDx = typedArray.getDimension(R.styleable.autoShadow_shadowDx, dip2px(0));
            mShadowDy = typedArray.getDimension(R.styleable.autoShadow_shadowDy, dip2px(0));
            mShadowSide = typedArray.getInt(R.styleable.autoShadow_shadowSide, ALL);
            mShadowCorner = typedArray.getDimension(R.styleable.autoShadow_shadowCorner, 0);
            //mShadowViewId = typedArray.getResourceId(R.styleable.autoShadow_shadowViewId,0);
            mShadowShapeType = typedArray.getString(R.styleable.autoShadow_shadowShapeType);
            typedArray.recycle();
        }
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
    }

    public void prepareDraw(int viewWidth) {
        //如果shape是圆形，则取宽度作为corner
        if (null != mShadowShapeType && "oval".equalsIgnoreCase(mShadowShapeType)) {
            mShadowCorner = viewWidth;
        }
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);  // 关闭硬件加速
        this.setWillNotDraw(false);                    // 调用此方法后，才会执行 onDraw(Canvas) 方法
    }

    public int getShadowRadius() {
        return (int)mShadowRadius*2;
    }

    /**
     * dip2px dp 值转 px 值
     *
     * @param dpValue dp 值
     * @return px 值
     */
    private float dip2px(float dpValue) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        float scale = dm.density;
        return (dpValue * scale + 0.5F);
    }

}
