package com.autonavi.auto.skin.impl;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkinWrapper;

/**
 * Created by AutoSdk.
 */

public class SkinWrapper4ProgressDrawable implements ISkinWrapper<ProgressBar> {

    private SkinItems skinItems;
    private Context mContext;

    @Override
    public void init(Context context, SkinItems skinItems) {
        this.mContext = context;
        this.skinItems = skinItems;
    }

    @Override
    public void apply(ProgressBar view, boolean isNight) {
        if (skinItems == null || skinItems.getProgressDrawable() == null) {
            return;
        }
        int resId = 0;
        if (isNight) {
            resId = skinItems.getProgressDrawable().getNightResId();
        } else {
            resId = skinItems.getProgressDrawable().getDefaultResId();
        }
        if (!TextUtils.isEmpty(NightModeGlobal.getSuffix())) {
            Resources res = view.getResources();
            String resPackage = res.getResourceName(resId);
            int suffixId = res.getIdentifier(resPackage+"_"+ NightModeGlobal.getSuffix(), null, null);
            if (suffixId != 0) {
                resId = suffixId;
            }
        }
        if(resId > 0){
            Drawable draw = mContext.getResources().getDrawable(resId);
            Rect bounds = view.getProgressDrawable().getBounds();
            view.setProgressDrawable(draw);
            if(view instanceof SeekBar){ //SeekBar 代码设置progressDrawable之后高度会变化，用该方法屏蔽这个bug
                view.getProgressDrawable().setBounds(bounds);
            }
        }
    }
}
