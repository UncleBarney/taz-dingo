package com.tazdingo.platform.keymanager;

import com.tazdingo.core.util.ConstantUtil;
import com.tazdingo.core.util.Encryption;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.SecretKey;

import com.tazdingo.platform.KeyServerRequestAPI.KeyRequestAPI;
import com.tazdingo.platform.Platform;
import com.tazdingo.core.Request;
import com.tazdingo.core.Response;
import com.tazdingo.requestAPI.KDCAuthenticationAPI;
import com.tazdingo.requestAPI.TGSAuthorizationAPI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 *
 * @author congliu
 */
public class KeyManagerImpl implements IKeyManager {

    private final ConstructKey constructkey;
    private final Map<String, String> tickets = new HashMap<>();
    private final Map<String, String> identifierkeyserverlist = new HashMap<>();
    private final Map<String, String> keyservermap = new HashMap<>();
    @SuppressWarnings("unused")
	private final String defaultkeyserverurl;
    private final String defaultkeyservername;

    public KeyManagerImpl(String name, String password, String adminpassword, String defaultkeyserverurl, String defaultkeyservername) {
        constructkey = new ConstructKey(name, password, adminpassword);
        this.defaultkeyserverurl = defaultkeyserverurl;
        this.defaultkeyservername = defaultkeyservername;
        keyservermap.put(defaultkeyservername, defaultkeyserverurl);
    }

    @Override
    public SecretKey getSessionMasterKey() {
        return constructkey.getSessionMasterKey();
    }

    @Override
    public SecretKey getKeyServerMasterKey() {
        return constructkey.getKeyServerMasterKey();
    }

    @Override
    public SecretKey getKey(String identifier, String type, String sessionid) {
        String keyservername = identifierkeyserverlist.get(identifier);
        String ticket = tickets.get(keyservername);
        int i = 0;
        while (ticket == null && i < 5) {
            ticket = getNewTicket(keyservername);
            i++;
        }
        Request request = KeyRequestAPI.findKeyRequest(type, identifier, sessionid);
        request.getData().put(ConstantUtil.PLATFORM_NAME, constructkey.getPlatformname());
        request.getData().put(ConstantUtil.USERSSIONID, sessionid);
        request.setData(Encryption.encrypt(constructkey.getKeyServerMasterKey(), request.getData()));
        String keyserverurl = keyservermap.get(keyservername);
        request.getData().put(ConstantUtil.TICKET, ticket);
        System.out.println();
        System.out.println("Platform send request to keyserver ask for key");

        Response response = sendRequestToKeyServer(request, keyserverurl);
        System.out.println();
        System.out.println("Platform received response from KeyServer");
        SecretKey key = null;
        String error = response.getData().get(ConstantUtil.ERROR);
        if (error.equals(ConstantUtil.NO_ERROR)) {
            response.getData().remove(ConstantUtil.ERROR);
            String keystring = response.getData().get(ConstantUtil.KEY);
            String decryptedkey = Encryption.decrypt(constructkey.getKeyServerMasterKey(), keystring);
            key = Encryption.stringToKey(decryptedkey);
            System.out.println("platform get key from keyserver:" + key);
            System.out.println();
        }
        return key;
    }

    @Override
    public ConstructKey getConstructkey() {
        return constructkey;
    }

    @Override
    public void setTicket(String ticket) {
        setTicket(ticket, defaultkeyservername);
    }

    @Override
    public void setTicket(String ticket, String keyservername) {
        this.tickets.put(keyservername, ticket);
    }

    @Override
    public String getTicket() {
        return getTicket(defaultkeyservername);
    }

    @Override
    public String getTicket(String keyservername) {
        return tickets.get(keyservername);
    }

    @Override
    public void setKeyServerMasterKey(SecretKey key) {
        constructkey.setKeyServerMasterKey(key);
    }

    private Response sendRequestToKeyServer(Request request, String keyserverurl) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(keyserverurl);
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

    @Override
    public String getPlatformname() {
        return constructkey.getPlatformname();
    }

    @Override
    public Map<String, String> getKeyservermap() {
        return keyservermap;
    }

    @Override
    public void addkeyserver(String keyservername, String keyserverurl) {
        keyservermap.put(keyservername, keyserverurl);
    }

    @Override
    public void addIdentifier(String identifier, String keyservername) {
        identifierkeyserverlist.put(identifier, keyservername);
    }

