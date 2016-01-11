package io.fabianterhorst.iron.compiler;

public class StoreEntry {

    private String key;
    private String className;
    private boolean transaction;
    private boolean listener;
    private boolean loader;
    private boolean async;

    public StoreEntry(String key, String className, boolean transaction,boolean listener, boolean loader, boolean async) {
        this.key = key;
        this.className = className;
        this.transaction = transaction;
        this.listener = listener;
        this.loader = loader;
        this.async = async;
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

    public boolean isLoader() {
        return loader;
    }

    public boolean isAsync() {
        return async;
    }
}
