package net.buggy.components.list;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.util.Predicate;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class FactoryBasedAdapter<T>
        extends RecyclerView.Adapter<FactoryBasedAdapter.ViewHolder> {

    public enum SelectionMode {NONE, SINGLE, MULTI}

    private final static int MAIN_VIEW_TYPE = 0;

    private final CellFactory<T, View> defaultFactory;
    private final List<CellFactory<T, View>> customFactories = new CopyOnWriteArrayList<>();

    private final List<Row<T>> rows = new CopyOnWriteArrayList<>();

    private volatile List<Row<T>> shownRows = rows;

    private final List<DataListener<T>> dataListeners = new CopyOnWriteArrayList<>();
    private final List<SelectionListener<T>> selectionListeners = new CopyOnWriteArrayList<>();
    private Predicate<T> filter = null;

    private SelectionMode selectionMode = SelectionMode.NONE;

    private Comparator<T> sorter;

    public FactoryBasedAdapter(CellFactory<T, ? extends View> defaultFactory) {
        this.defaultFactory = (CellFactory<T, View>) defaultFactory;

        setHasStableIds(true);
    }

    public void setSorter(Comparator<T> sorter) {
        this.sorter = sorter;
    }

    public void setSelectionMode(SelectionMode selectionMode) {
        this.selectionMode = selectionMode;

        if (!rows.isEmpty()) {
            throw new UnsupportedOperationException("Changing selection mode on shown items is not supported");
        }
    }

    @Override
    public int getItemViewType(int viewPosition) {
        final CellFactory<T, View> customFactory = getCustomFactory(viewPosition);

        if (customFactory != null) {
            return 1 + customFactories.indexOf(customFactory);
        }

        return MAIN_VIEW_TYPE;
    }

    @Override
    public FactoryBasedAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CellFactory<T, View> customFactory = null;
        if (viewType != MAIN_VIEW_TYPE) {
            customFactory = customFactories.get(viewType - 1);
        }

        final CellFactory<T, View> cellFactory = (customFactory != null) ? customFactory : defaultFactory;

        final View cell = cellFactory.createEmptyCell(parent.getContext(), parent);

        return new ViewHolder(cell);
    }


    @Override
    public long getItemId(int position) {
        final Row<T> row = shownRows.get(position);

        return row.getId();
    }

    private void toggleSelected(int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);

        Set<Row<T>> changedRows = new LinkedHashSet<>();
        changedRows.add(row);

        row.setSelected(!row.isSelected());
        notifyItemChanged(viewPosition);

        if (row.isSelected() && (selectionMode == SelectionMode.SINGLE)) {
            for (Row<T> anotherRow : rows) {
                if (anotherRow.equals(row)) {
                    continue;
                }

                if (!anotherRow.isSelected()) {
                    continue;
                }

                anotherRow.setSelected(false);

                final int anotherRowIndex = shownRows.indexOf(anotherRow);
                if (anotherRowIndex >= 0) {
                    notifyItemChanged(anotherRowIndex);
                }
            }
        }

        for (Row<T> changedRow : changedRows) {
            fireSelectionChanged(changedRow.getData(), changedRow.isSelected());
        }
    }

    @Override
    public void onBindViewHolder(final FactoryBasedAdapter.ViewHolder holder, final int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);
        final T value = row.getData();
        final View view = holder.getView();

        final CellFactory<T, View> customFactory = getCustomFactory(viewPosition);
        final CellFactory<T, View> cellFactory = (customFactory != null) ? customFactory : defaultFactory;

        cellFactory.fillCell(value, view, new CellFactory.ChangeListener<T>() {
            @Override
            public void onChange(T newValue) {
                fireDataChanged(newValue);
            }
        }, row.isSelected(), row.isEnabled());

        if ((selectionMode != SelectionMode.NONE) && (row.isEnabled())) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int viewPosition = holder.getAdapterPosition();

                    toggleSelected(viewPosition);
                }
            });
        }
    }

    private CellFactory<T, View> getCustomFactory(int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);

        return row.getCustomCellFactory();
    }

    private int viewPositionToModel(int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);

        return rows.indexOf(row);
    }

    @Override
    public int getItemCount() {
        return shownRows.size();
    }

    public void setFilter(final Predicate<T> filter) {
        this.filter = filter;

        applyFilter();

        notifyDataSetChanged();
    }

    private void applyFilter() {
        if (filter != null) {
            final Collection<Row<T>> unfiltered = Collections2.filter(rows, new com.google.common.base.Predicate<Row<T>>() {
                @Override
                public boolean apply(Row<T> input) {
                    return filter.apply(input.getData());
                }
            });
            shownRows = new ArrayList<>(unfiltered);
        } else {
            shownRows = new ArrayList<>(rows);
        }
    }

    public T getItem(int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);
        return (row != null) ? row.getData() : null;
    }

    public void clear() {
        rows.clear();

        applyFilter();

        customFactories.clear();

        notifyDataSetChanged();
    }

    public void add(T item) {
        if (sorter != null) {
            int index = findSortedIndex(item);

            insert(item, index);

        } else {
            insert(item, rows.size());
        }
    }

    private int findSortedIndex(T item) {
        int index = rows.size();

        for (int i = 0; i < rows.size(); i++) {
            final Row<T> row = rows.get(i);

            if (sorter.compare(item, row.getData()) < 0) {
                index = i;

                break;
            }
        }
        return index;
    }

    @SafeVarargs
    public final void addAll(T... items) {
        for (T item : items) {
            add(item);
        }
    }

    public void addAll(Collection<T> items) {
        for (T item : items) {
            add(item);
        }
    }

    public void add(T item, int modelPosition) {
        if (sorter != null) {
            throw new IllegalStateException("Cannot use explicit index insert with sorter");
        }

        insert(item, modelPosition);
    }

    private void insert(T item, int modelPosition) {
        final Row<T> row = new Row<>(item);
        rows.add(modelPosition, row);

        applyFilter();

        final int viewIndex = rows.indexOf(row);
        if (viewIndex >= 0) {
            notifyItemInserted(modelPosition);
        }

        fireDataAdded(item);
    }

    public T remove(int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);
        rows.remove(row);
        applyFilter();

        notifyItemRemoved(viewPosition);
        fireDataRemoved(row.getData());

        return row.getData();
    }

    public void update(T item) {
        final List<Row<T>> itemRows = findRows(item);
        for (Row<T> row : itemRows) {
            row.setData(item);
        }

        Integer minChangedIndex = shownRows.size();
        Integer maxChangedIndex = -1;

        if (sorter != null) {
            for (Row<T> itemRow : itemRows) {
                rows.remove(itemRow);
            }

            for (Row<T> itemRow : itemRows) {
                final int index = findSortedIndex(itemRow.getData());
                rows.add(index, itemRow);
            }

            for (Row<T> row : itemRows) {
                final int index = shownRows.indexOf(row);
                if (index == -1) {
                    continue;
                }

                if (minChangedIndex > index) {
                    minChangedIndex = index;
                }
                if (maxChangedIndex < index) {
                    maxChangedIndex = index;
                }
            }

            applyFilter();
        }


        for (Row<T> row : itemRows) {
            final int index = shownRows.indexOf(row);
            if (index == -1) {
                continue;
            }

            if (minChangedIndex > index) {
                minChangedIndex = index;
            }
            if (maxChangedIndex < index) {
                maxChangedIndex = index;
            }
        }

        for (int i = minChangedIndex; i < maxChangedIndex + 1; i++) {
            notifyItemChanged(i);
        }

        fireDataChanged(item);
    }

    public void remove(T item) {
        final List<Row<T>> itemRows = findRows(item);

        this.rows.removeAll(itemRows);

        for (Row<T> row : itemRows) {
            final int index = shownRows.indexOf(row);
            if (index == -1) {
                continue;
            }

            shownRows.remove(index);
            notifyItemRemoved(index);
        }

        fireDataRemoved(item);
    }

    public List<T> getAllItems() {
        List<T> result = new ArrayList<>(rows.size());

        for (Row<T> row : rows) {
            result.add(row.getData());
        }

        return result;
    }

    public List<T> getSelectedItems() {
        List<T> result = new ArrayList<>();

        for (Row<T> row : rows) {
            if (row.isSelected()) {
                result.add(row.getData());
            }
        }

        return result;
    }

    public void addDataListener(DataListener<T> listener) {
        dataListeners.add(listener);
    }

    public void addSelectionListener(SelectionListener<T> listener) {
        selectionListeners.add(listener);
    }

    public void selectItem(T item) {
        setItemSelected(item, true);
    }

    public void deselectItem(T item) {
        setItemSelected(item, false);
    }

    private void setItemSelected(T item, boolean selected) {
        final List<Row<T>> rows = findRows(item);
        for (Row<T> row : rows) {
            row.setSelected(selected);
            fireSelectionChanged(item, selected);
        }
    }

    public void disableItem(T item) {
        final List<Row<T>> itemRows = findRows(item);
        for (Row<T> row : itemRows) {
            row.setEnabled(false);
        }
    }

    private List<Row<T>> findRows(T item) {
        List<Row<T>> result = new ArrayList<>();
        for (Row<T> row : rows) {
            if (Objects.equal(row.getData(), item)) {
                result.add(row);
            }
        }
        return result;
    }

    private void fireDataAdded(T item) {
        for (DataListener<T> listener : dataListeners) {
            listener.added(item);
        }
    }

    private void fireDataChanged(T changedItem) {
        for (DataListener<T> listener : dataListeners) {
            listener.changed(changedItem);
        }
    }

    private void fireDataRemoved(T item) {
        for (DataListener<T> listener : dataListeners) {
            listener.removed(item);
        }
    }

    private void fireSelectionChanged(T item, boolean selected) {
        for (SelectionListener<T> listener : selectionListeners) {
            listener.selectionChanged(item, selected);
        }
    }

    public void setCustomFactory(CellFactory<T, View> newFactory, int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);

        final CellFactory<T, View> existingFactory = row.getCustomCellFactory();


        if (existingFactory != null) {
            int index = customFactories.indexOf(existingFactory);

            if (index >= 0) {
                customFactories.set(index, newFactory);
            }

        } else if (newFactory != null) {
            int nullIndex = customFactories.indexOf(null);

            if (nullIndex >= 0) {
                customFactories.set(nullIndex, newFactory);
            } else {
                customFactories.add(newFactory);
            }
        }

        row.setCustomCellFactory(newFactory);

        notifyItemChanged(viewPosition);
    }

    private static class Row<T> {

        private static long idCounter = 1;

        private T data;
        private CellFactory<T, View> customCellFactory;
        private boolean selected = false;
        private boolean enabled = true;
        private final long id = idCounter++;

        public Row(T data) {
            this.data = data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public CellFactory<T, View> getCustomCellFactory() {
            return customCellFactory;
        }

        public void setCustomCellFactory(CellFactory<T, ? extends View> customCellFactory) {
            this.customCellFactory = (CellFactory<T, View>) customCellFactory;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public long getId() {
            return id;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final View view;

        ViewHolder(View view) {
            super(view);

            this.view = view;
        }

        public View getView() {
            return view;
        }
    }

    public interface DataListener<T> {
        void added(T item);

        void removed(T item);

        void changed(T changedItem);
    }

    public interface SelectionListener<T> {
        void selectionChanged(T item, boolean selected);
    }
}
