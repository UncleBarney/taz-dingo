/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.keyserver.service;

import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import com.tazdingo.core.util.Hashing;
import com.tazdingo.kerberos.TGS;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import edu.neu.coe.platform.keyserver.KeyServer;

/**
 *
 * @author Cynthia
 */
public class KeyServerTGS extends TGS{
    
    private final SecretKey masterkey;
    private final Map<String,String> platformPrivilegelist=new HashMap<>();
 //   private SecretKey platformmasterkey;
    private KeyStore platformkeystore;
    
    public KeyServerTGS(SecretKey masterkey){
        this.masterkey=masterkey;
    }
    
   
    public void setPlatformkeystore(KeyStore platformkeystore) {
        this.platformkeystore = platformkeystore;
    }
    
    

    @Override
    protected SecretKey getMasterKey() {
       return masterkey;
    }

    @Override
    protected SecretKey getShortTermKey(WorkRequest request) {
        String platformname=request.getRequest().getData().get(ConstantUtil.PLATFORM_NAME);
        return getKey(platformname);
    }

    @Override
    protected String getIdentifier(Map<String, String> data, String type) {
        return data.get(ConstantUtil.PLATFORM_NAME);
    }

    @Override
    protected String getPrivilege(String identifier, String type) {
       return platformPrivilegelist.get(identifier);
    }

    @Override
    protected Map<String, String> addAdditionalData(Map<String, String> data, WorkRequest request) {
        //data.put(ConstantUtil.STEPID, Encryption.keyToString(platformmasterkey));
        return data;
    }

    @Override
    protected String getStepid(WorkRequest request) {
        String platformname=request.getRequest().getData().get(ConstantUtil.PLATFORM_NAME);
        return Encryption.keyToString(getKey(platformname));
    }
    
    public void addPlatform(String platformname,String Privilege){
        platformPrivilegelist.put(platformname, Privilege);
    }
    
    public SecretKey getKey(String accountname){
        SecretKey key=null;
        try {
            key=(SecretKey)platformkeystore.getKey(accountname,generatePassword(accountname));
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
