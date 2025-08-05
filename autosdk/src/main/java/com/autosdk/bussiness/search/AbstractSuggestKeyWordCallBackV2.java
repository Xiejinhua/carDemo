package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.SuggestionSearchResult;
import com.autonavi.gbl.search.observer.ISuggestionSearchObserver;

public abstract class AbstractSuggestKeyWordCallBackV2 implements ISuggestionSearchObserver {
    public abstract void setSuggestCallbackWrapper(SearchCallbackWrapper<SuggestionSearchResult> callbackWrapper);

    @Override
    public void onGetSuggestionResult(int taskid, int errorCode, SuggestionSearchResult pstResult) {

    }
}
