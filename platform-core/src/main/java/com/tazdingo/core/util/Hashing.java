package com.tazdingo.core.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author apple
 */
public class Hashing {

    public enum HashingTechqniue {

        SSHA256("SHA-256"), MD5("MD5");
        private final String value;

        private HashingTechqniue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static byte[] getHash(String input, HashingTechqniue technique) {
        if (input == null || input.isEmpty() || technique == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(technique.value);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        digest.reset();
        byte[] hashedBytes = digest.digest(base64StringToByte(input));
        return hashedBytes;
    }

    public static byte[] base64StringToByte(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        return Base64.decodeBase64(input);
    }

    public static String bytetoBase64String(byte[] input) {
        if (input == null) {
            return null;
        }
        return Base64.encodeBase64String(input);
    }

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        return bytes;
    }

    public static byte[] getHashWithSalt(String input, HashingTechqniue technique, byte[] salt) {
        if (input == null || input.isEmpty() || technique == null || salt == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(technique.value);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        digest.reset();
        digest.update(salt);
        byte[] hashedBytes = digest.digest(base64StringToByte(input));
        return hashedBytes;
    }

    public static String getSessionKey() {
        return bytetoBase64String(generateSalt());
    }
}
