package com.desaysv.psmap.ui.settings.vehicle

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.base.utils.unPeek
import com.desaysv.psmap.databinding.FragmentVehicleBinding
import com.desaysv.psmap.model.utils.ToastUtil
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


/**
 * @author 王漫生
 * @description 我的车辆
 */
@AndroidEntryPoint
class VehicleFragment : Fragment(), LicensePlateView.InputListener {
    private lateinit var binding: FragmentVehicleBinding
    private val viewModel by viewModels<VehicleViewModel>()

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    @Inject
    lateinit var toastUtil: ToastUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVehicleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initBinding()
        initEventOperation()
    }

    override fun onPause() {
        super.onPause()
        hideSoftInput()//隐藏键盘
    }

    override fun onDestroyView() {
        super.onDestroyView()
        deleteALL() //车牌号清空了
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel

        initView();//初始化View
        showData();//显示最新数据
    }

    private fun initEventOperation() {
        //退出该界面
        binding.carNumberTitle.setDebouncedOnClickListener {
            findNavController().navigateUp()
        }

        //保持车牌操作
        binding.addVehicle.setDebouncedOnClickListener {
            viewModel.doVehicle()//添加车牌号
        }

        //限行开关操作
        binding.limitSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.limitOperation(isChecked)
        }

        //设置按钮背景
        viewModel.clickVehicleEnable.observe(viewLifecycleOwner) { addVehicleLayout() }

        //限行按钮alpha值
        viewModel.limitEnable.observe(viewLifecycleOwner) {
            binding.limitSwitch.alpha = if (it) 1.0f else 0.3f
        }

        //登录状态回调
        viewModel.loginLoading.unPeek().observe(viewLifecycleOwner) { showData() }

        //显示toast
        viewModel.setToast.observe(viewLifecycleOwner) {
            toastUtil.showToast(it)
        }

        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) {
            viewModel.isNight.postValue(it)
            addVehicleLayout()
            if (binding.licensePlateView.updateViewPosition in 0..7) {
                if (binding.licensePlateView.showVehicleSoftInput()) {
                    binding.licensePlateView.setTextViewsBackground(binding.licensePlateView.updateViewPosition)
                } else {
                    binding.licensePlateView.restoreDefault()
                }
                binding.licensePlateView.onSetTextColor(if (it) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay)
            }
        }
    }

    private fun initView() {
        binding.licensePlateView.setInputListener(this)
        binding.licensePlateView.setKeyboardContainerLayout(binding.reLicensePlate)
        binding.licensePlateView.showLastView()
        binding.licensePlateView.onSetTextColor(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.model.R.color.onPrimaryNight else com.desaysv.psmap.model.R.color.onPrimaryDay)
        binding.licensePlateView.hideProvinceView()
    }

    //打开车辆设置显示最新数据
    private fun showData() {
        Timber.d(" setProvince showData")
        viewModel.getVehicleNumberLimit() //获取限行数据
        viewModel.vehicleNum?.let { setProvince(it) } //设置车牌
    }

    //设置车牌
    private fun setProvince(vehicleNumber: String) {
        if (TextUtils.isEmpty(vehicleNumber)) {
            binding.licensePlateView.clearEditText()
            binding.licensePlateView.initFirstStringBuffer("京")
        } else {
            binding.licensePlateView.initStringBuffer(vehicleNumber)
        }
    }

    //隐藏键盘
    private fun hideSoftInput() {
        binding.licensePlateView.hideSoftInput()
    }

    //车牌号清空了
    private fun deleteALL() {
        if (TextUtils.isEmpty(viewModel.vehicleNum)) {
            viewModel.savePlateNumber("");//保存车牌号
            viewModel.limitEnable.postValue(false)
        }
    }

    private fun addVehicleLayout() {
        if (viewModel.clickVehicleEnable.value!!) {
            binding.addVehicle.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.selector_bg_confirm_night else R.drawable.selector_bg_confirm_day)
            binding.addVehicle.setTextColor(resources.getColor(com.desaysv.psmap.model.R.color.onPrimaryNight))
        } else {
            binding.addVehicle.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.shape_bg_btn_not_confirm_night else R.drawable.shape_bg_btn_not_confirm_day)
            binding.addVehicle.setTextColor(
                if (NightModeGlobal.isNightMode()) resources.getColor(com.desaysv.psmap.model.R.color.onSecondaryNight) else resources.getColor(
                    com.desaysv.psmap.model.R.color.onSecondaryContainerDay
                )
            )
        }
    }

    override fun inputComplete(content: String?) {
        viewModel.vehicleNum = content
        viewModel.clickVehicleEnable.postValue(true)
        Timber.d(" inputComplete $content")
    }

    override fun deleteContent() {
        viewModel.clickVehicleEnable.postValue(false)
        Timber.d(" deleteContent  删除")
    }
}