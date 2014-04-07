/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.keyserver;

import com.tazdingo.core.IService;
import com.tazdingo.core.Request;
import com.tazdingo.core.Response;
import com.tazdingo.core.ServiceWorkRequest;
import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import com.tazdingo.core.util.Hashing;
import com.tazdingo.ticket.TGT;
import static edu.neu.coe.platform.keyserver.AbstractKeyServerConfig.getKeyServerConfig;
import edu.neu.coe.platform.keyserver.service.KeyServerKDC;
import edu.neu.coe.platform.keyserver.service.KeyServerTGS;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import edu.neu.coe.platform.service.serviceserver.GeneralServer.Server;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;


/**
 *
 * @author Cynthia
 */
public class KeyServer extends Server implements IKeyServer{
    
    private final IService kdc;
    private final IService tgs;
    private final SecretKey masterKey;
    private KeyStore userKeyStore;
    private KeyStore deviceKeyStore;
    private KeyStore servicepasswordKeyStore;
    private KeyStore platformKeyStore;
 //  private SecretKey platformmasterkey;
    private KeyStore serviceKeyStore;
    private final String keystorefilespath;
    private final String defaultplatformname;
    
    public KeyServer(String keyservername,String keyserverpassword,String adminpassword,String defaultplatformurl,String keystorefilespath,String defaultplatformname){
        super(keyservername, keyserverpassword, adminpassword,defaultplatformurl);
        this.defaultplatformname=defaultplatformname;
        addPlatformPrivilege(defaultplatformname, "1");
        masterKey=Encryption.generateSecretKey(keyserverpassword);
        kdc=new KeyServerKDC(masterKey,keystorefilespath);
        tgs=new KeyServerTGS(masterKey);
        this.keystorefilespath=keystorefilespath+"/";
        File file=new File(keystorefilespath);
        if(file.isDirectory())
            if(file.list().length>0)
                loadKeyStore(adminpassword);
            else init(adminpassword);
        ((KeyServerKDC)kdc).setPlatformkeystore(platformKeyStore);
        ((KeyServerTGS)tgs).setPlatformkeystore(platformKeyStore);
    }

   
    
