package io.fabianterhorst.iron.compiler;

public class StoreEntry {

    String key;
    String className;

    public StoreEntry(String key, String className) {
        this.key = key;
        this.className = className;
    }

    public String getKey() {
        return key;
    }

    public String getClassName() {
        return className;
    }
}
