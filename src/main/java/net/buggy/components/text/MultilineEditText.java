package net.buggy.components.text;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class MultilineEditText extends EditText {
    public MultilineEditText(Context context) {
        super(context);
    }

    public MultilineEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultilineEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MultilineEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
