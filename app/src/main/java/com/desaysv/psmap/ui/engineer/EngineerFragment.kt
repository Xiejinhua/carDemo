package com.desaysv.psmap.ui.engineer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.config.AutoEggConfig
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentEngineerBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.adapter.EngineerFunctionListAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * @author 王漫生
 * @description 工程模式主界面
 */
@AndroidEntryPoint
class EngineerFragment : Fragment() {
    private lateinit var binding: FragmentEngineerBinding
    private lateinit var adapter: EngineerFunctionListAdapter
    private val viewModel by viewModels<EngineerViewModel>()

    @Inject
    lateinit var toastUtil: ToastUtil

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEngineerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initBinding()
        initData()
    }

    private fun initData() {
        adapter.onRefreshData(
            listOf(
                getString(R.string.sv_engineer_version_info),
                getString(R.string.sv_engineer_log_control),
                getString(R.string.sv_engineer_position_debug),
                getString(R.string.sv_engineer_performance_debug),
                getString(R.string.sv_engineer_uuid_debug),
                getString(R.string.sv_engineer_other_debug)
            )
        )
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        adapter = EngineerFunctionListAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : EngineerFunctionListAdapter.ItemClickCallback {
                override fun onItemClick(position: Int) {
                    adapter.notifyDataChangedByClick(position)
                    viewModel.setSelect(position)
                }
            })
        }
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.listFunction.adapter = adapter

        binding.locationLayout.etLon.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        binding.locationLayout.etLat.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        binding.uuidLayout.etUuid.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                requireActivity().window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                requireActivity().window.setLocalFocus(true, true)
            }
            false
        }

        binding.locationLayout.btnSetPosition.setDebouncedOnClickListener {
            val success = viewModel.setStartCarPosition(binding.locationLayout.etLon.text.toString(), binding.locationLayout.etLat.text.toString())
            if (!success) toastUtil.showToast(R.string.sv_engineer_set_position_fail)
        }

        binding.logLayout.btnAutoBl.setDebouncedOnClickListener {
            viewModel.switchBlLogLevel()
        }

        binding.performanceLayout.sbFpsNormal.progress = viewModel.sbFpsNormalProgress.value ?: 10
        binding.performanceLayout.sbFpsNavi.progress = viewModel.sbFpsNaviProgress.value ?: 10
        binding.performanceLayout.sbFpsAnimation.progress = viewModel.sbFpsAnimationProgress.value ?: 10
        binding.performanceLayout.sbFpsGesture.progress = viewModel.sbFpsGestureProgress.value ?: 10

        binding.performanceLayout.sbFpsNormalBack.progress = viewModel.sbFpsNormalBackProgress.value ?: 10
        binding.performanceLayout.sbFpsNaviBack.progress = viewModel.sbFpsNaviBackProgress.value ?: 10
        binding.performanceLayout.sbFpsAnimationBack.progress = viewModel.sbFpsAnimationBackProgress.value ?: 10
        binding.performanceLayout.sbFpsGestureBack.progress = viewModel.sbFpsGestureBackProgress.value ?: 10

        binding.performanceLayout.sbFpsNormal.setOnSeekBarChangeListener(object : MyOnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.sbFpsNormalProgress.postValue(p1)
            }

        })
        binding.performanceLayout.sbFpsNavi.setOnSeekBarChangeListener(object : MyOnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.sbFpsNaviProgress.postValue(p1)
            }

        })
        binding.performanceLayout.sbFpsAnimation.setOnSeekBarChangeListener(object : MyOnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.sbFpsAnimationProgress.postValue(p1)
            }

        })
        binding.performanceLayout.sbFpsGesture.setOnSeekBarChangeListener(object : MyOnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.sbFpsGestureProgress.postValue(p1)
            }

        })
        binding.performanceLayout.sbFpsNormalBack.setOnSeekBarChangeListener(object : MyOnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.sbFpsNormalBackProgress.postValue(p1)
            }

        })
        binding.performanceLayout.sbFpsNaviBack.setOnSeekBarChangeListener(object : MyOnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.sbFpsNaviBackProgress.postValue(p1)
            }

        })
        binding.performanceLayout.sbFpsAnimationBack.setOnSeekBarChangeListener(object : MyOnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.sbFpsAnimationBackProgress.postValue(p1)
            }

        })
        binding.performanceLayout.sbFpsGestureBack.setOnSeekBarChangeListener(object : MyOnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                viewModel.sbFpsGestureBackProgress.postValue(p1)
            }

        })

        binding.performanceLayout.btnConfirmFps.setDebouncedOnClickListener {
            viewModel.setRenderFps(
                true, binding.performanceLayout.sbFpsNormal.progress, binding.performanceLayout.sbFpsNavi.progress,
                binding.performanceLayout.sbFpsAnimation.progress, binding.performanceLayout.sbFpsGesture.progress
            )
            viewModel.setRenderFps(
                false, binding.performanceLayout.sbFpsNormalBack.progress, binding.performanceLayout.sbFpsNaviBack.progress,
                binding.performanceLayout.sbFpsAnimationBack.progress, binding.performanceLayout.sbFpsGestureBack.progress
            )
        }
        binding.performanceLayout.btnResetFps.setDebouncedOnClickListener {
            viewModel.resetRenderFps()
            binding.performanceLayout.sbFpsNormal.progress = AutoEggConfig.FOREGROUND_MapRenderModeNormal
            binding.performanceLayout.sbFpsNavi.progress = AutoEggConfig.FOREGROUND_MapRenderModeNavi
            binding.performanceLayout.sbFpsAnimation.progress = AutoEggConfig.FOREGROUND_MapRenderModeAnimation
            binding.performanceLayout.sbFpsGesture.progress = AutoEggConfig.FOREGROUND_MapRenderModeGestureAction

            binding.performanceLayout.sbFpsNormalBack.progress = AutoEggConfig.BACKEND_MapRenderModeNormal
            binding.performanceLayout.sbFpsNaviBack.progress = AutoEggConfig.BACKEND_MapRenderModeNavi
            binding.performanceLayout.sbFpsAnimationBack.progress = AutoEggConfig.BACKEND_MapRenderModeAnimation
            binding.performanceLayout.sbFpsGestureBack.progress = AutoEggConfig.BACKEND_MapRenderModeGestureAction
        }

        binding.uuidLayout.btConfirm.setDebouncedOnClickListener {
            val flag = viewModel.saveTestUuid(binding.uuidLayout.etUuid.text.toString())
            if (!flag) toastUtil.showToast(R.string.sv_engineer_require_uuid_15)
        }
        binding.uuidLayout.sbUuidTest.setDebouncedOnClickListener {
            val flag = viewModel.openTestUuid(!viewModel.uuidCheckFlag.value!!)
            if (!flag) toastUtil.showToast(R.string.sv_engineer_require_uuid_15)
        }
        viewModel.testUuid.observe(this.viewLifecycleOwner) {
            binding.uuidLayout.tvUuid.text = getString(R.string.sv_engineer_current_uuid, it)
        }

        binding.otherLayout.btnResetMap.setDebouncedOnClickListener {
            binding.otherLayout.btnResetMap.isClickable = false
            binding.otherLayout.tvResetMap.text = "Exit applicaiton after 5 second"
            viewModel.resetMap()
        }

        skyBoxBusiness.themeChange().unPeek().observe(viewLifecycleOwner) {
            adapter?.notifyDataSetChanged()
        }
    }

    interface MyOnSeekBarChangeListener : SeekBar.OnSeekBarChangeListener {

        override fun onStartTrackingTouch(p0: SeekBar?) {

        }

        override fun onStopTrackingTouch(p0: SeekBar?) {

        }
    }

}