    @Override
    public void addIdentifier(String identifier) {
        addIdentifier(identifier, defaultkeyservername);
    }

    @Override
    public String platformLogin(String keyservername) {
        String status = ConstantUtil.DEFAULT_ERROR;
        Request request = KDCAuthenticationAPI.createPlatformAuthenticationRequest(getPlatformname());
        System.out.println();
        System.out.println("Send platformrname to KeyServerKDC for authentication");
        Response response = sendRequestToKeyServer(request, keyservermap.get(keyservername));
        System.out.println();
        System.out.println("Recevied response from KeyServer");
        SecretKey key = constructkey.getPlatformMasterKey();
        Map<String, String> data = response.getData();
        String error = data.get(ConstantUtil.ERROR);
        String tempkey = null;
        if (error == null) {
            status = ConstantUtil.NO_RESPONSE;
        } else if (!error.isEmpty()) {
            if (error.equals(ConstantUtil.NO_ERROR)) {
                System.out.println("decrypted data with key:" + key);
                data.remove(ConstantUtil.ERROR);
                System.out.println("data1:" + data);
                data = Encryption.decrypt(key, data);
                System.out.println("data2:" + data);
                if (data.get(ConstantUtil.ERROR) != null && data.get(ConstantUtil.ERROR).equals(ConstantUtil.WRONDKEY)) {
                    status = ConstantUtil.INVALIDKEYSERVER;
                } else {
                    setTicket(data.get(ConstantUtil.TICKET), keyservername);
                    tempkey = data.get(ConstantUtil.STEPID);
                    System.out.println("platformkey:" + tempkey);
                    setKeyServerMasterKey(Encryption.stringToKey(tempkey));
                    status = ConstantUtil.SUCCESS_LOGIN;
                }
            } else {
                status = error;
            }
        }
        System.out.println(status);
        return status;
    }

    @Override
    public String platformAuthorization(String keyservername) {
        String status = ConstantUtil.DEFAULT_ERROR;
        SecretKey key = getKeyServerMasterKey();
        System.out.println("platformkey:" + key);
        Request request = TGSAuthorizationAPI.createPlatformAuthorizationRequest(getPlatformname());
        System.out.println();
        request = encryptRequest(request, key, getTicket());
        System.out.println("Platform send platformticket,platformname to TGS for authorization");

        Response response = sendRequestToKeyServer(request, keyservermap.get(keyservername));
        System.out.println();
        System.out.println("Platform received response from keyserver");
        Map<String, String> data = response.getData();
        String error = data.get(ConstantUtil.ERROR);
        if (error == null) {
            status = ConstantUtil.NO_RESPONSE;
        } else if (!error.isEmpty()) {
            if (error.equals(ConstantUtil.NO_ERROR)) {
                System.out.println("decrypted data with key:" + key);
                data.remove(ConstantUtil.ERROR);
                data = Encryption.decrypt(key, data);
                System.out.println("data" + data);
                if (data.containsKey(ConstantUtil.ERROR)) {
                    status = ConstantUtil.INVALIDKEYSERVER;
                } else {
                    //System.out.println(data.get(ConstantUtil.TICKET));
                    setTicket(data.get(ConstantUtil.TICKET), keyservername);
                    System.out.println(getTicket(keyservername));
                    System.out.println("Authorizate Successfully");
                    status = ConstantUtil.SUCCESS_GET_TICKET;
                }
            } else {
                status = error;
            }
        }
        return status;
    }

    private Request encryptRequest(Request request, SecretKey key, String ticket) {
        System.out.println("encrypted data with key:" + key);
        Map<String, String> data = request.getData();
        data = Encryption.encrypt(key, data);
        data.put(ConstantUtil.TGT, ticket);
        request.setData(data);
        return request;
    }

    private synchronized String getNewTicket(String keyservername) {
        String ticket = tickets.get(keyservername);
        if (ticket == null) {

            String response = platformLogin(keyservername);
            if (response.equals(ConstantUtil.SUCCESS_LOGIN)) {
                response = platformAuthorization(keyservername);
            }
            if (!response.equals(ConstantUtil.SUCCESS_GET_TICKET)) {
                tickets.remove(keyservername);
            }
        }
        return tickets.get(keyservername);
    }
    
}
