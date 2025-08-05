package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.SearchAlongWayResult;
import com.autonavi.gbl.search.model.SearchLineDeepInfoResult;
import com.autonavi.gbl.search.observer.IGSearchAlongWayObserver;
import com.autonavi.gbl.search.observer.IGSearchLineDeepInfoObserver;

/**
 * Created by AutoSDK
 **/
public abstract class AbstractLineDeepInfoSearchCallBack implements IGSearchLineDeepInfoObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<SearchLineDeepInfoResult> callbackWrapper);

    @Override
    public void onGetLineDeepInfoResult(int taskid, int euRet, SearchLineDeepInfoResult result) {

    }
}
