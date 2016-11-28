package net.buggy.components;


import android.app.Activity;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ViewUtils {

    public static int dpToPx(float dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);

        return Math.round(px);
    }

    @SuppressWarnings("deprecation")
    public static int resolveColor(int xmlColor, Context context) {
        final int result;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            result = context.getResources().getColor(xmlColor, context.getTheme());
        } else {
            result = context.getResources().getColor(xmlColor);
        }

        return result;
    }

    public static void hideSoftKeyboard(Activity activity, View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(int drawableId, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return activity.getDrawable(drawableId);
        } else {
            return activity.getResources().getDrawable(drawableId);
        }
    }

    public static Drawable createGradientDrawable(final int... colors) {
        ShapeDrawable.ShaderFactory factory = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                final float[] positions = new float[colors.length];
                float step = 1f / (colors.length - 1);

                positions[0] = 0;
                positions[colors.length - 1] = 1;

                for (int i = 1; i < (colors.length - 1); i++) {
                    positions[i] = i * step;
                }

                return new LinearGradient(0, 0, width, height,
                        colors,
                        positions,
                        Shader.TileMode.CLAMP);
            }
        };

        PaintDrawable drawable = new PaintDrawable();
        final RectShape shape = new RectShape();
        drawable.setShape(shape);
        drawable.setShaderFactory(factory);

        return drawable;
    }

    public static void focusTextField(EditText editText) {
        editText.requestFocus();
        final Context context = editText.getContext();
        InputMethodManager inputManager = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }
}
