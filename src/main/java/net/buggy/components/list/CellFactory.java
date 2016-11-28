package net.buggy.components.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public interface CellFactory<T, V extends View> {

    V createEmptyCell(Context context, ViewGroup parent);

    void fillCell(T value, V view, ChangeListener<T> listener, boolean selected, boolean enabled);

    interface ChangeListener<T> {
        void onChange(T newValue);
    }
}
