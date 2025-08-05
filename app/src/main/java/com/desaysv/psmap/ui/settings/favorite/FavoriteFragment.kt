package com.desaysv.psmap.ui.settings.favorite

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.gbl.user.behavior.model.FavoriteType
import com.autonavi.gbl.user.behavior.model.SimpleFavoriteItem
import com.autonavi.gbl.util.errorcode.common.Service
import com.autosdk.bussiness.account.utils.ConverUtils
import com.autosdk.bussiness.common.POI
import com.autosdk.bussiness.navi.route.RouteRequestController
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.data.INaviRepository
import com.desaysv.psmap.base.utils.BaseConstant
import com.desaysv.psmap.base.utils.CommonUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentFavoriteBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.MapCommandType
import com.desaysv.psmap.model.business.JsonStandardProtocolManager
import com.desaysv.psmap.model.business.SettingAccountBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_93
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.CompanyHomeAdapter
import com.desaysv.psmap.ui.adapter.FavoriteNormalAdapter
import com.desaysv.psmap.ui.dialog.CustomDialogFragment
import com.desaysv.psmap.ui.settings.AccountAndSettingTab
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 收藏夹
 */
@AndroidEntryPoint
class FavoriteFragment : Fragment() {
    private lateinit var binding: FragmentFavoriteBinding
    private val viewModel by viewModels<FavoriteViewModel>()
    private var homeAdapter: CompanyHomeAdapter? = null
    private var companyAdapter: CompanyHomeAdapter? = null
    private var adapter: FavoriteNormalAdapter? = null
    private var isFirst = true
    private var toFavoriteType = BaseConstant.TO_FAVORITE_MAIN_TYPE

    private var lastMidpoi: POI? = null

    @Inject
    lateinit var mRouteRequestController: RouteRequestController

    @Inject
    lateinit var mINaviRepository: INaviRepository

