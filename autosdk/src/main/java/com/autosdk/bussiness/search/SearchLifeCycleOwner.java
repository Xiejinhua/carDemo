package com.autosdk.bussiness.search;

/**
 * 搜索页面生命周期
 * 目前只用于请求回调更新ui时判断界面是否已被销毁
 */
public interface SearchLifeCycleOwner {


    /**
     * 判断页面是否可更新UI,在 fragment的 [onStart, onDestroyView] 之间可更新
     */
    boolean isPageActive();

}
