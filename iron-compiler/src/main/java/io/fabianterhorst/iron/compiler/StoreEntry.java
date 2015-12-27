package io.fabianterhorst.iron.compiler;

public class StoreEntry {

    String key;
    String className;
    boolean transaction;
    boolean listener;

    public StoreEntry(String key, String className, boolean transaction,boolean listener) {
        this.key = key;
        this.className = className;
        this.transaction = transaction;
        this.listener = listener;
    }

    public String getKey() {
        return key;
    }

    public String getClassName() {
        return className;
    }

    public boolean isTransaction() {
        return transaction;
    }

    public boolean isListener() {
        return listener;
    }
}
