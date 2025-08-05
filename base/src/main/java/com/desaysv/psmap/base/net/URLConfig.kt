package com.desaysv.psmap.base.net

/**
 * Author : wangmansheng
 * Date : 2024-1-4
 * Description : 网络请求地址，API等配置
 */
object URLConfig {
    const val DEFAULT_BASE_URL = "https://test.desayav.com/" //用来占位的，实际上不可用

    //账号绑定
    const val G50_TEST_ACC_URL = "https://deviov.desaysv-xxzx.com:39086/bapi/" //开发环境
    const val G50_BASE_ACC_URL = "https://blueapi.desaysv-iov.com/authApi/" //正式环境
    const val apiKey = "B312220D9F55BE75B151009CA6C91A760A6D9AB29C04C2AC07694300D6478669" //开发环境:
    const val baseApiKey = "282DA1920B958749C736777D235C064F3349AD4ECDDAA338001D03094FEB52B2" //正式环境
    const val saveGDBind = "vehicle/V1/saveGDBind" //账号绑定接口

    //激活统计
    const val ACTIVATE_TEST_URL = "https://deviov.desaysv-xxzx.com:39087" //开发环境
    const val ACTIVATE_BASE_URL = "https://activate.desaysv-iov.com" //正式环境
    const val sdkActivate = "/api/sv/app/activate/appActivateStatistics" //SDK激活统计

    //捷途专属分类
    const val CUSTOM_CATEGORY_TEST_URL = "https://bfgntspconsole-stg.mychery.com" //测试环境
    const val CUSTOM_GET_POICATEGORY_URL = "/ocs/openNow/getPoiCategory" //获取POI分类
    const val CUSTOM_GET_FULLPOILIST_URL = "/ocs/openNow/getFullPoiList" //获取全量POI列表信息
    const val CUSTOM_GET_POIDETAIL_URL = "/ocs/openNow/getPoiDetail" //获取POI列表信息

    //乐享出行
    const val JETOUT_TEST_URL = "https://terminal-lioncloud-stg.mychery.com" //测试环境
    const val JETOUT_PRO_URL = "https://terminal-lioncloud.mychery.com" //生产环境
    const val ONE_CLICK_CAR_PREPARATION_URL = "/hs/hu/command" //一键备车

    //捷途探趣
    const val JETOUT_BYTE_AUTO_URL = "https://api-vehicle.volcengine.com" //捷途探趣字节大模型API地址
    const val JETOUT_BYTE_AUTO_GET_VIDEO_SCHEMA_URL = "/dpfm/v1/plugin/do/stream" //请求字节大模型视频Schema


    const val JETOUT_BYTE_AUTO_DOUYIN_AGREEMENT_URL = "https://lf3-cdn-tos.draftstatic.com/obj/ies-hotsoon-draft/3292/a8921ec7-7e48-4769-b7ff-07bff8491f16.html?theme=vehicle_black_v3" //抖音
}
