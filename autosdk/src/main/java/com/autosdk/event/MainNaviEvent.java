package com.autosdk.event;

public class MainNaviEvent {
    private int type;

    public MainNaviEvent() {
    }

    public MainNaviEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
