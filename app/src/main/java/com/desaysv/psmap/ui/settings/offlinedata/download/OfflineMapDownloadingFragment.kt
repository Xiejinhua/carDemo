package com.desaysv.psmap.ui.settings.offlinedata.download

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.autonavi.gbl.data.model.DownLoadMode
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING
import com.desaysv.psmap.base.business.MapDataBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentOfflineMapDownloadingBinding
import com.desaysv.psmap.model.bean.DownloadCityDataBean
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.DataDownLoadingManagerAdapter
import com.desaysv.psmap.ui.settings.offlinedata.OnMapDataItemClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 离线地图--下载中城市界面
 */
@AndroidEntryPoint
class OfflineMapDownloadingFragment : Fragment() {
    private lateinit var binding: FragmentOfflineMapDownloadingBinding
    private val viewModel by viewModels<OfflineMapDownloadingViewModel>()

    private var dataDownLoadingManagerAdapter: DataDownLoadingManagerAdapter? = null
    private var downloadingDataList = ArrayList<DownloadCityDataBean>()

    private var isAllPause = true
    private var isAllStart = true
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
        binding = FragmentOfflineMapDownloadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        updateDownLoadingData() //更新下载中列表数据，数据布局
        initEventOperation()
    }

    override fun onDestroy() {
        super.onDestroy()
        dataDownLoadingManagerAdapter = null
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        dataDownLoadingManagerAdapter = DataDownLoadingManagerAdapter(mapDataBusiness).also { binding.downloadingCitylist.adapter = it }
        dataDownLoadingManagerAdapter!!.setOnItemClickListener(onItemClickListener)
        if (isFirst) {
            isFirst = false
            viewModel.isLoading.postValue(true)
        }
    }

    private fun initEventOperation() {
        //更新数据
        viewModel.updateAllData.unPeek().observe(viewLifecycleOwner) {
            updateDownLoadingData() //更新下载中列表数据，数据布局
        }

        //更新下载状态
        viewModel.updateOperated.unPeek().observe(viewLifecycleOwner) {
            updateDownLoadingData() //更新下载中列表数据，数据布局
        }

        //更新下载Percent数据
        viewModel.updatePercent.unPeek().observe(viewLifecycleOwner) {
            updateDownLoadingData() //更新下载中列表数据，数据布局
        }

        //界面Hidden监听
        viewModel.onHiddenChanged.unPeek().observe(viewLifecycleOwner) {
            if (!it) {
                updateDownLoadingData() //更新下载中列表数据，数据布局
            }
        }

        //暂停所有下载任务
        binding.allPause.setDebouncedOnClickListener {
            viewModel.pauseAllTask()
        }
        ViewClickEffectUtils.addClickScale(binding.allPause, CLICKED_SCALE_95)

        //开始所有暂停任务
        binding.allDownload.setDebouncedOnClickListener {
            viewModel.startAllTask()
        }
        ViewClickEffectUtils.addClickScale(binding.allDownload, CLICKED_SCALE_95)

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            dataDownLoadingManagerAdapter?.notifyDataSetChanged()
            downloadingBtnLayout()//下载中顶部按钮状态
        }
    }

    //更新下载中列表数据，数据布局
    private fun updateDownLoadingData() {
        updateWokingDownLoad()
        updateDownLoadNowNumber()
    }

    private fun updateDownLoadNowNumber() {
        val workings = mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)
        if (workings!!.size > 0) {
            viewModel.hasData.postValue(true)

            isAllPause = true
            isAllStart = true
            for (i in workings.indices) {
                val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, workings[i])
                if (cityDownLoadItem != null) {
                    if (cityDownLoadItem.taskState != TASK_STATUS_CODE_PAUSE) {
                        isAllPause = false
                    }
                    if (cityDownLoadItem.taskState != TASK_STATUS_CODE_DOING && cityDownLoadItem.taskState != TASK_STATUS_CODE_WAITING) {
                        isAllStart = false
                    }
                }
            }
            downloadingBtnLayout() //下载中顶部按钮状态
        } else {
            viewModel.hasData.postValue(false)
        }
        viewModel.isLoading.postValue(false)
    }

    //下载中顶部按钮状态
    private fun downloadingBtnLayout() {
        viewModel.isAllStart.postValue(isAllStart)
        viewModel.isAllPause.postValue(isAllPause)
    }

    private fun updateWokingDownLoad() {
        val cityItemInfos: ArrayList<DownloadCityDataBean>? =
            viewModel.converNewDownLoadData(mapDataBusiness.getWorkingQueueAdCodeList(DownLoadMode.DOWNLOAD_MODE_NET)!!)
        cityItemInfos?.run {
            downloadingDataList.clear()
            downloadingDataList.addAll(cityItemInfos)
            dataDownLoadingManagerAdapter!!.setList(downloadingDataList)
        }
    }

    private var onItemClickListener: OnMapDataItemClickListener = object : OnMapDataItemClickListener {
        override fun onGroupClick(groupPosition: Int) {
            //不需要处理
        }

        override fun onItemClick(groupPosition: Int, childPosition: Int, arcode: Int) {
            if (childPosition == -100) { //省份一栏操作
                if (groupPosition > downloadingDataList.size - 1) {
                    return
                }
                val cityAdCode = downloadingDataList[groupPosition].arCodes
                val name = downloadingDataList[groupPosition].cityItemInfo.cityName
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
                val cityDownLoadItem = mapDataBusiness.getCityDownLoadItem(DownLoadMode.DOWNLOAD_MODE_NET, arcode) ?: return
                when (cityDownLoadItem.taskState) {
                    TASK_STATUS_CODE_SUCCESS -> {
                        if (groupPosition > downloadingDataList.size - 1) {
                            return
                        }
                        viewModel.showDeleteDialog(Bundle().apply {
                            putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                            putString(BaseConstant.OFFLINE_CITY_NAME, downloadingDataList[childPosition].cityItemInfo.cityName)
                            putInt(BaseConstant.OFFLINE_AD_CODE, arcode)
                            putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                        })
                    }

                    TASK_STATUS_CODE_DOING, TASK_STATUS_CODE_WAITING -> {
                        viewModel.showPauseAskDialog(Bundle().apply {
                            putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                            putInt(BaseConstant.OFFLINE_AD_CODE, arcode)
                            putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                        })
                    }

                    TASK_STATUS_CODE_PAUSE -> {
                        viewModel.showContinueDialog(Bundle().apply {
                            putBoolean(BaseConstant.OFFLINE_IS_BATCH, false)
                            putInt(BaseConstant.OFFLINE_AD_CODE, arcode)
                            putIntegerArrayList(BaseConstant.OFFLINE_AD_CODES, null)
                        })
                    }

                    else -> {
                        viewModel.dealListDownLoad(arcode)
                    }
                }
            }
        }

        override fun onStartClick(groupPosition: Int, childPosition: Int, arCode: Int) {
            viewModel.dealListDownLoad(arCode)
        }
    }
}