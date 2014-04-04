package com.tazdingo.platform.platformRequestAPI;

import com.tazdingo.core.util.ConstantUtil;
import java.util.HashMap;
import java.util.Map;
import com.tazdingo.core.Request;

/**
 *
 * @author Cynthia
 */
public class KDCAuthenticationAPI {

    public static Request createDeviceAuthenticationRequest(String deviceId) {
        if (deviceId == null || deviceId.isEmpty()) {
            return null;
        }
        Request request = new Request();
        Map<String, String> data = new HashMap<>();
        data.put(ConstantUtil.DEVICEID, deviceId);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.DEVICE_AUTHENTICATE_REQUEST);
        request.setData(data);
        return request;
    }

    public static Request createUserAuthenticationRequest(String deviceId, String username, String deviceTicket) {
        if (username == null || username.isEmpty()
                // || deviceTicket == null ||deviceTicket.isEmpty() 
                || deviceId == null || deviceId.isEmpty()) {
            return null;
        }
        Request request = new Request();
        Map<String, String> data = new HashMap<>();
        data.put(ConstantUtil.USERNAME, username);
        data.put(ConstantUtil.DEVICEID, deviceId);
        data.put(ConstantUtil.DEVICE_TICKET, deviceTicket);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.USER_AUTHENTICATE_REQUEST);
        request.setData(data);
        return request;
    }

    public static Request createServerAuthenticationRequest(String servername, String type) {
        if (servername == null || servername.isEmpty()) {
            return null;
        }
        Request request = new Request();
        Map<String, String> data = new HashMap<>();
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
    
}
