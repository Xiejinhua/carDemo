package com.desaysv.psmap.ui.settings.vehicle

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnKeyListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.autonavi.auto.skin.NightModeGlobal
import com.desaysv.psmap.model.R
import timber.log.Timber

/**
 * @author 王漫生
 * @project：自定义车辆显示框及输入键盘
 */
class LicensePlateView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    RelativeLayout(context, attrs, defStyleAttr), View.OnClickListener {
    private val editText: EditText
    private val textViews: Array<TextView?>
    private val mActivity: Activity
    private lateinit var mNumView: View
    private lateinit var mProvinceView: View
    private var select_num_up: TextView? = null
    private var select_province_up: TextView? = null
    private var count = 0
    var updateViewPosition = 0
    private var itemViewCount = 7
    private var inputContent: String
    private var isUpdateView = false //是否更新view内容
    private var isSetLast = false
    private val stringBuffer = StringBuilder()
    private val mTouchListener = OnFrameTouchListener()

    private fun setListener() {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //不做处理
            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                //不做处理
            }

            override fun afterTextChanged(editable: Editable) {
                //如果字符不为""时才进行操作
                if (editable.toString() != "") {
                    if (stringBuffer.length > itemViewCount - 1) {
                        //当文本长度大于 ITEM_VIEW_COUNT - 1 位时 EditText 置空
                        editText.setText("")
                        return
                    } else {
                        doEdit(editable)
                    }
                    for (i in stringBuffer.indices) {
                        textViews[i]!!.text = inputContent[i].toString()
                        if (i > 0) {
                            textViews[i]!!.setTextColor(onSetTextColor(if (NightModeGlobal.isNightMode()) R.color.onPrimaryNight else R.color.onPrimaryDay))
                        }
                    }
                    setTextViewsBackground(count)
                }
            }
        })
        editText.setOnKeyListener(OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_DEL
                && event.action == KeyEvent.ACTION_DOWN
            ) {
                onKeyDelete(false)
                return@OnKeyListener true
            }
            false
        })
    }

    private fun doEdit(editable: Editable) {
        //将文字添加到 StringBuffer 中
        Timber.d(" doEdit stringBuffer 1 stringBuffer:$stringBuffer updateViewPosition:$updateViewPosition")
        if (count > 0) {
            if (!TextUtils.isEmpty(textViews[count - 1]!!.text.toString())) {
                Timber.d(" doEdit textViews:%s", textViews[count - 1]!!.text.toString())
                stringBuffer.delete(count, count + 1)
                Timber.d(" doEdit stringBuffer  2 stringBuffer:%s", stringBuffer.toString())
            }
        }
        stringBuffer.append(editable)
        Timber.d(" doEdit stringBuffer  3 stringBuffer:%s", stringBuffer.toString())
        //添加后将 EditText 置空  造成没有文字输入的错局
        editText.setText("")
        //记录 stringBuffer 的长度
        count = stringBuffer.length
        inputContent = stringBuffer.toString()
        if (count == 1) {
            mProvinceView.visibility = GONE
            mNumView.visibility = VISIBLE
        }
        if (stringBuffer.length == itemViewCount && inputListener != null) {
            //文字长度为 sbLength  则调用完成输入的监听
            inputListener!!.inputComplete(inputContent)
            mNumView.visibility = GONE
            restoreDefault() //按钮背景恢复原状
        }
    }

    //隐藏省份布局
    fun hideProvinceView() {
        mProvinceView.visibility = GONE
    }

    //隐藏键盘
    fun hideSoftInput() {
        mProvinceView.visibility = GONE
        mNumView.visibility = GONE
        restoreDefault() //按钮背景恢复原状
        select_num_up!!.isSelected = false
        select_province_up!!.isSelected = false
    }

    fun showVehicleSoftInput(): Boolean {
        return mProvinceView.visibility == VISIBLE || mNumView.visibility == VISIBLE
    }

    //设置车牌号
    fun initStringBuffer(number: String?) {
        stringBuffer.delete(0, stringBuffer.length)
        stringBuffer.append(number)
        inputContent = stringBuffer.toString()
        inputListener?.inputComplete(inputContent)
        for (i in stringBuffer.indices) {
            textViews[i]!!.text = inputContent[i].toString()
        }
    }

    //设置省份
    fun initFirstStringBuffer(first: String?) {
        stringBuffer.delete(0, stringBuffer.length)
        stringBuffer.append(first)
        inputContent = stringBuffer.toString()
        for (i in stringBuffer.indices) {
            textViews[i]!!.text = inputContent[i].toString()
        }
    }

    /**
     * 设置框内字体颜色
     */
    fun onSetTextColor(resId: Int): Int {
        return this.resources.getColor(resId)
    }

    fun setKeyboardContainerLayout(layout: RelativeLayout) {
        val mInflater = LayoutInflater.from(mActivity)
        mProvinceView = mInflater.inflate(com.desaysv.psmap.R.layout.view_keyboard_province, null)
        val rlParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rlParams.addRule(ALIGN_PARENT_BOTTOM)
        mProvinceView.layoutParams = rlParams
        select_province_up = mProvinceView.findViewById(com.desaysv.psmap.R.id.select_province_up)
        mNumView = mInflater.inflate(com.desaysv.psmap.R.layout.view_keyboard_num, null)
        mNumView.layoutParams = rlParams
        select_num_up = mNumView.findViewById(com.desaysv.psmap.R.id.select_num_up)
        val provinceLength = VIEW_PROVINCE_IDS.size
        var view: View
        for (i in 0 until provinceLength) {
            view = mProvinceView.findViewById(VIEW_PROVINCE_IDS[i])
            view.setOnClickListener(this)
        }
        val numLength = VIEW_NUM_IDS.size
        for (i in 0 until numLength) {
            view = mNumView.findViewById(VIEW_NUM_IDS[i])
            view.setOnClickListener(this)
        }
        mNumView.findViewById<View>(com.desaysv.psmap.R.id.select_num_delete_tv)
            .setOnClickListener(this) //G06特殊处理
        layout.addView(mProvinceView)
        layout.addView(mNumView)
        mNumView.visibility = GONE
    }

    /**
     * 显示 8 个输入框
     */
    fun showLastView(): Boolean {
        isSetLast = true
        textViews[7]!!.visibility = VISIBLE
        itemViewCount = 8
        if (!TextUtils.isEmpty(textViews[6]!!.text)) {
            mProvinceView.visibility = GONE
            mNumView.visibility = VISIBLE
        }
        if (isUpdateView) {
            setTextViewsBackground(updateViewPosition)
        } else {
            setTextViewsBackground(count)
        }
        return true
    }

    /**
     * 显示 7 个输入框
     */
    fun hideLastView(): Boolean {
        isSetLast = true
        textViews[7]?.visibility = GONE
        itemViewCount = 7
        if (stringBuffer.length == 8 || stringBuffer.length == 7) {
            textViews[7]?.text = ""
            stringBuffer.delete(7, 8)
            inputContent = stringBuffer.toString()
            count = stringBuffer.length
            inputListener?.inputComplete(inputContent)
            if (!isUpdateView) {
                mNumView.visibility = GONE
            }
        }
        if (isUpdateView) {
            setTextViewsBackground(updateViewPosition)
        } else {
            setTextViewsBackground(count)
        }
        return false
    }

    private fun onKeyDelete(isLastToDelete: Boolean): Boolean {
        Timber.d(" onKeyDelete setTextViewsBackground")
        if (count == 0) {
            stringBuffer.delete(0, 1)
            updateViewPosition = 0
            if (count == 0) {
                //切换回省份选择
                mProvinceView.visibility = VISIBLE
                mNumView.visibility = GONE
            }
            inputContent = stringBuffer.toString()
            textViews[0]?.text = ""
            setTextViewsBackground(0)
            //有删除就通知manger
            inputListener?.deleteContent()
            return true
        }
        if (stringBuffer.isNotEmpty()) {
            //删除相应位置的字符
            if (isLastToDelete) {
                stringBuffer.delete(updateViewPosition, updateViewPosition + 1)
            } else {
                try {
                    if (count - 1 > count) return false
                    stringBuffer.delete(count - 1, count)
                } catch (e: Exception) {
                    Timber.d(" onKeyDelete e:%s", e.message)
                    return false
                }
            }
            if (updateViewPosition + 1 == itemViewCount) {
                isUpdateView = !isUpdateView
            }
            count--
            if (count == 0) {
                //切换回省份选择
                clearNumberEditText()
                mProvinceView.visibility = VISIBLE
                mNumView.visibility = GONE
                inputContent = stringBuffer.toString()
            } else {
                inputContent = stringBuffer.toString()
                if (isLastToDelete) {
                    textViews[updateViewPosition]?.text = ""
                } else {
                    textViews[stringBuffer.length]?.text = ""
                }
            }
            setTextViewsBackground(if (isLastToDelete) updateViewPosition else count)
            //有删除就通知manger
            inputListener?.deleteContent()
        }
        return false
    }

    /**
     * 清空输入内容
     */
    fun clearEditText() {
        stringBuffer.delete(0, stringBuffer.length)
        inputContent = stringBuffer.toString()
        textViews.forEachIndexed { index, textView ->
            textView?.text = ""
            if (index == 0) {
                textView?.alpha = 1.0f
            } else {
                textView?.setBackgroundResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.R.drawable.shape_bg_vehicle_number_night else com.desaysv.psmap.R.drawable.shape_bg_vehicle_number_day)
            }
        }
    }

    /**
     * 清空非省份数据
     */
    fun clearNumberEditText() {
        val province = textViews[0]?.text.toString()
        stringBuffer.delete(0, stringBuffer.length)
        inputContent = stringBuffer.toString()
        Timber.d(" clearNumberEditText stringBuffer:$stringBuffer inputContent:$inputContent province:$province")
        textViews.forEachIndexed { index, textView ->
            if (index == 0) {
                textView?.text = province
                textView?.alpha = 1.0f
            } else {
                textView?.text = ""
                textView?.setBackgroundResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.R.drawable.shape_bg_vehicle_number_night else com.desaysv.psmap.R.drawable.shape_bg_vehicle_number_day)
            }
        }
    }

    private var inputListener: InputListener? = null

    init {
        mActivity = getActivityFromContext(context)
        textViews = arrayOfNulls(8)
        inflate(context, com.desaysv.psmap.R.layout.view_license_plate_frame, this)
        val textsLength = VIEW_IDS.size
        for (i in 0 until textsLength) {
            //textview放进数组中，方便修改操作
            textViews[i] = findViewById<View>(VIEW_IDS[i]) as TextView
            textViews[i]?.setOnTouchListener(mTouchListener)
        }
        editText = findViewById<View>(com.desaysv.psmap.R.id.item_edittext) as EditText
        editText.isCursorVisible = false //将光标隐藏
        setListener()
        hideSoftInputMethod()
        stringBuffer.append("京")
        inputContent = stringBuffer.toString()
    }

    private fun getActivityFromContext(context: Context): Activity {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        throw IllegalStateException("Could not find Activity from the given Context")
    }

    fun setInputListener(inputListener: InputListener) {
        this.inputListener = inputListener
    }

    /**
     * 键盘的点击事件
     */
    override fun onClick(view: View) {
        if (view is TextView) {
            val tv = view
            tv.isSelected = true
            val text = tv.text.toString()
            if (view.getId() == com.desaysv.psmap.R.id.select_province_up || view.getId() == com.desaysv.psmap.R.id.select_num_up) {
                Timber.d("select_province_up select_num_up ")
                hideSoftInput() //隐藏键盘
            } else {
                editContent = text
            }
        } else if (view is ImageView && view.getId() == com.desaysv.psmap.R.id.select_num_delete_tv) {
            if (stringBuffer.toString().length == itemViewCount) {
                count = 7
                onKeyDelete(true)
                setTextViewsEnable(true)
            } else {
                if (updateViewPosition > 0) {
                    updateViewPosition--
                }
                editContent = ""
            }
        }
    }

    /**
     * 输入完成监听回调接口
     */
    interface InputListener {
        /**
         * @param content 当输入完成时的全部内容
         */
        fun inputComplete(content: String?)

        /**
         * 删除操作
         */
        fun deleteContent()
    }

    var editContent: String
        /**
         * 获取输入文本
         *
         * @return
         */
        get() = inputContent
        /**
         * 设置 EditText 的输入内容
         * 根据isUpdateView 判断修改/删除操作
         */
        private set(content) {
            if (!isUpdateView) {
                setEditContent1(content)
            } else {
                if (TextUtils.isEmpty(content)) {
                    setEditContent2(content)
                } else {
                    setEditContent3(content)
                }
            }
        }

    private fun setEditContent1(content: String) {
        Timber.d(" setEditContent ")
        if (content.isNotEmpty()) {
            Timber.d(" setEditContent 1")
            editText.setText(content)
        } else {
            Timber.d(" setEditContent 2")
            onKeyDelete(false)
            setTextViewsEnable(true)
        }
    }

    private fun setEditContent2(content: String) {
        updateViewPosition++
        Timber.d(" setEditContent 3: $updateViewPosition content $content stringBuffer $stringBuffer")
        if (TextUtils.isEmpty(textViews[updateViewPosition]?.text.toString())) {
            Timber.d(" 末格 已删除啦")
        } else {
            val deleteIndex = if (updateViewPosition > stringBuffer.length) {
                (0..updateViewPosition).count { textViews[it]?.text.toString().isEmpty() } - 1
            } else {
                updateViewPosition
            }
            stringBuffer.delete(deleteIndex, deleteIndex + 1)
            count--
            if (count == 0) {
                //切换回省份选择
                mProvinceView.visibility = VISIBLE
                mNumView.visibility = GONE
            }
            inputContent = stringBuffer.toString()
            textViews[updateViewPosition]?.text = ""
        }
        setTextViewsBackground(updateViewPosition)
        //有删除就通知manger
        inputListener?.deleteContent()
    }

    private fun setEditContent3(content: String) {
        Timber.d(" setEditContent 4: $updateViewPosition content $content")
        textViews[updateViewPosition]?.text = content
        doUpdateView(content)
        inputContent = stringBuffer.toString()
        count = stringBuffer.length
        if (content.isNotEmpty()) {
            setTextViewsBackground(if (updateViewPosition + 1 == itemViewCount) count else updateViewPosition)
        } else {
            setTextViewsBackground(updateViewPosition)
        }
        //切换数字输入
        mProvinceView.visibility = GONE
        mNumView.visibility = VISIBLE
        if (updateViewPosition == 0) {
            updateViewPosition = 1
            setTextViewsBackground(updateViewPosition)
        }
        Timber.d(" setEditContent 5:  " + stringBuffer.length + " " + itemViewCount + " " + (inputListener != null))
        if (stringBuffer.length == itemViewCount && inputListener != null) {
            //文字长度为sblength  则调用完成输入的监听
            inputListener?.inputComplete(inputContent)
            mNumView.visibility = GONE
            restoreDefault() //按钮背景恢复原状
        }
    }

    private fun doUpdateView(content: String) {
        if (!TextUtils.isEmpty(content)) {
            Timber.d(" doUpdateView 1 $content $stringBuffer")
            stringBuffer.delete(0, stringBuffer.length)
            for (i in 0 until itemViewCount) {
                textViews[i]?.text.toString().takeIf { it.isNotEmpty() }?.let { stringBuffer.append(it) }
            }
            isUpdateView = !isUpdateView
            setTextViewsEnable(true)
            Timber.d(" doUpdateView 2 %s", stringBuffer.toString())
        } else {
            Timber.d(" doUpdateView 3 ")
            textViews[updateViewPosition]?.text = content
            if (updateViewPosition + 1 == itemViewCount) {
                isUpdateView = !isUpdateView
                stringBuffer.delete(updateViewPosition, updateViewPosition + 1)
                count--
            }
            inputListener?.deleteContent()
            setTextViewsEnable(true)
        }
    }

    /**
     * 显示输入框的TouchListener
     */
    private inner class OnFrameTouchListener : OnTouchListener {
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            if (view is TextView) {
                val tv = view
                tv.isFocusable = true
                val tvString = tv.text as String
                val viewId = tv.id
                Timber.d(" onTouch 1")
                if (tvString.isEmpty()) {
                    if (viewId == VIEW_IDS[1]) {
                        Timber.d(" onTouch 2")
                        updateViewPosition = 1
                        mProvinceView.visibility = GONE
                        mNumView.visibility = VISIBLE
                        isUpdateView = true
                        setTextViewsBackground(1)
                    } else {
                        Timber.d(" onTouch 3: %s", VIEW_IDS.size)
                        inputUpdateView(viewId, VIEW_IDS.size)
                    }
                    return true
                } else {
                    Timber.d(" onTouch 4")
                    inputUpdateView(viewId, VIEW_IDS.size)
                }
            }
            return true
        }

        private fun inputUpdateView(viewId: Int, length: Int) {
            for (i in 0 until length) {
                if (viewId == VIEW_IDS[i]) {
                    Timber.d("inputUpdateView $viewId ${VIEW_IDS[i]} $updateViewPosition")
                    updateViewPosition = i
                    if (i == 0) {
                        mProvinceView.visibility = VISIBLE
                        mNumView.visibility = GONE
                    } else {
                        mProvinceView.visibility = GONE
                        mNumView.visibility = VISIBLE
                    }
                    isUpdateView = true
                    setTextViewsBackground(i)
                    break
                }
            }
        }
    }

    /**
     * 当修改选中的某个号码，其他数字不能被选中，防止只改变显示，造成数据错误
     */
    private fun setTextViewsEnable(enabled: Boolean) {
        textViews.forEach { it?.isEnabled = enabled }
    }

    fun setTextViewsBackground(position: Int) {
        if (isSetLast) {
            isSetLast = false
            return
        }
        restoreDefault() //按钮背景恢复原状
        if (position < itemViewCount) {
            if (position == 0) {
                textViews[0]?.alpha = 0.3f
            } else {
                textViews[position]?.setBackgroundResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.R.drawable.shape_bg_vehicle_number_select_night else com.desaysv.psmap.R.drawable.shape_bg_vehicle_number_select_day)
            }
        }
    }

    //按钮背景恢复原状
    fun restoreDefault() {
        textViews.forEachIndexed { index, textView ->
            if (index == 0) {
                textView?.alpha = 1.0f
            } else {
                textView?.setBackgroundResource(if (NightModeGlobal.isNightMode()) com.desaysv.psmap.R.drawable.shape_bg_vehicle_number_night else com.desaysv.psmap.R.drawable.shape_bg_vehicle_number_day)
            }
        }
    }

    /**
     * 禁用系统软键盘
     */
    fun hideSoftInputMethod() {
        mActivity.window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        val currentVersion = Build.VERSION.SDK_INT
        var methodName: String? = null
        if (currentVersion >= 16) {
            // 4.2
            methodName = "setShowSoftInputOnFocus"
        } else if (currentVersion >= 14) {
            // 4.0
            methodName = "setSoftInputShownOnFocus"
        }
        if (methodName == null) {
            editText.inputType = InputType.TYPE_NULL
        } else {
            try {
                setSoftInputOnFocus(editText, methodName, false)
            } catch (e: Exception) {
                editText.inputType = InputType.TYPE_NULL
                Timber.d("Exception %s", e.message)
            }
        }
    }

    /**
     * 使用反射调用方法
     */
    private fun setSoftInputOnFocus(editText: EditText, methodName: String, value: Boolean) {
        val cls = EditText::class.java
        val method = cls.getMethod(methodName, Boolean::class.javaPrimitiveType)
        method.isAccessible = true
        method.invoke(editText, value)
    }

    companion object {
        private val VIEW_IDS = intArrayOf(
            com.desaysv.psmap.R.id.item_code_iv1,
            com.desaysv.psmap.R.id.item_code_iv2,
            com.desaysv.psmap.R.id.item_code_iv3,
            com.desaysv.psmap.R.id.item_code_iv4,
            com.desaysv.psmap.R.id.item_code_iv5,
            com.desaysv.psmap.R.id.item_code_iv6,
            com.desaysv.psmap.R.id.item_code_iv7,
            com.desaysv.psmap.R.id.item_code_iv8
        )
        private val VIEW_PROVINCE_IDS = intArrayOf(
            com.desaysv.psmap.R.id.select_province_up,
            com.desaysv.psmap.R.id.select_province_1_tv,
            com.desaysv.psmap.R.id.select_province_2_tv,
            com.desaysv.psmap.R.id.select_province_3_tv,
            com.desaysv.psmap.R.id.select_province_4_tv,
            com.desaysv.psmap.R.id.select_province_5_tv,
            com.desaysv.psmap.R.id.select_province_6_tv,
            com.desaysv.psmap.R.id.select_province_7_tv,
            com.desaysv.psmap.R.id.select_province_8_tv,
            com.desaysv.psmap.R.id.select_province_9_tv,
            com.desaysv.psmap.R.id.select_province_10_tv,
            com.desaysv.psmap.R.id.select_province_11_tv,
            com.desaysv.psmap.R.id.select_province_12_tv,
            com.desaysv.psmap.R.id.select_province_13_tv,
            com.desaysv.psmap.R.id.select_province_14_tv,
            com.desaysv.psmap.R.id.select_province_15_tv,
            com.desaysv.psmap.R.id.select_province_16_tv,
            com.desaysv.psmap.R.id.select_province_17_tv,
            com.desaysv.psmap.R.id.select_province_18_tv,
            com.desaysv.psmap.R.id.select_province_19_tv,
            com.desaysv.psmap.R.id.select_province_20_tv,
            com.desaysv.psmap.R.id.select_province_21_tv,
            com.desaysv.psmap.R.id.select_province_22_tv,
            com.desaysv.psmap.R.id.select_province_23_tv,
            com.desaysv.psmap.R.id.select_province_24_tv,
            com.desaysv.psmap.R.id.select_province_25_tv,
            com.desaysv.psmap.R.id.select_province_26_tv,
            com.desaysv.psmap.R.id.select_province_27_tv,
            com.desaysv.psmap.R.id.select_province_28_tv,
            com.desaysv.psmap.R.id.select_province_29_tv,
            com.desaysv.psmap.R.id.select_province_30_tv,
            com.desaysv.psmap.R.id.select_province_31_tv,
            com.desaysv.psmap.R.id.select_province_32_tv,
            com.desaysv.psmap.R.id.select_province_33_tv,
            com.desaysv.psmap.R.id.select_province_34_tv,
            com.desaysv.psmap.R.id.select_province_35_tv,
            com.desaysv.psmap.R.id.select_province_36_tv,
            com.desaysv.psmap.R.id.select_province_37_tv
        )
        private val VIEW_NUM_IDS = intArrayOf(
            com.desaysv.psmap.R.id.select_num_100_tv,
            com.desaysv.psmap.R.id.select_num_101_tv,
            com.desaysv.psmap.R.id.select_num_102_tv,
            com.desaysv.psmap.R.id.select_num_103_tv,
            com.desaysv.psmap.R.id.select_num_104_tv,
            com.desaysv.psmap.R.id.select_num_105_tv,
            com.desaysv.psmap.R.id.select_num_106_tv,
            com.desaysv.psmap.R.id.select_num_107_tv,
            com.desaysv.psmap.R.id.select_num_108_tv,
            com.desaysv.psmap.R.id.select_num_109_tv,
            com.desaysv.psmap.R.id.select_num_200_tv,
            com.desaysv.psmap.R.id.select_num_201_tv,
            com.desaysv.psmap.R.id.select_num_202_tv,
            com.desaysv.psmap.R.id.select_num_203_tv,
            com.desaysv.psmap.R.id.select_num_204_tv,
            com.desaysv.psmap.R.id.select_num_205_tv,
            com.desaysv.psmap.R.id.select_num_206_tv,
            com.desaysv.psmap.R.id.select_num_207_tv,
            com.desaysv.psmap.R.id.select_num_208_tv,
            com.desaysv.psmap.R.id.select_num_209_tv,
            com.desaysv.psmap.R.id.select_num_300_tv,
            com.desaysv.psmap.R.id.select_num_301_tv,
            com.desaysv.psmap.R.id.select_num_302_tv,
            com.desaysv.psmap.R.id.select_num_303_tv,
            com.desaysv.psmap.R.id.select_num_304_tv,
            com.desaysv.psmap.R.id.select_num_305_tv,
            com.desaysv.psmap.R.id.select_num_306_tv,
            com.desaysv.psmap.R.id.select_num_307_tv,
            com.desaysv.psmap.R.id.select_num_308_tv,
            com.desaysv.psmap.R.id.select_num_309_tv,
            com.desaysv.psmap.R.id.select_num_up,
            com.desaysv.psmap.R.id.select_num_400_tv,
            com.desaysv.psmap.R.id.select_num_401_tv,
            com.desaysv.psmap.R.id.select_num_402_tv,
            com.desaysv.psmap.R.id.select_num_403_tv,
            com.desaysv.psmap.R.id.select_num_404_tv,
            com.desaysv.psmap.R.id.select_num_405_tv,
            com.desaysv.psmap.R.id.select_num_406_tv
        )
    }
}
