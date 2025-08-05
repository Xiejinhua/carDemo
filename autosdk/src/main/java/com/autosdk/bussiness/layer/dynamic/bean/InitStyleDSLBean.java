package com.autosdk.bussiness.layer.dynamic.bean;

import com.autosdk.bussiness.layer.DynamicLayer;

import java.util.List;

public class InitStyleDSLBean {
    private boolean debug_card;
    private boolean update_card;
    private boolean local_style;
    private String asset_path;
    private String cmb_name;
    private List<FontBean> font_list;

    public InitStyleDSLBean() {
        this.debug_card = false;
        this.update_card = true;
        this.local_style = true;
    }

    public boolean isDebug_card() {
        return debug_card;
    }

    public void setDebug_card(boolean debug_card) {
        this.debug_card = debug_card;
    }

    public boolean isUpdate_card() {
        return update_card;
    }

    public void setUpdate_card(boolean update_card) {
        this.update_card = update_card;
    }

    public boolean isLocal_style() {
        return local_style;
    }

    public void setLocal_style(boolean local_style) {
        this.local_style = local_style;
    }

    public String getAsset_path() {
        return asset_path;
    }

    public void setAsset_path(String asset_path) {
        this.asset_path = asset_path;
    }

    public String getCmb_name() {
        return cmb_name;
    }

    public void setCmb_name(String cmb_name) {
        this.cmb_name = cmb_name;
    }

    public List<FontBean> getFont_list() {
        return font_list;
    }

    public void setFont_list(List<FontBean> font_list) {
        this.font_list = font_list;
    }
}
