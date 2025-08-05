package com.desaysv.psmap.ui.group

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.Guideline
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.map.model.MapviewMode
import com.autonavi.gbl.map.model.MapviewModeParam
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.map.SurfaceViewID
import com.autosdk.common.utils.AutoGuideLineHelper
import com.autosdk.common.utils.ResUtil
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.base.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.component.UserComponent
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMyTeamBinding
import com.desaysv.psmap.model.business.UserGroupBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.JoinCallMemberAdapter
import com.desaysv.psmap.ui.adapter.MyTeamMemberAdapter
import com.desaysv.psmap.ui.adapter.RemoveMemberAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.utils.LoadingUtil
import com.google.gson.Gson
import com.txzing.sdk.bean.UserInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 组队出行主界面
 */
@AndroidEntryPoint
class MyTeamFragment : Fragment() {
    private lateinit var binding: FragmentMyTeamBinding
    private val viewModel by viewModels<MyTeamViewModel>()

    private var dialog: CustomDialogFragment? = null
    private var myTeamMemberAdapter: MyTeamMemberAdapter? = null
    private var joinCallMemberAdapter: JoinCallMemberAdapter? = null
    private var mRemoveMemberAdapter: RemoveMemberAdapter? = null
    private var toTeamType = BaseConstant.TO_TEAM_OTHER_TYPE

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var userGroupBusiness: UserGroupBusiness

