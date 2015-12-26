package io.fabianterhorst.iron;

public interface IronLoadExtension {
    <T> void load(T call, final String key);
}
