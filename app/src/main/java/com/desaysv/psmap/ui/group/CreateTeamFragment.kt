package com.desaysv.psmap.ui.group

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.R
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentCreateTeamBinding
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.utils.LoadingUtil
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 创建队伍
 */
@AndroidEntryPoint
class CreateTeamFragment : Fragment() {
    private lateinit var binding: FragmentCreateTeamBinding
    private val viewModel by viewModels<CreateTeamViewModel>()

    private var customDialogFragment: CustomDialogFragment? = null

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var loadingUtil: LoadingUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        viewModel.initData()
        initEventOperation()
        if (viewModel.isFirst.value == true) {
            viewModel.isFirst.postValue(false)
            viewModel.loginTip() //判断是否登录，未登录提示，只需要进来提示一次
        }
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        loadingUtil.cancelLoading(onCancelClick = { viewModel.abortRequest() })
        dismissDialog()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        initPasswordInputs()

        //退出该界面
        binding.back.setDebouncedOnClickListener {
            if (viewModel.showServiceOrInputOrder.value!! > 0) {
                viewModel.showServiceOrInputOrder.postValue(0)
            } else {
                findNavController().navigateUp()
            }
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        //创建队伍
        binding.stvTextEstablish.setDebouncedOnClickListener {
            Timber.i("stvTextEstablish isLoggedIn:${viewModel.isLoggedIn()}")
            if (viewModel.isLoggedIn()) {
                loadingUtil.cancelLoading()
                loadingUtil.showLoading("正在创建队伍，请稍等", onItemClick = { viewModel.abortRequest() })
                viewModel.createTeam()
            } else {
                gotoLogin()  //判断是否进行车机账号登录还是高德账号登录
            }
        }
        ViewClickEffectUtils.addClickScale(binding.stvTextEstablish, CLICKED_SCALE_95)

        //进入输入口令界面
        binding.stvTextJoin.setDebouncedOnClickListener {
            Timber.i("stvTextJoin isLoggedIn:${viewModel.isLoggedIn()}")
            if (viewModel.isLoggedIn()) {
                viewModel.showServiceOrInputOrder.postValue(2)
            } else {
                gotoLogin()  //判断是否进行车机账号登录还是高德账号登录
            }
        }
        ViewClickEffectUtils.addClickScale(binding.stvTextJoin, CLICKED_SCALE_95)

        //确定进入组队
        binding.joinTeam.setDebouncedOnClickListener {
            if (netWorkManager.isNetworkConnected()) {
                loadingUtil.cancelLoading()
                loadingUtil.showLoading("正在加入队伍，请稍等")
                viewModel.joinTeam(viewModel.inputOrder.value ?: "")
            } else {
                toastUtil.showToast(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.joinTeam, CLICKED_SCALE_95)

        //组队出行服务协议
        binding.groupProtocol.setDebouncedOnClickListener {
            viewModel.showServiceOrInputOrder.postValue(1)
        }

        //登录状态监听
        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { loginState ->
            if (loginState == BaseConstant.LOGIN_STATE_SUCCESS || loginState == BaseConstant.LOGOUT_STATE_LOADING) {

            } else {
                dismissDialog()
            }
        }

        //创建队伍界面进入组队出行主界面
        viewModel.teamInfo.unPeek().observe(viewLifecycleOwner) { info ->
            Timber.i("teamInfo:${info?.team_id}")
            if ((info?.team_id ?: 0) > 0) {
                findNavController().navigateUp()
                findNavController().navigate(R.id.to_myTeamFragment, Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_SETTING_TYPE) })
            }
        }

        //界面Hidden监听
        viewModel.onHiddenChanged.unPeek().observe(viewLifecycleOwner) {
            if (it) {
                dismissDialog()
            }
        }

        //toast显示
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }


        //已经在队伍中啦，弹框提示
        viewModel.responseStatusHasGroup.unPeek()
            .observe(viewLifecycleOwner) {//0.创建或加入队伍界面--获取组队状态--判断已经在队里了 1.创建或加入队伍界面--创建队伍--判断已经在队里了 2.创建或加入队伍界面--加入队伍--判断已经在队里了
                dismissDialog()
                customDialogFragment = CustomDialogFragment.builder()
                    .setTitle(if (it == 0) "您已在一个队伍中" else if (it == 1) "您已在一个队伍中，无法创建新队伍" else "您已在一个队伍中，无法加入新队伍")
                    .setContent("点击【确定】进入当前队伍")
                    .singleButton(requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm))
                    .doubleButton("", "")
                    .setOnClickListener {
                        viewModel.createJoinButHasGroup()  //创建或加入队伍发现已经再队伍中啦--后续进入组队界面
                    }.apply {
                        show(this@CreateTeamFragment.childFragmentManager, "responseStatusHasGroup")
                    }
            }

        //加入队伍结果
        /*viewModel.joinTeamResult.unPeek().observe(viewLifecycleOwner) {
            loadingUtil.cancelLoading()
            if (it) {
                toastUtil.showToast(getString(com.autosdk.R.string.agroup_join_success))
            } else {
                toastUtil.showToast(getString(com.autosdk.R.string.agroup_join_team_error))
            }
        }*/

        //创建队伍结果
        viewModel.createTeamResult.unPeek().observe(viewLifecycleOwner) {
            loadingUtil.cancelLoading()
            if (it) {
                toastUtil.showToast(getString(com.autosdk.R.string.agroup_create_team_success))
            } else {
                toastUtil.showToast(getString(com.autosdk.R.string.agroup_create_team_error))
            }
        }
    }

    //判断是否进行车机账号登录
    private fun gotoLogin() {
        try {
            settingAccountBusiness.launchAccountApp()  //登录车机账号弹框弹窗
        } catch (e: java.lang.Exception) {
            Timber.d(" Exception:%s", e.message)
            toastUtil.showToast(getString(R.string.sv_setting_failed_open_qrcode_vehicle_account))
        }
    }

    private fun dismissDialog() {
        customDialogFragment?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        customDialogFragment = null
    }

    private fun initPasswordInputs() {
        val editTexts = arrayOf(
            binding.etInput1, binding.etInput2, binding.etInput3,
            binding.etInput4, binding.etInput5, binding.etInput6
        )

        editTexts.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    Timber.i("afterTextChanged index:$index s:${s.toString()}")
                    if (s?.length == 1) {
                        if (index < editTexts.size - 1) {
                            editTexts[index + 1].requestFocus()
                        }
                    } else if (s?.isEmpty() == true) {
                        if (index > 0) {
                            editTexts[index - 1].requestFocus()
                        }
                    }
                    val password = editTexts.joinToString("") { it.text.toString() }
                    viewModel.inputOrder.postValue(password)
                }
            })
        }
    }
}