package com.autosdk.bussiness.search.result.city;

import java.util.ArrayList;

/**
 * 城市分类列表，当前仅用到一层，即附近城市
 */
public class CityCategory {
    private String categoryName;
    private ArrayList<AdCity> cityList = new ArrayList<>();

    public ArrayList<AdCity> getCityList() {
        return cityList;
    }

    public void setCityList(ArrayList<AdCity> cityList) {
        this.cityList = cityList;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}
