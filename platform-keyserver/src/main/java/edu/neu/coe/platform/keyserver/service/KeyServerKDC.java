/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.keyserver.service;

import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import com.tazdingo.core.util.Hashing;
import com.tazdingo.kerberos.KDC;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import edu.neu.coe.platform.keyserver.KeyServer;
import edu.neu.coe.platform.keyserver.ticket.PlatformTGT;



/**
 *
 * @author Cynthia
 */
public class KeyServerKDC extends KDC {
    
    private final List<String> platformnamelist=new ArrayList<>();
    private final SecretKey keyServerMasterKey;
    private KeyStore platformkeystore;
    private KeyStore platformPasswordKeystore;
    private final String keystorefilespath;
    //private String platformname;
    //private SecretKey platformmasterkey;
    
    public KeyServerKDC(SecretKey keyServerMasterKey,String keystorefilespath){
        this.keyServerMasterKey=keyServerMasterKey;
        this.keystorefilespath=keystorefilespath;
    }
   
    
    public void init(String password){
        try {
            char[] p=password.toCharArray();
            platformPasswordKeystore= KeyStore.getInstance("JCEKS");
            platformPasswordKeystore.load(null, p);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.PLATFORMPASSWORD_KEYSTORE);
            platformPasswordKeystore.store(fos, p);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(KeyServerKDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void load(String password){
        try {
            char[] p=password.toCharArray();
            platformPasswordKeystore= KeyStore.getInstance("JCEKS");
            java.io.FileInputStream fis = new java.io.FileInputStream(keystorefilespath+ConstantUtil.PLATFORMPASSWORD_KEYSTORE);
            platformPasswordKeystore.load(fis, p);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(KeyServerKDC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void save(String password){
        try {java.io.FileOutputStream fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.PLATFORMPASSWORD_KEYSTORE);
            char[] p=password.toCharArray();
            platformPasswordKeystore.store(fos, p);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(KeyServerKDC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    public void setPlatformkeystore(KeyStore platformkeystore) {
        this.platformkeystore = platformkeystore;
    }
    
    
    

    @Override
    protected String getIdentifier(Map<String, String> data, String type) {
        
       return data.get(ConstantUtil.PLATFORM_NAME);
    }

    @Override
    protected String checkIdentifier(String identifier, String type) {
        if(platformnamelist.contains(identifier))
            return ConstantUtil.NO_ERROR;
        return ConstantUtil.DEFAULT_IDENTIFIER;
    }

    @Override
    protected SecretKey getKey(String identifier, String type,String sessionid) {
       return getKey(identifier);
    }

    @Override
    protected String generateKDCTicket(String identifier, String type, String sessionid) {
        System.out.println("encrypted ticket with key:"+keyServerMasterKey);
        String ticket=(new PlatformTGT(sessionid, identifier)).converToString();
        //System.out.println(ticket);
        return Encryption.encrypt(keyServerMasterKey, ticket);
    }
    
    public void addPlatform(String platformname,String platformpassword){
        SecretKey key=Encryption.generateSecretKey(platformpassword);
        KeyStore.SecretKeyEntry entry=new KeyStore.SecretKeyEntry(key);
        try {
            
            platformPasswordKeystore.setEntry(platformname, entry, new KeyStore.PasswordProtection(generatePassword(platformname)));
        } catch (KeyStoreException ex) {
            Logger.getLogger(KeyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void addPlatformname(String platformname){
        platformnamelist.add(platformname);
    }

    @Override
    protected String getTempKey(WorkRequest request) {
        String platformname=request.getRequest().getData().get(ConstantUtil.PLATFORM_NAME);
        SecretKey tempkey=null;
        KeyGenerator keygenerator;
        try {
            keygenerator = KeyGenerator.getInstance("AES");
            tempkey = keygenerator.generateKey();
            KeyStore.SecretKeyEntry entry=new KeyStore.SecretKeyEntry(tempkey);
            System.out.println("platformkeytemp:"+tempkey);
            platformkeystore.setEntry(platformname, entry, new KeyStore.PasswordProtection(generatePassword(platformname)));
            System.out.println(platformkeystore.getKey(platformname,generatePassword(platformname)));
            
        } catch (NoSuchAlgorithmException | KeyStoreException ex) {
            Logger.getLogger(KeyServerKDC.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(KeyServerKDC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Encryption.keyToString(tempkey);
    }

    public SecretKey getKey(String accountname){
        SecretKey key=null;
        try {
            key=(SecretKey)platformPasswordKeystore.getKey(accountname,generatePassword(accountname));
        } catch (    KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
            Logger.getLogger(KeyServer.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return key;
    }
    
    private char[] generatePassword(String identifier){
        byte[] passwordbyte=Hashing.getHashWithSalt(identifier, Hashing.HashingTechqniue.SSHA256, getsalt(identifier));
        String password=Hashing.bytetoBase64String(passwordbyte);
        return password.toCharArray();
    }
    
    private byte[] getsalt(String identifier){
        char[] chr=identifier.toCharArray();
        int key=0;
        for(int i=0;i<chr.length;i++){
            key=key+chr[i]*i*i;
        }
        key=key/701;
        return String.valueOf(key).getBytes();
        
    }
}
