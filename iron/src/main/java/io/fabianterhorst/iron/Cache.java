package io.fabianterhorst.iron;

public interface Cache {
    int NONE = 1;

    int MEMORY = 2;

    void evictAll();

    Object put(String key, Object value);

    Object get(String key);

    Object remove(String key);
}
