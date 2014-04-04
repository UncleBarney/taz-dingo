package com.tazdingo.platform.service;

import com.tazdingo.core.Service;
import com.tazdingo.core.util.ConstantUtil;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import com.tazdingo.core.Response;
import com.tazdingo.platform.ticket.TGT;
import com.tazdingo.platform.ticket.ServiceTicket;
import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.Encryption;

/**
 *
 * @author Cynthia
 */
public abstract class TGS extends Service {

    /**
     * Method to execute request and return response
     *
     * @param sessionid
     * @return
     */
    @Override
    public Response excuteRequest(String sessionid) {
        Response response = new Response();
        System.out.println("TGS received request");
        Map<String, String> responsedata = new HashMap<>();
        WorkRequest request = super.workqueue.pullWorkRequest(sessionid);
        Map<String, String> data = request.getRequest().getData();
        String ticketstring = data.get(ConstantUtil.TICKET);
        TGT ticket = new TGT();
        ticket.setTicket(ticketstring);
        String type = data.get(ConstantUtil.REQUEST_TYPE);
        String identifier = getIdentifier(data, type);
        if (checkTicket(ticket, identifier, sessionid)) {
            ServiceTicket tgt = generateTicket(getPrivilege(identifier, type), ticket, getStepid(request));
            SecretKey masterkey = getMasterKey();
            //System.out.println(tgt.converToString());
            System.out.println("encrypted ticket with key:" + masterkey);
            String encryptedtgt = Encryption.encrypt(masterkey, tgt.converToString());
            // System.out.println(encryptedtgt);
            responsedata.put(ConstantUtil.TICKET, encryptedtgt);
            responsedata = addAdditionalData(responsedata, request);
            SecretKey shortkey = getShortTermKey(request);
            System.out.println("encrypted data with key:" + shortkey);
            responsedata = Encryption.encrypt(shortkey, responsedata);
            responsedata.put(ConstantUtil.ERROR, ConstantUtil.NO_ERROR);
            System.out.println("Authorizate Successfully");
        } else {
            responsedata.put(ConstantUtil.ERROR, ConstantUtil.INVALID_KDC_TICKET);
        }
        response.setData(responsedata);
        return response;
    }

    /**
     * Method to add additional data to response
     *
     * @param data
     * @param request
     * @return
     */
    protected abstract Map<String, String> addAdditionalData(Map<String, String> data, WorkRequest request);

    /**
     * Method to get the masterkey
     *
     * @return
     */
    protected abstract SecretKey getMasterKey();

    /**
     * Method to get short term key /old stepid
     *
     * @param request
     * @return
     */
    protected abstract SecretKey getShortTermKey(WorkRequest request);

    /**
     * Method to get account identification
     *
     * @param data
     * @param type
     * @return
     */
    protected abstract String getIdentifier(Map<String, String> data, String type);

    /**
     * Method to get privilege of an account
     *
     * @param identifier
     * @param type
     * @return
     */
    protected abstract String getPrivilege(String identifier, String type);

    private boolean checkTicket(TGT ticket, String identifier, String sessionid) {
        if (ticket.getAuthenticator().equals(identifier)) {
            if (ticket.getSessionID().equals(sessionid)) {
                if (System.currentTimeMillis() - ticket.getExpiredTime() < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private ServiceTicket generateTicket(String Privilege, TGT ticket, String stepid) {
        return new ServiceTicket(Privilege, ticket, stepid);
    }

    /**
     * Method to get new stepid/new temporary key
     *
     * @param request
     * @return
     */
    protected abstract String getStepid(WorkRequest request);
    
}
