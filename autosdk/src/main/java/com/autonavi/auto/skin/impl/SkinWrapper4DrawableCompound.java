package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.TextView;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.ResBean;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkinWrapper;

/**
 * Created by AutoSdk.
 */
public class SkinWrapper4DrawableCompound implements ISkinWrapper<TextView> {

    private SkinItems skinItems;


    @Override
    public void init(Context context, SkinItems skinItems) {
        this.skinItems = skinItems;
    }

    @Override
    public void apply(TextView view, boolean isNight) {
        if (skinItems == null) {
            return;
        }
        if (skinItems.getDrawableLeft() == null && skinItems.getDrawableTop() == null && skinItems.getDrawableRight() == null && skinItems.getDrawableBottom() == null) {
            return;
        }
        Drawable left = getDrawable(view.getContext(), skinItems.getDrawableLeft(), isNight);
        Drawable right = getDrawable(view.getContext(), skinItems.getDrawableRight(), isNight);
        Drawable top = getDrawable(view.getContext(), skinItems.getDrawableTop(), isNight);
        Drawable bottom = getDrawable(view.getContext(), skinItems.getDrawableBottom(), isNight);
        view.setCompoundDrawables(left, top, right, bottom);
    }

    private Drawable getDrawable(Context context, ResBean bean, boolean isNight) {
        if (bean == null || bean.getDefaultResId() <= 0) {
            return null;
        }
        int resId = bean.getDefaultResId();
        if (isNight) {
            resId = bean.getNightResId();
        }
        if (!TextUtils.isEmpty(NightModeGlobal.getSuffix())) {
            Resources res = context.getResources();
            String resPackage = res.getResourceName(resId);
            int suffixId = res.getIdentifier(resPackage+"_"+ NightModeGlobal.getSuffix(), null, null);
            if (suffixId != 0) {
                resId = suffixId;
            }
        }
        Drawable drawable = null;
        if (resId > 0) {
            drawable = context.getResources().getDrawable(resId);
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        }
        return drawable;
    }
}
