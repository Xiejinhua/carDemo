package com.desaysv.psmap.ui.settings.poidetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.UserBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentPoiDetailBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchResultChildAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

/**
 * POI 详情弹窗-给我的消息，收藏夹使用
 */
@AndroidEntryPoint
class POIDetailFragment : Fragment() {
    private val viewModel by viewModels<POIDetailViewModel>()
    private lateinit var binding: FragmentPoiDetailBinding

    private lateinit var searchResultChildAdapter: SearchResultChildAdapter

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var userBusiness: UserBusiness

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPoiDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.rlSearchChildStation.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        searchResultChildAdapter = SearchResultChildAdapter()
        binding.rlSearchChildStation.adapter = searchResultChildAdapter
        searchResultChildAdapter.run {
            this.setOnSearchResultChildListener(object :
                SearchResultChildAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    searchResultChildAdapter.toggleSelectPosition(position)
                    viewModel.updatePointCardChildPoiIndex(selectPosition())
                }
            })
        }

        binding.ivGoHere.setDebouncedOnClickListener {
            Timber.i("clGoHere click")
            viewModel.pointDetail.value?.poi?.run {
                var goPoi = this
                if (!this.childPois.isNullOrEmpty() && viewModel.pointDetail.value?.poi?.childIndex != -1) {
                    goPoi = this.childPois[viewModel.pointDetail.value!!.poi.childIndex]
                }
                Timber.i("clGoHere ${goPoi.name}")
                val commandBean = CommandRequestRouteNaviBean.Builder().build(goPoi)
                viewModel.planRoute(commandBean)
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivGoHere, CLICKED_SCALE_95)

        binding.ivClose1.setDebouncedOnClickListener {
            Timber.i("ivClose1 click")
            viewModel.backCurrentCarPositionOther()
            findNavController().navigateUp()
        }
        ViewClickEffectUtils.addClickScale(binding.ivClose1, CLICKED_SCALE_90)

        //外部关闭
        viewModel.pointDetail.unPeek().observe(viewLifecycleOwner) {
            Timber.i("pointDetail showStatus:${it?.showStatus}")
            if (it?.showStatus == false) {
                if (findNavController().currentDestination?.id == R.id.POIDetailFragment)
                    findNavController().navigateUp()
            } else {
                it?.poi?.run {
                    if (this.childPois.isNullOrEmpty()) {
                        viewModel.showChild(false, false)
                    } else {
                        viewModel.showChild(true, this.childPois.size > 2)
                        if (this.childPois.size > 2 && !binding.ivMoreChild.isSelected) {
                            searchResultChildAdapter.updateData(this.childPois.subList(0, 2))
                        } else {
                            searchResultChildAdapter.updateData(this.childPois)
                        }
                    }

                }
            }

        }

        binding.ivMoreChild.setDebouncedOnClickListener {
            Timber.i("ivMoreChild click")
            binding.ivMoreChild.isSelected = !binding.ivMoreChild.isSelected
            viewModel.pointDetail.value?.poi?.childPois?.run {
                if (this.size > 2) {
                    if (binding.ivMoreChild.isSelected) {
                        searchResultChildAdapter.updateData(this)
                    } else {
                        searchResultChildAdapter.updateData(this.subList(0, 2))
                    }
                } else {
                    Timber.w("childPois < 2 ")
                    viewModel.showChild(this.isNotEmpty(), false)
                }
            }
        }
        ViewClickEffectUtils.addClickScale(binding.ivMoreChild, CLICKED_SCALE_90)

        viewModel.pointDetail.value?.poi?.childPois?.run {
            if (this.isEmpty()) {
                viewModel.showChild(false, false)
            } else {
                viewModel.showChild(true, this.size > 2)
                if (this.size > 2 && !binding.ivMoreChild.isSelected) {
                    searchResultChildAdapter.updateData(this.subList(0, 2))
                } else {
                    searchResultChildAdapter.updateData(this)
                }
            }
        }

        viewModel.themeChange.observe(viewLifecycleOwner) {
            //view?.run { skyBoxBusiness.updateView(this, true) }
            viewModel.defaultTrafficPic.value = if (it) ContextCompat.getDrawable(
                requireContext(), R.drawable
                    .shape_bg_traffic_nopic_night
            )
            else ContextCompat.getDrawable(requireContext(), R.drawable.shape_bg_traffic_nopic_day)
            searchResultChildAdapter.notifyDataSetChanged()
        }

        viewModel.mapToast.unPeek().observe(viewLifecycleOwner) { toastContext ->
            toastContext?.run {
                toast.showToast(this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        userBusiness.showAllFavoritesItem(false)
    }

    override fun onPause() {
        super.onPause()
        viewModel.showAllFavoritesItem()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView")
        viewModel.backCurrentCarPositionOther()
        viewModel.showAllFavoritesItem()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            viewModel.showAllFavoritesItem()
        } else {
            userBusiness.showAllFavoritesItem(false)
        }
    }
}