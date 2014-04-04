package com.tazdingo.platform.keymanager;

/**
 *
 * @author Cynthia
 */
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import com.tazdingo.core.util.Encryption;

public class ConstructKey {

    private final String platformname;
    private final String platformpassword;
    private SecretKey sessionMasterKey;
    private SecretKey keyServerMasterKey;
    private SecretKey platformMasterKey;

    public ConstructKey(String name, String password, String adminpassword) {
        platformname = name;
        keyServerMasterKey = Encryption.generateSecretKey(password);
        platformMasterKey = Encryption.generateSecretKey(password);
        SecretKey key = Encryption.generateSecretKey(adminpassword);
        platformpassword = Encryption.encrypt(key, password);
        KeyGenerator keygenerator;
        try {
            keygenerator = KeyGenerator.getInstance("AES");
            sessionMasterKey = keygenerator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ConstructKey.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void updatePassword(String pass) {
        keyServerMasterKey = Encryption.generateSecretKey(pass);
    }

    public String getPlatformpassword() {
        return platformpassword;
    }

    public String getPlatformname() {
        return platformname;
    }

    public SecretKey getSessionMasterKey() {
        return sessionMasterKey;
    }

    public SecretKey getKeyServerMasterKey() {
        return keyServerMasterKey;
    }

    public void setKeyServerMasterKey(SecretKey keyServerMasterKey) {
        this.keyServerMasterKey = keyServerMasterKey;
    }

    public SecretKey getPlatformMasterKey() {
        return platformMasterKey;
    }
    
}
