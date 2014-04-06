/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.keyserver;

import com.tazdingo.config.Config;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import java.io.File;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author Cynthia
 */
public abstract class AbstractKeyServerConfig {
    
     public static XMLConfiguration getKeyServerConfig(){
        return Config.config("../conf/keyserver.xml");
    }

    public IKeyServer defaultKeyServerConfiguration() {
        KeyServer keyserver = null;
        XMLConfiguration config = getKeyServerConfig();
        String keyservername = config.getString("keyserver.name");
        String platformurl = config.getString("keyserver.platformurl");
        String defaultplatformname = config.getString("keyserver.defaultplatformname");
        String keystorefilespath = config.getString("keyserver.keystorefilesdirectory");
        String key = config.getString("keyserver.key");

        if ((new File(keystorefilespath)).isDirectory()) {
            Scanner scanIn = new Scanner(System.in);
            String password = null;
            System.out.println("Enter keyserver adminpassword:");
            String adminpassword = scanIn.next();
            String platformpassword = null;
            if (key == null || key.isEmpty()) {
                try {
                    System.out.println("Enter keyserver password:");
                    password = scanIn.next();
                    System.out.println("Enter default platform password:");
                    platformpassword = scanIn.next();
                    String encryptedpassword = Encryption.encrypt(Encryption.generateSecretKey(adminpassword), password);
                    // System.out.println(Encryption.decrypt(Encryption.generateSecretKey(adminpassword), encryptedpassword));
                    config.addProperty("keyserver.key", encryptedpassword);
                    config.save();
                } catch (ConfigurationException ex) {
                    Logger.getLogger(AbstractKeyServerConfig.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                String encryptedpassword = key;
                password = Encryption.decrypt(Encryption.generateSecretKey(adminpassword), encryptedpassword);
            }
            if (!password.equals(ConstantUtil.WRONDKEY)) {
                keyserver = newKeyServer(keyservername, password, adminpassword, platformurl, keystorefilespath, defaultplatformname);
                if (platformpassword != null) {
                    keyserver.addPlatform(defaultplatformname, platformpassword, "");
                    keyserver.saveKeyStore(adminpassword);
                    System.out.println("How many user do you want to add?");
                    int num = scanIn.nextInt();
                    for (int i = 0; i < num; i++) {
                        System.out.println("Enter username for " + (i + 1) + "th user:");
                        String username = scanIn.next();
                        System.out.println("Enter password for " + (i + 1) + "th user:");
                        String pass = scanIn.next();
                        keyserver.addOrUpdateUser(username, pass);
                    }
                    System.out.println("How many device do you want to add?");
                    num = scanIn.nextInt();
                    for (int i = 0; i < num; i++) {
                        System.out.println("Enter deviceid for " + (i + 1) + "th device:");
                        String deviceid = scanIn.next();
                        System.out.println("Enter password for " + (i + 1) + "th device:");
                        String pass = scanIn.next();
                        keyserver.addOrUpdataDevice(deviceid, pass);
                    }
                    System.out.println("How many service do you want to add?");
                    num = scanIn.nextInt();
                    for (int i = 0; i < num; i++) {
                        System.out.println("Enter servicename for " + (i + 1) + "th service:");
                        String servicename = scanIn.next();
                        System.out.println("Enter password for " + (i + 1) + "th service:");
                        String pass = scanIn.next();
                        keyserver.addOrUpdateService(servicename, pass);
                    }
                    keyserver.saveKeyStore(adminpassword);
                }
                List<HierarchicalConfiguration> platform = config.configurationsAt("platform");
                for (HierarchicalConfiguration sub : platform) {
                    String platformname = sub.getString("name");
                    String privilege = sub.getString("privilege");
                    keyserver.addPlatformPrivilege(platformname, privilege);
                }
            }
        } else {
            System.out.println("Invalid directory for keystore!!");
        }

        return keyserver;

    }

    protected abstract KeyServer newKeyServer(String keyservername, String password, String adminpassword, String platformurl, String keystorefilespath, String defaultplatformname);

}
