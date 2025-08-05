package com.desaysv.psmap.model.layerstyle.parser;

/**
 * 路线itemType
 */
public class GlLineItemType {
    /**
     * <  可以设置颜色的实线
     */
    public static final int TYPE_MARKER_LINE_COLOR = 1;
    /**
     * <  使用纹理绘制的实线
     */
    public static final int TYPE_MARKER_LINE = 2;
    /**
     * <  带有箭头纹理的实线
     */
    public static final int TYPE_MARKER_LINE_ARROW = 3;
    /**
     * <  使用纹理绘制的虚线
     */
    public static final int TYPE_MARKER_LINE_DOT = 4;
    /**
     * <  可以设置颜色的虚线，也可以使用纹理颜色
     */
    public static final int TYPE_MARKER_LINE_DOT_COLOR = 5;
    /**
     * <  使用限行图层实线
     */
    public static final int TYPE_MARKER_LINE_RESTRICT = 6;
    /**
     * <  轮渡线，使用纹理绘制的虚线
     */
    public static final int TYPE_MARKER_LINE_FERRY = 7;

}
