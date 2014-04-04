package com.tazdingo.platform.service;

import com.tazdingo.core.util.ConstantUtil;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import com.tazdingo.platform.session.ISession;
import com.tazdingo.platform.keymanager.ConstructKey;
import com.tazdingo.platform.workrequest.PlatformWorkRequest;
import com.tazdingo.core.WorkRequest;
import com.tazdingo.core.util.Encryption;

/**
 *
 * @author Cynthia
 */
public class PlatformTGS extends TGS {

    private ConstructKey constructor;
    //private Map<String,String> keyserverPrivilegelist=new HashMap<>();
    private Map<String, String> servicePrivilegelist = new HashMap<>();
    private Map<String, String> userPrivilegelist = new HashMap<>();
    private Map<String, String> devicePrivilegelist = new HashMap<>();

    public PlatformTGS(ConstructKey constructor) {
        this.constructor = constructor;
    }

    /**
     * Method to return masterkey
     *
     * @return
     */
    @Override
    protected SecretKey getMasterKey() {
        return constructor.getSessionMasterKey();
    }

    /**
     * Method to get short term key/old stepid
     *
     * @param request
     * @return
     */
    @Override
    protected SecretKey getShortTermKey(WorkRequest request) {
        String key = ((PlatformWorkRequest) request).getPrestep();
        return Encryption.stringToKey(key);
    }

    /**
     * Method to get account identification
     * e.g.username,deviceid,serviceid,servicename
     *
     * @param data
     * @param type
     * @return
     */
    @Override
    protected String getIdentifier(Map<String, String> data, String type) {
        String identifier = ConstantUtil.DEFAULT_IDENTIFIER;
        if (type.equals(ConstantUtil.DEVICE_AUTHERIZATION_REQUEST)) {
            identifier = data.get(ConstantUtil.DEVICEID);
        }
        if (type.equals(ConstantUtil.USER_AUTHERIZATION_REQUEST)) {
            String deviceid = data.get(ConstantUtil.DEVICEID);
            identifier = data.get(ConstantUtil.USERNAME) + ConstantUtil.FROM + deviceid;
        }
        if (type.equals(ConstantUtil.SERVICE_AUTHERIZATION_REQUEST)) {
            identifier = data.get(ConstantUtil.SERVICE_NAME);
        }
        return identifier;
    }

    /**
     * Method to get account privilege
     *
     * @param identifier
     * @param type
     * @return
     */
    @Override
    protected String getPrivilege(String identifier, String type) {
        String Privilege = ConstantUtil.DEFAULT_PRIVILEDGE;
        if (type.equals(ConstantUtil.DEVICE_AUTHERIZATION_REQUEST)) {
            Privilege = devicePrivilegelist.get(identifier);

        }
        if (type.equals(ConstantUtil.USER_AUTHERIZATION_REQUEST)) {
            int i = identifier.indexOf(ConstantUtil.FROM);
            String username = identifier.substring(0, i);
            Privilege = userPrivilegelist.get(username);

        }
        if (type.equals(ConstantUtil.SERVICE_AUTHERIZATION_REQUEST)) {
            Privilege = servicePrivilegelist.get(identifier);
        }
        return Privilege;
    }

    /**
     * Method to add additional data to response
     *
     * @param data
     * @param request
     * @return
     */
    @Override
    protected Map<String, String> addAdditionalData(Map<String, String> data, WorkRequest request) {
        ISession session = ((PlatformWorkRequest) request).getSession();
        data.put(ConstantUtil.STEPID, session.getStepID());
        return data;
    }

    /**
     * Method to add user
     *
     * @param username
     * @param Privilege
     */
    public void addUser(String username, String Privilege) {
        userPrivilegelist.put(username, Privilege);
    }

    /**
     * Method to add device
     *
     * @param devicename
     * @param Privilege
     */
    public void addDevice(String devicename, String Privilege) {
        devicePrivilegelist.put(devicename, Privilege);
    }

    /**
     * Method to add service
     *
     * @param servicename
     * @param Privilege
     */
    public void addService(String servicename, String Privilege) {
        servicePrivilegelist.put(servicename, Privilege);
    }

    /**
     * Method to delete user
     *
     * @param username
     */
    public void deleteUser(String username) {
        userPrivilegelist.remove(username);
    }

    /**
     * Method to delete device
     *
     * @param devicename
     */
    public void deleteDevice(String devicename) {
        devicePrivilegelist.remove(devicename);
    }

    /**
     * Method to delete service
     *
     * @param servicename
     */
    public void deleteService(String servicename) {
        servicePrivilegelist.remove(servicename);
    }

    /**
     * Method to get new stepid
     *
     * @param request
     * @return
     */
    @Override
    protected String getStepid(WorkRequest request) {
        return ((PlatformWorkRequest) request).getSession().getStepID();
    }

    private Map<String, String> getList(String type) {
        if (type.equals(ConstantUtil.DEVICE)) {
            return devicePrivilegelist;
        }
        if (type.equals(ConstantUtil.USER)) {
            return userPrivilegelist;
        }
        if (type.equals(ConstantUtil.SERVICE) || type.equals(ConstantUtil.KEYSERVER)) {
            return servicePrivilegelist;
        }
        return null;
    }

    /**
     * Method to add account
     *
     * @param type
     * @param accountname
     * @param Privilege
     */
    public void addAccount(String type, String accountname, String Privilege) {
        getList(type).put(accountname, Privilege);
    }

    /**
     * Method to delete an account
     *
     * @param type
     * @param accountname
     */
    public void removeAccount(String type, String accountname) {
        getList(type).remove(accountname);
    }
}
