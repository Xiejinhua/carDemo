package com.desaysv.psmap.ui.settings.message

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autonavi.gbl.user.msgpush.model.AimPushMsg
import com.autonavi.gbl.user.msgpush.model.TeamPushMsg
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.NaviBusiness
import com.desaysv.psmap.base.business.PushMessageBusiness
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMessageBinding
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.BroadcastMessageAdapter
import com.desaysv.psmap.ui.adapter.MyMessageAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 我的消息
 */
@AndroidEntryPoint
class MessageFragment : Fragment() {
    private lateinit var binding: FragmentMessageBinding
    private val viewModel by viewModels<MessageViewModel>()
    private var isFirst = true
    private var myMessageAdapter: MyMessageAdapter? = null
    private var broadcastMessageAdapter: BroadcastMessageAdapter? = null

    private var customDialogFragment: CustomDialogFragment? = null
    private var lastTargetX = 0

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    @Inject
    lateinit var pushMessageBusiness: PushMessageBusiness

    @Inject
    lateinit var mNaviBusiness: NaviBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMessageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initLayout()//布局状态，需要打开时触发，若是其他界面退出，重新显示不需要重新设置
        initEventOperation()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView()")
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.myMessageListview.closeMenu()//关闭左滑
        binding.broadcastMessageListview.closeMenu()//关闭左滑
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.d(" hidden:%s", hidden)
        if (hidden) {
            dismissCustomDialog()
        }
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        Timber.d(" initBinding selectTab:%s", viewModel.selectTab.value )
        if (viewModel.selectTab.value == true){
            binding.layoutTab.check(R.id.rb_my_message)
        }
        myMessageAdapter = MyMessageAdapter().also { binding.myMessageListview.adapter = it }
        broadcastMessageAdapter = BroadcastMessageAdapter().also { binding.broadcastMessageListview.adapter = it }
    }

    //登录状态回调
    private fun loginLister(isLogin: Boolean) {
        Timber.d(" loginLister: $isLogin")
        if (isLogin) {
            initMessageData()
        } else {
            dismissCustomDialog()
        }
    }

    //布局状态，需要打开时触发，若是其他界面退出，重新显示不需要重新设置
    private fun initLayout() {
        if (isFirst) {
            isFirst = false
            viewModel.selectTab.postValue(true)
            viewModel.setEmptyMessageTip(requireContext().getString(R.string.sv_setting_message_tip))
            initMessageData()
        }
    }

    //获取消息列表
    private fun initMessageData() {
        viewModel.initMessageData()
        if (viewModel.send2carPushMessages.size > 0) {
            binding.myMessageListview.scrollToPosition(0)
        }
        if (viewModel.teamPushMsgMessages != null && viewModel.teamPushMsgMessages!!.size > 0) {
            binding.broadcastMessageListview.scrollToPosition(0)
        }
    }

    private fun initEventOperation() {
        //退出该界面
        binding.back.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.back, CLICKED_SCALE_90)

        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_my_message){
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
                                    R.id.rb_my_message -> {
                                        viewModel.selectTab.postValue(true)
                                        viewModel.setEmptyMessageTip(requireContext().getString(R.string.sv_setting_message_tip))
                                    }

                                    R.id.rb_broadcast_message -> {
                                        viewModel.selectTab.postValue(false)
                                        viewModel.setEmptyMessageTip(requireContext().getString(R.string.sv_setting_message_tip_1))
                                    }
                                }
                            }
                        })
                        .start()
                }
            }
        }

        viewModel.hasAimData.observe(viewLifecycleOwner) {
            myMessageAdapter?.onRefreshData(viewModel.send2carPushMessages)
        }

        viewModel.hasTeamData.observe(viewLifecycleOwner) {
            broadcastMessageAdapter?.onRefreshData(viewModel.teamPushMsgMessages)
        }

        //我的消息列表点击操作
        myMessageAdapter?.setItemClickListener(object : MyMessageAdapter.OnItemClickListener {
            override fun onItemClick(aimPushMsg: AimPushMsg) {
                binding.myMessageListview.closeMenu()//关闭左滑
                if (pushMessageBusiness.isPoiMsg(aimPushMsg)) {
                    viewModel.selectAimPoi(aimPushMsg.aimPoiMsg)
                    viewModel.showPoiCard(aimPushMsg.aimPoiMsg)
                } else if (pushMessageBusiness.isRouteMsg(aimPushMsg)) {
                    viewModel.selectAimRoute(aimPushMsg.aimRouteMsg)
                    viewModel.startRoute(aimPushMsg.aimRouteMsg)
                }
            }

            override fun onDeleteClick(aimPushMsg: AimPushMsg) {
                binding.myMessageListview.closeMenu()//关闭左滑
                viewModel.toDeleteAimPushMsg(aimPushMsg) //我的消息删除记录
            }

            override fun onCancelClick() {
                binding.myMessageListview.closeMenu()//关闭左滑
            }
        })

        //广播消息列表点击操作
        broadcastMessageAdapter?.setItemClickListener(object : BroadcastMessageAdapter.OnBroadcastItemClickListener {
            override fun onItemClick(teamPushMsg: TeamPushMsg) {
                binding.broadcastMessageListview.closeMenu()//关闭左滑
                viewModel.selectTeam(teamPushMsg)
                if (TextUtils.equals(teamPushMsg.content?.type, "INVITE")) {
                    if (TextUtils.isEmpty(viewModel.getTeamId())) {//不在组队中，加入
                        dismissCustomDialog()
                        customDialogFragment = CustomDialogFragment.builder().setTitle(teamPushMsg.title ?: "")
                            .setContent(teamPushMsg.text ?: "")
                            .singleButton("")
                            .doubleButton("立即加入", "暂不加入")
                            .setOnClickListener {
                                if (it) {
                                    viewModel.sendReqWsServiceTeamJoin(teamPushMsg.content?.teamNumber) //组队-加入队伍
                                }
                            }.apply {
                                show(this@MessageFragment.childFragmentManager, "customDialog")
                            }
                    } else if (TextUtils.equals(teamPushMsg.content.teamId, viewModel.getTeamId())) {//已经在邀请的队伍中
                        try {
                            if (mNaviBusiness.isSimulationNavi()){
                                toastUtil.showToast("模拟导航态下无法使用组队出行")
                            }else {
                                toastUtil.showToast("您已经在这个队伍中")
                                findNavController().navigate(
                                    R.id.to_myTeamFragment,
                                    Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_OTHER_TYPE) })
                            }
                        }catch (e: Exception){
                            Timber.e("setItemClickListener Exception:${e.message}")
                        }
                    } else {//已经在其他队伍中了 您已在一个队伍中，无法创建新队伍 点击【确定】进入当前队伍
                        try {
                            if (mNaviBusiness.isSimulationNavi()){
                                toastUtil.showToast("模拟导航态下无法使用组队出行")
                            }else {
                                dismissCustomDialog()
                                customDialogFragment = CustomDialogFragment.builder().setTitle("您已在一个队伍中，无法加入新队伍")
                                    .setContent("点击【确定】进入当前队伍")
                                    .singleButton(requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm))
                                    .setOnClickListener {
                                        findNavController().navigate(
                                            R.id.to_myTeamFragment,
                                            Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_OTHER_TYPE) })
                                    }.apply {
                                        show(this@MessageFragment.childFragmentManager, "hasTeamComeIn")
                                    }
                            }
                        }catch (e: Exception){
                            Timber.e("setItemClickListener 已经在其他队伍中了 Exception:${e.message}")
                        }
                    }
                }
            }

            override fun onDeleteClick(teamPushMsg: TeamPushMsg) {
                binding.broadcastMessageListview.closeMenu()//关闭左滑
                viewModel.toDeleteTeamPushMsg(teamPushMsg) //广播消息删除记录
            }

            override fun onCancelClick() {
                binding.broadcastMessageListview.closeMenu()//关闭左滑
            }
        })

        //刷新消息
        settingAccountBusiness.refreshMessage.unPeek().observe(viewLifecycleOwner) {
            if (!viewModel.isLogin()) { //未登录
                Timber.d(" refreshMessage 未登录")
            } else {
                viewModel.initMessageData()
            }
        }

        //打开登录弹框时加载二维码
        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) {
            loginLister(it == BaseConstant.LOGIN_STATE_SUCCESS)
        }

        //进入组队出行主界面
        viewModel.comeInTeam.unPeek().observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.to_myTeamFragment, Bundle().apply { putInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_OTHER_TYPE) })
        }

        //日夜模式切换时，布局动态调整
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            myMessageAdapter?.notifyDataSetChanged()
            broadcastMessageAdapter?.notifyDataSetChanged()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            Timber.d("isLoading:$it")
        }
        viewModel.hasAimData.observe(viewLifecycleOwner) {
            Timber.d("hasAimData:$it")
        }
        viewModel.hasTeamData.observe(viewLifecycleOwner) {
            Timber.d("hasTeamData:$it")
        }
        viewModel.selectTab.observe(viewLifecycleOwner) {
            Timber.d("selectTab:$it")
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