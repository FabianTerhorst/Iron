package io.fabianterhorst.iron;

public interface Cache {

    void evictAll();

    Object put(String key, Object value);

    Object get(String key);

    Object remove(String key);

    int NONE = 1;

    int MEMORY = 2;
}
