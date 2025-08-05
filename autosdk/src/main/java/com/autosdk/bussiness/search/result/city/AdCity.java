package com.autosdk.bussiness.search.result.city;

import java.io.Serializable;

/**
 * @author AutoSDk
 */
public class AdCity implements Serializable {
    private String cityName;
    private int cityAdcode;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityAdcode() {
        return cityAdcode;
    }

    public void setCityAdcode(int cityAdcode) {
        this.cityAdcode = cityAdcode;
    }

}
