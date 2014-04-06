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
public class KDCAuthenticationAPI {
    
        /**
         * Method to generate device authentication request
         * @param deviceId
         * @return 
         */
	public static Request createDeviceAuthenticationRequest(String deviceId){
		if (deviceId == null || deviceId.isEmpty()){
			return null;
		}
		Request request=new Request();
                Map<String,String> data=new HashMap<>();
                data.put(ConstantUtil.DEVICEID, deviceId);
                data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.DEVICE_AUTHENTICATE_REQUEST);
                request.setData(data);
		return request;
	}
        
        /**
         * Method to generate user authentication request
         * @param deviceId
         * @param username
         * @param deviceTicket
         * @return 
         */
        
        public static Request createUserAuthenticationRequest(String deviceId, String username, String deviceTicket){
                if(username ==null ||username.isEmpty() 
                       // || deviceTicket == null ||deviceTicket.isEmpty() 
                        || deviceId == null || deviceId.isEmpty()){
                    return null;
                }
		Request request=new Request();
                 Map<String,String> data=new HashMap<>();
                 data.put(ConstantUtil.USERNAME, username);
                 data.put(ConstantUtil.DEVICEID, deviceId);
                 data.put(ConstantUtil.DEVICE_TICKET, deviceTicket);
                 data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.USER_AUTHENTICATE_REQUEST);
                 request.setData(data);
		return request;
	}
        /**
         * Method to generate service authentication request
         * @param servername
         * @param type
         * @return 
         */
        
        public static Request createServerAuthenticationRequest(String servername,String type){
		if (servername == null || servername.isEmpty()){
			return null;
		}
		Request request=new Request();
                Map<String,String> data=new HashMap<>();
            switch (type) {
                case ConstantUtil.SERVICE_SERVER:
                    data.put(ConstantUtil.SERVICE_NAME, servername);
                    data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.SERVICE_AUTHENTICATE_REQUEST);
                    break;
                case ConstantUtil.KEYSERVER:
                    data.put(ConstantUtil.KEYSERVER_NAME, servername);
                    data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.KEYSERVER_AUTHENTICATE_REQUEST);
                    break;
            }
                request.setData(data);
		return request;
	}
        /**
     * Method to generate platform authentication request
     *
     * @param platformname
     * @return
     */
    public static Request createPlatformAuthenticationRequest(String platformname) {
        if (platformname == null || platformname.isEmpty()) {
            return null;
        }
        Request request = new Request();
        Map<String, String> data = new HashMap<>();
        data.put(ConstantUtil.PLATFORM_NAME, platformname);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.PLATFORM_AUTHENTICATE_REQUEST);
        request.setData(data);
        return request;
    }
}
