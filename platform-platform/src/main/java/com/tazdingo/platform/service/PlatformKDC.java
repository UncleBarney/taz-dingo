package com.tazdingo.platform.service;

import com.tazdingo.kerberos.KDC;
import com.tazdingo.core.util.ConstantUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import com.tazdingo.platform.keymanager.IKeyManager;
import com.tazdingo.platform.ticket.DeviceTGT;
import com.tazdingo.ticket.TGT;
import com.tazdingo.platform.ticket.ServiceTGT;
import com.tazdingo.ticket.ServiceTicket;
import com.tazdingo.platform.ticket.UseTGT;
import com.tazdingo.platform.workrequest.PlatformWorkRequest;
import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.Encryption;

/**
 *
 * @author Cynthia
 */
public class PlatformKDC extends KDC {

    protected IKeyManager keymanager;
    private List<String> keyserverlist = new ArrayList<>();
    private List<String> servicerlist = new ArrayList<>();
    private List<String> usernamelist = new ArrayList<>();
    private List<String> devicelist = new ArrayList<>();

    public PlatformKDC(IKeyManager keymanager) {
        this.keymanager = keymanager;
    }

    /**
     * Method to get key from keyserver
     *
     * @param identifier
     * @param type
     * @param sessionid
     * @return
     */
    @Override
    protected SecretKey getKey(String identifier, String type, String sessionid) {
        SecretKey key = null;
        if (type.equals(ConstantUtil.DEVICE_AUTHENTICATE_REQUEST)) {
            key = keymanager.getKey(identifier, ConstantUtil.DEVICE, sessionid);
        }
        if (type.equals(ConstantUtil.USER_AUTHENTICATE_REQUEST)) {
            int i = identifier.indexOf(ConstantUtil.DELIMITER);
            key = keymanager.getKey(identifier.substring(0, i), ConstantUtil.USER, sessionid);
        }
        if (type.equals(ConstantUtil.SERVICE_AUTHENTICATE_REQUEST)) {
            key = keymanager.getKey(identifier, ConstantUtil.SERVICEPASSWORD, sessionid);
        }
        if (type.equals(ConstantUtil.KEYSERVER_AUTHENTICATE_REQUEST)) {
            key = keymanager.getKeyServerMasterKey();
        }
        return key;
    }

    /**
     * Method to get account identification (e.g.
     * username,deviceid,servicename,keyservername) request data
     *
     * @param data
     * @param type
     * @return
     */
    @Override
    protected String getIdentifier(Map<String, String> data, String type) {
        String identifier = ConstantUtil.DEFAULT_IDENTIFIER;
        if (type.equals(ConstantUtil.DEVICE_AUTHENTICATE_REQUEST)) {
            identifier = data.get(ConstantUtil.DEVICEID);
        }
        if (type.equals(ConstantUtil.USER_AUTHENTICATE_REQUEST)) {
            String deviceid = data.get(ConstantUtil.DEVICEID);
            String ticket = data.get(ConstantUtil.DEVICE_TICKET);
            String decryptedticket = Encryption.decrypt(keymanager.getSessionMasterKey(), ticket);
            System.out.println(ticket + " ::: " + decryptedticket);
            if (!decryptedticket.equals(ConstantUtil.WRONDKEY)) {
                ServiceTicket tgt = new ServiceTicket(decryptedticket);
                if (tgt.getPriviledge().contains(ConstantUtil.CONNECT)) {
                    TGT t = tgt.getKdcticket();
                    if (t.getAuthenticator().equals(deviceid)) {
                        identifier = data.get(ConstantUtil.USERNAME) + ConstantUtil.DELIMITER + deviceid;
                    } else {
                        identifier = ConstantUtil.INVALID_DEVICE_TICKET;
                    }
                } else {
                    identifier = ConstantUtil.INVALID_DEVICE_TICKET;
                }
            } else {
                identifier = ConstantUtil.INVALID_DEVICE_TICKET;
            }
        }
        if (type.equals(ConstantUtil.SERVICE_AUTHENTICATE_REQUEST)) {
            identifier = data.get(ConstantUtil.SERVICE_NAME);
        }
        if (type.equals(ConstantUtil.KEYSERVER_AUTHENTICATE_REQUEST)) {
            identifier = data.get(ConstantUtil.KEYSERVER_NAME);
        }
        return identifier;
    }

