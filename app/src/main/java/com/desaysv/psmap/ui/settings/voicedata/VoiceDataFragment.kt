package com.desaysv.psmap.ui.settings.voicedata

import android.annotation.SuppressLint
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.autonavi.gbl.data.model.DownLoadMode.DOWNLOAD_MODE_NET
import com.autonavi.gbl.data.model.OperationType
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_DOING
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_PAUSE
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_SUCCESS
import com.autonavi.gbl.data.model.TaskStatusCode.TASK_STATUS_CODE_WAITING
import com.autonavi.gbl.data.model.Voice
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.business.VoiceDataBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentVoiceDataBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.VoiceDataAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.dialog.OfflineDataDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * 导航语音
 */
@AndroidEntryPoint
class VoiceDataFragment : Fragment() {
    private lateinit var binding: FragmentVoiceDataBinding
    private val viewModel: VoiceDataViewModel by viewModels()
    private var adapter: VoiceDataAdapter? = null
    private var showFlowDialog: CustomDialogFragment? = null
    private var deleteDialog: OfflineDataDialogFragment? = null
    private var pauseDialog: OfflineDataDialogFragment? = null
    private var continueDialog: OfflineDataDialogFragment? = null
    private var isFirstOpen = true

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var voiceDataBusiness: VoiceDataBusiness

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var netWorkManager: NetWorkManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVoiceDataBinding.inflate(inflater, container, false)
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
        voiceDataBusiness.unregisterVoiceDataObserver()
    }

    override fun onResume() {
        super.onResume()
        if (isFirstOpen) {
            isFirstOpen = false
        } else {
            voiceDataBusiness.registerVoiceDataObserver()
        }
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
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceDataBusiness.unregisterVoiceDataObserver()
        voiceDataBusiness.todoAbortRequestDataImage()
        voiceDataBusiness.abortRequestDataListCheck(DOWNLOAD_MODE_NET)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        voiceDataBusiness.unregisterVoiceDataObserver()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            voiceDataBusiness.unregisterVoiceDataObserver()
        } else {
            voiceDataBusiness.registerVoiceDataObserver()
            voiceDataBusiness.getVoiceIdList() //获取voiceId集合并
        }
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        KeyboardUtil.hideKeyboard(view)
        adapter = VoiceDataAdapter().also { binding.voiceDataList.adapter = it }
    }

    private fun initData() {
        if (voiceDataBusiness.isInitSuccess()) {
            voiceDataBusiness.registerVoiceDataObserver()
            voiceDataBusiness.requestDataListCheck(DOWNLOAD_MODE_NET, "")
            if (!netWorkManager.isNetworkConnected()){
                voiceDataBusiness.getVoiceIdListJudeData() //获取voiceId集合并判断是否有数据
            }
        } else {
            toastUtil.showToast(requireContext().getString(R.string.sv_setting_service_init_fail))
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        //检测云端语音数据列表失败处理
        viewModel.dataListCheckResult.unPeek().observe(viewLifecycleOwner) {
            if (it == false) {
                voiceDataBusiness.getVoiceIdListJudeData() //获取voiceId集合并判断是否有数据
            }
        }

        //voice数据列表--用于界面更新
        viewModel.voiceList.observe(viewLifecycleOwner) {
            val nowUseVoice = viewModel.getNowUseVoice() ?: Voice() //正在使用的语音包
            viewModel.useVoice.postValue(nowUseVoice) //正在使用的语音包
            adapter?.onRefreshData(it, nowUseVoice)
        }

        //头像更新回调
        viewModel.updateImageResult.unPeek().observe(viewLifecycleOwner) {
            adapter?.notifyDataSetChanged()
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            adapter?.notifyDataSetChanged()
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
            showFlowDialog = CustomDialogFragment.builder().setTitle("流量提醒").setContent("当前为非Wi-Fi网络，下载导航语音包数据将产生流量费用，是否继续下载")
                .doubleButton("继续下载", getString(com.desaysv.psmap.base.R.string.sv_common_cancel)).setOnClickListener {
                    if (it) {
                        viewModel.downLoadConfirmFlow(arCodes, OperationType.OPERATION_TYPE_START)
                    }
                }.apply {
                    show(this@VoiceDataFragment.childFragmentManager, "showFlowDialog")
                }
        }

        //解压时取消暂停对话框
        viewModel.tipsEnterUnzip.unPeek().observe(viewLifecycleOwner) { voiceId -> tipsEnterUnzip(voiceId) }

        //更新数据
        viewModel.updateAllData.unPeek().observe(viewLifecycleOwner) {
            voiceDataBusiness.getVoiceIdList() //获取voiceId集合并
        }

        //更新下载状态
        viewModel.updateOperated.unPeek().observe(viewLifecycleOwner) {
            voiceDataBusiness.getVoiceIdList() //获取voiceId集合并
        }

        //更新下载Percent数据
        viewModel.updatePercent.unPeek().observe(viewLifecycleOwner) {
            voiceDataBusiness.getVoiceIdList() //获取voiceId集合并
        }

        //使用语音包操作
        viewModel.setUseVoice.unPeek().observe(viewLifecycleOwner) {
            todoSetVoice(it)
        }

        //下载，删除等操作
        adapter?.setVoiceDataItemClickListener(object : VoiceDataAdapter.OnVoiceDataItemClickListener {
            override fun onItemClick(voice: Voice) {
                if (voice.id == -1) {
                    toastUtil.showToast("默认语音不可删除")
                } else if (voice.taskState == TASK_STATUS_CODE_SUCCESS) {
                    showDeleteDialog(voice) //删除对话框
                } else if (voice.taskState == TASK_STATUS_CODE_DOING || voice.taskState == TASK_STATUS_CODE_WAITING) {
                    showPauseAskDialog(voice) //暂停对话框
                } else if (voice.taskState == TASK_STATUS_CODE_PAUSE) {
                    showContinueDialog(voice) //继续对话框
                } else {
                    viewModel.dealWithVoice(voice) //下载操作
                }
            }

            override fun onDownLoadClick(voice: Voice) {
                if (voice.id == -1) {
                    todoSetVoice(voice)
                } else {
                    viewModel.dealWithVoice(voice) //下载按钮操作
                }
            }
        })
    }

    private fun todoSetVoice(voice: Voice) {
        val saveLocal = viewModel.getNowUseVoice() //本地设置的语音包
        if (saveLocal!!.id == voice.id) {
            toastUtil.showToast(String.format(requireContext().getString(R.string.sv_setting_now_use_voice), voice.name))
        } else {
            val result = viewModel.todoSetVoice(voice)
            Timber.i("setUseVoice result:$result")
            if (result == Service.ErrorCodeOK) {
                toastUtil.showToast(String.format(requireContext().getString(R.string.sv_setting_set_voice_success), voice.name))
                viewModel.setSpeechVoiceId(voice.id)
                viewModel.useVoice.postValue(voice)
                voiceDataBusiness.getVoiceIdList() //获取voiceId集合并
                EventTrackingUtils.trackEvent(
                    EventTrackingUtils.EventName.Map_Set,
                    mapOf(
                        Pair(EventTrackingUtils.EventValueName.BroadcastSet, voice.name)
                    )
                )
            }
        }
    }

    //删除对话框
    private fun showDeleteDialog(voice: Voice) {
        val voiceIds = ArrayList<Int>()
        voiceIds.add(voice.id)
        viewModel.tipsVoiceIds = voiceIds
        dismissCustomOfflineDialog(deleteDialog) //dismiss 删除对话框
        deleteDialog = null
        deleteDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("确定删除" + voice.name + "导航语音包数据吗？")
            .doubleButton(getString(com.desaysv.psmap.base.R.string.sv_common_confirm), getString(com.desaysv.psmap.base.R.string.sv_common_cancel)).setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(voiceIds, OperationType.OPERATION_TYPE_DELETE)
                }
                viewModel.tipsVoiceIds = arrayListOf()
            }.apply {
                show(this@VoiceDataFragment.childFragmentManager, "deleteDialog")
            }
    }

    //暂停对话框
    private fun showPauseAskDialog(voice: Voice) {
        val voiceIds = ArrayList<Int>()
        voiceIds.add(voice.id)
        viewModel.tipsVoiceIds = voiceIds
        dismissCustomOfflineDialog(pauseDialog) //dismiss 暂停对话框
        pauseDialog = null
        pauseDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("取消下载${voice.name}语音？")
            .doubleButton("确定", "关闭").setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                    viewModel.downLoadClick(voiceIds, OperationType.OPERATION_TYPE_START)
                } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(voiceIds, OperationType.OPERATION_TYPE_CANCEL)
                }
                viewModel.tipsVoiceIds = arrayListOf()
            }.apply {
                show(this@VoiceDataFragment.childFragmentManager, "pauseDialog")
            }
    }

    //继续对话框
    private fun showContinueDialog(voice: Voice) {
        val voiceIds = ArrayList<Int>()
        voiceIds.add(voice.id)
        viewModel.tipsVoiceIds = voiceIds
        dismissCustomOfflineDialog(continueDialog) //dismiss 继续对话框
        continueDialog = null
        continueDialog = OfflineDataDialogFragment.builder().setTitle("").setContent("继续下载${voice.name}语音？")
            .doubleButton("确定", "关闭").setOnClickListener {
                if (it == BaseConstant.OFFLINE_DIALOG_STATE_CANCEL) {//200.取消并关闭dialog 100.确定并关闭dialog 300.关闭dialog
                    viewModel.downLoadClick(voiceIds, OperationType.OPERATION_TYPE_PAUSE)
                } else if (it == BaseConstant.OFFLINE_DIALOG_STATE_CONFIRM) {
                    viewModel.downLoadClick(voiceIds, OperationType.OPERATION_TYPE_START)
                }
                viewModel.tipsVoiceIds = arrayListOf()
            }.apply {
                show(this@VoiceDataFragment.childFragmentManager, "continueDialog")
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
        if (viewModel.tipsVoiceIds.size == 1) {
            viewModel.tipsVoiceIds.forEach {
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
}