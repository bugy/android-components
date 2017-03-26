package net.buggy.components.text;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;

import net.buggy.components.ViewUtils;

public class BackableEditText extends android.support.v7.widget.AppCompatEditText {

    public BackableEditText(Context context) {
        super(context);
    }

    public BackableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BackableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int key_code, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            this.clearFocus();
            ViewUtils.hideSoftKeyboard(this);
            return true;
        }

        return super.onKeyPreIme(key_code, event);
    }
}
