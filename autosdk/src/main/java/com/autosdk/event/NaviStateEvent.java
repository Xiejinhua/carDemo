package com.autosdk.event;

/**
 * 导航状态事件透出，如开始导航、结束导航
 */
public class NaviStateEvent {

    private @DriveNaviEvent.DriveNaviEventVal int driveNaviEvent;

    private Object object;

    public NaviStateEvent(@DriveNaviEvent.DriveNaviEventVal int driveNaviEvent, Object object) {
        this.driveNaviEvent = driveNaviEvent;
        this.object = object;
    }

    public @DriveNaviEvent.DriveNaviEventVal int getDriveNaviEvent() {
        return driveNaviEvent;
    }

    public void setDriveNaviEvent(@DriveNaviEvent.DriveNaviEventVal int driveNaviEvent) {
        this.driveNaviEvent = driveNaviEvent;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
