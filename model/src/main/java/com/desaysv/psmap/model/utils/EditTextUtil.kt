package com.desaysv.psmap.model.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText

/**
 * Author : wangmansheng
 * Date : 2024-1-11
 * Description : EditText工具类
 */
object EditTextUtil {
    //屏蔽editText弹出工具条
    fun setCustomSelectionActionModeCallback(editText: EditText) {
        editText.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {}
        }
    }
}