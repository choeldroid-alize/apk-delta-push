package com.apkdeltapush.encrypt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class DeltaEncryptorTest {

    private DeltaEncryptor encryptor;

    @BeforeEach
    void setUp() throws Exception {
        encryptor = new DeltaEncryptor();
    }

    @Test
    void encryptAndDecryptRoundTrip() throws Exception {
        byte[] original = "delta-patch-data-v1".getBytes(StandardCharsets.UTF_8);
        EncryptedPayload payload = encryptor.encrypt(original);
        byte[] decrypted = encryptor.decrypt(payload);
        assertArrayEquals(original, decrypted);
    }

    @Test
    void encryptProducesDifferentCiphertextEachTime() throws Exception {
        byte[] data = "same-input".getBytes(StandardCharsets.UTF_8);
        EncryptedPayload p1 = encryptor.encrypt(data);
        EncryptedPayload p2 = encryptor.encrypt(data);
        assertFalse(Arrays.equals(p1.getCiphertext(), p2.getCiphertext()),
                "Each encryption should produce unique ciphertext due to random IV");
    }

    @Test
    void constructorWithRawKeyWorks() throws Exception {
        byte[] key = new byte[32];
        Arrays.fill(key, (byte) 0x42);
        DeltaEncryptor e = new DeltaEncryptor(key);
        byte[] data = "test-payload".getBytes(StandardCharsets.UTF_8);
        EncryptedPayload payload = e.encrypt(data);
        assertArrayEquals(data, e.decrypt(payload));
    }

    @Test
    void invalidKeyLengthThrows() {
        byte[] shortKey = new byte[16];
        assertThrows(IllegalArgumentException.class, () -> new DeltaEncryptor(shortKey));
    }

    @Test
    void decryptNullPayloadThrows() {
        assertThrows(IllegalArgumentException.class, () -> encryptor.decrypt(null));
    }

    @Test
    void getEncodedKeyReturns32Bytes() throws Exception {
        byte[] key = encryptor.getEncodedKey();
        assertEquals(32, key.length);
    }

    @Test
    void encryptEmptyByteArray() throws Exception {
        byte[] empty = new byte[0];
        EncryptedPayload payload = encryptor.encrypt(empty);
        byte[] decrypted = encryptor.decrypt(payload);
        assertArrayEquals(empty, decrypted);
    }
}
