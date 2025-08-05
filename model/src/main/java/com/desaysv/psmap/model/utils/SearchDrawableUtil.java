package com.desaysv.psmap.model.utils;


import com.desaysv.psmap.model.R;

public class SearchDrawableUtil {

    private static int[] poiNumResArray = {
            R.drawable.global_image_icon_list_01_normal,
            R.drawable.global_image_icon_list_02_normal,
            R.drawable.global_image_icon_list_03_normal,
            R.drawable.global_image_icon_list_04_normal,
            R.drawable.global_image_icon_list_05_normal,
            R.drawable.global_image_icon_list_06_normal,
            R.drawable.global_image_icon_list_07_normal,
            R.drawable.global_image_icon_list_08_normal,
            R.drawable.global_image_icon_list_09_normal,
            R.drawable.global_image_icon_list_10_normal,
            R.drawable.global_image_icon_list_11_normal
    };
    private static int[] poiNumResArraySelected = {
            R.drawable.global_image_icon_list_01_active,
            R.drawable.global_image_icon_list_02_active,
            R.drawable.global_image_icon_list_03_active,
            R.drawable.global_image_icon_list_04_active,
            R.drawable.global_image_icon_list_05_active,
            R.drawable.global_image_icon_list_06_active,
            R.drawable.global_image_icon_list_07_active,
            R.drawable.global_image_icon_list_08_active,
            R.drawable.global_image_icon_list_09_active,
            R.drawable.global_image_icon_list_10_active,
            R.drawable.global_image_icon_list_11_active
    };

    public static int getPoiNumResId(int index, boolean isSelected) {
        if (isSelected) {
            return poiNumResArraySelected[index];
        } else {
            return poiNumResArray[index];
        }
    }
}
