package io.fabianterhorst.iron;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;

public interface IronEncryptionExtension {
    Cipher getCipher(int mode);
    ByteArrayInputStream decrypt(InputStream text);
    CipherOutputStream encrypt(OutputStream bytes);
}
