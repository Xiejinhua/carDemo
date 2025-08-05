package com.desaysv.psmap.ui.search.view

import android.app.Dialog
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import com.autonavi.auto.skin.NightModeGlobal
import com.autonavi.auto.skin.view.SkinConstraintLayout.GONE
import com.autonavi.auto.skin.view.SkinConstraintLayout.VISIBLE
import com.autonavi.gbl.search.model.SearchChildCategoryInfo
import com.autonavi.gbl.search.model.SearchClassifyInfo
import com.desaysv.psmap.R
import com.desaysv.psmap.base.business.SkyBoxBusiness
import com.desaysv.psmap.databinding.ClassifyDialogFragmentBinding
import com.desaysv.psmap.ui.adapter.SearchClassifyCateGoryAdapter
import com.desaysv.psmap.ui.adapter.SearchClassifyDetailAdapter
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.abs


/**
 * @author 张楠
 * @time 2025/03/17
 * @description
 */
@AndroidEntryPoint
class ClassifyFragment : DialogFragment(), View.OnClickListener {
    private lateinit var binding: ClassifyDialogFragmentBinding
    private var listener: OnClassifyClickListener? = null // 自定义接口OnClassifyClickListener

    private lateinit var searchClassifyInfo: SearchClassifyInfo
    private var selectFilterIndex = 0

    //二级分类
    private lateinit var searchClassifyCateGoryAdapter: SearchClassifyDetailAdapter<SearchChildCategoryInfo>

    //三级分类的左分类
    private lateinit var searchClassifyCategoryLeftAdapter: SearchClassifyCateGoryAdapter

    //三级分类的右分类
    private lateinit var searchClassifyCategoryRightAdapter: SearchClassifyDetailAdapter<SearchChildCategoryInfo>

    @Inject
    lateinit var skyBoxBusiness: SkyBoxBusiness

