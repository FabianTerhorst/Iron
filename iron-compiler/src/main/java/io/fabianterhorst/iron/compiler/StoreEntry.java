package io.fabianterhorst.iron.compiler;

public class StoreEntry {

    private String name;
    private String key;
    private String className;
    private boolean transaction;
    private boolean listener;
    private boolean loader;
    private boolean async;
    private String defaultValue;

    public StoreEntry(String name, String key, String className, boolean transaction, boolean listener, boolean loader, boolean async, String defaultValue) {
        this.name = name;
        this.key = key;
        this.className = className;
        this.transaction = transaction;
        this.listener = listener;
        this.loader = loader;
        this.async = async;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
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

    public String getDefaultValue() {
        return defaultValue;
    }
}
