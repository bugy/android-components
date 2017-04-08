package net.buggy.components;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
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
import android.os.LocaleList;
import android.support.annotation.ColorRes;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
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

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), HIDE_NOT_ALWAYS);
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

    public static void addFont(Context context, String familyName, String fontFilename) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return; // fontFamilies are not supported in styles
        }

        final Typeface customFontTypeface = Typeface.createFromAsset(context.getAssets(), fontFilename);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                final Field staticField = Typeface.class
                        .getDeclaredField("sSystemFontMap");
                staticField.setAccessible(true);

                Map<String, Typeface> map = (Map<String, Typeface>) staticField.get(null);
                map.put(familyName.toLowerCase(), customFontTypeface);

                staticField.set(null, map);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public static void setLeftMargin(View view, int marginPx) {
        final ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = marginPx;
        view.setLayoutParams(layoutParams);
    }

    public static void setHeight(View view, int heightPx) {
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = heightPx;
        view.setLayoutParams(layoutParams);
    }

    public static void setHeightDp(View view, int heightDp) {
        final int height = ViewUtils.dpToPx(heightDp, view.getContext());
        setHeight(view, height);
    }

    public static void setLeftMarginDp(
            int marginDp,
            ViewGroup.MarginLayoutParams layoutParams,
            Context context) {
        layoutParams.leftMargin = dpToPx(marginDp, context);
    }

    public static void setTextWithoutAnimation(TextView materialTextView, String text) {
        ViewParent parent = materialTextView.getParent();
        while ((parent != null) && (!(parent instanceof TextInputLayout))) {
            parent = parent.getParent();
        }

        if (parent instanceof TextInputLayout) {
            ((TextInputLayout) parent).setHintAnimationEnabled(false);
            materialTextView.setText(text);
            ((TextInputLayout) parent).setHintAnimationEnabled(true);
            return;
        }

        materialTextView.setText(text);
    }

    public static int pickFromColorScale(int color1, int color2, float overdueScale) {
        final int red1 = Color.red(color1);
        final int blue1 = Color.blue(color1);
        final int green1 = Color.green(color1);

        final int red2 = Color.red(color2);
        final int blue2 = Color.blue(color2);
        final int green2 = Color.green(color2);

        final int red = weightedValue(red1, red2, overdueScale);
        final int green = weightedValue(green1, green2, overdueScale);
        final int blue = weightedValue(blue1, blue2, overdueScale);
        return Color.argb(255, red, green, blue);
    }

    private static int weightedValue(int value1, int value2, float value2Weight) {
        return Math.round(value1 * (1f - value2Weight) + value2 * value2Weight);
    }

    @SuppressWarnings("deprecation")
    public static ContextWrapper wrap(Context context, Locale locale) {
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale);

            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);

            context = context.createConfigurationContext(configuration);

        } else if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
            configuration.setLocale(locale);
            context = context.createConfigurationContext(configuration);

        } else {
            configuration.locale = locale;
            res.updateConfiguration(configuration, res.getDisplayMetrics());
        }

        return new ContextWrapper(context);
    }

    @SuppressWarnings("deprecation")
    public static Locale getAppLocale(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }
        return locale;
    }

    public static void setColorListTint(ImageView imageView, @ColorRes int colorListId) {
        final Drawable drawable = DrawableCompat.wrap(imageView.getDrawable());
        imageView.setImageDrawable(drawable);

        final ColorStateList colorStateList =
                ContextCompat.getColorStateList(imageView.getContext(), colorListId);
        DrawableCompat.setTintList(drawable, colorStateList);
    }

    public static void setTint(ImageView imageView, int tintColor) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(imageView.getDrawable());
        imageView.setImageDrawable(wrappedDrawable);
        final int color = resolveColor(tintColor, imageView.getContext());

        DrawableCompat.setTint(wrappedDrawable, color);
    }
}
