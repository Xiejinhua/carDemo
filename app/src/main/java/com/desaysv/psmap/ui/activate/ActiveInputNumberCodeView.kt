package com.desaysv.psmap.ui.activate

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.EditText
import com.autonavi.auto.skin.SkinManager
import com.autonavi.auto.skin.view.SkinConstraintLayout
import com.autosdk.view.KeyboardUtil
import com.desaysv.psmap.databinding.LayoutActiveInputSerialNumberActivationCodeBinding
import timber.log.Timber

/**
 * 激活输入框自定义控件
 */
class ActiveInputNumberCodeView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    SkinConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: LayoutActiveInputSerialNumberActivationCodeBinding =
        LayoutActiveInputSerialNumberActivationCodeBinding.inflate(LayoutInflater.from(context), this, true)
    private var activeInputImpl: ActiveInputImpl? = null

    init {
        SkinManager.getInstance().updateView(this, true)
        binding.editText1.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    Timber.i("ActiveInputNumberCodeView onTextChanged s:${s?.length}")
                    if (s?.length == 6) {
                        binding.editText2.apply {
                            requestFocus()
                            setSelection(text.length)
                            KeyboardUtil.showKeyboard(this)
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    todoSetActiveInput() //判断是否已经输入完24位数据
                }
            })

            setOnClickListener {
                requestFocus()
                KeyboardUtil.showKeyboard(binding.editText1)
                Timber.i("setOnClickListener editText1")
            }
        }
        binding.editText2.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 6) {
                        binding.editText3.apply {
                            requestFocus()
                            setSelection(text.length)
                            KeyboardUtil.showKeyboard(this)
                        }
                    } else if (s?.length == 0) {
                        binding.editText1.requestFocus()
                        binding.editText1.setSelection(binding.editText1.text.length)
                        KeyboardUtil.showKeyboard(binding.editText1)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    todoSetActiveInput() //判断是否已经输入完24位数据
                }
            })
        }
        binding.editText3.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 6) {
                        binding.editText4.requestFocus()
                        binding.editText4.setSelection(binding.editText4.text.length)
                        KeyboardUtil.showKeyboard(binding.editText4)
                    } else if (s?.length == 0) {
                        binding.editText2.requestFocus()
                        binding.editText2.setSelection(binding.editText2.text.length)
                        KeyboardUtil.showKeyboard(binding.editText2)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    todoSetActiveInput() //判断是否已经输入完24位数据
                }
            })
        }
        binding.editText4.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 0) {
                        binding.editText3.requestFocus()
                        binding.editText3.setSelection(binding.editText3.text.length)
                        KeyboardUtil.showKeyboard(binding.editText3)
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    todoSetActiveInput() //判断是否已经输入完24位数据
                }
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setOnTouchListener(activity: Activity) {
        binding.editText1.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                activity.window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                activity.window.setLocalFocus(true, true)
            }
            false
        }
        binding.editText2.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                activity.window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                activity.window.setLocalFocus(true, true)
            }
            false
        }
        binding.editText3.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                activity.window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                activity.window.setLocalFocus(true, true)
            }
            false
        }
        binding.editText4.setOnTouchListener { _, event ->
            if (event?.action == MotionEvent.ACTION_UP) {
                activity.window.setLocalFocus(true, true)
            } else if (event?.action == MotionEvent.ACTION_DOWN) {
                activity.window.setLocalFocus(true, true)
            }
            false
        }
    }

    //判断是否已经输入完24位数据
    private fun todoSetActiveInput() {
        val data =
            getEditTextData(binding.editText1) + getEditTextData(binding.editText2) + getEditTextData(binding.editText3) + getEditTextData(binding.editText4)
        if (!TextUtils.isEmpty(data) && data.length >= 24) {
            activeInputImpl?.onActiveInput(data)
        } else {
            activeInputImpl?.onActiveInput("")
        }
    }

    //判断是否需要将文本重新填充--一般是界面切换时处理
    fun savedInstanceState(savedInstanceState: Bundle?, type: String) {//type： serial.序列号 active.激活码
        if (savedInstanceState != null) {
            setText(savedInstanceState.getString(type + "1", ""), binding.editText1)
            setText(savedInstanceState.getString(type + "2", ""), binding.editText2)
            setText(savedInstanceState.getString(type + "3", ""), binding.editText3)
            setText(savedInstanceState.getString(type + "4", ""), binding.editText4)
        }
    }

    //设置文本
    fun setText(data: String, edit: EditText) {
        edit.text = Editable.Factory.getInstance().newEditable(data)
        edit.clearFocus()
        KeyboardUtil.hideKeyboard(edit)
    }

    //在onSaveInstanceState方法中，需要保存输入框的文本
    fun onSaveInstanceState(outState: Bundle, type: String) { //type： serial.序列号 active.激活码
        outState.putString(type + "1", getEditTextData(binding.editText1))
        outState.putString(type + "2", getEditTextData(binding.editText2))
        outState.putString(type + "3", getEditTextData(binding.editText3))
        outState.putString(type + "4", getEditTextData(binding.editText4))
    }

    private fun getEditTextData(edit: EditText): String {
        return edit.text?.toString()?.trim() ?: ""
    }

    //设置日夜模式
    fun setNight(isNight: Boolean) {
        binding.isNight = isNight
    }

    fun setActiveInputImpl(activeInputImpl: ActiveInputImpl?) {
        this.activeInputImpl = activeInputImpl
    }

    interface ActiveInputImpl {
        fun onActiveInput(text: String) //将输入框数据汇总透出
    }
}