package com.desaysv.psmap.ui.adapter

import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.GridLayoutManager
import com.autonavi.auto.skin.NightModeGlobal
import com.autosdk.bussiness.common.POI
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.desaysv.psmap.R
import com.desaysv.psmap.databinding.ItemSearchResultBinding
import com.desaysv.psmap.model.utils.ViewClickEffectUtils
import com.desaysv.psmap.model.utils.ViewClickEffectUtils.CLICKED_SCALE_90
import com.desaysv.psmap.model.utils.setDebouncedOnClickListener
import com.desaysv.psmap.ui.search.bean.SearchResultBean
import timber.log.Timber


class SearchResultAdapter(
    private var selectPos: Int = -1, //选中的item，默认0
    private var childSelectPos: Int = -1,//选中的子POI，没有选中时则为-1
    private var childPois: ArrayList<POI> = ArrayList(), //子poi
    private var childPoisAdapterMap: HashMap<Int, SearchResultChildAdapter> = HashMap(),
) : BaseQuickAdapter<SearchResultBean, BaseDataBindingHolder<ItemSearchResultBinding>>(R.layout.item_search_result) {


    /**
     * 当前选中的pos
     */
    fun getSelection(): Int {
        return selectPos
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemSearchResultBinding>,
        item: SearchResultBean
    ) {
        holder.dataBinding?.run {
            item.isSelect = holder.layoutPosition == selectPos
            this.item = item
            this.position = holder.layoutPosition + 1
            this.isNight = NightModeGlobal.isNightMode()
            this.executePendingBindings()
            if (item.type != 0) {
                val constraintSet = ConstraintSet()
                constraintSet.clone(this.top)
                constraintSet.connect(
                    R.id.stv_text_address,
                    ConstraintSet.END,
                    R.id.goto_there,
                    ConstraintSet.START,
                    20
                )
                constraintSet.applyTo(this.top)
            }
            this.gotoThere.setDebouncedOnClickListener {
                goThere(holder.layoutPosition)
            }
            this.top.setDebouncedOnClickListener {
                setSelection(holder.layoutPosition)
            }
            childPoiLayout(this, holder.layoutPosition)
            ViewClickEffectUtils.addClickScale(gotoThere, CLICKED_SCALE_90)
        }
    }

    fun updateData(poiList: List<SearchResultBean>?) {
        poiList?.let { setList(it) }
        notifyDataSetChanged()
    }

    fun updateData(bean: SearchResultBean, position: Int) {
        data[position] = bean
        notifyItemChanged(position)
    }

    fun reSetSelect() {
        selectPos = -1
        childSelectPos = -1
    }

    /**
     * 设置选中的pos
     */
    fun setSelection(position: Int) {
        Timber.i("setSelection() called with: position = $position")
        if (selectPos == position) {
            val item = data[selectPos]
//            selectPos = -1
            setupChildStatus(position)
//            childSelectPos = -1;
//            notifyItemChanged(position)
            onSearchResultItemClickListener?.onItemClick(-1, item)
        } else {
            notifyItemChanged(selectPos)
            selectPos = position
            val item = data[selectPos]
            childSelectPos = -1;
            setupChildStatus(position)
            notifyItemChanged(position)
            onSearchResultItemClickListener?.onItemClick(position, item)
        }
    }

    /**
     * 设置选中的子pos,
     */
    fun setChildSelection(parentPosition: Int, childPosition: Int) {
        if (childSelectPos == childPosition) {
            childPoisAdapterMap[parentPosition]?.toggleSelectPosition(childPosition)
            childSelectPos = -1
        } else {
            childPoisAdapterMap[parentPosition]?.toggleSelectPosition(childPosition)
            childSelectPos = childPosition
            //展开子Poi列表
            setupChildStatus(parentPosition)
            notifyItemChanged(parentPosition)
        }

    }


    //子POI 布局显隐及操作
    private fun childPoiLayout(dataBinding: ItemSearchResultBinding, parentPosition: Int) {
        val childPoisAdapter = SearchResultChildAdapter()
        childPoisAdapter.isSearchResult = true
        childPoisAdapterMap.put(parentPosition, childPoisAdapter)
        dataBinding.ctSearchChildStation.layoutManager =
            GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
        dataBinding.ctSearchChildStation.adapter = childPoisAdapter

        childPoisAdapter.let { adapter ->
            adapter.updateData(childPois)
            if (childSelectPos != -1) {
                adapter.setSelectPosition(childSelectPos)
            }
            adapter.setOnSearchResultChildListener(object :
                SearchResultChildAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    onSearchResultItemClickListener?.let {
                        val selected = adapter.toggleSelectPosition(position)
                        childSelectPos = if (selected) position else -1
                        it.onChildPoiItemClick(
                            parentPosition,
                            childSelectPos,
                            adapter.getItem(position)
                        )
                    }
                }
            })
        }
    }

    //子POI 布局状态更改
    private fun setupChildStatus(position: Int) {
        Timber.i("setupChildStatus() called with: position = $position,selectPos = $selectPos")
        if (selectPos == position) {
            Timber.i("childPoiLayout position: ok")
            data[position].isChildLayout = false
            data[position].isMoreVisible = false
            childPois.clear()
            if (data[position].poi?.childPois.isNullOrEmpty()) {
                data[position].run {
                    this.isMoreVisible = false
                    this.isChildLayout = false
                    childPois.addAll(ArrayList())
                }
            } else if (data[position].poi?.childPois?.size!! <= 4) {
                data[position].run {
                    this.moreText = "更多"
                    this.isMoreVisible = false
                    this.isChildLayout = true
                    this.poi?.childPois?.let {
                        childPois.addAll(it)
                    }
                }
            } else {
                data[position].run {
                    this.moreText = "更多"
                    this.poi?.childPois?.forEach{
                        childPois.add(it)
                    }
                    this.isMoreVisible = true
                    this.isChildLayout = true
                }
            }
        } else {
            data[position].run {
                this.isChildLayout = false
                this.isMoreVisible = false
            }
        }
    }

    /**
     * 当前选中的poi item对象
     */
    fun getSelectionPoi() = if (selectPos == -1) null else getItem(selectPos).poi

    /**
     * 当前选中的子POI对象
     */
    fun getChildSelectionPoi(): POI? {
        val childPois: ArrayList<POI>? = getSelectionPoi()?.childPois
        val childSize = childPois?.size ?: 0
        return if (childSelectPos in 0 until childSize && childPois != null) {
            childPois[childSelectPos]
        } else null
    }

    //进入路线规划
    private fun goThere(position: Int) {
        onSearchResultItemClickListener?.onGoThere(position)
    }

    private fun doFavorite(position: Int) {
        onSearchResultItemClickListener?.onFavorite(position)
    }

    //进入周边搜
    private fun doSearchAround(item: POI?) {
        val poi = getChildSelectionPoi() ?: item
        onSearchResultItemClickListener?.toAround(poi)
    }

    private var onSearchResultItemClickListener: OnSearchResultItemClickListener? = null

    fun setOnItemClickListener(listener: OnSearchResultItemClickListener) {
        this.onSearchResultItemClickListener = listener
    }

    interface OnSearchResultItemClickListener {

        /**
         * 点击item
         *
         * @param position  选中的item的下标，没有选中时则为-1
         * @param resultBean 选中的item
         */
        fun onItemClick(position: Int, resultBean: SearchResultBean)

        /**
         * 进行收藏
         *
         * @param position
         */
        fun onFavorite(position: Int)

        /**
         * 进行路线规划
         *
         * @param poi
         */
        fun onGoThere(position: Int)

        /**
         * 进行周边搜
         *
         * @param position
         */
        fun toAround(poi: POI?)

        /**
         * 选中子POI,默认-1为不选中
         *
         * @param position  选中的父poi的下标
         * @param resultBean 选中的item
         */
        fun onChildPoiItemClick(position: Int, childPosition: Int, childPoi: POI?)
    }
}