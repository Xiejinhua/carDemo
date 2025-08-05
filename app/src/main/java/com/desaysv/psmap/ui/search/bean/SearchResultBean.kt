package com.desaysv.psmap.ui.search.bean

import com.autonavi.gbl.search.model.DeepinfoPoi
import com.autosdk.bussiness.common.POI

data class SearchResultBean(
    var poi: POI? = null,   //扎标信息
    var deepinfoPoi: DeepinfoPoi? = null,   //深度信息
    var isSelect: Boolean = false,   //是否选中item
    var isFavorite: Boolean = false,  //收藏信息
    var isChildLayout: Boolean = false,  //子POI布局是否显示
    var moreText: String? = null,   //收起展开按钮文字
    var isChargeLeftVisible: Boolean = false,    //剩余电量是否显示
    var isMoreVisible: Boolean = false,  //收起展开按钮是否显示
    var chargeLeft: String? = null, //剩余电量
    var chargeLeftText: String? = null, //电量文字提示
    var isLowCharge: Boolean = false, //是否电量低于5%
    var distance: String? = null, //距离
    var disAndTime: String? = null, //距离和时间
    var createTime: String? = null, //创建时间
    var type: Int = 0, //0普通搜索结果， 1途经点搜索结果， 2收到的点 , 3家 , 4公司 ， 5 组队目的地
) {
    override fun toString(): String {
        return "SearchResultBean{" +
                "poi=" + poi +
                ", deepinfoPoi=" + deepinfoPoi +
                ", isSelect=" + isSelect +
                ", isFavorite=" + isFavorite +
                ", childLayout=" + isChildLayout +
                ", moreText='" + moreText + '\'' +
                ", chargeLeftVisible=" + isChargeLeftVisible +
                ", moreVisible=" + isMoreVisible +
                ", chargeLeft='" + chargeLeft + '\'' +
                ", chargeLeftText='" + chargeLeftText + '\'' +
                ", lowCharge=" + isLowCharge +
                '}'
    }
}
