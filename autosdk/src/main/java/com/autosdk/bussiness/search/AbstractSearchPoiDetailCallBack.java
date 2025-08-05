package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.PoiDetailSearchResult;
import com.autonavi.gbl.search.model.SceneSearchResult;
import com.autonavi.gbl.search.observer.IPoiDetailSearchObserver;
import com.autonavi.gbl.search.observer.ISceneSearchObserver;

/**
 * Created by AutoSdk on 2020/10/22.
 **/
public abstract class AbstractSearchPoiDetailCallBack implements IPoiDetailSearchObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<PoiDetailSearchResult> callbackWrapper);

    @Override
    public void onGetPoiDetailResult(int taskid, int euRet, PoiDetailSearchResult pstResult) {
    }
}
