package com.autonavi.auto.skin.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * @author AutoSdk
 */
public class SkinIconFontTextView extends SkinTextView{

    public SkinIconFontTextView(Context context) {
        this(context,null);
    }

    public SkinIconFontTextView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SkinIconFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(Typeface.createFromAsset(context.getAssets(),"font/iconfont.ttf"));
    }
}
