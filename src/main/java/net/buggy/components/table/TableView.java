package net.buggy.components.table;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import net.buggy.components.scrollable.ScrollListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class TableView extends LinearLayout {

    private TableModel tableModel;
    private TableLayout headerLayout;
    private TableLayout bodyLayout;

    private final Map<Integer, Integer> fixedColumnWidths = Maps.newLinkedHashMap();
    private Table<Integer, Integer, View> bodyCells = HashBasedTable.create();
    private List<View> headerCells = new CopyOnWriteArrayList<>();

    private final Map<Integer, CellFactory> columnCellFactories = new ConcurrentHashMap<>();
    private CellFactory defaultCellFactory = new TextCellFactory();

    private final List<ScrollListener> scrollListeners = new CopyOnWriteArrayList<>();

    private Runnable scrollerTask;
    private int scrollPosition;

    public TableView(Context context) {
        super(context);
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;

        Context context = getContext();

        if (tableModel.getHeader() != null) {
            headerCells = createHeaderCells(context, tableModel.getHeader());
            headerLayout = createHeaderView(context, headerCells);
            setLayoutParams(headerLayout, MATCH_PARENT, WRAP_CONTENT);
        } else {
            headerCells.clear();
            headerLayout = null;
        }

        bodyLayout = new TableLayout(context);

        setLayoutParams(bodyLayout, MATCH_PARENT, WRAP_CONTENT);

        bodyCells = createBodyCells(context);
        for (int i = 0; i < tableModel.rowCount(); i++) {
            TableRow tableRow = createRow(context);

            for (int j = 0; j < tableModel.columnCount(); j++) {
                View cell = bodyCells.get(i, j);
                tableRow.addView(cell);
            }
            bodyLayout.addView(tableRow);
        }

        ScrollView scrollView = new ScrollView(context);
        setLayoutParams(scrollView, MATCH_PARENT, WRAP_CONTENT);
        scrollView.addView(bodyLayout);

        LinearLayout mainLayout = new LinearLayout(context);
        setLayoutParams(mainLayout, MATCH_PARENT, WRAP_CONTENT);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        if (headerLayout != null) {
            mainLayout.addView(headerLayout);
        }
        mainLayout.addView(scrollView);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context) {
            @Override
            protected void onScrollChanged(int l, int t, int oldl, int oldt) {
                super.onScrollChanged(l, t, oldl, oldt);

                for (ScrollListener listener : scrollListeners) {
                    listener.scrolled(l, t, oldl, oldt);
                }
            }
        };

        setLayoutParams(horizontalScrollView, WRAP_CONTENT, MATCH_PARENT);
        horizontalScrollView.addView(mainLayout);
        removeAllViews();

        addView(horizontalScrollView);
    }

    public void setColumnWidth(int column, int width) {
        fixedColumnWidths.put(column, width);
    }

    public void setDefaultCellFactory(CellFactory defaultCellFactory) {
        this.defaultCellFactory = defaultCellFactory;
    }

    public void setColumnCellFactory(int column, CellFactory cellFactory) {
        columnCellFactories.put(column, cellFactory);
    }

    public void addScrollListener(ScrollListener listener) {
        scrollListeners.add(listener);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (headerLayout != null) {
            for (int column = 0; column < tableModel.columnCount(); column++) {
                int width;
                if (fixedColumnWidths.get(column) != null) {
                    width = fixedColumnWidths.get(column);
                } else {
                    width = calcMaxWidth(column);
                }

                List<View> cells = ImmutableList.<View>builder()
                        .addAll(bodyCells.column(column).values())
                        .add(headerCells.get(column))
                        .build();

                for (View cell : cells) {
                    final TableRow.LayoutParams layoutParams = (TableRow.LayoutParams) cell.getLayoutParams();
                    layoutParams.width = width;
                    layoutParams.height = LayoutParams.MATCH_PARENT;
                }
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int calcMaxWidth(int column) {
        View headerCell = headerCells.get(column);

        int maxWidth = getCellWidth(headerCell);

        for (int row = 0; row < tableModel.rowCount(); row++) {
            View cell = bodyCells.get(row, column);
            maxWidth = Math.max(maxWidth, getCellWidth(cell));
        }

        return maxWidth;
    }

    private int getCellWidth(View cell) {
        int maxWidth = cell.getMeasuredWidth();
        if (maxWidth <= 0) {
            cell.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            maxWidth = cell.getMeasuredWidth();
        }

        return maxWidth;
    }

    private Table<Integer, Integer, View> createBodyCells(Context context) {
        Table<Integer, Integer, View> result = HashBasedTable.create();

        for (int i = 0; i < tableModel.rowCount(); i++) {
            for (int j = 0; j < tableModel.columnCount(); j++) {

                Object value = tableModel.getValueAt(i, j);
                View cell = createBodyCell(context, value, i, j);
                result.put(i, j, cell);
            }
        }

        return result;
    }

    private View createBodyCell(Context context, Object value, int row, int column) {
        CellFactory cellFactory = columnCellFactories.get(column);
        if (cellFactory == null) {
            cellFactory = defaultCellFactory;
        }

        return cellFactory.createCell(value, row, column, context);
    }

    private static void setLayoutParams(View view, int width, int height) {
        LayoutParams layoutParams = new LayoutParams(width, height);
        view.setLayoutParams(layoutParams);
    }

    private TableLayout createHeaderView(Context context, List<View> headerCells) {
        TableLayout result = new TableLayout(context);
        TableRow headerRow = createRow(context);
        headerRow.setBackgroundColor(Color.LTGRAY);
        result.addView(headerRow);

        for (View cell : headerCells) {
            headerRow.addView(cell);
        }

        return result;
    }

    private List<View> createHeaderCells(Context context, List<String> header) {
        List<View> cells = new ArrayList<>();
        for (String headerText : header) {
            TextView cell = createHeaderCell(context, headerText);
            cell.setTypeface(cell.getTypeface(), Typeface.BOLD);

            cells.add(cell);
        }

        return cells;
    }

    private TextView createHeaderCell(Context context, Object value) {
        TextView view = new TextView(context);
        if (value != null) {
            view.setPadding(6, 6, 6, 6);
            view.setText(String.valueOf(value));
            view.setGravity(Gravity.CENTER);
        }

        return view;
    }

    @NonNull
    private TableRow createRow(Context context) {
        TableRow headerRow = new TableRow(context);
        headerRow.setLayoutParams(new TableRow.LayoutParams(
                MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));

        return headerRow;
    }

}
