package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.ImageView;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkinWrapper;

/**
 * Created by AutoSdk.
 */
public class SkinWrapper4Src implements ISkinWrapper<ImageView> {
    private SkinItems skinItems;


    @Override
    public void init(Context context, SkinItems skinItems) {
        this.skinItems = skinItems;
    }

    @Override
    public void apply(ImageView view, boolean isNight) {
        if (skinItems == null || skinItems.getSrc() == null) {
            return;
        }
        int resId = 0;
        if (isNight) {
            resId = skinItems.getSrc().getNightResId();
        } else {
            resId = skinItems.getSrc().getDefaultResId();
        }
        if (!TextUtils.isEmpty(NightModeGlobal.getSuffix())) {
            Resources res = view.getResources();
            String resPackage = res.getResourceName(resId);
            int suffixId = res.getIdentifier(resPackage+"_"+ NightModeGlobal.getSuffix(), null, null);
            if (suffixId != 0) {
                resId = suffixId;
            }
        }
        view.setImageResource(resId);
    }
}
