package com.autosdk.bussiness.layer;

import com.autonavi.gbl.common.path.model.ChargeStationInfo;
import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.map.layer.PointLayerItem;
import com.autonavi.gbl.map.layer.impl.PointLayerItemImpl;

public class ChargeStationLayerItem extends PointLayerItem {

    private ChargeStationInfo mInfo;

    public void setInfo(ChargeStationInfo info){
        this.mInfo = info;
    }

    public ChargeStationInfo getStationInfo(){
        return mInfo;
    }
}
