package com.desaysv.psmap.base.auto.layerstyle.bean;

/**
 * 矢量路口大图json配置实体类
 * Created by AutoSdk
 * 注：目前的变量命名是和style.json里的字段一一对应，故先不修改命名规范，后续统一处理
 */
public class VectorCrossBean {

    private VectorCrossLayerStyleBean vector_cross_layer_style;

    public VectorCrossLayerStyleBean getVector_cross_layer_style() {
        return vector_cross_layer_style;
    }

    public void setVector_cross_layer_style(VectorCrossLayerStyleBean vector_cross_layer_style) {
        this.vector_cross_layer_style = vector_cross_layer_style;
    }

    public static class VectorCrossLayerStyleBean {

        private VectorCrossMarkerBean vector_cross_marker;
        private VectorCrossAttrBean vector_cross_attr;

        public VectorCrossMarkerBean getVector_cross_marker() {
            return vector_cross_marker;
        }

        public void setVector_cross_marker(VectorCrossMarkerBean vector_cross_marker) {
            this.vector_cross_marker = vector_cross_marker;
        }

        public VectorCrossAttrBean getVector_cross_attr() {
            return vector_cross_attr;
        }

        public void setVector_cross_attr(VectorCrossAttrBean vector_cross_attr) {
            this.vector_cross_attr = vector_cross_attr;
        }

        public static class VectorCrossMarkerBean {
            private String bg_marker_id;
            private String bg_marker_info;
            private String arrow_outer_marker_id;
            private String arrow_inner_marker_id;
            private String car_marker_id;

            public String getBg_marker_info() {
                return bg_marker_info;
            }

            public void setBg_marker_info(String bg_marker_info) {
                this.bg_marker_info = bg_marker_info;
            }

            public String getBg_marker_id() {
                return bg_marker_id;
            }

            public void setBg_marker_id(String bg_marker_id) {
                this.bg_marker_id = bg_marker_id;
            }

            public String getArrow_outer_marker_id() {
                return arrow_outer_marker_id;
            }

            public void setArrow_outer_marker_id(String arrow_outer_marker_id) {
                this.arrow_outer_marker_id = arrow_outer_marker_id;
            }

            public String getArrow_inner_marker_id() {
                return arrow_inner_marker_id;
            }

            public void setArrow_inner_marker_id(String arrow_inner_marker_id) {
                this.arrow_inner_marker_id = arrow_inner_marker_id;
            }

            public String getCar_marker_id() {
                return car_marker_id;
            }

            public void setCar_marker_id(String car_marker_id) {
                this.car_marker_id = car_marker_id;
            }
        }

        public static class VectorCrossAttrBean {

            private int day_mode;
            private int arrow_border_width;
            private int arrow_line_width;
            private AreaColorBean area_color;
            private ArrowBorderColorBean arrow_border_color;
            private ArrowLineColorBean arrow_line_color;
            private ArrowLineCapTextureBean arrow_line_cap_texture;
            private ArrowHeaderCapTextureBean arrow_header_cap_texture;
            private ArrowLineTextureBean arrow_line_texture;
            private RectBean rect;

            public int getDay_mode() {
                return day_mode;
            }

            public void setDay_mode(int day_mode) {
                this.day_mode = day_mode;
            }

            public int getArrow_border_width() {
                return arrow_border_width;
            }

            public void setArrow_border_width(int arrow_border_width) {
                this.arrow_border_width = arrow_border_width;
            }

            public int getArrow_line_width() {
                return arrow_line_width;
            }

            public void setArrow_line_width(int arrow_line_width) {
                this.arrow_line_width = arrow_line_width;
            }

            public AreaColorBean getArea_color() {
                return area_color;
            }

            public void setArea_color(AreaColorBean area_color) {
                this.area_color = area_color;
            }

            public ArrowBorderColorBean getArrow_border_color() {
                return arrow_border_color;
            }

            public void setArrow_border_color(ArrowBorderColorBean arrow_border_color) {
                this.arrow_border_color = arrow_border_color;
            }

            public ArrowLineColorBean getArrow_line_color() {
                return arrow_line_color;
            }

            public void setArrow_line_color(ArrowLineColorBean arrow_line_color) {
                this.arrow_line_color = arrow_line_color;
            }

            public ArrowLineCapTextureBean getArrow_line_cap_texture() {
                return arrow_line_cap_texture;
            }

            public void setArrow_line_cap_texture(ArrowLineCapTextureBean arrow_line_cap_texture) {
                this.arrow_line_cap_texture = arrow_line_cap_texture;
            }

            public ArrowHeaderCapTextureBean getArrow_header_cap_texture() {
                return arrow_header_cap_texture;
            }

