package com.desaysv.psmap.ui.settings.offlinedata.download

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autonavi.gbl.data.model.AreaType
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.OperationType
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentOfflineMapDownloadManageBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.dialog.OfflineDataDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 离线地图--下载管理界面
 */
@AndroidEntryPoint
class OfflineMapDownloadManageFragment : Fragment() {
    private lateinit var binding: FragmentOfflineMapDownloadManageBinding
    private val viewModel by viewModels<OfflineMapDownloadManageViewModel>()

    private var offlineMapDownloadingFragment: OfflineMapDownloadingFragment? = null
    private var offlineMapDownloadedFragment: OfflineMapDownloadedFragment? = null
    private var showFlowDialog: CustomDialogFragment? = null
    private var deleteDialog: OfflineDataDialogFragment? = null
    private var pauseDialog: OfflineDataDialogFragment? = null
    private var continueDialog: OfflineDataDialogFragment? = null
    private var updateDialog: OfflineDataDialogFragment? = null
    private var isFirstOpen = true
    private var lastTargetX = 0

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var mapDataBusiness: MapDataBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOfflineMapDownloadManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initData()
        initEventOperation()
    }

    override fun onPause() {
        super.onPause()
        mapDataBusiness.unregisterMapDataObserver()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstOpen) {
            isFirstOpen = false
        } else {
            mapDataBusiness.registerMapDataObserver(viewModel.mapDataObserverImp)
            viewModel.onHiddenChanged(true) //界面Hidden通知
        }
        viewModel.setHeadViewLoadManagerVisible() //设置下载管理显隐
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
        dismissCustomOfflineDialog(deleteDialog) //dismiss 删除对话框
        deleteDialog = null
        dismissCustomOfflineDialog(pauseDialog) //dismiss 暂停对话框
        pauseDialog = null
        dismissCustomOfflineDialog(continueDialog) //dismiss 继续对话框
        continueDialog = null
        dismissCustomOfflineDialog(updateDialog) //dismiss 继续对话框
        updateDialog = null
    }

    override fun onDestroy() {
        super.onDestroy()
        offlineMapDownloadingFragment = null
        offlineMapDownloadedFragment = null

        mapDataBusiness.unregisterMapDataObserver()
        mapDataBusiness.abortDataListCheck(DownLoadMode.DOWNLOAD_MODE_NET)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView()")
        mapDataBusiness.unregisterMapDataObserver()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.onHiddenChanged(hidden) //界面Hidden通知
        if (hidden) {
            mapDataBusiness.unregisterMapDataObserver()
        } else {
            mapDataBusiness.registerMapDataObserver(viewModel.mapDataObserverImp)
            viewModel.setHeadViewLoadManagerVisible() //设置下载管理显隐
        }
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        KeyboardUtil.hideKeyboard(view)
        Timber.d(" initBinding selectTab:%s", viewModel.tabSelect.value )
        if (viewModel.tabSelect.value == 0){
            binding.layoutTab.check(R.id.rb_downloading)
            gotoOfflineMapDownloadingFragment() //切换到下载中列表Fragment
        }
    }

    private fun initData() {
        if (mapDataBusiness.isInitSuccess()) {
            Timber.d(" initData ")
            mapDataBusiness.unregisterMapDataObserver()
            mapDataBusiness.registerMapDataObserver(viewModel.mapDataObserverImp)
            mapDataBusiness.requestDataListCheck(DownLoadMode.DOWNLOAD_MODE_NET, "")
            viewModel.onRequestDataListCheckResult()
        } else {
            toastUtil.showToast("服务初始化异常")
        }
    }

    private fun initEventOperation() {
        //退出下载管理
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            if (!isAdded) return@setOnCheckedChangeListener
            childFragmentManager.executePendingTransactions()
            val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
            hideFragment(transaction)
            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_downloading){
                    binding.indicator.animate()
                        .x(lastTargetX.toFloat())
                        .setDuration(0)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .start()
                } else {
                    binding.indicator.animate()
                        .x(targetX.toFloat())
                        .setDuration(200)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                lastTargetX = targetX
                                when (checkedId) {
                                    R.id.rb_downloading -> {
                                        viewModel.tabSelect.postValue(0)
                                        if (offlineMapDownloadingFragment == null) {
                                            offlineMapDownloadingFragment = OfflineMapDownloadingFragment()
                                            transaction.add(R.id.fragment_container, offlineMapDownloadingFragment!!)
                                        } else {
                                            transaction.show(offlineMapDownloadingFragment!!)
                                        }
                                    }

                                    R.id.rb_downloaded -> {
                                        viewModel.tabSelect.postValue(1)
                                        if (offlineMapDownloadedFragment == null) {
                                            offlineMapDownloadedFragment = OfflineMapDownloadedFragment()
                                            transaction.add(R.id.fragment_container, offlineMapDownloadedFragment!!)
                                        } else {
                                            transaction.show(offlineMapDownloadedFragment!!)
                                        }
                                    }
                                }
                                transaction.commitAllowingStateLoss()
                            }
                        })
                        .start()
                }
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
        }

        //显示toast
        viewModel.showToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        //空间不足提示
        viewModel.enough.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast("可用存储空间不足，无法继续下载，请空间清理完成后，手动点击继续下载")
        }

        //弹窗提示是否用流量下载
        viewModel.toDownloadDialog.unPeek().observe(viewLifecycleOwner) { arCodes ->
            dismissCustomDialog()
            showFlowDialog = CustomDialogFragment.builder().setTitle("流量提醒").setContent("当前为非Wi-Fi网络，下载离线数据将产生流量费用，是否继续下载")
                .doubleButton("继续下载", getString(com.desaysv.psmap.base.R.string.sv_common_cancel)).setOnClickListener {
                    if (it) {
                        viewModel.downLoadConfirmFlow(arCodes, OperationType.OPERATION_TYPE_START)
                    }
                }.apply {
                    show(this@OfflineMapDownloadManageFragment.childFragmentManager, "showFlowDialog")
                }
        }

        //显示删除对话框
        viewModel.showDeleteDialog.unPeek().observe(viewLifecycleOwner) { bundle ->
            showDeleteDialog(bundle)
        }

        //显示暂停对话框
        viewModel.showPauseAskDialog.unPeek().observe(viewLifecycleOwner) { bundle ->
            showPauseAskDialog(bundle)
        }

        //显示继续对话框
        viewModel.showContinueDialog.unPeek().observe(viewLifecycleOwner) { bundle ->
            showContinueDialog(bundle)
        }

        //更新对话框
        viewModel.showUpdateDialog.unPeek().observe(viewLifecycleOwner) { bundle ->
            showUpdateDialog(bundle)
        }

        //解压时取消暂停对话框
        viewModel.tipsEnterUnzip.unPeek().observe(viewLifecycleOwner) { arCode -> tipsEnterUnzip(arCode) }

        //更新数据
        viewModel.updateAllData.unPeek().observe(viewLifecycleOwner) {
            viewModel.setHeadViewLoadManagerVisible() //设置下载管理显隐
        }

        //更新下载状态
        viewModel.updateOperated.unPeek().observe(viewLifecycleOwner) {
            viewModel.setHeadViewLoadManagerVisible() //设置下载管理显隐
        }

        //更新下载Percent数据
        viewModel.updatePercent.unPeek().observe(viewLifecycleOwner) {
            viewModel.setHeadViewLoadManagerVisible() //设置下载管理显隐
        }
    }

    //删除对话框
    private fun showDeleteDialog(bundle: Bundle) {//isBatch是否批量
        val isBatch = bundle.getBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
        val cityName = bundle.getString(BaseConstant.OFFLINE_CITY_NAME, "")
        val adCode = bundle.getInt(BaseConstant.OFFLINE_AD_CODE, 0)
        val adCodes = bundle.getIntegerArrayList(BaseConstant.OFFLINE_AD_CODES)
        var content = "" //弹框content
        var confirm = getString(R.string.sv_common_app_confirm)
        var cityAdCode = ArrayList<Int>()
        if (isBatch) {
            cityAdCode = adCodes ?: arrayListOf()
            content = "删除" + cityName + "城市数据？"
        } else {
            if ("全省地图" == cityName) {
                val provinceInfo = mapDataBusiness.getProvinceInfo(adCode)
                if (provinceInfo != null) {
                    content = "删除" + provinceInfo.provName + "全省地图" + "地图数据吗？"
                    for (cityItemInfo in provinceInfo.cityInfoList) {
                        cityAdCode.add(cityItemInfo.cityAdcode)
                    }
                }
            } else {
                cityAdCode.add(adCode)
                val baseCountry = mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)
                if (adCode == baseCountry?.get(0)) {//基础包删除
                    content = "删除基础功能包数据后，将不\n能离线跨城导航"
                    confirm = getString(com.desaysv.psmap.base.R.string.sv_common_confirm)
                } else {
                    content = "删除" + cityName + "城市数据？"
                }
            }
        }
        viewModel.tipsAdCodes = cityAdCode
        dismissCustomOfflineDialog(deleteDialog) //dismiss 删除对话框
        deleteDialog = null
        deleteDialog = OfflineDataDialogFragment.builder().setTitle("").setContent(content)
            .doubleButton(confirm, getString(com.desaysv.psmap.base.R.string.sv_common_cancel)).setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.dataDeleteOperation(cityAdCode)
                }
                viewModel.tipsAdCodes = arrayListOf()
            }.apply {
                show(this@OfflineMapDownloadManageFragment.childFragmentManager, "deleteDialog")
            }
    }

    //暂停对话框
    private fun showPauseAskDialog(bundle: Bundle) {
        val isBatch = bundle.getBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
        val adCode = bundle.getInt(BaseConstant.OFFLINE_AD_CODE, 0)
        val adCodes = bundle.getIntegerArrayList(BaseConstant.OFFLINE_AD_CODES)
        var cityAdCode = ArrayList<Int>()
        if (isBatch) {
            cityAdCode = adCodes ?: arrayListOf()
        } else {
            cityAdCode.add(adCode)
        }
        viewModel.tipsAdCodes = cityAdCode
        dismissCustomOfflineDialog(pauseDialog) //dismiss 暂停对话框
        pauseDialog = null
        pauseDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("暂停下载提醒")
            .doubleButton("暂停下载", "取消下载").setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_CANCEL)
                } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_PAUSE)
                }
                viewModel.tipsAdCodes = arrayListOf()
            }.apply {
                show(this@OfflineMapDownloadManageFragment.childFragmentManager, "pauseDialog")
            }
    }

    //继续对话框
    private fun showContinueDialog(bundle: Bundle) {
        val isBatch = bundle.getBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
        val adCode = bundle.getInt(BaseConstant.OFFLINE_AD_CODE, 0)
        val adCodes = bundle.getIntegerArrayList(BaseConstant.OFFLINE_AD_CODES)
        var cityAdCode = ArrayList<Int>()
        if (isBatch) {
            cityAdCode = adCodes ?: arrayListOf()
        } else {
            cityAdCode.add(adCode)
        }
        viewModel.tipsAdCodes = cityAdCode
        dismissCustomOfflineDialog(continueDialog) //dismiss 继续对话框
        continueDialog = null
        continueDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("继续下载提醒")
            .doubleButton("继续下载", "取消下载").setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_CANCEL)
                } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_START)
                }
                viewModel.tipsAdCodes = arrayListOf()
            }.apply {
                show(this@OfflineMapDownloadManageFragment.childFragmentManager, "continueDialog")
            }
    }

    //更新对话框
    private fun showUpdateDialog(bundle: Bundle) {
        val isBatch = bundle.getBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
        val adCode = bundle.getInt(BaseConstant.OFFLINE_AD_CODE, 0)
        val adCodes = bundle.getIntegerArrayList(BaseConstant.OFFLINE_AD_CODES)
        var cityAdCode = ArrayList<Int>()
        if (isBatch) {
            cityAdCode = adCodes ?: arrayListOf()
        } else {
            cityAdCode.add(adCode)
        }
        viewModel.tipsAdCodes = cityAdCode
        dismissCustomOfflineDialog(updateDialog) //dismiss 继续对话框
        updateDialog = null
        updateDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("更新提醒")
            .doubleButton("更新", "删除数据").setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                    showDeleteDialog(bundle)
                } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_START)
                }
                viewModel.tipsAdCodes = arrayListOf()
            }.apply {
                show(this@OfflineMapDownloadManageFragment.childFragmentManager, "updateDialog")
            }
    }

    //dialog dismiss
    private fun dismissCustomOfflineDialog(dialog: OfflineDataDialogFragment?) {
        dialog?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
    }

    //解压时取消暂停对话框
    private fun tipsEnterUnzip(adCode: Int) {
        if (viewModel.tipsAdCodes.size == 1) {
            viewModel.tipsAdCodes.forEach {
                if (TextUtils.equals(it.toString(), adCode.toString())) {
                    dismissCustomOfflineDialog(pauseDialog) //dismiss 暂停对话框
                    pauseDialog = null
                }
            }
        }
    }

    private fun dismissCustomDialog() {
        showFlowDialog?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        showFlowDialog = null
    }

    /**
     * 动态添加子Fragment
     */
    private fun hideFragment(transaction: FragmentTransaction) {
        val fragments = childFragmentManager.fragments
        Timber.i(" --------->: hideFragment:%s", fragments.size)
        for (fragment1 in fragments) {
            transaction.hide(fragment1)
        }
    }

    //切换到城市列表Fragment
    private fun gotoOfflineMapDownloadingFragment() {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        hideFragment(transaction)
        if (offlineMapDownloadingFragment == null) {
            offlineMapDownloadingFragment = OfflineMapDownloadingFragment()
            transaction.add(R.id.fragment_container, offlineMapDownloadingFragment!!)
        } else {
            transaction.show(offlineMapDownloadingFragment!!)
        }
        transaction.commitAllowingStateLoss()
    }
}