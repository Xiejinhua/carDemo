package com.desaysv.psmap.ui.ahatrip

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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMyAhaFavoriteBinding
import com.desaysv.psmap.model.bean.MineGuideList
import com.desaysv.psmap.model.bean.TankCollectItem
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.AhaCuratedAdapter
import com.desaysv.psmap.ui.adapter.AhaGuideAdapter
import com.desaysv.psmap.ui.adapter.MineTankCollectAdapter
import com.desaysv.psmap.ui.search.view.CustomRefreshLayout
import com.example.aha_api_sdkd01.manger.models.LineListModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 我的路书收藏
 */
@AndroidEntryPoint
class MyAhaFavoriteFragment : Fragment() {
    private lateinit var binding: FragmentMyAhaFavoriteBinding
    private val viewModel: MyAhaFavoriteViewModel by viewModels()

    private var lastTargetX = 0
    private var isFirst = true
    private var isVisibility = true

    private var ahaGuideAdapter: AhaGuideAdapter? = null
    private var ahaCuratedAdapter: AhaCuratedAdapter? = null
    private var ahaTankAdapter: MineTankCollectAdapter? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toast: ToastUtil

    override fun onResume() {
        super.onResume()
        isVisibility = true
    }

    override fun onPause() {
        super.onPause()
        isVisibility = false
    }

