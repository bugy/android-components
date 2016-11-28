package net.buggy.components.table;


import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimpleTableModel implements TableModel {

    private final Table<Integer, Integer, Object> table = HashBasedTable.create();
    private final List<TableModelListener> listeners = new CopyOnWriteArrayList<>();
    private final List<String> header = new CopyOnWriteArrayList<>();
    private final int rows;
    private final int columns;

    public SimpleTableModel(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }


    @Override
    public void setHeader(List<String> header) {
        if ((header != null) && (header.size() != columns)) {
            throw new IllegalStateException("Headers count should match column count");
        }

        List<String> oldHeader;
        List<String> newHeader;

        synchronized (this.header) {
            oldHeader = ImmutableList.copyOf(this.header);
            this.header.clear();
            if (header != null) {
                this.header.addAll(header);
                newHeader = ImmutableList.copyOf(header);
            } else {
                newHeader = null;
            }
        }


        fireHeaderChanged(newHeader, oldHeader);
    }

    private void fireHeaderChanged(List<String> newHeader, List<String> oldHeader) {
        for (TableModelListener listener : listeners) {
            try {
                listener.headerChanged(newHeader, oldHeader);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public List<String> getHeader() {
        synchronized (header) {
            if (header.isEmpty()) {
                return null;
            } else {
                return ImmutableList.copyOf(header);
            }
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object value;
        synchronized (table) {
            value = table.get(row, column);
        }
        return value;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        Object oldValue;
        synchronized (table) {
            if (value == null) {
                oldValue = table.remove(row, column);
            } else {
                oldValue = table.put(row, column, value);
            }
        }

        fireCellChanged(value, oldValue, row, column);
    }

    private void fireCellChanged(Object value, Object oldValue, int row, int column) {
        for (TableModelListener listener : listeners) {
            try {
                listener.valueChanged(value, oldValue, row, column);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void addListener(TableModelListener listener) {
        listeners.add(listener);
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int columnCount() {
        return columns;
    }
}
