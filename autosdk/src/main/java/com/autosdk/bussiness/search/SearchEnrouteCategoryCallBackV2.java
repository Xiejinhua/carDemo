package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.KeywordSearchResultV2;
import com.autonavi.gbl.search.model.SearchEnrouteCategoryResult;
import com.autonavi.gbl.search.observer.IKeyWordSearchObserverV2;
import com.autonavi.gbl.search.observer.ISearchEnrouteCategoryObserver;

/**
 * Created by AutoSdk on 2020/10/22.
 **/
public abstract class SearchEnrouteCategoryCallBackV2 implements ISearchEnrouteCategoryObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<SearchEnrouteCategoryResult> callbackWrapper);
    @Override
    public void onResult(SearchEnrouteCategoryResult var1){

    };
}
