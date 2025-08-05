package com.autonavi.auto.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class StrokeTextView extends TextView {

    private TextView mStrokeTextView;


    public StrokeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attributeSet) {
        mStrokeTextView = new TextView(context, attributeSet);
        TextPaint paint = mStrokeTextView.getPaint();
        paint.setStrokeWidth(8);
        paint.setStyle(Paint.Style.STROKE);
        mStrokeTextView.setTextColor(Color.WHITE);
        mStrokeTextView.setGravity(getGravity());
    }

    public void setStrokeWidth(int width){
        TextPaint paint = mStrokeTextView.getPaint();
        paint.setStrokeWidth(width);
        paint.setStyle(Paint.Style.STROKE);
        mStrokeTextView.setTextColor(Color.WHITE);
        mStrokeTextView.setGravity(getGravity());
        invalidate();
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        mStrokeTextView.setLayoutParams(params);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        CharSequence outlineText = mStrokeTextView.getText();
        if (outlineText == null || !outlineText.equals(this.getText())) {
            mStrokeTextView.setText(getText());
            postInvalidate();
        }
        mStrokeTextView.measure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mStrokeTextView.layout(left, top, right, bottom);
    }

    /**
     *
     * @param str 文本内容
     * @param strokeWidth 描边宽度
     */
    public void setText(String str,int strokeWidth){
        if (TextUtils.isEmpty(str)) {
            //设置隐藏 不占图层绘制空间
            this.setVisibility(GONE);
        }else{
            this.setVisibility(VISIBLE);
            this.setStrokeWidth(strokeWidth);
            this.setText(str);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mStrokeTextView.draw(canvas);
        mStrokeTextView.setText(getText());
        super.onDraw(canvas);
    }

}
