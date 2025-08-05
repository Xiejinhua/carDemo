package com.autonavi.auto.skin;

/**
 * Created by AutoSdk.
 */

public class NightModeGlobal {


    /**
     * 是否支持昼夜
     */
    public static final boolean IS_SUPPORT_DAY_NIGHT = true;

    private static boolean isNight;

    /**
     * 换肤资源后缀根据实际场景 , 这里以pink为例 ， 完整资源名称
     * 例: 默认白天资源R.drawable.navi_day
     *     默认黑夜资源R.drawable.navi_night
     * 则: 皮肤后缀为pink的白天资源R.drawable.navi_day_pink
     *     皮肤后缀为pink的黑夜资源R.drawable.navi_night_pink
     * 另: 资源文件不存在时 , 使用默认资源
     * @param suffix
     */
    private static String suffix = ""; // 资源后缀 , 默认为空

    public static boolean isNightMode() {
        return isNight;
    }

    public static void setNightMode(boolean b) {
        isNight = b;
    }

    public static String getSuffix() {
        return suffix;
    }

    /**
     * 设置皮肤后缀名,设置完成后将全局使用带有该后缀名的资源
     * 设置时机在View绘制之前及SkinManager.updateView(View view)触发之前
     * View绘制之后需要调用SkinManager.updateView(View iew)进行刷新
     * @param suffix
     */
    public static void setSuffix(String suffix) {
        NightModeGlobal.suffix = suffix;
    }


}
