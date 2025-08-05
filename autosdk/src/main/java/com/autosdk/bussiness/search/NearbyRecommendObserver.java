package com.autosdk.bussiness.search;


import com.autonavi.gbl.information.nearby.model.NearbyRecommendResult;
import com.autonavi.gbl.information.nearby.observer.INearbyRecommendObserver;

public abstract class NearbyRecommendObserver implements INearbyRecommendObserver {

    public abstract void setSearchCallbackWrapper(SearchCallbackWrapper<NearbyRecommendResult> callbackWrapper);

    @Override
    public void onResult(int taskId, NearbyRecommendResult result) {

    }
}
