/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.neu.coe.platform.service.platform.platformRequestAPI;

import edu.neu.coe.platform.service.util.ConstantUtil;
import java.util.HashMap;
import java.util.Map;
import edu.neu.coe.platform.service.platform.Request;

/**
 *
 * @author apple
 */
public class ServiceRequestAPI {
	
	
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
               // data.put(ConstantUtil.USERTICKET, serviceTicket);
                data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.USER_CONNECT_SERVICE_REQUEST);
                request.setData(data);
                return request;
	}
	
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
	
	
    
}
