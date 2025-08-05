package com.desaysv.psmap.base.auto.layerstyle.bean;

/**
 * 矢量路口大图json配置实体类
 * Created by AutoSdk
 * 注：目前的变量命名是和style.json里的字段一一对应，故先不修改命名规范，后续统一处理
 */
public class CarTypeBean {

    private CcarLayerStyle car_layer_style;

    public CcarLayerStyle getCar_layer_style() {
        return car_layer_style;
    }

    public void setCar_layer_style(CcarLayerStyle car_layer_style) {
        this.car_layer_style = car_layer_style;
    }

    public static class CcarLayerStyle {

        private CarMarker car_marker;
        private CompassMarkerInfo compass_marker_info;
        private CarAnimationStyle car_animation_style;

        public CarMarker getCar_marker() {
            return car_marker;
        }

        public void setCar_marker(CarMarker car_marker) {
            this.car_marker = car_marker;
        }

        public CompassMarkerInfo getCompass_marker_info() {
            return compass_marker_info;
        }

        public void setCompass_marker_info(CompassMarkerInfo compass_marker_info) {
            this.compass_marker_info = compass_marker_info;
        }

        public CarAnimationStyle getCar_animation_style() {
            return car_animation_style;
        }

        public void setCar_animation_style(CarAnimationStyle car_animation_style) {
            this.car_animation_style = car_animation_style;
        }

        public static class CarMarker {
            public String compass_marker_id;
            public String compass_indicator_marker_id;
            public String track_marker_id;
            public String track_arc_marker_id;
            public String shine_marker_id;

            public String getCompass_marker_id() {
                return compass_marker_id;
            }

            public void setCompass_marker_id(String compass_marker_id) {
                this.compass_marker_id = compass_marker_id;
            }

            public String getCompass_indicator_marker_id() {
                return compass_indicator_marker_id;
            }

            public void setCompass_indicator_marker_id(String compass_indicator_marker_id) {
                this.compass_indicator_marker_id = compass_indicator_marker_id;
            }

            public String getTrack_marker_id() {
                return track_marker_id;
            }

            public void setTrack_marker_id(String track_marker_id) {
                this.track_marker_id = track_marker_id;
            }

            public String getTrack_arc_marker_id() {
                return track_arc_marker_id;
            }

            public void setTrack_arc_marker_id(String track_arc_marker_id) {
                this.track_arc_marker_id = track_arc_marker_id;
            }

            public String getShine_marker_id() {
                return shine_marker_id;
            }

            public void setShine_marker_id(String shine_marker_id) {
                this.shine_marker_id = shine_marker_id;
            }
        }

        public static class CompassMarkerInfo {
            public String east_marker_id;
            public String south_marker_id;
            public String west_marker_id;
            public String north_marker_id;
            public int relative_distance;

            public String getEast_marker_id() {
                return east_marker_id;
            }

            public void setEast_marker_id(String east_marker_id) {
                this.east_marker_id = east_marker_id;
            }

            public String getSouth_marker_id() {
                return south_marker_id;
            }

            public void setSouth_marker_id(String south_marker_id) {
                this.south_marker_id = south_marker_id;
            }

            public String getWest_marker_id() {
                return west_marker_id;
            }

            public void setWest_marker_id(String west_marker_id) {
                this.west_marker_id = west_marker_id;
            }

            public String getNorth_marker_id() {
                return north_marker_id;
            }

            public void setNorth_marker_id(String north_marker_id) {
                this.north_marker_id = north_marker_id;
            }

            public int getRelative_distance() {
                return relative_distance;
            }

            public void setRelative_distance(int relative_distance) {
                this.relative_distance = relative_distance;
            }
        }

        public static class CarAnimationStyle {
            public int car_style;
            public NoNet no_net;
            public Net net;

            public int getCar_style() {
                return car_style;
            }

            public void setCar_style(int car_style) {
                this.car_style = car_style;
            }

            public NoNet getNo_net() {
                return no_net;
            }

            public void setNo_net(NoNet no_net) {
                this.no_net = no_net;
            }

            public Net getNet() {
                return net;
            }

            public void setNet(Net net) {
                this.net = net;
            }

            public static class NoNet {
                public float animation_duration;
                public float from_zoom;
                public float end_zoom;
                public float from_lpha;
                public float end_lpha;

                public float getAnimation_duration() {
                    return animation_duration;
                }

                public void setAnimation_duration(float animation_duration) {
                    this.animation_duration = animation_duration;
                }

                public float getFrom_zoom() {
                    return from_zoom;
                }

                public void setFrom_zoom(float from_zoom) {
                    this.from_zoom = from_zoom;
                }

                public float getEnd_zoom() {
                    return end_zoom;
                }

                public void setEnd_zoom(float end_zoom) {
                    this.end_zoom = end_zoom;
                }

                public float getFrom_lpha() {
                    return from_lpha;
                }

                public void setFrom_lpha(float from_lpha) {
                    this.from_lpha = from_lpha;
                }

                public float getEnd_lpha() {
                    return end_lpha;
                }

                public void setEnd_lpha(float end_lpha) {
                    this.end_lpha = end_lpha;
                }
            }

            public static class Net {
                public float animation_duration;
                public float from_zoom;
                public float end_zoom;
                public float from_lpha;
                public float end_lpha;

                public float getAnimation_duration() {
                    return animation_duration;
                }

                public void setAnimation_duration(float animation_duration) {
                    this.animation_duration = animation_duration;
                }

                public float getFrom_zoom() {
                    return from_zoom;
                }

                public void setFrom_zoom(float from_zoom) {
                    this.from_zoom = from_zoom;
                }

                public float getEnd_zoom() {
                    return end_zoom;
                }

                public void setEnd_zoom(float end_zoom) {
                    this.end_zoom = end_zoom;
                }

                public float getFrom_lpha() {
                    return from_lpha;
                }

                public void setFrom_lpha(float from_lpha) {
                    this.from_lpha = from_lpha;
                }

                public float getEnd_lpha() {
                    return end_lpha;
                }

                public void setEnd_lpha(float end_lpha) {
                    this.end_lpha = end_lpha;
                }
            }
        }
    }
}
