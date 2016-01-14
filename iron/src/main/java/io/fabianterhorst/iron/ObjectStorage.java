package io.fabianterhorst.iron;


import io.fabianterhorst.iron.util.ArrayMap;

public class ObjectStorage {
    private final ArrayMap<String, Object> mObjects = new ArrayMap<>();
    private final Storage mStorage;

    public ObjectStorage(Storage storage) {
        this.mStorage = storage;
        load();
    }

    private void load() {
        for (String key : mStorage.getAllKeys()) {
            mObjects.put(key, mStorage.doSelect(key));
        }
    }

    public <V> V get(String key) {
        return (V)mObjects.get(key);
    }

    public <V> void add(String key, V value) {
        mObjects.put(key, value);
        mStorage.insert(key, value);
    }

    public void save() {
        for (int i = 0; i < mObjects.size(); i++) {
            mStorage.insert(mObjects.keyAt(i), mObjects.get(i));
        }
    }
}
