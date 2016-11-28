package net.buggy.components.list;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.buggy.components.R;
import net.buggy.components.ViewUtils;

import java.util.Arrays;


public class MenuCellFactory implements CellFactory<MenuCellFactory.Item, LinearLayout> {

    private final int pressedColor;

    public MenuCellFactory() {
        this(0x60000000);
    }

    public MenuCellFactory(int pressedColor) {
        this.pressedColor = pressedColor;
    }

    @Override
    public LinearLayout createEmptyCell(Context context, ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(context);

        return (LinearLayout) inflater.inflate(R.layout.menu_item, parent, false);
    }

    @Override
    public void fillCell(Item value, final LinearLayout view, ChangeListener<Item> listener, boolean selected, boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final RippleDrawable drawable = getPressedRippleDrawable(pressedColor);
            ViewUtils.setBackground(view, drawable);
        } else {
            final StateListDrawable stateListDrawable = getPressedDrawable(pressedColor);
            ViewUtils.setBackground(view, stateListDrawable);
        }

        final Drawable icon = value.getIcon();

        final ImageView iconView = (ImageView) view.findViewById(R.id.menu_item_icon);
        if (icon != null) {
            iconView.setImageDrawable(icon);
        } else {
            iconView.setAlpha(0.4f);
        }

        final String text = value.getText();
        final TextView itemTextView = (TextView) view.findViewById(R.id.menu_item_text);
        itemTextView.setText(text);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static RippleDrawable getPressedRippleDrawable(int pressedColor) {
        return new RippleDrawable(
                ColorStateList.valueOf(pressedColor),
                null,
                getRippleMask(pressedColor));
    }

    private static Drawable getRippleMask(int color) {
        float[] outerRadius = new float[8];
        Arrays.fill(outerRadius, 3);

        RoundRectShape r = new RoundRectShape(outerRadius, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(r);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    private static StateListDrawable getPressedDrawable(
            int pressedColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},
                new ColorDrawable(pressedColor));
        return states;
    }

    public static final class Item {
        private final Drawable icon;
        private final String text;

        public Item(String text, Drawable icon) {
            this.icon = icon;
            this.text = text;
        }

        public Drawable getIcon() {
            return icon;
        }

        public String getText() {
            return text;
        }
    }
}
