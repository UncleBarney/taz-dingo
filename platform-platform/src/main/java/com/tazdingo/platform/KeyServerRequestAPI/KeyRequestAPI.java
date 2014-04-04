package com.tazdingo.platform.KeyServerRequestAPI;

import java.util.HashMap;
import java.util.Map;
import com.tazdingo.core.Request;
import com.tazdingo.core.util.ConstantUtil;

/**
 *
 * @author Cynthia
 */
public class KeyRequestAPI {

    /**
     * Method to generate find key request to keyserver
     *
     * @param accounttype
     * @param accountname
     * @param sessionid
     * @return
     */
    public static Request findKeyRequest(String accounttype, String accountname, String sessionid) {
        if (accountname == null || accountname.isEmpty() || accounttype == null || accounttype.isEmpty()) {
            return null;
        }
        Request request = new Request();
        Map<String, String> data = new HashMap<>();
        data.put(ConstantUtil.ACCOUNTNAME, accountname);
        data.put(ConstantUtil.ACCOUNTTYPE, accounttype);
        data.put(ConstantUtil.USERSSIONID, sessionid);
        data.put(ConstantUtil.OPERATION, ConstantUtil.FIND);
        data.put(ConstantUtil.REQUEST_TYPE, ConstantUtil.PLATFORM_KEY_REQUEST);
        request.setData(data);
        return request;
    }
    
}
