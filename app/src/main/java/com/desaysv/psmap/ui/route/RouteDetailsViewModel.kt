package com.desaysv.psmap.ui.route

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autosdk.bussiness.widget.route.model.NaviStationItemData
import com.desaysv.psmap.base.business.CruiseBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/8
 * @description 路线规划viewModel
 */
@HiltViewModel
class RouteDetailsViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val gson: Gson,
    mSkyBoxBusiness: SkyBoxBusiness,
) : ViewModel() {
    val setToast: LiveData<String> = mRouteBusiness.setToast
    val themeChange = mSkyBoxBusiness.themeChange()

    //路线详情数据
    val naviStationFatalist = mRouteBusiness.naviStationFatalist

    //是否进入避开路段选择
    private val _parryVisibility = MutableLiveData(false)
    val parryVisibility: LiveData<Boolean> = _parryVisibility

    //避开按钮是否可点击
    private val _parryClick = MutableLiveData(false)
    val parryClick: LiveData<Boolean> = _parryClick

    private var naviStationFataSelect = ArrayList<NaviStationItemData>()

    fun getNaviStationFataSelect(): ArrayList<NaviStationItemData> = naviStationFataSelect

    init {
        Timber.i("RouteDetailsViewModel init")
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("RouteDetailsViewModel onCleared")
    }


    /**
     * 是否进入避开路段选择
     */
    fun goParrySelect(isParry: Boolean) = _parryVisibility.postValue(isParry)

    /**
     * 避开按钮是否可点击
     */
    fun setParryClick() {
        naviStationFataSelect = naviStationFatalist.value?.filter { it.isSelect } as ArrayList<NaviStationItemData>
        _parryClick.postValue(naviStationFataSelect.isNotEmpty())
    }

    /**
     * 路线部分道路展示 避开路线显示
     */
    fun updateRouteDodgeLine(naviStationFata: NaviStationItemData) {
        mRouteBusiness.onClickGroupItem(naviStationFata)
    }

    /**
     * 请求路线详情避开路段算路
     */
    fun avoidRoute() = mRouteBusiness.avoidRoute(naviStationFataSelect)

    /**
     * 退出路线详情更新路线
     */
    fun handleNewRoute() = mRouteBusiness.handleNewRoute(false)
}

