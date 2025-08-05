package com.autosdk.bussiness.navi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.autonavi.gbl.common.path.model.ElecVehicleETAInfo;
import com.autonavi.gbl.common.path.model.RouteLimitInfo;
import com.autonavi.gbl.common.path.model.TollGateInfo;
import com.autonavi.gbl.common.path.option.PathInfo;
import com.autonavi.gbl.common.path.option.RouteOption;
import com.autonavi.gbl.guide.model.CrossImageInfo;
import com.autonavi.gbl.guide.model.CruiseCongestionInfo;
import com.autonavi.gbl.guide.model.CruiseEventInfo;
import com.autonavi.gbl.guide.model.CruiseFacilityInfo;
import com.autonavi.gbl.guide.model.CruiseInfo;
import com.autonavi.gbl.guide.model.CruiseTimeAndDist;
import com.autonavi.gbl.guide.model.DriveEventTip;
import com.autonavi.gbl.guide.model.DriveReport;
import com.autonavi.gbl.guide.model.ExitDirectionInfo;
import com.autonavi.gbl.guide.model.ExitDirectionResponseData;
import com.autonavi.gbl.guide.model.LaneInfo;
import com.autonavi.gbl.guide.model.LightBarInfo;
import com.autonavi.gbl.guide.model.LightBarDetail;
import com.autonavi.gbl.guide.model.ManeuverIconResponseData;
import com.autonavi.gbl.guide.model.ManeuverInfo;
import com.autonavi.gbl.guide.model.MixForkInfo;
import com.autonavi.gbl.guide.model.NaviCameraExt;
import com.autonavi.gbl.guide.model.NaviCongestionInfo;
import com.autonavi.gbl.guide.model.NaviFacility;
import com.autonavi.gbl.guide.model.NaviGreenWaveCarSpeed;
import com.autonavi.gbl.guide.model.NaviInfo;
import com.autonavi.gbl.guide.model.NaviIntervalCameraDynamicInfo;
import com.autonavi.gbl.guide.model.NaviRoadFacility;
import com.autonavi.gbl.guide.model.NaviWeatherInfo;
import com.autonavi.gbl.guide.model.PathTrafficEventInfo;
import com.autonavi.gbl.guide.model.SAPAInquireResponseData;
import com.autonavi.gbl.guide.model.SocolEventInfo;
import com.autonavi.gbl.guide.model.SoundInfo;
import com.autonavi.gbl.guide.model.SuggestChangePathReason;
import com.autonavi.gbl.guide.model.TrafficLightCountdown;
import com.autonavi.gbl.guide.model.TrafficSignal;
import com.autonavi.gbl.guide.observer.INaviObserver;
import com.autonavi.gbl.route.model.BLRerouteRequestInfo;
import com.autonavi.gbl.route.model.PathResultData;
import com.autonavi.gbl.route.model.RouteAlongServiceAreaInfo;
import com.autonavi.gbl.route.model.WeatherLabelItem;
import com.autonavi.gbl.route.observer.IRouteServiceAreaObserver;
import com.autonavi.gbl.util.model.BinaryStream;
import com.autosdk.bussiness.navi.constant.NaviConstant;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 该类负责将来自Tbt线程的消息转换到ui线程中，并通知相关的listener.
 * <p>
 * 这个类紧密依赖于TbtManager，用于下发Tbt的信息，并针对不同的信息进行一些逻辑处理<br/>
 * </p>
 */
public class NaviMessenger {

    public static final String TAG = "[drive]NaviMessenger";

    private static class MessengerUtilsHolder {
        private static NaviMessenger mInstance = new NaviMessenger();
    }

    public static NaviMessenger getInstance() {
        return NaviMessenger.MessengerUtilsHolder.mInstance;
    }

    public void sendMessage(Message msg) {
        mMessenger.sendMessage(msg);
    }

    public void sendMessage(int what) {
        mMessenger.sendMessage(newMessage(what));
    }

    public void sendMessage(int what, Object obj) {
        Message msg = Message.obtain();
        msg.obj = obj;
        msg.what = what;
        mMessenger.sendMessage(msg);
    }

    public Message newMessage(int what) {
        Message msg = Message.obtain();
        msg.what = what;
        return msg;
    }

    public void clearAllMessages() {
        mMessenger.removeCallbacksAndMessages(null);
    }

