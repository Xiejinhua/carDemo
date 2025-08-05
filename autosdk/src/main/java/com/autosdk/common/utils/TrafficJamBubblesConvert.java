package com.autosdk.common.utils;

import com.autonavi.gbl.aosclient.model.GWsNavigationDynamicDataResponseParam;
import com.autonavi.gbl.aosclient.model.WsNavigationDynamicDataJamBubblesDataSegment;
import com.autonavi.gbl.aosclient.model.WsNavigationDynamicDataJamBubblesResponseData;
import com.autonavi.gbl.common.model.Coord2DDouble;
import com.autonavi.gbl.common.path.model.JamBubblesOfSegment;
import com.autonavi.gbl.common.path.model.TrafficJamBubbles;

import java.util.ArrayList;
import java.util.List;

public class TrafficJamBubblesConvert {
    private static final String TAG=TrafficJamBubblesConvert.class.getSimpleName();

    public static List<TrafficJamBubbles> doDynamicTrafficJamBubbles(GWsNavigationDynamicDataResponseParam response) {
        List<TrafficJamBubbles> mTrafficJamBubblesLst = new ArrayList<>();

        List<WsNavigationDynamicDataJamBubblesResponseData> datas = response.data.front_end.traffic_jam_bubbles.data;
        for (WsNavigationDynamicDataJamBubblesResponseData item : datas) {
            TrafficJamBubbles trafficJamBubbles = new TrafficJamBubbles();

            trafficJamBubbles.pathId = item.pathId;
            List<WsNavigationDynamicDataJamBubblesDataSegment> segments = item.segments;
            for (WsNavigationDynamicDataJamBubblesDataSegment segItem : segments) {
                JamBubblesOfSegment jamBubblesOfSegment = new JamBubblesOfSegment();

                jamBubblesOfSegment.deepInfo.text = segItem.deepInfo.text;
                jamBubblesOfSegment.deepInfo.icon = segItem.deepInfo.icon;
                jamBubblesOfSegment.deepInfo.sceneType = segItem.deepInfo.sceneType;

                jamBubblesOfSegment.trend.text = segItem.trend.text;
                jamBubblesOfSegment.trend.icon = segItem.trend.icon;
                jamBubblesOfSegment.trend.sceneType = segItem.trend.sceneType;

                jamBubblesOfSegment.cost.text = segItem.cost.text;
                jamBubblesOfSegment.cost.icon = segItem.cost.icon;
                jamBubblesOfSegment.cost.sceneType = segItem.cost.sceneType;

                jamBubblesOfSegment.degree.text = segItem.degree.text;
                jamBubblesOfSegment.degree.icon = segItem.degree.icon;
                jamBubblesOfSegment.degree.sceneType = segItem.degree.sceneType;

                jamBubblesOfSegment.linkSegment.startRoadId = segItem.linkSegment.startRoadId;
                jamBubblesOfSegment.linkSegment.startDistance = segItem.linkSegment.startDistance;
                jamBubblesOfSegment.linkSegment.endRoadId = segItem.linkSegment.endRoadId;
                jamBubblesOfSegment.linkSegment.endDistance = segItem.linkSegment.endDistance;

                jamBubblesOfSegment.data.congestionId = segItem.data.congestionId;
                jamBubblesOfSegment.data.eventId = segItem.data.eventId;
                jamBubblesOfSegment.data.topLeft = new Coord2DDouble(segItem.data.topLeft.lon, segItem.data.topLeft.lat);
                jamBubblesOfSegment.data.bottomRight = new Coord2DDouble(segItem.data.bottomRight.lon, segItem.data.bottomRight.lat);
                jamBubblesOfSegment.data.roadName = segItem.data.roadName;
                jamBubblesOfSegment.data.trendCode = segItem.data.trendCode;
                jamBubblesOfSegment.data.trendEtaMatch = segItem.data.trendEtaMatch;

                jamBubblesOfSegment.pic = segItem.pic;
                jamBubblesOfSegment.display = new Coord2DDouble(segItem.display.lon, segItem.display.lat);
                jamBubblesOfSegment.jumpType = segItem.jumpType;
                jamBubblesOfSegment.showType = segItem.showType;

                jamBubblesOfSegment.postback.arriveTime = segItem.postback.arriveTime;
                jamBubblesOfSegment.postback.congestionId = segItem.postback.congestionId;
                jamBubblesOfSegment.postback.cordLinkId = segItem.postback.cordLinkId;
                jamBubblesOfSegment.postback.detailType = segItem.postback.detailType;
                jamBubblesOfSegment.postback.eta = segItem.postback.eta;
                jamBubblesOfSegment.postback.length = segItem.postback.length;
                jamBubblesOfSegment.postback.linkIds = segItem.postback.linkIds;
                jamBubblesOfSegment.postback.roadName = segItem.postback.roadName;

                jamBubblesOfSegment.postback.trendDesc = segItem.postback.trendDesc;
                jamBubblesOfSegment.postback.trendEndTime = segItem.postback.trendEndTime;
                jamBubblesOfSegment.postback.trendStartTime = segItem.postback.trendStartTime;
                jamBubblesOfSegment.postback.trendType = segItem.postback.trendType;

                trafficJamBubbles.segments.add(jamBubblesOfSegment);
            }
            mTrafficJamBubblesLst.add(trafficJamBubbles);
        }
        return mTrafficJamBubblesLst;
    }
}