    private var customDialogFragment: CustomDialogFragment? = null

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var settingAccountBusiness: SettingAccountBusiness

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var jsonStandardProtocolManager: JsonStandardProtocolManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        if (isFirst) {
            isFirst = false
            viewModel.initData(true)
        } else {
            viewModel.initData(false)
        }
        toFavoriteType = arguments?.getInt(Biz.TO_FAVORITE_TYPE, BaseConstant.TO_FAVORITE_MAIN_TYPE) ?: BaseConstant.TO_FAVORITE_MAIN_TYPE
        adapter?.setIsVia(toFavoriteType == BaseConstant.TO_FAVORITE_VIA_TYPE || toFavoriteType == BaseConstant.TO_FAVORITE_TEAM_DESTINATION)
        viewModel.isVia = (toFavoriteType == BaseConstant.TO_FAVORITE_VIA_TYPE|| toFavoriteType == BaseConstant.TO_FAVORITE_TEAM_DESTINATION)
        initEventOperation()
    }

    override fun onStop() {
        super.onStop()
        Timber.i("onStop()")
        dismissCustomDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.normalFavoriteList.closeMenu()//关闭左滑菜单
        binding.favoriteHomeList.closeMenu()//关闭左滑菜单
        binding.favoriteCompanyList.closeMenu()//关闭左滑菜单
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        viewModel.isLoading.postValue(true)
        adapter = FavoriteNormalAdapter().also { binding.normalFavoriteList.adapter = it }
        homeAdapter = CompanyHomeAdapter().also { binding.favoriteHomeList.adapter = it }
        companyAdapter = CompanyHomeAdapter().also { binding.favoriteCompanyList.adapter = it }
        binding.inputName.setClearDrawable(if (NightModeGlobal.isNightMode()) R.drawable.selector_ic_delete_circle_night else R.drawable.selector_ic_delete_circle_day)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initEventOperation() {
        //退出该界面
        binding.backTitle.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.backTitle, CLICKED_SCALE_90)

        //同步按钮操作
        binding.refresh.setDebouncedOnClickListener { viewModel.startSync() }
        ViewClickEffectUtils.addClickScale(binding.refresh, CLICKED_SCALE_90)

        //点击重命名界面的返回键，回到主页
        binding.backFavoriteHome.setDebouncedOnClickListener {
            viewModel.showChangeLayout.postValue(false)
        }
        ViewClickEffectUtils.addClickScale(binding.backFavoriteHome, CLICKED_SCALE_90)

        //名称输入框监听
        binding.inputName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                viewModel.toSetChangeName(s.toString().trim()) //名称输入
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding.inputName.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        //名称保存
        binding.save.setDebouncedOnClickListener {
            viewModel.submitFavoriteName()
        }
        ViewClickEffectUtils.addClickScale(binding.save, CLICKED_SCALE_93)

        //家一栏点击事件
        homeAdapter?.setItemClickListener(object : CompanyHomeAdapter.OnItemClickListener {
            override fun onItemClick(favoriteItem: SimpleFavoriteItem) { //整行点击
                binding.favoriteHomeList.closeMenu()
                if (!TextUtils.isEmpty(favoriteItem.item_id)) {
                    if (toFavoriteType == BaseConstant.TO_FAVORITE_MAIN_TYPE) {
                        viewModel.showPoiCard(favoriteItem) //显示POI详情
                    } else if (toFavoriteType == BaseConstant.TO_FAVORITE_VIA_TYPE) {
                        addViaPoi(ConverUtils.converSimpleFavoriteToPoi(favoriteItem))
                    } else if (toFavoriteType == BaseConstant.TO_FAVORITE_TEAM_DESTINATION) {
                        addDestination(ConverUtils.converSimpleFavoriteToPoi(favoriteItem))
                    }
                } else { //家未设置点击进入家地址编辑界面
                    toEditFavorite("家") //0 家编辑 1 公司编辑
                }
            }

            override fun onDeleteClick(favoriteItem: SimpleFavoriteItem) { //删除操作
                binding.favoriteHomeList.closeMenu()
                deleteFavoriteDialog(favoriteItem, -1, 1)
            }

            override fun onChangeClick() {
                binding.favoriteHomeList.closeMenu()//修改地址操作
                toEditFavorite("家") //0 家编辑 1 公司编辑
            }

            override fun onOpenMenuClick() { //手动打开左滑菜单
                binding.favoriteHomeList.openMenu()
            }
        })

        //公司一栏点击事件
        companyAdapter?.setItemClickListener(object : CompanyHomeAdapter.OnItemClickListener {
            override fun onItemClick(favoriteItem: SimpleFavoriteItem) { //整行点击
                binding.favoriteCompanyList.closeMenu()
                if (!TextUtils.isEmpty(favoriteItem.item_id)) {
                    if (toFavoriteType == BaseConstant.TO_FAVORITE_MAIN_TYPE) {
                        viewModel.showPoiCard(favoriteItem) //显示POI详情
                    } else if (toFavoriteType == BaseConstant.TO_FAVORITE_VIA_TYPE) {
                        addViaPoi(ConverUtils.converSimpleFavoriteToPoi(favoriteItem))
                    } else if (toFavoriteType == BaseConstant.TO_FAVORITE_TEAM_DESTINATION) {
                        addDestination(ConverUtils.converSimpleFavoriteToPoi(favoriteItem))
                    }
                } else { //公司未设置点击进入公司地址编辑界面
                    toEditFavorite("公司") //0 家编辑 1 公司编辑
                }
            }

            override fun onDeleteClick(favoriteItem: SimpleFavoriteItem) { //删除操作
                binding.favoriteCompanyList.closeMenu()
                deleteFavoriteDialog(favoriteItem, -1, 2)
            }

            override fun onChangeClick() {
                binding.favoriteCompanyList.closeMenu()//修改地址操作
                toEditFavorite("公司") //0 家编辑 1 公司编辑
            }

            override fun onOpenMenuClick() {
                binding.favoriteCompanyList.openMenu()
            }
        })

        //普通收藏点列表操作
        adapter?.setOnFavoriteItemListener(object : FavoriteNormalAdapter.FavoriteItemImpl {
            override fun onItemClick(position: Int) {
                binding.normalFavoriteList.closeMenu()//关闭左滑菜单
                if (viewModel.favoriteItems.isNotEmpty() && position != -1) {
                    if (toFavoriteType == BaseConstant.TO_FAVORITE_MAIN_TYPE) {
                        viewModel.showPoiCard(viewModel.favoriteItems[position]) //显示POI详情
                    } else if (toFavoriteType == BaseConstant.TO_FAVORITE_VIA_TYPE) {
                        addViaPoi(ConverUtils.converSimpleFavoriteToPoi(viewModel.favoriteItems[position]))
                    } else if (toFavoriteType == BaseConstant.TO_FAVORITE_TEAM_DESTINATION) {
                        addDestination(ConverUtils.converSimpleFavoriteToPoi(viewModel.favoriteItems[position]))
                    }
                } else {
                    Timber.d(" setOnFavoriteItemListener onItemClick list null or size 0")
                }
            }

            override fun onItemDeleteClick(position: Int) {
                binding.normalFavoriteList.closeMenu()//关闭左滑菜单
                if (viewModel.favoriteItems.isNotEmpty() && position != -1) {
                    val size: Int = viewModel.favoriteItems.size
                    if (position < size) {
                        Timber.d(" setOnFavoriteItemListener deleteFavoriteDialog position:$position")
                        deleteFavoriteDialog(viewModel.favoriteItems[position], position, 3)
                    } else {
                        Timber.d(" setOnFavoriteItemListener onItemDeleteClick, java.lang.IndexOutOfBoundsException: Index: $position, Size: $size")
                    }
                } else {
                    Timber.d(" setOnFavoriteItemListener onItemDeleteClick list null or size 0")
                }
            }

            override fun onItemTopClick(position: Int, toTop: Boolean) { //toTop true.置顶 false.取消置顶
                binding.normalFavoriteList.closeMenu()//关闭左滑菜单
                if (viewModel.favoriteItems.isNotEmpty() && position != -1) {
                    val result = viewModel.topFavorite(viewModel.favoriteItems[position], toTop)
                    if (result == Service.ErrorCodeOK) {
                        toastUtil.showToast(if (toTop) "已置顶" else "已取消置顶")
                        Timber.d("onItemTopClick refreshFavoriteList")
                        viewModel.refreshFavoriteList()
                    } else {
                        toastUtil.showToast(if (toTop) "置顶失败" else "取消置顶失败")
                    }
                } else {
                    Timber.d(" setOnFavoriteItemListener onItemTopClick list null or size 0")
                }
            }

            override fun onItemChangeClick(position: Int) {
                binding.normalFavoriteList.closeMenu()//关闭左滑菜单
                if (viewModel.favoriteItems.isNotEmpty() && position != -1) {
                    viewModel.showChangeLayout.postValue(true)
                    viewModel.changePosition = position
                    KeyboardUtil.showKeyboard(binding.inputName)
                    val customName = viewModel.favoriteItems[position].custom_name
                    val default = if (TextUtils.isEmpty(customName)) viewModel.favoriteItems[position].name else customName
                    viewModel.inputNameStr.postValue(default)
                    binding.inputName.text = Editable.Factory.getInstance().newEditable(default)
                } else {
                    Timber.d(" setOnFavoriteItemListener onItemChangeClick list null or size 0")
                }
            }

            override fun onItemAddViaClick(position: Int) {
                if (viewModel.favoriteItems.isNotEmpty() && position != -1) {
                    if (toFavoriteType == BaseConstant.TO_FAVORITE_VIA_TYPE) {
                        addViaPoi(ConverUtils.converSimpleFavoriteToPoi(viewModel.favoriteItems[position]))
                    } else if (toFavoriteType == BaseConstant.TO_FAVORITE_TEAM_DESTINATION) {
                        addDestination(ConverUtils.converSimpleFavoriteToPoi(viewModel.favoriteItems[position]))
                    }
                } else {
                    Timber.d(" setOnFavoriteItemListener onItemClick list null or size 0")
                }

            }
        })

        //进行登录
        viewModel.toLogin.observe(viewLifecycleOwner) {
            if (it) {
                viewModel.toLogin.postValue(false)
                if (CommonUtils.isVehicle() && CommonUtils.isUseVehicleAccount()) {
                    gotoLoginDialog()
                } else {
                    findNavController().navigate(R.id.to_loginFragment,
                        Bundle().also { bundle ->
                            bundle.putInt(
                                BaseConstant.ACCOUNT_SETTING_TAB,
                                AccountAndSettingTab.QR_LOGIN
                            )
                        })
                }
            }
        }

        //同步完毕布局操作
        viewModel.isRefresh.observe(viewLifecycleOwner) {
            if (!it) {
                binding.normalFavoriteList.scrollToPosition(0)
            }
        }

        settingAccountBusiness.favoritesUpdate.unPeek().observe(viewLifecycleOwner) {
            Timber.d("login success getFavoritesUpdate refreshFavoriteList")
            viewModel.refreshFavoriteList()
        }

        jsonStandardProtocolManager.favoritesUpdate.unPeek().observe(viewLifecycleOwner) {
            Timber.d("jsonStandardProtocolManager.favoritesUpdate")
            viewModel.refreshFavoriteList()
        }
        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }
        viewModel.setNaviToast.unPeek().observe(viewLifecycleOwner) {
            it?.let { nonNullString ->
                if (nonNullString.isNotEmpty()) {
                    toastUtil.showToast(nonNullString)
                }
            }
        }
        viewModel.onRefreshData.unPeek().observe(viewLifecycleOwner) {
            try {
                when (it) {//-3:家刷新-2:公司部刷新-1:全部刷新，其他是对应position刷新
                    -3 -> {
                        binding.favoriteHomeList.setSlide(!TextUtils.isEmpty(viewModel.homeItems[0].item_id))
                        homeAdapter?.onRefreshData(viewModel.homeItems, FavoriteType.FavoriteTypeHome)
                    }

                    -2 -> {
                        binding.favoriteCompanyList.setSlide(!TextUtils.isEmpty(viewModel.companyItems[0].item_id))
                        companyAdapter?.onRefreshData(viewModel.companyItems, FavoriteType.FavoriteTypeCompany)
                    }

                    -1 -> {
                        binding.favoriteHomeList.setSlide(!TextUtils.isEmpty(viewModel.homeItems[0].item_id))
                        binding.favoriteCompanyList.setSlide(!TextUtils.isEmpty(viewModel.companyItems[0].item_id))
                        homeAdapter?.onRefreshData(viewModel.homeItems, FavoriteType.FavoriteTypeHome)
                        companyAdapter?.onRefreshData(viewModel.companyItems, FavoriteType.FavoriteTypeCompany)
                        adapter?.onRefreshData(viewModel.favoriteItems)
                    }

                    else -> {
                        Timber.i("onRefreshData it:$it")
                        adapter?.removeAt(it)
                        viewModel.favoriteItems.removeAt(it)
                        if (viewModel.favoriteItems.isEmpty()) {
                            viewModel.hasCollection.postValue(0)
                        }
                        binding.normalFavoriteList.closeMenu()//关闭左滑菜单
                    }
                }
                viewModel.protocolFavoriteChangeExecute() //通知语音收藏点变化
            } catch (e: Exception) {
                Timber.e("onRefreshData Exception:${e.message}")
            }
        }

        //重命名成功刷新对应的item
        viewModel.updateFavorite.unPeek().observe(viewLifecycleOwner) {
            if (it != null && it < viewModel.favoriteItems.size) {
                adapter?.notifyItemChanged(it)
            }
        }

        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { integer: Int ->
            if (integer == BaseConstant.LOGIN_STATE_GUEST) {
                dismissCustomDialog()
            }
        }

        //判断关闭键盘
        viewModel.showChangeLayout.unPeek().observe(viewLifecycleOwner) {
            if (!it) {
                KeyboardUtil.hideKeyboard(binding.inputName)
            }
        }

        skyBoxBusiness.themeChange().unPeek().observe(viewLifecycleOwner) {
            viewModel.isNight.postValue(it)
            adapter?.notifyDataSetChanged()
            homeAdapter?.notifyDataSetChanged()
            companyAdapter?.notifyDataSetChanged()
            binding.inputName.setClearDrawable(if (it) R.drawable.selector_ic_delete_circle_night else R.drawable.selector_ic_delete_circle_day)
        }

        viewModel.isShowHome.observe(viewLifecycleOwner) {
            Timber.d("isShowHome it:$it")
            binding.favoriteHomeListSl.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.isShowCompany.observe(viewLifecycleOwner) {
            Timber.d("isShowCompany it:$it")
            binding.favoriteCompanyListSl.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModel.mapCommandBean.unPeek().observe(viewLifecycleOwner) { mapCommandBean ->
            Timber.d("mapCommandBean :$mapCommandBean")
            when (mapCommandBean.mapCommandType) {
                MapCommandType.PosRank -> {
                    adapter?.let { adapter ->
                        Timber.i("PosRank rank = ${mapCommandBean.pair} ")
                        val offset = mapCommandBean.pair?.second!!
                        if (adapter.data.size < offset) {
                            viewModel.notifyPosRankCommandResult(false, "当前只有${adapter.data.size}个搜索结果，请换个试试")
                        } else {
                            ConverUtils.converSimpleFavoriteToPoi(adapter.data[offset - 1])?.let { poi ->
                                val commandBean = CommandRequestRouteNaviBean.Builder().build(poi)
                                viewModel.planRoute(commandBean)
                                viewModel.notifyPosRankCommandResult(true, "即将为您导航到${poi.name}")
                            }
                        }
                    }
                }

                MapCommandType.PageRank -> {
                    Timber.i("PageRank rank = ${mapCommandBean.pair} ")
                    when (mapCommandBean.pair?.first) {
                        //下一页
                        "++" -> {
                            binding.data.smoothScrollBy(0, binding.data.height)
                            viewModel.notifyPageRankCommandResult(true, "")
                        }

                        //上一页
                        "--" -> {
                            binding.data.smoothScrollBy(0, -binding.data.height)
                            viewModel.notifyPageRankCommandResult(true, "")
                        }
                    }
                }

                else -> {}
            }
        }

    }

    //打开地址搜索界面
    private fun toEditFavorite(itemType: String) {
        when (itemType) {
            "家" -> {
                val commandBean = CommandRequestSearchBean.Builder().setType(CommandRequestSearchBean.Type.SEARCH_HOME).build()
                findNavController().navigate(R.id.to_searchAddHomeFragment, commandBean.toBundle())
            }

            "公司" -> {
                val commandBean = CommandRequestSearchBean.Builder().setType(CommandRequestSearchBean.Type.SEARCH_COMPANY).build()
                findNavController().navigate(R.id.to_searchAddHomeFragment, commandBean.toBundle())
            }
        }
        Timber.d(" toEditFavorite ")
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
                show(this@FavoriteFragment.childFragmentManager, "launchAccountAppDialog")
            }
    }

    //删除弹窗
    private fun deleteFavoriteDialog(favoriteItem: SimpleFavoriteItem, position: Int, type: Int) {
        dismissCustomDialog()
        customDialogFragment = CustomDialogFragment.builder().setTitle("")
            .setContent(if (type == 1) "确认删除家地址收藏吗？" else if (type == 2) "确认删除公司地址收藏吗？" else "确认删除这条收藏吗？")
            .doubleButton(
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_confirm),
                requireContext().getString(com.desaysv.psmap.base.R.string.sv_common_cancel)
            )
            .setOnClickListener {
                if (it) {
                    when (type) {
                        1 -> viewModel.getDeleteHomeFavorite(favoriteItem)
                        2 -> viewModel.getDeleteCompanyFavorite(favoriteItem)
                        else -> viewModel.cancelFavorite(favoriteItem, position, 3)
                    }
                }
            }.apply {
                show(this@FavoriteFragment.childFragmentManager, "deleteFavoriteDialog")
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

    fun addViaPoi(poi: POI?) {
        lastMidpoi = poi
        if (mINaviRepository.isRealNavi()) {
            poi?.let {
                val carRouteResult = mRouteRequestController.carRouteResult
                val endPoi = carRouteResult.toPOI
                val viaList = arrayListOf<POI>()
                if (carRouteResult?.hasMidPos() == true) {
                    if (checkViaPoi(poi, carRouteResult.midPois)) {
                        viaList.addAll(carRouteResult.midPois)
                    } else {
                        return@let
                    }
                }
                viaList.add(poi)
//                val commandBean = CommandRequestRouteNaviBean.Builder().buildMisPoi(endPoi, viaList)
//                findNavController().navigate(R.id.action_searchAlongWayResultFragment_to_naviFragment, commandBean.toBundle())
                viewModel.addWayPoint(poi)
            }
        } else {
            poi?.let {
                Timber.i("btSet newPoi = $poi")
                val carRouteResult = mRouteRequestController.carRouteResult
                val startPoi = carRouteResult.fromPOI
                val endPoi = carRouteResult.toPOI
                val viaList = arrayListOf<POI>()
                if (carRouteResult?.hasMidPos() == true) {
                    if (checkViaPoi(poi, carRouteResult.midPois)) {
                        viaList.addAll(carRouteResult.midPois)
                    } else {
                        return@let
                    }
                }
                viaList.add(poi)
//                val commandBean = CommandRequestRouteNaviBean.Builder().build(startPoi, endPoi, viaList)
                viewModel.addWayPointPlan(poi)
            }
        }

    }

    private fun checkViaPoi(addPOI: POI, mMidPois: ArrayList<POI>): Boolean {
        Timber.i("checkViaPoi() called with: addPOI = $addPOI, mMidPois = $mMidPois")
        var result: Boolean = true
        if (!mMidPois.isNullOrEmpty()) {
            if (mMidPois.size >= 15) {
                toastUtil.showToast(com.desaysv.psmap.base.R.string.sv_route_result_addmid_has_15)
                result = false
            } else {
                for (poi in mMidPois) {
                    Timber.i("checkViaPoi() called with: addPOI.id = ${addPOI?.id}, mMidPois = ${poi.id}")
                    if (addPOI.id != null && addPOI.id.equals(poi.id)) {
                        toastUtil.showToast(com.desaysv.psmap.base.R.string.sv_route_via_poi_add_fail)
                        result = false
                    }
                }
            }
        } else {
            result = true
        }
        return result
    }

    fun addDestination(poi: POI?) {
        poi?.let {
            viewModel.addDestination(poi)
            findNavController().popBackStack(R.id.searchAddTeamDestinationFragment, true)
        }
    }
}