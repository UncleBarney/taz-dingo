package com.tazdingo.http;

/**
 *
 * @author Cynthia
 */
public interface IPlatform extends Gateway {

    /**
     * Method to block a device
     *
     * @param deviceid
     * @return
     */
    public String blockDevice(String deviceid);

    /**
     * Method to authenticate platform to default keyserver
     *
     * @param adminpassword
     * @return
     */
    public String platformLogin();

    /**
     * Method to login platform to any keyserver
     *
     * @param adminpassword
     * @param keyservername
     * @return
     */
    public String platformLogin(String keyservername);

    /**
     * Method to authorize platform to default keyserver
     *
     * @return
     */
    public String platformAuthorization();

    /**
     * Method to authorize platform to any keyserver
     *
     * @param keyservername
     * @return
     */
    public String platformAuthorization(String keyservername);

    /**
     * Method to add user to any keyserver
     *
     * @param username
     * @param privilege
     * @param keyservername
     */
    public void addUser(String username, String privilege, String keyservername);

    /**
     * Method to add user to default keyserver
     *
     * @param username
     * @param privilege
     */
    public void addUser(String username, String privilege);

    /**
     * Method to add device to any keyserver
     *
     * @param deviceid
     * @param privilege
     * @param keyservername
     */
    public void addDevice(String deviceid, String privilege, String keyservername);

    /**
     * Method to add service to any keyserver
     *
     * @param servicename
     * @param serviceurl
     * @param privilege
     * @param keyservername
     */
    public void addService(String servicename, String serviceurl, String privilege, String keyservername);

    /**
     * Method to add keyserver
     *
     * @param keyservername
     * @param keyserverurl
     * @param privilege
     */
    public void addKeyServer(String keyservername, String keyserverurl, String privilege);

    /**
     * Method to add service to default keyserver
     *
     * @param servicename
     * @param serviceurl
     * @param privilege
     */
    public void addService(String servicename, String serviceurl, String privilege);

    /**
     * Method to add device to default keyserver
     *
     * @param deviceid
     * @param privilege
     */
    public void addDevice(String deviceid, String privilege);
}
