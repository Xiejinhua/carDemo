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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentMyAhaTripBinding
import com.desaysv.psmap.model.bean.MineGuideList
import com.desaysv.psmap.model.bean.MineTankList
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.AhaGuideAdapter
import com.desaysv.psmap.ui.adapter.AhaTankAdapter
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 我的路书
 */
@AndroidEntryPoint
class MyAhaTripFragment : Fragment() {
    private lateinit var binding: FragmentMyAhaTripBinding
    private val viewModel: MyAhaTripViewModel by viewModels()
    private var lastTargetX = 0
    private var isFirst = true

    private var ahaGuideAdapter: AhaGuideAdapter? = null
    private var ahaTankAdapter: AhaTankAdapter? = null

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toast: ToastUtil

    override fun onDestroy() {
        super.onDestroy()
        ahaGuideAdapter = null
        ahaTankAdapter = null
        binding.guideList.closeMenu()//关闭左滑菜单
        binding.tankList.closeMenu()//关闭左滑菜单
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden){
            Timber.d(" onHiddenChanged selectTab:%s", viewModel.selectTab.value)
            if (viewModel.selectTab.value == true){
                binding.layoutTab.check(R.id.rb_co_creation_trip)
                viewModel.requestMineGuideList() //我的-我制作的共创路书列表
            } else {
                Timber.d(" onHiddenChanged 轨迹")
                viewModel.requestMineTankList() //我的-我制作的轨迹列表
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyAhaTripBinding.inflate(inflater, container, false)
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
        ahaTankAdapter = AhaTankAdapter().also { binding.tankList.adapter = it }
        binding.guideRefreshLayout.setEnableRefresh(false)
        binding.guideRefreshLayout.setEnableAutoLoadMore(true)
        binding.guideRefreshLayout.setDisableContentWhenLoading(true)
        binding.guideRefreshLayout.setIsTrip(2)
        binding.tankRefreshLayout.setEnableRefresh(false)
        binding.tankRefreshLayout.setEnableAutoLoadMore(true)
        binding.tankRefreshLayout.setDisableContentWhenLoading(true)
        binding.tankRefreshLayout.setIsTrip(2)
        comeInRequestLineList() //进入界面加载详情数据
        KeyboardUtil.hideKeyboard(view)
    }

    //进入界面加载详情数据
    private fun comeInRequestLineList(){
        Timber.d(" initBinding selectTab:%s", viewModel.selectTab.value)
        if (viewModel.selectTab.value == true){
            binding.layoutTab.check(R.id.rb_co_creation_trip)
            if (isFirst) {
                isFirst = false
                binding.guideList.closeMenu()//关闭左滑菜单
                binding.guideRefreshLayout.resetNoMoreData()
                viewModel.guideCurrentPage = 1
                viewModel.guideAllList.postValue(arrayListOf())
                binding.guideList.scrollToPosition(0)
                viewModel.guideScrollToPosition.postValue(0)
                viewModel.requestMineGuideList() //我的-我制作的共创路书列表
            } else {
                if (viewModel.guideAllList.value == null || viewModel.guideAllList.value?.isEmpty() == true){
                    binding.guideList.closeMenu()//关闭左滑菜单
                    binding.guideRefreshLayout.resetNoMoreData()
                    viewModel.guideCurrentPage = 1
                    viewModel.guideAllList.postValue(arrayListOf())
                    binding.guideList.scrollToPosition(0)
                    viewModel.guideScrollToPosition.postValue(0)
                    viewModel.requestMineGuideList() //我的-我制作的共创路书列表
                } else {
                    ahaGuideAdapter?.onRefreshData(viewModel.guideAllList.value, false)
                    binding.guideList.scrollToPosition( viewModel.guideScrollToPosition.value ?: 0)
                }
            }
        } else {
            Timber.d(" initBinding 轨迹")
            if (viewModel.tankAllList.value == null || viewModel.tankAllList.value?.isEmpty() == true){
                binding.tankList.closeMenu()//关闭左滑菜单
                binding.tankRefreshLayout.resetNoMoreData()
                viewModel.tankCurrentPage = 1
                binding.tankList.scrollToPosition(0)
                viewModel.tankScrollToPosition.postValue(0)
                viewModel.tankAllList.postValue(arrayListOf())
                viewModel.requestMineTankList() //我的-我制作的轨迹列表
            } else {
                ahaTankAdapter?.onRefreshData(viewModel.tankAllList.value, false)
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
                val targetX: Int = checkedButton.left + (checkedButton.width - binding.indicator.width) / 2
                Timber.i("layoutTab checkedId:$checkedId targetX:$targetX lastTargetX:$lastTargetX")
                // 创建平移动画
                if (targetX == 0 && checkedId != R.id.rb_co_creation_trip){
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
                                        binding.guideList.scrollToPosition(0)
                                        viewModel.guideScrollToPosition.postValue(0)
                                        viewModel.selectTab.postValue(true)
                                        viewModel.requestMineGuideList() //我的-我制作的共创路书列表
                                    }

                                    R.id.rb_track_trip -> {
                                        binding.tankRefreshLayout.resetNoMoreData()
                                        viewModel.tankCurrentPage = 1
                                        viewModel.tankAllList.postValue(arrayListOf())
                                        binding.tankList.scrollToPosition(0)
                                        viewModel.tankScrollToPosition.postValue(0)
                                        viewModel.selectTab.postValue(false)
                                        viewModel.requestMineTankList() //我的-我制作的轨迹列表
                                    }
                                }
                            }
                        })
                        .start()
                }
            }
        }

        //共创路书列表加载更多
        binding.guideRefreshLayout.setOnRefreshLoadMoreListener(
            object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    //不作处理
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    if (viewModel.guideCurrentPage < (viewModel.guideMaxPage.value ?: 1)) {
                        viewModel.guideCurrentPage += 1
                        viewModel.requestMineGuideList() //我的-我制作的共创路书列表
                    } else { //最后一页了
                        binding.guideRefreshLayout.finishLoadMore()//结束加载
                    }
                }
            })

        //轨迹路书列表加载更多
        binding.tankRefreshLayout.setOnRefreshLoadMoreListener(
            object : OnRefreshLoadMoreListener {
                override fun onRefresh(refreshLayout: RefreshLayout) {
                    //不作处理
                }

                override fun onLoadMore(refreshLayout: RefreshLayout) {
                    if (viewModel.tankCurrentPage < (viewModel.tankMaxPage.value ?: 1)) {
                        viewModel.tankCurrentPage += 1
                        viewModel.requestMineTankList() //我的-我制作的轨迹列表
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
                putBoolean("isMineFav", false)
            })
        }

        //删除共创路书
        ahaGuideAdapter?.setOnGuideClickListener(object: AhaGuideAdapter.OnGuideClickListener{
            override fun onDeleteClick(item: MineGuideList?) {
                binding.guideList.closeMenu()//关闭左滑菜单
                viewModel.requestMineGuideDelete(item?.id.toString())
            }
        })

        //轨迹路书列表点击
        ahaTankAdapter?.setOnItemClickListener { _, _, position ->
            viewModel.setIsTankFav(false)
            findNavController().navigate(R.id.to_myAhaTankDetailFragment, Bundle().apply {
                putInt("id", ahaTankAdapter?.data?.get(position)?.id ?: 0)
                putInt("position", position)
                putBoolean("isMineFav", false)
            })
        }

        //删除轨迹路书
        ahaTankAdapter?.setOnTankClickListener(object: AhaTankAdapter.OnTankClickListener{
            override fun onDeleteClick(item: MineTankList?) {
                binding.tankList.closeMenu()//关闭左滑菜单
                viewModel.requestMineTankDelete(item?.id.toString())
            }
        })

        //更新路书列表数据
        viewModel.guideList.unPeek().observe(viewLifecycleOwner) {
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

        //更新轨迹路书列表数据
        viewModel.tankList.unPeek().observe(viewLifecycleOwner) {
            Timber.i("tankList:${it.size} tankCurrentPage:${viewModel.tankCurrentPage} tankMaxPage:${viewModel.tankMaxPage.value}")
            if (viewModel.tankCurrentPage == 1){
                ahaTankAdapter?.onRefreshData(it, false)
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
                    ahaTankAdapter?.onRefreshData(data as List<MineTankList>, false)
                    binding.tankList.scrollToPosition(lastSize)
                    viewModel.tankScrollToPosition.postValue(lastSize)
                    viewModel.tankAllList.postValue(data as List<MineTankList>)
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

        //删除共创路书结果
        viewModel.deleteGuideResult.unPeek().observe(viewLifecycleOwner) {
            ahaGuideAdapter?.data?.forEachIndexed{ index, _ ->
                if (TextUtils.equals(ahaGuideAdapter?.data?.get(index)?.id.toString(), it)){
                    ahaGuideAdapter?.data?.removeAt(index)
                    ahaGuideAdapter?.notifyItemRemoved(index)
                    if (ahaGuideAdapter?.data == null || ahaGuideAdapter?.data?.size == 0){
                        viewModel.guideList.postValue(arrayListOf())
                    }
                    return@observe
                }
            }
        }

        //删除轨迹路书结果
        viewModel.deleteTankResult.unPeek().observe(viewLifecycleOwner) {
            ahaTankAdapter?.data?.forEachIndexed{ index, _ ->
                if (TextUtils.equals(ahaTankAdapter?.data?.get(index)?.id.toString(), it)){
                    ahaTankAdapter?.data?.removeAt(index)
                    ahaTankAdapter?.notifyItemRemoved(index)
                    if (ahaTankAdapter?.data == null || ahaTankAdapter?.data?.size == 0){
                        viewModel.tankList.postValue(arrayListOf())
                    }
                    return@observe
                }
            }
        }

        viewModel.setToast.unPeek().observe(viewLifecycleOwner) {
            toast.showToast(it)
        }

        //日夜模式监听
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            ahaGuideAdapter?.notifyDataSetChanged()
            ahaTankAdapter?.notifyDataSetChanged()
            binding.guideRefreshLayout.setNight(it == true)
            binding.tankRefreshLayout.setNight(it == true)
        }
    }
}