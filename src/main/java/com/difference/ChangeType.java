package com.difference;

public sealed class ChangeType permits PropertyUpdate, ListUpdate {
    String property;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
