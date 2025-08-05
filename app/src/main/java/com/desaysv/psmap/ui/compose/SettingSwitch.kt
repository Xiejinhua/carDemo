package com.desaysv.psmap.ui.compose

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.desaysv.psmap.base.R
import com.desaysv.psmap.databinding.LayoutSettingSwitchBinding
import com.desaysv.psmap.ui.theme.DsDefaultTheme
import timber.log.Timber

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SettingSwitch(
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    check: Boolean = true,
    isNight: Boolean = true,
    binding: LayoutSettingSwitchBinding,
    setOnCheckedChangeListener: (isChecked: Boolean) -> Unit = {}
) {
    val tag = "layoutSettingSwitchBinding"
    Box(modifier = modifier) {
        AndroidView(
            factory = {
                // 使用findViewWithTag来查找视图
                val viewToRemove = binding.root.findViewWithTag<View>(tag)
                // 检查视图是否存在，并移除它
                if (viewToRemove != null) {
                    // 获取视图的父视图
                    val parent = viewToRemove.parent
                    if (parent != null) {
                        try {
                            (parent as ViewGroup).removeView(viewToRemove)
                            Timber.e(" removeView ");
                        } catch (e: Exception) {
                            Timber.e("catch Exception:${e.message}");
                        }
                    }
                }
                binding.root.tag = tag
                binding.root
            },
            update = {
                binding.switchChange.isEnabled = enable
                binding.check = check
                binding.isNight = isNight
                //步行导航设置
                binding.switchChange.setOnCheckedChangeListener { _, isChecked ->
                    setOnCheckedChangeListener(isChecked)
                    binding.check = isChecked
                }
            },
            modifier = Modifier
        )
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SettingLimitSwitch(
    modifier: Modifier = Modifier,
    vehicleNumber: String = "",
    isNetworkConnected: Boolean = true,
    enable: Boolean = true,
    check: Boolean = true,
    isNight: Boolean = true,
    binding: LayoutSettingSwitchBinding,
    setOnCheckedChangeListener: (isChecked: Int) -> Unit = {}
) {
    val tag = "layoutSettingSwitchBinding"
    Box(modifier = modifier) {
        AndroidView(
            factory = {
                // 使用findViewWithTag来查找视图
                val viewToRemove = binding.root.findViewWithTag<View>(tag)
                // 检查视图是否存在，并移除它
                if (viewToRemove != null) {
                    // 获取视图的父视图
                    val parent = viewToRemove.parent
                    if (parent != null) {
                        try {
                            (parent as ViewGroup).removeView(viewToRemove)
                            Timber.e(" removeView ");
                        } catch (e: Exception) {
                            Timber.e("catch Exception:${e.message}");
                        }
                    }
                }
                binding.root.tag = tag
                binding.root
            },
            update = {
                binding.switchChange.isEnabled = enable
                binding.check = check
                binding.isNight = isNight
                binding.switchChange.alpha = if (check) (if (isNetworkConnected) 1.0f else 0.4f) else (if (isNetworkConnected) 1.0f else 0.3f)
                //步行导航设置
                binding.switchChange.setOnCheckedChangeListener { _, isChecked ->
                    Timber.i("setOnCheckedChangeListener check:$check isChecked:$isChecked")
                    if (TextUtils.isEmpty(vehicleNumber)) { //跳转到系统设置车牌号编辑界面
                        if (!check && !isChecked) {
                            Timber.i("!check && !isChecked")
                            binding.switchChange.setChecked(false, false)
                            binding.switchChange.alpha = if (isNetworkConnected) 1.0f else 0.3f
                        } else {
                            binding.switchChange.setChecked(!isChecked, false)
                            if (!isNetworkConnected) {
                                binding.switchChange.setChecked(!isChecked, false)
                                setOnCheckedChangeListener(1)
                                Timber.i("TextUtils.isEmpty(vehicleNumber) 网络状态不佳，无法使用避开限行功能")
                            } else {
                                setOnCheckedChangeListener(2)
                                Timber.i("跳转到系统设置车牌号编辑界面")
                            }
                        }
                    } else if (!isNetworkConnected) {
                        binding.switchChange.setChecked(!isChecked, false)
                        setOnCheckedChangeListener(1)
                        Timber.i("网络状态不佳，无法使用避开限行功能")
                    } else {
                        setOnCheckedChangeListener(if (isChecked) 3 else 4)
                        binding.check = isChecked
                    }
                }
            },
            modifier = Modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CustomSettingBottomItemPreview() {
    DsDefaultTheme {
        val context = LocalContext.current
        SettingSwitch(binding = LayoutSettingSwitchBinding.inflate(LayoutInflater.from(context)))
    }
}