package net.buggy.components;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import static android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY;
import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

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

    public static void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),
                HIDE_NOT_ALWAYS & HIDE_IMPLICIT_ONLY);
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

    public static String getVersion(Activity activity) {
        try {
            PackageInfo pInfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void showStyled(AlertDialog dialog) {
        dialog.show();

        final Resources.Theme theme = dialog.getContext().getTheme();
        final TypedArray typedArray = theme.obtainStyledAttributes(new int[]{R.attr.colorAccent});
        final int colorAccent = typedArray.getColor(0, Color.BLACK);
        typedArray.recycle();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.GRAY);
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(colorAccent);
    }

    public static void makeHintItalic(final EditText editText) {
        final Typeface originalTypeface = editText.getTypeface();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateHintStyle(s, editText, originalTypeface);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        updateHintStyle(editText.getText(), editText, originalTypeface);
    }

    private static void updateHintStyle(CharSequence text, EditText editText, Typeface originalTypeface) {
        if (text.length() == 0) {
            editText.setTypeface(originalTypeface, Typeface.ITALIC);
        } else {
            editText.setTypeface(originalTypeface, Typeface.NORMAL);
        }
    }

    public static Rect getLocationOnScreen(View view) {
        Rect mRect = new Rect();
        int[] location = new int[2];

        view.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + view.getWidth();
        mRect.bottom = location[1] + view.getHeight();

        return mRect;
    }
}
