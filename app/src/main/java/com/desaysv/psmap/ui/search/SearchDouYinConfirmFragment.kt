package com.desaysv.psmap.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.autosdk.common.storage.MapSharePreference
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.common.NetWorkManager
import com.desaysv.psmap.base.common.SharePreferenceFactory
import com.desaysv.psmap.base.impl.ICarInfoProxy
import com.desaysv.psmap.base.net.URLConfig.JETOUT_BYTE_AUTO_DOUYIN_AGREEMENT_URL
import com.desaysv.psmap.base.tracking.EventTrackingUtils
import com.desaysv.psmap.databinding.FragmentSearchCategoryBinding
import com.desaysv.psmap.databinding.FragmentSearchDouyinConfirmBinding
import com.desaysv.psmap.model.bean.CommandRequestSearchBean
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_JETOURPOI_SCHEMA
import com.desaysv.psmap.model.bean.CommandRequestSearchBean.Type.Companion.SEARCH_TEAM_DESTINATION
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean.Type.Companion.SEARCH_ALONG_WAY
import com.desaysv.psmap.model.bean.CommandRequestSearchCategoryBean.Type.Companion.SEARCH_AROUND
import com.desaysv.psmap.model.business.ByteAutoBusiness
import com.desaysv.psmap.model.utils.Biz
import com.desaysv.psmap.model.utils.QrUtils
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.SearchCategoryAdapter
import com.desaysv.psmap.ui.search.view.SearchCategoryView.OnItemClickListener
import com.desaysv.psmap.utils.LoadingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * @author 张楠
 * @time 2024/2/22
 * @description
 */
@AndroidEntryPoint
class SearchDouYinConfirmFragment : Fragment() {
    private lateinit var binding: FragmentSearchDouyinConfirmBinding

    @Inject
    lateinit var toast: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var netWorkManager: NetWorkManager

    @Inject
    lateinit var byteAutoBusiness: ByteAutoBusiness

    @Inject
    lateinit var loadingUtil: LoadingUtil

    @Inject
    lateinit var sharePreferenceFactory: SharePreferenceFactory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Timber.i("onCreateView()")
        binding = FragmentSearchDouyinConfirmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initView()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
    }

    private fun initView() {

        binding.searchCategoryBack.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }
        binding.ivQrCode.setImageBitmap(QrUtils.createQRImage(JETOUT_BYTE_AUTO_DOUYIN_AGREEMENT_URL, 308, 308))
        binding.tvConfirm.setDebouncedOnClickListener {
            lifecycleScope.launch(Dispatchers.Main) {
                doDouYinConfirm(true)
                if (netWorkManager.isNetworkConnected()) {
                    loadingUtil.showLoading("正在搜索")
                    val list = byteAutoBusiness.fefreshJetourPoiListDistance()
                    if (list.isNotEmpty()) {
                        val schema = byteAutoBusiness.getByteAutoVideoSchemaByKeyword(list.first().venue_name)
                        loadingUtil.cancelLoading()
                        if (schema.isNullOrEmpty()) {
                            toast.showToast("网络异常，请检查网络后再试")
                        } else {
                            val options = NavOptions.Builder()
                                .setPopUpTo(R.id.searchDouYinConfirmFragment, true) // 弹出当前Fragment
                                .build()
                            val commandBean =
                                CommandRequestSearchBean.Builder()
                                    .setKeyword(schema)
                                    .setType(SEARCH_JETOURPOI_SCHEMA)
                                    .build()
                            findNavController().navigate(R.id.to_jetourPoiListFragment, commandBean.toBundle(), options)
                        }
                    } else {
                        //走不到这里
                        loadingUtil.cancelLoading()
                        toast.showToast("未找到数据")
                    }
                } else {
                    toast.showToast("网络异常，请检查网络后再试")
                }

            }
        }
    }

    fun doDouYinConfirm(flag: Boolean) {
        sharePreferenceFactory.getMapSharePreference(MapSharePreference.SharePreferenceName.active)?.putBooleanValue(
            MapSharePreference.SharePreferenceKeyEnum.douYinConfirm, flag
        )
    }

    private fun initData() {

    }

    override fun onDestroyView() {
        Timber.i("onDestroyView()")
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        Timber.i("onPause()")
    }

    override fun onDestroy() {
        Timber.i("onDestroy()")
        super.onDestroy()
    }
}

