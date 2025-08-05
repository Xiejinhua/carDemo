package com.desaysv.psmap.ui.search.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.constant.RefreshState
import timber.log.Timber


/**
 * @author 张楠
 * @time 2024/12/03
 * @description
 */
class CustomRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SmartRefreshLayout(context, attrs) {
    private val mHeaderView: CustomClassicsHeader = CustomClassicsHeader(context)
    private val mFooterView: CustomClassicsFooter = CustomClassicsFooter(context)
    private var mTotalPage = 0 //总页码
    private var mCurrentPage = 0 //当前页码

    init {
        setRefreshHeader(mHeaderView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setRefreshFooter(mFooterView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setIsTrip()
    }

    override fun notifyStateChanged(state: RefreshState?) {
        super.notifyStateChanged(state)
    }

    //设置日夜模式
    fun setNight(isNight: Boolean) {
        mHeaderView.setNight(isNight)
        mFooterView.setNight(isNight)
    }

    //是否是我的行程界面
    fun setIsTrip(isTrip: Int = 0) {
        mHeaderView.setIsTrip(isTrip)
        mFooterView.setIsTrip(isTrip)
    }

    fun setTotalPage(totalPage: Int) {
        Timber.i("setTotalPage() called with: totalPage = $totalPage")
        mTotalPage = totalPage
        handlePage()
    }

    fun setCurrentPage(currentPage: Int) {
        mCurrentPage = currentPage
        mHeaderView.page = currentPage
        mFooterView.page = currentPage
        handlePage()
    }

    //处理边界情况
    @SuppressLint("Range")
    fun handlePage() {
        if (mCurrentPage in 1..mTotalPage) {
            if (mCurrentPage > 1) { //当前页码大于1，可以上一页
                //设置下拉刷新判定的距离来间接实现
                setHeaderTriggerRate(1f)
            } else { //当前页码等于1，不可以下拉刷新至上一页。将刷新判定的距离修改为3f，超过2.5f就行，下拉就不会再触发Refresh
                setHeaderTriggerRate(3f)
            }
            if (mCurrentPage < mTotalPage) {
                setFooterTriggerRate(1f)
            } else { //当前页码小于总页码，可以上拉刷新至下一页。将刷新判定的距离修改为3f，超过2.5f就行，上拉就不会再触发Load
                setFooterTriggerRate(3f)
            }
        }
    }


}