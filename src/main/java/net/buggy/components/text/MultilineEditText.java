package net.buggy.components.text;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class MultilineEditText extends AppCompatEditText {
    public MultilineEditText(Context context) {
        this(context, null);
    }

    public MultilineEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
    }

    public MultilineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection conn = super.onCreateInputConnection(outAttrs);

        if ((outAttrs.imeOptions & EditorInfo.IME_ACTION_DONE) == EditorInfo.IME_ACTION_DONE) {
            outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
        }

        return conn;
    }
}
