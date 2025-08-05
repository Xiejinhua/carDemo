package com.autosdk.bussiness.search;

import androidx.annotation.Nullable;

import com.autosdk.bussiness.common.task.TaskManager;

import java.lang.ref.WeakReference;

/**
 * 搜索回调包装类,回调时自动切换到主线程执行
 * M层内部使用
 */
public class SearchCallbackWrapper<T> extends SearchCallback<T> {
    @Nullable
    private SearchCallback<T> oriCallBack;

    public SearchCallbackWrapper(@Nullable SearchCallback<T> oriCallBack) {
        this.oriCallBack = oriCallBack;
    }

    /**
     * 目标页面是否可回调(可更新UI)
     */
    private boolean canCallBack() {
        WeakReference<SearchLifeCycleOwner> wkLifecycleOwner = oriCallBack == null ? null : oriCallBack.wkLifecycleOwner;
        if (wkLifecycleOwner == null) {
            return true;
        }

        SearchLifeCycleOwner lifecycleOwner = wkLifecycleOwner.get();
        return lifecycleOwner != null && lifecycleOwner.isPageActive();
    }

    @Override
    public void onSuccess(final T data) {
        if (oriCallBack == null) {
            return;
        }

        TaskManager.post(new Runnable() {
            @Override
            public void run() {
                if (canCallBack()) {
                    oriCallBack.onSuccess(data);
                }
            }
        });
    }

    @Override
    public void onFailure(final int errCode, final String msg) {
        if (oriCallBack == null) {
            return;
        }
        TaskManager.post(new Runnable() {
            @Override
            public void run() {
                if (canCallBack()) {
                    oriCallBack.onFailure(errCode, msg);
                }
            }
        });
    }


    @Override
    public void onComplete() {
        if (oriCallBack == null) {
            return;
        }
        TaskManager.post(new Runnable() {
            @Override
            public void run() {
                if (canCallBack()) {
                    oriCallBack.onComplete();
                }
            }
        });
    }
}
