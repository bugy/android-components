package net.buggy.components.list;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.android.internal.util.Predicate;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FactoryBasedAdapter<T>
        extends RecyclerView.Adapter<FactoryBasedAdapter.ViewHolder> {

    public enum SelectionMode {NONE, SINGLE, MULTI}

    public enum ChangeType {SELECTION, DATA, ENABLE, REDRAW}

    private final static int MAIN_VIEW_TYPE = 0;

    private final CellFactory<T, View> defaultFactory;
    private final List<CellFactory<T, View>> customFactories = new CopyOnWriteArrayList<>();

    private final List<Row<T>> rows = new CopyOnWriteArrayList<>();

    private volatile List<Row<T>> shownRows = rows;

    private final List<DataListener<T>> dataListeners = new CopyOnWriteArrayList<>();
    private final List<SelectionListener<T>> selectionListeners = new CopyOnWriteArrayList<>();
    private final List<ClickListener<T>> clickListeners = new CopyOnWriteArrayList<>();
    private final List<ClickListener<T>> longClickListeners = new CopyOnWriteArrayList<>();
    private Predicate<T> filter = null;

    private SelectionMode selectionMode = SelectionMode.NONE;

    private Comparator<T> sorter;

    public <V extends View> FactoryBasedAdapter(CellFactory<T, V> defaultFactory) {
        this.defaultFactory = (CellFactory<T, View>) defaultFactory;

        setHasStableIds(true);
    }

    public void setSorter(final Comparator<T> sorter) {
        this.sorter = sorter;

        if (sorter != null) {
            Collections.sort(rows, new Comparator<Row<T>>() {
                @Override
                public int compare(Row<T> o1, Row<T> o2) {
                    return sorter.compare(o1.getData(), o2.getData());
                }
            });

            applyFilter();

            if (!shownRows.isEmpty()) {
                notifyItemRangeChanged(0, shownRows.size());
            }
        }
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

        final View view = cellFactory.createEmptyCell(parent.getContext(), parent);

        return new ViewHolder(view, cellFactory);
    }


    @Override
    public long getItemId(int position) {
        final Row<T> row = shownRows.get(position);

        return row.getId();
    }

    private void toggleSelected(int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);

        setItemSelected(row.getData(), !row.isSelected());
    }

    @Override
    public void onBindViewHolder(final FactoryBasedAdapter.ViewHolder holder, final int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);
        final View view = holder.itemView;

        final Cell<T> cell = row.getCell();

        boolean newCell = !Objects.equal(cell, holder.getCell());
        if (newCell) {
            cell.resetViewState();
            holder.setCell(cell);
        }

        final CellContext<T> cellContext = createCellContext(viewPosition, cell, newCell);

        final CellFactory<T, View> factory = holder.getFactory();
        factory.fillCell(cell, view, cellContext,
                new CellFactory.ChangeListener<T>() {
                    @Override
                    public void onChange(T newValue) {
                        for (int i = 0; i < shownRows.size(); i++) {
                            final Row<T> shownRow = shownRows.get(i);

                            if (Objects.equal(shownRow.getData(), newValue)) {
                                notifyItemChanged(i, ChangeType.DATA);
                            }
                        }

                        fireDataChanged(newValue);
                    }

                    @Override
                    public void setSelected(boolean selected) {
                        setItemSelected(cell.getData(), selected);
                    }

                    @Override
                    public void redraw(Cell<T> cell) {
                        for (int i = 0; i < shownRows.size(); i++) {
                            final Row<T> row = shownRows.get(i);
                            if (Objects.equal(row.getCell(), cell)) {
                                notifyItemChanged(i, ChangeType.REDRAW);
                            }
                        }
                    }
                }
        );

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!clickListeners.isEmpty()) {
                    for (ClickListener<T> listener : clickListeners) {
                        listener.itemClicked(row.getData());
                    }
                    return;
                }

                if ((selectionMode != SelectionMode.NONE) && (row.isEnabled())) {
                    final int viewPosition = holder.getAdapterPosition();

                    toggleSelected(viewPosition);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!longClickListeners.isEmpty()) {
                    for (ClickListener<T> listener : longClickListeners) {
                        listener.itemClicked(row.getData());
                    }

                    return true;
                }

                if ((selectionMode != SelectionMode.NONE) && (row.isEnabled())) {
                    final int viewPosition = holder.getAdapterPosition();

                    toggleSelected(viewPosition);

                    return true;
                }

                return false;
            }
        });
    }

    private CellContext<T> createCellContext(int viewPosition, Cell<T> cell, boolean newCell) {
        Cell<T> prevCell;
        if (viewPosition == 0) {
            prevCell = null;
        } else {
            prevCell = shownRows.get(viewPosition - 1).getCell();
        }

        Cell<T> nextCell;
        if (viewPosition == (shownRows.size() - 1)) {
            nextCell = null;
        } else {
            nextCell = shownRows.get(viewPosition + 1).getCell();
        }
        return new CellContext<>(cell, nextCell, prevCell, newCell);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        final View itemView = holder.itemView;

        final CellFactory<Object, View> factory = holder.getFactory();
        final Cell<Object> cell = holder.getCell();

        factory.clearCell(cell, itemView);

        cell.resetViewState();
        holder.setCell(null);
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

            notifyNeighboursRedraw(viewIndex, false);
        }

        fireDataAdded(item);
    }

    public T remove(int viewPosition) {
        final Row<T> row = shownRows.get(viewPosition);
        rows.remove(row);
        applyFilter();

        notifyItemRemoved(viewPosition);
        notifyNeighboursRedraw(viewPosition, true);
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
            notifyItemChanged(i, ChangeType.DATA);
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
            notifyNeighboursRedraw(index, true);
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
            if ((row.isSelected() && (row.isEnabled()))) {
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
        if (selected && (selectionMode == SelectionMode.NONE)) {
            return;
        }

        final List<Row<T>> rows = findRows(item);
        for (Row<T> row : rows) {
            if (selected && !row.isEnabled()) {
                continue;
            }

            if (row.isSelected() == selected) {
                continue;
            }

            row.setSelected(selected);

            final int position = shownRows.indexOf(row);
            if (position >= 0) {
                notifyItemChanged(position, ChangeType.SELECTION);
                notifyNeighboursRedraw(position, false);
            }

            fireSelectionChanged(item, selected);
        }

        if (selected && (selectionMode == SelectionMode.SINGLE)) {
            for (Row<T> anotherRow : this.rows) {
                if (rows.contains(anotherRow)) {
                    continue;
                }

                if (!anotherRow.isSelected()) {
                    continue;
                }

                anotherRow.setSelected(false);

                final int anotherRowIndex = shownRows.indexOf(anotherRow);
                if (anotherRowIndex >= 0) {
                    notifyItemChanged(anotherRowIndex, ChangeType.SELECTION);
                    notifyNeighboursRedraw(anotherRowIndex, false);
                }
            }
        }
    }

    private void notifyNeighboursRedraw(int rowIndex, boolean rowDeleted) {
        if (rowIndex > 0) {
            notifyItemChanged(rowIndex - 1, ChangeType.REDRAW);
        }

        int nextRowIndex = rowDeleted ? rowIndex : rowIndex + 1;
        if (nextRowIndex < shownRows.size()) {
            notifyItemChanged(nextRowIndex, ChangeType.REDRAW);
        }
    }

    public void disableItem(T item) {
        final List<Row<T>> itemRows = findRows(item);
        final List<Integer> changedPositions = new ArrayList<>();

        for (Row<T> row : itemRows) {
            row.setEnabled(false);

            if (row.isSelected()) {
                row.setSelected(false);

                fireSelectionChanged(item, false);
            }

            final int index = shownRows.indexOf(row);
            if (index >= 0) {
                changedPositions.add(index);
            }
        }

        for (Integer position : changedPositions) {
            notifyItemChanged(position, ChangeType.ENABLE);
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

    private void fireDataAdded(final T item) {
        for (DataListener<T> listener : dataListeners) {
            listener.added(item);
        }
    }

    private void fireDataChanged(final T changedItem) {
        for (DataListener<T> listener : dataListeners) {
            listener.changed(changedItem);
        }
    }

    private void fireDataRemoved(final T item) {
        for (DataListener<T> listener : dataListeners) {
            listener.removed(item);
        }
    }

    private void fireSelectionChanged(final T item, final boolean selected) {
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

        private final Cell<T> cell;
        private CellFactory<T, View> customCellFactory;
        private final long id = idCounter++;

        public Row(T data) {
            this.cell = new Cell<>(data);
        }

        public Cell<T> getCell() {
            return cell;
        }

        public void setData(T data) {
            cell.setData(data);
        }

        public T getData() {
            return cell.getData();
        }

        public CellFactory<T, View> getCustomCellFactory() {
            return customCellFactory;
        }

        public <V extends View> void setCustomCellFactory(CellFactory<T, V> customCellFactory) {
            this.customCellFactory = (CellFactory<T, View>) customCellFactory;
        }

        public boolean isSelected() {
            return cell.isSelected();
        }

        public void setSelected(boolean selected) {
            cell.setSelected(selected);
        }

        public boolean isEnabled() {
            return cell.isEnabled();
        }

        public void setEnabled(boolean enabled) {
            cell.setEnabled(enabled);
        }

        public long getId() {
            return id;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private Cell cell;
        private final CellFactory<Object, View> factory;

        private <T> ViewHolder(View view, CellFactory<T, View> factory) {
            super(view);
            this.factory = (CellFactory<Object, View>) factory;
        }

        public <T> CellFactory<T, View> getFactory() {
            return (CellFactory<T, View>) factory;
        }

        public <T> Cell<T> getCell() {
            return (Cell<T>) cell;
        }

        public void setCell(Cell cell) {
            this.cell = cell;
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

    public interface ClickListener<T> {
        void itemClicked(T item);
    }

    public void addClickListener(ClickListener<T> clickListener) {
        clickListeners.add(clickListener);
    }

    public void addLongClickListener(ClickListener<T> clickListener) {
        longClickListeners.add(clickListener);
    }
}
