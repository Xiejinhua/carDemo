package com.autonavi.auto.skin.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
/**
 * Created by AutoSdk.
 */

public class SkinFontTextView extends SkinTextView {

    Paint.FontMetricsInt fontMetricsInt;

    static Typeface iconfont = null;

    public SkinFontTextView(Context context) {
        this(context,null);
    }

    public SkinFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs,defStyleAttr);
        //屏蔽掉，解决iconfont无法显示阴影问题
        //setLayerType(LAYER_TYPE_SOFTWARE,null);
    }

    public SkinFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void initTypeface(AttributeSet attrs) {
        if(null == iconfont) {
            iconfont = Typeface.createFromAsset(getResources().getAssets(), "icomoon.ttf");
        }
        setTypeface(iconfont);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (fontMetricsInt == null){
            fontMetricsInt = new Paint.FontMetricsInt();
            getPaint().getFontMetricsInt(fontMetricsInt);
        }
        canvas.translate(0, fontMetricsInt.top - fontMetricsInt.ascent);
    }
}
