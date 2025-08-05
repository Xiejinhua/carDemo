package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.KeywordSearchResultV2;
import com.autonavi.gbl.search.model.SearchKeywordResult;
import com.autonavi.gbl.search.observer.IGSearchKeyWordObserver;
import com.autonavi.gbl.search.observer.IKeyWordSearchObserverV2;

/**
 * Created by AutoSdk on 2020/10/22.
 **/
public abstract class AbstractSearchKeyWordCallBackV2 implements IKeyWordSearchObserverV2 {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<KeywordSearchResultV2> callbackWrapper);

    @Override
    public void onGetKeyWordResult(int i, int i1, KeywordSearchResultV2 searchKeywordResult) {

    }
}