    fun showClassifyDialog(manager: FragmentManager, searchClassifyInfo: SearchClassifyInfo, selectFilter: Int) {
        this.searchClassifyInfo = searchClassifyInfo
        this.selectFilterIndex = selectFilter
        manager.beginTransaction().remove(this).commitAllowingStateLoss()
        if (!isAdded) {
            val ft = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, com.desaysv.psmap.model.R.style.ClassifyDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ClassifyDialogFragmentBinding.inflate(inflater, container, false)
        skyBoxBusiness.updateView(binding.root, true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated()")
        initView() //初始化界面数据
        initBinding()
    }

    private fun initView() {
        binding.clRoot.setOnClickListener { dismissAllowingStateLoss() }
        binding.layoutFilter0.root.setOnClickListener(this)
        binding.layoutFilter1.root.setOnClickListener(this)
        binding.layoutFilter2.root.setOnClickListener(this)
        binding.layoutFilter3.root.setOnClickListener(this)


        searchClassifyCateGoryAdapter = SearchClassifyDetailAdapter<SearchChildCategoryInfo>().also { adapter ->
            adapter.setOnListener(object : SearchClassifyDetailAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    if (!adapter.data[position].childCategoryInfoList.isNullOrEmpty()) {
                        updateLeftSearchChildCategory(adapter.data, position)
                        binding.clClassifySecondLayout.visibility = GONE
                        binding.clClassifyThirdLayout.visibility = VISIBLE
                    } else {
                        //触发搜索操作
                        if (listener != null) {
                            searchClassifyInfo.classifyItemInfo?.categoryInfoList?.get(selectFilterIndex)?.baseInfo =
                                adapter.data[position].baseInfo
                            listener?.onClassifyClick(
                                getClassifyValue(adapter.data[position].baseInfo.value),
                                searchClassifyInfo.retainState,
                                searchClassifyInfo
                            )
                        }
                        dismissAllowingStateLoss()
                    }
                }
            })
        }
        binding.rlClassifyCategory.layoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        binding.rlClassifyCategory.adapter = searchClassifyCateGoryAdapter
        searchClassifyCategoryLeftAdapter = SearchClassifyCateGoryAdapter().also { adapter ->
            adapter.setOnListener(object : SearchClassifyCateGoryAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    if (!adapter.data[position].childCategoryInfoList.isNullOrEmpty()) {
                        //触发搜索操作
                        adapter.setSelectPosition(position)
                        updateRightSearchChildCategory(adapter.data[position].childCategoryInfoList)

                    } else {
                        //触发搜索操作
                        if (listener != null) {
                            searchClassifyInfo.classifyItemInfo.categoryInfoList[selectFilterIndex].baseInfo =
                                adapter.data[position].baseInfo
                            listener!!.onClassifyClick(
                                getClassifyValue(adapter.data[position].baseInfo.value),
                                searchClassifyInfo.retainState,
                                searchClassifyInfo
                            )
                        }
                        dismissAllowingStateLoss()
                    }
                }

                override fun onSelectPosition(selectPosition: Int) {
                    Timber.i("onSelectPosition() selectPosition = $selectPosition")
                    if (selectPosition != -1 && selectPosition == adapter.data.size - 1) {
                        binding.ivClassifyThirdLeftBg.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_classify_item_bg_bottom_night else R.drawable.ic_classify_item_bg_bottom_day)
                        binding.ivClassifyThirdLeftBg.setBackground(R.drawable.ic_classify_item_bg_bottom_day, R.drawable.ic_classify_item_bg_bottom_night)
                    }else{
                        binding.ivClassifyThirdLeftBg.setBackgroundResource(if (NightModeGlobal.isNightMode()) R.drawable.ic_classify_item_bg_middle_night else R.drawable.ic_classify_item_bg_middle_day)
                        binding.ivClassifyThirdLeftBg.setBackground(R.drawable.ic_classify_item_bg_middle_day, R.drawable.ic_classify_item_bg_middle_night)
                    }
                }
            })
        }

        binding.rlClassifyThirdLeft.adapter = searchClassifyCategoryLeftAdapter

        searchClassifyCategoryRightAdapter = SearchClassifyDetailAdapter<SearchChildCategoryInfo>().also { adapter ->
            adapter.setOnListener(object : SearchClassifyDetailAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    //触发搜索操作
                    if (listener != null) {
                        searchClassifyInfo.classifyItemInfo.categoryInfoList[selectFilterIndex].baseInfo =
                            adapter.data[position].baseInfo
                        listener!!.onClassifyClick(
                            getClassifyValue(adapter.data[position].baseInfo.value),
                            searchClassifyInfo.retainState,
                            searchClassifyInfo
                        )
                    }
                    dismissAllowingStateLoss()
                }
            })
        }
        binding.rlClassifyThirdRight.layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        binding.rlClassifyThirdRight.adapter = searchClassifyCategoryRightAdapter

        updateFilterData()
        updateFilterView()
        binding.clRoot.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.clRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)
                updateSelectFilter(selectFilterIndex, false)
            }

        })
