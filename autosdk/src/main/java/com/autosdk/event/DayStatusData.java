package com.autosdk.event;

public class DayStatusData {
    private boolean isNight;
    private int mode;

    public boolean isNight() {
        return isNight;
    }

    public void setNight(boolean night) {
        isNight = night;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }
}
