package net.buggy.components.list;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cell<T> {

    private T data;
    private boolean selected = false;
    private boolean enabled = true;
    private final Map<Object, Object> persistentState = new ConcurrentHashMap<>();
    private final Map<Object, Object> viewState = new ConcurrentHashMap<>();

    public Cell(T data) {
        this.data = data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
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

    public Map<Object, Object> getPersistentState() {
        return persistentState;
    }

    public Map<Object, Object> getViewState() {
        return viewState;
    }

    public void resetViewState() {
        viewState.clear();
    }
}
