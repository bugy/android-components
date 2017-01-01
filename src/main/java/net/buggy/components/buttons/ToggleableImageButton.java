package net.buggy.components.buttons;


import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

public class ToggleableImageButton extends ImageButton {
    public ToggleableImageButton(Context context) {
        super(context);

        init();
    }

    public ToggleableImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public ToggleableImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ToggleableImageButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final boolean pressed = isPressed();
                    setPressed(!pressed);

                    performClick();
                }

                return true;
            }
        });
    }
}
