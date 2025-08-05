package com.autosdk.event;

public class SearchEvent {
    private int type;
    private Object obj;

    public SearchEvent(int type) {
        this.type = type;
    }

    public SearchEvent(int type, Object obj) {
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
