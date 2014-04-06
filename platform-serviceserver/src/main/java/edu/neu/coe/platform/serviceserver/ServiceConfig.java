/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.neu.coe.platform.serviceserver;

/**
 *
 * @author Cynthia
 */
public class ServiceConfig extends AbstractServiceConfig{

    public ServiceConfig() {
    }
    
    

    @Override
    protected ServiceServer newServiceServer(String servicename, String password, String adminpassword, String platformurl) {
       return new ServiceServer(servicename, password, adminpassword, platformurl);
    }
    
}
