package io.fabianterhorst.iron;

import java.io.InputStream;

public interface IronEncryptionExtension {
    InputStream decrypt(String text);
    String encrypt(byte[] bytes);
}