    /**
     * Method to check whether an account is in the list
     *
     * @param identifier
     * @param type
     * @return
     */
    @Override
    protected String checkIdentifier(String identifier, String type) {
        String checker = ConstantUtil.DEFAULT_ERROR;
        if (identifier.equals(ConstantUtil.INVALID_DEVICE_TICKET)) {
            checker = identifier;
        } else if (type.equals(ConstantUtil.DEVICE_AUTHENTICATE_REQUEST)) {
            if (devicelist.contains(identifier)) {
                checker = ConstantUtil.NO_ERROR;
            } else {
                checker = ConstantUtil.INVALIDDEVICE;
            }
        }
        if (type.equals(ConstantUtil.USER_AUTHENTICATE_REQUEST)) {
            String[] value = identifier.split(ConstantUtil.DELIMITER);
            String username = value[0];
            //String deviceid=value[1];
            if (usernamelist.contains(username)) {
                checker = ConstantUtil.NO_ERROR;
            } else {
                checker = ConstantUtil.INVALIDUSERNAME;
            }
        }
        if (type.equals(ConstantUtil.SERVICE_AUTHENTICATE_REQUEST)) {
            if (servicerlist.contains(identifier)) {
                checker = ConstantUtil.NO_ERROR;
            } else {
                checker = ConstantUtil.INVALIDSERVICE;
            }
        }
        if (type.equals(ConstantUtil.KEYSERVER_AUTHENTICATE_REQUEST)) {
            if (keyserverlist.contains(identifier)) {
                checker = ConstantUtil.NO_ERROR;
            } else {
                checker = ConstantUtil.INVALIDKEYSERVER;
            }
        }
        return checker;
    }

    /**
     * Method to generate TGT
     *
     * @param identifier
     * @param type
     * @param sessionid
     * @return
     */
    @Override
    protected String generateKDCTicket(String identifier, String type, String sessionid) {
        TGT ticket = new TGT();
        if (type.equals(ConstantUtil.DEVICE_AUTHENTICATE_REQUEST)) {
            ticket = new DeviceTGT(sessionid, identifier);
        }
        if (type.equals(ConstantUtil.USER_AUTHENTICATE_REQUEST)) {
            String[] value = identifier.split(ConstantUtil.DELIMITER);
            String username = value[0];
            String deviceid = value[1];
            ticket = new UseTGT(sessionid, deviceid, username);
        }
        if (type.equals(ConstantUtil.SERVICE_AUTHENTICATE_REQUEST)) {
            ticket = new ServiceTGT(sessionid, identifier);
        }
        if (type.equals(ConstantUtil.KEYSERVER_AUTHENTICATE_REQUEST)) {
            ticket = new ServiceTGT(sessionid, identifier);
        }
        System.out.println("encrypted ticket with key:" + keymanager.getSessionMasterKey());
        String encryptedTicket = Encryption.encrypt(keymanager.getSessionMasterKey(), ticket.converToString());
        return encryptedTicket;
    }

    /**
     * Method to add user
     *
     * @param username
     */
    public void addUser(String username) {
        usernamelist.add(username);
    }

    /**
     * Method to add device
     *
     * @param devicename
     */
    public void addDevice(String devicename) {
        devicelist.add(devicename);
    }

    /**
     * Method to add keyserver
     *
     * @param keyservername
     */
    public void addKeyServer(String keyservername) {
        keyserverlist.add(keyservername);
    }

    /**
     * Method to add service
     *
     * @param servicename
     */
    public void addService(String servicename) {
        servicerlist.add(servicename);
    }

    /**
     * Method to delete a user
     *
     * @param username
     */
    public void deleteUser(String username) {
        usernamelist.remove(username);
    }

    /**
     * Method to delete a device
     *
     * @param devicename
     */
    public void deleteDevice(String devicename) {
        devicelist.remove(devicename);
    }

    /**
     * Method to delete a keyserver
     *
     * @param keyservername
     */
    public void deleteKeyServer(String keyservername) {
        keyserverlist.remove(keyservername);
    }

    /**
     * Method to delete service
     *
     * @param servicename
     */
    public void deleteService(String servicename) {
        servicerlist.remove(servicename);
    }

    private List<String> getList(String type) {
        if (type.equals(ConstantUtil.DEVICE)) {
            return devicelist;
        }
        if (type.equals(ConstantUtil.USER)) {
            return usernamelist;
        }
        if (type.equals(ConstantUtil.KEYSERVER)) {
            return keyserverlist;
        }
        if (type.equals(ConstantUtil.SERVICE)) {
            return servicerlist;
        }
        return null;
    }

    /**
     * Method to add an account
     *
     * @param type
     * @param accountname
     */
    public void addAccount(String type, String accountname) {
        getList(type).add(accountname);
    }

    /**
     * Method to remove an account
     *
     * @param type
     * @param accountname
     */
    public void removeAccount(String type, String accountname) {
        getList(type).remove(accountname);
    }

    /**
     * Method to get the temporary key/stepid
     *
     * @param request
     * @return
     */
    @Override
    protected String getTempKey(WorkRequest request) {
        return ((PlatformWorkRequest) request).getSession().getStepID();
    }

    /**
     * Method to return devicelist
     *
     * @return
     */
    public List<String> getDevicelist() {
        return devicelist;
    }
    
}