    private void init(String adminpassword){
        try {        
            SecretKey key=Encryption.generateSecretKey(adminpassword);
            char[] p=Encryption.decrypt(key,constructor.getServerpassword()).toCharArray();
            deviceKeyStore= KeyStore.getInstance("JCEKS");
            deviceKeyStore.load(null, p);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.DEVICE_KEYSTORE);
            deviceKeyStore.store(fos, p);
            userKeyStore= KeyStore.getInstance("JCEKS");
            userKeyStore.load(null, p);
             fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.USER_KEYSTORE);
             userKeyStore.store(fos, p);
             serviceKeyStore= KeyStore.getInstance("JCEKS");
             serviceKeyStore.load(null, p);
             fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.SERVICE_KEYSTORE);
            serviceKeyStore.store(fos, p);
            servicepasswordKeyStore= KeyStore.getInstance("JCEKS");
            servicepasswordKeyStore.load(null, p);
            fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.SERVICEPASSWORD_KEYSTORE);
            servicepasswordKeyStore.store(fos, p);
            platformKeyStore= KeyStore.getInstance("JCEKS");
            platformKeyStore.load(null, p);
            fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.PLATFORM_KEYSTORE);
            platformKeyStore.store(fos, p);
            ((KeyServerKDC)kdc).init(Encryption.decrypt(key,constructor.getServerpassword()));
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(KeyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
  
    private void loadKeyStore(String adminpassword){
         try {        
            SecretKey key=Encryption.generateSecretKey(adminpassword);
            char[] p=Encryption.decrypt(key,constructor.getServerpassword()).toCharArray();
            deviceKeyStore= KeyStore.getInstance("JCEKS");
            java.io.FileInputStream fis = new java.io.FileInputStream(keystorefilespath+ConstantUtil.DEVICE_KEYSTORE);
            deviceKeyStore.load(fis, p);
            userKeyStore= KeyStore.getInstance("JCEKS");
            fis = new java.io.FileInputStream(keystorefilespath+ConstantUtil.USER_KEYSTORE);
            userKeyStore.load(fis, p);
            serviceKeyStore= KeyStore.getInstance("JCEKS");
            fis = new java.io.FileInputStream(keystorefilespath+ConstantUtil.SERVICE_KEYSTORE);
            serviceKeyStore.load(fis, p);
            servicepasswordKeyStore= KeyStore.getInstance("JCEKS");
            fis = new java.io.FileInputStream(keystorefilespath+ConstantUtil.SERVICEPASSWORD_KEYSTORE);
            servicepasswordKeyStore.load(fis, p);
            platformKeyStore= KeyStore.getInstance("JCEKS");
            fis = new java.io.FileInputStream(keystorefilespath+ConstantUtil.PLATFORM_KEYSTORE);
            platformKeyStore.load(fis, p);
            ((KeyServerKDC)kdc).load(Encryption.decrypt(key,constructor.getServerpassword()));
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(KeyServer.class.getName()).log(Level.SEVERE, null, ex);
        } 
         
    }

    /**
     * Method to execute request and return response
     * @param usersessionid
     * @return 
     */
    @Override
    public Response excuteRequest(String usersessionid) {
        Response response=new Response();
        WorkRequest request=workqueue.pullWorkRequest(usersessionid);
        Map<String,String> data=request.getRequest().getData();
        String operation=data.get(ConstantUtil.OPERATION);
        String accountname=data.get(ConstantUtil.ACCOUNTNAME);
        String accounttype=data.get(ConstantUtil.ACCOUNTTYPE);
        String platformname=data.get(ConstantUtil.PLATFORM_NAME);
        SecretKey platformmasterkey=getKey(ConstantUtil.PLATFORM, platformname);
         if(platformmasterkey!=null){
        if(operation.equals(ConstantUtil.FIND)){
            SecretKey key=getKey(accounttype, accountname);
            String keystring=null;
            if(key!=null)
            keystring=Encryption.keyToString(key);
            response.getData().put(ConstantUtil.KEY, keystring);
            System.out.println("encrypted data with key:"+platformmasterkey);
            response.setData(Encryption.encrypt(platformmasterkey,response.getData()));
            response.getData().put(ConstantUtil.ERROR, ConstantUtil.NO_ERROR);
            System.out.println("KeyServer Send key back to platform:"+key);
            }
           
        }else response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDPLATFORM);
        return response;
       
    }

    /**
     * Method to handle request without ticket
     * @param request
     * @return 
     */
    @Override
    protected Response handleNOTicketRequest(Request request) {
        Response response=new Response();
        String type=request.getData().get(ConstantUtil.REQUEST_TYPE);
        if(type!=null){
            if(type.equals(ConstantUtil.PLATFORM_AUTHENTICATE_REQUEST)){
                int i=0;
                while(sessionid==null&&i<5){
                    getNewTicket();
                    i++;
                }
                WorkRequest workrequest=new ServiceWorkRequest(sessionid, request);
                workqueue.addWorkRequest(sessionid, workrequest);
                return kdc.takeRequest(workqueue.pullWorkRequest(sessionid));
            }else{
                String tgtstring=request.getData().get(ConstantUtil.TGT);
                System.out.println("decrypted ticket with key:"+masterKey);
                String decryptedtgt=Encryption.decrypt(masterKey, tgtstring);
                if(decryptedtgt!=null && !decryptedtgt.equals(ConstantUtil.WRONDKEY)){
                 TGT tgt=new TGT();
                 tgt.setTicket(decryptedtgt);
                String platformname=tgt.getAuthenticator();
//                SecretKey key=Encryption.generateSecretKey(stepid);
                request.getData().remove(ConstantUtil.TGT);
                System.out.println("decrypted data with key"+getKey(ConstantUtil.PLATFORM, platformname));
                SecretKey key=getKey(ConstantUtil.PLATFORM, platformname);
                if(key!=null){
                    Map<String,String> data=Encryption.decrypt(key, request.getData());
                    if(!data.containsKey(ConstantUtil.ERROR)){
                request.setData(data);
                request.getData().put(ConstantUtil.TICKET,decryptedtgt);
                WorkRequest workrequest=new ServiceWorkRequest(sessionid, request);
                workqueue.addWorkRequest(sessionid, workrequest);
                return tgs.takeRequest(workqueue.pullWorkRequest(sessionid));
                    }else response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDPLATFORM);
                }else response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDPLATFORM);
                }else {
                    response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALID_KDC_TICKET);
                }
        }
            
        }
        response.getData().put(ConstantUtil.ERROR, ConstantUtil.DEFAULT_ERROR);
       return response;
    }
