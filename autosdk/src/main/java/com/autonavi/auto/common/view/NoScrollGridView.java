package com.autonavi.auto.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 通用-无法滚动的GridView
 * Created by AutoSdk.
 */
public class NoScrollGridView extends GridView {

    public NoScrollGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NoScrollGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollGridView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
