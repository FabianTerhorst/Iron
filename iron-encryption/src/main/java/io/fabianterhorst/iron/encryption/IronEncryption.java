package io.fabianterhorst.iron.encryption;

import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import io.fabianterhorst.iron.Encryption;
import io.fabianterhorst.iron.Iron;

public class IronEncryption implements Encryption {

    protected AesCbcWithIntegrity.SecretKeys mKey;

    private static final String CIPHER_TRANSFORMATION = "AES";///CBC/PKCS5Padding
    private static final String CIPHER = "AES";
    private static final int AES_KEY_LENGTH_BITS = 128;
    private static final int HMAC_KEY_LENGTH_BITS = 256;
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String RANDOM_ALGORITHM = "SHA1PRNG";

    private static final int IV_LENGTH_BYTES = 16;

    static final AtomicBoolean prngFixed = new AtomicBoolean(false);

    public IronEncryption() {
        mKey = getKey();
        Log.d("crypt", "created with key:" + mKey.toString());
    }

    private static void fixPrng() {
        if (!prngFixed.get()) {
            synchronized (AesCbcWithIntegrity.PrngFixes.class) {
                if (!prngFixed.get()) {
                    AesCbcWithIntegrity.PrngFixes.apply();
                    prngFixed.set(true);
                }
            }
        }
    }

    private static byte[] randomBytes(int length) throws GeneralSecurityException {
        fixPrng();
        SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);
        byte[] b = new byte[length];
        random.nextBytes(b);
        return b;
    }

    public static byte[] generateIv() throws GeneralSecurityException {
        return randomBytes(IV_LENGTH_BYTES);
    }

    public static AesCbcWithIntegrity.SecretKeys generateKey() throws GeneralSecurityException {
        fixPrng();
        KeyGenerator keyGen = KeyGenerator.getInstance(CIPHER);
        // No need to provide a SecureRandom or set a seed since that will
        // happen automatically.
        keyGen.init(AES_KEY_LENGTH_BITS);
        SecretKey confidentialityKey = keyGen.generateKey();


        AesCbcWithIntegrity.SecretKeySpec secretKeySpec = new AesCbcWithIntegrity.SecretKeySpec();
        secretKeySpec.algorithm = confidentialityKey.getAlgorithm();
        secretKeySpec.format = confidentialityKey.getFormat();
        secretKeySpec.encoded = confidentialityKey.getEncoded();

        //Now make the HMAC key
        byte[] integrityKeyBytes = randomBytes(HMAC_KEY_LENGTH_BITS / 8);//to get bytes
        AesCbcWithIntegrity.SecretKeySpec integrityKey = new AesCbcWithIntegrity.SecretKeySpec();
        integrityKey.generate(integrityKeyBytes, HMAC_ALGORITHM);
        AesCbcWithIntegrity.SecretKeys secretKeys = new AesCbcWithIntegrity.SecretKeys();
        secretKeys.setConfidentialityKey(secretKeySpec/*confidentialityKey*/);
        secretKeys.setIntegrityKey(integrityKey);
        return secretKeys;
    }

    @Override
    public Cipher getCipher(int mode){
        try {
            byte[] iv = generateIv();
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(mode, mKey.getConfidentialityKey(), new IvParameterSpec(iv));
            return cipher;
        }catch(GeneralSecurityException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ByteArrayInputStream decrypt(InputStream inputStream) {
        CipherInputStream cipherInputStream = new CipherInputStream(inputStream, getCipher(Cipher.DECRYPT_MODE));
        try {
            return new ByteArrayInputStream(IOUtils.toByteArray(cipherInputStream));
        }catch(IOException io){
            io.printStackTrace();
        }
        return null;
    }

    @Override
    public CipherOutputStream encrypt(OutputStream outputStream) {
        return new CipherOutputStream(outputStream, getCipher(Cipher.ENCRYPT_MODE));
    }

    public AesCbcWithIntegrity.SecretKeys getKey() {
        AesCbcWithIntegrity.SecretKeys key = Iron.chest("keys").read("key");
        if (key == null)
            try {
                key = generateKey();
                Iron.chest("keys").write("key", key);
            } catch (GeneralSecurityException gse) {
                gse.printStackTrace();
            }
        return key;
    }
}