/**
 * method to get the keyserver masterkey
 * @return 
 */
    @Override
    protected SecretKey getServiceKey() {
        return this.masterKey;
    }
/**
 * Method to get account identification i.e. platformname
 * @param data
 * @return 
 */
    @Override
    protected String getIdentifier(Map<String, String> data) {
        String identifier=ConstantUtil.DEFAULT_IDENTIFIER;
       String type=data.get(ConstantUtil.REQUEST_TYPE);
        switch (type) {
            case ConstantUtil.PLATFORM_KEY_REQUEST:
                identifier=data.get(ConstantUtil.PLATFORM_NAME);
                break;
            case ConstantUtil.SERVICE_KEY_REQUEST:
                identifier=data.get(ConstantUtil.SERVICE_NAME);
                break;
        }
         
       return identifier; 
    }
/**
 * Method to get the service password i.e. defaultplatform password
 * @return 
 */
    @Override
    protected SecretKey getServicePassword() {
       return getKey(ConstantUtil.PLATFORM, defaultplatformname);
    }
    
    private KeyStore findKeyStore(String type){
        if(type.equals(ConstantUtil.DEVICE))
            return deviceKeyStore;
        if(type.equals(ConstantUtil.SERVICE))
            return serviceKeyStore;
        if(type.equals(ConstantUtil.USER))
            return userKeyStore;
        if(type.equals(ConstantUtil.SERVICEPASSWORD))
            return servicepasswordKeyStore;
        if(type.equals(ConstantUtil.PLATFORM))
            return platformKeyStore;
        return null;
    }
    /**
     * Method to add or update user
     * @param username
     * @param password
     * @return 
     */
    @Override
    public String addOrUpdateUser(String username,String password){
        return addOrUpdateKey(ConstantUtil.USER, username, password);
    }
    /**
     * Method to add or update device
     * @param deviceid
     * @param password
     * @return 
     */
    @Override
    public String addOrUpdataDevice(String deviceid,String password){
        return addOrUpdateKey(ConstantUtil.DEVICE, deviceid, password);
    }
    /**
     * Method to add or update service
     * @param servicename
     * @param password
     * @return 
     */
    @Override
    public String addOrUpdateService(String servicename,String password){
        addOrUpdateServiceKey(servicename);
        return addOrUpdateKey(ConstantUtil.SERVICEPASSWORD, servicename, password);
    }
    /**
     * Method to add or update service
     * @param servicename
     * @return 
     */
    @Override
    public String addOrUpdateServiceKey(String servicename){
         SecureRandom random = new SecureRandom();
       return addOrUpdateKey(ConstantUtil.SERVICE,servicename,new BigInteger(130, random).toString(32));
    }
    /**
     * Method to add or update any key
     * @param accounttype
     * @param accountname
     * @param password
     * @return 
     */
    public String addOrUpdateKey(String accounttype,String accountname,String password){
        String statu=ConstantUtil.DEFAULT_ERROR;
        SecretKey key=Encryption.generateSecretKey(password);
        KeyStore.SecretKeyEntry entry=new KeyStore.SecretKeyEntry(key);
        try {
            
            findKeyStore(accounttype).setEntry(accountname, entry, new KeyStore.PasswordProtection(generatePassword(accountname)));
            statu=ConstantUtil.NO_ERROR;
        } catch (KeyStoreException ex) {
            Logger.getLogger(KeyServer.class.getName()).log(Level.SEVERE, null, ex);
            return ConstantUtil.ERROR;
        }
        
        return statu;
    }
    /**
     * Method to delete user
     * @param username
     * @return 
     */
    @Override
    public String deleteUser(String username){
        return deleteKey(ConstantUtil.USER, username);
    }
    /**
     * Method to delete device
     * @param deviceid
     * @return 
     */
    @Override
    public String deleteDevice(String deviceid){
        return deleteKey(ConstantUtil.DEVICE, deviceid);
    }
    /**
     * Method to delete service
     * @param servicename
     * @return 
     */
    @Override
    public String deleteService(String servicename){
        deleteKey(ConstantUtil.SERVICE,servicename);
        return deleteKey(ConstantUtil.SERVICEPASSWORD, servicename);
    }
    /**
     * Method to delete any key
     * @param accounttype
     * @param accountname
     * @return 
     */
    public String deleteKey(String accounttype,String accountname){
        String statu=ConstantUtil.DEFAULT_ERROR;
        try {
            findKeyStore(accounttype).deleteEntry(accountname);
            statu=ConstantUtil.NO_ERROR;
        } catch (KeyStoreException ex) {
            Logger.getLogger(KeyServer.class.getName()).log(Level.SEVERE, null, ex);
            return ConstantUtil.ERROR;
        }
        return statu;
    }
    
    /**
     * Method to get any key
     * @param accounttype
     * @param accountname
     * @return 
     */
    public SecretKey getKey(String accounttype,String accountname){
        SecretKey key=null;
        try {
            key=(SecretKey)findKeyStore(accounttype).getKey(accountname,generatePassword(accountname));
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
    /**
     * Method to add platform
     * @param platformname
     * @param platformpassword
     * @param privilege 
     */
    @Override
    public void addPlatform(String platformname,String platformpassword,String privilege){
        ((KeyServerKDC)kdc).addPlatform(platformname, platformpassword);
        addOrUpdateKey(ConstantUtil.PLATFORM, platformname, platformpassword);
        addPlatformPrivilege(platformname, privilege);
        //this.platformmasterkey=Encryption.generateSecretKey(platformpassword);
    }
    /**
     * Method to add platform privilege
     * @param platformname
     * @param privilege 
     */
    public void addPlatformPrivilege(String platformname,String privilege){
            ((KeyServerKDC)kdc).addPlatformname(platformname);
            ((KeyServerTGS)tgs).addPlatform(platformname, privilege+ConstantUtil.DELIMITER+ConstantUtil.CONNECT);
           boolean change = false;
        boolean exist = false;
        XMLConfiguration config=getKeyServerConfig();
         List<HierarchicalConfiguration> platform = config.configurationsAt("platform");
            for(HierarchicalConfiguration sub : platform){
                 String name = sub.getString("name");
                 String oldprivilege = sub.getString("privilege");
                 if(name.equals(platformname)){
                     exist=true;
                     if(!oldprivilege.equals(privilege)){
                         sub.setProperty("privilege", privilege);
                         change=true;
                     }
                     break;
                 }
            }
       
        if (!exist) {
            config.addProperty("platform(-1).name", platformname);
            config.addProperty("platform.privilege", privilege);
            change = true;
        }
        if (change) {
            try {
                config.save();
            } catch (ConfigurationException ex) {
                Logger.getLogger(KeyServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Method to save keystore
     * @param adminpassword 
     */
    @Override
    public void saveKeyStore(String adminpassword){
         try {        
            SecretKey key=Encryption.generateSecretKey(adminpassword);
            char[] p=Encryption.decrypt(key,constructor.getServerpassword()).toCharArray();
            java.io.FileOutputStream fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.DEVICE_KEYSTORE);
            deviceKeyStore.store(fos, p);
            fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.USER_KEYSTORE);
             userKeyStore.store(fos, p);
             fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.SERVICE_KEYSTORE);
            serviceKeyStore.store(fos, p);
           fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.SERVICEPASSWORD_KEYSTORE);
            servicepasswordKeyStore.store(fos, p);
             fos = new java.io.FileOutputStream(keystorefilespath+ConstantUtil.PLATFORM_KEYSTORE);
            platformKeyStore.store(fos, p);
             ((KeyServerKDC)kdc).save(Encryption.decrypt(key,constructor.getServerpassword()));
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            Logger.getLogger(KeyServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("Enter adminpassword");
        Scanner scanIn=new Scanner(System.in);
        String adminpassword=scanIn.next();
        saveKeyStore(adminpassword);
        super.finalize(); //To change body of generated methods, choose Tools | Templates.
    }
    

/**
 * Method to login keyserver to platform
 * @return 
 */
    @Override
    public String serverLogin() {
        return super.serverLogin(ConstantUtil.KEYSERVER);
    }
    /**
     * Method to get platform password
     * @param platformname
     * @return 
     */
    public SecretKey getPlatformPassword(String platformname){
        return ((KeyServerKDC)kdc).getKey(platformname);
    }
    
}
