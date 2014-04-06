/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tazdingo.platform;

import com.tazdingo.config.Config;
import com.tazdingo.http.IPlatform;
import java.util.Scanner;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author Cynthia
 */
public abstract class AbstractPlatformConfig {

    public static XMLConfiguration getPlatformConfig() {
        return Config.config("../conf/platform.xml");
    }

    /**
     *
     * @return
     */
    public IPlatform defaultPlatformConfiguration() {
        IPlatform platform = null;
        XMLConfiguration config = getPlatformConfig();
        String platformname = config.getString("platform.name");
        String defaultkeyserverurl = config.getString("platform.defaultkeyserverurl");
        String defaultkeyservername = config.getString("platform.defaultkeyservername");
        Scanner scanIn = new Scanner(System.in);
        String password = null;
        System.out.println("Enter platform adminpassword:");
        String adminpassword = scanIn.next();
        String key = config.getString("platform.key");
        if (key == null || key.isEmpty()) {
            try {
                System.out.println("Enter password:");
                password = scanIn.next();
                String encryptedpassword = Encryption.encrypt(Encryption.generateSecretKey(adminpassword), password);
                config.addProperty("platform.key", encryptedpassword);
                config.save();
            } catch (ConfigurationException ex) {
                Logger.getLogger(AbstractPlatformConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            password = Encryption.decrypt(Encryption.generateSecretKey(adminpassword), key);
        }

        if (!password.equals(ConstantUtil.WRONDKEY)) {
            platform = newPlatform(platformname, password, adminpassword, defaultkeyserverurl, defaultkeyservername);
            List<HierarchicalConfiguration> keyserver = config.configurationsAt("keyserver");
            for (HierarchicalConfiguration sub : keyserver) {
                String keyservername = sub.getString("name");
                String keyserverurl = sub.getString("url");
                String privilege = sub.getString("privilege");
                platform.addKeyServer(keyservername, keyserverurl, privilege);
            }
            List<HierarchicalConfiguration> service = config.configurationsAt("service");
            for (HierarchicalConfiguration sub : service) {
                String servicename = sub.getString("name");
                String serviceurl = sub.getString("url");
                String privilege = sub.getString("privilege");
                String keyservername = sub.getString("keyservername");
                if (keyservername == null || keyservername.isEmpty()) {
                    platform.addService(servicename, serviceurl, privilege);
                } else {
                    platform.addService(servicename, serviceurl, privilege, keyservername);
                }
            }
            List<HierarchicalConfiguration> device = config.configurationsAt("device");
            for (HierarchicalConfiguration sub : device) {
                String deviceid = sub.getString("id");
                String privilege = sub.getString("privilege");
                String keyservername = sub.getString("keyservername");
                if (keyservername == null || keyservername.isEmpty()) {
                    platform.addDevice(deviceid, privilege);
                } else {
                    platform.addDevice(deviceid, privilege, keyservername);
                }
            }

            List<HierarchicalConfiguration> user = config.configurationsAt("user");
            for (HierarchicalConfiguration sub : user) {
                String username = sub.getString("name");
                String privilege = sub.getString("privilege");
                String keyservername = sub.getString("keyservername");
                if (keyservername == null || keyservername.isEmpty()) {
                    platform.addUser(username, privilege);
                } else {
                    platform.addUser(username, privilege, keyservername);
                }
            }
            List<Object> blocklist = config.getList("blocked.bolockeddeviceid");
            for (Object deviceid : blocklist) {
                platform.blockDevice((String) deviceid);
            }
        }
        return platform;

    }

    protected abstract IPlatform newPlatform(String platformname, String password, String adminpassword, String defaultkeyserverurl, String defaultkeyservername);
}
