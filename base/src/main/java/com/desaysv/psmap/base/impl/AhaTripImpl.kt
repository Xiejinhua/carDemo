package com.desaysv.psmap.base.impl

import androidx.activity.result.ActivityResult
import com.autonavi.gbl.data.model.CityItemInfo

interface AhaTripImpl {
    fun ahaInit() //路书init

    suspend fun requestLineList(
        page: Int, //页码
        size: Int,
        sort: String, //排序类型
        cityItemInfo: CityItemInfo, //选择城市
        keyword: String, //路书搜索 关键词 景区相关路书（取景区详情 caption 字段作为 keyword 进行搜索）
        day: String
    ) //请求路书列表

    suspend fun requestLineDetail(
        id: String, //路书id
    ) //请求路书详情

    suspend fun requestLineThemeList() //请求路书主题分类

    suspend fun requestFavorite(
        type: String, //路书 1、轨迹 12、景点 3 、共创 4
        id: String, //对应精品路书/共创路书/轨迹的 id
        isMineFav: Boolean //是否是路书收藏界面
    ) //请求收藏/取消收藏

    suspend fun requestScenicList(
        page: Int, //页码
        sort: String, //排序类型
        isCurrentCity: Boolean, //是否是当前城市
        cityCode: String //选择城市code
    ) //景区列表、搜索接口

    suspend fun requestScenicDetail(
        id: String, //景点 id
    ) //景区详情接口

    suspend fun requestScenicSector(
        distance: Int, //前方扇形搜索范围 单位米 默认 5000 米
    ) //景区播报

    suspend fun requestMineGuideList(
        page: Int, //页码
        size: Int
    ) //我的-我制作的共创路书列表

    suspend fun requestMineGuideDetail(
        id: String, //共创路书 id
    ) //共创路书详情

    suspend fun requestMineTankList(
        page: Int, //页码
        size: Int
    ) //我的-我制作的轨迹列表

    suspend fun requestMineTankDetail(
        id: String, //轨迹 id
    ) //我的-轨迹详情

    suspend fun requestMineGuideDelete(
        id: String, //共创路书 id
    ) //删除我的共创

    suspend fun requestMineTankDelete(
        id: String, //轨迹 id
    ) //删除我的轨迹

    suspend fun requestMineCollectGuideList(
        page: Int, //页码
        size: Int
    ) //我的-我的收藏共创路书列表

    suspend fun requestMineCollectList(
        page: Int, //页码
        size: Int,
        type: Int //路书1、轨迹12、景点3
    ) //我的-我的收藏（路书(非共创路书)、轨迹、景点）

    fun isLogin(): Boolean //判断当前是否是登录状态 true 登录 false 未登录

    fun registerLogin() //注册阿哈登录回调

    fun goLogin(result: ActivityResult) //跳转阿哈登录

    fun goHome(type: Int, cityId: Int, cityName: String, themeId:Int)//页面跳转根据type跳转

    fun goDetail(type: Int, id: Int) //跳转详情页
}