package com.autosdk.bussiness.map.Observer;

import com.autonavi.gbl.map.observer.IMapEventObserver;

public class MapEventObserver implements IMapEventObserver {
    @Override
    public boolean onMapMoveStart() {
        return false;
    }

    @Override
    public boolean onMapMoveEnd() {
        return false;
    }
}
