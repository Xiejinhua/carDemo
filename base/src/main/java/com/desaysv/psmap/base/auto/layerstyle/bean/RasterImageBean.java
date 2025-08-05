package com.desaysv.psmap.base.auto.layerstyle.bean;

/**
 * 栅格路口大图json配置实体类
 * Created by AutoSdk
 * 注：目前的变量命名是和style.json里的字段一一对应，故先不修改命名规范，后续统一处理
 */
public class RasterImageBean {


    private RasterImageLayerItemStyleBean raster_image_layer_item_style;

    public RasterImageLayerItemStyleBean getRaster_image_layer_item_style() {
        return raster_image_layer_item_style;
    }

    public void setRaster_image_layer_item_style(RasterImageLayerItemStyleBean raster_image_layer_item_style) {
        this.raster_image_layer_item_style = raster_image_layer_item_style;
    }

    public static class RasterImageLayerItemStyleBean {

        private int priority;
        private int winx;
        private int winy;
        private int width;
        private int height;
        private String bg_marker_id;
        private String bg_marker_info;
        private int marker_id;

        public String getBg_marker_info() {
            return bg_marker_info;
        }

        public void setBg_marker_info(String bg_marker_info) {
            this.bg_marker_info = bg_marker_info;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getWinx() {
            return winx;
        }

        public void setWinx(int winx) {
            this.winx = winx;
        }

        public int getWiny() {
            return winy;
        }

        public void setWiny(int winy) {
            this.winy = winy;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getBg_marker_id() {
            return bg_marker_id;
        }

        public void setBg_marker_id(String bg_marker_id) {
            this.bg_marker_id = bg_marker_id;
        }

        public int getMarker_id() {
            return marker_id;
        }

        public void setMarker_id(int marker_id) {
            this.marker_id = marker_id;
        }
    }
}