            public void setArrow_header_cap_texture(ArrowHeaderCapTextureBean arrow_header_cap_texture) {
                this.arrow_header_cap_texture = arrow_header_cap_texture;
            }

            public ArrowLineTextureBean getArrow_line_texture() {
                return arrow_line_texture;
            }

            public void setArrow_line_texture(ArrowLineTextureBean arrow_line_texture) {
                this.arrow_line_texture = arrow_line_texture;
            }

            public RectBean getRect() {
                return rect;
            }

            public void setRect(RectBean rect) {
                this.rect = rect;
            }

            public static class AreaColorBean {
                private int r;
                private int g;
                private int b;
                private int a;

                public int getR() {
                    return r;
                }

                public void setR(int r) {
                    this.r = r;
                }

                public int getG() {
                    return g;
                }

                public void setG(int g) {
                    this.g = g;
                }

                public int getB() {
                    return b;
                }

                public void setB(int b) {
                    this.b = b;
                }

                public int getA() {
                    return a;
                }

                public void setA(int a) {
                    this.a = a;
                }
            }

            public static class ArrowBorderColorBean {

                private int r;
                private int g;
                private int b;
                private int a;

                public int getR() {
                    return r;
                }

                public void setR(int r) {
                    this.r = r;
                }

                public int getG() {
                    return g;
                }

                public void setG(int g) {
                    this.g = g;
                }

                public int getB() {
                    return b;
                }

                public void setB(int b) {
                    this.b = b;
                }

                public int getA() {
                    return a;
                }

                public void setA(int a) {
                    this.a = a;
                }
            }

            public static class ArrowLineColorBean {

                private int r;
                private int g;
                private int b;
                private int a;

                public int getR() {
                    return r;
                }

                public void setR(int r) {
                    this.r = r;
                }

                public int getG() {
                    return g;
                }

                public void setG(int g) {
                    this.g = g;
                }

                public int getB() {
                    return b;
                }

                public void setB(int b) {
                    this.b = b;
                }

                public int getA() {
                    return a;
                }

                public void setA(int a) {
                    this.a = a;
                }
            }

            public static class ArrowLineCapTextureBean {

                private double x1;
                private double x2;
                private double y1;
                private double y2;

                public double getX1() {
                    return x1;
                }

                public void setX1(double x1) {
                    this.x1 = x1;
                }

                public double getX2() {
                    return x2;
                }

                public void setX2(double x2) {
                    this.x2 = x2;
                }

                public double getY1() {
                    return y1;
                }

                public void setY1(double y1) {
                    this.y1 = y1;
                }

                public double getY2() {
                    return y2;
                }

                public void setY2(double y2) {
                    this.y2 = y2;
                }
            }

            public static class ArrowHeaderCapTextureBean {

                private double x1;
                private double x2;
                private double y1;
                private int y2;

                public double getX1() {
                    return x1;
                }

                public void setX1(double x1) {
                    this.x1 = x1;
                }

                public double getX2() {
                    return x2;
                }

                public void setX2(double x2) {
                    this.x2 = x2;
                }

                public double getY1() {
                    return y1;
                }

                public void setY1(double y1) {
                    this.y1 = y1;
                }

                public int getY2() {
                    return y2;
                }

                public void setY2(int y2) {
                    this.y2 = y2;
                }
            }

            public static class ArrowLineTextureBean {

                private double x1;
                private double x2;
                private double y1;
                private double y2;
                private double texture_len;

                public double getX1() {
                    return x1;
                }

                public void setX1(double x1) {
                    this.x1 = x1;
                }

                public double getX2() {
                    return x2;
                }

                public void setX2(double x2) {
                    this.x2 = x2;
                }

                public double getY1() {
                    return y1;
                }

                public void setY1(double y1) {
                    this.y1 = y1;
                }

                public double getY2() {
                    return y2;
                }

                public void setY2(double y2) {
                    this.y2 = y2;
                }

                public double getTexture_len() {
                    return texture_len;
                }

                public void setTexture_len(double texture_len) {
                    this.texture_len = texture_len;
                }
            }

            public static class RectBean {

                private int x_min;
                private int y_min;
                private int x_max;
                private int y_max;

                public int getX_min() {
                    return x_min;
                }

                public void setX_min(int x_min) {
                    this.x_min = x_min;
                }

                public int getY_min() {
                    return y_min;
                }

                public void setY_min(int y_min) {
                    this.y_min = y_min;
                }

                public int getX_max() {
                    return x_max;
                }

                public void setX_max(int x_max) {
                    this.x_max = x_max;
                }

                public int getY_max() {
                    return y_max;
                }

                public void setY_max(int y_max) {
                    this.y_max = y_max;
                }
            }
        }
    }
}
