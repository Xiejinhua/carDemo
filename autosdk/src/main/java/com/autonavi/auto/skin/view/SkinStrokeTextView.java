package com.autonavi.auto.skin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 *
 */
public class SkinStrokeTextView extends SkinTextView {

    private TextPaint mTextPaint;
    private int mStrokeTextColor;
    private int mInnerColor;
    private int mStrokeTextColorDay;
    private int mStrokeTextColorNight;
    private int mInnerTextColorDay;
    private int mInnerTextColorNight;
    private int strokeTextWidth;

    public SkinStrokeTextView(Context context) {
        super(context);
        mTextPaint = this.getPaint();
    }

    public SkinStrokeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SkinStrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStrokeTextColor(int color, int colorNight) {
        mStrokeTextColorDay = color;
        mStrokeTextColorNight = colorNight;
    }

    public void setInnerTextColor(int color, int colorNight) {
        mInnerTextColorDay = color;
        mInnerTextColorNight = colorNight;
    }

    public void setStrokeTextWidth(int width) {
        strokeTextWidth = width;
    }

    public void updateStrokeTextColor(boolean misNightMode) {
        if (misNightMode) {
            mStrokeTextColor = mStrokeTextColorNight;
        } else {
            mStrokeTextColor = mStrokeTextColorDay;
        }
    }

    public void updateInnerTextColor(boolean misNightMode) {
        if (misNightMode) {
            mInnerColor = mInnerTextColorNight;
        } else {
            mInnerColor = mInnerTextColorDay;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 描外层
        // super.setTextColor(Color.BLUE); // 不能直接这么设，如此会导致递归
        setTextColorUseReflection(mStrokeTextColor);
        mTextPaint.setStrokeWidth(strokeTextWidth); // 描边宽度
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE); // 描边种类
        mTextPaint.setFakeBoldText(false); // 外层text采用粗体
        mTextPaint.setShadowLayer(1, 0, 0, 0); // 字体的阴影效果，可以忽略
        super.onDraw(canvas);

        // 描内层，恢复原先的画笔

        // super.setTextColor(Color.BLUE); // 不能直接这么设，如此会导致递归
        setTextColorUseReflection(mInnerColor);
        mTextPaint.setStrokeWidth(0);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setFakeBoldText(true);
        mTextPaint.setShadowLayer(0, 0, 0, 0);

        super.onDraw(canvas);
    }

    /**
     * 使用反射的方法进行字体颜色的设置
     *
     * @param color
     */
    private void setTextColorUseReflection(int color) {
        Field textColorField;
        try {
            textColorField = TextView.class.getDeclaredField("mCurTextColor");
            textColorField.setAccessible(true);
            textColorField.set(this, color);
            textColorField.setAccessible(false);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        mTextPaint.setColor(color);
    }
}
