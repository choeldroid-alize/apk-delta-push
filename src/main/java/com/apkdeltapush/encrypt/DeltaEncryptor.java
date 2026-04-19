package com.apkdeltapush.encrypt;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Encrypts and decrypts delta patch payloads using AES-GCM.
 */
public class DeltaEncryptor {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_SIZE = 256;

    private final SecretKey secretKey;

    public DeltaEncryptor(byte[] rawKey) {
        if (rawKey == null || rawKey.length != 32) {
            throw new IllegalArgumentException("Key must be 32 bytes (256-bit)");
        }
        this.secretKey = new SecretKeySpec(rawKey, "AES");
    }

    public DeltaEncryptor() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE);
        this.secretKey = keyGen.generateKey();
    }

    public EncryptedPayload encrypt(byte[] plaintext) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] ciphertext = cipher.doFinal(plaintext);
        return new EncryptedPayload(iv, ciphertext);
    }

    public byte[] decrypt(EncryptedPayload payload) throws Exception {
        if (payload == null) {
            throw new IllegalArgumentException("Payload must not be null");
        }
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, payload.getIv()));
        return cipher.doFinal(payload.getCiphertext());
    }

    public byte[] getEncodedKey() {
        return secretKey.getEncoded();
    }
}
