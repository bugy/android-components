package net.buggy.components.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public abstract class CellFactory<T, V extends View> {

    public abstract V createEmptyCell(Context context, ViewGroup parent);

    public abstract void fillCell(
            Cell<T> cell,
            V view,
            boolean newCell,
            ChangeListener<T> listener);

    public void clearCell(Cell<T> cell, V itemView) {

    }

    public interface ChangeListener<T> {
        void onChange(T newValue);

        void setSelected(boolean selected);
    }

}
