package io.fabianterhorst.iron;

public interface Loader {
    <T> void load(T call, final String key);
}
