package com.difference;

import java.util.List;


public final class ListUpdate<T> extends ChangeType {
    List<T> added;
    List<T> removed;

    public List<T> getAdded() {
        return added;
    }

    public void setAdded(List<T> added) {
        this.added = added;
    }

    public List<T> getRemoved() {
        return removed;
    }

    public void setRemoved(List<T> removed) {
        this.removed = removed;
    }
}