//        updateSelectFilter(selectFilterIndex)
        skyBoxBusiness.themeChange().observe(viewLifecycleOwner) { isNight ->
            view?.run { skyBoxBusiness.updateView(this, true) }
            setNight(isNight)
        }
    }

    private fun updateFilterData() {
        if (searchClassifyInfo != null && searchClassifyInfo.classifyItemInfo != null && searchClassifyInfo.classifyItemInfo.categoryInfoList != null && !searchClassifyInfo.classifyItemInfo.categoryInfoList.isEmpty()) {
            for (i in searchClassifyInfo.classifyItemInfo.categoryInfoList.indices) {
                val searchCategoryInfo = searchClassifyInfo.classifyItemInfo.categoryInfoList[i]
                when (i) {
                    0 -> {
                        binding.layoutFilter0.stvText.text = searchCategoryInfo.baseInfo.name
                    }

                    1 -> {
                        binding.layoutFilter1.stvText.text = searchCategoryInfo.baseInfo.name
                    }

                    2 -> {
                        binding.layoutFilter2.stvText.text = searchCategoryInfo.baseInfo.name
                    }

                    3 -> {
                        binding.layoutFilter3.stvText.text = "更多筛选"
                    }
                }
            }
        }
    }

    fun updateFilterView() {
        if (searchClassifyInfo != null && searchClassifyInfo.classifyItemInfo != null && searchClassifyInfo.classifyItemInfo.categoryInfoList != null && !searchClassifyInfo.classifyItemInfo.categoryInfoList.isEmpty()) {
            when (searchClassifyInfo.classifyItemInfo.categoryInfoList.size) {
                1 -> {
                    binding.layoutFilter0.root.visibility = VISIBLE
                    binding.layoutFilter1.root.visibility = GONE
                    binding.layoutFilter2.root.visibility = GONE
                    binding.layoutFilter3.root.visibility = GONE
                }

                2 -> {
                    binding.layoutFilter0.root.visibility = VISIBLE
                    binding.layoutFilter1.root.visibility = VISIBLE
                    binding.layoutFilter2.root.visibility = GONE
                    binding.layoutFilter3.root.visibility = GONE
                }

                3 -> {
                    binding.layoutFilter0.root.visibility = VISIBLE
                    binding.layoutFilter1.root.visibility = VISIBLE
                    binding.layoutFilter2.root.visibility = VISIBLE
                    binding.layoutFilter3.root.visibility = GONE
                }

                4 -> {
                    binding.layoutFilter0.root.visibility = VISIBLE
                    binding.layoutFilter1.root.visibility = VISIBLE
                    binding.layoutFilter2.root.visibility = VISIBLE
                    binding.layoutFilter3.root.visibility = VISIBLE
                }
            }
        }
    }

    private fun updateSelectFilter(selectFilter: Int, withAnimation: Boolean = true) {
        Timber.i("updateSelectFilter() called with: selectFilter = $selectFilter, selectFilterIndex = $selectFilterIndex")
        binding.clClassifySecondLayout.visibility = VISIBLE
        binding.clClassifyThirdLayout.visibility = GONE
        var duration = if (abs(selectFilter - selectFilterIndex) > 1) {
            300// 非相邻的 RadioButton
        } else {
            200// 非相邻的 RadioButton
        }
        selectFilterIndex = selectFilter

        // 判断动画持续时间
        // 计算指示条应该移动到的位置
        var targetX = 0
        when (selectFilter) {
            0 -> {
                targetX = binding.layoutFilter0.root.left + (binding.layoutFilter0.root.width - binding.indicator.width) / 2
                binding.layoutFilter0.root.isSelected = true
                binding.layoutFilter0.isSelect = true
                binding.layoutFilter1.root.isSelected = false
                binding.layoutFilter1.isSelect = false
                binding.layoutFilter2.root.isSelected = false
                binding.layoutFilter2.isSelect = false
                binding.layoutFilter3.root.isSelected = false
                binding.layoutFilter3.isSelect = false
            }

            1 -> {
                targetX = binding.layoutFilter1.root.left + (binding.layoutFilter1.root.width - binding.indicator.width) / 2
                binding.layoutFilter0.root.isSelected = false
                binding.layoutFilter0.isSelect = false
                binding.layoutFilter1.root.isSelected = true
                binding.layoutFilter1.isSelect = true
                binding.layoutFilter2.root.isSelected = false
                binding.layoutFilter2.isSelect = false
                binding.layoutFilter3.root.isSelected = false
                binding.layoutFilter3.isSelect = false
            }

            2 -> {
                targetX = binding.layoutFilter2.root.left + (binding.layoutFilter2.root.width - binding.indicator.width) / 2
                binding.layoutFilter0.root.isSelected = false
                binding.layoutFilter0.isSelect = false
                binding.layoutFilter1.root.isSelected = false
                binding.layoutFilter1.isSelect = false
                binding.layoutFilter2.root.isSelected = true
                binding.layoutFilter2.isSelect = true
                binding.layoutFilter3.root.isSelected = false
                binding.layoutFilter3.isSelect = false
            }

            3 -> {
                targetX = binding.layoutFilter3.root.left + (binding.layoutFilter3.root.width - binding.indicator.width) / 2
                binding.layoutFilter0.root.isSelected = false
                binding.layoutFilter0.isSelect = false
                binding.layoutFilter1.root.isSelected = false
                binding.layoutFilter1.isSelect = false
                binding.layoutFilter2.root.isSelected = false
                binding.layoutFilter2.isSelect = false
                binding.layoutFilter3.root.isSelected = true
                binding.layoutFilter3.isSelect = true
            }
        }
        if (withAnimation) {
            binding.indicator.animate()
                .x(targetX.toFloat())
                .setDuration(duration.toLong())
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        } else {
            binding.indicator.animate()
                .x(targetX.toFloat())
                .setDuration(0L)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
        binding.executePendingBindings()
        updateSearchChildCategory()
    }

    private fun updateSearchChildCategory() {
        Timber.i("updateSearchChildCategory() called")
        if (searchClassifyInfo.classifyItemInfo?.categoryInfoList?.isNotEmpty() == true
            && searchClassifyInfo.classifyItemInfo?.categoryInfoList?.get(selectFilterIndex) != null
        ) {
            searchClassifyCateGoryAdapter.setList(searchClassifyInfo.classifyItemInfo.categoryInfoList[selectFilterIndex].childCategoryInfo)
            if (hasGrandChild(searchClassifyInfo.classifyItemInfo?.categoryInfoList?.get(selectFilterIndex)?.childCategoryInfo)) {
                var leftSelectPos = 0
                for ((index, searchCategoryInfo) in searchClassifyInfo.classifyItemInfo.categoryInfoList[selectFilterIndex].childCategoryInfo.withIndex()) {
                    if (searchCategoryInfo.baseInfo.checked == 1) {
                        leftSelectPos = index
                        break
                    }
                }
                if (searchClassifyInfo.classifyItemInfo.categoryInfoList[selectFilterIndex].childCategoryInfo.isNotEmpty()) {
                    searchClassifyCategoryLeftAdapter.updateData(searchClassifyInfo.classifyItemInfo.categoryInfoList[selectFilterIndex].childCategoryInfo)
                    while (searchClassifyCategoryLeftAdapter.data[leftSelectPos].childCategoryInfoList.isNullOrEmpty()
                        && leftSelectPos < searchClassifyCategoryLeftAdapter.data.size - 1
                    ) {
                        leftSelectPos++
                    }
                    searchClassifyCategoryLeftAdapter.setSelectPosition(leftSelectPos)
                    if (!searchClassifyCategoryLeftAdapter.data[leftSelectPos].childCategoryInfoList.isNullOrEmpty()) {
                        updateRightSearchChildCategory(searchClassifyCategoryLeftAdapter.data[leftSelectPos].childCategoryInfoList)
                    }

                    binding.rlClassifyThirdLeft.scrollToPosition(if (leftSelectPos > 0) leftSelectPos - 1 else leftSelectPos)

                }

                binding.clClassifySecondLayout.visibility = GONE
                binding.clClassifyThirdLayout.visibility = VISIBLE
            } else {
                binding.clClassifySecondLayout.visibility = VISIBLE
                binding.clClassifyThirdLayout.visibility = GONE
            }
            for ((index, searchChildCategoryInfo) in searchClassifyInfo.classifyItemInfo.categoryInfoList[selectFilterIndex].childCategoryInfo.withIndex()) {
                if (searchChildCategoryInfo.baseInfo.checked == 1) {
                    searchClassifyCateGoryAdapter.setSelectPosition(index)
                    return
                }
            }
        }
    }

    private fun hasGrandChild(list: ArrayList<SearchChildCategoryInfo>?): Boolean {
        var flag = false
        list?.let {
            for (categoryInfo in list) {
                if (!categoryInfo.childCategoryInfoList.isNullOrEmpty()) {
                    flag = true
                }
            }
        }
        return flag
    }

    override fun dismiss() {
        dismissAllowingStateLoss()
    }

    override fun dismissAllowingStateLoss() {
        if (isAdded) {
            super.dismissAllowingStateLoss()
//            removeViewFromWindow()
        }
    }

    private fun initBinding() {
        binding.lifecycleOwner = viewLifecycleOwner
//        doWindowManager() //窗口操作
        dialog?.setCanceledOnTouchOutside(false) //空白处不能取消动画
        isCancelable = false //返回键不能取消
    }


    fun updateLeftSearchChildCategory(list: List<SearchChildCategoryInfo>, selectIndex: Int) {
        if (list.isNotEmpty()) {
            searchClassifyCategoryLeftAdapter.updateData(list)
            searchClassifyCategoryLeftAdapter.setSelectPosition(selectIndex)
            if (!searchClassifyCategoryLeftAdapter.data[selectIndex].childCategoryInfoList.isNullOrEmpty()) {
                updateRightSearchChildCategory(searchClassifyCategoryLeftAdapter.data[selectIndex].childCategoryInfoList)
            }
            binding.rlClassifyThirdLeft.scrollToPosition(selectIndex)

        }
    }

    fun updateRightSearchChildCategory(list: ArrayList<SearchChildCategoryInfo>) {
        if (list.isNotEmpty()) {
            searchClassifyCategoryRightAdapter.setList(list)
            for ((index, searchChildCategoryInfo) in list.withIndex()) {
                if (searchChildCategoryInfo.baseInfo.checked == 1) {
                    searchClassifyCategoryRightAdapter.setSelectPosition(index)
                    return
                }
            }
            searchClassifyCategoryRightAdapter.setSelectPosition(-1)
        }
    }

    //设置日夜模式
    fun setNight(isNight: Boolean) {
        searchClassifyCateGoryAdapter.notifyDataSetChanged()
        searchClassifyCategoryLeftAdapter.notifyDataSetChanged()
        searchClassifyCategoryRightAdapter.notifyDataSetChanged()
    }

    fun setOnClassifyClickListener(listener: OnClassifyClickListener) {
        this.listener = listener
    }

    interface OnClassifyClickListener {
        fun onClassifyClick(value: String, retainState: String, searchClassifyInfo: SearchClassifyInfo)
    }

    override fun onClick(v: View) {
        val viewID = v.id
        if (viewID == R.id.layout_filter0) {
            if (selectFilterIndex == 0) {
                dismissAllowingStateLoss()
            } else {
                updateSelectFilter(0)
            }
        } else if (viewID == R.id.layout_filter1) {
            if (selectFilterIndex == 1) {
                dismissAllowingStateLoss()
            } else {
                updateSelectFilter(1)
            }
        } else if (viewID == R.id.layout_filter2) {
            if (selectFilterIndex == 2) {
                dismissAllowingStateLoss()
            } else {
                updateSelectFilter(2)
            }
        } else if (viewID == R.id.layout_filter3) {
            if (selectFilterIndex == 3) {
                dismissAllowingStateLoss()
            } else {
                updateSelectFilter(3)
            }
        }
    }

    fun getClassifyValue(value: String): String {
        val param = StringBuilder()
        for (i in searchClassifyInfo.classifyItemInfo.categoryInfoList.indices) {
            if (i == selectFilterIndex) {
                param.append(value).append("+")
            } else {
                for (searchChildCategoryInfo1 in searchClassifyInfo.classifyItemInfo.categoryInfoList[i].childCategoryInfo) {
                    if (searchChildCategoryInfo1.baseInfo.checked == 1) {
                        if (searchChildCategoryInfo1.childCategoryInfoList != null && searchChildCategoryInfo1.childCategoryInfoList.size > 0) {
                            for (childCategoryInfo2 in searchChildCategoryInfo1.childCategoryInfoList) {
                                if (childCategoryInfo2.baseInfo.checked == 1) {
                                    param.append(childCategoryInfo2.baseInfo.value).append("+")
                                }
                            }
                        } else {
                            param.append(searchChildCategoryInfo1.baseInfo.value).append("+")
                        }
                    }
                }
            }
        }
        return param.substring(0, param.length - 1)
    }

    companion object {
        fun builder(): ClassifyFragment {
            return ClassifyFragment()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 设置对话框宽度
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        return dialog
    }

    override fun onStop() {
        super.onStop()
//        removeViewFromWindow()
    }
}