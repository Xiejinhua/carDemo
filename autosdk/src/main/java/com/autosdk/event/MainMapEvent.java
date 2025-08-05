package com.autosdk.event;

import com.autosdk.bussiness.common.POI;

public class MainMapEvent {
    private int type;
    private POI poi;
    private String text;
    private int id;

    public MainMapEvent(int type) {
        this.type = type;
    }

    public MainMapEvent(int type, POI poi) {
        this.type = type;
        this.poi = poi;
    }
    public MainMapEvent(int type, int id) {
        this.type = type;
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public POI getPoi() {
        return poi;
    }

    public void setPoi(POI poi) {
        this.poi = poi;
    }
}
