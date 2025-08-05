package com.desaysv.psmap.base.auto.layerstyle.bean;


/**
 * 图层设置锚点json配置实体类
 * Created by AutoSdk
 * 注：目前的变量命名是和style.json里的字段一一对应，故先不修改命名规范，后续统一处理
 */
public class MarkerInfoBean {

    private int repeat;
    private int anchor;
    private float x_ratio;
    private float y_ratio;
    private int gen_mipmaps;
    private int pre_mul_alpha;
    private long x_offset;
    private long y_offset;

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public int getAnchor() {
        return anchor;
    }

    public void setAnchor(int anchor) {
        this.anchor = anchor;
    }

    public float getX_ratio() {
        return x_ratio;
    }

    public void setX_ratio(float x_ratio) {
        this.x_ratio = x_ratio;
    }

    public float getY_ratio() {
        return y_ratio;
    }

    public void setY_ratio(float y_ratio) {
        this.y_ratio = y_ratio;
    }

    public int getGen_mipmaps() {
        return gen_mipmaps;
    }

    public void setGen_mipmaps(int gen_mipmaps) {
        this.gen_mipmaps = gen_mipmaps;
    }

    public int getPre_mul_alpha() {
        return pre_mul_alpha;
    }

    public void setPre_mul_alpha(int pre_mul_alpha) {
        this.pre_mul_alpha = pre_mul_alpha;
    }

    public long getX_offset() {
        return x_offset;
    }

    public void setX_offset(long x_offset) {
        this.x_offset = x_offset;
    }

    public long getY_offset() {
        return y_offset;
    }

    public void setY_offset(long y_offset) {
        this.y_offset = y_offset;
    }
}
