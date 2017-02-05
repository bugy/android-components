package net.buggy.components.list;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.buggy.components.ViewUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class TextCellFactory<T> extends CellFactory<T, TextView> {

    public enum HorizontalAlignment {LEFT, CENTER, RIGHT}

    private final HorizontalAlignment horizontalAlignment;
    private Integer selectedBackgroundColor;
    private Integer selectedForegroundColor;

    private final Map<View, Drawable> replacedBackgrounds = new LinkedHashMap<>();
    private final Map<View, Integer> replacedTextColors = new LinkedHashMap<>();

    public TextCellFactory() {
        this(HorizontalAlignment.CENTER);
    }

    public TextCellFactory(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public void setSelectedBackgroundColor(Integer selectedBackgroundColor) {
        this.selectedBackgroundColor = selectedBackgroundColor;
    }

    public void setSelectedForegroundColor(Integer selectedForegroundColor) {
        this.selectedForegroundColor = selectedForegroundColor;
    }

    @Override
    public TextView createEmptyCell(Context context, ViewGroup parent) {
        TextView view = new TextView(context);
        view.setId((int) System.currentTimeMillis());

        final ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(layoutParams);

        final int padding = ViewUtils.dpToPx(10, context);
        view.setPadding(padding, padding, padding, padding);


        switch (horizontalAlignment) {
            case LEFT:
                view.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                break;
            case CENTER:
                view.setGravity(Gravity.CENTER);
                break;
            case RIGHT:
                view.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
                break;
            default:
                throw new IllegalStateException("Unknown alignment: " + horizontalAlignment);
        }

        return view;
    }

    @Override
    public void fillCell(Cell<T> cell, TextView view, CellContext<T> cellContext, ChangeListener<T> listener) {
        final T data = cell.getData();
        if (data != null) {
            view.setText(String.valueOf(data));
        }

        if (!cell.isEnabled()) {
            setSelected(view, false);
            setDisabled(view, true);
        } else {
            setDisabled(view, false);

            setSelected(view, cell.isSelected());
        }
    }

    private void setDisabled(TextView view, boolean disabled) {
        if (disabled) {
            view.setAlpha(0.7f);

        } else {
            view.setAlpha(1);
        }
    }

    private void setSelected(TextView view, boolean selected) {
        if (view.isSelected() == selected) {
            return;
        }

        view.setSelected(selected);

        if (selected) {
            Drawable oldBackground = view.getBackground();
            if (!replacedBackgrounds.containsKey(view)) {
                replacedBackgrounds.put(view, oldBackground);
            }

            if (selectedBackgroundColor == null) {
                view.setBackgroundColor(0x10000000);
            } else {
                view.setBackgroundColor(selectedBackgroundColor);
            }

            final int currentTextColor = view.getCurrentTextColor();
            if (!replacedTextColors.containsKey(view)) {
                replacedTextColors.put(view, currentTextColor);
            }

            if (selectedForegroundColor != null) {
                view.setTextColor(selectedForegroundColor);
            }

        } else {
            final Drawable oldBackground = replacedBackgrounds.remove(view);
            ViewUtils.setBackground(view, oldBackground);

            final Integer textColor = replacedTextColors.remove(view);
            view.setTextColor(textColor);
        }
    }
}
