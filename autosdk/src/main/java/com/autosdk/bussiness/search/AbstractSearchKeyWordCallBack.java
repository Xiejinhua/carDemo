package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.SearchKeywordResult;
import com.autonavi.gbl.search.observer.IGSearchKeyWordObserver;

/**
 * Created by AutoSdk on 2020/10/22.
 **/
public abstract class AbstractSearchKeyWordCallBack implements IGSearchKeyWordObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<SearchKeywordResult> callbackWrapper);

    @Override
    public void onGetKeyWordResult(int i, int i1, SearchKeywordResult searchKeywordResult) {

    }
}
