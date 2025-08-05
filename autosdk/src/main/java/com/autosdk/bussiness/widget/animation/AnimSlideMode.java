package com.autosdk.bussiness.widget.animation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author AutoSDK
 */
@IntDef({AnimSlideMode.DEFAULT,AnimSlideMode.LEFT_IN, AnimSlideMode.LEFT_OUT, AnimSlideMode.RIGHT_IN, AnimSlideMode.RIGHT_OUT,
        AnimSlideMode.UP_IN, AnimSlideMode.UP_OUT, AnimSlideMode.DOWN_IN, AnimSlideMode.DOWN_OUT})
@Retention(RetentionPolicy.SOURCE)
public @interface AnimSlideMode {
    int DEFAULT = -1;
    int LEFT_IN = 0;
    int LEFT_OUT = 1;
    int RIGHT_IN = 2;
    int RIGHT_OUT = 3;
    int UP_IN = 4;
    int UP_OUT = 5;
    int DOWN_IN = 6;
    int DOWN_OUT = 7;
}
