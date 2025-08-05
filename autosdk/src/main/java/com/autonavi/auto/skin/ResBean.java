package com.autonavi.auto.skin;

/**
 * Created by AutoSdk.
 */
public class ResBean {
    public ResBean() {

    }

    public ResBean(int defaultResId, int nightResId) {
        this.defaultResId = defaultResId;
        this.nightResId = nightResId;
    }
    /**
     * 默认资源类型
     */
    private int defaultResId;
    /**
     * 夜晚的资源id
     */
    private int nightResId;
    /**
     * 资源类型,用来区分是color还是drawable
     */
    private ResType resType;

    public int getDefaultResId() {
        return defaultResId;
    }

    public void setDefaultResId(int defaultResId) {
        this.defaultResId = defaultResId;
    }

    public int getNightResId() {
        return nightResId;
    }

    public void setNightResId(int nightResId) {
        this.nightResId = nightResId;
    }

    public ResType getResType() {
        return resType;
    }

    public void setResType(ResType resType) {
        this.resType = resType;
    }

    public enum ResType {
        UNKOWN,
        COLOR,
        DRAWABLE,
        COLOR_SELECTOR
    }
}