    /**
     * 分发Tbt消息的Hanlder,将Tbt线程传递过来的消息，进行逻辑处理，并传递到Ui线程中，并通知相关的观察者
     */
    private final Handler mMessenger = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message msg) {
            int what = msg.what;
            NaviController naviController = NaviController.getInstance();
            List<INaviObserver> naviObserverSet = naviController.getNaviObserver();
            switch (what) {
                case NaviConstant.HANDLER_ON_NEW_ROUTE:  //算路成功
                    Timber.i("NaviConstant.HANDLER_ON_NEW_ROUTE");
                    if (naviController.getRouteObservers() != null) {
                        final Object[] objs = msg.obj != null ? (Object[]) msg.obj : null;
                        if (objs == null || objs.length != 3) {
                            return;
                        }
                        naviController.getRouteObservers().onNewRoute((PathResultData) objs[0], (ArrayList<PathInfo>) objs[1], (RouteLimitInfo) objs[2]);
                    }
                    break;
                case NaviConstant.HANDLER_ON_ERROR_ROUTE: //算路失败
                    Timber.i("NaviConstant.HANDLER_ON_ERROR_ROUTE");
                    if (naviController.getRouteObservers() != null) {
                        final Object[] objs = msg.obj != null ? (Object[]) msg.obj : null;
                        if (objs == null) {
                            return;
                        }
                        naviController.getRouteObservers().onNewRouteError((PathResultData) objs[0], (RouteLimitInfo) objs[1]);
                    }
                    break;
                case NaviConstant.HANDLER_ON_SHOWCAMERA: //电子狗
                    Timber.i("NaviConstant.HANDLER_ON_SHOWCAMERA");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowNaviCameraExt((ArrayList<NaviCameraExt>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_SHOWINTERVALCAMERA://区间测速电子眼
                    Timber.i("NaviConstant.HANDLER_ON_SHOWINTERVALCAMERA");
                    /*if (naviObserver != null) {
                        naviObserver.onShowNaviIntervalCamera((ArrayList<NaviIntervalCamera>) msg.obj);
                    }*/
                    break;
                case NaviConstant.HANDLER_ON_INTERVALCAMERADYNAMICINFO://区间测速电子眼动态信息
                    Timber.i("NaviConstant.HANDLER_ON_INTERVALCAMERADYNAMICINFO");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateIntervalCameraDynamicInfo((ArrayList<NaviIntervalCameraDynamicInfo>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_SHOW_NAVI_CROSS_TMC: // 【4.1.8】精品三维大图路线支持路况显示
                    Timber.i("NaviConstant.HANDLER_ON_SHOW_NAVI_CROSS_TMC");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowNaviCrossTMC((BinaryStream) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATE_CHARGE_STATION_PASS: // 更新经过充电站索引
                    Timber.i("NaviConstant.HANDLER_ON_UPDATE_CHARGE_STATION_PASS");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateChargeStationPass((Long) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATENAVIINFO: //导航过程中的信息
                    Timber.i("NaviConstant.HANDLER_ON_UPDATENAVIINFO_导航过程中的信息");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateNaviInfo((ArrayList<NaviInfo>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATEEXITINFO: //更新出口信息
                    Timber.i("NaviConstant.HANDLER_ON_UPDATEEXITINFO");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateExitDirectionInfo((ExitDirectionInfo) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_SHOWCROSSPIC: //显示路口大图
                    Timber.i("NaviConstant.HANDLER_ON_SHOWCROSSPIC_显示路口大图");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowCrossImage((CrossImageInfo) msg.obj);
                        }
                    }
                    break;

                case NaviConstant.HANDLER_ON_UPDATESOCOL: //显示socol文本
                    Timber.i("NaviConstant.HANDLER_ON_UPDATESOCOL");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateSocolText((String) msg.obj);
                        }
                    }
                    break;
//                case NaviConstant.HANDLER_ON_OBTAININFO: //显示socol文本
//                    if (naviObserver != null) {
//                        naviObserver.onObtainAsyncInfo((ObtainInfo) msg.obj);
//                    }
//                    break;
                case NaviConstant.HANDLER_ON_SPEED: //显示基础限速
                    Timber.i("NaviConstant.HANDLER_ON_SPEED");
                    if (naviObserverSet != null) {
                        Bundle bundle = msg.getData();
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onCurrentRoadSpeed(bundle.getInt("speed"));
                        }
                    }
                    break;

                case NaviConstant.HANDLER_ON_UPDATEEVENT: //导航中的交通事件
                    Timber.i("NaviConstant.HANDLER_ON_UPDATEEVENT");
                    if (naviObserverSet != null) {
                        Bundle bundle = msg.getData();
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateTREvent((ArrayList<PathTrafficEventInfo>) msg.obj, bundle.getLong("act"));
                        }
                    }
                    break;

                case NaviConstant.HANDLER_ON_CRUISE_FAC: //巡航中切换道路设施
                    Timber.i("NaviConstant.HANDLER_ON_CRUISE_FAC");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onUpdateCruiseFacility((ArrayList<CruiseFacilityInfo>) msg.obj);
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATE_ELEC_CAMERA_INFO: //巡航中电子眼信息
                    Timber.i("NaviConstant.HANDLER_ON_UPDATE_ELEC_CAMERA_INFO");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onUpdateElecCameraInfo((ArrayList<CruiseFacilityInfo>) msg.obj);
                    }
                    break;

                case NaviConstant.HANDLER_ON_HIDE_CROSS: //隐藏放大路口
                    Timber.i("NaviConstant.HANDLER_ON_HIDE_CROSS");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onHideCrossImage((Integer) msg.obj);
                        }
                    }
                    break;

                case NaviConstant.HANDLER_ON_SHOW_MANEUVER://显示放大路口
                    Timber.i("NaviConstant.HANDLER_ON_SHOW_MANEUVER");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowNaviManeuver((ManeuverInfo) msg.obj);
                        }
                    }
                    break;

                case NaviConstant.HANDLER_ON_CRUISE_CONGESTION: //巡航拥堵信息
                    Timber.i("NaviConstant.HANDLER_ON_CRUISE_CONGESTION");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onUpdateCruiseCongestionInfo((CruiseCongestionInfo) msg.obj);
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATE_BAR: //更新导航进度信息
                    Timber.i("NaviConstant.HANDLER_ON_UPDATE_BAR");
                    if (naviObserverSet != null) {
                        Bundle bundle = msg.getData();
                        final Object[] objs = msg.obj != null ? (Object[]) msg.obj : null;
                        if (objs == null) {
                            return;
                        }
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateTMCLightBar((ArrayList<LightBarInfo>) objs[0], (LightBarDetail) objs[1], bundle.getLong("passedIdx"), bundle.getBoolean("dataStatus"));
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_REROTE:
                    Timber.i("NaviConstant.HANDLER_ON_REROTE");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onReroute((RouteOption) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_CONGESTION:
                    Timber.i("NaviConstant.HANDLER_ON_CONGESTION");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateTMCCongestionInfo((NaviCongestionInfo) msg.obj);
                        }
                    }
                    break;

                case NaviConstant.HANDLER_ON_SHOWMIXINFO: //分歧路口
                    Timber.i("NaviConstant.HANDLER_ON_SHOWMIXINFO");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowSameDirectionMixForkInfo((ArrayList<MixForkInfo>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_NAVIWEATHER: //导航过程更新天气
                    Timber.i("NaviConstant.HANDLER_ON_NAVIWEATHER");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowNaviWeather((ArrayList<NaviWeatherInfo>) msg.obj);
                        }
                    }
                    break;

                case NaviConstant.HANDLER_ON_NAVIFACILITY: //导航中的道路设施
                    Timber.i("NaviConstant.HANDLER_ON_NAVIFACILITY");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowNaviFacility((ArrayList<NaviRoadFacility>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATE_CRUISE_INFO: // 传出巡航信息
                    Timber.i("NaviConstant.HANDLER_ON_UPDATE_CRUISE_INFO");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onUpdateCruiseInfo((CruiseInfo) msg.obj);
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATE_CRUISE_TIME_AND_DIST: // 连续巡航时间
                    Timber.i("NaviConstant.HANDLER_ON_UPDATE_CRUISE_TIME_AND_DIST");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onUpdateCruiseTimeAndDist((CruiseTimeAndDist) msg.obj);
                    }
                    break;
                case NaviConstant.HANDLER_ON_SHOW_CRUISE_LANE_INFO: // 显示巡航车道线
                    Timber.i("NaviConstant.HANDLER_ON_SHOW_CRUISE_LANE_INFO");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onShowCruiseLaneInfo((LaneInfo) msg.obj);
                    }
                    break;
                case NaviConstant.HANDLER_ON_HIDE_CRUISE_LANE_INFO: //隐藏巡航车道线
                    Timber.i("NaviConstant.HANDLER_ON_HIDE_CRUISE_LANE_INFO");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onHideCruiseLaneInfo();
                    }
                    break;
                case NaviConstant.HANDLER_ON_CRUISEEVENT: //传出巡航状态下的交通事件信息
                    Timber.i("NaviConstant.HANDLER_ON_CRUISEEVENT");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onUpdateCruiseEvent((CruiseEventInfo) msg.obj);
                    }
                    break;

                case NaviConstant.HANDLER_ON_CRUISESOCOL: //传出巡航状态下的交通事件信息
                    Timber.i("NaviConstant.HANDLER_ON_CRUISESOCOL");
                    if (naviController.getCruiseObserver() != null) {
                        naviController.getCruiseObserver().onUpdateCruiseSocolEvent((SocolEventInfo) msg.obj);
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATESAPA: //更新服务区信息
                    Timber.i("NaviConstant.HANDLER_ON_UPDATESAPA");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateSAPA((ArrayList<NaviFacility>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_STOPNAVI: //停止导航
                    Timber.i("NaviConstant.HANDLER_ON_STOPNAVI");
                    final int type = msg.getData().getInt("type");
                    final long id = msg.getData().getLong("id");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onNaviStop(id, type);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ARRIVE: //导航到达终点
                    Timber.i("NaviConstant.HANDLER_ARRIVE");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onNaviArrive(msg.getData().getLong("id"), msg.getData().getInt("type"));
                        }
                    }
                    break;

                case NaviConstant.HANDLER_ON_NAVISOCOL: //传出巡航状态下的交通事件信息
                    Timber.i("NaviConstant.HANDLER_ON_NAVISOCOL");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateNaviSocolEvent((SocolEventInfo) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_VEHICLEETAINFO: //透出电动车ETA信息。
                    Timber.i("NaviConstant.HANDLER_ON_VEHICLEETAINFO");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateElecVehicleETAInfo((ArrayList<ElecVehicleETAInfo>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_TRAFFICSIGNALINFO: //传出红路灯交通信号信息
                    Timber.i("NaviConstant.HANDLER_ON_TRAFFICSIGNALINFO");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateTrafficSignalInfo((ArrayList<TrafficSignal>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_TRAFFIC_LIGHT_COUNTDOWN: //传出红路灯倒计时
                    Timber.i("NaviConstant.HANDLER_ON_TRAFFIC_LIGHT_COUNTDOWN");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateTrafficLightCountdown((ArrayList<TrafficLightCountdown>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_LANEINFO: //车道线
                    Timber.i("NaviConstant.HANDLER_ON_LANEINFO");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowNaviLaneInfo((LaneInfo) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_HIDELANE: //隐藏导航车道线
                    Timber.i("NaviConstant.HANDLER_ON_HIDELANE");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onHideNaviLaneInfo();
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_GATELANE: // 透出一定距离范围内的收费站车道信息
                    Timber.i("NaviConstant.HANDLER_ON_GATELANE");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowTollGateLane((TollGateInfo) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_SELECT_MAIN_PATH_STATUS_INFO: //通知用户切换主导航路线状态
                    Timber.i("NaviConstant.HANDLER_ON_SELECT_MAIN_PATH_STATUS_INFO");
                    if (naviObserverSet != null) {
                        Bundle bundle = msg.getData();
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onSelectMainPathStatus(bundle.getLong("pathID"), bundle.getInt("result"));
                        }
                    }
                    break;
                case NaviConstant.HANDLER_TTS_PLAYING:
                    Timber.i("NaviConstant.HANDLER_TTS_PLAYING");
                    if (NaviController.getInstance().getTbtSoundPlayOberver() != null) {
                        SoundInfo info = (SoundInfo) msg.obj;
                        NaviController.getInstance().getTbtSoundPlayOberver().onPlayTTS(info);
                    }
                    break;
                case NaviConstant.HANDLER_RING_PLAYING:
                    Timber.i("NaviConstant.HANDLER_RING_PLAYING");
                    if (NaviController.getInstance().getTbtSoundPlayOberver() != null) {
                        final int ringType = msg.getData().getInt("type");
                        NaviController.getInstance().getTbtSoundPlayOberver().onPlayRing(ringType);
                    }
                    break;
                case NaviConstant.HANDLER_ON_UPDATE_VIA_PASS:
                    Timber.i("NaviConstant.HANDLER_ON_UPDATE_VIA_PASS");
                    if (naviObserverSet != null) {
                        final long viaIndex = msg.getData().getLong("viaIndex");
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateViaPass(viaIndex);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_DELETE_PATH:
                    Timber.i("NaviConstant.HANDLER_ON_DELETE_PATH");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onDeletePath((ArrayList<Long>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_CHANGE_NAVIPATH:
                    Timber.i("NaviConstant.HANDLER_ON_CHANGE_NAVIPATH");
                    if (naviObserverSet != null) {
                        Bundle bundle = msg.getData();
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onChangeNaviPath(bundle.getLong("oldPathID"), bundle.getLong("pathID"));
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_SUGGEST_CHANGE_PATH:
                    Timber.i("NaviConstant.HANDLER_ON_SUGGEST_CHANGE_PATH");
                    if (naviObserverSet != null) {
                        Bundle bundle = msg.getData();
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onSuggestChangePath(bundle.getLong("newPathID"), bundle.getLong("oldPathID"), (SuggestChangePathReason) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_WEATHER:
                    Timber.i("NaviConstant.HANDLER_WEATHER");
                    if (NaviController.getInstance().getRouteWeatherObserver() != null) {
                        Bundle bundle = msg.getData();
                        NaviController.getInstance().getRouteWeatherObserver().onWeatherUpdated(bundle.getLong("requestId"), (ArrayList<WeatherLabelItem>) msg.obj);
                    }
                    break;
                case NaviConstant.HANDLER_ON_MANEUVERICON://在线转向图标
                    Timber.i("NaviConstant.HANDLER_ON_MANEUVERICON");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onObtainManeuverIconData((ManeuverIconResponseData) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_EXIT_DIRECTION://出口编号和路牌方向信息
                    Timber.i("NaviConstant.HANDLER_ON_EXIT_DIRECTION");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onObtainExitDirectionInfo((ExitDirectionResponseData) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_SAPA_INQUIRE://服务区收费站信息
                    Timber.i("NaviConstant.HANDLER_ON_SAPA_INQUIRE");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onObtainSAPAInfo((SAPAInquireResponseData) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.ON_MODIFY_REROUTE_OPTION://更新重算设置，在发起算路之前
                    if (naviController.getNaviRerouteObserver() != null) {
                        naviController.getNaviRerouteObserver().onModifyRerouteOption((RouteOption) msg.obj);
                    }
                    break;
                case NaviConstant.ON_REROUTE_INFO:
                    Timber.i("handleMessage_ON_REROUTE_INFO");
                    if (naviController.getNaviRerouteObserver() != null) {
                        naviController.getNaviRerouteObserver().onRerouteInfo((BLRerouteRequestInfo) msg.obj);
                    }
                    break;
                case NaviConstant.ON_SWITCHPARALLELROADREROUTE_INFO:
                    Timber.i("NaviConstant.HANDLER_ON_SWITCHPARALLELROADREROUTE_INFO");
                    if (naviController.getNaviRerouteObserver() != null) {
                        naviController.getNaviRerouteObserver().onSwitchParallelRoadRerouteInfo((BLRerouteRequestInfo) msg.obj);
                    }
                    break;
                case NaviConstant.HANDLER_GREEN_WAVE:
                    Timber.i("NaviConstant.HANDLER_GREEN_WAVE");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onUpdateGreenWaveCarSpeed((ArrayList<NaviGreenWaveCarSpeed>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_NAVI_REPORT:
                    Timber.i("NaviConstant.HANDLER_ON_NAVI_REPORT");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onDriveReport((DriveReport) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ON_NAVI_DRIVEEVENT:
                    Timber.i("NaviConstant.HANDLER_ON_NAVI_DRIVEEVENT");
                    if (naviObserverSet != null) {
                        for (INaviObserver naviObserver : naviObserverSet) {
                            naviObserver.onShowDriveEventTip((ArrayList<DriveEventTip>) msg.obj);
                        }
                    }
                    break;
                case NaviConstant.HANDLER_ALONG_SERVICE_AREA:
                    List<IRouteServiceAreaObserver> routeServiceAreaObserverSet = naviController.getAlongServiceAreaObserver();
                    if (routeServiceAreaObserverSet != null) {
                        for (IRouteServiceAreaObserver serviceAreaObserver : routeServiceAreaObserverSet) {
                            serviceAreaObserver.onUpdateAlongServiceArea((ArrayList<RouteAlongServiceAreaInfo>) msg.obj);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
