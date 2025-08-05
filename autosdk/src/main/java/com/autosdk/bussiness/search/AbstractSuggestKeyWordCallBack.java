package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.SearchSuggestResult;
import com.autonavi.gbl.search.observer.IGSearchSuggestionObserver;

/**
 * Created by AutoSdk on 2020/10/22.
 **/
public abstract class AbstractSuggestKeyWordCallBack implements IGSearchSuggestionObserver {
    public abstract void setSuggestCallbackWrapper(SearchCallbackWrapper<SearchSuggestResult> callbackWrapper);

    @Override
    public void onGetSuggestionResult(int i, int i1, SearchSuggestResult searchSuggestResult) {

    }
}
