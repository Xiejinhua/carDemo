package com.desaysv.psmap.model.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.desaysv.psmap.model.R

/**
 * 带进度的进度条，线程安全的View，可直接在线程中更新进度
 */
class RoundProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {
    /**
     * 画笔对象的引用
     */
    private val paint: Paint = Paint()

    /**
     * 圆环的颜色
     */
    private var circleColor: Int

    /**
     * 圆环进度的颜色
     */
    private var circleProgressColor: Int

    /**
     * 中间进度百分比的字符串的颜色
     */
    var textColor: Int

    /**
     * 中间进度百分比的字符串的字体
     */
    var textSize: Float

    /**
     * 圆环的宽度
     */
    private var roundWidth: Float

    /**
     * 最大进度
     */
    private var max: Int

    /**
     * 当前进度
     */
    private var progress: Int = 0

    /**
     * 进度圆环的起始角度
     */
    private var startAngle: Int

    /**
     * 进度圆环的扫描角度
     */
    private var sweepAngle: Int

    /**
     * 是否显示中间的进度
     */
    private var textIsDisplayable: Boolean

    /**
     * 进度的风格，实心或者空心
     */
    private val style: Int

    private val STROKE = 0
    private val FILL = 1

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar)

        // 获取自定义属性和默认值
        progress = typedArray.getInt(R.styleable.RoundProgressBar_progressInt, progress)
        circleColor = typedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED)
        circleProgressColor = typedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN)
        textColor = typedArray.getColor(R.styleable.RoundProgressBar_txtColor, Color.GREEN)
        textSize = typedArray.getDimension(R.styleable.RoundProgressBar_txtSize, 15f)
        roundWidth = typedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5f)
        max = typedArray.getInt(R.styleable.RoundProgressBar_max, 100)
        startAngle = typedArray.getInt(R.styleable.RoundProgressBar_startAngle, 0)
        sweepAngle = typedArray.getInt(R.styleable.RoundProgressBar_sweepAngle, 360)
        textIsDisplayable = typedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable, true)
        style = typedArray.getInt(R.styleable.RoundProgressBar_style, 0)
        typedArray.recycle()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        /**
         * 画最外层的大圆环
         */
        val centre = width / 2 // 获取圆心的x坐标
        val radius = (centre - roundWidth / 2).toInt() // 圆环的半径
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = circleColor // 设置圆环的颜色
        paint.style = Paint.Style.FILL // 设置空心
        //		paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
        paint.isAntiAlias = true // 消除锯齿
        canvas.drawCircle(centre.toFloat(), centre.toFloat(), radius.toFloat(), paint) // 画出圆环
        /**
         * 画进度百分比
         */
        paint.strokeWidth = 0f
        paint.color = textColor
        paint.textSize = textSize
        paint.typeface = Typeface.DEFAULT_BOLD // 设置字体
        val percent = (progress.toFloat() / max.toFloat() * 100).toInt() // 中间的进度百分比，先转换成float在进行除法运算，不然都为0
        val textWidth = paint.measureText("$percent%") // 测量字体宽度，我们需要根据字体的宽度设置在圆环中间
        if (textIsDisplayable && percent != 0 && style == STROKE) {
            canvas.drawText("$percent%", centre - textWidth / 2, centre + textSize / 2, paint) // 画出进度百分比
        }
        /**
         * 画圆弧 ，画圆环的进度
         */

        // 设置进度是实心还是空心
        paint.strokeWidth = roundWidth // 设置圆环的宽度
        paint.color = circleColor // 设置进度的颜色
        val oval = RectF(
            (centre - radius).toFloat(),
            (centre - radius).toFloat(),
            (centre + radius).toFloat(),
            (centre + radius).toFloat()
        ) // 用于定义的圆弧的形状和大小的界限
        when (style) {
            STROKE -> {
                paint.style = Paint.Style.STROKE
                canvas.drawArc(oval, startAngle.toFloat(), sweepAngle.toFloat(), false, paint) // 根据进度画圆弧
                paint.color = circleProgressColor // 设置进度的颜色
                paint.style = Paint.Style.STROKE
                canvas.drawArc(oval, startAngle.toFloat(), (sweepAngle * progress / max).toFloat(), false, paint) // 根据进度画圆弧
            }

            FILL -> {
                paint.style = Paint.Style.FILL_AND_STROKE
                if (progress != 0) canvas.drawArc(oval, 0f, (360 * progress / max).toFloat(), true, paint) // 根据进度画圆弧
            }
        }
    }

    @Synchronized
    fun getMax(): Int {
        return max
    }

    /**
     * 设置进度的最大值
     *
     * @param max
     */
    @Synchronized
    fun setMax(max: Int) {
        require(max >= 0) { "max not less than 0" }
        this.max = max
    }

    /**
     * 获取进度.需要同步
     *
     * @return
     */
    @Synchronized
    fun getProgress(): Int {
        return progress
    }

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
     *
     * @param progress
     */
    @Synchronized
    fun setProgress(progress: Int) {
        require(progress >= 0) { "progress not less than 0" }
        this.progress = if (progress > max) max else progress
        postInvalidate()
    }

    /**
     * 设置圆环进度的颜色，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
     * @param isNight
     */
    @Synchronized
    fun setCircleProgressColor(isNight: Boolean) {
        this.circleProgressColor = if (isNight) {
            ContextCompat.getColor(context, R.color.primaryContainerNight)
        } else {
            ContextCompat.getColor(context, R.color.primaryContainerDay)
        }
        postInvalidate()
    }

    /**
     * 设置圆环的颜色
     */
    @Synchronized
    fun setCircleColor(circleColor: Int) {
        this.circleColor = circleColor
        postInvalidate()
    }

    /**
     * 设置圆环的宽度
     */
    @Synchronized
    fun setRoundWidth(roundWidth: Float) {
        this.roundWidth = roundWidth
        postInvalidate()
    }

    /**
     * 设置进度圆环的起始角度
     */
    @Synchronized
    fun setStartAngle(startAngle: Int) {
        this.startAngle = startAngle
        postInvalidate()
    }

    /**
     * 设置进度圆环的扫描角度
     */
    @Synchronized
    fun setSweepAngle(sweepAngle: Int) {
        this.sweepAngle = sweepAngle
        postInvalidate()
    }

    /**
     * 设置是否显示中间的进度
     */
    @Synchronized
    fun setTextIsDisplayable(textIsDisplayable: Boolean) {
        this.textIsDisplayable = textIsDisplayable
        postInvalidate()
    }
}
