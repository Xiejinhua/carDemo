package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.text.TextUtils;
import android.widget.ImageView;
import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkinWrapper;
import com.autonavi.auto.skin.view.SkinSVGImageView;

/**
 * Created by AutoSdk.
 */
public class SkinWrapper4Svg implements ISkinWrapper<ImageView> {
    private SkinItems skinItems;
    private boolean isNightMode = false;

    private boolean isOpenSvgDrawable = true;

    @Override
    public void init(Context context, SkinItems skinItems) {
        this.skinItems = skinItems;
    }

    @Override
    public void apply(ImageView view, boolean isNight) {
        if(!isOpenSvgDrawable) {
            return;
        }
        if (skinItems == null || skinItems.getSvgColor() == null) {
            return;
        }
        this.isNightMode = isNight;
        int resId = 0;
        if (isNight) {
            resId = skinItems.getSvgColor().getNightResId();
        } else {
            resId = skinItems.getSvgColor().getDefaultResId();
        }
        if (!TextUtils.isEmpty(NightModeGlobal.getSuffix())) {
            Resources res = view.getResources();
            String resPackage = res.getResourceName(resId);
            int suffixId = res.getIdentifier(resPackage + "_" + NightModeGlobal.getSuffix(), null, null);
            if (suffixId != 0) {
                resId = suffixId;
            }
        }

        Drawable drawable = view.getDrawable();
        if (drawable != null && drawable instanceof VectorDrawable) {
            VectorDrawable vectorDrawable = (VectorDrawable)drawable;
            vectorDrawable.setColorFilter(
                new PorterDuffColorFilter(view.getResources().getColor(resId), PorterDuff.Mode.SRC_IN));
            view.setImageDrawable(vectorDrawable);
        } else if (drawable != null && drawable instanceof BitmapDrawable){
            BitmapDrawable vectorDrawable = (BitmapDrawable)drawable;
            vectorDrawable.setColorFilter(
                new PorterDuffColorFilter(view.getResources().getColor(resId), PorterDuff.Mode.SRC_IN));
            view.setColorFilter(view.getResources().getColor(resId), PorterDuff.Mode.SRC_IN);
            view.setImageDrawable(vectorDrawable);
        }
    }

    public void setSelected(SkinSVGImageView view, boolean selected,boolean isSelectedColor) {
        if (skinItems == null || skinItems.getSvgColor() == null) {
            return;
        }
        int resId = 0;
        if (NightModeGlobal.isNightMode()) {
            resId = skinItems.getSvgColor().getNightResId();
        } else {
            resId = skinItems.getSvgColor().getDefaultResId();
        }
        if (selected && isSelectedColor) {
            Resources res = view.getResources();
            String resPackage = res.getResourceName(resId);
            if (resPackage.contains("_normal")) {
                resPackage = resPackage.replace("_normal", "_selected");
            }else{
                return;
            }
            int selectId = res.getIdentifier(resPackage, null, null);
            if (selectId != 0) {
                resId = selectId;
            }
        }

        setImageResource(view, resId);
    }

    public void setPressed(SkinSVGImageView view, boolean pressed) {
        if (skinItems == null || skinItems.getSvgColor() == null) {
            return;
        }
        if (view.isSelected()) {
            return;
        }
        int resId = 0;
        if (NightModeGlobal.isNightMode()) {
            resId = skinItems.getSvgColor().getNightResId();
        } else {
            resId = skinItems.getSvgColor().getDefaultResId();
        }
        if (pressed) {
            Resources res = view.getResources();
            String resPackage = res.getResourceName(resId);
            if (resPackage.contains("_normal")) {
                resPackage = resPackage.replace("_normal", "_pressed");
            }else{
                return;
            }
            int pressedId = res.getIdentifier(resPackage, null, null);
            if (pressedId != 0) {
                resId = pressedId;
            } else {
                return;
            }
        }

        setImageResource(view, resId);
    }

    public void setEnabled(SkinSVGImageView view, boolean enabled) {
        if (skinItems == null || skinItems.getSvgColor() == null) {
            return;
        }
        int resId = 0;
        if (NightModeGlobal.isNightMode()) {
            resId = skinItems.getSvgColor().getNightResId();
        } else {
            resId = skinItems.getSvgColor().getDefaultResId();
        }
        if (!enabled) {
            Resources res = view.getResources();
            String resPackage = res.getResourceName(resId);
            if (resPackage.contains("_normal")) {
                resPackage = resPackage.replace("_normal", "_disabled");
            }else{
                return;
            }
            int disabledId = res.getIdentifier(resPackage, null, null);
            if (disabledId != 0) {
                resId = disabledId;
            } else {
                return;
            }
        }

        setImageResource(view, resId);
    }

    private void setImageResource(SkinSVGImageView view, int resId) {
        if(!isOpenSvgDrawable) {
            return;
        }
        Drawable drawable = view.getDrawable();
        if (drawable != null && drawable instanceof VectorDrawable) {
            VectorDrawable vectorDrawable = (VectorDrawable)drawable;
            vectorDrawable.setColorFilter(
                    new PorterDuffColorFilter(view.getResources().getColor(resId), PorterDuff.Mode.SRC_IN));
            view.setColorFilter(view.getResources().getColor(resId), PorterDuff.Mode.SRC_IN);
            view.setImageDrawable(vectorDrawable);
        }else if (drawable instanceof BitmapDrawable){
            BitmapDrawable vectorDrawable = (BitmapDrawable)drawable;
            vectorDrawable.setColorFilter(
                    new PorterDuffColorFilter(view.getResources().getColor(resId), PorterDuff.Mode.SRC_IN));
            view.setColorFilter(view.getResources().getColor(resId), PorterDuff.Mode.SRC_IN);
            view.setImageDrawable(vectorDrawable);
        }
    }
}
