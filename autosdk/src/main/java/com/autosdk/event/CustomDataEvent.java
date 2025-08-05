package com.autosdk.event;

/**
 * 多屏自定义消息
 *
 * @author AutoSDK
 */
public class CustomDataEvent<T> {
    private @CustomDataEventEnum
    int action;
    private T data;

    public CustomDataEvent() {
    }

    public CustomDataEvent(@CustomDataEventEnum int action, T data) {
        this.action = action;
        this.data = data;
    }

    public @CustomDataEventEnum int getAction() {
        return action;
    }

    public void setAction(@CustomDataEventEnum int action) {
        this.action = action;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
