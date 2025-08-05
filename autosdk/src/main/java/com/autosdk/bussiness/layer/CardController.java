package com.autosdk.bussiness.layer;

import com.autonavi.gbl.map.MapView;
import com.autonavi.gbl.servicemanager.ServiceMgr;
import com.autonavi.gbl.util.model.SingleServiceID;
import com.autosdk.bussiness.common.POI;
import com.autosdk.bussiness.manager.SDKManager;
import com.autosdk.bussiness.map.SurfaceViewID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class CardController {
    private ArrayList<POI> mAlongWayPoiList;

    public void setAlongWayPoiList(ArrayList<POI> mAlongWayPoiList) {
        this.mAlongWayPoiList=mAlongWayPoiList;
    }

    public POI getAlongWayPoi(String id) {
        POI alongWayPoi = null;
        if (mAlongWayPoiList != null && !mAlongWayPoiList.isEmpty()) {
            for (int i = 0; i < mAlongWayPoiList.size(); i++) {
                alongWayPoi = mAlongWayPoiList.get(i);
                if (Objects.equals(mAlongWayPoiList.get(i).getId(), id)) {
                    break;
                }
            }
        }
        return alongWayPoi;
    }

    @SurfaceViewID.SurfaceViewID1 int mSurfaceViewID;
    private CardController(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        mSurfaceViewID = surfaceViewID;
    }
    private static final Map<Integer, CardController> INSTANCE_MAP = new HashMap<>();
    public static synchronized CardController getInstance(@SurfaceViewID.SurfaceViewID1 int surfaceViewID) {
        if(INSTANCE_MAP.containsKey(surfaceViewID)) {
            return INSTANCE_MAP.get(surfaceViewID);
        } else {
            CardController cardController = new CardController(surfaceViewID);
            INSTANCE_MAP.put(surfaceViewID, cardController);
        }

        return INSTANCE_MAP.get(surfaceViewID);
    }

    public static void unInitCardController(){
        synchronized (CardController.class) {
            for(Map.Entry<Integer, CardController> entry : INSTANCE_MAP.entrySet()){
                entry.getValue().unInit();
            }
            INSTANCE_MAP.clear();
        }
    }

    public void init(String layerAssetDir, String fontDir) {
    }

    public void unInit() {
        if (mAlongWayPoiList!=null){
            mAlongWayPoiList.clear();
            mAlongWayPoiList=null;
        }
    }
}
