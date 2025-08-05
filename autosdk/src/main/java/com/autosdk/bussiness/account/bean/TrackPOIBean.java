package com.autosdk.bussiness.account.bean;

import java.io.Serializable;

/**
 * Created by AutoSdk on 2021/1/7.
 **/
public class TrackPOIBean implements Serializable {
    private String x;
    private String y;

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
