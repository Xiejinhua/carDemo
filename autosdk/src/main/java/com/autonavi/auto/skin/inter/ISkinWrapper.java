package com.autonavi.auto.skin.inter;

import android.content.Context;
import android.view.View;

import com.autonavi.auto.skin.SkinItems;

/**
 * Created by AutoSdk.
 */
public interface ISkinWrapper<T extends View> {
    void init(Context context, SkinItems skinItems);

    void apply(T view, boolean isNight);
}
