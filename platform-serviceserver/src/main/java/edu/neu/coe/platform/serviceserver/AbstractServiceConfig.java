/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.neu.coe.platform.serviceserver;

import com.tazdingo.config.Config;
import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;


/**
 *
 * @author Cynthia
 */
public abstract class AbstractServiceConfig {
    
    
    public IServiceServer defaultServiceServerConfiguration(){
        ServiceServer serviceserver=null;
        XMLConfiguration config=Config.config("../conf/service.xml");
        String servicename=config.getString("service.name");
        String platformurl=config.getString("service.platformurl");
        Scanner scanIn = new Scanner(System.in);
        String password=null; 
        System.out.println("Enter service adminpassword:");
        String adminpassword=scanIn.next();
        String key=config.getString("service.key");
        if(key==null ||key.isEmpty()){
            try {
                System.out.println("Enter service password:");
                password=scanIn.next();
                String encryptedpassword=Encryption.encrypt(Encryption.generateSecretKey(adminpassword), password);
                config.addProperty("service.key", encryptedpassword);
                config.save();
            } catch (ConfigurationException ex) {
                Logger.getLogger(AbstractServiceConfig.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            password = Encryption.decrypt(Encryption.generateSecretKey(adminpassword), key);
        }
        if(!password.equals(ConstantUtil.WRONDKEY)){
             serviceserver=newServiceServer(servicename, password, adminpassword, platformurl);
         }
        
             return serviceserver;
        
    }

    protected abstract ServiceServer newServiceServer(String servicename, String password, String adminpassword, String platformurl);
    
}
