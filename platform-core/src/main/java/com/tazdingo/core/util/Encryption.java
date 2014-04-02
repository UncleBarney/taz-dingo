package com.tazdingo.core.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author apple
 */
public class Encryption {

    private static final String UNICODE_FORMAT = "UTF-8";
    private static final String AES_ENCRYPTION_SCHEME = "AES/CBC/PKCS5Padding";
    private static final byte[] IV = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
        0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
    private static final byte[] SALT = new byte[]{72, 34, 1, -98, 41, 68, -55, 34};
    private static final int KEY_LENGTH = 128;
    private static final int ITERATION_COUNT = 65536;

    public static SecretKey generateSecretKey(String encryptionKey) {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            return null;
        }
        SecretKey secretKey = null;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(encryptionKey.toCharArray(), SALT, ITERATION_COUNT, KEY_LENGTH);
            SecretKey tmp = factory.generateSecret(spec);
            secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
        return secretKey;
    }

    public static SecretKey generateSecretKeyFromBytes(byte[] keyBytes) {
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        return secretKey;
    }

    /**
     * Method To Encrypt The String
     *
     * @param key
     * @param input
     * @return
     */
    public static String encrypt(SecretKey key, String input) {
        if (input == null || key == null || input.isEmpty()) {
            return null;
        }
        String encryptedInput = null;
        try {
            Cipher cipher = Cipher.getInstance(AES_ENCRYPTION_SCHEME);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
            String data = input;
            byte[] plainText = data.getBytes(UNICODE_FORMAT);
            byte[] cipherBytes = cipher.doFinal(plainText);
            encryptedInput = Hashing.bytetoBase64String(cipherBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            return null;
        }
        return encryptedInput;
    }

    public static Map<String, String> encrypt(SecretKey key, Map<String, String> dataMap) {
        if (key == null || dataMap == null) {
            return null;
        }
        Map<String, String> encData = null;
        if (dataMap.size() > 0) {
            try {
                Cipher cipher = Cipher.getInstance(AES_ENCRYPTION_SCHEME);
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV));
                Iterator<String> iterator = dataMap.keySet().iterator();
                encData = new HashMap<>();
                String mapKey = null;
                String data = null;
                while (iterator.hasNext()) {
                    mapKey = iterator.next();
                    data = dataMap.get(mapKey);
                    if (data != null && !data.isEmpty()) {
                        byte[] plainText = data.getBytes(UNICODE_FORMAT);
                        byte[] cipherBytes = cipher.doFinal(plainText);
                        encData.put(mapKey, Hashing.bytetoBase64String(cipherBytes));
                    } else {
                        encData.put(mapKey, data);
                    }
                }
            } catch (InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchPaddingException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
                return null;
            }
        }
        return encData;
    }

    /**
     * Method To Decrypt An Ecrypted String
     *
     * @param key
     * @param input
     * @return
     */
    public static String decrypt(SecretKey key, String input) {
        if (input == null || key == null || input.isEmpty()) {
            return null;
        }
        String decryptedInput = null;
        try {
            Cipher cipher = Cipher.getInstance(AES_ENCRYPTION_SCHEME);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
            String data = input;
            byte[] encryptedText = null;
            byte[] plainText = null;
            if (data != null && !data.isEmpty()) {
                encryptedText = Hashing.base64StringToByte(data);
                plainText = cipher.doFinal(encryptedText);
                decryptedInput = new String(plainText);
            } else {
                decryptedInput = data;
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            return ConstantUtil.WRONDKEY;
        }
        return decryptedInput;
    }

    public static Map<String, String> decrypt(SecretKey key, Map<String, String> encData) {
        if (key == null || encData == null) {
            return null;
        }
        if (encData.size() > 0) {
            Map<String, String> decData = new HashMap<>();
            try {
                Cipher cipher = Cipher.getInstance(AES_ENCRYPTION_SCHEME);
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));

                Iterator<String> iterator = encData.keySet().iterator();
                String mapKey = null;
                String value = null;
                byte[] encryptedText = null;
                byte[] plainText = null;
                while (iterator.hasNext()) {
                    mapKey = iterator.next();
                    value = encData.get(mapKey);
                    if (value != null && !value.isEmpty() && !value.equals("null")) {
                        encryptedText = Hashing.base64StringToByte(value);
                        plainText = cipher.doFinal(encryptedText);
                        decData.put(mapKey, new String(plainText));
                    } else {
                        decData.put(mapKey, value);
                    }
                }
            } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                    InvalidKeyException | InvalidAlgorithmParameterException |
                    IllegalBlockSizeException | BadPaddingException e) {
                decData.put(ConstantUtil.ERROR, ConstantUtil.WRONDKEY);
                return decData;
            }
            return decData;
        }
        return null;
    }

    public static boolean validateDecryptedAttributes(String... attributes) {
        if (attributes == null) {
            return false;
        }

        for (String attribute : attributes) {
            if (attribute == null || attribute.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static SecretKey stringToKey(String string) {
        if (string == null) {
            return null;
        }
        byte[] encodedKey = Hashing.base64StringToByte(string);
        SecretKey originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
        return originalKey;
    }

    public static String keyToString(SecretKey key) {
        return Hashing.bytetoBase64String(key.getEncoded());
    }
}
