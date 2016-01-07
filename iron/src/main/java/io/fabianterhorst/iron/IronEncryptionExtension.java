package io.fabianterhorst.iron;

public interface IronEncryptionExtension {
    String encrypt(String text);
    String decrypt(String text);
}
