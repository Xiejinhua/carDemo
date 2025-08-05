package com.desaysv.psmap.ui.settings

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.layer.LayerController
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentAccountCenterBinding
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * 个人中心主界面
 */
@AndroidEntryPoint
class AccountCenterFragment : Fragment(), NetWorkManager.NetWorkChangeListener {
    private lateinit var binding: FragmentAccountCenterBinding
    private val viewModel: AccountCenterViewModel by viewModels()
    private var isLogoutSuccess = false //true.退出账号成功回调，false.退出账号回到设置fragment
    private var customDialogFragment: CustomDialogFragment? = null

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var mINaviRepository: INaviRepository

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var userGroupBusiness: UserGroupBusiness

    @Inject
    lateinit var layerController: LayerController

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountCenterBinding.inflate(inflater, container, false)
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
        netWorkManager.addNetWorkChangeListener(this)
        viewModel.firstFailShowFootSummary() //进入个人中心判断释放登录和是否有网络，已经登录没有网络显示failShowFootSummary

        viewModel.initTeam()
    }

    private fun initEventOperation() {
        //头像点击进行登录
        binding.userImage.setDebouncedOnClickListener {
            val accountProfile = viewModel.getAccountProfile()
            if (accountProfile == null || accountProfile.uid.isNullOrEmpty()) {
                Timber.i("头像点击 账号未登录")
            } else { //退出登录
                toLogout()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.userImage, CLICKED_SCALE_95)
        binding.loginBtn.setDebouncedOnClickListener {
            toLogin() //判断登录
        }
        ViewClickEffectUtils.addClickScale(binding.loginBtn, CLICKED_SCALE_95)

        //点击【出行记录】跳转--我的行程
        binding.toSeeTripV.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_myTripFragment)
        }
        //我的行程按钮点击
        binding.rbTrip.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_myTripFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.rbTrip, CLICKED_SCALE_95)

        //微信互联
        binding.rbConnectWx.setDebouncedOnClickListener {
            if (viewModel.getAccountProfile()?.uid.isNullOrEmpty()) {
                toLogin() //判断登录
            } else {
                findNavController().navigate(R.id.to_wechatFragment)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.rbConnectWx, CLICKED_SCALE_95)

        //手车互联
        binding.rbConnectMobile.setDebouncedOnClickListener {
            if (viewModel.getAccountProfile()?.uid.isNullOrEmpty()) {
                if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
                    if (settingAccountBusiness.isLoggedIn()) {
                        findNavController().navigate(R.id.to_mobileInternetFragment)
                    } else {
                        try {
                            launchAccountAppDialog() //登录车机账号弹框弹窗
                        } catch (e: java.lang.Exception) {
                            Timber.d(" Exception:%s", e.message)
                            toastUtil.showToast(getString(R.string.sv_setting_failed_open_qrcode_vehicle_account))
                        }
                    }
                } else {
                    findNavController().navigate(R.id.to_mobileInternetFragment)
                }
            } else {
                findNavController().navigate(R.id.to_mobileInternetFragment)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.rbConnectMobile, CLICKED_SCALE_95)

        //我的消息
        binding.rbMessage.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_messageFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.rbMessage, CLICKED_SCALE_95)

        //组队出行
        binding.rbTeam.setDebouncedOnClickListener {
            if (mINaviRepository.isSimulationNavi()) {
                toastUtil.showToast("模拟导航态下无法使用组队出行")
            } else {
                if (!netWorkManager.isNetworkConnected()) {
                    toastUtil.showToast(getString(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again))
                } else if ((viewModel.teamInfo.value?.team_id ?: 0) == 0) {
                    findNavController().navigate(R.id.to_createTeamFragment)
                } else {
                    userGroupBusiness.aMapLayer?.setCarVisible(false)
                    userGroupBusiness.aMapLayer?.setFollowMode(mINaviRepository.isNavigating())
                    findNavController().navigate(
                        R.id.to_myTeamFragment,
                        Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_SETTING_TYPE) })
                }
            }
        }
        ViewClickEffectUtils.addClickScale(binding.rbTeam, CLICKED_SCALE_95)

        //我的车辆
        binding.rbNumber.setDebouncedOnClickListener {
            startSettingCar(BaseConstant.ACTION_VEHICLE_INFO) //跳转车牌设置页（跳转到车辆设置我的车页面）
        }
        ViewClickEffectUtils.addClickScale(binding.rbNumber, CLICKED_SCALE_95)

        //离线地图
        binding.rbOffline.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_offlineMapFragment)
        }
        ViewClickEffectUtils.addClickScale(binding.rbOffline, CLICKED_SCALE_95)

        //收藏夹
        binding.rbFavor.setDebouncedOnClickListener {
            findNavController().navigate(R.id.to_favoriteFragment,
                Bundle().apply { putInt(Biz.TO_FAVORITE_TYPE, BaseConstant.TO_FAVORITE_MAIN_TYPE) })
        }
        ViewClickEffectUtils.addClickScale(binding.rbFavor, CLICKED_SCALE_95)

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            view?.run { skyBoxBusiness.updateView(this, true) }
            if (viewModel != null) {
                viewModel.isNight.postValue(it)
                viewModel.defaultAvatar.value =
                    if (it) ContextCompat.getDrawable(requireContext(), com.desaysv.psmap.base.R.drawable.ic_default_avatar_night)
                    else ContextCompat.getDrawable(requireContext(), com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)
            }
        }

        viewModel.avatar.unPeek().observe(viewLifecycleOwner) {
            if (TextUtils.isEmpty(it)) {
                Timber.i("avatar defaultAvatar")
                viewModel.defaultAvatar.value =
                    if (NightModeGlobal.isNightMode()) ContextCompat.getDrawable(
                        requireContext(),
                        com.desaysv.psmap.base.R.drawable.ic_default_avatar_night
                    )
                    else ContextCompat.getDrawable(requireContext(), com.desaysv.psmap.base.R.drawable.ic_default_avatar_day)
            }
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { integer: Int ->
            if (integer == BaseConstant.LOGIN_STATE_GUEST) {
                if (viewModel.getAccountProfile()?.uid.isNullOrEmpty()) {
                    isLogoutSuccess = true //true.退出账号成功回调，false.退出账号回到设置fragment
                    Timber.d("已退出账号")
                    isLogoutSuccess = false //true.退出账号成功回调，false.退出账号回到设置fragment
                }
                try {
                    layerController.getAGroupLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).clearAllItems()
                    layerController.getUserBehaviorLayer(SurfaceViewID.SURFACE_VIEW_ID_MAIN).clearAllItems()
                } catch (e: Exception) {
                    Timber.d(" getLoginLoading Exception:%s", e.message)
                }
                dismissCustomDialog()
                viewModel.getVehicleNumberLimit() //获取车牌号数据
                viewModel.myMessageRed.postValue(false)
                viewModel.initStrategy()
            } else if (integer == BaseConstant.LOGIN_STATE_SUCCESS) {
                viewModel.getTeamUserStatus() //获取组队状态
                viewModel.requestFootSummary() //获取足迹信息
                viewModel.getVehicleNumberLimit() //获取车牌号数据
                viewModel.getMessageData() //获取消息红点
            }
        }

        //有新消息
        settingAccountBusiness.refreshMessage.unPeek().observe(viewLifecycleOwner) {
            if (!settingAccountBusiness.isLogin()) { //未登录
                Timber.d(" refreshMessage 未登录")
                viewModel.myMessageRed.postValue(false)
            } else {
                viewModel.myMessageRed.postValue(true)
            }
        }

        //通知系统车牌号变化
        viewModel.licensePlateChange.unPeek().observe(viewLifecycleOwner) {
            viewModel.getVehicleNumberLimit() //获取车牌号数据
        }
    }

    //退出登录弹窗
    private fun toLogout() {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle("确定退出登录?").setContent("退出后，将无法同步搜索记录")
            .doubleButton(
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            )
            .setOnClickListener {
                if (it) {
                    viewModel.signOut()
                }
            }.apply {
                show(this@AccountCenterFragment.childFragmentManager, "customDialog")
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
                show(this@AccountCenterFragment.childFragmentManager, "launchAccountAppDialog")
            }
    }

    private fun dismissCustomDialog() {
        Timber.i("dismissCustomDialog")
        customDialogFragment?.run {
            if (this.isAdded || this.isVisible)
                this.dismissAllowingStateLoss()
        }
        customDialogFragment = null
    }

    //判断登录
    private fun toLogin() {
        val accountProfile = viewModel.getAccountProfile()
        if (accountProfile == null || accountProfile.uid.isNullOrEmpty()) {
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
        } else {
            Timber.i("头像点击 账号已经登录")
        }
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
                launchAccountAppDialog() //登录车机账号弹框弹窗
            } catch (e: java.lang.Exception) {
                Timber.d(" Exception:%s", e.message)
                toastUtil.showToast(getString(R.string.sv_setting_failed_open_qrcode_vehicle_account))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getVehicleNumberLimit() //获取车牌号数据
        viewModel.getMessageData() //获取消息红点
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        netWorkManager.removeNetWorkChangeListener(this)
    }

    //监听网络变化
    override fun onNetWorkChangeListener(isNetworkConnected: Boolean) {
        viewModel.isNetworkConnected.postValue(isNetworkConnected)
        val accountProfile = viewModel.getAccountProfile()
        if (accountProfile == null || accountProfile.uid.isNullOrEmpty()) {
            Timber.i("onNetWorkChangeListener 未登录")
        } else if (isNetworkConnected) {
            viewModel.updateAvatar()//方便网络变化重新加载头像
            viewModel.requestFootSummary() //获取足迹信息
            viewModel.getTeamUserStatus() //获取组队状态
        }
    }

    /**
     * 跳转车牌设置页（跳转到车辆设置我的车页面）
     */
    private fun startSettingCar(settingAction: String) {
        try {
            if (CommonUtils.isVehicle()) {
                CommonUtils.startSettingCar(settingAction, requireContext())
            } else {
                findNavController().navigate(R.id.to_vehicleFragment)
            }
        } catch (e: Exception) {
            Timber.d("startSettingCar: Exception e: ${e.message}")
        }
    }
}