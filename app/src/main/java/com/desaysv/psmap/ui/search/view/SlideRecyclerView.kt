package com.desaysv.psmap.ui.search.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.Scroller
import androidx.recyclerview.widget.LinearLayoutManager
import com.autonavi.auto.skin.view.SkinRecyclerView
import timber.log.Timber
import kotlin.math.abs

/**
 * @author 谢锦华
 * @date 2023/04/22
 * @project： 左滑RecyclerView
 */
class SlideRecyclerView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) :
    SkinRecyclerView(context, attrs, defStyle) {
    private var mVelocityTracker: VelocityTracker? = null // 速度追踪器
    private val mTouchSlop = ViewConfiguration.get(context!!).scaledTouchSlop // 认为是滑动的最小距离（一般由系统提供）
    private var mTouchFrame: Rect? = null // 子View所在的矩形范围
    private val mScroller = Scroller(context)
    private var mLastX = 0f // 滑动过程中记录上次触碰点X
    private var mFirstX = 0f
    private var mFirstY = 0f // 首次触碰范围
    private var mIsSlide = false // 是否滑动子View
    private var mFlingView: ViewGroup? = null // 触碰的子View
    private var mPosition = 0 // 触碰的view的位置
    private var mMenuViewWidth = 0 // 菜单按钮宽度

    companion object {
        private const val TAG = "SlideRecyclerView"
        private const val INVALID_POSITION = -1 // 触摸到的点不在子View范围内
        private const val INVALID_CHILD_WIDTH = -1 // 子ItemView不含两个子View
        private const val SNAP_VELOCITY = 600 // 最小滑动速度
    }

    fun setSlide(slide: Boolean) {
        isSlide = slide
    }

    private var isSlide = true

    override fun onInterceptTouchEvent(e: MotionEvent): Boolean {
        if (!isSlide) {
            return super.onInterceptTouchEvent(e)
        }
        val x = e.x.toInt()
        val y = e.y.toInt()
        obtainVelocity(e)
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!mScroller.isFinished) {  // 如果动画还没停止，则立即终止动画
                    mScroller.abortAnimation()
                }
                run {
                    mLastX = x.toFloat()
                    mFirstX = mLastX
                }
                mFirstY = y.toFloat()
                mPosition = pointToPosition(x, y) // 获取触碰点所在的position
                if (mPosition != INVALID_POSITION) {
                    val view: View? = mFlingView
                    // 获取触碰点所在的view
                    mFlingView = getChildAt(mPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()) as ViewGroup
                    // 这里判断一下如果之前触碰的view已经打开，而当前碰到的view不是那个view则立即关闭之前的view，此处并不需要担动画没完成冲突，因为之前已经abortAnimation
                    if (view != null && mFlingView !== view && view.scrollX != 0) {
                        view.scrollTo(0, 0)
                    }
                    // 这里进行了强制的要求，RecyclerView的子ViewGroup必须要有2个子view,这样菜单按钮才会有值，
                    // 需要注意的是:如果不定制RecyclerView的子View，则要求子View必须要有固定的width。
                    // 比如使用LinearLayout作为根布局，而content部分width已经是match_parent，此时如果菜单view用的是wrap_content，menu的宽度就会为0。
                    mMenuViewWidth = if (mFlingView!!.childCount == 2) {
                        mFlingView!!.getChildAt(1).width
                    } else {
                        INVALID_CHILD_WIDTH
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                mVelocityTracker!!.computeCurrentVelocity(1000)
                // 此处有俩判断，满足其一则认为是侧滑：
                // 1.如果x方向速度大于y方向速度，且大于最小速度限制；
                // 2.如果x方向的侧滑距离大于y方向滑动距离，且x方向达到最小滑动距离；
                val xVelocity = mVelocityTracker!!.xVelocity
                val yVelocity = mVelocityTracker!!.yVelocity
                if (abs(xVelocity.toDouble()) > SNAP_VELOCITY && abs(xVelocity.toDouble()) > abs(yVelocity.toDouble())
                    || abs((x - mFirstX).toDouble()) >= mTouchSlop
                    && abs((x - mFirstX).toDouble()) > abs((y - mFirstY).toDouble())
                ) {
                    mIsSlide = true
                    return true
                }
            }

            MotionEvent.ACTION_UP -> releaseVelocity()
        }
        return super.onInterceptTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!isSlide) {
            return super.onTouchEvent(e)
        }
        if (mIsSlide && mPosition != INVALID_POSITION) {
            val x = e.x
            obtainVelocity(e)
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {}
                MotionEvent.ACTION_MOVE -> {          // 随手指滑动
                    if (mMenuViewWidth != INVALID_CHILD_WIDTH && mMenuViewWidth != 0) {
                        val dx = mLastX - x
                        if (mFlingView!!.scrollX + dx <= mMenuViewWidth
                            && mFlingView!!.scrollX + dx > 0
                        ) {
                            mFlingView!!.scrollBy(dx.toInt(), 0)
                        } else if (mFlingView!!.scrollX + dx > mMenuViewWidth && mFlingView!!.scrollX < mMenuViewWidth) { //处理按住滑动到左侧总是差1px的问题
                            mFlingView!!.scrollBy(1, 0)
                        } else if (mFlingView!!.scrollX + dx <= mMenuViewWidth && mFlingView!!.scrollX > 0) { //处理按住滑动到右侧总是差1px的问题
                            mFlingView!!.scrollBy(-1, 0)
                        }
                        mLastX = x
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (mMenuViewWidth != INVALID_CHILD_WIDTH && mMenuViewWidth != 0) {
                        val scrollX = mFlingView!!.scrollX
                        mVelocityTracker!!.computeCurrentVelocity(1000)
                        // 此处有两个原因决定是否打开菜单：
                        // 1.菜单被拉出宽度大于菜单宽度一半；
                        // 2.横向滑动速度大于最小滑动速度；
                        // 注意：之所以要小于负值，是因为向左滑则速度为负值
                        if (mVelocityTracker!!.xVelocity < -SNAP_VELOCITY) {    // 向左侧滑达到侧滑最低速度，则打开
                            mScroller.startScroll(scrollX, 0, mMenuViewWidth - scrollX, 0)
                        } else if (mVelocityTracker!!.xVelocity >= SNAP_VELOCITY) {  // 向右侧滑达到侧滑最低速度，则关闭
                            mScroller.startScroll(scrollX, 0, -scrollX, 0, abs(scrollX.toDouble()).toInt())
                        } else if (scrollX >= mMenuViewWidth / 2) { // 如果超过删除按钮一半，则打开
                            mScroller.startScroll(scrollX, 0, mMenuViewWidth - scrollX, 0)
                        } else {    // 其他情况则关闭
                            mScroller.startScroll(scrollX, 0, -scrollX, 0)
                        }
                        invalidate()
                    }
                    mMenuViewWidth = INVALID_CHILD_WIDTH
                    mIsSlide = false
                    mPosition = INVALID_POSITION
                    releaseVelocity() // 这里之所以会调用，是因为如果前面拦截了，就不会执行ACTION_UP,需要在这里释放追踪
                }
            }
            return true
        } else {
            // 此处防止RecyclerView正常滑动时，还有菜单未关闭
//            closeMenu()
            // Velocity，这里的释放是防止RecyclerView正常拦截了，但是在onTouchEvent中却没有被释放；
            // 有三种情况：1.onInterceptTouchEvent并未拦截，在onInterceptTouchEvent方法中，DOWN和UP一对获取和释放；
            // 2.onInterceptTouchEvent拦截，DOWN获取，但事件不是被侧滑处理，需要在这里进行释放；
            // 3.onInterceptTouchEvent拦截，DOWN获取，事件被侧滑处理，则在onTouchEvent的UP中释放。
            releaseVelocity()
        }
        return super.onTouchEvent(e)
    }

    private fun releaseVelocity() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.clear()
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    private fun obtainVelocity(event: MotionEvent) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
    }

    fun pointToPosition(x: Int, y: Int): Int {
        Timber.i("pointToPosition x=$x, y=$y")
        if (null == layoutManager) return INVALID_POSITION
        val firstPosition = (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        var frame = mTouchFrame
        if (frame == null) {
            mTouchFrame = Rect()
            frame = mTouchFrame
        }

        val count = childCount
        for (i in count - 1 downTo 0) {
            val child = getChildAt(i)
            if (child.visibility == VISIBLE) {
                child.getHitRect(frame)
                if (frame!!.contains(x, y)) {
                    return firstPosition + i
                }
            }
        }
        Timber.i("pointToPosition return INVALID_POSITION")
        return INVALID_POSITION
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mFlingView!!.scrollTo(mScroller.currX, mScroller.currY)
            invalidate()
        }
    }

    /**
     * 将显示子菜单的子view关闭
     * 这里本身是要自己来实现的，但是由于不定制item，因此不好监听器点击事件，因此需要调用者手动的关闭
     */
    fun closeMenu(bAnimation: Boolean = false) {
        Timber.i("closeMenu")
        if (bAnimation) {
            mFlingView?.let { flingView ->
                if (flingView.scrollX != 0) {
                    val startX = flingView.scrollX
                    val startY = flingView.scrollY
                    mScroller.startScroll(startX, startY, -startX, -startY, 1000)
                }
            }
        } else {
            mFlingView?.let { flingView ->
                if (flingView.scrollX != 0) {
                    flingView.scrollTo(0, 0)
                }
            }
        }
    }

    //手动打开
    fun openMenu() {
        Timber.i("openMenu")
        mFlingView?.scrollTo(mMenuViewWidth, 0)
    }
}
