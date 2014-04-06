package com.tazdingo.platform;

import com.tazdingo.http.IPlatform;
import com.tazdingo.core.Response;
import com.tazdingo.core.Request;
import com.tazdingo.core.WorkQueue;
import com.tazdingo.core.util.ConstantUtil;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.tazdingo.platform.session.DeviceSession;
import com.tazdingo.platform.session.ISession;
import com.tazdingo.platform.session.ISessionManager;
import com.tazdingo.platform.session.SessionManagerImpl;
import com.tazdingo.platform.session.UserSession;
import com.tazdingo.platform.service.ControlService;
import com.tazdingo.core.IService;
import com.tazdingo.core.util.Encryption;
import static com.tazdingo.platform.AbstractPlatformConfig.getPlatformConfig;
import com.tazdingo.platform.service.PlatformKDC;
import com.tazdingo.platform.service.PlatformTGS;
import com.tazdingo.platform.session.ServiceSession;
import com.tazdingo.platform.devicemanager.DeviceManagerImpl;
import com.tazdingo.platform.devicemanager.IDeviceManager;
import com.tazdingo.platform.keymanager.IKeyManager;
import com.tazdingo.platform.keymanager.KeyManagerImpl;
import com.tazdingo.platform.workrequest.PlatformWorkRequest;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author Cynthia
 */
public class Platform implements IPlatform {

    private final WorkQueue workqueue;
    private final ISessionManager sessionmanager = new SessionManagerImpl();
    private final IService kdc;
    private final IService tgs;
    private final IService controlservice;
    private final IKeyManager keymanager;
    private final IDeviceManager deviceManager;
    private final String defaultkeyserverurl;
    private final String defaultkeyservername;

    public Platform(String platformname, String password, String adminpassword, String defaultkeyserverurl, String keyservername) {
        workqueue = new WorkQueue();
        keymanager = new KeyManagerImpl(platformname, password, adminpassword, defaultkeyserverurl, keyservername);
        controlservice = new ControlService(keymanager);
        kdc = new PlatformKDC(keymanager);
        tgs = new PlatformTGS(keymanager.getConstructkey());
        deviceManager = new DeviceManagerImpl();
        this.defaultkeyserverurl = defaultkeyserverurl;
        this.defaultkeyservername = keyservername;
        addKeyServer(keyservername, defaultkeyserverurl, "");
    }

