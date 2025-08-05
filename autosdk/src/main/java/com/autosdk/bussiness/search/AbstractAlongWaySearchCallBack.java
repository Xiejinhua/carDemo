package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.SearchAlongWayResult;
import com.autonavi.gbl.search.observer.IGSearchAlongWayObserver;

/**
 * Created by AutoSdk on 2020/10/22.
 **/
public abstract class AbstractAlongWaySearchCallBack implements IGSearchAlongWayObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<SearchAlongWayResult> callbackWrapper);

    @Override
    public void onGetAlongWayResult(int taskid, int euRet, SearchAlongWayResult result) {

    }
}
