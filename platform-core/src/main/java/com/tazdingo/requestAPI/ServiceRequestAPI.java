
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tazdingo.requestAPI;

import com.tazdingo.core.Request;
import com.tazdingo.core.util.ConstantUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author apple
 */
public class ServiceRequestAPI {
    
    /**
         * Method to generated service request
         * @param username
         * @param deviceid
         * @param servicename
         * @return 
         */
	public static Request connectServiceRequest(String username,String deviceid,String servicename){
		
		if(   servicename==null || servicename.isEmpty() ||
                        username==null || username.isEmpty() ||
                        deviceid==null||deviceid.isEmpty()){
            return null;
                }
		
		Request request=new Request();
                Map<String,String> data=new HashMap<>();
                data.put(ConstantUtil.DEVICEID, deviceid);
                data.put(ConstantUtil.USERNAME, username);
                data.put(ConstantUtil.SERVICE_NAME, servicename);
                data.put(ConstantUtil.OPERATION, ConstantUtil.CONNECT);
               // data.put(ConstantUtil.USERTICKET, serviceTicket);
                data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.USER_CONNECT_SERVICE_REQUEST);
                request.setData(data);
                return request;
	}
        /**
         * Method to generate get key request
         * @param servicename
         * @return 
         */
	
        public static Request getServiceKey(String servicename){
              if(servicename==null|| servicename.isEmpty())
                  return null;
              Request request=new Request();
              Map<String,String> data=new HashMap<>();
               data.put(ConstantUtil.SERVICE_NAME, servicename);
                data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.SERVICE_KEY_REQUEST);
               request.setData(data);
                return request;
            
        }
        /**
         * Method to generate service request with additional information/parameters
         * @param username
         * @param deviceid
         * @param servicename
         * @param additionalInformation
         * @return 
         */

    public static Request connectServiceRequest(String username, String deviceid, String servicename, Map<String, String> additionalInformation) {
        if(   servicename==null || servicename.isEmpty() ||
                        username==null || username.isEmpty() ||
                        deviceid==null||deviceid.isEmpty()){
            return null;
                }
		
		Request request=new Request();
                Map<String,String> data=new HashMap<>();
                data.put(ConstantUtil.DEVICEID, deviceid);
                data.put(ConstantUtil.USERNAME, username);
                data.put(ConstantUtil.SERVICE_NAME, servicename);
                data.put(ConstantUtil.OPERATION, ConstantUtil.CONNECT);
                if(additionalInformation!=null){
                Iterator ite=additionalInformation.entrySet().iterator();
                while(ite.hasNext()){
                    Map.Entry<String,String>entry=(Map.Entry)ite.next();
                    data.put(entry.getKey(), entry.getValue());
                }
                }
               // data.put(ConstantUtil.USERTICKET, serviceTicket);
                data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.USER_CONNECT_SERVICE_REQUEST);
                request.setData(data);
                return request;
    }
}
