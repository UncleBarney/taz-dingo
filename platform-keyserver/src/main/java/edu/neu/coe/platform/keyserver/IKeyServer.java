/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.neu.coe.platform.keyserver;

import edu.neu.coe.platform.service.serviceserver.GeneralServer.IServer;

/**
 *
 * @author Cynthia
 */
public interface IKeyServer extends IServer{
    /**
     * Method to delete user
     * @param username
     * @return 
     */
     public String deleteUser(String username);
     /**
      * Method to delete device
      * @param deviceid
      * @return 
      */
     public String deleteDevice(String deviceid);
      /**
       * Method to delete service
       * @param servicename
       * @return 
       */
       public String deleteService(String servicename);
      
      /**
       * Method to add platform
       * @param platformname
       * @param platformpassword
       * @param Privilege 
       */
      public void addPlatform(String platformname,String platformpassword,String Privilege);
      /**
       * Method to save keystores;
       * @param adminpassword 
       */
      public void saveKeyStore(String adminpassword); 
      /**
       * Method to add or update service key
       * @param servicename
       * @return 
       */
     public String addOrUpdateServiceKey(String servicename);
    /**
     * Method to add or update user
     * @param username
     * @param password
     * @return 
     */
    public String addOrUpdateUser(String username,String password);
    /**
     * Method to add or update device
     * @param deviceid
     * @param password
     * @return 
     */
    public String addOrUpdataDevice(String deviceid,String password);
    /**
     * Method to add or update service
     * @param servicename
     * @param password
     * @return 
     */
    public String addOrUpdateService(String servicename,String password);
    
}