    @Inject
    lateinit var loadingUtil: LoadingUtil

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var netWorkManager: NetWorkManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyTeamBinding.inflate(inflater, container, false)
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
        viewModel.setMessage(arguments?.getInt(Biz.TEAM_MY_MESSAGE, -1) ?: -1)
        toTeamType = arguments?.getInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_OTHER_TYPE) ?: BaseConstant.TO_TEAM_OTHER_TYPE
        viewModel.onResume()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop")
        loadingUtil.cancelLoading()
        dismissCustomDialog()
        startCallAnim(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch {
            Timber.i("onDestroyView()")
            if (findNavController().currentDestination?.id != com.desaysv.psmap.R.id.inviteJoinTeamFragment) {
                viewModel.onHiddenChanged(hidden = true, destroy = true)
            }
            UserComponent.getInstance().clearGroupBitmap()
            startCallAnim(false)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Timber.i(" onHiddenChanged ")
        if (!hidden) {
            viewModel.setMessage(arguments?.getInt(Biz.TEAM_MY_MESSAGE, -1) ?: -1)
            toTeamType = arguments?.getInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_OTHER_TYPE) ?: BaseConstant.TO_TEAM_OTHER_TYPE
        } else {
            dismissCustomDialog()
        }
        viewModel.onHiddenChanged(hidden, false)
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        val mapviewModeParam = MapviewModeParam()
        mapviewModeParam.bChangeCenter = true
        mapviewModeParam.mode = MapviewMode.MapviewModeNorth
        mapviewModeParam.mapZoomLevel = userGroupBusiness.mMapView?.operatorPosture?.zoomLevel!!
        viewModel.setMapMode(SurfaceViewID.SURFACE_VIEW_ID_MAIN, mapviewModeParam, false)

        binding.setNickname.setClearDrawable(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.R.drawable.selector_ic_delete_circle_night else com.desaysv.psmap.R.drawable.selector_ic_delete_circle_day)
    }

    private fun initData() {
        viewModel.setMessage(arguments?.getInt(Biz.TEAM_MY_MESSAGE, -1) ?: -1)
        toTeamType = arguments?.getInt(Biz.TO_TEAM_TYPE, BaseConstant.TO_TEAM_OTHER_TYPE) ?: BaseConstant.TO_TEAM_OTHER_TYPE
        myTeamMemberAdapter = MyTeamMemberAdapter().also { binding.skrPlayers.adapter = it }
        joinCallMemberAdapter = JoinCallMemberAdapter().also { binding.skrJoinCallPlayers.adapter = it }
        mRemoveMemberAdapter = RemoveMemberAdapter().also { binding.skrRemovePlayers.adapter = it }

        myTeamMemberAdapter?.setOnItemClickListener(object : MyTeamMemberAdapter.OnItemListener {
            override fun onItemClick(position: Int) {
                when (position) { //进入邀请界面
                    0 -> findNavController().navigate(
                        com.desaysv.psmap.R.id.to_inviteJoinTeamFragment,
                        Bundle().apply { putString(Biz.TEAM_NUMBER, viewModel.toGetTeamPassword()) })

                    else -> {
                        lifecycleScope.launch {
                            viewModel.setListDataFalse()
                            Timber.i(
                                "MyTeamMemberAdapter onItemClick position:$position getLastSelectPosition:${viewModel.getLastSelectPosition()} MembersFocusList size:${viewModel.getMembersFocusList().size} MembersFocusList${
                                    gson
                                        .toJson(viewModel.getMembersFocusList())
                                }"
                            )
                            if (myTeamMemberAdapter != null && viewModel.getLastSelectPosition() > -1 && viewModel.getLastSelectPosition() < viewModel.getMembersFocusList().size) {
                                //将所有item选中效果去除
                                myTeamMemberAdapter?.updateData(
                                    viewModel.getLastSelectPosition(),
                                    viewModel.getUserId(),
                                    viewModel.getLeader(),
                                    viewModel.getMembersFocusList()
                                )
                            }
                            Timber.i("MyTeamMemberAdapter onItemClick inviteUserInfoList:${gson.toJson(viewModel.inviteUserInfoList.value)}")
                            viewModel.onItemClick(viewModel.inviteUserInfoList.value?.get(position)?.user_id.toString(), position)
                        }
                    }
                }
            }

            override fun onTransferCaptainClick(userInfo: UserInfo) {
                createCustomDialog(
                    "",
                    ResUtil.getString(com.desaysv.psmap.R.string.sv_group_main_transfer_tips),
                    getString(R.string.sv_common_cancel),
                    getString(R.string.sv_common_confirm)
                )
                dialog?.setOnClickListener { isOk ->  //确定移除
                    if (isOk) {
                        viewModel.transferTeam(userInfo.user_id)
                        dismissCustomDialog()
                    }
                }
                dialog?.apply {
                    show(this@MyTeamFragment.childFragmentManager, "customDialog")
                }
            }

            override fun onRemoveMemberClick(userInfo: UserInfo) {
                createCustomDialog(
                    "",
                    ResUtil.getString(com.desaysv.psmap.R.string.sv_group_main_delete_tips),
                    getString(R.string.sv_common_cancel),
                    getString(R.string.sv_common_confirm)
                )
                dialog?.setOnClickListener { isOk ->  //确定移除
                    if (isOk) {
                        viewModel.kickTeam(userInfo)
                        dismissCustomDialog()
                    }
                }
                dialog?.apply {
                    show(this@MyTeamFragment.childFragmentManager, "customDialog")
                }
            }

            override fun onNickNameClick() {
                viewModel.nickNameTv()
            }

            override fun onRefreshHead(position: Int) {
                viewModel.onRefreshHead(position)
            }
        })

        joinCallMemberAdapter?.setOnItemClickListener(object : JoinCallMemberAdapter.OnItemListener {
            override fun onItemClick(position: Int) {
                lifecycleScope.launch {
//                    viewModel.setListDataFalse()
                    Timber.i(
                        "MyTeamMemberAdapter onItemClick position:$position getLastSelectPosition:${viewModel.getLastSelectPosition()} MembersFocusList size:${viewModel.getMembersFocusList().size} MembersFocusList${
                            gson
                                .toJson(viewModel.getMembersFocusList())
                        }"
                    )
                    /*if (joinCallMemberAdapter != null  && viewModel.getLastSelectPosition() > -1 && viewModel.getLastSelectPosition() < viewModel.getMembersFocusList().size) {
                        //将所有item选中效果去除
                        joinCallMemberAdapter?.updateData(
                            viewModel.getLastSelectPosition(),
                            viewModel.getUserId(),
                            viewModel.getLeader(),
                            viewModel.getMembersFocusList()
                        )
                    }*/
                    /*Timber.i("MyTeamMemberAdapter onItemClick inviteUserInfoList:${gson.toJson(viewModel.inviteUserInfoList.value)}")
                    viewModel.onItemClick(viewModel.inviteUserInfoList.value?.get(position)?.user_id.toString(), position)*/
                }
            }

            override fun onTransferCaptainClick(userInfo: UserInfo) {
                createCustomDialog(
                    "",
                    ResUtil.getString(com.desaysv.psmap.R.string.sv_group_main_transfer_tips),
                    getString(R.string.sv_common_cancel),
                    getString(R.string.sv_common_confirm)
                )
                dialog?.setOnClickListener { isOk ->  //确定移除
                    if (isOk) {
                        viewModel.transferTeam(userInfo.user_id)
                        dismissCustomDialog()
                    }
                }
                dialog?.apply {
                    show(this@MyTeamFragment.childFragmentManager, "customDialog")
                }
            }

            override fun onRemoveMemberClick(userInfo: UserInfo) {
                createCustomDialog(
                    "",
                    ResUtil.getString(com.desaysv.psmap.R.string.sv_group_main_delete_tips),
                    getString(R.string.sv_common_cancel),
                    getString(R.string.sv_common_confirm)
                )
                dialog?.setOnClickListener { isOk ->  //确定移除
                    if (isOk) {
                        viewModel.kickTeam(userInfo)
                        dismissCustomDialog()
                    }
                }
                dialog?.apply {
                    show(this@MyTeamFragment.childFragmentManager, "customDialog")
                }
            }

            override fun onNickNameClick() {
                viewModel.nickNameTv()
            }

            override fun onRefreshHead(position: Int) {
                viewModel.onRefreshHead(position)
            }

            override fun onUserForbidden(view: View, otherUserId: Int, isForbidden: Boolean) {
                Timber.i("onUserForbidden otherUserId:$otherUserId isForbidden:$isForbidden")
                if (viewModel.isLeader.value == true) {
                    viewModel.setForbidden(otherUserId, isForbidden)
//                    view.isSelected = isForbidden
                } else {
                    toastUtil.showToast(ResUtil.getString(com.autosdk.R.string.agroup_no_permission_forbidden))
                }
            }
        })

        mRemoveMemberAdapter?.setOnSelectionChangedListener(object : RemoveMemberAdapter.OnSelectionChangedListener {
            override fun onSelectionChanged(isEmpty: Boolean) {
                Timber.d("onSelectionChanged isEmpty:$isEmpty")
                binding.tvRemovePeople.isEnabled = !isEmpty
            }
        })

        binding.tvRemovePeople.setDebouncedOnClickListener {
            val selectedMembers = mRemoveMemberAdapter?.getSelectedMembers()
            Timber.d("removeMemberTv selectedMembers:${selectedMembers?.size}")
            if (!selectedMembers.isNullOrEmpty()) {
                createCustomDialog(
                    "",
                    ResUtil.getString(com.desaysv.psmap.R.string.sv_group_main_delete_tips),
                    getString(R.string.sv_common_cancel),
                    getString(R.string.sv_common_confirm)
                )
                dialog?.setOnClickListener { isOk ->  //确定移除
                    if (isOk) {
                        viewModel.kickTeam(selectedMembers)
                        dismissCustomDialog()
                    }
                }
                dialog?.apply {
                    show(this@MyTeamFragment.childFragmentManager, "customDialog")
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        //创建队伍
        binding.skiBack.setDebouncedOnClickListener {
            when (viewModel.getPageType()) {
                BaseConstant.GROUP_DEFAULT_TYPE -> {
                    Timber.d("finishFragment toTeamType$toTeamType")
                    finishFragment()
                }

                BaseConstant.GROUP_CHANGE_USERNAME_TYPE,
                BaseConstant.GROUP_START_CALL_CHANGE_USERNAME_TYPE -> {
                    KeyboardUtil.hideKeyboard(binding.setNickname)
                }
            }
            viewModel.doBack()
        }
        ViewClickEffectUtils.addClickScale(binding.skiBack, CLICKED_SCALE_90)

        //点击了 设置按钮
        binding.sivSetting.setDebouncedOnClickListener {
            viewModel.doSettingIcon()
        }
        ViewClickEffectUtils.addClickScale(binding.sivSetting, CLICKED_SCALE_90)

        //点击了 退出通话
        binding.sivStopCall.setDebouncedOnClickListener {
            viewModel.stopCall()
        }
        ViewClickEffectUtils.addClickScale(binding.sivStopCall, CLICKED_SCALE_90)

        //回到自身位置
        binding.btnBackCar.setDebouncedOnClickListener {
            viewModel.btnBackCar()
        }
        ViewClickEffectUtils.addClickScale(binding.btnBackCar, CLICKED_SCALE_95)

        //全览按钮
        binding.fullView.setDebouncedOnClickListener {
            viewModel.fullView()
        }
        ViewClickEffectUtils.addClickScale(binding.fullView, CLICKED_SCALE_95)

        //点击了 加入对讲
        binding.tvTeamPeople.setDebouncedOnClickListener {
            viewModel.joinCall()
        }
        ViewClickEffectUtils.addClickScale(binding.tvTeamPeople, CLICKED_SCALE_95)

        //点击了 按住说话
        binding.stvStartCall.setOnTouchListener { view, event ->
            Timber.d("stvStartCall onTouch event:$event isSelected:${binding.stvStartCall.isSelected}")
            if (!view.isSelected) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 按下时设置透明度和放大动画
                        ViewClickEffectUtils.animateScale(view, CLICKED_SCALE_95)
                        viewModel.onHoldButtonPressed()
                        true
                    }

                    MotionEvent.ACTION_UP -> {
                        // 抬起或取消时恢复透明度和缩小动画
                        ViewClickEffectUtils.animateScale(view, ViewClickEffectUtils.UNCLICKED_SCALE)
                        viewModel.onHoldButtonReleased()
                        true
                    }

                    else -> false
                }
            } else {
                false
            }
        }

        //设置昵称--点击确定按钮 确定修改昵称
        binding.tvOk.setDebouncedOnClickListener {
            KeyboardUtil.hideKeyboard(binding.setNickname)
            val nickname = viewModel.nicknameStr.value
            when {
                !netWorkManager.isNetworkConnected() -> {
                    toastUtil.showToast(resources.getString(R.string.sv_common_network_anomaly_please_try_again))
                }

                nickname.isNullOrBlank() -> {
                    toastUtil.showToast(resources.getString(com.desaysv.psmap.model.R.string.sv_group_main_set_nickname_tip_empty))
                }

                nickname == viewModel.nickname.value -> {
                    toastUtil.showToast(resources.getString(com.desaysv.psmap.model.R.string.sv_group_main_set_same_nickname))
                }

                else -> {
                    loadingUtil.cancelLoading()
                    loadingUtil.showLoading(getString(com.desaysv.psmap.R.string.sv_group_change_name_loading))
                    viewModel.modifyRemark(nickname)
                }
            }
        }
        ViewClickEffectUtils.addClickScale(binding.tvOk, CLICKED_SCALE_95)

        //点击退出组队
        /*binding.disbandTeamTv.setDebouncedOnClickListener {
            doDisbandTeam()
        }*/
        binding.ivDisbandTeamArrow.setDebouncedOnClickListener {
            doDisbandTeam()
        }
        ViewClickEffectUtils.addClickScale(binding.ivDisbandTeamArrow, CLICKED_SCALE_95)

        //点击移除组员
        /*binding.removeMemberTv.setDebouncedOnClickListener {
            viewModel.doRemoveMember()
        }*/
        binding.ivRemoveMemberArrow.setDebouncedOnClickListener {
            viewModel.doRemoveMember()
        }
        ViewClickEffectUtils.addClickScale(binding.ivRemoveMemberArrow, CLICKED_SCALE_95)

        //全员禁言
        binding.switchAllMute.setDebouncedOnClickListener {
            val isChecked = binding.switchAllMute.isChecked
            Timber.i("switchAllMute isChecked:$isChecked")
            // 处理开关状态变化逻辑
            viewModel.setTeamForbidden(isChecked)
        }

        //全员免打扰
        binding.switchSpeakerMute.setDebouncedOnClickListener {
            val isChecked = binding.switchSpeakerMute.isChecked
            Timber.i("switchSpeakerMute isChecked:$isChecked")
            // 处理开关状态变化逻辑
            viewModel.setSpeakerMute(isChecked)
        }

        //方控辅助
        binding.switchWheelAssist.setDebouncedOnClickListener {
            val isChecked = binding.switchWheelAssist.isChecked
            Timber.i("switchWheelAssist isChecked:$isChecked")
            // 处理开关状态变化逻辑
            viewModel.setWheelAssist(isChecked)
        }


        binding.skcSetDestination.setDebouncedOnClickListener {
            if (TextUtils.equals(viewModel.getUserId(), viewModel.getLeader())) {
                viewModel.clearAllItems() //退出组队界面--清空图层
                viewModel.setMapLeftTop() //移除移图操作观察者
//                setPunctuateYourself() //隐藏自己的地图队伍扎点
                //编辑目的地
                findNavController().navigate(com.desaysv.psmap.R.id.to_searchAddTeamDestinationFragment)
            }

        }
        ViewClickEffectUtils.addClickScale(binding.skcSetDestination)
        binding.skcSetDestinationNo.setDebouncedOnClickListener {
            if (TextUtils.equals(viewModel.getUserId(), viewModel.teamInfo.value?.master_user_id.toString())) {
                viewModel.clearAllItems() //退出组队界面--清空图层
                viewModel.setMapLeftTop() //移除移图操作观察者
//                setPunctuateYourself() //隐藏自己的地图队伍扎点
                //编辑目的地
                findNavController().navigate(com.desaysv.psmap.R.id.to_searchAddTeamDestinationFragment)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.skcSetDestinationNo)

        binding.llGoHere.setDebouncedOnClickListener {
            //去这里 目的地
            if (viewModel.getPoi().value == null) {
                return@setDebouncedOnClickListener;
            }
            viewModel.setMapLeftTop() //移除移图操作观察者
            setPunctuateYourself() //隐藏自己的地图队伍扎点
            viewModel.mPOI.value?.run { viewModel.startRoute(this) }
            finishFragment()
        }
        ViewClickEffectUtils.addClickScale(binding.llGoHere, CLICKED_SCALE_95)

        //全览
        viewModel.showPreview.unPeek().observe(viewLifecycleOwner) {
            viewModel.setRect(getMapPreviewRect())
            viewModel.showPreview()
        }

        //队长转让弹窗
        viewModel.teamTransfer.unPeek().observe(viewLifecycleOwner) {
            Timber.d("teamTransfer:$it")
            if (it.isNotEmpty()) {
                createCustomDialog(
                    resources.getString(com.desaysv.psmap.R.string.sv_group_main_transfer_new_leader),
                    resources.getString(com.desaysv.psmap.R.string.sv_group_main_transfer_old_leader, it),
                    "",
                    getString(R.string.sv_common_confirm),
                    true
                )
                dialog?.setOnClickListener { isOk ->
                    if (isOk) {
                        dismissCustomDialog()
                    }
                }
                dialog?.apply {
                    show(this@MyTeamFragment.childFragmentManager, "customDialog")
                }
            }
        }


        //显示队伍信息时，隐藏loading
        viewModel.mMemberShow.unPeek().observe(viewLifecycleOwner) {
            binding.loading.visibility = if (it) View.GONE else View.VISIBLE
            binding.loadingTip.visibility = if (it) View.GONE else View.VISIBLE

            binding.pbJoinCallLoading.visibility = if (it) View.GONE else View.VISIBLE
            binding.tvJoinCallLoadingTip.visibility = if (it) View.GONE else View.VISIBLE
        }

        viewModel.isLeader.unPeek().observe(viewLifecycleOwner) { aBoolean: Boolean ->
            viewModel.judeLeaderShowLayout(aBoolean)
        }

        viewModel.showCallMaskType.unPeek().observe(viewLifecycleOwner) { type ->
            Timber.d("showCallMaskType:$type")
            startCallAnim(type == 2)
        }

        //昵称输入监听
        binding.setNickname.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.setNickname(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.setNickname.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        viewModel.mPOI.unPeek().observe(viewLifecycleOwner) { poi: POI? ->
            viewModel.judeHasDestination(poi)
        }

        //退出账号监听
        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) {
            if (it == BaseConstant.LOGIN_STATE_SUCCESS || it == BaseConstant.LOGOUT_STATE_LOADING) {

            } else {
                Timber.d(" getLoginLoading integer: $it")
                dismissCustomDialog()
            }
        }

        //更新队伍中某个人信息
        viewModel.updateMembersData.unPeek().observe(viewLifecycleOwner) {
            if (myTeamMemberAdapter != null && it > -1 && it < viewModel.getMembersFocusList().size) {
                myTeamMemberAdapter?.updateData(it, viewModel.getUserId(), viewModel.getLeader(), viewModel.getMembersFocusList())
            }
        }

        /*
         //更新队伍列表信息
         viewModel.updateALLMembersData.unPeek().observe(viewLifecycleOwner) {
             if (myTeamMemberAdapter != null) {
                 myTeamMemberAdapter?.updateData(viewModel.getMembersList(), viewModel.getUserId(), it, viewModel.getMembersFocusList())
             }
         }*/

        viewModel.inviteUserInfoList.unPeek().observe(viewLifecycleOwner) {
            if (myTeamMemberAdapter != null) {
                myTeamMemberAdapter?.updateData(it, viewModel.getUserId(), viewModel.getLeader(), viewModel.getMembersFocusList())
            }
        }

        viewModel.joinCallList.unPeek().observe(viewLifecycleOwner) {
            if (joinCallMemberAdapter != null) {
                joinCallMemberAdapter?.updateData(
                    it,
                    viewModel.getUserId(),
                    viewModel.getLeader(),
                    viewModel.getMembersFocusList(),
                    viewModel.isAllSpeakerMute.value ?: false
                )
            }
        }

        //更新删除队友列表数据
        viewModel.removeUserInfoList.unPeek().observe(viewLifecycleOwner) { list ->
            Timber.i("removeUserInfoList:${gson.toJson(list)}")
            if (list != null) {
                mRemoveMemberAdapter?.updateData(list)
            }
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            viewModel.isNight.postValue(NightModeGlobal.isNightMode());
            view?.run { skyBoxBusiness.updateView(this, true) }
            if (myTeamMemberAdapter != null)
                myTeamMemberAdapter?.notifyDataSetChanged()
            if (joinCallMemberAdapter != null)
                joinCallMemberAdapter?.notifyDataSetChanged()
            if (mRemoveMemberAdapter != null)
                mRemoveMemberAdapter?.notifyDataSetChanged()
            binding.setNickname.setClearDrawable(if (it) com.desaysv.psmap.R.drawable.selector_ic_delete_circle_night else com.desaysv.psmap.R.drawable.selector_ic_delete_circle_day)
        }

        //toast提示
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        //关闭界面
        viewModel.finishFragment.unPeek().observe(viewLifecycleOwner) {
            Timber.d("finishFragment toTeamType$toTeamType")
            /*finishFragment()
            mapBusiness.backCurrentCarPosition(false)*/
            viewModel.backToMap()
        }

        //更新组队目的地
        viewModel.updateGroupDestination.unPeek().observe(viewLifecycleOwner) {
            Timber.d("updateGroupDestination $it")
            loadingUtil.cancelLoading()
            loadingUtil.showLoading(getString(com.desaysv.psmap.R.string.sv_group_set_team_destination))
        }

        //屏幕切屏操作
        viewModel.screenStatus.unPeek().observe(viewLifecycleOwner) {
            viewModel.setRect(getMapPreviewRect())
            viewModel.showPreview()
        }

        //组队设置目的地成功
        viewModel.updateGroupResponseResult.unPeek().observe(viewLifecycleOwner) {
            if (it) {
                Timber.i("组队设置目的地成功")
                loadingUtil.cancelLoading()
                if (viewModel.isLeader.value != true) {
                    toastUtil.showToast(ResUtil.getString(com.autosdk.R.string.agroup_destination_had_change))
                }
                viewModel.setRect(getMapPreviewRect())
                viewModel.showPreview()
            } else {
                Timber.i("设置队伍目的地失败")
                loadingUtil.cancelLoading()
                toastUtil.showToast(ResUtil.getString(com.autosdk.R.string.agroup_destination_fail))
            }
        }

        //修改昵称结果
        viewModel.modifyRemarkResult.unPeek().observe(viewLifecycleOwner) {
            if (it) {
                loadingUtil.cancelLoading()
                toastUtil.showToast(getString(com.desaysv.psmap.model.R.string.sv_group_change_username_success))
            } else {
                loadingUtil.cancelLoading()
                toastUtil.showToast(getString(com.desaysv.psmap.model.R.string.sv_group_change_username_error))
            }
        }

        viewModel.myTeamStr.unPeek().observe(viewLifecycleOwner) {
            try {
                Timber.i("myTeamStr: $it")
                if (TextUtils.isEmpty(it)) {
                    binding.sivSetting.visibility = View.GONE
                } else if (TextUtils.equals(it, "0")) {
                    binding.sivSetting.visibility = View.GONE
                } else {
                    binding.sivSetting.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                Timber.i("myTeamStr Exception:${e.message}")
            }
        }

        //显示退出队伍弹条
        viewModel.userKickedType.unPeek().observe(viewLifecycleOwner) {
            Timber.i("userKickedType: $it")
            if (it == BaseConstant.GROUP_EXIT_USER_KICK_TYPE || it == BaseConstant.GROUP_EXIT_TEAM_DISBAND_TYPE) {
                createCustomDialog(
                    if (it == BaseConstant.GROUP_EXIT_USER_KICK_TYPE) resources.getString(com.autosdk.R.string.message_team_exit_tips)
                    else resources.getString(com.autosdk.R.string.message_team_dismiss_group_by_leader_tips),
                    "(3)秒后离开此界面",
                    "",
                    getString(R.string.sv_common_got_it),
                    true,
                    time = 4000L
                )
                dialog?.setOnClickListener { isOk ->
                    if (isOk) {
                        viewModel.leaveTeam()
                        dismissCustomDialog()
                    }
                }
                dialog?.apply {
                    show(this@MyTeamFragment.childFragmentManager, "customDialog")
                }
            }
        }

        // 使用 MediatorLiveData 合并 myTeamStr 和 joinCallStr
        val combinedLiveData = androidx.lifecycle.MediatorLiveData<Pair<String?, String?>>()
        combinedLiveData.addSource(viewModel.myTeamStr.unPeek()) { myTeamStr ->
            combinedLiveData.value = Pair(myTeamStr, combinedLiveData.value?.second)
        }
        combinedLiveData.addSource(viewModel.joinCallStr.unPeek()) { joinCallStr ->
            combinedLiveData.value = Pair(combinedLiveData.value?.first, joinCallStr)
        }
        // 观察合并后的 LiveData
        combinedLiveData.observe(viewLifecycleOwner) { (myTeamStr, joinCallStr) ->
            try {
                Timber.i("myTeamStr: $myTeamStr, joinCallStr: $joinCallStr")
                val shouldShow =
                    !TextUtils.isEmpty(myTeamStr) && !TextUtils.equals(myTeamStr, "0") || !TextUtils.isEmpty(joinCallStr) && !TextUtils.equals(
                        joinCallStr,
                        "0"
                    )
                binding.sivSetting.visibility = if (shouldShow) View.VISIBLE else View.GONE
            } catch (e: Exception) {
                Timber.i("Combine LiveData Exception:${e.message}")
            }
        }

    }

    /**
     * 完成当前 Fragment 的操作并返回上一界面。
     * 在退出组队界面时，会调用 ViewModel 的方法将界面恢复为默认布局，
     * 确保下次进入组队界面时显示默认布局，然后通过导航控制器返回上一界面。
     */
    private fun finishFragment() {
        viewModel.defaultPage()  //退出组队界面时，回到默认布局，下次进入组队界面显示默认布局
        findNavController().navigateUp()
    }

    /**
     * 获取地图预览区域的矩形范围。
     * 该方法会根据界面的布局参数和屏幕状态，计算并返回地图预览区域的矩形坐标。
     *
     * @return 包含地图预览区域坐标的 [Rect] 对象。
     */
    private fun getMapPreviewRect(): Rect {
        val rect = Rect()
        val margin = ResUtil.getDimension(R.dimen.sv_dimen_24)
        val guideLine = binding.glVerticalLeft as Guideline
        rect.left =
            AutoGuideLineHelper.getCardWidthByGuideLine(guideLine) + (if (viewModel.screenStatus.value == true) ResUtil.getDimension(R.dimen.sv_dimen_500) else margin)
        rect.top = ResUtil.getDimension(R.dimen.sv_dimen_200) + margin
        rect.right = ResUtil.getDimension(R.dimen.sv_dimen_100) + margin
        rect.bottom = ResUtil.getDimension(R.dimen.sv_dimen_210)
        return rect
    }

    /**
     * 隐藏自己在地图上的队伍扎点。
     * 该方法通过调用 ViewModel 中的 `removeItem` 方法，移除地图上与自身相关的队伍扎点元素。
     */
    private fun setPunctuateYourself() {
        viewModel.removeItem()
    }

    /**
     * 关闭自定义对话框。
     * 该方法会检查对话框是否已添加到 FragmentManager 或是否可见，
     * 如果满足条件，则使用 `dismissAllowingStateLoss` 方法安全地关闭对话框，
     * 最后将对话框引用置为 null，避免内存泄漏。
     */
    private fun dismissCustomDialog() {
        dialog?.run {
            if (isAdded || isVisible) {
                dismissAllowingStateLoss()
            }
        }
        dialog = null
    }

    /**
     * 创建一个自定义对话框。
     * 在创建新对话框之前，会先关闭当前可能存在的对话框。
     *
     * @param title 对话框的标题文本。
     * @param content 对话框的内容文本。
     * @param cancel 取消按钮的文本。
     * @param yes 确认按钮的文本。
     */
    private fun createCustomDialog(
        title: String,
        content: String,
        cancel: String,
        yes: String,
        isSingleButton: Boolean = false,
        isMoreLine: Boolean = false,
        time: Long = 0L
    ) {
        dismissCustomDialog()
        dialog = if (time != 0L) {
            CustomDialogFragment.builder().setTitle(title).setContent(content).singleButton(yes).setIsTeam(true).setCountDown(4000)
        } else if (isSingleButton) {
            CustomDialogFragment.builder().setTitle(title).setContent(content).singleButton(yes).setMoreLine(isMoreLine)
        } else {
            CustomDialogFragment.builder().setTitle(title).setContent(content).doubleButton(yes, cancel).setMoreLine(isMoreLine)
        }
    }

    /**
     * 处理解散队伍或退出队伍的操作。
     * 根据当前用户是否为队长，显示不同的确认对话框。
     * 如果用户确认操作，则显示加载提示并调用相应的业务逻辑。
     */
    private fun doDisbandTeam() {
        if (viewModel.isLeader.value == true) {
            createCustomDialog(
                "",
                getString(com.autosdk.R.string.agroup_main_cancel_team) + "\n" + getString(com.desaysv.psmap.R.string.sv_group_main_cancel_team),
                getString(R.string.sv_common_cancel),
                getString(R.string.sv_common_confirm),
                isMoreLine = true
            )
        } else {
            createCustomDialog(
                "",
                getString(com.desaysv.psmap.R.string.sv_group_main_text_sure_team_quit) + "\n" + getString(com.desaysv.psmap.R.string.sv_group_main_text_sure_team_quit_content),
                getString(R.string.sv_common_cancel),
                getString(R.string.sv_common_confirm),
                isMoreLine = true
            )
        }
        dialog?.setOnClickListener { isOk ->  //确定退出登录:1
            if (isOk) {
//                loadingUtil.cancelLoading(onCancelClick = { userGroupBusiness.abortRequest() })
                loadingUtil.showLoading(
                    if (viewModel.isLeader.value == true) "正在解散队伍，请稍等" else "正在退出队伍，请稍等",
                    onItemClick = { /*userGroupBusiness.abortRequest()*/ })
                viewModel.quitOrDismiss()
                dismissCustomDialog()
            }
        }
        dialog?.apply {
            show(this@MyTeamFragment.childFragmentManager, "customDialog")
        }
    }

    /**
     * 启动或停止通话动画。
     *
     * 该方法根据传入的布尔值参数，控制 `sivStartCallMask` 对应的 `LayerDrawable` 中指定索引位置的 `AnimationDrawable` 动画的启动或停止。
     *
     * @param isStart 一个布尔值，`true` 表示启动动画，`false` 表示停止动画。
     */
    private fun startCallAnim(isStart: Boolean) {
        Timber.i("startCallAnim isStart: $isStart")
        val layerDrawable = binding.sivStartCallMask.background as LayerDrawable
        val animationDrawable = layerDrawable.getDrawable(1) as AnimationDrawable
        if (isStart) animationDrawable.start() else animationDrawable.stop()
    }
}