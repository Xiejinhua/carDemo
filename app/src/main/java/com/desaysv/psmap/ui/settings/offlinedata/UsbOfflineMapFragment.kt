package com.desaysv.psmap.ui.settings.offlinedata

import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.util.errorcode.common.Service
import com.autonavi.gbl.util.errorcode.data.Common.ErrorCodeUsbIncompatibleData
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentUsbOfflineMapBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.system.exitProcess


/**
 * @author 王漫生
 * @description 数据罐装-U盘下载离线地图
 */
@AndroidEntryPoint
class UsbOfflineMapFragment : Fragment() {
    private lateinit var binding: FragmentUsbOfflineMapBinding
    private val viewModel by viewModels<UsbOfflineMapViewModel>()
    private var isFirstOpen = true
    private var customDialogFragment: CustomDialogFragment? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var mapDataBusiness: MapDataBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var sharePreferenceFactory: SharePreferenceFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsbOfflineMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        KeyboardUtil.hideKeyboard(view)
        intUsbMapData() //U盘下载初始化操作
    }

    //U盘下载初始化操作
    private fun intUsbMapData() {
        lifecycleScope.launch {
            if (mapDataBusiness.isInitSuccess()) {
                Timber.d(" initData 地图数据服务已经初始化")
                mapDataBusiness.registerMapDataObserver(viewModel.usbMapDataObserverImp)
                checkDataInDisk() //检测U盘和检测数据列表等操作
            } else {
                mapDataBusiness.initMapData() // 地图数据服务初始化
                if (mapDataBusiness.isInitSuccess()) {
                    Timber.d(" initData 地图数据服务初始化 成功")
                    mapDataBusiness.registerMapDataObserver(viewModel.usbMapDataObserverImp)
                    checkDataInDisk() //检测U盘和检测数据列表等操作
                } else {
                    Timber.d(" initData 地图数据服务初始化 失败")
                    viewModel.checkDataInDiskState.postValue(ErrorCodeUsbIncompatibleData)
                    viewModel.setCheckDataInDiskState(ErrorCodeUsbIncompatibleData)
                }
            }
        }
    }

    //检测U盘和检测数据列表等操作
    private fun checkDataInDisk() {
        var nResult: Int = mapDataBusiness.checkDataInDisk(DownLoadMode.DOWNLOAD_MODE_USB, BaseConstant.USB_ROOT_PATH)
        if (Service.ErrorCodeOK == nResult) {// 存在U盘数据
            nResult = mapDataBusiness.requestDataListCheck(DownLoadMode.DOWNLOAD_MODE_USB, BaseConstant.USB_ROOT_PATH)
            if (nResult == 0) {
                nResult = -100
            }
        }
        Timber.i("checkDataInDisk nResult:$nResult")
        viewModel.checkDataInDiskState.postValue(nResult)
        viewModel.setCheckDataInDiskState(nResult)
    }

    private fun initEventOperation() {
        //重启地图
        binding.restart.setDebouncedOnClickListener {
            sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.normal)
                .putBooleanValue(MapSharePreference.SharePreferenceKeyEnum.usbReStart, true)
            toastUtil.showToast("地图即将重启,壁纸主题模式下请手动打开地图")
            lifecycleScope.launch {
                delay(1000)
                Process.killProcess(Process.myPid())
                exitProcess(0)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.restart, CLICKED_SCALE_95)

        binding.cancel.setDebouncedOnClickListener {
            dismissCustomDialog()
            customDialogFragment = CustomDialogFragment.builder().setTitle("确定退出数据罐装？").setContent("退出后，将中断当前罐装")
                .doubleButton(
                    requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
                    requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
                )
                .setOnClickListener {
                    if (it) {
                        viewModel.stopUsbDownload()//停止U盘下载
                        findNavController().navigateUp()
                    }
                }.apply {
                    show(this@UsbOfflineMapFragment.childFragmentManager, "customDialog")
                }
        }
        ViewClickEffectUtils.addClickScale(binding.cancel, CLICKED_SCALE_95)

        //根据U盘数据状态判断加载状态或者是否有数据
        viewModel.checkDataInDiskState.observe(viewLifecycleOwner) {
            viewModel.loading.postValue(it == 0 || it == 1)
            if (it != 0) {
                viewModel.hasData.postValue(false)
            }
        }

        //U盘模式--检测数据列表回调，true表示有满足要求的数据，可以U盘下载了
        viewModel.usbRequestDataListCheckSuccess.unPeek().observe(viewLifecycleOwner) {
            if (it != true) {
                Timber.i("usbRequestDataListCheckSuccess fail")
                viewModel.checkDataInDiskState.postValue(ErrorCodeUsbIncompatibleData)
                viewModel.setCheckDataInDiskState(ErrorCodeUsbIncompatibleData)
            }
        }

        //空间不足提示
        viewModel.enough.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast("可用存储空间不足，无法继续下载，请空间清理完成后，手动点击继续下载")
        }

        //显示toast
        viewModel.showToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        //U盘挂载状态处理
        viewModel.usbMount.unPeek().observe(viewLifecycleOwner) {
            viewModel.defaultUsbState() //退出罐装界面，数据状态恢复默认值
            mapDataBusiness.unregisterMapDataObserver()
            mapDataBusiness.abortDataListCheck(DownLoadMode.DOWNLOAD_MODE_USB)
            viewModel.stopUsbDownload()//停止U盘下载
            intUsbMapData() //U盘下载初始化操作
        }
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
            mapDataBusiness.registerMapDataObserver(viewModel.usbMapDataObserverImp)
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView()")
        mapDataBusiness.unregisterMapDataObserver()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.defaultUsbState() //退出罐装界面，数据状态恢复默认值
        mapDataBusiness.unregisterMapDataObserver()
        mapDataBusiness.abortDataListCheck(DownLoadMode.DOWNLOAD_MODE_USB)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            mapDataBusiness.unregisterMapDataObserver()
        } else {
            mapDataBusiness.registerMapDataObserver(viewModel.usbMapDataObserverImp)
        }
    }

    private fun dismissCustomDialog() {
        customDialogFragment?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        customDialogFragment = null
    }
}