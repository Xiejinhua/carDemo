package com.desaysv.psmap.ui.settings.offlinedata.download

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.autonavi.gbl.data.model.AreaType
import com.autonavi.gbl.data.model.CityDownLoadItem
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.OperationType
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_READY
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CustomFileUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentOfflineMapDownloadedBinding
import com.desaysv.psmap.model.bean.DownloadCityDataBean
import com.desaysv.psmap.model.bean.TYPE_CITY
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.DataDownLoadedManagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 离线地图--已下载城市界面
 */
@AndroidEntryPoint
class OfflineMapDownloadedFragment : Fragment() {
    private lateinit var binding: FragmentOfflineMapDownloadedBinding
    private val viewModel by viewModels<OfflineMapDownloadedViewModel>()

    private var dataDownLoadedManagerAdapter: DataDownLoadedManagerAdapter? = null
    private var downloadedDataList = ArrayList<DownloadCityDataBean>()
    private var dataDownLoadedUpdateAdapter: DataDownLoadedManagerAdapter? = null
    private var downloadedUpdateDataList = ArrayList<DownloadCityDataBean>()

    private var isFirst = true

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
        binding = FragmentOfflineMapDownloadedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        updateDownLoaded() //更新已下载数据，刷新布局
        initEventOperation()
    }

    override fun onDestroy() {
        super.onDestroy()
        dataDownLoadedManagerAdapter = null
        dataDownLoadedUpdateAdapter = null
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        dataDownLoadedManagerAdapter = DataDownLoadedManagerAdapter(mapDataBusiness).also { binding.downloadedCitylist.adapter = it }
        dataDownLoadedManagerAdapter!!.setOnItemClickListener(onItemClickListener)

        dataDownLoadedUpdateAdapter = DataDownLoadedManagerAdapter(mapDataBusiness).also { binding.updateCitylist.adapter = it }
        dataDownLoadedUpdateAdapter!!.setOnItemClickListener(onItemClickListener)
        if (isFirst) {
            isFirst = false
            viewModel.isLoading.postValue(true)
        }
    }

    private fun initEventOperation() {
        //全部更新
        binding.offlineDataUpdateTip.setDebouncedOnClickListener {
            val cityAdCode: ArrayList<Int> = arrayListOf()
            for (downLoadItem in downloadedUpdateDataList) {
                cityAdCode.add(downLoadItem.cityItemInfo.cityAdcode)
            }
            viewModel.downLoadClick(cityAdCode, OperationType.OPERATION_TYPE_START)
        }
        ViewClickEffectUtils.addClickScale(binding.offlineDataUpdateTip)

        //更新数据
        viewModel.updateAllData.unPeek().observe(viewLifecycleOwner) {
            updateDownLoaded() //更新已下载数据，刷新布局
        }

        //更新下载状态
        viewModel.updateOperated.unPeek().observe(viewLifecycleOwner) {
            updateDownLoaded() //更新已下载数据，刷新布局
        }

        //更新下载Percent数据
        viewModel.updatePercent.unPeek().observe(viewLifecycleOwner) {
            updateDownLoaded() //更新已下载数据，刷新布局
        }

        //界面Hidden监听
        viewModel.onHiddenChanged.unPeek().observe(viewLifecycleOwner) {
            if (!it) {
                updateDownLoaded() //更新已下载数据，刷新布局
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            dataDownLoadedManagerAdapter?.notifyDataSetChanged()
            dataDownLoadedUpdateAdapter?.notifyDataSetChanged()
        }
    }

    //更新已下载数据，刷新布局
    private fun updateDownLoaded() {
        val downLoadedList = ArrayList<Int>()
        val downLoadedUpdateList = ArrayList<Int>()
        val downLoadedUpdateSizeList = ArrayList<CityDownLoadItem>()
        for (cityItemInfo in mapDataBusiness.getCityList()!!) {
            val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, cityItemInfo.cityAdcode)
            if (cityDownLoadItem != null && cityDownLoadItem.taskState == TASK_STATUS_CODE_SUCCESS) {
                downLoadedList.add(cityDownLoadItem.adcode)
            }
            if (cityDownLoadItem != null && cityDownLoadItem.bUpdate && cityDownLoadItem.taskState == TASK_STATUS_CODE_READY) {
                downLoadedUpdateList.add(cityDownLoadItem.adcode)
                downLoadedUpdateSizeList.add(cityDownLoadItem)
            }
        }
        //判断基础包是否下载
        val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(
            DownLoadMode.DOWNLOAD_MODE_NET,
            mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)?.get(0) ?: 0
        )
        if (cityDownLoadItem != null && cityDownLoadItem.taskState == TASK_STATUS_CODE_SUCCESS) {
            downLoadedList.add(0, mapDataBusiness.getAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET, AreaType.AREA_TYPE_COUNTRY)?.get(0) ?: 0)
        }
        if (cityDownLoadItem != null && cityDownLoadItem.bUpdate && cityDownLoadItem.taskState == TASK_STATUS_CODE_READY) {
            downLoadedUpdateList.add(cityDownLoadItem.adcode)
            downLoadedUpdateSizeList.add(cityDownLoadItem)
        }
        if (downLoadedList.size > 0) {
            Timber.d(" updateDownLoaded downLoadedList.size() > 0 ")
            viewModel.hasDownloadedData.postValue(true)
            val converResult: ArrayList<DownloadCityDataBean>? = viewModel.converNewDownLoadData(downLoadedList)
            if (converResult != null) {
                downloadedDataList.clear()
                downloadedDataList.addAll(converResult)
            }
            dataDownLoadedManagerAdapter!!.setIsUpdate(false)
            dataDownLoadedManagerAdapter!!.setList(downloadedDataList)
            var downloadedNum = 0
            for (item in downloadedDataList) {
                if (item.type == TYPE_CITY){
                    downloadedNum++
                }
            }
            viewModel.downloadedTip.postValue("以下" + downloadedNum + "个城市下载成功") //已下载提示 比如：以下2个城市下载成功
        } else {
            viewModel.hasDownloadedData.postValue(false)
        }

        if (downLoadedUpdateList.size > 0 && downLoadedUpdateSizeList.size > 0) {
            Timber.d(" updateDownLoaded downLoadedUpdateList.size() > 0 ")
            viewModel.hasUpdateData.postValue(true)
            val converResult: ArrayList<DownloadCityDataBean>? = viewModel.converNewDownLoadData(downLoadedUpdateList)
            if (converResult != null) {
                downloadedUpdateDataList.clear()
                downloadedUpdateDataList.addAll(converResult)
            }
            dataDownLoadedManagerAdapter!!.setIsUpdate(true)
            dataDownLoadedUpdateAdapter!!.setList(downloadedUpdateDataList)
            var value = 0L
            for (downLoadItem in downLoadedUpdateSizeList) {
                value += downLoadItem.nUnpackSize.toLong()
            }
            var downloadedUpdateNum = 0
            for (item in downloadedUpdateDataList) {
                if (item.type == TYPE_CITY){
                    downloadedUpdateNum++
                }
            }
            viewModel.updateTip.postValue("更新全部" + downloadedUpdateNum + "个城市 " + CustomFileUtils.formetFileSize(value)) //更新提示 比如：更新全部1个城市 2.8M
        } else {
            viewModel.hasUpdateData.postValue(false)
        }
        viewModel.isLoading.postValue(false)
    }

    private var onItemClickListener: DataDownLoadedManagerAdapter.OnMapDataItemClickListener =
        object : DataDownLoadedManagerAdapter.OnMapDataItemClickListener {
            override fun onItemClick(isProvince: Boolean, item: DownloadCityDataBean, isUpdate: Boolean) {
                val list = if (isUpdate) downloadedUpdateDataList else downloadedDataList
                if (isProvince) { //省份一栏操作
                    val cityAdCode = item.arCodes
                    val name = item.cityItemInfo.cityName
                    val text = if (TextUtils.equals(name, "直辖市")) {
                        "直辖市全部"
                    } else if (TextUtils.equals(name, "特别行政区")) {
                        "特别行政区全部"
                    } else {
                        name + "全省"
                    }
                    viewModel.showDeleteDialog(Bundle().apply {
                        putBoolean(BaseConstant.OFFLINE_IS_BATCH, true)
                        putString(BaseConstant.OFFLINE_CITY_NAME, text)
                        putInt(BaseConstant.OFFLINE_AD_CODE, -1)
                        putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, cityAdCode)
                    })
                } else {
                    val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, item.cityItemInfo.cityAdcode) ?: return
                    when (cityDownLoadItem.taskState) {
                        TASK_STATUS_CODE_SUCCESS -> {
                            viewModel.showDeleteDialog(Bundle().apply {
                                putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                                putString(BaseConstant.OFFLINE_CITY_NAME, item.cityItemInfo.cityName)
                                putInt(BaseConstant.OFFLINE_AD_CODE, item.cityItemInfo.cityAdcode)
                                putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                            })
                        }

                        TASK_STATUS_CODE_READY -> {
                            if (cityDownLoadItem.bUpdate) {
                                viewModel.showUpdateDialog(Bundle().apply {
                                    putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                                    putInt(BaseConstant.OFFLINE_AD_CODE, item.cityItemInfo.cityAdcode)
                                    putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                                })
                            } else {
                                viewModel.dealListDownLoad(item.cityItemInfo.cityAdcode)
                            }
                        }

                        TASK_STATUS_CODE_DOING, TASK_STATUS_CODE_WAITING -> {
                            viewModel.showPauseAskDialog(Bundle().apply {
                                putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                                putInt(BaseConstant.OFFLINE_AD_CODE, item.cityItemInfo.cityAdcode)
                                putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                            })
                        }

                        TASK_STATUS_CODE_PAUSE -> {
                            viewModel.showContinueDialog(Bundle().apply {
                                putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                                putInt(BaseConstant.OFFLINE_AD_CODE, item.cityItemInfo.cityAdcode)
                                putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                            })
                        }

                        else -> {
                            viewModel.dealListDownLoad(item.cityItemInfo.cityAdcode)
                        }
                    }
                }
            }

            override fun onStartClick(isProvince: Boolean, item: DownloadCityDataBean, isUpdate: Boolean) {
                viewModel.dealListDownLoad(item.cityItemInfo.cityAdcode)
            }
        }
}