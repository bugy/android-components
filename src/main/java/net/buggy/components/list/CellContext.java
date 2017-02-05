package net.buggy.components.list;


public class CellContext<T> {

    private final Cell<T> cell;
    private final Cell<T> nextCell;
    private final Cell<T> prevCell;
    private final boolean newCell;

    public CellContext(Cell<T> cell, Cell<T> nextCell, Cell<T> prevCell, boolean newCell) {
        this.cell = cell;
        this.nextCell = nextCell;
        this.prevCell = prevCell;
        this.newCell = newCell;
    }

    public Cell<T> getCell() {
        return cell;
    }

    public Cell<T> getNextCell() {
        return nextCell;
    }

    public Cell<T> getPrevCell() {
        return prevCell;
    }

    public boolean isNewCell() {
        return newCell;
    }
}
