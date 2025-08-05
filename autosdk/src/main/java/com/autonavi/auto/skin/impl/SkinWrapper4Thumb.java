package com.autonavi.auto.skin.impl;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.widget.SeekBar;

import com.autonavi.auto.skin.NightModeGlobal;
import com.autonavi.auto.skin.SkinItems;
import com.autonavi.auto.skin.inter.ISkinWrapper;
import com.autonavi.auto.skin.view.SkinSeekBar;

/**
 * Created by AutoSdk.
 */

public class SkinWrapper4Thumb implements ISkinWrapper<SeekBar> {

    private SkinItems skinItems;
    private Context mContext;

    @Override
    public void init(Context context, SkinItems skinItems) {
        this.mContext = context;
        this.skinItems = skinItems;
    }

    @Override
    public void apply(SeekBar view, boolean isNight) {
        if (skinItems == null || skinItems.getThumb() == null) {
            return;
        }
        int resId = 0;
        if (isNight) {
            resId = skinItems.getThumb().getNightResId();
            int offset = view.getThumbOffset();
            //view.setThumb(getSeekbarThumbNightBackgroundDrawable());
            int offset1 = view.getThumbOffset();
            int a = 10;
            int b = a/10;
            //view.setThumbOffset(offset);
            //view.invalidate();
        } else {
            resId = skinItems.getThumb().getDefaultResId();
            int offset = view.getThumbOffset();
            //view.setThumb(getSeekbarThumbDayBackgroundDrawable());
            int offset1 = view.getThumbOffset();
            int a = 10;
            int b = a/10;
//            view.setThumbOffset(offset);
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
            int offset = view.getThumbOffset();
            int left = 0;
            int right = 0;
            int top = 0;
            int bottom = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){ //getThumb版本号限制需要api 16
                left = view.getThumb().getBounds().left;
                right = view.getThumb().getBounds().right;
                top = view.getThumb().getBounds().top;
                bottom = view.getThumb().getBounds().bottom;
                view.setThumb(draw);
                draw.setBounds(left, top, right, bottom);
                view.setThumbOffset(offset);
            }
            else{
                if(view instanceof SkinSeekBar){
                    Drawable thumbDrawable = ((SkinSeekBar) view).getThumbSkin();
                    if(null != thumbDrawable){
                        Rect rect = thumbDrawable.getBounds();
                        left = rect.left;
                        right = rect.right;
                        top = rect.top;
                        bottom = rect.bottom;
                        view.setThumb(draw);
                        draw.setBounds(left, top, right, bottom);
                        view.setThumbOffset(offset);
                    }
                    //不兼容的情况直接取消设置昼夜
                }
            }
        }
    }

//    protected Drawable getSeekbarThumbDayBackgroundDrawable(){
//        int ThumbWidth = (int)mContext.getResources().getDimension(R.dimen.auto_dimen2_32);
//        int ThumbHeight = (int)mContext.getResources().getDimension(R.dimen.auto_dimen2_32);
//        Resources resources = mContext.getResources();
//
//        StateListDrawable stateListDrawable = new StateListDrawable();
//
//
//        GradientDrawable normalDrawble = new GradientDrawable();
//        normalDrawble.setColor(resources.getColor(R.color.custom_btn_drag_drag_solid_normal_day));
//        normalDrawble.setShape(GradientDrawable.OVAL);
//        normalDrawble.setSize((int)ThumbWidth, (int)ThumbHeight);
//
//
//        GradientDrawable[] pressLayers = new GradientDrawable[2];
//        pressLayers[0] = normalDrawble;
//        pressLayers[1] = new GradientDrawable();
//        pressLayers[1].setColor(resources.getColor(R.color.custom_btn_drag_drag_solid_pressed_day));
//        pressLayers[1].setShape(GradientDrawable.OVAL);
//        pressLayers[1].setSize((int)ThumbWidth, (int)ThumbHeight);
//
//        LayerDrawable pressDrawable = new LayerDrawable(pressLayers);
//
//
//        stateListDrawable.addState(new int[]{-android.R.attr.state_enabled},normalDrawble);
//        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressDrawable);
//        stateListDrawable.addState(new int[]{},normalDrawble);
//
//        return stateListDrawable;
//    }
//    protected Drawable getSeekbarThumbNightBackgroundDrawable(){
//        int ThumbWidth = (int)mContext.getResources().getDimension(R.dimen.auto_dimen2_32);
//        int ThumbHeight = (int)mContext.getResources().getDimension(R.dimen.auto_dimen2_32);
//        Resources resources = mContext.getResources();
//
//        StateListDrawable stateListDrawable = new StateListDrawable();
//
//        GradientDrawable normalDrawble = new GradientDrawable();
//        normalDrawble.setColor(resources.getColor(R.color.custom_btn_drag_drag_solid_normal_night));
//        normalDrawble.setShape(GradientDrawable.OVAL);
//        normalDrawble.setSize((int)ThumbWidth, (int)ThumbHeight);
//
//        GradientDrawable[] pressLayers = new GradientDrawable[2];
//        pressLayers[0] = normalDrawble;
//        pressLayers[1] = new GradientDrawable();
//        pressLayers[1].setColor(resources.getColor(R.color.custom_btn_drag_drag_solid_pressed_night));
//        pressLayers[1].setShape(GradientDrawable.OVAL);
//        pressLayers[1].setSize((int)ThumbWidth, (int)ThumbHeight);
//
//        LayerDrawable pressDrawable = new LayerDrawable(pressLayers);
//
//
//        stateListDrawable.addState(new int[]{-android.R.attr.state_enabled},normalDrawble);
//        stateListDrawable.addState(new int[]{android.R.attr.state_pressed},pressDrawable);
//        stateListDrawable.addState(new int[]{},normalDrawble);
//
//        return stateListDrawable;
//    }
}
