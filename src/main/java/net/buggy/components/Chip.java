package net.buggy.components;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class Chip extends LinearLayout {

    private TextView textView;

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public Chip(Context context) {
        super(context);
        init();
    }

    public Chip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Chip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Chip(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        final int dp4 = ViewUtils.dpToPx(4f, getContext());
        final int dp13 = ViewUtils.dpToPx(13f, getContext());
        final int dp32 = ViewUtils.dpToPx(32f, getContext());

        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(
                ViewUtils.dpToPx(12f, getContext()),
                0,
                0,
                0);

        textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setIncludeFontPadding(false);
        final LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(
                WRAP_CONTENT, dp32);
        textView.setLayoutParams(textViewParams);

        final ImageButton imageButton = new ImageButton(getContext());
        imageButton.setImageResource(R.drawable.ic_cancel_black_24dp);
        final LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT);
        imageViewParams.setMargins(dp4, 0, dp4, 0);
        imageButton.setLayoutParams(imageViewParams);
        imageButton.setPadding(0, 0, 0, 0);
        imageButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageButton.setAlpha(0.30f);
        imageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fireCloseClicked();
            }
        });

        addView(textView, textViewParams);
        addView(imageButton, imageViewParams);

        final float[] corners = new float[8];
        Arrays.fill(corners, dp13);
        final RoundRectShape rectShape = new RoundRectShape(corners, null, null);
        final ShapeDrawable background = new ShapeDrawable(rectShape);
        background.getPaint().setColor(0xFFE0E0E0);

        ViewUtils.setBackground(this, background);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    private void fireCloseClicked() {
        for (Listener listener : listeners) {
            listener.closeClicked();
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public interface Listener {
        void closeClicked();
    }
}
