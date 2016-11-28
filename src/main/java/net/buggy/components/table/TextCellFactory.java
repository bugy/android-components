package net.buggy.components.table;


import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

public class TextCellFactory implements CellFactory {

    public enum HorizontalAlignment {LEFT, CENTER, RIGHT}

    private final HorizontalAlignment horizontalAlignment;

    public TextCellFactory() {
        this(HorizontalAlignment.CENTER);
    }

    public TextCellFactory(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    @Override
    public View createCell(Object value, int row, int column, Context context) {
        TextView view = new TextView(context);
        view.setPadding(6, 6, 6, 6);

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

        if (value != null) {
            view.setText(String.valueOf(value));
        }

        return view;
    }
}