    override fun onDestroy() {
        super.onDestroy()
        ahaGuideAdapter = null
        ahaCuratedAdapter = null
        ahaTankAdapter = null
        binding.guideList.closeMenu()//关闭左滑菜单
        binding.curatedList.closeMenu()//关闭左滑菜单
        binding.tankList.closeMenu()//关闭左滑菜单
        viewModel.deleteGuideCollectResult.unPeek().removeObserver(guideCollectResultOb)
        viewModel.deleteLineCollectResult.unPeek().removeObserver(lineCollectResultOb)
        viewModel.deleteTankCollectResult.unPeek().removeObserver(tankCollectResultOb)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            Timber.d(" onHiddenChanged selectTab:%s", viewModel.selectTab.value)
            when (viewModel.selectTab.value) {
                0 -> {
                    binding.layoutTab.check(R.id.rb_co_creation_trip)
                    viewModel.requestMineCollectGuideList() //我的-我制作的共创路书列表
                }
                1 -> {
                    Timber.d(" onHiddenChanged 精选")
                    viewModel.requestMineCollectList(1) //我的-我的收藏（路书(非共创路书)、轨迹、景点） 路书1、轨迹12、景点3
                }
                else -> {
                    Timber.d(" onHiddenChanged 轨迹")
                    viewModel.requestMineCollectList(12) //我的-我的收藏（路书(非共创路书)、轨迹、景点） 路书1、轨迹12、景点3
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyAhaFavoriteBinding.inflate(inflater, container, false)
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

        ahaGuideAdapter = AhaGuideAdapter().also { binding.guideList.adapter = it }
        ahaCuratedAdapter = AhaCuratedAdapter().also { binding.curatedList.adapter = it }
        ahaTankAdapter = MineTankCollectAdapter().also { binding.tankList.adapter = it }

        setupRefreshLayout(binding.guideRefreshLayout)
        setupRefreshLayout(binding.curatedRefreshLayout)
        setupRefreshLayout(binding.tankRefreshLayout)
        comeInRequestLineList() //进入界面加载详情数据
        KeyboardUtil.hideKeyboard(view)
    }

    private fun setupRefreshLayout(refreshLayout: CustomRefreshLayout){
        refreshLayout.setEnableRefresh(false)
        refreshLayout.setEnableAutoLoadMore(true)
        refreshLayout.setDisableContentWhenLoading(true)
        refreshLayout.setIsTrip(2)
    }

    //进入界面加载详情数据
    private fun comeInRequestLineList() {
        Timber.d(" initBinding selectTab:%s", viewModel.selectTab.value)
        if (viewModel.selectTab.value == 0) {
            binding.layoutTab.check(R.id.rb_co_creation_trip)
            if (isFirst) {
                isFirst = false
                binding.guideList.closeMenu()//关闭左滑菜单
                binding.guideRefreshLayout.resetNoMoreData()
                viewModel.guideCurrentPage = 1
                viewModel.guideAllList.postValue(arrayListOf())
                binding.guideList.scrollToPosition(0)
                viewModel.guideScrollToPosition.postValue(0)
                viewModel.requestMineCollectGuideList() //我的-我制作的共创路书列表
            } else {
                if (viewModel.guideAllList.value == null || viewModel.guideAllList.value?.isEmpty() == true) {
                    binding.guideList.closeMenu()//关闭左滑菜单
                    binding.guideRefreshLayout.resetNoMoreData()
                    viewModel.guideCurrentPage = 1
                    viewModel.guideAllList.postValue(arrayListOf())
                    binding.guideList.scrollToPosition(0)
                    viewModel.guideScrollToPosition.postValue(0)
                    viewModel.requestMineCollectGuideList() //我的-我制作的共创路书列表
                } else {
                    ahaGuideAdapter?.onRefreshData(viewModel.guideAllList.value, true)
                    val deleteGuideCollectResult = viewModel.deleteGuideCollectResult.value
                    Timber.i("deleteGuideCollectResult:$deleteGuideCollectResult")
                    if (!TextUtils.isEmpty(deleteGuideCollectResult)){
                        viewModel.deleteGuideCollectResult.postValue("")
                        ahaGuideAdapter?.data?.forEachIndexed { index, _ ->
                            if (TextUtils.equals(ahaGuideAdapter?.data?.get(index)?.id.toString(), deleteGuideCollectResult)) {
                                Timber.i("deleteGuideCollectResult removeAt")
                                viewModel.guideAllList.postValue(ahaGuideAdapter?.data as List<MineGuideList>)
                                ahaGuideAdapter?.data?.removeAt(index)
                                ahaGuideAdapter?.notifyItemRemoved(index)
                                return
                            }
                        }
                    }
                    binding.guideList.scrollToPosition(viewModel.guideScrollToPosition.value ?: 0)
                }
            }
        } else if (viewModel.selectTab.value == 1){
            Timber.d(" initBinding 精选")
            if (viewModel.lineAllList.value == null || viewModel.lineAllList.value?.isEmpty() == true) {
                binding.curatedList.closeMenu()//关闭左滑菜单
                binding.curatedRefreshLayout.resetNoMoreData()
                viewModel.lineCurrentPage = 1
                viewModel.lineAllList.postValue(arrayListOf())
                binding.curatedList.scrollToPosition(0)
                viewModel.lineScrollToPosition.postValue(0)
                viewModel.requestMineCollectList(1) //我的-我的收藏（路书(非共创路书)、轨迹、景点） 路书1、轨迹12、景点3
            } else {
                ahaCuratedAdapter?.onRefreshData(viewModel.lineAllList.value)
                val deleteLineCollectResult = viewModel.deleteLineCollectResult.value
                Timber.i("deleteLineCollectResult:$deleteLineCollectResult")
                if (!TextUtils.isEmpty(deleteLineCollectResult)){
                    viewModel.deleteLineCollectResult.postValue("")
                    ahaCuratedAdapter?.data?.forEachIndexed { index, _ ->
                        if (TextUtils.equals(ahaCuratedAdapter?.data?.get(index)?.id.toString(), deleteLineCollectResult)) {
                            Timber.i("deleteLineCollectResult removeAt")
                            viewModel.lineAllList.postValue(ahaCuratedAdapter?.data as List<LineListModel.DataDTO.ListDTO>)
                            ahaCuratedAdapter?.data?.removeAt(index)
                            ahaCuratedAdapter?.notifyItemRemoved(index)
                            return
                        }
                    }
                }
                binding.curatedList.scrollToPosition(viewModel.lineScrollToPosition.value ?: 0)
            }
        }else {
            Timber.d(" initBinding 轨迹")
            if (viewModel.tankAllList.value == null || viewModel.tankAllList.value?.isEmpty() == true) {
                binding.tankList.closeMenu()//关闭左滑菜单
                binding.tankRefreshLayout.resetNoMoreData()
                viewModel.tankCurrentPage = 1
                viewModel.tankAllList.postValue(arrayListOf())
                binding.tankList.scrollToPosition(0)
                viewModel.tankScrollToPosition.postValue(0)
                viewModel.requestMineCollectList(12) //我的-我的收藏（路书(非共创路书)、轨迹、景点） 路书1、轨迹12、景点3
            } else {
                ahaTankAdapter?.onRefreshData(viewModel.tankAllList.value, true)
                val deleteTankCollectResult = viewModel.deleteTankCollectResult.value
                Timber.i("deleteTankCollectResult:$deleteTankCollectResult")
                if (!TextUtils.isEmpty(deleteTankCollectResult)){
                    viewModel.deleteTankCollectResult.postValue("")
                    ahaTankAdapter?.data?.forEachIndexed { index, _ ->
                        if (TextUtils.equals(ahaTankAdapter?.data?.get(index)?.id.toString(), deleteTankCollectResult)) {
                            Timber.i("deleteTankCollectResult removeAt")
                            viewModel.tankAllList.postValue(ahaTankAdapter?.data as List<TankCollectItem>)
                            ahaTankAdapter?.data?.removeAt(index)
                            ahaTankAdapter?.notifyItemRemoved(index)
                            return
                        }
                    }
                }
                binding.tankList.scrollToPosition( viewModel.tankScrollToPosition.value ?: 0)
            }
        }
    }

    private fun initEventOperation() {
        //跳转到路书APP
        binding.more.setDebouncedOnClickListener {
            viewModel.goAhaHome()
        }

        binding.layoutTab.setOnCheckedChangeListener { group, checkedId ->
            lifecycleScope.launch {
                val checkedButton: RadioButton = group.findViewById(checkedId)
                // 计算指示条应该移动到的位置
                val targetX: Int =
                    checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_co_creation_trip) {
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
                                    R.id.rb_co_creation_trip -> {
                                        binding.guideRefreshLayout.resetNoMoreData()
                                        viewModel.guideCurrentPage = 1
                                        viewModel.guideAllList.postValue(arrayListOf())
                                        viewModel.selectTab.postValue(0)
                                        binding.guideList.scrollToPosition(0)
                                        viewModel.guideScrollToPosition.postValue(0)
                                        viewModel.requestMineCollectGuideList() //我的-我的收藏共创路书列表
                                    }

                                    R.id.rb_curated_trip -> {
                                        binding.curatedRefreshLayout.resetNoMoreData()
                                        viewModel.lineCurrentPage = 1
                                        viewModel.lineAllList.postValue(arrayListOf())
                                        viewModel.selectTab.postValue(1)
                                        binding.curatedList.scrollToPosition(0)
                                        viewModel.lineScrollToPosition.postValue(0)
                                        viewModel.requestMineCollectList(1) //我的-我的收藏（路书(非共创路书)、轨迹、景点） 路书1、轨迹12、景点3
                                    }

                                    R.id.rb_track_trip -> {
                                        binding.tankRefreshLayout.resetNoMoreData()
                                        viewModel.tankCurrentPage = 1
                                        viewModel.tankAllList.postValue(arrayListOf())
                                        viewModel.selectTab.postValue(2)
                                        binding.tankList.scrollToPosition(0)
                                        viewModel.tankScrollToPosition.postValue(0)
                                        viewModel.requestMineCollectList(12) //我的-我的收藏（路书(非共创路书)、轨迹、景点） 路书1、轨迹12、景点3
                                    }
                                }
                            }
                        })
                        .start()
                }
            }
        }

        //我的收藏--共创路书列表加载更多
        binding.guideRefreshLayout.setOnRefreshLoadMoreListener(
            object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    //不作处理
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    if (viewModel.guideCurrentPage < (viewModel.guideMaxPage.value ?: 1)) {
                        viewModel.guideCurrentPage += 1
                        viewModel.requestMineCollectGuideList() //我的-我的收藏共创路书列表
                    } else { //最后一页了
                        binding.guideRefreshLayout.finishLoadMore()//结束加载
                    }
                }
            })

        //我的收藏--精选路书列表加载更多
        binding.curatedRefreshLayout.setOnRefreshLoadMoreListener(
            object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    //不作处理
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    if (viewModel.lineCurrentPage < (viewModel.lineMaxPage.value ?: 1)) {
                        viewModel.lineCurrentPage += 1
                        viewModel.requestMineCollectList(1) //我的-我的收藏（路书(非共创路书)、轨迹、景点） 路书1、轨迹12、景点3
                    } else { //最后一页了
                        binding.curatedRefreshLayout.finishLoadMore()//结束加载
                    }
                }
            })

        //我的收藏--轨迹路书列表加载更多
        binding.tankRefreshLayout.setOnRefreshLoadMoreListener(
            object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    //不作处理
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    if (viewModel.tankCurrentPage < (viewModel.tankMaxPage.value ?: 1)) {
                        viewModel.tankCurrentPage += 1
                        viewModel.requestMineCollectList(12) //我的-我的收藏（路书(非共创路书)、轨迹、景点） 路书1、轨迹12、景点3
                    } else { //最后一页了
                        binding.tankRefreshLayout.finishLoadMore()//结束加载
                    }
                }
            })

        //共创路书列表点击
        ahaGuideAdapter?.setOnItemClickListener { _, _, position ->
            viewModel.setIsGuideFav(false)
            findNavController().navigate(R.id.to_myAhaTripDetailFragment, Bundle().apply {
                putInt("id", ahaGuideAdapter?.data?.get(position)?.id ?: 0)
                putInt("totalDay", ahaGuideAdapter?.data?.get(position)?.totalDay ?: 0)
                putInt("position", position)
                putBoolean("isMineFav", true)
            })
        }

        //取消收藏共创路书
        ahaGuideAdapter?.setOnGuideClickListener(object : AhaGuideAdapter.OnGuideClickListener {
            override fun onDeleteClick(item: MineGuideList?) {
                binding.guideList.closeMenu()//关闭左滑菜单
                viewModel.requestFavorite(item?.id.toString(), 4)
            }
        })

        //精选路书列表点击
        ahaCuratedAdapter?.setOnItemClickListener { _, _, position ->
            findNavController().navigate(R.id.to_ahaTripDetailFragment, Bundle().apply {
                putInt("id", ahaCuratedAdapter?.data?.get(position)?.id ?: 0)
                putInt("totalDay", ahaCuratedAdapter?.data?.get(position)?.totalDay ?: 0)
                putInt("position", position)
                putBoolean("isMineFav", true)
            })
        }

        //取消收藏精选共创路书
        ahaCuratedAdapter?.setOnCuratedClickListener(object : AhaCuratedAdapter.OnCuratedClickListener {
            override fun onDeleteClick(item: LineListModel.DataDTO.ListDTO?) {
                binding.curatedList.closeMenu()//关闭左滑菜单
                viewModel.requestFavorite(item?.id.toString(), 1)
            }
        })

        //轨迹路书列表点击
        ahaTankAdapter?.setOnItemClickListener { _, _, position ->
            viewModel.setIsLineFav(false)
            findNavController().navigate(R.id.to_myAhaTankDetailFragment, Bundle().apply {
                putInt("id", ahaTankAdapter?.data?.get(position)?.id ?: 0)
                putInt("position", position)
                putBoolean("isMineFav", true)
            })
        }

        //取消收藏轨迹路书
        ahaTankAdapter?.setOnTankClickListener(object : MineTankCollectAdapter.OnTankClickListener {
            override fun onDeleteClick(item: TankCollectItem?) {
                binding.tankList.closeMenu()//关闭左滑菜单
                viewModel.requestFavorite(item?.id.toString(), 12)
            }
        })

        //更新路书列表数据
        viewModel.guideCollectList.unPeek().observe(viewLifecycleOwner) {
            Timber.i("guideList:${it.size} guideCurrentPage:${viewModel.guideCurrentPage} guideMaxPage:${viewModel.guideMaxPage.value}")
            if (viewModel.guideCurrentPage == 1){
                ahaGuideAdapter?.onRefreshData(it, false)
                if (it.isNotEmpty()){
                    binding.guideList.closeMenu()
                    binding.guideList.scrollToPosition(0)
                    viewModel.guideScrollToPosition.postValue(0)
                    viewModel.guideAllList.postValue(it)
                } else {
                    viewModel.guideAllList.postValue(arrayListOf())
                }
            } else {
                if (it.isNotEmpty()){
                    binding.guideList.closeMenu()
                    val lastSize = ahaGuideAdapter?.data?.size ?: 0
                    val data = ahaGuideAdapter?.data ?: arrayListOf()
                    data.addAll(it)
                    ahaGuideAdapter?.onRefreshData(data as List<MineGuideList>, false)
                    binding.guideList.scrollToPosition(lastSize)
                    viewModel.guideScrollToPosition.postValue(lastSize)
                    viewModel.guideAllList.postValue(data as List<MineGuideList>)
                } else {
                    binding.guideList.closeMenu()
                    viewModel.guideCurrentPage -= 1
                    val size = ahaGuideAdapter?.data?.size ?: 0
                    val lastSize = if(size > 0 ) size - 1 else 0
                    binding.guideList.scrollToPosition(lastSize)
                    viewModel.guideScrollToPosition.postValue(lastSize)
                    viewModel.guideAllList.postValue(viewModel.guideAllList.value)
                }
            }
            if (viewModel.guideCurrentPage < (viewModel.guideMaxPage.value ?: 1)) {
                binding.guideRefreshLayout.finishLoadMore()
            } else { //最后一页了
                binding.guideRefreshLayout.finishLoadMore()
                binding.guideRefreshLayout.finishLoadMoreWithNoMoreData()
                binding.guideRefreshLayout.setIsTrip(2)
            }
        }

        //更新精选路书列表数据
        viewModel.lineCollectList.unPeek().observe(viewLifecycleOwner) {
            Timber.i("lineCollectList:${it.size} lineCurrentPage:${viewModel.lineCurrentPage} lineMaxPage:${viewModel.lineMaxPage.value}")
            if (viewModel.lineCurrentPage == 1){
                ahaCuratedAdapter?.onRefreshData(it)
                if (it.isNotEmpty()){
                    binding.curatedList.closeMenu()
                    binding.curatedList.scrollToPosition(0)
                    viewModel.lineScrollToPosition.postValue(0)
                    viewModel.lineAllList.postValue(it)
                } else {
                    viewModel.lineAllList.postValue(arrayListOf())
                }
            } else {
                if (it.isNotEmpty()){
                    binding.curatedList.closeMenu()
                    val lastSize = ahaCuratedAdapter?.data?.size ?: 0
                    val data = ahaCuratedAdapter?.data ?: arrayListOf()
                    data.addAll(it)
                    ahaCuratedAdapter?.onRefreshData(data as List<LineListModel.DataDTO.ListDTO>)
                    binding.curatedList.scrollToPosition(lastSize)
                    viewModel.lineScrollToPosition.postValue(lastSize)
                    viewModel.lineAllList.postValue(data as List<LineListModel.DataDTO.ListDTO>)
                } else {
                    binding.curatedList.closeMenu()
                    viewModel.lineCurrentPage -= 1
                    val size = ahaCuratedAdapter?.data?.size ?: 0
                    val lastSize = if(size > 0 ) size - 1 else 0
                    binding.curatedList.scrollToPosition(lastSize)
                    viewModel.lineScrollToPosition.postValue(lastSize)
                    viewModel.lineAllList.postValue(viewModel.lineAllList.value)
                }
            }
            if (viewModel.lineCurrentPage < (viewModel.lineMaxPage.value ?: 1)) {
                binding.curatedRefreshLayout.finishLoadMore()
            } else { //最后一页了
                binding.curatedRefreshLayout.finishLoadMore()
                binding.curatedRefreshLayout.finishLoadMoreWithNoMoreData()
                binding.curatedRefreshLayout.setIsTrip(2)
            }
        }

        //更新轨迹路书列表数据
        viewModel.tankCollectList.unPeek().observe(viewLifecycleOwner) {
            Timber.i("tankList:${it.size} tankCurrentPage:${viewModel.tankCurrentPage} tankMaxPage:${viewModel.tankMaxPage.value}")
            if (viewModel.tankCurrentPage == 1){
                ahaTankAdapter?.onRefreshData(it, true)
                if (it.isNotEmpty()){
                    binding.tankList.closeMenu()
                    binding.tankList.scrollToPosition(0)
                    viewModel.tankScrollToPosition.postValue(0)
                    viewModel.tankAllList.postValue(it)
                } else {
                    viewModel.tankAllList.postValue(arrayListOf())
                }
            } else {
                if (it.isNotEmpty()){
                    binding.tankList.closeMenu()
                    val lastSize = ahaTankAdapter?.data?.size ?: 0
                    val data = ahaTankAdapter?.data ?: arrayListOf()
                    data.addAll(it)
                    ahaTankAdapter?.onRefreshData(data as List<TankCollectItem>, false)
                    binding.tankList.scrollToPosition(lastSize)
                    viewModel.tankScrollToPosition.postValue(lastSize)
                    viewModel.tankAllList.postValue(data as List<TankCollectItem>)
                } else {
                    binding.tankList.closeMenu()
                    viewModel.tankCurrentPage -= 1
                    val size = ahaTankAdapter?.data?.size ?: 0
                    val lastSize = if(size > 0 ) size - 1 else 0
                    binding.tankList.scrollToPosition(lastSize)
                    viewModel.tankScrollToPosition.postValue(lastSize)
                    viewModel.tankAllList.postValue(viewModel.tankAllList.value)
                }
            }
            if (viewModel.tankCurrentPage < (viewModel.tankMaxPage.value ?: 1)) {
                binding.tankRefreshLayout.finishLoadMore()
            } else { //最后一页了
                binding.tankRefreshLayout.finishLoadMore()
                binding.tankRefreshLayout.finishLoadMoreWithNoMoreData()
                binding.tankRefreshLayout.setIsTrip(2)
            }
        }

        //取消共创路书收藏结果
        viewModel.deleteGuideCollectResult.unPeek().observeForever(guideCollectResultOb)

        //取消精选路书收藏结果
        viewModel.deleteLineCollectResult.unPeek().observeForever(lineCollectResultOb)

        //取消精选路书收藏结果
        viewModel.deleteTankCollectResult.unPeek().observeForever(tankCollectResultOb)

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            ahaGuideAdapter?.notifyDataSetChanged()
            ahaCuratedAdapter?.notifyDataSetChanged()
            ahaTankAdapter?.notifyDataSetChanged()
            binding.guideRefreshLayout.setNight(it == true)
            binding.curatedRefreshLayout.setNight(it == true)
            binding.tankRefreshLayout.setNight(it == true)
        }
    }

    private val guideCollectResultOb = Observer<String> {
        Timber.i("guideCollectResultOb isVisibility:$isVisibility")
        if (isVisibility && !TextUtils.isEmpty(it)){
            Timber.i("guideCollectResultOb it:$it")
            ahaGuideAdapter?.data?.forEachIndexed { index, _ ->
                if (TextUtils.equals(ahaGuideAdapter?.data?.get(index)?.id.toString(), it)) {
                    ahaGuideAdapter?.data?.removeAt(index)
                    ahaGuideAdapter?.notifyItemRemoved(index)
                    if (ahaGuideAdapter?.data == null || ahaGuideAdapter?.data?.size == 0){
                        viewModel.guideCollectList.postValue(arrayListOf())
                    }
                    return@Observer
                }
            }
        }
    }

    private val lineCollectResultOb = Observer<String> {
        Timber.i("lineCollectResultOb isVisibility:$isVisibility")
        if (isVisibility && !TextUtils.isEmpty(it)){
            Timber.i("lineCollectResultOb it:$it")
            ahaCuratedAdapter?.data?.forEachIndexed { index, _ ->
                if (TextUtils.equals(ahaCuratedAdapter?.data?.get(index)?.id.toString(), it)) {
                    ahaCuratedAdapter?.data?.removeAt(index)
                    ahaCuratedAdapter?.notifyItemRemoved(index)
                    if (ahaCuratedAdapter?.data == null || ahaCuratedAdapter?.data?.size == 0){
                        viewModel.lineCollectList.postValue(arrayListOf())
                    }
                    return@Observer
                }
            }
        }
    }

    private val tankCollectResultOb = Observer<String> {
        Timber.i("tankCollectResultOb isVisibility:$isVisibility")
        if (isVisibility && !TextUtils.isEmpty(it)){
            Timber.i("tankCollectResultOb it:$it")
            ahaTankAdapter?.data?.forEachIndexed { index, _ ->
                if (TextUtils.equals(ahaTankAdapter?.data?.get(index)?.id.toString(), it)) {
                    ahaTankAdapter?.data?.removeAt(index)
                    ahaTankAdapter?.notifyItemRemoved(index)
                    if (ahaTankAdapter?.data == null || ahaTankAdapter?.data?.size == 0){
                        viewModel.tankCollectList.postValue(arrayListOf())
                    }
                    return@Observer
                }
            }
        }
    }
}