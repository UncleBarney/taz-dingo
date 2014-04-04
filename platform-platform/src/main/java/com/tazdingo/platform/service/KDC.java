package com.tazdingo.platform.service;

import com.tazdingo.core.Service;
import com.tazdingo.core.util.ConstantUtil;
import java.util.Map;
import javax.crypto.SecretKey;
import com.tazdingo.core.Response;
import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.Encryption;

/**
 *
 * @author Cynthia
 */
public abstract class KDC extends Service {

    /**
     * Method to execute request and return response
     *
     * @param sessionid
     * @return
     */
    @Override
    public Response excuteRequest(String sessionid) {
        System.out.println("KDC received request");

        Response response = new Response();
        Map<String, String> responsedata = response.getData();
        WorkRequest request = super.workqueue.pullWorkRequest(sessionid);
        Map<String, String> data = request.getRequest().getData();
        String type = data.get(ConstantUtil.REQUEST_TYPE);
        String identifier = getIdentifier(data, type);
        System.out.println(identifier);
        String check = checkIdentifier(identifier, type);
        String stepid = null;
        if (check.equals(ConstantUtil.NO_ERROR)) {
            SecretKey key = getKey(identifier, type, sessionid);
            if (key != null) {
                String ticket = generateKDCTicket(identifier, type, sessionid);
                responsedata.put(ConstantUtil.TICKET, ticket);
                responsedata.put(ConstantUtil.SESSIONID, sessionid);
                stepid = getTempKey(request);
                responsedata.put(ConstantUtil.STEPID, stepid);

                System.out.println("encrypted data with key:" + key);
                responsedata = Encryption.encrypt(key, responsedata);
                System.out.println("authenticate successfully");
            } else {
                responsedata.put(ConstantUtil.ERROR, ConstantUtil.PASSWORDRESET);
            }
        }
        responsedata.put(ConstantUtil.ERROR, check);
        response.setData(responsedata);
        System.out.println(check);
        return response;
    }

    /**
     * Method to get temporary key/stepid
     *
     * @param request
     * @return
     */
    protected abstract String getTempKey(WorkRequest request);

    /**
     * Method to get account identifier e.g. username,deviceid,
     * servicename,keyservername,platformname
     *
     * @param data
     * @param type
     * @return
     */
    protected abstract String getIdentifier(Map<String, String> data, String type);

    /**
     * Method to check whether an account is in the list
     *
     * @param identifier
     * @param type
     * @return
     */
    protected abstract String checkIdentifier(String identifier, String type);

    /**
     * Method to get the account key from keyserver
     *
     * @param identifier
     * @param type
     * @param sessionid
     * @return
     */
    protected abstract SecretKey getKey(String identifier, String type, String sessionid);

    /**
     * Method to generate TGT
     *
     * @param identifier
     * @param type
     * @param sessionid
     * @return
     */
    protected abstract String generateKDCTicket(String identifier, String type, String sessionid);
    
}
