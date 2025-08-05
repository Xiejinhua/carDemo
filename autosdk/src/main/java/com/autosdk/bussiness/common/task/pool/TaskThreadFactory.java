package com.autosdk.bussiness.common.task.pool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by AutoSdk.
 */
public class TaskThreadFactory implements ThreadFactory {
    private final AtomicInteger mCount = new AtomicInteger(1);
    private String executorName;

    public TaskThreadFactory(String name){
        this.executorName = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, executorName + " Executor#" + mCount.getAndIncrement());
    }
}