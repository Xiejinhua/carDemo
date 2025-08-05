package com.autosdk.bussiness.search;

import com.autonavi.gbl.search.model.SceneSearchResult;
import com.autonavi.gbl.search.observer.ISceneSearchObserver;

/**
 * Created by AutoSdk on 2020/10/22.
 **/
public abstract class AbstractSearchSceneCallBack implements ISceneSearchObserver {
    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<SceneSearchResult> callbackWrapper);

    @Override
    public void onGetSceneResult(int i, int i1, SceneSearchResult sceneSearchResult) {

    }
}
