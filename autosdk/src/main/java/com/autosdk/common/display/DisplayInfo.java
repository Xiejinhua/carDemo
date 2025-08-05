package com.autosdk.common.display;

import com.autosdk.common.display.ScreenMode;

import java.util.Locale;

/**显示相关尺寸信息
 */

public class DisplayInfo {
    /**
     * 应用的宽高
     */
    public final int appWidth;
    public final int appHeight;

    /**
     * fragment默认宽度
     * 异形屏时：fragmentDefaultWidth < appWidth
     * 其它：fragmentDefaultWidth == appWidth
     */
    public final int fragmentDefaultWidth;
    /**
     * fragment默认高度
     * 默认与appHeight一致
     */
    public final int fragmentDefaultHeight;

    /**
     * 屏幕模式
     */
    public final  @ScreenMode
    int screenMode;

    /**
     * 主图扎点的缩放系数
     * */
    public final float mainMapMarkerRatio;

    /**
     * DPI
     * */
    public final int displayDpi;
    public DisplayInfo(@ScreenMode int screenMode, int appWidth, int appHeight, int fragmentWidth, int
        fragmentHeight, int dpi, float mainMapMarkerRatio){
        this.screenMode = screenMode;
        this.appWidth = appWidth;
        this.appHeight = appHeight;
        this.fragmentDefaultWidth = fragmentWidth;
        this.fragmentDefaultHeight = fragmentHeight;
        this.displayDpi = dpi;
        this.mainMapMarkerRatio = mainMapMarkerRatio;
    }

    @Override
    public String toString() {
        return String.format(Locale.CHINA,"%s[screenMode=%d, appWidth=%d, appHeight=%d, fragmentDefaultWidth=%d, fragmentDefaultHeight=%d]",
            this.getClass().getSimpleName(), screenMode, appWidth, appHeight, fragmentDefaultWidth,
            fragmentDefaultHeight);
    }
}
