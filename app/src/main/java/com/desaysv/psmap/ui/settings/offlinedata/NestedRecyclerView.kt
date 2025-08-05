package com.desaysv.psmap.ui.settings.offlinedata

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

/**
 * @author 王漫生
 * @project：个性化地图——自定义ExpandableListView
 */
class NestedRecyclerView : RecyclerView {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context!!, attrs, defStyleAttr
    )

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMeasureSpecCustom = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, heightMeasureSpecCustom)
        val params = layoutParams // 存在一个问题 ，如果是全部收起的话 ，就会导致页面空白
        params.height = measuredHeight
    }
}
