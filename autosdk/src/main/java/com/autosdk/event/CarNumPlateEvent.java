package com.autosdk.event;

/**
 * 车牌变更事件
 */
public class CarNumPlateEvent {
    private int type;
    private Object obj;

    public CarNumPlateEvent(int type) {
        this.type = type;
    }

    public CarNumPlateEvent(int type, Object obj) {
        this.type = type;
        this.obj = obj;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
