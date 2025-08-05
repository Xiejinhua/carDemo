package com.desaysv.psmap.ui.settings.trip

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.account.bean.TrackItemBean
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMyTripBinding
import com.desaysv.psmap.model.bean.MoreInfoBean
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.refresh.PullToRefreshListView
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.TripHistoryAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.settings.AccountAndSettingTab
import com.desaysv.psmap.ui.settings.view.PromptLanguagePopWindow
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 我的行程
 */
@AndroidEntryPoint
class MyTripFragment : Fragment() {
    private lateinit var binding: FragmentMyTripBinding
    private val viewModel: MyTripViewModel by viewModels()
    private var tripHistoryAdapter: TripHistoryAdapter? = null

    private var customDialogFragment: CustomDialogFragment? = null

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    @Inject
    lateinit var promptLanguagePopWindow: PromptLanguagePopWindow

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMyTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        promptLanguagePopWindow.cancelPromptLanguagePop()
        dismissCustomDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.list.closeMenu()//关闭左滑菜单
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.requestAccountProfilt()
        }
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        tripHistoryAdapter = TripHistoryAdapter().also { binding.list.adapter = it }
        binding.refreshLayout.setTotalPage(0)
        binding.refreshLayout.finishLoadMoreWithNoMoreData()
        binding.refreshLayout.setIsTrip(1)
        viewModel.initData()
        val autoRecord = viewModel.getAutoRecord()
        binding.autoRecordTripSwitch.setChecked(autoRecord == 0, false)
        viewModel.setShowMoreInfo(MoreInfoBean())
    }

    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            if (viewModel.showTripSetting.value == true) {
                viewModel.showTripSetting.postValue(false)
            } else {
                findNavController().navigateUp()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        //进入设置
        binding.tripSetting.setDebouncedOnClickListener {
            viewModel.showTripSetting.postValue(true)
        }
        ViewClickEffectUtils.addClickScale(binding.tripSetting, CLICKED_SCALE_90)

        //进入登录界面
        binding.loginText.setDebouncedOnClickListener {
            if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
                gotoLoginDialog()
            } else {
                findNavController().navigate(R.id.to_loginFragment,
                    Bundle().also {
                        it.putInt(
                            BaseConstant.ACCOUNT_SETTING_TAB,
                            AccountAndSettingTab.QR_LOGIN
                        )
                    })
            }
        }
        ViewClickEffectUtils.addClickScale(binding.loginText, CLICKED_SCALE_95)

        binding.refreshLayout.setOnRefreshLoadMoreListener(
            object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    if (netWorkManager.isNetworkConnected()) {
                        if (viewModel.loginState.value == true) {
                            viewModel.syncTrackHistory()
                        } else {
                            stopSyncRefresh()
                        }
                    } else {
                        toastUtil.showToast("无网络，请检查网络后重试")
                        stopSyncRefresh()
                    }
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    //不作处理
                }
            })

        //查看行程开关说明
        binding.autoRecordTripInfo.setDebouncedOnClickListener {
            viewModel.setShowMoreInfo(MoreInfoBean(requireContext().getString(com.autosdk.R.string.trace_setting_text_auto_matic_content), requireContext().getString(if (viewModel.autoRecordTripSwitch.value == 0) R.string.sv_setting_close_trip else R.string.sv_setting_open_trip)))
        }

        //全部清除说明
        binding.clearRecordTripInfo.setDebouncedOnClickListener {
            viewModel.setShowMoreInfo(MoreInfoBean(requireContext().getString(com.autosdk.R.string.trace_setting_text_clean_trace), requireContext().getString(R.string.sv_setting_will_clear_no_trip)))
        }

        //记录行程开关
        binding.autoRecordTripSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAutoRecord(if (isChecked) 0 else 1)
            if (!isChecked && viewModel.showTripSetting.value == true) {
                toastUtil.showToast(requireContext().resources.getString(R.string.sv_setting_close_no_trip))
            }
        }

        //全部清除按钮
        binding.clearRecordTripBtn.setDebouncedOnClickListener {
            clearRecordTripDialog() //清除行程弹窗
        }
        ViewClickEffectUtils.addClickScale(binding.clearRecordTripBtn, CLICKED_SCALE_90)

        viewModel.autoRecordTripSwitch.observe(viewLifecycleOwner) {
            switchViewSetDrawable(NightModeGlobal.isNightMode()) //设置 记录行程开关样式和Checked
        }

        //判断是否显示提示文言
        viewModel.showMoreInfo.observe(viewLifecycleOwner) {
            if (!TextUtils.isEmpty(it.content)) {
                promptLanguagePopWindow.showPromptLanguagePop(it)
            } else {
                promptLanguagePopWindow.cancelPromptLanguagePop()
            }
        }

        //列表点击--进入详情
        tripHistoryAdapter?.setOnTrackClickListener(object: TripHistoryAdapter.OnTrackClickListener{
            override fun onClick(item: TrackItemBean?) {
                binding.list.closeMenu()//关闭左滑菜单
                findNavController().navigate(R.id.to_myTripDetailFragment, Bundle().apply {
                    putSerializable(Biz.TO_TRIP_DETAIL_INFO, item) // 使用一个键来存储 TrackItemBean
                })
            }

            override fun onDeleteClick(item: TrackItemBean?) {
                binding.list.closeMenu()//关闭左滑菜单
                if (item != null && !TextUtils.isEmpty(item.id)){
                    clearRecordTripItemDialog(item.id, item)
                }
            }
        })

        //停止刷新
        viewModel.stopSyncRefresh.observe(viewLifecycleOwner) {
            stopSyncRefresh()
        }

        //同步更新列表
        viewModel.syncTrackHistory.unPeek().observe(viewLifecycleOwner) {
            viewModel.syncTrackHistory()
        }

        //更新数据
        viewModel.addNewData.unPeek().observe(viewLifecycleOwner) {
            tripHistoryAdapter?.onRefreshData(it)
        }

        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) {
            if (it == BaseConstant.LOGIN_STATE_SUCCESS) {
                viewModel.refreshTripData()
                viewModel.syncTrackHistory()
            } else if (it == BaseConstant.LOGIN_STATE_GUEST) {
                dismissCustomDialog()
            }
        }

        viewModel.showToast.observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            switchViewSetDrawable(it) //设置 记录行程开关样式和Checked
            binding.refreshLayout.setNight(it == true)
            tripHistoryAdapter?.notifyDataSetChanged()
        }
    }

    //设置 记录行程开关样式和Checked
    private fun switchViewSetDrawable(isNight: Boolean) {
        if (viewModel.autoRecordTripSwitch.value != null) {
            val isChecked = viewModel.autoRecordTripSwitch.value == 0
            Timber.i("setAutoRecord switchSetDrawable isNight:$isNight isChecked:$isChecked")
            binding.autoRecordTripSwitch.thumbDrawable = if (isNight) ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_switch_thumb_night
            ) else ContextCompat.getDrawable(requireContext(), R.drawable.bg_switch_thumb_day)
            binding.autoRecordTripSwitch.backDrawable = if (isNight) ContextCompat.getDrawable(
                requireContext(), R.drawable.bg_switch_track_night
            ) else ContextCompat.getDrawable(requireContext(), R.drawable.bg_switch_track_day)
            binding.autoRecordTripSwitch.isChecked = isChecked
        }
    }

    fun stopSyncRefresh() {
        binding.list.closeMenu()
        binding.refreshLayout.finishRefresh()
        binding.refreshLayout.finishLoadMore()
        binding.refreshLayout.finishLoadMoreWithNoMoreData()
        binding.refreshLayout.setIsTrip(1)
    }

    /**
     * 判断打开高德登录框还是车机个人中心登录框
     */
    private fun gotoLoginDialog() {
        if (settingAccountBusiness.isLoggedIn()) {
            findNavController().navigate(R.id.to_loginFragment,
                Bundle().also {
                    it.putInt(
                        BaseConstant.ACCOUNT_SETTING_TAB,
                        AccountAndSettingTab.QR_LOGIN
                    )
                })
        } else {
            try {
                launchAccountAppDialog()  //登录车机账号弹框弹窗
            } catch (e: java.lang.Exception) {
                Timber.d(" Exception:%s", e.message)
                toastUtil.showToast(getString(R.string.sv_setting_failed_open_qrcode_vehicle_account))
            }
        }
    }

    //登录车机账号弹框弹窗
    private fun launchAccountAppDialog() {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle("").setContent("请先登录车机账号")
            .doubleButton(
                requireContext().getString(com.autosdk.R.string.login_text_signin1),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            )
            .setOnClickListener {
                if (it) {
                    settingAccountBusiness.launchAccountApp()
                }
            }.apply {
                show(this@MyTripFragment.childFragmentManager, "launchAccountAppDialog")
            }
    }

    //清除行程弹窗
    private fun clearRecordTripDialog() {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle(requireContext().getString(R.string.sv_setting_sure_clear_no_trip))
            .setContent(requireContext().getString(R.string.sv_setting_clear_no_trip_forever))
            .doubleButton(
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            )
            .setOnClickListener {
                if (it) {
                    viewModel.clearTrackHistory()
                }
            }.apply {
                show(this@MyTripFragment.childFragmentManager, "clearRecordTripDialog")
            }
    }

    //删除该记录弹框
    private fun clearRecordTripItemDialog(id: String, item: TrackItemBean) {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle(requireContext().getString(R.string.sv_setting_sure_clear_this_trip)).setContent(
            requireContext().getString(
                R.string.sv_setting_clear_no_trip_forever
            )
        )
            .doubleButton(
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            )
            .setOnClickListener {
                if (it) {
                    if (viewModel.deleteById(id)){ //成功
                        tripHistoryAdapter?.remove(item)
                    } else {
                        Timber.i("clearRecordTripItemDialog fail")
                    }
                }
            }.apply {
                show(this@MyTripFragment.childFragmentManager, "clearRecordTripItemDialog")
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