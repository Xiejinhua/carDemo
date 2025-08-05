package com.desaysv.psmap.ui.adapter.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.autonavi.auto.skin.NightModeGlobal
import com.chad.library.adapter.base.BaseQuickAdapter
import com.desaysv.psmap.R
import timber.log.Timber

class RecyclerViewItemTouchHelper(recycleViewAdapter: BaseQuickAdapter<*, *>) :
    ItemTouchHelper.Callback() {
    private val recycleViewAdapter: BaseQuickAdapter<*, *>

    init {
        Timber.i(TAG, "into RecyclerViewItemTouchHelper")
        this.recycleViewAdapter = recycleViewAdapter
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags =
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        val swipeFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        recyclerView.parent.requestDisallowInterceptTouchEvent(true)
        //得到当拖拽的viewHolder的Position
        val fromPosition = viewHolder.adapterPosition
        //拿到当前拖拽到的item的viewHolder
        val toPosition = target.adapterPosition
        recycleViewAdapter.notifyItemMoved(fromPosition, toPosition)
        Timber.i(" fromPosition = $fromPosition toPosition$toPosition")
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    /**
     * 长按选中Item时修改颜色
     *
     * @param viewHolder
     * @param actionState
     */
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        Timber.i(" onSelectedChanged = actionState %s", actionState)
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            recycleViewAdapter.notifyItemChanged(0)
            viewHolder?.itemView?.setBackgroundResource(R.drawable.shape_bg_adapter_item_transparent)
        } else if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            if (viewHolder != null) {
                if (NightModeGlobal.isNightMode()) {
                    viewHolder.itemView.setBackgroundResource(R.drawable.shape_bg_adapter_item_night)
                } else {
                    viewHolder.itemView.setBackgroundResource(R.drawable.shape_bg_adapter_item_day)
                }
            }
        }
    }

    /**
     * 手指松开的时候还原颜色
     *
     * @param recyclerView
     * @param viewHolder
     */
    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        //viewHolder.itemView.setBackground(getDrawable(R.drawable.card));
        viewHolder.itemView.setBackgroundResource(R.drawable.shape_bg_adapter_item_transparent)
    }

    /**
     * 重写拖拽不可用
     *
     * @return
     */
    override fun isLongPressDragEnabled(): Boolean {
        return true
    }

    companion object {
        private val TAG = RecyclerViewItemTouchHelper::class.java.simpleName
    }
}
