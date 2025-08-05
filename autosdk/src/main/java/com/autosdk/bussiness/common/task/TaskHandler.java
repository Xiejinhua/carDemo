package com.autosdk.bussiness.common.task;

/**
 * (功能说明)
 * <p/>
 * 创建日期: 2014年05月22日
 */
public interface TaskHandler {

    boolean supportPause();

    boolean supportResume();

    boolean supportCancel();

    void pause();

    void resume();

    boolean isPaused();

    boolean isStopped();

    void cancel();

    boolean isCancelled();
}
