package com.autosdk.bussiness.layer.dynamic.bean;

public class FontBean {
    private String font_path;
    private String font_name;

    public FontBean(String font_path, String font_name) {
        this.font_path = font_path;
        this.font_name = font_name;
    }

    public String getFont_path() {
        return font_path;
    }

    public void setFont_path(String font_path) {
        this.font_path = font_path;
    }

    public String getFont_name() {
        return font_name;
    }

    public void setFont_name(String font_name) {
        this.font_name = font_name;
    }
}
