package com.autonavi.auto.skin;

/**
 * Created by AutoSdk.
 */
public class SkinItems {
    private ResBean background;
    private ResBean textColorHint;
    private ResBean textColor;
    private ResBean src;
    private ResBean svgColor;
    private ResBean drawableLeft;
    private ResBean drawableRight;
    private ResBean drawableTop;
    private ResBean drawableBottom;
    private ResBean progressDrawable;
    private ResBean thumb;

    public ResBean getBackground() {
        return background;
    }

    public void setBackground(ResBean background) {
        this.background = background;
    }

    public ResBean getTextColor() {
        return textColor;
    }

    public void setTextColor(ResBean textColor) {
        this.textColor = textColor;
    }

    public ResBean getSrc() {
        return src;
    }

    public void setSrc(ResBean src) {
        this.src = src;
    }

    public ResBean getDrawableBottom() {
        return drawableBottom;
    }

    public void setDrawableBottom(ResBean drawableBottom) {
        this.drawableBottom = drawableBottom;
    }

    public ResBean getDrawableLeft() {
        return drawableLeft;
    }

    public void setDrawableLeft(ResBean drawableLeft) {
        this.drawableLeft = drawableLeft;
    }

    public ResBean getDrawableRight() {
        return drawableRight;
    }

    public void setDrawableRight(ResBean drawableRight) {
        this.drawableRight = drawableRight;
    }

    public ResBean getDrawableTop() {
        return drawableTop;
    }

    public void setDrawableTop(ResBean drawableTop) {
        this.drawableTop = drawableTop;
    }

    public ResBean getTextColorHint() {
        return textColorHint;
    }

    public void setTextColorHint(ResBean textColorHint) {
        this.textColorHint = textColorHint;
    }

    public ResBean getProgressDrawable(){
        return progressDrawable;
    }

    public void setProgressDrawable(ResBean progressDrawable){
        this.progressDrawable = progressDrawable;
    }

    public ResBean getThumb(){
        return thumb;
    }

    public void setThumb(ResBean thumb){
        this.thumb = thumb;
    }

    public ResBean getSvgColor() {
        return svgColor;
    }

    public void setSvgColor(ResBean svgColor) {
        this.svgColor = svgColor;
    }

    public boolean isEmpty() {
        return background == null && textColor == null && src == null && drawableLeft == null
            && drawableBottom == null && drawableRight == null && drawableTop == null
            && textColorHint == null && progressDrawable == null && thumb == null && svgColor == null;
    }

}
