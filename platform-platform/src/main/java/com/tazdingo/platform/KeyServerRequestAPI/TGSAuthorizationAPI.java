package com.tazdingo.platform.KeyServerRequestAPI;

import java.util.HashMap;
import java.util.Map;
import com.tazdingo.core.Request;
import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class TGSAuthorizationAPI {

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
