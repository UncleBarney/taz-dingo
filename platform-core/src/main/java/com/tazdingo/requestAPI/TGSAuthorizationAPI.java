/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tazdingo.requestAPI;

import com.tazdingo.core.Request;
import com.tazdingo.core.util.ConstantUtil;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Cynthia
 */
public class TGSAuthorizationAPI {
    
     /**
     * Method to generate user authorization request
     * @param username
     * @param deviceid
     * @return 
     */
    public static Request createUserAuthorizationRequest(String username,String deviceid){
        if(//userticket==null || userticket.isEmpty() || 
                username==null || username.isEmpty() ||
                deviceid==null||deviceid.isEmpty()){
            return null;
        }
        Request request=new Request();
        Map<String,String> data=new HashMap<>();
        data.put(ConstantUtil.DEVICEID, deviceid);
        data.put(ConstantUtil.USERNAME, username);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.USER_AUTHERIZATION_REQUEST);
        request.setData(data);
        return request;
        
    }
    /**
     * Method to generate service authorization request
     * @param servicename
     * @return 
     */
   public static Request createServerAuthorizationRequest(String servicename){
        if(servicename==null || servicename.isEmpty() ){
            return null;
        }
        Request request=new Request();
        Map<String,String> data=new HashMap<>();
        data.put(ConstantUtil.SERVICE_NAME, servicename);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.SERVICE_AUTHERIZATION_REQUEST);
        request.setData(data);
        return request;
        
    }
   /**
    * Method to generate key server authorization request
    * @param keyservername
    * @return 
    */
  public static Request createKeyServerAuthorizationRequest(String keyservername){
        if(keyservername==null || keyservername.isEmpty() ){
            return null;
        }
        Request request=new Request();
        Map<String,String> data=new HashMap<>();
        data.put(ConstantUtil.SERVICE_NAME, keyservername);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.KEYSERVER_AUTHENTICATE_REQUEST);
        request.setData(data);
        return request;
        
    }
   /**
    * Method to generate device authorization request
    * @param deviceid
    * @return 
    */ 
   public static Request createDeviceAuthorizationRequest(String deviceid){
        if(deviceid==null || deviceid.isEmpty() ){
            return null;
        }
        Request request=new Request();
        Map<String,String> data=new HashMap<>();
        data.put(ConstantUtil.DEVICEID, deviceid);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.DEVICE_AUTHERIZATION_REQUEST);
        request.setData(data);
        return request;
        
    }
    
   /**
     * Method to generate platform authorization request
     *
     * @param platformname
     * @return
     */
    public static Request createPlatformAuthorizationRequest(String platformname) {
        if (platformname == null || platformname.isEmpty()) {
            return null;
        }
        Request request = new Request();
        Map<String, String> data = new HashMap<>();
        data.put(ConstantUtil.PLATFORM_NAME, platformname);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.PLATFORM_AUTHORIZATION_REQUEST);
        request.setData(data);
        return request;
    }
   
   
   
    
}
