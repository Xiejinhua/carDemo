package com.autosdk.bussiness.search.bean;

import java.io.Serializable;

/**
 * Created by cbaoqiang on 2022/5/20
 * 油价实体类（搜索结果页展示）
 **/
public class SearchGasBean implements Serializable {
    //汽油型号
    String type;
    //汽油价格
    String price;

    public SearchGasBean(String type, String price) {
        this.type = type;
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
