package com.autonavi.auto.common.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.constraintlayout.widget.Guideline;

/**
 * Created by AutoSdk.
 */

public class BaseGuideline extends Guideline {
    public BaseGuideline(Context context) {
        super(context);
    }

    public BaseGuideline(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseGuideline(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BaseGuideline(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
