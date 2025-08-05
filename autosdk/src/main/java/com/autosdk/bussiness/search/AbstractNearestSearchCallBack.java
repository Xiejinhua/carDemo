package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.SearchNearestResult;
import com.autonavi.gbl.search.observer.IGSearchNearestObserver;

/**
 * Created by AutoSdk on 2020/10/22.
 **/
public abstract class AbstractNearestSearchCallBack implements IGSearchNearestObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<SearchNearestResult> callbackWrapper);

    @Override
    public void onGetNearestResult(int taskid, int euRet, SearchNearestResult result) {

    }
}
