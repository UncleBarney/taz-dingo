/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.neu.coe.platform.device.platform.device;

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
 * 
 */
public abstract class AbstractDeviceConfiguration {
     
     /*
     Method to configure the device
     */
     public IDevice defaultDeviceConfiguration(){
        IDevice device=null;
        XMLConfiguration config=Config.config("../conf/device.xml");
        String deviceid=config.getString("device.id");
        String platformurl=config.getString("device.platformurl");
        Scanner scanIn = new Scanner(System.in);
        String password=null; 
        String adminpassword=null;
        String key=config.getString("device.key");
        if(key==null ||key.isEmpty()){
            try {
                System.out.println("Enter device adminpassword:");
                adminpassword=scanIn.next();
                System.out.println("Enter local password:");
                password=scanIn.next();
                String encryptedpassword=Encryption.encrypt(Encryption.generateSecretKey(adminpassword), password);
                config.addProperty("device.key", encryptedpassword);
                config.save();
            } catch (ConfigurationException ex) {
                Logger.getLogger(AbstractDeviceConfiguration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else{
            password=key;
        }
        if(!password.equals(ConstantUtil.WRONDKEY)){
              if(adminpassword!=null)
              device=newDevice(deviceid, password, adminpassword, platformurl);
              else device=newDevice(deviceid, password, platformurl);
              if(device.getDeviceticket()==null){
                  if(adminpassword==null){
                      System.out.println("Enter device adminpassword:");
                      adminpassword=scanIn.next();
                  }
                  String response= device.deviceLogin(adminpassword);
                  if(response.equals(ConstantUtil.SUCCESS_LOGIN)){
                  device.deviceAuthorization();
                  }
              }
         }
        
       return device;
         
    }

    protected abstract IDevice newDevice(String deviceid, String password, String adminpassword, String platformurl);

    protected abstract IDevice newDevice(String deviceid, String password, String platformurl);
    
    
    
    
    
    
}
