package com.autosdk.bussiness.search;


import java.lang.ref.WeakReference;

/**
 * P/V层使用的请求回调类
 * M层内部请使用 {@link SearchCallbackWrapper} (自动进行主线程切换)
 * 注意:若presenter发起请求但未返回前, view销毁, 可能导致空指针异常
 * -    此时请使用构造方法: {@link #SearchCallback(SearchLifeCycleOwner)}
 * -    并且在 SearchController 中使用 SearchCallbackWrapper 以便进行自动判断
 */
public class SearchCallback<T> implements IMvpCallback<T> {
    WeakReference<SearchLifeCycleOwner> wkLifecycleOwner;

    public SearchCallback() {
        //在请求回调时, 不做生命周期判断,直接回调
    }

    public SearchCallback(SearchLifeCycleOwner lifecycleOwner) {
        if (lifecycleOwner == null) {
            //此时在请求回调时, 不做生命周期判断,直接回调
            return;
        }

        // 只有此种情形会在请求回调时,判断目标宿主页面的生命周期状态,并在ui可更新时才回调
        this.wkLifecycleOwner = new WeakReference<>(lifecycleOwner);
    }


    @Override
    public void onSuccess(T data) {

    }

    @Override
    public void onFailure(int errCode, String msg) {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError() {

    }
}
