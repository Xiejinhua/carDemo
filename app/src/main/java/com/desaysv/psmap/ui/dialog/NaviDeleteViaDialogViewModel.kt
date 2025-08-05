package com.desaysv.psmap.ui.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.desaysv.psmap.base.bean.NaviViaDataBean
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.RouteBusiness
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * @author 谢锦华
 * @time 2024/1/5
 * @description 删除途经点ViewModel
 */
@HiltViewModel
class NaviDeleteViaDialogViewModel @Inject constructor(
    private val mRouteBusiness: RouteBusiness,
    private val mNaviBusiness: NaviBusiness
) : ViewModel() {
    var showViaNaviViaData: LiveData<NaviViaDataBean> =
        if (mNaviBusiness.isNavigating()) mNaviBusiness.showViaNaviViaDataDialog else mRouteBusiness.showViaNaviViaDataDialog

    val routeErrorMessage: LiveData<String> = mRouteBusiness.routeErrorMessage
    val naviErrorMessage: LiveData<String> = mNaviBusiness.naviErrorMessage
    val setNavToast: LiveData<String> = mNaviBusiness.setToast
    val setRouteToast: LiveData<String> = mRouteBusiness.setToast

    fun delMisPoi(naviViaDataBean: NaviViaDataBean) {
        if (mNaviBusiness.isNavigating()) {
            mNaviBusiness.deleteViaPoi(naviViaDataBean.index)
        } else {
            mRouteBusiness.deleteViaPoi(naviViaDataBean.index)
        }
    }
}