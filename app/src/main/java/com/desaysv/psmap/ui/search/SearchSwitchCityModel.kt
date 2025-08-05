package com.desaysv.psmap.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autonavi.gbl.data.model.AreaType
import com.autonavi.gbl.data.model.CityItemInfo
import com.autonavi.gbl.data.model.DownLoadMode
import com.autosdk.common.storage.MapSharePreference
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.ProvinceDataBean
import com.desaysv.psmap.model.business.OfflineDataBusiness
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.CityUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import java.lang.reflect.Type
import javax.inject.Inject


/**
 * @author 张楠
 * @time 2024/1/29
 * @description
 */

@HiltViewModel
class SearchSwitchCityModel @Inject constructor(
    private val settingAccountBusiness: SettingAccountBusiness,
    private val offlineDataBusiness: OfflineDataBusiness,
    private val mapDataBusiness: MapDataBusiness,
    private val gson: Gson,
    private val mUserBusiness: UserBusiness,
    private val sharePreferenceFactory: SharePreferenceFactory
) : ViewModel() {
    private val mapSharePreference = sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.account)

    //搜索城市列表
    val searchCityListLiveData: MutableLiveData<ArrayList<CityItemInfo>> = MutableLiveData()

    //无搜索结果
    var noCityLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //无搜索结果
    var noCityTextLiveData: MutableLiveData<String> = MutableLiveData("没有找到相关的城市")

    private var commandRequestSearchBean: CommandRequestSearchBean? = null

    //是否搜索城市界面
    var isSearchCityLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    //searchBox的删除、loading状态
    val buttonType = MutableLiveData(0)//0隐藏 1显示删除按钮 2显示loading

    @Synchronized
    fun setSearchBean(commandBean: CommandRequestSearchBean?) {
        commandBean?.let {
            commandRequestSearchBean = commandBean
        }
    }

    fun getSearchBean(): CommandRequestSearchBean? {
        return commandRequestSearchBean
    }

    //获取离线城市列表数据
    fun getAllCityData(): ArrayList<ProvinceDataBean> {
        val provinceList = ArrayList<ProvinceDataBean>()

        //常用地点
        val commonList = getCommonCity()
        provinceList.add(ProvinceDataBean("常用城市", commonList))


        // 当前选择的是省级行政区域，不显示附近的城市
        val canShowvecNearAdcodeList: Boolean = commandRequestSearchBean?.city?.cityAdcode?.let { acCode ->
            !CityUtil.isProvince(acCode)
        } ?: true

        //附近城市
        if (canShowvecNearAdcodeList) {
            offlineDataBusiness.getVecNearAdCodeList(commandRequestSearchBean?.city?.cityAdcode).let { vecNearAdCodeList ->
                val vecNearList = ArrayList<CityItemInfo>()
                var list = vecNearAdCodeList
                if (vecNearAdCodeList.size > 4) {
                    list = vecNearAdCodeList.take(4) as ArrayList
                }
                for (adCode in list) {
                    mapDataBusiness.getCityInfo(adCode)?.let { vecNearList.add(it) }
                }
                provinceList.add(ProvinceDataBean("附近城市", vecNearList))
            }
        }

        //直辖市
        val direct = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_DIRECT)
        val arrayList = ArrayList<CityItemInfo>()
        if (direct != null) {
            for (i in direct.indices) {
                mapDataBusiness.getCityInfo(direct[i])?.let { arrayList.add(it) }
            }
        }
        provinceList.add(ProvinceDataBean("直辖市", arrayList))

        //省份
        val prov = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_PROV)
        if (prov != null) {
            for (i in prov.indices) {
                val provinceInfo = mapDataBusiness.getProvinceInfo(prov[i])
                if (provinceInfo != null) {
                    val cityInfoList = provinceInfo.cityInfoList
                    val cityItemInfo = CityItemInfo().apply {
                        belongedProvince = provinceInfo.provAdcode
                        cityX = provinceInfo.provX
                        cityY = provinceInfo.provY
                        cityName = provinceInfo.provName
                        cityLevel = provinceInfo.provLevel
                        cityAdcode = provinceInfo.provAdcode
                        initial = provinceInfo.provInitial
                        pinyin = provinceInfo.provPinyin
                    }
                    cityInfoList.add(0, cityItemInfo)
                    provinceList.add(ProvinceDataBean(provinceInfo.provName, cityInfoList))
                }
            }
        }

        //特别行政区
        val special = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_SPECIAL)
        val specialArrayList = ArrayList<CityItemInfo>()
        if (special != null) {
            for (i in special.indices) {
                mapDataBusiness.getCityInfo(special[i])?.let { specialArrayList.add(it) }
            }
        }
        provinceList.add(ProvinceDataBean("特别行政区", specialArrayList))

        for (provinceDataBean in provinceList) {
            Timber.i("provinceDataBean = ${gson.toJson(provinceDataBean)}")
        }

        return provinceList
    }

    //用户选择
    fun saveCommonCity(cityItemInfo: CityItemInfo) {
        val key = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.commonCityList.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.commonCityList.toString()
        }
        val type: Type = object : TypeToken<ArrayList<CityItemInfo>>() {}.type
        val list = gson.fromJson<ArrayList<CityItemInfo>>(mapSharePreference.getStringValue(key, ""), type)
        //常用地点最多4个
        if (!list.isNullOrEmpty() && list.size >= 4) {
            var isExist = false
            var isExistIndex = -1
            for ((index, item) in list.withIndex()) {
                if (item.cityName.equals(cityItemInfo.cityName)) {
                    isExist = true
                    isExistIndex = index
                    break
                }
            }
            //如果已有四个中没有需要保存的city
            if (isExist) {
                if (isExistIndex != 0) { //如果已有四个中有需要保存的city,并且不是第一位，调整到第一位
                    list.removeAt(isExistIndex)
                    list.add(0, cityItemInfo)
                }
            } else {  //如果已有四个中没有需要保存的city，删除最后一个
                list.removeLast()
                list.add(0, cityItemInfo)
            }
        }
        mapSharePreference.putStringValue(key, gson.toJson(list))
    }

    //常用地点
    private fun getCommonCity(): ArrayList<CityItemInfo> {
        val key = if (settingAccountBusiness.isLogin()) {
            BaseConstant.uid + MapSharePreference.SharePreferenceKeyEnum.commonCityList.toString()
        } else {
            MapSharePreference.SharePreferenceKeyEnum.commonCityList.toString()
        }
        val type: Type = object : TypeToken<ArrayList<CityItemInfo>>() {}.type
        var jsonList = gson.fromJson<ArrayList<CityItemInfo>>(mapSharePreference.getStringValue(key, ""), type)
        Timber.i("getCommonCity() called $jsonList")
        if (jsonList.isNullOrEmpty()) {
            //初始默认为北京、上海、广州、深圳
            jsonList = ArrayList<CityItemInfo>().also { list ->
                //北京市
                mapDataBusiness.getCityInfo(110000)?.let { list.add(it) }
                //上海市
                mapDataBusiness.getCityInfo(310000)?.let { list.add(it) }
                //广州市
                mapDataBusiness.getCityInfo(440100)?.let { list.add(it) }
                //深圳市
                mapDataBusiness.getCityInfo(440300)?.let { list.add(it) }
            }
            //保存在SP中
            mapSharePreference.putStringValue(key, gson.toJson(jsonList))
        }
        return jsonList
    }

    fun onInputKeywordChanged(keyword: String) {
        val arrayList = ArrayList<CityItemInfo>()
        if (keyword.isNotEmpty()) {
            val list = mapDataBusiness.searchAdCode(keyword)
            Timber.i("list = $list")
            if (!list.isNullOrEmpty()) {
                for (adCode in list) {
                    mapDataBusiness.getCityInfo(adCode)?.let { arrayList.add(it) }
                }
                noCityLiveData.postValue(false)
            } else {

                noCityTextLiveData.postValue("没有找到${keyword}相关的城市")
                noCityLiveData.postValue(true)
            }
            isSearchCityLiveData.postValue(true)
            buttonType.postValue(1)
        } else {
            isSearchCityLiveData.postValue(false)
            buttonType.postValue(0)
        }
        searchCityListLiveData.postValue(arrayList)
    }



}