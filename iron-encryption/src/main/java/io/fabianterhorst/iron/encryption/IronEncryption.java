package io.fabianterhorst.iron.encryption;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import io.fabianterhorst.iron.Iron;
import io.fabianterhorst.iron.IronEncryptionExtension;

public class IronEncryption implements IronEncryptionExtension {

    protected AesCbcWithIntegrity.SecretKeys mKey;

    public IronEncryption() {
        mKey = getKey();
        Log.d("crypt", "created with key:" + mKey.toString());
    }

    @Override
    public String encrypt(String text) {
        try {
            return AesCbcWithIntegrity.encrypt(text, mKey, "UTF-8").toString();
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public InputStream decrypt(String text) {
        try {
            Log.d("decrypt", text);
            Log.d("decryptResult", AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(text), mKey, "UTF-8"));
            return new ByteArrayInputStream(AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(text), mKey, "UTF-8").getBytes("UTF-8"));
            //return AesCbcWithIntegrity.decryptString(new AesCbcWithIntegrity.CipherTextIvMac(text), mKey);
        } catch (GeneralSecurityException | IOException /*| UnsupportedEncodingException*/ e) {
            e.printStackTrace();
        }
        return null;
    }

    public AesCbcWithIntegrity.SecretKeys getKey(){
        AesCbcWithIntegrity.SecretKeys key = Iron.chest("keys").read("key");
        if (key == null)
            try {
                key = AesCbcWithIntegrity.generateKey();
                Iron.chest("keys").write("key", key);
                AesCbcWithIntegrity.SecretKeys key2 = Iron.chest("keys").read("key");
                Log.d("key", key2.toString());
            } catch (GeneralSecurityException gse) {
                gse.printStackTrace();
            }
        return key;
    }
}
