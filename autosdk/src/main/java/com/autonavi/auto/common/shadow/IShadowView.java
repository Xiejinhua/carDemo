package com.autonavi.auto.common.shadow;

import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by AutoSdk.
 */

public interface IShadowView {
    void initShadowView(AttributeSet attrs);
    void setShadowVisibility(int visibility);
    void draw(Canvas canvas, View view);
}
