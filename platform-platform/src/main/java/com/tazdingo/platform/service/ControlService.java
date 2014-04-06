package com.tazdingo.platform.service;

import com.tazdingo.core.Service;
import com.tazdingo.core.util.ConstantUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import com.tazdingo.platform.Platform;
import com.tazdingo.core.Request;
import com.tazdingo.core.Response;
import com.tazdingo.platform.keymanager.IKeyManager;
import com.tazdingo.ticket.ServiceTicket;
import com.tazdingo.platform.workrequest.PlatformWorkRequest;
import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.Encryption;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author Cynthia
 */
public class ControlService extends Service {

    private final IKeyManager keymanager;
    private final Map<String, String> servicemap = new HashMap<>();

    public ControlService(IKeyManager keymanager) {
        this.keymanager = keymanager;
    }

    /**
     * Method to add a service mapping
     *
     * @param servicename
     * @param serviceurl
     */
    public void addService(String servicename, String serviceurl) {
        servicemap.put(servicename, serviceurl);
    }

    /**
     * Method to execute request and return response
     *
     * @param sessionid
     * @return
     */
    @Override
    public Response excuteRequest(String sessionid) {
        Response response = new Response();
        System.out.println("control service received request");
        Map<String, String> responsedata = response.getData();
        WorkRequest request = super.workqueue.pullWorkRequest(sessionid);
        Map<String, String> data = request.getRequest().getData();
        String ticketstring = data.get(ConstantUtil.TICKET);
        ServiceTicket ticket = new ServiceTicket(ticketstring);
        String type = data.get(ConstantUtil.REQUEST_TYPE);
        String author = getAuthor(data, type);
        if (checkTicket(author, ticket, sessionid)) {
            String servicename = data.get(ConstantUtil.SERVICE_NAME);
            SecretKey serviceKey = keymanager.getKey(servicename, ConstantUtil.SERVICE, sessionid);
            if (serviceKey != null) {
                switch (type) {
                    case ConstantUtil.USER_CONNECT_SERVICE_REQUEST: {

                        System.out.println("encrypted ticket with key:" + serviceKey);
                        String encryptedTicket = Encryption.encrypt(serviceKey, ticket.converToString());
                        data.remove(ConstantUtil.TICKET);
                        SecretKey shortKey = Encryption.stringToKey(((PlatformWorkRequest) request).getPrestep());
                        System.out.println("encrypted data with key:" + shortKey);
                        data = Encryption.encrypt(shortKey, data);
                        data.put(ConstantUtil.TICKET, encryptedTicket);
                        request.getRequest().setData(data);
                        System.out.println("ControlService Redirected request to service");

                        String serviceurl = servicemap.get(servicename);
                        response = sendRequestToServiceServer(request.getRequest(), serviceurl);
                        String newstepid = ((PlatformWorkRequest) request).getSession().getStepID();
                        response.getData().put(ConstantUtil.STEPID, Encryption.encrypt(shortKey, newstepid));
                        ticket.renewTicket(newstepid);
                        response.getData().put(ConstantUtil.TICKET, Encryption.encrypt(shortKey, Encryption.encrypt(keymanager.getSessionMasterKey(), ticket.converToString())));
                        System.out.println("ControlService reveiced response from service and send it back to device");

                        break;
                    }
                    case ConstantUtil.SERVICE_KEY_REQUEST: {

                        SecretKey shortKey = Encryption.stringToKey(((PlatformWorkRequest) request).getPrestep());
                        responsedata.put(ConstantUtil.STEPID, ((PlatformWorkRequest) request).getSession().getStepID());
                        responsedata.put(ConstantUtil.KEY, Encryption.keyToString(serviceKey));
                        System.out.println("encrypted data with key:" + shortKey);
                        responsedata = Encryption.encrypt(shortKey, responsedata);
                        responsedata.put(ConstantUtil.ERROR, ConstantUtil.NO_ERROR);
                        response.setData(responsedata);
                        break;
                    }
                }
            } else {
                response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDSERVICE);
            }
        } else {
            response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDTICKET);
        }
        return response;
    }

    private Response sendRequestToServiceServer(Request request, String serviceurl) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(serviceurl);
        Response response = new Response();
        try {
            post.setEntity(new UrlEncodedFormEntity(request.getParameterPair()));
            HttpResponse httpresponse = client.execute(post);
            response = new Response(httpresponse);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
            return new Response();
        } catch (IOException ex) {
            Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
            return new Response();
        }
        return response;
    }

    private String getAuthor(Map<String, String> data, String type) {
        String author = ConstantUtil.DEFAULT_IDENTIFIER;
        switch (type) {
            case ConstantUtil.USER_CONNECT_SERVICE_REQUEST:
                String username = data.get(ConstantUtil.USERNAME);
                String deviceid = data.get(ConstantUtil.DEVICEID);
                author = username + ConstantUtil.FROM + deviceid;
                break;
            case ConstantUtil.SERVICE_KEY_REQUEST:
                author = data.get(ConstantUtil.SERVICE_NAME);
                break;
        }
        return author;
    }

    private boolean checkTicket(String author, ServiceTicket ticket, String sessionid) {
        if ((System.currentTimeMillis() - ticket.getExpiredtime()) < 0) {
            if (ticket.getKdcticket().getAuthenticator().equals(author)) {
                if (ticket.getKdcticket().getSessionID().equals(sessionid)) {
                    if (ticket.getPriviledge().contains(ConstantUtil.CONNECT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
