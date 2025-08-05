package com.desaysv.psmap.ui.route.restrict

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autonavi.gbl.aosclient.model.GReStrictedAreaDataRuleRes
import com.autonavi.gbl.aosclient.model.GRestrictCity
import com.desaysv.psmap.base.business.MapBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/5
 * @description 限行区域ViewModel
 */
@HiltViewModel
class RestrictViewModel @Inject constructor(
    private val mSkyBoxBusiness: SkyBoxBusiness,
    private val mRouteBusiness: RouteBusiness,
    private val mMapBusiness: MapBusiness,
    private val gson: Gson
) : ViewModel() {
    val themeChange = mSkyBoxBusiness.themeChange()

    //限行详情信息
    val restrictInfoDetails = if (mRouteBusiness.isPlanRouteing()) mRouteBusiness.restrictInfoDetails else mMapBusiness.restrictInfoDetails
    private var routeRestrictBean: GReStrictedAreaDataRuleRes? = null
    private var mainRestrictSelectList: ArrayList<Pair<Int, Boolean>> = arrayListOf()

    //城市限行区域列表数据
    private val _restrictCityList = MutableLiveData<ArrayList<GRestrictCity>>()
    val restrictCityList: LiveData<ArrayList<GRestrictCity>> = _restrictCityList

    init {
        if (mRouteBusiness.isPlanRouteing())
            mRouteBusiness.selectRoute()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("RestrictViewModel onCleared")
        mMapBusiness.removeRestrictInfoDetails()
        mRouteBusiness.clearRouteRestRestrict()
        if (mRouteBusiness.isPlanRouteing())
            mRouteBusiness.switchFocusPath()

    }

    /**
     * 保存传过来的 限行区域信息
     */
    @Synchronized
    fun setRouteRestrictBean(commandBean: GReStrictedAreaDataRuleRes) {
        Timber.i("citynums.size = ${commandBean.citynums}")
        routeRestrictBean = commandBean
        mainRestrictSelectList.clear()
        routeRestrictBean?.cities?.forEachIndexed { index, gRestrictCity ->
            mainRestrictSelectList.add(Pair(index, false))
        }
        setOpenAndStowRestrictInfo()
        onRuleSelected()
    }

    /**
     * 获取 起点终点和途经点数据
     */
    @Synchronized
    fun getRouteRestrictBean(): GReStrictedAreaDataRuleRes? {
        return routeRestrictBean
    }

    /**
     * 选择查看限行区域
     */
    fun onRuleSelected(mainIndex: Int = 0, childIndex: Int = 0) {
        routeRestrictBean?.cities?.let { cities ->
            if (mainIndex in cities.indices) {
                val rules = cities[mainIndex].rules
                if (childIndex in rules.indices) {
                    mRouteBusiness.onRuleSelected(rules[childIndex])
                } else {
                    // 处理 childIndex 超出范围的情况
                    Timber.d("Child index out of bounds: $childIndex, size: ${rules.size}")
                }
            } else {
                Timber.d("Main index out of bounds: $mainIndex, size: ${cities.size}")
            }
        } ?: run {
            Timber.d("Cities list is empty")
        }
    }


    /**
     * 打开和收起更多限行信息
     */
    fun setOpenAndStowRestrictInfo(mainIndex: Int = 0, isOpenMore: Boolean = false) {
        Timber.i("setOpenAndStowRestrictInfo mainIndex = $mainIndex, isOpenMore = $isOpenMore")
        mainRestrictSelectList.forEach { pair ->
            if (mainIndex == pair.first) {
                mainRestrictSelectList[mainIndex] = Pair(pair.first, isOpenMore)
            }
        }
        Timber.i("setOpenAndStowRestrictInfo mainRestrictSelectList = ${gson.toJson(mainRestrictSelectList)}")
        routeRestrictBean?.cities?.takeIf { it.isNotEmpty() }?.mapIndexed { index, cityList ->
            Timber.i("setOpenAndStowRestrictInfo ruleNums = ${cityList.ruleNums}")
            GRestrictCity().apply {
                cityName = cityList.cityName
                ruleNums = cityList.ruleNums
                ruleType = cityList.ruleType
                rules = if (mainRestrictSelectList[index].second) cityList.rules else arrayListOf(cityList.rules[0])

            }
        }?.let { restrictDataList ->
            _restrictCityList.postValue(restrictDataList as ArrayList<GRestrictCity>?)
        }
    }

}