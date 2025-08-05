package com.desaysv.psmap.ui.search.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.GridLayoutManager
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemSearchDetailCategoryBinding
import com.desaysv.psmap.databinding.ViewSearchCategoryBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_95
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchCategoryBean

class SearchCategoryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: ViewSearchCategoryBinding = ViewSearchCategoryBinding.inflate(LayoutInflater.from(context), this, true)
    private val categoryAdapter: CategoryAdapter

    init {
        val gridLayoutManager = GridLayoutManager(context, 3, GridLayoutManager.VERTICAL, false)
        binding.searchCategoryListview.layoutManager = gridLayoutManager
        categoryAdapter = CategoryAdapter().also { adapter ->
            adapter.setOnItemClickListener(object : OnItemClickListener {
                override fun onItemClick(name: String) {
                    onItemClickListener?.onItemClick(name)
                }
            })
        }
        binding.searchCategoryListview.adapter = categoryAdapter
    }

    fun init(title: String, list: List<SearchCategoryBean>, listener: OnItemClickListener) {
        binding.searchCategoryTitle.text = title
        categoryAdapter.updateData(list)
        this.onItemClickListener = listener
    }

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(name: String)
    }

    class CategoryAdapter :
        BaseQuickAdapter<SearchCategoryBean, BaseDataBindingHolder<ItemSearchDetailCategoryBinding>>(R.layout.item_search_detail_category) {

        override fun convert(holder: BaseDataBindingHolder<ItemSearchDetailCategoryBinding>, item: SearchCategoryBean) {
            // 获取 Binding
            holder.dataBinding?.run {
                searchDetailCategoryName.text = item.name
                searchDetailCategoryName.setDebouncedOnClickListener {
                    onItemClickListener?.onItemClick(item.name)
                }
                ViewClickEffectUtils.addClickScale(searchDetailCategoryName, CLICKED_SCALE_95)
            }
        }

        fun updateData(list: List<SearchCategoryBean>?) {
            list?.let { setList(it) }
            notifyDataSetChanged()
        }

        private var onItemClickListener: OnItemClickListener? = null
        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.onItemClickListener = listener
        }
    }
}