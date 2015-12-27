package io.fabianterhorst.iron;

import java.util.List;

interface Storage {

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
