package io.fabianterhorst.iron;

import java.io.InputStream;

public interface IronEncryptionExtension {
    String encrypt(String text);
    InputStream decrypt(String text);
}
