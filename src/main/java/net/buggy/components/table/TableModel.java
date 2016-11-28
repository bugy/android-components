
package net.buggy.components.table;

import java.util.List;

public interface TableModel {

    void setHeader(List<String> header);

    List<String> getHeader();

    Object getValueAt(int row, int column);

    void setValueAt(Object value, int row, int column);

    void addListener(TableModelListener listener);

    int rowCount();

    int columnCount();

    interface TableModelListener {
        void valueChanged(Object newValue, Object oldValue, int row, int column);
        void headerChanged(List<String> newHeader, List<String> oldHeader);
    }
}
