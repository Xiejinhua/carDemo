package com.autonavi.auto.skin.impl;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.TextView;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.ResBean;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkinWrapper;

/**
 * Created by AutoSdk.
 */
public class SkinWrapper4TextColor implements ISkinWrapper<TextView> {

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
        if (skinItems.getTextColor() != null) {
            view.setTextColor(getColor(view.getContext(), skinItems.getTextColor(), isNight));
        }
        if (skinItems.getTextColorHint() != null) {
            view.setHintTextColor(getColor(view.getContext(), skinItems.getTextColorHint(), isNight));
        }

    }

    /**
     * 根据资源id，获取对应的颜色值
     *
     * @param context
     * @param resBean
     * @param isNight
     * @return
     */
    private ColorStateList getColor(Context context, ResBean resBean, boolean isNight) {
        ColorStateList textColor;
        int textColorId = resBean.getDefaultResId();
        if (isNight) {
            textColorId = resBean.getNightResId();
        }
        if (!TextUtils.isEmpty(NightModeGlobal.getSuffix())) {
            Resources res = context.getResources();
            String resPackage = res.getResourceName(textColorId);
            int suffixId = res.getIdentifier(resPackage+"_"+ NightModeGlobal.getSuffix(), null, null);
            if (suffixId != 0) {
                textColorId = suffixId;
            }
        }
        textColor = context.getResources().getColorStateList(textColorId);
        return textColor;
    }
}