    /**
     * Method to take request and return response
     *
     * @param request
     * @return
     */
    public Response takeRequest(Request request) {
        Response response = new Response();
        Map<String, String> data = request.getData();
        String ticket = data.get(ConstantUtil.TICKET);
        ISession session = null;
        IService subservice = null;
        //Create Session 
        String prestep = null;
        if (ticket == null) {
            String type = data.get(ConstantUtil.REQUEST_TYPE);
            switch (type) {
                case ConstantUtil.DEVICE_AUTHENTICATE_REQUEST: {
                    String deviceid = data.get(ConstantUtil.DEVICEID);
                    if (deviceManager.isBlock(deviceid)) {
                        response = deviceManager.generateBlockedDeviceResponse();
                        break;
                    }
                    session = new DeviceSession(deviceid);
                    subservice = kdc;
                    break;
                }
                case ConstantUtil.USER_AUTHENTICATE_REQUEST: {
                    String username = data.get(ConstantUtil.USERNAME);
                    String deviceid = data.get(ConstantUtil.DEVICEID);
                    if (deviceManager.isBlock(deviceid)) {
                        response = deviceManager.generateBlockedDeviceResponse();
                        break;
                    }
                    session = new UserSession(username, deviceid);
                    subservice = kdc;
                    break;
                }

            }
            if (type.equals(ConstantUtil.SERVICE_AUTHENTICATE_REQUEST)) {
                String servicename = data.get(ConstantUtil.SERVICE_AUTHENTICATE_REQUEST);
                session = new ServiceSession(servicename);
                subservice = kdc;
            }
            if (type.equals(ConstantUtil.KEYSERVER_AUTHENTICATE_REQUEST)) {
                String keyservername = data.get(ConstantUtil.KEYSERVER_NAME);
                session = new ServiceSession(keyservername);
                subservice = kdc;
            }
            if (session != null) {
                sessionmanager.addORUpdateSession(session);
            }
        } //Decrypt Ticket and Get Session
        else {
            System.out.println("decrepted ticket with key:" + keymanager.getSessionMasterKey());
            String decryptedticket = Encryption.decrypt(keymanager.getSessionMasterKey(), ticket);
            if (decryptedticket.equals(ConstantUtil.WRONDKEY)) {
                response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDTICKET);
            } else {
                String tickettype = data.get(ConstantUtil.TICKET_TYPE);
                int i = decryptedticket.indexOf(ConstantUtil.DELIMITER);
                String sessionid = decryptedticket.substring(0, i);
                session = sessionmanager.getSession(sessionid);
                if (sessionmanager.validateSession(session)) {
                    switch (tickettype) {
                        case ConstantUtil.SERVICEICKET:
                            subservice = controlservice;
                            break;
                        case ConstantUtil.TGT:
                            subservice = tgs;
                            break;
                    }
                    data.remove(ConstantUtil.TICKET);
                    data.remove(ConstantUtil.TICKET_TYPE);
                    SecretKey key = Encryption.stringToKey(session.getStepID());
                    System.out.println("decrpted data with key:" + key);
                    data = Encryption.decrypt(key, data);
                    if (data.containsKey(ConstantUtil.ERROR)) {
                        response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDREQUEST);
                    } else {
                        data.put(ConstantUtil.TICKET, decryptedticket);
                        request.setData(data);
                        prestep = session.getStepID();
                        session.extendExpiredTime();
                    }
                } else {
                    response.getData().put(ConstantUtil.ERROR, ConstantUtil.SESSION_TIME_OUT);
                }
            }
        }
        if (session != null) {
            System.out.println("Platform receive request and dispatch to next service");

            sessionmanager.addORUpdateSession(session);
            PlatformWorkRequest workrequest = new PlatformWorkRequest(session, request, prestep);
            workqueue.addWorkRequest(session.getSessionID(), workrequest);
            if (subservice != null) {
                response = subservice.takeRequest((PlatformWorkRequest) workqueue.pullWorkRequest(session.getSessionID()));
            }

            // Map<String,String> responseData=response.getData();
            // responseData.put(ConstantUtil.SESSIONID, session.getSessionID());
            //responseData.put(ConstantUtil.STEPID,session.getStepID());
            //responseData.put(ConstantUtil.ERROR, ConstantUtil.NO_ERROR);
            //responseData=Encryption.encrypt(key, responseData);
        } else {
            response.getData().put(ConstantUtil.ERROR, ConstantUtil.INVALIDSESSION);
        }
        return response;
    }

    /**
     * Method to block device
     *
     * @param deviceid
     * @return
     */
    @Override
    public String blockDevice(String deviceid) {
        String result = deviceManager.blockDevice(deviceid, ((PlatformKDC) kdc).getDevicelist());
        return result;
    }

    /**
     * Method to login platform to default keyserver
     *
     * @return
     */
    @Override
    public String platformLogin() {
        return platformLogin(defaultkeyservername);
    }

    /**
     * Method to login platform to any keyserver
     *
     * @param keyservername
     * @return
     */
    @Override
    public String platformLogin(String keyservername) {
        return keymanager.platformLogin(keyservername);
    }

    /**
     * Method to authorize platform to default keyserver
     *
     * @return
     */
    @Override
    public String platformAuthorization() {
        return platformAuthorization(defaultkeyservername);
    }

    /**
     * Method to authorize platform to any keyserver
     *
     * @param keyservername
     * @return
     */
    @Override
    public String platformAuthorization(String keyservername) {
        return keymanager.platformAuthorization(keyservername);
    }

    /**
     * method to add account to any keyserver
     *
     * @param accountname
     * @param type
     * @param privilege
     * @param keyservername
     */
    public void addAccount(String accountname, String type, String privilege, String keyservername) {
        ((PlatformKDC) kdc).addAccount(type, accountname);
        ((PlatformTGS) tgs).addAccount(type, accountname, privilege + ConstantUtil.DELIMITER + ConstantUtil.CONNECT);
        keymanager.addIdentifier(accountname, keyservername);
    }

    /**
     * Method to add user to any keyserver
     *
     * @param username
     * @param privilege
     * @param keyservername
     */
    @Override
    public void addUser(String username, String privilege, String keyservername) {
        addAccount(username, ConstantUtil.USER, privilege, keyservername);
        boolean change = false;
        boolean exist = false;
        XMLConfiguration config=getPlatformConfig();
         List<HierarchicalConfiguration> user = config.configurationsAt("user");
            for(HierarchicalConfiguration sub : user){
                 String name = sub.getString("name");
                 String oldprivilege = sub.getString("privilege");
                 String oldkeyservername = sub.getString("keyservername");
                 if(name.equals(username)){
                     exist=true;
                     if(!oldprivilege.equals(privilege)){
                         sub.setProperty("privilege", privilege);
                         change=true;
                     }
                     if(!oldkeyservername.equals(keyservername)){
                         sub.setProperty("keyservername", keyservername);
                         change=true;
                     }
                     break;
                 }
            }
       
        if (!exist) {
            config.addProperty("user(-1).name", username);
            config.addProperty("user.privilege", privilege);
            config.addProperty("user.keyservername", keyservername); 
            change = true;
        }
        if (change) {
            try {
                config.save();
            } catch (ConfigurationException ex) {
                Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Method to add user to default keyserver
     *
     * @param username
     * @param privilege
     */
    @Override
    public void addUser(String username, String privilege) {
        addUser(username, privilege, defaultkeyservername);
    }

    /**
     * Method to add device to any keyserver
     *
     * @param deviceid
     * @param privilege
     * @param keyservername
     */
    @Override
    public void addDevice(String deviceid, String privilege, String keyservername) {
        addAccount(deviceid, ConstantUtil.DEVICE, privilege, keyservername);
        boolean change = false;
        boolean exist = false;
        XMLConfiguration config=getPlatformConfig();
         List<HierarchicalConfiguration> device = config.configurationsAt("device");
            for(HierarchicalConfiguration sub : device){
                 String id = sub.getString("id");
                 String oldprivilege = sub.getString("privilege");
                 String oldkeyservername = sub.getString("keyservername");
                 if(id.equals(deviceid)){
                     exist=true;
                     if(!oldprivilege.equals(privilege)){
                         sub.setProperty("privilege", privilege);
                         change=true;
                     }
                     if(!oldkeyservername.equals(keyservername)){
                         sub.setProperty("keyservername", keyservername);
                         change=true;
                     }
                     break;
                 }
            }
       
        if (!exist) {
            config.addProperty("device(-1).id", deviceid);
            config.addProperty("device.privilege", privilege);
            config.addProperty("device.keyservername", keyservername); 
            change = true;
        }
        if (change) {
            try {
                config.save();
            } catch (ConfigurationException ex) {
                Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    /**
     * Method to add device to default keyserver
     *
     * @param deviceid
     * @param privilege
     */
    @Override
    public void addDevice(String deviceid, String privilege) {
        addDevice(deviceid, privilege, defaultkeyservername);
    }

    /**
     * Method to add service to any keyserver
     *
     * @param servicename
     * @param serviceurl
     * @param privilege
     * @param keyservername
     */
    @Override
    public void addService(String servicename, String serviceurl, String privilege, String keyservername) {
        addAccount(servicename, ConstantUtil.SERVICE, privilege, keyservername);
        addServiceURL(servicename, serviceurl);
        boolean change = false;
        boolean exist = false;
        XMLConfiguration config=getPlatformConfig();
         List<HierarchicalConfiguration> service = config.configurationsAt("service");
            for(HierarchicalConfiguration sub : service){
                 String name = sub.getString("name");
                 String oldprivilege = sub.getString("privilege");
                 String oldkeyservername = sub.getString("keyservername");
                 String oldurl=sub.getString("url");
                 if(name.equals(servicename)){
                     exist=true;
                     if(!oldprivilege.equals(privilege)){
                         sub.setProperty("privilege", privilege);
                         change=true;
                     }
                     if(!oldkeyservername.equals(keyservername)){
                         sub.setProperty("keyservername", keyservername);
                         change=true;
                     }
                     if(!oldurl.equals(serviceurl)){
                         sub.setProperty("url", serviceurl);
                     }
                     break;
                 }
            }
       
        if (!exist) {
            config.addProperty("service(-1).name", servicename);
            config.addProperty("service.privilege", privilege);
            config.addProperty("service.keyservername", keyservername);
            config.addProperty("service.url", serviceurl);
            change = true;
        }
        if (change) {
            try {
                config.save();
            } catch (ConfigurationException ex) {
                Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }

    /**
     * Method to add service to default keyserver
     *
     * @param servicename
     * @param serviceurl
     * @param privilege
     */
    @Override
    public void addService(String servicename, String serviceurl, String privilege) {
        addService(servicename, serviceurl, privilege, defaultkeyservername);
    }

    /**
     * Method to add keyserver
     *
     * @param keyservername
     * @param keyserverurl
     * @param privilege
     */
    @Override
    public void addKeyServer(String keyservername, String keyserverurl, String privilege) {
        ((PlatformKDC) kdc).addAccount(ConstantUtil.KEYSERVER, keyservername);
        ((PlatformTGS) tgs).addAccount(ConstantUtil.KEYSERVER, keyservername, privilege);
        keymanager.addkeyserver(keyservername, keyserverurl);
        boolean change = false;
        boolean exist = false;
        XMLConfiguration config=getPlatformConfig();
         List<HierarchicalConfiguration> keyserver = config.configurationsAt("keyserver");
            for(HierarchicalConfiguration sub : keyserver){
                 String name = sub.getString("name");
                 String oldprivilege = sub.getString("privilege");
                 String oldurl = sub.getString("url");
                 if(name.equals(keyservername)){
                     exist=true;
                     if(!oldprivilege.equals(privilege)){
                         sub.setProperty("privilege", privilege);
                         change=true;
                     }
                     if(!oldurl.equals(keyserverurl)){
                         sub.setProperty("url", keyserverurl);
                         change=true;
                     }
                     break;
                 }
            }
       
        if (!exist) {
            config.addProperty("keyserver(-1).name", keyservername);
            config.addProperty("keyserver.privilege", privilege);
            config.addProperty("keyserver.url", keyserverurl); 
            change = true;
        }
        if (change) {
            try {
                config.save();
            } catch (ConfigurationException ex) {
                Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        

    }

    private void addServiceURL(String servicename, String serviceurl) {
        ((ControlService) controlservice).addService(servicename, serviceurl);
    }

    @Override
    public HttpServletResponse takeRequest(HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        Request request = new Request(httprequest);
        Response response = takeRequest(request);
        return response.convertToHttpResponse(httpresponse);
    }

}
