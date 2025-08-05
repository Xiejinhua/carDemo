package com.desaysv.psmap.ui.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentInviteFriendsBinding
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.desaysv.psmap.model.impl.OnItemClickListener
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.utils.LoadingUtil
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.InviteJoinAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * 组队出行-邀请好友Fragment
 */
@AndroidEntryPoint
class InviteJoinTeamFragment : Fragment() {
    private lateinit var binding: FragmentInviteFriendsBinding
    private val viewModel by viewModels<InviteJoinViewModel>()

    private var dialog: CustomDialogFragment? = null
    private var inviteJoinAdapter: InviteJoinAdapter? = null
    private var teamNumber: String? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var userGroupBusiness: UserGroupBusiness

    @Inject
    lateinit var loadingUtil: LoadingUtil

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var gson: Gson

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInviteFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initData()
        viewModel.initData()
        initEventOperation()
    }

    override fun onResume() {
        super.onResume()
        Timber.i("onResume()")
        viewModel.setCarVisible(false)
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        loadingUtil.cancelLoading()
        dismissCustomDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        inviteJoinAdapter = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView()")
        viewModel.onHiddenChanged(true)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        viewModel.onHiddenChanged(hidden)
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
    }

    /**
     * 初始化数据
     */
    private fun initData() {
        teamNumber = arguments?.getString(Biz.TEAM_NUMBER, "") ?: ""
        inviteJoinAdapter = InviteJoinAdapter().also { binding.rycFriendsList.adapter = it }
//        viewModel.teamPassword.postValue(ResUtil.getString(R.string.sv_group_password_number, teamNumber))
    }

    private fun initEventOperation() {
        //退出邀请界面
        binding.tvBack.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.tvBack, CLICKED_SCALE_90)

        //重试按钮操作
        binding.skvRetry.setDebouncedOnClickListener {
            if (netWorkManager.isNetworkConnected()) {
                viewModel.getHistoryFriends()
            } else {
                toastUtil.showToast(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.skvRetry, CLICKED_SCALE_95)

        //发出邀请
        binding.stvTextEstablish.setDebouncedOnClickListener {
            if (netWorkManager.isNetworkConnected()) {
                val selectedMembers = inviteJoinAdapter?.getSelectedMembers()
                Timber.d("inviteJoinAdapter selectedMembers:${selectedMembers?.size}")
                if (!selectedMembers.isNullOrEmpty()) {
                    loadingUtil.cancelLoading()
                    loadingUtil.showLoading(getString(R.string.sv_group_invite_member_loading))
                    viewModel.inviteMembers(selectedMembers)
                }
            } else {
                toastUtil.showToast(com.desaysv.psmap.base.R.string.sv_common_network_anomaly_please_try_again)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.stvTextEstablish, CLICKED_SCALE_95)


        //更新好友列表
        viewModel.historyFriend.unPeek().observe(viewLifecycleOwner) { list ->
            if (list != null) {
                Timber.d("historyFriend:${gson.toJson(list)}  userInfoList:${gson.toJson(viewModel.userInfoList.value)} inviteJoinAdapter:${inviteJoinAdapter}")
                inviteJoinAdapter?.updateData(list)
            }
        }

        //设置发送邀请弹窗是否可点击
        inviteJoinAdapter?.setOnSelectionChangedListener(object :
            InviteJoinAdapter.OnSelectionChangedListener {
            override fun onSelectionChanged(isEmpty: Boolean) {
                Timber.d("onSelectionChanged isEmpty:$isEmpty")
                binding.stvTextEstablish.isEnabled = !isEmpty
            }
        })

        // 回退栈发生变化时的处理逻辑
        /*childFragmentManager.addOnBackStackChangedListener {
            val currentFragment = childFragmentManager.findFragmentById(R.id.inviteJoinTeamFragment)
            if (currentFragment is InviteJoinTeamFragment) {
                teamNumber = arguments?.getString(Biz.TEAM_NUMBER, "") ?: ""
                Timber.d("addOnBackStackChangedListener settingTab:$teamNumber")
                viewModel.getTeamInfo() //获取队伍信息
            }
        }*/

        viewModel.isSuccess.unPeek().observe(viewLifecycleOwner) {
            loadingUtil.cancelLoading()
            if (it) {
                dismissCustomDialog()
                dialog = CustomDialogFragment.builder()
                    .setTitle(getString(R.string.sv_group_text_invite_tips))
                    .setContent(getString(R.string.sv_group_text_invite_tips_content))
                    .singleButton(getString(com.desaysv.psmap.base.R.string.sv_common_got_it))
                    .setOnClickListener {
                        findNavController().navigateUp()
                    }.apply {
                        show(this@InviteJoinTeamFragment.childFragmentManager, "customDialog")
                    }
            } else {
                toastUtil.showToast(R.string.sv_group_send_invitation_error)
            }
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            viewModel.isNight.postValue(NightModeGlobal.isNightMode())
            inviteJoinAdapter?.notifyDataSetChanged()
        }

        //toast提示
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        //关闭界面
        viewModel.finishFragment.unPeek().observe(viewLifecycleOwner) {
            Timber.d("finishFragment")
            viewModel.backToMap()
        }

        //显示退出队伍弹条
        viewModel.userKickedType.unPeek().observe(viewLifecycleOwner) {
            Timber.i("userKickedType: $it")
            if (it == BaseConstant.GROUP_EXIT_USER_KICK_TYPE || it == BaseConstant.GROUP_EXIT_TEAM_DISBAND_TYPE) {
                dismissCustomDialog()
                dialog = CustomDialogFragment.builder().setTitle(
                    if (it == BaseConstant.GROUP_EXIT_USER_KICK_TYPE) resources.getString(com.autosdk.R.string.message_team_exit_tips)
                    else resources.getString(com.autosdk.R.string.message_team_dismiss_group_by_leader_tips)
                )
                    .setContent("(3)秒后离开此界面")
                    .singleButton(getString(com.desaysv.psmap.base.R.string.sv_common_got_it))
                    .setIsTeam(true).setCountDown(4000L).setOnClickListener {
                        viewModel.leaveTeam()
                        dismissCustomDialog()
                    }.apply {
                        show(this@InviteJoinTeamFragment.childFragmentManager, "customDialog")
                    }
            }
        }
    }

    private fun dismissCustomDialog() {
        dialog?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        dialog = null
    }
}
