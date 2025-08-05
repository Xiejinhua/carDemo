package com.autonavi.auto.common.view;

import android.content.Context;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.autonavi.auto.skin.view.SkinTextView;

/**
 * Created by AutoSdk on 2021/7/26.
 **/
public class SpanableTextView extends SkinTextView {

    public SpanableTextView(Context context) {
        super(context);
    }

    public SpanableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpanableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        int startSelection = getSelectionStart();
        int endSelection = getSelectionEnd();
        if (startSelection < 0 || endSelection < 0) {
            if (getText() instanceof Spannable) {
                Selection.setSelection((Spannable) getText(), getText().length());
            } else if (getText() instanceof SpannableString) {
                Selection.setSelection((SpannableString) getText(), getText().length());
            } else if(getText() instanceof SpannableStringBuilder){
                Selection.setSelection((SpannableStringBuilder) getText(), getText().length());
            }
        } else if (startSelection != endSelection) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                final CharSequence text = getText();
                setText(null);
                setText(text);
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
