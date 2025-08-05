package com.autosdk.event;

public class PushEvent {
    private int type;
    private Object obj;

    public PushEvent(int type) {
        this.type = type;
    }

    public PushEvent(int type, Object obj) {
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
