package com.desaysv.psmap.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.LayoutStatelliteInfoBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.GnssSnrListAdapter
import com.desaysv.psmap.ui.home.compose.GnssImageCompose
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * @author ZZP
 * @date 2024年12月4日
 * 星图
 */
@AndroidEntryPoint
class GnssInfoDialogFragment : Fragment() {
    private val viewModel by viewModels<GnssInfoDialogViewModel>()
    private lateinit var binding: LayoutStatelliteInfoBinding

    private lateinit var gnssSnrListAdapter: GnssSnrListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutStatelliteInfoBinding.inflate(inflater, container, false).apply {
            cvGnss.setContent {
                GnssView()
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        gnssSnrListAdapter = GnssSnrListAdapter()
        binding.rvSnrList.adapter = gnssSnrListAdapter
        viewModel.satelliteSnrInfo.observe(viewLifecycleOwner) { data ->
            gnssSnrListAdapter.updateData(data.toList())
        }
        binding.ivClose.setDebouncedOnClickListener {
            Timber.i("ivClose")
            findNavController().navigateUp()
        }

        viewModel.isNight.observe(viewLifecycleOwner) { isNight ->
            binding.pbLoading.indeterminateDrawable = AppCompatResources.getDrawable(
                this.requireContext(),
                if (isNight) R.drawable
                    .rotate_loading_active_view_night else R.drawable
                    .rotate_loading_active_view_day
            )
        }

        ViewClickEffectUtils.addClickScale(binding.ivClose, CLICKED_SCALE_90)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.i("onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy")
    }

    @Composable
    private fun GnssView() {
        val isNight by viewModel.isNight.unPeek()
            .observeAsState(NightModeGlobal.isNightMode())
        val gnssLocationInfo by viewModel.gnssLocationInfo.observeAsState(emptyList())
        DsDefaultTheme(isNight) {
            GnssImageCompose(Modifier.fillMaxSize(), gnssLocationInfo)
        }
    }
}