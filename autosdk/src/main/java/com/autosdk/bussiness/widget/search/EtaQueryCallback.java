package com.autosdk.bussiness.widget.search;

import androidx.recyclerview.widget.RecyclerView;

import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryRequestParam;
import com.autonavi.gbl.aosclient.model.GNavigationEtaqueryResponseParam;
import com.autonavi.gbl.aosclient.observer.ICallBackNavigationEtaquery;
import com.autosdk.bussiness.common.POI;

import java.util.List;

/**
 * Created by AutoSdk on 2022/4/26.
 **/
public class EtaQueryCallback implements ICallBackNavigationEtaquery {

    public POI poi;
    public GNavigationEtaqueryRequestParam etaQueryRequestParam;
    public long oldCurTime;
    public RecyclerView.Adapter multiDisplaySearchResultMapAdapter;
    public List<POI> pois;

    public void updateFiled(POI poi,GNavigationEtaqueryRequestParam etaQueryRequestParam, long oldCurTime) {
        this.poi = poi;
        this.etaQueryRequestParam = etaQueryRequestParam;
        this.oldCurTime = oldCurTime;
    }

    public void updateFiled(List<POI> pois, RecyclerView.Adapter adapter, GNavigationEtaqueryRequestParam etaQueryRequestParam, long oldCurTime) {
        this.pois = pois;
        this.multiDisplaySearchResultMapAdapter = adapter;
        this.etaQueryRequestParam = etaQueryRequestParam;
        this.oldCurTime = oldCurTime;
    }

    public void updateFiled(List<POI> pois, GNavigationEtaqueryRequestParam etaQueryRequestParam, long oldCurTime) {
        this.pois = pois;
        this.etaQueryRequestParam = etaQueryRequestParam;
        this.oldCurTime = oldCurTime;
    }

    @Override
    public void onRecvAck(GNavigationEtaqueryResponseParam pResponse) {

    }
}
