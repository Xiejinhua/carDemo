package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.view.View;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkinWrapper;

/**
 * Created by AutoSdk.
 */
public class SkinWrapper4Background implements ISkinWrapper<View> {
    private SkinItems skinItems;


    @Override
    public void init(Context context, SkinItems skinItems) {
        this.skinItems = skinItems;
    }

    @Override
    public void apply(View view, boolean isNight) {
        if (skinItems == null || skinItems.getBackground() == null) {
            return;
        }
        int resId = 0;
        if (isNight) {
            resId = skinItems.getBackground().getNightResId();
        } else {
            resId = skinItems.getBackground().getDefaultResId();
        }
        if (!TextUtils.isEmpty(NightModeGlobal.getSuffix())) {
            Resources res = view.getResources();
            String resPackage = res.getResourceName(resId);
            int suffixId = res.getIdentifier(resPackage+"_"+ NightModeGlobal.getSuffix(), null, null);
            if (suffixId != 0) {
                resId = suffixId;
            }
        }
        /**
         * 直接在各自实现中处理，避免重复设置,此外，部分6.0系统也存在这个问题，外部直接设置，不区分版本号
         * @since 2016/09/28
         */
        view.setBackgroundResource(resId);
    }

}
