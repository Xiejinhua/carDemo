package com.desaysv.psmap.ui.dialog

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.autosdk.bussiness.common.POI
import com.autosdk.common.utils.ResUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.ViewMapPoiCardBinding
import com.desaysv.psmap.model.bean.CommandRequestRouteNaviBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.business.JsonStandardProtocolManager
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
 * @author ZZP
 * @date 2024年11月15日
 * @project：POI卡片弹窗
 */
@AndroidEntryPoint
class POICardDialogFragment : Fragment() {
    private val viewModel by viewModels<POICardDialogViewModel>()
    private lateinit var binding: ViewMapPoiCardBinding

    private lateinit var searchResultChildAdapter: SearchResultChildAdapter

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var jsonStandardProtocolManager: JsonStandardProtocolManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ViewMapPoiCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.rlSearchChildStation.layoutManager =
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
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

        //卡片收藏点击
        binding.ivFavorite.setDebouncedOnClickListener {
            Timber.i("clFavorite click")
            viewModel.addOrDelFavorite()
        }

        binding.lyFavoriteMy.setDebouncedOnClickListener {
            Timber.i("lyFavoriteMy click")
            viewModel.addOrDelFavorite()
        }

        binding.ivGoHere.setDebouncedOnClickListener {
            Timber.i("clGoHere click")
            viewModel.mapPointCard.value?.poi?.run {
                var goPoi = this
                if (!this.childPois.isNullOrEmpty() && viewModel.mapPointCard.value?.poi?.childIndex != -1) {
                    goPoi = this.childPois[viewModel.mapPointCard.value!!.poi.childIndex]
                }
                Timber.i("clGoHere ${goPoi.name}")
                findNavController().navigateUp()
                val commandBean = CommandRequestRouteNaviBean.Builder().build(goPoi)
                viewModel.planRoute(commandBean)
            }
        }
        binding.ivSearchAround.setDebouncedOnClickListener {
            Timber.i("clSearchAround click")
            viewModel.mapPointCard.value?.poi?.let {
                Timber.i("clSearchAround ${it.name}")
                EventTrackingUtils.trackEvent(
                    EventTrackingUtils.EventName.SurroundSearch_Click,
                    Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
                )
                val commandBean = CommandRequestSearchCategoryBean.Builder()
                    .setPoi(it)
                    .setType(CommandRequestSearchCategoryBean.Type.SEARCH_AROUND)
                    .build()
                findNavController().navigate(
                    R.id.to_searchCategoryFragment,
                    commandBean.toBundle()
                )
            }
        }

        binding.lySearchAroundMy.setDebouncedOnClickListener {
            Timber.i("lySearchAroundMy click")
            viewModel.mapPointCard.value?.poi?.let {
                EventTrackingUtils.trackEvent(
                    EventTrackingUtils.EventName.SurroundSearch_Click,
                    Pair(EventTrackingUtils.EventValueName.SearchTime, System.currentTimeMillis())
                )
                Timber.i("clSearchAround ${it.name}")
                val commandBean = CommandRequestSearchCategoryBean.Builder()
                    .setPoi(it)
                    .setType(CommandRequestSearchCategoryBean.Type.SEARCH_AROUND)
                    .build()
                findNavController().navigate(
                    R.id.to_searchCategoryFragment,
                    commandBean.toBundle()
                )
            }
        }

        binding.ivClose.setDebouncedOnClickListener {
            Timber.i("ivClose click")
            viewModel.hideMapPointCard()
            findNavController().navigateUp()
        }

        binding.ivClose1.setDebouncedOnClickListener {
            Timber.i("ivClose1 click")
            viewModel.hideMapPointCard()
            findNavController().navigateUp()
        }

        binding.ivClose2.setDebouncedOnClickListener {
            Timber.i("ivClose1 click")
            viewModel.hideMapPointCard()
            findNavController().navigateUp()
        }

        //外部关闭
        viewModel.mapPointCard.unPeek().observe(viewLifecycleOwner) {
            if (it?.showStatus == false) {
                if (findNavController().currentDestination?.id == R.id.POICardDialogFragment)
                    findNavController().navigateUp()
            } else {
                it?.poi?.run {
                    if (this.childPois.isNullOrEmpty()) {
                        viewModel.showChild(false, false)
                    } else {
                        viewModel.showChild(true, this.childPois.size > 2)
                        if (this.childPois.size > 2 && !binding.ivMoreChild.isSelected) {
                            updateData(this.childPois.subList(0, 2))
                        } else {
                            updateData(this.childPois)
                        }
                    }

                }
            }

        }

        binding.ivMoreChild.setDebouncedOnClickListener {
            Timber.i("ivMoreChild click")
            binding.ivMoreChild.isSelected = !binding.ivMoreChild.isSelected
            viewModel.mapPointCard.value?.poi?.childPois?.run {
                if (this.size > 2) {
                    if (binding.ivMoreChild.isSelected) {
                        updateData(this)
                    } else {
                        updateData(this.subList(0, 2))

                    }
                } else {
                    Timber.w("childPois < 2 ")
                    viewModel.showChild(this.isNotEmpty(), false)
                }
            }
        }

        binding.ivPhoneCall.setDebouncedOnClickListener {
            viewModel.mapPointCard.value?.poi?.phone?.let { phone ->
                var num: String = phone
                val index: Int = phone.indexOf(";")
                if (index > -1) {
                    val phoneList: Array<String> = phone.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (phoneList.size > 0) {
                        num = phoneList[0].trim { it <= ' ' }
                    }
                }
                viewModel.onPhoneCall(num)
            }
        }

        binding.tvReTry.setDebouncedOnClickListener {
            viewModel.retrySearchPoiCardInfo()
        }

        viewModel.mapPointCard.value?.poi?.childPois?.run {
            if (this.isEmpty()) {
                viewModel.showChild(false, false)
            } else {
                viewModel.showChild(true, this.size > 2)
                if (this.size > 2 && !binding.ivMoreChild.isSelected) {
                    updateData(this.subList(0, 2))
                } else {
                    updateData(this)
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
        }

        viewModel.mapToast.unPeek().observe(viewLifecycleOwner) { toastContext ->
            toastContext?.run {
                toast.showToast(this, com.desaysv.psmap.model.R.drawable.ic_toast_complete_day)
            }
        }
        jsonStandardProtocolManager.favoritesUpdate.unPeek().observe(viewLifecycleOwner) {
            Timber.d("jsonStandardProtocolManager.favoritesUpdate")
            viewModel.refreshMapPointCard()
        }

        binding.tvOpenTime.movementMethod = ScrollingMovementMethod()

        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivClose1, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivClose2, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivSearchAround, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivFavorite, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivPhoneCall, CLICKED_SCALE_90)
        ViewClickEffectUtils.addClickScale(binding.ivGoHere, CLICKED_SCALE_95)
        ViewClickEffectUtils.addClickScale(binding.ivMoreChild, CLICKED_SCALE_90)
    }

    private fun updateData(poiList: List<POI>?) {
        if ((poiList?.size ?: 0) > 6) {
            binding.rlSearchChildStation.layoutParams = binding.rlSearchChildStation.layoutParams.apply {
                height = ResUtil.getDimension(com.desaysv.psmap.base.R.dimen.sv_dimen_250)
            }
        } else {
            binding.rlSearchChildStation.layoutParams = binding.rlSearchChildStation.layoutParams.apply {
                height = LayoutParams.WRAP_CONTENT
            }
        }
        searchResultChildAdapter.updateData(poiList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView")
    }
}