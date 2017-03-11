package net.buggy.components.spinners;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StyleRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.common.base.Function;

import net.buggy.components.R;
import net.buggy.components.ViewUtils;
import net.buggy.components.list.Cell;
import net.buggy.components.list.CellContext;
import net.buggy.components.list.FactoryBasedAdapter;
import net.buggy.components.list.ListDecorator;
import net.buggy.components.list.TextCellFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static android.widget.ListPopupWindow.WRAP_CONTENT;

public class MaterialSpinner<T> extends FrameLayout {

    private TextInputLayout textInputLayout;
    private T selectedItem;
    private FactoryBasedAdapter<T> listAdapter;
    private Function<T, String> stringConverter;

    private String selectNothingString;

    private List<T> values = new ArrayList<>();
    private EditText textField;

    public MaterialSpinner(@NonNull Context context) {
        super(context);

        init(context);
    }

    public MaterialSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public MaterialSpinner(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MaterialSpinner(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context);
    }

    private void init(final Context context) {
        selectNothingString = context.getString(R.string.material_spinner_empty_selection);

        final LayoutInflater inflater = LayoutInflater.from(context);

        inflater.inflate(R.layout.material_spinner, this, true);
        textInputLayout = (TextInputLayout) this.findViewById(R.id.material_spinner_input_layout);
        textField = (EditText) this.findViewById(
                R.id.material_spinner_text_field);
        textField.setInputType(InputType.TYPE_NULL);

        final View popupContent = inflater.inflate(R.layout.material_spinner_popup, this, false);
        final RecyclerView recyclerView = (RecyclerView) popupContent.findViewById(
                R.id.material_spinner_popup_list);
        ListDecorator.decorateList(recyclerView);
        listAdapter = new FactoryBasedAdapter<>(new SpinnerCellFactory());
        listAdapter.setSorter(new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                if (o1 == selectedItem) {
                    return -1;
                } else if (o2 == selectedItem) {
                    return 1;
                }

                final int i1 = MaterialSpinner.this.values.indexOf(o1);
                final int i2 = MaterialSpinner.this.values.indexOf(o2);

                return i1 - i2;
            }
        });
        recyclerView.setAdapter(listAdapter);

        final PopupWindow popupWindow = new PopupWindow(popupContent);
        popupWindow.setHeight(WRAP_CONTENT);

        listAdapter.addClickListener(new FactoryBasedAdapter.ClickListener<T>() {
            @Override
            public void itemClicked(T item) {
                selectItem(item);

                textField.clearFocus();
            }
        });

        textField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ViewUtils.hideSoftKeyboard(v);

                    if (!popupWindow.isShowing()) {
                        popupWindow.setWidth(textField.getWidth());
                        final int fieldHeight = textField.getHeight();
                        final int verticalOffset = ViewUtils.dpToPx(6f, context);
                        popupWindow.showAsDropDown(textField, 0, -fieldHeight + verticalOffset);
                    }
                } else {
                    popupWindow.dismiss();
                }
            }
        });

    }

    private void selectItem(T item) {
        this.selectedItem = item;

        listAdapter.clear();
        listAdapter.addAll(this.values);

        if (item == null) {
            textField.setText("");
        } else {
            textField.setText(stringify(item));
        }
    }

    private String stringify(T value) {
        if (value == null) {
            return selectNothingString;
        }

        if (stringConverter != null) {
            return stringConverter.apply(value);
        }

        return value.toString();
    }

    public void setValues(List<T> values) {
        this.values.clear();

        this.values.addAll(values);

        while (this.values.contains(null)) {
            this.values.remove(null);
        }

        this.values.add(null);

        listAdapter.clear();
        listAdapter.addAll(this.values);
    }

    public T getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(T selectedItem) {
        setSelectedItem(selectedItem, false);
    }

    public void setSelectedItem(T selectedItem, boolean withAnimation) {
        boolean hintAnimationEnabled = textInputLayout.isHintAnimationEnabled();

        textInputLayout.setHintAnimationEnabled(withAnimation);

        selectItem(selectedItem);

        textInputLayout.setHintAnimationEnabled(hintAnimationEnabled);
    }

    public void setHint(String hint) {
        textInputLayout.setHint(hint);
    }

    public void setStringConverter(Function<T, String> stringConverter) {
        this.stringConverter = stringConverter;

        listAdapter.notifyDataSetChanged();

        if (selectedItem != null) {
            textField.setText(stringify(selectedItem));
        }
    }

    private class SpinnerCellFactory extends TextCellFactory<T> {

        private Typeface originalTypeface;

        public SpinnerCellFactory() {
            super(HorizontalAlignment.LEFT);
        }

        @Override
        public TextView createEmptyCell(Context context, ViewGroup parent) {
            final TextView emptyCell = super.createEmptyCell(context, parent);

            if (originalTypeface == null) {
                originalTypeface = emptyCell.getTypeface();
            }

            return emptyCell;
        }

        @Override
        public void fillCell(Cell<T> cell, TextView view, CellContext<T> cellContext, ChangeListener<T> listener) {
            final T value = cell.getData();

            final String valueString = stringify(value);

            view.setText(valueString);

            if (value == selectedItem) {
                if (value == null) {
                    view.setTypeface(originalTypeface, Typeface.BOLD_ITALIC);
                } else {
                    view.setTypeface(originalTypeface, Typeface.BOLD);
                }
            } else if (value == null) {
                view.setTypeface(originalTypeface, Typeface.ITALIC);
            } else {
                view.setTypeface(originalTypeface, Typeface.NORMAL);
            }
        }
    }
}
