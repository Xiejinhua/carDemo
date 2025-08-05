package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.PoiCmallDetailSearchResult;
import com.autonavi.gbl.search.model.PoiDetailSearchResult;
import com.autonavi.gbl.search.observer.IPoiCmallDetailSearchObserver;
import com.autonavi.gbl.search.observer.IPoiDetailSearchObserver;
import com.autonavi.gbl.search.router.PoiCmallDetailSearchObserverRouter;

/**
 * Created by AutoSdk on 2022/08/11.
 **/
public abstract class AbstractSearchProductInfoDetaillCallBack implements IPoiCmallDetailSearchObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<PoiCmallDetailSearchResult> callbackWrapper);

    @Override
    public void onGetPoiCmallDetailResult(int taskid, int euRet, PoiCmallDetailSearchResult pstResult) {
    }
}
