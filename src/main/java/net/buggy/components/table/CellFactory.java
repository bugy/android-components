package net.buggy.components.table;


import android.content.Context;
import android.view.View;

public interface CellFactory {

    View createCell(Object value, int row, int column, Context context);
}
