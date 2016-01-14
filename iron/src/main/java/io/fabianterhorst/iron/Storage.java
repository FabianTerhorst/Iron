package io.fabianterhorst.iron;

import java.util.List;

public interface Storage {
    /**
     * use file storage
     */
    int FILE = 0;
    /**
     * use file and object storage to load all your object to the list at start
     */
    int FILE_OBJECT = 1;

    void destroy();

    <E> void insert(String key, E value);

    <E> E select(String key);

    <E> E doSelect(String key);

    boolean exist(String key);

    void deleteIfExists(String key);

    List<String> getAllKeys();

    void invalidateCache();

    void invalidateCache(String key);
}
