
package com.autonavi.auto.common.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AutoEditText extends android.widget.EditText implements OnLongClickListener {

    private static final int BLINK_DELAY = 500;
    private int mInputType = EditorInfo.TYPE_NULL;
    private boolean mShouldBlinkInternal = false;
    private BlinkHandler mBlinkHandler;
    private boolean mIsAttachedToWindow = false;
    private List<TextWatcher> mTextListeners;
    private OnAttachStateChangeListener mOnAttachStateChangeListener = null;
    private OnSelectionChangedListener mOnSelectionChangedListener;
    private Method mMethodInvalidateCursorPath;
    private static boolean isInputOpen;

    public AutoEditText(Context context) {
        super(context);
        onCreate(context, null, 0);
    }

    public AutoEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        onCreate(context, attrs, 0);
    }

    public AutoEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        onCreate(context, attrs, defStyle);
    }

    private void onCreate(Context context, AttributeSet attrs, int defStyle) {
        //IME_FLAG_NO_EXTRACT_UI或者IME_FLAG_NO_FULLSCREEN，这个设置会影响系统输入法，会导致有些系统输入法中文候选字出不来
        // 根据不同系统选择IME_FLAG_NO_EXTRACT_UI或者IME_FLAG_NO_FULLSCREEN，可以解决该问题
        setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mTextListeners = new ArrayList<>();
        if (context instanceof Activity) {
            ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        try {
            //不弹出软键盘，>API-14
            Method method = TextView.class.getDeclaredMethod(
                    "setShowSoftInputOnFocus", boolean.class);
            method.setAccessible(false);
            method.invoke(this, false);
        } catch (NoSuchMethodException e) {
            try {
                Method method = TextView.class.getDeclaredMethod(
                        "setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(false);
                method.invoke(this, false);
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
                //没有 setShowSoftInputOnFocus 方法，改用其他方式setSoftInputShownOnFocus

                mInputType = super.getInputType();
                //设为不可编辑，用于禁用输入法
                super.setRawInputType(InputType.TYPE_NULL);
                //禁止弹出输入法菜单
                setOnLongClickListener(this);
                mShouldBlinkInternal = android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }

        //EditText不可编辑时，光标也不会闪烁，在此类中自行实现
        if (mShouldBlinkInternal) {
            makeBlink();
        }
        //禁止edittext复制粘贴
        setCustomSelectionActionModeCallback(new ActionMode.Callback() {

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                return false;
            }
        });
    }

    public OnAttachStateChangeListener getOnAttachStateChangeListener() {
        return mOnAttachStateChangeListener;
    }

    public void setOnAttachStateChangeListener(OnAttachStateChangeListener onAttachStateChangeListener) {
        this.mOnAttachStateChangeListener = onAttachStateChangeListener;
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener onSelectionChangedListener) {
        mOnSelectionChangedListener = onSelectionChangedListener;
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        //同一个监听对象不能add两次，否则会出现监听重复的问题出现
        if (watcher != null && !mTextListeners.contains(watcher)) {
            mTextListeners.add(watcher);
            super.addTextChangedListener(watcher);
        }
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        mTextListeners.remove(watcher);
        super.removeTextChangedListener(watcher);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (mOnSelectionChangedListener != null) {
            mOnSelectionChangedListener.onSelectionChanged(selStart, selEnd);
        }
    }

    @Override
    public void setInputType(int type) {
        if (!isInputOpen) {
            super.setInputType(type);
        }
        mInputType = type;
    }

    @Override
    public void setRawInputType(int type) {
        if (!isInputOpen) {
            super.setRawInputType(type);
        }
        mInputType = type;
    }
    public static void setIsInputOpen(boolean inputOpen){
        isInputOpen = inputOpen;
    }
//    @Override
//    public int getInputType() {
//        return super.getInputType();
//    }

    public int getImeInputType() {
        return mInputType;
    }

    //根据是否为系统输入法，决定是否重写该方法。内置输入法必须返回null，否则会Auto001-19251。系统输入法必须返回父类方法，否则系统输入法中文候选字为空，Auto001-21598
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        if (!isInputOpen) {
            return super.onCreateInputConnection(outAttrs);
        }
        return null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mIsAttachedToWindow = true;
        if (mOnAttachStateChangeListener != null) {
            mOnAttachStateChangeListener.onViewAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mIsAttachedToWindow = false;
        removeAllListener();
        if (mBlinkHandler != null) {
            mBlinkHandler.cancel();
            mBlinkHandler.removeCallbacksAndMessages(null);
            mBlinkHandler = null;
        }
        super.onDetachedFromWindow();
        if (mOnAttachStateChangeListener != null) {
            mOnAttachStateChangeListener.onViewDetachedFromWindow();
        }
    }

    private void removeAllListener() {
        if (mTextListeners != null && mTextListeners.size() > 0) {
            for (TextWatcher watcher : mTextListeners) {
                super.removeTextChangedListener(watcher);
            }
            mTextListeners.clear();
        }
    }

    @SuppressLint("Override")
    @Override
    public boolean isAttachedToWindow() {
        return mIsAttachedToWindow;
    }

    /**
     * Returns true if pressing ENTER in this field advances focus instead of
     * inserting the character. This is true mostly in single-line fields, but
     * also in mail addresses and subjects which will display on multiple lines
     * but where it doesn't make sense to insert newlines.
     */
    private boolean shouldAdvanceFocusOnEnter() {
        if (getKeyListener() == null) {
            return false;
        }

        if (getTransformationMethod() instanceof SingleLineTransformationMethod) {
            return true;
        }
        int inputType = getInputType();
        if ((inputType & InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT) {
            int variation = inputType & InputType.TYPE_MASK_VARIATION;
            if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                    || variation == InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT) {
                return true;
            }
        }

        return false;
    }

    private static boolean isMultilineInputType(int type) {
        return (type & (InputType.TYPE_MASK_CLASS | InputType.TYPE_TEXT_FLAG_MULTI_LINE)) == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
    }

    public InputConnection createInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = getImeInputType();
        outAttrs.imeOptions = getImeOptions();
        if (outAttrs.imeOptions != EditorInfo.IME_NULL) {
            outAttrs.privateImeOptions = getPrivateImeOptions();
            outAttrs.actionLabel = getImeActionLabel();
            outAttrs.actionId = getImeActionId();
            outAttrs.extras = getInputExtras(true);
        }
        if (focusSearch(FOCUS_DOWN) != null) {
            outAttrs.imeOptions |= EditorInfo.IME_FLAG_NAVIGATE_NEXT;
        }
        if (focusSearch(FOCUS_UP) != null) {
            outAttrs.imeOptions |= EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS;
        }
        if ((outAttrs.imeOptions & EditorInfo.IME_MASK_ACTION) == EditorInfo.IME_ACTION_UNSPECIFIED) {
            if ((outAttrs.imeOptions & EditorInfo.IME_FLAG_NAVIGATE_NEXT) != 0) {
                // An action has not been set, but the enter key will move to
                // the next focus, so set the action to that.
                outAttrs.imeOptions |= EditorInfo.IME_ACTION_NEXT;
            } else {
                // An action has not been set, and there is no focus to move
                // to, so let's just supply a "done" action.
                outAttrs.imeOptions |= EditorInfo.IME_ACTION_DONE;
            }
            if (!shouldAdvanceFocusOnEnter()) {
                outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
            }
        }
        if (isMultilineInputType(outAttrs.inputType)) {
            // Multi-line text editors should always show an enter key.
            outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }
        outAttrs.hintText = getHint();
        if (getText() instanceof Editable) {
            InputConnection ic = new EditableInputConnection(this);
            outAttrs.initialSelStart = getSelectionStart();
            outAttrs.initialSelEnd = getSelectionEnd();
            outAttrs.initialCapsMode = ic.getCursorCapsMode(getInputType());
            return ic;
        }
        return null;
    }

    @Override
    public boolean onLongClick(View v) {
        return true;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (mBlinkHandler != null) {
            if (!hasWindowFocus) {
                mBlinkHandler.cancel();
            } else {
                makeBlink();
            }
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        makeBlink();
    }

    boolean shouldBlink() {
        if (!mShouldBlinkInternal) {
            return false;
        }
        if (!isFocused()) {
            return false;
        }

        final int start = getSelectionStart();
        if (start < 0) {
            return false;
        }

        final int end = getSelectionEnd();
        if (end < 0) {
            return false;
        }

        return start == end;
    }

    private void makeBlink() {
        if (shouldBlink()) {
            if (mBlinkHandler == null) {
                mBlinkHandler = new BlinkHandler();
            }
            mBlinkHandler.uncancel();
            mBlinkHandler.removeCallbacks(mBlinkHandler);
            mBlinkHandler.postAtTime(mBlinkHandler, SystemClock.uptimeMillis() + BLINK_DELAY);
        } else {
            if (mBlinkHandler != null) {
                mBlinkHandler.removeCallbacks(mBlinkHandler);
            }
        }
    }

    void superInvalidateCursorPath() {
        try {
            if (mMethodInvalidateCursorPath == null) {
                mMethodInvalidateCursorPath = TextView.class
                        .getDeclaredMethod("invalidateCursorPath");
                //私有方法，需要setAccessible(true)，否则无法访问
                mMethodInvalidateCursorPath.setAccessible(true);
            }
        } catch (NoSuchMethodException e) {
        }
        try {
            mMethodInvalidateCursorPath.invoke(this);
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
    }

    private class EditableInputConnection extends BaseInputConnection {

        private final TextView mTextView;

        // Keeps track of nested begin/end batch edit to ensure this connection always has a
        // balanced impact on its associated TextView.
        // A negative value means that this connection has been finished by the InputMethodManager.
        private int mBatchEditNesting;

        public EditableInputConnection(TextView textview) {
            super(textview, true);
            mTextView = textview;
        }

        @Override
        public Editable getEditable() {
            TextView tv = mTextView;
            if (tv != null) {
                return tv.getEditableText();
            }
            return null;
        }

        @Override
        public boolean beginBatchEdit() {
            synchronized (this) {
                if (mBatchEditNesting >= 0) {
                    mTextView.beginBatchEdit();
                    mBatchEditNesting++;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean endBatchEdit() {
            synchronized (this) {
                if (mBatchEditNesting > 0) {
                    // When the connection is reset by the InputMethodManager and reportFinish
                    // is called, some endBatchEdit calls may still be asynchronously received from the
                    // IME. Do not take these into account, thus ensuring that this IC's final
                    // contribution to mTextView's nested batch edit count is zero.
                    mTextView.endBatchEdit();
                    mBatchEditNesting--;
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean clearMetaKeyStates(int states) {
            final Editable content = getEditable();
            if (content == null) {
                return false;
            }
            KeyListener kl = mTextView.getKeyListener();
            if (kl != null) {
                try {
                    kl.clearMetaKeyState(mTextView, content, states);
                } catch (AbstractMethodError e) {
                    // This is an old listener that doesn't implement the
                    // new method.
                }
            }
            return true;
        }

        @Override
        public boolean commitCompletion(CompletionInfo text) {
            mTextView.beginBatchEdit();
            mTextView.onCommitCompletion(text);
            mTextView.endBatchEdit();
            return true;
        }

        /**
         * Calls the {@link TextView#onCommitCorrection} method of the
         * associated TextView.
         */
        @Override
        public boolean commitCorrection(CorrectionInfo correctionInfo) {
            mTextView.beginBatchEdit();
            mTextView.onCommitCorrection(correctionInfo);
            mTextView.endBatchEdit();
            return true;
        }

        @Override
        public boolean performEditorAction(int actionCode) {
            mTextView.onEditorAction(actionCode);
            return true;
        }

        @Override
        public boolean performContextMenuAction(int id) {
            mTextView.beginBatchEdit();
            mTextView.onTextContextMenuItem(id);
            mTextView.endBatchEdit();
            return true;
        }

        @Override
        public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
            if (mTextView != null) {
                ExtractedText et = new ExtractedText();
                if (mTextView.extractText(request, et)) {
                    return et;
                }
            }
            return null;
        }

        @Override
        public boolean performPrivateCommand(String action, Bundle data) {
            mTextView.onPrivateIMECommand(action, data);
            return true;
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return super.commitText(text, newCursorPosition);
        }
    }

    private class BlinkHandler extends Handler implements Runnable {

        private boolean mCancelled;

        @Override
        public void run() {
            if (mCancelled) {
                return;
            }

            removeCallbacks(BlinkHandler.this);

            if (shouldBlink()) {
                if (getLayout() != null) {
                    superInvalidateCursorPath();
                }

                postAtTime(this, SystemClock.uptimeMillis() + BLINK_DELAY);
            }
        }

        public void cancel() {
            if (!mCancelled) {
                removeCallbacks(BlinkHandler.this);
                mCancelled = true;
            }
        }

        public void uncancel() {
            mCancelled = false;
        }

    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(int selStart, int selEnd);
    }

    /**
     * Interface definition for a callback to be invoked when this view is attached
     * or detached from its window.
     */
    public interface OnAttachStateChangeListener {
        /**
         * Called when the view is attached to a window.
         */
        public void onViewAttachedToWindow();

        /**
         * Called when the view is detached from a window.
         */
        public void onViewDetachedFromWindow();
    }

}
