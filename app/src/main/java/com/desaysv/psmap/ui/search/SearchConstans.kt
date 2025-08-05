package com.desaysv.psmap.ui.search

import com.desaysv.psmap.R
import com.desaysv.psmap.ui.search.bean.SearchCategoryBean

/**
 * @author 张楠
 * @time 2025/01/08
 * @description
 */
object SearchCategoryConstants {
    val  AROUND_CATEGORY_LIST: List<SearchCategoryBean> = ArrayList<SearchCategoryBean>().apply {
        add(
            SearchCategoryBean(
                name = "卫生间",
                imgDay = R.drawable.selector_ic_search_category_restroom_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_restroom_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "加油站",
                imgDay = R.drawable.selector_ic_search_category_gas_station_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_gas_station_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "停车场",
                imgDay = R.drawable.selector_ic_search_category_paking_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_paking_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "美食",
                imgDay = R.drawable.selector_ic_search_category_restaurant_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_restaurant_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "酒店",
                imgDay = R.drawable.selector_ic_search_category_hotel_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_hotel_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "洗车",
                imgDay = R.drawable.selector_ic_search_category_car_wash_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_car_wash_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "维修站",
                imgDay = R.drawable.selector_ic_search_category_repair_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_repair_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "4S店",
                imgDay = R.drawable.selector_ic_search_category_4s_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_4s_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "银行",
                imgDay = R.drawable.selector_ic_search_category_bank_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_bank_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "购物", imgDay = R.drawable.selector_ic_search_category_shop_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_shop_110_100_night
            )
        )
        add(
            SearchCategoryBean(
                name = "充电站", imgDay = R.drawable.selector_ic_search_category_charging_pile_110_100_day,
                imgNight = R.drawable.selector_ic_search_category_charging_pile_110_100_night
            )
        )
    }

    val ALONG_WAY_CATEGORY_LIST: List<SearchCategoryBean> = ArrayList<SearchCategoryBean>().apply {
        add(
            SearchCategoryBean(
                name = "加油站",
                imgDay = R.drawable.selector_ic_search_category_gas_station_day,
                imgNight = R.drawable.selector_ic_search_category_gas_station_night
            )
        )
        add(
            SearchCategoryBean(
                name = "卫生间",
                imgDay = R.drawable.selector_ic_search_category_restroom_day,
                imgNight = R.drawable.selector_ic_search_category_restroom_night
            )
        )
        add(
            SearchCategoryBean(
                name = "维修站",
                imgDay = R.drawable.selector_ic_search_category_repair_day,
                imgNight = R.drawable.selector_ic_search_category_repair_night
            )
        )
        add(
            SearchCategoryBean(
                name = "美食",
                imgDay = R.drawable.selector_ic_search_category_restaurant_day,
                imgNight = R.drawable.selector_ic_search_category_restaurant_night
            )
        )
        add(
            SearchCategoryBean(
                name = "更多",
                imgDay = R.drawable.selector_ic_search_category_more_day,
                imgNight = R.drawable.selector_ic_search_category_more_night
            )
        )
        add(
            SearchCategoryBean(
                name = "收藏点",
                imgDay = R.drawable.selector_ic_search_category_favorite_day,
                imgNight = R.drawable.selector_ic_search_category_favorite_night
            )
        )
        add(
            SearchCategoryBean(
                name = "收到的点",
                imgDay = R.drawable.selector_ic_search_category_received_day,
                imgNight = R.drawable.selector_ic_search_category_received_night
            )
        )
        add(
            SearchCategoryBean(
                name = "地图选点",
                imgDay = R.drawable.selector_ic_search_category_map_select_day,
                imgNight = R.drawable.selector_ic_search_category_map_select_night
            )
        )
    }

    val ALONG_WAY_IN_NAVIGATING_CATEGORY_LIST: List<SearchCategoryBean> = ArrayList<SearchCategoryBean>().apply {
        add(
            SearchCategoryBean(
                name = "加油站",
                imgDay = R.drawable.selector_ic_search_category_gas_station_day,
                imgNight = R.drawable.selector_ic_search_category_gas_station_night
            )
        )
        add(
            SearchCategoryBean(
                name = "卫生间",
                imgDay = R.drawable.selector_ic_search_category_restroom_day,
                imgNight = R.drawable.selector_ic_search_category_restroom_night
            )
        )
        add(
            SearchCategoryBean(
                name = "维修站",
                imgDay = R.drawable.selector_ic_search_category_repair_day,
                imgNight = R.drawable.selector_ic_search_category_repair_night
            )
        )
        add(
            SearchCategoryBean(
                name = "美食",
                imgDay = R.drawable.selector_ic_search_category_restaurant_day,
                imgNight = R.drawable.selector_ic_search_category_restaurant_night
            )
        )
        add(
            SearchCategoryBean(
                name = "充电站",
                imgDay = R.drawable.selector_ic_search_category_charging_pile_day,
                imgNight = R.drawable.selector_ic_search_category_charging_pile_night
            )
        )
        add(
            SearchCategoryBean(
                name = "服务区",
                imgDay = R.drawable.selector_ic_search_category_service_area_day,
                imgNight = R.drawable.selector_ic_search_category_service_area_night
            )
        )
    }

    val ALONG_WAY_CATEGORY_TRIP_LIST: List<SearchCategoryBean> = ArrayList<SearchCategoryBean>().apply {
        add(SearchCategoryBean("风景名胜"))
        add(SearchCategoryBean("公园广场"))
        add(SearchCategoryBean("游乐场"))
        add(SearchCategoryBean("动物园"))
        add(SearchCategoryBean("博物馆"))
    }

    val AROUND_CATEGORY_FOOD_LIST: List<SearchCategoryBean> = ArrayList<SearchCategoryBean>().apply {
        add(SearchCategoryBean("中餐"))
        add(SearchCategoryBean("快餐"))
        add(SearchCategoryBean("火锅"))
        add(SearchCategoryBean("西餐"))
        add(SearchCategoryBean("咖啡厅"))
        add(SearchCategoryBean("美食"))
        add(SearchCategoryBean("川菜"))
        add(SearchCategoryBean("韩国料理"))
        add(SearchCategoryBean("日本料理"))
    }
}