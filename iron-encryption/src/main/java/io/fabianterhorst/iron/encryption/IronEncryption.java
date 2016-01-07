package io.fabianterhorst.iron.encryption;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.IronEncryptionExtension;

public class IronEncryption implements IronEncryptionExtension {

    protected AesCbcWithIntegrity.SecretKeys mKey;

    public IronEncryption() {
        mKey = Iron.chest().read("key");
        if (mKey == null)
            try {
                mKey = AesCbcWithIntegrity.generateKey();
                Iron.chest().write("key", mKey);
                AesCbcWithIntegrity.SecretKeys key = Iron.chest().read("key");
                Log.d("key", key.toString());
            } catch (GeneralSecurityException gse) {
                gse.printStackTrace();
            }
    }

    @Override
    public String encrypt(String text) {
        try {
            return AesCbcWithIntegrity.encrypt(text, mKey).toString();
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String decrypt(String text) {
        try {
            return AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(text), mKey);
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
