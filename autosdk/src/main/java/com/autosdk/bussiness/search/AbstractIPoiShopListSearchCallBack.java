package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.PoiCmallDetailSearchResult;
import com.autonavi.gbl.search.model.PoiShopListSearchResult;
import com.autonavi.gbl.search.observer.IPoiCmallDetailSearchObserver;
import com.autonavi.gbl.search.observer.IPoiShopListSearchObserver;

/**
 * Created by AutoSdk on 2022/08/11.
 **/
public abstract class AbstractIPoiShopListSearchCallBack implements IPoiShopListSearchObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<PoiShopListSearchResult> callbackWrapper);

    @Override
    public void onGetPoiShopListResult(int taskid, int euRet, PoiShopListSearchResult pstResult) {
    }
}
